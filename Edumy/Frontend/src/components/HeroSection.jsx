import React from 'react';
import { motion } from 'framer-motion';
import { Search, Sparkles } from 'lucide-react';
import './HeroSection.css';

const HeroSection = () => {
  return (
    <section className="hero-section">
      <div className="hero-background" aria-hidden="true"></div>
      
      <div className="container hero-container">
        <motion.div 
          className="hero-content"
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <div className="hero-card rounded-2xl shadow-sm">
            <span className="hero-badge pill-badge">
              <Sparkles size={14} strokeWidth={1.75} aria-hidden="true" />
              Nền tảng học tập thế hệ mới
            </span>
            <h1 className="text-slate-900">Học tập mở lối tương lai</h1>
            <p className="text-slate-600">
              Trang bị kỹ năng thực chiến cho hiện tại và tương lai. Bắt đầu hành trình cùng Edumy ngay hôm nay.
            </p>
            <div className="hero-search-mobile">
              <input
                type="text"
                placeholder="Bạn muốn học kỹ năng gì hôm nay?"
                className="focus:ring-primary"
              />
              <button
                type="button"
                className="search-btn cursor-pointer transition-all duration-200 active:scale-95"
                aria-label="Tìm kiếm khóa học"
              >
                <Search size={18} strokeWidth={1.75} aria-hidden="true" />
              </button>
            </div>
          </div>
        </motion.div>

        <motion.div 
          className="hero-image-wrapper"
          initial={{ opacity: 0, scale: 0.97 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5, delay: 0.15 }}
        >
          <img 
            src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1000&q=80" 
            alt="Học viên trực tuyến Edumy" 
            className="hero-image rounded-2xl"
          />
        </motion.div>
      </div>
    </section>
  );
};

export default HeroSection;
