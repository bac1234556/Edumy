# EduMy Frontend (React 19 + Vite 8)

Ứng dụng Web SPA của nền tảng học trực tuyến **EduMy**, xây dựng bằng **React 19.2**, **Vite 8.1**, **React Router 7**, **Bootstrap 5**, **Framer Motion** và **Axios**.

## Công nghệ & Thư viện chính
- **Core**: `react` 19.2.7, `react-router-dom` 7.18.1, `vite` 8.1.1
- **UI & Styling**: `bootstrap` 5.3.8, `framer-motion` 12.42.2, `lucide-react`, `react-icons`, `canvas-confetti`, Design System tokens (`src/design-system.css`)
- **HTTP & Auth**: `axios` 1.18.1, `jwt-decode` 4.0.0, `@react-oauth/google` 0.13.5
- **Linter**: `oxlint` 1.71.0

## Kiến trúc kết nối API (`src/api/axiosConfig.js`)
- Base URL mặc định: `import.meta.env.VITE_API_URL || 'http://localhost:5000/api'`
- Tự động gắn `Authorization: Bearer <token>` từ `localStorage`.
- Tự động giải nén envelope chuẩn `ApiResponse<T>` (`{ success, message, data, errors }`) qua `unwrapApiResponse()`.
- Hỗ trợ cơ chế tự động làm mới token (`POST /auth/refresh-token` với `withCredentials: true`) khi gặp HTTP `401`.

## Các phân hệ màn hình chính (`src/App.jsx`)
- **Công khai (Guest)**: Trang chủ (`/`), Danh sách & tìm kiếm khóa học (`/courses`), Chi tiết khóa học (`/courses/:id`), Đăng nhập/Đăng ký/Xác thực Email/Quên & Đặt lại mật khẩu
- **Học viên (Student)**: Giỏ hàng (`/cart`), Thanh toán VietQR/VNPay (`/payment`, `/payment/qr`, `/payment-success`, `/payment-cancel`), Lịch sử đơn hàng (`/orders`), Khóa học của tôi (`/my-courses`), Trình học video + Quiz + Hỏi đáp Q&A (`/my-courses/:id/learn`), Hồ sơ cá nhân (`/profile`)
- **Giảng viên (Instructor)**: Bảng điều khiển giảng viên & doanh thu/rút tiền (`/instructor`), Tạo khóa học (`/instructor/courses/new`), Trình biên tập giáo trình (`/instructor/courses/:id/curriculum`)
- **Quản trị (Admin)**: Bảng điều khiển quản trị duyệt khóa học, người dùng, danh mục, đơn hàng, kiểm duyệt Q&A & yêu cầu rút tiền (`/admin`)

## Chạy và Build
```bash
# Cài đặt dependencies
npm install

# Chạy môi trường dev (http://localhost:5173)
npm run dev

# Kiểm tra lint với Oxlint
npm run lint

# Build production (xuất ra dist/)
npm run build
```

Trong Docker (`Dockerfile` + `nginx.conf`), ứng dụng được build đa tầng bằng `node:20-alpine` và phục vụ SPA qua `nginx:alpine` trên cổng `80` (ánh xạ `3000:80` trong `docker-compose.yml`).
