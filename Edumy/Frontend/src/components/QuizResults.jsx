import React from 'react';
import { RotateCcw, CheckCircle, XCircle } from 'lucide-react';

function QuizResults({ result, quizTitle, onRetry, darkMode = true }) {
  if (!result) return null;

  const isPassed = result.passed;
  const passColor = isPassed ? '#10b981' : '#ef4444';

  const containerStyle = darkMode ? {
    backgroundColor: '#0f172a',
    color: '#f8fafc',
    border: '1px solid #1e293b'
  } : {};

  return (
    <div 
      className={`quiz-results p-4 rounded shadow-sm h-100 overflow-auto ${darkMode ? '' : 'bg-white text-dark'}`}
      style={containerStyle}
    >
      <div className="text-center mb-5 mt-2">
        <div className="mb-2">
          {isPassed ? (
            <CheckCircle size={48} color="#10b981" style={{ margin: '0 auto' }} />
          ) : (
            <XCircle size={48} color="#ef4444" style={{ margin: '0 auto' }} />
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
        <h4 style={{ color: passColor, fontSize: '1.1rem', fontWeight: 600 }}>
          {isPassed 
            ? 'Chúc mừng bạn! Bạn đã vượt qua bài kiểm tra xuất sắc.' 
            : 'Bạn chưa đạt điểm yêu cầu. Hãy ôn tập lại bài học và thử lại nhé!'}
        </h4>
        
        {!isPassed && (
          <button 
            className="btn-edumy-primary mt-3 touch-target"
            onClick={onRetry}
            style={{ 
              backgroundColor: darkMode ? '#1e293b' : '#ffffff', 
              borderColor: darkMode ? '#334155' : '#cbd5e1',
              color: darkMode ? '#f8fafc' : '#0f172a',
              minHeight: '44px'
            }}
          >
            <RotateCcw size={16} /> Làm lại bài kiểm tra
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
            className="card mb-3 shadow-sm"
            style={{
              backgroundColor: darkMode ? '#1e293b' : '#ffffff',
              border: darkMode ? '1px solid #334155' : '1px solid #e2e8f0',
              borderLeft: `4px solid ${r.isCorrect ? '#10b981' : '#ef4444'}`,
              color: darkMode ? '#f8fafc' : '#0f172a'
            }}
          >
            <div className="card-body">
              <h5 className="card-title d-flex align-items-center justify-content-between">
                <span>Câu hỏi {i + 1}</span>
                {r.isCorrect ? (
                  <span className="badge" style={{ backgroundColor: 'rgba(16, 185, 129, 0.15)', color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
                    Chính xác
                  </span>
                ) : (
                  <span className="badge" style={{ backgroundColor: 'rgba(239, 68, 68, 0.15)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
                    Chưa chính xác
                  </span>
                )}
              </h5>
              {r.explanation && (
                <div 
                  className="mt-3 p-3 rounded"
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
