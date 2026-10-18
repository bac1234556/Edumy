-- ====================================================================================
-- DỰ ÁN: EDUMY ELEARNING PLATFORM (Spring Boot 3.2 + MSSQL + React 18)
-- KỊCH BẢN NẠP SEED DATA HOÀN CHỈNH: 41 KHÓA HỌC, 81 CHƯƠNG, 159 BÀI HỌC, 77 QUIZZES
-- TỰ ĐỘNG CHUẨN HÓA NVARCHAR ĐỂ KHÔNG BAO GIỜ BỊ LỖI DẤU TIẾNG VIỆT
-- ====================================================================================
USE EduMyDb;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;

-- 1. NẠP KHÓA HỌC (COURSES)
SET IDENTITY_INSERT dbo.courses ON;
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 1)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (1, 1, 4, N'Lập trình Web Full-Stack MERN & Next.js Thực chiến', N'lp-trnh-full-stack-0c7425', N'Khóa học lập trình Full-Stack từ cơ bản đến nâng cao với MongoDB, Express, React và Node.js.', 130000.00, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&q=80', N'Beginner', N'Published', 0.0, 0, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 2)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (2, 6, 4, N'Lập trình Java Spring Boot 3 & Microservices Thực chiến', N'lap-trinh-java-spring-boot-3-microservices-thuc-chien', N'Khóa học chuyên sâu đưa bạn từ nền tảng Spring Core, Spring Data JPA, Spring Security JWT đến kiến trúc Microservices phân tán với Spring Cloud, Docker, Kafka và Microsoft SQL Server.', 1499000.00, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1000&q=80', N'Intermediate', N'Published', 4.9, 38, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 3)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (3, 6, 4, N'Master ReactJS 18, Next.js 14 & TypeScript từ Zero đến Pro', N'master-reactjs-18-nextjs-14-typescript', N'Làm chủ hệ sinh thái Frontend hiện đại: React Hooks nâng cao, Redux Toolkit, Next.js App Router, SSR/SSG, TailwindCSS và tối ưu hiệu năng điểm 100 Lighthouse.', 1290000.00, 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=1000&q=80', N'All Levels', N'Published', 4.8, 45, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 4)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (4, 6, 6, N'Trí tuệ Nhân tạo & Machine Learning chuyên sâu với Python', N'tri-tue-nhan-tao-machine-learning-python', N'Nắm vững toán giải tích và đại số tuyến tính trong AI, xây dựng mô hình Phân loại, Hồi quy, Xử lý ngôn ngữ tự nhiên NLP (Sentiment Analysis) với Scikit-Learn và PyTorch. Đóng gói API FastAPI.', 1890000.00, 'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=1000&q=80', N'Advanced', N'Published', 4.9, 29, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 5)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (5, 10, 7, N'DevOps Toàn Diện: CI/CD Pipeline, Docker, Kubernetes & AWS', N'devops-toan-dien-docker-kubernetes-aws', N'Tự động hóa toàn bộ vòng đời phát triển phần mềm với GitHub Actions, container hóa Docker đa tầng, quản lý cụm Kubernetes (EKS) và giám sát hạ tầng với Prometheus & Grafana.', 1650000.00, 'https://images.unsplash.com/photo-1607799279861-4dd421887fb3?w=1000&q=80', N'Intermediate', N'Published', 4.7, 22, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 6)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (6, 6, 7, N'Lập trình Ứng dụng Di động Đa nền tảng Flutter & Dart', N'lap-trinh-di-dong-flutter-dart', N'Học viết ứng dụng di động cho cả iOS và Android chỉ với một lần viết mã. Quản lý trạng thái BLoC, tích hợp Firebase Auth, RESTful API và thanh toán qua cổng điện tử.', 990000.00, 'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=1000&q=80', N'Beginner', N'Published', 4.6, 31, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 7)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (7, 8, 4, N'Thiết kế UI/UX Chuyên nghiệp với Figma & Design System', N'thiet-ke-ui-ux-figma-design-system', N'Nghiên cứu tâm lý hành vi người dùng (UX Research), xây dựng Wireframe, Auto Layout, Component biến thể và xuất bản Design System hoàn chỉnh cho dự án ứng dụng web và di động.', 850000.00, 'https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=1000&q=80', N'Beginner', N'Published', 4.8, 36, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 8)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (8, 7, 6, N'Kỹ năng Quản trị Dự án Phần mềm theo mô hình Agile & Scrum', N'quan-tri-du-an-phan-mem-agile-scrum', N'Áp dụng linh hoạt quy trình Agile Scrum vào môi trường doanh nghiệp công nghệ. Quản lý Product Backlog, lập kế hoạch Sprint, ước lượng điểm Story Point trên Jira và Azure DevOps.', 750000.00, 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=1000&q=80', N'All Levels', N'Published', 4.5, 18, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 9)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (9, 5, 6, N'Xây dựng Ứng dụng Generative AI với OpenAI API & LangChain', N'xay-dung-ung-dung-generative-ai-langchain', N'Khóa học mới nhất năm 2026: Tích hợp mô hình ngôn ngữ lớn (LLM), kỹ thuật RAG (Retrieval-Augmented Generation) với Vector Database (Pinecone, ChromaDB) để tạo chatbot thông minh.', 1590000.00, 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=1000&q=80', N'Intermediate', N'Published', 0.0, 0, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 10)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (10, 1, 4, N'Lập trình C#', N'lp-trnh-c-ae4459', N'', 0.00, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&q=80', N'Beginner', N'Published', 0.0, 1, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 11)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (11, 1, 17, N'Lập trình Python', N'lp-trnh-python-160936', N'', 60000.00, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&q=80', N'Beginner', N'Published', 0.0, 0, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 12)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (12, 1, 4, N'Lập trình Backend Chuyên nghiệp với NestJS, TypeScript & PostgreSQL', N'lap-trinh-backend-chuyen-nghiep-voi-nestjs-typescript-postgresql-5102', N'Xây dựng hệ thống Backend kiến trúc Module, Dependency Injection, TypeORM, Swagger API và Microservices chuẩn Enterprise.', 1250000.00, 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1000&q=80', N'Intermediate', N'Published', 4.8, 890, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 13)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (13, 1, 6, N'Lập trình Web Siêu tốc độ với Go (Golang), Gin Framework & gRPC', N'lap-trinh-web-sieu-toc-do-voi-go-golang-gin-framework-grpc-8335', N'Làm chủ ngôn ngữ Go cho Backend, lập trình đồng thời Concurrency với Goroutines, Channels, Gin Web Framework và giao thức gRPC tốc độ cao.', 1450000.00, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1000&q=80', N'Advanced', N'Published', 4.9, 640, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 14)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (14, 2, 7, N'Phân tích Dữ liệu Kinh doanh Toàn diện với Power BI & SQL', N'phan-tich-du-lieu-kinh-doanh-toan-dien-voi-power-bi-sql-9818', N'Thực chiến phân tích doanh thu, hành vi khách hàng, xây dựng Interactive Dashboard và báo cáo quản trị chuyên nghiệp.', 890000.00, 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1000&q=80', N'Beginner', N'Published', 4.8, 1420, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 15)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (15, 2, 2, N'Quản trị Tài chính Doanh nghiệp & Chiến lược Đầu tư Thực chiến', N'quan-tri-tai-chinh-doanh-nghiep-chien-luoc-dau-tu-thuc-chien-6358', N'Nắm vững báo cáo tài chính (BCTC), dòng tiền (Cash Flow), dự báo doanh thu và chiến lược huy động vốn khởi nghiệp.', 1190000.00, 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=1000&q=80', N'Intermediate', N'Published', 4.7, 930, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 16)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (16, 2, 4, N'Nghệ thuật Đàm phán và Bán hàng Chiến lược B2B cho Quản lý', N'nghe-thuat-dam-phan-va-ban-hang-chien-luoc-b2b-cho-quan-ly-2925', N'Kỹ năng tìm kiếm khách hàng doanh nghiệp, xây dựng bài thuyết trình bán hàng (Pitch Deck) và chốt hợp đồng lớn.', 790000.00, 'https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=1000&q=80', N'Intermediate', N'Published', 4.9, 810, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 17)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (17, 2, 17, N'Khởi nghiệp Tinh gọn (Lean Startup) & Xây dựng Mô hình Kinh doanh Đột phá', N'khoi-nghiep-tinh-gon-lean-startup-xay-dung-mo-hinh-kinh-doanh-dot-pha-6138', N'Ứng dụng phương pháp Lean Startup, kiểm chứng MVP thần tốc, xây dựng Business Model Canvas và tối ưu chi phí vận hành.', 0.00, 'https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=1000&q=80', N'Beginner', N'Published', 4.9, 2150, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 18)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (18, 3, 4, N'Thiết kế Đồ họa Chuyên nghiệp với Adobe Photoshop & Illustrator 2024', N'thiet-ke-do-hoa-chuyen-nghiep-voi-adobe-photoshop-illustrator-2024-3641', N'Khóa học đồ họa nền tảng đến chuyên sâu: Xử lý ảnh quảng cáo, vẽ vector, thiết kế poster, banner và logo chuyên nghiệp.', 850000.00, 'https://images.unsplash.com/photo-1626785774573-4b799315345d?w=1000&q=80', N'Beginner', N'Published', 4.9, 1680, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 19)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (19, 3, 7, N'Làm chủ Motion Graphics & Dựng Video Thực chiến với After Effects & Premiere Pro', N'lam-chu-motion-graphics-dung-video-thuc-chien-voi-after-effects-premiere-pro-4881', N'Kỹ xảo điện ảnh, diễn hoạt 2D hoạt hình, hiệu ứng text động (kinetic typography) và cắt dựng video triệu view cho TikTok, YouTube.', 1050000.00, 'https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=1000&q=80', N'Intermediate', N'Published', 4.8, 920, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 20)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (20, 3, 4, N'Thiết kế Nhận diện Thương hiệu & Nghệ thuật Typography Đột phá', N'thiet-ke-nhan-dien-thuong-hieu-nghe-thuat-typography-dot-pha-5813', N'Chiến lược xây dựng Brand Identity từ bản sắc thương hiệu, kết hợp font chữ (Typography), bảng màu chuẩn phong thủy và Brand Guidelines.', 690000.00, 'https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=1000&q=80', N'Beginner', N'Published', 4.7, 750, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 21)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (21, 3, 6, N'Dựng hình & Diễn họa 3D với Blender từ Cơ bản đến Nâng cao', N'dung-hinh-dien-hoa-3d-voi-blender-tu-co-ban-den-nang-cao-5549', N'Học Blender miễn phí 100%: Dựng hình Polygon, đi lưới Topology, gán vật liệu PBR, thiết lập ánh sáng và Render ảnh sản phẩm sống động.', 1290000.00, 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1000&q=80', N'Intermediate', N'Published', 4.9, 870, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 22)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (22, 4, 2, N'Chiến lược Digital Marketing Tổng thể & Growth Hacking Thực chiến', N'chien-luoc-digital-marketing-tong-the-growth-hacking-thuc-chien-2815', N'Xây dựng chiến lược tiếp thị đa kênh (Omnichannel), thiết lập phễu chuyển đổi AARRR, đo lường ROI và tối ưu ngân sách quảng cáo.', 950000.00, 'https://images.unsplash.com/photo-1432888622747-4eb9a8efeb07?w=1000&q=80', N'Intermediate', N'Published', 4.8, 1340, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 23)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (23, 4, 17, N'Quảng cáo Chuyển đổi Facebook & TikTok Ads Bùng nổ Doanh số', N'quang-cao-chuyen-doi-facebook-tiktok-ads-bung-no-doanh-so-3722', N'Học cách thiết lập Pixel, CAPI, phân tích insight tệp đối tượng (Custom Audience), xây dựng kịch bản video viral và vít ngân sách hiệu quả.', 890000.00, 'https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=1000&q=80', N'Beginner', N'Published', 4.9, 1820, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 24)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (24, 4, 6, N'SEO Master: Kỹ thuật Đưa Website Lên Top 1 Google Bền vững', N'seo-master-ky-thuat-dua-website-len-top-1-google-ben-vung-3038', N'Nắm vững SEO On-page, Technical SEO, nghiên cứu từ khóa (Keyword Research), xây dựng liên kết (Link Building) và tối ưu Core Web Vitals.', 790000.00, 'https://images.unsplash.com/photo-1562577309-4932fdd64cd1?w=1000&q=80', N'Intermediate', N'Published', 4.8, 1150, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 25)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (25, 4, 7, N'Xây dựng Thương hiệu Cá nhân & Content Marketing Đa kênh', N'xay-dung-thuong-hieu-ca-nhan-content-marketing-da-kenh-6602', N'Bí quyết định vị bản thân, viết nội dung chạm cảm xúc, xây dựng kênh TikTok, LinkedIn và tạo dòng thu nhập từ chuyên môn cá nhân.', 0.00, 'https://images.unsplash.com/photo-1557804506-669a67965ba0?w=1000&q=80', N'Beginner', N'Published', 4.9, 2490, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 26)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (26, 5, 6, N'An toàn Thông tin Mạng & Thực hành Ethical Hacking Chuẩn CEH', N'an-toan-thong-tin-mang-thuc-hanh-ethical-hacking-chuan-ceh-6928', N'Trang bị kiến thức bảo mật toàn diện: Quét lỗ hổng mạng, tấn công thử nghiệm (Penetration Testing), phòng chống SQL Injection, XSS và CSRF.', 1790000.00, 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=1000&q=80', N'Intermediate', N'Published', 4.9, 780, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 27)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (27, 5, 4, N'Quản trị và Tối ưu Cơ sở Dữ liệu SQL Server & PostgreSQL Chuyên sâu', N'quan-tri-va-toi-uu-co-so-du-lieu-sql-server-postgresql-chuyen-sau-1326', N'Thiết kế lược đồ chuẩn hóa, phân vùng bảng (Partitioning), tối ưu hóa chỉ mục (Index Tuning) và viết Stored Procedure hiệu năng cao.', 1150000.00, 'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=1000&q=80', N'Advanced', N'Published', 4.8, 690, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 28)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (28, 5, 7, N'Kiểm thử Tự động Hóa Phần mềm (Automation Testing) với Selenium & Playwright', N'kiem-thu-tu-dong-hoa-phan-mem-automation-testing-voi-selenium-playwright-3478', N'Xây dựng Framework kiểm thử tự động End-to-End (E2E), mô hình Page Object Model (POM), tích hợp CI/CD Jenkins và sinh báo cáo tự động.', 990000.00, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=1000&q=80', N'Intermediate', N'Published', 4.7, 830, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 29)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (29, 7, 2, N'Kỹ năng Lãnh đạo Đột phá và Quản trị Đội ngũ Hiệu suất Cao', N'ky-nang-lanh-dao-dot-pha-va-quan-tri-doi-ngu-hieu-suat-cao-9400', N'Học cách truyền cảm hứng, giải quyết xung đột, phân quyền hiệu quả và xây dựng văn hóa đội nhóm gắn kết, sáng tạo.', 890000.00, 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=1000&q=80', N'Intermediate', N'Published', 4.9, 1280, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 30)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (30, 7, 6, N'Quản trị Vận hành và Chuỗi Cung ứng Toàn cầu (Supply Chain)', N'quan-tri-van-hanh-va-chuoi-cung-ung-toan-cau-supply-chain-5465', N'Thiết kế mạng lưới Logistics, tối ưu hóa lượng hàng tồn kho (EOQ), quản lý nhà cung cấp và áp dụng công nghệ chuyển đổi số chuỗi cung ứng.', 1100000.00, 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=1000&q=80', N'Intermediate', N'Published', 4.7, 710, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 31)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (31, 7, 4, N'Thiết lập và Thực thi Mục tiêu Chiến lược với OKR & KPI trong Doanh nghiệp', N'thiet-lap-va-thuc-thi-muc-tieu-chien-luoc-voi-okr-kpi-trong-doanh-nghiep-2527', N'Phương pháp triển khai OKRs (Objectives and Key Results) chuẩn Google, liên kết KPI cá nhân với chiến lược công ty, theo dõi và đo lường minh bạch.', 0.00, 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=1000&q=80', N'Beginner', N'Published', 4.8, 2100, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 32)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (32, 8, 4, N'Nghiên cứu Người dùng (UX Research) & Kiểm thử Trải nghiệm Thực tế', N'nghien-cuu-nguoi-dung-ux-research-kiem-thu-trai-nghiem-thuc-te-3206', N'Phương pháp phỏng vấn người dùng, xây dựng User Persona, Journey Map, Card Sorting và tiến hành Usability Testing chuyên nghiệp.', 890000.00, 'https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=1000&q=80', N'Intermediate', N'Published', 4.9, 940, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 33)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (33, 8, 7, N'Xây dựng Hệ thống Thiết kế (Design System) Quy mô Lớn trên Figma', N'xay-dung-he-thong-thiet-ke-design-system-quy-mo-lon-tren-figma-7068', N'Thiết kế Design Tokens, Typography Scale, Color Palette, Auto Layout 5.0, Component Variants và đồng bộ code cùng lập trình viên.', 1190000.00, 'https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=1000&q=80', N'Advanced', N'Published', 4.9, 820, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 34)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (34, 8, 4, N'Thiết kế Giao diện Ứng dụng Di động Chuẩn iOS & Android', N'thiet-ke-giao-dien-ung-dung-di-dong-chuan-ios-android-7556', N'Học sâu Apple Human Interface Guidelines và Google Material You 3.0: Thiết kế luồng Onboarding, Giỏ hàng, Tab Bar và Micro-interactions.', 950000.00, 'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=1000&q=80', N'Intermediate', N'Published', 4.8, 1050, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 35)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (35, 9, 17, N'Nghệ thuật Viết Content Bán hàng Đỉnh cao (Copywriting & Storytelling)', N'nghe-thuat-viet-content-ban-hang-dinh-cao-copywriting-storytelling-3540', N'Ứng dụng các công thức kinh điển AIDA, PAS, FAB: Viết tiêu đề thôi miên, bài viết quảng cáo nghìn đơn và kịch bản video bán hàng.', 590000.00, 'https://images.unsplash.com/photo-1455390582262-044cdead277a?w=1000&q=80', N'Beginner', N'Published', 4.9, 1750, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 36)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (36, 9, 6, N'Tối ưu Quảng cáo Google Ads Tìm kiếm, Hiển thị & Mua sắm', N'toi-uu-quang-cao-google-ads-tim-kiem-hien-thi-mua-sam-3523', N'Làm chủ Google Search Ads, Performance Max (PMax), Google Shopping, nghiên cứu đối sánh từ khóa và kỹ thuật tối ưu điểm chất lượng Quality Score.', 890000.00, 'https://images.unsplash.com/photo-1563986768609-322da13575f3?w=1000&q=80', N'Intermediate', N'Published', 4.8, 1120, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 37)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (37, 9, 2, N'Quản trị Truyền thông Xã hội & Xử lý Khủng hoảng Thương hiệu', N'quan-tri-truyen-thong-xa-hoi-xu-ly-khung-hoang-thuong-hieu-1189', N'Xây dựng chiến lược Social Media đa kênh, quy trình giám sát dư luận (Social Listening), kịch bản phản ứng nhanh và giải tỏa khủng hoảng.', 750000.00, 'https://images.unsplash.com/photo-1516251193007-45ef944ab0c6?w=1000&q=80', N'Intermediate', N'Published', 4.7, 890, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 38)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (38, 9, 7, N'Chiến lược Email Marketing Tự động hóa & Phễu Nuôi dưỡng Khách hàng', N'chien-luoc-email-marketing-tu-dong-hoa-pheu-nuoi-duong-khach-hang-1095', N'Thiết kế chuỗi email tự động chào mừng, email chăm sóc giỏ hàng bị bỏ quên (Abandoned Cart), tối ưu tỷ lệ mở (Open Rate) và nhấp chuột (CTR).', 0.00, 'https://images.unsplash.com/photo-1596526131083-e8c633c948d2?w=1000&q=80', N'Beginner', N'Published', 4.9, 1950, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 39)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (39, 10, 6, N'Luyện thi Chứng chỉ AWS Certified Solutions Architect Associate (SAA-C03)', N'luyen-thi-chung-chi-aws-certified-solutions-architect-associate-saa-c03-9736', N'Toàn diện các dịch vụ cốt lõi của Amazon Web Services: VPC, EC2, S3, RDS, Lambda, ECS, CloudFront, Route53 và kiến trúc High Availability.', 1850000.00, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1000&q=80', N'Intermediate', N'Published', 4.9, 1450, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 40)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (40, 10, 7, N'Quản trị Hệ điều hành Linux Server & Tự động hóa với Bash Scripting', N'quan-tri-he-dieu-hanh-linux-server-tu-dong-hoa-voi-bash-scripting-9102', N'Làm chủ dòng lệnh Linux (Ubuntu, Debian, CentOS), quản trị người dùng, phân quyền chmod/chown, quản lý dịch vụ Systemd và viết script tự động hóa.', 790000.00, 'https://images.unsplash.com/photo-1629654297299-c8506221ca97?w=1000&q=80', N'Beginner', N'Published', 4.8, 1620, 0, GETDATE(), GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE course_id = 41)
  INSERT INTO dbo.courses (course_id, category_id, instructor_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at) VALUES (41, 10, 4, N'Triển khai Kubernetes (K8s) & Kiến trúc Hạ tầng Đám mây Cloud-Native', N'trien-khai-kubernetes-k8s-kien-truc-ha-tang-dam-may-cloud-native-8518', N'Triển khai cụm Kubernetes thực chiến: Deployments, StatefulSets, Ingress Controller NGINX, Horizontal Pod Autoscaler (HPA) và GitOps với ArgoCD.', 1590000.00, 'https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=1000&q=80', N'Advanced', N'Published', 4.9, 910, 0, GETDATE(), GETDATE());
SET IDENTITY_INSERT dbo.courses OFF;
GO

-- 2. NẠP CHƯƠNG MỤC (COURSE_SECTIONS)
SET IDENTITY_INSERT dbo.course_sections ON;
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 1)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (1, 1, N'Chuong 1: Kien Thuc Nen Tang', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 2)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (2, 2, N'Chương 1: Thiết kế Kiến trúc và Khởi tạo Dự án Spring Boot 3', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 3)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (3, 2, N'Chương 2: Bảo mật Nâng cao với Spring Security 6 & JWT Stateless', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 4)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (4, 2, N'Chương 3: Tối ưu Hóa Truy Vấn Cơ Sở Dữ Liệu MSSQL & HikariCP', 3);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 5)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (5, 3, N'Chương 1: Nền tảng React 18, JSX và Component Architecture', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 6)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (6, 3, N'Chương 2: Quản lý State Phức tạp với Redux Toolkit & Context API', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 7)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (7, 4, N'Chương 1: Cơ sở Toán học & Xử lý Dữ liệu với NumPy, Pandas', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 8)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (8, 4, N'Chương 2: Xây dựng Mô hình Học máy & NLP Sentiment Pipeline', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 9)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (9, 5, N'Chương 1: Đóng gói Ứng dụng với Docker Đa tầng (Multi-stage)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 10)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (10, 5, N'Chương 2: Dàn dựng Cụm Kubernetes và Cấu hình Ingress Nginx', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 11)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (11, 11, N'Chương 1', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 12)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (12, 6, N'Phần 1: Nhập môn & Nền tảng Lập trình Ứng dụng Di động Đa ', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 13)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (13, 6, N'Phần 2: Kỹ năng Nâng cao & Dự án Thực tế', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 14)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (14, 7, N'Phần 1: Nhập môn & Nền tảng Thiết kế UI/UX Chuyên nghiệp v', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 15)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (15, 7, N'Phần 2: Kỹ năng Nâng cao & Dự án Thực tế', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 16)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (16, 8, N'Phần 1: Nhập môn & Nền tảng Kỹ năng Quản trị Dự án Phần mề', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 17)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (17, 8, N'Phần 2: Kỹ năng Nâng cao & Dự án Thực tế', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 18)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (18, 9, N'Phần 1: Nhập môn & Nền tảng Xây dựng Ứng dụng Generative A', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 19)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (19, 9, N'Phần 2: Kỹ năng Nâng cao & Dự án Thực tế', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 20)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (20, 10, N'Phần 1: Nhập môn & Nền tảng Lập trình C#', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 21)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (21, 10, N'Phần 2: Kỹ năng Nâng cao & Dự án Thực tế', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 22)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (22, 12, N'Chương 1: Kiến trúc NestJS, Controllers & Services', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 23)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (23, 12, N'Chương 2: Tích hợp PostgreSQL, TypeORM & Bảo mật JWT', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 24)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (24, 13, N'Chương 1: Golang Cơ Bản & Lập Trình Đồng Thời (Concurrency)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 25)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (25, 13, N'Chương 2: Xây dựng REST API với Gin & Giao thức gRPC', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 26)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (26, 14, N'Chương 1: Trích xuất & Xử lý Dữ liệu Kinh doanh với SQL', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 27)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (27, 14, N'Chương 2: Thiết kế Báo cáo Trực quan trên Power BI', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 28)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (28, 15, N'Chương 1: Đọc hiểu 3 Báo cáo Tài chính Cốt lõi', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 29)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (29, 15, N'Chương 2: Định giá Doanh nghiệp & Gọi vốn Đầu tư', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 30)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (30, 16, N'Chương 1: Quy trình Bán hàng B2B Chuẩn Quốc tế', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 31)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (31, 16, N'Chương 2: Tâm lý học Đàm phán & Kỹ thuật Win-Win', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 32)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (32, 17, N'Chương 1: Tư duy Khởi nghiệp Tinh gọn & Khung Canvas', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 33)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (33, 17, N'Chương 2: Đo lường Chỉ số Phù hợp Thị trường (Product-Market Fit)', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 34)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (34, 18, N'Chương 1: Làm chủ Adobe Photoshop & Xử lý Hình ảnh', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 35)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (35, 18, N'Chương 2: Vẽ Vector & Thiết kế Logo với Illustrator', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 36)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (36, 19, N'Chương 1: Dựng Video & Kể chuyện bằng Hình ảnh với Premiere', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 37)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (37, 19, N'Chương 2: Diễn hoạt Chuyển động & Kỹ xảo với After Effects', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 38)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (38, 20, N'Chương 1: Nguyên lý Thị giác & Phân loại Typography', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 39)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (39, 20, N'Chương 2: Đóng gói Bộ Brand Guidelines Hoàn chỉnh', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 40)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (40, 21, N'Chương 1: Giao diện Blender & Dựng hình Khối Cơ Bản', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 41)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (41, 21, N'Chương 2: Vật liệu PBR, Đánh sáng & Render với Cycles', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 42)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (42, 22, N'Chương 1: Khung Chiến lược Tiếp thị Đa kênh', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 43)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (43, 22, N'Chương 2: Mô hình Tăng trưởng Người dùng (Growth Funnel AARRR)', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 44)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (44, 23, N'Chương 1: Thuật toán Phân phối & Chuẩn bị Tài nguyên Ads', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 45)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (45, 23, N'Chương 2: Sáng tạo Nội dung Viral & Tối ưu Chiến dịch TikTok Ads', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 46)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (46, 24, N'Chương 1: Nghiên cứu Từ khóa & Kiến trúc Thông tin Website', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 47)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (47, 24, N'Chương 2: Technical SEO & Tối ưu Core Web Vitals', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 48)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (48, 25, N'Chương 1: Định vị Bản thân & Lựa chọn Thị trường Ngách (Niche)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 49)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (49, 25, N'Chương 2: Công thức Sáng tạo Nội dung Hút Khán giả', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 50)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (50, 26, N'Chương 1: Thu thập Thông tin & Quét Lỗ hổng Mạng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 51)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (51, 26, N'Chương 2: Tấn công Web & Phòng chống Top 10 Lỗ hổng OWASP', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 52)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (52, 27, N'Chương 1: Phân tích Kế hoạch Thực thi (Execution Plan) & Index Tuning', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 53)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (53, 27, N'Chương 2: Khóa Dữ liệu (Locking), Giao dịch (ACID) & Sao lưu', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 54)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (54, 28, N'Chương 1: Kiến trúc Automation Testing & Page Object Model', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 55)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (55, 28, N'Chương 2: Chạy Kiểm thử Song song & Tích hợp CI/CD', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 56)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (56, 29, N'Chương 1: Chân dung Nhà Lãnh đạo Thời đại Số', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 57)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (57, 29, N'Chương 2: Động viên & Xây dựng Đội ngũ Gắn kết', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 58)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (58, 30, N'Chương 1: Tổng quan Chuỗi Cung ứng & Dự báo Nhu cầu', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 59)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (59, 30, N'Chương 2: Quản trị Kho bãi & Mô hình Tồn kho Tối ưu', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 60)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (60, 31, N'Chương 1: Bản chất của Khung Mục tiêu OKR', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 61)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (61, 31, N'Chương 2: Nhịp điệu Vận hành & Check-in OKR Hàng Tuần', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 62)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (62, 32, N'Chương 1: Phương pháp Nghiên cứu Định tính & Định lượng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 63)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (63, 32, N'Chương 2: Thiết kế User Journey Map & Usability Testing', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 64)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (64, 33, N'Chương 1: Kiến trúc Design Tokens & Biến Figma Variables', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 65)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (65, 33, N'Chương 2: Thiết kế Component Library Chuẩn Atomic Design', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 66)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (66, 34, N'Chương 1: Quy chuẩn Giao diện iOS Human Interface Guidelines', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 67)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (67, 34, N'Chương 2: Thiết kế Android Material You & Micro-animations', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 68)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (68, 35, N'Chương 1: Các Công thức Copywriting Kinh điển', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 69)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (69, 35, N'Chương 2: Storytelling - Nghệ thuật Chạm tới Trái tim Khách hàng', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 70)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (70, 36, N'Chương 1: Chiến dịch Tìm kiếm Google Search & Điểm Chất lượng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 71)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (71, 36, N'Chương 2: Chiến dịch Tối đa hóa Hiệu suất (Performance Max)', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 72)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (72, 37, N'Chương 1: Lập Kế hoạch Truyền thông Mạng Xã hội Đa kênh', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 73)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (73, 37, N'Chương 2: Quy trình 24 Giờ Vàng Xử lý Khủng hoảng Truyền thông', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 74)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (74, 38, N'Chương 1: Xây dựng Danh sách Email Khách hàng Chất lượng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 75)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (75, 38, N'Chương 2: Thiết lập Kịch bản Automation Flow Bán hàng', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 76)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (76, 39, N'Chương 1: Mạng Đám Mây Toàn Cầu & Điện Toán AWS (VPC & EC2)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 77)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (77, 39, N'Chương 2: Cơ sở Dữ liệu & Kiến trúc Serverless (RDS & Lambda)', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 78)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (78, 40, N'Chương 1: Quản trị Hệ thống Tệp, Tiến trình & Phân quyền Linux', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 79)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (79, 40, N'Chương 2: Tự động hóa Quản trị với Bash Shell Scripting & Cron', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 80)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (80, 41, N'Chương 1: Các Đối Tượng Cốt Lõi trong Kubernetes (K8s Objects)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE section_id = 81)
  INSERT INTO dbo.course_sections (section_id, course_id, title, order_index) VALUES (81, 41, N'Chương 2: Mạng & Điều hướng Lưu lượng với Service & Ingress', 2);
SET IDENTITY_INSERT dbo.course_sections OFF;
GO

-- 3. NẠP BÀI HỌC (LESSONS)
SET IDENTITY_INSERT dbo.lessons ON;
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 1)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (1, 2, N'1.1 Giới thiệu Tổng quan Kiến trúc Spring Boot 3 & Hệ sinh thái 2026', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 650, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 2)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (2, 2, N'1.2 Cấu hình Spring Initializr, Dependency Injection & IoC Container', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 890, 1, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 3)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (3, 2, N'1.3 Thiết kế DTO, Entity Mapping và Repository Pattern chuẩn 3-Tier', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1200, 0, 3);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 4)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (4, 3, N'2.1 Cơ chế Filter Chain trong Spring Security 6', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1150, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 5)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (5, 3, N'2.2 Cấp phát và Xác thực JSON Web Token (JWT)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1420, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 6)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (6, 3, N'2.3 Phân quyền Theo Role-Based Access Control (@PreAuthorize)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 980, 0, 3);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 7)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (7, 4, N'3.1 Tối ưu hóa HikariCP Connection Pool cho MSSQL', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', 1300, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 8)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (8, 4, N'3.2 Xử lý N+1 Problem với EntityGraph & Fetch Join', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', 1540, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 9)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (9, 5, N'1.1 Giới thiệu React 18, Cơ chế Virtual DOM và Cài đặt Vite', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4', 720, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 10)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (10, 5, N'1.2 Quản lý State với useState, useEffect và Custom Hooks', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', 1100, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 11)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (11, 5, N'1.3 Tối ưu re-render với useMemo, useCallback và React.memo', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4', 950, 0, 3);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 12)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (12, 6, N'2.1 Thiết lập Redux Toolkit Store, Slice và createAsyncThunk', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4', 1350, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 13)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (13, 6, N'2.2 Quản lý Giỏ hàng và Trạng thái Đăng nhập Toàn cục', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1280, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 14)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (14, 7, N'1.1 Khởi tạo Môi trường Python, Jupyter Notebook và Scikit-Learn', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 800, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 15)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (15, 7, N'1.2 Tiền xử lý Dữ liệu Văn bản Tiếng Việt và TF-IDF Vectorization', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1400, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 16)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (16, 8, N'2.1 Huấn luyện Mô hình Phân loại Cảm xúc Review (Positive/Negative)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1600, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 17)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (17, 9, N'1.1 Viết Dockerfile Tối ưu Kích thước Image cho Spring Boot & Node.js', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 920, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 18)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (18, 10, N'2.1 Cấu hình Kubernetes Deployment, Service và Horizontal Pod Autoscaler', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1500, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 19)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (19, 12, N'Bài 1: Giới thiệu khóa học & Lộ trình học tập', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 600, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 20)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (20, 12, N'Bài 2: Cài đặt công cụ & Thiết lập môi trường thực hành', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 840, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 21)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (21, 13, N'Bài 3: Thực hành đồ án mẫu từ yêu cầu doanh nghiệp', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1200, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 22)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (22, 13, N'Bài 4: Tối ưu hóa hiệu năng & Đóng gói sản phẩm', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 980, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 23)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (23, 14, N'Bài 1: Giới thiệu khóa học & Lộ trình học tập', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 600, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 24)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (24, 14, N'Bài 2: Cài đặt công cụ & Thiết lập môi trường thực hành', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 840, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 25)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (25, 15, N'Bài 3: Thực hành đồ án mẫu từ yêu cầu doanh nghiệp', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1200, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 26)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (26, 15, N'Bài 4: Tối ưu hóa hiệu năng & Đóng gói sản phẩm', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 980, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 27)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (27, 16, N'Bài 1: Giới thiệu khóa học & Lộ trình học tập', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 600, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 28)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (28, 16, N'Bài 2: Cài đặt công cụ & Thiết lập môi trường thực hành', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 840, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 29)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (29, 17, N'Bài 3: Thực hành đồ án mẫu từ yêu cầu doanh nghiệp', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1200, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 30)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (30, 17, N'Bài 4: Tối ưu hóa hiệu năng & Đóng gói sản phẩm', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 980, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 31)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (31, 18, N'Bài 1: Giới thiệu khóa học & Lộ trình học tập', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 600, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 32)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (32, 18, N'Bài 2: Cài đặt công cụ & Thiết lập môi trường thực hành', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 840, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 33)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (33, 19, N'Bài 3: Thực hành đồ án mẫu từ yêu cầu doanh nghiệp', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1200, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 34)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (34, 19, N'Bài 4: Tối ưu hóa hiệu năng & Đóng gói sản phẩm', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 980, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 35)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (35, 20, N'Bài 1: Giới thiệu khóa học & Lộ trình học tập', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 600, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 36)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (36, 20, N'Bài 2: Cài đặt công cụ & Thiết lập môi trường thực hành', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 840, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 37)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (37, 21, N'Bài 3: Thực hành đồ án mẫu từ yêu cầu doanh nghiệp', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1200, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 38)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (38, 21, N'Bài 4: Tối ưu hóa hiệu năng & Đóng gói sản phẩm', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 980, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 39)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (39, 22, N'Giới thiệu NestJS CLI & Cấu trúc dự án chuẩn', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 750, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 40)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (40, 22, N'Controllers, DTOs & Validation Pipe', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 980, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 41)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (41, 22, N'Dependency Injection & Providers chuyên sâu', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1100, 0, 3);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 42)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (42, 23, N'Cấu hình TypeORM & Migration dữ liệu', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 850, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 43)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (43, 23, N'Authentication với Passport-JWT & Refresh Token', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1250, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 44)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (44, 24, N'Cú pháp Golang, Structs, Pointers & Interfaces', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 890, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 45)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (45, 24, N'Goroutines & Channels: Lập trình đa luồng chuẩn xác', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1200, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 46)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (46, 25, N'Xây dựng High Performance REST API với Gin Framework', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 950, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 47)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (47, 25, N'Giao thức gRPC & Protocol Buffers trong Microservices', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1150, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 48)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (48, 26, N'Truy vấn kết hợp nhiều bảng (JOINs) & Gom nhóm (GROUP BY)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 800, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 49)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (49, 26, N'Tính toán các chỉ số kinh doanh: CAC, LTV, Retention Rate', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1020, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 50)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (50, 27, N'Data Modeling & DAX Formulas cơ bản', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 950, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 51)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (51, 27, N'Xây dựng Dashboard Doanh thu & Báo cáo Lãnh đạo', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1100, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 52)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (52, 28, N'Bảng cân đối kế toán & Báo cáo kết quả kinh doanh', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 890, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 53)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (53, 28, N'Báo cáo lưu chuyển tiền tệ & Quản lý dòng tiền', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1050, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 54)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (54, 29, N'Phương pháp DCF (Chiết khấu dòng tiền) & P/E', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 940, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 55)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (55, 29, N'Quy trình gọi vốn từ Angel Investors & Quỹ VC', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1120, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 56)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (56, 30, N'Xác định chân dung khách hàng B2B (ICP)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 700, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 57)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (57, 30, N'Chiến lược tiếp cận & Thiết lập cuộc hẹn C-Level', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 900, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 58)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (58, 31, N'Phương pháp đàm phán Harvard & BATNA', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 850, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 59)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (59, 31, N'Xử lý từ chối và chốt hợp đồng giá trị cao', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1000, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 60)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (60, 32, N'Vòng lặp Xây dựng - Đo lường - Học hỏi (Build - Measure - Learn)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 720, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 61)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (61, 32, N'Hoàn thiện Business Model Canvas trong 1 trang giấy', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 880, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 62)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (62, 33, N'Thực hiện phỏng vấn khách hàng sâu (CustDev)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 800, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 63)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (63, 33, N'Dấu hiệu nhận biết đạt Product-Market Fit', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 950, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 64)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (64, 34, N'Công cụ Layer, Masking & Blend Modes cơ bản', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 900, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 65)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (65, 34, N'Cắt ghép & Chỉnh sửa ảnh chân dung, sản phẩm thương mại', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1100, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 66)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (66, 35, N'Làm chủ công cụ Pen Tool, Shape Builder & Pathfinder', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 950, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 67)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (67, 35, N'Quy chuẩn thiết kế Logo & Hệ thống nhận diện cơ bản', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1200, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 68)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (68, 36, N'Quy trình import, cắt ghép thô và nhịp điệu chuyển cảnh', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 850, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 69)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (69, 36, N'Chỉnh màu Lumetri Color & Xử lý âm thanh chuẩn truyền hình', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1000, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 70)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (70, 37, N'Keyframes, Graph Editor & Nguyên lý hoạt hình chuyển động mượt mà', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1100, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 71)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (71, 37, N'Thiết kế Intro động & Kinetic Typography cho mạng xã hội', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1250, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 72)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (72, 38, N'Serif, Sans-serif, Script & Quy tắc kết hợp Font chữ', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 750, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 73)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (73, 38, N'Tỷ lệ phân cấp thị giác (Visual Hierarchy) trong thiết kế', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 890, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 74)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (74, 39, N'Quy chuẩn logo, khoảng cách an toàn & ứng dụng Mockup', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 920, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 75)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (75, 39, N'Xuất file in ấn và bàn giao tài liệu nhận diện cho khách hàng', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 850, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 76)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (76, 40, N'Điều hướng 3D Viewport & Các lệnh biến đổi (G, R, S)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 800, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 77)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (77, 40, N'Edit Mode, Extrude, Bevel & Loop Cut trong dựng hình', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1050, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 78)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (78, 41, N'Tạo vật liệu kim loại, thủy tinh bằng Shader Editor', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 960, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 79)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (79, 41, N'Bố trí ánh sáng 3 điểm (Three-Point Lighting) & Render ảnh', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1100, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 80)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (80, 42, N'Phân tích thị trường, đối thủ cạnh tranh & SWOT', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 750, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 81)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (81, 42, N'Thiết lập mục tiêu SMART & Phân bổ ngân sách Marketing', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 920, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 82)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (82, 43, N'Acquisition, Activation & Retention trong SaaS/E-commerce', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 880, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 83)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (83, 43, N'Thử nghiệm A/B Testing để tối ưu tỷ lệ chuyển đổi Landing Page', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1020, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 84)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (84, 44, N'Hiểu cơ chế đấu thầu (Ad Auction) & Điểm chất lượng quảng cáo', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 800, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 85)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (85, 44, N'Cài đặt Facebook Pixel & Conversions API (CAPI) chống rụng số liệu', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1000, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 86)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (86, 45, N'Công thức 3 giây đầu giữ chân người xem video ngắn', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 750, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 87)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (87, 45, N'Kỹ thuật scale ngân sách không làm đắt giá CPM và CPA', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1150, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 88)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (88, 46, N'Phân loại Search Intent (Mục đích tìm kiếm) của người dùng', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 720, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 89)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (89, 46, N'Lập kế hoạch từ khóa bằng Google Keyword Planner & Ahrefs', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 960, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 90)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (90, 47, N'Cấu trúc URL, Thẻ Canonical, Robots.txt & Sitemap.xml', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 850, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 91)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (91, 47, N'Tối ưu tốc độ tải trang LCP, FID và CLS đạt chuẩn Google', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1080, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 92)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (92, 48, N'Xác định điểm mạnh độc bản (Unique Value Proposition)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 680, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 93)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (93, 48, N'Chọn lựa nền tảng phù hợp: LinkedIn vs TikTok vs YouTube', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 840, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 94)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (94, 49, N'Khung nội dung giáo dục, truyền cảm hứng và giải trí', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 780, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 95)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (95, 49, N'Quy trình chuyển đổi người theo dõi thành khách hàng trả phí', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 950, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 96)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (96, 50, N'Kỹ thuật OSINT thu thập thông tin mục tiêu', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 850, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 97)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (97, 50, N'Quét cổng & Lập bản đồ mạng với Nmap, Wireshark', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1100, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 98)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (98, 51, N'Khai thác & Phòng vệ SQL Injection với Prepared Statements', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1050, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 99)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (99, 51, N'Cross-Site Scripting (XSS) & Kỹ thuật Content Security Policy (CSP)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1200, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 100)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (100, 52, N'Đọc hiểu Execution Plan & Nhận biết Table Scan nguy hiểm', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 950, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 101)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (101, 52, N'Clustered vs Non-Clustered Index & Covering Index tối ưu', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1180, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 102)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (102, 53, N'Các cấp độ cô lập giao dịch (Transaction Isolation Levels)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 920, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 103)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (103, 53, N'Chiến lược sao lưu Full, Differential & Transaction Log', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1040, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 104)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (104, 54, N'Cài đặt môi trường Playwright với Node.js & TypeScript', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 780, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 105)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (105, 54, N'Thiết kế mô hình Page Object Model (POM) tái sử dụng code', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 980, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 106)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (106, 55, N'Kiểm thử API song song với UI Testing', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 890, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 107)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (107, 55, N'Tích hợp test suite tự động vào pipeline Jenkins/GitHub Actions', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1050, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 108)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (108, 56, N'Sự khác biệt cốt lõi giữa Quản lý (Manager) và Lãnh đạo (Leader)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 700, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 109)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (109, 56, N'Trí tuệ cảm xúc (EQ) trong lắng nghe và thấu cảm nhân sự', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 860, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 110)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (110, 57, N'Ma trận phân quyền Eisenhower & Giao việc thông minh', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 800, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 111)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (111, 57, N'Kỹ năng phản hồi mang tính xây dựng (Constructive Feedback)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 940, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 112)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (112, 58, N'Dòng chảy hàng hóa, thông tin và tài chính trong Supply Chain', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 820, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 113)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (113, 58, N'Dự báo nhu cầu và hiệu ứng Bullwhip Effect', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 980, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 114)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (114, 59, N'Công thức EOQ (Economic Order Quantity) & Điểm đặt hàng lại (ROP)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 920, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 115)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (115, 59, N'Ứng dụng Barcode, RFID & Hệ thống WMS hiện đại', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1020, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 116)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (116, 60, N'Sự khác biệt cốt lõi giữa OKR và KPI', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 720, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 117)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (117, 60, N'Cách viết Mục tiêu (Objective) truyền cảm hứng và Key Results định lượng', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 900, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 118)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (118, 61, N'Quy trình Check-in 1-on-1 hàng tuần hiệu quả trong 15 phút', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 800, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 119)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (119, 61, N'Chấm điểm OKR cuối quý & Rút bài học cải tiến', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 920, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 120)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (120, 62, N'Kỹ thuật phỏng vấn sâu 1-on-1 không đặt câu hỏi mớm cung', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 840, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 121)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (121, 62, N'Xây dựng Khảo sát định lượng & Đo lường chỉ số SUS, NPS', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1000, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 122)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (122, 63, N'Lập bản đồ hành trình trải nghiệm người dùng (Customer Journey)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 910, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 123)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (123, 63, N'Tổ chức phiên kiểm thử thực tế và ghi nhận điểm đau (Pain points)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1150, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 124)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (124, 64, N'Hiểu về Design Tokens: Spacing, Sizing, Colors, Radii', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 850, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 125)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (125, 64, N'Ứng dụng Figma Variables cho Dark Mode và Multi-brand', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1100, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 126)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (126, 65, N'Auto Layout, Min/Max Width và Component Properties', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 980, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 127)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (127, 65, N'Đóng gói Button, Form Inputs, Modals & Bàn giao Dev Mode', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1200, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 128)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (128, 66, N'Hệ thống lưới (Grid 8pt), Typography San Francisco & Safe Area', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 820, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 129)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (129, 66, N'Thiết kế Navigation Bar, Tab Bar & Dynamic Island trên iOS', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1020, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 130)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (130, 67, N'Hệ thống màu động Dynamic Color của Material 3', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 880, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 131)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (131, 67, N'Nguyên mẫu tương tác chuyển động (Interactive Prototyping) trên Figma', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1120, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 132)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (132, 68, N'Công thức PAS: Problem (Vấn đề) - Agitate (Xoáy sâu) - Solution (Giải pháp)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 720, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 133)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (133, 68, N'10 Công thức đặt tiêu đề thôi miên tăng 300% tỷ lệ nhấp chuột', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 890, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 134)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (134, 69, N'Cấu trúc câu chuyện ''Hành trình của người hùng'' (Hero''s Journey)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 800, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 135)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (135, 69, N'Ứng dụng AI (ChatGPT, Claude) nhân bản ý tưởng nội dung', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 950, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 136)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (136, 70, N'Các loại đối sánh từ khóa: Chính xác, Cụm từ và Mở rộng', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 820, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 137)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (137, 70, N'3 Yếu tố cấu thành Điểm chất lượng (Quality Score) 10/10', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 990, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 138)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (138, 71, N'Cấu hình nhóm tài sản (Asset Groups) & Tín hiệu đối tượng (Audience Signals)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 900, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 139)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (139, 71, N'Theo dõi giá trị chuyển đổi (ROAS) và chiến lược đặt giá thầu thông minh', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1080, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 140)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (140, 72, N'Xây dựng bảng kế hoạch nội dung (Content Calendar) hàng tháng', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 750, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 141)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (141, 72, N'Đo lường các chỉ số tương tác: Engagement Rate, Reach & Virality', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 880, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 142)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (142, 73, N'Các dấu hiệu cảnh báo sớm từ Social Listening', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 920, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 143)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (143, 73, N'Quy trình ứng phó phát ngôn báo chí & Chân thành nhận trách nhiệm', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1100, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 144)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (144, 74, N'Thiết kế mồi nhử Lead Magnet & Form thu thập thông tin', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 720, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 145)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (145, 74, N'Phân nhóm tệp email (Segmentation) dựa trên hành vi', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 890, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 146)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (146, 75, N'Chuỗi kịch bản Email Chào mừng (Welcome Series) 5 bước', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 850, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 147)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (147, 75, N'Tối ưu hóa tránh hòm thư Rác (Spam) với SPF, DKIM & DMARC', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1020, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 148)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (148, 76, N'Thiết kế Virtual Private Cloud (VPC), Subnets, Route Tables & IGW', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1100, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 149)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (149, 76, N'Cấu hình EC2 Auto Scaling Group & Elastic Load Balancer (ALB)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1350, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 150)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (150, 77, N'Cấu hình Amazon RDS Multi-AZ & Read Replicas cho tính sẵn sàng cao', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 980, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 151)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (151, 77, N'Serverless Computing với AWS Lambda, API Gateway & EventBridge', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1200, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 152)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (152, 78, N'Cấu trúc cây thư mục Linux & Các lệnh điều hướng cơ bản', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 780, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 153)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (153, 78, N'Phân quyền tệp (Chmod, Chown) và Quản trị tiến trình (ps, top, kill)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 960, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 154)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (154, 79, N'Viết Bash script tự động backup database và gửi thông báo Telegram', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1020, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 155)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (155, 79, N'Lập lịch tác vụ định kỳ với Crontab chuẩn hệ thống', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 850, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 156)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (156, 80, N'Kiến trúc Control Plane vs Worker Node & Cấu hình Kubectl', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 950, 1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 157)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (157, 80, N'Triển khai Pods, Deployments & Rollout Updates không downtime', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1180, 0, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 158)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (158, 81, N'So sánh ClusterIP, NodePort, LoadBalancer & Ingress Controller', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1080, 0, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.lessons WHERE lesson_id = 159)
  INSERT INTO dbo.lessons (lesson_id, section_id, title, video_url, duration, is_preview, order_index) VALUES (159, 81, N'Cấu hình Tự động co giãn theo tải (Horizontal Pod Autoscaler - HPA)', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1250, 0, 2);
SET IDENTITY_INSERT dbo.lessons OFF;
GO

-- 4. NẠP BÀI TRẮC NGHIỆM (QUIZZES)
SET IDENTITY_INSERT dbo.quizzes ON;
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 1)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (1, NULL, N'E2E Automated Verification Quiz', N'', 80, 30);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 2)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (2, NULL, N'E2E Automated Verification Quiz', N'', 80, 30);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 4)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (4, 1, N'Kiem Tra Kien Thuc Lap Trinh Chuyen Sau', N'', 80, 45);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 5)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (5, 2, N'Tr?c nghi?m: Spring Boot 3 Architecture & Microservices', N'Ki?m tra ki?n th?c v? Dependency Injection, Bean Lifecycle và REST Controller.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 6)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (6, 5, N'Tr?c nghi?m: React Hooks & State Management', N'Ðánh giá hi?u bi?t sâu v? useState, useEffect, useCallback và TypeScript trong React.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 7)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (7, 7, N'Tr?c nghi?m: Machine Learning Co B?n & Thu Vi?n Python', N'Ki?m tra ki?n th?c v? Supervised vs Unsupervised Learning, Scikit-learn & Overfitting.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 8)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (8, 9, N'Tr?c nghi?m: Docker Containers & Kubernetes Pods', N'Ðánh giá ki?n th?c v? containerization, Dockerfile và K8s orchestrator.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 9)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (9, 12, N'Tr?c nghi?m Ki?n th?c Ph?n 1 - L?p trình ?ng d?ng Di d?ng Ða ', N'Ki?m tra m?c d? n?m b?t ki?n th?c n?n t?ng.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 10)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (10, 13, N'Tr?c nghi?m Th?c hành D? án - L?p trình ?ng d?ng Di d?ng Ða ', N'Ðánh giá kh? nang áp d?ng vào d? án th?c chi?n.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 11)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (11, 14, N'Tr?c nghi?m Ki?n th?c Ph?n 1 - Thi?t k? UI/UX Chuyên nghi?p v', N'Ki?m tra m?c d? n?m b?t ki?n th?c n?n t?ng.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 12)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (12, 15, N'Tr?c nghi?m Th?c hành D? án - Thi?t k? UI/UX Chuyên nghi?p v', N'Ðánh giá kh? nang áp d?ng vào d? án th?c chi?n.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 13)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (13, 16, N'Tr?c nghi?m Ki?n th?c Ph?n 1 - K? nang Qu?n tr? D? án Ph?n m?', N'Ki?m tra m?c d? n?m b?t ki?n th?c n?n t?ng.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 14)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (14, 17, N'Tr?c nghi?m Th?c hành D? án - K? nang Qu?n tr? D? án Ph?n m?', N'Ðánh giá kh? nang áp d?ng vào d? án th?c chi?n.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 15)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (15, 18, N'Tr?c nghi?m Ki?n th?c Ph?n 1 - Xây d?ng ?ng d?ng Generative A', N'Ki?m tra m?c d? n?m b?t ki?n th?c n?n t?ng.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 16)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (16, 19, N'Tr?c nghi?m Th?c hành D? án - Xây d?ng ?ng d?ng Generative A', N'Ðánh giá kh? nang áp d?ng vào d? án th?c chi?n.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 17)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (17, 20, N'Tr?c nghi?m Ki?n th?c Ph?n 1 - L?p trình C#', N'Ki?m tra m?c d? n?m b?t ki?n th?c n?n t?ng.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 18)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (18, 21, N'Tr?c nghi?m Th?c hành D? án - L?p trình C#', N'Ðánh giá kh? nang áp d?ng vào d? án th?c chi?n.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 19)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (19, 22, N'Tr?c nghi?m: Ki?n trúc NestJS & DI', N'Ki?m tra ki?n th?c v? Controllers, Services và co ch? Dependency Injection.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 20)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (20, 23, N'Tr?c nghi?m: TypeORM & B?o m?t JWT', N'Ðánh giá kh? nang qu?n lý Database Entity và b?o m?t phiên dang nh?p.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 21)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (21, 24, N'Tr?c nghi?m: Goroutines & Channels trong Go', N'Ðánh giá s? hi?u bi?t v? mô hình CSP concurrency c?a ngôn ng? Go.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 22)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (22, 25, N'Tr?c nghi?m: Gin & gRPC High Performance', N'Ðánh giá ki?n th?c v? gRPC so v?i REST.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 23)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (23, 26, N'Tr?c nghi?m: Ch? s? Kinh doanh & SQL Analytics', N'Ki?m tra ki?n th?c tính toán ch? s? tài chính và truy v?n phân tích.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 24)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (24, 27, N'Tr?c nghi?m: DAX & Tr?c quan hóa Power BI', N'Ki?m tra k? nang s? d?ng hàm DAX và thi?t k? dashboard.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 25)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (25, 28, N'Tr?c nghi?m: Ð?c hi?u Báo cáo Tài chính', N'Ðánh giá kh? nang nh?n di?n các kho?n m?c tài s?n và l?i nhu?n.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 26)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (26, 29, N'Tr?c nghi?m: Ð?nh giá & G?i v?n', N'Ki?m tra ki?n th?c v? các phuong pháp d?nh giá.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 27)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (27, 30, N'Tr?c nghi?m: Ph?u Bán Hàng B2B', N'Ðánh giá hi?u bi?t v? bán hàng doanh nghi?p.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 28)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (28, 31, N'Tr?c nghi?m: K? nang Ðàm phán', N'Ki?m tra khái ni?m BATNA trong dàm phán.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 29)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (29, 32, N'Tr?c nghi?m: Tu duy Lean Startup', N'Ðánh giá tri?t lý tinh g?n và th? nghi?m gi? thuy?t kinh doanh.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 30)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (30, 33, N'Tr?c nghi?m: Product-Market Fit', N'Ki?m tra m?c d? s?n sàng tang tru?ng quy mô.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 31)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (31, 34, N'Tr?c nghi?m: Công c? Photoshop', N'Ki?m tra hi?u bi?t v? Layer Mask và H? màu RGB/CMYK.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 32)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (32, 35, N'Tr?c nghi?m: Ð? h?a Vector Illustrator', N'Ðánh giá hi?u bi?t v? d? h?a vector.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 33)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (33, 36, N'Tr?c nghi?m: K? thu?t D?ng Video', N'Ki?m tra k? thu?t biên t?p video chuyên nghi?p.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 34)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (34, 37, N'Tr?c nghi?m: After Effects & Motion Design', N'Ðánh giá ki?n th?c v? keyframe và gia t?c chuy?n d?ng.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 35)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (35, 38, N'Tr?c nghi?m: Phân lo?i Typography', N'Ki?m tra ki?n th?c v? font ch? có chân và không chân.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 36)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (36, 39, N'Tr?c nghi?m: Brand Guidelines', N'Ki?m tra quy chu?n xu?t file in ?n và không gian an toàn.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 37)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (37, 40, N'Tr?c nghi?m: Thao tác 3D trong Blender', N'Ki?m tra các phím t?t và thao tác d?ng hình 3D.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 38)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (38, 41, N'Tr?c nghi?m: V?t li?u & Render Engine', N'Ðánh giá hi?u bi?t v? Eevee và Cycles.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 39)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (39, 42, N'Tr?c nghi?m: Chi?n lu?c Marketing', N'Ki?m tra ki?n th?c m?c tiêu SMART và mô hình SWOT.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 40)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (40, 43, N'Tr?c nghi?m: Ph?u AARRR', N'Ðánh giá hi?u bi?t v? ph?u tang tru?ng Pirate Metrics.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 41)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (41, 44, N'Tr?c nghi?m: Thu?t toán Qu?ng cáo', N'Ki?m tra ki?n th?c d?u th?u và theo dõi chuy?n d?i.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 42)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (42, 45, N'Tr?c nghi?m: T?i uu Qu?ng cáo Video', N'Ðánh giá k? nang t?i uu t? l? xem h?t video (VTR).', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 43)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (43, 46, N'Tr?c nghi?m: Ý d?nh Tìm ki?m (Search Intent)', N'Ki?m tra phân lo?i Intent: Informational, Navigational, Commercial, Transactional.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 44)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (44, 47, N'Tr?c nghi?m: Technical SEO', N'Ðánh giá ki?n th?c v? th? Canonical và Robots.txt.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 45)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (45, 48, N'Tr?c nghi?m: Ð?nh v? Thuong hi?u Cá nhân', N'Ki?m tra hi?u bi?t v? b?n s?c cá nhân và phân khúc khán gi?.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 46)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (46, 49, N'Tr?c nghi?m: Chuy?n d?i Khách hàng', N'Ðánh giá k? nang monetizing t?p khán gi?.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 47)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (47, 50, N'Tr?c nghi?m: M?ng & Quét L? H?ng', N'Ki?m tra k? nang s? d?ng Nmap và phân tích giao th?c TCP/IP.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 48)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (48, 51, N'Tr?c nghi?m: OWASP Top 10 Web Vulnerabilities', N'Ðánh giá hi?u bi?t v? cách vá l? h?ng ?ng d?ng web.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 49)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (49, 52, N'Tr?c nghi?m: Ch? m?c Database & T?i uu', N'Ki?m tra ki?n th?c v? Clustered Index và SARGable queries.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 50)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (50, 53, N'Tr?c nghi?m: ACID & Transaction Isolation', N'Ðánh giá m?c d? hi?u bi?t v? Deadlock và Isolation levels.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 51)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (51, 54, N'Tr?c nghi?m: Khái ni?m Automation Testing', N'Ki?m tra ki?n th?c v? POM pattern.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 52)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (52, 55, N'Tr?c nghi?m: CI/CD Testing', N'Ðánh giá vi?c ch?y headless browser trong pipeline.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 53)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (53, 56, N'Tr?c nghi?m: Phong cách Lãnh d?o', N'Ðánh giá hi?u bi?t v? lãnh d?o ph?c v? (Servant Leadership).', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 54)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (54, 57, N'Tr?c nghi?m: Ph?n h?i Xây d?ng', N'Ki?m tra k? nang feedback theo mô hình SBI (Situation - Behavior - Impact).', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 55)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (55, 58, N'Tr?c nghi?m: Hi?u ?ng Bullwhip Effect', N'Ki?m tra s? dao d?ng thông tin nhu c?u d?c chu?i cung ?ng.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 56)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (56, 59, N'Tr?c nghi?m: Mô hình EOQ', N'Ðánh giá cách t?i uu hóa t?ng chi phí d?t hàng và luu kho.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 57)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (57, 60, N'Tr?c nghi?m: C?u trúc OKR', N'Ki?m tra quy t?c thi?t l?p Key Results do lu?ng du?c.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 58)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (58, 61, N'Tr?c nghi?m: Check-in OKR', N'Ðánh giá vi?c duy trì van hóa check-in liên t?c.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 59)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (59, 62, N'Tr?c nghi?m: Phuong pháp UX Research', N'Ki?m tra k? nang d?t câu h?i m? và thang do SUS.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 60)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (60, 63, N'Tr?c nghi?m: Usability Testing', N'Ðánh giá k? nang di?u ph?i bu?i ki?m th? tr?i nghi?m.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 61)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (61, 64, N'Tr?c nghi?m: Design Tokens & Variables', N'Ki?m tra ki?n th?c v? bi?n màu và bi?n kho?ng cách trong Figma.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 62)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (62, 65, N'Tr?c nghi?m: Atomic Design', N'Ðánh giá phân c?p Atoms, Molecules, Organisms.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 63)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (63, 66, N'Tr?c nghi?m: Quy chu?n iOS Design', N'Ki?m tra kích thu?c vùng ch?m t?i thi?u và Safe Area.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 64)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (64, 67, N'Tr?c nghi?m: Material Design & Prototyping', N'Ðánh giá vi?c áp d?ng Material Design 3.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 65)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (65, 68, N'Tr?c nghi?m: Công th?c Vi?t Content', N'Ki?m tra ki?n th?c v? các khung vi?t bài qu?ng cáo bán hàng.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 66)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (66, 69, N'Tr?c nghi?m: K? thu?t K? chuy?n (Storytelling)', N'Ðánh giá vi?c d?t khách hàng là trung tâm c?a câu chuy?n.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 67)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (67, 70, N'Tr?c nghi?m: Google Search Ads', N'Ki?m tra ki?n th?c v? Ði?m ch?t lu?ng và Ad Rank.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 68)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (68, 71, N'Tr?c nghi?m: Performance Max Ads', N'Ðánh giá vi?c s? d?ng máy h?c trong qu?ng cáo PMax.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 69)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (69, 72, N'Tr?c nghi?m: Qu?n tr? Kênh Social', N'Ki?m tra ki?n th?c do lu?ng hi?u qu? truy?n thông.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 70)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (70, 73, N'Tr?c nghi?m: X? lý Kh?ng ho?ng', N'Ðánh giá các bu?c x? lý kh?ng ho?ng dúng d?n.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 71)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (71, 74, N'Tr?c nghi?m: Email Segmentation', N'Ki?m tra hi?u bi?t v? cá nhân hóa email ti?p th?.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 72)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (72, 75, N'Tr?c nghi?m: K? thu?t Giao thu (Deliverability)', N'Ðánh giá các giao th?c xác th?c tên mi?n g?i thu.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 73)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (73, 76, N'Tr?c nghi?m: AWS Networking & Compute', N'Ki?m tra các câu h?i th?c chi?n chu?n d? thi SAA-C03.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 74)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (74, 77, N'Tr?c nghi?m: RDS & Serverless', N'Ðánh giá kh? nang thi?t k? ki?n trúc ch?u l?i (Fault-tolerant).', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 75)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (75, 78, N'Tr?c nghi?m: Quy?n H?n & Dòng L?nh Linux', N'Ki?m tra ki?n th?c v? giá tr? bát phân c?a quy?n h?n file trong Linux.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 76)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (76, 79, N'Tr?c nghi?m: Cron Schedule & Bash', N'Ðánh giá vi?c lên l?ch tác v? t? d?ng b?ng Crontab.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 77)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (77, 80, N'Tr?c nghi?m: Ki?n trúc Kubernetes', N'Ki?m tra vai trò c?a API Server, etcd, kubelet.', 80, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.quizzes WHERE quiz_id = 78)
  INSERT INTO dbo.quizzes (quiz_id, course_section_id, title, description, passing_score, time_limit_minutes) VALUES (78, 81, N'Tr?c nghi?m: K8s Networking & Autoscaling', N'Ðánh giá vi?c c?u hình d?nh tuy?n và t? d?ng co giãn pods.', 80, 15);
SET IDENTITY_INSERT dbo.quizzes OFF;
GO

-- 5. NẠP CÂU HỎI TRẮC NGHIỆM (QUESTIONS)
SET IDENTITY_INSERT dbo.questions ON;
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 1)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (1, 1, N'', N'', 10);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 2)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (2, 2, N'Spring Boot 3 default routing is case sensitive?', N'', 10);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 3)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (3, 4, N'Trong Spring Boot 3, controller co the ho tro ca PascalCase va camelCase routing khong?', N'', 10);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 4)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (4, 5, N'Annotation nào du?c dùng d? dánh d?u m?t Class là Service trong t?ng nghi?p v? c?a Spring Boot?', N'@Service là annotation chuyên bi?t trong Spring Framework cho t?ng business logic.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 5)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (5, 5, N'Annotation @Autowired trong Spring d?i di?n cho nguyên lý nào sau dây?', N'@Autowired th?c hi?n Dependency Injection (DI) - tiêm các ph? thu?c t? d?ng t? Spring IoC Container.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 6)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (6, 5, N'Công c? nào thu?ng du?c dùng làm API Gateway trong h? sinh thái Spring Cloud?', N'Spring Cloud Gateway là gi?i pháp API Gateway hi?n d?i, hi?u nang cao trên n?n Spring WebFlux.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 7)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (7, 6, N'Hook nào du?c dùng d? th?c hi?n các side-effect nhu g?i API, dang ký event listener?', N'useEffect du?c kích ho?t sau khi component render d? x? lý các side-effects.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 8)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (8, 6, N'Khi m?ng ph? thu?c (dependency array) c?a useEffect là r?ng [], hook s? ch?y khi nào?', N'Khi dependency array là [], effect ch? ch?y duy nh?t 1 l?n sau khi component mount l?n d?u.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 9)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (9, 6, N'TypeScript interface khác type alias ? di?m n?i b?t nào?', N'Interface có th? m? r?ng (declaration merging) b?ng cách khai báo trùng tên nhi?u l?n.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 10)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (10, 7, N'Thu?t toán Phân lo?i (Classification) thu?c nhóm h?c máy nào?', N'Classification thu?c H?c có giám sát (Supervised Learning) vì t?p d? li?u hu?n luy?n có s?n nhãn (label).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 11)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (11, 7, N'Hi?n tu?ng Overfitting (quá kh?p) x?y ra khi nào?', N'Overfitting x?y ra khi mô hình h?c quá k? các chi ti?t và nhi?u trên t?p train, d?n d?n d? doán kém trên t?p test.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 12)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (12, 8, N'Ðon v? tri?n khai nh? nh?t trong Kubernetes là gì?', N'Pod là don v? th?c thi nh? nh?t trong Kubernetes, có th? ch?a m?t ho?c nhi?u container chia s? chung m?ng và luu tr?.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 13)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (13, 8, N'L?nh Docker nào du?c dùng d? xây d?ng m?t image t? Dockerfile?', N'docker build -t <image_name> . dùng d? dóng gói ?ng d?ng thành container image.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 14)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (14, 9, N'M?c tiêu quan tr?ng nh?t khi b?t d?u m?t d? án là gì?', N'Xác d?nh rõ yêu c?u, ph?m vi và thi?t l?p môi tru?ng chu?n là bu?c ti?n d? s?ng còn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 15)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (15, 9, N'T?i sao c?n s? d?ng h? th?ng qu?n lý phiên b?n (nhu Git)?', N'Git giúp theo dõi l?ch s? thay d?i, ph?i h?p làm vi?c nhóm và khôi ph?c mã ngu?n an toàn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 16)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (16, 10, N'Nguyên t?c thi?t k? ph?n m?m DRY vi?t t?t c?a di?u gì?', N'Don''t Repeat Yourself - Nh?m gi?m thi?u vi?c l?p l?i mã ngu?n, tang tính tái s? d?ng và d? b?o trì.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 17)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (17, 11, N'M?c tiêu quan tr?ng nh?t khi b?t d?u m?t d? án là gì?', N'Xác d?nh rõ yêu c?u, ph?m vi và thi?t l?p môi tru?ng chu?n là bu?c ti?n d? s?ng còn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 18)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (18, 11, N'T?i sao c?n s? d?ng h? th?ng qu?n lý phiên b?n (nhu Git)?', N'Git giúp theo dõi l?ch s? thay d?i, ph?i h?p làm vi?c nhóm và khôi ph?c mã ngu?n an toàn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 19)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (19, 12, N'Nguyên t?c thi?t k? ph?n m?m DRY vi?t t?t c?a di?u gì?', N'Don''t Repeat Yourself - Nh?m gi?m thi?u vi?c l?p l?i mã ngu?n, tang tính tái s? d?ng và d? b?o trì.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 20)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (20, 13, N'M?c tiêu quan tr?ng nh?t khi b?t d?u m?t d? án là gì?', N'Xác d?nh rõ yêu c?u, ph?m vi và thi?t l?p môi tru?ng chu?n là bu?c ti?n d? s?ng còn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 21)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (21, 13, N'T?i sao c?n s? d?ng h? th?ng qu?n lý phiên b?n (nhu Git)?', N'Git giúp theo dõi l?ch s? thay d?i, ph?i h?p làm vi?c nhóm và khôi ph?c mã ngu?n an toàn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 22)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (22, 14, N'Nguyên t?c thi?t k? ph?n m?m DRY vi?t t?t c?a di?u gì?', N'Don''t Repeat Yourself - Nh?m gi?m thi?u vi?c l?p l?i mã ngu?n, tang tính tái s? d?ng và d? b?o trì.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 23)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (23, 15, N'M?c tiêu quan tr?ng nh?t khi b?t d?u m?t d? án là gì?', N'Xác d?nh rõ yêu c?u, ph?m vi và thi?t l?p môi tru?ng chu?n là bu?c ti?n d? s?ng còn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 24)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (24, 15, N'T?i sao c?n s? d?ng h? th?ng qu?n lý phiên b?n (nhu Git)?', N'Git giúp theo dõi l?ch s? thay d?i, ph?i h?p làm vi?c nhóm và khôi ph?c mã ngu?n an toàn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 25)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (25, 16, N'Nguyên t?c thi?t k? ph?n m?m DRY vi?t t?t c?a di?u gì?', N'Don''t Repeat Yourself - Nh?m gi?m thi?u vi?c l?p l?i mã ngu?n, tang tính tái s? d?ng và d? b?o trì.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 26)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (26, 17, N'M?c tiêu quan tr?ng nh?t khi b?t d?u m?t d? án là gì?', N'Xác d?nh rõ yêu c?u, ph?m vi và thi?t l?p môi tru?ng chu?n là bu?c ti?n d? s?ng còn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 27)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (27, 17, N'T?i sao c?n s? d?ng h? th?ng qu?n lý phiên b?n (nhu Git)?', N'Git giúp theo dõi l?ch s? thay d?i, ph?i h?p làm vi?c nhóm và khôi ph?c mã ngu?n an toàn.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 28)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (28, 18, N'Nguyên t?c thi?t k? ph?n m?m DRY vi?t t?t c?a di?u gì?', N'Don''t Repeat Yourself - Nh?m gi?m thi?u vi?c l?p l?i mã ngu?n, tang tính tái s? d?ng và d? b?o trì.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 29)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (29, 19, N'Decorator nào dùng d? d?nh nghia m?t Controller trong NestJS?', N'@Controller() là decorator c?a NestJS d? khai báo m?t class là REST Controller.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 30)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (30, 19, N'Ð? th?c hi?n Dependency Injection cho m?t Service, class dó ph?i có decorator nào?', N'@Injectable() thông báo cho NestJS IoC container bi?t class này có th? du?c inject vào noi khác.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 31)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (31, 20, N'Trong TypeORM, Migration có vai trò chính là gì?', N'Migration giúp theo dõi và c?p nh?t lu?c d? database d?ng b? an toàn qua các phiên b?n mã ngu?n.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 32)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (32, 21, N'T? khóa nào dùng d? kh?i ch?y m?t hàm ch?y b?t d?ng b? du?i d?ng Goroutine?', N'T? khóa ''go'' d?t tru?c l?i g?i hàm s? kh?i t?o m?t goroutine nh? m?i.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 33)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (33, 22, N'gRPC s? d?ng d?nh d?ng truy?n t?i d? li?u nào theo m?c d?nh?', N'gRPC s? d?ng Protocol Buffers (Protobuf) ? d?nh d?ng nh? phân, hi?u nang cao hon JSON r?t nhi?u.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 34)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (34, 23, N'Ch? s? CAC trong kinh doanh là vi?t t?t c?a thu?t ng? nào?', N'Customer Acquisition Cost - Chi phí d? thu hút và có du?c m?t khách hàng m?i.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 35)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (35, 24, N'Hàm DAX nào thu?ng du?c dùng d? tính toán có di?u ki?n linh ho?t nh?t?', N'CALCULATE là hàm m?nh m? nh?t trong DAX dùng d? thay d?i filter context.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 36)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (36, 25, N'Phuong trình k? toán co b?n trên B?ng cân d?i k? toán là gì?', N'T?ng Tài s?n = T?ng N? ph?i tr? + V?n ch? s? h?u (Assets = Liabilities + Equity).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 37)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (37, 26, N'Ch? s? P/E do lu?ng di?u gì?', N'Price-to-Earnings Ratio do lu?ng giá tr? th? tru?ng c?a c? phi?u so v?i l?i nhu?n trên m?i c? ph?n.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 38)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (38, 27, N'B2B là vi?t t?t c?a mô hình kinh doanh nào?', N'Business-to-Business: Giao d?ch thuong m?i tr?c ti?p gi?a các doanh nghi?p.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 39)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (39, 28, N'Thu?t ng? BATNA trong lý thuy?t dàm phán có nghia là gì?', N'Best Alternative to a Negotiated Agreement: Phuong án thay th? t?t nh?t khi cu?c dàm phán không d?t th?a thu?n.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 40)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (40, 29, N'M?c tiêu c?t lõi c?a S?n ph?m kh? d?ng t?i thi?u (MVP) là gì?', N'MVP giúp h?c h?i và ki?m ch?ng các gi? d?nh th? tru?ng v?i chi phí và th?i gian ít nh?t.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 41)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (41, 30, N'Khi nào m?t startup nên b?t d?u d? ti?n tang t?c ti?p th? di?n r?ng?', N'Ch? khi s?n ph?m dã d?t Product-Market Fit và t? l? gi? chân khách hàng (retention) ?n d?nh.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 42)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (42, 31, N'Trong Layer Mask c?a Photoshop, màu den trên mask có tác d?ng gì?', N'Màu den che gi?u (?n) ph?n hình ?nh, màu tr?ng hi?n th? (Black conceals, White reveals).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 43)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (43, 32, N'Uu di?m vu?t tr?i nh?t c?a d? h?a Vector so v?i Raster là gì?', N'Ð? h?a Vector d?a trên các thu?t toán toán h?c nên có th? phóng to thu nh? tùy ý mà không b? v? h?t (pixelation).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 44)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (44, 33, N'T?c d? khung hình (Frame Rate) tiêu chu?n cho video chu?n di?n ?nh thông thu?ng là bao nhiêu?', N'24 fps (frames per second) là t?c d? khung hình tiêu chu?n di?n ?nh truy?n th?ng.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 45)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (45, 34, N'Phím t?t nào trong After Effects dùng d? chuy?n keyframe thành Easy Ease (làm mu?t chuy?n d?ng)?', N'Phím F9 du?c dùng ph? bi?n d? chuy?n d?i linear keyframe sang Easy Ease trong After Effects.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 46)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (46, 35, N'Font ch? Serif có d?c di?m nh?n d?ng n?i b?t nào?', N'Font Serif có các nét g?ch chân nh? ? các d?u ch?, t?o c?m giác truy?n th?ng, sang tr?ng.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 47)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (47, 36, N'Không gian an toàn (Clear Space) c?a Logo có tác d?ng gì?', N'Clear space d?m b?o không có ch? ho?c y?u t? d? h?a nào xâm l?n, gi? s? rõ ràng và nh?n di?n c?a logo.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 48)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (48, 37, N'Phím t?t nào trong Blender dùng d? chuy?n d?i gi?a Object Mode và Edit Mode?', N'Phím Tab là phím t?t th?n thánh d? chuy?n d?i gi?a ch? d? xem d?i tu?ng và ch? d? ch?nh s?a lu?i.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 49)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (49, 38, N'Engine render nào trong Blender s? d?ng phuong pháp Ray Tracing d? tính toán ánh sáng v?t lý chính xác?', N'Cycles là render engine ray tracing v?t lý chính xác cao c?a Blender.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 50)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (50, 39, N'Ch? ''M'' trong tiêu chí thi?t l?p m?c tiêu SMART d?i di?n cho di?u gì?', N'Measurable - Có th? do lu?ng du?c b?ng các con s? c? th?.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 51)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (51, 40, N'Ch? s? nào sau dây do lu?ng m?c d? gi? chân ngu?i dùng ? l?i ti?p t?c s? d?ng s?n ph?m?', N'Retention Rate (T? l? gi? chân) do lu?ng ph?n tram ngu?i dùng quay l?i sau kho?ng th?i gian nh?t d?nh.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 52)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (52, 41, N'Conversions API (CAPI) gi?i quy?t v?n d? gì l?n nh?t c?a Facebook Pixel truy?n th?ng?', N'CAPI g?i s? ki?n tr?c ti?p t? Server thay vì trình duy?t, giúp vu?t qua các h?n ch? ch?n cookie và iOS 14+ tracking.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 53)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (53, 42, N'Ch? s? Hook Rate trên video TikTok Ads do lu?ng di?u gì?', N'Hook Rate do t? l? ngu?i xem ? l?i sau 2-3 giây d?u tiên c?a video.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 54)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (54, 43, N'Ngu?i dùng gõ ''mua laptop gaming dell giá r?'' th? hi?n Search Intent gì?', N'Transactional Intent - Ngu?i dùng có ý d?nh mua s?m và th?c hi?n giao d?ch c? th?.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 55)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (55, 44, N'Th? <link rel=''canonical''> du?c dùng v?i m?c dích chính là gì?', N'Th? Canonical ch? d?nh URL g?c d? tránh hình ph?t n?i dung trùng l?p (duplicate content).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 56)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (56, 45, N'Y?u t? c?t lõi nh?t d? duy trì thuong hi?u cá nhân b?n v?ng lâu dài là gì?', N'S? nh?t quán và tính xác th?c (Authenticity) mang l?i ni?m tin sâu s?c cho c?ng d?ng.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 57)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (57, 46, N'M?t ph?u chuy?n d?i thuong hi?u cá nhân thông thu?ng di theo trình t? nào?', N'Bi?t (Awareness) -> Thích (Interest) -> Tin (Trust) -> Mua (Purchase).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 58)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (58, 47, N'Ki?u quét SYN Scan (Nmap -sS) còn du?c g?i là gì vì không hoàn t?t b?t tay 3 bu?c?', N'Half-open scanning - Vì ch? g?i gói SYN, nh?n SYN/ACK r?i g?i RST mà không hoàn t?t 3-way handshake.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 59)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (59, 48, N'Gi?i pháp tri?t d? và hi?u qu? nh?t d? ngan ch?n l? h?ng SQL Injection là gì?', N'Parameterized Queries (Prepared Statements) tách b?ch hoàn toàn gi?a l?nh SQL và d? li?u d?u vào.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 60)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (60, 49, N'M?t b?ng trong SQL Server có th? có t?i da bao nhiêu Clustered Index?', N'Ch? có t?i da 1 Clustered Index vì nó quy?t d?nh th? t? luu tr? v?t lý c?a các dòng d? li?u trên dia.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 61)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (61, 50, N'C?p d? cô l?p giao d?ch nào ngan ch?n du?c hoàn toàn hi?n tu?ng Phantom Read?', N'SERIALIZABLE là c?p d? cô l?p nghiêm ng?t nh?t ngan ch?n du?c c? Dirty Read, Non-repeatable Read và Phantom Read.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 62)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (62, 51, N'L?i ích l?n nh?t c?a thi?t k? Page Object Model (POM) trong ki?m th? t? d?ng là gì?', N'POM tách bi?t logic giao di?n v?i test scripts, giúp b?o trì mã ngu?n d? dàng khi UI thay d?i.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 63)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (63, 52, N'Thu?t ng? ''Headless Browser'' nghia là trình duy?t ch?y nhu th? nào?', N'Ch?y trong n?n không c?n giao di?n d? h?a (GUI), t?i uu t?c d? và dùng t?t trong môi tru?ng server CI/CD.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 64)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (64, 53, N'Tri?t lý ''Servant Leadership'' (Lãnh d?o ph?c v?) d?t tr?ng tâm vào di?u gì?', N'T?p trung ph?c v?, h? tr? và phát tri?n ti?m nang c?a nhân viên d? cùng d?t m?c tiêu chung.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 65)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (65, 54, N'Mô hình ph?n h?i SBI bao g?m 3 y?u t? nào?', N'Situation (Tình hu?ng) - Behavior (Hành vi) - Impact (Tác d?ng).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 66)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (66, 55, N'Hi?u ?ng Bullwhip (Chi?c roi da) trong chu?i cung ?ng miêu t? hi?n tu?ng gì?', N'S? bi?n d?ng nhu c?u khách hàng b? phóng d?i d?n khi truy?n ngu?c v? phía nhà s?n xu?t ban d?u.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 67)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (67, 56, N'Mô hình EOQ cân b?ng gi?a hai lo?i chi phí nào?', N'Cân b?ng gi?a Chi phí d?t hàng (Ordering Cost) và Chi phí luu kho (Holding/Carrying Cost).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 68)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (68, 57, N'M?t Key Result (K?t qu? then ch?t) chu?n m?c b?t bu?c ph?i có tính ch?t gì?', N'Key Result ph?i luôn d?nh lu?ng du?c b?ng con s? rõ ràng (s? lu?ng, ph?n tram, doanh thu).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 69)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (69, 58, N'M?c dích quan tr?ng nh?t c?a bu?i Check-in OKR hàng tu?n là gì?', N'Nh?n di?n s?m các tr? ng?i, th?o lu?n gi?i pháp và tang s? t?p trung vào m?c tiêu uu tiên.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 70)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (70, 59, N'Thang do SUS (System Usability Scale) dùng d? dánh giá y?u t? nào?', N'Ðo lu?ng m?c d? d? s? d?ng và tính kh? d?ng c?a m?t h? th?ng ho?c s?n ph?m ph?n m?m.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 71)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (71, 60, N'Theo chuyên gia Jakob Nielsen, s? lu?ng ngu?i tham gia t?i uu cho m?t d?t Usability Test d? phát hi?n 85% l?i là bao nhiêu?', N'Kho?ng 5 ngu?i dùng th? nghi?m d?i di?n là d? d? phát hi?n ph?n l?n các v?n d? nghiêm tr?ng.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 72)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (72, 61, N'Design Tokens dóng vai trò là c?u n?i c?t lõi gi?a hai b? ph?n nào?', N'Tokens chu?n hóa các giá tr? thi?t k? thành d? li?u có th? dùng chung gi?a Designer và Developer.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 73)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (73, 62, N'Trong phuong pháp lu?n Atomic Design c?a Brad Frost, c?p d? nh? nh?t là gì?', N'Atoms (Nguyên t?) là thành ph?n co b?n nh?t không th? chia nh? nhu màu s?c, font ch?, icon.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 74)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (74, 63, N'Kích thu?c vùng ch?m c?m ?ng t?i thi?u du?c Apple khuy?n ngh? là bao nhiêu di?m (pt)?', N'Apple HIG khuy?n ngh? vùng ch?m t?i thi?u là 44 x 44 pt d? ngón tay b?m d? dàng.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 75)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (75, 64, N'Kích thu?c vùng ch?m t?i thi?u du?c Google Material Design khuy?n ngh? là bao nhiêu dp?', N'Material Design quy d?nh kích thu?c m?c tiêu c?m ?ng t?i thi?u là 48 x 48 dp.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 76)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (76, 65, N'Công th?c AIDA trong ti?p th? n?i dung là vi?t t?t c?a chu?i nào?', N'Attention (Chú ý) - Interest (H?ng thú) - Desire (Khao khát) - Action (Hành d?ng).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 77)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (77, 66, N'Trong câu chuy?n thuong hi?u thành công, ai dóng vai trò là ''Ngu?i hùng'' (Hero)?', N'Khách hàng luôn là Ngu?i hùng, còn Thuong hi?u dóng vai trò là Ngu?i d?n du?ng (Guide).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 78)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (78, 67, N'Th? h?ng qu?ng cáo (Ad Rank) c?a Google du?c tính b?ng công th?c co b?n nào?', N'Ad Rank = Giá th?u t?i da (Max CPC) x Ði?m ch?t lu?ng (Quality Score) + Tác d?ng c?a ti?n ích m? r?ng.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 79)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (79, 68, N'Qu?ng cáo Performance Max (PMax) c?a Google t? d?ng phân ph?i trên nh?ng kênh nào?', N'PMax phân ph?i xuyên su?t toàn b? h? sinh thái: Tìm ki?m, YouTube, M?ng hi?n th?, Gmail, Maps và Khám phá.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 80)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (80, 69, N'T? l? tuong tác (Engagement Rate) trên m?t bài vi?t thu?ng du?c tính theo t? l? nào?', N'T?ng lu?t tuong tác (Like, Comment, Share) chia cho T?ng s? ngu?i ti?p c?n (Reach) ho?c Lu?t hi?n th? (Impressions).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 81)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (81, 70, N'Hành vi nào sau dây là T?I K? nh?t khi doanh nghi?p g?p kh?ng ho?ng truy?n thông?', N'Xóa bài dang tiêu c?c, né tránh ho?c d? l?i cho khách hàng ch? khi?n du lu?n ph?n n? d? d?i hon.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 82)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (82, 71, N'Vi?c phân khúc danh sách email (List Segmentation) mang l?i l?i ích l?n nh?t nào?', N'G?i n?i dung cá nhân hóa dúng nhu c?u t?ng nhóm ngu?i nh?n, tang t? l? m? và chuy?n d?i don hàng.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 83)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (83, 72, N'Ba b?n ghi DNS tiêu chu?n nào giúp xác th?c danh tính máy ch? g?i email h?p pháp?', N'SPF, DKIM và DMARC là b? ba giao th?c xác th?c email qu?c t? ch?ng gi? m?o.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 84)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (84, 73, N'Lo?i Load Balancer nào c?a AWS ho?t d?ng ? t?ng ?ng d?ng Layer 7 và h? tr? d?nh tuy?n theo path/host?', N'Application Load Balancer (ALB) ho?t d?ng ? t?ng Layer 7 c?a mô hình OSI.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 85)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (85, 73, N'D?ch v? luu tr? d?i tu?ng (Object Storage) ph? bi?n nh?t trên AWS là d?ch v? nào?', N'Amazon Simple Storage Service (Amazon S3) là d?ch v? object storage hàng d?u th? gi?i v?i d? b?n 99.999999999% (11 s? 9).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 86)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (86, 74, N'Tính nang RDS Multi-AZ Deployment ch? y?u ph?c v? cho m?c tiêu nào sau dây?', N'Multi-AZ t? d?ng nhân b?n d?ng b? sang m?t Availability Zone khác d? d?m b?o tính s?n sàng cao và t? d?ng ph?c h?i khi có s? c?.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 87)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (87, 75, N'L?nh ''chmod 755 filename'' thi?t l?p quy?n cho Ch? s? h?u (Owner) là gì?', N'Ch? s? d?u tiên 7 = 4 (read) + 2 (write) + 1 (execute) -> Quy?n d?c, ghi và th?c thi (rwx).', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 88)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (88, 76, N'Bi?u th?c cron ''0 2 * * *'' s? kích ho?t l?nh ch?y vào th?i di?m nào?', N'Phút 0, Gi? 2 m?i ngày -> Lúc 02:00 sáng hàng ngày.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 89)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (89, 77, N'Thành ph?n nào trong Control Plane c?a Kubernetes luu tr? tr?ng thái nh?t quán c?a toàn b? c?m?', N'etcd là co s? d? li?u key-value phân tán luu tr? toàn b? tr?ng thái và c?u hình c?a c?m K8s.', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.questions WHERE question_id = 90)
  INSERT INTO dbo.questions (question_id, quiz_id, content, explanation, points) VALUES (90, 78, N'Lo?i Service nào trong Kubernetes là m?c d?nh và ch? cho phép giao ti?p n?i b? trong c?m?', N'ClusterIP là lo?i Service m?c d?nh, cung c?p m?t IP n?i b? ch? có th? truy c?p du?c t? bên trong c?m.', 1);
