import React, { useRef, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import api from '../api/axiosConfig';
import './CategorySlider.css';

const CategorySlider = () => {
  const navigate = useNavigate();
  const scrollRef = useRef(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showLeft, setShowLeft] = useState(false);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true);
        const res = await api.get('/categories');
        const data = Array.isArray(res.data) ? res.data : (res.data?.items || []);
        setCategories(data);
      } catch (err) {
        console.error('Không thể tải danh mục từ API:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchCategories();
  }, []);
  
  const handleScroll = () => {
    if (scrollRef.current) {
      const { scrollLeft } = scrollRef.current;
      setShowLeft(scrollLeft > 0);
    }
  };

  const scrollLeft = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: -220, behavior: 'smooth' });
    }
  };

  const scrollRight = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: 220, behavior: 'smooth' });
    }
  };

  const handleCategoryClick = (cat) => {
    if (cat.categoryId) {
      navigate(`/courses?categoryId=${cat.categoryId}`);
    } else {
      navigate(`/courses?search=${encodeURIComponent(cat.name || '')}`);
    }
  };

  if (loading) {
    return (
      <div className="category-slider-wrapper container py-2">
        <div className="d-flex gap-3 overflow-hidden">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="skeleton-shimmer-box rounded-pill" style={{ width: '140px', height: '42px', flexShrink: 0 }} />
          ))}
        </div>
      </div>
    );
  }

  if (categories.length === 0) {
    return null;
  }

  return (
    <div className="category-slider-wrapper container">
      {showLeft && (
        <button className="slider-btn left" onClick={scrollLeft} aria-label="Cuộn sang trái">
          <ChevronLeft size={24} />
        </button>
      )}
      
      <div 
        className="category-slider" 
        ref={scrollRef} 
        onScroll={handleScroll}
      >
        {categories.map((cat) => (
          <div
            key={cat.categoryId || cat.name}
            className="category-item hover-3d"
            onClick={() => handleCategoryClick(cat)}
            role="button"
            tabIndex={0}
          >
            {cat.name}
          </div>
        ))}
      </div>
      
      <button className="slider-btn right" onClick={scrollRight} aria-label="Cuộn sang phải">
        <ChevronRight size={24} />
      </button>
    </div>
  );
};

export default CategorySlider;
