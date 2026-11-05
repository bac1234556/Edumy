import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AlertCircle, GraduationCap, BookOpen } from 'lucide-react';
import './Auth.css';

function Register() {
  const [searchParams] = useSearchParams();
  const roleParam = searchParams.get('role');
  const [role, setRole] = useState(roleParam?.toLowerCase() === 'instructor' ? 'Instructor' : 'Student');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const validate = () => {
    const errors = {};
    if (!fullName.trim()) {
      errors.fullName = 'Vui lòng nhập họ và tên của bạn.';
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim()) {
      errors.email = 'Vui lòng nhập địa chỉ email.';
    } else if (!emailRegex.test(email)) {
      errors.email = 'Email không hợp lệ. Ví dụ: student@edumy.com';
    }
    if (!password) {
      errors.password = 'Vui lòng thiết lập mật khẩu.';
    } else if (password.length < 6) {
      errors.password = 'Mật khẩu phải chứa ít nhất 6 ký tự bảo mật.';
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!validate() || isSubmitting) return;

    setIsSubmitting(true);
    setError('');
    try {
      await api.post('/auth/register', { 
        fullName: fullName.trim(), 
        email: email.trim().toLowerCase(), 
        password,
        role
      });
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng ký tài khoản thất bại. Email có thể đã được đăng ký trước đó.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card hover-3d">
        <h2 className="auth-title">Đăng ký và bắt đầu học tập</h2>

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
        
        <form className="auth-form" onSubmit={handleRegister} noValidate>
          <div className="form-group mb-3">
            <label style={{ fontSize: '13px', color: '#475569', marginBottom: '8px' }}>
              Vai trò tài khoản bạn muốn đăng ký:
            </label>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <button
                type="button"
                onClick={() => setRole('Student')}
                style={{
                  padding: '12px 8px',
                  borderRadius: '8px',
                  border: role === 'Student' ? '2px solid #4f46e5' : '1px solid #cbd5e1',
                  backgroundColor: role === 'Student' ? '#eef2ff' : '#ffffff',
                  color: role === 'Student' ? '#4f46e5' : '#475569',
                  fontWeight: 600,
                  fontSize: '0.9rem',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '4px',
                  transition: 'all 0.15s ease'
                }}
              >
                <GraduationCap size={20} />
                <span>Học viên</span>
                <span style={{ fontSize: '0.725rem', fontWeight: 400, color: '#64748b' }}>Học & nhận chứng chỉ</span>
              </button>
              
              <button
                type="button"
                onClick={() => setRole('Instructor')}
                style={{
                  padding: '12px 8px',
                  borderRadius: '8px',
                  border: role === 'Instructor' ? '2px solid #4f46e5' : '1px solid #cbd5e1',
                  backgroundColor: role === 'Instructor' ? '#eef2ff' : '#ffffff',
                  color: role === 'Instructor' ? '#4f46e5' : '#475569',
                  fontWeight: 600,
                  fontSize: '0.9rem',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '4px',
                  transition: 'all 0.15s ease'
                }}
              >
                <BookOpen size={20} />
                <span>Giảng viên</span>
                <span style={{ fontSize: '0.725rem', fontWeight: 400, color: '#64748b' }}>Tạo & bán khóa học</span>
              </button>
            </div>
          </div>

          <div className="form-group">
            <label>Họ và tên</label>
            <input 
              type="text" 
              value={fullName}
              onChange={(e) => {
                setFullName(e.target.value);
                if (fieldErrors.fullName) setFieldErrors(prev => ({ ...prev, fullName: null }));
              }}
              placeholder="ví dụ: Nguyễn Văn An"
              className={fieldErrors.fullName ? 'input-error' : ''}
              style={fieldErrors.fullName ? { borderColor: '#ef4444' } : {}}
            />
            {fieldErrors.fullName && (
              <div className="field-error-text">
                <AlertCircle size={14} /> {fieldErrors.fullName}
              </div>
            )}
          </div>

          <div className="form-group">
            <label>Địa chỉ Email</label>
            <input 
              type="email" 
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (fieldErrors.email) setFieldErrors(prev => ({ ...prev, email: null }));
              }}
              placeholder="ví dụ: an.nguyen@example.com"
              className={fieldErrors.email ? 'input-error' : ''}
              style={fieldErrors.email ? { borderColor: '#ef4444' } : {}}
            />
            {fieldErrors.email && (
              <div className="field-error-text">
                <AlertCircle size={14} /> {fieldErrors.email}
              </div>
            )}
          </div>

          <div className="form-group">
            <label>Mật khẩu</label>
            <input 
              type="password" 
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (fieldErrors.password) setFieldErrors(prev => ({ ...prev, password: null }));
              }}
              placeholder="Tối thiểu 6 ký tự"
              className={fieldErrors.password ? 'input-error' : ''}
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
            disabled={isSubmitting}
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
                Đang tạo tài khoản...
              </>
            ) : (
              'Đăng ký tài khoản'
            )}
          </button>
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
