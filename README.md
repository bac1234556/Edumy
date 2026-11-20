# Edumy - Nền Tảng E-Learning & Đào Tạo Trực Tuyến (Spring Modulith + React 19)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Modulith](https://img.shields.io/badge/Spring%20Modulith-1.1.5-6DB33F.svg)](https://spring.io/projects/spring-modulith)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![React](https://img.shields.io/badge/React-19.2-blue.svg)](https://react.dev/)
[![SQL Server](https://img.shields.io/badge/Database-SQL%20Server%202022-red.svg)](https://www.microsoft.com/sql-server)
[![Observability](https://img.shields.io/badge/Monitoring-Prometheus%20%2B%20Grafana-E6522C.svg)](https://grafana.com/)
[![Jenkins](https://img.shields.io/badge/CI%2FCD-Jenkins%20Pipeline-D24939.svg)](https://www.jenkins.io/)

Edumy là nền tảng học trực tuyến (E-Learning Platform) được xây dựng theo kiến trúc **Modular Monolith (Spring Modulith 1.1.5)** trên nền tảng **Java 17 + Spring Boot 3.2.0**, phục vụ đồng thời **Web SPA (React 19 + Vite 8)** và **Mobile Client (Expo/REST Bearer Token)** trên một lõi nghiệp vụ thống nhất (*Single Source of Truth*), tích hợp thanh toán **VietQR / SePay Webhook / VNPay / Stripe**, cấp chứng chỉ PDF tiếng Việt (**OpenPDF**), gửi email bất đồng bộ (**Mailpit SMTP**), sổ cái doanh thu giảng viên (**Payout Ledger**) và giám sát hệ thống thời gian thực (**Prometheus + Grafana**).

---

## 🏛️ Kiến Trúc Hệ Thống (Spring Modulith Architecture)

```text
  [ Web SPA: React 19 + Vite 8 / Nginx ] (Docker: 3000 | Dev: 5173)
  [ Mobile App: Expo / Native Client   ] (/api/v1/auth/mobile/*)
                    │
                    ▼  (REST JSON + JWT / HttpOnly Refresh Cookie)
  ┌─────────────────────────────────────────────────────────────────┐
  │        Java 17 + Spring Boot 3.2.0 + Spring Modulith 1.1.5      │
  │                     (Port 5000 | /api, /api/v1)                 │
  ├─────────────┬─────────────┬──────────────┬──────────────────────┤
  │ 01.identity │ 02.catalog  │ 03.authoring │ 04.commerce          │
  │ 05.learning │ 06.payout   │ notification │ shared (Kernel)      │
  └──────┬──────┴─────────────┴──────┬───────┴──────────┬───────────┘
         │ JDBC (MSSQL)              │ SMTP             │ /actuator/prometheus
         ▼                           ▼                  ▼
  [ SQL Server 2022 ]       [ Mailpit SMTP ]   [ Prometheus + Grafana ]
  (Docker: 14333->1433)     (UI: 8025 | 1025)  (9090 | 3001 + Telegram)
```

### 8 Module Trong Spring Modulith (`com.edumy.*`)
| Module | Vai trò & Nghiệp vụ chính | Phụ thuộc cho phép (`@ApplicationModule`) |
| :--- | :--- | :--- |
| **`identity`** (`01 Identity`) | Đăng ký, đăng nhập Web (Cookie Refresh) & Mobile (Opaque Refresh Token Rotation), Google OAuth, xác thực email, quên/đặt lại mật khẩu, quản lý User/Role, log hoạt động (`UserActivity`) & `SearchHistory` | `shared` |
| **`catalog`** (`02 Catalog`) | Tìm kiếm khóa học tức thì (`instant-search`), duyệt danh mục/thẻ (`Categories`, `Tags`), gợi ý khóa học tương đồng (`similar`), combo (`bundle-recommendation`) & nổi bật (`recommend`) | `shared`, `authoring :: api`, `learning :: api` |
| **`authoring`** (`03 Authoring`) | Tạo/chỉnh sửa khóa học, chương (`CourseSection`), bài học (`Lesson`), sắp xếp giáo trình, upload media, đồng giảng viên (`Co-Instructor` & lời mời), quy trình gửi duyệt/phê duyệt/ẩn khóa học | `shared` |
| **`commerce`** (`04 Commerce`) | Giỏ hàng (`Cart`), mã giảm giá (`Coupon`), đơn hàng (`Order`, `buy-now`, `checkout`), thanh toán **VietQR động**, **SePay/Casso Webhook** tự kích hoạt khóa học, **VNPay**, **Stripe** | `shared`, `authoring :: api`, `learning :: api`, `payout :: api` |
| **`learning`** (`05 Learning`) | Ghi danh (`Enrollment`), kiểm soát quyền truy cập/preview bài học, theo dõi tiến độ (`LessonProgress`), bài kiểm tra trắc nghiệm (`Quiz`, `QuizAttempt`), đánh giá (`Review`), diễn đàn hỏi đáp **Q&A** (`CourseQuestion`, `QuestionAnswer`), xuất & xác thực chứng chỉ PDF tiếng Việt (**OpenPDF**) | `shared`, `authoring :: api` |
| **`payout`** (`06 Payout`) | Sổ cái doanh thu giảng viên (`EarningEntry`), tách phí nền tảng & chia sẻ đồng giảng viên, chính sách kỳ hạn (`maturity`), cấu hình tài khoản ngân hàng (`PayoutAccount`), yêu cầu & duyệt rút tiền (`WithdrawalRequest`) | `shared`, `identity :: api`, `authoring :: api` |
| **`notification`** (`Notification`) | Lắng nghe Domain Events (`@Async` + `@TransactionalEventListener` AFTER_COMMIT) và gửi 10 mẫu email HTML tiếng Việt qua **Mailpit / SMTP** | `shared`, `identity :: api`, `authoring :: api`, `commerce :: api`, `learning :: api` |
| **`shared`** (`Shared`) | Security JWT & CORS, `DataSeeder`, `ApiResponseAdvice` (chuẩn hóa envelope JSON), `GlobalExceptionHandler`, `PagedResultDto`, chuẩn hóa tiếng Việt & slug, validation giá khóa học | *(Shared Kernel)* |

---

## 📁 Cấu Trúc Thư Mục Dự Án

```text
Edumy/
├── 📂 Edumy/                          <-- Mã nguồn lõi & hạ tầng Docker
│   ├── 📂 Backend/                    <-- Java 17 + Spring Boot 3.2.0 + Spring Modulith 1.1.5
│   ├── 📂 Frontend/                   <-- React 19.2 + Vite 8.1 + Design System + Nginx
│   ├── 📂 Database/                   <-- SQL Server 2022 Docker, EduMyDb.bak, DDL & Seed scripts
│   ├── 📂 monitoring/                 <-- Prometheus scrape config & Grafana dashboards/alerts
│   ├── 📂 k8s/                        <-- Kubernetes Namespace, Database, Backend, Frontend & Mailpit
│   ├── 📂 docs/                       <-- Tài liệu kỹ thuật (system, design-system, ml legacy)
│   ├── 📄 docker-compose.yml          <-- Stack 6 containers (database, backend, mailpit, frontend, prometheus, grafana)
│   └── 📄 .env.example                <-- Biến môi trường Telegram Alerting cho Grafana
├── 📂 scripts/                        <-- Script vận hành & kiểm thử trên Windows PowerShell
│   ├── 📄 docker_start.ps1            <-- Khởi chạy toàn bộ stack 6 containers trên Docker Desktop
│   ├── 📄 docker_stop.ps1             <-- Dừng toàn bộ containers
│   ├── 📄 start_all.ps1               <-- Khởi chạy cục bộ Backend + Frontend trên Windows
│   └── 📄 verify_audit.ps1            <-- Kiểm thử tích hợp các luồng API đa vai trò
├── 📄 Jenkinsfile                     <-- Pipeline CI/CD tự động (Build, Test, Docker Compose Deploy)
├── 📄 .gitignore & .gitattributes
└── 📄 README.md                       <-- Tài liệu tổng quan dự án (file này)
```

---

## 🚀 Hướng Dẫn Khởi Chạy Nhanh (Quick Start)

### Cách 1: Chạy toàn bộ hệ thống với Docker Desktop (Khuyên dùng)
Mở PowerShell tại thư mục `Edumy/` và chạy:
```powershell
.\scripts\docker_start.ps1
```
*(Hoặc chạy trực tiếp `cd Edumy; docker compose up -d --build` — tùy chọn copy `.env.example` thành `.env` nếu muốn bật cảnh báo Telegram cho Grafana).*

Hệ thống gồm **6 containers** sẽ sẵn sàng tại:
| Dịch vụ | URL / Port | Ghi chú |
| :--- | :--- | :--- |
| **Frontend UI (Nginx)** | `http://localhost:3000` | Giao diện React 19 production |
| **Backend API (Spring Boot)** | `http://localhost:5000` | REST API + `/actuator/health` & `/actuator/prometheus` |
| **SQL Server 2022** | `localhost:14333` (`sa` / `YourPassword123`) | Tự động restore `EduMyDb.bak` khi khởi tạo volume |
| **Mailpit Web UI / SMTP** | `http://localhost:8025` (SMTP `1025`) | Hộp thư test nhận email xác thực, đơn hàng, Q&A |
| **Prometheus** | `http://localhost:9090` | Thu thập metrics từ Backend mỗi 5 giây |
| **Grafana** | `http://localhost:3001` (`admin` / `admin`) | Dashboard & Alerting (Backend Down, CPU > 85%, HTTP 5xx) |

Để dừng toàn bộ container:
```powershell
.\scripts\docker_stop.ps1
```

### Cập nhật Database (Payout & Sync)
Chạy script idempotent `Edumy/Database/scripts/08_Fix_Payout_And_Sync.sql` trên SQL Server (`EduMyDb`) để tạo filtered unique index `UX_WithdrawalRequests_OpenPerInstructor` và backfill `MaturesAt`:
```powershell
sqlcmd -S 127.0.0.1,1433 -U sa -P YourPassword123 -d EduMyDb -i Edumy\Database\scripts\08_Fix_Payout_And_Sync.sql
```

---

### Cách 2: Khởi chạy cục bộ cho môi trường phát triển (Localhost)

#### Bước 1: Chuẩn bị SQL Server
- Đảm bảo SQL Server đang chạy tại `127.0.0.1:1433` (tài khoản mặc định trong `application.yml`: `sa` / `YourPassword123`, database `EduMyDb`).
- Có thể chạy script trong `Edumy/Database/scripts/06_Script_Tao_Database_EduMyDb.sql` và `07_Script_Seed_Data_EduMyDb.sql`, hoặc để `DataSeeder` & Hibernate `ddl-auto: update` tự khởi tạo schema và dữ liệu mẫu khi chạy Backend.

#### Bước 2: Chạy 1-Click hoặc thủ công
Chạy nhanh cả Backend & Frontend trên Windows:
```powershell
.\scripts\start_all.ps1
```
Hoặc chạy thủ công từng thành phần:
```bash
# Backend (Port 5000)
cd Edumy/Backend
.\mvnw.cmd spring-boot:run

# Frontend (Port 5173)
cd Edumy/Frontend
npm install
npm run dev
```

---

## 👥 Tài Khoản Mẫu (Seed Accounts)

Theo `DataSeeder.java` và `07_Script_Seed_Data_EduMyDb.sql`, hệ thống tạo sẵn các tài khoản sau (chỉ dùng cho môi trường phát triển/demo):

| Vai trò | Email đăng nhập | Mật khẩu | Chức năng chính |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@edumy.com` | `Admin@123` | Duyệt/ẩn khóa học, khóa/mở tài khoản, duyệt rút tiền giảng viên, kiểm duyệt Q&A, quản lý danh mục & đơn hàng |
| **Instructor** | `instructor@edumy.com` | `Admin@123` | Tạo & biên tập giáo trình, mời đồng giảng viên, trả lời Q&A, xem doanh thu & gửi yêu cầu rút tiền |
| **Student** | `student@edumy.com` | `Admin@123` | Tìm kiếm, giỏ hàng, thanh toán VietQR/VNPay, học bài giảng, làm Quiz, hỏi đáp Q&A, nhận chứng chỉ PDF |

---

## 🧪 Kiểm Thử & CI/CD

- **Kiểm thử Unit & Kiến trúc Spring Modulith**:
  ```bash
  cd Edumy/Backend
  .\mvnw.cmd test
  ```
  Bao gồm `ModularityTest`, `AuthServiceImplTest`, `MobileAuthControllerTest`, `EarningServiceImplTest`, `WithdrawalServiceImplTest` cùng JaCoCo coverage report.
- **Kiểm thử tích hợp API**:
  ```powershell
  .\scripts\verify_audit.ps1
  ```
- **CI/CD (Jenkins)**: `Jenkinsfile` tự động hóa Checkout SCM → Build & Test Backend & Frontend → Build image database local và build/push image `edumy-backend`, `edumy-frontend` với tag build và `latest` → Deploy Kubernetes (mặc định) hoặc Docker Compose.

### Triển khai Kubernetes qua Jenkins

Pipeline mặc định dùng image prefix `docker.io/lynkenion`; đổi `DOCKER_REGISTRY` nếu dùng namespace/registry khác. Jenkins agent cần Docker CLI, `kubectl`, quyền truy cập cluster và plugin Credentials Binding. Chỉ cần Docker Compose v2 hoặc `docker-compose` nếu chọn deploy bằng Docker Compose. Tạo các Jenkins credentials:

| Credential ID (mặc định) | Loại | Giá trị |
| :--- | :--- | :--- |
| `dockerhub-credentials` | Username with password | Tài khoản Docker Hub và access token có quyền push |
| `kubeconfig` | Secret file | File kubeconfig có quyền quản lý namespace `edumy` |

Tạo secret runtime một lần trên cluster; không commit mật khẩu vào repository:
```powershell
kubectl create namespace edumy
kubectl -n edumy create secret generic edumy-runtime `
  --from-literal=database-password='<mật-khẩu-SQL-Server-mạnh>' `
  --from-literal=jwt-secret='<JWT-secret-ngẫu-nhiên-dài, ít nhất 32 ký tự>'
```
Tạo secret này trước lần deploy Kubernetes đầu tiên; pipeline sẽ dừng sớm nếu thiếu `edumy-runtime`. Pipeline cũng chờ StatefulSet SQL Server sẵn sàng trước khi kiểm tra rollout của Backend.

Database image `docker.io/lynkenion/edumy-database:latest` được build và tag local, không push registry vì image chứa `EduMyDb.bak`. Kubernetes StatefulSet dùng `imagePullPolicy: IfNotPresent`: node sẽ dùng image đã cache hoặc pull image hiện có từ registry, với `edumy-registry` làm pull secret. Image vừa build trên Jenkins không tự được chuyển sang containerd của node; muốn dùng bản build mới mà không push, cần import image vào node bằng công cụ phù hợp với cluster.

Sau đó chạy Jenkins với `DEPLOY_TARGET=kubernetes`, `BUILD_DOCKER_IMAGES=true`, `PUSH_TO_REGISTRY=true` và `DEPLOY_APPLICATION=true`. Pipeline cập nhật registry pull secret, apply manifests trong `Edumy/k8s/`, cập nhật image tag theo build và đợi rollout. Frontend được expose qua Service `LoadBalancer`; nếu cluster local không cấp external IP, dùng:
```powershell
kubectl -n edumy port-forward service/edumy-frontend 8080:80
```
Mở `http://localhost:8080`. Kubernetes cần một StorageClass mặc định cho PVC SQL Server 20Gi. Stack SQL Server `Developer` và bản backup đi kèm phù hợp môi trường development/demo; cấu hình production cần xem xét license, backup/restore và lưu trữ dữ liệu riêng.

Nếu `kubectl apply` báo không tải được OpenAPI hoặc `connection refused` tới `127.0.0.1`, cluster API chưa chạy hoặc kubeconfig đang dùng endpoint chỉ truy cập được từ một máy/container khác. Kiểm tra `kubectl cluster-info` trên chính Jenkins agent và cấu hình kubeconfig với endpoint truy cập được từ agent; không dùng `--validate=false` để xử lý lỗi kết nối.

Lưu ý: image `edumy-database` chứa file `EduMyDb.bak` được copy bởi Dockerfile, vì vậy chỉ push lên registry private nếu backup có dữ liệu không được phép công khai.
