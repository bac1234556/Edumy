import React, { useState, useEffect, useCallback } from 'react';
import {
  MessageSquare,
  Send,
  CheckCircle2,
  Clock,
  User,
  ShieldCheck,
  Search,
  Filter,
  Plus,
  X,
  ChevronDown,
  ChevronUp,
  AlertCircle
} from 'lucide-react';
import qaApi from '../../api/modules/qaApi';
import './CourseQaTab.css';

export default function CourseQaTab({ courseId, activeLesson, currentUser }) {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Filters
  const [filterByLesson, setFilterByLesson] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');

  // Ask Question Modal / Accordion
  const [showAskForm, setShowAskForm] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newContent, setNewContent] = useState('');
  const [submittingQuestion, setSubmittingQuestion] = useState(false);

  // Active Selected Thread (Modal or Drawer)
  const [selectedQuestionId, setSelectedQuestionId] = useState(null);
  const [threadData, setThreadData] = useState(null);
  const [loadingThread, setLoadingThread] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [submittingReply, setSubmittingReply] = useState(false);

  // Load questions
  const loadQuestions = useCallback(async () => {
    if (!courseId) return;
    setLoading(true);
    setError('');
    try {
      const params = {
        page: 0,
        size: 30
      };
      if (filterByLesson && activeLesson?.lessonId) {
        params.lessonId = activeLesson.lessonId;
      }
      const res = await qaApi.getCourseQuestions(courseId, params);
      const data = res.data?.content || (Array.isArray(res.data) ? res.data : []);
      setQuestions(data);
    } catch (err) {
      console.error('Failed to load questions:', err);
      setError('Không thể tải danh sách hỏi đáp. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  }, [courseId, filterByLesson, activeLesson?.lessonId]);

  useEffect(() => {
    loadQuestions();
  }, [loadQuestions]);

  // Load full thread
  const openThread = async (questionId) => {
    setSelectedQuestionId(questionId);
    setLoadingThread(true);
    try {
      const res = await qaApi.getThread(questionId);
      setThreadData(res.data);
    } catch (err) {
      console.error('Failed to load question thread:', err);
      alert('Không thể tải nội dung câu hỏi.');
      setSelectedQuestionId(null);
    } finally {
      setLoadingThread(false);
    }
  };

  const closeThread = () => {
    setSelectedQuestionId(null);
    setThreadData(null);
    setReplyContent('');
  };

  // Submit new question
  const handleAskQuestion = async (e) => {
    e.preventDefault();
    if (!newTitle.trim() || !newContent.trim()) {
      alert('Vui lòng nhập đầy đủ tiêu đề và nội dung câu hỏi.');
      return;
    }
    setSubmittingQuestion(true);
    try {
      const payload = {
        title: newTitle.trim(),
        content: newContent.trim(),
        lessonId: activeLesson?.lessonId || null
      };
      await qaApi.askQuestion(courseId, payload);
      setNewTitle('');
      setNewContent('');
      setShowAskForm(false);
      await loadQuestions();
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể gửi câu hỏi. Vui lòng đăng nhập lại.');
    } finally {
      setSubmittingQuestion(false);
    }
  };

  // Post reply to question
  const handlePostReply = async (e) => {
    e.preventDefault();
    if (!replyContent.trim() || !selectedQuestionId) return;
    setSubmittingReply(true);
    try {
      await qaApi.postAnswer(selectedQuestionId, { content: replyContent.trim() });
      setReplyContent('');
      // Reload thread
      const res = await qaApi.getThread(selectedQuestionId);
      setThreadData(res.data);
      // Also refresh outer list to update answer count
      loadQuestions();
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể gửi phản hồi.');
    } finally {
      setSubmittingReply(false);
    }
  };

  // Toggle resolve status
  const handleToggleResolve = async (questionId) => {
    try {
      await qaApi.toggleResolve(questionId);
      if (selectedQuestionId === questionId && threadData) {
        setThreadData(prev => ({
          ...prev,
          isResolved: !prev.isResolved
        }));
      }
      loadQuestions();
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể cập nhật trạng thái.');
    }
  };

  // Format relative time helper
  const formatTime = (timeStr) => {
    if (!timeStr) return '';
    try {
      const date = new Date(timeStr);
      return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return timeStr;
    }
  };

  const filteredQuestions = questions.filter(q => {
    if (!searchKeyword.trim()) return true;
    const term = searchKeyword.toLowerCase();
    return (q.title && q.title.toLowerCase().includes(term)) ||
           (q.askerName && q.askerName.toLowerCase().includes(term));
  });

  return (
    <div className="qa-tab-container">
      {/* QA Header & Controls */}
      <div className="qa-controls-bar">
        <div className="qa-search-box">
          <Search size={16} className="qa-search-icon" />
          <input
            type="text"
            placeholder="Tìm kiếm câu hỏi theo từ khóa..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="qa-search-input"
          />
        </div>

        <div className="qa-filter-actions">
          {activeLesson && (
            <button
              type="button"
              className={`qa-filter-btn ${filterByLesson ? 'active' : ''}`}
              onClick={() => setFilterByLesson(!filterByLesson)}
              title="Lọc câu hỏi cho bài học này"
            >
              <Filter size={15} />
              {filterByLesson ? 'Chỉ bài hiện tại' : 'Tất cả bài học'}
            </button>
          )}

          <button
            type="button"
            className="qa-btn-ask"
            onClick={() => setShowAskForm(!showAskForm)}
          >
            {showAskForm ? <X size={16} /> : <Plus size={16} />}
            {showAskForm ? 'Đóng form' : 'Đặt câu hỏi mới'}
          </button>
        </div>
      </div>

      {/* Form: Create new Question */}
      {showAskForm && (
        <form className="qa-ask-form-card" onSubmit={handleAskQuestion}>
          <h4 className="qa-form-title">
            <MessageSquare size={18} /> Đặt câu hỏi thắc mắc
          </h4>
          <p className="qa-form-sub">
            Câu hỏi của bạn sẽ được gửi tới Giảng viên và cộng đồng học viên cùng thảo luận.
            {activeLesson && (
              <span className="qa-attached-lesson">
                {' '}Gắn kèm bài học: <strong>{activeLesson.title}</strong>
              </span>
            )}
          </p>

          <div className="qa-form-group">
            <label className="qa-form-label">Tiêu đề câu hỏi (tóm tắt vấn đề)</label>
            <input
              type="text"
              className="qa-form-input"
              placeholder="Ví dụ: Đoạn video 04:20 code bị lỗi NullPointerException..."
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              required
            />
          </div>

          <div className="qa-form-group">
            <label className="qa-form-label">Nội dung chi tiết & Log lỗi</label>
            <textarea
              className="qa-form-textarea"
              rows={4}
              placeholder="Mô tả cụ thể thắc mắc, mã lỗi gặp phải hoặc câu hỏi bạn cần giải đáp..."
              value={newContent}
              onChange={(e) => setNewContent(e.target.value)}
              required
            />
          </div>

          <div className="qa-form-actions">
            <button
              type="button"
              className="qa-btn-cancel"
              onClick={() => setShowAskForm(false)}
            >
              Hủy
            </button>
            <button
              type="submit"
              className="qa-btn-submit"
              disabled={submittingQuestion || !newTitle.trim() || !newContent.trim()}
            >
              <Send size={15} />
              {submittingQuestion ? 'Đang gửi...' : 'Gửi câu hỏi'}
            </button>
          </div>
        </form>
      )}

      {/* Question List */}
      {error && (
        <div className="qa-alert-error">
          <AlertCircle size={16} /> {error}
        </div>
      )}

      {loading ? (
        <div className="qa-loading-state">
          <div className="qa-spinner"></div>
          <span>Đang tải các câu hỏi thảo luận...</span>
        </div>
      ) : filteredQuestions.length === 0 ? (
        <div className="qa-empty-state">
          <div className="qa-empty-icon-wrap">
            <MessageSquare size={36} />
          </div>
          <h4>Chưa có câu hỏi nào trong khóa học này</h4>
          <p>
            Bạn gặp khó khăn hay thắc mắc về bài học? Hãy đặt câu hỏi ngay để được giảng viên và các bạn học viên hỗ trợ!
          </p>
          <button
            type="button"
            className="qa-btn-ask"
            onClick={() => setShowAskForm(true)}
          >
            <Plus size={16} /> Đặt câu hỏi đầu tiên
          </button>
        </div>
      ) : (
        <div className="qa-questions-list">
          {filteredQuestions.map((q) => (
            <div
              key={q.questionId}
              className={`qa-question-card ${q.isResolved ? 'resolved' : ''}`}
              onClick={() => openThread(q.questionId)}
            >
              <div className="qa-card-header">
                <div className="qa-author-info">
                  <div className="qa-avatar">
                    {q.askerAvatar ? (
                      <img src={q.askerAvatar} alt={q.askerName} />
                    ) : (
                      <User size={16} />
                    )}
                  </div>
                  <div>
                    <span className="qa-author-name">{q.askerName || 'Học viên'}</span>
                    <span className="qa-post-time">{formatTime(q.createdAt)}</span>
                  </div>
                </div>

                <div className="qa-badges-group">
                  {q.isResolved ? (
                    <span className="qa-badge resolved">
                      <CheckCircle2 size={13} /> Đã giải quyết
                    </span>
                  ) : (
                    <span className="qa-badge unanswered">
                      <Clock size={13} /> Chờ giải đáp
                    </span>
                  )}
                  {q.answerCount > 0 && (
                    <span className="qa-badge replies-count">
                      <MessageSquare size={13} /> {q.answerCount} câu trả lời
                    </span>
                  )}
                </div>
              </div>

              <h4 className="qa-card-title">{q.title}</h4>
              <p className="qa-card-snippet">{q.contentSnippet}</p>

              <div className="qa-card-footer">
                <span className="qa-btn-view-thread">
                  Xem thảo luận & trả lời &rarr;
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal / Thread Drawer for Reading and Replying */}
      {selectedQuestionId && (
        <div className="qa-thread-overlay" onClick={closeThread}>
          <div className="qa-thread-modal" onClick={(e) => e.stopPropagation()}>
            <div className="qa-thread-header">
              <div className="qa-thread-title-area">
                <span className="qa-thread-course-tag">Khóa học: {threadData?.courseTitle}</span>
                <h3 className="qa-thread-title">{threadData?.title || 'Đang tải...'}</h3>
              </div>
              <button type="button" className="qa-thread-close-btn" onClick={closeThread}>
                <X size={20} />
              </button>
            </div>

            {loadingThread ? (
              <div className="qa-loading-state" style={{ padding: '3rem' }}>
                <div className="qa-spinner"></div>
                <span>Đang tải nội dung thảo luận...</span>
              </div>
            ) : threadData ? (
              <div className="qa-thread-body">
                {/* Asker Question Post */}
                <div className="qa-thread-original-post">
                  <div className="qa-post-author-row">
                    <div className="qa-avatar large">
                      <User size={20} />
                    </div>
                    <div className="qa-post-author-meta">
                      <strong className="qa-name">{threadData.askerName || 'Học viên'}</strong>
                      <span className="qa-time">{formatTime(threadData.createdAt)}</span>
                    </div>

                    <div className="qa-resolve-toggle-wrap">
                      <button
                        type="button"
                        className={`qa-btn-toggle-resolve ${threadData.isResolved ? 'is-resolved' : ''}`}
                        onClick={() => handleToggleResolve(threadData.questionId)}
                        title="Nhấn để đổi trạng thái đã giải quyết"
                      >
                        <CheckCircle2 size={16} />
                        {threadData.isResolved ? 'Đã giải quyết (Nhấn để mở lại)' : 'Đánh dấu đã giải quyết'}
                      </button>
                    </div>
                  </div>

                  <div className="qa-post-content-body">
                    {threadData.content}
                  </div>
                </div>

                {/* Answers List */}
                <div className="qa-answers-section">
                  <h4 className="qa-answers-headline">
                    <MessageSquare size={16} /> Câu trả lời & Thảo luận ({threadData.answers?.length || 0})
                  </h4>

                  {(!threadData.answers || threadData.answers.length === 0) ? (
                    <div className="qa-no-answers-box">
                      Chưa có câu trả lời nào. Hãy là người đầu tiên chia sẻ giải pháp!
                    </div>
                  ) : (
                    threadData.answers.map((ans) => {
                      const isInstructor = ans.authorType === 'INSTRUCTOR';
                      return (
                        <div
                          key={ans.answerId}
                          className={`qa-answer-card ${isInstructor ? 'instructor-reply' : ''}`}
                        >
                          <div className="qa-answer-header">
                            <div className="qa-avatar">
                              {isInstructor ? (
                                <ShieldCheck size={18} color="#3b82f6" />
                              ) : (
                                <User size={16} />
                              )}
                            </div>
                            <div className="qa-answer-author-meta">
                              <span className="qa-name">
                                {ans.authorName || (isInstructor ? 'Giảng viên' : 'Học viên')}
                              </span>
                              {isInstructor && (
                                <span className="qa-badge-instructor">
                                  Giảng viên / Ban đào tạo
                                </span>
                              )}
                              <span className="qa-time">{formatTime(ans.createdAt)}</span>
                            </div>
                          </div>

                          <div className="qa-answer-content">
                            {ans.content}
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>

                {/* Reply Input Box */}
                <form className="qa-reply-box" onSubmit={handlePostReply}>
                  <label className="qa-reply-label">Để lại câu trả lời hoặc thảo luận của bạn:</label>
                  <textarea
                    className="qa-reply-textarea"
                    rows={3}
                    placeholder="Nhập câu trả lời, chia sẻ kinh nghiệm hoặc đoạn mã giúp đỡ bạn học..."
                    value={replyContent}
                    onChange={(e) => setReplyContent(e.target.value)}
                    required
                  />
                  <div className="qa-reply-actions">
                    <button
                      type="submit"
                      className="qa-btn-send-reply"
                      disabled={submittingReply || !replyContent.trim()}
                    >
                      <Send size={15} />
                      {submittingReply ? 'Đang gửi phản hồi...' : 'Gửi câu trả lời'}
                    </button>
                  </div>
                </form>
              </div>
            ) : null}
          </div>
        </div>
      )}
    </div>
  );
}
