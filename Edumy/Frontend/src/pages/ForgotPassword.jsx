import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Mail, AlertCircle, CheckCircle2, ArrowRight, KeyRound, Wrench } from 'lucide-react';
import './Auth.css';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [devToken, setDevToken] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');
    setDevToken('');

    try {
      const response = await api.post('/auth/forgot-password', { email: email.trim().toLowerCase() });
      setMessage(response.data?.message || 'Mã xác thực khôi phục mật khẩu đã được tạo thành công.');
      if (response.data?.resetToken) {
        setDevToken(response.data.resetToken);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra. Vui lòng kiểm tra lại email hoặc thử lại sau.');
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
          <h2 className="auth-title">Quên mật khẩu</h2>
          <p className="auth-subtitle">
            Nhập email của bạn để nhận mã xác thực và đặt lại mật khẩu mới an toàn
          </p>
        </div>

        {/* Message / Error alerts */}
        {message && (
          <div className="auth-alert-success" role="alert">
            <CheckCircle2 size={18} style={{ flexShrink: 0 }} />
            <span>{message}</span>
          </div>
        )}

        {error && (
          <div className="auth-alert-error" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label className="form-label" htmlFor="forgot-email">Địa chỉ Email</label>
            <div className="input-with-icon">
              <Mail size={18} className="input-icon-left" />
              <input
                type="email"
                id="forgot-email"
                placeholder="ví dụ: student@edumy.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="auth-input"
                required
                autoComplete="email"
              />
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
                <span>Gửi mã xác thực</span>
                <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        {/* Dev Token Box */}
        {devToken && (
          <div className="dev-token-box">
            <div className="d-flex align-items-center gap-2 mb-2" style={{ color: '#b45309', fontWeight: 700, fontSize: '0.85rem' }}>
              <Wrench size={16} /> Chế độ thử nghiệm (Demo Token):
            </div>
            <p className="small text-muted mb-2">Hệ thống tạo mã xác thực khôi phục mật khẩu mẫu:</p>
            <div className="d-flex justify-content-between align-items-center bg-white p-2 border rounded-3">
              <code className="dev-token-badge">{devToken}</code>
              <button 
                onClick={() => navigate(`/reset-password?email=${encodeURIComponent(email)}&token=${devToken}`)}
                className="btn btn-sm btn-outline-primary fw-semibold"
                style={{ borderRadius: '8px', fontSize: '0.82rem' }}
              >
                Đặt lại mật khẩu ngay
              </button>
            </div>
          </div>
        )}

        <div className="auth-footer-text">
          Đã nhớ mật khẩu? <Link to="/login">Quay lại đăng nhập</Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
