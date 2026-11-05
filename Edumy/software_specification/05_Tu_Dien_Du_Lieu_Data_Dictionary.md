# PHẦN 5: TỪ ĐIỂN DỮ LIỆU CHI TIẾT (DATA DICTIONARY)
*(Database Schema & Column-Level Specifications)*

Tài liệu này đặc tả chi tiết toàn bộ **30 bảng dữ liệu** trong cơ sở dữ liệu `EduMyDb` của hệ thống Edumy trên Microsoft SQL Server.

---

## NHÓM I: NGƯỜI DÙNG & XÁC THỰC (AUTHENTICATION & IDENTITY)

### 1. Bảng `Roles` (Vai trò người dùng)
*Mô tả*: Quản lý danh mục vai trò người dùng trong hệ thống RBAC (`Admin`, `Instructor`, `Student`).
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã định danh vai trò |
| `Name` | NVARCHAR(50) | NO | UNIQUE | | Tên vai trò (Admin, Instructor, Student) |

### 2. Bảng `Users` (Người dùng)
*Mô tả*: Lưu trữ thông tin tài khoản, hồ sơ cá nhân và trạng thái đăng nhập của người dùng.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `UserId` | INT | NO | PK, IDENTITY(1,1) | | Mã định danh người dùng duy nhất |
| `FullName` | NVARCHAR(150) | NO | | | Họ và tên đầy đủ |
| `Email` | NVARCHAR(255) | NO | UNIQUE | | Email đăng nhập duy nhất trên toàn sàn |
| `PasswordHash` | NVARCHAR(255) | YES | | | Mật khẩu đã được mã hóa chuẩn BCrypt |
| `Role` | NVARCHAR(50) | NO | | 'Student' | Vai trò chính của người dùng |
| `Provider` | NVARCHAR(50) | YES | | | Nhà cung cấp định danh (`Local`, `Google`) |
| `ProviderUserId`| NVARCHAR(255) | YES | | | ID người dùng từ Google OAuth |
| `AvatarUrl` | NVARCHAR(500) | YES | | | URL hình ảnh đại diện cá nhân |
| `Bio` | NVARCHAR(MAX) | YES | | | Tiểu sử / Giới thiệu bản thân |
| `IsActive` | BIT | NO | | 1 | Trạng thái (1: Đang hoạt động, 0: Đang bị khóa) |
| `CreatedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm tạo tài khoản |
| `ResetToken` | NVARCHAR(100) | YES | | | Mã phục hồi mật khẩu gồm 8 ký tự |
| `ResetTokenExpiry`| DATETIME2 | YES | | | Hạn sử dụng mã phục hồi (15 phút) |

### 3. Bảng `UserRoles` (Bảng liên kết Nhiều - Nhiều Người dùng & Vai trò)
*Mô tả*: Bảng phụ phục vụ mô hình nhiều người dùng có thể sở hữu nhiều vai trò dự phòng.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `UserId` | INT | NO | PK, FK -> `Users(UserId)` | | Mã người dùng |
| `RoleId` | INT | NO | PK, FK -> `Roles(Id)` | | Mã vai trò được gán |

### 4. Bảng `RefreshTokens` (Token phiên làm việc)
*Mô tả*: Lưu trữ Refresh Token bảo mật trong Cookie HttpOnly phục vụ cơ chế Token Rotation.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã định danh bản ghi token |
| `UserId` | INT | NO | FK -> `Users(UserId)` | | Người dùng sở hữu token |
| `Token` | NVARCHAR(500) | NO | | | Chuỗi ngẫu nhiên Refresh Token an toàn |
| `Expires` | DATETIME2 | NO | | | Thời điểm hết hạn token (mặc định 7 ngày) |
| `Created` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm tạo token |
| `Revoked` | DATETIME2 | YES | | | Thời điểm token bị thu hồi khi xoay vòng |
| `ReplacedByToken`| NVARCHAR(500) | YES | | | Token mới thay thế token này |

---

## NHÓM II: DANH MỤC & PHÂN LOẠI (CATEGORIES & TAGS)

### 5. Bảng `Categories` (Danh mục khóa học)
*Mô tả*: Cây danh mục khóa học hỗ trợ phân cấp cha - con nhiều tầng.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `CategoryId` | INT | NO | PK, IDENTITY(1,1) | | Mã danh mục |
| `Name` | NVARCHAR(100) | NO | | | Tên danh mục (ví dụ: Development, Design) |
| `Description` | NVARCHAR(500) | YES | | | Mô tả danh mục |
| `ParentCategoryId`| INT | YES | FK -> `Categories(CategoryId)`| | Mã danh mục cha trực tiếp |

### 6. Bảng `Tags` (Thẻ từ khóa)
*Mô tả*: Danh mục các từ khóa chủ đề (ví dụ: Java, React, Machine Learning).
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã thẻ từ khóa |
| `Name` | NVARCHAR(100) | NO | UNIQUE | | Tên thẻ từ khóa duy nhất |

### 7. Bảng `CourseTags` (Liên kết Khóa học & Thẻ)
*Mô tả*: Quan hệ Nhiều - Nhiều giữa Khóa học và Thẻ phân loại.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `CourseId` | INT | NO | PK, FK -> `Courses(CourseId)` | | Mã khóa học |
| `TagId` | INT | NO | PK, FK -> `Tags(Id)` | | Mã thẻ từ khóa |

---

## NHÓM III: KHÓA HỌC & GIÁO TRÌNH (COURSES & CURRICULUM)

### 8. Bảng `Courses` (Khóa học)
*Mô tả*: Thực thể trung tâm lưu trữ thông tin sản phẩm khóa học của giảng viên.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `CourseId` | INT | NO | PK, IDENTITY(1,1) | | Mã khóa học duy nhất |
| `InstructorId` | INT | NO | FK -> `Users(UserId)` | | Giảng viên tạo và sở hữu khóa học |
| `CategoryId` | INT | NO | FK -> `Categories(CategoryId)`| | Danh mục chính của khóa học |
| `Title` | NVARCHAR(255) | NO | | | Tiêu đề khóa học |
| `Slug` | NVARCHAR(255) | NO | UNIQUE | | Đường dẫn thân thiện SEO |
| `Description` | NVARCHAR(MAX) | YES | | | Nội dung chi tiết khóa học |
| `Price` | DECIMAL(18,2) | NO | | 0.00 | Giá bán niêm yết |
| `ThumbnailUrl` | NVARCHAR(500) | YES | | | Đường dẫn ảnh đại diện |
| `Level` | NVARCHAR(50) | NO | | 'Beginner' | Trình độ (`Beginner`, `Intermediate`, `Advanced`)|
| `Status` | NVARCHAR(50) | NO | | 'Draft' | Trạng thái (`Draft`, `Analyzing`, `NeedsReview`, `PendingApproval`, `Published`)|
| `AverageRating`| FLOAT | NO | | 0.0 | Điểm đánh giá trung bình từ học viên |
| `StudentCount` | INT | NO | | 0 | Tổng số học viên đã ghi danh thành công |
| `NeedsReanalysis`| BIT | NO | | 0 | Cờ yêu cầu quét lại AI khi sửa đổi |
| `CreatedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm tạo khóa học |
| `UpdatedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm cập nhật cuối |

### 9. Bảng `CourseSections` (Chương mục khóa học)
*Mô tả*: Chia khóa học thành từng chương/phần kiến thức logic.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `SectionId` | INT | NO | PK, IDENTITY(1,1) | | Mã chương mục |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học trực thuộc |
| `Title` | NVARCHAR(255) | NO | | | Tiêu đề chương học |
| `OrderIndex` | INT | NO | | 0 | Thứ tự sắp xếp hiển thị trong khóa học |

### 10. Bảng `Lessons` (Bài học chi tiết)
*Mô tả*: Từng bài giảng cụ thể trong một chương mục.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `LessonId` | INT | NO | PK, IDENTITY(1,1) | | Mã bài học |
| `SectionId` | INT | NO | FK -> `CourseSections(SectionId)`| | Chương mục trực thuộc |
| `Title` | NVARCHAR(255) | NO | | | Tiêu đề bài giảng |
| `VideoUrl` | NVARCHAR(500) | YES | | | Đường dẫn video bài học |
| `Duration` | INT | NO | | 0 | Thời lượng bài học (tính bằng phút) |
| `OrderIndex` | INT | NO | | 0 | Thứ tự bài học trong chương |
| `IsPreview` | BIT | NO | | 0 | Cờ học thử miễn phí (1: Cho phép xem trước) |

---

## NHÓM IV: ĐÁNH GIÁ TRẮC NGHIỆM (QUIZ & ASSESSMENT)

### 11. Bảng `Quizzes` (Bài kiểm tra trắc nghiệm)
*Mô tả*: Đề kiểm tra đánh giá năng lực gắn liền với chương học.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `QuizId` | INT | NO | PK, IDENTITY(1,1) | | Mã bài kiểm tra |
| `CourseSectionId`| INT | NO | FK -> `CourseSections(SectionId)`| | Chương mục gắn kèm đề thi |
| `Title` | NVARCHAR(255) | NO | | | Tên bài trắc nghiệm |
| `Description` | NVARCHAR(MAX) | YES | | | Hướng dẫn làm bài |
| `PassingScore` | INT | NO | | 80 | Tỷ lệ điểm tối thiểu để đạt (%) |
| `TimeLimitMinutes`| INT | YES | | | Thời gian giới hạn làm bài (phút) |

### 12. Bảng `Questions` (Câu hỏi trong Quiz)
*Mô tả*: Các câu hỏi trắc nghiệm thuộc một đề thi Quiz.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `QuestionId` | INT | NO | PK, IDENTITY(1,1) | | Mã câu hỏi |
| `QuizId` | INT | NO | FK -> `Quizzes(QuizId)` | | Đề thi trắc nghiệm trực thuộc |
| `Content` | NVARCHAR(MAX) | NO | | | Nội dung câu hỏi |
| `Points` | INT | NO | | 1 | Điểm số của câu hỏi |
| `Explanation` | NVARCHAR(MAX) | YES | | | Lời giải thích khi xem lại kết quả |

### 13. Bảng `Answers` (Các phương án trả lời)
*Mô tả*: Các lựa chọn trả lời cho một câu hỏi trắc nghiệm.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `AnswerId` | INT | NO | PK, IDENTITY(1,1) | | Mã phương án lựa chọn |
| `QuestionId` | INT | NO | FK -> `Questions(QuestionId)`| | Câu hỏi trực thuộc |
| `Content` | NVARCHAR(MAX) | NO | | | Nội dung phương án |
| `IsCorrect` | BIT | NO | | 0 | Đánh dấu đáp án đúng (1: Đúng, 0: Sai) |

### 14. Bảng `QuizAttempts` (Lượt làm bài kiểm tra)
*Mô tả*: Ghi nhận mỗi lần học viên thực hiện làm bài Quiz.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `QuizAttemptId`| INT | NO | PK, IDENTITY(1,1) | | Mã lượt làm bài |
| `UserId` | INT | NO | FK -> `Users(UserId)` | | Học viên thực hiện bài thi |
| `QuizId` | INT | NO | FK -> `Quizzes(QuizId)` | | Đề thi đã làm |
| `Score` | INT | NO | | 0 | Tổng điểm đạt được |
| `StartedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm bắt đầu làm bài |
| `CompletedAt` | DATETIME2 | YES | | | Thời điểm nộp bài |

