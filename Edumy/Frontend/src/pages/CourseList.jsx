import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { courseApi } from '../api/modules/courseApi';
import CourseCard from '../components/CourseCard';
import EmptyState from '../components/EmptyState';
import { CourseGridSkeleton } from '../components/Skeleton';
import { Search, ChevronLeft, ChevronRight, BookOpen, AlertTriangle, Filter } from 'lucide-react';
import './CourseList.css';

function CourseList() {
  const [searchParams] = useSearchParams();
  const initialCategory = searchParams.get('categoryId');
  const initialSearch = searchParams.get('search') || searchParams.get('q') || '';
  const initialPriceType = searchParams.get('priceType') || 'ALL';

  const [courses, setCourses] = useState([]);
  const [categoriesList, setCategoriesList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState(initialSearch);
  const [selectedCategory, setSelectedCategory] = useState(initialCategory ? String(initialCategory) : 'ALL');
  const [priceType, setPriceType] = useState(initialPriceType.toUpperCase()); // ALL, FREE, PAID
  const [sortBy, setSortBy] = useState('NEWEST');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 6;

  const fetchCoursesAndCategories = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const [coursesRes, catRes] = await Promise.allSettled([
        courseApi.getCourses({ pageSize: 100 }),
        courseApi.getCategories()
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
      console.error('Error fetching courses', err);
      setError('Đã xảy ra lỗi khi tải dữ liệu. Vui lòng kiểm tra kết nối mạng và thử lại.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCoursesAndCategories();
  }, [fetchCoursesAndCategories]);

  useEffect(() => {
    if (initialCategory) setSelectedCategory(String(initialCategory));
    if (initialSearch) setSearchTerm(initialSearch);
    if (initialPriceType) setPriceType(initialPriceType.toUpperCase());
  }, [initialCategory, initialSearch, initialPriceType]);

  // Filtered & sorted courses
  const filteredCourses = useMemo(() => {
    const list = courses.filter((course) => {
      // Search by course title
      const matchSearch =
        searchTerm === '' ||
        course.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        course.description?.toLowerCase().includes(searchTerm.toLowerCase());

      // Filter by category
      const matchCategory =
        selectedCategory === 'ALL' ||
        selectedCategory === 'Tất cả' ||
        String(course.categoryId) === selectedCategory ||
        course.category?.name === selectedCategory;

      // Filter by priceType (ALL / FREE / PAID)
      const numericPrice = Number(course.price || 0);
      let matchPrice = true;
      if (priceType === 'FREE') {
        matchPrice = numericPrice === 0;
      } else if (priceType === 'PAID') {
        matchPrice = numericPrice > 0;
      }

      return matchSearch && matchCategory && matchPrice;
    });

    if (sortBy === 'PRICE_ASC') {
      list.sort((a, b) => Number(a.price || 0) - Number(b.price || 0));
    } else if (sortBy === 'PRICE_DESC') {
      list.sort((a, b) => Number(b.price || 0) - Number(a.price || 0));
    } else if (sortBy === 'RATING') {
      list.sort((a, b) => Number(b.averageRating || 0) - Number(a.averageRating || 0));
    } else {
      list.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
    }

    return list;
  }, [courses, searchTerm, selectedCategory, priceType, sortBy]);

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
        {/* Breadcrumb Header */}
        <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
          <div>
            <h1 className="h3 fw-bold text-dark mb-1">Tất cả khóa học</h1>
            <p className="text-secondary small mb-0">
              Khám phá hơn {courses.length} khóa học công nghệ thực chiến cùng chuyên gia
            </p>
          </div>

          {/* Quick Search Input */}
          <div className="search-box-wrapper" style={{ minWidth: '280px' }}>
            <div className="input-group">
              <span className="input-group-text bg-white border-end-0 text-secondary">
                <Search size={16} />
              </span>
              <input
                type="text"
                className="form-control border-start-0 ps-0 shadow-none"
                placeholder="Tìm kiếm khóa học..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setCurrentPage(1);
                }}
              />
            </div>
          </div>
        </div>

        {/* Filters and Sorting Bar */}
        <div className="filters-control-bar p-3 bg-white rounded-3 border mb-4 d-flex flex-wrap align-items-center justify-content-between gap-3">
          <div className="d-flex align-items-center gap-2 flex-wrap">
            <span className="small fw-bold text-secondary d-flex align-items-center gap-1">
              <Filter size={15} /> Bộ lọc giá:
            </span>
            {[
              { key: 'ALL', label: 'Tất cả' },
              { key: 'FREE', label: 'Miễn phí (0đ)' },
              { key: 'PAID', label: 'Trả phí' }
            ].map((item) => (
              <button
                key={item.key}
                type="button"
                onClick={() => {
                  setPriceType(item.key);
                  setCurrentPage(1);
                }}
                className={`filter-pill-btn ${priceType === item.key ? 'active' : ''}`}
              >
                {item.label}
              </button>
            ))}
          </div>
          <div className="d-flex align-items-center gap-3">
            <div className="d-flex align-items-center gap-2">
              <label htmlFor="course-sort-select" className="small fw-semibold text-secondary mb-0">
                Sắp xếp:
              </label>
              <select
                id="course-sort-select"
                className="form-select form-select-sm"
                value={sortBy}
                onChange={(e) => {
                  setSortBy(e.target.value);
                  setCurrentPage(1);
                }}
                style={{ width: 'auto', borderRadius: '8px', borderColor: 'var(--border-subtle)' }}
              >
                <option value="NEWEST">Mới nhất</option>
                <option value="RATING">Đánh giá cao</option>
                <option value="PRICE_ASC">Giá tăng dần</option>
                <option value="PRICE_DESC">Giá giảm dần</option>
              </select>
            </div>
            <div className="small text-secondary tabular-nums">
              Hiển thị <strong>{filteredCourses.length}</strong> khóa học
            </div>
          </div>
        </div>

        {/* Category Filter Pills */}
        {categoriesList.length > 0 && (
          <div className="category-filters-row d-flex flex-wrap gap-2 mb-4">
            <button
              type="button"
              onClick={() => {
                setSelectedCategory('ALL');
                setCurrentPage(1);
              }}
              className={`filter-pill-btn ${selectedCategory === 'ALL' ? 'active' : ''}`}
            >
              Tất cả danh mục
            </button>
            {categoriesList.map((cat) => {
              const isActive = selectedCategory === String(cat.categoryId) || selectedCategory === cat.name;
              return (
                <button
                  type="button"
                  key={cat.categoryId}
                  onClick={() => {
                    setSelectedCategory(String(cat.categoryId));
                    setCurrentPage(1);
                  }}
                  className={`filter-pill-btn ${isActive ? 'active' : ''}`}
                >
                  {cat.name}
                </button>
              );
            })}
          </div>
        )}

        {/* Course Grid */}
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
                ? `Không tìm thấy khóa học nào khớp với từ khóa "${searchTerm}".`
                : 'Không có khóa học nào khớp với bộ lọc hiện tại.'
            }
            actionText="Xóa bộ lọc"
            onAction={() => {
              setSearchTerm('');
              setSelectedCategory('ALL');
              setPriceType('ALL');
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
