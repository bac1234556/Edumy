# EDU-TSR-W-002 — BÁO CÁO TRẠNG THÁI KIỂM THỬ CHU KỲ C2 & C3 (TEST STATUS REPORT)

| Thuộc tính | Giá trị |
| :--- | :--- |
| **Mã tài liệu** | `EDU-TSR-W-002` |
| **Chu kỳ báo cáo** | **Chu kỳ C2** (`Edumy-main@836460a`) & **Chu kỳ C3** (`Edumy-main@a44cb7d`, gồm 7 TC UI trên Chrome 157) |
| **Thời điểm** | `2026-11-22` (Asia/Ho_Chi_Minh) |
| **Trạng thái tích lũy sau C3** | **129 Pass / 3 Fail / 2 Blocked / 0 Not Run** (Pass rate trên 134 TC: **96.27%**; trên 132 TC chạy được: **97.73%**) |
| **Đóng Defect** | **30/32 Closed (93.75%)** — **7/7 S1 Closed (100%)**, **17/18 S2 Closed** (1 S2 Open), **6/6 S3 Closed** (0 S3 Open), **1 S4 Deferred** |

## Tóm tắt tiến triển qua 3 chu kỳ

| Chu kỳ | Commit | Số lượt chạy | Pass | Fail | Blocked | Defect mới | Defect đóng |
| :---: | :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| **C1** | `Edumy-main@bf4ac6c` | 134 | 57 | 69 | 8 | 30 | 0 |
| **C2** | `Edumy-main@836460a` | 134 | 118 | 8 | 8 | 0 | 24 |
| **C3** | `Edumy-main@a44cb7d` | 65 | 62 | 2 | 1 | 2 | 6 |

- **Module đạt 100% Pass (`7/8 module`)**: `W-M01` (26/26), `W-M02` (10/10), `W-M03` (28/28), `W-M04` (18/18), `W-M05` (21/21), `W-M06` (9/9), **`W-REG` (3/3)**.
- **Hồi quy UI Chrome 157 (`RUN-W-C3-0128..0136`, commit `d83f0a0`)**: Toàn bộ **6/6 TC UI chạy trên Chrome 157 đều đạt PASS 100%** (`TC-W-REG-FN-001..002` tạo khóa 1 trang, `TC-W-M07-PERF-003` LCP `290–1016ms` & 0 lỗi console/4xx sau khi sửa `DEF-W-031`, `TC-W-M07-USA-001` responsive `360/768/1366px` & Việt hóa badge sau khi sửa `DEF-W-032`, `TC-W-M07-USA-002` validation & empty states, `TC-W-M07-USA-003` bàn phím & Lighthouse A11y `93/94/94 >= 90`).
- **Hiệu năng 50 CCU (`TC-W-M07-PERF-001`)**: `GET /api/courses` giảm từ `3431.2 ms` (C1) xuống `620.5 ms` (C2 — cải thiện **5.5x**), cả 4 API trọng điểm đều đạt `p95 <= 1800 ms` với `0` lỗi 5xx.
- **Email (`TC-W-M07-FN-001`)**: Mailpit nhận đủ **9/9 loại email**; 10x webhook song song chỉ gửi **1 email** duy nhất (`RUN-W-C3-0078`).
- **Còn lại**: Chỉ còn **1 lỗi S2 Open** (`DEF-W-027` secret mặc định môi trường dev trong `docker-compose.yml`/`application.yml`), **1 lỗi S4 Deferred** (`DEF-W-030` mã chết), và **2 TC Blocked** (`TC-W-M07-COMP-001` phần Edge/Firefox + `TC-W-M07-DEP-002` máy chủ Jenkins).

Xem báo cáo hoàn tất đầy đủ (9 bảng, 4 biểu đồ, khuyến nghị Go/No-Go) tại [`EDU-TCR-W-001_Test_Completion_Report.md`](EDU-TCR-W-001_Test_Completion_Report.md).