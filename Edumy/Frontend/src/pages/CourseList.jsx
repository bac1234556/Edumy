import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import CourseCard from '../components/CourseCard';
import { Search, ChevronLeft, ChevronRight, BookOpen, AlertCircle } from 'lucide-react';
import './CourseList.css';

function CourseList() {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 6;

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        setLoading(true);
        const response = await api.get('/courses');
        if (response.data && response.data.items) {
          setCourses(response.data.items);
        } else if (Array.isArray(response.data)) {
          setCourses(response.data);
        } else {
          setCourses([]);
        }
      } catch (error) {
        console.error("Error fetching courses", error);
      } finally {
        setLoading(false);
      }
    };
    fetchCourses();
  }, []);

  // Filter categories
  const categories = useMemo(() => {
    const cats = new Set();
    courses.forEach(c => {
      if (c.category?.name) cats.add(c.category.name);
    });
    return ['Tất cả', ...Array.from(cats)];
  }, [courses]);

  // Filtered courses
  const filteredCourses = useMemo(() => {
    return courses.filter(course => {
      const matchSearch = searchTerm === '' || 
        course.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        course.description?.toLowerCase().includes(searchTerm.toLowerCase());
      
      const matchCategory = selectedCategory === 'ALL' || selectedCategory === 'Tất cả' ||
        course.category?.name === selectedCategory;

      return matchSearch && matchCategory;
    });
  }, [courses, searchTerm, selectedCategory]);

  // Pagination calculation
  const totalPages = Math.ceil(filteredCourses.length / itemsPerPage) || 1;
  const paginatedCourses = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage;
    return filteredCourses.slice(start, start + itemsPerPage);
  }, [filteredCourses, currentPage]);

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  if (loading) {
    return (
      <div className="container py-5 text-center my-5" style={{ minHeight: '60vh' }}>
        <div className="spinner-border text-primary" style={{ width: '2.5rem', height: '2.5rem' }} role="status">
          <span className="visually-hidden">Đang tải...</span>
        </div>
        <p className="text-secondary mt-3 small">Đang tải danh mục khóa học...</p>
      </div>
    );
  }

  return (
    <div className="course-list-page py-4">
      <div className="container">
        {/* Breadcrumb Navigation */}
        <nav aria-label="breadcrumb" className="edumy-breadcrumbs">
          <Link to="/">Trang chủ</Link>
          <span className="separator">/</span>
          <span className="current">Khám phá Khóa học</span>
        </nav>

        {/* Page Header */}
        <div className="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3 mb-4 pb-3 border-bottom">
          <div>
            <h1 className="h3 fw-bold text-dark mb-1">Khám phá Khóa học Thực chiến</h1>
            <p className="text-secondary mb-0 small">
              Các khóa học lập trình, kỹ thuật và công nghệ chuẩn doanh nghiệp, được xây dựng bởi đội ngũ chuyên gia.
            </p>
          </div>

          {/* Search Box */}
          <div className="search-box-wrap" style={{ minWidth: '280px' }}>
            <div className="input-group">
              <span className="input-group-text bg-white border-end-0 text-muted">
                <Search size={16} />
              </span>
              <input
                type="text"
                className="form-control border-start-0 ps-1"
                placeholder="Tìm khóa học, kỹ năng..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setCurrentPage(1);
                }}
              />
            </div>
          </div>
        </div>

        {/* Category Filter Pills */}
        {categories.length > 1 && (
          <div className="category-filters-row d-flex flex-wrap gap-2 mb-4">
            {categories.map((cat) => {
              const isActive = (cat === 'Tất cả' && (selectedCategory === 'ALL' || selectedCategory === 'Tất cả')) || selectedCategory === cat;
              return (
                <button
                  key={cat}
                  onClick={() => {
                    setSelectedCategory(cat);
                    setCurrentPage(1);
                  }}
                  className={`btn btn-sm ${isActive ? 'btn-dark' : 'btn-outline-secondary bg-white text-dark'} rounded-pill px-3 py-1 fw-semibold`}
                  style={{ fontSize: '0.825rem' }}
                >
                  {cat}
                </button>
              );
            })}
          </div>
        )}

        {/* Course Grid */}
        {filteredCourses.length === 0 ? (
          <div className="text-center py-5 my-5 bg-white border rounded-3 p-5 shadow-sm">
            <AlertCircle size={44} className="text-muted mx-auto mb-3" />
            <h4 className="fw-bold text-dark mb-2">Không tìm thấy khóa học nào phù hợp</h4>
            <p className="text-muted small mb-4">
              Hãy thử tìm kiếm với từ khóa khác hoặc chuyển sang danh mục khác.
            </p>
            <button
              onClick={() => {
                setSearchTerm('');
                setSelectedCategory('ALL');
              }}
              className="btn btn-outline-primary btn-sm rounded-pill px-3"
            >
              Xem tất cả khóa học
            </button>
          </div>
        ) : (
          <>
            <div className="row g-4">
              {paginatedCourses.map((course) => (
                <div className="col-12 col-md-6 col-lg-4" key={course.courseId || course.id}>
                  <CourseCard course={course} />
                </div>
              ))}
            </div>

            {/* Production Pagination */}
            {totalPages > 1 && (
              <div className="edumy-pagination">
                <button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 1}
                  className="page-btn"
                  aria-label="Trang trước"
                >
                  <ChevronLeft size={16} />
                </button>

                {Array.from({ length: totalPages }, (_, idx) => idx + 1).map((pageNum) => (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    className={`page-btn ${currentPage === pageNum ? 'active' : ''}`}
                  >
                    {pageNum}
                  </button>
                ))}

                <button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage === totalPages}
                  className="page-btn"
                  aria-label="Trang sau"
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default CourseList;
