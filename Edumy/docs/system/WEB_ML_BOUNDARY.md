# Architecture Boundary — Clients, Spring Modulith & Historical ML Note

**Updated**: 2026-11-17

---

## 1. Current System Boundary (Single Source of Truth)

Hiện tại **EduMy** vận hành theo mô hình **Modular Monolith** (không còn tách riêng Python FastAPI `MLService`):

### A. Presentation Layer (Web SPA & Mobile Client)
- **Web SPA (`Frontend/`)**: React 19.2, Vite 8.1, React Router 7, Axios (`axiosConfig.js` xử lý `ApiResponse` envelope và refresh cookie), phục vụ trên Nginx port `3000:80` (hoặc `5173` ở chế độ dev).
- **Mobile Client**: Kết nối qua `/api/v1/auth/mobile/*` (Bearer JWT + Opaque Refresh Token Rotation) và các endpoint `/api/v1/*`.

### B. Backend Core (`Backend/` — Spring Boot 3.2.0 + Spring Modulith 1.1.5)
Toàn bộ logic nghiệp vụ và tích hợp được đóng gói trong 8 package dưới `com.edumy`:
- `identity` (`shared`)
- `catalog` (`shared`, `authoring :: api`, `learning :: api`)
- `authoring` (`shared`)
- `commerce` (`shared`, `authoring :: api`, `learning :: api`, `payout :: api`)
- `learning` (`shared`, `authoring :: api`)
- `payout` (`shared`, `identity :: api`, `authoring :: api`)
- `notification` (`shared`, `identity :: api`, `authoring :: api`, `commerce :: api`, `learning :: api`)
- `shared` (Kernel dùng chung)

### C. Infrastructure & External Services
- **SQL Server 2022 (`Database/`)**: Lưu trữ 33 bảng nghiệp vụ trong `EduMyDb`.
- **Mailpit (`mailpit`)**: Nhận SMTP email giao dịch từ module `notification` (`1025` SMTP / `8025` Web UI).
- **Prometheus & Grafana (`monitoring/`)**: Thu thập metrics từ `/actuator/prometheus` và cảnh báo qua Telegram.

---

## 2. Historical ML Service Note

Các tài liệu trong thư mục `docs/ml/` mô tả thiết kế nghiên cứu Machine Learning (Sentiment Analysis, Course Classification, OULAD Recommendation) từ giai đoạn kiến trúc trước đây khi hệ thống còn sử dụng dịch vụ Python FastAPI riêng biệt (`MLService`). Hiện tại các tính năng gợi ý khóa học (`/api/v1/courses/recommend`, `/similar`, `/bundle-recommendation`) và thống kê đánh giá (`/api/admin/ml-monitoring`) được xử lý trực tiếp bằng truy vấn dữ liệu trong Backend Spring Modulith.
