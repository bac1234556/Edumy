import React, { useState, useEffect, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { User, Mail, Calendar, Edit3, Check, X, ShieldCheck, CheckCircle2 } from 'lucide-react';
import EmptyState from '../components/EmptyState';
import { SkeletonBox } from '../components/Skeleton';
import './UserProfile.css';

function UserProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [isEditing, setIsEditing] = useState(false);
  const [fullName, setFullName] = useState('');
  const [saving, setSaving] = useState(false);

  const { user } = useContext(AuthContext);

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
      alert('Cập nhật họ tên cá nhân thành công!');
    } catch (err) {
      alert(err.response?.data?.message || 'Cập nhật hồ sơ thất bại. Vui lòng thử lại.');
    } finally {
      setSaving(false);
    }
  };

  if (!user) {
    return (
      <div className="container my-5">
        <EmptyState
          icon={User}
          title="Vui lòng đăng nhập"
          description="Đăng nhập tài khoản để xem và cập nhật hồ sơ cá nhân của bạn."
          actionText="Đăng nhập ngay"
          actionLink="/login"
        />
      </div>
    );
  }

  if (loading) {
    return (
      <div className="container my-5" style={{ maxWidth: '720px' }}>
        <div className="card shadow-sm border p-4 rounded-3 text-center">
          <div className="mx-auto mb-3" style={{ width: '90px', height: '90px' }}>
            <SkeletonBox width="90px" height="90px" borderRadius="50%" />
          </div>
          <div className="mx-auto mb-2" style={{ width: '220px' }}>
            <SkeletonBox width="220px" height="24px" />
          </div>
          <div className="d-flex flex-column gap-3 text-start mt-4">
            <SkeletonBox width="100%" height="45px" />
            <SkeletonBox width="100%" height="45px" />
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container my-5">
        <EmptyState
          icon={User}
          title="Lỗi tải hồ sơ"
          description={error}
          actionText="Thử lại"
          onAction={fetchProfile}
        />
      </div>
    );
  }

  const activeRoles = Array.isArray(profile?.roles) && profile.roles.length > 0
    ? profile.roles
    : [profile?.role || user.role || 'STUDENT'];

  return (
    <div className="container my-5 profile-page">
      <div className="row justify-content-center">
        <div className="col-md-8">
          <div className="card shadow-sm border-0 profile-card">
            <div className="card-header bg-white border-bottom-0 pt-4 pb-0 d-flex justify-content-between align-items-center">
              <h3 className="mb-0">Hồ sơ cá nhân</h3>
              {!isEditing && (
                <button className="btn btn-outline-primary btn-sm rounded-pill px-3" onClick={handleEdit}>
                  <Edit3 size={16} className="me-2" /> Cập nhật Họ tên
                </button>
              )}
            </div>
            <div className="card-body p-4">
              <div className="text-center mb-4 pb-4 border-bottom">
                <div className="profile-avatar mb-3 mx-auto d-flex align-items-center justify-content-center bg-primary text-white fs-1 fw-bold rounded-circle shadow" style={{ width: '100px', height: '100px' }}>
                  {profile?.fullName ? profile.fullName.charAt(0).toUpperCase() : 'U'}
                </div>
                <h4 className="mb-2">{profile?.fullName}</h4>
                <div className="d-flex justify-content-center gap-2 flex-wrap">
                  {activeRoles.map((r, idx) => (
                    <span key={idx} className="badge bg-primary-subtle text-primary px-3 py-2 rounded-pill">
                      <ShieldCheck size={14} className="me-1" /> {String(r).toUpperCase()}
                    </span>
                  ))}
                  <span className="badge bg-success-subtle text-success px-3 py-2 rounded-pill">
                    <CheckCircle2 size={14} className="me-1" /> {profile?.status || 'ACTIVE'}
                  </span>
                </div>
              </div>

              {isEditing ? (
                <form onSubmit={handleSubmit} className="profile-form">
                  <div className="mb-3">
                    <label className="form-label fw-bold">Họ và tên</label>
                    <input
                      type="text"
                      className="form-control"
                      name="fullName"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      required
                    />
                  </div>

                  <div className="d-flex gap-2 justify-content-end mt-4">
                    <button type="button" className="btn btn-light px-4" onClick={handleCancel} disabled={saving}>
                      <X size={18} className="me-1" /> Hủy bỏ
                    </button>
                    <button type="submit" className="btn btn-primary px-4" disabled={saving}>
                      {saving ? 'Đang lưu...' : <><Check size={18} className="me-1" /> Lưu họ tên</>}
                    </button>
                  </div>
                </form>
              ) : (
                <div className="profile-info">
                  <div className="row mb-3">
                    <div className="col-sm-4 text-muted"><Mail size={16} className="me-2" /> Địa chỉ Email</div>
                    <div className="col-sm-8 fw-medium">
                      {profile?.email}{' '}
                      <span className="badge bg-success-subtle text-success ms-2">Đã xác thực Email</span>
                    </div>
                  </div>

                  <div className="row mb-3">
                    <div className="col-sm-4 text-muted"><User size={16} className="me-2" /> Họ và tên</div>
                    <div className="col-sm-8 fw-medium">{profile?.fullName}</div>
                  </div>

                  <div className="row mb-3">
                    <div className="col-sm-4 text-muted"><ShieldCheck size={16} className="me-2" /> Vai trò đang hiệu lực</div>
                    <div className="col-sm-8 fw-medium">{activeRoles.join(', ')}</div>
                  </div>

                  {profile?.createdAt && (
                    <div className="row mb-3">
                      <div className="col-sm-4 text-muted"><Calendar size={16} className="me-2" /> Ngày tạo tài khoản</div>
                      <div className="col-sm-8 fw-medium">
                        {new Date(profile.createdAt).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' })}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserProfile;
