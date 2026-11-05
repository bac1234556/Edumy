import React, { useState, useEffect, useContext, useMemo } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import {
  BookOpen,
  PlayCircle,
  Award,
  Search,
  CheckCircle2,
  Clock,
  ArrowRight,
  Sparkles,
  Layers
} from 'lucide-react';
import './MyLearning.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function MyLearning() {
  const { user } = useContext(AuthContext);
  const [enrolledCourses, setEnrolledCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Tab: 'all' | 'in_progress' | 'completed'
  const [activeTab, setActiveTab] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const fetchEnrolledCourses = async () => {
      try {
        setLoading(true);
        const response = await api.get('/courses/enrolled');
        setEnrolledCourses(response.data || []);
      } catch (err) {
        setError('Không thể tải danh sách khóa học của bạn.');
      } finally {
        setLoading(false);
      }
    };
    fetchEnrolledCourses();
  }, []);

  // Filtered courses based on Tab & Search Query
  const filteredCourses = useMemo(() => {
    return enrolledCourses.filter((item) => {
      // 1. Tab filter
      const progress = item.progressPercentage || 0;
      if (activeTab === 'in_progress' && progress >= 100) return false;
      if (activeTab === 'completed' && progress < 100) return false;

      // 2. Search query filter
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase().trim();
        const titleMatch = item.title?.toLowerCase().includes(query);
        const instructorMatch = (item.fullName || item.instructor)?.toLowerCase().includes(query);
        return titleMatch || instructorMatch;
      }

      return true;
    });
  }, [enrolledCourses, activeTab, searchQuery]);

  const countInProgress = useMemo(() => {
    return enrolledCourses.filter((c) => (c.progressPercentage || 0) < 100).length;
  }, [enrolledCourses]);

  const countCompleted = useMemo(() => {
    return enrolledCourses.filter((c) => (c.progressPercentage || 0) >= 100).length;
  }, [enrolledCourses]);

  return (
    <div className="my-learning-page">
      {/* --------------------------------------------------------------------
          1. HEADER BANNER
          -------------------------------------------------------------------- */}
      <div className="my-learning-header">
        <div className="container">
          <h1>Không gian học tập cá nhân</h1>
          <p className="my-learning-subtitle">
            Chào mừng {user?.fullName || 'bạn'} trở lại! Tiếp tục hoàn thành các mục tiêu tri thức hôm nay.
          </p>

          {/* Filter Tabs */}
          <div className="learning-tabs">
            <div
              className={`tab ${activeTab === 'all' ? 'active' : ''}`}
              onClick={() => setActiveTab('all')}
            >
              <BookOpen size={18} /> Tất cả khóa học
              <span className="tab-count-badge">{enrolledCourses.length}</span>
            </div>
            <div
              className={`tab ${activeTab === 'in_progress' ? 'active' : ''}`}
              onClick={() => setActiveTab('in_progress')}
            >
              <Clock size={18} /> Đang học
              <span className="tab-count-badge">{countInProgress}</span>
            </div>
            <div
              className={`tab ${activeTab === 'completed' ? 'active' : ''}`}
              onClick={() => setActiveTab('completed')}
            >
              <Award size={18} /> Đã hoàn thành
              <span className="tab-count-badge">{countCompleted}</span>
            </div>
          </div>
        </div>
      </div>

      {/* --------------------------------------------------------------------
          2. CONTROLS BAR (SEARCH & STATS)
          -------------------------------------------------------------------- */}
      <div className="container">
        <div className="learning-controls-bar">
          <div className="learning-search-box">
            <Search size={18} className="learning-search-icon" />
            <input
              type="text"
              className="learning-search-input"
              placeholder="Tìm kiếm trong khóa học của bạn..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          <div className="d-flex align-items-center gap-3 text-muted small">
            <span>
              Đang hiển thị <strong>{filteredCourses.length}</strong> / {enrolledCourses.length} khóa học
            </span>
          </div>
        </div>

        {/* --------------------------------------------------------------------
            3. MAIN CONTENT: SKELETON / EMPTY / GRID
            -------------------------------------------------------------------- */}
        {loading ? (
          <div className="enrolled-grid">
            {[1, 2, 3, 4].map((n) => (
              <div key={n} className="enrolled-skeleton-card">
                <div className="skeleton-box" style={{ width: '100%', height: '170px' }}></div>
                <div className="p-3 d-flex flex-column flex-grow-1">
                  <div className="skeleton-box mb-2" style={{ width: '85%', height: '20px' }}></div>
                  <div className="skeleton-box mb-4" style={{ width: '50%', height: '16px' }}></div>
                  <div className="skeleton-box mb-2" style={{ width: '100%', height: '8px' }}></div>
                  <div className="skeleton-box mt-auto" style={{ width: '100%', height: '36px' }}></div>
                </div>
              </div>
            ))}
          </div>
        ) : error ? (
          <div className="text-center py-5 text-danger">{error}</div>
        ) : enrolledCourses.length === 0 ? (
          /* Empty enrolled courses */
          <div className="empty-learning-box">
            <div className="empty-learning-icon-box">
              <BookOpen size={38} />
            </div>
            <h3 className="empty-learning-title">Bạn chưa đăng ký khóa học nào</h3>
            <p className="empty-learning-subtitle">
              Bắt đầu hành trình bứt phá sự nghiệp ngay hôm nay bằng việc lựa chọn khóa học phù hợp với định hướng của bạn.
            </p>
            <Link to="/courses" className="btn-continue-shopping">
              Khám phá các khóa học ngay <ArrowRight size={18} />
            </Link>
          </div>
        ) : filteredCourses.length === 0 ? (
          /* No search matches */
          <div className="empty-learning-box">
            <div className="empty-learning-icon-box">
              <Search size={38} />
            </div>
            <h3 className="empty-learning-title">Không tìm thấy khóa học phù hợp</h3>
            <p className="empty-learning-subtitle">
              Không có khóa học nào khớp với từ khóa "{searchQuery}" trong danh mục đã chọn.
            </p>
            <button
              onClick={() => {
                setSearchQuery('');
                setActiveTab('all');
              }}
              className="btn btn-outline-primary rounded-pill px-4"
            >
              Xóa bộ lọc
            </button>
          </div>
        ) : (
          /* Enrolled Courses Grid */
          <div className="enrolled-grid">
            {filteredCourses.map((item) => {
              const progress = item.progressPercentage || 0;
              const isCompleted = progress >= 100;

              return (
                <div key={item.courseId} className="enrolled-card">
                  {/* Thumbnail */}
                  <Link to={`/my-courses/${item.courseId}/learn`} className="card-img-wrapper">
                    <img
                      src={
                        item.thumbnailUrl
                          ? item.thumbnailUrl.startsWith('http')
                            ? item.thumbnailUrl
                            : `${BACKEND_URL}${item.thumbnailUrl}`
                          : 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400&q=80'
                      }
                      alt={item.title}
                      className="card-img-top"
                    />
                    <div className="play-overlay-hover">
                      <div className="play-circle-badge">
                        <PlayCircle size={26} />
                      </div>
                    </div>
                  </Link>

                  {/* Body */}
                  <div className="enrolled-card-body">
                    <Link
                      to={`/my-courses/${item.courseId}/learn`}
                      className="text-decoration-none"
                    >
                      <h4 className="enrolled-course-title">{item.title}</h4>
                    </Link>
                    <p className="enrolled-instructor-name">
                      {item.fullName || item.instructor || 'Giảng viên Edumy'}
                    </p>

                    {/* Progress */}
                    <div className="progress-meta-row">
                      <span>{isCompleted ? 'Đã hoàn thành' : 'Tiến độ học tập'}</span>
                      <span className={isCompleted ? 'text-success' : 'text-primary'}>
                        {progress}%
                      </span>
                    </div>

                    <div className="progress-track-bg">
                      <div
                        className={`progress-fill-bar ${isCompleted ? 'completed' : ''}`}
                        style={{ width: `${Math.min(100, Math.max(0, progress))}%` }}
                      ></div>
                    </div>

                    {/* Footer Actions */}
                    <div className="enrolled-card-footer">
                      <Link
                        to={`/my-courses/${item.courseId}/learn`}
                        className="btn-continue-learning"
                      >
                        <PlayCircle size={16} />
                        {isCompleted ? 'Xem lại bài học' : 'Tiếp tục học'}
                      </Link>

                      {isCompleted && item.certificateUrl && (
                        <Link
                          to={`/certificates/${item.certificateUrl}`}
                          className="btn-view-certificate"
                          title="Xem chứng chỉ tốt nghiệp"
                        >
                          <Award size={16} /> Chứng chỉ
                        </Link>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

export default MyLearning;
