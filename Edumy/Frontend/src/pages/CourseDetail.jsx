import { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { 
  PlayCircle, 
  Star, 
  Users, 
  CheckCircle, 
  Info, 
  Monitor, 
  FileText, 
  Award, 
  Clock, 
  Globe, 
  ShieldCheck, 
  Heart, 
  X, 
  ChevronDown, 
  ChevronUp, 
  Flame,
  ShoppingCart,
  ArrowRight
} from 'lucide-react';
import './CourseDetail.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function CourseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

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

  // Accordion state
  const [expandedSections, setExpandedSections] = useState({});

  // Video Preview Modal State
  const [previewVideoUrl, setPreviewVideoUrl] = useState(null);
  const [previewVideoTitle, setPreviewVideoTitle] = useState('');

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
        // Fast endpoint in Spring Boot Backend: /api/Courses/{id}/enrollment-status
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
        // Ignored if wishlist check fails
      }
    };

    checkStatus();
  }, [id, user]);

  const handleAddToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (user.role !== 'Student' && user.role !== 'Admin') {
      alert('Tài khoản Giảng viên không thể đăng ký mua khóa học.');
      return;
    }

    try {
      setEnrolling(true);
      await api.post(`/cart/add/${id}`);
      alert('Đã thêm khóa học vào giỏ hàng thành công!');
    } catch (error) {
      alert(error.response?.data?.message || 'Không thể thêm vào giỏ hàng.');
    } finally {
      setEnrolling(false);
    }
  };

  const handleBuyNow = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    try {
      setEnrolling(true);
      await api.post(`/cart/add/${id}`);
      navigate('/cart');
    } catch (error) {
      if (error.response?.data?.message === "Course is already in your cart.") {
        navigate('/cart');
      } else {
        alert(error.response?.data?.message || 'Không thể tiến hành mua ngay.');
      }
    } finally {
      setEnrolling(false);
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!reviewComment.trim()) return;

    try {
      setSubmittingReview(true);
      await api.post(`/courses/${id}/reviews`, { 
        rating: reviewRating, 
        comment: reviewComment 
      });
      alert('Đánh giá của bạn đã được ghi nhận và phân tích cảm xúc bởi AI!');
      setReviewComment('');
      
      // Refresh course data to show updated review & rating
      const response = await api.get(`/courses/${id}`);
      setCourse(response.data);
    } catch (error) {
      alert(error.response?.data?.message || 'Gửi đánh giá thất bại. Vui lòng thử lại.');
    } finally {
      setSubmittingReview(false);
    }
  };

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

  const toggleSection = (sectionId) => {
    setExpandedSections(prev => ({
      ...prev,
      [sectionId]: !prev[sectionId]
    }));
  };

  const toggleAllSections = () => {
    if (!course?.sections) return;
    const allExpanded = course.sections.every(s => expandedSections[s.sectionId]);
    const newState = {};
    course.sections.forEach(s => {
      newState[s.sectionId] = !allExpanded;
    });
    setExpandedSections(newState);
  };

  // Open Preview Video
  const handleOpenPreview = (videoUrl, title) => {
    if (!videoUrl) return;
    const resolvedUrl = videoUrl.startsWith('http://') || videoUrl.startsWith('https://') 
      ? videoUrl 
      : `${BACKEND_URL}${videoUrl}`;
    setPreviewVideoUrl(resolvedUrl);
    setPreviewVideoTitle(title || course?.title);
  };

  // Find first previewable lesson for the main hero card preview button
  const findFirstPreviewLesson = () => {
    if (!course?.sections) return null;
    for (const sec of course.sections) {
      const previewLesson = sec.lessons?.find(l => l.isPreview && l.videoUrl);
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

  if (loading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', minHeight: '60vh', color: '#94a3b8' }}>
        <div style={{ width: 44, height: 44, border: '4px solid #1f2937', borderTopColor: '#6366f1', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }}></div>
        <p style={{ marginTop: '1rem' }}>Đang tải thông tin khóa học...</p>
      </div>
    );
  }

  if (!course) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 20px', color: '#cbd5e1' }}>
        <h2>Không tìm thấy khóa học này.</h2>
        <Link to="/courses" style={{ color: '#818cf8', textDecoration: 'underline', marginTop: '1rem', display: 'inline-block' }}>
          Xem danh sách khóa học
        </Link>
      </div>
    );
  }

  const totalLectures = course.sections?.reduce((acc, sec) => acc + (sec.lessons?.length || 0), 0) || 0;
  const firstPreview = findFirstPreviewLesson();

  return (
    <div className="course-detail-page">
      {/* ---------------- Hero Section (Dark Slate) ---------------- */}
      <section className="course-hero-banner">
        <div className="hero-inner-container">
          <div className="hero-content-col">
            <nav className="hero-breadcrumbs">
              <Link to="/courses">Khóa học</Link>
              <span>/</span>
              <span>{course.category?.name || 'Lập trình & Phát triển Phần mềm'}</span>
            </nav>

            <h1 className="course-main-title">{course.title}</h1>
            <p className="course-main-subtitle">{course.description}</p>

            {course.courseTags && course.courseTags.length > 0 && (
              <div className="course-tags-row">
                {course.courseTags.map(ct => (
                  <span key={ct.tagId} className="tag-badge">
                    #{ct.tag?.name}
                  </span>
                ))}
              </div>
            )}

            <div className="course-metrics-bar">
              <span className="bestseller-badge">
                <Flame size={12} style={{ display: 'inline', marginRight: 2 }} /> Bestseller
              </span>
              <div className="rating-group">
                <span className="rating-number">
                  {course.averageRating > 0 ? course.averageRating.toFixed(1) : '4.8'}
                </span>
                <div className="stars-wrapper">
                  {[1, 2, 3, 4, 5].map(s => (
                    <Star key={s} size={15} fill="#fbbf24" color="#fbbf24" />
                  ))}
                </div>
              </div>
              <span className="rating-count-link">
                ({course.reviews?.length || 245} đánh giá)
              </span>
              <span className="students-counter">
                <Users size={15} /> {course.studentCount || 1280} học viên đã tham gia
              </span>
            </div>

            <div className="hero-creator-row">
              <div>
                Được hướng dẫn bởi <a href="#" className="creator-link">{course.instructor?.fullName || 'Chuyên gia Công nghệ'}</a>
              </div>
              <div className="hero-meta-item">
                <Info size={14} /> Cập nhật lần cuối: 10/2026
              </div>
              <div className="hero-meta-item">
                <Globe size={14} /> Tiếng Việt & Phụ đề Tiếng Anh
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ---------------- Main Content Layout ---------------- */}
      <div className="detail-main-container">
        <div className="detail-left-col">
          {/* What you'll learn card */}
          <div className="learning-objectives-card">
            <h3 className="card-section-title">
              <Award size={20} color="#6366f1" /> Bạn sẽ học được gì trong khóa học này?
            </h3>
            <div className="objectives-grid">
              <div className="objective-item">
                <CheckCircle size={18} className="objective-icon" />
                <span>Nắm vững kiến thức từ căn bản đến chuyên sâu, có tính ứng dụng công nghiệp thực tế.</span>
              </div>
              <div className="objective-item">
                <CheckCircle size={18} className="objective-icon" />
                <span>Xây dựng các dự án thực chiến cho Portfolio nghề nghiệp cá nhân.</span>
              </div>
              <div className="objective-item">
                <CheckCircle size={18} className="objective-icon" />
                <span>Hiểu sâu các best practices, kiến trúc Clean Architecture & tối ưu hiệu năng.</span>
              </div>
              <div className="objective-item">
                <CheckCircle size={18} className="objective-icon" />
                <span>Nhận chứng chỉ Edumy Certified có mã số định danh sau khi hoàn thành.</span>
              </div>
            </div>
          </div>

          {/* Curriculum Section */}
          <div className="curriculum-container">
            <h3 className="card-section-title">Nội dung chi tiết khóa học</h3>
            <div className="curriculum-stats-header">
              <span className="curriculum-meta-text">
                {course.sections?.length || 0} chương • {totalLectures} bài giảng
              </span>
              <button className="btn-toggle-all-sections" onClick={toggleAllSections}>
                Mở rộng / Thu gọn tất cả
              </button>
            </div>

            <div className="accordion-group">
              {course.sections && course.sections.length > 0 ? (
                course.sections.map((section, sIdx) => {
                  const isExpanded = !!expandedSections[section.sectionId];

                  return (
                    <div className="accordion-section-item" key={section.sectionId}>
                      <div className="section-head-bar" onClick={() => toggleSection(section.sectionId)}>
                        <div className="section-head-left">
                          {isExpanded ? <ChevronUp size={16} color="#94a3b8" /> : <ChevronDown size={16} color="#94a3b8" />}
                          <span className="section-title-text">
                            Chương {sIdx + 1}: {section.title}
                          </span>
                        </div>
                        <span className="section-lessons-count">
                          {section.lessons?.length || 0} bài giảng
                        </span>
                      </div>

                      {isExpanded && (
                        <div className="section-body-list">
                          {section.lessons && section.lessons.map((lesson, lIdx) => (
                            <div className="lesson-row-item" key={lesson.lessonId}>
                              <div className="lesson-row-left">
                                <PlayCircle size={16} color="#818cf8" />
                                <span>{lIdx + 1}. {lesson.title}</span>
                              </div>
                              <div className="lesson-row-right">
                                {lesson.isPreview && lesson.videoUrl && (
                                  <button 
                                    className="btn-preview-lesson"
                                    onClick={() => handleOpenPreview(lesson.videoUrl, lesson.title)}
                                  >
                                    Học thử
                                  </button>
                                )}
                                <span className="lesson-duration-text">
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
              ) : (
                <div style={{ padding: '2rem', textAlign: 'center', color: '#64748b' }}>
                  Giảng viên đang biên soạn nội dung chi tiết.
                </div>
              )}
            </div>
          </div>

          {/* Reviews & Student Feedback Section */}
          <div className="reviews-container">
            <h3 className="card-section-title">Đánh giá từ học viên & Phân tích Cảm xúc AI</h3>
            
            <div className="reviews-summary-card">
              <div style={{ textAlign: 'center' }}>
                <div className="big-rating-score">
                  {course.averageRating > 0 ? course.averageRating.toFixed(1) : '4.8'}
                </div>
                <div className="stars-wrapper" style={{ justifyContent: 'center', margin: '0.4rem 0' }}>
                  {[1, 2, 3, 4, 5].map(s => (
                    <Star key={s} size={16} fill="#fbbf24" color="#fbbf24" />
                  ))}
                </div>
                <span style={{ fontSize: '0.85rem', color: '#94a3b8' }}>Xếp hạng khóa học</span>
              </div>
              <div style={{ flex: 1, borderLeft: '1px solid #1f2937', paddingLeft: '2rem' }}>
                <p style={{ margin: 0, fontSize: '0.925rem', color: '#cbd5e1' }}>
                  Mọi đánh giá từ học viên đều được kiểm duyệt và phân tích cảm xúc thời gian thực bởi mô hình <strong>AI BiLSTM Sentiment Service</strong> nhằm duy trì chất lượng bài giảng cao nhất.
                </p>
              </div>
            </div>

            <div className="reviews-list">
              {course.reviews && course.reviews.length > 0 ? (
                course.reviews.map(review => (
                  <div key={review.reviewId} className="review-card-item">
                    <div className="review-author-row">
                      <div className="review-author-info">
                        <div className="avatar-circle">
                          {(review.user?.fullName || 'H')[0].toUpperCase()}
                        </div>
                        <div>
                          <span className="author-name-text">{review.user?.fullName || 'Học viên Edumy'}</span>
                          <div className="stars-wrapper" style={{ marginTop: '2px' }}>
                            {[1, 2, 3, 4, 5].map(s => (
                              <Star 
                                key={s} 
                                size={12} 
                                fill={s <= review.rating ? "#fbbf24" : "#374151"} 
                                color={s <= review.rating ? "#fbbf24" : "#374151"} 
                              />
                            ))}
                          </div>
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        {review.sentimentLabel && (
                          <span className={`sentiment-pill ${
                            review.sentimentLabel === 'Positive' ? 'sentiment-positive' :
                            review.sentimentLabel === 'Negative' ? 'sentiment-negative' : 'sentiment-neutral'
                          }`}>
                            {review.sentimentLabel === 'Positive' ? 'Tích cực' :
                             review.sentimentLabel === 'Negative' ? 'Cần cải thiện' : 'Trung lập'}
                            {review.sentimentScore !== null && review.sentimentScore !== undefined && (
                              ` ${(review.sentimentScore * 100).toFixed(0)}%`
                            )}
                          </span>
                        )}
                        <span className="review-date-text">
                          {new Date(review.createdAt).toLocaleDateString('vi-VN')}
                        </span>
                      </div>
                    </div>

                    <p className="review-comment-body">{review.comment}</p>
                  </div>
                ))
              ) : (
                <div style={{ padding: '2rem', textAlign: 'center', color: '#64748b', background: '#111827', borderRadius: '10px' }}>
                  Chưa có đánh giá nào cho khóa học này. Hãy là người đầu tiên trải nghiệm!
                </div>
              )}
            </div>

            {/* Leave a review form for enrolled students */}
            {isEnrolled && (
              <div className="review-form-card">
                <h4>Viết đánh giá của bạn</h4>
                <form onSubmit={handleSubmitReview}>
                  <div style={{ marginBottom: '1rem' }}>
                    <label style={{ display: 'block', fontSize: '0.875rem', color: '#94a3b8', marginBottom: '0.4rem' }}>
                      Mức độ hài lòng:
                    </label>
                    <select 
                      value={reviewRating} 
                      onChange={e => setReviewRating(Number(e.target.value))}
                      style={{
                        background: '#0f172a',
                        color: '#f8fafc',
                        border: '1px solid #374151',
                        borderRadius: '8px',
                        padding: '0.5rem 0.75rem',
                        outline: 'none',
                        cursor: 'pointer'
                      }}
                    >
                      <option value="5">5 Sao - Cực kỳ hài lòng</option>
                      <option value="4">4 Sao - Rất tốt</option>
                      <option value="3">3 Sao - Bình thường</option>
                      <option value="2">2 Sao - Chưa như mong đợi</option>
                      <option value="1">1 Sao - Kém</option>
                    </select>
                  </div>

                  <div style={{ marginBottom: '1rem' }}>
                    <label style={{ display: 'block', fontSize: '0.875rem', color: '#94a3b8', marginBottom: '0.4rem' }}>
                      Nhận xét chi tiết:
                    </label>
                    <textarea 
                      required 
                      rows="3" 
                      value={reviewComment}
                      onChange={e => setReviewComment(e.target.value)}
                      placeholder="Chia sẻ trải nghiệm học tập của bạn về giảng viên và nội dung..."
                      style={{
                        width: '100%',
                        background: '#0f172a',
                        color: '#f8fafc',
                        border: '1px solid #374151',
                        borderRadius: '8px',
                        padding: '0.75rem',
                        outline: 'none',
                        fontFamily: 'inherit',
                        resize: 'vertical'
                      }}
                    />
                  </div>

                  <button 
                    type="submit" 
                    className="btn-enroll-primary" 
                    style={{ width: 'auto', padding: '0.6rem 1.5rem', marginBottom: 0 }}
                    disabled={submittingReview}
                  >
                    {submittingReview ? 'Đang gửi...' : 'Gửi đánh giá & Phân tích AI'}
                  </button>
                </form>
              </div>
            )}
          </div>
        </div>

        {/* ---------------- Right Sticky Enrollment Card ---------------- */}
        <aside className="detail-right-col">
          <div className="sticky-enroll-card">
            {/* Preview Thumbnail */}
            <div 
              className="card-preview-thumb" 
              onClick={() => {
                if (firstPreview) {
                  handleOpenPreview(firstPreview.videoUrl, firstPreview.title);
                } else if (course.trailerVideoUrl) {
                  handleOpenPreview(course.trailerVideoUrl, 'Giới thiệu khóa học');
                }
              }}
            >
              <img 
                src={course.thumbnailUrl 
                  ? (course.thumbnailUrl.startsWith('http') ? course.thumbnailUrl : `${BACKEND_URL}${course.thumbnailUrl}`)
                  : "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&q=80"} 
                alt={course.title} 
              />
              <div className="card-play-overlay">
                <div className="play-pulse-btn">
                  <PlayCircle size={32} />
                </div>
                <span className="preview-tag-caption">Xem video học thử</span>
              </div>
            </div>

            <div className="card-body-content">
              <div className="price-box-row">
                <span className="current-price">{formatPrice(course.price)}</span>
                <span className="original-price">{formatPrice(course.price ? course.price * 2.5 : 1200000)}</span>
                <span className="discount-tag">-60% OFF</span>
              </div>

              <div className="urgency-badge">
                <Clock size={14} /> Ưu đãi giảm giá sắp kết thúc trong 12 giờ!
              </div>

              {isEnrolled ? (
                <div>
                  <div style={{ textAlign: 'center', padding: '0.5rem', background: 'rgba(16, 185, 129, 0.15)', color: '#34d399', borderRadius: '8px', fontWeight: 600, fontSize: '0.9rem', marginBottom: '1rem' }}>
                    <ShieldCheck size={16} style={{ display: 'inline', marginRight: 4, verticalAlign: -2 }} />
                    Bạn đã sở hữu khóa học này
                  </div>
                  <Link to={`/my-courses/${id}/learn`} className="btn-go-to-course">
                    <PlayCircle size={18} /> Vào phòng học ngay <ArrowRight size={18} />
                  </Link>
                </div>
              ) : isInstructorOfCourse ? (
                <div style={{ textAlign: 'center', padding: '1rem', background: '#1f2937', color: '#a5b4fc', borderRadius: '8px', marginBottom: '1rem' }}>
                  Bạn là Giảng viên quản trị khóa học này.
                </div>
              ) : (
                <>
                  <button 
                    className="btn-enroll-primary" 
                    onClick={handleAddToCart}
                    disabled={enrolling}
                  >
                    <ShoppingCart size={18} /> {enrolling ? 'Đang xử lý...' : 'Thêm vào giỏ hàng'}
                  </button>
                  <button 
                    className="btn-enroll-secondary" 
                    onClick={handleBuyNow}
                    disabled={enrolling}
                  >
                    Mua ngay
                  </button>
                </>
              )}

              {!isEnrolled && (
                <button 
                  className={`wishlist-toggle-btn ${inWishlist ? 'active' : ''}`}
                  onClick={toggleWishlist}
                >
                  <Heart size={16} fill={inWishlist ? "#f43f5e" : "none"} />
                  {inWishlist ? 'Đã lưu trong Wishlist' : 'Thêm vào Wishlist yêu thích'}
                </button>
              )}

              <div className="guarantee-note">
                Cam kết hoàn tiền trong 30 ngày nếu không hài lòng
              </div>

              <div className="features-included-list">
                <h5>Khóa học bao gồm:</h5>
                <ul>
                  <li>
                    <Monitor size={15} color="#818cf8" />
                    <span>Hơn {Math.round(totalLectures * 0.25)} giờ video theo yêu cầu</span>
                  </li>
                  <li>
                    <FileText size={15} color="#818cf8" />
                    <span>Tài liệu & mã nguồn dự án đầy đủ</span>
                  </li>
                  <li>
                    <Award size={15} color="#818cf8" />
                    <span>Chứng chỉ hoàn thành có mã tra cứu</span>
                  </li>
                  <li>
                    <ShieldCheck size={15} color="#818cf8" />
                    <span>Quyền truy cập học tập trọn đời</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </aside>
      </div>

      {/* ---------------- Video Preview Modal ---------------- */}
      {previewVideoUrl && (
        <div className="preview-modal-backdrop" onClick={() => setPreviewVideoUrl(null)}>
          <div className="preview-modal-box" onClick={e => e.stopPropagation()}>
            <div className="preview-modal-header">
              <h4>Học thử: {previewVideoTitle}</h4>
              <button className="btn-close-modal" onClick={() => setPreviewVideoUrl(null)}>
                <X size={20} />
              </button>
            </div>
            <div className="preview-modal-body">
              <video 
                src={previewVideoUrl} 
                controls 
                autoPlay 
                controlsList="nodownload"
              >
                Trình duyệt không hỗ trợ video HTML5.
              </video>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default CourseDetail;
