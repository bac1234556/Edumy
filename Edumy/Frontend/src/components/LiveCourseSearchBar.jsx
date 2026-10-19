import React, { useState, useEffect, useRef, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Loader2, ArrowRight, BookOpen, User, Tag, CheckCircle2 } from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import api from '../api/axiosConfig';
import './LiveCourseSearchBar.css';

const LiveCourseSearchBar = ({
  placeholder = "Tìm kiếm khóa học, kỹ năng, giảng viên...",
  className = "",
  variant = "navbar", // "navbar" | "hero" | "mobile"
  onCloseMobile = null
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [results, setResults] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  const { isCourseEnrolled } = useContext(AuthContext);
  const containerRef = useRef(null);
  const inputRef = useRef(null);
  const navigate = useNavigate();

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // Debounced Instant Search (300ms)
  useEffect(() => {
    const query = searchTerm.trim();

    // Rule: query length >= 2 characters triggers search
    if (query.length < 2) {
      setResults([]);
      setIsLoading(false);
      setIsOpen(false);
      setHasSearched(false);
      return;
    }

    setIsLoading(true);
    setHasSearched(false);

    const debounceTimer = setTimeout(async () => {
      try {
        const response = await api.get('/v1/courses/instant-search', {
          params: { q: query }
        });

        if (Array.isArray(response.data)) {
          setResults(response.data);
          setIsOpen(true);
        } else {
          setResults([]);
        }
      } catch (error) {
        console.error('Instant course search error:', error);
        setResults([]);
      } finally {
        setIsLoading(false);
        setHasSearched(true);
      }
    }, 300);

    return () => clearTimeout(debounceTimer);
  }, [searchTerm]);

  const handleSubmit = (e) => {
    if (e) e.preventDefault();
    const query = searchTerm.trim();
    if (query) {
      setIsOpen(false);
      if (onCloseMobile) onCloseMobile();
      navigate(`/courses?search=${encodeURIComponent(query)}`);
    }
  };

  const handleSelectCourse = (courseId) => {
    setIsOpen(false);
    if (onCloseMobile) onCloseMobile();
    navigate(`/courses/${courseId}`);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Escape') {
      setIsOpen(false);
    } else if (e.key === 'Enter') {
      handleSubmit(e);
    }
  };

  const formatPrice = (price) => {
    if (!price || Number(price) === 0) {
      return 'Miễn phí';
    }
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  };

  return (
    <div 
      className={`live-search-container live-search-${variant} ${className}`} 
      ref={containerRef}
    >
      <form className="live-search-input-wrap" onSubmit={handleSubmit}>
        <button 
          type="submit" 
          className="live-search-icon-btn" 
          aria-label="Tìm kiếm"
        >
          <Search size={variant === 'hero' ? 20 : 18} />
        </button>

        <input
          ref={inputRef}
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          onFocus={() => {
            if (searchTerm.trim().length >= 2) {
              setIsOpen(true);
            }
          }}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          className="live-search-input"
          autoComplete="off"
          spellCheck="false"
        />

        {isLoading && (
          <div className="live-search-spinner" aria-label="Đang tìm kiếm...">
            <Loader2 size={16} className="animate-spin" />
          </div>
        )}

        {variant === 'hero' && (
          <button type="submit" className="live-search-hero-submit-btn">
            Tìm kiếm
          </button>
        )}
      </form>

      {/* Instant Suggestion Dropdown */}
      {isOpen && searchTerm.trim().length >= 2 && (
        <div className="live-search-dropdown shadow-2xl animate-fade-in">
          {isLoading && results.length === 0 ? (
            <div className="live-search-loading-state">
              <Loader2 size={20} className="animate-spin text-primary" />
              <span>Đang tìm kiếm khóa học phù hợp...</span>
            </div>
          ) : results.length > 0 ? (
            <>
              <div className="live-search-header">
                <span className="live-search-header-title">
                  Khóa học gợi ý ({results.length})
                </span>
                <span className="live-search-header-badge">Instant</span>
              </div>

              <div className="live-search-list" role="listbox">
                {results.map((course) => (
                  <div
                    key={course.id}
                    className="live-search-item"
                    role="option"
                    tabIndex={0}
                    onClick={() => handleSelectCourse(course.id)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') handleSelectCourse(course.id);
                    }}
                  >
                    <div className="live-search-thumb-box">
                      {course.thumbnailUrl ? (
                        <img
                          src={course.thumbnailUrl}
                          alt={course.title}
                          className="live-search-thumb-img"
                          onError={(e) => {
                            e.target.style.display = 'none';
                            e.target.nextElementSibling.style.display = 'flex';
                          }}
                        />
                      ) : null}
                      <div 
                        className="live-search-thumb-fallback"
                        style={{ display: course.thumbnailUrl ? 'none' : 'flex' }}
                      >
                        <BookOpen size={16} />
                      </div>
                    </div>

                    <div className="live-search-info">
                      <h4 className="live-search-item-title" title={course.title}>
                        {course.title}
                      </h4>
                      <div className="live-search-item-meta">
                        <span className="live-search-item-instructor">
                          <User size={12} style={{ display: 'inline', marginRight: 4, verticalAlign: -1 }} />
                          {course.instructorName || 'Giảng viên Edumy'}
                        </span>
                        {isCourseEnrolled && isCourseEnrolled(course.id) ? (
                          <span style={{ color: '#10b981', fontWeight: 700, fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}>
                            <CheckCircle2 size={12} /> ✓ Đã sở hữu
                          </span>
                        ) : (
                          <span className={`live-search-item-price ${!course.price || Number(course.price) === 0 ? 'is-free' : ''}`}>
                            {formatPrice(course.price)}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div 
                className="live-search-footer"
                onClick={handleSubmit}
              >
                <span>Xem tất cả kết quả cho "<strong>{searchTerm.trim()}</strong>"</span>
                <ArrowRight size={15} />
              </div>
            </>
          ) : hasSearched ? (
            <div className="live-search-empty-state">
              <Search size={24} className="text-muted-foreground mb-2" />
              <p className="empty-text">
                Không tìm thấy khóa học nào phù hợp với "<strong>{searchTerm.trim()}</strong>"
              </p>
              <button 
                type="button" 
                className="empty-view-all-btn"
                onClick={handleSubmit}
              >
                Tìm kiếm trong toàn bộ thư viện khóa học
              </button>
            </div>
          ) : null}
        </div>
      )}
    </div>
  );
};

export default LiveCourseSearchBar;
