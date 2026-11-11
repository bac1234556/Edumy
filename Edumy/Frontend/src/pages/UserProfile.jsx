import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import {
  User,
  Mail,
  Calendar,
  Edit3,
  Check,
  X,
  ShieldCheck,
  CheckCircle2,
  Camera,
  ShoppingBag,
  BookOpen,
  Lock,
  ArrowRight
} from 'lucide-react';
import EmptyState from '../components/EmptyState';
import { SkeletonBox } from '../components/Skeleton';
import './UserProfile.css';

function UserProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [activeTab, setActiveTab] = useState('profile'); // 'profile' | 'security'
  const [isEditing, setIsEditing] = useState(false);
  const [fullName, setFullName] = useState('');
  const [saving, setSaving] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  const { user } = useContext(AuthContext);
  const navigate = useNavigate();

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/v1/me');
      setProfile(response.data);
      setFullName(response.data?.fullName || '');
    } catch {
      try {
        const fallback = await api.get('/users/profile');
        setProfile(fallback.data);
        setFullName(fallback.data?.fullName || '');
      } catch {
        setError('Không thể tải thông tin hồ sơ người dùng.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleCancel = () => {
    setFullName(profile?.fullName || '');
    setIsEditing(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!fullName.trim()) {
      alert('Họ và tên không được để trống.');
      return;
    }
    try {
      setSaving(true);
      const response = await api.patch('/v1/me', { fullName: fullName.trim() });
      setProfile((prev) => ({ ...prev, ...response.data, fullName: fullName.trim() }));
      setIsEditing(false);
      setToastMessage('Cập nhật họ tên cá nhân thành công!');
      setTimeout(() => setToastMessage(''), 3000);
    } catch (err) {
      alert(err.response?.data?.message || 'Cập nhật hồ sơ thất bại. Vui lòng thử lại.');
    } finally {
      setSaving(false);
    }
  };

  if (!user) {
    return (
      <div className="profile-page-wrapper">
        <div className="profile-container">
          <EmptyState
            icon={User}
            title="Vui lòng đăng nhập"
            description="Đăng nhập tài khoản để xem và cập nhật hồ sơ cá nhân của bạn."
            actionText="Đăng nhập ngay"
            actionLink="/login"
          />
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="profile-page-wrapper">
        <div className="profile-container">
          <div className="profile-layout-grid">
            <div className="profile-sidebar-card">
              <div className="mx-auto mb-3" style={{ width: '108px', height: '108px' }}>
                <SkeletonBox width="108px" height="108px" borderRadius="50%" />
              </div>
              <SkeletonBox width="180px" height="22px" className="mx-auto mb-2" />
              <SkeletonBox width="140px" height="16px" className="mx-auto mb-4" />
              <SkeletonBox width="100%" height="40px" className="mb-2" />
              <SkeletonBox width="100%" height="40px" />
            </div>
            <div className="profile-content-card">
              <SkeletonBox width="240px" height="28px" className="mb-4" />
              <div className="profile-info-grid">
                <SkeletonBox width="100%" height="90px" />
                <SkeletonBox width="100%" height="90px" />
                <SkeletonBox width="100%" height="90px" />
                <SkeletonBox width="100%" height="90px" />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="profile-page-wrapper">
        <div className="profile-container">
          <EmptyState
            icon={User}
            title="Lỗi tải hồ sơ"
            description={error}
            actionText="Thử lại"
            onAction={fetchProfile}
          />
        </div>
      </div>
    );
  }

  const activeRoles = Array.isArray(profile?.roles) && profile.roles.length > 0
    ? profile.roles
    : [profile?.role || user.role || 'STUDENT'];

  return (
    <div className="profile-page-wrapper">
      <div className="profile-container">
        {/* Header Banner */}
        <div className="profile-header-banner">
          <h2>Tài khoản & Thiết lập</h2>
          <p>Quản lý thông tin định danh cá nhân, trạng thái vai trò và bảo mật tài khoản</p>
        </div>

        {toastMessage && (
          <div className="alert alert-success d-flex align-items-center gap-2 mb-4 rounded-3 shadow-sm">
            <CheckCircle2 size={18} />
            <span>{toastMessage}</span>
          </div>
        )}

        {/* 2-Column Dashboard Grid */}
        <div className="profile-layout-grid">
          {/* Left Column: Sidebar Card */}
          <aside className="profile-sidebar-card">
            <div className="profile-avatar-wrapper">
              <div className="profile-avatar-circle">
                {profile?.fullName ? profile.fullName.charAt(0).toUpperCase() : 'U'}
              </div>
              <button 
                type="button" 
                className="avatar-edit-badge" 
                title="Thay đổi ảnh đại diện"
                onClick={() => alert('Tính năng tải ảnh đại diện lên cloud đang được hoàn thiện.')}
              >
                <Camera size={16} />
              </button>
            </div>

            <h3 className="profile-user-name">{profile?.fullName}</h3>
            <p className="profile-user-email">{profile?.email}</p>

            <div className="profile-roles-badge-row">
              {activeRoles.map((r, idx) => (
                <span key={idx} className="profile-role-pill">
                  <ShieldCheck size={13} /> {String(r).toUpperCase()}
                </span>
              ))}
              <span className="profile-status-pill">
                <CheckCircle2 size={13} /> {profile?.status || 'ACTIVE'}
              </span>
            </div>

            {/* Sidebar Navigation */}
            <nav className="profile-nav-menu">
              <button
                type="button"
                className={`profile-nav-item ${activeTab === 'profile' ? 'active' : ''}`}
                onClick={() => setActiveTab('profile')}
              >
                <User size={18} />
                <span>Hồ sơ cá nhân</span>
              </button>

              <button
                type="button"
                className="profile-nav-item"
                onClick={() => navigate('/my-courses')}
              >
                <BookOpen size={18} />
                <span>Khóa học của tôi</span>
              </button>

              <button
                type="button"
                className="profile-nav-item"
                onClick={() => navigate('/orders')}
              >
                <ShoppingBag size={18} />
                <span>Lịch sử đơn hàng</span>
              </button>

              <button
                type="button"
                className={`profile-nav-item ${activeTab === 'security' ? 'active' : ''}`}
                onClick={() => setActiveTab('security')}
              >
                <Lock size={18} />
                <span>Bảo mật & Đổi mật khẩu</span>
              </button>
            </nav>
          </aside>

          {/* Right Column: Detailed Card */}
          <main className="profile-content-card">
            {activeTab === 'profile' ? (
              <>
                <div className="profile-section-header">
                  <h3 className="profile-section-title">
                    <User size={22} className="text-primary" /> Thông tin cá nhân
                  </h3>
                  {!isEditing && (
                    <button 
                      type="button" 
                      className="btn btn-outline-primary btn-sm rounded-pill px-3 fw-semibold d-inline-flex align-items-center gap-1"
                      onClick={handleEdit}
                    >
                      <Edit3 size={15} /> Chỉnh sửa họ tên
                    </button>
                  )}
                </div>

                {isEditing ? (
                  <form onSubmit={handleSubmit} className="profile-edit-form">
                    <div className="profile-input-group">
                      <label htmlFor="edit-fullname">Họ và tên hiển thị</label>
                      <input
                        id="edit-fullname"
                        type="text"
                        className="profile-text-input"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        required
                        autoFocus
                      />
                    </div>

                    <div className="profile-actions-row">
                      <button 
                        type="button" 
                        className="btn-cancel-edit" 
                        onClick={handleCancel} 
                        disabled={saving}
                      >
                        <X size={16} /> Hủy bỏ
                      </button>
                      <button 
                        type="submit" 
                        className="btn-save-profile" 
                        disabled={saving}
                      >
                        {saving ? (
                          'Đang lưu...'
                        ) : (
                          <>
                            <Check size={16} /> Lưu thay đổi
                          </>
                        )}
                      </button>
                    </div>
                  </form>
                ) : (
                  <div className="profile-info-grid">
                    {/* Full Name */}
                    <div className="profile-field-block">
                      <div className="profile-field-label">
                        <User size={14} /> Họ và tên
                      </div>
                      <div className="profile-field-value">{profile?.fullName || 'Chưa cập nhật'}</div>
                    </div>

                    {/* Email */}
                    <div className="profile-field-block">
                      <div className="profile-field-label">
                        <Mail size={14} /> Địa chỉ Email
                      </div>
                      <div className="profile-field-value d-flex align-items-center gap-2">
                        <span>{profile?.email}</span>
                        <span className="badge bg-success-subtle text-success small px-2 py-1 rounded-pill" style={{ fontSize: '0.72rem' }}>
                          Đã xác thực
                        </span>
                      </div>
                    </div>

                    {/* Roles */}
                    <div className="profile-field-block">
                      <div className="profile-field-label">
                        <ShieldCheck size={14} /> Quyền hạn & Vai trò
                      </div>
                      <div className="profile-field-value">
                        {activeRoles.map(r => String(r).toUpperCase()).join(', ')}
                      </div>
                    </div>

                    {/* Joined date */}
                    <div className="profile-field-block">
                      <div className="profile-field-label">
                        <Calendar size={14} /> Ngày tham gia
                      </div>
                      <div className="profile-field-value">
                        {profile?.createdAt
                          ? new Date(profile.createdAt).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' })
                          : 'Thành viên Edumy'}
                      </div>
                    </div>
                  </div>
                )}
              </>
            ) : (
              /* Security / Password Tab */
              <div>
                <div className="profile-section-header">
                  <h3 className="profile-section-title">
                    <Lock size={22} className="text-primary" /> Bảo mật & Đặt lại mật khẩu
                  </h3>
                </div>

                <div className="p-4 bg-light rounded-3 border mb-4">
                  <h5 className="fw-bold mb-2">Đổi mật khẩu tài khoản</h5>
                  <p className="text-muted small mb-3">
                    Để đảm bảo an toàn tuyệt đối, hệ thống Edumy sử dụng quy trình gửi mã xác thực qua Email đã đăng ký để xác nhận đổi mật khẩu mới.
                  </p>
                  <Link 
                    to={`/forgot-password`} 
                    className="btn btn-primary rounded-pill px-4 fw-semibold d-inline-flex align-items-center gap-2"
                  >
                    <span>Yêu cầu mã đổi mật khẩu</span>
                    <ArrowRight size={16} />
                  </Link>
                </div>
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}

export default UserProfile;
