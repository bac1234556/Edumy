# EDU-TER-W-001 — BÁO CÁO THIẾT LẬP MÔI TRƯỜNG & DỮ LIỆU KIỂM THỬ (TEST ENVIRONMENT REPORT)

| Thuộc tính | Giá trị |
| :--- | :--- |
| **Mã tài liệu** | `EDU-TER-W-001` |
| **Phiên bản** | `1.0` |
| **Ngày lập** | `2026-11-21` (Asia/Ho_Chi_Minh) |
| **Build kiểm thử** | `Edumy-Project@f2f5240` · `Edumy-main@bf4ac6c` |
| **Trạng thái Entry Gate** | **ĐẠT (READY)** — `mvnw verify` (30/30 PASS), `npm run build` (PASS), 6/6 container `edumy-test` healthy/running, baseline `.bak` đã tạo, Smoke Test **10/10 PASS** |

---

## 1. Cấu hình máy chủ kiểm thử (Host Specs)

| Thành phần | Thông số thực tế |
| :--- | :--- |
| **Hệ điều hành** | Microsoft Windows 11 Pro (`10.0.26300 Build 26300`) |
| **Bộ nhớ RAM** | `11.973 MB` (~12 GB) |
| **Phần cứng / BIOS** | Lenovo (`PQCN30WW`) |
| **Docker Engine** | Docker `29.9.2` (build `a82e528`) |
| **Java / Maven** | Java `21.0.10 LTS` (biên dịch mục tiêu Java 17 qua `mvnw.cmd`) |
| **Node.js** | `v24.16.0` |
| **Python** | `3.12.10` (`matplotlib 3.11.0`, `openpyxl 3.1.5` trong venv tách biệt ngoài repo) |
| **Trình duyệt cài đặt** | Google Chrome `157.0.8103.80`, Microsoft Edge `158.0.4370.33`, Mozilla Firefox: **Chưa cài đặt** *(sẽ ghi nhận BLOCKED riêng phần Firefox trong `TC-W-M07-COMP-001`)* |

---

## 2. Stack Docker Kiểm Thử Độc Lập (`edumy-test`)