SET IDENTITY_INSERT dbo.questions OFF;
GO

-- 6. NẠP ĐÁP ÁN (ANSWERS)
SET IDENTITY_INSERT dbo.answers ON;
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 1)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (1, 1, N'', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 2)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (2, 1, N'', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 3)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (3, 2, N'True', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 4)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (4, 2, N'False', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 5)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (5, 3, N'Co, su dung mang String trong @RequestMapping', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 6)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (6, 3, N'Khong the lam duoc', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 7)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (7, 4, N'@Service', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 8)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (8, 4, N'@Controller', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 9)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (9, 4, N'@Repository', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 10)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (10, 4, N'@Configuration', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 11)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (11, 5, N'Dependency Injection (DI)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 12)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (12, 5, N'Polymorphism (Ða hình)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 13)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (13, 5, N'Encapsulation (Ðóng gói)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 14)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (14, 5, N'Inheritance (K? th?a)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 15)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (15, 6, N'Spring Cloud Gateway', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 16)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (16, 6, N'Spring Data JPA', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 17)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (17, 6, N'Spring Security', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 18)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (18, 6, N'Spring Batch', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 19)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (19, 7, N'useEffect', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 20)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (20, 7, N'useState', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 21)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (21, 7, N'useMemo', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 22)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (22, 7, N'useRef', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 23)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (23, 8, N'Ch? 1 l?n duy nh?t sau l?n render d?u tiên (mount)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 24)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (24, 8, N'M?i khi component re-render', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 25)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (25, 8, N'Ch? khi component b? h?y (unmount)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 26)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (26, 8, N'Không bao gi? ch?y', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 27)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (27, 9, N'Interface h? tr? Declaration Merging', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 28)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (28, 9, N'Interface ch?y du?c ? runtime', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 29)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (29, 9, N'Type alias không d?nh nghia du?c object', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 30)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (30, 9, N'Interface không h? tr? generic', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 31)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (31, 10, N'Supervised Learning (H?c có giám sát)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 32)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (32, 10, N'Unsupervised Learning (H?c không giám sát)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 33)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (33, 10, N'Reinforcement Learning (H?c tang cu?ng)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 34)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (34, 10, N'Self-supervised Learning', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 35)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (35, 11, N'Mô hình d?t d? chính xác r?t cao trên t?p Train nhung kém trên t?p Test', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 36)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (36, 11, N'Mô hình d? doán kém trên c? t?p Train l?n t?p Test', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 37)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (37, 11, N'D? li?u hu?n luy?n quá ít', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 38)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (38, 11, N'Mô hình có quá ít tham s?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 39)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (39, 12, N'Pod', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 40)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (40, 12, N'Container', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 41)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (41, 12, N'Node', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 42)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (42, 12, N'Service', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 43)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (43, 13, N'docker build', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 44)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (44, 13, N'docker run', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 45)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (45, 13, N'docker create', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 46)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (46, 13, N'docker commit', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 47)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (47, 14, N'Xác d?nh rõ ràng m?c tiêu, yêu c?u và ki?n trúc h? th?ng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 48)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (48, 14, N'Vi?t mã ngay l?p t?c không c?n thi?t k?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 49)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (49, 14, N'B? qua các bu?c ki?m th? d? ti?t ki?m th?i gian', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 50)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (50, 14, N'Không c?n thi?t l?p môi tru?ng chu?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 51)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (51, 15, N'Theo dõi l?ch s? thay d?i và h? tr? làm vi?c nhóm hi?u qu?', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 52)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (52, 15, N'Ð? tang t?c d? ph?n c?ng máy tính', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 53)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (53, 15, N'T? d?ng s?a l?i cú pháp trong code', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 54)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (54, 15, N'B?t bu?c ph?i tr? phí duy trì hàng tháng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 55)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (55, 16, N'Don''t Repeat Yourself', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 56)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (56, 16, N'Do Right Yourself', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 57)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (57, 16, N'Data Run Yearly', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 58)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (58, 16, N'Design Rule Yield', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 59)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (59, 17, N'Xác d?nh rõ ràng m?c tiêu, yêu c?u và ki?n trúc h? th?ng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 60)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (60, 17, N'Vi?t mã ngay l?p t?c không c?n thi?t k?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 61)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (61, 17, N'B? qua các bu?c ki?m th? d? ti?t ki?m th?i gian', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 62)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (62, 17, N'Không c?n thi?t l?p môi tru?ng chu?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 63)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (63, 18, N'Theo dõi l?ch s? thay d?i và h? tr? làm vi?c nhóm hi?u qu?', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 64)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (64, 18, N'Ð? tang t?c d? ph?n c?ng máy tính', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 65)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (65, 18, N'T? d?ng s?a l?i cú pháp trong code', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 66)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (66, 18, N'B?t bu?c ph?i tr? phí duy trì hàng tháng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 67)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (67, 19, N'Don''t Repeat Yourself', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 68)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (68, 19, N'Do Right Yourself', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 69)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (69, 19, N'Data Run Yearly', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 70)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (70, 19, N'Design Rule Yield', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 71)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (71, 20, N'Xác d?nh rõ ràng m?c tiêu, yêu c?u và ki?n trúc h? th?ng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 72)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (72, 20, N'Vi?t mã ngay l?p t?c không c?n thi?t k?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 73)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (73, 20, N'B? qua các bu?c ki?m th? d? ti?t ki?m th?i gian', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 74)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (74, 20, N'Không c?n thi?t l?p môi tru?ng chu?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 75)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (75, 21, N'Theo dõi l?ch s? thay d?i và h? tr? làm vi?c nhóm hi?u qu?', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 76)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (76, 21, N'Ð? tang t?c d? ph?n c?ng máy tính', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 77)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (77, 21, N'T? d?ng s?a l?i cú pháp trong code', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 78)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (78, 21, N'B?t bu?c ph?i tr? phí duy trì hàng tháng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 79)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (79, 22, N'Don''t Repeat Yourself', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 80)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (80, 22, N'Do Right Yourself', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 81)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (81, 22, N'Data Run Yearly', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 82)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (82, 22, N'Design Rule Yield', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 83)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (83, 23, N'Xác d?nh rõ ràng m?c tiêu, yêu c?u và ki?n trúc h? th?ng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 84)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (84, 23, N'Vi?t mã ngay l?p t?c không c?n thi?t k?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 85)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (85, 23, N'B? qua các bu?c ki?m th? d? ti?t ki?m th?i gian', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 86)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (86, 23, N'Không c?n thi?t l?p môi tru?ng chu?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 87)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (87, 24, N'Theo dõi l?ch s? thay d?i và h? tr? làm vi?c nhóm hi?u qu?', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 88)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (88, 24, N'Ð? tang t?c d? ph?n c?ng máy tính', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 89)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (89, 24, N'T? d?ng s?a l?i cú pháp trong code', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 90)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (90, 24, N'B?t bu?c ph?i tr? phí duy trì hàng tháng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 91)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (91, 25, N'Don''t Repeat Yourself', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 92)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (92, 25, N'Do Right Yourself', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 93)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (93, 25, N'Data Run Yearly', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 94)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (94, 25, N'Design Rule Yield', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 95)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (95, 26, N'Xác d?nh rõ ràng m?c tiêu, yêu c?u và ki?n trúc h? th?ng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 96)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (96, 26, N'Vi?t mã ngay l?p t?c không c?n thi?t k?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 97)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (97, 26, N'B? qua các bu?c ki?m th? d? ti?t ki?m th?i gian', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 98)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (98, 26, N'Không c?n thi?t l?p môi tru?ng chu?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 99)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (99, 27, N'Theo dõi l?ch s? thay d?i và h? tr? làm vi?c nhóm hi?u qu?', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 100)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (100, 27, N'Ð? tang t?c d? ph?n c?ng máy tính', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 101)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (101, 27, N'T? d?ng s?a l?i cú pháp trong code', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 102)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (102, 27, N'B?t bu?c ph?i tr? phí duy trì hàng tháng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 103)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (103, 28, N'Don''t Repeat Yourself', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 104)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (104, 28, N'Do Right Yourself', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 105)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (105, 28, N'Data Run Yearly', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 106)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (106, 28, N'Design Rule Yield', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 107)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (107, 29, N'@Controller()', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 108)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (108, 29, N'@RestController()', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 109)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (109, 29, N'@Service()', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 110)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (110, 29, N'@Injectable()', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 111)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (111, 30, N'@Injectable()', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 112)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (112, 30, N'@Provider()', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 113)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (113, 30, N'@Bean()', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 114)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (114, 30, N'@AutoWired()', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 115)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (115, 31, N'Qu?n lý và d?ng b? lu?c d? co s? d? li?u có ki?m soát phiên b?n', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 116)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (116, 31, N'Tang t?c d? truy v?n m?ng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 117)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (117, 31, N'T? d?ng sao luu d? li?u dám mây', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 118)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (118, 31, N'Mã hóa m?t kh?u ngu?i dùng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 119)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (119, 32, N'go', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 120)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (120, 32, N'async', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 121)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (121, 32, N'thread', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 122)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (122, 32, N'spawn', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 123)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (123, 33, N'Protocol Buffers (Protobuf)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 124)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (124, 33, N'JSON', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 125)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (125, 33, N'XML', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 126)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (126, 33, N'YAML', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 127)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (127, 34, N'Customer Acquisition Cost', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 128)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (128, 34, N'Company Asset Cost', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 129)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (129, 34, N'Client Activity Charge', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 130)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (130, 34, N'Cost And Cashflow', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 131)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (131, 35, N'CALCULATE', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 132)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (132, 35, N'SUMX', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 133)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (133, 35, N'FILTER', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 134)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (134, 35, N'RELATED', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 135)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (135, 36, N'Tài s?n = N? ph?i tr? + V?n ch? s? h?u', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 136)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (136, 36, N'Tài s?n = Doanh thu - Chi phí', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 137)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (137, 36, N'Tài s?n = L?i nhu?n g?p + Ti?n m?t', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 138)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (138, 36, N'Tài s?n = V?n góp - Chi phí thu?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 139)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (139, 37, N'T? giá gi?a th? giá c? phi?u và l?i nhu?n trên m?i c? ph?n', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 140)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (140, 37, N'T?ng l?i nhu?n ròng c?a công ty', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 141)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (141, 37, N'T? l? n? trên v?n ch? s? h?u', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 142)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (142, 37, N'Chi phí kh?u hao tài s?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 143)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (143, 38, N'Business to Business', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 144)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (144, 38, N'Business to Consumer', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 145)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (145, 38, N'Brand to Business', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 146)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (146, 38, N'Buyer to Buyer', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 147)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (147, 39, N'Phuong án thay th? t?t nh?t cho m?t th?a thu?n dàm phán', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 148)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (148, 39, N'M?c giá t?i da có th? ch?p nh?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 149)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (149, 39, N'M?c tiêu l?i nhu?n cao nh?t', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 150)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (150, 39, N'Chi?n thu?t gây áp l?c th?i gian', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 151)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (151, 40, N'Ki?m ch?ng gi? d?nh th? tru?ng v?i ngu?n l?c t?i thi?u', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 152)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (152, 40, N'T?o ra m?t s?n ph?m hoàn h?o d?y d? tính nang', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 153)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (153, 40, N'Thu h?i v?n d?u tu ngay l?p t?c', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 154)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (154, 40, N'Ð? qu?ng cáo trên truy?n hình', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 155)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (155, 41, N'Khi dã ch?ng minh du?c Product-Market Fit và retention t?t', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 156)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (156, 41, N'Ngay ngày d?u tiên ra m?t s?n ph?m', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 157)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (157, 41, N'Khi chua có ngu?i dùng nào th? nghi?m', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 158)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (158, 41, N'Khi d?i th? c?nh tranh chua xu?t hi?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 159)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (159, 42, N'?n (che gi?u) n?i dung c?a layer dó', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 160)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (160, 42, N'Hi?n th? toàn b? n?i dung', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 161)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (161, 42, N'Tô màu den lên ?nh', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 162)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (162, 42, N'Xóa vinh vi?n layer', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 163)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (163, 43, N'Phóng to thu nh? kích thu?c tùy ý mà không b? v? hình', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 164)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (164, 43, N'Dung lu?ng file luôn n?ng hon', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 165)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (165, 43, N'Ch? xem du?c trên máy tính', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 166)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (166, 43, N'Không th? in ?n ra gi?y', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 167)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (167, 44, N'24 fps', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 168)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (168, 44, N'60 fps', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 169)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (169, 44, N'12 fps', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 170)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (170, 44, N'120 fps', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 171)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (171, 45, N'F9', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 172)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (172, 45, N'F5', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 173)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (173, 45, N'Ctrl + Z', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 174)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (174, 45, N'Shift + Enter', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 175)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (175, 46, N'Có các nét g?ch chân nh? ? ph?n duôi ch?', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 176)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (176, 46, N'Hoàn toàn tròn tron không nét th?a', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 177)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (177, 46, N'Vi?t tay u?n lu?n t? do', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 178)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (178, 46, N'Ch? có th? vi?t hoa', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 179)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (179, 47, N'Ð?m b?o không b? các thành ph?n khác l?n chi?m, gi? tính rõ ràng cho logo', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 180)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (180, 47, N'Tang kích thu?c file khi in ?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 181)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (181, 47, N'T? d?ng d?i màu logo khi n?n t?i', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 182)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (182, 47, N'Làm m? n?n xung quanh logo', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 183)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (183, 48, N'Tab', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 184)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (184, 48, N'Space', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 185)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (185, 48, N'Shift + A', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 186)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (186, 48, N'Z', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 187)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (187, 49, N'Cycles', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 188)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (188, 49, N'Eevee', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 189)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (189, 49, N'Workbench', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 190)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (190, 49, N'Octane', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 191)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (191, 50, N'Measurable (Có th? do lu?ng)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 192)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (192, 50, N'Manageable (Có th? qu?n tr?)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 193)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (193, 50, N'Marketable (Có th? ti?p th?)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 194)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (194, 50, N'Meaningful (Có ý nghia)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 195)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (195, 51, N'Retention Rate', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 196)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (196, 51, N'Bounce Rate', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 197)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (197, 51, N'Click-Through Rate (CTR)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 198)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (198, 51, N'Churn Rate', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 199)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (199, 52, N'G?i d? li?u tr?c ti?p t? Server, tránh b? ch?n b?i trình duy?t và iOS', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 200)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (200, 52, N'T? d?ng gi?m 50% chi phí qu?ng cáo', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 201)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (201, 52, N'T? d?ng t?o video qu?ng cáo', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 202)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (202, 52, N'Không c?n thi?t l?p tài kho?n qu?ng cáo', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 203)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (203, 53, N'T? l? ngu?i xem ? l?i trong 2-3 giây d?u tiên', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 204)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (204, 53, N'T?ng s? lu?t chia s? video', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 205)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (205, 53, N'S? ti?n dã chi tiêu cho qu?ng cáo', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 206)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (206, 53, N'T? l? mua hàng thành công', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 207)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (207, 54, N'Transactional (Giao d?ch / Mua s?m)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 208)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (208, 54, N'Informational (Thu th?p thông tin)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 209)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (209, 54, N'Navigational (Ði?u hu?ng tìm website)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 210)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (210, 54, N'Educational (H?c t?p lý thuy?t)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 211)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (211, 55, N'Gi?i quy?t v?n d? trùng l?p n?i dung gi?a các URL gi?ng nhau', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 212)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (212, 55, N'Tang t?c d? t?i trang', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 213)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (213, 55, N'Chèn ?nh d?i di?n lên m?ng xã h?i', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 214)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (214, 55, N'Ch?n Google bot thu th?p d? li?u', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 215)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (215, 56, N'S? nh?t quán v? giá tr? và tính chân th?c (Authenticity)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 216)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (216, 56, N'Chi th?t nhi?u ti?n qu?ng cáo ch?y theo trend ng?n h?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 217)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (217, 56, N'Sao chép nguyên b?n phong cách c?a ngu?i n?i ti?ng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 218)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (218, 56, N'Ðang t?i bài vi?t không ki?m soát s? lu?ng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 219)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (219, 57, N'Bi?t -> Thích -> Tin tu?ng -> Mua hàng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 220)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (220, 57, N'Mua hàng -> Bi?t -> Thích', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 221)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (221, 57, N'Qu?ng cáo -> Ép mua -> Ðóng kênh', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 222)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (222, 57, N'Bán hàng ngay l?n d?u g?p m?t', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 223)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (223, 58, N'Half-open scanning', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 224)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (224, 58, N'Full TCP connect scan', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 225)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (225, 58, N'UDP ping scan', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 226)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (226, 58, N'Zombie idle scan', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 227)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (227, 59, N'S? d?ng Parameterized Queries (Prepared Statements)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 228)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (228, 59, N'T?t hi?n th? l?i database', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 229)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (229, 59, N'Mã hóa MD5 câu truy v?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 230)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (230, 59, N'Ch? dùng phuong th?c POST thay vì GET', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 231)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (231, 60, N'1', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 232)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (232, 60, N'2', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 233)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (233, 60, N'256', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 234)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (234, 60, N'Không gi?i h?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 235)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (235, 61, N'SERIALIZABLE', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 236)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (236, 61, N'READ COMMITTED', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 237)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (237, 61, N'READ UNCOMMITTED', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 238)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (238, 61, N'REPEATABLE READ', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 239)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (239, 62, N'Tách bi?t gi?a d?nh v? ph?n t? UI và các ca ki?m th?, d? b?o trì', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 240)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (240, 62, N'Làm test case ch?y nhanh g?p 10 l?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 241)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (241, 62, N'T? d?ng phát hi?n l?i b?o m?t', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 242)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (242, 62, N'Không c?n vi?t assertion ki?m tra', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 243)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (243, 63, N'Ch?y ng?m không có giao di?n d? h?a ngu?i dùng (GUI)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 244)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (244, 63, N'Trình duy?t không h? tr? JavaScript', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 245)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (245, 63, N'Trình duy?t không có thanh tìm ki?m', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 246)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (246, 63, N'Ch? dùng cho di?n tho?i', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 247)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (247, 64, N'H? tr?, ph?c v? và phát tri?n nhân viên d? d?t thành công chung', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 248)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (248, 64, N'Dùng quy?n l?c áp d?t c?p du?i tuân l?nh tuy?t d?i', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 249)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (249, 64, N'Giao toàn b? công vi?c và không can thi?p', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 250)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (250, 64, N'Ch? t?p trung vào l?i nhu?n ng?n h?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 251)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (251, 65, N'Situation - Behavior - Impact', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 252)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (252, 65, N'Strategy - Budget - Innovation', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 253)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (253, 65, N'Solution - Business - Idea', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 254)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (254, 65, N'Skill - Background - Interest', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 255)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (255, 66, N'S? bi?n d?ng nhu c?u b? khu?ch d?i ngày càng l?n khi di ngu?c lên d?u chu?i', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 256)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (256, 66, N'Hàng hóa b? hu h?ng trong quá trình v?n chuy?n du?ng bi?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 257)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (257, 66, N'Giá nguyên v?t li?u d?t ng?t gi?m', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 258)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (258, 66, N'Nhà cung c?p t? ch?i giao hàng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 259)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (259, 67, N'Chi phí d?t hàng và Chi phí luu kho', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 260)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (260, 67, N'Chi phí mua hàng và Chi phí nhân s?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 261)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (261, 67, N'Chi phí b?o hi?m và Chi phí thu?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 262)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (262, 67, N'Chi phí qu?ng cáo và Chi phí v?n chuy?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 263)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (263, 68, N'Ph?i do lu?ng du?c b?ng các con s? c? th?', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 264)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (264, 68, N'Ch? mang tính c?m tính d?nh tính', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 265)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (265, 68, N'Không có h?n chót hoàn thành', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 266)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (266, 68, N'Ch? dành riêng cho ban giám d?c', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 267)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (267, 69, N'Nh?n di?n khó khan, th?o lu?n phuong án h? tr? k?p th?i', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 268)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (268, 69, N'Ch? trích và ph?t l?i nhân viên', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 269)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (269, 69, N'Báo cáo ti?n d? cho phòng nhân s?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 270)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (270, 69, N'Vi?t l?i toàn b? m?c tiêu m?i', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 271)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (271, 70, N'Ðo lu?ng tính kh? d?ng (Usability) c?a h? th?ng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 272)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (272, 70, N'Ðo t?c d? ph?n h?i c?a server', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 273)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (273, 70, N'Ðo d? phân gi?i màn hình ngu?i dùng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 274)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (274, 70, N'Ðo lu?ng doanh thu t? ngu?i dùng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 275)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (275, 71, N'Kho?ng 5 ngu?i', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 276)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (276, 71, N'Kho?ng 50 ngu?i', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 277)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (277, 71, N'T?i thi?u 100 ngu?i', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 278)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (278, 71, N'Ch? c?n 1 ngu?i duy nh?t', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 279)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (279, 72, N'B? ph?n Thi?t k? (Designer) và Phát tri?n ph?n m?m (Developer)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 280)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (280, 72, N'B? ph?n Marketing và K? toán', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 281)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (281, 72, N'B? ph?n Bán hàng và Khách hàng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 282)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (282, 72, N'Ban Giám d?c và Nhà d?u tu', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 283)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (283, 73, N'Atoms (Nguyên t?)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 284)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (284, 73, N'Molecules (Phân t?)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 285)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (285, 73, N'Organisms (Sinh v?t)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 286)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (286, 73, N'Templates (B?n m?u)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 287)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (287, 74, N'44 x 44 pt', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 288)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (288, 74, N'24 x 24 pt', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 289)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (289, 74, N'64 x 64 pt', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 290)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (290, 74, N'100 x 100 pt', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 291)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (291, 75, N'48 x 48 dp', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 292)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (292, 75, N'32 x 32 dp', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 293)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (293, 75, N'56 x 56 dp', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 294)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (294, 75, N'40 x 40 dp', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 295)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (295, 76, N'Attention - Interest - Desire - Action', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 296)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (296, 76, N'Action - Idea - Direct - Advertising', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 297)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (297, 76, N'Audience - Information - Data - Analysis', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 298)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (298, 76, N'Article - Image - Design - Audio', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 299)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (299, 77, N'Khách hàng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 300)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (300, 77, N'Ch? doanh nghi?p', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 301)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (301, 77, N'Nhân viên ti?p th?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 302)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (302, 77, N'S?n ph?m c?a công ty', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 303)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (303, 78, N'Giá th?u x Ði?m ch?t lu?ng + Tác d?ng ti?n ích m? r?ng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 304)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (304, 78, N'Ch? d?a vào ai tr? ti?n th?u cao nh?t', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 305)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (305, 78, N'D?a vào d? tu?i tài kho?n qu?ng cáo', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 306)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (306, 78, N'D?a vào s? lu?ng ký t? c?a tiêu d?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 307)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (307, 79, N'Toàn b? h? sinh thái: Search, YouTube, Display, Gmail, Maps và Discover', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 308)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (308, 79, N'Ch? hi?n th? trên k?t qu? tìm ki?m Google', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 309)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (309, 79, N'Ch? phát trên ?ng d?ng YouTube', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 310)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (310, 79, N'Ch? g?i email qu?ng cáo vào h?p thu d?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 311)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (311, 80, N'T?ng tuong tác chia cho t?ng lu?t ti?p c?n (Reach)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 312)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (312, 80, N'S? ngu?i b?m mua hàng chia cho s? like', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 313)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (313, 80, N'Th?i gian bài vi?t du?c dang t?i', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 314)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (314, 80, N'S? lu?ng ?nh dính kèm bài vi?t', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 315)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (315, 81, N'Xóa bình lu?n tiêu c?c, né tránh và d? l?i cho khách hàng', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 316)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (316, 81, N'L?ng nghe ý ki?n và nhanh chóng xác minh s? th?t n?i b?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 317)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (317, 81, N'Thành l?p ban ch? d?o kh?ng ho?ng ph?n ?ng nhanh', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 318)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (318, 81, N'Ðua ra thông di?p chân thành và bi?n pháp kh?c ph?c c? th?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 319)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (319, 82, N'Tang tính liên quan c?a n?i dung, nâng cao t? l? m? và nh?p chu?t', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 320)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (320, 82, N'Giúp g?i thu không c?n internet', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 321)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (321, 82, N'T? d?ng d?i m?t kh?u hòm thu ngu?i nh?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 322)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (322, 82, N'Làm email tr? nên dài hon', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 323)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (323, 83, N'SPF, DKIM và DMARC', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 324)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (324, 83, N'HTTP, HTTPS và SSL', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 325)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (325, 83, N'FTP, SSH và Telnet', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 326)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (326, 83, N'SQL, NoSQL và Redis', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 327)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (327, 84, N'Application Load Balancer (ALB)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 328)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (328, 84, N'Network Load Balancer (NLB)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 329)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (329, 84, N'Classic Load Balancer (CLB)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 330)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (330, 84, N'Gateway Load Balancer (GLB)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 331)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (331, 85, N'Amazon S3', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 332)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (332, 85, N'Amazon EBS', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 333)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (333, 85, N'Amazon EFS', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 334)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (334, 85, N'Amazon DynamoDB', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 335)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (335, 86, N'Ð?m b?o tính s?n sàng cao (High Availability) và t? d?ng Failover', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 336)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (336, 86, N'Tang t?c d? truy v?n d?c d? li?u báo cáo', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 337)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (337, 86, N'Gi?m dung lu?ng ? dia luu tr?', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 338)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (338, 86, N'Mi?n phí cu?c m?ng internet', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 339)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (339, 87, N'Ð?c, Ghi và Th?c thi (rwx)', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 340)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (340, 87, N'Ch? Ð?c (r--)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 341)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (341, 87, N'Ð?c và Ghi (rw-)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 342)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (342, 87, N'Ch? Th?c thi (--x)', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 343)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (343, 88, N'Vào lúc 02:00 sáng m?i ngày', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 344)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (344, 88, N'M?i 2 phút m?t l?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 345)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (345, 88, N'Vào 00:02 hàng tháng', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 346)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (346, 88, N'Ch? ch?y vào th? Hai hàng tu?n', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 347)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (347, 89, N'etcd', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 348)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (348, 89, N'kube-scheduler', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 349)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (349, 89, N'kube-proxy', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 350)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (350, 89, N'kubelet', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 351)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (351, 90, N'ClusterIP', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 352)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (352, 90, N'NodePort', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 353)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (353, 90, N'LoadBalancer', 0);
IF NOT EXISTS (SELECT 1 FROM dbo.answers WHERE answer_id = 354)
  INSERT INTO dbo.answers (answer_id, question_id, content, is_correct) VALUES (354, 90, N'ExternalName', 0);
SET IDENTITY_INSERT dbo.answers OFF;
GO

-- 7. NẠP ĐÁNH GIÁ HỌC VIÊN (REVIEWS)
SET IDENTITY_INSERT dbo.reviews ON;
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 1)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (1, 2, 5, 5, N'Khóa học Java Spring Boot xuất sắc nhất mình từng học! Thầy Cường giảng rất chi tiết từ tầng kiến trúc đến tối ưu câu lệnh truy vấn MSSQL. Dự án thực tế sát với yêu cầu tuyển dụng.', 'POSITIVE', 0.98, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 2)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (2, 3, 8, 5, N'Giáo trình React 18 và Next.js 14 cực kỳ cập nhật, mã nguồn sạch đẹp. Mình đã tự tin làm chủ Server Components và áp dụng ngay vào công việc.', 'POSITIVE', 0.96, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 3)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (3, 4, 9, 5, N'Thầy Long dạy phần Machine Learning và Sentiment Analysis rất dễ hiểu. Cách thầy hướng dẫn toán giải tích ứng dụng vào PyTorch thực sự khai sáng tư duy.', 'POSITIVE', 0.99, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 4)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (4, 5, 10, 5, N'Khóa DevOps của cô Nhung rất tuyệt vời! Các bài thực hành Docker và cụm Kubernetes trên AWS chạy mượt mà, tài liệu hướng dẫn rất đầy đủ.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 5)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (5, 2, 11, 5, N'Cảm ơn thầy Cường rất nhiều! Nhờ kiến thức Microservices và Kafka trong khóa học mà mình đã pass phỏng vấn vị trí Senior Backend Engineer.', 'POSITIVE', 0.98, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 6)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (6, 3, 12, 5, N'Video bài giảng chất lượng cao, âm thanh sắc nét, các bài tập thực hành Redux Toolkit rất bổ ích cho người mới làm quen.', 'POSITIVE', 0.93, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 7)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (7, 4, 13, 5, N'Rất ấn tượng với phần tích hợp API FastAPI của mô hình Học máy lên Docker. Một khóa học AI thực chiến đúng nghĩa.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 8)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (8, 5, 14, 5, N'Giải thích cơ chế CI/CD Pipeline trên GitHub Actions rất trực quan. Giúp tối ưu thời gian deploy của team mình đi rất nhiều.', 'POSITIVE', 0.92, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 9)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (9, 2, 15, 4, N'Nội dung chuyên sâu nhưng tốc độ giảng của thầy ở chương 2 hơi nhanh đối với người mới bắt đầu. Bù lại tài liệu đính kèm rất đầy đủ.', 'NEUTRAL', 0.58, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 10)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (10, 3, 16, 4, N'Khóa học khá ổn, tổng hợp đủ các kiến thức cơ bản đến nâng cao. Hy vọng thầy bổ sung thêm chuyên đề về React Server Actions mới nhất.', 'NEUTRAL', 0.52, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 11)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (11, 3, 10, 3, N'Âm lượng ở bài học 2.2 hơi nhỏ so với các bài trước, phải đeo tai nghe mới nghe rõ. Đề nghị ban quản trị chỉnh sửa lại âm thanh video.', 'NEGATIVE', 0.28, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 12)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (12, 4, 11, 3, N'Cần có thêm nhiều bài tập tình huống thực tế và case study giải quyết lỗi mô hình overfitting để người học luyện tập nhiều hơn.', 'NEGATIVE', 0.32, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 13)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (13, 12, 9, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 14)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (14, 12, 14, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 15)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (15, 13, 16, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 16)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (16, 13, 12, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 17)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (17, 14, 9, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 18)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (18, 14, 11, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 19)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (19, 15, 10, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 20)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (20, 15, 13, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 21)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (21, 16, 5, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 22)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (22, 16, 8, 5, N'Nội dung cực kỳ chất lượng, lộ trình bài bản từ cơ bản đến nâng cao. Rất đáng đồng tiền bát gạo!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 23)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (23, 17, 8, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 24)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (24, 17, 16, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 25)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (25, 18, 12, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 26)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (26, 18, 16, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 27)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (27, 19, 16, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 28)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (28, 19, 12, 5, N'Nội dung cực kỳ chất lượng, lộ trình bài bản từ cơ bản đến nâng cao. Rất đáng đồng tiền bát gạo!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 29)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (29, 20, 14, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 30)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (30, 20, 5, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 31)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (31, 21, 13, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 32)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (32, 21, 16, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 33)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (33, 22, 12, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 34)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (34, 22, 15, 5, N'Khóa học rất chi tiết và thực tế! Giảng viên nhiệt tình giải đáp thắc mắc, bài tập sát với thực tế đi làm.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 35)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (35, 23, 10, 5, N'Khóa học rất chi tiết và thực tế! Giảng viên nhiệt tình giải đáp thắc mắc, bài tập sát với thực tế đi làm.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 36)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (36, 23, 11, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 37)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (37, 24, 9, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 38)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (38, 24, 13, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 39)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (39, 25, 13, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 40)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (40, 25, 14, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 41)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (41, 26, 8, 5, N'Khóa học rất chi tiết và thực tế! Giảng viên nhiệt tình giải đáp thắc mắc, bài tập sát với thực tế đi làm.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 42)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (42, 26, 15, 5, N'Khóa học rất chi tiết và thực tế! Giảng viên nhiệt tình giải đáp thắc mắc, bài tập sát với thực tế đi làm.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 43)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (43, 27, 16, 5, N'Nội dung cực kỳ chất lượng, lộ trình bài bản từ cơ bản đến nâng cao. Rất đáng đồng tiền bát gạo!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 44)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (44, 27, 5, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 45)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (45, 28, 11, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 46)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (46, 28, 14, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 47)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (47, 29, 11, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 48)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (48, 29, 8, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 49)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (49, 30, 10, 5, N'Khóa học rất chi tiết và thực tế! Giảng viên nhiệt tình giải đáp thắc mắc, bài tập sát với thực tế đi làm.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 50)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (50, 30, 14, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 51)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (51, 31, 9, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 52)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (52, 31, 13, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 53)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (53, 32, 8, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 54)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (54, 32, 16, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 55)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (55, 33, 15, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 56)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (56, 33, 10, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 57)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (57, 34, 14, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 58)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (58, 34, 13, 5, N'Nội dung cực kỳ chất lượng, lộ trình bài bản từ cơ bản đến nâng cao. Rất đáng đồng tiền bát gạo!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 59)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (59, 35, 14, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 60)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (60, 35, 15, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 61)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (61, 36, 10, 5, N'Khóa học rất chi tiết và thực tế! Giảng viên nhiệt tình giải đáp thắc mắc, bài tập sát với thực tế đi làm.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 62)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (62, 36, 8, 5, N'Khóa học rất chi tiết và thực tế! Giảng viên nhiệt tình giải đáp thắc mắc, bài tập sát với thực tế đi làm.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 63)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (63, 37, 11, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 64)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (64, 37, 10, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 65)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (65, 38, 14, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 66)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (66, 38, 15, 5, N'Giọng giảng viên truyền cảm, giải thích các khái niệm phức tạp rất dễ hiểu. 10/10 điểm!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 67)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (67, 39, 5, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 68)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (68, 39, 16, 5, N'Kiến thức cập nhật phiên bản mới nhất, hệ thống quiz sau mỗi chương giúp củng cố kiến thức rất tốt.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 69)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (69, 40, 12, 5, N'Nội dung cực kỳ chất lượng, lộ trình bài bản từ cơ bản đến nâng cao. Rất đáng đồng tiền bát gạo!', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 70)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (70, 40, 9, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 71)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (71, 41, 16, 4, N'Rất hài lòng với nội dung khóa học. Tài liệu đính kèm đầy đủ và có source code tham khảo chi tiết.', 'POSITIVE', 0.95, GETDATE());
IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE review_id = 72)
  INSERT INTO dbo.reviews (review_id, course_id, user_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES (72, 41, 11, 4, N'Khóa học hay, nhiều ví dụ thực tế. Phần thực hành dự án cuối khóa giúp mình tự tin phỏng vấn hơn nhiều.', 'POSITIVE', 0.95, GETDATE());
SET IDENTITY_INSERT dbo.reviews OFF;
GO
