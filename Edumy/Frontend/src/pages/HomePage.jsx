import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../api/axiosConfig';
import { courseApi } from '../api/modules/courseApi';
import { mlApi } from '../api/modules/mlApi';
import CourseCard from '../components/CourseCard';
import {
  Search,
  Sparkles,
  TrendingUp,
  Clock,
  ArrowRight,
  Award,
  BookOpen,
  Users,
  ShieldCheck,
  Star,
  CheckCircle2,
  Code,
  Palette,
  BrainCircuit,
  BarChart3,
  Layers,
  GraduationCap
} from 'lucide-react';
import LiveCourseSearchBar from '../components/LiveCourseSearchBar';
import './HomePage.css';

// Dynamic category icon mapper
const getCategoryIcon = (categoryName = '') => {
  const name = categoryName.toLowerCase();
  if (name.includes('lập trình') || name.includes('dev') || name.includes('code')) return <Code size={24} />;
  if (name.includes('thiết kế') || name.includes('design') || name.includes('ui')) return <Palette size={24} />;
  if (name.includes('ai') || name.includes('dữ liệu') || name.includes('data')) return <BrainCircuit size={24} />;
  if (name.includes('kinh doanh') || name.includes('marketing') || name.includes('tài chính')) return <BarChart3 size={24} />;
  return <Layers size={24} />;
};