Để bảo toàn tuyệt đối dữ liệu của môi trường phát triển (`edumy_*` đang chạy trên các cổng mặc định `3000/5000/14333/8025/9090/3001`), môi trường kiểm thử được dựng tách biệt bằng compose project `edumy-test` qua file [docker-compose.test.yml](file:///c:/Users/bacle/Downloads/Edumy-Project/Edumy-main/docs/testing/web/scripts/docker-compose.test.yml) (thời gian khởi chạy sạch: **78 giây**):

| Dịch vụ | Container Name | Host Port → Container Port | URL truy cập kiểm thử | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **Database (SQL Server 2022)** | `edumy-test-database-1` | `14334 → 1433` | `localhost:14334` (`EduMyDb`) | Healthy |
| **Backend (Spring Boot 3.2.0)** | `edumy-test-backend-1` | `5001 → 5000` | `http://localhost:5001` | UP (`/actuator/health`) |
| **Frontend (React 19 + Nginx)** | `edumy-test-frontend-1` | `3002 → 80` | `http://localhost:3002` | Running |
| **Mailpit (SMTP + Web UI)** | `edumy-test-mailpit-1` | `1026 → 1025`, `8026 → 8025` | `http://localhost:8026` | Healthy |
| **Prometheus v2.51.0** | `edumy-test-prometheus-1` | `9091 → 9090` | `http://localhost:9091` | Running |
| **Grafana 10.4.1** | `edumy-test-grafana-1` | `3003 → 3000` | `http://localhost:3003` | Running |

---

## 3. Bộ Dữ Liệu Kiểm Thử & Baseline Tái Lập

> **Bảo mật**: Không ghi mật khẩu, JWT token, API key hay secret thật vào tài liệu này (mọi thông tin đăng nhập kiểm thử được quản lý cục bộ qua biến môi trường khi chạy script).

### 3.1. 10 Tài khoản kiểm thử chuẩn (đã seed qua `seed_and_baseline.mjs`)

| Alias | User ID | Email | Vai trò & Trạng thái |
| :--- | :---: | :--- | :--- |
| `ADM1` | `1` | `admin@edumy.com` | Admin hoạt động (seed mặc định) |
| `ADM2` | `19` | `qa.adm2@edumy.test` | Admin hoạt động thứ 2 (test AC-37) |
| `GV-A` | `20` | `qa.gva@edumy.test` | Giảng viên chính sở hữu bộ khóa test |
| `GV-B` | `21` | `qa.gvb@edumy.test` | Giảng viên độc lập thứ 2 (test IDOR) |
| `GV-CO` | `22` | `qa.gvco@edumy.test` | Đồng giảng viên (`shareBps=2000` trên khóa chia doanh thu) |
| `GV-REVOKED` | `23` | `qa.gvrevoked@edumy.test` | Từng là GV, đã bị Admin thu hồi về Student (test AC-26) |
| `HV1` | `24` | `qa.hv1@edumy.test` | Học viên chính (đã xác thực email) |
| `HV2` | `25` | `qa.hv2@edumy.test` | Học viên độc lập thứ 2 (test IDOR) |
| `HV-LOCKED` | `26` | `qa.hvlocked@edumy.test` | Tài khoản đã bị khóa (`isActive=false`) |
| `HV-UNVERIFIED` | `27` | `qa.hvunverified@edumy.test` | Tài khoản chưa xác thực email (test BR-34) |

### 3.2. Dữ liệu nghiệp vụ & Baseline `.bak`
- **Migration**: Đã áp dụng `08_Fix_Payout_And_Sync.sql` (tạo filtered unique index `UX_WithdrawalRequests_OpenPerInstructor` & đồng bộ `matures_at`).
- **Danh mục**: Đủ `>= 4` danh mục gồm các danh mục có khóa học + 1 danh mục trống (`QA Danh Mục Trống D21`) để test xóa theo D-21.
- **Khóa học (8 khóa QA + khóa seed)**: Phủ đủ **5 trạng thái D-13** (`Draft`, `PendingApproval`, `NeedsReview`, `Hidden`, `Published`), gồm khóa miễn phí 3 bài (`Lập Trình Java Cơ Bản Miễn Phí`), khóa miễn phí **10 bài** (`Khóa Học Chứng Chỉ 10 Bài Tiếng Việt`), khóa trả phí 300.000đ có Quiz (`Lập Trình Java Nâng Cao Spring Boot`), khóa trả phí 1.000.000đ có đồng GV 20% (`Kiến Trúc Microservices Chuyên Sâu`).
- **Coupon**: 3 mã (`QAGIAM20` hợp lệ 20%, `QAEXPIRED` hết hạn, `QAINACTIVE` ngừng hoạt động).
- **Baseline tái lập**: Đã sao lưu thành công `edumy_test_baseline.bak` (`1.146 pages` trong `0.125s`) tại `/var/opt/mssql/edumy_test_baseline.bak` bên trong `edumy-test-database-1`.

---

## 4. Kết quả Smoke Test Entry Gate (10/10 PASS)

Bằng chứng: [RUN-W-C1-0000_smoke_test.json](file:///c:/Users/bacle/Downloads/Edumy-Project/Edumy-main/docs/testing/web/evidence/RUN-W-C1-0000_smoke_test.json)

| Mã | Module | Kiểm tra | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| `SMK-01` | W-M07 | Backend `/actuator/health` | `HTTP 200 status=UP` | **PASS** |
| `SMK-02` | W-M07 | Frontend SPA (`http://localhost:3002`) | `HTTP 200 len=824` | **PASS** |
| `SMK-03` | W-M01 | Login HV1 & `GET /api/v1/me` | `login=200 me=200` | **PASS** |
| `SMK-04` | W-M02 | Public Catalog `GET /api/courses` | `HTTP 200 items=12` | **PASS** |
| `SMK-05` | W-M03 | Instructor `GET /api/courses/my-courses` | `HTTP 200 count=8` | **PASS** |
| `SMK-06` | W-M04 | Student `GET /api/v1/cart` | `HTTP 200` | **PASS** |
| `SMK-07` | W-M05 | Student `GET /api/courses/enrolled` | `HTTP 200` | **PASS** |
| `SMK-08` | W-M06 | Instructor `GET /api/v1/me/earnings/summary` | `HTTP 200 holdDays=7` | **PASS** |
| `SMK-09` | W-M01 | Admin `GET /api/v1/admin/stats` | `HTTP 200 users=27` | **PASS** |
| `SMK-10` | W-M07 | Mailpit + Prometheus + Grafana Health | `mailpit=15 prom=200 grafana=200` | **PASS** |