### 15. Bảng `QuizAttemptAnswers` (Chi tiết câu trả lời)
*Mô tả*: Chi tiết các đáp án học viên đã chọn trong từng lượt làm bài.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `QuizAttemptAnswerId`| INT | NO | PK, IDENTITY(1,1) | | Mã định danh bản ghi |
| `QuizAttemptId`| INT | NO | FK -> `QuizAttempts(QuizAttemptId)`| | Lượt làm bài tương ứng |
| `QuestionId` | INT | NO | FK -> `Questions(QuestionId)`| | Câu hỏi |
| `AnswerId` | INT | NO | FK -> `Answers(AnswerId)` | | Phương án học viên đã chọn |

---

## NHÓM V: THƯƠNG MẠI & THANH TOÁN (COMMERCE & ORDERS)

### 16. Bảng `Carts` (Giỏ hàng)
*Mô tả*: Giỏ hàng cá nhân của người dùng (Quan hệ 1-1 với `Users`).
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã giỏ hàng |
| `UserId` | INT | NO | UNIQUE, FK -> `Users(UserId)`| | Người dùng sở hữu giỏ hàng |
| `CreatedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm khởi tạo giỏ hàng |
| `UpdatedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm cập nhật cuối |

### 17. Bảng `CartItems` (Các mục trong giỏ)
*Mô tả*: Danh sách khóa học được chọn mua trong giỏ hàng.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã mục trong giỏ hàng |
| `CartId` | INT | NO | FK -> `Carts(Id)` | | Giỏ hàng trực thuộc |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học đã thêm vào giỏ |
| `AddedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm thêm vào giỏ |

### 18. Bảng `Coupons` (Mã giảm giá)
*Mô tả*: Quản lý chính sách mã khuyến mãi toàn sàn hoặc từng khóa học.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã định danh coupon |
| `Code` | NVARCHAR(50) | NO | UNIQUE | | Mã giảm giá duy nhất (ví dụ: `EDUMY20`) |
| `DiscountPercentage`| DECIMAL(5,2)| NO | | | Phần trăm giảm giá (0.01% - 100.00%) |
| `ExpiryDate` | DATETIME2 | NO | | | Ngày giờ hết hạn sử dụng |
| `IsActive` | BIT | NO | | 1 | Trạng thái (1: Đang hoạt động, 0: Đã tắt) |

### 19. Bảng `CourseCoupons` (Khóa học áp dụng Coupon)
*Mô tả*: Giới hạn mã giảm giá chỉ áp dụng cho một số khóa học nhất định.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `CourseId` | INT | NO | PK, FK -> `Courses(CourseId)` | | Mã khóa học |
| `CouponId` | INT | NO | PK, FK -> `Coupons(Id)` | | Mã coupon được áp dụng |

### 20. Bảng `Orders` (Đơn hàng)
*Mô tả*: Ghi nhận các giao dịch mua khóa học.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `OrderId` | INT | NO | PK, IDENTITY(1,1) | | Mã đơn hàng |
| `UserId` | INT | NO | FK -> `Users(UserId)` | | Học viên đặt mua |
| `TotalAmount` | DECIMAL(18,2) | NO | | | Tổng số tiền thanh toán thực tế |
| `Status` | NVARCHAR(50) | NO | | 'Pending' | Trạng thái đơn (`Pending`, `Completed`, `Cancelled`)|
| `CreatedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Ngày giờ khởi tạo đơn hàng |

