import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
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
      const response = await api.post('/auth/forgot-password', { email });
      setMessage(response.data.message || 'Mã xác thực khôi phục mật khẩu đã được tạo thành công.');
      if (response.data.resetToken) {
        setDevToken(response.data.resetToken);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>Quên mật khẩu</h2>
        <p className="auth-subtitle">Nhập địa chỉ email của bạn để nhận mã xác thực đặt lại mật khẩu.</p>
        
        {message && <div className="alert alert-success">{message}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Địa chỉ Email</label>
            <input
              type="email"
              id="email"
              placeholder="ví dụ: student@edumy.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-edumy-primary w-100" disabled={loading}>
            {loading ? 'Đang xử lý...' : 'Gửi mã xác thực'}
          </button>
        </form>

        {devToken && (
          <div className="dev-token-box mt-3 p-3 bg-light border rounded">
            <h6 className="text-warning fw-bold">🔧 Chế độ thử nghiệm (Demo):</h6>
            <p className="small mb-2">Hệ thống gửi mã xác thực khôi phục mật khẩu mẫu:</p>
            <div className="d-flex justify-content-between align-items-center bg-white p-2 border rounded">
              <code className="fw-bold">{devToken}</code>
              <button 
                onClick={() => navigate(`/reset-password?email=${encodeURIComponent(email)}&token=${devToken}`)}
                className="btn btn-sm btn-outline-primary"
              >
                Đặt lại mật khẩu ngay
              </button>
            </div>
          </div>
        )}

        <div className="auth-footer">
          Đã nhớ mật khẩu? <Link to="/login">Đăng nhập</Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
