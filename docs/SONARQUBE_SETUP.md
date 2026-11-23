# Hướng Dẫn Tích Hợp & Cấu Hình SonarQube Cho Jenkins Pipeline (Edumy)

Tài liệu này hướng dẫn từng bước dựng **SonarQube Server** bằng Docker và kết nối thật với **Jenkins CI/CD** (chạy trên Windows 11 dưới dạng Windows Service) cho dự án **Edumy**.

> [!NOTE]
> **Tại sao SonarQube được tách thành file Compose riêng (`Edumy/sonarqube/docker-compose.sonar.yml`)?**
> File `Edumy/docker-compose.yml` chính được dùng để deploy ứng dụng lên máy chủ chỉ có **2 GB RAM**. SonarQube (cùng Elasticsearch nhúng bên trong) yêu cầu lượng RAM đáng kể, do đó được tách riêng để chạy trên máy trạm CI/CD (Docker Desktop) nhằm không làm quá tải máy chủ triển khai.

---

## Phạm Vi Phân Tích Hiện Tại

* **Backend (`edumy-backend`)**: Được phân tích tự động trong stage **`SonarQube Analysis`** bằng Maven Wrapper (`mvnw` / `mvnw.cmd`) kết hợp sẵn `sonar-maven-plugin:3.10.0.2594` và báo cáo độ phủ mã nguồn từ `jacoco-maven-plugin:0.8.11` (`target/site/jacoco/jacoco.xml`).
* **Lý do chưa gộp Frontend (`edumy-frontend`) vào pipeline Windows**:
  * Quét Frontend React/Vite yêu cầu cài đặt và cấu hình thêm công cụ rời **`SonarScanner` CLI** trong Jenkins Global Tool Configuration và đảm bảo biến `PATH` có cả `JAVA_HOME` lẫn `Node.js` cho tài khoản `LocalSystem` của Windows Service — dễ phát sinh lỗi môi trường trên Windows.
  * Ngoài ra, plugin SonarQube cho Jenkins chỉ theo dõi 1 `ceTaskId` cho mỗi khối `withSonarQubeEnv`; nếu chạy 2 dự án liên tiếp trong cùng một stage sẽ gây khó kiểm soát cho bước `waitForQualityGate`.
  * Do đó, pipeline tập trung phân tích **Backend Spring Boot** để đảm bảo độ ổn định tối đa, không cần cài thêm CLI ngoài và kiểm tra Quality Gate chính xác tuyệt đối. *(Nếu sau này muốn mở rộng Frontend, có thể tạo thêm 1 stage riêng `SonarQube Frontend` dùng tool `SonarScanner` với `-Dsonar.projectKey=edumy-frontend -Dsonar.sources=src -Dsonar.exclusions=node_modules/**,dist/**` kèm 1 bước `waitForQualityGate` riêng).*

---

## Bước 1: Khởi Động SonarQube Server Bằng Docker

1. Mở **PowerShell** (hoặc Terminal) tại thư mục gốc repository `Edumy` và chạy:
   ```powershell
   docker compose -f Edumy/sonarqube/docker-compose.sonar.yml up -d
   ```
2. *(Nếu dùng Docker Desktop trên Windows/WSL2 mà container bị dừng ngay khi khởi động)*, hãy chạy lệnh cấp phát `vm.max_map_count` sau trên PowerShell (Run as Administrator) rồi khởi động lại container:
   ```powershell
   wsl -d docker-desktop sysctl -w vm.max_map_count=262144
   docker compose -f Edumy/sonarqube/docker-compose.sonar.yml up -d
   ```
3. Chờ khoảng **1 – 2 phút** để SonarQube khởi tạo xong (có thể theo dõi bằng `docker logs -f edumy_sonarqube` cho đến khi thấy dòng `SonarQube is operational`).
4. Mở trình duyệt truy cập **`http://localhost:9000`**:
   * Đăng nhập tài khoản mặc định: **`admin`** / **`admin`**.
   * Hệ thống sẽ yêu cầu **đổi mật khẩu mới** ở lần đăng nhập đầu tiên — hãy đặt mật khẩu mới và lưu lại.

