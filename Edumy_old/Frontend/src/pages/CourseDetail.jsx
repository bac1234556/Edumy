import { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { PlayCircle, Star, Users, CheckCircle, Info, Monitor, FileText, Award } from 'lucide-react';
import './CourseDetail.css';

function CourseDetail() {
  const { id } = useParams();
  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [isEnrolled, setIsEnrolled] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [inWishlist, setInWishlist] = useState(false);
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCourse = async () => {
      try {
        const response = await api.get(`/courses/${id}`);
        setCourse(response.data);
      } catch (error) {
        console.error('Error fetching course:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchCourse();
  }, [id]);

  useEffect(() => {
    if (!user || user.role !== 'Student') return;
    const checkEnrollment = async () => {
      try {
        const response = await api.get('/orders/my-orders');
        const orders = response.data;
        const enrolled = orders.some(order =>
          order.status === 'Completed' &&
          order.orderItems?.some(item => item.courseId === parseInt(id))
        );
        setIsEnrolled(enrolled);
      } catch (err) {
      }
    };
    
    const checkWishlist = async () => {
      try {
        const response = await api.get(`/wishlist/check/${id}`);
        setInWishlist(response.data.inWishlist);
      } catch(err) {
      }
    };
    
    checkEnrollment();
    checkWishlist();
  }, [id, user]);

  const handleAddToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (user.role !== 'Student') {
      alert('Only students can enroll in courses.');
      return;
    }

    try {
      setEnrolling(true);
      await api.post(`/cart/add/${id}`);
      alert('Course added to cart!');
      // Optionally emit event or context to update navbar cart count
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to add to cart.');
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
        alert(error.response?.data?.message || 'Failed to buy now.');
      }
    } finally {
      setEnrolling(false);
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    try {
      setSubmittingReview(true);
      await api.post(`/courses/${id}/reviews`, { rating: reviewRating, comment: reviewComment });
      alert("Review submitted successfully!");
      setReviewComment('');
      // Refresh course data to show new review
      const response = await api.get(`/courses/${id}`);
      setCourse(response.data);
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to submit review.');
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
      alert(error.response?.data?.message || 'Failed to update wishlist');
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <h2>Loading...</h2>
      </div>
    );
  }

  if (!course) return (
    <div style={{ textAlign: 'center', padding: '100px 0' }}>
      <h2>Course not found.</h2>
    </div>
  );

  const isInstructor = user && (user.role === 'Instructor' || user.role === 'Admin');

  return (
    <div className="course-detail-page">
      {/* Dark Header */}
      <div className="course-header-dark">
        <div className="container header-container">
          <div className="header-content">
            <div className="breadcrumbs">
              {course.category?.name || 'Development'} &gt; Web Development
            </div>
            <h1 className="course-title-main">{course.title}</h1>
            <p className="course-subtitle">{course.description}</p>
            
            {course.courseTags && course.courseTags.length > 0 && (
              <div className="course-tags mb-3 d-flex gap-2">
                {course.courseTags.map(ct => (
                  <span key={ct.tagId} className="badge bg-light text-dark rounded-pill">
                    #{ct.tag?.name}
                  </span>
                ))}
              </div>
            )}
            
            <div className="course-meta">
              <span className="rating-badge">Bestseller</span>
              <span className="rating-score">{course.averageRating > 0 ? course.averageRating : '4.7'}</span>
              <div className="stars">
                {[1,2,3,4,5].map(s => <Star key={s} size={14} fill="#eb8a2f" color="#eb8a2f" />)}
              </div>
              <a href="#" className="ratings-link">(245,102 ratings)</a>
              <span className="students-count">{course.studentCount || '890,230'} students</span>
            </div>
            
            <div className="instructor-meta">
              Created by <a href="#">{course.instructor?.fullName || 'Dr. Angela Yu'}</a>
            </div>
            
            <div className="lang-meta">
              <Info size={14} /> <span>Last updated 11/2026</span>
              <GlobeIcon /> <span>English</span>
            </div>
          </div>
        </div>
      </div>

      <div className="container main-content-grid">
        <div className="left-column">
          {/* What you'll learn */}
          <div className="what-you-learn">
            <h2>What you'll learn</h2>
            <div className="objectives-grid">
              <div className="objective-item"><CheckCircle size={16} /><span>Build 16 web development projects for your portfolio</span></div>
              <div className="objective-item"><CheckCircle size={16} /><span>Learn the latest technologies, including Javascript, React, Node</span></div>
              <div className="objective-item"><CheckCircle size={16} /><span>After the course you will be able to build ANY website you want</span></div>
              <div className="objective-item"><CheckCircle size={16} /><span>Work as a freelance web developer</span></div>
            </div>
          </div>

          {/* Course Content */}
          <div className="course-curriculum">
            <h2>Course content</h2>
            <div className="curriculum-meta">
              {course.sections?.length || 10} sections • {course.sections?.reduce((acc, sec) => acc + sec.lessons?.length, 0) || 120} lectures
            </div>
            
            <div className="accordion-list">
              {course.sections && course.sections.length > 0 ? course.sections.map((section, index) => (
                <div className="accordion-item-udemy" key={section.sectionId}>
                  <div className="accordion-header-udemy">
                    <strong>{section.title}</strong>
                    <span>{section.lessons?.length || 0} lectures</span>
                  </div>
                  <div className="accordion-body-udemy">
                    {section.lessons && section.lessons.map(lesson => (
                      <div className="lesson-item" key={lesson.lessonId}>
                        <PlayCircle size={14} /> 
                        <span className="lesson-title">{lesson.title}</span>
                        <span className="lesson-duration">{lesson.duration ? `${lesson.duration} min` : '10:00'}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )) : (
                <p>No content available for this course yet.</p>
              )}
            </div>
          </div>

          {/* Reviews Section */}
          <div className="course-reviews mt-5">
            <h2>Student feedback</h2>
            
            <div className="reviews-list mt-4">
              {course.reviews && course.reviews.length > 0 ? (
                course.reviews.map(review => (
                  <div key={review.reviewId} className="review-card mb-4 p-3 border rounded shadow-sm">
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <strong>{review.user?.fullName || 'Anonymous'}</strong>
                      <span className="text-muted small">{new Date(review.createdAt).toLocaleDateString()}</span>
                    </div>
                    <div className="d-flex align-items-center mb-2">
                      <div className="stars me-2">
                        {[...Array(5)].map((_, i) => (
                          <Star key={i} size={14} fill={i < review.rating ? "#eb8a2f" : "#e4e8eb"} color={i < review.rating ? "#eb8a2f" : "#e4e8eb"} />
                        ))}
                      </div>
                      {review.sentimentLabel && (
                        <span className={`badge ${review.sentimentLabel === 'Positive' ? 'bg-success' : review.sentimentLabel === 'Negative' ? 'bg-danger' : review.sentimentLabel === 'Neutral' ? 'bg-warning' : 'bg-secondary'}`}>
                          {review.sentimentLabel}
                          {review.sentimentScore !== null && review.sentimentScore !== undefined && ` (${(review.sentimentScore * 100).toFixed(0)}%)`}
                        </span>
                      )}
                    </div>
                    <p className="mb-0">{review.comment}</p>
                  </div>
                ))
              ) : (
                <p>No reviews yet.</p>
              )}
            </div>

            {isEnrolled && (
              <div className="leave-review-form mt-4 p-4 bg-light rounded">
                <h4>Leave a review</h4>
                <form onSubmit={handleSubmitReview}>
                  <div className="mb-3">
                    <label className="form-label">Rating</label>
                    <select className="form-select" value={reviewRating} onChange={e => setReviewRating(Number(e.target.value))}>
                      <option value="5">5 - Excellent</option>
                      <option value="4">4 - Good</option>
                      <option value="3">3 - Average</option>
                      <option value="2">2 - Poor</option>
                      <option value="1">1 - Terrible</option>
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Comment</label>
                    <textarea 
                      className="form-control" 
                      rows="3" 
                      required 
                      value={reviewComment}
                      onChange={e => setReviewComment(e.target.value)}
                    ></textarea>
                  </div>
                  <button type="submit" className="btn btn-primary" disabled={submittingReview}>
                    {submittingReview ? 'Submitting...' : 'Submit Review'}
                  </button>
                </form>
              </div>
            )}
          </div>
        </div>

        {/* Right Sidebar (Sticky) */}
        <div className="right-column">
          <div className="sidebar-card hover-3d">
            <div className="sidebar-video">
              <img src={course.thumbnailUrl ? `http://localhost:5150${course.thumbnailUrl}` : "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&q=80"} alt="Course preview" />
              <div className="play-overlay"><PlayCircle size={64} color="white" fill="rgba(0,0,0,0.6)" /></div>
              <div className="preview-text">Preview this course</div>
            </div>
            
            <div className="sidebar-content">
              <div className="price-container">
                <span className="price-big">₫{course.price || '399,000'}</span>
                <span className="price-old">₫1,200,000</span>
                <span className="discount">82% off</span>
              </div>
              
              <div className="urgency-text">
                <span style={{color: '#b32d0f', fontWeight: 'bold'}}>5 hours</span> left at this price!
              </div>
              
              {isEnrolled ? (
                <>
                  <div className="enrolled-msg mb-3">You are enrolled in this course!</div>
                  <button className="btn-udemy w-100 mb-2" onClick={() => navigate(`/my-courses/${id}/learn`)}>Go to course</button>
                </>
              ) : isInstructor ? (
                <div className="enrolled-msg">Instructors cannot enroll.</div>
              ) : (
                <>
                  <button className="btn-udemy-primary w-100 mb-2" onClick={handleAddToCart} disabled={enrolling}>
                    {enrolling ? 'Processing...' : 'Add to cart'}
                  </button>
                  <button className="btn-udemy-outline w-100 mb-3" onClick={handleBuyNow} disabled={enrolling}>Buy now</button>
                </>
              )}
              
              {!isInstructor && !isEnrolled && (
                <div className="text-center mt-3 mb-4">
                  <button 
                    className="btn btn-link text-decoration-none p-0 d-inline-flex align-items-center" 
                    style={{ color: inWishlist ? '#b32d0f' : '#2d2f31', fontWeight: 'bold' }}
                    onClick={toggleWishlist}
                  >
                    <Star size={18} className="me-2" fill={inWishlist ? "#b32d0f" : "none"} />
                    {inWishlist ? 'Wishlisted' : 'Add to Wishlist'}
                  </button>
                </div>
              )}
              
              <div className="guarantee">30-Day Money-Back Guarantee</div>
              
              <div className="includes-list">
                <h4>This course includes:</h4>
                <ul>
                  <li><Monitor size={14} /> 65 hours on-demand video</li>
                  <li><FileText size={14} /> 80 articles</li>
                  <li><Award size={14} /> Certificate of completion</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

const GlobeIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
);

export default CourseDetail;

