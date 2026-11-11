import React from 'react';
import { Smile, Frown, Meh } from 'lucide-react';
import './SentimentBadge.css';

const SentimentBadge = ({ sentiment, label, score }) => {
  const normalized = (sentiment || label || '').toUpperCase();

  let type = 'neutral';
  let text = 'Trung tính';
  let IconComponent = Meh;

  if (
    normalized === 'POSITIVE' ||
    normalized === 'POS' ||
    normalized === 'TÍCH CỰC' ||
    normalized === 'TICH CUC'
  ) {
    type = 'positive';
    text = 'Tích cực';
    IconComponent = Smile;
  } else if (
    normalized === 'NEGATIVE' ||
    normalized === 'NEG' ||
    normalized === 'TIÊU CỰC' ||
    normalized === 'TIEU CUC' ||
    normalized === 'CẦN CẢI THIỆN' ||
    normalized === 'CAN CAI THIEN'
  ) {
    type = 'negative';
    text = 'Cần cải thiện';
    IconComponent = Frown;
  }

  return (
    <span
      className={`sentiment-badge sentiment-${type} pill-badge`}
      title={score ? `Điểm AI đánh giá: ${Math.round(score * 100)}%` : 'AI phân tích cảm xúc'}
    >
      <IconComponent size={13} strokeWidth={2} aria-hidden="true" />
      <span>{text}</span>
    </span>
  );
};

export default SentimentBadge;