---

## Bước 2: Tạo Token Xác Thực Trong SonarQube

1. Trên giao diện SonarQube (`http://localhost:9000`), bấm vào biểu tượng **Avatar góc trên bên phải** → chọn **My Account**.
2. Chuyển sang tab **Security**.
3. Trong mục **Generate Tokens**:
   * **Name**: nhập tên dễ nhớ, ví dụ `jenkins-edumy`.
   * **Type**: chọn **Global Analysis Token** (hoặc **User Token**).
   * **Expires in**: chọn thời hạn phù hợp (ví dụ 90 days hoặc No expiration).
4. Bấm **Generate** và **copy ngay chuỗi token** vừa sinh ra (chỉ hiển thị duy nhất 1 lần).

> [!CAUTION]
> **Tuyệt đối không commit token vào `Jenkinsfile` hay bất kỳ file nào trong Git.** Token phải được lưu bảo mật trong Jenkins Credentials.

---

## Bước 3: Cấu Hình SonarQube Trong Jenkins

### 3.1. Cài đặt Plugin
1. Mở Jenkins tại **`http://localhost:8080`**.
2. Vào **Manage Jenkins → Plugins → Available plugins**.
3. Tìm và cài đặt plugin **`SonarQube Scanner`** (tên chính thức: *SonarQube Scanner for Jenkins*). Nếu đã cài sẵn ở tab **Installed plugins** thì bỏ qua bước này.

### 3.2. Tạo Credential lưu Token
1. Vào **Manage Jenkins → Credentials → System → Global credentials (unrestricted)**.
2. Bấm **+ Add Credentials** và điền chính xác:
   * **Kind**: `Secret text`
   * **Scope**: `Global`
   * **Secret**: dán chuỗi token đã copy ở Bước 2.
   * **ID**: `sonarqube-token` *(khuyến nghị đặt đúng ID này để dễ quản lý)*.
   * **Description**: `SonarQube Token for Edumy Jenkins Pipeline`
3. Bấm **Create**.

### 3.3. Khai báo SonarQube Server trong Jenkins
1. Vào **Manage Jenkins → System**.
2. Cuộn xuống mục **SonarQube servers**:
   * Tích chọn **Environment variables** (`Enable injection of SonarQube server configuration as build environment variables`).
   * Bấm **Add SonarQube** và nhập chính xác:
     * **Name**: `SonarQube` *(bắt buộc khớp chính xác với tên trong `withSonarQubeEnv('SonarQube')` của `Jenkinsfile`)*.
     * **Server URL**: `http://localhost:9000`
     * **Server authentication token**: chọn credential `sonarqube-token` vừa tạo.
3. Bấm **Save**.

---

## Bước 4: Tạo Webhook Từ SonarQube Trỏ Về Jenkins (Bắt Buộc Cho Quality Gate)

Stage **`Quality Gate`** sử dụng lệnh `waitForQualityGate`, lệnh này sẽ chờ SonarQube gửi tín hiệu HTTP Webhook báo kết quả phân tích xong về Jenkins. **Nếu thiếu bước này, pipeline sẽ bị treo đến khi hết timeout 10 phút.**

1. Trên giao diện SonarQube (`http://localhost:9000`), vào **Administration → Configuration → Webhooks**.
2. Bấm **Create** và nhập:
   * **Name**: `Jenkins Local`
   * **URL**:
     ```text
     http://host.docker.internal:8080/sonarqube-webhook/
     ```
   * **Secret**: để trống (trừ khi bạn có cấu hình webhook secret tương ứng trong Jenkins).
3. Bấm **Create**.

> [!IMPORTANT]
> * Vì **SonarQube chạy bên trong Docker container** còn **Jenkins chạy trực tiếp trên Windows host (cổng 8080)**, URL **bắt buộc phải dùng `host.docker.internal`** thay vì `localhost`.
> * URL **bắt buộc phải có dấu gạch chéo `/` ở cuối** (`/sonarqube-webhook/`).

---

## Bước 5: Chạy Pipeline & Kiểm Tra Kết Quả

