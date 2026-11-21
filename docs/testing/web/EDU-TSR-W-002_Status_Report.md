# EDU-TSR-W-002 — BÁO CÁO TRẠNG THÁI KIỂM THỬ CHU KỲ C2 & C3 (TEST STATUS REPORT)

| Thuộc tính | Giá trị |
| :--- | :--- |
| **Mã tài liệu** | `EDU-TSR-W-002` |
| **Chu kỳ báo cáo** | **Chu kỳ C2** (`Edumy-main@836460a`) & **Chu kỳ C3** (`Edumy-main@a44cb7d`) |
| **Thời điểm** | `2026-11-21` (Asia/Ho_Chi_Minh) |
| **Trạng thái tích lũy sau C3** | **123 Pass / 3 Fail / 8 Blocked / 0 Not Run** (Pass rate trên 134 TC: **91.79%**; trên 126 TC chạy được: **97.62%**) |
| **Đóng Defect** | **28/30 Closed (93.33%)** — **7/7 S1 Closed (100%)**, **16/17 S2 Closed**, **5/5 S3 Closed (100%)**, **1 S4 Deferred** |

## Tóm tắt tiến triển qua 3 chu kỳ

| Chu kỳ | Commit | Số lượt chạy | Pass | Fail | Blocked | Defect mới | Defect đóng |
| :---: | :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| **C1** | `Edumy-main@bf4ac6c` | 134 | 57 | 69 | 8 | 30 | 0 |
| **C2** | `Edumy-main@836460a` | 134 | 118 | 8 | 8 | 0 | 24 |
| **C3** | `Edumy-main@a44cb7d` | 56 | 56 | 0 | 0 | 0 | 4 |

- **Module đạt 100% Pass**: `W-M01` (26/26), `W-M02` (10/10), `W-M03` (28/28), `W-M04` (18/18), `W-M05` (21/21), `W-M06` (9/9).
- **Hiệu năng 50 CCU (`TC-W-M07-PERF-001`)**: `GET /api/courses` giảm từ `3431.2 ms` (C1) xuống `620.5 ms` (C2 — cải thiện **5.5x**), cả 4 API trọng điểm đều đạt `p95 <= 1800 ms` với `0` lỗi 5xx.
- **Email (`TC-W-M07-FN-001`)**: Mailpit nhận đủ **9/9 loại email**; 10x webhook song song chỉ gửi **1 email** duy nhất (`RUN-W-C3-0078`).
- **Còn lại**: 1 lỗi S2 cấu hình bảo mật môi trường dev (`DEF-W-027` — Nginx headers đã sửa xong, còn fallback secret trong `docker-compose.yml`/`application.yml`), 1 lỗi S4 mã chết (`DEF-W-030`), và 8 TC Blocked (7 UI trình duyệt do Chrome `DevToolsActivePort ENOENT` + 1 Jenkins Server).

Xem báo cáo hoàn tất đầy đủ (9 bảng, 4 biểu đồ, khuyến nghị Go/No-Go) tại [`EDU-TCR-W-001_Test_Completion_Report.md`](EDU-TCR-W-001_Test_Completion_Report.md).