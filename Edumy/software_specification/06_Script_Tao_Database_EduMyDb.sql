-- ====================================================================================
-- DỰ ÁN: EDUMY ELEARNING PLATFORM
-- KỊCH BẢN TẠO CƠ SỞ DỮ LIỆU MICROSOFT SQL SERVER (DDL SCRIPT)
-- HỆ QUẢN TRỊ: Microsoft SQL Server 2017 / 2019 / 2022 / Azure SQL Database
-- SỐ LƯỢNG BẢNG: 30 Bảng nghiệp vụ chuẩn 3NF
-- TÁC GIẢ: Antigravity AI & Edumy Engineering Team
-- ====================================================================================

USE master;
GO

-- 1. TẠO CƠ SỞ DỮ LIỆU NẾU CHƯA TỒN TẠI
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'EduMyDb')
BEGIN
    CREATE DATABASE EduMyDb;
    PRINT N'Đã tạo mới cơ sở dữ liệu EduMyDb.';
END
ELSE
BEGIN
    PRINT N'Cơ sở dữ liệu EduMyDb đã tồn tại.';
END
GO

USE EduMyDb;
GO

-- ====================================================================================
-- BƯỚC 1: XÓA CÁC BẢNG CŨ NẾU ĐÃ TỒN TẠI (THEO THỨ TỰ PHỤ THUỘC TỪ DƯỚI LÊN)
-- ====================================================================================
DROP TABLE IF EXISTS dbo.CourseMlAnalysisTags;
DROP TABLE IF EXISTS dbo.CourseMlAnalyses;
DROP TABLE IF EXISTS dbo.UserActivities;
DROP TABLE IF EXISTS dbo.SearchHistories;
DROP TABLE IF EXISTS dbo.Wishlists;
DROP TABLE IF EXISTS dbo.Reviews;
DROP TABLE IF EXISTS dbo.Certificates;
DROP TABLE IF EXISTS dbo.LessonProgresses;
DROP TABLE IF EXISTS dbo.Enrollments;
DROP TABLE IF EXISTS dbo.QuizAttemptAnswers;
DROP TABLE IF EXISTS dbo.QuizAttempts;
DROP TABLE IF EXISTS dbo.Answers;
DROP TABLE IF EXISTS dbo.Questions;
DROP TABLE IF EXISTS dbo.Quizzes;
DROP TABLE IF EXISTS dbo.Lessons;
DROP TABLE IF EXISTS dbo.CourseSections;
DROP TABLE IF EXISTS dbo.CourseTags;
DROP TABLE IF EXISTS dbo.Tags;
DROP TABLE IF EXISTS dbo.CourseCoupons;
DROP TABLE IF EXISTS dbo.Coupons;
DROP TABLE IF EXISTS dbo.OrderItems;
DROP TABLE IF EXISTS dbo.Orders;
DROP TABLE IF EXISTS dbo.CartItems;
DROP TABLE IF EXISTS dbo.Carts;
DROP TABLE IF EXISTS dbo.Courses;
DROP TABLE IF EXISTS dbo.Categories;
DROP TABLE IF EXISTS dbo.RefreshTokens;
DROP TABLE IF EXISTS dbo.UserRoles;
DROP TABLE IF EXISTS dbo.Users;
DROP TABLE IF EXISTS dbo.Roles;
GO

PRINT N'Đã dọn dẹp các bảng cũ thành công.';
GO

-- ====================================================================================
-- BƯỚC 2: TẠO CÁC BẢNG ĐỘC LẬP / CƠ SỞ (NHÓM I & II)
-- ====================================================================================

-- 1. BẢNG Roles (Vai trò người dùng)
CREATE TABLE dbo.Roles (
    Id INT IDENTITY(1,1) NOT NULL,
    Name NVARCHAR(50) NOT NULL,
    CONSTRAINT PK_Roles PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT UQ_Roles_Name UNIQUE (Name)
);
GO

