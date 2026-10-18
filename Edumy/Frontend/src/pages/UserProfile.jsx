import React, { useState, useEffect, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { User, Mail, Calendar, Edit3, Check, X } from 'lucide-react';
import EmptyState from '../components/EmptyState';
import { SkeletonBox } from '../components/Skeleton';
import './UserProfile.css';

function UserProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({ fullName: '', bio: '' });
  const [saving, setSaving] = useState(false);

  const { user } = useContext(AuthContext);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/users/profile');
      setProfile(response.data);
      setFormData({
        fullName: response.data.fullName || '',
        bio: response.data.bio || ''
      });
    } catch (err) {
      setError('Không thể tải thông tin hồ sơ người dùng.');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleCancel = () => {
    setFormData({
      fullName: profile.fullName || '',
      bio: profile.bio || ''
    });
    setIsEditing(false);
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      const response = await api.put('/users/profile', formData);
      setProfile(response.data);
      setIsEditing(false);
      alert('Cập nhật hồ sơ cá nhân thành công!');
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
          <div className="mx-auto mb-4" style={{ width: '120px' }}>
            <SkeletonBox width="120px" height="16px" />
          </div>
          <div className="d-flex flex-column gap-3 text-start">
            <SkeletonBox width="100%" height="45px" />
            <SkeletonBox width="100%" height="45px" />
            <SkeletonBox width="100%" height="90px" />
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

  return (
    <div className="container my-5 profile-page">
      <div className="row justify-content-center">
        <div className="col-md-8">
          <div className="card shadow-sm border-0 profile-card">
            <div className="card-header bg-white border-bottom-0 pt-4 pb-0 d-flex justify-content-between align-items-center">
              <h3 className="mb-0">Hồ sơ cá nhân</h3>
              {!isEditing && (
                <button className="btn btn-outline-primary btn-sm rounded-pill px-3" onClick={handleEdit}>
                  <Edit3 size={16} className="me-2" /> Chỉnh sửa hồ sơ
                </button>
              )}
            </div>
            <div className="card-body p-4">
              
              <div className="text-center mb-4 pb-4 border-bottom">
                <div className="profile-avatar mb-3 mx-auto d-flex align-items-center justify-content-center bg-primary text-white fs-1 fw-bold rounded-circle shadow" style={{width: '100px', height: '100px'}}>
                  {profile.fullName ? profile.fullName.charAt(0).toUpperCase() : 'U'}
                </div>
                <h4 className="mb-1">{profile.fullName}</h4>
                <p className="text-muted mb-0"><User size={14} className="me-1"/> {user.role === 'Admin' ? 'Quản trị viên' : user.role === 'Instructor' ? 'Giảng viên' : 'Học viên'}</p>
              </div>

              {isEditing ? (
                <form onSubmit={handleSubmit} className="profile-form">
                  <div className="mb-3">
                    <label className="form-label fw-bold">Họ và tên</label>
                    <input 
                      type="text" 
                      className="form-control" 
                      name="fullName" 
                      value={formData.fullName} 
                      onChange={handleChange} 
                      required 
                    />
                  </div>
                  
                  <div className="mb-3">
                    <label className="form-label fw-bold">Tiểu sử giới thiệu</label>
                    <textarea 
                      className="form-control" 
                      name="bio" 
                      rows="4" 
                      value={formData.bio} 
                      onChange={handleChange} 
                      placeholder="Chia sẻ đôi nét về kinh nghiệm, sở thích hoặc định hướng học tập của bạn..."
                    ></textarea>
                  </div>

                  <div className="d-flex gap-2 justify-content-end mt-4">
                    <button type="button" className="btn btn-light px-4" onClick={handleCancel} disabled={saving}>
                      <X size={18} className="me-1" /> Hủy bỏ
                    </button>
                    <button type="submit" className="btn btn-primary px-4" disabled={saving}>
                      {saving ? 'Đang lưu...' : <><Check size={18} className="me-1" /> Lưu thay đổi</>}
                    </button>
                  </div>
                </form>
              ) : (
                <div className="profile-info">
                  <div className="row mb-3">
                    <div className="col-sm-3 text-muted"><Mail size={16} className="me-2"/> Địa chỉ Email</div>
                    <div className="col-sm-9 fw-medium">{profile.email}</div>
                  </div>
                  
                  <div className="row mb-3">
                    <div className="col-sm-3 text-muted"><User size={16} className="me-2"/> Họ và tên</div>
                    <div className="col-sm-9 fw-medium">{profile.fullName}</div>
                  </div>

                  <div className="row mb-3">
                    <div className="col-sm-3 text-muted"><Calendar size={16} className="me-2"/> Ngày gia nhập</div>
                    <div className="col-sm-9 fw-medium">{new Date(profile.createdAt).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' })}</div>
                  </div>

                  <div className="row mt-4 pt-3 border-top">
                    <div className="col-12">
                      <h6 className="text-muted mb-3">Tiểu sử giới thiệu</h6>
                      {profile.bio ? (
                        <p className="mb-0" style={{whiteSpace: 'pre-line'}}>{profile.bio}</p>
                      ) : (
                        <p className="text-muted fst-italic mb-0">Chưa có thông tin giới thiệu.</p>
                      )}
                    </div>
                  </div>
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
