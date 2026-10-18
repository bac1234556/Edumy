import React, { useState, useEffect, useContext, useCallback } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { CourseGridSkeleton } from '../components/Skeleton';
import EmptyState from '../components/EmptyState';
import { Heart, Trash2, BookOpen, AlertTriangle } from 'lucide-react';
import { Link } from 'react-router-dom';
import './CourseList.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function Wishlist() {
  const [wishlist, setWishlist] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useContext(AuthContext);

  const fetchWishlist = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const response = await api.get('/wishlist');
      setWishlist(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Lỗi tải wishlist:", err);
      setError('Không thể tải danh sách khóa học yêu thích. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user) {
      fetchWishlist();
    } else {
      setLoading(false);
    }
  }, [user, fetchWishlist]);

  const handleRemove = async (courseId) => {
    try {
      await api.delete(`/wishlist/${courseId}`);
      setWishlist(prev => prev.filter(item => item.courseId !== courseId));
    } catch (err) {
      console.error(err);
      alert('Không thể xóa khóa học khỏi danh sách yêu thích.');
    }
  };

  if (!user) {
    return (
      <div className="container my-5">
        <EmptyState
          icon={Heart}
          title="Vui lòng đăng nhập"
          description="Đăng nhập tài khoản của bạn để xem và quản lý danh sách các khóa học yêu thích."
          actionText="Đăng nhập ngay"
          actionLink="/login"
        />
      </div>
    );
  }

  return (
    <div className="container my-5">
      <div className="d-flex align-items-center justify-content-between mb-4 pb-2 border-bottom">
        <div>
          <h1 className="h3 fw-bold text-dark mb-1">
            Khóa học yêu thích <Heart size={24} className="text-danger ms-1" fill="#ef4444" />
          </h1>
          <p className="text-secondary small mb-0">Các khóa học bạn đã lưu để theo dõi và đăng ký sau</p>
        </div>
        <span className="badge bg-light text-dark border px-3 py-2 rounded-pill fw-semibold">
          {wishlist.length} khóa học
        </span>
      </div>
      
      {loading ? (
        <CourseGridSkeleton count={3} />
      ) : error ? (
        <EmptyState
          icon={AlertTriangle}
          title="Không thể tải danh sách"
          description={error}
          actionText="Thử lại"
          onAction={fetchWishlist}
        />
      ) : wishlist.length === 0 ? (
        <EmptyState
          icon={BookOpen}
          title="Danh sách yêu thích của bạn đang trống"
          description="Bạn chưa lưu khóa học nào. Hãy khám phá kho tàng khóa học thực chiến của Edumy và bấm biểu tượng trái tim để lưu lại!"
          actionText="Khám phá khóa học ngay"
          actionLink="/courses"
        />
      ) : (
        <div className="row g-4">
          {wishlist.map(item => {
            const course = item.course || {};
            const thumb = course.thumbnailUrl 
              ? (course.thumbnailUrl.startsWith('http') ? course.thumbnailUrl : `${BACKEND_URL}${course.thumbnailUrl}`)
              : "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=600&q=80";

            return (
              <div key={item.id || item.courseId} className="col-12 col-md-6 col-lg-4">
                <div className="course-card position-relative shadow-sm border rounded-3 overflow-hidden bg-white h-100 d-flex flex-column">
                  <Link to={`/courses/${item.courseId}`}>
                    <img 
                      src={thumb} 
                      alt={course.title || 'Khóa học'} 
                      className="w-100" 
                      style={{ height: '180px', objectFit: 'cover' }}
                    />
                    <div className="p-3 d-flex flex-column flex-grow-1">
                      <h4 className="course-title text-truncate fw-bold mb-1" title={course.title}>{course.title}</h4>
                      <p className="course-instructor text-muted small mb-2">{course.instructor?.fullName || 'Giảng viên Edumy'}</p>
                      <div className="course-price fw-bold fs-5 mt-auto text-primary">
                        {course.price ? `₫${Number(course.price).toLocaleString('vi-VN')}` : 'Miễn phí'}
                      </div>
                    </div>
                  </Link>
                  
                  <button 
                    className="btn btn-light position-absolute top-0 end-0 m-2 rounded-circle shadow-sm border"
                    style={{ width: '38px', height: '38px', padding: '0', zIndex: 10 }}
                    onClick={(e) => {
                      e.preventDefault();
                      handleRemove(item.courseId);
                    }}
                    title="Xóa khỏi danh sách yêu thích"
                  >
                    <Trash2 size={16} className="text-danger" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default Wishlist;
