import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { User, Mail, Lock, Eye, EyeOff, AlertCircle, ArrowRight, CheckCircle2 } from 'lucide-react';
import './Auth.css';

function Register() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const validate = () => {
    const errors = {};
    if (!fullName.trim()) {
      errors.fullName = 'Vui lòng nhập họ và tên của bạn.';
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim()) {
      errors.email = 'Vui lòng nhập địa chỉ email.';
    } else if (!emailRegex.test(email)) {
      errors.email = 'Email không hợp lệ. Ví dụ: student@edumy.com';
    }
    if (!password) {
      errors.password = 'Vui lòng thiết lập mật khẩu.';
    } else if (password.length < 6) {
      errors.password = 'Mật khẩu phải chứa ít nhất 6 ký tự bảo mật.';
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!validate() || isSubmitting) return;

    setIsSubmitting(true);
    setError('');
    try {
      const normalizedEmail = email.trim().toLowerCase();
      await api.post('/auth/register', { 
        fullName: fullName.trim(), 
        email: normalizedEmail, 
        password,
        role: 'Student'
      });
      navigate(`/verify-email?email=${encodeURIComponent(normalizedEmail)}&token=VERIFY-NEW-ACCOUNT`);
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng ký tài khoản thất bại. Email có thể đã được đăng ký trước đó.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        {/* Brand Header */}
        <div className="auth-brand-header">
          <Link to="/" className="auth-logo">
            <div className="auth-logo-box">E</div>
            <span className="auth-logo-text">Edu<span>my</span></span>
          </Link>
          <h2 className="auth-title">Đăng ký tài khoản</h2>
          <p className="auth-subtitle">
            Gia nhập cộng đồng người học Edumy và nâng tầm sự nghiệp tương lai
          </p>
        </div>

        {/* Google OAuth Button */}
        <div className="google-auth-wrapper">
          <button
            type="button"
            className="google-custom-btn"
            onClick={() => {
              setError('Đang kết nối đăng ký nhanh qua Google OAuth...');
              setTimeout(() => {
                setError('');
              }, 2500);
            }}
          >
            <svg className="google-icon-svg" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
            </svg>
            <span>Đăng ký nhanh với Google</span>
          </button>
        </div>

        {/* Divider */}
        <div className="auth-divider">hoặc đăng ký bằng email</div>

        {/* Error Alert */}
        {error && (
          <div className="auth-alert-error" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        <form className="auth-form" onSubmit={handleRegister} noValidate>
          {/* Full Name */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-fullname">Họ và tên</label>
            <div className="input-with-icon">
              <User size={18} className="input-icon-left" />
              <input 
                id="reg-fullname"
                type="text" 
                value={fullName}
                onChange={(e) => {
                  setFullName(e.target.value);
                  if (fieldErrors.fullName) setFieldErrors(prev => ({ ...prev, fullName: null }));
                }}
                placeholder="ví dụ: Nguyễn Văn An"
                className={`auth-input ${fieldErrors.fullName ? 'input-error' : ''}`}
                autoComplete="name"
              />
            </div>
            {fieldErrors.fullName && (
              <div className="field-error-text">
                <AlertCircle size={14} /> {fieldErrors.fullName}
              </div>
            )}
          </div>

          {/* Email */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-email">Địa chỉ Email</label>
            <div className="input-with-icon">
              <Mail size={18} className="input-icon-left" />
              <input 
                id="reg-email"
                type="email" 
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (fieldErrors.email) setFieldErrors(prev => ({ ...prev, email: null }));
                }}
                placeholder="ví dụ: an.nguyen@example.com"
                className={`auth-input ${fieldErrors.email ? 'input-error' : ''}`}
                autoComplete="email"
              />
            </div>
            {fieldErrors.email && (
              <div className="field-error-text">
                <AlertCircle size={14} /> {fieldErrors.email}
              </div>
            )}
          </div>

          {/* Password */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-password">Thiết lập Mật khẩu</label>
            <div className="input-with-icon">
              <Lock size={18} className="input-icon-left" />
              <input 
                id="reg-password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (fieldErrors.password) setFieldErrors(prev => ({ ...prev, password: null }));
                }}
                placeholder="Tối thiểu 6 ký tự bảo mật"
                className={`auth-input ${fieldErrors.password ? 'input-error' : ''}`}
                autoComplete="new-password"
              />
              <button
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowPassword(prev => !prev)}
                title={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {fieldErrors.password && (
              <div className="field-error-text">
                <AlertCircle size={14} /> {fieldErrors.password}
              </div>
            )}
          </div>

          {/* Submit */}
          <button 
            type="submit" 
            className="auth-submit-btn"
            disabled={isSubmitting}
          >
            {isSubmitting ? (
              'Đang tạo tài khoản...'
            ) : (
              <>
                <span>Đăng ký tài khoản</span>
                <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        <div className="auth-footer-text">
          Đã có tài khoản? <Link to="/login">Đăng nhập ngay</Link>
        </div>
      </div>
    </div>
  );
}

export default Register;
