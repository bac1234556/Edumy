import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Users, BookOpen, DollarSign, Star, Sparkles, AlertTriangle, PlayCircle } from 'lucide-react';
import './InstructorDashboard.css';

function InstructorDashboard() {
  const [courses, setCourses] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('courses'); // courses, analytics, reviews

  const fetchData = async () => {
    try {
      const coursesRes = await api.get('/courses/my-courses');
      setCourses(coursesRes.data);
      
      const statsRes = await api.get('/instructor/stats');
      setStats(statsRes.data);
    } catch (err) {
      setError('Lỗi khi tải thông tin bảng điều khiển.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleTriggerAnalysis = async (courseId) => {
    try {
      setLoading(true);
      await api.post(`/courses/${courseId}/status`, '"Analyzing"', {
        headers: { 'Content-Type': 'application/json' }
      });
      alert('AI content analysis triggered successfully!');
      fetchData();
    } catch (err) {
      alert('Failed to trigger AI content analysis.');
      setLoading(false);
    }
  };

  if (loading) return <div className="loading py-5 text-center">Loading Instructor Portal...</div>;
  if (error) return <div className="error text-danger text-center py-5">{error}</div>;

  return (
    <div className="instructor-dashboard container my-5">
      <div className="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom">
        <div>
          <h2 className="fw-bold text-primary m-0">Instructor Dashboard</h2>
          <p className="text-muted m-0 small">Manage your lessons, analyze quality scores, and view student feedback.</p>
        </div>
        <div className="d-flex gap-3">
          <div className="btn-group">
            <button 
              className={`btn btn-sm ${activeTab === 'courses' ? 'btn-primary' : 'btn-outline-primary'}`} 
              onClick={() => setActiveTab('courses')}
            >
              My Courses
            </button>
            <button 
              className={`btn btn-sm ${activeTab === 'analytics' ? 'btn-primary' : 'btn-outline-primary'}`} 
              onClick={() => setActiveTab('analytics')}
            >
              Stats & ML Analytics
            </button>
          </div>
          <Link to="/instructor/courses/new" className="btn btn-primary fw-bold">Create New Course</Link>
        </div>
      </div>

      {stats && (
        <div className="row mb-5 g-4">
          <div className="col-md-3">
            <div className="card shadow-sm border-0 h-100 bg-white">
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <h6 className="text-muted mb-0 small uppercase fw-bold">Total Courses</h6>
                  <div className="bg-primary bg-opacity-10 p-2 rounded"><BookOpen size={18} className="text-primary"/></div>
                </div>
                <h3 className="mb-0 fw-bold">{stats.totalCourses}</h3>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card shadow-sm border-0 h-100 bg-white">
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <h6 className="text-muted mb-0 small uppercase fw-bold">Active Students</h6>
                  <div className="bg-success bg-opacity-10 p-2 rounded"><Users size={18} className="text-success"/></div>
                </div>
                <h3 className="mb-0 fw-bold">{stats.totalStudents}</h3>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card shadow-sm border-0 h-100 bg-white">
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <h6 className="text-muted mb-0 small uppercase fw-bold">Total Revenue</h6>
                  <div className="bg-warning bg-opacity-10 p-2 rounded"><DollarSign size={18} className="text-warning"/></div>
                </div>
                <h3 className="mb-0 fw-bold">₫{stats.totalRevenue?.toLocaleString('vi-VN')}</h3>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card shadow-sm border-0 h-100 bg-white">
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <h6 className="text-muted mb-0 small uppercase fw-bold">AI Quality Rating</h6>
                  <div className="bg-info bg-opacity-10 p-2 rounded"><Sparkles size={18} className="text-info"/></div>
                </div>
                <h3 className="mb-0 fw-bold">{stats.averageQualityScore}%</h3>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Warnings & Suggestions Section */}
      {stats && stats.recommendations && stats.recommendations.length > 0 && (
        <div className="alert alert-warning border-0 shadow-sm p-4 mb-5" style={{ borderRadius: '12px' }}>
          <h5 className="fw-bold d-flex align-items-center gap-2 mb-3">
            <AlertTriangle size={20} /> Actionable Improvement Steps
          </h5>
          <ul className="mb-0 ps-3">
            {stats.recommendations.map((rec, i) => (
              <li key={i} className="mb-2 small">{rec}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Tab: Course List */}
      {activeTab === 'courses' && (
        <div>
          <h4 className="mb-3 fw-bold">Course Curriculum</h4>
          <div className="course-list-grid">
            {courses.length === 0 ? (
              <p className="text-muted">You have not created any courses yet.</p>
            ) : (
              courses.map(course => (
                <div key={course.courseId} className="dashboard-course-card border-0 shadow-sm bg-white p-3 d-flex gap-3 mb-3 rounded" style={{ border: '1px solid var(--border-color)' }}>
                  <img 
                    src={course.thumbnailUrl ? (course.thumbnailUrl.startsWith('http') ? course.thumbnailUrl : `http://localhost:5150${course.thumbnailUrl}`) : 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80'} 
                    alt={course.title} 
                    className="course-thumb rounded" 
                    style={{ width: '180px', height: '110px', objectFit: 'cover' }}
                  />
                  <div className="course-info d-flex flex-column flex-grow-1 justify-content-between">
                    <div>
                      <h5 className="fw-bold mb-1">{course.title}</h5>
                      <div className="d-flex align-items-center gap-3">
                        <span className={`badge ${course.status === 'Published' ? 'bg-success' : course.status === 'PendingApproval' ? 'bg-primary' : 'bg-warning'} rounded-pill`}>
                          {course.status}
                        </span>
                        {course.needsReanalysis && (
                          <span className="badge bg-danger rounded-pill d-flex align-items-center gap-1">
                            <AlertTriangle size={12} /> Content Changed
                          </span>
                        )}
                        <span className="small text-muted">Price: ₫{(course.price || 0).toLocaleString()}</span>
                      </div>
                    </div>
                    <div className="d-flex gap-3">
                      <Link to={`/instructor/courses/${course.courseId}/edit`} className="btn btn-sm btn-outline-primary px-3">
                        Edit Course
                      </Link>
                      {course.needsReanalysis && (
                        <button 
                          onClick={() => handleTriggerAnalysis(course.courseId)}
                          className="btn btn-sm btn-warning d-flex align-items-center gap-1 px-3"
                        >
                          <Sparkles size={14} /> Run AI Analysis
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Tab: Analytics & ML Charts */}
      {activeTab === 'analytics' && stats && (
        <div className="row g-4">
          <div className="col-md-6">
            <div className="card border-0 shadow-sm bg-white p-4 h-100">
              <h5 className="fw-bold mb-3">Revenue Breakdown (Last 6 Months)</h5>
              {stats.revenueByDate?.length === 0 ? (
                <p className="text-muted small py-4 text-center">No sales records found.</p>
              ) : (
                <div className="d-flex flex-column gap-3 mt-3">
                  {stats.revenueByDate?.map(rev => (
                    <div key={rev.date} className="d-flex justify-content-between align-items-center border-bottom pb-2">
                      <span className="fw-bold small">{rev.date}</span>
                      <span className="badge bg-success font-monospace">₫{(rev.revenue || 0).toLocaleString()}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="col-md-6">
            <div className="card border-0 shadow-sm bg-white p-4 h-100">
              <h5 className="fw-bold mb-3">Reviews Sentiment Distribution</h5>
              {stats.sentimentStats?.length === 0 ? (
                <p className="text-muted small py-4 text-center">No review sentiment ratings compiled yet.</p>
              ) : (
                <div className="d-flex flex-column gap-3 mt-3">
                  {stats.sentimentStats?.map(sent => (
                    <div key={sent.label} className="d-flex justify-content-between align-items-center border-bottom pb-2">
                      <span className="fw-bold small">{sent.label}</span>
                      <span className={`badge ${sent.label === 'Positive' ? 'bg-success' : sent.label === 'Negative' ? 'bg-danger' : 'bg-warning'}`}>
                        {sent.count} reviews
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {stats.recentReviews && stats.recentReviews.length > 0 && (
            <div className="col-12 mt-4">
              <h4 className="fw-bold mb-3">Recent Reviews</h4>
              <div className="card shadow-sm border-0 bg-white">
                <ul className="list-group list-group-flush">
                  {stats.recentReviews.map(review => (
                    <li key={review.reviewId} className="list-group-item p-3 bg-transparent">
                      <div className="d-flex justify-content-between align-items-start">
                        <div>
                          <h6 className="mb-1 fw-bold">{review.studentName} <span className="text-muted fw-normal">on {review.courseTitle}</span></h6>
                          <p className="mb-1 text-muted small">{review.comment}</p>
                        </div>
                        <div className="text-end">
                          <div className="text-warning mb-1">
                            {[...Array(5)].map((_, i) => <Star key={i} size={12} fill={i < review.rating ? "currentColor" : "none"} />)}
                          </div>
                          {review.sentimentLabel && (
                            <span className={`badge ${review.sentimentLabel === 'Positive' ? 'bg-success' : review.sentimentLabel === 'Negative' ? 'bg-danger' : review.sentimentLabel === 'Neutral' ? 'bg-warning' : 'bg-secondary'}`}>
                              {review.sentimentLabel}
                              {review.sentimentScore !== null && review.sentimentScore !== undefined && ` (${(review.sentimentScore * 100).toFixed(0)}%)`}
                            </span>
                          )}
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default InstructorDashboard;
