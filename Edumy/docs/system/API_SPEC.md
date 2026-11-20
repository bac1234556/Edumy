# API Specification — EduMy Core Backend

**Updated**: 2026-11-17
**Base URL**: `http://localhost:5000` (`/api`, `/api/v1`)

---

## 1. Response Envelope Contract (`ApiResponseAdvice`)

Các endpoint Web chuẩn của Spring Modulith được `ApiResponseAdvice` tự động chuẩn hóa theo cấu trúc `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "OK",
  "data": {},
  "errors": null
}
```

Các nhóm endpoint trả về **raw JSON hoặc binary** (bỏ qua envelope):
- Mobile Auth: `/api/v1/auth/mobile/**`, `/api/auth/mobile/**`
- Payout: `/api/v1/me/earnings/**`, `/api/v1/me/payout-account`, `/api/v1/me/withdrawals/**`, `/api/v1/admin/withdrawals/**`
- Webhooks thanh toán: `/api/v1/payments/webhook/**`, `/api/v1/payments/vnpay-ipn`
- Tệp tĩnh & PDF chứng chỉ (`byte[]`, `Resource`)

---

## 2. Authentication Contracts

### A. Web Auth (`/api/v1/auth/login`)
- **Request**: `{ "email": "student@edumy.com", "password": "..." }`
- **Response (`data`)**: JWT Access Token + thông tin `user`, đồng thời set `refreshToken` trong `HttpOnly` cookie.

### B. Mobile Auth (`/api/v1/auth/mobile/{register,login,refresh,logout}`)
- **Response (`MobileTokensDto`, raw JSON)**:
```json
{
  "accessToken": "<jwt>",
  "expiresAt": "2026-...",
  "refreshToken": "<opaque-token>",
  "refreshExpiresAt": "2026-..."
}
```
- Hỗ trợ Refresh Token Rotation & phát hiện dùng lại token (reuse detection).

---

## 3. Core Business Contracts

### A. Catalog & Recommendations (`/api/v1/courses/**`)
- `GET /api/courses` — Phân trang, tìm kiếm (`q`/`search`), lọc `categoryId`, `minPrice`, `maxPrice`, `minRating`, `level`.
- `GET /api/courses/instant-search?q=` — Gợi ý tìm kiếm nhanh tối đa 6 khóa học (tối thiểu 2 ký tự).
- `GET /api/v1/courses/{id}/similar?limit=4` — Gợi ý khóa học liên quan cùng danh mục.
- `GET /api/v1/courses/{id}/bundle-recommendation` — Combo 2 khóa học giảm 15%.
- `GET /api/v1/courses/recommend` — Top 4 khóa học được đánh giá cao.

### B. Payments & Webhooks (`/api/v1/payments/**`)
- `POST /api/v1/payments/create-qr` / `GET /api/v1/payments/vietqr/{orderId}` — Sinh mã VietQR động.
- `GET /api/v1/orders/{orderId}/status` — Kiểm tra trạng thái đơn hàng thời gian thực.
- `POST /api/v1/payments/webhook/sepay` — Nhận callback đối soát chuyển khoản (header `Authorization` hoặc `X-Api-Key`), tự động tạo `Enrollment`, gọi `EarningApi.recordForPaidOrder` và gửi email xác nhận.
- `POST /api/v1/payments/create-url`, `GET /api/v1/payments/vnpay-ipn`, `GET /api/v1/payments/vnpay-return` — Cổng thanh toán VNPay.

### C. Q&A Forum & Certificates (`/api/v1/courses/{courseId}/questions`, `/api/certificates/**`)
- `GET /api/v1/courses/{courseId}/questions?lessonId=&mineOnly=&unansweredOnly=&q=` — Danh sách câu hỏi phân trang.
- `POST /api/v1/courses/{courseId}/questions` — Tạo câu hỏi mới.
- `POST /api/v1/questions/{questionId}/answers` — Trả lời câu hỏi.
- `GET /api/certificates/my/{courseId}/pdf` — Tải PDF chứng chỉ A4 ngang (OpenPDF + `NotoSans`).
- `GET /api/certificates/verify/{code}` — Xác thực công khai mã chứng chỉ.

### D. Instructor Payout (`/api/v1/me/**`, `/api/v1/admin/withdrawals/**`)
- `GET /api/v1/me/earnings/{summary,monthly,courses,entries}` — Xem thống kê và sổ cái thu nhập.
- `GET/PUT /api/v1/me/payout-account` — Quản lý tài khoản ngân hàng nhận tiền.
- `GET/POST /api/v1/me/withdrawals` & `POST /api/v1/me/withdrawals/{id}/cancel` — Tạo/hủy yêu cầu rút tiền (tối thiểu 1.000.000 đ mặc định, tối đa 1 lần/tháng dương lịch).
- `POST /api/v1/admin/withdrawals/{id}/{pay,reject}` — Admin duyệt chi trả hoặc từ chối.

---

## 4. Health & Observability

- `GET /actuator/health` — Trạng thái ứng dụng và kết nối Database (`UP` / `DOWN`).
- `GET /actuator/prometheus` — Metrics định dạng Prometheus cho Grafana.
