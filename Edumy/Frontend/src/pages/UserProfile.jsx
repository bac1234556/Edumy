import React, { useState, useEffect, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { User, Mail, Calendar, Edit3, Check, X } from 'lucide-react';
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
      setError('Could not fetch profile information.');
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
      alert('Profile updated successfully!');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  if (!user) {
    return <div className="container text-center my-5">Please login to view your profile.</div>;
  }

  if (loading) return <div className="container text-center my-5">Loading profile...</div>;
  if (error) return <div className="container text-center my-5 text-danger">{error}</div>;

  return (
    <div className="container my-5 profile-page">
      <div className="row justify-content-center">
        <div className="col-md-8">
          <div className="card shadow-sm border-0 profile-card">
            <div className="card-header bg-white border-bottom-0 pt-4 pb-0 d-flex justify-content-between align-items-center">
              <h3 className="mb-0">Public Profile</h3>
              {!isEditing && (
                <button className="btn btn-outline-primary btn-sm rounded-pill px-3" onClick={handleEdit}>
                  <Edit3 size={16} className="me-2" /> Edit Profile
                </button>
              )}
            </div>
            <div className="card-body p-4">
              
              <div className="text-center mb-4 pb-4 border-bottom">
                <div className="profile-avatar mb-3 mx-auto d-flex align-items-center justify-content-center bg-primary text-white fs-1 fw-bold rounded-circle shadow" style={{width: '100px', height: '100px'}}>
                  {profile.fullName ? profile.fullName.charAt(0).toUpperCase() : 'U'}
                </div>
                <h4 className="mb-1">{profile.fullName}</h4>
                <p className="text-muted mb-0"><User size={14} className="me-1"/> {user.role}</p>
              </div>

              {isEditing ? (
                <form onSubmit={handleSubmit} className="profile-form">
                  <div className="mb-3">
                    <label className="form-label fw-bold">Full Name</label>
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
                    <label className="form-label fw-bold">Bio</label>
                    <textarea 
                      className="form-control" 
                      name="bio" 
                      rows="4" 
                      value={formData.bio} 
                      onChange={handleChange}
                      placeholder="Tell us a little bit about yourself..."
                    ></textarea>
                  </div>

                  <div className="d-flex gap-2 justify-content-end mt-4">
                    <button type="button" className="btn btn-light px-4" onClick={handleCancel} disabled={saving}>
                      <X size={18} className="me-1" /> Cancel
                    </button>
                    <button type="submit" className="btn btn-primary px-4" disabled={saving}>
                      {saving ? 'Saving...' : <><Check size={18} className="me-1" /> Save Changes</>}
                    </button>
                  </div>
                </form>
              ) : (
                <div className="profile-info">
                  <div className="row mb-3">
                    <div className="col-sm-3 text-muted"><Mail size={16} className="me-2"/> Email</div>
                    <div className="col-sm-9 fw-medium">{profile.email}</div>
                  </div>
                  
                  <div className="row mb-3">
                    <div className="col-sm-3 text-muted"><User size={16} className="me-2"/> Full Name</div>
                    <div className="col-sm-9 fw-medium">{profile.fullName}</div>
                  </div>

                  <div className="row mb-3">
                    <div className="col-sm-3 text-muted"><Calendar size={16} className="me-2"/> Member Since</div>
                    <div className="col-sm-9 fw-medium">{new Date(profile.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</div>
                  </div>

                  <div className="row mt-4 pt-3 border-top">
                    <div className="col-12">
                      <h6 className="text-muted mb-3">Bio</h6>
                      {profile.bio ? (
                        <p className="mb-0" style={{whiteSpace: 'pre-line'}}>{profile.bio}</p>
                      ) : (
                        <p className="text-muted fst-italic mb-0">No bio provided yet.</p>
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