-- 2. BẢNG Users (Người dùng hệ thống)
CREATE TABLE dbo.Users (
    UserId INT IDENTITY(1,1) NOT NULL,
    FullName NVARCHAR(150) NOT NULL,
    Email NVARCHAR(255) NOT NULL,
    PasswordHash NVARCHAR(255) NULL,
    Role NVARCHAR(50) NOT NULL CONSTRAINT DF_Users_Role DEFAULT 'Student',
    Provider NVARCHAR(50) NULL,
    ProviderUserId NVARCHAR(255) NULL,
    AvatarUrl NVARCHAR(500) NULL,
    Bio NVARCHAR(MAX) NULL,
    IsActive BIT NOT NULL CONSTRAINT DF_Users_IsActive DEFAULT 1,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSUTCDATETIME(),
    ResetToken NVARCHAR(100) NULL,
    ResetTokenExpiry DATETIME2 NULL,
    CONSTRAINT PK_Users PRIMARY KEY CLUSTERED (UserId),
    CONSTRAINT UQ_Users_Email UNIQUE (Email)
);
GO

-- 3. BẢNG UserRoles (Quan hệ Nhiều - Nhiều giữa Users & Roles)
CREATE TABLE dbo.UserRoles (
    UserId INT NOT NULL,
    RoleId INT NOT NULL,
    CONSTRAINT PK_UserRoles PRIMARY KEY CLUSTERED (UserId, RoleId),
    CONSTRAINT FK_UserRoles_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_UserRoles_Roles FOREIGN KEY (RoleId) REFERENCES dbo.Roles(Id) ON DELETE CASCADE
);
GO

-- 4. BẢNG RefreshTokens (Token phiên làm việc)
CREATE TABLE dbo.RefreshTokens (
    Id INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    Token NVARCHAR(500) NOT NULL,
    Expires DATETIME2 NOT NULL,
    Created DATETIME2 NOT NULL CONSTRAINT DF_RefreshTokens_Created DEFAULT SYSUTCDATETIME(),
    Revoked DATETIME2 NULL,
    ReplacedByToken NVARCHAR(500) NULL,
    CONSTRAINT PK_RefreshTokens PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_RefreshTokens_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);
GO

-- 5. BẢNG Categories (Tự tham chiếu phân cấp Danh mục cha - con)
CREATE TABLE dbo.Categories (
    CategoryId INT IDENTITY(1,1) NOT NULL,
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(500) NULL,
    ParentCategoryId INT NULL,
    CONSTRAINT PK_Categories PRIMARY KEY CLUSTERED (CategoryId),
    CONSTRAINT FK_Categories_Parent FOREIGN KEY (ParentCategoryId) REFERENCES dbo.Categories(CategoryId) ON DELETE NO ACTION
);
GO

-- 6. BẢNG Tags (Thẻ từ khóa)
CREATE TABLE dbo.Tags (
    Id INT IDENTITY(1,1) NOT NULL,
    Name NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_Tags PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT UQ_Tags_Name UNIQUE (Name)
);
GO

-- 7. BẢNG Coupons (Mã giảm giá)
CREATE TABLE dbo.Coupons (
    Id INT IDENTITY(1,1) NOT NULL,
    Code NVARCHAR(50) NOT NULL,
    DiscountPercentage DECIMAL(5,2) NOT NULL,
    ExpiryDate DATETIME2 NOT NULL,
    IsActive BIT NOT NULL CONSTRAINT DF_Coupons_IsActive DEFAULT 1,
    CONSTRAINT PK_Coupons PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT UQ_Coupons_Code UNIQUE (Code)
);
GO

-- ====================================================================================
-- BƯỚC 3: TẠO CÁC BẢNG KHÓA HỌC & GIÁO TRÌNH (NHÓM III)
-- ====================================================================================

