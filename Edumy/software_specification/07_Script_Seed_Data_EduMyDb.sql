-- ====================================================================================
-- DỰ ÁN: EDUMY ELEARNING PLATFORM (Spring Boot 3.2 + MSSQL + React 18)
-- KỊCH BẢN NẠP SEED DATA TƯƠNG THÍCH CHÍNH XÁC SCHEMA HIBERNATE & MSSQL
-- TỰ ĐỘNG CHUẨN HÓA NVARCHAR ĐỂ KHÔNG BAO GIỜ BỊ LỖI DẤU TIẾNG VIỆT
-- ====================================================================================

USE EduMyDb;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

PRINT N'>>> 1. CHUẨN HÓA TẤT CẢ CÁC CỘT VĂN BẢN SANG NVARCHAR (CHỐNG LỖI FONT 100%)...';

-- Chuyển đổi các cột sang NVARCHAR
ALTER TABLE dbo.users ALTER COLUMN full_name NVARCHAR(255) NULL;
ALTER TABLE dbo.users ALTER COLUMN bio NVARCHAR(MAX) NULL;
ALTER TABLE dbo.users ALTER COLUMN role NVARCHAR(50) NULL;

ALTER TABLE dbo.categories ALTER COLUMN name NVARCHAR(100) NULL;
ALTER TABLE dbo.categories ALTER COLUMN description NVARCHAR(500) NULL;

ALTER TABLE dbo.tags ALTER COLUMN name NVARCHAR(100) NULL;

ALTER TABLE dbo.courses ALTER COLUMN title NVARCHAR(255) NULL;
ALTER TABLE dbo.courses ALTER COLUMN slug NVARCHAR(255) NULL;
ALTER TABLE dbo.courses ALTER COLUMN description NVARCHAR(MAX) NULL;
ALTER TABLE dbo.courses ALTER COLUMN level NVARCHAR(50) NULL;
ALTER TABLE dbo.courses ALTER COLUMN status NVARCHAR(50) NULL;

ALTER TABLE dbo.course_sections ALTER COLUMN title NVARCHAR(255) NULL;
ALTER TABLE dbo.lessons ALTER COLUMN title NVARCHAR(255) NULL;
ALTER TABLE dbo.lessons ALTER COLUMN video_url NVARCHAR(500) NULL;

ALTER TABLE dbo.orders ALTER COLUMN status NVARCHAR(50) NULL;
ALTER TABLE dbo.enrollments ALTER COLUMN status NVARCHAR(50) NULL;

ALTER TABLE dbo.reviews ALTER COLUMN comment NVARCHAR(MAX) NULL;
ALTER TABLE dbo.reviews ALTER COLUMN sentiment_label NVARCHAR(50) NULL;

ALTER TABLE dbo.certificates ALTER COLUMN certificate_url NVARCHAR(255) NULL;

ALTER TABLE dbo.course_ml_analyses ALTER COLUMN primary_category NVARCHAR(100) NULL;
ALTER TABLE dbo.course_ml_analyses ALTER COLUMN sub_category NVARCHAR(100) NULL;
ALTER TABLE dbo.course_ml_analyses ALTER COLUMN suggested_level NVARCHAR(50) NULL;
ALTER TABLE dbo.course_ml_analyses ALTER COLUMN risk_level NVARCHAR(50) NULL;
ALTER TABLE dbo.course_ml_analyses ALTER COLUMN model_version NVARCHAR(100) NULL;
ALTER TABLE dbo.course_ml_analyses ALTER COLUMN status NVARCHAR(50) NULL;

ALTER TABLE dbo.user_activities ALTER COLUMN activity_type NVARCHAR(100) NULL;
ALTER TABLE dbo.user_activities ALTER COLUMN resource_id NVARCHAR(100) NULL;

PRINT N'   [+] Đã nâng cấp các cột văn bản sang NVARCHAR thành công.';
GO

