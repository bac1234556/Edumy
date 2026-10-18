import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import './AdminDashboard.css';

function AdminDashboard() {
  const [stats, setStats] = useState({ totalUsers: 0, totalCourses: 0, totalRevenue: 0 });
  const [courses, setCourses] = useState([]);
  const [users, setUsers] = useState([]);
  const [mlStats, setMlStats] = useState({ 
    totalAnalyses: 0, 
    highRiskCount: 0, 
    pendingReviews: 0, 
    totalReviews: 0, 
    sentimentStats: [], 
    analysesHistory: [] 
  });
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [activeTab, setActiveTab] = useState('courses'); // 'courses', 'users', 'ml'
  
  // Filter & Search states
  const [courseSearch, setCourseSearch] = useState('');
  const [courseStatusFilter, setCourseStatusFilter] = useState('ALL');
  
  const [userSearch, setUserSearch] = useState('');
  const [userRoleFilter, setUserRoleFilter] = useState('ALL');
  const [userStatusFilter, setUserStatusFilter] = useState('ALL');

  // Override Modal
  const [showOverrideModal, setShowOverrideModal] = useState(false);
  const [selectedAnalysisId, setSelectedAnalysisId] = useState(null);
  const [overrideCategory, setOverrideCategory] = useState('Development');
  const [feedbackMsg, setFeedbackMsg] = useState(null);

  const categoriesOptions = [
    'Development', 
    'Business', 
    'Design', 
    'Marketing', 
    'IT & Software', 
    'Office Productivity', 
    'Personal Development', 
    'Photography'
  ];

  const showNotification = (msg, type = 'success') => {
    setFeedbackMsg({ text: msg, type });
    setTimeout(() => setFeedbackMsg(null), 3500);
  };

  const fetchDashboardData = async (isSilent = false) => {
    if (!isSilent) setLoading(true);
    else setRefreshing(true);

    try {
      const [statsRes, coursesRes, usersRes, mlRes] = await Promise.all([
        api.get('/admin/stats'),
        api.get('/admin/courses'),
        api.get('/admin/users'),
        api.get('/admin/ml-monitoring')
      ]);
      setStats(statsRes.data || { totalUsers: 0, totalCourses: 0, totalRevenue: 0 });
      setCourses(coursesRes.data || []);
      setUsers(usersRes.data || []);
      setMlStats(mlRes.data || {
        totalAnalyses: 0,
        highRiskCount: 0,
        pendingReviews: 0,
        totalReviews: 0,
        sentimentStats: [],
        analysesHistory: []
      });
    } catch (error) {
      console.error('Failed to fetch admin data', error);
      showNotification('Không thể tải dữ liệu quản trị viên. Vui lòng thử lại!', 'error');
    } finally {
      setLoading(false);
      setRefreshing(false);
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
      showNotification(`Đã chuyển trạng thái khóa học sang ${targetStatus}!`);
      fetchDashboardData(true);
    } catch (error) {
      console.error('Failed to update status', error);
      showNotification('Lỗi khi cập nhật trạng thái khóa học', 'error');
    }
  };

  const handleToggleUserStatus = async (id, currentStatus) => {
    try {
      await api.put(`/admin/users/${id}/toggle-status`);
      showNotification(currentStatus ? 'Đã khóa tài khoản thành công!' : 'Đã mở khóa tài khoản thành công!');
      fetchDashboardData(true);
    } catch (error) {
      console.error('Failed to update user status', error);
      showNotification('Lỗi khi cập nhật trạng thái tài khoản', 'error');
    }
  };

  const handleUpdateUserRole = async (id, newRole) => {
    try {
      await api.put(`/admin/users/${id}/role`, `"${newRole}"`, {
        headers: { 'Content-Type': 'application/json' }
      });
      showNotification(`Đã cập nhật vai trò người dùng thành ${newRole}!`);
      fetchDashboardData(true);
    } catch (error) {
      console.error('Failed to update user role', error);
      showNotification('Lỗi khi phân quyền người dùng', 'error');
    }
  };

  const handleApproveMl = async (analysisId) => {
    try {
      await api.post(`/admin/ml-analyses/${analysisId}/approve`);
      showNotification('Đã duyệt nhãn AI thành công!');
      fetchDashboardData(true);
    } catch (error) {
      console.error('Failed to approve ML analysis', error);
      showNotification('Lỗi duyệt nhãn AI', 'error');
    }
  };

  const handleOverrideMlSubmit = async () => {
    try {
      await api.post(`/admin/ml-analyses/${selectedAnalysisId}/override`, {
        categoryName: overrideCategory
      });
      showNotification('Đã ghi đè danh mục AI thành công!');
      setShowOverrideModal(false);
      fetchDashboardData(true);
    } catch (error) {
      console.error('Failed to override ML classification', error);
      showNotification('Lỗi ghi đè danh mục', 'error');
    }
  };

  // Filtered courses
  const filteredCourses = courses.filter(c => {
    const matchesSearch = !courseSearch || 
      c.title?.toLowerCase().includes(courseSearch.toLowerCase()) ||
      c.instructor?.fullName?.toLowerCase().includes(courseSearch.toLowerCase()) ||
      c.category?.name?.toLowerCase().includes(courseSearch.toLowerCase());
    
    const matchesStatus = courseStatusFilter === 'ALL' || c.status === courseStatusFilter;
    return matchesSearch && matchesStatus;
  });

  // Filtered users
  const filteredUsers = users.filter(u => {
    const matchesSearch = !userSearch ||
      u.fullName?.toLowerCase().includes(userSearch.toLowerCase()) ||
      u.email?.toLowerCase().includes(userSearch.toLowerCase());
    
    const matchesRole = userRoleFilter === 'ALL' || u.role === userRoleFilter;
    const matchesStatus = userStatusFilter === 'ALL' || 
      (userStatusFilter === 'ACTIVE' && u.isActive) ||
      (userStatusFilter === 'BLOCKED' && !u.isActive);

    return matchesSearch && matchesRole && matchesStatus;
  });

  const pendingCoursesCount = courses.filter(c => c.status === 'PendingApproval').length;

  return (
    <div className="admin-portal">
      {/* Toast Notification Banner */}
      {feedbackMsg && (
        <div 
          style={{
            position: 'fixed',
            top: '84px',
            right: '24px',
            zIndex: 9999,
            backgroundColor: feedbackMsg.type === 'error' ? '#ef4444' : '#10b981',
            color: '#ffffff',
            padding: '0.75rem 1.25rem',
            borderRadius: '8px',
            boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.2)',
            fontSize: '0.9rem',
            fontWeight: 600,
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            animation: 'fadeIn 0.2s ease-in'
          }}
        >
          <span>{feedbackMsg.type === 'error' ? '⚠️' : '✓'}</span>
          <span>{feedbackMsg.text}</span>
        </div>
      )}

      {/* Enterprise Dark Sidebar */}
      <aside className="admin-sidebar">
        <div className="admin-sidebar-brand">
          <div className="admin-brand-icon">
            <i className="bi bi-shield-lock-fill"></i>
          </div>
          <div>
            <h4 className="admin-brand-title">Edumy Admin</h4>
            <p className="admin-brand-subtitle">Control Center</p>
          </div>
        </div>

        <ul className="admin-nav">
          <li className="admin-nav-item">
            <button 
              className={`admin-nav-btn ${activeTab === 'courses' ? 'active' : ''}`}
              onClick={() => setActiveTab('courses')}
            >
              <span className="admin-nav-label">
                <i className="bi bi-journal-check"></i>
                Kiểm duyệt Khóa học
              </span>
              {pendingCoursesCount > 0 && (
                <span className="admin-nav-badge warning">{pendingCoursesCount}</span>
              )}
            </button>
          </li>
          <li className="admin-nav-item">
            <button 
              className={`admin-nav-btn ${activeTab === 'users' ? 'active' : ''}`}
              onClick={() => setActiveTab('users')}
            >
              <span className="admin-nav-label">
                <i className="bi bi-people-fill"></i>
                Quản lý Người dùng
              </span>
              <span className="admin-nav-badge">{users.length}</span>
            </button>
          </li>
          <li className="admin-nav-item">
            <button 
              className={`admin-nav-btn ${activeTab === 'ml' ? 'active' : ''}`}
              onClick={() => setActiveTab('ml')}
            >
              <span className="admin-nav-label">
                <i className="bi bi-cpu-fill"></i>
                Giám sát AI & ML
              </span>
              {mlStats.highRiskCount > 0 && (
                <span className="admin-nav-badge danger">{mlStats.highRiskCount}</span>
              )}
            </button>
          </li>
        </ul>
      </aside>

      {/* Main Content Area */}
      <main className="admin-main">
        {/* Header */}
        <div className="admin-header">
          <div>
            <h1 className="admin-title">
              {activeTab === 'courses' && 'Kiểm duyệt & Quản lý Khóa học'}
              {activeTab === 'users' && 'Danh bạ & Phân quyền Người dùng'}
              {activeTab === 'ml' && 'Hệ thống Giám sát & An toàn AI/ML'}
            </h1>
            <p className="admin-desc">
              Trung tâm điều hành nền tảng Edumy Enterprise — giám sát doanh thu, nội dung và người dùng thời gian thực.
            </p>
          </div>
          <div className="admin-header-actions">
            <button 
              className="btn-admin-refresh"
              onClick={() => fetchDashboardData(true)}
              disabled={refreshing || loading}
            >
              <i className={`bi bi-arrow-clockwise ${refreshing ? 'fa-spin' : ''}`}></i>
              {refreshing ? 'Đang cập nhật...' : 'Làm mới'}
            </button>
          </div>
        </div>

        {/* Top KPI Metrics Grid */}
        <div className="admin-kpi-grid">
          <div className="admin-kpi-card">
            <div className="admin-kpi-header">
              <span className="admin-kpi-label">Tổng doanh thu nền tảng</span>
              <div className="admin-kpi-icon-wrap success">
                <i className="bi bi-cash-stack"></i>
              </div>
            </div>
            <div className="admin-kpi-value success">
              ₫{(stats.totalRevenue || 0).toLocaleString('vi-VN')}
            </div>
            <p className="admin-kpi-hint">Tổng giá trị đơn hàng thanh toán thành công</p>
          </div>

          <div className="admin-kpi-card">
            <div className="admin-kpi-header">
              <span className="admin-kpi-label">Tài khoản thành viên</span>
              <div className="admin-kpi-icon-wrap primary">
                <i className="bi bi-person-badge"></i>
              </div>
            </div>
            <div className="admin-kpi-value">
              {(stats.totalUsers || 0).toLocaleString()}
            </div>
            <p className="admin-kpi-hint">Học viên, giảng viên và quản trị viên</p>
          </div>

          <div className="admin-kpi-card">
            <div className="admin-kpi-header">
              <span className="admin-kpi-label">Tổng khóa học</span>
              <div className="admin-kpi-icon-wrap warning">
                <i className="bi bi-collection-play"></i>
              </div>
            </div>
            <div className="admin-kpi-value">
              {stats.totalCourses || 0}
            </div>
            <p className="admin-kpi-hint">
              {pendingCoursesCount > 0 ? `${pendingCoursesCount} khóa đang chờ duyệt` : 'Tất cả trạng thái ổn định'}
            </p>
          </div>

          <div className="admin-kpi-card">
            <div className="admin-kpi-header">
              <span className="admin-kpi-label">Cảnh báo An toàn AI</span>
              <div className="admin-kpi-icon-wrap danger">
                <i className="bi bi-exclamation-triangle-fill"></i>
              </div>
            </div>
            <div className="admin-kpi-value danger">
              {mlStats.highRiskCount || 0}
            </div>
            <p className="admin-kpi-hint">Nội dung có nguy cơ độc hại hoặc spam cao</p>
          </div>
        </div>

        {/* TAB 1: COURSES MANAGEMENT */}
        {activeTab === 'courses' && (
          <div className="admin-card">
            <div className="admin-card-header">
              <div>
                <h3 className="admin-card-title">
                  <i className="bi bi-grid-3x3-gap-fill text-primary"></i>
                  Danh sách Khóa học
                </h3>
                <p className="admin-card-subtitle">
                  Hiển thị {filteredCourses.length} / {courses.length} khóa học trên hệ thống
                </p>
              </div>

              <div className="admin-card-toolbar">
                <div className="admin-search-input-wrap">
                  <i className="bi bi-search admin-search-icon"></i>
                  <input 
                    type="text"
                    className="admin-search-input"
                    placeholder="Tìm theo tên khóa học, giảng viên..."
                    value={courseSearch}
                    onChange={(e) => setCourseSearch(e.target.value)}
                  />
                </div>

                <select 
                  className="admin-filter-select"
                  value={courseStatusFilter}
                  onChange={(e) => setCourseStatusFilter(e.target.value)}
                >
                  <option value="ALL">Tất cả trạng thái</option>
                  <option value="PendingApproval">Chờ duyệt (Pending)</option>
                  <option value="Published">Đã xuất bản (Published)</option>
                  <option value="Draft">Bản nháp (Draft)</option>
                </select>
              </div>
            </div>

            <div className="admin-table-container">
              {loading ? (
                <div style={{ padding: '1.5rem' }}>
                  <div className="admin-skeleton-row"></div>
                  <div className="admin-skeleton-row"></div>
                  <div className="admin-skeleton-row"></div>
                </div>
              ) : filteredCourses.length === 0 ? (
                <div className="admin-empty-state">
                  <div className="admin-empty-icon">📂</div>
                  <h4 className="admin-empty-title">Không tìm thấy khóa học nào</h4>
                  <p className="admin-empty-desc">Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc trạng thái phía trên.</p>
                </div>
              ) : (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Khóa học</th>
                      <th>Giảng viên</th>
                      <th>Danh mục</th>
                      <th>Đánh giá</th>
                      <th>Giá niêm yết</th>
                      <th>Trạng thái</th>
                      <th style={{ textAlign: 'right' }}>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredCourses.map(course => (
                      <tr key={course.courseId}>
                        <td style={{ fontWeight: 600, color: '#0f172a' }}>
                          {course.title}
                        </td>
                        <td>{course.instructor?.fullName || 'Chưa xác định'}</td>
                        <td>
                          <span style={{ 
                            background: '#f1f5f9', 
                            padding: '0.2rem 0.5rem', 
                            borderRadius: '4px',
                            fontSize: '0.78rem',
                            fontWeight: 600
                          }}>
                            {course.category?.name || 'Chưa phân loại'}
                          </span>
                        </td>
                        <td>
                          <span style={{ color: '#d97706', fontWeight: 600 }}>
                            ★ {course.averageRating ? course.averageRating.toFixed(1) : 'Mới'}
                          </span>
                        </td>
                        <td style={{ fontWeight: 600 }}>
                          {course.price === 0 ? (
                            <span style={{ color: '#16a34a' }}>Miễn phí</span>
                          ) : (
                            `₫${(course.price || 0).toLocaleString('vi-VN')}`
                          )}
                        </td>
                        <td>
                          {course.status === 'Published' && (
                            <span className="admin-badge published">
                              <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#16a34a' }}></span>
                              Published
                            </span>
                          )}
                          {course.status === 'PendingApproval' && (
                            <span className="admin-badge pending">
                              <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#d97706' }}></span>
                              Chờ duyệt
                            </span>
                          )}
                          {course.status === 'Draft' && (
                            <span className="admin-badge draft">
                              <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#64748b' }}></span>
                              Bản nháp
                            </span>
                          )}
                        </td>
                        <td>
                          <div className="admin-action-btn-group">
                            {course.status === 'PendingApproval' ? (
                              <>
                                <button 
                                  className="btn-admin-action success"
                                  onClick={() => handleUpdateStatus(course.courseId, 'Published')}
                                  title="Duyệt và phát hành khóa học"
                                >
                                  <i className="bi bi-check-lg"></i> Duyệt
                                </button>
                                <button 
                                  className="btn-admin-action outline-danger"
                                  onClick={() => handleUpdateStatus(course.courseId, 'Draft')}
                                  title="Từ chối và trả về bản nháp"
                                >
                                  <i className="bi bi-x-lg"></i> Từ chối
                                </button>
                              </>
                            ) : (
                              <button 
                                className={`btn-admin-action ${course.status === 'Published' ? 'outline-secondary' : 'success'}`}
                                onClick={() => handleUpdateStatus(course.courseId, course.status === 'Published' ? 'Draft' : 'Published')}
                              >
                                {course.status === 'Published' ? (
                                  <><i className="bi bi-eye-slash"></i> Ẩn khóa học</>
                                ) : (
                                  <><i className="bi bi-check-circle"></i> Xuất bản</>
                                )}
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* TAB 2: USERS MANAGEMENT */}
        {activeTab === 'users' && (
          <div className="admin-card">
            <div className="admin-card-header">
              <div>
                <h3 className="admin-card-title">
                  <i className="bi bi-person-lines-fill text-primary"></i>
                  Danh bạ Tài khoản
                </h3>
                <p className="admin-card-subtitle">
                  Hiển thị {filteredUsers.length} / {users.length} người dùng trên hệ thống
                </p>
              </div>

              <div className="admin-card-toolbar">
                <div className="admin-search-input-wrap">
                  <i className="bi bi-search admin-search-icon"></i>
                  <input 
                    type="text"
                    className="admin-search-input"
                    placeholder="Tìm theo họ tên, email..."
                    value={userSearch}
                    onChange={(e) => setUserSearch(e.target.value)}
                  />
                </div>

                <select 
                  className="admin-filter-select"
                  value={userRoleFilter}
                  onChange={(e) => setUserRoleFilter(e.target.value)}
                >
                  <option value="ALL">Tất cả vai trò</option>
                  <option value="Student">Học viên (Student)</option>
                  <option value="Instructor">Giảng viên (Instructor)</option>
                  <option value="Admin">Quản trị viên (Admin)</option>
                </select>

                <select 
                  className="admin-filter-select"
                  value={userStatusFilter}
                  onChange={(e) => setUserStatusFilter(e.target.value)}
                >
                  <option value="ALL">Tất cả trạng thái</option>
                  <option value="ACTIVE">Đang hoạt động</option>
                  <option value="BLOCKED">Bị tạm khóa</option>
                </select>
              </div>
            </div>

            <div className="admin-table-container">
              {loading ? (
                <div style={{ padding: '1.5rem' }}>
                  <div className="admin-skeleton-row"></div>
                  <div className="admin-skeleton-row"></div>
                  <div className="admin-skeleton-row"></div>
                </div>
              ) : filteredUsers.length === 0 ? (
                <div className="admin-empty-state">
                  <div className="admin-empty-icon">👥</div>
                  <h4 className="admin-empty-title">Không tìm thấy người dùng phù hợp</h4>
                  <p className="admin-empty-desc">Vui lòng kiểm tra lại bộ lọc hoặc từ khóa tìm kiếm.</p>
                </div>
              ) : (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Họ và Tên</th>
                      <th>Email tài khoản</th>
                      <th>Phân quyền</th>
                      <th>Trạng thái</th>
                      <th style={{ textAlign: 'right' }}>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredUsers.map(u => (
                      <tr key={u.userId}>
                        <td style={{ fontWeight: 600, color: '#0f172a' }}>
                          {u.fullName}
                        </td>
                        <td>{u.email}</td>
                        <td>
                          <select 
                            value={u.role} 
                            onChange={(e) => handleUpdateUserRole(u.userId, e.target.value)}
                            className="admin-role-select"
                          >
                            <option value="Student">Student</option>
                            <option value="Instructor">Instructor</option>
                            <option value="Admin">Admin</option>
                          </select>
                        </td>
                        <td>
                          <span className={`admin-badge ${u.isActive ? 'active' : 'blocked'}`}>
                            <span style={{ 
                              width: 6, 
                              height: 6, 
                              borderRadius: '50%', 
                              background: u.isActive ? '#16a34a' : '#dc2626' 
                            }}></span>
                            {u.isActive ? 'Đang hoạt động' : 'Đã khóa'}
                          </span>
                        </td>
                        <td>
                          <div className="admin-action-btn-group">
                            <button 
                              onClick={() => handleToggleUserStatus(u.userId, u.isActive)}
                              className={`btn-admin-action ${u.isActive ? 'outline-danger' : 'success'}`}
                            >
                              {u.isActive ? (
                                <><i className="bi bi-slash-circle"></i> Khóa tài khoản</>
                              ) : (
                                <><i className="bi bi-unlock-fill"></i> Mở khóa</>
                              )}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* TAB 3: ML MONITORING & MODERATION */}
        {activeTab === 'ml' && (
          <div>
            <div className="ai-mon-grid">
              {/* Sentiment Overview Panel */}
              <div className="admin-card" style={{ marginBottom: 0 }}>
                <div className="admin-card-header">
                  <div>
                    <h3 className="admin-card-title">
                      <i className="bi bi-pie-chart-fill text-primary"></i>
                      Phân tích Cảm xúc Học viên (Sentiment)
                    </h3>
                    <p className="admin-card-subtitle">
                      Tổng số {mlStats.totalReviews || 0} bài đánh giá đã qua xử lý ML
                    </p>
                  </div>
                </div>
                <div style={{ padding: '1.25rem 1.5rem' }}>
                  {(!mlStats.sentimentStats || mlStats.sentimentStats.length === 0) ? (
                    <p className="text-muted small">Chưa có đủ dữ liệu thống kê cảm xúc.</p>
                  ) : (
                    mlStats.sentimentStats.map(stat => (
                      <div key={stat.label} className="ai-sentiment-item">
                        <span style={{ fontWeight: 600, color: '#334155' }}>
                          {stat.label === 'POS' ? '🟢 Tích cực (Positive)' : 
                           stat.label === 'NEG' ? '🔴 Tiêu cực (Negative)' : 
                           '🟡 Trung lập (Neutral)'}
                        </span>
                        <span style={{ 
                          fontWeight: 700, 
                          color: '#0f172a',
                          background: '#f1f5f9',
                          padding: '0.2rem 0.6rem',
                          borderRadius: '6px'
                        }}>
                          {stat.count} lượt
                        </span>
                      </div>
                    ))
                  )}
                </div>
              </div>

              {/* High Risk & Safety Panel */}
              <div className="ai-risk-panel">
                <div className="ai-risk-panel-title">
                  <i className="bi bi-shield-slash-fill"></i>
                  Chỉ số An toàn & Kiểm duyệt Thủ công
                </div>
                <p className="ai-risk-panel-desc">
                  Các khóa học hoặc bình luận vượt ngưỡng cảnh báo spam, độc hại hoặc có độ tin cậy AI thấp.
                </p>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginTop: '1rem' }}>
                  <div style={{ background: '#ffffff', padding: '1rem', borderRadius: '8px', border: '1px solid #fecaca' }}>
                    <div style={{ fontSize: '0.75rem', fontWeight: 700, color: '#991b1b', textTransform: 'uppercase' }}>
                      Cần duyệt thủ công
                    </div>
                    <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#d97706', marginTop: '0.25rem' }}>
                      {mlStats.pendingReviews || 0}
                    </div>
                    <div style={{ fontSize: '0.72rem', color: '#64748b' }}>Độ tin cậy &lt; 65%</div>
                  </div>

                  <div style={{ background: '#ffffff', padding: '1rem', borderRadius: '8px', border: '1px solid #fecaca' }}>
                    <div style={{ fontSize: '0.75rem', fontWeight: 700, color: '#991b1b', textTransform: 'uppercase' }}>
                      Nguy cơ cao (High Risk)
                    </div>
                    <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#dc2626', marginTop: '0.25rem' }}>
                      {mlStats.highRiskCount || 0}
                    </div>
                    <div style={{ fontSize: '0.72rem', color: '#64748b' }}>Cảnh báo độc hại / vi phạm</div>
                  </div>
                </div>
              </div>
            </div>

            {/* Auto Classification & Override Table */}
            <div className="admin-card">
              <div className="admin-card-header">
                <div>
                  <h3 className="admin-card-title">
                    <i className="bi bi-cpu text-primary"></i>
                    Nhật ký Tự động Phân loại & Quyền ghi đè (AI Overrides)
                  </h3>
                  <p className="admin-card-subtitle">
                    Kiểm tra dự đoán danh mục và can thiệp điều chỉnh nếu AI phân loại sai
                  </p>
                </div>
              </div>

              <div className="admin-table-container">
                {(!mlStats.analysesHistory || mlStats.analysesHistory.length === 0) ? (
                  <div className="admin-empty-state">
                    <div className="admin-empty-icon">🤖</div>
                    <h4 className="admin-empty-title">Chưa có dữ liệu phân loại</h4>
                    <p className="admin-empty-desc">Các phiên phân loại ML mới sẽ tự động hiển thị ở đây.</p>
                  </div>
                ) : (
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>Tên Khóa học</th>
                        <th>Danh mục Dự đoán</th>
                        <th>Độ tin cậy (Confidence)</th>
                        <th>Mức độ rủi ro</th>
                        <th>Trạng thái AI</th>
                        <th style={{ textAlign: 'right' }}>Quyền kiểm duyệt</th>
                      </tr>
                    </thead>
                    <tbody>
                      {mlStats.analysesHistory.map(log => {
                        const confPercent = Math.round((log.confidence || 0) * 100);
                        const confClass = (log.confidence > 0.85) ? 'high' : (log.confidence > 0.65) ? 'med' : 'low';
                        return (
                          <tr key={log.id}>
                            <td style={{ fontWeight: 600, color: '#0f172a' }}>{log.courseTitle}</td>
                            <td>
                              <span style={{ 
                                background: '#f1f5f9', 
                                padding: '0.2rem 0.5rem', 
                                borderRadius: '4px',
                                fontWeight: 600
                              }}>
                                {log.primaryCategory || 'Chưa rõ'}
                              </span>
                            </td>
                            <td>
                              <div className="ai-confidence-wrap">
                                <div className="ai-confidence-bar">
                                  <div 
                                    className={`ai-confidence-fill ${confClass}`}
                                    style={{ width: `${confPercent}%` }}
                                  ></div>
                                </div>
                                <span style={{ fontSize: '0.78rem', fontWeight: 700 }}>{confPercent}%</span>
                              </div>
                            </td>
                            <td>
                              <span className={`admin-badge ${log.riskLevel === 'High' ? 'high-risk' : 'low-risk'}`}>
                                {log.riskLevel === 'High' ? '⚠️ High Risk' : '✓ An toàn'}
                              </span>
                            </td>
                            <td>
                              <span style={{ 
                                fontSize: '0.78rem', 
                                padding: '0.2rem 0.5rem', 
                                borderRadius: '4px',
                                background: '#f8fafc',
                                border: '1px solid #cbd5e1',
                                fontWeight: 600
                              }}>
                                {log.status}
                              </span>
                            </td>
                            <td>
                              <div className="admin-action-btn-group">
                                <button 
                                  className="btn-admin-action success"
                                  onClick={() => handleApproveMl(log.id)}
                                  disabled={log.status === 'Approved'}
                                >
                                  <i className="bi bi-check-circle"></i> Duyệt
                                </button>
                                <button 
                                  className="btn-admin-action warning"
                                  onClick={() => {
                                    setSelectedAnalysisId(log.id);
                                    setShowOverrideModal(true);
                                  }}
                                >
                                  <i className="bi bi-pencil-square"></i> Ghi đè
                                </button>
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Override Category Modal Dialog */}
      {showOverrideModal && (
        <div className="admin-modal-overlay" onClick={() => setShowOverrideModal(false)}>
          <div className="admin-modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="admin-modal-header">
              <h4 className="admin-modal-title">Ghi đè Danh mục Khóa học</h4>
              <button 
                type="button" 
                className="admin-modal-close"
                onClick={() => setShowOverrideModal(false)}
              >
                &times;
              </button>
            </div>
            <div className="admin-modal-body">
              <p style={{ fontSize: '0.88rem', color: '#64748b', marginBottom: '1.25rem' }}>
                Chọn danh mục chính xác do chuyên viên quản trị viên thẩm định thay cho kết quả tự động của mô hình AI:
              </p>
              <div>
                <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 700, color: '#334155', marginBottom: '0.5rem' }}>
                  Danh mục chuẩn:
                </label>
                <select 
                  value={overrideCategory} 
                  onChange={(e) => setOverrideCategory(e.target.value)}
                  className="admin-filter-select"
                  style={{ width: '100%', padding: '0.6rem 0.75rem' }}
                >
                  {categoriesOptions.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>
            </div>
            <div className="admin-modal-footer">
              <button 
                type="button" 
                className="btn-admin-action outline-secondary"
                onClick={() => setShowOverrideModal(false)}
              >
                Hủy bỏ
              </button>
              <button 
                type="button" 
                className="btn-admin-action success"
                onClick={handleOverrideMlSubmit}
              >
                Lưu ghi đè
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;