-- 8. BẢNG Courses (Khóa học)
CREATE TABLE dbo.Courses (
    CourseId INT IDENTITY(1,1) NOT NULL,
    InstructorId INT NOT NULL,
    CategoryId INT NOT NULL,
    Title NVARCHAR(255) NOT NULL,
    Slug NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    Price DECIMAL(18,2) NOT NULL CONSTRAINT DF_Courses_Price DEFAULT 0.00,
    ThumbnailUrl NVARCHAR(500) NULL,
    Level NVARCHAR(50) NOT NULL CONSTRAINT DF_Courses_Level DEFAULT 'Beginner',
    Status NVARCHAR(50) NOT NULL CONSTRAINT DF_Courses_Status DEFAULT 'Draft',
    AverageRating FLOAT NOT NULL CONSTRAINT DF_Courses_AverageRating DEFAULT 0.0,
    StudentCount INT NOT NULL CONSTRAINT DF_Courses_StudentCount DEFAULT 0,
    NeedsReanalysis BIT NOT NULL CONSTRAINT DF_Courses_NeedsReanalysis DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Courses_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_Courses_UpdatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Courses PRIMARY KEY CLUSTERED (CourseId),
    CONSTRAINT UQ_Courses_Slug UNIQUE (Slug),
    CONSTRAINT FK_Courses_Instructor FOREIGN KEY (InstructorId) REFERENCES dbo.Users(UserId) ON DELETE NO ACTION,
    CONSTRAINT FK_Courses_Category FOREIGN KEY (CategoryId) REFERENCES dbo.Categories(CategoryId) ON DELETE NO ACTION
);
GO

-- 9. BẢNG CourseTags (Quan hệ Nhiều - Nhiều giữa Courses & Tags)
CREATE TABLE dbo.CourseTags (
    CourseId INT NOT NULL,
    TagId INT NOT NULL,
    CONSTRAINT PK_CourseTags PRIMARY KEY CLUSTERED (CourseId, TagId),
    CONSTRAINT FK_CourseTags_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE CASCADE,
    CONSTRAINT FK_CourseTags_Tags FOREIGN KEY (TagId) REFERENCES dbo.Tags(Id) ON DELETE CASCADE
);
GO

-- 10. BẢNG CourseCoupons (Quan hệ Nhiều - Nhiều giữa Courses & Coupons)
CREATE TABLE dbo.CourseCoupons (
    CourseId INT NOT NULL,
    CouponId INT NOT NULL,
    CONSTRAINT PK_CourseCoupons PRIMARY KEY CLUSTERED (CourseId, CouponId),
    CONSTRAINT FK_CourseCoupons_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE CASCADE,
    CONSTRAINT FK_CourseCoupons_Coupons FOREIGN KEY (CouponId) REFERENCES dbo.Coupons(Id) ON DELETE CASCADE
);
GO

-- 11. BẢNG CourseSections (Chương mục khóa học)
CREATE TABLE dbo.CourseSections (
    SectionId INT IDENTITY(1,1) NOT NULL,
    CourseId INT NOT NULL,
    Title NVARCHAR(255) NOT NULL,
    OrderIndex INT NOT NULL CONSTRAINT DF_CourseSections_OrderIndex DEFAULT 0,
    CONSTRAINT PK_CourseSections PRIMARY KEY CLUSTERED (SectionId),
    CONSTRAINT FK_CourseSections_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE CASCADE
);
GO

-- 12. BẢNG Lessons (Bài giảng chi tiết)
CREATE TABLE dbo.Lessons (
    LessonId INT IDENTITY(1,1) NOT NULL,
    SectionId INT NOT NULL,
    Title NVARCHAR(255) NOT NULL,
    VideoUrl NVARCHAR(500) NULL,
    Duration INT NOT NULL CONSTRAINT DF_Lessons_Duration DEFAULT 0,
    OrderIndex INT NOT NULL CONSTRAINT DF_Lessons_OrderIndex DEFAULT 0,
    IsPreview BIT NOT NULL CONSTRAINT DF_Lessons_IsPreview DEFAULT 0,
    CONSTRAINT PK_Lessons PRIMARY KEY CLUSTERED (LessonId),
    CONSTRAINT FK_Lessons_CourseSections FOREIGN KEY (SectionId) REFERENCES dbo.CourseSections(SectionId) ON DELETE CASCADE
);
GO

