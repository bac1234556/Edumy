# EduMy System

## Yêu cầu môi trường
- Java 17+ SDK (Eclipse Temurin / OpenJDK 17)
- Python 3.11+ (cho ML Service)
- Microsoft SQL Server 2019+
- Docker & Docker Compose (tùy chọn)

## Cấu trúc thư mục
- `Backend/`: Spring Boot 3.2.0 Web API (Java 17, Spring Data JPA, Spring Security, JWT, MSSQL, Stripe).
- `Frontend/`: React Client (Vite + React UI).
- `MLService/`: FastAPI Machine Learning Service (Phân tích văn bản, gợi ý, phân loại).
- `software_specification/`: Toàn bộ tài liệu đặc tả nghiệp vụ (SRS), luồng xử lý, ERD và kịch bản SQL DDL tạo 30 bảng.

## Hướng dẫn chạy dự án với Docker (Khuyên dùng)
Dự án được cấu hình sẵn với Docker Compose bao gồm SQL Server, Backend và MLService:

1. Chạy lệnh:
   ```bash
   docker-compose up -d --build
   ```
2. API Backend sẽ chạy ở: `http://localhost:5000`
3. ML Service sẽ chạy ở: `http://localhost:8000`
4. SQL Server chạy ở port `1433`.

## Hướng dẫn chạy dự án thủ công

### Cấu hình SQL Server
1. Chạy script SQL tạo database và 30 bảng trong thư mục:
   `software_specification/06_Script_Tao_Database_EduMyDb.sql`
2. Kiểm tra chuỗi kết nối trong `Backend/src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=EduMyDb;encrypt=false;trustServerCertificate=true
       username: sa
       password: YourPassword123
   ```

### Chạy Backend (Spring Boot)
1. Di chuyển vào thư mục `Backend`:
   ```bash
   cd Backend
   ```
2. Khởi động server:
   - Trên Windows:
     ```bash
     .\mvnw.cmd spring-boot:run
     ```
   - Trên Linux / macOS:
     ```bash
     ./mvnw spring-boot:run
     ```
3. Backend API lắng nghe tại port: `http://localhost:5000`

### Chạy ML Service
1. Di chuyển vào thư mục `MLService`:
   ```bash
   cd MLService
   ```
2. Cài đặt thư viện:
   ```bash
   pip install fastapi uvicorn pydantic
   ```
3. Chạy server:
   ```bash
   uvicorn main:app --reload --port 8000
   ```

### Chạy Frontend
1. Di chuyển vào thư mục `Frontend`:
   ```bash
   cd Frontend
   ```
2. Cài đặt dependencies:
   ```bash
   npm install
   ```
3. Chạy ứng dụng:
   ```bash
   npm run dev
   ```

## Tài khoản Seed Data
Hệ thống có sẵn các tài khoản mẫu sau khi chạy script SQL:
- **Admin**: admin@edumy.com / Admin@123
- **Instructor**: instructor@edumy.com / Instructor@123
- **Student**: student@edumy.com / Student@123
