import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import api from '../api/axiosConfig';
import CourseCard from '../components/CourseCard';
import EmptyState from '../components/EmptyState';
import { CourseGridSkeleton, SkeletonBox } from '../components/Skeleton';
import { Search, ChevronLeft, ChevronRight, BookOpen, AlertTriangle } from 'lucide-react';
import './CourseList.css';

function CourseList() {
  const [searchParams] = useSearchParams();
  const initialCategory = searchParams.get('categoryId');
  const initialSearch = searchParams.get('search') || '';

  const [courses, setCourses] = useState([]);
  const [categoriesList, setCategoriesList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState(initialSearch);
  const [selectedCategory, setSelectedCategory] = useState(initialCategory ? String(initialCategory) : 'ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 6;

  const fetchCoursesAndCategories = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const [coursesRes, catRes] = await Promise.allSettled([
        api.get('/courses'),
        api.get('/categories')
      ]);

      if (coursesRes.status === 'fulfilled') {
        const data = coursesRes.value.data;
        if (data && data.items) {
          setCourses(data.items);
        } else if (Array.isArray(data)) {
          setCourses(data);
        } else {
          setCourses([]);
        }
      } else {
        throw new Error('Không thể tải danh sách khóa học từ máy chủ.');
      }

      if (catRes.status === 'fulfilled') {
        const catData = Array.isArray(catRes.value.data) ? catRes.value.data : (catRes.value.data?.items || []);
        setCategoriesList(catData);
      }
    } catch (err) {
      console.error("Error fetching courses", err);
      setError('Đã xảy ra lỗi khi tải dữ liệu. Vui lòng kiểm tra kết nối mạng và thử lại.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCoursesAndCategories();
  }, [fetchCoursesAndCategories]);

  // Sync with searchParams
  useEffect(() => {
    if (initialCategory) setSelectedCategory(String(initialCategory));
    if (initialSearch) setSearchTerm(initialSearch);
  }, [initialCategory, initialSearch]);

  // Filtered courses
  const filteredCourses = useMemo(() => {
    return courses.filter(course => {
      const matchSearch = searchTerm === '' || 
        course.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        course.description?.toLowerCase().includes(searchTerm.toLowerCase());
      
      const matchCategory = selectedCategory === 'ALL' || selectedCategory === 'Tất cả' ||
        String(course.categoryId) === selectedCategory ||
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

        {/* Category Filter Pills from Real API */}
        {categoriesList.length > 0 && (
          <div className="category-filters-row d-flex flex-wrap gap-2 mb-4">
            <button
              onClick={() => {
                setSelectedCategory('ALL');
                setCurrentPage(1);
              }}
              className={`btn btn-sm ${selectedCategory === 'ALL' ? 'btn-dark' : 'btn-outline-secondary bg-white text-dark'} rounded-pill px-3 py-1 fw-semibold`}
              style={{ fontSize: '0.825rem' }}
            >
              Tất cả danh mục
            </button>
            {categoriesList.map((cat) => {
              const isActive = selectedCategory === String(cat.categoryId) || selectedCategory === cat.name;
              return (
                <button
                  key={cat.categoryId}
                  onClick={() => {
                    setSelectedCategory(String(cat.categoryId));
                    setCurrentPage(1);
                  }}
                  className={`btn btn-sm ${isActive ? 'btn-dark' : 'btn-outline-secondary bg-white text-dark'} rounded-pill px-3 py-1 fw-semibold`}
                  style={{ fontSize: '0.825rem' }}
                >
                  {cat.name}
                </button>
              );
            })}
          </div>
        )}

        {/* Loading State: Skeleton */}
        {loading ? (
          <div className="py-2">
            <CourseGridSkeleton count={6} />
          </div>
        ) : error ? (
          <EmptyState
            icon={AlertTriangle}
            title="Không thể tải danh sách khóa học"
            description={error}
            actionText="Thử lại ngay"
            onAction={fetchCoursesAndCategories}
          />
        ) : filteredCourses.length === 0 ? (
          <EmptyState
            icon={BookOpen}
            title="Chưa có khóa học nào phù hợp"
            description={
              searchTerm
                ? `Không tìm thấy khóa học nào khớp với từ khóa "${searchTerm}". Bạn hãy thử tìm kiếm với cụm từ khác.`
                : "Danh mục này hiện chưa có khóa học nào được xuất bản. Hãy quay lại sau!"
            }
            actionText={searchTerm || selectedCategory !== 'ALL' ? "Xem tất cả khóa học" : "Khám phá danh mục khác"}
            onAction={() => {
              setSearchTerm('');
              setSelectedCategory('ALL');
              setCurrentPage(1);
            }}
          />
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
              <div className="edumy-pagination mt-5">
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