BEGIN TRANSACTION;
BEGIN TRY

    -- ================================================================================
    -- 1. SỬA DỮ LIỆU TÀI KHOẢN CŨ BỊ LỖI KÝ TỰ & NẠP ĐẦY ĐỦ 14 USERS
    -- ================================================================================
    PRINT N'>>> 2. Chuẩn hóa và nạp tài khoản người dùng (Users)...';

    -- Sửa 2 tài khoản cũ bị lỗi font
    UPDATE dbo.users 
    SET full_name = N'ThS. Nguyễn Văn Cường', 
        bio = N'Software Architect với hơn 10 năm kinh nghiệm Java Spring Boot, Microservices và ReactJS Frontend.',
        avatar_url = N'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80',
        is_active = 1
    WHERE email = N'instructor@edumy.com';

    UPDATE dbo.users 
    SET full_name = N'Đặng Quốc Hưng', 
        bio = N'Học viên xuất sắc, đam mê kiến trúc Backend phân tán và giải thuật tối ưu.',
        avatar_url = N'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&q=80',
        is_active = 1
    WHERE email = N'student@edumy.com';

    UPDATE dbo.users
    SET full_name = N'System Administrator',
        bio = N'Quản trị viên cấp cao phụ trách hệ thống và vận hành Edumy Enterprise.',
        avatar_url = N'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80',
        is_active = 1
    WHERE email = N'admin@edumy.com';

    -- Thêm các giảng viên và học viên mới nếu chưa có
    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'long.tran@edumy.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'long.tran@edumy.com', N'TS. Trần Hoàng Long', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Instructor', 
                N'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&q=80', N'Tiến sĩ Khoa học Máy tính, chuyên gia Trí tuệ Nhân tạo & Xử lý ngôn ngữ tự nhiên NLP.', 1, DATEADD(MONTH, -7, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'nhung.le@edumy.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'nhung.le@edumy.com', N'Kỹ sư Lê Thị Hồng Nhung', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Instructor', 
                N'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&q=80', N'Senior Cloud Solutions Architect & Mobile Lead tại tập đoàn công nghệ lớn. Chuyên gia Flutter và AWS.', 1, DATEADD(MONTH, -7, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'phuong.mai@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'phuong.mai@gmail.com', N'Mai Phương Thảo', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80', N'Sinh viên năm 4 ngành CNTT Đại học Bách Khoa, mục tiêu trở thành Frontend Engineer.', 1, DATEADD(MONTH, -6, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'tuan.kiet@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'tuan.kiet@gmail.com', N'Nguyễn Tuấn Kiệt', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400&q=80', N'Data Engineer định hướng phát triển mô hình Machine Learning thực tế.', 1, DATEADD(MONTH, -5, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'minh.anh@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'minh.anh@gmail.com', N'Lê Minh Anh', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&q=80', N'DevOps Fresher đang học CI/CD và Kubernetes triển khai thực tế.', 1, DATEADD(MONTH, -5, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'hoang.duc@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'hoang.duc@gmail.com', N'Phạm Hoàng Đức', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&q=80', N'Mobile Developer mong muốn nâng cao kỹ năng Flutter & Dart.', 1, DATEADD(MONTH, -4, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'ngoc.anh@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'ngoc.anh@gmail.com', N'Vũ Ngọc Ánh', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&q=80', N'UI/UX Designer tìm kiếm giải pháp tối ưu hệ thống thiết kế Figma.', 1, DATEADD(MONTH, -4, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'quang.huy@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'quang.huy@gmail.com', N'Trịnh Quang Huy', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=400&q=80', N'Quản lý sản phẩm (Product Owner) tìm hiểu mô hình Agile Scrum.', 1, DATEADD(MONTH, -3, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'thu.trang@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'thu.trang@gmail.com', N'Bùi Thu Trang', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&q=80', N'Học viên chuyển ngành sang lập trình web toàn diện.', 1, DATEADD(MONTH, -3, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'viet.anh@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'viet.anh@gmail.com', N'Đỗ Việt Anh', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=400&q=80', N'Kỹ sư phần mềm quan tâm đến công nghệ Generative AI & LLM.', 1, DATEADD(MONTH, -2, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'bao.ngoc@gmail.com')
        INSERT INTO dbo.users (email, full_name, password_hash, role, avatar_url, bio, is_active, created_at)
        VALUES (N'bao.ngoc@gmail.com', N'Hoàng Bảo Ngọc', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
                N'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80', N'Sinh viên nghiên cứu khoa học dữ liệu và xử lý ngôn ngữ tiếng Việt.', 1, DATEADD(MONTH, -1, SYSUTCDATETIME()));

    -- Khởi tạo Giỏ hàng (carts) cho tất cả user chưa có
    INSERT INTO dbo.carts (user_id, created_at, updated_at)
    SELECT u.user_id, SYSUTCDATETIME(), SYSUTCDATETIME()
    FROM dbo.users u
    WHERE NOT EXISTS (SELECT 1 FROM dbo.carts c WHERE c.user_id = u.user_id);

    -- Đảm bảo quyền trong user_roles
    -- Lấy role IDs
    DECLARE @adminRoleId INT = (SELECT TOP 1 id FROM dbo.roles WHERE name = 'Admin');
    DECLARE @instructorRoleId INT = (SELECT TOP 1 id FROM dbo.roles WHERE name = 'Instructor');
    DECLARE @studentRoleId INT = (SELECT TOP 1 id FROM dbo.roles WHERE name = 'Student');

    IF (@adminRoleId IS NULL) BEGIN INSERT INTO dbo.roles (name) VALUES ('Admin'); SET @adminRoleId = SCOPE_IDENTITY(); END
    IF (@instructorRoleId IS NULL) BEGIN INSERT INTO dbo.roles (name) VALUES ('Instructor'); SET @instructorRoleId = SCOPE_IDENTITY(); END
    IF (@studentRoleId IS NULL) BEGIN INSERT INTO dbo.roles (name) VALUES ('Student'); SET @studentRoleId = SCOPE_IDENTITY(); END

    INSERT INTO dbo.user_roles (user_id, role_id)
    SELECT u.user_id, 
           CASE WHEN u.role = 'Admin' THEN @adminRoleId 
                WHEN u.role = 'Instructor' THEN @instructorRoleId 
                ELSE @studentRoleId END
    FROM dbo.users u
    WHERE NOT EXISTS (SELECT 1 FROM dbo.user_roles ur WHERE ur.user_id = u.user_id);

    -- ================================================================================
    -- 2. SEED CATEGORIES (DANH MỤC KHÓA HỌC)
    -- ================================================================================
    PRINT N'>>> 3. Nạp danh mục khóa học (Categories)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE name = N'Lập trình phần mềm')
        INSERT INTO dbo.categories (name, description, parent_category_id)
        VALUES (N'Lập trình phần mềm', N'Lập trình Web, Di động, Hệ thống Backend & Kiến trúc Microservices', NULL);

    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE name = N'Kinh doanh & Quản lý')
        INSERT INTO dbo.categories (name, description, parent_category_id)
        VALUES (N'Kinh doanh & Quản lý', N'Quản trị dự án Agile Scrum, Khởi nghiệp công nghệ & Tài chính', NULL);

    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE name = N'Thiết kế đồ họa & UI/UX')
        INSERT INTO dbo.categories (name, description, parent_category_id)
        VALUES (N'Thiết kế đồ họa & UI/UX', N'Thiết kế giao diện người dùng Figma, Design System & Mỹ thuật số', NULL);

    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE name = N'Marketing & Truyền thông')
        INSERT INTO dbo.categories (name, description, parent_category_id)
        VALUES (N'Marketing & Truyền thông', N'Digital Marketing, SEO kỹ thuật số và Tăng trưởng người dùng', NULL);

    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE name = N'Hạ tầng IT & Điện toán đám mây')
        INSERT INTO dbo.categories (name, description, parent_category_id)
        VALUES (N'Hạ tầng IT & Điện toán đám mây', N'Hệ điều hành Linux, Mạng máy tính, DevOps, Docker & AWS', NULL);

    -- ================================================================================
    -- 3. SEED TAGS & COUPONS
    -- ================================================================================
    PRINT N'>>> 4. Nạp Thẻ từ khóa và Mã giảm giá (Tags & Coupons)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.tags WHERE name = 'Java')
        INSERT INTO dbo.tags (name) VALUES 
        (N'Java'), (N'Spring Boot'), (N'React'), (N'Python'),
        (N'Machine Learning'), (N'SQL Server'), (N'DevOps'),
        (N'Docker'), (N'Kubernetes'), (N'Flutter'), (N'Figma'), (N'Agile');

    IF NOT EXISTS (SELECT 1 FROM dbo.coupons WHERE code = 'EDUMY50')
        INSERT INTO dbo.coupons (code, discount_percentage, expiry_date, is_active) VALUES 
        (N'WELCOME20', 20.00, DATEADD(YEAR, 2, SYSUTCDATETIME()), 1),
        (N'EDUMY50', 50.00, DATEADD(YEAR, 2, SYSUTCDATETIME()), 1),
        (N'PRODEV30', 30.00, DATEADD(YEAR, 2, SYSUTCDATETIME()), 1);

    -- ================================================================================
    -- 4. SEED COURSES (8 KHÓA HỌC HOT)
    -- ================================================================================
    PRINT N'>>> 5. Nạp danh sách Khóa học chuẩn LMS (Courses)...';
    
    -- Lấy Instructor IDs & Category IDs thực tế
    DECLARE @insCuongId INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'instructor@edumy.com');
    DECLARE @insLongId INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'long.tran@edumy.com');
    DECLARE @insNhungId INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'nhung.le@edumy.com');

    DECLARE @catDevId INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE name = N'Lập trình phần mềm');
    DECLARE @catBizId INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE name = N'Kinh doanh & Quản lý');
    DECLARE @catDesignId INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE name = N'Thiết kế đồ họa & UI/UX');
    DECLARE @catCloudId INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE name = N'Hạ tầng IT & Điện toán đám mây');

    -- Khóa 1: Spring Boot
    IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE slug = 'lap-trinh-java-spring-boot-3-microservices-thuc-chien')
        INSERT INTO dbo.courses (instructor_id, category_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at)
        VALUES (@insCuongId, @catDevId,
                N'Lập trình Java Spring Boot 3 & Microservices Thực chiến',
                N'lap-trinh-java-spring-boot-3-microservices-thuc-chien',
                N'Khóa học chuyên sâu đưa bạn từ nền tảng Spring Core, Spring Data JPA, Spring Security JWT đến kiến trúc Microservices phân tán với Spring Cloud, Docker, Kafka và Microsoft SQL Server.',
                1499000.00,
                N'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1000&q=80',
                N'Intermediate', N'Published', 4.9, 38, 0, DATEADD(MONTH, -6, SYSUTCDATETIME()), SYSUTCDATETIME());

    -- Khóa 2: ReactJS
    IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE slug = 'master-reactjs-18-nextjs-14-typescript')
        INSERT INTO dbo.courses (instructor_id, category_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at)
        VALUES (@insCuongId, @catDevId,
                N'Master ReactJS 18, Next.js 14 & TypeScript từ Zero đến Pro',
                N'master-reactjs-18-nextjs-14-typescript',
                N'Làm chủ hệ sinh thái Frontend hiện đại: React Hooks nâng cao, Redux Toolkit, Next.js App Router, SSR/SSG, TailwindCSS và tối ưu hiệu năng điểm 100 Lighthouse.',
                1290000.00,
                N'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=1000&q=80',
                N'All Levels', N'Published', 4.8, 45, 0, DATEADD(MONTH, -6, SYSUTCDATETIME()), SYSUTCDATETIME());

    -- Khóa 3: Python AI/ML
    IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE slug = 'tri-tue-nhan-tao-machine-learning-python')
        INSERT INTO dbo.courses (instructor_id, category_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at)
        VALUES (@insLongId, @catDevId,
                N'Trí tuệ Nhân tạo & Machine Learning chuyên sâu với Python',
                N'tri-tue-nhan-tao-machine-learning-python',
                N'Nắm vững toán giải tích và đại số tuyến tính trong AI, xây dựng mô hình Phân loại, Hồi quy, Xử lý ngôn ngữ tự nhiên NLP (Sentiment Analysis) với Scikit-Learn và PyTorch. Đóng gói API FastAPI.',
                1890000.00,
                N'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=1000&q=80',
                N'Advanced', N'Published', 4.9, 29, 0, DATEADD(MONTH, -5, SYSUTCDATETIME()), SYSUTCDATETIME());

    -- Khóa 4: DevOps
    IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE slug = 'devops-toan-dien-docker-kubernetes-aws')
        INSERT INTO dbo.courses (instructor_id, category_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at)
        VALUES (@insNhungId, @catCloudId,
                N'DevOps Toàn Diện: CI/CD Pipeline, Docker, Kubernetes & AWS',
                N'devops-toan-dien-docker-kubernetes-aws',
                N'Tự động hóa toàn bộ vòng đời phát triển phần mềm với GitHub Actions, container hóa Docker đa tầng, quản lý cụm Kubernetes (EKS) và giám sát hạ tầng với Prometheus & Grafana.',
                1650000.00,
                N'https://images.unsplash.com/photo-1607799279861-4dd421887fb3?w=1000&q=80',
                N'Intermediate', N'Published', 4.7, 22, 0, DATEADD(MONTH, -5, SYSUTCDATETIME()), SYSUTCDATETIME());

    -- Khóa 5: Flutter Mobile
    IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE slug = 'lap-trinh-di-dong-flutter-dart')
        INSERT INTO dbo.courses (instructor_id, category_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at)
        VALUES (@insNhungId, @catDevId,
                N'Lập trình Ứng dụng Di động Đa nền tảng Flutter & Dart',
                N'lap-trinh-di-dong-flutter-dart',
                N'Học viết ứng dụng di động cho cả iOS và Android chỉ với một lần viết mã. Quản lý trạng thái BLoC, tích hợp Firebase Auth, RESTful API và thanh toán qua cổng điện tử.',
                990000.00,
                N'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=1000&q=80',
                N'Beginner', N'Published', 4.6, 31, 0, DATEADD(MONTH, -4, SYSUTCDATETIME()), SYSUTCDATETIME());

    -- Khóa 6: UI/UX Figma
    IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE slug = 'thiet-ke-ui-ux-figma-design-system')
        INSERT INTO dbo.courses (instructor_id, category_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at)
        VALUES (@insCuongId, @catDesignId,
                N'Thiết kế UI/UX Chuyên nghiệp với Figma & Design System',
                N'thiet-ke-ui-ux-figma-design-system',
                N'Nghiên cứu tâm lý hành vi người dùng (UX Research), xây dựng Wireframe, Auto Layout, Component biến thể và xuất bản Design System hoàn chỉnh cho dự án ứng dụng web và di động.',
                850000.00,
                N'https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=1000&q=80',
                N'Beginner', N'Published', 4.8, 36, 0, DATEADD(MONTH, -4, SYSUTCDATETIME()), SYSUTCDATETIME());

    -- Khóa 7: Agile Scrum
    IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE slug = 'quan-tri-du-an-phan-mem-agile-scrum')
        INSERT INTO dbo.courses (instructor_id, category_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at)
        VALUES (@insLongId, @catBizId,
                N'Kỹ năng Quản trị Dự án Phần mềm theo mô hình Agile & Scrum',
                N'quan-tri-du-an-phan-mem-agile-scrum',
                N'Áp dụng linh hoạt quy trình Agile Scrum vào môi trường doanh nghiệp công nghệ. Quản lý Product Backlog, lập kế hoạch Sprint, ước lượng điểm Story Point trên Jira và Azure DevOps.',
                750000.00,
                N'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=1000&q=80',
                N'All Levels', N'Published', 4.5, 18, 0, DATEADD(MONTH, -3, SYSUTCDATETIME()), SYSUTCDATETIME());

    -- Khóa 8: Generative AI (PendingReview)
    IF NOT EXISTS (SELECT 1 FROM dbo.courses WHERE slug = 'xay-dung-ung-dung-generative-ai-langchain')
        INSERT INTO dbo.courses (instructor_id, category_id, title, slug, description, price, thumbnail_url, level, status, average_rating, student_count, needs_reanalysis, created_at, updated_at)
        VALUES (@insLongId, @catDevId,
                N'Xây dựng Ứng dụng Generative AI với OpenAI API & LangChain',
                N'xay-dung-ung-dung-generative-ai-langchain',
                N'Khóa học mới nhất năm 2026: Tích hợp mô hình ngôn ngữ lớn (LLM), kỹ thuật RAG (Retrieval-Augmented Generation) với Vector Database (Pinecone, ChromaDB) để tạo chatbot thông minh.',
                1590000.00,
                N'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=1000&q=80',
                N'Intermediate', N'PendingReview', 0.0, 0, 1, DATEADD(DAY, -5, SYSUTCDATETIME()), SYSUTCDATETIME());

    -- ================================================================================
    -- 5. SEED COURSE SECTIONS & LESSONS
    -- ================================================================================
    PRINT N'>>> 6. Nạp Giáo trình và Bài học Video MP4 Preview...';
    
    DECLARE @c1Id INT = (SELECT TOP 1 course_id FROM dbo.courses WHERE slug = 'lap-trinh-java-spring-boot-3-microservices-thuc-chien');
    DECLARE @c2Id INT = (SELECT TOP 1 course_id FROM dbo.courses WHERE slug = 'master-reactjs-18-nextjs-14-typescript');
    DECLARE @c3Id INT = (SELECT TOP 1 course_id FROM dbo.courses WHERE slug = 'tri-tue-nhan-tao-machine-learning-python');
    DECLARE @c4Id INT = (SELECT TOP 1 course_id FROM dbo.courses WHERE slug = 'devops-toan-dien-docker-kubernetes-aws');

    -- Thêm Sections cho Khóa 1 (Spring Boot)
    IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE course_id = @c1Id)
    BEGIN
        INSERT INTO dbo.course_sections (course_id, title, order_index) VALUES
        (@c1Id, N'Chương 1: Thiết kế Kiến trúc và Khởi tạo Dự án Spring Boot 3', 1),
        (@c1Id, N'Chương 2: Bảo mật Nâng cao với Spring Security 6 & JWT Stateless', 2),
        (@c1Id, N'Chương 3: Tối ưu Hóa Truy Vấn Cơ Sở Dữ Liệu MSSQL & HikariCP', 3);
        
        DECLARE @s1Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c1Id AND order_index = 1);
        DECLARE @s2Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c1Id AND order_index = 2);
        DECLARE @s3Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c1Id AND order_index = 3);

        INSERT INTO dbo.lessons (section_id, title, video_url, duration, order_index, is_preview) VALUES
        (@s1Id, N'1.1 Giới thiệu Tổng quan Kiến trúc Spring Boot 3 & Hệ sinh thái 2026', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 650, 1, 1),
        (@s1Id, N'1.2 Cấu hình Spring Initializr, Dependency Injection & IoC Container', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 890, 2, 1),
        (@s1Id, N'1.3 Thiết kế DTO, Entity Mapping và Repository Pattern chuẩn 3-Tier', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1200, 3, 0),

        (@s2Id, N'2.1 Cơ chế Filter Chain trong Spring Security 6', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1150, 1, 1),
        (@s2Id, N'2.2 Cấp phát và Xác thực JSON Web Token (JWT)', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1420, 2, 0),
        (@s2Id, N'2.3 Phân quyền Theo Role-Based Access Control (@PreAuthorize)', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 980, 3, 0),

        (@s3Id, N'3.1 Tối ưu hóa HikariCP Connection Pool cho MSSQL', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', 1300, 1, 0),
        (@s3Id, N'3.2 Xử lý N+1 Problem với EntityGraph & Fetch Join', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', 1540, 2, 0);
    END

    -- Thêm Sections cho Khóa 2 (ReactJS)
    IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE course_id = @c2Id)
    BEGIN
        INSERT INTO dbo.course_sections (course_id, title, order_index) VALUES
        (@c2Id, N'Chương 1: Nền tảng React 18, JSX và Component Architecture', 1),
        (@c2Id, N'Chương 2: Quản lý State Phức tạp với Redux Toolkit & Context API', 2);

        DECLARE @s4Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c2Id AND order_index = 1);
        DECLARE @s5Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c2Id AND order_index = 2);

        INSERT INTO dbo.lessons (section_id, title, video_url, duration, order_index, is_preview) VALUES
        (@s4Id, N'1.1 Giới thiệu React 18, Cơ chế Virtual DOM và Cài đặt Vite', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4', 720, 1, 1),
        (@s4Id, N'1.2 Quản lý State với useState, useEffect và Custom Hooks', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', 1100, 2, 0),
        (@s4Id, N'1.3 Tối ưu re-render với useMemo, useCallback và React.memo', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4', 950, 3, 0),

        (@s5Id, N'2.1 Thiết lập Redux Toolkit Store, Slice và createAsyncThunk', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4', 1350, 1, 1),
        (@s5Id, N'2.2 Quản lý Giỏ hàng và Trạng thái Đăng nhập Toàn cục', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1280, 2, 0);
    END

    -- Thêm Sections cho Khóa 3 (Python AI)
    IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE course_id = @c3Id)
    BEGIN
        INSERT INTO dbo.course_sections (course_id, title, order_index) VALUES
        (@c3Id, N'Chương 1: Cơ sở Toán học & Xử lý Dữ liệu với NumPy, Pandas', 1),
        (@c3Id, N'Chương 2: Xây dựng Mô hình Học máy & NLP Sentiment Pipeline', 2);

        DECLARE @s6Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c3Id AND order_index = 1);
        DECLARE @s7Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c3Id AND order_index = 2);

        INSERT INTO dbo.lessons (section_id, title, video_url, duration, order_index, is_preview) VALUES
        (@s6Id, N'1.1 Khởi tạo Môi trường Python, Jupyter Notebook và Scikit-Learn', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 800, 1, 1),
        (@s6Id, N'1.2 Tiền xử lý Dữ liệu Văn bản Tiếng Việt và TF-IDF Vectorization', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1400, 2, 0),
        (@s7Id, N'2.1 Huấn luyện Mô hình Phân loại Cảm xúc Review (Positive/Negative)', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1600, 1, 0);
    END

    -- Thêm Sections cho Khóa 4 (DevOps)
    IF NOT EXISTS (SELECT 1 FROM dbo.course_sections WHERE course_id = @c4Id)
    BEGIN
        INSERT INTO dbo.course_sections (course_id, title, order_index) VALUES
        (@c4Id, N'Chương 1: Đóng gói Ứng dụng với Docker Đa tầng (Multi-stage)', 1),
        (@c4Id, N'Chương 2: Dàn dựng Cụm Kubernetes và Cấu hình Ingress Nginx', 2);

        DECLARE @s8Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c4Id AND order_index = 1);
        DECLARE @s9Id INT = (SELECT TOP 1 section_id FROM dbo.course_sections WHERE course_id = @c4Id AND order_index = 2);

        INSERT INTO dbo.lessons (section_id, title, video_url, duration, order_index, is_preview) VALUES
        (@s8Id, N'1.1 Viết Dockerfile Tối ưu Kích thước Image cho Spring Boot & Node.js', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 920, 1, 1),
        (@s9Id, N'2.1 Cấu hình Kubernetes Deployment, Service và Horizontal Pod Autoscaler', N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1500, 1, 0);
    END

    -- ================================================================================
    -- 6. SEED ORDERS & ORDER ITEMS (30 ĐƠN HÀNG RẢI RÁC 6 THÁNG)
    -- ================================================================================
    PRINT N'>>> 7. Nạp 30 Đơn hàng thanh toán thành công (Orders & OrderItems)...';
    
    DECLARE @uStudent1 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'student@edumy.com');
    DECLARE @uStudent2 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'phuong.mai@gmail.com');
    DECLARE @uStudent3 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'tuan.kiet@gmail.com');
    DECLARE @uStudent4 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'minh.anh@gmail.com');
    DECLARE @uStudent5 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'hoang.duc@gmail.com');
    DECLARE @uStudent6 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'ngoc.anh@gmail.com');
    DECLARE @uStudent7 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'quang.huy@gmail.com');
    DECLARE @uStudent8 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'thu.trang@gmail.com');
    DECLARE @uStudent9 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'viet.anh@gmail.com');
    DECLARE @uStudent10 INT = (SELECT TOP 1 user_id FROM dbo.users WHERE email = 'bao.ngoc@gmail.com');

    IF NOT EXISTS (SELECT 1 FROM dbo.orders WHERE user_id = @uStudent1)
    BEGIN
        -- Bảng tạm chứa thông tin đơn hàng demo
        DECLARE @OrderSeedTable TABLE (
            OrderId INT IDENTITY(1,1),
            UserId INT,
            CourseId INT,
            Price NUMERIC(18,2),
            DaysAgo INT
        );

        INSERT INTO @OrderSeedTable (UserId, CourseId, Price, DaysAgo) VALUES
        -- Tháng 5/2026: 3 đơn
        (@uStudent1, @c1Id, 1499000.00, 165),
        (@uStudent2, @c2Id, 1290000.00, 158),
        (@uStudent3, @c1Id, 1499000.00, 152),
        -- Tháng 6/2026: 4 đơn
        (@uStudent4, @c1Id, 1499000.00, 135),
        (@uStudent5, @c2Id, 1290000.00, 128),
        (@uStudent6, @c3Id, 1890000.00, 122),
        (@uStudent7, @c4Id, 1650000.00, 118),
        -- Tháng 7/2026: 5 đơn
        (@uStudent1, @c2Id, 1290000.00, 105),
        (@uStudent3, @c3Id, 1890000.00, 99),
        (@uStudent8, @c1Id, 1499000.00, 94),
        (@uStudent2, @c4Id, 1650000.00, 88),
        (@uStudent9, @c2Id, 1290000.00, 84),
        -- Tháng 8/2026: 6 đơn
        (@uStudent4, @c3Id, 1890000.00, 75),
        (@uStudent5, @c1Id, 1499000.00, 70),
        (@uStudent6, @c2Id, 1290000.00, 65),
        (@uStudent10, @c3Id, 1890000.00, 61),
        (@uStudent7, @c1Id, 1499000.00, 57),
        (@uStudent8, @c2Id, 1290000.00, 53),
        -- Tháng 9/2026: 6 đơn
        (@uStudent1, @c3Id, 1890000.00, 44),
        (@uStudent2, @c1Id, 1499000.00, 39),
        (@uStudent3, @c4Id, 1650000.00, 35),
        (@uStudent9, @c3Id, 1890000.00, 30),
        (@uStudent4, @c4Id, 1650000.00, 26),
        (@uStudent10, @c2Id, 1290000.00, 22),
        -- Tháng 10/2026: 6 đơn
        (@uStudent5, @c3Id, 1890000.00, 18),
        (@uStudent6, @c1Id, 1499000.00, 14),
        (@uStudent7, @c4Id, 1650000.00, 10),
        (@uStudent8, @c3Id, 1890000.00, 7),
        (@uStudent2, @c2Id, 1290000.00, 4),
        (@uStudent1, @c1Id, 1499000.00, 1);

        -- Insert từng order và order_item
        DECLARE @curRow INT = 1;
        DECLARE @maxRow INT = (SELECT COUNT(*) FROM @OrderSeedTable);
        WHILE @curRow <= @maxRow
        BEGIN
            DECLARE @uId INT, @cId INT, @p NUMERIC(18,2), @d INT;
            SELECT @uId = UserId, @cId = CourseId, @p = Price, @d = DaysAgo FROM @OrderSeedTable WHERE OrderId = @curRow;

            INSERT INTO dbo.orders (user_id, total_amount, status, created_at)
            VALUES (@uId, @p, N'PAID', DATEADD(DAY, -@d, SYSUTCDATETIME()));

            DECLARE @newOrderId INT = SCOPE_IDENTITY();

            INSERT INTO dbo.order_items (order_id, course_id, price)
            VALUES (@newOrderId, @cId, @p);

            SET @curRow = @curRow + 1;
        END
    END

    -- ================================================================================
    -- 7. SEED ENROLLMENTS & LESSON PROGRESSES
    -- ================================================================================
    PRINT N'>>> 8. Nạp Ghi danh khóa học (Enrollments) & Tiến độ học tập...';
    
    -- HV 1 (Đặng Quốc Hưng): Khóa 1 (100%), Khóa 2 (60%), Khóa 3 (25%)
    IF NOT EXISTS (SELECT 1 FROM dbo.enrollments WHERE user_id = @uStudent1 AND course_id = @c1Id)
        INSERT INTO dbo.enrollments (user_id, course_id, enrolled_at, progress_percentage, status, completed_at)
        VALUES (@uStudent1, @c1Id, DATEADD(DAY, -160, SYSUTCDATETIME()), 100, N'ACTIVE', DATEADD(DAY, -10, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.enrollments WHERE user_id = @uStudent1 AND course_id = @c2Id)
        INSERT INTO dbo.enrollments (user_id, course_id, enrolled_at, progress_percentage, status, completed_at)
        VALUES (@uStudent1, @c2Id, DATEADD(DAY, -100, SYSUTCDATETIME()), 60, N'ACTIVE', NULL);

    -- HV 2 (Mai Phương Thảo): Khóa 2 (100%), Khóa 1 (40%)
    IF NOT EXISTS (SELECT 1 FROM dbo.enrollments WHERE user_id = @uStudent2 AND course_id = @c2Id)
        INSERT INTO dbo.enrollments (user_id, course_id, enrolled_at, progress_percentage, status, completed_at)
        VALUES (@uStudent2, @c2Id, DATEADD(DAY, -155, SYSUTCDATETIME()), 100, N'ACTIVE', DATEADD(DAY, -15, SYSUTCDATETIME()));

    -- HV 3 (Nguyễn Tuấn Kiệt): Khóa 3 (100%), Khóa 4 (50%)
    IF NOT EXISTS (SELECT 1 FROM dbo.enrollments WHERE user_id = @uStudent3 AND course_id = @c3Id)
        INSERT INTO dbo.enrollments (user_id, course_id, enrolled_at, progress_percentage, status, completed_at)
        VALUES (@uStudent3, @c3Id, DATEADD(DAY, -95, SYSUTCDATETIME()), 100, N'ACTIVE', DATEADD(DAY, -8, SYSUTCDATETIME()));

    -- Ghi danh các học viên khác
    IF NOT EXISTS (SELECT 1 FROM dbo.enrollments WHERE user_id = @uStudent4 AND course_id = @c1Id)
        INSERT INTO dbo.enrollments (user_id, course_id, enrolled_at, progress_percentage, status, completed_at)
        VALUES (@uStudent4, @c1Id, DATEADD(DAY, -130, SYSUTCDATETIME()), 80, N'ACTIVE', NULL);

    IF NOT EXISTS (SELECT 1 FROM dbo.enrollments WHERE user_id = @uStudent5 AND course_id = @c2Id)
        INSERT INTO dbo.enrollments (user_id, course_id, enrolled_at, progress_percentage, status, completed_at)
        VALUES (@uStudent5, @c2Id, DATEADD(DAY, -125, SYSUTCDATETIME()), 75, N'ACTIVE', NULL);

    IF NOT EXISTS (SELECT 1 FROM dbo.enrollments WHERE user_id = @uStudent6 AND course_id = @c3Id)
        INSERT INTO dbo.enrollments (user_id, course_id, enrolled_at, progress_percentage, status, completed_at)
        VALUES (@uStudent6, @c3Id, DATEADD(DAY, -120, SYSUTCDATETIME()), 45, N'ACTIVE', NULL);

    -- Nạp tiến độ học bài (lesson_progresses) cho HV 1 (Đặng Quốc Hưng)
    INSERT INTO dbo.lesson_progresses (user_id, course_id, lesson_id, completed_at)
    SELECT @uStudent1, @c1Id, l.lesson_id, DATEADD(DAY, -15, SYSUTCDATETIME())
    FROM dbo.lessons l
    JOIN dbo.course_sections cs ON l.section_id = cs.section_id
    WHERE cs.course_id = @c1Id
      AND NOT EXISTS (SELECT 1 FROM dbo.lesson_progresses lp WHERE lp.user_id = @uStudent1 AND lp.lesson_id = l.lesson_id);

    -- ================================================================================
    -- 8. SEED CERTIFICATES (CHỨNG NHẬN TỐT NGHIỆP CẤP THỰC TẾ)
    -- ================================================================================
    PRINT N'>>> 9. Nạp Chứng chỉ tốt nghiệp (Certificates)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.certificates WHERE certificate_url = 'CERT-2026-SPRING-001')
        INSERT INTO dbo.certificates (user_id, course_id, certificate_url, issued_at)
        VALUES (@uStudent1, @c1Id, N'CERT-2026-SPRING-001', DATEADD(DAY, -10, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.certificates WHERE certificate_url = 'CERT-2026-REACT-002')
        INSERT INTO dbo.certificates (user_id, course_id, certificate_url, issued_at)
        VALUES (@uStudent2, @c2Id, N'CERT-2026-REACT-002', DATEADD(DAY, -15, SYSUTCDATETIME()));

    IF NOT EXISTS (SELECT 1 FROM dbo.certificates WHERE certificate_url = 'CERT-2026-PYTHON-003')
        INSERT INTO dbo.certificates (user_id, course_id, certificate_url, issued_at)
        VALUES (@uStudent3, @c3Id, N'CERT-2026-PYTHON-003', DATEADD(DAY, -8, SYSUTCDATETIME()));

    -- ================================================================================
    -- 9. SEED REVIEWS & SENTIMENT LABELS
    -- ================================================================================
    PRINT N'>>> 10. Nạp Đánh giá nhận xét & Cảm xúc AI (Reviews)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.reviews WHERE user_id = @uStudent1 AND course_id = @c1Id)
    BEGIN
        INSERT INTO dbo.reviews (user_id, course_id, rating, comment, sentiment_label, sentiment_score, created_at) VALUES
        (@uStudent1, @c1Id, 5, 
         N'Khóa học Java Spring Boot xuất sắc nhất mình từng học! Thầy Cường giảng rất chi tiết từ tầng kiến trúc đến tối ưu câu lệnh truy vấn MSSQL. Dự án thực tế sát với yêu cầu tuyển dụng.', 
         N'POSITIVE', 0.98, DATEADD(DAY, -60, SYSUTCDATETIME())),

        (@uStudent2, @c2Id, 5, 
         N'Giáo trình React 18 và Next.js 14 cực kỳ cập nhật, mã nguồn sạch đẹp. Mình đã tự tin làm chủ Server Components và áp dụng ngay vào công việc.', 
         N'POSITIVE', 0.96, DATEADD(DAY, -55, SYSUTCDATETIME())),

        (@uStudent3, @c3Id, 5, 
         N'Thầy Long dạy phần Machine Learning và Sentiment Analysis rất dễ hiểu. Cách thầy hướng dẫn toán giải tích ứng dụng vào PyTorch thực sự khai sáng tư duy.', 
         N'POSITIVE', 0.99, DATEADD(DAY, -50, SYSUTCDATETIME())),

        (@uStudent4, @c4Id, 5, 
         N'Khóa DevOps của cô Nhung rất tuyệt vời! Các bài thực hành Docker và cụm Kubernetes trên AWS chạy mượt mà, tài liệu hướng dẫn rất đầy đủ.', 
         N'POSITIVE', 0.95, DATEADD(DAY, -45, SYSUTCDATETIME())),

        (@uStudent5, @c1Id, 5, 
         N'Cảm ơn thầy Cường rất nhiều! Nhờ kiến thức Microservices và Kafka trong khóa học mà mình đã pass phỏng vấn vị trí Senior Backend Engineer.', 
         N'POSITIVE', 0.98, DATEADD(DAY, -30, SYSUTCDATETIME())),

        (@uStudent6, @c2Id, 5, 
         N'Video bài giảng chất lượng cao, âm thanh sắc nét, các bài tập thực hành Redux Toolkit rất bổ ích cho người mới làm quen.', 
         N'POSITIVE', 0.93, DATEADD(DAY, -25, SYSUTCDATETIME())),

        (@uStudent7, @c3Id, 5, 
         N'Rất ấn tượng với phần tích hợp API FastAPI của mô hình Học máy lên Docker. Một khóa học AI thực chiến đúng nghĩa.', 
         N'POSITIVE', 0.95, DATEADD(DAY, -20, SYSUTCDATETIME())),

        (@uStudent8, @c4Id, 5, 
         N'Giải thích cơ chế CI/CD Pipeline trên GitHub Actions rất trực quan. Giúp tối ưu thời gian deploy của team mình đi rất nhiều.', 
         N'POSITIVE', 0.92, DATEADD(DAY, -16, SYSUTCDATETIME())),

        -- Neutral Reviews (Trung lập)
        (@uStudent9, @c1Id, 4, 
         N'Nội dung chuyên sâu nhưng tốc độ giảng của thầy ở chương 2 hơi nhanh đối với người mới bắt đầu. Bù lại tài liệu đính kèm rất đầy đủ.', 
         N'NEUTRAL', 0.58, DATEADD(DAY, -18, SYSUTCDATETIME())),

        (@uStudent10, @c2Id, 4, 
         N'Khóa học khá ổn, tổng hợp đủ các kiến thức cơ bản đến nâng cao. Hy vọng thầy bổ sung thêm chuyên đề về React Server Actions mới nhất.', 
         N'NEUTRAL', 0.52, DATEADD(DAY, -14, SYSUTCDATETIME())),

        -- Negative Reviews (Góp ý âm thanh / kiểm thử cảnh báo AI)
        (@uStudent4, @c2Id, 3, 
         N'Âm lượng ở bài học 2.2 hơi nhỏ so với các bài trước, phải đeo tai nghe mới nghe rõ. Đề nghị ban quản trị chỉnh sửa lại âm thanh video.', 
         N'NEGATIVE', 0.28, DATEADD(DAY, -22, SYSUTCDATETIME())),

        (@uStudent5, @c3Id, 3, 
         N'Cần có thêm nhiều bài tập tình huống thực tế và case study giải quyết lỗi mô hình overfitting để người học luyện tập nhiều hơn.', 
         N'NEGATIVE', 0.32, DATEADD(DAY, -3, SYSUTCDATETIME()));
    END

    -- ================================================================================
    -- 10. SEED COURSE ML ANALYSES & RISK FLAGS
    -- ================================================================================
    PRINT N'>>> 11. Nạp Báo cáo Phân tích AI & Cảnh báo an toàn (CourseMlAnalyses)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.course_ml_analyses WHERE course_id = @c1Id)
    BEGIN
        INSERT INTO dbo.course_ml_analyses (course_id, primary_category, sub_category, suggested_level, confidence, quality_score, risk_level, raw_response_json, model_version, status, created_at, approved_at, approved_by_user_id)
        VALUES 
        (@c1Id, N'Lập trình phần mềm', N'Backend Development', N'Intermediate', 0.98, 96, N'Low', N'{"category": "Development", "confidence": 0.98, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -6, SYSUTCDATETIME()), DATEADD(MONTH, -6, SYSUTCDATETIME()), 1),
        (@c2Id, N'Lập trình phần mềm', N'Frontend Development', N'All Levels', 0.97, 94, N'Low', N'{"category": "Development", "confidence": 0.97, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -6, SYSUTCDATETIME()), DATEADD(MONTH, -6, SYSUTCDATETIME()), 1),
        (@c3Id, N'Lập trình phần mềm', N'Data Science & AI', N'Advanced', 0.99, 98, N'Low', N'{"category": "Development", "confidence": 0.99, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -5, SYSUTCDATETIME()), DATEADD(MONTH, -5, SYSUTCDATETIME()), 1),
        (@c4Id, N'Hạ tầng IT & Điện toán đám mây', N'DevOps & Cloud', N'Intermediate', 0.96, 92, N'Low', N'{"category": "IT & Software", "confidence": 0.96, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -5, SYSUTCDATETIME()), DATEADD(MONTH, -5, SYSUTCDATETIME()), 1);

        -- Thêm cờ High Risk cho khóa học thứ 8 để Admin thấy cảnh báo
        DECLARE @c8Id INT = (SELECT TOP 1 course_id FROM dbo.courses WHERE slug = 'xay-dung-ung-dung-generative-ai-langchain');
        IF (@c8Id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.course_ml_analyses WHERE course_id = @c8Id))
            INSERT INTO dbo.course_ml_analyses (course_id, primary_category, sub_category, suggested_level, confidence, quality_score, risk_level, raw_response_json, model_version, status, created_at, approved_at, approved_by_user_id)
            VALUES (@c8Id, N'Lập trình phần mềm', N'Generative AI & LLM', N'Intermediate', 0.61, 65, N'High', N'{"category": "Development", "confidence": 0.61, "risk": "High"}', N'ml-course-classifier-v2.1', N'NeedsManualReview', DATEADD(DAY, -2, SYSUTCDATETIME()), NULL, NULL);
    END

    -- ================================================================================
    -- 11. SEED USER ACTIVITIES (NHẬT KÝ HOẠT ĐỘNG THỜI GIAN THỰC)
    -- ================================================================================
    PRINT N'>>> 12. Nạp Nhật ký hoạt động người dùng (UserActivities)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.user_activities WHERE user_id = @uStudent1)
    BEGIN
        INSERT INTO dbo.user_activities (user_id, activity_type, resource_id, timestamp) VALUES 
        (@uStudent1, N'LOGIN', N'AUTH_JWT', DATEADD(MINUTE, -15, SYSUTCDATETIME())),
        (@uStudent1, N'COMPLETE_LESSON', N'LESSON_8', DATEADD(MINUTE, -10, SYSUTCDATETIME())),
        (@uStudent1, N'RECEIVE_CERTIFICATE', N'CERT-2026-SPRING-001', DATEADD(MINUTE, -8, SYSUTCDATETIME())),
        (@uStudent2, N'PAYMENT_SUCCESS', N'ORDER_29', DATEADD(HOUR, -2, SYSUTCDATETIME())),
        (@uStudent3, N'SUBMIT_REVIEW', N'COURSE_3', DATEADD(HOUR, -5, SYSUTCDATETIME())),
        (@uStudent4, N'VIEW_COURSE', N'COURSE_1', DATEADD(HOUR, -8, SYSUTCDATETIME()));
    END

    -- ================================================================================
    -- 12. SEED WISHLISTS
    -- ================================================================================
    IF NOT EXISTS (SELECT 1 FROM dbo.wishlists WHERE user_id = @uStudent1)
    BEGIN
        INSERT INTO dbo.wishlists (user_id, course_id, added_at) VALUES 
        (@uStudent1, @c4Id, DATEADD(DAY, -5, SYSUTCDATETIME())),
        (@uStudent2, @c3Id, DATEADD(DAY, -3, SYSUTCDATETIME())),
        (@uStudent3, @c2Id, DATEADD(DAY, -2, SYSUTCDATETIME()));
    END

    COMMIT TRANSACTION;
    PRINT N'====================================================================================';
    PRINT N'>>> HOÀN THÀNH NẠP SEED DATA THÀNH CÔNG VÀO CƠ SỞ DỮ LIỆU EDUMYDB!';
    PRINT N'====================================================================================';

END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    PRINT N'*** ĐÃ XẢY RA LỖI KHI NẠP SEED DATA! GIAO DỊCH ĐÃ ĐƯỢC ROLLBACK.';
    PRINT N'Thông báo lỗi: ' + ERROR_MESSAGE();
    PRINT N'Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(20));
    PRINT N'Dòng phát sinh lỗi: ' + CAST(ERROR_LINE() AS NVARCHAR(20));
END CATCH;
GO
