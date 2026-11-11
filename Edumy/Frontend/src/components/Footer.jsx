import React from 'react';
import { Globe } from 'lucide-react';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-top">
          <div className="footer-links-grid">
            <div className="footer-col">
              <a href="#" className="cursor-pointer transition-all duration-200">Edumy Doanh nghiệp</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Giảng dạy trên Edumy</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Tải ứng dụng di động</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Về chúng tôi</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Liên hệ hỗ trợ</a>
            </div>
            <div className="footer-col">
              <a href="#" className="cursor-pointer transition-all duration-200">Cơ hội nghề nghiệp</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Blog công nghệ</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Trung tâm trợ giúp</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Tiếp thị liên kết</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Nhà đầu tư</a>
            </div>
            <div className="footer-col">
              <a href="#" className="cursor-pointer transition-all duration-200">Điều khoản sử dụng</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Chính sách bảo mật</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Cài đặt Cookie</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Sơ đồ trang web</a>
              <a href="#" className="cursor-pointer transition-all duration-200">Tuyên bố trợ năng</a>
            </div>
          </div>
          <div className="footer-lang">
            <button type="button" className="lang-btn-footer cursor-pointer transition-all duration-200 active:scale-95">
              <Globe size={18} strokeWidth={1.75} aria-hidden="true" />
              <span>Tiếng Việt</span>
            </button>
          </div>
        </div>
        
        <div className="footer-bottom">
          <div className="footer-logo">
            <span className="logo-text">Edu<span>my</span></span>
          </div>
          <div className="footer-copyright">
            © 2026 Edumy, Inc. Bản quyền được bảo lưu.
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
