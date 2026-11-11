import React from 'react';
import { RotateCcw, CheckCircle2, XCircle } from 'lucide-react';

function QuizResults({ result, quizTitle, onRetry, darkMode = true }) {
  if (!result) return null;

  const isPassed = result.passed;
  const passColor = isPassed ? '#10b981' : '#ef4444';

  const containerStyle = darkMode ? {
    backgroundColor: '#0f172a',
    color: '#f8fafc',
    border: '1px solid #1e293b',
    borderRadius: '1rem'
  } : {
    backgroundColor: '#ffffff',
    color: '#0f172a',
    border: '1px solid rgba(226, 232, 240, 0.85)',
    borderRadius: '1rem'
  };

  return (
    <div 
      className="quiz-results p-4 p-md-5 rounded-2xl shadow-sm h-100 overflow-auto"
      style={containerStyle}
    >
      <div className="text-center mb-5 mt-2">
        <div className="mb-3">
          {isPassed ? (
            <CheckCircle2 size={48} strokeWidth={1.75} color="#10b981" style={{ margin: '0 auto' }} aria-hidden="true" />
          ) : (
            <XCircle size={48} strokeWidth={1.75} color="#ef4444" style={{ margin: '0 auto' }} aria-hidden="true" />
          )}
        </div>
        <h2 className="mb-2" style={{ color: darkMode ? '#f8fafc' : '#0f172a', fontWeight: 800 }}>
          Kết quả: {quizTitle}
        </h2>
        <div 
          className="display-4 fw-bold mb-2"
          style={{ color: passColor, letterSpacing: '-0.03em' }}
        >
          {result.score} / {result.totalPoints}
        </div>
        <h4 style={{ color: passColor, fontSize: '1.05rem', fontWeight: 600 }}>
          {isPassed 
            ? 'Chúc mừng bạn! Bạn đã vượt qua bài kiểm tra xuất sắc.' 
            : 'Bạn chưa đạt điểm yêu cầu. Hãy ôn tập lại bài học và thử lại nhé!'}
        </h4>
        
        {!isPassed && (
          <button 
            type="button"
            className="btn-edumy-primary mt-3 touch-target cursor-pointer transition-all duration-200 active:scale-95"
            onClick={onRetry}
            style={{ 
              backgroundColor: darkMode ? '#1e293b' : '#ffffff', 
              borderColor: darkMode ? '#334155' : '#cbd5e1',
              color: darkMode ? '#f8fafc' : '#0f172a',
              borderRadius: '0.75rem',
              minHeight: '44px'
            }}
          >
            <RotateCcw size={16} strokeWidth={1.75} aria-hidden="true" /> Làm lại bài kiểm tra
          </button>
        )}
      </div>

      <div className="results-breakdown mt-4">
        <h4 
          className="mb-4 pb-2" 
          style={{ 
            color: darkMode ? '#f8fafc' : '#0f172a',
            borderBottom: darkMode ? '1px solid #1e293b' : '1px solid #e2e8f0',
            fontWeight: 700
          }}
        >
          Chi tiết từng câu hỏi
        </h4>
        {result.results?.map((r, i) => (
          <div 
            key={r.questionId || i} 
            className="card mb-3 rounded-xl shadow-sm"
            style={{
              backgroundColor: darkMode ? '#1e293b' : '#ffffff',
              border: darkMode ? '1px solid #334155' : '1px solid rgba(226, 232, 240, 0.85)',
              borderLeft: `4px solid ${r.isCorrect ? '#10b981' : '#ef4444'}`,
              color: darkMode ? '#f8fafc' : '#0f172a'
            }}
          >
            <div className="card-body p-4">
              <h5 className="card-title d-flex align-items-center justify-content-between mb-0">
                <span style={{ fontWeight: 700 }}>Câu hỏi {i + 1}</span>
                {r.isCorrect ? (
                  <span className="pill-badge" style={{ backgroundColor: darkMode ? 'rgba(16, 185, 129, 0.15)' : '#ecfdf5', color: darkMode ? '#34d399' : '#047857', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
                    <CheckCircle2 size={12} strokeWidth={2} aria-hidden="true" /> Chính xác
                  </span>
                ) : (
                  <span className="pill-badge" style={{ backgroundColor: darkMode ? 'rgba(239, 68, 68, 0.15)' : '#fef2f2', color: darkMode ? '#f87171' : '#b91c1c', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
                    <XCircle size={12} strokeWidth={2} aria-hidden="true" /> Chưa chính xác
                  </span>
                )}
              </h5>
              {r.explanation && (
                <div 
                  className="mt-3 p-3 rounded-xl"
                  style={{
                    backgroundColor: darkMode ? '#0f172a' : '#f8fafc',
                    color: darkMode ? '#cbd5e1' : '#64748b',
                    fontSize: '0.875rem',
                    border: darkMode ? '1px solid #1e293b' : '1px solid #e2e8f0'
                  }}
                >
                  <strong style={{ color: darkMode ? '#f8fafc' : '#334155' }}>Giải thích:</strong> {r.explanation}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default QuizResults;
