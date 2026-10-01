import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import './MyLearning.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5150/api').replace('/api', '');

function MyLearning() {
  const [enrolledCourses, setEnrolledCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useContext(AuthContext);

  useEffect(() => {
    const fetchEnrolledCourses = async () => {
      try {
        const response = await api.get('/courses/enrolled');
        setEnrolledCourses(response.data);
      } catch (err) {
        setError('Could not fetch your courses.');
      } finally {
        setLoading(false);
      }
    };
    fetchEnrolledCourses();
  }, []);

  if (loading) return <div className="container text-center mt-5" style={{padding: '5rem 0'}}>Loading...</div>;
  if (error) return <div className="container text-center mt-5 error-msg">{error}</div>;

  return (
    <div className="my-learning-page">
      <div className="my-learning-header">
        <div className="container">
          <h1>My learning</h1>
          <div className="learning-tabs">
            <span className="tab active">All courses</span>
            <span className="tab">My Lists</span>
            <span className="tab">Wishlist</span>
          </div>
        </div>
      </div>

      <div className="container mt-4 mb-5">
        {enrolledCourses.length === 0 ? (
          <div className="empty-learning">
            <h3>You don't have any enrolled courses yet.</h3>
            <Link to="/" className="btn-udemy-primary mt-3">Browse Courses</Link>
          </div>
        ) : (
          <div className="enrolled-grid">
            {enrolledCourses.map(item => (
              <Link to={`/my-courses/${item.courseId}/learn`} className="enrolled-card-link" key={item.courseId}>
                <div className="enrolled-card hover-3d">
                  <div className="card-img-wrapper">
                    <img 
                      src={item.thumbnailUrl ? `${BACKEND_URL}${item.thumbnailUrl}` : "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400&q=80"} 
                      alt={item.title} 
                      className="card-img-top" 
                    />
                    <div className="play-overlay-small">▶</div>
                  </div>
                  <div className="card-body">
                    <h5 className="card-title">{item.title}</h5>
                    <p className="instructor-name">{item.fullName || 'Instructor'}</p>
                    <div className="progress-container">
                      <div className="progress-bar" style={{width: `${item.progressPercentage}%`}}></div>
                    </div>
                    <div className="progress-text">{item.progressPercentage}% complete</div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default MyLearning;
