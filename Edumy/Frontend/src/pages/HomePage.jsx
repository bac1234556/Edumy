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
                <BookOpen size={14} strokeWidth={2} aria-hidden="true" />
                <span>NỀN TẢNG HỌC TẬP TRỰC TUYẾN EDUMY</span>
              </div>

              <h1 className="hero-title">
                Nâng tầm kỹ năng chuyên môn cùng chuyên gia thực chiến.
              </h1>

              <p className="hero-subtitle">
                Khám phá kho khóa học công nghệ, thiết kế và quản trị bài bản — học mọi lúc, thực hành qua dự án và nhận chứng chỉ hoàn thành.
              </p>

              {/* Interactive Live Search Input */}
              <div className="hero-search-box">
                <LiveCourseSearchBar variant="hero" placeholder="Bạn muốn học kỹ năng gì hôm nay?" />
              </div>

              {/* Primary & Secondary CTAs */}
              <div className="hero-cta-group">
                <Link to="/courses" className="hero-primary-cta">
                  <span>Khám phá khóa học</span>
                  <ArrowRight size={16} strokeWidth={2} aria-hidden="true" />
                </Link>
                <Link to="/courses?priceType=FREE" className="hero-secondary-cta">
                  <span>Khóa học miễn phí</span>
                </Link>
              </div>

              {/* Quick Topic Pills */}
              <div className="hero-quick-topics">
                <span className="quick-topics-label">Chủ đề phổ biến:</span>
                <button type="button" onClick={() => navigate('/courses?search=Spring+Boot')} className="topic-chip">
                  Spring Boot
                </button>
                <button type="button" onClick={() => navigate('/courses?search=React')} className="topic-chip">
                  React &amp; Next.js
                </button>
                <button type="button" onClick={() => navigate('/courses?search=Docker')} className="topic-chip">
                  Docker &amp; K8s
                </button>
                <button type="button" onClick={() => navigate('/courses?search=UI')} className="topic-chip">
                  UI/UX Design
                </button>
              </div>
            </div>

            {/* Right Clean Learning Visual (No fake stats/widgets) */}
            <div className="hero-visual-panel">
              <img
                src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1000&q=80"
                alt="Học viên cùng thảo luận và học tập trực tuyến trên Edumy"
                className="hero-visual-image"
              />
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          2. CATEGORIES INDEX SECTION
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
