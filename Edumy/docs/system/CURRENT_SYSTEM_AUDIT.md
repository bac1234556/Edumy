# Current System Audit — EduMy

**Updated**: 2026-11-17
**Architecture**: Modular Monolith (Spring Boot 3.2.0 + Spring Modulith 1.1.5) + React 19 SPA + SQL Server 2022 + Mailpit + Prometheus/Grafana

---

## 1. Executive Summary

### What Exists Today
EduMy là nền tảng đào tạo trực tuyến hoàn chỉnh phục vụ đồng thời **Web SPA (React 19.2 + Vite 8.1)** và **Mobile Client (REST Bearer + Opaque Refresh Token Rotation)** trên một lõi backend thống nhất (**Java 17 + Spring Boot 3.2.0 + Spring Modulith 1.1.5**), kết nối **Microsoft SQL Server 2022**, **Mailpit SMTP** cho email giao dịch bất đồng bộ và **Prometheus + Grafana** cho giám sát vận hành.

> **Thay đổi kiến trúc quan trọng so với phiên bản cũ**: Dự án đã loại bỏ hoàn toàn backend `.NET 10` và dịch vụ Python `MLService` (FastAPI) bên ngoài. Toàn bộ nghiệp vụ được hợp nhất vào **Spring Modulith** gồm 8 package (`identity`, `catalog`, `authoring`, `commerce`, `learning`, `payout`, `notification`, `shared`) nhằm đảm bảo *Single Source of Truth*, tính toàn vẹn giao dịch ACID và triển khai gọn nhẹ bằng Docker Compose (6 containers).

### Component Summary

| Thành phần | Đường dẫn | Công nghệ | Vai trò | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **Backend Core** | `Backend/` | Java 17, Spring Boot 3.2.0, Spring Modulith 1.1.5, JPA/Hibernate, JWT, OpenPDF, Stripe, Actuator | REST API & Domain Modules | ACTIVE |
| **Frontend Web** | `Frontend/` | React 19.2.7, Vite 8.1.1, React Router 7, Bootstrap 5, Framer Motion, Axios, Oxlint | Web SPA (Port 3000 Docker / 5173 Dev) | ACTIVE |
| **Database** | `Database/` | Microsoft SQL Server 2022 (`EduMyDb.bak` + SQL DDL/Seed scripts) | Data Persistence (33 JPA tables) | ACTIVE |
| **Email Testing** | `docker-compose.yml` | Mailpit `v1.32.0` (SMTP 1025 / Web UI 8025) | Async Transactional Email Preview | ACTIVE |
| **Observability** | `monitoring/` | Prometheus `v2.51.0` + Grafana `10.4.1` + Telegram Alerting | Metrics & Alerting | ACTIVE |
| **Unit Tests** | `Backend/src/test/` | JUnit 5, Spring Modulith Test, Mockito | `ModularityTest`, Auth, MobileAuth, Payout tests | ACTIVE |

---

## 2. Backend Modular Architecture (`com.edumy.*`)

Kiến trúc được kiểm soát ranh giới qua `@ApplicationModule` và `@NamedInterface("api")` trong `package-info.java`:

1. **`identity` (`01 Identity`)** — Phụ thuộc: `shared`
   - Web Auth (`AuthController`: `/api/auth/*`, `/api/v1/auth/*` với `HttpOnly` Refresh Cookie, Google OAuth, Verify Email, Forgot/Reset Password).
   - Mobile Auth (`MobileAuthController`: `/api/v1/auth/mobile/*` với Token Family & Refresh Token Rotation).
   - Quản trị User/Role (`UsersController`, `InstructorController`, `AdminController`), log hành vi (`UserActivity`, `SearchHistory`).
2. **`catalog` (`02 Catalog`)** — Phụ thuộc: `shared`, `authoring :: api`, `learning :: api`
   - Tìm kiếm tức thì (`instant-search`), duyệt `Categories` & `Tags`, gợi ý khóa học tương đồng (`/similar`), gợi ý combo giảm 15% (`/bundle-recommendation`) và khóa học nổi bật (`/recommend`).
3. **`authoring` (`03 Authoring`)** — Phụ thuộc: `shared`
   - CRUD khóa học, chương (`CourseSection`), bài giảng (`Lesson`), trình biên tập giáo trình (`CurriculumController`), upload media/ảnh (`MediaController`, `UploadsController`).
   - Điều phối 59 Use Case đặc tả trong `SpecUseCaseController`: gửi duyệt/thu hồi/phê duyệt/từ chối/ẩn khóa học, quản lý đồng giảng viên (`Co-Instructor`) & lời mời chia sẻ doanh thu.
4. **`commerce` (`04 Commerce`)** — Phụ thuộc: `shared`, `authoring :: api`, `learning :: api`, `payout :: api`
   - Giỏ hàng (`CartController`), mã giảm giá (`CouponsController`), đơn hàng (`OrdersController`: `buy-now`, `checkout`, `my-orders`).
   - Thanh toán đa kênh (`PaymentController`): **VietQR động**, **SePay / Casso Webhook** tự động kích hoạt ghi danh, **VNPay** (`create-url`, `vnpay-ipn`, `vnpay-return`), **Stripe** và polling trạng thái đơn hàng. Gọi `EarningApi.recordForPaidOrder(...)` ghi nhận doanh thu khi đơn hàng thanh toán thành công.
