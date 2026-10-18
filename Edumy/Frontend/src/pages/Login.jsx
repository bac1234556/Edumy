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
      setError(err.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.');
    }
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      setError('');
      setIsGoogleLoading(true);
      await googleLogin(credentialResponse.credential);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng nhập Google thất bại.');
    } finally {
      setIsGoogleLoading(false);
    }
  };

  const handleDemoGoogleLogin = async () => {
    try {
      setError('');
      setIsGoogleLoading(true);
      // Gửi mock token lên API backend để cấp JWT ngay trên localhost
      await googleLogin('mock-google-oauth-token');
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng nhập Google Demo thất bại.');
    } finally {
      setIsGoogleLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card hover-3d">
        <h2 className="auth-title">Đăng nhập vào EduMy</h2>
        
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
        
        <div className="google-auth-wrapper">
          {isGoogleConfigured ? (
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
          ) : (
            <button 
              type="button" 
              className="google-custom-btn"
              onClick={handleDemoGoogleLogin}
              disabled={isGoogleLoading}
            >
                <svg className="google-icon-svg" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
                </svg>
                <span>{isGoogleLoading ? 'Đang xác thực...' : 'Đăng nhập bằng Google'}</span>
              </button>
          )}
        </div>

        <div className="auth-divider">hoặc bằng email</div>
        
        <form className="auth-form" onSubmit={handleLogin}>
          <div className="form-group">
            <label>Email</label>
            <input 
              type="email" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ví dụ: student@edumy.com"
              required 
            />
          </div>
          <div className="form-group">
            <label>Mật khẩu</label>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required 
            />
          </div>
          <button type="submit" className="auth-submit-btn">Đăng nhập</button>
        </form>
        
        <div className="auth-footer-text">
          Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
