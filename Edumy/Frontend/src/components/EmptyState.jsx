import React from 'react';
import { Link } from 'react-router-dom';
import { Inbox, ArrowRight, RefreshCw } from 'lucide-react';
import './EmptyState.css';

const EmptyState = ({
  icon: Icon = Inbox,
  title = "Không tìm thấy dữ liệu",
  description = "Hiện chưa có thông tin nào được hiển thị trong mục này.",
  actionText,
  actionLink,
  onAction,
  secondaryActionText,
  onSecondaryAction,
  className = ""
}) => {
  return (
    <div className={`edumy-empty-state ${className}`}>
      <div className="empty-state-icon-wrapper">
        <Icon size={42} strokeWidth={1.75} className="empty-state-icon" />
      </div>
      <h3 className="empty-state-title">{title}</h3>
      <p className="empty-state-description">{description}</p>
      
      <div className="empty-state-actions">
        {actionText && actionLink && (
          <Link to={actionLink} className="btn btn-primary rounded-pill px-4 py-2 fw-semibold empty-state-btn">
            {actionText} <ArrowRight size={16} className="ms-1" />
          </Link>
        )}
        {actionText && onAction && !actionLink && (
          <button onClick={onAction} className="btn btn-primary rounded-pill px-4 py-2 fw-semibold empty-state-btn">
            {actionText} <RefreshCw size={16} className="ms-1" />
          </button>
        )}
        {secondaryActionText && onSecondaryAction && (
          <button onClick={onSecondaryAction} className="btn btn-outline-secondary rounded-pill px-4 py-2 fw-semibold">
            {secondaryActionText}
          </button>
        )}
      </div>
    </div>
  );
};

export default EmptyState;
