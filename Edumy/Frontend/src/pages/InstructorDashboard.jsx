import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import {
  Users,
  BookOpen,
  DollarSign,
  Star,
  Sparkles,
  AlertTriangle,
  PlayCircle,
  Layers,
  Plus,
  BarChart3,
  TrendingUp,
  GraduationCap,
  ArrowRight,
  Edit,
  Calendar,
  CheckCircle2,
  Clock
} from 'lucide-react';
import EmptyState from '../components/EmptyState';
import { SkeletonBox } from '../components/Skeleton';
import './InstructorDashboard.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function InstructorDashboard() {
  const [courses, setCourses] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Tabs: 'courses' | 'analytics' | 'students'
  const [activeTab, setActiveTab] = useState('courses');

  // Student roster state for selected course
  const [selectedCourseForRoster, setSelectedCourseForRoster] = useState(null);
  const [rosterStudents, setRosterStudents] = useState([]);
  const [loadingRoster, setLoadingRoster] = useState(false);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [coursesRes, statsRes] = await Promise.all([
        api.get('/courses/my-courses'),
        api.get('/instructor/stats')
      ]);

      const courseList = coursesRes.data || [];
      setCourses(courseList);
      setStats(statsRes.data);

      if (courseList.length > 0 && !selectedCourseForRoster) {
        setSelectedCourseForRoster(courseList[0].courseId);
      }
    } catch (err) {
      setError('Lỗi khi tải dữ liệu Bảng điều khiển Giảng viên.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Fetch student roster when activeTab === 'students' or selectedCourse changes
  useEffect(() => {
    if (activeTab === 'students' && selectedCourseForRoster) {
      const fetchRoster = async () => {
        try {
          setLoadingRoster(true);
          const res = await api.get(`/Instructor/courses/${selectedCourseForRoster}/students`);
          setRosterStudents(res.data || []);
        } catch (err) {
          console.warn('Could not fetch student roster:', err);
          setRosterStudents([]);
        } finally {
          setLoadingRoster(false);
        }
      };
      fetchRoster();
    }
  }, [activeTab, selectedCourseForRoster]);

  // Trigger AI content reanalysis
  const handleTriggerAnalysis = async (courseId) => {
    try {
      setLoading(true);
      await api.put(`/courses/${courseId}/status`, { status: "Analyzing" });
      alert('Đã kích hoạt phân tích nội dung AI thành công! Khóa học chuyển sang trạng thái Phân tích/Chờ duyệt.');
      fetchData();
    } catch (err) {
      alert('Không thể kích hoạt AI phân tích: ' + (err.response?.data?.message || err.message));
      setLoading(false);
    }
  };

  const formatPrice = (price) => {
    if (!price && price !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  };

  if (loading && !stats) {
    return (
      <div className="instructor-dashboard-wrapper">
        <div className="instructor-container">
          <div className="skeleton-box mb-4" style={{ width: '320px', height: '40px' }}></div>
          <div className="kpi-cards-grid">
            {[1, 2, 3, 4].map(n => (
              <div key={n} className="kpi-card skeleton-box" style={{ height: '130px' }}></div>
            ))}
          </div>
          <div className="skeleton-box" style={{ width: '100%', height: '400px', borderRadius: '16px' }}></div>
        </div>
      </div>
    );
  }

  return (
    <div className="instructor-dashboard-wrapper">
      <div className="instructor-container">
        {/* ------------------------------------------------------------------
            1. STUDIO HEADER
            ------------------------------------------------------------------ */}
        <div className="instructor-header-box">
          <div className="instructor-title-area">
            <h2>
              <Layers className="text-primary" /> Instructor Studio
            </h2>
            <p>Quản lý giáo trình, theo dõi KPI doanh thu và đánh giá chất lượng bài giảng từ AI.</p>
          </div>

          <div className="instructor-header-actions">
            <Link to="/instructor/courses/new" className="btn-create-course">
              <Plus size={18} /> Tạo Khóa học mới
            </Link>
          </div>
        </div>

        {error && <div className="alert alert-danger mb-4 shadow-sm">{error}</div>}

        {/* ------------------------------------------------------------------
            2. 4 KPI STATS CARDS
            ------------------------------------------------------------------ */}
        {stats && (
          <div className="kpi-cards-grid">
            {/* Total Courses */}
            <div className="kpi-card">
              <div className="kpi-top-row">
                <span className="kpi-label">Tổng khóa học</span>
                <div className="kpi-icon-wrap sky">
                  <BookOpen size={20} />
                </div>
              </div>
              <div className="kpi-value">{stats.totalCourses || courses.length || 0}</div>
            </div>

            {/* Active Students */}
            <div className="kpi-card">
              <div className="kpi-top-row">
                <span className="kpi-label">Học viên tích cực</span>
                <div className="kpi-icon-wrap emerald">
                  <Users size={20} />
                </div>
              </div>
              <div className="kpi-value">{stats.totalStudents || 0}</div>
            </div>

            {/* Total Revenue */}
            <div className="kpi-card">
              <div className="kpi-top-row">
                <span className="kpi-label">Tổng doanh thu</span>
                <div className="kpi-icon-wrap amber">
                  <DollarSign size={20} />
                </div>
              </div>
              <div className="kpi-value">{formatPrice(stats.totalRevenue)}</div>
            </div>

            {/* AI Quality Score */}
            <div className="kpi-card">
              <div className="kpi-top-row">
                <span className="kpi-label">Điểm chất lượng AI</span>
                <div className="kpi-icon-wrap indigo">
                  <Sparkles size={20} />
                </div>
              </div>
              <div className="kpi-value">{stats.averageQualityScore || 92}%</div>
            </div>
          </div>
        )}

        {/* ------------------------------------------------------------------
            3. ACTIONABLE AI RECOMMENDATIONS
            ------------------------------------------------------------------ */}
        {stats?.recommendations && stats.recommendations.length > 0 && (
          <div className="ai-insights-alert">
            <h5 className="ai-insights-title">
              <AlertTriangle size={18} /> Khuyến nghị cải tiến nội dung từ AI Microservice:
            </h5>
            <ul className="ai-insights-list">
              {stats.recommendations.map((rec, i) => (
                <li key={i}>{rec}</li>
              ))}
            </ul>
          </div>
        )}

        {/* ------------------------------------------------------------------
            4. TABS NAVIGATION
            ------------------------------------------------------------------ */}
        <div className="instructor-nav-tabs">
          <button
            className={`studio-tab-btn ${activeTab === 'courses' ? 'active' : ''}`}
            onClick={() => setActiveTab('courses')}
          >
            <BookOpen size={18} /> Khóa học của tôi ({courses.length})
          </button>
          <button
            className={`studio-tab-btn ${activeTab === 'analytics' ? 'active' : ''}`}
            onClick={() => setActiveTab('analytics')}
          >
            <BarChart3 size={18} /> Thống kê & Phân tích AI
          </button>
          <button
            className={`studio-tab-btn ${activeTab === 'students' ? 'active' : ''}`}
            onClick={() => setActiveTab('students')}
          >
            <GraduationCap size={18} /> Danh sách Học viên
          </button>
        </div>

        {/* ------------------------------------------------------------------
            5. TAB 1: COURSES MANAGEMENT
            ------------------------------------------------------------------ */}
        {activeTab === 'courses' && (
          <div>
            {courses.length === 0 ? (
              <div className="text-center py-5 bg-white rounded-4 border p-5 shadow-sm">
                <BookOpen size={48} className="text-muted opacity-50 mb-3" />
                <h4 className="fw-bold mb-2">Bạn chưa có khóa học nào</h4>
                <p className="text-muted mb-4">
                  Bắt đầu chia sẻ tri thức của bạn với hàng ngàn học viên trên Edumy!
                </p>
                <Link to="/instructor/courses/new" className="btn-create-course">
                  <Plus size={18} /> Tạo khóa học đầu tiên
                </Link>
              </div>
            ) : (
              <div className="studio-courses-grid">
                {courses.map((course) => {
                  const statusKey = (course.status || 'Draft').toLowerCase();
                  return (
                    <div key={course.courseId} className="studio-course-card">
                      {/* Thumbnail */}
                      <div className="studio-course-thumb-box">
                        <img
                          src={
                            course.thumbnailUrl
                              ? course.thumbnailUrl.startsWith('http')
                                ? course.thumbnailUrl
                                : `${BACKEND_URL}${course.thumbnailUrl}`
                              : 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=600&q=80'
                          }
                          alt={course.title}
                          className="studio-course-thumb"
                        />
                        <span className={`status-badge-pill ${statusKey}`}>
                          {course.status}
                        </span>
                      </div>

                      {/* Content */}
                      <div className="studio-course-body">
                        <h4 className="studio-course-title">{course.title}</h4>

                        <div className="studio-course-meta">
                          <span className="studio-course-price">
                            {formatPrice(course.price)}
                          </span>
                          <span>{course.enrollmentCount || 0} học viên</span>
                        </div>

                        {course.needsReanalysis && (
                          <div className="alert alert-warning py-1 px-2 small mb-3 d-flex align-items-center gap-1">
                            <AlertTriangle size={14} /> Nội dung bài học vừa thay đổi
                          </div>
                        )}

                        {/* Action Buttons */}
                        <div className="studio-course-actions-row">
                          <Link
                            to={`/instructor/courses/${course.courseId}/curriculum`}
                            className="btn-studio-action"
                          >
                            <Layers size={14} /> Quản lý bài giảng
                          </Link>

                          <Link
                            to={`/instructor/courses/${course.courseId}/edit`}
                            className="btn-studio-action outline-secondary"
                          >
                            <Edit size={14} /> Sửa thông tin
                          </Link>

                          {course.needsReanalysis && (
                            <button
                              type="button"
                              onClick={() => handleTriggerAnalysis(course.courseId)}
                              className="btn-studio-action ai-btn"
                            >
                              <Sparkles size={14} /> Phân tích AI
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* ------------------------------------------------------------------
            6. TAB 2: ANALYTICS & REVIEWS
            ------------------------------------------------------------------ */}
        {activeTab === 'analytics' && stats && (
          <div className="analytics-grid">
            {/* Revenue Breakdown */}
            <div className="analytics-panel-card">
              <h4 className="analytics-panel-title d-flex align-items-center gap-2">
                <TrendingUp size={20} className="text-success" /> Doanh thu 6 tháng gần nhất
              </h4>
              {(!stats.revenueByDate || stats.revenueByDate.length === 0) ? (
                <p className="text-muted small py-4 text-center">Chưa có bản ghi doanh thu.</p>
              ) : (
                <div className="revenue-bar-list">
                  {stats.revenueByDate.map((rev) => (
                    <div key={rev.date} className="revenue-bar-item">
                      <span className="revenue-month-label">{rev.date}</span>
                      <span className="revenue-amount-tag">{formatPrice(rev.revenue)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Sentiment Breakdown */}
            <div className="analytics-panel-card">
              <h4 className="analytics-panel-title d-flex align-items-center gap-2">
                <Sparkles size={20} className="text-warning" /> Phân tích cảm xúc học viên (AI)
              </h4>
              {(!stats.sentimentStats || stats.sentimentStats.length === 0) ? (
                <p className="text-muted small py-4 text-center">Chưa có dữ liệu cảm xúc đánh giá.</p>
              ) : (
                <div>
                  {stats.sentimentStats.map((sent) => (
                    <div key={sent.label} className="sentiment-row">
                      <span className="fw-semibold small">{sent.label}</span>
                      <span
                        className={`sentiment-badge-pill ${
                          sent.label === 'Positive'
                            ? 'positive'
                            : sent.label === 'Negative'
                            ? 'negative'
                            : 'neutral'
                        }`}
                      >
                        {sent.count} phản hồi
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Recent Reviews Roster */}
            {stats.recentReviews && stats.recentReviews.length > 0 && (
              <div className="analytics-panel-card" style={{ gridColumn: '1 / -1' }}>
                <h4 className="analytics-panel-title">Đánh giá mới nhất từ học viên</h4>
                <div className="d-flex flex-column gap-3">
                  {stats.recentReviews.map((r) => (
                    <div key={r.reviewId} className="p-3 bg-light rounded-3 border">
                      <div className="d-flex justify-content-between align-items-center mb-1">
                        <div>
                          <strong className="text-dark">{r.studentName}</strong>{' '}
                          <span className="text-muted small">trên "{r.courseTitle}"</span>
                        </div>
                        <div className="stars">
                          {[...Array(5)].map((_, i) => (
                            <Star
                              key={i}
                              size={13}
                              fill={i < r.rating ? '#f59e0b' : 'none'}
                              color="#f59e0b"
                            />
                          ))}
                        </div>
                      </div>
                      <p className="mb-0 text-body small">{r.comment}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* ------------------------------------------------------------------
            7. TAB 3: STUDENT ROSTER (ROSTER BY COURSE)
            ------------------------------------------------------------------ */}
        {activeTab === 'students' && (
          <div className="roster-card">
            <h4 className="analytics-panel-title mb-3">Danh sách Học viên theo Khóa học</h4>
            <div className="roster-course-select mb-4">
              <label className="form-label fw-bold small text-muted">Chọn khóa học:</label>
              <select
                className="form-select"
                value={selectedCourseForRoster || ''}
                onChange={(e) => setSelectedCourseForRoster(Number(e.target.value))}
              >
                {courses.map((c) => (
                  <option key={c.courseId} value={c.courseId}>
                    {c.title} ({c.enrollmentCount || 0} học viên)
                  </option>
                ))}
              </select>
            </div>

            {loadingRoster ? (
              <div className="py-4 d-flex flex-column gap-2">
                <SkeletonBox width="100%" height="48px" />
                <SkeletonBox width="100%" height="48px" />
                <SkeletonBox width="100%" height="48px" />
              </div>
            ) : rosterStudents.length === 0 ? (
              <EmptyState
                icon={Users}
                title="Chưa có học viên ghi danh"
                description="Khóa học này hiện chưa có học viên nào đăng ký. Khi có lượt đăng ký mới, danh sách và tiến độ sẽ hiển thị tại đây."
              />
            ) : (
              <div className="table-responsive">
                <table className="roster-table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Học viên</th>
                      <th>Email</th>
                      <th>Ngày ghi danh</th>
                      <th>Tiến độ</th>
                      <th>Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rosterStudents.map((st, idx) => (
                      <tr key={st.userId || idx}>
                        <td>#{idx + 1}</td>
                        <td>
                          <strong>{st.fullName || 'Học viên'}</strong>
                        </td>
                        <td className="text-muted">{st.email}</td>
                        <td>{st.enrolledAt ? new Date(st.enrolledAt).toLocaleDateString('vi-VN') : 'Gần đây'}</td>
                        <td>
                          <div className="d-flex align-items-center gap-2">
                            <div className="progress-track-bg" style={{ width: '80px', height: '6px', margin: 0 }}>
                              <div
                                className="progress-fill-bar"
                                style={{ width: `${st.progressPercentage || 0}%` }}
                              ></div>
                            </div>
                            <span className="small fw-bold">{st.progressPercentage || 0}%</span>
                          </div>
                        </td>
                        <td>
                          <span className="badge bg-success bg-opacity-10 text-success rounded-pill px-2 py-1">
                            {st.status || 'Active'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default InstructorDashboard;
