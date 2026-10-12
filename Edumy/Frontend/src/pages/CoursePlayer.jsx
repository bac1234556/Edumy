import React, { useState, useEffect, useContext, useRef } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { 
  PlayCircle, 
  CheckCircle, 
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
  MessageSquare, 
  Info,
  Check
} from 'lucide-react';
import QuizTaker from '../components/QuizTaker';
import QuizResults from '../components/QuizResults';
import './CoursePlayer.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function CoursePlayer() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [course, setCourse] = useState(null);
  const [curriculum, setCurriculum] = useState([]);
  const [activeLesson, setActiveLesson] = useState(null);
  const [streamData, setStreamData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [streamLoading, setStreamLoading] = useState(false);
  const [error, setError] = useState('');
  const [completedLessons, setCompletedLessons] = useState([]);
  const [progressPercent, setProgressPercent] = useState(0);
  const [certificateUrl, setCertificateUrl] = useState(null);
  const [quizResult, setQuizResult] = useState(null);
  
  // UI States
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'notes' | 'qa'
  const [expandedSections, setExpandedSections] = useState({});
  const [notes, setNotes] = useState(() => localStorage.getItem(`notes_course_${id}`) || '');

  const videoRef = useRef(null);

  // Flattened lesson list for easy prev/next navigation
  const allPlayableItems = React.useMemo(() => {
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

  // Fetch initial course data & curriculum
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError('');

        const [courseRes, currRes] = await Promise.all([
          api.get(`/courses/${id}`),
          api.get(`/courses/${id}/curriculum`)
        ]);

        setCourse(courseRes.data);
        setCurriculum(currRes.data || []);

        // Expand all sections by default
        const initialSections = {};
        (currRes.data || []).forEach(s => {
          initialSections[s.sectionId] = true;
        });
        setExpandedSections(initialSections);

        await fetchProgress();

        // Set default active lesson
        if (currRes.data?.length > 0 && currRes.data[0].lessons?.length > 0) {
          setActiveLesson(currRes.data[0].lessons[0]);
        }
      } catch (err) {
        console.error('Failed to load course player:', err);
        setError('Không thể tải nội dung khóa học. Vui lòng kiểm tra quyền truy cập hoặc đăng ký khóa học.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  // Fetch secure streaming permissions whenever activeLesson changes
  useEffect(() => {
    if (!activeLesson || activeLesson.isQuiz) {
      setStreamData(null);
      return;
    }

    const fetchStreamPermission = async () => {
      setStreamLoading(true);
      try {
        const res = await api.get(`/lessons/${activeLesson.lessonId}/play`);
        setStreamData(res.data);
      } catch (err) {
        console.error('Error fetching lesson playback permission:', err);
        setStreamData({
          canPlay: false,
          message: 'Bài giảng này yêu cầu quyền học viên chính thức. Vui lòng đăng ký khóa học.'
        });
      } finally {
        setStreamLoading(false);
      }
    };

    fetchStreamPermission();
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
      console.warn('Failed to fetch user progress:', err);
    }
  };

  const handleMarkCompleted = async () => {
    if (!activeLesson || activeLesson.isQuiz) return;
    try {
      await api.post(`/courses/${id}/lessons/${activeLesson.lessonId}/complete`);
      if (!completedLessons.includes(activeLesson.lessonId)) {
        setCompletedLessons(prev => [...prev, activeLesson.lessonId]);
      }
      fetchProgress();
    } catch (err) {
      console.error('Failed to mark lesson completed:', err);
    }
  };

  // Navigate to Next / Previous
  const currentIndex = allPlayableItems.findIndex(item => 
    activeLesson?.isQuiz 
      ? (item.isQuiz && item.quizId === activeLesson.quizId)
      : (!item.isQuiz && item.lessonId === activeLesson.lessonId)
  );

  const hasPrevious = currentIndex > 0;
  const hasNext = currentIndex >= 0 && currentIndex < allPlayableItems.length - 1;

  const handlePreviousLesson = () => {
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

  // Video ended -> mark completed and auto advance
  const handleVideoEnded = async () => {
    await handleMarkCompleted();
    if (hasNext) {
      handleNextLesson();
    }
  };

  const toggleSection = (sectionId) => {
    setExpandedSections(prev => ({
      ...prev,
      [sectionId]: !prev[sectionId]
    }));
  };

  const handleSaveNotes = (val) => {
    setNotes(val);
    localStorage.setItem(`notes_course_${id}`, val);
  };

  const formatDuration = (seconds) => {
    if (!seconds) return '10:00';
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

  if (loading) {
    return (
      <div className="player-loading-skeleton">
        <div className="player-spinner"></div>
        <p>Đang tải phòng học Cinema Edumy...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="player-error-box">
        <h3>Truy Cập Bị Giới Hạn</h3>
        <p>{error}</p>
        <Link to={`/courses/${id}`} className="btn-player-nav" style={{ marginTop: '1rem' }}>
          Quay lại trang thông tin khóa học
        </Link>
      </div>
    );
  }

  return (
    <div className="course-player-container">
      {/* ---------------- Top Header Bar ---------------- */}
      <header className="player-header">
        <div className="player-header-left">
          <Link to="/my-learning" className="back-link">
            <ArrowLeft size={16} /> Khóa học của tôi
          </Link>
          <h1 className="player-course-title" title={course?.title}>
            {course?.title}
          </h1>
        </div>

        <div className="player-header-right">
          <div className="progress-widget">
            <div className="progress-track" title={`Tiến độ hoàn thành: ${progressPercent}%`}>
              <div 
                className="progress-fill" 
                style={{ width: `${progressPercent}%` }}
              ></div>
            </div>
            <span className="progress-percent-label">{progressPercent}%</span>
          </div>

          {certificateUrl && (
            <Link 
              to={`/certificates/${certificateUrl}`} 
              target="_blank" 
              className="btn-cert-badge"
              title="Xem chứng chỉ hoàn thành khóa học"
            >
              <Award size={15} /> Nhận Chứng Chỉ
            </Link>
          )}

          <button 
            className="btn-toggle-sidebar" 
            onClick={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
            title={isSidebarCollapsed ? "Mở danh sách bài học" : "Đóng danh sách bài học"}
            aria-label="Toggle Sidebar"
          >
            {isSidebarCollapsed ? <Menu size={18} /> : <X size={18} />}
          </button>
        </div>
      </header>

      {/* ---------------- Main Layout ---------------- */}
      <div className="player-layout">
        <main className="player-main">
          {/* Video or Quiz Stage */}
          <div className="video-stage">
            {activeLesson?.isQuiz ? (
              <div style={{ width: '100%', padding: '2rem', maxWidth: '900px' }}>
                {quizResult ? (
                  <QuizResults 
                    result={quizResult} 
                    quizTitle={activeLesson.title} 
                    onRetry={() => setQuizResult(null)} 
                  />
                ) : (
                  <QuizTaker 
                    quizId={activeLesson.quizId} 
                    onComplete={(result) => {
                      setQuizResult(result);
                      fetchProgress();
                    }} 
                  />
                )}
              </div>
            ) : streamLoading ? (
              <div className="no-video-banner">
                <div className="player-spinner" style={{ width: 36, height: 36 }}></div>
                <p style={{ marginTop: '1rem' }}>Đang xác thực bảo mật stream video...</p>
              </div>
            ) : streamData && !streamData.canPlay ? (
              <div className="no-video-banner">
                <Lock size={56} color="#ef4444" />
                <h4>Nội dung được bảo vệ</h4>
                <p>{streamData.message || 'Bạn chưa có quyền xem bài học này. Vui lòng đăng ký khóa học để tiếp tục.'}</p>
                <Link 
                  to={`/courses/${id}`} 
                  className="btn-complete-lesson not-completed" 
                  style={{ marginTop: '1.25rem', textDecoration: 'none' }}
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
                className="main-video-player"
                autoPlay
                onEnded={handleVideoEnded}
              >
                Trình duyệt của bạn không hỗ trợ phát video HTML5.
              </video>
            ) : (
              <div className="no-video-banner">
                <PlayCircle size={56} color="#64748b" />
                <h4>Chưa có video cho bài học này</h4>
                <p>Nội dung đang được giảng viên cập nhật hoặc định dạng ở dạng tài liệu đọc.</p>
              </div>
            )}
          </div>

          {/* Controls Bar Below Video */}
          <div className="video-action-bar">
            <div className="nav-buttons-group">
              <button 
                className="btn-player-nav" 
                onClick={handlePreviousLesson}
                disabled={!hasPrevious}
              >
                <ChevronLeft size={16} /> Bài trước
              </button>
              <button 
                className="btn-player-nav" 
                onClick={handleNextLesson}
                disabled={!hasNext}
              >
                Bài tiếp theo <ChevronRight size={16} />
              </button>
            </div>

            {!activeLesson?.isQuiz && (
              <button 
                className={`btn-complete-lesson ${completedLessons.includes(activeLesson?.lessonId) ? 'completed' : 'not-completed'}`}
                onClick={handleMarkCompleted}
                disabled={completedLessons.includes(activeLesson?.lessonId)}
              >
                {completedLessons.includes(activeLesson?.lessonId) ? (
                  <>
                    <Check size={16} /> Đã hoàn thành
                  </>
                ) : (
                  'Đánh dấu hoàn thành'
                )}
              </button>
            )}
          </div>

          {/* Tabs & Detail Panel */}
          <div className="player-details-panel">
            <div className="lesson-meta-header">
              <h2>{activeLesson?.title || 'Chọn bài học'}</h2>
              <div className="lesson-badge-info">
                <span>
                  {activeLesson?.isQuiz ? 'Bài kiểm tra trắc nghiệm' : 'Bài giảng video'}
                </span>
                {!activeLesson?.isQuiz && activeLesson?.duration && (
                  <span>
                    <Clock size={14} style={{ display: 'inline', marginRight: 4, verticalAlign: -1 }} /> 
                    {formatDuration(activeLesson.duration)}
                  </span>
                )}
                {streamData?.isPreview && (
                  <span className="preview-tag">Học thử miễn phí</span>
                )}
              </div>
            </div>

            <nav className="player-tabs-nav">
              <button 
                className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
                onClick={() => setActiveTab('overview')}
              >
                Tổng quan & Tài liệu
              </button>
              <button 
                className={`tab-btn ${activeTab === 'notes' ? 'active' : ''}`}
                onClick={() => setActiveTab('notes')}
              >
                Ghi chú cá nhân
              </button>
              <button 
                className={`tab-btn ${activeTab === 'qa' ? 'active' : ''}`}
                onClick={() => setActiveTab('qa')}
              >
                Hỏi đáp (Q&A)
              </button>
            </nav>

            <div className="tab-content-body">
              {activeTab === 'overview' && (
                <div>
                  <h4 style={{ color: '#f1f5f9', marginBottom: '0.75rem' }}>Mô tả bài học</h4>
                  <p>
                    {activeLesson?.content || 
                      'Bài học này cung cấp kiến thức nền tảng và bài tập thực hành. Hãy chú ý ghi chú các khái niệm quan trọng để vận dụng vào dự án thực tế.'}
                  </p>
                  <div style={{ marginTop: '1.5rem', padding: '1rem', background: '#111827', borderRadius: '8px', border: '1px solid #1f2937' }}>
                    <h5 style={{ margin: '0 0 0.5rem 0', color: '#e2e8f0' }}>Thông tin khóa học</h5>
                    <p style={{ margin: 0, fontSize: '0.875rem', color: '#94a3b8' }}>
                      {course?.description || 'Nền tảng đào tạo trực tuyến chuẩn công nghiệp Edumy.'}
                    </p>
                  </div>
                </div>
              )}

              {activeTab === 'notes' && (
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#94a3b8', marginBottom: '0.5rem' }}>
                    Ghi chú của bạn được tự động lưu trữ trên trình duyệt.
                  </p>
                  <textarea 
                    value={notes} 
                    onChange={(e) => handleSaveNotes(e.target.value)}
                    placeholder="Nhập ghi chú tại đây (ví dụ: công thức, lưu ý ở phút 02:45)..."
                    style={{
                      width: '100%',
                      minHeight: '200px',
                      background: '#111827',
                      color: '#f8fafc',
                      border: '1px solid #374151',
                      borderRadius: '8px',
                      padding: '1rem',
                      fontFamily: 'inherit',
                      fontSize: '0.95rem',
                      lineHeight: '1.6',
                      resize: 'vertical',
                      outline: 'none'
                    }}
                  />
                </div>
              )}

              {activeTab === 'qa' && (
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                    <h4 style={{ color: '#f1f5f9', margin: 0 }}>Cộng đồng trao đổi bài học</h4>
                    <span style={{ fontSize: '0.85rem', color: '#64748b' }}>Hỗ trợ 24/7 bởi Giảng viên & Trợ giảng AI</span>
                  </div>
                  <div style={{ padding: '2rem', textAlign: 'center', background: '#111827', borderRadius: '8px', border: '1px solid #1f2937' }}>
                    <MessageSquare size={36} color="#6366f1" style={{ marginBottom: '0.75rem' }} />
                    <p style={{ color: '#cbd5e1', marginBottom: '0.5rem' }}>Bạn có thắc mắc trong bài học này?</p>
                    <p style={{ fontSize: '0.85rem', color: '#64748b', margin: 0 }}>
                      Tính năng diễn đàn thảo luận cộng đồng đang đồng bộ theo thời gian thực.
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </main>

        {/* ---------------- Right Sidebar (Curriculum) ---------------- */}
        <aside className={`player-sidebar ${isSidebarCollapsed ? 'collapsed' : ''}`}>
          <div className="sidebar-top">
            <h3>Nội dung khóa học</h3>
            <div className="sidebar-stats">
              {completedLessons.length} / {allPlayableItems.length} nội dung đã hoàn thành
            </div>
          </div>

          <div className="curriculum-scroll-list">
            {curriculum.map((section, sIdx) => {
              const isExpanded = expandedSections[section.sectionId] !== false;
              const sectionLessonCount = (section.lessons?.length || 0) + (section.quizzes?.length || 0);

              return (
                <div className="curriculum-section-group" key={section.sectionId}>
                  <div 
                    className="section-toggle-header" 
                    onClick={() => toggleSection(section.sectionId)}
                  >
                    <div className="section-title-wrap">
                      <span className="section-label">
                        Phần {sIdx + 1}: {section.title}
                      </span>
                      <span className="section-meta-count">
                        {sectionLessonCount} bài giảng
                      </span>
                    </div>
                    {isExpanded ? <ChevronUp size={16} color="#94a3b8" /> : <ChevronDown size={16} color="#94a3b8" />}
                  </div>

                  {isExpanded && (
                    <div className="lesson-list-items">
                      {section.lessons?.map((lesson, lIdx) => {
                        const isActive = !activeLesson?.isQuiz && activeLesson?.lessonId === lesson.lessonId;
                        const isDone = completedLessons.includes(lesson.lessonId);

                        return (
                          <div 
                            key={lesson.lessonId}
                            className={`lesson-item-row ${isActive ? 'active' : ''}`}
                            onClick={() => {
                              setActiveLesson(lesson);
                              setQuizResult(null);
                            }}
                          >
                            <div className="lesson-status-icon">
                              {isDone ? (
                                <CheckCircle size={16} color="#10b981" />
                              ) : (
                                <PlayCircle size={16} color={isActive ? "#6366f1" : "#64748b"} />
                              )}
                            </div>
                            <div className="lesson-title-meta">
                              <span className="lesson-name" title={lesson.title}>
                                {lIdx + 1}. {lesson.title}
                              </span>
                              <div className="lesson-duration-badge">
                                <span>{formatDuration(lesson.duration)}</span>
                                {lesson.isPreview && (
                                  <span className="preview-tag">Học thử</span>
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })}

                      {section.quizzes?.map((quiz) => {
                        const isActive = activeLesson?.isQuiz && activeLesson?.quizId === quiz.quizId;

                        return (
                          <div 
                            key={`quiz-${quiz.quizId}`}
                            className={`lesson-item-row ${isActive ? 'active' : ''}`}
                            onClick={() => {
                              setActiveLesson({ ...quiz, isQuiz: true });
                              setQuizResult(null);
                            }}
                          >
                            <div className="lesson-status-icon">
                              <HelpCircle size={16} color={isActive ? "#f59e0b" : "#64748b"} />
                            </div>
                            <div className="lesson-title-meta">
                              <span className="lesson-name" title={quiz.title}>
                                Quiz: {quiz.title}
                              </span>
                              <div className="lesson-duration-badge">
                                <span>Trắc nghiệm kiến thức</span>
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
