import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { Globe } from 'lucide-react';
import './Footer.css';

const Footer = () => {
  const location = useLocation();

  // Hide footer on dashboard / editor / player pages to maintain fixed viewport
  if (
    location.pathname.startsWith('/admin') ||
    location.pathname.startsWith('/instructor') ||
    location.pathname.includes('/learn')
  ) {
    return null;
  }

  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-top">
          <div className="footer-links-grid">
            {/* Column 1: Về Edumy */}
            <div className="footer-col">
              <h5 className="footer-col-title">Về Edumy</h5>
              <Link to="/courses">Giới thiệu nền tảng</Link>
              <a href="#business">Edumy Doanh nghiệp</a>
              <Link to="/instructor">Giảng dạy trên Edumy</Link>
              <a href="#careers">Cơ hội nghề nghiệp</a>
              <a href="#investors">Nhà đầu tư & Đối tác</a>
            </div>

            {/* Column 2: Khóa học nổi bật */}
            <div className="footer-col">
              <h5 className="footer-col-title">Khóa học nổi bật</h5>
              <Link to="/courses?categoryId=1">Lập trình Fullstack Web</Link>
              <Link to="/courses?categoryId=2">Khoa học Dữ liệu & AI</Link>
              <Link to="/courses?categoryId=3">Thiết kế UI/UX Chuyên nghiệp</Link>
              <Link to="/courses?categoryId=4">Quản trị & Kinh doanh số</Link>
              <Link to="/courses">Tất cả chứng chỉ nghề nghiệp</Link>
            </div>

            {/* Column 3: Hỗ trợ & Tài nguyên */}
            <div className="footer-col">
              <h5 className="footer-col-title">Hỗ trợ & Tài nguyên</h5>
              <a href="#help">Trung tâm trợ giúp 24/7</a>
              <a href="#blog">Blog & Bản tin công nghệ</a>
              <a href="#affiliate">Chương trình Tiếp thị liên kết</a>
              <a href="#community">Cộng đồng học viên</a>
              <a href="#accessibility">Tuyên bố trợ năng WCAG</a>
            </div>

            {/* Column 4: Pháp lý & Điều khoản */}
            <div className="footer-col">
              <h5 className="footer-col-title">Pháp lý & Điều khoản</h5>
              <a href="#terms">Điều khoản sử dụng</a>
              <a href="#privacy">Chính sách bảo mật</a>
              <a href="#cookies">Cài đặt Cookie</a>
              <a href="#sitemap">Sơ đồ trang web</a>
              <a href="#guidelines">Tiêu chuẩn cộng đồng</a>
            </div>
          </div>

          <div className="footer-lang-wrap">
            <button type="button" className="lang-btn-footer" aria-label="Chọn ngôn ngữ">
              <Globe size={18} strokeWidth={1.75} aria-hidden="true" />
              <span>Tiếng Việt (VN)</span>
            </button>
          </div>
        </div>
        
        <div className="footer-bottom">
          <div className="footer-logo">
            <Link to="/" className="logo-text">
              Edu<span>my</span>
            </Link>
          </div>
          <div className="footer-copyright">
            © 2026 Edumy Inc. Nền tảng Đào tạo Trực tuyến Chuẩn Quốc tế. Bảo lưu mọi quyền.
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