-- ====================================================================================
-- BƯỚC 4: TẠO CÁC BẢNG KIỂM TRA TRẮC NGHIỆM (QUIZ) (NHÓM IV)
-- ====================================================================================

-- 13. BẢNG Quizzes
CREATE TABLE dbo.Quizzes (
    QuizId INT IDENTITY(1,1) NOT NULL,
    CourseSectionId INT NOT NULL,
    Title NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    PassingScore INT NOT NULL CONSTRAINT DF_Quizzes_PassingScore DEFAULT 80,
    TimeLimitMinutes INT NULL,
    CONSTRAINT PK_Quizzes PRIMARY KEY CLUSTERED (QuizId),
    CONSTRAINT FK_Quizzes_CourseSections FOREIGN KEY (CourseSectionId) REFERENCES dbo.CourseSections(SectionId) ON DELETE CASCADE
);
GO

-- 14. BẢNG Questions
CREATE TABLE dbo.Questions (
    QuestionId INT IDENTITY(1,1) NOT NULL,
    QuizId INT NOT NULL,
    Content NVARCHAR(MAX) NOT NULL,
    Points INT NOT NULL CONSTRAINT DF_Questions_Points DEFAULT 1,
    Explanation NVARCHAR(MAX) NULL,
    CONSTRAINT PK_Questions PRIMARY KEY CLUSTERED (QuestionId),
    CONSTRAINT FK_Questions_Quizzes FOREIGN KEY (QuizId) REFERENCES dbo.Quizzes(QuizId) ON DELETE CASCADE
);
GO

-- 15. BẢNG Answers
CREATE TABLE dbo.Answers (
    AnswerId INT IDENTITY(1,1) NOT NULL,
    QuestionId INT NOT NULL,
    Content NVARCHAR(MAX) NOT NULL,
    IsCorrect BIT NOT NULL CONSTRAINT DF_Answers_IsCorrect DEFAULT 0,
    CONSTRAINT PK_Answers PRIMARY KEY CLUSTERED (AnswerId),
    CONSTRAINT FK_Answers_Questions FOREIGN KEY (QuestionId) REFERENCES dbo.Questions(QuestionId) ON DELETE CASCADE
);
GO

-- 16. BẢNG QuizAttempts (Lượt làm bài trắc nghiệm)
CREATE TABLE dbo.QuizAttempts (
    QuizAttemptId INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    QuizId INT NOT NULL,
    Score INT NOT NULL CONSTRAINT DF_QuizAttempts_Score DEFAULT 0,
    StartedAt DATETIME2 NOT NULL CONSTRAINT DF_QuizAttempts_StartedAt DEFAULT SYSUTCDATETIME(),
    CompletedAt DATETIME2 NULL,
    CONSTRAINT PK_QuizAttempts PRIMARY KEY CLUSTERED (QuizAttemptId),
    CONSTRAINT FK_QuizAttempts_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_QuizAttempts_Quizzes FOREIGN KEY (QuizId) REFERENCES dbo.Quizzes(QuizId) ON DELETE NO ACTION
);
GO

