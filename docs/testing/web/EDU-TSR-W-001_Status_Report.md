# EDU-TSR-W-001 — BÁO CÁO TRẠNG THÁI KIỂM THỬ CHU KỲ C1 (TEST STATUS REPORT)

| Thuộc tính | Giá trị |
| :--- | :--- |
| **Mã tài liệu** | `EDU-TSR-W-001` |
| **Chu kỳ** | **Chu kỳ C1** (`2026-11-21`, Asia/Ho_Chi_Minh) |
| **Build kiểm thử** | `Edumy-Project@f2f5240` · `Edumy-main@bf4ac6c` |
| **Môi trường** | Stack Docker độc lập `edumy-test` (6 containers healthy/running) |
| **Kết quả tổng quan C1** | **57 Pass / 69 Fail / 8 Blocked / 0 Not Run** (Tổng **134 TC**) |
| **Execution rate / Pass rate** | **100.0%** đã thực thi · **42.54%** Pass rate · **30 Defect** (`S1`: 7, `S2`: 17, `S3`: 5, `S4`: 1) |
| **Khuyến nghị cuối C1** | **NO-GO (Chưa đạt Exit Criteria)** — Tồn tại **7 lỗi S1 Critical** (bypass xác thực webhook SePay, thanh toán đơn đã hủy, IDOR biên soạn, upload độc hại, Admin khóa Admin/tự khóa, JWT cũ còn hiệu lực sau khóa, lỗ hổng kiểm tra quyền Q&A/Quiz) và **17 lỗi S2 Major**. Đề nghị đội phát triển sửa lỗi S1/S2 trước khi chạy Chu kỳ C2. |

---

## Bảng 1 — Thống kê theo Module (`W-M01`..`W-M07`, `W-REG`)

| Module | Tổng TC | Đã thực thi | Pass | Fail | Blocked | Not Run | Số lần chạy | Số chu kỳ | Execution rate | Pass rate | First-time pass rate | Defect S1/S2/S3/S4 |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | :---: |
| W-M01 | 26 | 26 | 16 | 10 | 0 | 0 | 26 | 1 | 100.0% | 61.54% | 61.54% | 2/5/0/0 |
| W-M02 | 10 | 10 | 3 | 7 | 0 | 0 | 10 | 1 | 100.0% | 30.0% | 30.0% | 0/2/0/0 |
| W-M03 | 28 | 28 | 8 | 20 | 0 | 0 | 28 | 1 | 100.0% | 28.57% | 28.57% | 2/3/2/0 |
| W-M04 | 18 | 18 | 5 | 13 | 0 | 0 | 18 | 1 | 100.0% | 27.78% | 27.78% | 2/2/1/0 |
| W-M05 | 21 | 21 | 9 | 12 | 0 | 0 | 21 | 1 | 100.0% | 42.86% | 42.86% | 1/2/1/0 |
| W-M06 | 9 | 9 | 9 | 0 | 0 | 0 | 9 | 1 | 100.0% | 100.0% | 100.0% | 0/0/0/0 |
| W-M07 | 19 | 19 | 7 | 6 | 6 | 0 | 19 | 1 | 100.0% | 36.84% | 36.84% | 0/3/0/1 |
| W-REG | 3 | 3 | 0 | 1 | 2 | 0 | 3 | 1 | 100.0% | 0.0% | 0.0% | 0/0/1/0 |
| **Tổng** | **134** | **134** | **57** | **69** | **8** | **0** | **134** | 1 | 100.0% | 42.54% | 42.54% | 7/17/5/1 |

## Bảng 2 — Thống kê theo Loại kiểm thử

