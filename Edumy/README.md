# EduMy System (Core Application & Infrastructure)

Thư mục lõi chứa toàn bộ mã nguồn ứng dụng, cơ sở dữ liệu, cấu hình Docker Compose, hệ thống giám sát và tài liệu kỹ thuật của nền tảng **EduMy E-Learning**.

## Yêu cầu môi trường
- **Docker & Docker Compose** (khuyên dùng để chạy trọn bộ 6 dịch vụ)
- Hoặc chạy thủ công cục bộ:
  - **Java 17+ SDK** (Eclipse Temurin / OpenJDK 17)
  - **Node.js 20+** & **npm** (cho Frontend React 19 + Vite 8)
  - **Microsoft SQL Server 2019 / 2022**

---

## Kiến trúc & Cấu trúc thư mục

Backend được thiết kế theo mô hình **Modular Monolith** sử dụng **Spring Boot 3.2.0** và **Spring Modulith 1.1.5** (`com.edumy.*`) với ranh giới module rõ ràng (kiểm chứng bằng `ModularityTest`):

- `Backend/`: Spring Boot 3.2.0 Web API (Java 17, Spring Modulith 1.1.5, Spring Data JPA, Spring Security + JWT, MSSQL, OpenPDF 1.3.43, Stripe, VNPay, VietQR/SePay, Actuator + Prometheus):
  - `identity` — Xác thực Web (Cookie Refresh) & Mobile (`/api/v1/auth/mobile`), Google OAuth, hồ sơ người dùng, quản trị Admin/Instructor, `UserActivity`, `SearchHistory`
  - `catalog` — Danh mục, thẻ, tìm kiếm tức thì (`instant-search`), gợi ý khóa học tương đồng & combo
  - `authoring` — Quản lý khóa học, chương, bài giảng, media upload, đồng giảng viên (`Co-Instructor`), quy trình duyệt khóa học
  - `commerce` — Giỏ hàng, mã giảm giá, đơn hàng, thanh toán VietQR + SePay Webhook, VNPay IPN, Stripe
  - `learning` — Ghi danh, tiến độ bài học, Quiz & QuizAttempt, Review, Q&A Forum, Chứng chỉ PDF tiếng Việt (OpenPDF)
  - `payout` — Sổ cái doanh thu giảng viên (`EarningEntry`), chia sẻ đồng giảng viên, tài khoản ngân hàng (`PayoutAccount`), rút tiền (`WithdrawalRequest`)
  - `notification` — Gửi email HTML bất đồng bộ (`@Async` + `@TransactionalEventListener`) qua SMTP/Mailpit
  - `shared` — Security, JWT, `ApiResponseAdvice`, `GlobalExceptionHandler`, `DataSeeder`, chuẩn hóa tiếng Việt & slug
- `Frontend/`: React 19.2 + Vite 8.1 + Bootstrap 5 + React Router 7 + Axios (Nginx serving trong Docker).
- `Database/`: SQL Server 2022 Dockerfile, `entrypoint.sh` tự động restore `EduMyDb.bak` và thư mục `scripts/` (`06_Script_Tao_Database_EduMyDb.sql`, `07_Script_Seed_Data_EduMyDb.sql`).
- `monitoring/`: Cấu hình **Prometheus** (`prometheus.yml`) và **Grafana** (datasources, dashboard `edumy-overview.json`, cảnh báo `rules.yaml` hỗ trợ Telegram).
- `docs/`: Tài liệu kiến trúc hệ thống (`system/`), quy chuẩn giao diện (`design-system/`), và lưu trữ tài liệu ML trước đây (`ml/`).

---

## Hướng dẫn chạy dự án với Docker (Khuyên dùng)

File `docker-compose.yml` khởi chạy đồng thời **6 containers**: `database`, `backend`, `mailpit`, `frontend`, `prometheus`, `grafana`.

1. *(Tùy chọn)* Tạo file `.env` từ `.env.example` nếu muốn nhận cảnh báo Grafana qua Telegram:
   ```bash
   cp .env.example .env
   ```
2. Khởi chạy toàn bộ stack:
   ```bash
   docker compose up -d --build
   ```
3. Truy cập các dịch vụ:
   - **Frontend (Web App)**: `http://localhost:3000`
   - **Backend REST API**: `http://localhost:5000` (Health: `http://localhost:5000/actuator/health`)
   - **SQL Server 2022**: `localhost:14333` (`sa` / `YourPassword123`, DB: `EduMyDb`)
   - **Mailpit Web UI**: `http://localhost:8025` (SMTP port `1025`)
   - **Prometheus**: `http://localhost:9090`
   - **Grafana**: `http://localhost:3001` (tài khoản mặc định: `admin` / `admin`)

> Lưu ý: Để chạy Docker Backend từ JAR mới nhất, hãy build trước `cd Backend && .\mvnw.cmd clean package -DskipTests` (hoặc chạy qua `Jenkinsfile`).

---

## Hướng dẫn chạy dự án thủ công (Local Dev)

### 1. Cấu hình SQL Server
1. Đảm bảo SQL Server đang chạy trên port `1433` và có database `EduMyDb` (có thể chạy `Database/scripts/06_Script_Tao_Database_EduMyDb.sql` và `07_Script_Seed_Data_EduMyDb.sql`, hoặc tạo database trống `EduMyDb` để Hibernate + `DataSeeder` tự khởi tạo).
2. Kiểm tra cấu hình mặc định trong `Backend/src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: ${SPRING_DATASOURCE_URL:jdbc:sqlserver://127.0.0.1:1433;databaseName=EduMyDb;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true;characterEncoding=UTF-8;useUnicode=true}
       username: ${SPRING_DATASOURCE_USERNAME:sa}
       password: ${SPRING_DATASOURCE_PASSWORD:YourPassword123}
   ```

### 2. Chạy Backend (Spring Boot 3.2)
```bash
cd Backend
# Windows:
.\mvnw.cmd spring-boot:run
# Linux / macOS:
./mvnw spring-boot:run
```
- Backend API lắng nghe tại: `http://localhost:5000`
- Prometheus metrics endpoint: `http://localhost:5000/actuator/prometheus`
- Kiểm tra unit tests & Spring Modulith boundaries:
  ```bash
  .\mvnw.cmd test
  ```

### 3. Chạy Frontend (React 19 + Vite 8)
```bash
cd Frontend
npm install
npm run dev
```
- Frontend dev server chạy tại: `http://localhost:5173` (gọi API mặc định `VITE_API_URL=http://localhost:5000/api`)
- Kiểm tra lint & build production:
  ```bash
  npm run lint
  npm run build
  ```

---

## Tài khoản Seed Data
Hệ thống có sẵn các tài khoản mẫu (khởi tạo qua `DataSeeder.java` hoặc `07_Script_Seed_Data_EduMyDb.sql`):
- **Admin**: `admin@edumy.com` / `Admin@123`
- **Instructor**: `instructor@edumy.com` / `Admin@123`
- **Student**: `student@edumy.com` / `Admin@123`
