import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { courseApi } from '../api/modules/courseApi';
import { mlApi } from '../api/modules/mlApi';
import CourseCard from '../components/CourseCard';
import {
  ArrowRight,
  Award,
  BookOpen,
  ShieldCheck,
  Star,
  CheckCircle2,
  Code,
  Palette,
  BrainCircuit,
  BarChart3,
  Layers,
  Terminal,
  MessageSquare,
  FileCheck2,
  Compass
} from 'lucide-react';
import LiveCourseSearchBar from '../components/LiveCourseSearchBar';
import './HomePage.css';

const getCategoryIcon = (categoryName = '') => {
  const name = categoryName.toLowerCase();
  if (name.includes('lập trình') || name.includes('dev') || name.includes('code') || name.includes('phần mềm')) return <Code size={18} strokeWidth={1.75} />;
  if (name.includes('thiết kế') || name.includes('design') || name.includes('ui')) return <Palette size={18} strokeWidth={1.75} />;
  if (name.includes('ai') || name.includes('dữ liệu') || name.includes('data') || name.includes('hạ tầng')) return <BrainCircuit size={18} strokeWidth={1.75} />;
  if (name.includes('kinh doanh') || name.includes('marketing') || name.includes('tài chính')) return <BarChart3 size={18} strokeWidth={1.75} />;
  return <Layers size={18} strokeWidth={1.75} />;
};

