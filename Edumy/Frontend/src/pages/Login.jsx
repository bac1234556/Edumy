import { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { GoogleLogin } from '@react-oauth/google';
import './Auth.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isGoogleLoading, setIsGoogleLoading] = useState(false);
  const { login, googleLogin } = useContext(AuthContext);
  const navigate = useNavigate();

  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
  const isGoogleConfigured = Boolean(
    googleClientId && 
    googleClientId !== 'YOUR_GOOGLE_CLIENT_ID_HERE' && 
    googleClientId.trim().length > 15
  );

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      setError('');
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email hoặc mật khẩu.');
    }
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    if (!credentialResponse?.credential) {
      setError('Không nhận được thông tin xác thực từ Google.');
      return;
    }
    try {
      setError('');
      setIsGoogleLoading(true);
      await googleLogin(credentialResponse.credential);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng nhập Google thất bại. Token không hợp lệ.');
    } finally {
      setIsGoogleLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card hover-3d">
        <h2 className="auth-title">Đăng nhập vào Edumy</h2>
        <p className="text-center text-muted small mb-4">Chào mừng bạn quay trở lại nền tảng học tập trực tuyến</p>
        
        {error && (
          <div style={{ 
            color: '#b32d0f', 
            backgroundColor: '#fcd3ce', 
            padding: '12px', 
            marginBottom: '16px', 
            borderRadius: '6px', 
            fontSize: '14px', 
            fontWeight: 600 
          }}>
            {error}
          </div>
        )}
        
        {isGoogleConfigured && (
          <>
            <div className="google-auth-wrapper d-flex justify-content-center mb-3">
              <GoogleLogin
                onSuccess={handleGoogleSuccess}
                onError={() => {
                  setError('Google Login Failed. Kiểm tra lại cấu hình OAuth Client.');
                }}
                width="336px"
                theme="outline"
                size="large"
                text="signin_with"
                shape="rectangular"
              />
            </div>
            <div className="auth-divider">hoặc bằng email</div>
          </>
        )}
        
        <form className="auth-form" onSubmit={handleLogin}>
          <div className="form-group">
            <label>Email tài khoản</label>
            <input 
              type="email" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ví dụ: student@edumy.com"
              required 
            />
          </div>
          <div className="form-group">
            <div className="d-flex justify-content-between align-items-center">
              <label>Mật khẩu</label>
              <Link to="/forgot-password" style={{ fontSize: '12px', color: '#6366f1' }}>Quên mật khẩu?</Link>
            </div>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required 
            />
          </div>
          <button type="submit" className="auth-submit-btn" disabled={isGoogleLoading}>
            Đăng nhập
          </button>
        </form>
        
        <div className="auth-footer-text">
          Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
