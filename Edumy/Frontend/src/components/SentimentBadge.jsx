import React from 'react';
import './SentimentBadge.css';

const SentimentBadge = ({ sentiment, label, score }) => {
  const normalized = (sentiment || label || '').toUpperCase();

  let type = 'neutral';
  let text = '💬 Trung tính';

  if (
    normalized === 'POSITIVE' ||
    normalized === 'TÍCH CỰC' ||
    normalized === 'TICH CUC'
  ) {
    type = 'positive';
    text = '🌿 Tích cực';
  } else if (
    normalized === 'NEGATIVE' ||
    normalized === 'TIÊU CỰC' ||
    normalized === 'TIEU CUC' ||
    normalized === 'CẦN CẢI THIỆN' ||
    normalized === 'CAN CAI THIEN'
  ) {
    type = 'negative';
    text = '⚠️ Cần cải thiện';
  }

  return (
    <span
      className={`sentiment-badge sentiment-${type}`}
      title={score ? `Điểm AI đánh giá: ${Math.round(score * 100)}%` : 'AI phân tích cảm xúc'}
    >
      {text}
    </span>
  );
};

export default SentimentBadge;