### 21. Bảng `OrderItems` (Chi tiết đơn hàng)
*Mô tả*: Danh sách từng khóa học và giá chốt mua trong đơn hàng.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `OrderItemId` | INT | NO | PK, IDENTITY(1,1) | | Mã chi tiết đơn |
| `OrderId` | INT | NO | FK -> `Orders(OrderId)` | | Đơn hàng trực thuộc |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học đã mua |
| `Price` | DECIMAL(18,2) | NO | | | Giá bán tại thời điểm mua (Snapshot Price) |

---

## NHÓM VI: HỌC TẬP & TƯƠNG TÁC (LEARNING & INTERACTIONS)

### 22. Bảng `Enrollments` (Ghi danh học tập)
*Mô tả*: Quyền truy cập học tập của học viên đối với khóa học sau khi thanh toán.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `EnrollmentId` | INT | NO | PK, IDENTITY(1,1) | | Mã bản ghi ghi danh |
| `UserId` | INT | NO | FK -> `Users(UserId)` | | Học viên |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học được vào học |
| `EnrolledAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Ngày giờ ghi danh thành công |
| `ProgressPercentage`| INT | NO | | 0 | Tiến độ học tập hiện tại (0% - 100%) |
| `CompletedAt` | DATETIME2 | YES | | | Ngày giờ đạt 100% tiến độ |

### 23. Bảng `LessonProgresses` (Tiến độ từng bài học)
*Mô tả*: Đánh dấu những bài giảng video mà học viên đã học xong.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã bản ghi tiến độ bài học |
| `UserId` | INT | NO | FK -> `Users(UserId)` | | Học viên |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học |
| `LessonId` | INT | NO | FK -> `Lessons(LessonId)` | | Bài học đã hoàn thành |
| `CompletedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Ngày giờ đánh dấu hoàn thành bài |