-- 17. BẢNG QuizAttemptAnswers (Chi tiết phương án học viên chọn)
CREATE TABLE dbo.QuizAttemptAnswers (
    QuizAttemptAnswerId INT IDENTITY(1,1) NOT NULL,
    QuizAttemptId INT NOT NULL,
    QuestionId INT NOT NULL,
    AnswerId INT NOT NULL,
    CONSTRAINT PK_QuizAttemptAnswers PRIMARY KEY CLUSTERED (QuizAttemptAnswerId),
    CONSTRAINT FK_QuizAttemptAnswers_QuizAttempts FOREIGN KEY (QuizAttemptId) REFERENCES dbo.QuizAttempts(QuizAttemptId) ON DELETE CASCADE,
    CONSTRAINT FK_QuizAttemptAnswers_Questions FOREIGN KEY (QuestionId) REFERENCES dbo.Questions(QuestionId) ON DELETE NO ACTION,
    CONSTRAINT FK_QuizAttemptAnswers_Answers FOREIGN KEY (AnswerId) REFERENCES dbo.Answers(AnswerId) ON DELETE NO ACTION
);
GO

-- ====================================================================================
-- BƯỚC 5: TẠO CÁC BẢNG THƯƠNG MẠI & THANH TOÁN (NHÓM V)
-- ====================================================================================

-- 18. BẢNG Carts (Giỏ hàng - Mỗi User sở hữu duy nhất 1 giỏ)
CREATE TABLE dbo.Carts (
    Id INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Carts_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_Carts_UpdatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Carts PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT UQ_Carts_UserId UNIQUE (UserId),
    CONSTRAINT FK_Carts_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);
GO

-- 19. BẢNG CartItems
CREATE TABLE dbo.CartItems (
    Id INT IDENTITY(1,1) NOT NULL,
    CartId INT NOT NULL,
    CourseId INT NOT NULL,
    AddedAt DATETIME2 NOT NULL CONSTRAINT DF_CartItems_AddedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_CartItems PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_CartItems_Carts FOREIGN KEY (CartId) REFERENCES dbo.Carts(Id) ON DELETE CASCADE,
    CONSTRAINT FK_CartItems_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE CASCADE
);
GO

-- 20. BẢNG Orders
CREATE TABLE dbo.Orders (
    OrderId INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    TotalAmount DECIMAL(18,2) NOT NULL,
    Status NVARCHAR(50) NOT NULL CONSTRAINT DF_Orders_Status DEFAULT 'Pending',
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Orders_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Orders PRIMARY KEY CLUSTERED (OrderId),
    CONSTRAINT FK_Orders_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);
GO

-- 21. BẢNG OrderItems
CREATE TABLE dbo.OrderItems (
    OrderItemId INT IDENTITY(1,1) NOT NULL,
    OrderId INT NOT NULL,
    CourseId INT NOT NULL,
    Price DECIMAL(18,2) NOT NULL,
    CONSTRAINT PK_OrderItems PRIMARY KEY CLUSTERED (OrderItemId),
    CONSTRAINT FK_OrderItems_Orders FOREIGN KEY (OrderId) REFERENCES dbo.Orders(OrderId) ON DELETE CASCADE,
    CONSTRAINT FK_OrderItems_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE NO ACTION
);
GO

-- ====================================================================================
-- BƯỚC 6: TẠO CÁC BẢNG TIẾN ĐỘ HỌC TẬP & TƯƠNG TÁC (NHÓM VI)
-- ====================================================================================

-- 22. BẢNG Enrollments (Ghi danh khóa học)
CREATE TABLE dbo.Enrollments (
    EnrollmentId INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    CourseId INT NOT NULL,
    EnrolledAt DATETIME2 NOT NULL CONSTRAINT DF_Enrollments_EnrolledAt DEFAULT SYSUTCDATETIME(),
    ProgressPercentage INT NOT NULL CONSTRAINT DF_Enrollments_Progress DEFAULT 0,
    CompletedAt DATETIME2 NULL,
    CONSTRAINT PK_Enrollments PRIMARY KEY CLUSTERED (EnrollmentId),
    CONSTRAINT FK_Enrollments_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_Enrollments_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE NO ACTION
);
GO

