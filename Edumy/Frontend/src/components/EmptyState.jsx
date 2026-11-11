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
      <div className="empty-state-icon-wrapper" aria-hidden="true">
        <Icon size={38} strokeWidth={1.5} className="empty-state-icon" />
      </div>
      <h3 className="empty-state-title">{title}</h3>
      <p className="empty-state-description">{description}</p>
      
      <div className="empty-state-actions">
        {actionText && actionLink && (
          <Link
            to={actionLink}
            className="empty-state-btn btn-solid-primary"
          >
            <span>{actionText}</span>
            <ArrowRight size={16} strokeWidth={1.75} aria-hidden="true" />
          </Link>
        )}
        {actionText && onAction && !actionLink && (
          <button
            type="button"
            onClick={onAction}
            className="empty-state-btn btn-solid-primary"
          >
            <span>{actionText}</span>
            <RefreshCw size={16} strokeWidth={1.75} aria-hidden="true" />
          </button>
        )}
        {secondaryActionText && onSecondaryAction && (
          <button
            type="button"
            onClick={onSecondaryAction}
            className="empty-state-secondary-btn btn-outline-neutral"
          >
            {secondaryActionText}
          </button>
        )}
      </div>
    </div>
  );
};

export default EmptyState;