### 24. Bảng `Certificates` (Chứng chỉ hoàn thành)
*Mô tả*: Chứng chỉ tự động cấp khi học viên đạt 100% tiến độ khóa học.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã chứng chỉ |
| `UserId` | INT | NO | FK -> `Users(UserId)` | | Học viên được cấp chứng chỉ |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học đã tốt nghiệp |
| `CertificateUrl`| NVARCHAR(255) | NO | | | Mã tra cứu chứng chỉ trực tuyến (UUID) |
| `IssuedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Ngày giờ cấp chứng chỉ |

### 25. Bảng `Reviews` (Đánh giá khóa học)
*Mô tả*: Đánh giá sao và nhận xét của học viên kèm kết quả phân tích cảm xúc từ AI.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `ReviewId` | INT | NO | PK, IDENTITY(1,1) | | Mã đánh giá |
| `UserId` | INT | NO | FK -> `Users(UserId)` | | Người gửi đánh giá |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học nhận đánh giá |
| `Rating` | INT | NO | CHECK (1-5) | | Số sao đánh giá (1 đến 5 sao) |
| `Comment` | NVARCHAR(MAX) | NO | | | Nội dung nhận xét bằng văn bản |
| `SentimentLabel`| NVARCHAR(50) | YES | | | Nhãn cảm xúc do AI gán (`Positive`, `Negative`)|
| `SentimentScore`| FLOAT | YES | | | Điểm số tin cậy của AI (0.0 - 1.0) |
| `CreatedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm gửi đánh giá |

