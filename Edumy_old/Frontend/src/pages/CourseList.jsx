import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';

function CourseList() {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const response = await api.get('/courses');
        if (response.data && response.data.items) {
          setCourses(response.data.items);
        } else {
          setCourses(response.data);
        }
      } catch (error) {
        console.error("Error fetching courses", error);
      } finally {
        setLoading(false);
      }
    };
    fetchCourses();
  }, []);

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
        <div className="spinner-border text-primary" style={{ width: '3rem', height: '3rem' }} role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="py-4">
      <div className="d-flex justify-content-between align-items-end mb-5">
        <div>
          <h2 className="display-6 fw-bold mb-2">Explore <span className="gradient-text">Courses</span></h2>
          <p className="text-muted fs-5 mb-0">Find the perfect course to advance your skills.</p>
        </div>
        <div className="d-none d-md-block">
          <div className="input-group glass-card overflow-hidden rounded-pill">
            <span className="input-group-text bg-transparent border-0 pe-1"><i className="bi bi-search text-muted"></i></span>
            <input type="text" className="form-control bg-transparent border-0 shadow-none py-2" placeholder="Search courses..." />
          </div>
        </div>
      </div>

      <div className="row g-4">
        {courses.length === 0 ? (
          <div className="col-12 text-center py-5">
            <div className="text-muted mb-3"><i className="bi bi-inbox fs-1"></i></div>
            <h4>No courses found</h4>
            <p className="text-muted">Check back later for new content.</p>
          </div>
        ) : (
          courses.map(course => (
            <div className="col-md-6 col-lg-4" key={course.courseId}>
              <div className="card glass-card hover-lift h-100 border-0 p-2">
                <div className="position-relative">
                  <img 
                    src={course.thumbnailUrl ? `http://localhost:5150${course.thumbnailUrl}` : `https://images.unsplash.com/photo-1516321318423-f06f85e504b3?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80`} 
                    className="card-img-top rounded-4 object-fit-cover" 
                    alt={course.title}
                    style={{ height: '200px' }}
                  />
                  <span className="position-absolute top-0 end-0 m-3 badge bg-white text-dark shadow-sm rounded-pill px-3 py-2 fw-bold">
                    ${course.price}
                  </span>
                </div>
                <div className="card-body p-4 d-flex flex-column">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="badge bg-primary bg-opacity-10 text-primary rounded-pill px-3">{course.category?.name || 'General'}</span>
                    <span className="text-warning small fw-bold"><i className="bi bi-star-fill me-1"></i>{course.averageRating}</span>
                  </div>
                  <h5 className="card-title fw-bold mb-3">{course.title}</h5>
                  <p className="card-text text-muted small flex-grow-1">{course.description?.substring(0, 80)}...</p>
                  <hr className="text-muted opacity-25" />
                  <div className="d-flex justify-content-between align-items-center">
                    <div className="d-flex align-items-center gap-2">
                      <div className="bg-secondary rounded-circle" style={{width: '32px', height: '32px'}}></div>
                      <small className="text-muted fw-medium">{course.instructor?.fullName || 'Instructor'}</small>
                    </div>
                    <Link to={`/courses/${course.courseId}`} className="btn btn-outline-primary rounded-pill btn-sm fw-medium px-3">
                      View Details
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default CourseList;
