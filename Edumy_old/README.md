# EduMy System

## Yêu cầu môi trường
- .NET 10.0 SDK
- Python 3.11+ (cho ML Service)
- SQL Server
- Docker & Docker Compose

## Cấu trúc thư mục
- `Backend/`: ASP.NET Core Web API (Xử lý Auth, Khóa học, Mua bán, Thanh toán, Tích hợp ML).
- `Frontend/`: React Client (Giao diện người dùng).
- `MLService/`: FastAPI Machine Learning Service (Phân tích văn bản, gợi ý, phân loại).

## Hướng dẫn chạy dự án với Docker (Khuyên dùng)
Dự án được cấu hình sẵn với Docker Compose bao gồm SQL Server, Backend và MLService.

1. Chạy lệnh:
   ```bash
   docker-compose up -d --build
   ```
2. API Backend sẽ chạy ở: `http://localhost:5000`
3. ML Service sẽ chạy ở: `http://localhost:8000`
4. SQL Server chạy ở port `1433`.

## Hướng dẫn chạy dự án thủ công

### Cấu hình SQL Server
Trong thư mục `Backend`, tạo file `appsettings.Development.json` (hoặc sửa `appsettings.json`) và thay đổi chuỗi kết nối:
```json
"ConnectionStrings": {
  "DefaultConnection": "Server=localhost;Database=EduMyDb;User Id=sa;Password=YourPassword123!;TrustServerCertificate=True;"
}
```

### Chạy Backend
1. Cài đặt các công cụ EF Core nếu chưa có:
   ```bash
   dotnet tool install --global dotnet-ef --version 10.0.10
   ```
2. Chạy migration để tạo bảng:
   ```bash
   cd Backend
   dotnet ef database update
   ```
   *(Lưu ý: Nếu gặp lỗi chặn `An Application Control policy has blocked this file. (0x800711C7)`, bạn cần cấu hình lại chính sách bảo mật Windows hoặc dùng máy ảo / Docker).*
3. Khởi động server:
   ```bash
   dotnet run
   ```

### Chạy ML Service
1. Di chuyển vào thư mục MLService:
   ```bash
   cd MLService
   ```
2. Cài đặt thư viện:
   ```bash
   pip install fastapi uvicorn pydantic
   ```
3. Chạy server:
   ```bash
   uvicorn main:app --reload
   ```

### Chạy Frontend
1. Di chuyển vào thư mục Frontend:
   ```bash
   cd Frontend
   ```
2. Cài đặt dependencies:
   ```bash
   npm install
   ```
3. Chạy ứng dụng:
   ```bash
   npm start
   ```

## Tài khoản Seed Data
Sau khi chạy thành công, hệ thống sẽ tự động tạo các tài khoản mẫu:
- **Admin**: admin@edumy.com / Admin@123
- **Instructor**: instructor@edumy.com / Instructor@123
- **Student**: student@edumy.com / Student@123
