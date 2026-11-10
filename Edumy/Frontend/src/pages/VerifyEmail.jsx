import React, { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { CheckCircle2, MailCheck, RefreshCw, AlertCircle, ArrowRight } from 'lucide-react';
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
    <div className="auth-wrapper">
      <div className="auth-card" style={{ maxWidth: 480 }}>
        <div className="auth-brand-header">
          <Link to="/" className="auth-logo">
            <div className="auth-logo-box">E</div>
            <span className="auth-logo-text">Edu<span>my</span></span>
          </Link>
          <h1 className="auth-title">Xác thực Email (UC-G06)</h1>
          <p className="auth-subtitle">
            Kích hoạt tài khoản Học viên của bạn để bắt đầu đăng nhập và học tập trên Edumy
          </p>
        </div>

        {error && (
          <div className="auth-error-alert" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        {message && (
          <div
            style={{
              background: 'rgba(16, 185, 129, 0.12)',
              border: '1px solid rgba(16, 185, 129, 0.35)',
              color: '#34d399',
              padding: '0.85rem 1rem',
              borderRadius: '0.75rem',
              marginBottom: '1.25rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.6rem',
              fontSize: '0.9rem'
            }}
          >
            <CheckCircle2 size={18} style={{ flexShrink: 0 }} />
            <span>{message}</span>
          </div>
        )}

        {verified ? (
          <div style={{ textAlign: 'center', padding: '1rem 0' }}>
            <button
              type="button"
              className="btn-auth-submit"
              onClick={() => navigate('/login')}
              style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}
            >
              Chuyển đến trang Đăng nhập <ArrowRight size={18} />
            </button>
          </div>
        ) : (
          <form onSubmit={handleVerify} className="auth-form">
            <div className="auth-field-group">
              <label htmlFor="verify-email" className="auth-label">Địa chỉ Email đã đăng ký</label>
              <input
                id="verify-email"
                type="email"
                className="auth-input"
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="auth-field-group">
              <label htmlFor="verify-token" className="auth-label">Mã xác thực (Token từ email)</label>
              <input
                id="verify-token"
                type="text"
                className="auth-input"
                placeholder="Nhập mã xác thực hoặc bấm Xác thực ngay"
                value={token}
                onChange={(e) => setToken(e.target.value)}
              />
            </div>

            <button type="submit" className="btn-auth-submit" disabled={loading}>
              <MailCheck size={18} style={{ display: 'inline', marginRight: 6, verticalAlign: -3 }} />
              {loading ? 'Đang kiểm tra...' : 'Xác thực tài khoản'}
            </button>

            <button
              type="button"
              onClick={handleResend}
              disabled={resending}
              style={{
                marginTop: '0.75rem',
                width: '100%',
                padding: '0.75rem 1rem',
                borderRadius: '0.75rem',
                border: '1px solid rgba(148, 163, 184, 0.25)',
                background: 'rgba(30, 41, 59, 0.6)',
                color: '#cbd5e1',
                cursor: 'pointer',
                fontWeight: 600,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 8
              }}
            >
              <RefreshCw size={16} />
              {resending ? 'Đang gửi lại...' : 'Gửi lại email xác thực'}
            </button>
          </form>
        )}

        <div className="auth-footer-prompt">
          Đã xác thực tài khoản?{' '}
          <Link to="/login" className="auth-footer-link">Đăng nhập ngay</Link>
        </div>
      </div>
    </div>
  );
};

export default VerifyEmail;
