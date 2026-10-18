import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import './Auth.css';

function Register() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      setError('');
      await api.post('/auth/register', { fullName, email, password });
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng ký tài khoản thất bại. Vui lòng thử lại.');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card hover-3d">
        <h2 className="auth-title">Đăng ký và bắt đầu học tập</h2>

        {error && <div style={{ color: '#b32d0f', backgroundColor: '#fcd3ce', padding: '12px', marginBottom: '16px', borderRadius: '4px', fontSize: '14px', fontWeight: 'bold' }}>{error}</div>}
        
        <form className="auth-form" onSubmit={handleRegister}>
          <div className="form-group">
            <input 
              type="text" 
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Họ và tên"
              required 
            />
          </div>
          <div className="form-group">
            <input 
              type="email" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Địa chỉ Email"
              required 
            />
          </div>
          <div className="form-group">
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Mật khẩu"
              required 
              minLength="6"
            />
          </div>
          <button type="submit" className="auth-submit-btn">Đăng ký tài khoản</button>
        </form>
        
        <div className="auth-footer-text" style={{ fontSize: '12px', marginBottom: '16px' }}>
          Bằng việc đăng ký, bạn đồng ý với <a href="#">Điều khoản sử dụng</a> và <a href="#">Chính sách bảo mật</a> của chúng tôi.
        </div>
        
        <div className="auth-divider"></div>

        <div className="auth-footer-text">
          Đã có tài khoản? <Link to="/login">Đăng nhập ngay</Link>
        </div>
      </div>
    </div>
  );
}

export default Register;