### 26. Bảng `Wishlists` (Khóa học yêu thích)
*Mô tả*: Danh sách khóa học được người dùng đánh dấu quan tâm.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã bản ghi |
| `UserId` | INT | NO | FK -> `Users(UserId)` | | Người dùng |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học được yêu thích |
| `AddedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm thêm vào yêu thích |

---

## NHÓM VII: AI/ML & HÀNH VI (MACHINE LEARNING & BEHAVIOR)

### 27. Bảng `CourseMlAnalyses` (Lịch sử phân tích của AI)
*Mô tả*: Lưu vết các lần quét tự động của AI kiểm duyệt khóa học.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã bản ghi phân tích |
| `CourseId` | INT | NO | FK -> `Courses(CourseId)` | | Khóa học được phân tích |
| `PrimaryCategory`| NVARCHAR(100)| YES | | | Danh mục chính do AI đề xuất |
| `SubCategory` | NVARCHAR(100)| YES | | | Danh mục phụ do AI đề xuất |
| `SuggestedLevel`| NVARCHAR(50) | YES | | | Trình độ do AI gợi ý |
| `Confidence` | FLOAT | YES | | | Độ tự tin của mô hình AI (0.0 - 1.0) |
| `QualityScore` | INT | YES | | | Điểm chất lượng nội dung AI chấm (0 - 100) |
| `RiskLevel` | NVARCHAR(50) | YES | | | Mức độ rủi ro độc hại (`Low`, `Medium`, `High`)|
| `RawResponseJson`| NVARCHAR(MAX)| YES | | | JSON phản hồi thô từ FastAPI ML Service |
| `ModelVersion` | NVARCHAR(100)| YES | | | Tên và phiên bản mô hình AI sử dụng |
| `Status` | NVARCHAR(50) | NO | | 'Pending' | Trạng thái xét duyệt (`Pending`, `Approved`, `Overridden`)|
| `CreatedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm AI phân tích |
| `ApprovedAt` | DATETIME2 | YES | | | Thời điểm Admin xét duyệt |
| `ApprovedByUserId`| INT | YES | FK -> `Users(UserId)` | | Admin thực hiện duyệt hoặc ghi đè |

### 28. Bảng `CourseMlAnalysisTags` (Thẻ do AI đề xuất)
*Mô tả*: Danh sách thẻ từ khóa tự động trích xuất từ tiêu đề và mô tả khóa học.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã bản ghi |
| `CourseMlAnalysisId`| INT | NO | FK -> `CourseMlAnalyses(Id)`| | Lần phân tích tương ứng |
| `TagName` | NVARCHAR(100)| NO | | | Tên thẻ từ khóa do AI sinh ra |
| `Confidence` | FLOAT | YES | | | Độ tự tin đối với thẻ này |

### 29. Bảng `SearchHistories` (Lịch sử tìm kiếm)
*Mô tả*: Theo dõi hành vi tìm kiếm của người dùng phục vụ phân tích xu hướng và gợi ý khóa học.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã bản ghi tìm kiếm |
| `UserId` | INT | YES | FK -> `Users(UserId)` | | Người tìm kiếm (null nếu là khách vãng lai) |
| `SearchQuery` | NVARCHAR(255) | NO | | | Từ khóa tìm kiếm |
| `SearchedAt` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm tìm kiếm |

### 30. Bảng `UserActivities` (Nhật ký hành vi người dùng)
*Mô tả*: Nhật ký sự kiện tương tác của người dùng phục vụ mô hình Recommendation Engine.
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Khóa | Mặc định | Diễn giải nghiệp vụ |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `Id` | INT | NO | PK, IDENTITY(1,1) | | Mã bản ghi nhật ký |
| `UserId` | INT | YES | FK -> `Users(UserId)` | | Người dùng thực hiện hành vi |
| `ActivityType` | NVARCHAR(100)| NO | | | Loại hành vi (`VIEW_COURSE`, `START_LESSON`, `CLICK_RECOMMEND`)|
| `ResourceId` | NVARCHAR(100)| YES | | | Mã tài nguyên được tương tác |
| `Timestamp` | DATETIME2 | NO | | SYSUTCDATETIME() | Thời điểm ghi nhận sự kiện |
