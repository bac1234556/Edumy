import { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { GoogleLogin } from '@react-oauth/google';
import { AlertCircle } from 'lucide-react';
import './Auth.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isGoogleLoading, setIsGoogleLoading] = useState(false);
  const { login, googleLogin } = useContext(AuthContext);
  const navigate = useNavigate();

  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
  const isGoogleConfigured = Boolean(
    googleClientId && 
    googleClientId !== 'YOUR_GOOGLE_CLIENT_ID_HERE' && 
    googleClientId.trim().length > 15
  );

  const validate = () => {
    const errors = {};
    if (!email.trim()) {
      errors.email = 'Vui lòng nhập địa chỉ email.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      errors.email = 'Email không hợp lệ. Ví dụ: student@edumy.com';
    }
    if (!password) {
      errors.password = 'Vui lòng nhập mật khẩu.';
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!validate() || isSubmitting) return;

    setIsSubmitting(true);
    setError('');
    try {
      await login(email.trim().toLowerCase(), password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email hoặc mật khẩu.');
    } finally {
      setIsSubmitting(false);
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
            color: '#b91c1c', 
            backgroundColor: '#fef2f2', 
            border: '1px solid #fecaca',
            padding: '12px', 
            marginBottom: '16px', 
            borderRadius: '6px', 
            fontSize: '14px', 
            fontWeight: 600,
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <AlertCircle size={16} />
            <span>{error}</span>
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
        
        <form className="auth-form" onSubmit={handleLogin} noValidate>
          <div className="form-group">
            <label>Email tài khoản</label>
            <input 
              type="email" 
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (fieldErrors.email) setFieldErrors(prev => ({ ...prev, email: null }));
              }}
              placeholder="ví dụ: student@edumy.com"
              style={fieldErrors.email ? { borderColor: '#ef4444' } : {}}
            />
            {fieldErrors.email && (
              <div className="field-error-text">
                <AlertCircle size={14} /> {fieldErrors.email}
              </div>
            )}
          </div>

          <div className="form-group">
            <div className="d-flex justify-content-between align-items-center">
              <label>Mật khẩu</label>
              <Link to="/forgot-password" style={{ fontSize: '12px', color: '#6366f1' }}>Quên mật khẩu?</Link>
            </div>
            <input 
              type="password" 
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (fieldErrors.password) setFieldErrors(prev => ({ ...prev, password: null }));
              }}
              placeholder="••••••••"
              style={fieldErrors.password ? { borderColor: '#ef4444' } : {}}
            />
            {fieldErrors.password && (
              <div className="field-error-text">
                <AlertCircle size={14} /> {fieldErrors.password}
              </div>
            )}
          </div>

          <button 
            type="submit" 
            className="auth-submit-btn touch-target" 
            disabled={isSubmitting || isGoogleLoading}
            style={{ 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center', 
              gap: '8px',
              minHeight: '44px',
              opacity: isSubmitting ? 0.8 : 1,
              cursor: isSubmitting ? 'not-allowed' : 'pointer'
            }}
          >
            {isSubmitting ? (
              <>
                <span className="btn-spinner"></span>
                Đang xác thực...
              </>
            ) : (
              'Đăng nhập'
            )}
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
