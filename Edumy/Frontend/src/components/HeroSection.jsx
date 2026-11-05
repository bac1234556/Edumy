import React from 'react';
import { motion } from 'framer-motion';
import './HeroSection.css';

const HeroSection = () => {
  return (
    <section className="hero-section">
      <div className="hero-background"></div>
      
      <div className="container hero-container">
        <motion.div 
          className="hero-content"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <div className="hero-card">
            <h1>Học tập mở lối tương lai</h1>
            <p>Trang bị kỹ năng thực chiến cho hiện tại và tương lai. Bắt đầu hành trình cùng Edumy ngay hôm nay.</p>
            <div className="hero-search-mobile">
              <input type="text" placeholder="Bạn muốn học kỹ năng gì hôm nay?" />
              <button className="search-btn">🔍</button>
            </div>
          </div>
        </motion.div>

        <motion.div 
          className="hero-image-wrapper"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6, delay: 0.2 }}
        >
          <img 
            src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1000&q=80" 
            alt="Học viên trực tuyến Edumy" 
            className="hero-image"
          />
        </motion.div>
      </div>
    </section>
  );
};

export default HeroSection;
