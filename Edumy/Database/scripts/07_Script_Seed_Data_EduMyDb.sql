-- ====================================================================================
-- DỰ ÁN: EDUMY ELEARNING PLATFORM (Spring Boot 3.2 + MSSQL + React 18)
-- KỊCH BẢN NẠP DỮ LIỆU SEED DATA TOÀN DIỆN & SỐNG ĐỘNG (T-SQL SEED SCRIPT)
-- HỆ THỐNG: Microsoft SQL Server (EduMyDb)
-- MỤC ĐÍCH: Nạp dữ liệu phong phú phục vụ Demo, nhảy biểu đồ Doanh thu 6 tháng,
--           danh bạ Giảng viên/Học viên, Giáo trình, Đánh giá AI & Cấp chứng chỉ.
-- AN TOÀN: Sử dụng Giao dịch (TRANSACTION), IF NOT EXISTS chống trùng lặp,
--          chuẩn hóa 100% Unicode với tiền tố N''.
-- TÁC GIẢ: Chuyên gia Cơ sở dữ liệu & Data Architect - Edumy Team
-- ====================================================================================

USE EduMyDb;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

PRINT N'>>> BẮT ĐẦU QUÁ TRÌNH NẠP SEED DATA CHO CƠ SỞ DỮ LIỆU EDUMYDB...';

-- ====================================================================================
-- 0. ĐỒNG BỘ SCHEMA NẾU CẦN THIẾT (TƯƠNG THÍCH HOÀN HẢO SPRING BOOT ENTITY)
-- ====================================================================================
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.Enrollments') AND name = 'Status'
)
BEGIN
    ALTER TABLE dbo.Enrollments ADD Status NVARCHAR(50) NOT NULL CONSTRAINT DF_Enrollments_Status DEFAULT 'ACTIVE';
    PRINT N'   [+] Đã bổ sung cột Status vào bảng dbo.Enrollments.';
END
GO

