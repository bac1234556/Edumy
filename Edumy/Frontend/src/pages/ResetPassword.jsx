import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Mail, KeyRound, Lock, Eye, EyeOff, AlertCircle, CheckCircle2, ArrowRight } from 'lucide-react';
import './Auth.css';

const ResetPassword = () => {
  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  // Pre-fill query params if redirected from ForgotPassword dev box
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const emailParam = params.get('email');
    const tokenParam = params.get('token');
    if (emailParam) setEmail(emailParam);
    if (tokenParam) setToken(tokenParam);
  }, [location]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');

    try {
      const response = await api.post('/auth/reset-password', {
        email: email.trim().toLowerCase(),
        token: token.trim(),
        newPassword
      });
      setMessage(response.data?.message || 'Đặt lại mật khẩu thành công!');
      setTimeout(() => {
        navigate('/login');
      }, 2500);
    } catch (err) {
      setError(err.response?.data?.message || 'Đặt lại mật khẩu thất bại. Vui lòng kiểm tra mã xác thực hoặc thử lại.');
    } finally {
      setLoading(false);
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
          <h2 className="auth-title">Đặt lại mật khẩu</h2>
          <p className="auth-subtitle">
            Xác thực mã bảo mật và thiết lập mật khẩu mới an toàn cho tài khoản
          </p>
        </div>

        {/* Message / Error alerts */}
        {message && (
          <div className="auth-alert-success" role="alert">
            <CheckCircle2 size={18} style={{ flexShrink: 0 }} />
            <span>{message} Đang chuyển hướng về trang Đăng nhập...</span>
          </div>
        )}

        {error && (
          <div className="auth-alert-error" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          {/* Email */}
          <div className="form-group">
            <label className="form-label" htmlFor="reset-email">Địa chỉ Email</label>
            <div className="input-with-icon">
              <Mail size={18} className="input-icon-left" />
              <input
                type="email"
                id="reset-email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="auth-input"
                required
                autoComplete="email"
              />
            </div>
          </div>

          {/* Token */}
          <div className="form-group">
            <label className="form-label" htmlFor="reset-token">Mã xác thực (Token)</label>
            <div className="input-with-icon">
              <KeyRound size={18} className="input-icon-left" />
              <input
                type="text"
                id="reset-token"
                placeholder="ví dụ: A1B2C3D4"
                value={token}
                onChange={(e) => setToken(e.target.value)}
                className="auth-input"
                required
              />
            </div>
          </div>

          {/* New password */}
          <div className="form-group">
            <label className="form-label" htmlFor="reset-newpassword">Mật khẩu mới</label>
            <div className="input-with-icon">
              <Lock size={18} className="input-icon-left" />
              <input
                type={showPassword ? 'text' : 'password'}
                id="reset-newpassword"
                placeholder="Tối thiểu 6 ký tự bảo mật"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="auth-input"
                required
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
          </div>

          <button 
            type="submit" 
            className="auth-submit-btn" 
            disabled={loading}
          >
            {loading ? (
              'Đang xử lý...'
            ) : (
              <>
                <span>Xác nhận đặt lại mật khẩu</span>
                <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        <div className="auth-footer-text">
          Đã nhớ mật khẩu? <Link to="/login">Quay lại đăng nhập</Link>
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