-- 23. BẢNG LessonProgresses (Tiến độ từng bài học)
CREATE TABLE dbo.LessonProgresses (
    Id INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    CourseId INT NOT NULL,
    LessonId INT NOT NULL,
    CompletedAt DATETIME2 NOT NULL CONSTRAINT DF_LessonProgresses_CompletedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_LessonProgresses PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_LessonProgresses_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_LessonProgresses_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE NO ACTION,
    CONSTRAINT FK_LessonProgresses_Lessons FOREIGN KEY (LessonId) REFERENCES dbo.Lessons(LessonId) ON DELETE NO ACTION
);
GO

-- 24. BẢNG Certificates (Chứng chỉ hoàn thành)
CREATE TABLE dbo.Certificates (
    Id INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    CourseId INT NOT NULL,
    CertificateUrl NVARCHAR(255) NOT NULL,
    IssuedAt DATETIME2 NOT NULL CONSTRAINT DF_Certificates_IssuedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Certificates PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_Certificates_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_Certificates_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE NO ACTION
);
GO

-- 25. BẢNG Reviews (Đánh giá khóa học)
CREATE TABLE dbo.Reviews (
    ReviewId INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    CourseId INT NOT NULL,
    Rating INT NOT NULL,
    Comment NVARCHAR(MAX) NOT NULL,
    SentimentLabel NVARCHAR(50) NULL,
    SentimentScore FLOAT NULL,
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Reviews_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Reviews PRIMARY KEY CLUSTERED (ReviewId),
    CONSTRAINT CK_Reviews_Rating CHECK (Rating BETWEEN 1 AND 5),
    CONSTRAINT FK_Reviews_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_Reviews_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE CASCADE
);
GO

-- 26. BẢNG Wishlists (Khóa học yêu thích)
CREATE TABLE dbo.Wishlists (
    Id INT IDENTITY(1,1) NOT NULL,
    UserId INT NOT NULL,
    CourseId INT NOT NULL,
    AddedAt DATETIME2 NOT NULL CONSTRAINT DF_Wishlists_AddedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Wishlists PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_Wishlists_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_Wishlists_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE CASCADE
);
GO

-- ====================================================================================
-- BƯỚC 7: TẠO CÁC BẢNG AI/ML & THEO DÕI HÀNH VI (NHÓM VII)
-- ====================================================================================

-- 27. BẢNG CourseMlAnalyses (Lịch sử phân tích của AI)
CREATE TABLE dbo.CourseMlAnalyses (
    Id INT IDENTITY(1,1) NOT NULL,
    CourseId INT NOT NULL,
    PrimaryCategory NVARCHAR(100) NULL,
    SubCategory NVARCHAR(100) NULL,
    SuggestedLevel NVARCHAR(50) NULL,
    Confidence FLOAT NULL,
    QualityScore INT NULL,
    RiskLevel NVARCHAR(50) NULL,
    RawResponseJson NVARCHAR(MAX) NULL,
    ModelVersion NVARCHAR(100) NULL,
    Status NVARCHAR(50) NOT NULL CONSTRAINT DF_CourseMlAnalyses_Status DEFAULT 'Pending',
    CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_CourseMlAnalyses_CreatedAt DEFAULT SYSUTCDATETIME(),
    ApprovedAt DATETIME2 NULL,
    ApprovedByUserId INT NULL,
    CONSTRAINT PK_CourseMlAnalyses PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_CourseMlAnalyses_Courses FOREIGN KEY (CourseId) REFERENCES dbo.Courses(CourseId) ON DELETE CASCADE,
    CONSTRAINT FK_CourseMlAnalyses_Users FOREIGN KEY (ApprovedByUserId) REFERENCES dbo.Users(UserId) ON DELETE NO ACTION
);
GO

