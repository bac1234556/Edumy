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
              <a href="#">Edumy Doanh nghiệp</a>
              <a href="#">Giảng dạy trên Edumy</a>
              <a href="#">Tải ứng dụng di động</a>
              <a href="#">Về chúng tôi</a>
              <a href="#">Liên hệ hỗ trợ</a>
            </div>
            <div className="footer-col">
              <a href="#">Cơ hội nghề nghiệp</a>
              <a href="#">Blog công nghệ</a>
              <a href="#">Trung tâm trợ giúp</a>
              <a href="#">Tiếp thị liên kết</a>
              <a href="#">Nhà đầu tư</a>
            </div>
            <div className="footer-col">
              <a href="#">Điều khoản sử dụng</a>
              <a href="#">Chính sách bảo mật</a>
              <a href="#">Cài đặt Cookie</a>
              <a href="#">Sơ đồ trang web</a>
              <a href="#">Tuyên bố trợ năng</a>
            </div>
          </div>
          <div className="footer-lang">
            <button className="lang-btn-footer">
              <Globe size={18} />
              <span>Tiếng Việt</span>
            </button>
          </div>
        </div>
        
        <div className="footer-bottom">
          <div className="footer-logo">
            <span className="logo-text">Edumy</span>
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