| Loại | Tổng TC | Đã thực thi | Pass | Fail | Blocked | Not Run | Số lần chạy | Execution rate | Pass rate | First-time pass rate |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| FN | 95 | 95 | 42 | 51 | 2 | 0 | 95 | 100.0% | 44.21% | 44.21% |
| SEC | 19 | 19 | 8 | 11 | 0 | 0 | 19 | 100.0% | 42.11% | 42.11% |
| PERF | 3 | 3 | 1 | 1 | 1 | 0 | 3 | 100.0% | 33.33% | 33.33% |
| REL | 4 | 4 | 1 | 3 | 0 | 0 | 4 | 100.0% | 25.0% | 25.0% |
| REC | 2 | 2 | 1 | 1 | 0 | 0 | 2 | 100.0% | 50.0% | 50.0% |
| OBS | 2 | 2 | 2 | 0 | 0 | 0 | 2 | 100.0% | 100.0% | 100.0% |
| USA | 3 | 3 | 0 | 0 | 3 | 0 | 3 | 100.0% | 0.0% | 0.0% |
| COMP | 1 | 1 | 0 | 0 | 1 | 0 | 1 | 100.0% | 0.0% | 0.0% |
| MNT | 3 | 3 | 1 | 2 | 0 | 0 | 3 | 100.0% | 33.33% | 33.33% |
| DEP | 2 | 2 | 1 | 0 | 1 | 0 | 2 | 100.0% | 50.0% | 50.0% |
| **Tổng** | **134** | **134** | **57** | **69** | **8** | **0** | **134** | 100.0% | 42.54% | 42.54% |

## Bảng 3 — Thống kê theo Chu kỳ

| Chu kỳ | TC chạy | Pass | Fail | Blocked | Defect mới | Defect đóng | Defect mở lại |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| **C1** | 134 | 57 | 69 | 8 | 30 | 0 | 0 |

## Bảng 6 — Độ phủ yêu cầu (`182/182 = 100%`)

| Nhóm yêu cầu | Tổng số | Số có TC | Độ phủ TC | Pass toàn bộ | Có TC Fail/Blocked |
| :--- | ---: | ---: | ---: | ---: | ---: |
| **UC** | 75 | 75 | 100.0% | 12 | 62 |
| **BR** | 41 | 41 | 100.0% | 5 | 36 |
| **AC** | 37 | 37 | 100.0% | 8 | 28 |
| **NFR** | 10 | 10 | 100.0% | 0 | 9 |
| **D** | 4 | 4 | 100.0% | 0 | 4 |
| **CODE** | 13 | 13 | 100.0% | 10 | 3 |
| **REG** | 2 | 2 | 100.0% | 0 | 1 |

## Bảng 7 — Kết quả đo lường Phi chức năng (NFR-01..NFR-10)

