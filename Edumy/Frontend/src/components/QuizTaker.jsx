import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { SkeletonBox } from './Skeleton';
import EmptyState from './EmptyState';
import { HelpCircle, CheckCircle2 } from 'lucide-react';

function QuizTaker({ quizId, onComplete, darkMode = true }) {
  const [quiz, setQuiz] = useState(null);
  const [loading, setLoading] = useState(true);
  const [answers, setAnswers] = useState({}); // questionId -> answerId
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchQuiz = async () => {
      try {
        const res = await api.get(`/quizzes/${quizId}`);
        setQuiz(res.data);
      } catch (err) {
        setError("Không thể tải bài kiểm tra trắc nghiệm.");
      } finally {
        setLoading(false);
      }
    };
    fetchQuiz();
  }, [quizId]);

  const handleSelectAnswer = (questionId, answerId) => {
    if (submitting) return;
    setAnswers({ ...answers, [questionId]: answerId });
  };

  const handleSubmit = async () => {
    if (submitting) return;
    setSubmitting(true);
    try {
      const res = await api.post(`/quizattempts/submit`, {
        quizId: quizId,
        selectedAnswers: answers
      });
      if (onComplete) onComplete(res.data);
    } catch (err) {
      setError("Nộp bài kiểm tra thất bại. Vui lòng thử lại.");
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className={`p-4 rounded shadow-sm ${darkMode ? 'bg-slate-900 text-slate-100' : 'bg-white'}`} style={darkMode ? { backgroundColor: '#0f172a', color: '#f8fafc', border: '1px solid #1e293b' } : {}}>
        <SkeletonBox width="50%" height="28px" className="mb-2" />
        <SkeletonBox width="80%" height="16px" className="mb-4" />
        <div className="card p-3 mb-3 border-0" style={{ backgroundColor: darkMode ? '#1e293b' : '#f8fafc' }}>
          <SkeletonBox width="30%" height="20px" className="mb-2" />
          <SkeletonBox width="70%" height="18px" className="mb-3" />
          <SkeletonBox width="100%" height="40px" className="mb-2" />
          <SkeletonBox width="100%" height="40px" />
        </div>
      </div>
    );
  }

  if (error || !quiz) {
    return (
      <EmptyState
        icon={HelpCircle}
        title="Không tìm thấy bài kiểm tra"
        description={error || "Bài trắc nghiệm chưa sẵn sàng hoặc không tồn tại."}
      />
    );
  }

  const containerStyle = darkMode ? {
    backgroundColor: '#0f172a',
    color: '#f8fafc',
    border: '1px solid #1e293b'
  } : {};

  const cardStyle = darkMode ? {
    backgroundColor: '#1e293b',
    borderColor: '#334155',
    color: '#f8fafc'
  } : {};

  const totalQuestions = quiz.questions?.length || 0;
  const answeredCount = Object.keys(answers).length;
  const allAnswered = answeredCount === totalQuestions && totalQuestions > 0;

  return (
    <div 
      className={`quiz-taker p-4 rounded shadow-sm h-100 overflow-auto ${darkMode ? '' : 'bg-white text-dark'}`}
      style={containerStyle}
    >
      <div className="d-flex justify-content-between align-items-center mb-2">
        <h3 className="mb-0" style={{ color: darkMode ? '#f8fafc' : '#0f172a', fontWeight: 800 }}>
          {quiz.title}
        </h3>
        <span 
          className="badge" 
          style={{ 
            backgroundColor: darkMode ? '#334155' : '#e2e8f0', 
            color: darkMode ? '#94a3b8' : '#475569',
            fontSize: '0.8rem',
            padding: '0.4rem 0.75rem',
            borderRadius: '9999px'
          }}
        >
          {answeredCount}/{totalQuestions} đã trả lời
        </span>
      </div>

      <p className="mb-4" style={{ color: darkMode ? '#cbd5e1' : '#64748b' }}>
        {quiz.description || "Hãy chọn một đáp án chính xác cho mỗi câu hỏi dưới đây."}
      </p>
      
      {quiz.questions?.map((q, i) => (
        <div 
          key={q.questionId} 
          className="card mb-4 border"
          style={cardStyle}
        >
          <div className="card-body">
            <h5 className="card-title mb-3 d-flex justify-content-between align-items-center" style={{ color: darkMode ? '#f8fafc' : '#0f172a' }}>
              <span>Câu hỏi {i + 1}</span>
              <span 
                className="badge" 
                style={{ 
                  backgroundColor: darkMode ? '#0f172a' : '#f1f5f9', 
                  color: darkMode ? '#818cf8' : '#4f46e5',
                  border: darkMode ? '1px solid #334155' : '1px solid #cbd5e1'
                }}
              >
                {q.points} điểm
              </span>
            </h5>
            <p className="card-text fw-medium" style={{ color: darkMode ? '#e2e8f0' : '#334155' }}>
              {q.content}
            </p>
            <div className="d-flex flex-column gap-2 mt-3">
              {q.answers?.map(a => {
                const isSelected = answers[q.questionId] === a.answerId;
                const optionStyle = darkMode ? {
                  backgroundColor: isSelected ? '#4f46e5' : '#0f172a',
                  borderColor: isSelected ? '#6366f1' : '#334155',
                  color: isSelected ? '#ffffff' : '#cbd5e1',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                } : {
                  cursor: 'pointer'
                };

                return (
                  <label 
                    key={a.answerId} 
                    className={`p-3 rounded border cursor-pointer d-flex align-items-center justify-content-between ${
                      !darkMode && (isSelected ? 'bg-primary text-white border-primary' : 'bg-white border-secondary')
                    }`}
                    style={optionStyle}
                  >
                    <div className="d-flex align-items-center gap-2">
                      <input 
                        type="radio" 
                        name={`q-${q.questionId}`} 
                        className="d-none"
                        checked={isSelected}
                        onChange={() => handleSelectAnswer(q.questionId, a.answerId)}
                      />
                      <span>{a.content}</span>
                    </div>
                    {isSelected && <CheckCircle2 size={16} />}
                  </label>
                );
              })}
            </div>
          </div>
        </div>
      ))}

      <div className="d-flex justify-content-between align-items-center mt-4 pt-3" style={{ borderTop: darkMode ? '1px solid #1e293b' : '1px solid #e2e8f0' }}>
        <span style={{ fontSize: '0.85rem', color: darkMode ? '#94a3b8' : '#64748b' }}>
          {allAnswered ? 'Đã hoàn thành toàn bộ câu hỏi. Sẵn sàng nộp bài!' : 'Vui lòng chọn đáp án cho tất cả câu hỏi trước khi nộp.'}
        </span>
        <button 
          className="btn-edumy-primary touch-target"
          style={{ minHeight: '44px', padding: '0 1.5rem', fontWeight: 700 }}
          onClick={handleSubmit}
          disabled={submitting || !allAnswered}
        >
          {submitting ? (
            <>
              <span className="btn-spinner"></span>
              Đang chấm điểm...
            </>
          ) : (
            'Nộp bài kiểm tra'
          )}
        </button>
      </div>
    </div>
  );
}

export default QuizTaker;
