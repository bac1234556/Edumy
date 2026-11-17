# EduMy eLearning Platform Walkthrough

Tài liệu hướng dẫn tổng quan kiến trúc, nghiệp vụ và cách vận hành nền tảng **EduMy** sau khi nâng cấp lên kiến trúc **Spring Modulith 1.1.5 (Java 17 + Spring Boot 3.2.0)** kết hợp **React 19 + Vite 8**, **SQL Server 2022**, **Mailpit SMTP** và **Prometheus + Grafana**.

---

## 1. Kiến Trúc Tổng Thể (Single Source of Truth)

```text
  [ React 19 + Vite 8 Web SPA ]      [ Mobile App (Expo / REST) ]
       (Docker :3000 / Dev :5173)         (/api/v1/auth/mobile/*)
                   │                                 │
                   └────────────────┬────────────────┘
                                    ▼  HTTP REST JSON
           ┌─────────────────────────────────────────────────┐
           │     Spring Boot 3.2 + Spring Modulith Core      │
           │                   (Port 5000)                   │
           │  identity | catalog | authoring | commerce      │
           │  learning | payout  | notification | shared     │
           └──────┬─────────────────┬─────────────────┬──────┘
                  │ JDBC            │ Async SMTP      │ /actuator/prometheus
                  ▼                 ▼                 ▼
          [SQL Server 2022]    [Mailpit]     [Prometheus + Grafana]
          (Docker :14333)    (8025 / 1025)      (9090 / 3001)
```

---

## 2. Các Luồng Nghiệp Vụ Cốt Lõi

### A. Xác Thực Đa Nền Tảng (`identity` + `notification`)
1. **Web SPA**:
   - Đăng ký (`POST /api/v1/auth/register`) phát Domain Event `UserRegistered`.
   - `IdentityMailListener` (`@Async` + `@TransactionalEventListener` sau khi transaction commit) gửi email xác thực HTML tiếng Việt qua **Mailpit** (`http://localhost:8025`).
   - Đăng nhập cấp JWT Access Token ngắn hạn + Refresh Token lưu trong `HttpOnly` Cookie (`/api/v1/auth/refresh-token`). Hỗ trợ đăng nhập Google OAuth & khôi phục mật khẩu qua email.
2. **Mobile App**:
   - Sử dụng `/api/v1/auth/mobile/{register,login,refresh,logout}` trả về Access Token + Opaque Refresh Token kèm cơ chế **Refresh Token Rotation** & phát hiện dùng lại token (Token Family revocation).

### B. Quản Lý Khóa Học, Đồng Giảng Viên & Kiểm Duyệt (`authoring` + `catalog`)
1. Giảng viên tạo khóa học (`Draft`), xây dựng chương/bài học, upload video/ảnh (`/api/media/upload`, `/api/uploads/image`).
2. Có thể mời **Đồng giảng viên (Co-Instructor)** và cấu hình tỷ lệ chia sẻ doanh thu (`shareBps`).
3. Quy trình kiểm duyệt: Giảng viên `submit` (`PendingApproval`) → Admin `approve` (`Published`), `reject` (kèm review note + email thông báo) hoặc `hide`/`unhide`.
4. Học viên/Khách tìm kiếm khóa học công khai qua `instant-search`, bộ lọc danh mục/giá/đánh giá, xem gợi ý khóa học tương đồng (`/similar`) và combo ưu đãi 15% (`/bundle-recommendation`).

### C. Thương Mại, Thanh Toán & Sổ Cái Doanh Thu (`commerce` + `payout`)
1. Học viên thêm khóa học vào giỏ hàng (`Cart`), áp dụng mã giảm giá (`Coupon`) hoặc bấm Mua ngay (`buy-now`).
2. Hỗ trợ các kênh thanh toán:
   - **VietQR + SePay Webhook**: Sinh mã QR chuyển khoản động (`/api/v1/payments/create-qr`). Khi nhận webhook từ SePay (`/api/v1/payments/webhook/sepay`), hệ thống tự động đối soát, chuyển đơn sang `PAID`, tạo `Enrollment`, gửi email xác nhận mua hàng.
   - **VNPay / Stripe**: Tạo URL thanh toán và xử lý IPN/Return callback.
3. **Hạch toán doanh thu giảng viên (`payout`)**:
   - Ngay khi đơn hàng `PAID`, `commerce` gọi `EarningApi.recordForPaidOrder(...)` tạo các dòng sổ cái `EarningEntry` (`payout_earnings`) bất biến.
   - Khấu trừ phí nền tảng (mặc định 30% = `3000 bps`), chia phần cho Co-Instructor theo `shareBps`, phần còn lại cho giảng viên chính.
   - Giảng viên khai báo tài khoản ngân hàng (`/api/v1/me/payout-account`), gửi yêu cầu rút tiền (`/api/v1/me/withdrawals`) khi đạt số dư khả dụng tối thiểu (`100.000đ`), và Admin duyệt chi/từ chối (`/api/v1/admin/withdrawals/{id}/{pay,reject}`).

### D. Học Tập, Hỏi Đáp Q&A & Chứng Chỉ Số (`learning`)
1. Học viên xem bài giảng (kiểm tra quyền truy cập hoặc preview công khai), đánh dấu hoàn thành bài học để cập nhật `%` tiến độ.
2. Làm bài kiểm tra trắc nghiệm (`Quiz`, `QuizAttempt`) và đánh giá khóa học (`Review`).
3. **Diễn đàn Hỏi đáp Q&A**: Học viên đặt câu hỏi theo khóa học/bài học (`CourseQuestion`), giảng viên trả lời (`QuestionAnswer`, phát email thông báo `question-answered.html`), đánh dấu `Resolved`, Admin kiểm duyệt ẩn/khôi phục nội dung vi phạm.
4. **Chứng chỉ PDF OpenPDF**: Khi hoàn thành khóa học, học viên tải trực tiếp file PDF A4 ngang hỗ trợ tiếng Việt đầy đủ (`NotoSans`) tại `/api/certificates/my/{courseId}/pdf` và mọi bên thứ ba có thể xác thực qua `/api/certificates/verify/{code}`.

---

## 3. Hướng Dẫn Chạy Hệ Thống

### Cách 1: Docker Compose (Khuyên dùng - 6 Containers)
Tại thư mục `Edumy/`:
```powershell
.\scripts\docker_start.ps1
```
Hoặc tại `Edumy/Edumy/`:
```bash
docker compose up -d --build
```
Các dịch vụ sau khi khởi chạy:
- **Frontend UI (Nginx)**: `http://localhost:3000`
- **Backend API**: `http://localhost:5000` (`http://localhost:5000/actuator/health`)
- **Mailpit Web UI**: `http://localhost:8025`
- **SQL Server 2022**: `localhost:14333` (`sa` / `YourPassword123`, DB `EduMyDb`)
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3001` (`admin` / `admin`)

### Cách 2: Chạy Thủ Công (Local Dev)
1. Đảm bảo SQL Server chạy tại `127.0.0.1:1433` với database `EduMyDb`.
2. Backend:
   ```bash
   cd Edumy/Edumy/Backend
   .\mvnw.cmd spring-boot:run
   ```
3. Frontend:
   ```bash
   cd Edumy/Edumy/Frontend
   npm install
   npm run dev
   ```
   Truy cập `http://localhost:5173`.

---

## 4. Tài Khoản Seed Mặc Định (Development)
- **Admin**: `admin@edumy.com` / `Admin@123`
- **Instructor**: `instructor@edumy.com` / `Admin@123`
- **Student**: `student@edumy.com` / `Admin@123`
