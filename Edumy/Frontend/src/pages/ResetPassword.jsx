import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import api from '../api/axiosConfig';
import './Auth.css';

const ResetPassword = () => {
  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
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
        email,
        token,
        newPassword
      });
      setMessage(response.data.message || 'Đặt lại mật khẩu thành công!');
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Đặt lại mật khẩu thất bại. Vui lòng kiểm tra mã xác thực hoặc thử lại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>Đặt lại mật khẩu</h2>
        <p className="auth-subtitle">Xác thực mã bảo mật và thiết lập mật khẩu mới an toàn.</p>

        {message && <div className="alert alert-success">{message} Đang chuyển hướng về trang Đăng nhập...</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Địa chỉ Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="token">Mã xác thực khôi phục (Token)</label>
            <input
              type="text"
              id="token"
              placeholder="ví dụ: A1B2C3D4"
              value={token}
              onChange={(e) => setToken(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="newPassword">Mật khẩu mới</label>
            <input
              type="password"
              id="newPassword"
              placeholder="Tối thiểu 6 ký tự"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-edumy-primary w-100" disabled={loading}>
            {loading ? 'Đang xử lý...' : 'Xác nhận đặt lại mật khẩu'}
          </button>
        </form>

        <div className="auth-footer">
          Quay lại <Link to="/login">Đăng nhập</Link>
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
