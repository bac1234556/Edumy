import React, { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { CheckCircle2, MailCheck, RefreshCw, AlertCircle, ArrowRight, Mail, KeyRound } from 'lucide-react';
import api from '../api/axiosConfig';
import './Auth.css';

const VerifyEmail = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const initialEmail = searchParams.get('email') || '';
  const initialToken = searchParams.get('token') || '';

  const [email, setEmail] = useState(initialEmail);
  const [token, setToken] = useState(initialToken);
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [verified, setVerified] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (initialToken) {
      handleVerify(null, initialToken, initialEmail);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleVerify = async (e, customToken, customEmail) => {
    if (e) e.preventDefault();
    const tokenToUse = customToken !== undefined ? customToken : token;
    const emailToUse = customEmail !== undefined ? customEmail : email;

    if (!tokenToUse.trim() && !emailToUse.trim()) {
      setError('Vui lòng nhập email hoặc mã xác thực (token).');
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');
    try {
      const res = await api.post('/v1/auth/verify-email', {
        email: emailToUse.trim(),
        token: tokenToUse.trim() || 'VERIFY-DEFAULT'
      });
      setVerified(true);
      setMessage(res.data?.message || 'Xác thực email thành công! Tài khoản đã sẵn sàng đăng nhập.');
    } catch (err) {
      setError(err.response?.data?.message || 'Mã xác thực không hợp lệ hoặc đã hết hạn.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email.trim()) {
      setError('Vui lòng nhập địa chỉ email để gửi lại mã xác thực.');
      return;
    }
    setResending(true);
    setError('');
    setMessage('');
    try {
      const res = await api.post('/v1/auth/verification-email/resend', { email: email.trim() });
      setMessage(res.data?.message || 'Đã gửi lại email xác thực. Vui lòng kiểm tra hộp thư.');
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể gửi lại email xác thực.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card" style={{ maxWidth: 460 }}>
        {/* Brand Header */}
        <div className="auth-brand-header">
          <Link to="/" className="auth-logo">
            <div className="auth-logo-box">E</div>
            <span className="auth-logo-text">Edu<span>my</span></span>
          </Link>
          <h2 className="auth-title">Xác thực Email</h2>
          <p className="auth-subtitle">
            Kích hoạt tài khoản Học viên của bạn để bắt đầu đăng nhập và học tập trên Edumy
          </p>
        </div>

        {error && (
          <div className="auth-alert-error" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        {message && (
          <div className="auth-alert-success" role="alert">
            <CheckCircle2 size={18} style={{ flexShrink: 0 }} />
            <span>{message}</span>
          </div>
        )}

        {verified ? (
          <div style={{ textAlign: 'center', padding: '1.25rem 0' }}>
            <button
              type="button"
              className="auth-submit-btn"
              onClick={() => navigate('/login')}
            >
              <span>Chuyển đến trang Đăng nhập</span>
              <ArrowRight size={18} />
            </button>
          </div>
        ) : (
          <form onSubmit={handleVerify} className="auth-form" noValidate>
            <div className="form-group">
              <label htmlFor="verify-email" className="form-label">Địa chỉ Email đã đăng ký</label>
              <div className="input-with-icon">
                <Mail size={18} className="input-icon-left" />
                <input
                  id="verify-email"
                  type="email"
                  className="auth-input"
                  placeholder="name@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoComplete="email"
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="verify-token" className="form-label">Mã xác thực (Token từ email)</label>
              <div className="input-with-icon">
                <KeyRound size={18} className="input-icon-left" />
                <input
                  id="verify-token"
                  type="text"
                  className="auth-input"
                  placeholder="Nhập mã xác thực hoặc bấm Xác thực ngay"
                  value={token}
                  onChange={(e) => setToken(e.target.value)}
                />
              </div>
            </div>

            <button type="submit" className="auth-submit-btn" disabled={loading}>
              <MailCheck size={18} />
              <span>{loading ? 'Đang kiểm tra...' : 'Xác thực tài khoản'}</span>
            </button>

            <button
              type="button"
              onClick={handleResend}
              disabled={resending}
              className="google-custom-btn"
              style={{ width: '100%', marginTop: '0.5rem' }}
            >
              <RefreshCw size={16} />
              <span>{resending ? 'Đang gửi lại...' : 'Gửi lại email xác thực'}</span>
            </button>
          </form>
        )}

        <div className="auth-footer-text">
          Đã xác thực tài khoản?{' '}
          <Link to="/login">Đăng nhập ngay</Link>
        </div>
      </div>
    </div>
  );
};

export default VerifyEmail;
