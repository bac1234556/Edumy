import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import {
  ArrowLeft,
  Plus,
  Trash2,
  Video,
  Clock,
  Eye,
  EyeOff,
  Layers,
  Sparkles,
  ChevronDown,
  ChevronUp,
  AlertCircle,
  CheckCircle2,
  Play
} from 'lucide-react';
import './CourseCurriculumEditor.css';

function CourseCurriculumEditor() {
  const { id } = useParams();
  const [course, setCourse] = useState(null);
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Modals / forms state
  const [newSectionTitle, setNewSectionTitle] = useState('');
  const [showAddSection, setShowAddSection] = useState(false);
  const [selectedSectionId, setSelectedSectionId] = useState(null);
  const [expandedSections, setExpandedSections] = useState({});

  // New Lesson form state
  const [lessonForm, setLessonForm] = useState({
    title: '',
    videoUrl: '',
    durationMinutes: 10,
    isPreview: false
  });
  const [activeLessonModalSection, setActiveLessonModalSection] = useState(null);

  // Video preview modal
  const [previewVideoUrl, setPreviewVideoUrl] = useState(null);

  const fetchCurriculum = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      // Fetch course details
      const courseRes = await api.get(`/courses/${id}`);
      setCourse(courseRes.data);

      // Fetch curriculum
      const curRes = await api.get(`/courses/${id}/curriculum`);
      setSections(curRes.data || []);

      // Expand all sections by default
      const expands = {};
      (curRes.data || []).forEach(s => {
        expands[s.sectionId] = true;
      });
      setExpandedSections(expands);
    } catch (err) {
      console.error(err);
      setError('Không thể tải giáo trình của khóa học. Vui lòng kiểm tra quyền truy cập.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchCurriculum();
  }, [fetchCurriculum]);

  const showNotification = (msg) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(''), 4000);
  };

  const toggleSection = (sectionId) => {
    setExpandedSections(prev => ({
      ...prev,
      [sectionId]: !prev[sectionId]
    }));
  };

  const handleCreateSection = async (e) => {
    e.preventDefault();
    if (!newSectionTitle.trim()) return;

    try {
      const payload = {
        title: newSectionTitle.trim(),
        orderIndex: sections.length + 1
      };
      await api.post(`/courses/${id}/sections`, payload);
      setNewSectionTitle('');
      setShowAddSection(false);
      showNotification('Đã thêm chương học mới thành công!');
      fetchCurriculum();
    } catch (err) {
      alert('Lỗi khi thêm chương học: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDeleteSection = async (sectionId, title) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa chương "${title}" và tất cả bài học bên trong?`)) {
      return;
    }
    try {
      await api.delete(`/sections/${sectionId}`);
      showNotification('Đã xóa chương học thành công!');
      fetchCurriculum();
    } catch (err) {
      alert('Lỗi khi xóa chương: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleOpenAddLesson = (sectionId) => {
    setActiveLessonModalSection(sectionId);
    setLessonForm({
      title: '',
      videoUrl: '',
      durationMinutes: 10,
      isPreview: false
    });
  };

  const handleCreateLesson = async (e) => {
    e.preventDefault();
    if (!lessonForm.title.trim()) return;

    try {
      const section = sections.find(s => s.sectionId === activeLessonModalSection);
      const currentLessonsCount = section?.lessons?.length || 0;

      const payload = {
        title: lessonForm.title.trim(),
        videoUrl: lessonForm.videoUrl.trim() || 'https://www.w3schools.com/html/mov_bbb.mp4',
        duration: Math.max(1, parseInt(lessonForm.durationMinutes, 10) || 5) * 60, // in seconds
        isPreview: Boolean(lessonForm.isPreview),
        orderIndex: currentLessonsCount + 1
      };

      await api.post(`/sections/${activeLessonModalSection}/lessons`, payload);
      setActiveLessonModalSection(null);
      showNotification('Đã thêm bài giảng mới thành công!');
      fetchCurriculum();
    } catch (err) {
      alert('Lỗi khi thêm bài học: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDeleteLesson = async (lessonId, title) => {
    if (!window.confirm(`Bạn có chắc muốn xóa bài giảng "${title}"?`)) return;

    try {
      await api.delete(`/Lessons/${lessonId}`);
      showNotification('Đã xóa bài giảng thành công!');
      fetchCurriculum();
    } catch (err) {
      alert('Lỗi khi xóa bài học: ' + (err.response?.data?.message || err.message));
    }
  };

  const formatDuration = (seconds) => {
    if (!seconds) return '0 phút';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    if (mins === 0) return `${secs}s`;
    return secs > 0 ? `${mins}p ${secs}s` : `${mins} phút`;
  };

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status"></div>
        <p className="mt-3 text-muted">Đang tải cấu trúc bài giảng...</p>
      </div>
    );
  }

  return (
    <div className="curriculum-editor-page">
      <div className="curriculum-editor-container">
        {/* Top Header */}
        <div className="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-3 border-bottom gap-3">
        <div>
          <Link to="/instructor" className="btn btn-outline-secondary btn-sm mb-2 d-inline-flex align-items-center gap-1 rounded-pill">
            <ArrowLeft size={16} /> Quay lại Bảng điều khiển Giảng viên
          </Link>
          <h2 className="fw-bold mb-1 d-flex align-items-center gap-2">
            <Layers className="text-primary" /> Quản lý Giáo trình & Bài giảng
          </h2>
          <p className="text-muted mb-0">
            Khóa học: <span className="fw-semibold text-dark">{course?.title || `ID #${id}`}</span>
            {course?.status && (
              <span className={`badge ms-2 ${course.status === 'Published' ? 'bg-success' : 'bg-warning'} rounded-pill`}>
                {course.status}
              </span>
            )}
          </p>
        </div>

        <div className="d-flex gap-2">
          <button
            onClick={() => setShowAddSection(true)}
            className="btn btn-primary d-inline-flex align-items-center gap-2 rounded-pill px-4 fw-semibold"
          >
            <Plus size={18} /> Thêm Chương học mới
          </button>
        </div>
      </div>

      {/* Notifications */}
      {successMsg && (
        <div className="alert alert-success d-flex align-items-center gap-2 shadow-sm rounded-3">
          <CheckCircle2 size={18} /> {successMsg}
        </div>
      )}
      {error && (
        <div className="alert alert-danger d-flex align-items-center gap-2 shadow-sm rounded-3">
          <AlertCircle size={18} /> {error}
        </div>
      )}

      {/* Add Section Form Modal/Card */}
      {showAddSection && (
        <div className="card shadow-sm border-primary mb-4 rounded-3 p-4 bg-light">
          <h5 className="fw-bold mb-3 text-primary d-flex align-items-center gap-2">
            <Plus size={20} /> Tạo Chương mới (Section)
          </h5>
          <form onSubmit={handleCreateSection}>
            <div className="mb-3">
              <label className="form-label fw-semibold">Tên chương / Mục học:</label>
              <input
                type="text"
                className="form-control"
                placeholder="VD: Chương 1: Giới thiệu căn bản & Môi trường cài đặt"
                value={newSectionTitle}
                onChange={(e) => setNewSectionTitle(e.target.value)}
                autoFocus
                required
              />
            </div>
            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-primary px-4 rounded-pill">Lưu chương</button>
              <button type="button" onClick={() => setShowAddSection(false)} className="btn btn-outline-secondary px-3 rounded-pill">Hủy</button>
            </div>
          </form>
        </div>
      )}

      {/* Sections List */}
      {sections.length === 0 ? (
        <div className="text-center py-5 bg-white rounded-4 border shadow-sm p-4">
          <Layers size={48} className="text-muted opacity-50 mb-3" />
          <h4 className="fw-bold">Chưa có bài giảng nào</h4>
          <p className="text-muted">Bắt đầu bằng việc thêm Chương đầu tiên cho khóa học của bạn.</p>
          <button
            onClick={() => setShowAddSection(true)}
            className="btn btn-primary rounded-pill px-4"
          >
            <Plus size={18} className="me-1" /> Thêm Chương đầu tiên
          </button>
        </div>
      ) : (
        <div className="d-flex flex-column gap-3">
          {sections.map((section, sIndex) => {
            const isExpanded = expandedSections[section.sectionId];
            const lessonsList = section.lessons || [];

            return (
              <div key={section.sectionId} className="card border-0 shadow-sm rounded-3 overflow-hidden bg-white">
                {/* Section Header */}
                <div
                  className="card-header bg-white py-3 px-4 d-flex justify-content-between align-items-center cursor-pointer border-bottom"
                  style={{ cursor: 'pointer' }}
                  onClick={() => toggleSection(section.sectionId)}
                >
                  <div className="d-flex align-items-center gap-3">
                    <span className="badge bg-secondary rounded-pill px-3 py-2 fw-semibold">
                      Chương {sIndex + 1}
                    </span>
                    <h5 className="mb-0 fw-bold text-dark">{section.title}</h5>
                    <span className="text-muted small">({lessonsList.length} bài giảng)</span>
                  </div>

                  <div className="d-flex align-items-center gap-2" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => handleOpenAddLesson(section.sectionId)}
                      className="btn btn-sm btn-outline-primary d-inline-flex align-items-center gap-1 rounded-pill px-3"
                    >
                      <Plus size={14} /> Thêm bài giảng
                    </button>
                    <button
                      onClick={() => handleDeleteSection(section.sectionId, section.title)}
                      className="btn btn-sm btn-outline-danger rounded-circle p-2"
                      title="Xóa chương này"
                    >
                      <Trash2 size={16} />
                    </button>
                    <div className="ms-2 text-muted" onClick={() => toggleSection(section.sectionId)}>
                      {isExpanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                    </div>
                  </div>
                </div>

                {/* Section Lessons List */}
                {isExpanded && (
                  <div className="card-body p-3 bg-light">
                    {lessonsList.length === 0 ? (
                      <div className="text-center py-3 text-muted">
                        <p className="mb-2 small">Chương này chưa có bài giảng nào.</p>
                        <button
                          onClick={() => handleOpenAddLesson(section.sectionId)}
                          className="btn btn-sm btn-link text-decoration-none fw-semibold"
                        >
                          + Bấm vào đây để thêm bài giảng
                        </button>
                      </div>
                    ) : (
                      <div className="list-group list-group-flush rounded-2 overflow-hidden border">
                        {lessonsList.map((lesson, lIndex) => (
                          <div
                            key={lesson.lessonId}
                            className="list-group-item d-flex justify-content-between align-items-center py-3 px-4 bg-white"
                          >
                            <div className="d-flex align-items-center gap-3">
                              <span className="text-muted fw-semibold small" style={{ width: '28px' }}>
                                #{lIndex + 1}
                              </span>
                              <div className="bg-primary bg-opacity-10 text-primary p-2 rounded-circle">
                                <Video size={18} />
                              </div>
                              <div>
                                <h6 className="mb-1 fw-bold text-dark">{lesson.title}</h6>
                                <div className="d-flex align-items-center gap-3 small text-muted">
                                  <span className="d-flex align-items-center gap-1">
                                    <Clock size={14} /> {formatDuration(lesson.duration)}
                                  </span>
                                  {lesson.isPreview ? (
                                    <span className="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 rounded-pill px-2 py-1 d-flex align-items-center gap-1">
                                      <Eye size={12} /> Học thử miễn phí
                                    </span>
                                  ) : (
                                    <span className="badge bg-secondary bg-opacity-10 text-secondary rounded-pill px-2 py-1 d-flex align-items-center gap-1">
                                      <EyeOff size={12} /> Khóa học viên
                                    </span>
                                  )}
                                  {lesson.videoUrl && (
                                    <span className="text-truncate d-none d-md-inline" style={{ maxWidth: '240px' }}>
                                      {lesson.videoUrl}
                                    </span>
                                  )}
                                </div>
                              </div>
                            </div>

                            <div className="d-flex align-items-center gap-2">
                              {lesson.videoUrl && (
                                <button
                                  onClick={() => setPreviewVideoUrl(lesson.videoUrl)}
                                  className="btn btn-sm btn-outline-info rounded-pill px-3 d-inline-flex align-items-center gap-1"
                                >
                                  <Play size={14} /> Xem thử
                                </button>
                              )}
                              <button
                                onClick={() => handleDeleteLesson(lesson.lessonId, lesson.title)}
                                className="btn btn-sm btn-outline-danger rounded-circle p-2"
                                title="Xóa bài học"
                              >
                                <Trash2 size={16} />
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Modal: Thêm bài giảng */}
      {activeLessonModalSection && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <div className="modal-header border-bottom pb-3">
                <h5 className="modal-title fw-bold text-primary d-flex align-items-center gap-2">
                  <Video size={20} /> Thêm bài giảng vào chương
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setActiveLessonModalSection(null)}
                ></button>
              </div>
              <form onSubmit={handleCreateLesson}>
                <div className="modal-body p-4">
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Tên bài giảng <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="VD: Bài 1: Cài đặt NodeJS và môi trường VSCode"
                      value={lessonForm.title}
                      onChange={(e) => setLessonForm({ ...lessonForm, title: e.target.value })}
                      required
                      autoFocus
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label fw-semibold">URL Video bài giảng</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="VD: https://www.youtube.com/watch?v=... hoặc link video mp4"
                      value={lessonForm.videoUrl}
                      onChange={(e) => setLessonForm({ ...lessonForm, videoUrl: e.target.value })}
                    />
                    <small className="text-muted">Hỗ trợ link video MP4 trực tiếp hoặc nhúng YouTube/HLS.</small>
                  </div>

                  <div className="row g-3 mb-3">
                    <div className="col-6">
                      <label className="form-label fw-semibold">Thời lượng (phút)</label>
                      <input
                        type="number"
                        min="1"
                        className="form-control"
                        value={lessonForm.durationMinutes}
                        onChange={(e) => setLessonForm({ ...lessonForm, durationMinutes: e.target.value })}
                      />
                    </div>
                    <div className="col-6 d-flex align-items-end">
                      <div className="form-check form-switch mb-2">
                        <input
                          className="form-check-input"
                          type="checkbox"
                          id="previewCheck"
                          checked={lessonForm.isPreview}
                          onChange={(e) => setLessonForm({ ...lessonForm, isPreview: e.target.checked })}
                        />
                        <label className="form-check-label fw-semibold ms-2" htmlFor="previewCheck">
                          Cho phép học thử
                        </label>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="modal-footer border-top pt-3">
                  <button
                    type="button"
                    className="btn btn-outline-secondary rounded-pill px-4"
                    onClick={() => setActiveLessonModalSection(null)}
                  >
                    Hủy
                  </button>
                  <button type="submit" className="btn btn-primary rounded-pill px-4 fw-semibold">
                    Thêm bài giảng
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Preview Video */}
      {previewVideoUrl && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }} tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow-lg rounded-4 overflow-hidden bg-dark text-white">
              <div className="modal-header border-secondary">
                <h5 className="modal-title fw-bold text-white d-flex align-items-center gap-2">
                  <Play size={20} className="text-primary" /> Xem thử video bài giảng
                </h5>
                <button
                  type="button"
                  className="btn-close btn-close-white"
                  onClick={() => setPreviewVideoUrl(null)}
                ></button>
              </div>
              <div className="modal-body p-0">
                <div className="ratio ratio-16x9">
                  {previewVideoUrl.includes('youtube.com') || previewVideoUrl.includes('youtu.be') ? (
                    <iframe
                      src={previewVideoUrl.replace('watch?v=', 'embed/')}
                      title="Video Preview"
                      allowFullScreen
                    ></iframe>
                  ) : (
                    <video src={previewVideoUrl} controls autoPlay className="w-100 h-100" />
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
      </div>
    </div>
  );
}

export default CourseCurriculumEditor;