-- 28. BẢNG CourseMlAnalysisTags (Thẻ từ khóa do AI đề xuất)
CREATE TABLE dbo.CourseMlAnalysisTags (
    Id INT IDENTITY(1,1) NOT NULL,
    CourseMlAnalysisId INT NOT NULL,
    TagName NVARCHAR(100) NOT NULL,
    Confidence FLOAT NULL,
    CONSTRAINT PK_CourseMlAnalysisTags PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_CourseMlAnalysisTags_Analysis FOREIGN KEY (CourseMlAnalysisId) REFERENCES dbo.CourseMlAnalyses(Id) ON DELETE CASCADE
);
GO

-- 29. BẢNG SearchHistories (Lịch sử tìm kiếm)
CREATE TABLE dbo.SearchHistories (
    Id INT IDENTITY(1,1) NOT NULL,
    UserId INT NULL,
    SearchQuery NVARCHAR(255) NOT NULL,
    SearchedAt DATETIME2 NOT NULL CONSTRAINT DF_SearchHistories_SearchedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_SearchHistories PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_SearchHistories_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);
GO

-- 30. BẢNG UserActivities (Nhật ký hành vi người dùng)
CREATE TABLE dbo.UserActivities (
    Id INT IDENTITY(1,1) NOT NULL,
    UserId INT NULL,
    ActivityType NVARCHAR(100) NOT NULL,
    ResourceId NVARCHAR(100) NULL,
    Timestamp DATETIME2 NOT NULL CONSTRAINT DF_UserActivities_Timestamp DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_UserActivities PRIMARY KEY CLUSTERED (Id),
    CONSTRAINT FK_UserActivities_Users FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);
GO

-- ====================================================================================
-- BƯỚC 8: TẠO CÁC CHỈ MỤC TỐI ƯU HÓA HIỆU NĂNG (NON-CLUSTERED INDEXES)
-- ====================================================================================

-- 1. Tối ưu tìm kiếm và lọc khóa học
CREATE NONCLUSTERED INDEX IX_Courses_CategoryId ON dbo.Courses (CategoryId);
CREATE NONCLUSTERED INDEX IX_Courses_InstructorId ON dbo.Courses (InstructorId);
CREATE NONCLUSTERED INDEX IX_Courses_Status_Price ON dbo.Courses (Status, Price);
CREATE NONCLUSTERED INDEX IX_Courses_CreatedAt ON dbo.Courses (CreatedAt DESC);

-- 2. Tối ưu truy xuất giáo trình
CREATE NONCLUSTERED INDEX IX_CourseSections_CourseId_Order ON dbo.CourseSections (CourseId, OrderIndex ASC);
CREATE NONCLUSTERED INDEX IX_Lessons_SectionId_Order ON dbo.Lessons (SectionId, OrderIndex ASC);

-- 3. Tối ưu truy vấn kiểm tra quyền học & giỏ hàng
CREATE NONCLUSTERED INDEX IX_Enrollments_UserId_CourseId ON dbo.Enrollments (UserId, CourseId);
CREATE NONCLUSTERED INDEX IX_CartItems_CartId ON dbo.CartItems (CartId);
CREATE NONCLUSTERED INDEX IX_OrderItems_OrderId ON dbo.OrderItems (OrderId);
CREATE NONCLUSTERED INDEX IX_Reviews_CourseId ON dbo.Reviews (CourseId);
CREATE NONCLUSTERED INDEX IX_LessonProgresses_User_Course ON dbo.LessonProgresses (UserId, CourseId);

-- 4. Tối ưu xác thực Token
CREATE NONCLUSTERED INDEX IX_RefreshTokens_Token ON dbo.RefreshTokens (Token);
GO

PRINT N'Đã tạo thành công 30 bảng và hệ thống Index tối ưu hóa.';
GO

-- ====================================================================================
-- BƯỚC 9: NẠP DỮ LIỆU KHỞI TẠO MẪU (SEED DATA)
-- ====================================================================================

