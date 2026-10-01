import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

function AdminDashboard() {
  const [stats, setStats] = useState({ totalUsers: 0, totalCourses: 0, totalRevenue: 0 });
  const [courses, setCourses] = useState([]);
  const [users, setUsers] = useState([]);
  const [mlStats, setMlStats] = useState({ totalAnalyses: 0, highRiskCount: 0, pendingReviews: 0, totalReviews: 0, sentimentStats: [], analysesHistory: [] });
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('courses'); // courses, users, ml
  
  // States for override modal
  const [showOverrideModal, setShowOverrideModal] = useState(false);
  const [selectedAnalysisId, setSelectedAnalysisId] = useState(null);
  const [overrideCategory, setOverrideCategory] = useState('Development');

  const categoriesOptions = ['Development', 'Business', 'Design', 'Marketing', 'IT & Software', 'Office Productivity', 'Personal Development', 'Photography'];

  const fetchDashboardData = async () => {
    try {
      const [statsRes, coursesRes, usersRes, mlRes] = await Promise.all([
        api.get('/admin/stats'),
        api.get('/admin/courses'),
        api.get('/admin/users'),
        api.get('/admin/ml-monitoring')
      ]);
      setStats(statsRes.data);
      setCourses(coursesRes.data);
      setUsers(usersRes.data);
      setMlStats(mlRes.data);
    } catch (error) {
      console.error('Failed to fetch admin data', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleUpdateStatus = async (id, targetStatus) => {
    try {
      await api.put(`/admin/courses/${id}/status`, `"${targetStatus}"`, {
        headers: { 'Content-Type': 'application/json' }
      });
      fetchDashboardData();
    } catch (error) {
      alert('Failed to update status');
    }
  };

  const handleToggleUserStatus = async (id) => {
    try {
      await api.put(`/admin/users/${id}/toggle-status`);
      fetchDashboardData();
    } catch (error) {
      alert('Failed to update user status');
    }
  };

  const handleUpdateUserRole = async (id, newRole) => {
    try {
      await api.put(`/admin/users/${id}/role`, `"${newRole}"`, {
        headers: { 'Content-Type': 'application/json' }
      });
      fetchDashboardData();
    } catch (error) {
      alert('Failed to update user role');
    }
  };

  const handleApproveMl = async (analysisId) => {
    try {
      await api.post(`/admin/ml-analyses/${analysisId}/approve`);
      alert('ML classification approved!');
      fetchDashboardData();
    } catch (error) {
      alert('Failed to approve ML analysis');
    }
  };

  const handleOverrideMlSubmit = async () => {
    try {
      await api.post(`/admin/ml-analyses/${selectedAnalysisId}/override`, {
        categoryName: overrideCategory
      });
      alert('ML classification overridden successfully!');
      setShowOverrideModal(false);
      fetchDashboardData();
    } catch (error) {
      alert('Failed to override ML classification');
    }
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
        <div className="spinner-border text-primary" style={{ width: '3rem', height: '3rem' }} role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4 border-bottom pb-3">
        <h2 className="fw-bold text-primary m-0">EduMy Control Center</h2>
        <div className="btn-group">
          <button 
            className={`btn ${activeTab === 'courses' ? 'btn-primary' : 'btn-outline-primary'}`} 
            onClick={() => setActiveTab('courses')}
          >
            Manage Courses
          </button>
          <button 
            className={`btn ${activeTab === 'users' ? 'btn-primary' : 'btn-outline-primary'}`} 
            onClick={() => setActiveTab('users')}
          >
            User Management
          </button>
          <button 
            className={`btn ${activeTab === 'ml' ? 'btn-primary' : 'btn-outline-primary'}`} 
            onClick={() => setActiveTab('ml')}
          >
            ML Monitoring & Moderation
          </button>
        </div>
      </div>

      {/* Top statistics widgets */}
      <div className="row mb-5 g-4">
        <div className="col-md-3">
          <div className="glass-card p-4 border-0 text-center bg-white shadow-sm">
            <h6 className="text-muted fw-bold text-uppercase mb-1 small">Total Accounts</h6>
            <h2 className="mb-0 fw-bold">{stats.totalUsers}</h2>
          </div>
        </div>
        <div className="col-md-3">
          <div className="glass-card p-4 border-0 text-center bg-white shadow-sm">
            <h6 className="text-muted fw-bold text-uppercase mb-1 small">Total Courses</h6>
            <h2 className="mb-0 fw-bold">{stats.totalCourses}</h2>
          </div>
        </div>
        <div className="col-md-3">
          <div className="glass-card p-4 border-0 text-center bg-white shadow-sm">
            <h6 className="text-muted fw-bold text-uppercase mb-1 small">Total Revenue</h6>
            <h2 className="mb-0 fw-bold text-success">₫{(stats.totalRevenue || 0).toLocaleString()}</h2>
          </div>
        </div>
        <div className="col-md-3">
          <div className="glass-card p-4 border-0 text-center bg-white shadow-sm">
            <h6 className="text-muted fw-bold text-uppercase mb-1 small">ML Warnings</h6>
            <h2 className="mb-0 fw-bold text-danger">{mlStats.highRiskCount}</h2>
          </div>
        </div>
      </div>

      {/* Tab: Courses */}
      {activeTab === 'courses' && (
        <div className="card shadow-sm border-0">
          <div className="card-header bg-white py-3 border-0">
            <h5 className="mb-0 fw-bold">Published & Pending Courses</h5>
          </div>
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr>
                  <th className="ps-4">Course Name</th>
                  <th>Instructor</th>
                  <th>Category</th>
                  <th>Avg Rating</th>
                  <th>Status</th>
                  <th className="pe-4 text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                {courses.length === 0 ? (
                  <tr><td colSpan="6" className="text-center p-4">No courses found.</td></tr>
                ) : (
                  courses.map(course => (
                    <tr key={course.courseId}>
                      <td className="ps-4 fw-bold">{course.title}</td>
                      <td>{course.instructor?.fullName}</td>
                      <td><span className="badge bg-light text-dark">{course.category?.name || 'N/A'}</span></td>
                      <td>⭐ {course.averageRating || 'New'}</td>
                      <td>
                        <span className={`badge ${course.status === 'Published' ? 'bg-success' : course.status === 'PendingApproval' ? 'bg-primary' : 'bg-warning'} rounded-pill`}>
                          {course.status}
                        </span>
                      </td>
                      <td className="pe-4 text-end">
                        {course.status === 'PendingApproval' ? (
                          <div className="d-flex gap-2 justify-content-end">
                            <button 
                              className="btn btn-sm btn-success px-3"
                              onClick={() => handleUpdateStatus(course.courseId, 'Published')}
                            >
                              Publish
                            </button>
                            <button 
                              className="btn btn-sm btn-outline-danger px-3"
                              onClick={() => handleUpdateStatus(course.courseId, 'Draft')}
                            >
                              Reject
                            </button>
                          </div>
                        ) : (
                          <button 
                            className={`btn btn-sm ${course.status === 'Published' ? 'btn-outline-danger' : 'btn-outline-success'} px-3`}
                            onClick={() => handleUpdateStatus(course.courseId, course.status === 'Published' ? 'Draft' : 'Published')}
                          >
                            {course.status === 'Published' ? 'Unpublish' : 'Publish'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab: Users */}
      {activeTab === 'users' && (
        <div className="card shadow-sm border-0">
          <div className="card-header bg-white py-3 border-0">
            <h5 className="mb-0 fw-bold">User Database</h5>
          </div>
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr>
                  <th className="ps-4">Full Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Account Status</th>
                  <th className="pe-4 text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.userId}>
                    <td className="ps-4 fw-bold">{u.fullName}</td>
                    <td>{u.email}</td>
                    <td>
                      <select 
                        value={u.role} 
                        onChange={(e) => handleUpdateUserRole(u.userId, e.target.value)}
                        className="form-select form-select-sm w-auto d-inline"
                      >
                        <option value="Student">Student</option>
                        <option value="Instructor">Instructor</option>
                        <option value="Admin">Admin</option>
                      </select>
                    </td>
                    <td>
                      <span className={`badge ${u.isActive ? 'bg-success' : 'bg-danger'} rounded-pill`}>
                        {u.isActive ? 'Active' : 'Blocked'}
                      </span>
                    </td>
                    <td className="pe-4 text-end">
                      <button 
                        onClick={() => handleToggleUserStatus(u.userId)}
                        className={`btn btn-sm ${u.isActive ? 'btn-danger' : 'btn-success'} px-3`}
                      >
                        {u.isActive ? 'Block Account' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab: ML Monitoring */}
      {activeTab === 'ml' && (
        <div>
          <div className="row mb-4 g-4">
            <div className="col-md-6">
              <div className="card p-4 border-0 shadow-sm bg-white h-100">
                <h5 className="fw-bold mb-3">AI Sentiment Classification Overview</h5>
                <p className="text-muted small">Total reviews processed: {mlStats.totalReviews}</p>
                <div className="d-flex flex-column gap-2">
                  {mlStats.sentimentStats?.map(stat => (
                    <div key={stat.label} className="d-flex align-items-center justify-content-between border-bottom pb-2">
                      <span className="fw-bold">{stat.label || 'Neutral'}</span>
                      <span className="badge bg-secondary">{stat.count} reviews</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="col-md-6">
              <div className="card p-4 border-0 shadow-sm bg-white h-100">
                <h5 className="fw-bold mb-3">Moderation Metrics</h5>
                <div className="d-flex flex-column gap-3 justify-content-center h-100">
                  <div className="p-3 bg-light rounded d-flex justify-content-between align-items-center">
                    <div>
                      <h6 className="m-0 fw-bold">Needs Manual Moderation</h6>
                      <small className="text-muted">Low AI confidence runs</small>
                    </div>
                    <span className="fs-4 fw-bold text-warning">{mlStats.pendingReviews}</span>
                  </div>
                  <div className="p-3 bg-light rounded d-flex justify-content-between align-items-center">
                    <div>
                      <h6 className="m-0 fw-bold">Toxicity Flagged Courses</h6>
                      <small className="text-muted">High spam/toxicity probability</small>
                    </div>
                    <span className="fs-4 fw-bold text-danger">{mlStats.highRiskCount}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="card shadow-sm border-0">
            <div className="card-header bg-white py-3 border-0">
              <h5 className="mb-0 fw-bold">Auto-Classification Log & Overrides</h5>
            </div>
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th className="ps-4">Course</th>
                    <th>Predicted Category</th>
                    <th>AI Confidence</th>
                    <th>Content Safety</th>
                    <th>AI Status</th>
                    <th className="pe-4 text-end">Override Controls</th>
                  </tr>
                </thead>
                <tbody>
                  {mlStats.analysesHistory?.map(log => (
                    <tr key={log.id}>
                      <td className="ps-4 fw-bold">{log.courseTitle}</td>
                      <td>{log.primaryCategory}</td>
                      <td>
                        <div className="progress" style={{ height: '8px', maxWidth: '100px' }}>
                          <div 
                            className={`progress-bar ${log.confidence > 0.85 ? 'bg-success' : log.confidence > 0.65 ? 'bg-warning' : 'bg-danger'}`} 
                            style={{ width: `${log.confidence * 100}%` }}
                          ></div>
                        </div>
                        <small className="text-muted">{Math.round(log.confidence * 100)}%</small>
                      </td>
                      <td>
                        <span className={`badge ${log.riskLevel === 'High' ? 'bg-danger' : 'bg-success'} rounded-pill`}>
                          {log.riskLevel === 'High' ? 'High Risk' : 'Low Risk'}
                        </span>
                      </td>
                      <td>
                        <span className={`badge bg-light text-dark border`}>{log.status}</span>
                      </td>
                      <td className="pe-4 text-end">
                        <div className="d-flex gap-2 justify-content-end">
                          <button 
                            className="btn btn-sm btn-success"
                            onClick={() => handleApproveMl(log.id)}
                            disabled={log.status === 'Approved'}
                          >
                            Approve
                          </button>
                          <button 
                            className="btn btn-sm btn-warning"
                            onClick={() => {
                              setSelectedAnalysisId(log.id);
                              setShowOverrideModal(true);
                            }}
                          >
                            Override
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Override Category Modal */}
      {showOverrideModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title fw-bold">Override AI Classification</h5>
                <button type="button" className="btn-close" onClick={() => setShowOverrideModal(false)}></button>
              </div>
              <div className="modal-body">
                <label className="form-label fw-bold">Select Correct Category:</label>
                <select 
                  value={overrideCategory} 
                  onChange={(e) => setOverrideCategory(e.target.value)}
                  className="form-select"
                >
                  {categoriesOptions.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowOverrideModal(false)}>Cancel</button>
                <button type="button" className="btn btn-primary" onClick={handleOverrideMlSubmit}>Apply Override</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;