5. **`learning` (`05 Learning`)** — Phụ thuộc: `shared`, `authoring :: api`
   - Ghi danh (`Enrollment`), kiểm soát truy cập/xem thử bài học (`LessonAccessController`, `LessonsController`), tiến độ học (`LessonProgress`), Wishlist.
   - Đánh giá trắc nghiệm (`QuizzesController`, `QuizAttemptsController`), đánh giá khóa học (`ReviewsController`).
   - Diễn đàn hỏi đáp **Q&A** (`QuestionController`, `InstructorQuestionController`, `QaService`) hỗ trợ lọc bài giảng/chưa trả lời, đánh dấu đã giải quyết, kiểm duyệt ẩn/hiện cho Admin.
   - Cấp và xác thực chứng chỉ PDF tiếng Việt A4 ngang qua **OpenPDF 1.3.43** (`OpenPdfCertificateRenderer` + font `NotoSans`).
6. **`payout` (`06 Payout`)** — Phụ thuộc: `shared`, `identity :: api`, `authoring :: api`
   - Sổ cái thu nhập bất biến (`EarningEntry` -> bảng `payout_earnings`): khấu trừ phí nền tảng (`platform-fee-bps`, mặc định `3000` = 30%), chia sẻ cho đồng giảng viên theo `shareBps`, phần còn lại cho giảng viên chính, áp dụng kỳ hạn khả dụng (`maturity`).
   - Quản lý tài khoản ngân hàng (`PayoutAccountController`), yêu cầu/hủy rút tiền (`WithdrawalController`), Admin duyệt chi/từ chối (`AdminWithdrawalController`).
7. **`notification` (`Notification`)** — Phụ thuộc: `shared`, `identity :: api`, `authoring :: api`, `commerce :: api`, `learning :: api`
   - Lắng nghe Domain Events (`@Async` + `@TransactionalEventListener(AFTER_COMMIT)`) và gửi 10 mẫu email HTML qua `SmtpMailGateway` / Mailpit.
8. **`shared` (`Shared`)** — Shared Kernel
   - `SecurityConfig`, `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityUtils`, `ApiResponseAdvice`, `GlobalExceptionHandler`, `DataSeeder`, `VietnameseNormalizer`, `SlugGenerator`, `CoursePriceValidator`.

---

## 3. Frontend Audit (`Frontend/`)

- **Stack**: React `19.2.7`, Vite `8.1.1`, React Router `7.18.1`, Axios `1.18.1`, Bootstrap `5.3.8`, Framer Motion `12.42.2`, `lucide-react`, `react-icons`, `canvas-confetti`, `@react-oauth/google`, `oxlint`.
- **API Integration**: `axiosConfig.js` mặc định trỏ `http://localhost:5000/api` (hoặc `VITE_API_URL`), tự động gắn Bearer JWT, giải nén envelope `ApiResponse<T>` qua `unwrapApiResponse()`, và tự động refresh token khi gặp `401`.
- **Pages**: 25+ trang phủ đầy đủ luồng Guest, Student (`Cart`, `PaymentQR`, `OrderHistory`, `MyLearning`, `CoursePlayer` kèm `CourseQaTab` & `QuizTaker`, `CertificateView`, `Wishlist`, `UserProfile`), Instructor (`InstructorDashboard`, `CourseCreate`, `CourseCurriculumEditor`), và Admin (`AdminDashboard` với `AdminCategoriesTab`, `AdminOrdersTab`).

---

## 4. Database Audit (`EduMyDb`)

Hệ thống sử dụng **33 bảng JPA** trên SQL Server 2022 (`Database/EduMyDb.bak` & DDL `06_Script_Tao_Database_EduMyDb.sql` + Hibernate `ddl-auto: update`):
- **Identity**: `Users`, `Roles`, `UserRoles`, `RefreshTokens`, `UserActivities`, `search_histories`
- **Authoring & Catalog**: `Categories`, `Courses`, `CourseSections`, `Lessons`, `Tags`, `CourseTags`
- **Commerce**: `Carts`, `CartItems`, `Orders`, `OrderItems`, `Coupons`, `CourseCoupons`
- **Learning & Q&A**: `Enrollments`, `LessonProgresses`, `Certificates`, `Wishlists`, `Reviews`, `Quizzes`, `Questions`, `Answers`, `QuizAttempts`, `QuizAttemptAnswers`, `course_questions`, `question_answers`
- **Payout**: `payout_earnings`, `payout_accounts`, `payout_withdrawals`

---

## 5. Observability & CI/CD

- **Spring Boot Actuator & Micrometer**: Expose `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`.
- **Prometheus (`9090`)**: Scrape `backend:5000/actuator/prometheus` mỗi `5s`.
- **Grafana (`3001`)**: Provision sẵn datasource Prometheus, dashboard `edumy-overview.json`, và 3 alert rules (`Backend Down`, `CPU > 85%`, `HTTP 5xx`).
- **Jenkinsfile**: Tự động hóa build & test song song Backend + Frontend, đóng gói Docker Compose và triển khai.