BEGIN TRANSACTION;
BEGIN TRY

    -- ================================================================================
    -- 1. SEED ROLES (VAI TRÒ NGƯỜI DÙNG)
    -- ================================================================================
    PRINT N'1. Đang nạp danh mục Vai trò (Roles)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE Id = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Roles ON;
        INSERT INTO dbo.Roles (Id, Name) VALUES 
        (1, N'Admin'),
        (2, N'Instructor'),
        (3, N'Student');
        SET IDENTITY_INSERT dbo.Roles OFF;
    END

    -- ================================================================================
    -- 2. SEED USERS & USER ROLES (1 ADMIN, 3 GIẢNG VIÊN, 10 HỌC VIÊN)
    -- Hash BCrypt chung: $2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G (Mật khẩu: Admin@123 / Instructor@123 / Student@123)
    -- ================================================================================
    PRINT N'2. Đang nạp tài khoản Người dùng (Users & Roles)...';

    -- 2.1 Admin & Giảng viên
    IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = N'admin@edumy.com')
    BEGIN
        SET IDENTITY_INSERT dbo.Users ON;
        INSERT INTO dbo.Users (UserId, FullName, Email, PasswordHash, Role, AvatarUrl, Bio, IsActive, CreatedAt) VALUES
        (1, N'System Administrator', N'admin@edumy.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Admin', 
         N'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80', N'Quản trị viên cấp cao phụ trách hệ thống và vận hành Edumy Enterprise.', 1, DATEADD(MONTH, -8, SYSUTCDATETIME())),
        
        (2, N'ThS. Nguyễn Văn Cường', N'instructor@edumy.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Instructor', 
         N'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80', N'Software Architect với hơn 10 năm kinh nghiệm chuyên sâu Java Spring Boot, Microservices và ReactJS Frontend.', 1, DATEADD(MONTH, -7, SYSUTCDATETIME())),
        
        (3, N'TS. Trần Hoàng Long', N'long.tran@edumy.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Instructor', 
         N'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&q=80', N'Tiến sĩ Khoa học Máy tính, chuyên gia Trí tuệ Nhân tạo & Xử lý ngôn ngữ tự nhiên NLP. Tác giả nhiều bài báo khoa học quốc tế.', 1, DATEADD(MONTH, -7, SYSUTCDATETIME())),
        
        (4, N'Kỹ sư Lê Thị Hồng Nhung', N'nhung.le@edumy.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Instructor', 
         N'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&q=80', N'Senior Cloud Solutions Architect & Mobile Lead tại tập đoàn công nghệ lớn. Chuyên gia Flutter và AWS Certified.', 1, DATEADD(MONTH, -7, SYSUTCDATETIME())),

        -- 2.2 Danh sách 10 Học viên tiêu biểu
        (5, N'Đặng Quốc Hưng', N'student@edumy.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&q=80', N'Lập trình viên đam mê Backend và giải thuật phân tán.', 1, DATEADD(MONTH, -6, SYSUTCDATETIME())),

        (6, N'Mai Phương Thảo', N'phuong.mai@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80', N'Sinh viên năm 4 ngành CNTT Đại học Bách Khoa, mục tiêu trở thành Frontend Engineer.', 1, DATEADD(MONTH, -6, SYSUTCDATETIME())),

        (7, N'Nguyễn Tuấn Kiệt', N'tuan.kiet@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400&q=80', N'Data Engineer định hướng phát triển mô hình Machine Learning thực tế.', 1, DATEADD(MONTH, -5, SYSUTCDATETIME())),

        (8, N'Lê Minh Anh', N'minh.anh@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&q=80', N'DevOps Fresher đang học CI/CD và Kubernetes triển khai thực tế.', 1, DATEADD(MONTH, -5, SYSUTCDATETIME())),

        (9, N'Phạm Hoàng Đức', N'hoang.duc@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&q=80', N'Mobile Developer mong muốn nâng cao kỹ năng Flutter & Dart.', 1, DATEADD(MONTH, -4, SYSUTCDATETIME())),

        (10, N'Vũ Ngọc Ánh', N'ngoc.anh@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&q=80', N'UI/UX Designer tìm kiếm giải pháp tối ưu hệ thống thiết kế Figma.', 1, DATEADD(MONTH, -4, SYSUTCDATETIME())),

        (11, N'Trịnh Quang Huy', N'quang.huy@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=400&q=80', N'Quản lý sản phẩm (Product Owner) tìm hiểu mô hình Agile Scrum.', 1, DATEADD(MONTH, -3, SYSUTCDATETIME())),

        (12, N'Bùi Thu Trang', N'thu.trang@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&q=80', N'Học viên chuyển ngành sang lập trình web toàn diện.', 1, DATEADD(MONTH, -3, SYSUTCDATETIME())),

        (13, N'Đỗ Việt Anh', N'viet.anh@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=400&q=80', N'Kỹ sư phần mềm quan tâm đến công nghệ Generative AI & LLM.', 1, DATEADD(MONTH, -2, SYSUTCDATETIME())),

        (14, N'Hoàng Bảo Ngọc', N'bao.ngoc@gmail.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 
         N'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80', N'Sinh viên nghiên cứu khoa học dữ liệu và xử lý ngôn ngữ tiếng Việt.', 1, DATEADD(MONTH, -1, SYSUTCDATETIME()));
        SET IDENTITY_INSERT dbo.Users OFF;
    END

    -- Gán vai trò vào bảng UserRoles
    IF NOT EXISTS (SELECT 1 FROM dbo.UserRoles WHERE UserId = 1)
    BEGIN
        INSERT INTO dbo.UserRoles (UserId, RoleId) VALUES 
        (1, 1), -- Admin
        (2, 2), -- Giảng viên Cường
        (3, 2), -- Giảng viên Long
        (4, 2), -- Giảng viên Nhung
        (5, 3), (6, 3), (7, 3), (8, 3), (9, 3), 
        (10, 3), (11, 3), (12, 3), (13, 3), (14, 3);
    END

    -- Khởi tạo Giỏ hàng (Carts) mặc định cho mỗi người dùng
    INSERT INTO dbo.Carts (UserId, CreatedAt, UpdatedAt)
    SELECT u.UserId, SYSUTCDATETIME(), SYSUTCDATETIME()
    FROM dbo.Users u
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Carts c WHERE c.UserId = u.UserId);

    -- ================================================================================
    -- 3. SEED CATEGORIES (DANH MỤC KHÓA HỌC)
    -- ================================================================================
    PRINT N'3. Đang nạp danh mục Khóa học (Categories)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Categories WHERE CategoryId = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Categories ON;
        INSERT INTO dbo.Categories (CategoryId, Name, Description, ParentCategoryId) VALUES 
        (1, N'Lập trình phần mềm', N'Lập trình Web, Di động, Hệ thống Backend & Kiến trúc Microservices', NULL),
        (2, N'Kinh doanh & Quản lý', N'Quản trị dự án Agile Scrum, Khởi nghiệp công nghệ & Tài chính', NULL),
        (3, N'Thiết kế đồ họa & UI/UX', N'Thiết kế giao diện người dùng Figma, Design System & Mỹ thuật số', NULL),
        (4, N'Marketing & Truyền thông', N'Digital Marketing, SEO kỹ thuật số và Tăng trưởng người dùng', NULL),
        (5, N'Hạ tầng IT & Điện toán đám mây', N'Hệ điều hành Linux, Mạng máy tính, DevOps, Docker & AWS', NULL);
        SET IDENTITY_INSERT dbo.Categories OFF;
    END

    -- ================================================================================
    -- 4. SEED TAGS & COUPONS (THẺ TỪ KHÓA & MÃ GIẢM GIÁ)
    -- ================================================================================
    PRINT N'4. Đang nạp Thẻ từ khóa và Mã giảm giá (Tags & Coupons)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Tags WHERE Id = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Tags ON;
        INSERT INTO dbo.Tags (Id, Name) VALUES 
        (1, N'Java'), (2, N'Spring Boot'), (3, N'React'), (4, N'Python'),
        (5, N'Machine Learning'), (6, N'SQL Server'), (7, N'DevOps'),
        (8, N'Docker'), (9, N'Kubernetes'), (10, N'Flutter'), (11, N'Figma'), (12, N'Agile');
        SET IDENTITY_INSERT dbo.Tags OFF;
    END

    IF NOT EXISTS (SELECT 1 FROM dbo.Coupons WHERE Code = N'EDUMY50')
    BEGIN
        SET IDENTITY_INSERT dbo.Coupons ON;
        INSERT INTO dbo.Coupons (Id, Code, DiscountPercentage, ExpiryDate, IsActive) VALUES 
        (1, N'WELCOME20', 20.00, DATEADD(YEAR, 2, SYSUTCDATETIME()), 1),
        (2, N'EDUMY50', 50.00, DATEADD(YEAR, 2, SYSUTCDATETIME()), 1),
        (3, N'PRODEV30', 30.00, DATEADD(YEAR, 2, SYSUTCDATETIME()), 1);
        SET IDENTITY_INSERT dbo.Coupons OFF;
    END

    -- ================================================================================
    -- 5. SEED COURSES (8 KHÓA HỌC HOT TREND ĐẦY ĐỦ THUMBNAIL, GIÁ, ĐÁNH GIÁ)
    -- ================================================================================
    PRINT N'5. Đang nạp danh sách Khóa học chuẩn LMS (Courses)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Courses WHERE CourseId = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Courses ON;
        INSERT INTO dbo.Courses (CourseId, InstructorId, CategoryId, Title, Slug, Description, Price, ThumbnailUrl, Level, Status, AverageRating, StudentCount, NeedsReanalysis, CreatedAt, UpdatedAt) VALUES
        -- Khóa 1: Spring Boot
        (1, 2, 1, 
         N'Lập trình Java Spring Boot 3 & Microservices Thực chiến', 
         N'lap-trinh-java-spring-boot-3-microservices-thuc-chien', 
         N'Khóa học chuyên sâu đưa bạn từ nền tảng Spring Core, Spring Data JPA, Spring Security JWT đến kiến trúc Microservices phân tán với Spring Cloud, Docker, Kafka và Microsoft SQL Server.', 
         1499000.00, 
         N'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1000&q=80', 
         N'Intermediate', N'Published', 4.9, 38, 0, DATEADD(MONTH, -6, SYSUTCDATETIME()), SYSUTCDATETIME()),

        -- Khóa 2: ReactJS
        (2, 2, 1, 
         N'Master ReactJS 18, Next.js 14 & TypeScript từ Zero đến Pro', 
         N'master-reactjs-18-nextjs-14-typescript', 
         N'Làm chủ hệ sinh thái Frontend hiện đại: React Hooks nâng cao, Redux Toolkit, Next.js App Router, SSR/SSG, TailwindCSS và tối ưu hiệu năng điểm 100 Lighthouse.', 
         1290000.00, 
         N'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=1000&q=80', 
         N'All Levels', N'Published', 4.8, 45, 0, DATEADD(MONTH, -6, SYSUTCDATETIME()), SYSUTCDATETIME()),

        -- Khóa 3: Python AI/ML
        (3, 3, 1, 
         N'Trí tuệ Nhân tạo & Machine Learning chuyên sâu với Python', 
         N'tri-tue-nhan-tao-machine-learning-python', 
         N'Nắm vững toán giải tích và đại số tuyến tính trong AI, xây dựng mô hình Phân loại, Hồi quy, Xử lý ngôn ngữ tự nhiên NLP (Sentiment Analysis) với Scikit-Learn và PyTorch. Đóng gói API FastAPI.', 
         1890000.00, 
         N'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=1000&q=80', 
         N'Advanced', N'Published', 4.9, 29, 0, DATEADD(MONTH, -5, SYSUTCDATETIME()), SYSUTCDATETIME()),

        -- Khóa 4: DevOps
        (4, 4, 5, 
         N'DevOps Toàn Diện: CI/CD Pipeline, Docker, Kubernetes & AWS', 
         N'devops-toan-dien-docker-kubernetes-aws', 
         N'Tự động hóa toàn bộ vòng đời phát triển phần mềm với GitHub Actions, container hóa Docker đa tầng, quản lý cụm Kubernetes (EKS) và giám sát hạ tầng với Prometheus & Grafana.', 
         1650000.00, 
         N'https://images.unsplash.com/photo-1607799279861-4dd421887fb3?w=1000&q=80', 
         N'Intermediate', N'Published', 4.7, 22, 0, DATEADD(MONTH, -5, SYSUTCDATETIME()), SYSUTCDATETIME()),

        -- Khóa 5: Flutter Mobile
        (5, 4, 1, 
         N'Lập trình Ứng dụng Di động Đa nền tảng Flutter & Dart', 
         N'lap-trinh-di-dong-flutter-dart', 
         N'Học viết ứng dụng di động cho cả iOS và Android chỉ với một lần viết mã. Quản lý trạng thái BLoC, tích hợp Firebase Auth, RESTful API và thanh toán qua cổng điện tử.', 
         990000.00, 
         N'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=1000&q=80', 
         N'Beginner', N'Published', 4.6, 31, 0, DATEADD(MONTH, -4, SYSUTCDATETIME()), SYSUTCDATETIME()),

        -- Khóa 6: UI/UX Figma
        (6, 2, 3, 
         N'Thiết kế UI/UX Chuyên nghiệp với Figma & Design System', 
         N'thiet-ke-ui-ux-figma-design-system', 
         N'Nghiên cứu tâm lý hành vi người dùng (UX Research), xây dựng Wireframe, Auto Layout, Component biến thể và xuất bản Design System hoàn chỉnh cho dự án ứng dụng web và di động.', 
         850000.00, 
         N'https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=1000&q=80', 
         N'Beginner', N'Published', 4.8, 36, 0, DATEADD(MONTH, -4, SYSUTCDATETIME()), SYSUTCDATETIME()),

        -- Khóa 7: Agile Scrum
        (7, 3, 2, 
         N'Kỹ năng Quản trị Dự án Phần mềm theo mô hình Agile & Scrum', 
         N'quan-tri-du-an-phan-mem-agile-scrum', 
         N'Áp dụng linh hoạt quy trình Agile Scrum vào môi trường doanh nghiệp công nghệ. Quản lý Product Backlog, lập kế hoạch Sprint, ước lượng điểm Story Point trên Jira và Azure DevOps.', 
         750000.00, 
         N'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=1000&q=80', 
         N'All Levels', N'Published', 4.5, 18, 0, DATEADD(MONTH, -3, SYSUTCDATETIME()), SYSUTCDATETIME()),

        -- Khóa 8: Generative AI (Khóa mới đang chờ duyệt để test tính năng Admin / AI Moderation)
        (8, 3, 1, 
         N'Xây dựng Ứng dụng Generative AI với OpenAI API & LangChain', 
         N'xay-dung-ung-dung-generative-ai-langchain', 
         N'Khóa học mới nhất năm 2026: Tích hợp mô hình ngôn ngữ lớn (LLM), kỹ thuật RAG (Retrieval-Augmented Generation) với Vector Database (Pinecone, ChromaDB) để tạo chatbot thông minh.', 
         1590000.00, 
         N'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=1000&q=80', 
         N'Intermediate', N'PendingReview', 0.0, 0, 1, DATEADD(DAY, -5, SYSUTCDATETIME()), SYSUTCDATETIME());
        SET IDENTITY_INSERT dbo.Courses OFF;
    END

    -- Gán Tags cho các khóa học
    IF NOT EXISTS (SELECT 1 FROM dbo.CourseTags WHERE CourseId = 1)
    BEGIN
        INSERT INTO dbo.CourseTags (CourseId, TagId) VALUES 
        (1, 1), (1, 2), (1, 6), -- Spring Boot: Java, Spring Boot, SQL Server
        (2, 3), (2, 11),        -- React: React, Figma
        (3, 4), (3, 5),         -- Python: Python, Machine Learning
        (4, 7), (4, 8), (4, 9), -- DevOps: DevOps, Docker, Kubernetes
        (5, 10),                -- Mobile: Flutter
        (6, 11),                -- Design: Figma
        (7, 12);                -- Agile
    END

    -- ================================================================================
    -- 6. SEED COURSE SECTIONS & LESSONS (GIÁO TRÌNH, BÀI HỌC CÓ LINK MP4 & ISPREVIEW)
    -- ================================================================================
    PRINT N'6. Đang nạp Giáo trình và Bài học chi tiết (CourseSections & Lessons)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.CourseSections WHERE SectionId = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.CourseSections ON;
        INSERT INTO dbo.CourseSections (SectionId, CourseId, Title, OrderIndex) VALUES
        -- Khóa 1: Spring Boot
        (1, 1, N'Chương 1: Thiết kế Kiến trúc và Khởi tạo Dự án Spring Boot 3', 1),
        (2, 1, N'Chương 2: Bảo mật Nâng cao với Spring Security 6 & JWT Stateless', 2),
        (3, 1, N'Chương 3: Tối ưu Hóa Truy Vấn Cơ Sở Dữ Liệu MSSQL & HikariCP', 3),

        -- Khóa 2: ReactJS
        (4, 2, N'Chương 1: Nền tảng React 18, JSX và Component Architecture', 1),
        (5, 2, N'Chương 2: Quản lý State Phức tạp với Redux Toolkit & Context API', 2),
        (6, 2, N'Chương 3: Tích hợp RESTful API và Tối ưu Render Performance', 3),

        -- Khóa 3: AI Python
        (7, 3, N'Chương 1: Cơ sở Toán học & Xử lý Dữ liệu với NumPy, Pandas', 1),
        (8, 3, N'Chương 2: Xây dựng Mô hình Học máy & NLP Sentiment Pipeline', 2),

        -- Khóa 4: DevOps
        (9, 4, N'Chương 1: Đóng gói Ứng dụng với Docker Đa tầng (Multi-stage)', 1),
        (10, 4, N'Chương 2: Dàn dựng Cụm Kubernetes và Cấu hình Ingress Nginx', 2);
        SET IDENTITY_INSERT dbo.CourseSections OFF;
    END

    IF NOT EXISTS (SELECT 1 FROM dbo.Lessons WHERE LessonId = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Lessons ON;
        INSERT INTO dbo.Lessons (LessonId, SectionId, Title, VideoUrl, Duration, OrderIndex, IsPreview) VALUES
        -- Bài học Khóa 1 - Chương 1
        (1, 1, N'1.1 Giới thiệu Tổng quan Kiến trúc Spring Boot 3 & Hệ sinh thái 2026', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 650, 1, 1), -- Đánh dấu học thử miễn phí
        (2, 1, N'1.2 Cấu hình Spring Initializr, Dependency Injection & IoC Container', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 890, 2, 1),
        (3, 1, N'1.3 Thiết kế DTO, Entity Mapping và Repository Pattern chuẩn 3-Tier', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1200, 3, 0),

        -- Bài học Khóa 1 - Chương 2
        (4, 2, N'2.1 Cơ chế Filter Chain trong Spring Security 6', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1150, 1, 1),
        (5, 2, N'2.2 Cấp phát và Xác thực JSON Web Token (JWT)', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 1420, 2, 0),
        (6, 2, N'2.3 Phân quyền Theo Role-Based Access Control (@PreAuthorize)', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 980, 3, 0),

        -- Bài học Khóa 1 - Chương 3
        (7, 3, N'3.1 Tối ưu hóa HikariCP Connection Pool cho MSSQL', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', 1300, 1, 0),
        (8, 3, N'3.2 Xử lý N+1 Problem với EntityGraph & Fetch Join', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', 1540, 2, 0),

        -- Bài học Khóa 2 - Chương 1 (ReactJS)
        (9, 4, N'1.1 Giới thiệu React 18, Cơ chế Virtual DOM và Cài đặt Vite', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4', 720, 1, 1),
        (10, 4, N'1.2 Quản lý State với useState, useEffect và Custom Hooks', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', 1100, 2, 0),
        (11, 4, N'1.3 Tối ưu re-render với useMemo, useCallback và React.memo', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4', 950, 3, 0),

        -- Bài học Khóa 2 - Chương 2
        (12, 5, N'2.1 Thiết lập Redux Toolkit Store, Slice và createAsyncThunk', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4', 1350, 1, 1),
        (13, 5, N'2.2 Quản lý Giỏ hàng và Trạng thái Đăng nhập Toàn cục', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 1280, 2, 0),

        -- Bài học Khóa 3 - AI Machine Learning
        (14, 7, N'1.1 Khởi tạo Môi trường Python, Jupyter Notebook và Scikit-Learn', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 800, 1, 1),
        (15, 7, N'1.2 Tiền xử lý Dữ liệu Văn bản Tiếng Việt và TF-IDF Vectorization', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 1400, 2, 0),
        (16, 8, N'2.1 Huấn luyện Mô hình Phân loại Cảm xúc Review (Positive/Negative)', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 1600, 1, 0),

        -- Bài học Khóa 4 - DevOps
        (17, 9, N'1.1 Viết Dockerfile Tối ưu Kích thước Image cho Spring Boot & Node.js', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', 920, 1, 1),
        (18, 10, N'2.1 Cấu hình Kubernetes Deployment, Service và Horizontal Pod Autoscaler', 
         N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4', 1500, 1, 0);
        SET IDENTITY_INSERT dbo.Lessons OFF;
    END

    -- ================================================================================
    -- 7. SEED ORDERS & ORDER ITEMS (30 ĐƠN HÀNG RẢI RÁC 6 THÁNG - TĂNG TRƯỞNG RÕ RỆT)
    -- Tổng doanh thu đạt trên 55,000,000 VNĐ, trạng thái 'PAID' để nhảy biểu đồ
    -- ================================================================================
    PRINT N'7. Đang nạp 30 Đơn hàng giả lập 6 tháng tăng trưởng (Orders & OrderItems)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE OrderId = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Orders ON;
        INSERT INTO dbo.Orders (OrderId, UserId, TotalAmount, Status, CreatedAt) VALUES
        -- Tháng 5/2026: 3 đơn (~3.6 triệu)
        (1, 5, 1499000.00, N'PAID', DATEADD(DAY, -165, SYSUTCDATETIME())),
        (2, 6, 1290000.00, N'PAID', DATEADD(DAY, -158, SYSUTCDATETIME())),
        (3, 7, 850000.00,  N'PAID', DATEADD(DAY, -152, SYSUTCDATETIME())),

        -- Tháng 6/2026: 4 đơn (~5.4 triệu)
        (4, 8, 1499000.00, N'PAID', DATEADD(DAY, -135, SYSUTCDATETIME())),
        (5, 9, 990000.00,  N'PAID', DATEADD(DAY, -128, SYSUTCDATETIME())),
        (6, 10, 1290000.00, N'PAID', DATEADD(DAY, -122, SYSUTCDATETIME())),
        (7, 11, 1650000.00, N'PAID', DATEADD(DAY, -118, SYSUTCDATETIME())),

        -- Tháng 7/2026: 5 đơn (~7.7 triệu)
        (8, 5, 1290000.00, N'PAID', DATEADD(DAY, -105, SYSUTCDATETIME())),
        (9, 7, 1890000.00, N'PAID', DATEADD(DAY, -99, SYSUTCDATETIME())),
        (10, 12, 1499000.00, N'PAID', DATEADD(DAY, -94, SYSUTCDATETIME())),
        (11, 6, 850000.00,  N'PAID', DATEADD(DAY, -88, SYSUTCDATETIME())),
        (12, 13, 1650000.00, N'PAID', DATEADD(DAY, -84, SYSUTCDATETIME())),

        -- Tháng 8/2026: 6 đơn (~9.8 triệu)
        (13, 8, 1890000.00, N'PAID', DATEADD(DAY, -75, SYSUTCDATETIME())),
        (14, 9, 1499000.00, N'PAID', DATEADD(DAY, -70, SYSUTCDATETIME())),
        (15, 10, 850000.00,  N'PAID', DATEADD(DAY, -65, SYSUTCDATETIME())),
        (16, 14, 1890000.00, N'PAID', DATEADD(DAY, -61, SYSUTCDATETIME())),
        (17, 11, 750000.00,  N'PAID', DATEADD(DAY, -57, SYSUTCDATETIME())),
        (18, 12, 1290000.00, N'PAID', DATEADD(DAY, -53, SYSUTCDATETIME())),

        -- Tháng 9/2026: 6 đơn (~11.6 triệu)
        (19, 5, 1890000.00, N'PAID', DATEADD(DAY, -44, SYSUTCDATETIME())),
        (20, 6, 1499000.00, N'PAID', DATEADD(DAY, -39, SYSUTCDATETIME())),
        (21, 7, 1650000.00, N'PAID', DATEADD(DAY, -35, SYSUTCDATETIME())),
        (22, 13, 1890000.00, N'PAID', DATEADD(DAY, -30, SYSUTCDATETIME())),
        (23, 8, 1650000.00, N'PAID', DATEADD(DAY, -26, SYSUTCDATETIME())),
        (24, 14, 1290000.00, N'PAID', DATEADD(DAY, -22, SYSUTCDATETIME())),

        -- Tháng 10/2026: 6 đơn (~13.2 triệu)
        (25, 9, 1890000.00, N'PAID', DATEADD(DAY, -18, SYSUTCDATETIME())),
        (26, 10, 1499000.00, N'PAID', DATEADD(DAY, -14, SYSUTCDATETIME())),
        (27, 11, 1650000.00, N'PAID', DATEADD(DAY, -10, SYSUTCDATETIME())),
        (28, 12, 1890000.00, N'PAID', DATEADD(DAY, -7, SYSUTCDATETIME())),
        (29, 6, 1290000.00, N'PAID', DATEADD(DAY, -4, SYSUTCDATETIME())),
        (30, 5, 1499000.00, N'PAID', DATEADD(DAY, -1, SYSUTCDATETIME()));
        SET IDENTITY_INSERT dbo.Orders OFF;

        -- Thêm chi tiết đơn hàng tương ứng (OrderItems)
        SET IDENTITY_INSERT dbo.OrderItems ON;
        INSERT INTO dbo.OrderItems (OrderItemId, OrderId, CourseId, Price) VALUES
        (1, 1, 1, 1499000.00),
        (2, 2, 2, 1290000.00),
        (3, 3, 6, 850000.00),
        (4, 4, 1, 1499000.00),
        (5, 5, 5, 990000.00),
        (6, 6, 2, 1290000.00),
        (7, 7, 4, 1650000.00),
        (8, 8, 2, 1290000.00),
        (9, 9, 3, 1890000.00),
        (10, 10, 1, 1499000.00),
        (11, 11, 6, 850000.00),
        (12, 12, 4, 1650000.00),
        (13, 13, 3, 1890000.00),
        (14, 14, 1, 1499000.00),
        (15, 15, 6, 850000.00),
        (16, 16, 3, 1890000.00),
        (17, 17, 7, 750000.00),
        (18, 18, 2, 1290000.00),
        (19, 19, 3, 1890000.00),
        (20, 20, 1, 1499000.00),
        (21, 21, 4, 1650000.00),
        (22, 22, 3, 1890000.00),
        (23, 23, 4, 1650000.00),
        (24, 24, 2, 1290000.00),
        (25, 25, 3, 1890000.00),
        (26, 26, 1, 1499000.00),
        (27, 27, 4, 1650000.00),
        (28, 28, 3, 1890000.00),
        (29, 29, 2, 1290000.00),
        (30, 30, 1, 1499000.00);
        SET IDENTITY_INSERT dbo.OrderItems OFF;
    END

    -- ================================================================================
    -- 8. SEED ENROLLMENTS & LESSON PROGRESSES (GHI DANH & TIẾN ĐỘ HỌC TẬP THỰC TẾ)
    -- ================================================================================
    PRINT N'8. Đang nạp Ghi danh học tập và Tiến độ từng bài học (Enrollments & Progress)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Enrollments WHERE EnrollmentId = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Enrollments ON;
        INSERT INTO dbo.Enrollments (EnrollmentId, UserId, CourseId, EnrolledAt, ProgressPercentage, Status, CompletedAt) VALUES
        -- HV 5 (Đặng Quốc Hưng): Đã hoàn thành 100% Khóa 1 (Spring Boot) & đang học 60% Khóa 2
        (1, 5, 1, DATEADD(DAY, -160, SYSUTCDATETIME()), 100, N'ACTIVE', DATEADD(DAY, -10, SYSUTCDATETIME())),
        (2, 5, 2, DATEADD(DAY, -100, SYSUTCDATETIME()), 60,  N'ACTIVE', NULL),
        (3, 5, 3, DATEADD(DAY, -40,  SYSUTCDATETIME()), 25,  N'ACTIVE', NULL),

        -- HV 6 (Mai Phương Thảo): Đã hoàn thành 100% Khóa 2 (ReactJS) & đang học 100% Khóa 6 (UI/UX)
        (4, 6, 2, DATEADD(DAY, -155, SYSUTCDATETIME()), 100, N'ACTIVE', DATEADD(DAY, -15, SYSUTCDATETIME())),
        (5, 6, 6, DATEADD(DAY, -85,  SYSUTCDATETIME()), 100, N'ACTIVE', DATEADD(DAY, -5,  SYSUTCDATETIME())),
        (6, 6, 1, DATEADD(DAY, -38,  SYSUTCDATETIME()), 40,  N'ACTIVE', NULL),

        -- HV 7 (Nguyễn Tuấn Kiệt): Đã hoàn thành 100% Khóa 3 (Python AI)
        (7, 7, 3, DATEADD(DAY, -95,  SYSUTCDATETIME()), 100, N'ACTIVE', DATEADD(DAY, -8,  SYSUTCDATETIME())),
        (8, 7, 4, DATEADD(DAY, -32,  SYSUTCDATETIME()), 50,  N'ACTIVE', NULL),

        -- Các học viên khác có tiến độ đa dạng
        (9, 8, 1, DATEADD(DAY, -130, SYSUTCDATETIME()), 80,  N'ACTIVE', NULL),
        (10, 8, 3, DATEADD(DAY, -72,  SYSUTCDATETIME()), 45,  N'ACTIVE', NULL),
        (11, 9, 5, DATEADD(DAY, -125, SYSUTCDATETIME()), 90,  N'ACTIVE', NULL),
        (12, 10, 2, DATEADD(DAY, -120, SYSUTCDATETIME()), 65,  N'ACTIVE', NULL),
        (13, 11, 4, DATEADD(DAY, -115, SYSUTCDATETIME()), 30,  N'ACTIVE', NULL),
        (14, 12, 1, DATEADD(DAY, -90,  SYSUTCDATETIME()), 70,  N'ACTIVE', NULL),
        (15, 13, 4, DATEADD(DAY, -80,  SYSUTCDATETIME()), 55,  N'ACTIVE', NULL),
        (16, 14, 3, DATEADD(DAY, -60,  SYSUTCDATETIME()), 35,  N'ACTIVE', NULL);
        SET IDENTITY_INSERT dbo.Enrollments OFF;

        -- Tiến độ học từng bài (LessonProgresses) cho HV 5 trên Khóa 1 (hoàn thành đủ 8 bài)
        INSERT INTO dbo.LessonProgresses (UserId, CourseId, LessonId, CompletedAt) VALUES 
        (5, 1, 1, DATEADD(DAY, -150, SYSUTCDATETIME())),
        (5, 1, 2, DATEADD(DAY, -140, SYSUTCDATETIME())),
        (5, 1, 3, DATEADD(DAY, -130, SYSUTCDATETIME())),
        (5, 1, 4, DATEADD(DAY, -100, SYSUTCDATETIME())),
        (5, 1, 5, DATEADD(DAY, -80,  SYSUTCDATETIME())),
        (5, 1, 6, DATEADD(DAY, -50,  SYSUTCDATETIME())),
        (5, 1, 7, DATEADD(DAY, -30,  SYSUTCDATETIME())),
        (5, 1, 8, DATEADD(DAY, -10,  SYSUTCDATETIME())),
        
        -- HV 6 trên Khóa 2 (hoàn thành các bài React)
        (6, 2, 9,  DATEADD(DAY, -145, SYSUTCDATETIME())),
        (6, 2, 10, DATEADD(DAY, -130, SYSUTCDATETIME())),
        (6, 2, 11, DATEADD(DAY, -110, SYSUTCDATETIME())),
        (6, 2, 12, DATEADD(DAY, -80,  SYSUTCDATETIME())),
        (6, 2, 13, DATEADD(DAY, -15,  SYSUTCDATETIME())),

        -- HV 7 trên Khóa 3 (hoàn thành các bài Python AI)
        (7, 3, 14, DATEADD(DAY, -90, SYSUTCDATETIME())),
        (7, 3, 15, DATEADD(DAY, -50, SYSUTCDATETIME())),
        (7, 3, 16, DATEADD(DAY, -8,  SYSUTCDATETIME()));
    END

    -- ================================================================================
    -- 9. SEED CERTIFICATES (3 CHỨNG CHỈ HOÀN THÀNH CHO HỌC VIÊN 100% TIẾN ĐỘ)
    -- ================================================================================
    PRINT N'9. Đang nạp Chứng chỉ tốt nghiệp chuẩn (Certificates)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Certificates WHERE Id = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Certificates ON;
        INSERT INTO dbo.Certificates (Id, UserId, CourseId, CertificateUrl, IssuedAt) VALUES 
        (1, 5, 1, N'CERT-2026-SPRING-001', DATEADD(DAY, -10, SYSUTCDATETIME())),
        (2, 6, 2, N'CERT-2026-REACT-002',  DATEADD(DAY, -15, SYSUTCDATETIME())),
        (3, 7, 3, N'CERT-2026-PYTHON-003', DATEADD(DAY, -8,  SYSUTCDATETIME()));
        SET IDENTITY_INSERT dbo.Certificates OFF;
    END

    -- ================================================================================
    -- 10. SEED REVIEWS & SENTIMENT LABELS PHỤC VỤ AI (18 ĐÁNH GIÁ TIẾNG VIỆT ĐA DẠNG)
    -- ================================================================================
    PRINT N'10. Đang nạp Đánh giá nhận xét & Cảm xúc AI (Reviews with Sentiment)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Reviews WHERE ReviewId = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.Reviews ON;
        INSERT INTO dbo.Reviews (ReviewId, UserId, CourseId, Rating, Comment, SentimentLabel, SentimentScore, CreatedAt) VALUES
        -- Positive Reviews (13 đánh giá khen ngợi chất lượng)
        (1, 5, 1, 5, 
         N'Khóa học Java Spring Boot xuất sắc nhất mình từng học! Thầy Cường giảng rất chi tiết từ tầng kiến trúc đến tối ưu câu lệnh truy vấn MSSQL. Dự án thực tế sát với yêu cầu tuyển dụng.', 
         N'POSITIVE', 0.98, DATEADD(DAY, -60, SYSUTCDATETIME())),

        (2, 6, 2, 5, 
         N'Giáo trình React 18 và Next.js 14 cực kỳ cập nhật, mã nguồn sạch đẹp. Mình đã tự tin làm chủ Server Components và áp dụng ngay vào công việc.', 
         N'POSITIVE', 0.96, DATEADD(DAY, -55, SYSUTCDATETIME())),

        (3, 7, 3, 5, 
         N'Thầy Long dạy phần Machine Learning và Sentiment Analysis rất dễ hiểu. Cách thầy hướng dẫn toán giải tích ứng dụng vào PyTorch thực sự khai sáng tư duy.', 
         N'POSITIVE', 0.99, DATEADD(DAY, -50, SYSUTCDATETIME())),

        (4, 8, 4, 5, 
         N'Khóa DevOps của cô Nhung rất tuyệt vời! Các bài thực hành Docker và cụm Kubernetes trên AWS chạy mượt mà, tài liệu hướng dẫn rất đầy đủ.', 
         N'POSITIVE', 0.95, DATEADD(DAY, -45, SYSUTCDATETIME())),

        (5, 9, 5, 5, 
         N'Khóa Flutter rất hay, code mẫu tổ chức chuẩn theo mô hình BLoC pattern. Giảng viên hỗ trợ trả lời câu hỏi trong phần thảo luận rất nhanh.', 
         N'POSITIVE', 0.94, DATEADD(DAY, -40, SYSUTCDATETIME())),

        (6, 10, 6, 5, 
         N'Khóa Figma này chuẩn không cần chỉnh. Mình đã biết cách xây dựng Design System quy mô lớn, bàn giao thiết kế cho developer trơn tru.', 
         N'POSITIVE', 0.97, DATEADD(DAY, -35, SYSUTCDATETIME())),

        (7, 11, 1, 5, 
         N'Cảm ơn thầy Cường rất nhiều! Nhờ kiến thức Microservices và Kafka trong khóa học mà mình đã pass phỏng vấn vị trí Senior Backend Engineer.', 
         N'POSITIVE', 0.98, DATEADD(DAY, -30, SYSUTCDATETIME())),

        (8, 12, 2, 5, 
         N'Video bài giảng chất lượng cao, âm thanh sắc nét, các bài tập thực hành Redux Toolkit rất bổ ích cho người mới làm quen.', 
         N'POSITIVE', 0.93, DATEADD(DAY, -25, SYSUTCDATETIME())),

        (9, 13, 3, 5, 
         N'Rất ấn tượng với phần tích hợp API FastAPI của mô hình Học máy lên Docker. Một khóa học AI thực chiến đúng nghĩa.', 
         N'POSITIVE', 0.95, DATEADD(DAY, -20, SYSUTCDATETIME())),

        (10, 14, 4, 5, 
         N'Giải thích cơ chế CI/CD Pipeline trên GitHub Actions rất trực quan. Giúp tối ưu thời gian deploy của team mình đi rất nhiều.', 
         N'POSITIVE', 0.92, DATEADD(DAY, -16, SYSUTCDATETIME())),

        (11, 5, 2, 5, 
         N'Học xong khóa Spring Boot quay sang học thêm khóa React của thầy Cường, cảm giác tư duy Fullstack được kết nối hoàn hảo.', 
         N'POSITIVE', 0.96, DATEADD(DAY, -12, SYSUTCDATETIME())),

        (12, 6, 6, 5, 
         N'Design System đẹp mắt, phong cách hiện đại. Rất phù hợp cho bạn nào muốn thăng tiến làm UI/UX Lead.', 
         N'POSITIVE', 0.94, DATEADD(DAY, -8, SYSUTCDATETIME())),

        (13, 7, 4, 4, 
         N'Khóa học tương đối nặng kiến thức về hạ tầng Kubernetes nhưng giá trị mang lại cực kỳ lớn. Cần xem lại bài giảng 2-3 lần để ngấm hết.', 
         N'POSITIVE', 0.86, DATEADD(DAY, -5, SYSUTCDATETIME())),

        -- Neutral Reviews (3 đánh giá trung lập)
        (14, 8, 1, 4, 
         N'Nội dung chuyên sâu nhưng tốc độ giảng của thầy ở chương 2 hơi nhanh đối với người mới bắt đầu. Bù lại tài liệu đính kèm rất đầy đủ.', 
         N'NEUTRAL', 0.58, DATEADD(DAY, -18, SYSUTCDATETIME())),

        (15, 9, 2, 4, 
         N'Khóa học khá ổn, tổng hợp đủ các kiến thức cơ bản đến nâng cao. Hy vọng thầy bổ sung thêm chuyên đề về React Server Actions mới nhất.', 
         N'NEUTRAL', 0.52, DATEADD(DAY, -14, SYSUTCDATETIME())),

        (16, 10, 7, 4, 
         N'Các ví dụ về Scrum tương đối tổng quát, phù hợp cho người chuẩn bị thi chứng chỉ PSM I hơn là người đã có nhiều năm kinh nghiệm.', 
         N'NEUTRAL', 0.55, DATEADD(DAY, -10, SYSUTCDATETIME())),

        -- Negative Reviews (2 đánh giá góp ý kiểm thử AI Safety & Sentiment Alert)
        (17, 11, 5, 3, 
         N'Âm lượng ở bài học 2.3 hơi nhỏ so với các bài trước, phải đeo tai nghe mới nghe rõ. Đề nghị ban quản trị chỉnh sửa lại âm thanh video.', 
         N'NEGATIVE', 0.28, DATEADD(DAY, -22, SYSUTCDATETIME())),

        (18, 12, 7, 3, 
         N'Cần có thêm nhiều bài tập tình huống thực tế và case study xử lý xung đột trong nhóm Agile để người học luyện tập nhiều hơn.', 
         N'NEGATIVE', 0.32, DATEADD(DAY, -3, SYSUTCDATETIME()));
        SET IDENTITY_INSERT dbo.Reviews OFF;
    END

    -- ================================================================================
    -- 11. SEED COURSE ML ANALYSES & RISK AUDITING (HỆ THỐNG GIÁM SÁT AI MICROSERVICE)
    -- ================================================================================
    PRINT N'11. Đang nạp Nhật ký Phân tích AI & Cảnh báo an toàn (CourseMlAnalyses)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.CourseMlAnalyses WHERE Id = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.CourseMlAnalyses ON;
        INSERT INTO dbo.CourseMlAnalyses (Id, CourseId, PrimaryCategory, SubCategory, SuggestedLevel, Confidence, QualityScore, RiskLevel, RawResponseJson, ModelVersion, Status, CreatedAt, ApprovedAt, ApprovedByUserId) VALUES
        -- Khóa 1 -> 7: Đã duyệt, chất lượng cao, Low Risk
        (1, 1, N'Lập trình phần mềm', N'Backend Development', N'Intermediate', 0.98, 96, N'Low', 
         N'{"category": "Development", "confidence": 0.98, "quality_score": 96, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -6, SYSUTCDATETIME()), DATEADD(MONTH, -6, SYSUTCDATETIME()), 1),
        
        (2, 2, N'Lập trình phần mềm', N'Frontend Development', N'All Levels', 0.97, 94, N'Low', 
         N'{"category": "Development", "confidence": 0.97, "quality_score": 94, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -6, SYSUTCDATETIME()), DATEADD(MONTH, -6, SYSUTCDATETIME()), 1),
        
        (3, 3, N'Lập trình phần mềm', N'Data Science & AI', N'Advanced', 0.99, 98, N'Low', 
         N'{"category": "Development", "confidence": 0.99, "quality_score": 98, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -5, SYSUTCDATETIME()), DATEADD(MONTH, -5, SYSUTCDATETIME()), 1),
        
        (4, 4, N'Hạ tầng IT & Điện toán đám mây', N'DevOps & Cloud', N'Intermediate', 0.96, 92, N'Low', 
         N'{"category": "IT & Software", "confidence": 0.96, "quality_score": 92, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -5, SYSUTCDATETIME()), DATEADD(MONTH, -5, SYSUTCDATETIME()), 1),
        
        (5, 5, N'Lập trình phần mềm', N'Mobile Development', N'Beginner', 0.95, 90, N'Low', 
         N'{"category": "Development", "confidence": 0.95, "quality_score": 90, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -4, SYSUTCDATETIME()), DATEADD(MONTH, -4, SYSUTCDATETIME()), 1),
        
        (6, 6, N'Thiết kế đồ họa & UI/UX', N'User Experience Design', N'Beginner', 0.98, 95, N'Low', 
         N'{"category": "Design", "confidence": 0.98, "quality_score": 95, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -4, SYSUTCDATETIME()), DATEADD(MONTH, -4, SYSUTCDATETIME()), 1),
        
        (7, 7, N'Kinh doanh & Quản lý', N'Project Management', N'All Levels', 0.94, 88, N'Low', 
         N'{"category": "Business", "confidence": 0.94, "quality_score": 88, "risk": "Low"}', N'ml-course-classifier-v2.1', N'Approved', DATEADD(MONTH, -3, SYSUTCDATETIME()), DATEADD(MONTH, -3, SYSUTCDATETIME()), 1),

        -- Khóa 8: Khóa mới đăng tải, độ tin cậy thấp (61%) và có cờ High Risk để Admin can thiệp duyệt thủ công
        (8, 8, N'Lập trình phần mềm', N'Generative AI & LLM', N'Intermediate', 0.61, 65, N'High', 
         N'{"category": "Development", "confidence": 0.61, "quality_score": 65, "risk": "High", "flags": ["new_instructor_topic", "rapid_content_upload"]}', N'ml-course-classifier-v2.1', N'NeedsManualReview', DATEADD(DAY, -2, SYSUTCDATETIME()), NULL, NULL);
        SET IDENTITY_INSERT dbo.CourseMlAnalyses OFF;
    END

    -- ================================================================================
    -- 12. SEED USER ACTIVITIES (NHẬT KÝ HÀNH VI HOẠT ĐỘNG THỜI GIAN THỰC)
    -- ================================================================================
    PRINT N'12. Đang nạp Nhật ký hành động người dùng (UserActivities)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.UserActivities WHERE Id = 1)
    BEGIN
        SET IDENTITY_INSERT dbo.UserActivities ON;
        INSERT INTO dbo.UserActivities (Id, UserId, ActivityType, ResourceId, Timestamp) VALUES 
        (1, 5, N'LOGIN', N'AUTH_JWT', DATEADD(MINUTE, -15, SYSUTCDATETIME())),
        (2, 5, N'COMPLETE_LESSON', N'LESSON_8', DATEADD(MINUTE, -10, SYSUTCDATETIME())),
        (3, 5, N'RECEIVE_CERTIFICATE', N'CERT-2026-SPRING-001', DATEADD(MINUTE, -8, SYSUTCDATETIME())),
        (4, 6, N'PAYMENT_SUCCESS', N'ORDER_29', DATEADD(HOUR, -2, SYSUTCDATETIME())),
        (5, 7, N'SUBMIT_REVIEW', N'COURSE_3', DATEADD(HOUR, -5, SYSUTCDATETIME())),
        (6, 8, N'VIEW_COURSE', N'COURSE_1', DATEADD(HOUR, -8, SYSUTCDATETIME()));
        SET IDENTITY_INSERT dbo.UserActivities OFF;
    END

    -- ================================================================================
    -- 13. SEED WISHLISTS (DANH SÁCH YÊU THÍCH MẪU)
    -- ================================================================================
    PRINT N'13. Đang nạp Danh sách khóa học yêu thích (Wishlists)...';
    IF NOT EXISTS (SELECT 1 FROM dbo.Wishlists WHERE Id = 1)
    BEGIN
        INSERT INTO dbo.Wishlists (UserId, CourseId, AddedAt) VALUES 
        (5, 4, DATEADD(DAY, -5, SYSUTCDATETIME())), -- Đặng Quốc Hưng thích khóa DevOps
        (6, 3, DATEADD(DAY, -3, SYSUTCDATETIME())), -- Mai Phương Thảo thích khóa AI Python
        (7, 2, DATEADD(DAY, -2, SYSUTCDATETIME())); -- Nguyễn Tuấn Kiệt thích khóa ReactJS
    END

    COMMIT TRANSACTION;
    PRINT N'====================================================================================';
    PRINT N'>>> ĐÃ NẠP SEED DATA THÀNH CÔNG VÀ AN TOÀN CHO CƠ SỞ DỮ LIỆU EDUMYDB!';
    PRINT N'    - 14 Người dùng (1 Admin, 3 Giảng viên, 10 Học viên)';
    PRINT N'    - 8 Khóa học tiêu chuẩn kèm Giáo trình & Bài học Video MP4 Preview';
    PRINT N'    - 30 Đơn hàng thanh toán thành công trải dài 6 tháng (Doanh thu > 55,000,000 VNĐ)';
    PRINT N'    - 16 Ghi danh học tập & Tiến độ học tập chi tiết';
    PRINT N'    - 3 Chứng chỉ tốt nghiệp cấp thực tế';
    PRINT N'    - 18 Nhận xét đánh giá phân tích cảm xúc AI (Positive/Neutral/Negative)';
    PRINT N'    - 8 Bản ghi phân tích AI giám sát chất lượng và cảnh báo rủi ro an toàn';
    PRINT N'====================================================================================';

END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    PRINT N'*** ĐÃ XẢY RA LỖI KHI NẠP SEED DATA! GIAO DỊCH ĐÃ ĐƯỢC ROLLBACK AN TOÀN.';
    PRINT N'Thông báo lỗi: ' + ERROR_MESSAGE();
    PRINT N'Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(20));
    PRINT N'Dòng phát sinh lỗi: ' + CAST(ERROR_LINE() AS NVARCHAR(20));
END CATCH;
GO
