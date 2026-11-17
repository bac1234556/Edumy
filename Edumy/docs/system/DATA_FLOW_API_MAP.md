# Data Flow & API Map — EduMy

**Updated**: 2026-11-17

---

## 1. System Architecture Diagram

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                    CLIENTS (STATELESS PRESENTATION)                     │
│  ┌─────────────────────────────────┐  ┌──────────────────────────────┐  │
│  │ Web SPA (React 19 + Vite 8)     │  │ Mobile App (Expo / REST)     │  │
│  │ Docker: :3000 | Dev: :5173      │  │ /api/v1/auth/mobile/*        │  │
│  │ HttpOnly Refresh Cookie + JWT   │  │ Bearer JWT + Opaque Refresh  │  │
│  └───────────────┬─────────────────┘  └──────────────┬───────────────┘  │
└──────────────────┼───────────────────────────────────┼──────────────────┘
                   │ HTTP REST JSON                    │
                   ▼                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│       BACKEND CORE — SPRING BOOT 3.2.0 + SPRING MODULITH 1.1.5          │
│                           http://localhost:5000                         │
│                                                                         │
│  SecurityConfig (Stateless JWT + CORS) ──► ApiResponseAdvice (Envelope) │
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐  │
│  │ 01. identity │  │ 02. catalog  │  │ 03. authoring│  │ 04. commerce│  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬──────┘  │
│         │                 │                 │                 │         │
│  ┌──────┴───────┐  ┌──────┴───────┐  ┌──────┴───────┐  ┌──────┴──────┐  │
│  │ 05. learning │  │ 06. payout   │  │ notification │  │   shared    │  │
│  └──────────────┘  └──────────────┘  └──────┬───────┘  └─────────────┘  │
└─────────┬───────────────────────────────────┼─────────────────┬─────────┘
          │ JDBC (MSSQL)                      │ @Async SMTP     │ /actuator/prometheus
          ▼                                   ▼                 ▼
┌─────────────────────┐             ┌─────────────────┐ ┌─────────────────┐
│  SQL Server 2022    │             │  Mailpit SMTP   │ │ Prometheus 9090 │
│  EduMyDb            │             │  SMTP :1025     │ │ Grafana :3001   │
│  Docker :14333      │             │  Web UI :8025   │ │ (Telegram Alert)│
└─────────────────────┘             └─────────────────┘ └─────────────────┘
```

### Quy tắc bọc Response (`ApiResponseAdvice.java`)
Mọi controller trả về mặc định được bọc vào envelope chuẩn:
```json
{
  "success": true,
  "message": "OK",
  "data": { ... },
  "errors": null
}
```
Các endpoint **giữ nguyên raw JSON/binary** (không bọc envelope):
- Payment webhooks (`/webhook/**`, `/vnpay-ipn`, `/ipn`)
- Mobile auth (`/api/v1/auth/mobile/**`, `/api/auth/mobile/**`)
- Payout endpoints (`/api/v1/me/earnings/**`, `/api/v1/me/payout-account`, `/api/v1/me/withdrawals/**`, `/api/v1/admin/withdrawals/**`)
- Static files (`/uploads/**`) và PDF binary (`byte[]` / `Resource`)

---

## 2. Complete API Endpoint Map

### 01. Identity & Auth (`AuthController`, `MobileAuthController`, `UsersController`, `AdminController`, `InstructorController`, `SpecUseCaseController`)
| Method | Route | Auth | Mô tả |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Đăng ký tài khoản + gửi email xác thực (Event `UserRegistered`) |
| POST | `/api/v1/auth/login` | Public | Đăng nhập Web (JWT + `HttpOnly` refresh cookie) |
| POST | `/api/v1/auth/refresh-token` | Cookie | Làm mới Access Token cho Web |
| POST | `/api/v1/auth/revoke-token` | Cookie | Thu hồi Refresh Token |
| POST | `/api/v1/auth/google-login` | Public | Đăng nhập bằng Google OAuth |
| POST | `/api/v1/auth/forgot-password` / `reset-password` | Public | Yêu cầu & đặt lại mật khẩu qua mã xác thực email |
| GET/POST | `/api/v1/auth/verify-email` & `resend-verification` | Public | Xác thực email tài khoản |
| POST | `/api/v1/auth/mobile/{register,login,refresh,logout}` | Public | Xác thực di động (Bearer + Opaque Refresh Token Rotation) |
| GET/PUT/PATCH | `/api/users/profile`, `/api/v1/me` | Authenticated | Xem & cập nhật hồ sơ cá nhân |
| POST | `/api/users/become-instructor` | Authenticated | Đăng ký nâng cấp vai trò Giảng viên |
| GET | `/api/instructor/stats`, `/courses/{id}/students` | Instructor, Admin | Thống kê giảng viên & danh sách học viên |
| GET/PUT/POST | `/api/v1/admin/{stats,activities,users,courses,orders,categories}` | Admin | Quản trị người dùng (lock/unlock/role), duyệt khóa học, đơn hàng, danh mục |

### 02. Catalog (`CategoriesController`, `TagsController`, `CourseRecommendationController`, `SpecUseCaseController`)
| Method | Route | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/courses`, `/api/v1/catalog/courses` | Public | Danh sách khóa học công khai (lọc danh mục, giá, rating, tìm kiếm + log `search_histories`) |
| GET | `/api/courses/instant-search?q=` | Public | Gợi ý tìm kiếm nhanh tối đa 6 khóa học |
| GET | `/api/v1/courses/{id}/similar` | Public | Gợi ý khóa học liên quan cùng danh mục |
| GET | `/api/v1/courses/{id}/bundle-recommendation` | Public | Gợi ý combo khóa học (giảm 15%) |
| GET | `/api/v1/courses/recommend` | Public | Top khóa học đánh giá cao cho trang chủ |
| GET/POST/PUT/DELETE | `/api/categories/**`, `/api/tags/**` | Public / Admin | Quản lý danh mục & thẻ |

### 03. Authoring (`CoursesController`, `CurriculumController`, `MediaController`, `UploadsController`, `SpecUseCaseController`)
| Method | Route | Auth | Mô tả |
|---|---|---|---|
| POST/PUT/DELETE | `/api/courses`, `/api/courses/{id}` | Instructor, Admin | Tạo, cập nhật, xóa khóa học |
| GET/POST/PUT/DELETE | `/api/courses/{id}/curriculum`, `/sections`, `/lessons` | Instructor, Admin | Quản lý chương, bài giảng & sắp xếp thứ tự |
| POST | `/api/media/upload`, `/api/uploads/image` | Authenticated | Upload video/tài liệu & hình ảnh khóa học |
| POST | `/api/v1/instructor/courses/{id}/submit`, `withdraw` | Instructor | Gửi duyệt / thu hồi yêu cầu duyệt khóa học |
| GET/POST/PUT/DELETE | `/api/v1/instructor/courses/{id}/co-instructors/**` | Instructor | Mời đồng giảng viên & phân chia tỷ lệ doanh thu |
| GET/POST | `/api/v1/instructor/invitations/**` | Instructor | Chấp nhận / từ chối lời mời đồng giảng viên |
| POST | `/api/v1/admin/courses/{id}/{approve,reject,hide,unhide}` | Admin | Phê duyệt, từ chối, ẩn hoặc mở lại khóa học |

### 04. Commerce & Payments (`CartController`, `CouponsController`, `OrdersController`, `PaymentController`)
| Method | Route | Auth | Mô tả |
|---|---|---|---|
| GET/POST/DELETE | `/api/v1/cart/**` | Authenticated | Quản lý giỏ hàng |
| POST/GET | `/api/coupons/validate`, `/api/coupons/**` | Public / Admin | Kiểm tra & gán mã giảm giá |
| POST/GET | `/api/v1/orders/{buy-now,checkout,my-orders,{id}/cancel}` | Authenticated | Đặt mua ngay, thanh toán giỏ hàng, lịch sử & hủy đơn |
| POST/GET | `/api/v1/payments/create-qr`, `/vietqr/{orderId}` | Authenticated | Sinh mã VietQR thanh toán chuyển khoản |
| GET | `/api/v1/orders/{orderId}/status` | Public | Polling trạng thái thanh toán đơn hàng |
| GET/POST | `/api/v1/payments/webhook/sepay` | Webhook Key | Tự động đối soát giao dịch SePay/Casso, mở khóa học & ghi nhận doanh thu |
| POST/GET | `/api/v1/payments/{create-url,vnpay-ipn,vnpay-return,webhook,callback}` | Auth / IPN | Thanh toán VNPay, Stripe & callback tương thích |

### 05. Learning, Quiz, Q&A & Certificates (`LessonsController`, `QuizzesController`, `QuizAttemptsController`, `ReviewsController`, `QuestionController`, `CertificatesController`)
| Method | Route | Auth | Mô tả |
|---|---|---|---|
| GET/POST/DELETE | `/api/v1/lessons/{id}/access`, `/courses/{cId}/lessons/{lId}/complete` | Public / Auth | Kiểm tra quyền xem (preview/enrolled) & đánh dấu hoàn thành bài học |
| GET/POST/DELETE | `/api/v1/quizzes/**`, `/api/v1/quiz-attempts/{quizId}/submit` | Auth / Instructor | Quản lý quiz & nộp bài chấm điểm tự động |
| GET/POST/PUT/DELETE | `/api/reviews/course/{courseId}`, `/api/reviews/{id}` | Public / Auth | Đánh giá khóa học |
| GET/POST/PUT/DELETE | `/api/v1/courses/{courseId}/questions`, `/questions/{id}/answers`, `/resolve` | Auth | Diễn đàn Q&A theo khóa học/bài giảng & đánh dấu đã giải quyết |
| GET/POST | `/api/v1/instructor/questions`, `/api/v1/admin/questions/**` | Instructor / Admin | Hàng đợi câu hỏi cho giảng viên & kiểm duyệt ẩn/khôi phục cho Admin |
| GET | `/api/certificates/{my,{url}/pdf,my/{courseId}/pdf,verify/{code}}` | Auth / Public | Cấp, tải PDF chứng chỉ OpenPDF (A4 ngang) & tra cứu xác thực |
| GET/POST/DELETE | `/api/wishlist/**` | Authenticated | Danh sách yêu thích |

### 06. Payout (`EarningsController`, `PayoutAccountController`, `WithdrawalController`, `AdminWithdrawalController`)
| Method | Route | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/v1/me/earnings/{summary,monthly,courses,entries}` | Instructor | Tổng quan, doanh thu theo tháng, theo khóa học & chi tiết sổ cái |
| GET/PUT | `/api/v1/me/payout-account` | Instructor | Xem & cập nhật tài khoản ngân hàng nhận tiền |
| GET/POST | `/api/v1/me/withdrawals`, `/{id}/cancel` | Instructor | Tạo yêu cầu rút tiền & hủy yêu cầu đang chờ |
| GET/POST | `/api/v1/admin/withdrawals`, `/{id}/{pay,reject}` | Admin | Duyệt chi trả hoặc từ chối yêu cầu rút tiền |

### 07. Observability
| Method | Route | Auth | Mô tả |
|---|---|---|---|
| GET | `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus` | Public | Kiểm tra sức khỏe ứng dụng & thu thập metrics Prometheus |
