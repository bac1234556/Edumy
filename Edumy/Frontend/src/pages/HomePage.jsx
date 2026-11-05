import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../api/axiosConfig';
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
        const catRes = await api.get('/categories');
        const catData = Array.isArray(catRes.data) ? catRes.data : (catRes.data?.items || []);
        setCategories(Array.isArray(catData) ? catData.slice(0, 8) : []);

        // 2. Fetch Popular Courses
        const popularRes = await api.get('/courses?pageSize=8&sortBy=rating&sortOrder=desc');
        const popularData = Array.isArray(popularRes.data?.items) ? popularRes.data.items : (Array.isArray(popularRes.data) ? popularRes.data : []);
        setPopularCourses(popularData);

        // 3. Fetch Newest Courses
        const newestRes = await api.get('/courses?pageSize=4&sortBy=newest&sortOrder=desc');
        const newestData = Array.isArray(newestRes.data?.items) ? newestRes.data.items : (Array.isArray(newestRes.data) ? newestRes.data : []);
        setNewestCourses(newestData);

        // 4. Fetch AI Recommendations if Student is logged in
        if (user && user.role === 'Student') {
          try {
            const recRes = await api.get('/courses/recommend');
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
                <span>Nền tảng đào tạo công nghệ hàng đầu 2026</span>
              </div>

              <h1 className="hero-title">
                Khai mở tiềm năng. <br />
                <span className="gradient-text">Nâng tầm sự nghiệp.</span>
              </h1>

              <p className="hero-subtitle">
                Tiếp cận các khóa học thực chiến từ chuyên gia hàng đầu. Hệ thống gợi ý AI cá nhân hóa giúp bạn xây dựng lộ trình học tập hiệu quả nhất.
              </p>

              {/* Quick Search Bar with Live Instant Suggestion */}
              <div className="hero-search-wrapper" style={{ width: '100%', maxWidth: '640px' }}>
                <LiveCourseSearchBar 
                  variant="hero"
                  placeholder="Bạn muốn làm chủ kỹ năng gì hôm nay? (VD: React, Python, UI/UX...)" 
                />
              </div>

              {/* Trending Tags */}
              <div className="trending-tags">
                <span className="fw-semibold">Xu hướng:</span>
                <Link to="/courses?search=React" className="trending-tag-link">#ReactJS</Link>
                <Link to="/courses?search=AI" className="trending-tag-link">#Machine Learning</Link>
                <Link to="/courses?search=Spring" className="trending-tag-link">#Spring Boot</Link>
                <Link to="/courses?search=Design" className="trending-tag-link">#Figma UI/UX</Link>
              </div>
            </div>

            {/* Hero Visual Card */}
            <div className="hero-visual-card d-none d-lg-block">
              <div className="hero-image-frame">
                <img
                  src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1000&q=80"
                  alt="Sinh viên học tập cùng Edumy"
                  className="hero-main-img"
                />
              </div>

              {/* Floating Stat 1 */}
              <div className="hero-floating-stat stat-left">
                <div className="stat-icon-circle indigo">
                  <GraduationCap size={22} />
                </div>
                <div>
                  <div className="stat-value">20,000+</div>
                  <div className="stat-label">Học viên đang học</div>
                </div>
              </div>

              {/* Floating Stat 2 */}
              <div className="hero-floating-stat stat-right">
                <div className="stat-icon-circle amber">
                  <Star size={20} fill="#f59e0b" color="#f59e0b" />
                </div>
                <div>
                  <div className="stat-value">4.9 / 5.0</div>
                  <div className="stat-label">Đánh giá xuất sắc</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          2. TRUST STRIP / SOCIAL PROOF
          -------------------------------------------------------------------- */}
      <section className="trust-strip">
        <div className="container">
          <div className="trust-grid">
            <div className="trust-item">
              <div className="trust-icon-box">
                <BookOpen size={22} />
              </div>
              <div>
                <div className="trust-text-title">500+ Khóa học chuyên sâu</div>
                <div className="trust-text-sub">Cập nhật xu hướng công nghệ mới nhất</div>
              </div>
            </div>

            <div className="trust-item">
              <div className="trust-icon-box">
                <Users size={22} />
              </div>
              <div>
                <div className="trust-text-title">Giảng viên chuyên gia</div>
                <div className="trust-text-sub">Kinh nghiệm thực chiến từ doanh nghiệp lớn</div>
              </div>
            </div>

            <div className="trust-item">
              <div className="trust-icon-box">
                <ShieldCheck size={22} />
              </div>
              <div>
                <div className="trust-text-title">Chứng chỉ xác thực</div>
                <div className="trust-text-sub">Cấp chứng nhận hoàn thành chuẩn quốc tế</div>
              </div>
            </div>

            <div className="trust-item">
              <div className="trust-icon-box">
                <Sparkles size={22} />
              </div>
              <div>
                <div className="trust-text-title">Gợi ý lộ trình AI</div>
                <div className="trust-text-sub">Định hướng cá nhân hóa theo mục tiêu</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          3. AI RECOMMENDATION SECTION (WHEN LOGGED IN AS STUDENT)
          -------------------------------------------------------------------- */}
      {user && user.role === 'Student' && recommendedCourses.length > 0 && (
        <section className="home-section pb-0">
          <div className="container">
            <div className="ai-recommendation-box">
              <div className="ai-recommendation-header">
                <div>
                  <div className="ai-title-badge mb-2">
                    <Sparkles size={16} /> Gợi ý riêng cho bạn
                  </div>
                  <h2 className="fw-bold mb-1" style={{ fontSize: '1.6rem' }}>
                    Khóa học phù hợp với hồ sơ học tập của {user.fullName || 'bạn'}
                  </h2>
                  <p className="text-muted mb-0 small">
                    Thuật toán AI phân tích kỹ năng và đề xuất các bài giảng tối ưu nhất cho bạn.
                  </p>
                </div>
                <Link to="/courses" className="section-view-all">
                  Xem tất cả đề xuất <ArrowRight size={16} />
                </Link>
              </div>

              <div className="courses-grid">
                {recommendedCourses.slice(0, 4).map((course) => (
                  <CourseCard key={course.courseId} course={course} />
                ))}
              </div>
            </div>
          </div>
        </section>
      )}

      {/* --------------------------------------------------------------------
          4. TOP CATEGORIES
          -------------------------------------------------------------------- */}
      <section className="home-section">
        <div className="container">
          <div className="section-header-row">
            <div className="section-title-wrap">
              <h2>Khám phá theo Danh mục</h2>
              <p>Lựa chọn lĩnh vực bạn yêu thích và bắt đầu xây dựng nền tảng vững chắc.</p>
            </div>
            <Link to="/courses" className="section-view-all">
              Tất cả danh mục <ArrowRight size={16} />
            </Link>
          </div>

          <div className="categories-grid">
            {loading ? (
              [1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
                <div key={n} className="category-card skeleton-box" style={{ height: '94px' }}></div>
              ))
            ) : categories.length === 0 ? (
              <div className="col-12 text-center py-4 text-muted">Đang cập nhật danh mục...</div>
            ) : (
              categories.map((cat) => (
                <Link
                  to={`/courses?categoryId=${cat.categoryId}`}
                  key={cat.categoryId}
                  className="category-card"
                >
                  <div className="category-icon-wrapper">
                    {getCategoryIcon(cat.name)}
                  </div>
                  <div className="category-info">
                    <h4>{cat.name}</h4>
                    <span>Khám phá lộ trình →</span>
                  </div>
                </Link>
              ))
            )}
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          5. BESTSELLING & TOP RATED COURSES
          -------------------------------------------------------------------- */}
      <section className="home-section bg-alt">
        <div className="container">
          <div className="section-header-row">
            <div className="section-title-wrap">
              <h2>Khóa học Nổi bật & Bán chạy nhất</h2>
              <p>Những khóa học được cộng đồng học viên bình chọn cao nhất tháng này.</p>
            </div>
            <Link to="/courses?sortBy=rating" className="section-view-all">
              Xem toàn bộ <ArrowRight size={16} />
            </Link>
          </div>

          <div className="courses-grid">
            {loading ? (
              [1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
                <div key={n} className="skeleton-card">
                  <div className="skeleton-img skeleton-box"></div>
                  <div className="skeleton-line long skeleton-box"></div>
                  <div className="skeleton-line medium skeleton-box"></div>
                  <div className="skeleton-line short skeleton-box mt-auto"></div>
                </div>
              ))
            ) : popularCourses.length === 0 ? (
              <div className="col-12 text-center py-5 bg-white rounded-4 border p-4">
                <BookOpen size={40} className="text-muted opacity-50 mb-3" />
                <h5>Chưa có khóa học nào được đăng tải</h5>
                <p className="text-muted small">Hãy quay lại sau hoặc chuyển sang danh mục khác.</p>
              </div>
            ) : (
              popularCourses.map((course) => (
                <CourseCard key={course.courseId} course={course} />
              ))
            )}
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          6. NEWEST COURSES
          -------------------------------------------------------------------- */}
      <section className="home-section">
        <div className="container">
          <div className="section-header-row">
            <div className="section-title-wrap">
              <h2>Mới ra mắt gần đây</h2>
              <p>Bắt kịp kiến thức và công cụ hiện đại với những bài giảng vừa cập nhật.</p>
            </div>
            <Link to="/courses?sortBy=newest" className="section-view-all">
              Xem thêm khóa mới <ArrowRight size={16} />
            </Link>
          </div>

          <div className="courses-grid">
            {loading ? (
              [1, 2, 3, 4].map((n) => (
                <div key={n} className="skeleton-card">
                  <div className="skeleton-img skeleton-box"></div>
                  <div className="skeleton-line long skeleton-box"></div>
                  <div className="skeleton-line medium skeleton-box"></div>
                  <div className="skeleton-line short skeleton-box mt-auto"></div>
                </div>
              ))
            ) : newestCourses.length === 0 ? (
              <div className="col-12 text-center py-4 text-muted">Chưa có khóa học mới.</div>
            ) : (
              newestCourses.map((course) => (
                <CourseCard key={course.courseId} course={course} />
              ))
            )}
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          7. VALUE PROPOSITIONS (WHY EDUMY)
          -------------------------------------------------------------------- */}
      <section className="home-section bg-alt">
        <div className="container text-center">
          <div className="max-w-2xl mx-auto mb-5">
            <h2 className="fw-bold mb-3" style={{ fontSize: '2rem' }}>
              Tại sao hàng ngàn học viên chọn Edumy?
            </h2>
            <p className="text-muted">
              Chúng tôi cam kết mang lại trải nghiệm học tập hiện đại, trực quan và gắn liền với yêu cầu tuyển dụng thực tế.
            </p>
          </div>

          <div className="why-edumy-grid text-start">
            <div className="feature-box">
              <div className="feature-icon-box">
                <Code size={28} />
              </div>
              <h3>Lộ trình chuẩn Doanh nghiệp</h3>
              <p>
                Giáo trình được thiết kế bài bản từ cơ bản đến chuyên sâu, tích hợp các dự án thực tế giúp bạn tự tin xây dựng portfolio.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-icon-box">
                <Sparkles size={28} />
              </div>
              <h3>Trải nghiệm học tập thông minh</h3>
              <p>
                Hệ thống video chất lượng cao hỗ trợ ghi chú cá nhân, kiểm tra kiến thức qua Quiz tương tác và theo dõi tiến độ thời gian thực.
              </p>
            </div>

            <div className="feature-box">
              <div className="feature-icon-box">
                <Award size={28} />
              </div>
              <h3>Chứng nhận uy tín tức thì</h3>
              <p>
                Nhận chứng chỉ số có mã xác thực duy nhất ngay khi hoàn thành 100% bài giảng để bổ sung trực tiếp vào CV hoặc LinkedIn.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------------------------------
          8. TESTIMONIALS (SOCIAL PROOF)
          -------------------------------------------------------------------- */}
      <section className="home-section">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="fw-bold mb-3" style={{ fontSize: '2rem' }}>
              Cảm nhận từ Học viên xuất sắc
            </h2>
            <p className="text-muted">
              Lắng nghe câu chuyện chuyển mình và thăng tiến nghề nghiệp của các bạn học viên tại Edumy.
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
            <Link to="/instructor" className="btn-cta-secondary">
              Trở thành Giảng viên
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}

export default HomePage;
