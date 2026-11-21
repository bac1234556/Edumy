# TIẾN ĐỘ KIỂM THỬ EDUMY WEB (QA PROGRESS TRACKER)

| Pha | Nội dung | Trạng thái | Kết quả / Sản phẩm chính |
| :--- | :--- | :---: | :--- |
| **Pha 1** | Lập kế hoạch & Danh mục yêu cầu | **HOÀN TẤT** | `EDU-TP-W-001_Test_Plan.md`, `requirements_inventory.csv` (182 yêu cầu) |
| **Pha 2** | Thiết kế & Sinh Test Case + RTM | **HOÀN TẤT** | `EDU-TDS-W-001_Test_Design.md`, `EDU-TCS-W-001_Test_Cases.xlsx` (8 sheet, 134 TC, 100% RTM) |
| **Pha 3** | Môi trường cô lập & Smoke Test | **HOÀN TẤT** | `docker-compose.test.yml` (6 containers `edumy-test`), `seed_and_baseline.mjs`, `smoke_test.mjs` (10/10 PASS), `EDU-TER-W-001_Test_Environment.md` |
| **Pha 4 (C1)** | Thực thi Chu kỳ C1 (`Edumy-main@bf4ac6c`) | **HOÀN TẤT** | 57 Pass / 69 Fail / 8 Blocked; phát hiện 30 Defect (7 S1, 17 S2, 5 S3, 1 S4); `EDU-TSR-W-001_Status_Report.md` |
| **Pha 5 (Fix + C2/C3)** | Sửa lỗi + Kiểm thử lại, Hồi quy API & UI Chrome 157 (`@836460a`, `@a44cb7d`) | **HOÀN TẤT** | 47/47 JUnit 5 PASS; C2/C3 + 7 TC UI nâng kết quả lên **127 Pass / 5 Fail / 2 Blocked** (**28/32 Defect Closed**, **7/7 S1 Closed**); `EDU-TSR-W-002_Status_Report.md` |
| **Pha 6** | Báo cáo hoàn tất & Chốt bàn giao | **HOÀN TẤT** | `EDU-TCR-W-001_Test_Completion_Report.md` (9 bảng + 4 biểu đồ PNG), `EDU-TIR-W-001_Defect_Report.md`, cập nhật `.xlsx` — **Quyết định: CONDITIONAL GO** |