function HomePage() {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();

  const [searchTerm, setSearchTerm] = useState('');
  const [categories, setCategories] = useState([]);
  const [popularCourses, setPopularCourses] = useState([]);
  const [newestCourses, setNewestCourses] = useState([]);
  const [recommendedCourses, setRecommendedCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchHomeData = async () => {
      setLoading(true);
      try {
        // 1. Fetch Categories
        const catRes = await courseApi.getCategories();
        const catData = Array.isArray(catRes.data) ? catRes.data : (catRes.data?.items || []);
        setCategories(Array.isArray(catData) ? catData.slice(0, 8) : []);

        // 2. Fetch Popular Courses
        const popularRes = await courseApi.getCourses({ pageSize: 8, sortBy: 'rating', sortOrder: 'desc' });
        const popularData = Array.isArray(popularRes.data?.items) ? popularRes.data.items : (Array.isArray(popularRes.data) ? popularRes.data : []);
        setPopularCourses(popularData);

        // 3. Fetch Newest Courses
        const newestRes = await courseApi.getCourses({ pageSize: 4, sortBy: 'newest', sortOrder: 'desc' });
        const newestData = Array.isArray(newestRes.data?.items) ? newestRes.data.items : (Array.isArray(newestRes.data) ? newestRes.data : []);
        setNewestCourses(newestData);

        // 4. Fetch AI Recommendations if Student is logged in
        if (user && user.role === 'Student') {
          try {
            const recRes = await mlApi.getStudentRecommendations();
            const recData = Array.isArray(recRes.data) ? recRes.data : (recRes.data?.items || []);
            setRecommendedCourses(Array.isArray(recData) ? recData : []);
          } catch (recErr) {
            console.warn('AI recommendations not available yet:', recErr);
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

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/courses?search=${encodeURIComponent(searchTerm.trim())}`);
    } else {
      navigate('/courses');
    }
  };

  return (
    <div className="homepage-container">
      {/* --------------------------------------------------------------------
          1. HERO BANNER
          -------------------------------------------------------------------- */}
      <section className="edumy-hero-section">
        <div className="container">
          <div className="hero-grid">
            <div className="hero-text-content">
              <div className="hero-badge-pill">
                <Sparkles size={16} className="text-warning" />
                <span>Nền tảng đào tạo công nghệ hàng đầu</span>
              </div>

              <h1 className="hero-title">
                Khai mở tiềm năng.<br />
                <span className="gradient-text">Nâng tầm sự nghiệp.</span>
              </h1>

              <p className="hero-subtitle">
                Tiếp cận các khóa học thực chiến từ chuyên gia hàng đầu. Hệ thống gợi ý AI cá nhân hóa giúp bạn xây dựng lộ trình học tập hiệu quả nhất.
              </p>

              {/* Interactive Live Search Input */}
              <div className="hero-search-box">
                <LiveCourseSearchBar variant="hero" placeholder="Tìm kiếm khóa học theo công nghệ, kỹ năng..." />
              </div>

              {/* Trust Badges Bar */}
              <div className="hero-trust-metrics">
                <div className="trust-item">
                  <GraduationCap size={18} className="trust-icon" />
                  <span>20,000+ Học viên</span>
                </div>
                <div className="trust-divider">•</div>
                <div className="trust-item">
                  <Award size={18} className="trust-icon" />
                  <span>Chứng chỉ chuẩn quốc tế</span>
                </div>
                <div className="trust-divider">•</div>
                <div className="trust-item">
                  <Star size={18} className="trust-icon text-warning" />
                  <span>4.8/5 Đánh giá hài lòng</span>
                </div>
              </div>
            </div>

            <div className="hero-visual-card">
              <div className="visual-image-wrapper">
                <img
                  src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1000&q=80"
                  alt="Học viên Edumy"
                  className="hero-main-img"
                  loading="eager"
                />
                {/* Floating Metric Pill 1 */}
                <div className="floating-metric-card top-left">
                  <div className="metric-icon-box bg-primary-soft">
                    <BookOpen size={20} className="text-primary" />
                  </div>
                  <div>
                    <span className="metric-number">500+</span>
                    <span className="metric-label">Khóa học chuyên sâu</span>
                  </div>
                </div>

                {/* Floating Metric Pill 2 */}
                <div className="floating-metric-card bottom-right">
                  <div className="metric-icon-box bg-success-soft">
                    <CheckCircle2 size={20} className="text-success" />
                  </div>
                  <div>
                    <span className="metric-number">95%</span>
                    <span className="metric-label">Tỷ lệ hoàn thành</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          2. STATS OVERVIEW SECTION
          -------------------------------------------------------------------- */}
      <section className="stats-banner-section">
        <div className="container">
          <div className="stats-grid">
            <div className="stat-box">
              <h3 className="stat-number">500+</h3>
              <p className="stat-label">Khóa học từ cơ bản đến nâng cao</p>
            </div>
            <div className="stat-box">
              <h3 className="stat-number">120+</h3>
              <p className="stat-label">Giảng viên & Chuyên gia công nghệ</p>
            </div>
            <div className="stat-box">
              <h3 className="stat-number">25,000+</h3>
              <p className="stat-label">Giờ video bài giảng chất lượng cao</p>
            </div>
            <div className="stat-box">
              <h3 className="stat-number">98%</h3>
              <p className="stat-label">Học viên tự tin ứng tuyển thành công</p>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          3. CATEGORIES SECTION
          -------------------------------------------------------------------- */}
      <section className="categories-section">
        <div className="container">
          <div className="section-header-flex">
            <div>
              <span className="section-eyebrow">Khám phá theo lĩnh vực</span>
              <h2 className="section-title">Danh mục nổi bật</h2>
            </div>
            <Link to="/courses" className="view-all-link">
              Xem tất cả danh mục <ArrowRight size={16} />
            </Link>
          </div>

          <div className="categories-grid">
            {categories.length > 0 ? (
              categories.map((cat) => (
                <Link
                  key={cat.categoryId || cat.id}
                  to={`/courses?categoryId=${cat.categoryId || cat.id}`}
                  className="category-card"
                >
                  <div className="category-icon-box">
                    {getCategoryIcon(cat.name || cat.categoryName)}
                  </div>
                  <h4 className="category-name">{cat.name || cat.categoryName}</h4>
                  <p className="category-desc line-clamp-2">
                    {cat.description || "Nắm vững kỹ năng thực tế với các dự án thực chiến chuẩn doanh nghiệp."}
                  </p>
                </Link>
              ))
            ) : (
              [1, 2, 3, 4].map((n) => (
                <div key={n} className="category-card skeleton-box" style={{ height: '140px' }} />
              ))
            )}
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          4. AI PERSONALIZED RECOMMENDATIONS (IF LOGGED IN)
          -------------------------------------------------------------------- */}
      {user && user.role === 'Student' && recommendedCourses.length > 0 && (
        <section className="courses-section ai-recommended-section">
          <div className="container">
            <div className="section-header-flex">
              <div>
                <div className="d-flex align-items-center gap-2 mb-1">
                  <Sparkles size={18} className="text-primary" />
                  <span className="section-eyebrow text-primary fw-bold">Gợi ý dành riêng cho bạn</span>
                </div>
                <h2 className="section-title">Khóa học được AI đề xuất</h2>
              </div>
              <Link to="/courses" className="view-all-link">
                Xem thêm lộ trình <ArrowRight size={16} />
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
              <span className="section-eyebrow">Học viên tin chọn</span>
              <h2 className="section-title">Khóa học bán chạy nhất</h2>
            </div>
            <Link to="/courses?sortBy=rating" className="view-all-link">
              Xem tất cả <ArrowRight size={16} />
            </Link>
          </div>

          <div className="courses-grid">
            {popularCourses.length > 0 ? (
              popularCourses.map((course) => (
                <CourseCard key={course.courseId || course.id} course={course} />
              ))
            ) : (
              [1, 2, 3, 4].map((n) => (
                <div key={n} className="skeleton-box" style={{ height: '360px', borderRadius: '16px' }} />
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
              <span className="section-eyebrow">Mới cập nhật</span>
              <h2 className="section-title">Khóa học mới phát hành</h2>
            </div>
            <Link to="/courses?sortBy=newest" className="view-all-link">
              Xem thêm <ArrowRight size={16} />
            </Link>
          </div>

          <div className="courses-grid">
            {newestCourses.length > 0 ? (
              newestCourses.map((course) => (
                <CourseCard key={course.courseId || course.id} course={course} />
              ))
            ) : (
              [1, 2, 3, 4].map((n) => (
                <div key={n} className="skeleton-box" style={{ height: '360px', borderRadius: '16px' }} />
              ))
            )}
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          7. VALUE PROPOSITION / FEATURES SECTION
          -------------------------------------------------------------------- */}
      <section className="features-section">
        <div className="container">
          <div className="text-center max-w-700 mx-auto mb-5">
            <span className="section-eyebrow">Lý do chọn Edumy</span>
            <h2 className="section-title">Trải nghiệm học tập vượt trội</h2>
            <p className="section-subtitle">
              Được thiết kế tối ưu cho sinh viên và kỹ sư công nghệ muốn học nhanh, thực chiến và nắm bắt công việc thực tế.
            </p>
          </div>

          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon-box bg-primary-soft text-primary">
                <BrainCircuit size={28} />
              </div>
              <h4 className="feature-title">Gợi ý lộ trình bằng AI</h4>
              <p className="feature-desc">
                Mô hình học máy phân tích kỹ năng và hành vi học tập để đề xuất khóa học phù hợp với từng giai đoạn phát triển sự nghiệp của bạn.
              </p>
            </div>

            <div className="feature-card">
              <div className="feature-icon-box bg-success-soft text-success">
                <Code size={28} />
              </div>
              <h4 className="feature-title">Bài tập & Quiz thực hành</h4>
              <p className="feature-desc">
                Không chỉ xem video lý thuyết, bạn được thử thách ngay với hệ thống Quiz tương tác và dự án thực chiến chuẩn môi trường doanh nghiệp.
              </p>
            </div>

            <div className="feature-card">
              <div className="feature-icon-box bg-warning-soft text-warning">
                <Award size={28} />
              </div>
              <h4 className="feature-title">Chứng chỉ xác thực độc quyền</h4>
              <p className="feature-desc">
                Nhận chứng chỉ điện tử có mã định danh xác thực ngay khi hoàn thành 100% giáo trình và bài kiểm tra đánh giá năng lực.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          8. TESTIMONIALS SECTION
          -------------------------------------------------------------------- */}
      <section className="testimonials-section">
        <div className="container">
          <div className="text-center max-w-700 mx-auto mb-5">
            <span className="section-eyebrow">Câu chuyện thành công</span>
            <h2 className="section-title">Học viên nói gì về chúng tôi</h2>
            <p className="section-subtitle">
              Lắng nghe chia sẻ từ những học viên đã bứt phá sự nghiệp tại Edumy.
            </p>
          </div>

          <div className="testimonials-grid">
            <div className="testimonial-card">
              <div>
                <div className="testimonial-stars">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} size={16} fill="currentColor" />
                  ))}
                </div>
                <p className="testimonial-quote">
                  "Khóa học Spring Boot & Microservices tại Edumy rất chi tiết và bám sát kiến trúc thực tế. Tôi đã tự tin vượt qua vòng phỏng vấn kỹ thuật và nhận offer Full-stack Developer chỉ sau 3 tháng học."
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
                  <p className="author-role">Backend Engineer tại FinTech</p>
                </div>
              </div>
            </div>

            <div className="testimonial-card">
              <div>
                <div className="testimonial-stars">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} size={16} fill="currentColor" />
                  ))}
                </div>
                <p className="testimonial-quote">
                  "Player học tập của Edumy cực kỳ mượt mà, video không bị giật lag và hệ thống ghi chú theo từng mốc thời gian giúp việc ôn lại kiến thức trở nên tiện lợi vô cùng."
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
                  <p className="author-role">Frontend Developer tại VNG</p>
                </div>
              </div>
            </div>

            <div className="testimonial-card">
              <div>
                <div className="testimonial-stars">
                  {[...Array(5)].map((_, i) => (
                    <Star key={i} size={16} fill="currentColor" />
                  ))}
                </div>
                <p className="testimonial-quote">
                  "Các bài tập Quiz và tài liệu thực hành đính kèm rất chỉn chu. Tính năng AI gợi ý khóa học đề xuất đúng những kỹ năng mà tôi đang còn thiếu để hoàn thiện hồ sơ."
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
                  <p className="author-role">Product Designer tại MoMo</p>
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
            <h2>Sẵn sàng bứt phá sự nghiệp ngay hôm nay?</h2>
            <p>
              Gia nhập cùng hơn 20,000 học viên tại Edumy để tiếp cận kho tàng tri thức công nghệ hiện đại nhất.
            </p>
          </div>
          <div className="cta-actions">
            <Link to="/courses" className="btn-cta-primary">
              Khám phá khóa học <ArrowRight size={18} />
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
