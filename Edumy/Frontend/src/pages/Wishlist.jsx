import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { Heart, Trash2 } from 'lucide-react';
import './CourseList.css'; // Reuse course list styles

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function Wishlist() {
  const [wishlist, setWishlist] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useContext(AuthContext);

  useEffect(() => {
    fetchWishlist();
  }, []);

  const fetchWishlist = async () => {
    try {
      const response = await api.get('/wishlist');
      setWishlist(response.data);
    } catch (err) {
      console.error(err);
      setError('Không thể tải danh sách khóa học yêu thích.');
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (courseId) => {
    try {
      await api.delete(`/wishlist/${courseId}`);
      // Optimistic update
      setWishlist(prev => prev.filter(item => item.courseId !== courseId));
    } catch (err) {
      console.error(err);
      alert('Không thể xóa khóa học khỏi danh sách yêu thích.');
    }
  };

  if (!user) {
    return <div className="container text-center my-5">Vui lòng đăng nhập để xem danh sách yêu thích.</div>;
  }

  if (loading) return <div className="container text-center my-5">Đang tải danh sách yêu thích...</div>;
  if (error) return <div className="container text-center my-5 text-danger">{error}</div>;

  return (
    <div className="container my-5">
      <h2 className="mb-4">Khóa học yêu thích <Heart className="text-danger ms-2" /></h2>
      
      {wishlist.length === 0 ? (
        <div className="text-center my-5">
          <h4 className="text-muted">Danh sách yêu thích của bạn đang trống.</h4>
          <p>Khám phá kho khóa học phong phú và lưu lại những khóa học bạn quan tâm!</p>
          <Link to="/courses" className="btn btn-primary mt-3 fw-bold">Khám phá khóa học ngay</Link>
        </div>
      ) : (
        <div className="course-grid">
          {wishlist.map(item => (
            <div key={item.id} className="course-card position-relative shadow-sm border-0">
              <Link to={`/courses/${item.courseId}`}>
                <img 
                  src={item.course.thumbnailUrl ? `${BACKEND_URL}${item.course.thumbnailUrl}` : "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=600&q=80"} 
                  alt={item.course.title} 
                  className="course-image" 
                />
                <div className="course-content">
                  <h3 className="course-title text-truncate" title={item.course.title}>{item.course.title}</h3>
                  <p className="course-instructor text-muted small">{item.course.instructor.fullName}</p>
                  <div className="course-price fw-bold fs-5 mt-2">₫{item.course.price.toLocaleString()}</div>
                </div>
              </Link>
              
              <button 
                className="btn btn-light position-absolute top-0 end-0 m-2 rounded-circle shadow-sm border"
                style={{ width: '40px', height: '40px', padding: '0', zIndex: 10 }}
                onClick={(e) => {
                  e.preventDefault();
                  handleRemove(item.courseId);
                }}
                title="Xóa khỏi danh sách yêu thích"
              >
                <Trash2 size={18} className="text-danger" />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Wishlist;