1. Vào Job Jenkins (`Edumy-CI-CD` hoặc `Edumy-Deploy-Server`).
   *(Lưu ý: ở lần chạy đầu tiên sau khi cập nhật `Jenkinsfile`, Jenkins có thể cần chạy 1 lượt để nạp định nghĩa tham số mới `RUN_SONAR` và `ENFORCE_QUALITY_GATE`.)*
2. Chọn **Build with Parameters** và kiểm tra các tùy chọn:
   * `SKIP_TESTS`: **bỏ chọn (`false`)** để Maven chạy unit test và tạo báo cáo JaCoCo coverage (`target/site/jacoco/jacoco.xml`).
   * `RUN_SONAR`: **tích chọn (`true`)**.
   * `ENFORCE_QUALITY_GATE`: **tích chọn (`true`)** nếu muốn pipeline tự dừng khi Quality Gate thất bại (Failed).
3. Bấm **Build** và theo dõi trên Stage View:
   * Stage **`Build & Test Các Dịch Vụ`** chạy `mvnw clean package` tạo `target/classes` và `target/site/jacoco/jacoco.xml`.
   * Stage **`SonarQube Analysis`** chạy `mvnw sonar:sonar` (không `clean`) gửi dữ liệu lên `http://localhost:9000`.
   * Stage **`Quality Gate`** nhận webhook từ SonarQube và báo trạng thái `OK` (Passed).
4. Truy cập **`http://localhost:9000/dashboard?id=edumy-backend`** để xem chi tiết: **Bugs, Vulnerabilities, Security Hotspots, Code Smells, Coverage và Duplications**.

---

## Bước 6: Bảng Xử Lý Lỗi Thường Gặp

| Lỗi / Triệu chứng | Nguyên nhân phổ biến | Cách khắc phục |
| :--- | :--- | :--- |
| **Stage `Quality Gate` treo mãi tới khi hết timeout (10 phút)** | Chưa tạo Webhook trong SonarQube, dùng `localhost:8080` thay vì `host.docker.internal:8080`, hoặc thiếu dấu `/` cuối URL. | Kiểm tra **Administration → Configuration → Webhooks** trên SonarQube, đảm bảo URL đúng là `http://host.docker.internal:8080/sonarqube-webhook/`. Xem mục *Recent Deliveries* để kiểm tra mã HTTP trả về `200`. |
| **Lỗi `401 Unauthorized` / `Not authorized`** khi chạy `sonar:sonar` | Token sai, token đã hết hạn, tạo nhầm loại Project Analysis Token cho sai project key, hoặc chưa chọn credential `sonarqube-token` trong cấu hình server. | Tạo lại **Global Analysis Token** (hoặc User Token) trong SonarQube, cập nhật lại credential `sonarqube-token` và chọn lại ở **Manage Jenkins → System → SonarQube servers**. |
| **Container `edumy_sonarqube` khởi động rồi tự tắt (Exited)** | Giới hạn `vm.max_map_count` của nhân Linux/WSL2 thấp hơn mức tối thiểu `262144` mà Elasticsearch trong SonarQube yêu cầu. | Chạy PowerShell (Administrator): `wsl -d docker-desktop sysctl -w vm.max_map_count=262144`, sau đó chạy lại `docker compose -f Edumy/sonarqube/docker-compose.sonar.yml up -d`. |
| **Dashboard hiển thị Coverage = `0.0%` hoặc `-`** | Đã tích `SKIP_TESTS=true` khi build (nên JaCoCo không chạy) hoặc chạy `mvn clean` làm xóa mất `target/site/jacoco/jacoco.xml`. | Chạy build với `SKIP_TESTS=false`. Kiểm tra file `Edumy/Backend/target/site/jacoco/jacoco.xml` có tồn tại sau stage Build & Test. |
| **Lỗi `SonarQube installation defined in this job (SonarQube) does not match any configured installation`** | Tên server trong Jenkins đặt khác với `'SonarQube'` (ví dụ `sonar cloud` hoặc `sonarqube` chữ thường). | Vào **Manage Jenkins → System → SonarQube servers**, đặt **Name** chính xác là `SonarQube` (phân biệt chữ hoa/thường) và bấm **Save**. |