| NFR | Chỉ số | Ngưỡng | Kết quả đo thực tế (C1) | Đạt / Không đạt |
| :--- | :--- | :--- | :--- | :---: |
| **NFR-01** | Tỷ lệ truy cập trái phép trên ma trận IDOR (8 vai trò x 13 nhóm endpoint) | 0 truy cập trái phép (0%) | `TC-W-M01-SEC-001`=Pass; `TC-W-M01-SEC-002`=Fail; `TC-W-M03-SEC-001`=Fail; `TC-W-M04-SEC-001`=Pass; `TC-W-M05-SEC-001`=Fail; `TC-W-M06-SEC-001`=Pass | **Không đạt** |
| **NFR-02** | Độ mạnh BCrypt cost ($2a$XX$) & 0 lộ mật khẩu/token/số TK đầy đủ | BCrypt cost >= 12; 0 dữ liệu rõ | `TC-W-M01-SEC-003`=Fail; `TC-W-M01-SEC-005`=Pass; `TC-W-M06-SEC-002`=Pass; `TC-W-M07-SEC-002`=Fail | **Không đạt** |
| **NFR-03** | Khóa tạm đăng nhập sai lần 6/15 phút & chặn tra cứu chứng chỉ lần 31/phút | 429/Lock ở lần 6 (login) & lần 31 (verify) | `TC-W-M01-SEC-004`=Fail; `TC-W-M05-SEC-002`=Fail | **Không đạt** |
| **NFR-04** | Idempotency webhook 10x & toàn vẹn giao dịch khi crash/concurrent/double-click | 10 webhook -> 1 Enrollment & 1 Earning; 0 nửa vời | `TC-W-M04-REL-001`=Fail; `TC-W-M04-REL-002`=Fail; `TC-W-M06-REL-001`=Pass | **Không đạt** |
| **NFR-05** | Hiệu năng 50 CCU (3 lượt sau warm-up): p95 API, tạo PDF chứng chỉ, tạo khóa 5x3, Lighthouse | p95 API <= 3s; PDF <= 5s; ghi rõ LCP/TBT | `TC-W-M07-PERF-001`=Fail; `TC-W-M07-PERF-002`=Pass; `TC-W-M07-PERF-003`=Blocked | **Không đạt** |
| **NFR-06** | Khôi phục CSDL container tạm (RTO) & lịch sao lưu hằng ngày (RPO) | RTO <= 4h, khớp 100% row count; RPO <= 24h (có lịch tự động) | `TC-W-M07-REC-001`=Pass; `TC-W-M07-REC-002`=Fail | **Không đạt** |
| **NFR-07** | Khả dụng: Responsive 360/768/1366px, tiếng Việt, lỗi rõ trường, bàn phím, Lighthouse A11y | 0 vỡ layout; 100% tiếng Việt; A11y >= 90 | `TC-W-M07-USA-001`=Blocked; `TC-W-M07-USA-002`=Blocked; `TC-W-M07-USA-003`=Blocked | **Blocked** |
| **NFR-08** | Observability: truy vết 1 đơn qua log không lộ secret; Actuator/Prometheus/Grafana hoạt động | Truy đủ vòng đời đơn; 0 secret trong log; 200 UP & scrape OK | `TC-W-M07-OBS-001`=Pass; `TC-W-M07-OBS-002`=Pass; `TC-W-M07-SEC-001`=Fail | **Không đạt** |
| **NFR-09** | Tự động đóng QR & hủy đơn chờ thanh toán quá hạn (Scheduled job) | Đơn quá hạn tự chuyển EXPIRED/CANCELLED đúng hạn | `TC-W-M04-REL-003`=Fail; `TC-W-M04-FN-010`=Pass | **Không đạt** |
| **NFR-10** | Độ phủ yêu cầu, Unit/Modularity/JaCoCo, Lint/Build, 3 trình duyệt, Deploy Docker sạch | Req cov 100%; mvnw verify & npm build PASS; 6/6 container healthy | `TC-W-M07-COMP-001`=Blocked; `TC-W-M07-MNT-001`=Pass; `TC-W-M07-MNT-002`=Fail; `TC-W-M07-DEP-001`=Pass; `TC-W-M07-DEP-002`=Blocked | **Không đạt** |

## Bảng 9 — JaCoCo Code Coverage theo Module Backend (`43/43 tests PASS`)

| Package Module (`com.edumy.*`) | Số lớp | Line Covered / Total | Line % | Branch Covered / Total | Branch % |
| :--- | ---: | ---: | ---: | ---: | ---: |
| `authoring` | 66 | 183 / 1376 | 13.3% | 70 / 886 | 7.9% |
| `catalog` | 10 | 0 / 104 | 0.0% | 0 / 56 | 0.0% |
| `commerce` | 57 | 64 / 872 | 7.34% | 38 / 528 | 7.2% |
| `identity` | 64 | 101 / 984 | 10.26% | 34 / 518 | 6.56% |
| `learning` | 93 | 0 / 1323 | 0.0% | 0 / 648 | 0.0% |
| `notification` | 6 | 1 / 206 | 0.49% | 0 / 24 | 0.0% |
| `other` | 1 | 0 / 3 | 0.0% | 0 / 0 | 0.0% |
| `payout` | 38 | 170 / 355 | 47.89% | 56 / 164 | 34.15% |
| `shared` | 23 | 109 / 604 | 18.05% | 34 / 238 | 14.29% |
| **TỔNG** | **358** | **628 / 5827** | **10.78%** | **232 / 3062** | **7.58%** |

