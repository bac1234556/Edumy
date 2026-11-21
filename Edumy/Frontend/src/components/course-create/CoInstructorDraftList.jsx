import React, { useState } from 'react';
import { UserPlus, X } from 'lucide-react';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function CoInstructorDraftList({ emails, onChange, disabled = false }) {
  const [inputEmail, setInputEmail] = useState('');
  const [emailError, setEmailError] = useState('');

  const handleAddEmail = () => {
    const trimmed = inputEmail.trim();
    if (!trimmed) {
      setEmailError('Vui lòng nhập địa chỉ email đồng giảng viên.');
      return;
    }
    if (!EMAIL_REGEX.test(trimmed)) {
      setEmailError('Định dạng email không hợp lệ.');
      return;
    }
    const normalized = trimmed.toLowerCase();
    if (emails.some((item) => item.toLowerCase() === normalized)) {
      setEmailError('Email này đã có trong danh sách mời.');
      return;
    }

    onChange([...emails, trimmed]);
    setInputEmail('');
    setEmailError('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddEmail();
    }
  };

  const handleRemoveEmail = (emailToRemove) => {
    onChange(emails.filter((item) => item !== emailToRemove));
  };

  return (
    <div className="co-instructor-draft-section">
      <div className="mb-3">
        <h3 className="form-section-title mb-1 border-0 pb-0">
          4. Đồng giảng viên (Tùy chọn)
        </h3>
        <p className="section-card-subtitle">
          Nhập email của giảng viên khác để gửi lời mời cộng tác sau khi khóa học được tạo.
        </p>
      </div>

      <div className="co-instructor-input-row">
        <input
          type="email"
          className={`studio-input ${emailError ? 'is-invalid-field' : ''}`}
          placeholder="VD: coinstructor@edumy.com"
          value={inputEmail}
          onChange={(e) => {
            setInputEmail(e.target.value);
            if (emailError) setEmailError('');
          }}
          onKeyDown={handleKeyDown}
          disabled={disabled}
        />
        <button
          type="button"
          className="btn-outline-studio co-instructor-add-btn"
          onClick={handleAddEmail}
          disabled={disabled}
        >
          <UserPlus size={16} /> Thêm email
        </button>
      </div>

      {emailError && <span className="field-error-text mt-1 d-block">{emailError}</span>}

      {emails.length > 0 && (
        <div className="co-instructor-chip-list">
          {emails.map((email) => (
            <span key={email} className="co-instructor-chip">
              <span>{email}</span>
              <button
                type="button"
                className="co-instructor-chip-remove"
                onClick={() => handleRemoveEmail(email)}
                disabled={disabled}
                aria-label={`Xóa ${email}`}
                title="Xóa khỏi danh sách"
              >
                <X size={14} />
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}

export default CoInstructorDraftList;
