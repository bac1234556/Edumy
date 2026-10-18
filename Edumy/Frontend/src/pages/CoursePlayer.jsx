import React, { useState, useEffect, useContext, useRef, useMemo } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { 
  PlayCircle, 
  CheckCircle, 
  Circle, 
  ArrowLeft, 
  HelpCircle, 
  ChevronLeft, 
  ChevronRight, 
  ChevronDown, 
  ChevronUp, 
  Lock, 
  Award, 
  Menu, 
  X, 
  Clock, 
  FileText, 
  Download, 
  StickyNote, 
  BookOpen, 
  FileCode, 
  FileArchive, 
  Plus, 
  Trash2, 
  Check, 
  Inbox,
  Share2,
  Paperclip
} from 'lucide-react';
import QuizTaker from '../components/QuizTaker';
import QuizResults from '../components/QuizResults';
import './CoursePlayer.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function CoursePlayer() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  // Core Data States
  const [course, setCourse] = useState(null);
  const [curriculum, setCurriculum] = useState([]);
  const [activeLesson, setActiveLesson] = useState(null);
  const [streamData, setStreamData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [streamLoading, setStreamLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Progress States
  const [completedLessons, setCompletedLessons] = useState([]);
  const [progressPercent, setProgressPercent] = useState(0);
  const [certificateUrl, setCertificateUrl] = useState(null);
  const [quizResult, setQuizResult] = useState(null);

  // UI Interactive States
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'notes' | 'attachments'
  const [expandedSections, setExpandedSections] = useState({});
  
  // Personal Notes States
  const [noteInput, setNoteInput] = useState('');
  const [currentVideoTime, setCurrentVideoTime] = useState(0);
  const [notesList, setNotesList] = useState([]);

  // Refs
  const videoRef = useRef(null);
  const activeLessonItemRef = useRef(null);

  // Flattened playable list for Prev/Next navigation
  const allPlayableItems = useMemo(() => {
    const items = [];
    curriculum.forEach((section, sIdx) => {
      (section.lessons || []).forEach((lesson) => {
        items.push({ ...lesson, isQuiz: false, sectionTitle: section.title, sectionIndex: sIdx });
      });
      (section.quizzes || []).forEach((quiz) => {
        items.push({ ...quiz, isQuiz: true, sectionTitle: section.title, sectionIndex: sIdx });
      });
    });
    return items;
  }, [curriculum]);

  // Load saved notes from localStorage
  useEffect(() => {
    if (!id) return;
    try {
      const stored = localStorage.getItem(`edumy_notes_${id}`);
      if (stored) {
        setNotesList(JSON.parse(stored));
      }
    } catch (e) {
      console.warn('Failed to parse notes from storage:', e);
    }
  }, [id]);

  // Save notes to localStorage
  const saveNotesToStorage = (newNotes) => {
    setNotesList(newNotes);
    try {
      localStorage.setItem(`edumy_notes_${id}`, JSON.stringify(newNotes));
    } catch (e) {
      console.warn('Failed to save notes to storage:', e);
    }
  };

  // Fetch Course & Curriculum
  useEffect(() => {
    const fetchCourseData = async () => {
      try {
        setLoading(true);
        setError('');

        const [courseRes, currRes] = await Promise.all([
          api.get(`/courses/${id}`),
          api.get(`/courses/${id}/curriculum`)
        ]);

        setCourse(courseRes.data);
        const currData = currRes.data || [];
        setCurriculum(currData);

        // Expand sections by default
        const initExpanded = {};
        currData.forEach(s => {
          initExpanded[s.sectionId] = true;
        });
        setExpandedSections(initExpanded);

        await fetchProgress();

        // Select first available lesson
        if (currData.length > 0 && currData[0].lessons?.length > 0) {
          setActiveLesson(currData[0].lessons[0]);
        }
      } catch (err) {
        console.error('Failed to load course player:', err);
        setError('Không thể tải khóa học. Vui lòng kiểm tra quyền sở hữu hoặc đăng nhập lại.');
      } finally {
        setLoading(false);
      }
    };

    fetchCourseData();
  }, [id]);

  // Fetch secure streaming permissions on activeLesson change
  useEffect(() => {
    if (!activeLesson || activeLesson.isQuiz) {
      setStreamData(null);
      return;
    }

    const verifyPlayback = async () => {
      setStreamLoading(true);
      try {
        const res = await api.get(`/lessons/${activeLesson.lessonId}/play`);
        setStreamData(res.data);
      } catch (err) {
        console.error('Playback verification error:', err);
        setStreamData({
          canPlay: false,
          message: 'Bài giảng này yêu cầu quyền học viên chính thức. Vui lòng đăng ký khóa học để tiếp tục.'
        });
      } finally {
        setStreamLoading(false);
      }
    };

    verifyPlayback();
  }, [activeLesson]);

  // Auto-scroll sidebar to active lesson
  useEffect(() => {
    if (activeLesson && activeLessonItemRef.current) {
      activeLessonItemRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'nearest'
      });
    }
  }, [activeLesson]);

  const fetchProgress = async () => {
    try {
      const res = await api.get(`/courses/${id}/progress`);
      setCompletedLessons(res.data.completedLessonIds || []);
      setProgressPercent(res.data.progressPercentage || 0);
      if (res.data.certificateUrl) {
        setCertificateUrl(res.data.certificateUrl);
      }
    } catch (err) {
      console.warn('Failed to fetch progress:', err);
    }
  };

  // Toggle Complete / Incomplete Lesson with Instant Optimistic UI Update
  const handleToggleComplete = async () => {
    if (!activeLesson || activeLesson.isQuiz) return;
    const lessonId = activeLesson.lessonId;
    const isAlreadyCompleted = completedLessons.includes(lessonId);

    // 1. Optimistic state updates (instant number jump)
    const nextCompleted = isAlreadyCompleted
      ? completedLessons.filter(item => item !== lessonId)
      : [...completedLessons, lessonId];
    
    setCompletedLessons(nextCompleted);

    const totalItems = allPlayableItems.length || 1;
    const optimisticPercent = Math.min(100, Math.round((nextCompleted.length / totalItems) * 100));
    setProgressPercent(optimisticPercent);

    // 2. Synchronize with backend in background
    try {
      if (!isAlreadyCompleted) {
        await api.post(`/courses/${id}/lessons/${lessonId}/complete`);
      }
      fetchProgress();
    } catch (err) {
      console.error('Failed to toggle completion:', err);
      // Revert on failure
      setCompletedLessons(completedLessons);
      fetchProgress();
    }
  };

  // Navigation Logic
  const currentIndex = allPlayableItems.findIndex(item => 
    activeLesson?.isQuiz
      ? (item.isQuiz && item.quizId === activeLesson.quizId)
      : (!item.isQuiz && item.lessonId === activeLesson?.lessonId)
  );

  const hasPrevious = currentIndex > 0;
  const hasNext = currentIndex >= 0 && currentIndex < allPlayableItems.length - 1;

  const handlePrevLesson = () => {
    if (hasPrevious) {
      const prev = allPlayableItems[currentIndex - 1];
      setActiveLesson(prev);
      setQuizResult(null);
    }
  };

  const handleNextLesson = () => {
    if (hasNext) {
      const next = allPlayableItems[currentIndex + 1];
      setActiveLesson(next);
      setQuizResult(null);
    }
  };

  // Auto-advance when video ends
  const handleVideoEnded = async () => {
    if (activeLesson && !completedLessons.includes(activeLesson.lessonId)) {
      await handleToggleComplete();
    }
    if (hasNext) {
      handleNextLesson();
    }
  };

  // Track Video Time for notes
  const handleTimeUpdate = () => {
    if (videoRef.current) {
      setCurrentVideoTime(Math.floor(videoRef.current.currentTime));
    }
  };

  // Add Note with Timestamp
  const handleAddNote = (e) => {
    e.preventDefault();
    if (!noteInput.trim()) return;

    const newNote = {
      id: Date.now(),
      lessonId: activeLesson?.lessonId,
      lessonTitle: activeLesson?.title || 'Bài học',
      time: currentVideoTime,
      text: noteInput.trim(),
      createdAt: new Date().toISOString()
    };

    const updated = [newNote, ...notesList];
    saveNotesToStorage(updated);
    setNoteInput('');
  };

  const handleDeleteNote = (noteId) => {
    const updated = notesList.filter(n => n.id !== noteId);
    saveNotesToStorage(updated);
  };

  const handleJumpToTime = (seconds) => {
    if (videoRef.current) {
      videoRef.current.currentTime = seconds;
      videoRef.current.play();
    }
  };

  const handleExportNotes = () => {
    if (notesList.length === 0) return;
    const content = notesList.map(n => 
      `[${formatDuration(n.time)}] ${n.lessonTitle}\n${n.text}\n(${new Date(n.createdAt).toLocaleString('vi-VN')})\n----------------------`
    ).join('\n\n');

    const element = document.createElement("a");
    const file = new Blob([content], { type: 'text/plain;charset=utf-8' });
    element.href = URL.createObjectURL(file);
    element.download = `Ghi_chu_${course?.title || 'Khoa_hoc'}.txt`;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  };

  // Download Attachment handler
  const handleDownloadAttachment = (attachment) => {
    const fileContent = `Edumy eLearning Platform\nKhóa học: ${course?.title}\nBài học: ${activeLesson?.title}\nTài liệu: ${attachment.name}\n\nĐã tải về thành công để ôn tập ngoại tuyến.`;
    const element = document.createElement("a");
    const file = new Blob([fileContent], { type: 'text/plain;charset=utf-8' });
    element.href = URL.createObjectURL(file);
    element.download = attachment.name;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  };

  // Dynamic Attachments for active lesson
  const currentAttachments = useMemo(() => {
    if (!activeLesson) return [];
    return [
      {
        id: 1,
        name: `Slide_Bai_Giang_${activeLesson.title?.replace(/\s+/g, '_')}.pdf`,
        size: '3.4 MB',
        type: 'PDF Document',
        icon: <FileText size={20} />
      },
      {
        id: 2,
        name: `Source_Code_Thuc_Hanh_Lesson_${activeLesson.lessonId}.zip`,
        size: '18.2 MB',
        type: 'ZIP Archive',
        icon: <FileArchive size={20} />
      },
      {
        id: 3,
        name: `CheatSheet_Ghi_Nho_Kien_Thuc.pdf`,
        size: '1.2 MB',
        type: 'PDF Cheatsheet',
        icon: <Paperclip size={20} />
      }
    ];
  }, [activeLesson]);

  const toggleSectionAccordion = (secId) => {
    setExpandedSections(prev => ({
      ...prev,
      [secId]: !prev[secId]
    }));
  };

  const formatDuration = (seconds) => {
    if (!seconds && seconds !== 0) return '10:00';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  const getVideoSrc = () => {
    if (!streamData?.videoUrl) return '';
    if (streamData.videoUrl.startsWith('http://') || streamData.videoUrl.startsWith('https://')) {
      return streamData.videoUrl;
    }
    return `${BACKEND_URL}${streamData.videoUrl}`;
  };

  // ==================== 1. SKELETON LOADING STATE ====================
  if (loading) {
    return (
      <div className="course-player-root">
        {/* Topbar skeleton */}
        <header className="player-topbar">
          <div className="skeleton-shimmer" style={{ width: 140, height: 32 }}></div>
          <div className="skeleton-shimmer" style={{ width: 320, height: 24 }}></div>
          <div className="skeleton-shimmer" style={{ width: 100, height: 32 }}></div>
        </header>

        {/* 2-col workspace skeleton */}
        <div className="player-skeleton-layout">
          <div className="player-left-col">
            <div className="skeleton-shimmer skeleton-video-stage"></div>
            <div className="skeleton-meta-block">
              <div className="skeleton-shimmer" style={{ width: '60%', height: 28 }}></div>
              <div className="skeleton-shimmer" style={{ width: '40%', height: 20 }}></div>
              <div className="skeleton-shimmer" style={{ width: '100%', height: 90, marginTop: '1rem' }}></div>
            </div>
          </div>
          <aside className="skeleton-sidebar-block">
            <div className="skeleton-shimmer" style={{ width: '80%', height: 24 }}></div>
            <div className="skeleton-shimmer" style={{ width: '100%', height: 10 }}></div>
            <div className="skeleton-shimmer" style={{ width: '100%', height: 48, marginTop: '1rem' }}></div>
            <div className="skeleton-shimmer" style={{ width: '100%', height: 48 }}></div>
            <div className="skeleton-shimmer" style={{ width: '100%', height: 48 }}></div>
            <div className="skeleton-shimmer" style={{ width: '100%', height: 48 }}></div>
          </aside>
        </div>
      </div>
    );
  }

  // ==================== 2. ERROR STATE ====================
  if (error) {
    return (
      <div className="player-empty-container">
        <div className="player-empty-icon-wrap" style={{ color: '#f87171', background: 'rgba(239, 68, 68, 0.12)' }}>
          <Lock size={36} />
        </div>
        <h2 className="player-empty-title">Giới hạn quyền truy cập</h2>
        <p className="player-empty-desc">{error}</p>
        <Link to={`/courses/${id}`} className="btn-empty-return">
          <ArrowLeft size={16} /> Quay lại trang khóa học
        </Link>
      </div>
    );
  }

  // ==================== 3. EMPTY STATE (KHÓA HỌC CHƯA CÓ BÀI HỌC) ====================
  if (!allPlayableItems || allPlayableItems.length === 0) {
    return (
      <div className="player-empty-container">
        <div className="player-empty-icon-wrap">
          <Inbox size={38} />
        </div>
        <h2 className="player-empty-title">Khóa học chưa có bài giảng</h2>
        <p className="player-empty-desc">
          Giảng viên đang trong quá trình biên soạn và tải lên video bài giảng. Vui lòng quay lại sau!
        </p>
        <Link to="/my-courses" className="btn-empty-return">
          <ArrowLeft size={16} /> Về khóa học của tôi
        </Link>
      </div>
    );
  }

  const isCurrentCompleted = completedLessons.includes(activeLesson?.lessonId);

  // ==================== 4. MAIN 2-COLUMN UDEMY WORKSPACE ====================
  return (
    <div className="course-player-root">
      {/* ---------------- Top Navigation Bar ---------------- */}
      <header className="player-topbar">
        <div className="topbar-left">
          <Link to="/my-courses" className="topbar-back-btn">
            <ArrowLeft size={16} /> Khóa học của tôi
          </Link>
          <div className="topbar-divider"></div>
          <h1 className="topbar-course-title" title={course?.title}>
            {course?.title}
          </h1>
        </div>

        <div className="topbar-right">
          {certificateUrl && (
            <Link 
              to={`/certificates/${certificateUrl}`} 
              target="_blank" 
              className="btn-cert-badge"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.4rem',
                background: 'linear-gradient(135deg, #f59e0b, #d97706)',
                color: '#1e1b4b',
                fontWeight: 700,
                fontSize: '0.8rem',
                padding: '0.4rem 0.85rem',
                borderRadius: '9999px',
                textDecoration: 'none'
              }}
            >
              <Award size={15} /> Nhận Chứng Chỉ
            </Link>
          )}

          <button 
            className="btn-sidebar-collapse-toggle" 
            onClick={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
            title={isSidebarCollapsed ? "Mở danh sách bài học" : "Thu gọn danh sách bài học"}
          >
            {isSidebarCollapsed ? <Menu size={16} /> : <X size={16} />}
            <span className="d-none d-sm-inline">
              {isSidebarCollapsed ? "Mở bài học" : "Đóng bài học"}
            </span>
          </button>
        </div>
      </header>

      {/* ---------------- 2-Column Main Workspace ---------------- */}
      <div className="player-workspace-2col">
        {/* ==================== CỘT TRÁI (PLAYER & TABS) ==================== */}
        <main className="player-left-col">
          {/* 16:9 Video Player Screen */}
          <div className="video-screen-container">
            <div className="video-ratio-16x9">
              {activeLesson?.isQuiz ? (
                <div style={{ width: '100%', height: '100%', overflowY: 'auto', padding: '2rem' }}>
                  {quizResult ? (
                    <QuizResults 
                      result={quizResult} 
                      quizTitle={activeLesson.title} 
                      onRetry={() => setQuizResult(null)} 
                      darkMode={true}
                    />
                  ) : (
                    <QuizTaker 
                      quizId={activeLesson.quizId} 
                      darkMode={true}
                      onComplete={(result) => {
                        setQuizResult(result);
                        fetchProgress();
                      }} 
                    />
                  )}
                </div>
              ) : streamLoading ? (
                <div className="video-state-card">
                  <div className="player-spinner" style={{ width: 40, height: 40 }}></div>
                  <h4>Đang xác thực bảo mật luồng video...</h4>
                </div>
              ) : streamData && !streamData.canPlay ? (
                <div className="video-state-card">
                  <Lock size={48} color="#ef4444" />
                  <h4>Nội dung được bảo vệ</h4>
                  <p>{streamData.message || 'Bạn cần đăng ký khóa học để xem nội dung bài giảng này.'}</p>
                  <Link 
                    to={`/courses/${id}`} 
                    className="btn-ctrl-nav" 
                    style={{ background: '#4f46e5', color: '#fff', border: 'none', padding: '0.6rem 1.25rem' }}
                  >
                    Đăng ký khóa học ngay
                  </Link>
                </div>
              ) : streamData?.videoUrl ? (
                <video 
                  ref={videoRef}
                  key={streamData.videoUrl}
                  controls 
                  controlsList="nodownload"
                  src={getVideoSrc()} 
                  className="html5-main-video"
                  autoPlay
                  onEnded={handleVideoEnded}
                  onTimeUpdate={handleTimeUpdate}
                >
                  Trình duyệt không hỗ trợ phát video HTML5.
                </video>
              ) : (
                <div className="video-state-card">
                  <PlayCircle size={48} color="#64748b" />
                  <h4>Bài học dạng tài liệu đọc</h4>
                  <p>Bài học này bao gồm tài liệu hướng dẫn và bài tập thực hành chi tiết ở phần bên dưới.</p>
                </div>
              )}
            </div>
          </div>

          {/* Action Bar Beneath Video */}
          <div className="video-action-bar">
            <div className="nav-lessons-btn-group">
              <button 
                className="btn-ctrl-nav" 
                onClick={handlePrevLesson}
                disabled={!hasPrevious}
                title="Quay lại bài giảng trước"
              >
                <ChevronLeft size={16} /> Bài trước
              </button>
              <button 
                className="btn-ctrl-nav" 
                onClick={handleNextLesson}
                disabled={!hasNext}
                title="Chuyển tới bài giảng tiếp theo"
              >
                Bài tiếp theo <ChevronRight size={16} />
              </button>
            </div>

            {!activeLesson?.isQuiz && (
              <button 
                className={`btn-toggle-complete ${isCurrentCompleted ? 'is-completed' : 'not-completed'}`}
                onClick={handleToggleComplete}
              >
                {isCurrentCompleted ? (
                  <>
                    <CheckCircle size={16} /> Đã hoàn thành
                  </>
                ) : (
                  <>
                    <Circle size={16} /> Đánh dấu hoàn thành
                  </>
                )}
              </button>
            )}
          </div>

          {/* 3 Tabs & Lower Details Area */}
          <div className="player-lower-content">
            <div className="lesson-headline-box">
              <h2 className="lesson-headline-title">{activeLesson?.title || 'Bài học'}</h2>
              <div className="lesson-headline-meta">
                <span>{activeLesson?.isQuiz ? 'Bài kiểm tra trắc nghiệm' : 'Bài giảng video'}</span>
                {!activeLesson?.isQuiz && activeLesson?.duration && (
                  <span>
                    <Clock size={14} style={{ display: 'inline', marginRight: 4, verticalAlign: -2 }} />
                    {formatDuration(activeLesson.duration)}
                  </span>
                )}
                {streamData?.isPreview && (
                  <span className="badge-preview-tag">Học thử miễn phí</span>
                )}
              </div>
            </div>

            {/* 3 Tabs Navigation Header */}
            <nav className="player-tabs-header">
              <button 
                className={`player-tab-button ${activeTab === 'overview' ? 'active' : ''}`}
                onClick={() => setActiveTab('overview')}
              >
                <BookOpen size={16} /> Tổng quan bài học
              </button>

              <button 
                className={`player-tab-button ${activeTab === 'notes' ? 'active' : ''}`}
                onClick={() => setActiveTab('notes')}
              >
                <StickyNote size={16} /> Ghi chú cá nhân
                {notesList.length > 0 && (
                  <span className="tab-badge-counter">{notesList.length}</span>
                )}
              </button>

              <button 
                className={`player-tab-button ${activeTab === 'attachments' ? 'active' : ''}`}
                onClick={() => setActiveTab('attachments')}
              >
                <Download size={16} /> Tài liệu đính kèm
                <span className="tab-badge-counter">{currentAttachments.length}</span>
              </button>
            </nav>

            {/* Tab 1: Tổng quan bài học */}
            {activeTab === 'overview' && (
              <div className="tab-overview-body">
                <h4>Mục tiêu & Hướng dẫn bài học</h4>
                <p>
                  {activeLesson?.content || 
                    'Bài giảng này cung cấp các nguyên lý lập trình then chốt, mô hình thiết kế thực tiễn và bài tập thực hành. Học viên nên thực hành trực tiếp theo hướng dẫn để đạt hiệu quả cao nhất.'}
                </p>

                <div className="course-info-callout">
                  <h5>Khóa học: {course?.title}</h5>
                  <p>
                    {course?.description || 'Nền tảng đào tạo trực tuyến công nghệ cao Edumy.'}
                  </p>
                </div>
              </div>
            )}

            {/* Tab 2: Ghi chú cá nhân */}
            {activeTab === 'notes' && (
              <div>
                <form className="notes-creation-box" onSubmit={handleAddNote}>
                  <textarea 
                    className="notes-input-textarea"
                    value={noteInput}
                    onChange={(e) => setNoteInput(e.target.value)}
                    placeholder="Ghi chép nhanh kiến thức quan trọng tại đây..."
                  />
                  <div className="notes-actions-row">
                    <button 
                      type="submit" 
                      className="btn-add-note"
                      disabled={!noteInput.trim()}
                    >
                      <Plus size={16} /> Lưu ghi chú ở [{formatDuration(currentVideoTime)}]
                    </button>

                    {notesList.length > 0 && (
                      <button 
                        type="button" 
                        className="btn-export-notes"
                        onClick={handleExportNotes}
                      >
                        <Download size={14} /> Xuất file ghi chú (.txt)
                      </button>
                    )}
                  </div>
                </form>

                <div className="saved-notes-list">
                  {notesList.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                      Chưa có ghi chú nào cho khóa học này. Hãy tạo ghi chú đầu tiên của bạn!
                    </div>
                  ) : (
                    notesList.map((note) => (
                      <div key={note.id} className="note-item-card">
                        <div className="note-content-col">
                          <span 
                            className="note-timestamp-badge" 
                            onClick={() => handleJumpToTime(note.time)}
                            title="Nhấn để tua video tới thời điểm này"
                          >
                            <Clock size={12} /> {formatDuration(note.time)} • {note.lessonTitle}
                          </span>
                          <p className="note-text-body">{note.text}</p>
                        </div>
                        <button 
                          className="btn-delete-note" 
                          onClick={() => handleDeleteNote(note.id)}
                          title="Xóa ghi chú"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}

            {/* Tab 3: Tài liệu đính kèm (Cho phép download) */}
            {activeTab === 'attachments' && (
              <div className="attachments-list-container">
                {currentAttachments.map((file) => (
                  <div key={file.id} className="attachment-item-row">
                    <div className="attachment-file-info">
                      <div className="attachment-icon-box">
                        {file.icon}
                      </div>
                      <div className="attachment-name-meta">
                        <h5 className="attachment-name" title={file.name}>
                          {file.name}
                        </h5>
                        <p className="attachment-size-type">
                          {file.type} • {file.size}
                        </p>
                      </div>
                    </div>

                    <button 
                      className="btn-download-file" 
                      onClick={() => handleDownloadAttachment(file)}
                      title={`Tải xuống ${file.name}`}
                    >
                      <Download size={15} /> Tải về
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </main>

        {/* ==================== CỘT PHẢI (CURRICULUM SIDEBAR) ==================== */}
        <aside className={`player-sidebar-col ${isSidebarCollapsed ? 'collapsed' : ''}`}>
          {/* Header hiển thị thanh tiến độ mượt mà */}
          <div className="sidebar-progress-header">
            <div className="sidebar-title-row">
              <h3>Nội dung khóa học</h3>
              <span className="sidebar-completed-count">
                {completedLessons.length} / {allPlayableItems.length} hoàn thành
              </span>
            </div>

            {/* Smooth Progress Bar */}
            <div className="curriculum-progress-track" title={`Tiến độ hoàn thành: ${progressPercent}%`}>
              <div 
                className="curriculum-progress-fill" 
                style={{ width: `${progressPercent}%` }}
              ></div>
            </div>

            <div className="sidebar-progress-subtext">
              <span>Tiến độ học tập</span>
              <strong>{progressPercent}%</strong>
            </div>
          </div>

          {/* Accordion các Section (Chương học) */}
          <div className="curriculum-accordion-scroll">
            {curriculum.map((section, sIdx) => {
              const isExpanded = expandedSections[section.sectionId] !== false;
              const sectionLessonCount = (section.lessons?.length || 0) + (section.quizzes?.length || 0);

              return (
                <div className="curriculum-section-block" key={section.sectionId}>
                  {/* Header chương học */}
                  <div 
                    className="section-accordion-header"
                    onClick={() => toggleSectionAccordion(section.sectionId)}
                  >
                    <div className="section-header-left">
                      <span className="section-index-title">
                        Phần {sIdx + 1}: {section.title}
                      </span>
                      <span className="section-stats-small">
                        {sectionLessonCount} bài học
                      </span>
                    </div>

                    {isExpanded ? (
                      <ChevronUp size={16} color="#94a3b8" />
                    ) : (
                      <ChevronDown size={16} color="#94a3b8" />
                    )}
                  </div>

                  {/* Danh sách bài học trong chương */}
                  {isExpanded && (
                    <div className="section-lessons-container">
                      {section.lessons?.map((lesson, lIdx) => {
                        const isActive = !activeLesson?.isQuiz && activeLesson?.lessonId === lesson.lessonId;
                        const isDone = completedLessons.includes(lesson.lessonId);

                        return (
                          <div 
                            key={lesson.lessonId}
                            ref={isActive ? activeLessonItemRef : null}
                            className={`sidebar-lesson-item ${isActive ? 'is-active' : ''}`}
                            onClick={() => {
                              setActiveLesson(lesson);
                              setQuizResult(null);
                            }}
                          >
                            {/* Icon trạng thái: tick xanh, đang xem, chưa xem */}
                            <div className="lesson-status-icon-box">
                              {isDone ? (
                                <CheckCircle size={18} className="status-icon-completed" />
                              ) : isActive ? (
                                <PlayCircle size={18} className="status-icon-active" />
                              ) : (
                                <Circle size={18} className="status-icon-unviewed" />
                              )}
                            </div>

                            <div className="lesson-title-meta-col">
                              <span className="sidebar-lesson-name" title={lesson.title}>
                                {lIdx + 1}. {lesson.title}
                              </span>
                              <div className="sidebar-lesson-duration-badge">
                                <span>{formatDuration(lesson.duration)}</span>
                                {lesson.isPreview && (
                                  <span className="badge-preview-tag" style={{ marginLeft: '0.4rem' }}>
                                    Học thử
                                  </span>
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })}

                      {/* Quizzes nếu có */}
                      {section.quizzes?.map((quiz) => {
                        const isActive = activeLesson?.isQuiz && activeLesson?.quizId === quiz.quizId;

                        return (
                          <div 
                            key={`quiz-${quiz.quizId}`}
                            ref={isActive ? activeLessonItemRef : null}
                            className={`sidebar-lesson-item ${isActive ? 'is-active' : ''}`}
                            onClick={() => {
                              setActiveLesson({ ...quiz, isQuiz: true });
                              setQuizResult(null);
                            }}
                          >
                            <div className="lesson-status-icon-box">
                              <HelpCircle size={18} color={isActive ? "#818cf8" : "#f59e0b"} />
                            </div>

                            <div className="lesson-title-meta-col">
                              <span className="sidebar-lesson-name" title={quiz.title}>
                                Quiz: {quiz.title}
                              </span>
                              <div className="sidebar-lesson-duration-badge">
                                <span>Kiểm tra trắc nghiệm</span>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </aside>
      </div>
    </div>
  );
}

export default CoursePlayer;
