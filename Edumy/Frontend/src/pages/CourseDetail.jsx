import React, { useState, useEffect, useContext, useMemo } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import {
  PlayCircle,
  Star,
  Users,
  CheckCircle,
  Clock,
  Globe,
  ShieldCheck,
  Heart,
  X,
  ChevronDown,
  ChevronUp,
  ChevronRight,
  ShoppingCart,
  ArrowRight,
  Sparkles,
  Monitor,
  FileText,
  Award,
  BookOpen,
  Calendar,
  AlertCircle,
  Eye,
  Check,
  MessageSquare,
  Flame
} from 'lucide-react';
import SentimentBadge from '../components/SentimentBadge';
import CourseBundleCard from '../components/CourseBundleCard';
import CourseCard from '../components/CourseCard';
import './CourseDetail.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function CourseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const { addToCart } = useContext(CartContext);

  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [isEnrolled, setIsEnrolled] = useState(false);
  const [isInstructorOfCourse, setIsInstructorOfCourse] = useState(false);
  const [inWishlist, setInWishlist] = useState(false);

  // Review states
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [filterSentiment, setFilterSentiment] = useState('ALL');

  // Similar courses state (Feature 3)
  const [similarCourses, setSimilarCourses] = useState([]);

  // Accordion state
  const [expandedSections, setExpandedSections] = useState({});

  // Video Preview Modal State
  const [previewVideoUrl, setPreviewVideoUrl] = useState(null);
  const [previewVideoTitle, setPreviewVideoTitle] = useState('');

  // Fetch Course details
  useEffect(() => {
    const fetchCourseData = async () => {
      try {
        setLoading(true);
        const response = await api.get(`/courses/${id}`);
        setCourse(response.data);

        // Expand first section by default
        if (response.data?.sections?.length > 0) {
          setExpandedSections({ [response.data.sections[0].sectionId]: true });
        }
      } catch (error) {
        console.error('Error fetching course:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchCourseData();

    // Fetch similar courses (Feature 3)
    const fetchSimilarCourses = async () => {
      try {
        const res = await api.get(`/courses/${id}/similar?limit=4`);
        if (Array.isArray(res.data)) {
          setSimilarCourses(res.data);
        }
      } catch (err) {
        console.warn('Could not fetch similar courses:', err);
      }
    };
    fetchSimilarCourses();
  }, [id]);

  // Check enrollment & wishlist status
  useEffect(() => {
    if (!user) {
      setIsEnrolled(false);
      setIsInstructorOfCourse(false);
      return;
    }

    const checkStatus = async () => {
      try {
        const statusRes = await api.get(`/courses/${id}/enrollment-status`);
        setIsEnrolled(!!statusRes.data?.isEnrolled);
        setIsInstructorOfCourse(!!statusRes.data?.isInstructor);
      } catch (err) {
        console.warn('Could not check enrollment status:', err);
      }

      try {
        const wishRes = await api.get(`/wishlist/check/${id}`);
        setInWishlist(!!wishRes.data?.inWishlist);
      } catch (err) {
        // Ignored
      }
    };

    checkStatus();
  }, [id, user]);

  // Add to cart via CartContext
  const handleAddToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (user.role === 'Instructor' && !user.roles?.includes('Student')) {
      alert('Tài khoản Giảng viên không thể đăng ký mua khóa học.');
      return;
    }

    try {
      setEnrolling(true);
      await addToCart(id);
      alert('Đã thêm khóa học vào giỏ hàng thành công!');
    } catch (error) {
      alert(error.response?.data?.message || 'Khóa học đã có trong giỏ hàng hoặc đã đăng ký.');
    } finally {
      setEnrolling(false);
    }
  };

  // Buy now & redirect to Cart
  const handleBuyNow = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    try {
      setEnrolling(true);
      await addToCart(id);
      navigate('/cart');
    } catch (error) {
      navigate('/cart');
    } finally {
      setEnrolling(false);
    }
  };

  // Submit review
  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!reviewComment.trim()) return;

    try {
      setSubmittingReview(true);
      await api.post(`/courses/${id}/reviews`, {
        rating: reviewRating,
        comment: reviewComment
      });
      alert('Đánh giá của bạn đã được ghi nhận và phân tích cảm xúc thành công!');
      setReviewComment('');

      // Refresh course data
      const response = await api.get(`/courses/${id}`);
      setCourse(response.data);
    } catch (error) {
      alert(error.response?.data?.message || 'Gửi đánh giá thất bại. Vui lòng thử lại.');
    } finally {
      setSubmittingReview(false);
    }
  };

  // Toggle wishlist
  const toggleWishlist = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    try {
      if (inWishlist) {
        await api.delete(`/wishlist/remove/${id}`);
        setInWishlist(false);
      } else {
        await api.post(`/wishlist/add/${id}`);
        setInWishlist(true);
      }
    } catch (error) {
      alert(error.response?.data?.message || 'Cập nhật danh sách yêu thích thất bại.');
    }
  };

  // Accordion Toggles
  const toggleSection = (sectionId) => {
    setExpandedSections((prev) => ({
      ...prev,
      [sectionId]: !prev[sectionId]
    }));
  };

  const toggleAllSections = () => {
    if (!course?.sections) return;
    const allExpanded = course.sections.every((s) => expandedSections[s.sectionId]);
    const newState = {};
    course.sections.forEach((s) => {
      newState[s.sectionId] = !allExpanded;
    });
    setExpandedSections(newState);
  };

  // Open Preview Video Modal
  const handleOpenPreview = (videoUrl, title) => {
    if (!videoUrl) return;
    const resolvedUrl =
      videoUrl.startsWith('http://') || videoUrl.startsWith('https://')
        ? videoUrl
        : `${BACKEND_URL}${videoUrl}`;
    setPreviewVideoUrl(resolvedUrl);
    setPreviewVideoTitle(title || course?.title);
  };

  // Find first previewable lesson
  const findFirstPreviewLesson = () => {
    if (!course?.sections) return null;
    for (const sec of course.sections) {
      const previewLesson = sec.lessons?.find((l) => l.isPreview && l.videoUrl);
      if (previewLesson) return previewLesson;
    }
    return null;
  };

  const formatPrice = (price) => {
    if (!price && price !== 0) return '399.000 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  };

  const formatDuration = (seconds) => {
    if (!seconds) return '10:00';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  // Calculated Stats
  const { totalLectures, totalDurationHours } = useMemo(() => {
    if (!course?.sections) return { totalLectures: 0, totalDurationHours: '0 giờ' };
    let lectures = 0;
    let totalSecs = 0;
    course.sections.forEach((s) => {
      (s.lessons || []).forEach((l) => {
        lectures += 1;
        totalSecs += l.duration || 600;
      });
    });
    const hours = (totalSecs / 3600).toFixed(1);
    return { totalLectures: lectures, totalDurationHours: `${hours} giờ` };
  }, [course]);

  // Filtered reviews by AI sentiment (Feature 2)
  const filteredReviews = useMemo(() => {
    if (!course?.reviews) return [];
    if (filterSentiment === 'ALL') return course.reviews;
    return course.reviews.filter((r) => {
      const s = (r.sentimentLabel || '').toUpperCase();
      if (filterSentiment === 'POSITIVE') return s === 'POSITIVE' || s === 'TÍCH CỰC' || r.rating >= 4;
      if (filterSentiment === 'NEUTRAL') return s === 'NEUTRAL' || s === 'TRUNG TÍNH' || r.rating === 3;
      if (filterSentiment === 'NEGATIVE') return s === 'NEGATIVE' || s === 'TIÊU CỰC' || r.rating <= 2;
      return true;
    });
  }, [course, filterSentiment]);

  // Loading Skeleton State
  if (loading) {
    return (
      <div className="course-detail-page">
        <div className="course-hero-banner">
          <div className="container">
            <div className="hero-inner-container">
              <div className="hero-content-col">
                <div className="skeleton-box mb-3" style={{ width: '220px', height: '20px' }}></div>
                <div className="skeleton-box mb-3" style={{ width: '85%', height: '40px' }}></div>
                <div className="skeleton-box mb-4" style={{ width: '60%', height: '22px' }}></div>
                <div className="skeleton-box" style={{ width: '40%', height: '28px' }}></div>
              </div>
              <div className="d-none d-lg-block">
                <div className="skeleton-box rounded-3" style={{ width: '380px', height: '460px' }}></div>
              </div>
            </div>
          </div>
        </div>

        <div className="container mt-5">
          <div className="course-body-container">
            <div className="course-main-col">
              <div className="content-box">
                <div className="skeleton-box mb-3" style={{ width: '220px', height: '30px' }}></div>
                <div className="skeleton-box mb-2" style={{ width: '100%', height: '18px' }}></div>
                <div className="skeleton-box mb-2" style={{ width: '90%', height: '18px' }}></div>
                <div className="skeleton-box" style={{ width: '70%', height: '18px' }}></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Error/Empty State
  if (!course) {
    return (
      <div className="container py-5 text-center my-5">
        <div className="card border p-5 max-w-lg mx-auto rounded-3 bg-white shadow-sm">
          <AlertCircle size={48} className="text-danger mx-auto mb-3" />
          <h3 className="fw-bold mb-2">Khóa học không tồn tại</h3>
          <p className="text-muted mb-4">
            Khóa học bạn đang tìm kiếm không tồn tại hoặc đã bị gỡ khỏi hệ thống.
          </p>
          <Link to="/courses" className="btn btn-primary rounded-2 px-4 mx-auto">
            Xem các khóa học khác
          </Link>
        </div>
      </div>
    );
  }

  const firstPreview = findFirstPreviewLesson();
  const allExpanded = course.sections?.every((s) => expandedSections[s.sectionId]);

  return (
    <div className="course-detail-page">
      {/* --------------------------------------------------------------------
          1. HERO BANNER - SOLID DEEP NAVY (PRODUCTION CINEMATIC CONTRAST)
          -------------------------------------------------------------------- */}
      <section className="course-hero-banner">
        <div className="container">
          <div className="hero-inner-container">
            <div className="hero-content-col">
              {/* Breadcrumbs */}
              <nav aria-label="breadcrumb" className="hero-breadcrumbs">
                <Link to="/">Trang chủ</Link>
                <ChevronRight size={13} className="breadcrumb-separator" />
                <Link to="/courses">Khóa học</Link>
                <ChevronRight size={13} className="breadcrumb-separator" />
                <span>{course.category?.name || 'Lập trình & Công nghệ'}</span>
                <ChevronRight size={13} className="breadcrumb-separator" />
                <span className="breadcrumb-current">{course.title}</span>
              </nav>

              <h1 className="course-main-title">{course.title}</h1>
              <p className="course-main-subtitle">
                {course.shortDescription || course.description?.substring(0, 160) + '...'}
              </p>

              {/* Meta Badges */}
              <div className="course-meta-pills">
                <span className="badge-bestseller-solid">Bán chạy nhất</span>
                
                <div className="rating-stars-group">
                  <span className="rating-number">{course.averageRating || '4.8'}</span>
                  <div className="d-flex align-items-center">
                    {[...Array(5)].map((_, i) => (
                      <Star
                        key={i}
                        size={15}
                        fill={i < Math.round(course.averageRating || 5) ? '#fbbf24' : 'none'}
                        color="#fbbf24"
                      />
                    ))}
                  </div>
                  <span className="rating-count">({(course.reviews?.length || 24).toLocaleString()} đánh giá)</span>
                </div>

                <div className="meta-students-count">
                  <Users size={15} />
                  <span>{(course.enrollmentCount || 1280).toLocaleString()} học viên</span>
                </div>

                <span className="badge-updated-hero">
                  <Calendar size={13} /> Cập nhật tháng 10/2026
                </span>
              </div>

              {/* Instructor Headline & Meta Row */}
              <div className="instructor-meta-row">
                <img
                  src={
                    course.instructor?.avatarUrl
                      ? course.instructor.avatarUrl.startsWith('http')
                        ? course.instructor.avatarUrl
                        : `${BACKEND_URL}${course.instructor.avatarUrl}`
                      : 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&q=80'
                  }
                  alt={course.instructor?.fullName || 'Giảng viên'}
                  className="instructor-avatar-sm"
                />
                <div>
                  Giảng dạy bởi{' '}
                  <span className="instructor-name-link">
                    {course.instructor?.fullName || 'Trần Quang Huy'}
                  </span>
                  <span className="ms-2 instructor-headline-tag">
                    (Senior Principal Engineer)
                  </span>
                </div>

                <span className="text-secondary d-none d-md-inline">•</span>
                <div className="d-flex align-items-center gap-1">
                  <Globe size={14} />
                  <span>Tiếng Việt</span>
                </div>

                <span className="text-secondary d-none d-md-inline">•</span>
                <div className="d-flex align-items-center gap-1">
                  <FileText size={14} />
                  <span>Có phụ đề</span>
                </div>
              </div>
            </div>

            {/* Empty right placeholder in Hero grid to balance the sticky card */}
            <div className="d-none d-lg-block"></div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          2. MAIN CONTENT + STICKY SIDEBAR
          -------------------------------------------------------------------- */}
      <div className="container">
        <div className="course-body-container">
          {/* Main Column */}
          <div className="course-main-col">
            {/* What you'll learn */}
            <div className="content-box">
              <h3 className="content-box-title">Nội dung bạn sẽ nhận được</h3>
              <div className="learn-grid">
                <div className="learn-item">
                  <CheckCircle size={18} />
                  <span>Nắm vững tư duy kiến trúc và quy trình làm việc chuẩn doanh nghiệp thực tế.</span>
                </div>
                <div className="learn-item">
                  <CheckCircle size={18} />
                  <span>Thực hành trực tiếp qua dự án thực chiến từ số 0 đến triển khai production.</span>
                </div>
                <div className="learn-item">
                  <CheckCircle size={18} />
                  <span>Xây dựng portfolio và mã nguồn chuẩn chỉnh phục vụ phỏng vấn tuyển dụng.</span>
                </div>
                <div className="learn-item">
                  <CheckCircle size={18} />
                  <span>Nhận chứng chỉ số có mã xác thực độc quyền từ hệ thống Edumy.</span>
                </div>
              </div>
            </div>

            {/* Course Requirements */}
            <div className="content-box">
              <h3 className="content-box-title">Yêu cầu khóa học</h3>
              <ul className="requirements-list">
                <li>
                  <span className="requirements-bullet"></span>
                  <span>Máy tính (Windows, macOS hoặc Linux) có kết nối Internet ổn định.</span>
                </li>
                <li>
                  <span className="requirements-bullet"></span>
                  <span>Kiến thức cơ bản về lập trình là một lợi thế, nhưng người mới bắt đầu vẫn có thể học dễ dàng từ bài giảng nhập môn.</span>
                </li>
                <li>
                  <span className="requirements-bullet"></span>
                  <span>Tinh thần sẵn sàng học hỏi, kiên trì gõ code và làm bài tập sau mỗi chương.</span>
                </li>
              </ul>
            </div>

            {/* Course Curriculum Accordion */}
            <div className="content-box">
              <div className="curriculum-header-bar">
                <div>
                  <h3 className="content-box-title mb-1">Đề cương khóa học</h3>
                  <div className="curriculum-meta">
                    {course.sections?.length || 0} chương • {totalLectures} bài giảng • Tổng thời lượng {totalDurationHours}
                  </div>
                </div>
                <button onClick={toggleAllSections} className="btn-toggle-all">
                  {allExpanded ? 'Thu gọn tất cả' : 'Mở rộng tất cả'}
                </button>
              </div>

              <div className="curriculum-accordion">
                {course.sections?.length === 0 ? (
                  <div className="p-4 text-center text-muted">
                    Giáo trình khóa học đang được cập nhật.
                  </div>
                ) : (
                  course.sections?.map((section, sIdx) => {
                    const isExpanded = expandedSections[section.sectionId];
                    const lessonCount = section.lessons?.length || 0;

                    return (
                      <div key={section.sectionId} className="accordion-section-item">
                        <div
                          className="accordion-header"
                          onClick={() => toggleSection(section.sectionId)}
                        >
                          <div className="accordion-header-left">
                            {isExpanded ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                            <span>
                              Chương {sIdx + 1}: {section.title}
                            </span>
                          </div>
                          <div className="accordion-header-right">
                            {lessonCount} bài học
                          </div>
                        </div>

                        {isExpanded && (
                          <div className="accordion-body">
                            {section.lessons?.map((lesson, lIdx) => (
                              <div key={lesson.lessonId} className="lesson-row">
                                <div className="lesson-left">
                                  <PlayCircle size={16} />
                                  <span>
                                    Bài {lIdx + 1}: {lesson.title}
                                  </span>
                                </div>

                                <div className="d-flex align-items-center gap-3">
                                  {lesson.isPreview && lesson.videoUrl && (
                                    <button
                                      type="button"
                                      className="preview-badge-btn"
                                      onClick={() => handleOpenPreview(lesson.videoUrl, lesson.title)}
                                    >
                                      <Eye size={13} /> Học thử
                                    </button>
                                  )}
                                  <span className="lesson-duration">
                                    {formatDuration(lesson.duration)}
                                  </span>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            {/* Course Description */}
            <div className="content-box">
              <h3 className="content-box-title">Mô tả khóa học</h3>
              <div
                className="course-description-text"
                style={{ lineHeight: '1.75', fontSize: '0.975rem', whiteSpace: 'pre-line' }}
              >
                {course.description || 'Chưa có mô tả chi tiết cho khóa học này.'}
              </div>
            </div>

            {/* Instructor Profile */}
            <div className="content-box">
              <h3 className="content-box-title">Giảng viên hướng dẫn</h3>
              <div className="instructor-profile-layout">
                <img
                  src={
                    course.instructor?.avatarUrl
                      ? course.instructor.avatarUrl.startsWith('http')
                        ? course.instructor.avatarUrl
                        : `${BACKEND_URL}${course.instructor.avatarUrl}`
                      : 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&q=80'
                  }
                  alt={course.instructor?.fullName || 'Giảng viên'}
                  className="instructor-avatar-lg"
                />
                <div className="flex-grow-1">
                  <div className="d-flex align-items-center gap-2 mb-1">
                    <h4 className="fw-bold mb-0 text-dark fs-5">
                      {course.instructor?.fullName || 'Trần Quang Huy'}
                    </h4>
                    <span className="badge bg-primary bg-opacity-10 text-primary py-1 px-2 rounded-2 small fw-semibold">
                      Giảng viên uy tín
                    </span>
                  </div>
                  <p className="text-secondary small mb-2">
                    {course.instructor?.headline || 'Senior Principal Engineer • Tech Lead tại Tập đoàn Công nghệ hàng đầu'}
                  </p>

                  <div className="instructor-stats-badges">
                    <span className="instructor-stat-pill">
                      <Star size={14} fill="#d97706" /> 4.9 Xếp hạng giảng viên
                    </span>
                    <span className="instructor-stat-pill">
                      <Users size={14} /> 15.420 Học viên
                    </span>
                    <span className="instructor-stat-pill">
                      <PlayCircle size={14} /> 8 Khóa học thực chiến
                    </span>
                  </div>

                  <p className="mb-0 text-body small" style={{ lineHeight: '1.65' }}>
                    {course.instructor?.bio ||
                      'Hơn 10 năm kinh nghiệm phát triển các hệ thống chịu tải cao và kiến trúc microservices. Đam mê chia sẻ kiến thức thực chiến, giúp học viên tiếp cận công nghệ một cách bài bản, dễ hiểu và tự tin làm việc trong môi trường thực tế.'}
                  </p>
                </div>
              </div>
            </div>

            {/* Feature 4: Course Bundle Recommendation */}
            <CourseBundleCard courseId={id} />

            {/* Reviews with Human-Crafted Feedback and Verified Purchase */}
            <div className="content-box">
              <h3 className="content-box-title">Đánh giá từ học viên</h3>

              {/* Reviews Summary */}
              <div className="reviews-summary-card">
                <div className="rating-big-box">
                  <div className="rating-big-score">{course.averageRating || '4.8'}</div>
                  <div className="d-flex justify-content-center gap-1 mb-1">
                    {[...Array(5)].map((_, i) => (
                      <Star key={i} size={16} fill="#d97706" color="#d97706" />
                    ))}
                  </div>
                  <span className="text-muted small">Điểm đánh giá trung bình</span>
                </div>

                <div>
                  <div className="rating-bar-row">
                    <span style={{ width: '45px', fontWeight: '600' }}>5 sao</span>
                    <div className="rating-progress-track">
                      <div className="rating-progress-fill" style={{ width: '84%' }}></div>
                    </div>
                    <span className="text-muted small" style={{ width: '35px', textAlign: 'right' }}>84%</span>
                  </div>
                  <div className="rating-bar-row">
                    <span style={{ width: '45px', fontWeight: '600' }}>4 sao</span>
                    <div className="rating-progress-track">
                      <div className="rating-progress-fill" style={{ width: '12%' }}></div>
                    </div>
                    <span className="text-muted small" style={{ width: '35px', textAlign: 'right' }}>12%</span>
                  </div>
                  <div className="rating-bar-row">
                    <span style={{ width: '45px', fontWeight: '600' }}>3 sao</span>
                    <div className="rating-progress-track">
                      <div className="rating-progress-fill" style={{ width: '3%' }}></div>
                    </div>
                    <span className="text-muted small" style={{ width: '35px', textAlign: 'right' }}>3%</span>
                  </div>
                  <div className="rating-bar-row">
                    <span style={{ width: '45px', fontWeight: '600' }}>2 sao</span>
                    <div className="rating-progress-track">
                      <div className="rating-progress-fill" style={{ width: '1%' }}></div>
                    </div>
                    <span className="text-muted small" style={{ width: '35px', textAlign: 'right' }}>1%</span>
                  </div>
                  <div className="rating-bar-row">
                    <span style={{ width: '45px', fontWeight: '600' }}>1 sao</span>
                    <div className="rating-progress-track">
                      <div className="rating-progress-fill" style={{ width: '0%' }}></div>
                    </div>
                    <span className="text-muted small" style={{ width: '35px', textAlign: 'right' }}>0%</span>
                  </div>
                </div>
              </div>

              {/* Feature 2: Smart Sentiment Filter Tabs */}
              {course.reviews?.length > 0 && (
                <div className="d-flex flex-wrap align-items-center gap-2 mb-4 pb-3 border-bottom">
                  <span className="small text-muted fw-semibold me-1">Lọc theo cảm xúc:</span>
                  <button
                    type="button"
                    className={`btn btn-sm ${filterSentiment === 'ALL' ? 'btn-dark' : 'btn-outline-secondary bg-white text-dark'} rounded-pill px-3 py-1`}
                    style={{ fontSize: '0.8rem' }}
                    onClick={() => setFilterSentiment('ALL')}
                  >
                    Tất cả ({course.reviews.length})
                  </button>
                  <button
                    type="button"
                    className={`btn btn-sm ${filterSentiment === 'POSITIVE' ? 'btn-success text-white' : 'btn-outline-success bg-white'} rounded-pill px-3 py-1`}
                    style={{ fontSize: '0.8rem' }}
                    onClick={() => setFilterSentiment('POSITIVE')}
                  >
                    🌿 Tích cực ({course.reviews.filter(r => (r.sentimentLabel || '').toUpperCase() === 'POSITIVE' || (r.sentimentLabel || '').toUpperCase() === 'TÍCH CỰC' || r.rating >= 4).length})
                  </button>
                  <button
                    type="button"
                    className={`btn btn-sm ${filterSentiment === 'NEUTRAL' ? 'btn-warning text-dark' : 'btn-outline-warning bg-white text-dark'} rounded-pill px-3 py-1`}
                    style={{ fontSize: '0.8rem' }}
                    onClick={() => setFilterSentiment('NEUTRAL')}
                  >
                    💬 Trung tính ({course.reviews.filter(r => (r.sentimentLabel || '').toUpperCase() === 'NEUTRAL' || (r.sentimentLabel || '').toUpperCase() === 'TRUNG TÍNH' || r.rating === 3).length})
                  </button>
                  <button
                    type="button"
                    className={`btn btn-sm ${filterSentiment === 'NEGATIVE' ? 'btn-danger text-white' : 'btn-outline-danger bg-white'} rounded-pill px-3 py-1`}
                    style={{ fontSize: '0.8rem' }}
                    onClick={() => setFilterSentiment('NEGATIVE')}
                  >
                    ⚠️ Cần cải thiện ({course.reviews.filter(r => (r.sentimentLabel || '').toUpperCase() === 'NEGATIVE' || (r.sentimentLabel || '').toUpperCase() === 'TIÊU CỰC' || r.rating <= 2).length})
                  </button>
                </div>
              )}

              {/* Reviews List */}
              <div className="reviews-list">
                {filteredReviews.length === 0 ? (
                  <p className="text-muted text-center py-4">Chưa có đánh giá nào phù hợp với bộ lọc này.</p>
                ) : (
                  filteredReviews.map((rev, rIdx) => (
                    <div key={rev.reviewId || rIdx} className="review-item">
                      <div className="review-user-row">
                        <div className="review-user-info">
                          <div className="user-avatar-circle">
                            {(rev.user?.fullName || rev.userName || 'U')[0].toUpperCase()}
                          </div>
                          <div>
                            <div className="d-flex align-items-center gap-2">
                              <h5 className="mb-0 fw-bold fs-6">
                                {rev.user?.fullName || rev.userName || 'Học viên Edumy'}
                              </h5>
                              <span className="badge-verified-purchase">
                                <Check size={11} /> Đã mua khóa học
                              </span>
                            </div>
                            <div className="d-flex align-items-center gap-2 mt-1">
                              <div className="d-flex gap-1">
                                {[...Array(5)].map((_, i) => (
                                  <Star
                                    key={i}
                                    size={13}
                                    fill={i < rev.rating ? '#d97706' : 'none'}
                                    color="#d97706"
                                  />
                                ))}
                              </div>
                              <span className="text-muted small">
                                {rev.createdAt ? new Date(rev.createdAt).toLocaleDateString('vi-VN') : 'Tháng 10/2026'}
                              </span>
                            </div>
                          </div>
                        </div>

                        {/* Feature 2: Smart AI Sentiment Badge */}
                        <SentimentBadge
                          sentiment={rev.sentimentLabel}
                          score={rev.sentimentScore}
                        />
                      </div>

                      <p className="mb-0 text-body small mt-2" style={{ lineHeight: '1.6' }}>
                        {rev.comment}
                      </p>
                    </div>
                  ))
                )}
              </div>

              {/* Add Review Form (If enrolled) */}
              {isEnrolled && (
                <div className="mt-4 pt-4 border-top">
                  <h5 className="fw-bold mb-3">Gửi đánh giá của bạn</h5>
                  <form onSubmit={handleSubmitReview}>
                    <div className="mb-3 d-flex align-items-center gap-2">
                      <span className="fw-semibold small">Mức độ hài lòng:</span>
                      {[1, 2, 3, 4, 5].map((star) => (
                        <Star
                          key={star}
                          size={22}
                          className="cursor-pointer"
                          style={{ cursor: 'pointer' }}
                          fill={star <= reviewRating ? '#d97706' : 'none'}
                          color="#d97706"
                          onClick={() => setReviewRating(star)}
                        />
                      ))}
                    </div>

                    <div className="mb-3">
                      <textarea
                        className="form-control"
                        rows="3"
                        placeholder="Chia sẻ cảm nhận chi tiết về nội dung bài giảng, chất lượng hỗ trợ..."
                        value={reviewComment}
                        onChange={(e) => setReviewComment(e.target.value)}
                        required
                      ></textarea>
                      <small className="text-muted d-block mt-1">
                        Đánh giá của bạn sẽ giúp cộng đồng học viên có cái nhìn chân thực nhất về khóa học.
                      </small>
                    </div>

                    <button
                      type="submit"
                      disabled={submittingReview}
                      className="btn-solid-primary"
                    >
                      {submittingReview ? 'Đang gửi...' : 'Đăng đánh giá'}
                    </button>
                  </form>
                </div>
              )}
            </div>
          </div>

          {/* Sticky Sidebar Column */}
          <div className="sticky-sidebar-col">
            <div className="sticky-checkout-card">
              {/* Thumbnail with Video Preview Play button */}
              <div
                className="checkout-thumbnail-box"
                onClick={() =>
                  handleOpenPreview(
                    firstPreview?.videoUrl || 'https://www.w3schools.com/html/mov_bbb.mp4',
                    firstPreview?.title || course.title
                  )
                }
              >
                <img
                  src={
                    course.thumbnailUrl
                      ? course.thumbnailUrl.startsWith('http')
                        ? course.thumbnailUrl
                        : `${BACKEND_URL}${course.thumbnailUrl}`
                      : 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&q=80'
                  }
                  alt={course.title}
                  className="checkout-thumbnail-img"
                />
                <div className="video-play-overlay">
                  <div className="play-circle-icon">
                    <PlayCircle size={28} />
                  </div>
                  <span className="play-label">Xem video học thử</span>
                </div>
              </div>

              {/* Pricing & CTAs */}
              <div className="checkout-card-content">
                <div className="price-row">
                  <span className="price-current">{formatPrice(course.price)}</span>
                  {course.originalPrice && course.originalPrice > course.price && (
                    <span className="price-original">{formatPrice(course.originalPrice)}</span>
                  )}
                  <span className="price-discount-tag">Tiết kiệm 45%</span>
                </div>

                <div className="deal-timer-notice">
                  <Clock size={13} />
                  <span>Ưu đãi áp dụng có hạn trong tháng này</span>
                </div>

                <div className="cta-buttons-stack">
                  {isEnrolled ? (
                    <Link
                      to={`/my-courses/${course.courseId}/learn`}
                      className="btn-primary-cta"
                    >
                      <PlayCircle size={18} /> Tiếp tục học bài
                    </Link>
                  ) : isInstructorOfCourse ? (
                    <Link
                      to={`/instructor/courses/${course.courseId}/curriculum`}
                      className="btn-primary-cta"
                    >
                      Quản lý bài giảng khóa học
                    </Link>
                  ) : (
                    <>
                      <button
                        onClick={handleAddToCart}
                        disabled={enrolling}
                        className="btn-primary-cta"
                      >
                        <ShoppingCart size={17} /> Thêm vào giỏ hàng
                      </button>
                      <button
                        onClick={handleBuyNow}
                        disabled={enrolling}
                        className="btn-secondary-cta"
                      >
                        Mua ngay
                      </button>
                    </>
                  )}
                </div>

                <div className="guarantee-note">
                  <ShieldCheck size={16} /> Cam kết hoàn tiền 100% trong 30 ngày
                </div>

                {/* Course Includes */}
                <div className="course-includes-box">
                  <h4>Khóa học này bao gồm:</h4>
                  <ul className="includes-list">
                    <li>
                      <Clock size={15} /> {totalDurationHours} video theo yêu cầu chất lượng cao
                    </li>
                    <li>
                      <BookOpen size={15} /> {totalLectures} bài giảng thực hành chi tiết
                    </li>
                    <li>
                      <FileText size={15} /> Tài liệu mã nguồn và bài tập đính kèm
                    </li>
                    <li>
                      <Monitor size={15} /> Truy cập trọn đời trên máy tính & điện thoại
                    </li>
                    <li>
                      <Award size={15} /> Chứng chỉ hoàn thành khóa học có mã xác thực
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* --------------------------------------------------------------------
          3. SIMILAR COURSES RECOMMENDATION (FEATURE 3)
          -------------------------------------------------------------------- */}
      {similarCourses.length > 0 && (
        <section className="similar-courses-section py-5 mt-5 border-top bg-white">
          <div className="container">
            <div className="d-flex justify-content-between align-items-end mb-4">
              <div>
                <div className="d-flex align-items-center gap-2 mb-1">
                  <span className="badge bg-warning bg-opacity-25 text-warning fw-bold px-2 py-1 rounded-2" style={{ color: '#b45309' }}>
                    <Flame size={13} className="me-1" /> Gợi ý thông minh
                  </span>
                  <span className="text-secondary small fw-semibold">Đánh giá trên 90% tích cực</span>
                </div>
                <h3 className="fw-bold mb-1 text-dark fs-4">Học viên thích khóa học này cũng học thêm</h3>
                <p className="text-muted small mb-0">Các khóa học cùng lộ trình đào tạo được đề xuất dựa trên mô hình gợi ý AI.</p>
              </div>
            </div>

            <div className="row g-4">
              {similarCourses.map((c) => (
                <div className="col-12 col-md-6 col-lg-3" key={c.courseId}>
                  <div className="position-relative h-100">
                    <CourseCard course={c} />
                    {c.positiveRatingPercent && (
                      <div className="position-absolute top-0 end-0 m-2" style={{ zIndex: 3 }}>
                        <span className="badge bg-dark bg-opacity-80 text-white px-2 py-1 small rounded-1 shadow-sm">
                          🔥 {c.positiveRatingPercent}% hài lòng
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      {/* --------------------------------------------------------------------
          4. VIDEO PREVIEW MODAL
          -------------------------------------------------------------------- */}
      {previewVideoUrl && (
        <div className="modal-overlay" onClick={() => setPreviewVideoUrl(null)}>
          <div className="modal-content-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header-bar">
              <h5 className="d-flex align-items-center gap-2">
                <PlayCircle size={18} className="text-primary" />
                {previewVideoTitle || 'Xem thử bài giảng'}
              </h5>
              <button
                type="button"
                className="btn-close-modal"
                onClick={() => setPreviewVideoUrl(null)}
              >
                <X size={20} />
              </button>
            </div>
            <div className="video-frame-wrap">
              {previewVideoUrl.includes('youtube.com') || previewVideoUrl.includes('youtu.be') ? (
                <iframe
                  src={previewVideoUrl.replace('watch?v=', 'embed/')}
                  title="Course Video Preview"
                  allowFullScreen
                ></iframe>
              ) : (
                <video src={previewVideoUrl} controls autoPlay className="w-100 h-100" />
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default CourseDetail;
