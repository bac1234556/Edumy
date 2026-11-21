# EDU-TSR-W-002 — BÁO CÁO TRẠNG THÁI KIỂM THỬ CHU KỲ C2 & C3 (TEST STATUS REPORT)

| Thuộc tính | Giá trị |
| :--- | :--- |
| **Mã tài liệu** | `EDU-TSR-W-002` |
| **Chu kỳ báo cáo** | **Chu kỳ C2** (`Edumy-main@836460a`) & **Chu kỳ C3** (`Edumy-main@a44cb7d`, gồm 7 TC UI trên Chrome 157) |
| **Thời điểm** | `2026-11-22` (Asia/Ho_Chi_Minh) |
| **Trạng thái tích lũy sau C3** | **127 Pass / 5 Fail / 2 Blocked / 0 Not Run** (Pass rate trên 134 TC: **94.78%**; trên 132 TC chạy được: **96.21%**) |
| **Đóng Defect** | **28/32 Closed (87.5%)** — **7/7 S1 Closed (100%)**, **16/18 S2 Closed** (2 S2 Open), **5/6 S3 Closed** (1 S3 Open), **1 S4 Deferred** |

## Tóm tắt tiến triển qua 3 chu kỳ

| Chu kỳ | Commit | Số lượt chạy | Pass | Fail | Blocked | Defect mới | Defect đóng |
| :---: | :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| **C1** | `Edumy-main@bf4ac6c` | 134 | 57 | 69 | 8 | 30 | 0 |
| **C2** | `Edumy-main@836460a` | 134 | 118 | 8 | 8 | 0 | 24 |
| **C3** | `Edumy-main@a44cb7d` | 63 | 60 | 2 | 1 | 2 | 4 |

- **Module đạt 100% Pass (`7/8 module`)**: `W-M01` (26/26), `W-M02` (10/10), `W-M03` (28/28), `W-M04` (18/18), `W-M05` (21/21), `W-M06` (9/9), **`W-REG` (3/3)**.
- **Hồi quy UI Chrome 157 (`RUN-W-C3-0128..0134`)**: `TC-W-REG-FN-001..002` (tạo khóa 1 trang `CourseCreate.jsx` + partial failure + double-submit), `TC-W-M07-USA-002` (validation & empty/error states), và `TC-W-M07-USA-003` (bàn phím + Lighthouse A11y `93/94/94 >= 90`) đều **PASS**; phát hiện 2 lỗi giao diện thật: **`DEF-W-031` (S3)** (`/courses/49` gọi trái quyền `/co-instructors` trả 403 trên console cho học viên & video mẫu bị ORB) và **`DEF-W-032` (S2)** (tràn ngang mobile 360px tại `/cart` & `/my-courses/{id}/learn`, cắt tab Q&A tại 768px, còn vài nhãn tiếng Anh).
- **Hiệu năng 50 CCU (`TC-W-M07-PERF-001`)**: `GET /api/courses` giảm từ `3431.2 ms` (C1) xuống `620.5 ms` (C2 — cải thiện **5.5x**), cả 4 API trọng điểm đều đạt `p95 <= 1800 ms` với `0` lỗi 5xx.
- **Email (`TC-W-M07-FN-001`)**: Mailpit nhận đủ **9/9 loại email**; 10x webhook song song chỉ gửi **1 email** duy nhất (`RUN-W-C3-0078`).
- **Còn lại**: 2 lỗi S2 Open (`DEF-W-027` secret mặc định dev & `DEF-W-032` responsive `/cart` + `/learn`), 1 lỗi S3 Open (`DEF-W-031` console 403 trên `/courses/49`), 1 lỗi S4 Deferred (`DEF-W-030`), và **2 TC Blocked** (`TC-W-M07-COMP-001` phần Edge/Firefox + `TC-W-M07-DEP-002` máy chủ Jenkins).

Xem báo cáo hoàn tất đầy đủ (9 bảng, 4 biểu đồ, khuyến nghị Go/No-Go) tại [`EDU-TCR-W-001_Test_Completion_Report.md`](EDU-TCR-W-001_Test_Completion_Report.md).