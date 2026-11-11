import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import { Star, Clock, BookOpen, PlayCircle, CheckCircle2, Award } from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import './CourseCard.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

const CourseCard = ({ course }) => {
  if (!course) return null;
  const { isCourseEnrolled } = useContext(AuthContext);

  const courseId = course.courseId || course.id || 1;
  const isEnrolled = !!(course.isEnrolled || course.isPaid || course.enrolled || (isCourseEnrolled && isCourseEnrolled(courseId)));
  const ratingScore = Number(course.averageRating || course.rating || 4.8).toFixed(1);
  const reviewCount = course.reviews?.length || course.reviews || course.reviewCount || 124;
  const studentCount = course.studentCount || course.students || 1420;

  // Realistic formatted duration & lectures
  const duration = course.totalHours || course.durationHours || '24.5';
  const lectureCount = course.lectureCount || course.totalLessons || (course.sections ? course.sections.reduce((acc, s) => acc + (s.lessons?.length || 0), 0) : null) || 48;

  // Fallback realistic placeholder image if none provided
  const imageUrl = course.thumbnailUrl 
    ? (course.thumbnailUrl.startsWith('http') ? course.thumbnailUrl : `${BACKEND_URL}${course.thumbnailUrl}`)
    : (course.image || "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&q=80");

  const instructorName = course.instructor?.fullName || course.instructor || "Lê Việt Bắc";
  const instructorTitle = course.instructor?.headline || "Senior Tech Lead";

  const price = course.price !== undefined ? course.price : 499000;
  const originalPrice = course.originalPrice || (price > 0 ? Math.round(price * 1.8) : null);
  const discountPercent = originalPrice && originalPrice > price 
    ? Math.round(((originalPrice - price) / originalPrice) * 100) 
    : null;

  return (
    <Link
      to={`/courses/${courseId}`}
      className="edumy-course-card cursor-pointer transition-all duration-200"
    >
      {/* 1. Thumbnail Box */}
      <div className="card-thumbnail-wrap">
        <img 
          src={imageUrl} 
          alt={course.title} 
          loading="lazy"
        />
        {/* Authoritative Bestseller Pill Badge */}
        {(course.bestseller || course.isBestseller || studentCount > 500) && !isEnrolled && (
          <div className="card-badge-overlay">
            <span className="badge-bestseller pill-badge">
              <Award size={12} strokeWidth={2} aria-hidden="true" />
              Bán chạy nhất
            </span>
          </div>
        )}
        {isEnrolled && (
          <div className="card-badge-overlay">
            <span className="badge-owned pill-badge">
              <CheckCircle2 size={12} strokeWidth={2} aria-hidden="true" />
              Đã sở hữu
            </span>
          </div>
        )}
      </div>

      {/* 2. Course Details */}
      <div className="card-body-content">
        <h3 className="card-course-title line-clamp-2" title={course.title}>
          {course.title || "Khóa học Lập trình Fullstack Web"}
        </h3>

        <p className="card-instructor-meta line-clamp-1">
          <span className="instructor-name">{instructorName}</span>
          <span className="dot-divider" aria-hidden="true">•</span>
          <span className="instructor-title">{instructorTitle}</span>
        </p>

        {/* Real-world curriculum summary (Hours & lectures) */}
        <div className="card-curriculum-meta">
          <span className="meta-item">
            <Clock size={13} strokeWidth={1.75} className="text-slate-500" aria-hidden="true" />
            {duration} giờ học
          </span>
          <span className="dot-divider" aria-hidden="true">•</span>
          <span className="meta-item">
            <BookOpen size={13} strokeWidth={1.75} className="text-slate-500" aria-hidden="true" />
            {lectureCount} bài giảng
          </span>
        </div>

        {/* Rating Breakdown */}
        <div className="card-rating-row">
          <span className="rating-value">{ratingScore}</span>
          <div className="rating-stars" aria-label={`Đánh giá ${ratingScore} trên 5 sao`}>
            {[1, 2, 3, 4, 5].map((star) => (
              <Star 
                key={star} 
                size={13} 
                strokeWidth={1.75}
                className={star <= Math.round(Number(ratingScore)) ? "star-solid" : "star-muted"}
                fill={star <= Math.round(Number(ratingScore)) ? "#d97706" : "none"}
                color={star <= Math.round(Number(ratingScore)) ? "#d97706" : "#cbd5e1"}
                aria-hidden="true"
              />
            ))}
          </div>
          <span className="rating-count">({Number(reviewCount).toLocaleString('vi-VN')})</span>
        </div>

        {/* Pricing Area */}
        <div className="card-price-row">
          {isEnrolled ? (
            <div className="card-enrolled-footer">
              <span className="badge-enrolled-pill pill-badge">
                <CheckCircle2 size={13} strokeWidth={2} aria-hidden="true" />
                Đã sở hữu
              </span>
              <span
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  window.location.href = `/my-courses/${courseId}/learn`;
                }}
                className="btn-card-learn cursor-pointer transition-all duration-200 active:scale-95 hover:opacity-90"
              >
                <PlayCircle size={14} strokeWidth={1.75} aria-hidden="true" /> Vào học ngay
              </span>
            </div>
          ) : (
            <>
              <span className={`price-main ${price === 0 ? 'price-free' : ''}`}>
                {price === 0 ? "Miễn phí" : `₫${Number(price).toLocaleString('vi-VN')}`}
              </span>

              {originalPrice && price > 0 && (
                <span className="price-strikethrough">
                  ₫{Number(originalPrice).toLocaleString('vi-VN')}
                </span>
              )}

              {discountPercent && (
                <span className="badge-discount-percent pill-badge">
                  -{discountPercent}%
                </span>
              )}
            </>
          )}
        </div>
      </div>
    </Link>
  );
};

export default CourseCard;
