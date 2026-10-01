import React from 'react';
import { Link } from 'react-router-dom';
import { Star } from 'lucide-react';
import './CourseCard.css';

const CourseCard = ({ course }) => {
  return (
    <Link to={`/courses/${course.courseId || course.id || 1}`} className="course-card hover-3d">
      <div className="course-image">
        <img 
          src={course.thumbnailUrl ? (course.thumbnailUrl.startsWith('http') ? course.thumbnailUrl : `http://localhost:5150${course.thumbnailUrl}`) : (course.image || "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&q=80")} 
          alt={course.title} 
        />
      </div>
      <div className="course-content">
        <h3 className="course-title">{course.title || "Untitled Course"}</h3>
        <p className="course-instructor">{course.instructor?.fullName || course.instructor || "Instructor"}</p>
        
        <div className="course-rating">
          <span className="rating-score">{course.averageRating || course.rating || "4.5"}</span>
          <div className="stars">
            {[1, 2, 3, 4, 5].map((star) => (
              <Star 
                key={star} 
                size={14} 
                className={star <= Math.round(course.averageRating || course.rating || 4.5) ? "star-filled" : "star-empty"}
                fill={star <= Math.round(course.averageRating || course.rating || 4.5) ? "var(--accent-color)" : "none"}
                color={star <= Math.round(course.averageRating || course.rating || 4.5) ? "var(--accent-color)" : "var(--text-muted)"}
              />
            ))}
          </div>
          <span className="rating-count">({course.reviews?.length || course.reviews || "0"})</span>
        </div>
        
        <div className="course-price">
          <span className="current-price">₫{(course.price || 0).toLocaleString()}</span>
          {course.originalPrice && (
            <span className="original-price">₫{course.originalPrice}</span>
          )}
        </div>
        
        {course.bestseller && (
          <div className="course-badges">
            <span className="badge bestseller">Bestseller</span>
          </div>
        )}
      </div>
    </Link>
  );
};

export default CourseCard;