function HomePage() {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [popularCourses, setPopularCourses] = useState([]);
  const [newestCourses, setNewestCourses] = useState([]);
  const [recommendedCourses, setRecommendedCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchHomeData = async () => {
      setLoading(true);
      try {
        const catRes = await courseApi.getCategories();
        const catData = Array.isArray(catRes.data) ? catRes.data : (catRes.data?.items || []);
        setCategories(Array.isArray(catData) ? catData.slice(0, 8) : []);

        const popularRes = await courseApi.getCourses({ pageSize: 8, sortBy: 'rating', sortOrder: 'desc' });
        const popularData = Array.isArray(popularRes.data?.items) ? popularRes.data.items : (Array.isArray(popularRes.data) ? popularRes.data : []);
        setPopularCourses(popularData);

        const newestRes = await courseApi.getCourses({ pageSize: 4, sortBy: 'newest', sortOrder: 'desc' });
        const newestData = Array.isArray(newestRes.data?.items) ? newestRes.data.items : (Array.isArray(newestRes.data) ? newestRes.data : []);
        setNewestCourses(newestData);

        if (user && user.role === 'Student') {
          try {
            const recRes = await mlApi.getStudentRecommendations();
            const recData = Array.isArray(recRes.data) ? recRes.data : (recRes.data?.items || []);
            setRecommendedCourses(Array.isArray(recData) ? recData : []);
          } catch {
            // Optional recommendations
          }
        }
      } catch (error) {
        console.error('Failed to load homepage data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchHomeData();
  }, [user]);

  return (
    <div className="homepage-container">
      {/* --------------------------------------------------------------------
          1. EDITORIAL SPLIT HERO SECTION (Anti-AI-Slop Architectural Layout)
          -------------------------------------------------------------------- */}
      <section className="edumy-hero-section">
        <div className="container">
          <div className="hero-grid">
            {/* Left Editorial Column */}
            <div className="hero-text-content">
              <div className="hero-eyebrow-tag">
                <Terminal size={13} strokeWidth={2} />
                <span>HỌC VIỆN KỸ THUẬT & QUẢN TRỊ THỰC CHIẾN</span>
              </div>

              <h1 className="hero-title">
                Nền tảng kiến thức chuyên sâu cho kỹ sư và nhà quản trị.
              </h1>

              <p className="hero-subtitle">
                Giáo trình biên soạn bài bản theo tiêu chuẩn doanh nghiệp. Học qua dự án mã nguồn thực tế, thảo luận trực tiếp 1-1 cùng giảng viên và nhận chứng chỉ định danh.
              </p>

              {/* Interactive Live Search Input */}
              <div className="hero-search-box">
                <LiveCourseSearchBar variant="hero" placeholder="Tìm khóa học: Spring Boot, Next.js, Kubernetes, Tài chính..." />
              </div>

              {/* Quick Topic Pills */}
              <div className="hero-quick-topics">
                <span className="quick-topics-label">Chủ đề trọng tâm:</span>
                <button type="button" onClick={() => navigate('/courses?search=Spring+Boot')} className="topic-chip">
                  Spring Boot 3
                </button>
                <button type="button" onClick={() => navigate('/courses?search=React')} className="topic-chip">
                  React & Next.js
                </button>
                <button type="button" onClick={() => navigate('/courses?search=Kubernetes')} className="topic-chip">
                  Docker & K8s
                </button>
                <button type="button" onClick={() => navigate('/courses?search=UI')} className="topic-chip">
                  Design System
                </button>
              </div>
            </div>

            {/* Right Architectural Dossier Card (No Stock Photo Cliché) */}
            <div className="hero-dossier-panel">
              <div className="dossier-card">
                <div className="dossier-header">
                  <span className="dossier-code">SYLLABUS // EDUMY-2026</span>
                  <span className="dossier-status">
                    <span className="status-dot"></span> Đang mở ghi danh
                  </span>
                </div>

                <div className="dossier-featured-course">
                  <div className="dossier-course-meta">LỘ TRÌNH KỸ SƯ HỆ THỐNG</div>
                  <h3 className="dossier-course-title">
                    Kiến trúc Phần mềm, Cloud-Native & Quản trị Sản phẩm Toàn diện
                  </h3>
                  <p className="dossier-course-desc">
                    Từ thiết kế hệ thống phân tán, bảo mật thanh toán đến vận hành hạ tầng thực tế.
                  </p>
                </div>

                <div className="dossier-modules-list">
                  <div className="dossier-module-row">
                    <span className="module-idx">01</span>
                    <div className="module-info">
                      <strong>Backend & Kiến trúc Modulith</strong>
                      <span>Java Spring Boot, SQL Server, REST API</span>
                    </div>
                    <span className="module-hours">32.5h</span>
                  </div>

                  <div className="dossier-module-row">
                    <span className="module-idx">02</span>
                    <div className="module-info">
                      <strong>Frontend & Hệ thống Thiết kế</strong>
                      <span>React 18, TypeScript, Design Tokens</span>
                    </div>
                    <span className="module-hours">28.0h</span>
                  </div>

                  <div className="dossier-module-row">
                    <span className="module-idx">03</span>
                    <div className="module-info">
                      <strong>DevOps, CI/CD & Giám sát</strong>
                      <span>Docker, Kubernetes, Prometheus, Grafana</span>
                    </div>
                    <span className="module-hours">24.0h</span>
                  </div>
                </div>

                <div className="dossier-footer">
                  <div className="dossier-cert-badge">
                    <Award size={16} strokeWidth={1.75} />
                    <span>Cấp chứng chỉ PDF xác thực mã hồ sơ</span>
                  </div>
                  <Link to="/courses" className="dossier-cta-link">
                    Xem danh mục <ArrowRight size={14} strokeWidth={2} />
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          2. ARCHITECTURAL LEDGER METRICS BAR (Tabular Numerals)
          -------------------------------------------------------------------- */}
      <section className="stats-banner-section">
        <div className="container">
          <div className="stats-grid">
            <div className="stat-box">
              <span className="stat-index">01 / GIÁO TRÌNH</span>
              <h3 className="stat-number">45+</h3>
              <p className="stat-label">Khóa học chuyên sâu đã xuất bản</p>
            </div>
            <div className="stat-box">
              <span className="stat-index">02 / CHUYÊN GIA</span>
              <h3 className="stat-number">100%</h3>
              <p className="stat-label">Giảng viên có kinh nghiệm thực tế</p>
            </div>
            <div className="stat-box">
              <span className="stat-index">03 / HỖ TRỢ HỌC TẬP</span>
              <h3 className="stat-number">&lt; 24h</h3>
              <p className="stat-label">Cam kết phản hồi câu hỏi Q&amp;A</p>
            </div>
            <div className="stat-box">
              <span className="stat-index">04 / ĐÁNH GIÁ</span>
              <h3 className="stat-number">4.8/5</h3>
              <p className="stat-label">Điểm hài lòng trung bình từ học viên</p>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          3. CATEGORIES INDEX SECTION
          -------------------------------------------------------------------- */}
      <section className="categories-section">
        <div className="container">
          <div className="section-header-flex">
            <div>
              <span className="section-eyebrow">CHUYÊN NGÀNH ĐÀO TẠO</span>
              <h2 className="section-title">Danh mục lĩnh vực</h2>
            </div>
            <Link to="/courses" className="view-all-link">
              Toàn bộ khóa học <ArrowRight size={15} strokeWidth={1.75} />
            </Link>
          </div>

          <div className="categories-grid">
            {categories.length > 0 ? (
              categories.map((cat, idx) => (
                <Link
                  key={cat.categoryId || cat.id}
                  to={`/courses?categoryId=${cat.categoryId || cat.id}`}
                  className="category-card"
                >
                  <div className="category-card-top">
                    <span className="category-index">0{idx + 1}</span>
                    <div className="category-icon-box">
                      {getCategoryIcon(cat.name || cat.categoryName)}
                    </div>
                  </div>
                  <h4 className="category-name">{cat.name || cat.categoryName}</h4>
                  <p className="category-desc line-clamp-2">
                    {cat.description || "Giáo trình bài bản từ nền tảng cốt lõi đến triển khai dự án thực tế."}
                  </p>
                </Link>
              ))
            ) : (
              [1, 2, 3, 4].map((n) => (
                <div key={n} className="category-card skeleton-box" style={{ height: '148px' }} />
              ))
            )}
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          4. PERSONALIZED RECOMMENDATIONS (IF LOGGED IN)
          -------------------------------------------------------------------- */}
      {user && user.role === 'Student' && recommendedCourses.length > 0 && (
        <section className="courses-section recommended-section-border">
          <div className="container">
            <div className="section-header-flex">
              <div>
                <span className="section-eyebrow">LỘ TRÌNH CÁ NHÂN HÓA</span>
                <h2 className="section-title">Đề xuất theo lịch sử học tập của bạn</h2>
              </div>
              <Link to="/courses" className="view-all-link">
                Xem thêm <ArrowRight size={15} strokeWidth={1.75} />
              </Link>
            </div>

            <div className="courses-grid">
              {recommendedCourses.slice(0, 4).map((course) => (
                <CourseCard key={course.courseId || course.id} course={course} />
              ))}
            </div>
          </div>
        </section>
      )}

      {/* --------------------------------------------------------------------
          5. POPULAR COURSES SECTION
          -------------------------------------------------------------------- */}
      <section className="courses-section">
        <div className="container">
          <div className="section-header-flex">
            <div>
              <span className="section-eyebrow">ĐƯỢC ĐĂNG KÝ NHIỀU NHẤT</span>
              <h2 className="section-title">Khóa học tiêu biểu</h2>
            </div>
            <Link to="/courses?sortBy=rating" className="view-all-link">
              Xem tất cả <ArrowRight size={15} strokeWidth={1.75} />
            </Link>
          </div>

          <div className="courses-grid">
            {!loading && popularCourses.length > 0 ? (
              popularCourses.map((course) => (
                <CourseCard key={course.courseId || course.id} course={course} />
              ))
            ) : (
              [1, 2, 3, 4].map((n) => (
                <div key={n} className="skeleton-box" style={{ height: '350px', borderRadius: '10px' }} />
              ))
            )}
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          6. NEWEST COURSES SECTION
          -------------------------------------------------------------------- */}
      <section className="courses-section bg-surface-alt">
        <div className="container">
          <div className="section-header-flex">
            <div>
              <span className="section-eyebrow">MỚI CẬP NHẬT</span>
              <h2 className="section-title">Giáo trình mới phát hành</h2>
            </div>
            <Link to="/courses?sortBy=newest" className="view-all-link">
              Xem thêm <ArrowRight size={15} strokeWidth={1.75} />
            </Link>
          </div>

          <div className="courses-grid">
            {!loading && newestCourses.length > 0 ? (
              newestCourses.map((course) => (
                <CourseCard key={course.courseId || course.id} course={course} />
              ))
            ) : (
              [1, 2, 3, 4].map((n) => (
                <div key={n} className="skeleton-box" style={{ height: '350px', borderRadius: '10px' }} />
              ))
            )}
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          7. ASYMMETRIC BENTO GRID (Replaces Generic 3-Card Row)
          -------------------------------------------------------------------- */}
      <section className="bento-features-section">
        <div className="container">
          <div className="section-header-flex">
            <div>
              <span className="section-eyebrow">TIÊU CHUẨN ĐÀO TẠO</span>
              <h2 className="section-title">Thiết kế cho việc học tập nghiêm túc</h2>
            </div>
          </div>

          <div className="editorial-bento-grid">
            {/* Bento Cell 1: Large Span */}
            <div className="bento-cell bento-span-large">
              <div className="bento-top-meta">
                <span className="bento-tag">01 // PHÒNG HỌC TƯƠNG TÁC</span>
                <BookOpen size={18} strokeWidth={1.75} className="bento-icon" />
              </div>
              <h3 className="bento-title">Trình phát bài giảng Cinema & Ghi chú theo mốc thời gian</h3>
              <p className="bento-desc">
                Môi trường học tập không xao nhãng với giao diện tối chuẩn điện ảnh. Lưu ghi chú cá nhân gắn trực tiếp vào từng giây của video bài giảng và xuất toàn bộ kiến thức ra tệp văn bản bất cứ lúc nào.
              </p>
              <div className="bento-spec-strip">
                <span><CheckCircle2 size={14} /> Tua nhanh theo mốc Note</span>
                <span><CheckCircle2 size={14} /> Tài liệu đính kèm từng chương</span>
                <span><CheckCircle2 size={14} /> Đồng bộ tiến độ tự động</span>
              </div>
            </div>

            {/* Bento Cell 2 */}
            <div className="bento-cell">
              <div className="bento-top-meta">
                <span className="bento-tag">02 // HỎI ĐÁP SLA</span>
                <MessageSquare size={18} strokeWidth={1.75} className="bento-icon" />
              </div>
              <h3 className="bento-title">Hàng đợi Hỏi &amp; Đáp ưu tiên câu hỏi cũ nhất</h3>
              <p className="bento-desc">
                Mỗi câu hỏi tại bài học được đưa vào hàng đợi ưu tiên (Oldest-First Queue) của giảng viên và thông báo qua email ngay khi có lời giải đáp.
              </p>
            </div>

            {/* Bento Cell 3 */}
            <div className="bento-cell">
              <div className="bento-top-meta">
                <span className="bento-tag">03 // KIỂM TRA NĂNG LỰC</span>
                <Compass size={18} strokeWidth={1.75} className="bento-icon" />
              </div>
              <h3 className="bento-title">Bài tập trắc nghiệm &amp; Đánh giá sau mỗi chương</h3>
              <p className="bento-desc">
                Kiểm chứng mức độ hiểu bài ngay tại phòng học thông qua hệ thống câu hỏi kiểm tra kiến thức và giải thích đáp án chi tiết.
              </p>
            </div>

            {/* Bento Cell 4: Wide Span */}
            <div className="bento-cell bento-span-wide">
              <div className="bento-top-meta">
                <span className="bento-tag">04 // CHỨNG CHỈ XÁC THỰC</span>
                <FileCheck2 size={18} strokeWidth={1.75} className="bento-icon" />
              </div>
              <h3 className="bento-title">Chứng chỉ hoàn thành kết xuất PDF chất lượng in ấn</h3>
              <p className="bento-desc">
                Khi đạt 100% tiến độ khóa học, hệ thống tự động cấp chứng chỉ điện tử định dạng PDF với mã số xác thực duy nhất để đính kèm vào hồ sơ năng lực (CV / LinkedIn).
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          8. EDITORIAL TESTIMONIALS LEDGER
          -------------------------------------------------------------------- */}
      <section className="testimonials-section">
        <div className="container">
          <div className="section-header-flex">
            <div>
              <span className="section-eyebrow">PHẢN HỒI THỰC TẾ</span>
              <h2 className="section-title">Chia sẻ từ học viên đã hoàn thành</h2>
            </div>
          </div>

          <div className="testimonials-grid">
            <div className="testimonial-card">
              <div>
                <div className="testimonial-stars">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} size={14} fill="#d97706" color="#d97706" />
                  ))}
                </div>
                <p className="testimonial-quote">
                  "Khóa học Spring Boot &amp; Microservices tại Edumy phân tích rất kỹ về ranh giới module và thiết kế cơ sở dữ liệu. Tôi đã áp dụng trực tiếp cấu trúc này vào dự án tại công ty."
                </p>
              </div>
              <div className="testimonial-author">
                <img
                  src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&q=80"
                  alt="Trần Minh Trang"
                  className="author-avatar"
                />
                <div>
                  <h4 className="author-name">Trần Minh Trang</h4>
                  <p className="author-role">Backend Engineer · FinTech</p>
                </div>
              </div>
            </div>

            <div className="testimonial-card">
              <div>
                <div className="testimonial-stars">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} size={14} fill="#d97706" color="#d97706" />
                  ))}
                </div>
                <p className="testimonial-quote">
                  "Tính năng Hỏi &amp; Đáp ngay dưới từng bài giảng cực kỳ hữu ích. Khi tôi gặp lỗi cấu hình Docker, giảng viên đã phản hồi chi tiết chỉ sau vài giờ và có thông báo về tận email."
                </p>
              </div>
              <div className="testimonial-author">
                <img
                  src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80"
                  alt="Lê Hoàng Nam"
                  className="author-avatar"
                />
                <div>
                  <h4 className="author-name">Lê Hoàng Nam</h4>
                  <p className="author-role">Frontend Developer · VNG</p>
                </div>
              </div>
            </div>

            <div className="testimonial-card">
              <div>
                <div className="testimonial-stars">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} size={14} fill="#d97706" color="#d97706" />
                  ))}
                </div>
                <p className="testimonial-quote">
                  "Giáo trình UI/UX &amp; Design System đi sâu vào kiến trúc Token 3 lớp thực tế thay vì chỉ dạy công cụ bề nổi. Tài liệu đính kèm và bài kiểm tra rất sát với công việc."
                </p>
              </div>
              <div className="testimonial-author">
                <img
                  src="https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&q=80"
                  alt="Nguyễn Thảo Vy"
                  className="author-avatar"
                />
                <div>
                  <h4 className="author-name">Nguyễn Thảo Vy</h4>
                  <p className="author-role">Product Designer · MoMo</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          9. CALL TO ACTION (CTA)
          -------------------------------------------------------------------- */}
      <section className="container mb-5">
        <div className="cta-banner">
          <div className="cta-content">
            <span className="cta-eyebrow">BẮT ĐẦU HÀNH TRÌNH</span>
            <h2>Tra cứu giáo trình và học thử miễn phí ngay hôm nay.</h2>
            <p>
              Truy cập ngay các bài giảng mở trước (Preview) trong từng khóa học trước khi quyết định đăng ký.
            </p>
          </div>
          <div className="cta-actions">
            <Link to="/courses" className="btn-cta-primary">
              Khám phá danh mục <ArrowRight size={16} strokeWidth={2} />
            </Link>
            <Link to="/courses?priceType=FREE" className="btn-cta-secondary">
              Khóa học Miễn phí (0đ)
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}

export default HomePage;
