# HỆ THỐNG EDUMY ELEARNING PLATFORM
## BỘ HỒ SƠ PHÂN TÍCH THIẾT KẾ PHẦN MỀM & CƠ SỞ DỮ LIỆU
*(Software Requirements Specification & Database Architecture Documentation)*

---

## 1. GIỚI THIỆU BỘ TÀI LIỆU
Bộ tài liệu này được xây dựng theo chuẩn quy trình công nghệ phần mềm chuyên nghiệp (Software Development Life Cycle - SDLC), phục vụ quá trình phân tích, thiết kế và cài đặt cơ sở dữ liệu cho dự án **Edumy eLearning Platform**.

Tài liệu bao quát toàn bộ từ khâu **khảo sát nghiệp vụ, phân rã chức năng, mô hình hóa luồng công việc, thiết kế kiến trúc thực thể dữ liệu** cho đến **kịch bản mã nguồn SQL DDL** sẵn sàng thực thi để khởi tạo hệ thống trên Microsoft SQL Server.

---

## 2. CẤU TRÚC VÀ DANH MỤC TÀI LIỆU

Bộ hồ sơ gồm 6 tài liệu thành phần được tổ chức khoa học:

| STT | Tên tài liệu / Tệp tin | Nội dung chính |
| :---: | :--- | :--- |
| **01** | [`01_Tong_Quan_Va_Dac_Ta_Tac_Nhan.md`](./01_Tong_Quan_Va_Dac_Ta_Tac_Nhan.md) | Bối cảnh dự án, kiến trúc tổng thể hệ thống, chân dung các tác nhân (Actors) và ma trận phân quyền (RBAC Matrix). |
| **02** | [`02_Dac_Ta_Yeu_Cau_Nghiep_Vu_SRS.md`](./02_Dac_Ta_Yeu_Cau_Nghiep_Vu_SRS.md) | Đặc tả 10 phân hệ nghiệp vụ chính (FR-01 đến FR-28), danh mục Use Cases và sơ đồ ca sử dụng tổng quan (Mermaid Use Case Diagram). |
| **03** | [`03_Thiet_Ke_Luong_Nghiep_Vu.md`](./03_Thiet_Ke_Luong_Nghiep_Vu.md) | Mô hình hóa 5 luồng nghiệp vụ xương sống của hệ thống qua sơ đồ tuần tự (Sequence Diagrams) và lưu đồ (Flowcharts). |
| **04** | [`04_Mo_Hinh_Thuc_The_ERD.md`](./04_Mo_Hinh_Thuc_The_ERD.md) | Phân nhóm 30 thực thể và sơ đồ quan hệ thực thể tổng thể (Mermaid Entity Relationship Diagram - ERD) chuẩn 3NF. |
| **05** | [`05_Tu_Dien_Du_Lieu_Data_Dictionary.md`](./05_Tu_Dien_Du_Lieu_Data_Dictionary.md) | Từ điển dữ liệu chi tiết cho 30 bảng (Tên cột, kiểu dữ liệu, Nullable, khóa PK/FK/UQ, giá trị mặc định, ý nghĩa nghiệp vụ). |
| **06** | [`06_Script_Tao_Database_EduMyDb.sql`](./06_Script_Tao_Database_EduMyDb.sql) | Toàn bộ mã nguồn T-SQL DDL hoàn chỉnh: tạo CSDL `EduMyDb`, tạo 30 bảng, thiết lập ràng buộc toàn vẹn, đánh Index và nạp dữ liệu mẫu (Seed Data). |

---

## 3. HƯỚNG DẪN THỰC THI SCRIPT CƠ SỞ DỮ LIỆU
Để tạo cơ sở dữ liệu `EduMyDb` phục vụ chạy dự án:

1. **Yêu cầu môi trường**:
   - Microsoft SQL Server 2017 / 2019 / 2022 hoặc Azure SQL Edge / Express.
   - Công cụ quản trị: SQL Server Management Studio (SSMS) hoặc Azure Data Studio hoặc VS Code mssql extension.

2. **Cách thực hiện**:
   - Mở công cụ quản trị (SSMS).
   - Kết nối tới SQL Server Instance (ví dụ `localhost` hoặc `localhost\SQLEXPRESS`).
   - Mở tệp tin [`06_Script_Tao_Database_EduMyDb.sql`](./06_Script_Tao_Database_EduMyDb.sql).
   - Nhấn **Execute (F5)** để chạy toàn bộ kịch bản.

3. **Kiểm tra kết quả**:
   - Kiểm tra Database `EduMyDb` đã xuất hiện trong Object Explorer.
   - Kiểm tra đủ 30 bảng nghiệp vụ.
   - Dữ liệu mẫu đã được nạp sẵn: Tài khoản Admin (`admin@edumy.com`), Instructor (`instructor@edumy.com`), Student (`student@edumy.com`), danh mục khóa học và các thẻ phân loại mẫu.
