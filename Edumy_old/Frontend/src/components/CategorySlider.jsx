import React, { useRef, useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import './CategorySlider.css';

const categories = [
  "IT & Software", "Business", "Finance & Accounting", 
  "Development", "Design", "Marketing", "Health & Fitness", 
  "Music", "Photography", "Teaching & Academics"
];

const CategorySlider = () => {
  const scrollRef = useRef(null);
  const [showLeft, setShowLeft] = useState(false);
  
  const handleScroll = () => {
    if (scrollRef.current) {
      const { scrollLeft } = scrollRef.current;
      setShowLeft(scrollLeft > 0);
    }
  };

  const scrollLeft = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: -200, behavior: 'smooth' });
    }
  };

  const scrollRight = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: 200, behavior: 'smooth' });
    }
  };

  return (
    <div className="category-slider-wrapper container">
      {showLeft && (
        <button className="slider-btn left" onClick={scrollLeft}>
          <ChevronLeft size={24} />
        </button>
      )}
      
      <div 
        className="category-slider" 
        ref={scrollRef} 
        onScroll={handleScroll}
      >
        {categories.map((cat, index) => (
          <div key={index} className="category-item hover-3d">
            {cat}
          </div>
        ))}
      </div>
      
      <button className="slider-btn right" onClick={scrollRight}>
        <ChevronRight size={24} />
      </button>
    </div>
  );
};

export default CategorySlider;
