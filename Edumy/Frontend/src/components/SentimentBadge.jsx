import React from 'react';
import { ThumbsUp, MessageCircle, AlertTriangle } from 'lucide-react';
import './SentimentBadge.css';

const SentimentBadge = ({ sentiment, label, score }) => {
  const normalized = (sentiment || label || '').toUpperCase();

  let type = 'neutral';
  let text = 'Trung tính';
  let IconComponent = MessageCircle;

  if (
    normalized === 'POSITIVE' ||
    normalized === 'TÍCH CỰC' ||
    normalized === 'TICH CUC'
  ) {
    type = 'positive';
    text = 'Tích cực';
    IconComponent = ThumbsUp;
  } else if (
    normalized === 'NEGATIVE' ||
    normalized === 'TIÊU CỰC' ||
    normalized === 'TIEU CUC' ||
    normalized === 'CẦN CẢI THIỆN' ||
    normalized === 'CAN CAI THIEN'
  ) {
    type = 'negative';
    text = 'Cần cải thiện';
    IconComponent = AlertTriangle;
  }

  return (
    <span
      className={`sentiment-badge sentiment-${type} pill-badge`}
      title={score ? `Điểm AI đánh giá: ${Math.round(score * 100)}%` : 'AI phân tích cảm xúc'}
    >
      <IconComponent size={12} strokeWidth={2} aria-hidden="true" />
      <span>{text}</span>
    </span>
  );
};

export default SentimentBadge;
