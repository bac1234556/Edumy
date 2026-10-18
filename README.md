# Edumy - Nền Tảng E-Learning & Đào Tạo Trực Tuyến Tích Hợp AI

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![FastAPI](https://img.shields.io/badge/FastAPI-Python%203.11-teal.svg)](https://fastapi.tiangolo.com/)
[![SQL Server](https://img.shields.io/badge/Database-SQL%20Server-red.svg)](https://www.microsoft.com/sql-server)
[![Jenkins](https://img.shields.io/badge/CI%2FCD-Jenkins%20Pipeline-orange.svg)](https://www.jenkins.io/)

Edumy là nền tảng học trực tuyến hiện đại được xây dựng theo kiến trúc **Microservices**, tích hợp sâu các tính năng **Trí tuệ Nhân tạo (AI)** để hỗ trợ giảng viên soạn đề cương, phân loại danh mục tự động, gợi ý câu hỏi trắc nghiệm và đề xuất lộ trình học tập thông minh cho học viên.

---

## 🏛️ Kiến Trúc Hệ Thống (Architecture)

```text
[ React 18 + Vite ] (Port 5173)
        │
        ▼ (REST API / JWT)
[ Java Spring Boot 3.2 ] (Port 5000) ──────► [ SQL Server 2022 ] (Port 1433)
        │                                         (Database: EduMyDb)
        ▼ (HTTP REST / Microservice)
[ Python 3.11 FastAPI ] (Port 8000)
  ├── AI Category & Tag Classification (TF-IDF / Similarity)
  ├── AI Curriculum & Syllabus Generator
  ├── AI Quiz & Question Generator
  └── AI Personalized Learning Path Recommendation
```

---

## 📁 Cấu Trúc Thư Mục Dự Án (Repository Structure)

```text
Edumy/
│
├── 📂 Edumy/
│   ├── 📂 Backend/                   <-- Java 17 + Spring Boot 3.2.0 (REST API Server)
│   │   ├── src/main/java/com/edumy/backend/
│   │   └── pom.xml
│   │
│   ├── 📂 Frontend/                  <-- React 18 + Vite + TailwindCSS (Single Page App)
│   │   ├── src/pages/                (Trang Học viên, Giảng viên, Quản trị viên)
│   │   ├── src/components/           (Navbar, CourseCard, QuizModal, ...)
│   │   └── package.json
│   │
│   ├── 📂 MLService/                 <-- Python 3.11 + FastAPI (AI Microservice Engine)
│   │   ├── main.py                   (FastAPI Endpoints)
│   │   └── requirements.txt
│   │
│   ├── 📄 docker-compose.yml         <-- Docker compose đa dịch vụ
│   └── 📄 walkthrough.md             <-- Tài liệu hướng dẫn sử dụng và kiểm thử chi tiết
│
├── 📂 software_specification/        <-- Tài liệu đặc tả kiến trúc & yêu cầu phần mềm
├── 📄 Jenkinsfile                    <-- Pipeline CI/CD tự động (Checkout, Build, Test)
├── 📄 start_all.ps1                  <-- Script 1-click kích hoạt toàn bộ hệ thống
└── 📄 README.md                      <-- Tài liệu tổng quan dự án
```

---

## 🚀 Hướng Dẫn Khởi Chạy Nhanh (Quick Start)

### Cách 1: Khởi chạy 1-Click (Khuyên dùng trên Windows)
Mở PowerShell tại thư mục gốc và chạy:
```powershell
.\start_all.ps1
```
Script sẽ tự động:
1. Kiểm tra trạng thái cổng SQL Server `1433`.
2. Bật dịch vụ **AI Microservice (FastAPI)** tại `http://127.0.0.1:8000`.
3. Bật dịch vụ **Backend API (Spring Boot)** tại `http://localhost:5000`.
4. Bật giao diện **Frontend (React Vite)** tại `http://localhost:5173`.

---

### Cách 2: Khởi chạy thủ công từng dịch vụ

#### 1. Cơ sở dữ liệu (SQL Server)
- Kết nối tới `127.0.0.1:1433`
- Database: `EduMyDb`
- Tài khoản mẫu: `sa` / `YourPassword123`

#### 2. Dịch vụ AI (FastAPI)
```bash
cd Edumy/MLService
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```
- Swagger API Docs: `http://127.0.0.1:8000/docs`

#### 3. Backend (Spring Boot 3.2)
```bash
cd Edumy/Backend
.\mvnw.cmd spring-boot:run
```
- Base API URL: `http://localhost:5000/api`

#### 4. Frontend (React 18)
```bash
cd Edumy/Frontend
npm install
npm run dev
```
- Web Client URL: `http://localhost:5173`

---

## 🤖 Các Tính Năng AI Nổi Bật

1. **AI Curriculum & Syllabus Generation**: Tự động gợi ý dàn bài khóa học gồm các Section và bài học mẫu chi tiết ngay khi nhập tiêu đề.
2. **AI Category & Tag Suggestion**: Phân tích tự động nội dung khóa học để gắn nhãn danh mục chuẩn xác.
3. **AI Quiz Generator**: Tự động sinh bộ câu hỏi trắc nghiệm kèm đáp án đúng và giải thích theo từng chủ đề bài học.
4. **AI Personalized Path**: Đề xuất khóa học tiếp theo dựa trên lịch sử học tập và sở thích của từng học viên.

---

## 👥 Tài Khoản Mẫu Trải Nghiệm (Demo Accounts)

| Phân vai | Email đăng nhập | Mật khẩu | Chức năng chính |
| :--- | :--- | :--- | :--- |
| **Học viên** | `student@edumy.com` | `123456` | Khám phá khóa học, xem video, làm bài kiểm tra, theo dõi tiến độ |
| **Giảng viên** | `ádj@edumy.com` | `123456` | Quản lý khóa học, soạn giáo trình với AI, xem thống kê doanh thu |
| **Quản trị viên** | `admin@edumy.com` | `123456` | Duyệt khóa học, quản lý người dùng, thống kê toàn hệ thống |

---

## 🔄 Quy Trình CI/CD (Jenkins)
Dự án được cấu hình Pipeline chuẩn trong `Jenkinsfile` với các bước:
1. **Checkout SCM**: Lấy mã nguồn mới nhất từ nhánh `main`.
2. **Build Backend**: Biên dịch và chạy bộ kiểm thử Spring Boot với Maven Wrapper.
3. **Build Frontend**: Kiểm tra cú pháp và đóng gói bundle React bằng Vite.
4. **Test AI Service**: Xác thực các endpoint của FastAPI.