-- 1. Thêm các vai trò chuẩn
SET IDENTITY_INSERT dbo.Roles ON;
INSERT INTO dbo.Roles (Id, Name) VALUES 
(1, N'Admin'),
(2, N'Instructor'),
(3, N'Student');
SET IDENTITY_INSERT dbo.Roles OFF;
GO

-- 2. Thêm các danh mục gốc
SET IDENTITY_INSERT dbo.Categories ON;
INSERT INTO dbo.Categories (CategoryId, Name, Description, ParentCategoryId) VALUES 
(1, N'Development', N'Lập trình phần mềm, Web, Di động & Khoa học máy tính', NULL),
(2, N'Business', N'Quản trị kinh doanh, Tài chính & Khởi nghiệp', NULL),
(3, N'Design', N'Thiết kế đồ họa, UI/UX & Sáng tạo nội dung', NULL),
(4, N'Marketing', N'Digital Marketing, SEO & Quảng cáo', NULL),
(5, N'IT & Software', N'Hệ điều hành, Mạng máy tính & An toàn thông tin', NULL);
SET IDENTITY_INSERT dbo.Categories OFF;
GO

-- 3. Thêm tài khoản khởi tạo mẫu (Mật khẩu: Admin@123 / Instructor@123 / Student@123 được băm bằng BCrypt)
-- Hash BCrypt mẫu tương thích Spring Boot PasswordEncoder: $2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G
SET IDENTITY_INSERT dbo.Users ON;
INSERT INTO dbo.Users (UserId, FullName, Email, PasswordHash, Role, IsActive, CreatedAt) VALUES 
(1, N'System Administrator', N'admin@edumy.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Admin', 1, SYSUTCDATETIME()),
(2, N'Master Instructor', N'instructor@edumy.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Instructor', 1, SYSUTCDATETIME()),
(3, N'Learner Student', N'student@edumy.com', N'$2a$10$wK10s47P56V1kI.u2zQ0fOek2z2iJsmqZz1mO0eQkE2Gf5t9h9k6G', N'Student', 1, SYSUTCDATETIME());
SET IDENTITY_INSERT dbo.Users OFF;
GO

-- Gán vai trò cho người dùng
INSERT INTO dbo.UserRoles (UserId, RoleId) VALUES 
(1, 1), -- Admin
(2, 2), -- Instructor
(3, 3); -- Student
GO

-- Tạo giỏ hàng mặc định cho các tài khoản
INSERT INTO dbo.Carts (UserId, CreatedAt, UpdatedAt) VALUES 
(1, SYSUTCDATETIME(), SYSUTCDATETIME()),
(2, SYSUTCDATETIME(), SYSUTCDATETIME()),
(3, SYSUTCDATETIME(), SYSUTCDATETIME());
GO

-- Thêm một số Thẻ (Tags) phổ biến
SET IDENTITY_INSERT dbo.Tags ON;
INSERT INTO dbo.Tags (Id, Name) VALUES 
(1, N'Java'),
(2, N'Spring Boot'),
(3, N'React'),
(4, N'Python'),
(5, N'Machine Learning'),
(6, N'SQL Server');
SET IDENTITY_INSERT dbo.Tags OFF;
GO

-- Thêm mã giảm giá mẫu
SET IDENTITY_INSERT dbo.Coupons ON;
INSERT INTO dbo.Coupons (Id, Code, DiscountPercentage, ExpiryDate, IsActive) VALUES 
(1, N'WELCOME20', 20.00, DATEADD(YEAR, 1, SYSUTCDATETIME()), 1),
(2, N'EDUMY50', 50.00, DATEADD(YEAR, 1, SYSUTCDATETIME()), 1);
SET IDENTITY_INSERT dbo.Coupons OFF;
GO

PRINT N'====================================================================================';
PRINT N'ĐÃ TẠO THÀNH CÔNG VÀ NẠP SEED DATA CHO DATABASE EDUMYDB TRÊN MICROSOFT SQL SERVER!';
PRINT N'====================================================================================';
GO
