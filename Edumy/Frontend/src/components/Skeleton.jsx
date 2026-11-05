import React from 'react';
import './Skeleton.css';

export const SkeletonBox = ({ width = '100%', height = '20px', borderRadius = '6px', className = '', style = {} }) => {
  return (
    <div
      className={`skeleton-shimmer-box ${className}`}
      style={{ width, height, borderRadius, ...style }}
    />
  );
};

export const CourseCardSkeleton = () => {
  return (
    <div className="skeleton-course-card">
      <div className="skeleton-thumb skeleton-shimmer-box" />
      <div className="skeleton-content">
        <SkeletonBox width="85%" height="22px" className="mb-2" />
        <SkeletonBox width="60%" height="16px" className="mb-3" />
        <div className="d-flex align-items-center justify-content-between mt-auto pt-2 border-top">
          <SkeletonBox width="45%" height="20px" />
          <SkeletonBox width="30%" height="16px" />
        </div>
      </div>
    </div>
  );
};

export const CourseGridSkeleton = ({ count = 6 }) => {
  return (
    <div className="row g-4">
      {Array.from({ length: count }).map((_, i) => (
        <div className="col-12 col-md-6 col-lg-4" key={i}>
          <CourseCardSkeleton />
        </div>
      ))}
    </div>
  );
};

export default SkeletonBox;