## Kết luận kiểm chứng 7 điểm nghi vấn mục 3.2 (bằng thực thi thật)

1. **`PUT /courses/{id}/status` set `PendingApproval` bỏ qua BR-07**: **ĐÚNG (Xác nhận lỗi — `DEF-W-013`)** — trả HTTP 200 với khóa rỗng (`RUN-W-C1-0049`).
2. **`POST /courses` thiếu `categoryId` tự gán danh mục 1**: **ĐÚNG (Xác nhận lỗi — `DEF-W-014`)** — trả 201 và gán `categoryId=1` cho cả thiếu/`null`/`0` (`RUN-W-C1-0038`).
3. **Bất nhất quy tắc giá tạo khóa vs gửi duyệt**: **ĐÚNG (Xác nhận lỗi — `DEF-W-015`)** — tạo khóa nhận `-1000, 1, 9999, 10500` (201), gửi duyệt mới chặn 400 (`RUN-W-C1-0040`).
4. **NFR-02 BCrypt cost >= 12**: **KHÔNG ĐẠT (Xác nhận lỗi — `DEF-W-008`)** — DB lưu `$2a$10$` (cost=10) và `reset_token` lưu dạng rõ 8 ký tự (`RUN-W-C1-0023`).
5. **NFR-03 Khóa tạm login sai lần 6 & rate-limit verify chứng chỉ lần 31**: **KHÔNG ĐẠT (Xác nhận lỗi — `DEF-W-009`)** — không chặn lần login thứ 6 (lần 7 vẫn 200) và cả 31 lần verify chứng chỉ đều 200 (`RUN-W-C1-0024`, `RUN-W-C1-0102`).
6. **UC-S16 / NFR-09 `@Scheduled` tự hủy đơn & đóng QR quá hạn**: **KHÔNG CÓ (Xác nhận Not Implemented — `DEF-W-018`)** — 0 `@Scheduled` trong mã nguồn, đơn quá hạn 48h không tự hủy (`RUN-W-C1-0080`).
7. **JWT thời hạn (`access-ttl=60m`, `expire-days=7d`)**: **ĐẠT kiểm chữ ký/alg:none (`RUN-W-C1-0025`), nhưng phát hiện lỗi S1 `DEF-W-006`**: JWT cũ vẫn dùng được suốt 60 phút sau khi Admin khóa tài khoản (`RUN-W-C1-0014`).

## Danh sách 8 TC bị BLOCKED (Minh bạch lý do)

- `TC-W-M07-DEP-002`: Các lệnh cục bộ trong `Jenkinsfile` (`mvnw verify`, `npm lint/build`, `docker compose build`) đều PASS; chỉ phần chạy trên máy chủ Jenkins CI bị **BLOCKED** do không có Jenkins Server cục bộ.
- 7 TC UI trình duyệt (`TC-W-M07-PERF-003`, `TC-W-M07-USA-001..003`, `TC-W-M07-COMP-001`, `TC-W-REG-FN-001..002`): **BLOCKED** do trình duyệt Chrome trên máy host chưa bật Remote Debugging (`DevToolsActivePort ENOENT`) và Mozilla Firefox chưa được cài đặt. Khi bạn bật `chrome://inspect/#remote-debugging`, QA sẽ chạy bổ sung 7 TC này ngay trong C2.

## Biểu đồ thống kê C1 (`charts/`)
- `charts/chart1_results_by_module.png` — Pass/Fail/Blocked theo module
- `charts/chart2_defects_by_severity.png` — Phân bố 30 defect theo Severity (S1..S4)
- `charts/chart3_trend_by_cycle.png` — Xu hướng theo chu kỳ
- `charts/chart4_performance_p95.png` — Hiệu năng p95 tại 50 CCU (NFR-05)