# EDU-TIR-W-001 — BÁO CÁO LỖI HỆ THỐNG EDUMY WEB (TEST INCIDENT / DEFECT REPORT)

| Thuộc tính | Giá trị |
| :--- | :--- |
| **Mã tài liệu** | `EDU-TIR-W-001` (Cập nhật cuối Chu kỳ C3) |
| **Các chu kỳ** | `C1` (`Edumy-main@bf4ac6c`) → `C2` (`Edumy-main@836460a`) → `C3` (`Edumy-main@a44cb7d`) |
| **Thời điểm cập nhật** | `2026-11-21` (Asia/Ho_Chi_Minh) |
| **Tổng số Defect** | **32** (`S1`: 7 · `S2`: 18 · `S3`: 6 · `S4`: 1) |
| **Trạng thái cuối C3** | **Closed: 28/32 (87.5%)** · **Open/Deferred: 4** (`S1 Open`: **0**, `S2 Open`: **2**, `S3 Open`: **1**, `S4 Deferred`: **1**) |

## 1. Bảng tổng hợp Defect theo Severity & Trạng thái đóng

| Severity | Tổng phát hiện (C1) | Đã đóng (C2/C3) | Còn mở / Hoãn | Tỷ lệ đóng |
| :---: | ---: | ---: | ---: | ---: |
| **S1** | 7 | 7 | 0 | 100.0% |
| **S2** | 18 | 16 | 2 | 88.89% |
| **S3** | 6 | 5 | 1 | 83.33% |
| **S4** | 1 | 0 | 1 | 0.0% |
| **Tổng** | **32** | **28** | **4** | **87.5%** |

## 2. Phân bố Defect theo Loại lỗi

| Loại lỗi | Số lượng |
| :--- | ---: |
| Functional | 15 |
| Security | 11 |
| Spec Mismatch | 3 |
| Not Implemented | 1 |
| Performance | 1 |
| Usability | 1 |

## 3. Danh sách chi tiết toàn bộ Defect (`DEF-W-001` .. `DEF-W-032`)

| Defect ID | Severity | Module | Loại lỗi | Trạng thái | Chu kỳ đóng | Commit sửa | TC liên quan | Tóm tắt & Bằng chứng xác nhận |
| :--- | :---: | :---: | :--- | :---: | :---: | :---: | :--- | :--- |
| **DEF-W-001** | **S1** | W-M04 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M04-FN-007` | SePay Webhook bỏ qua xác thực khi thiếu header Authorization/X-Api-Key — cho phép kích hoạt khóa học miễn phí trái phép ([evidence](evidence/RUN-W-C3-0071_TC-W-M04-FN-007.json)) |
| **DEF-W-002** | **S1** | W-M04 | Functional | **Closed** | C2 | `Edumy-main@836460a` | `TC-W-M04-FN-008, TC-W-M04-SEC-002` | Webhook thanh toán đến muộn trên đơn hàng đã hủy (CANCELLED) vẫn tự động kích hoạt khóa học và không đánh dấu chờ đối soát (needs_review) ([evidence](evidence/RUN-W-C3-0072_TC-W-M04-FN-008.json)) |
| **DEF-W-003** | **S1** | W-M03 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M03-SEC-001, TC-W-M03-FN-022` | Lỗ hổng IDOR nghiêm trọng trên các endpoint biên soạn (submit, withdraw, updateLesson, invite/remove co-instructor) trong SpecUseCaseController ([evidence](evidence/RUN-W-C3-0062_TC-W-M03-SEC-001.json)) |
| **DEF-W-004** | **S1** | W-M03 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M03-SEC-002, TC-W-M03-FN-010` | Lỗ hổng Upload tệp độc hại: /api/media/upload chấp nhận shell script (.sh) và /api/uploads/image chấp nhận tệp HTML giả mạo đuôi .jpg; path traversal gây 500 ([evidence](evidence/RUN-W-C3-0063_TC-W-M03-SEC-002.json)) |
| **DEF-W-005** | **S1** | W-M01 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M01-FN-015` | Admin có thể khóa tài khoản Admin khác (ADM2) qua /lock & /toggle-status và tự khóa chính mình qua /toggle-status (vi phạm AC-37) ([evidence](evidence/RUN-W-C2-0015_TC-W-M01-FN-015.json)) |
| **DEF-W-006** | **S1** | W-M01 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M01-FN-014` | Tài khoản bị khóa vẫn sử dụng được JWT Access Token cũ cho đến khi hết hạn (60 phút), chưa chấm dứt phiên ngay lập tức (vi phạm AC-09/BR-03) ([evidence](evidence/RUN-W-C2-0014_TC-W-M01-FN-014.json)) |
| **DEF-W-007** | **S1** | W-M05 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M05-FN-009, TC-W-M05-FN-010, TC-W-M05-FN-015, TC-W-M05-FN-017, TC-W-M05-SEC-001, TC-W-M05-FN-007` | Thiếu kiểm tra ghi danh/quyền trên Q&A, Quiz, Đánh giá & hoàn thành bài học — cho phép người chưa mua hoặc GV khóa khác tương tác (vi phạm AC-32, AC-33, NFR-01) ([evidence](evidence/RUN-W-C2-0091_TC-W-M05-FN-009.json)) |
| **DEF-W-008** | **S2** | W-M01 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M01-SEC-003` | BCrypt cost factor thực tế là 10 (yêu cầu NFR-02 >= 12) và ResetToken lưu dạng rõ 8 ký tự trong CSDL (Điểm nghi vấn 3.2) ([evidence](evidence/RUN-W-C2-0023_TC-W-M01-SEC-003.json)) |
| **DEF-W-009** | **S2** | W-M01 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M01-SEC-004, TC-W-M05-SEC-002` | Không có cơ chế khóa tạm chống brute-force ở lần đăng nhập sai thứ 6/15 phút và không giới hạn tra cứu chứng chỉ lần 31/phút (Điểm nghi vấn 3.2, NFR-03) ([evidence](evidence/RUN-W-C2-0024_TC-W-M01-SEC-004.json)) |
| **DEF-W-010** | **S2** | W-M01 | Functional | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M01-FN-012` | Giảng viên bị Admin thu hồi quyền vẫn tự kích hoạt lại vai trò Giảng viên qua POST /api/users/become-instructor (vi phạm AC-26/BR-05) ([evidence](evidence/RUN-W-C2-0012_TC-W-M01-FN-012.json)) |
| **DEF-W-011** | **S2** | W-M01 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M01-FN-008` | Quên mật khẩu trả 2 thông báo khác nhau giữa email tồn tại và không tồn tại (rò rỉ thông tin tài khoản, vi phạm AC-25) ([evidence](evidence/RUN-W-C2-0008_TC-W-M01-FN-008.json)) |
| **DEF-W-012** | **S2** | W-M01 | Functional | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M01-FN-002` | Đăng ký Web (/api/v1/auth/register) thiếu @Valid — chấp nhận mật khẩu 3 ký tự ('123') và email sai định dạng ('abc@') ([evidence](evidence/RUN-W-C2-0002_TC-W-M01-FN-002.json)) |
| **DEF-W-013** | **S2** | W-M03 | Security | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M03-FN-013` | PUT /api/courses/{id}/status cho phép giảng viên set 'PendingApproval' bỏ qua hoàn toàn checklist BR-07 (Điểm nghi vấn 3.2) ([evidence](evidence/RUN-W-C3-0049_TC-W-M03-FN-013.json)) |
| **DEF-W-014** | **S2** | W-M03 | Spec Mismatch | **Closed** | C2 | `Edumy-main@836460a` | `TC-W-M03-FN-002, TC-W-M03-FN-003` | POST /api/courses thiếu/null/0 categoryId tự fallback gán categoryId=1; categoryId không tồn tại gây HTTP 500 (Điểm nghi vấn 3.2) ([evidence](evidence/RUN-W-C3-0038_TC-W-M03-FN-002.json)) |
| **DEF-W-015** | **S3** | W-M03 | Spec Mismatch | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M03-FN-004, TC-W-M03-FN-012` | Bất nhất quy tắc giá khóa học giữa tạo khóa và gửi duyệt (Điểm nghi vấn 3.2, BR-08) ([evidence](evidence/RUN-W-C3-0040_TC-W-M03-FN-004.json)) |
| **DEF-W-016** | **S2** | W-M03 | Functional | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M03-FN-006, TC-W-M03-FN-007, TC-W-M03-FN-024` | Vi phạm vòng đời khóa học: cho sửa khi PendingApproval (AC-27), xóa khóa Published/Hidden gây 500 (AC-31), cho withdraw khóa Published và unhide khóa Draft (D-13) ([evidence](evidence/RUN-W-C3-0042_TC-W-M03-FN-006.json)) |
| **DEF-W-017** | **S2** | W-M07 | Functional | **Closed** | C3 | `Edumy-main@a44cb7d` | `TC-W-M07-FN-001, TC-W-M01-FN-011, TC-W-M03-FN-014, TC-W-M03-FN-017, TC-W-M03-FN-018, TC-W-M04-FN-006, TC-W-M04-REL-001` | 4/9 loại email giao dịch không được gửi vào Mailpit (đổi mật khẩu, từ chối khóa, ẩn khóa, xác nhận mua hàng), lỗi font UTF-8 cột reviewNote và trùng email khi webhook gửi song song ([evidence](evidence/RUN-W-C3-0078_TC-W-M04-REL-001.json)) |
| **DEF-W-018** | **S2** | W-M04 | Not Implemented | **Closed** | C3 | `Edumy-main@a44cb7d` | `TC-W-M03-FN-015, TC-W-M03-FN-019, TC-W-M03-FN-023, TC-W-M04-FN-009, TC-W-M04-REL-003, TC-W-M07-REC-002` | Chức năng chưa hiện thực theo SRS: Đồng giảng viên rời khóa (UC-I18), Ngừng sử dụng danh mục đã có khóa (D-21), Đối soát thủ công (UC-A18), Bộ hẹn giờ tự hủy đơn/QR quá hạn (UC-S16/NFR-09), Lịch sao lưu hằng ngày (NFR-06 RPO 24h) ([evidence](evidence/RUN-W-C3-0073_TC-W-M04-FN-009.json)) |
| **DEF-W-019** | **S2** | W-M02 | Functional | **Closed** | C3 | `Edumy-main@a44cb7d` | `TC-W-M02-FN-001, TC-W-M02-FN-002, TC-W-M02-FN-003` | Danh mục công khai (/api/courses) rò rỉ khóa trạng thái NeedsReview (vi phạm D-08), pageSize mặc định=12 thay vì 20, tìm không dấu D-09 trả rỗng và GET /api/courses/{hidden|draft} trả 200 ([evidence](evidence/RUN-W-C3-0027_TC-W-M02-FN-001.json)) |
| **DEF-W-020** | **S2** | W-M02 | Functional | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M02-FN-006, TC-W-M02-FN-007, TC-W-M02-FN-008, TC-W-M02-FN-009` | Lỗi HTTP 500 trên /api/v1/lessons/{id}/access và /api/v1/courses/recommend; /api/tags chặn khách 401; preview khóa Hidden trả 200 ([evidence](evidence/RUN-W-C3-0032_TC-W-M02-FN-006.json)) |
| **DEF-W-021** | **S2** | W-M04 | Functional | **Closed** | C3 | `Edumy-main@a44cb7d` | `TC-W-M04-FN-001, TC-W-M04-FN-003, TC-W-M04-FN-004, TC-W-M04-REL-002` | Giỏ hàng & Đơn hàng: cho thêm trùng & thêm khóa miễn phí vào giỏ (AC-29), cho phép >1 đơn Pending đồng thời (BR-29), double-click checkout tạo 2 đơn và checkout giỏ trống trả 200 ([evidence](evidence/RUN-W-C3-0079_TC-W-M04-REL-002.json)) |
| **DEF-W-022** | **S3** | W-M04 | Functional | **Closed** | C2 | `Edumy-main@836460a` | `TC-W-M04-FN-002, TC-W-M04-FN-012` | Coupon không kiểm tra trạng thái isActive=false khi tạo/validate và cho Admin tạo coupon % giảm giá 0% hoặc 101% ([evidence](evidence/RUN-W-C3-0066_TC-W-M04-FN-002.json)) |
| **DEF-W-023** | **S3** | W-M05 | Spec Mismatch | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M05-FN-002` | Tiến độ học tập làm tròn lên (Math.round: 2/3 = 67%) thay vì làm tròn xuống (floor: 66%) theo BR-32 ([evidence](evidence/RUN-W-C2-0084_TC-W-M05-FN-002.json)) |
| **DEF-W-024** | **S2** | W-M05 | Functional | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M05-FN-005, TC-W-M05-FN-006` | Chứng chỉ không lưu snapshot tên khóa học (vi phạm AC-35) và xác minh mã chứng chỉ sai trả HTTP 200 thay vì 404 (AC-36) ([evidence](evidence/RUN-W-C2-0087_TC-W-M05-FN-005.json)) |
| **DEF-W-025** | **S2** | W-M05 | Functional | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M05-FN-008` | Đánh giá bị Admin ẩn vẫn còn tồn tại trong averageRating (không trừ ra khỏi điểm TB) và cho phép sửa đánh giá đã ẩn / lỗi sửa đánh giá hợp lệ ([evidence](evidence/RUN-W-C2-0090_TC-W-M05-FN-008.json)) |
| **DEF-W-026** | **S2** | W-M07 | Performance | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-M07-PERF-001` | Hiệu năng GET /api/courses tại 50 CCU vượt ngưỡng NFR-05 (p95 = 3431.2 ms > 3000 ms do load toàn bảng findAll() và N+1 lazy loading) ([evidence](evidence/RUN-W-C2-0117_TC-W-M07-PERF-001.json)) |
| **DEF-W-027** | **S2** | W-M07 | Security | **Open** | — | `Edumy-main@098233f (partial: Nginx headers)` | `TC-W-M07-SEC-001, TC-W-M07-SEC-002` | Cấu hình bảo mật OWASP A05: Nginx thiếu HTTP security headers (ĐÃ SỬA C2) & còn mật khẩu/secret mặc định cho môi trường dev trong docker-compose.yml / application.yml ([evidence](evidence/RUN-W-C2-0115_TC-W-M07-SEC-001.json)) |
| **DEF-W-028** | **S3** | W-REG | Functional | **Closed** | C2 | `Edumy-main@098233f` | `TC-W-REG-MNT-001` | Còn sót endpoint AI/ML cũ GET /api/v1/admin/ml-monitoring trong AdminController.java:352 trả HTTP 200 ([evidence](evidence/RUN-W-C2-0127_TC-W-REG-MNT-001.json)) |
| **DEF-W-029** | **S3** | W-M03 | Functional | **Closed** | C2 | `Edumy-main@836460a` | `TC-W-M03-FN-008, TC-W-M03-FN-011, TC-W-M03-FN-021, TC-W-M01-FN-005, TC-W-M01-SEC-002, TC-W-M05-SEC-003` | Thiếu kiểm tra validation dữ liệu rỗng khi tạo chương học (CourseSection), tạo Tag, tạo Quiz sai sectionId (500), Web revoke-token trả 400 và đánh giá khóa COMPLETED bị chặn ([evidence](evidence/RUN-W-C3-0044_TC-W-M03-FN-008.json)) |
| **DEF-W-030** | **S4** | W-M07 | Functional | **Deferred** | — | — | `TC-W-M07-MNT-002` | Mã chết / trang chưa gắn route: Wishlist.jsx & MockPaymentGateway.jsx chưa được nối trong App.jsx; LessonController.java & QuizController.java rỗng ([evidence](evidence/RUN-W-C2-0124_TC-W-M07-MNT-002.json)) |
| **DEF-W-031** | **S3** | W-M07 | Functional | **Open** | — | — | `TC-W-M07-PERF-003` | Trang chi tiết khóa học (/courses/{id}) gọi trái quyền API giảng viên /co-instructors gây lỗi HTTP 403 trên console cho học viên; video mẫu BigBuckBunny.mp4 bị Chrome chặn ORB ([evidence](evidence/RUN-W-C3-0130_TC-W-M07-PERF-003.json)) |
| **DEF-W-032** | **S2** | W-M07 | Usability | **Open** | — | — | `TC-W-M07-USA-001` | Lỗi Responsive nghiêm trọng trên Mobile 360px & Tablet 768px: /cart tràn ngang cắt giá & nút thanh toán; /my-courses/{id}/learn mở sẵn sidebar đè player và cắt tab Hỏi & Đáp (Q&A); còn nhãn tiếng Anh chưa Việt hóa ([evidence](evidence/RUN-W-C3-0131_TC-W-M07-USA-001.json)) |

## 4. Chi tiết các Defect còn Mở / Hoãn (Residual Defects)

### DEF-W-027 [S2 / Open / Security] — Cấu hình bảo mật OWASP A05: Nginx thiếu HTTP security headers (ĐÃ SỬA C2) & còn mật khẩu/secret mặc định cho môi trường dev trong docker-compose.yml / application.yml
- **Module**: `W-M07` · **Truy vết**: `NFR-02, NFR-08, OWASP A05` · **Verify Run**: `RUN-W-C2-0115`
- **Thực tế mới nhất**: C2: Nginx đã có đủ X-Content-Type-Options, X-Frame-Options, Referrer-Policy, CSP (missingSecHeaders=0), /actuator/env=401, /v3/api-docs=404; CÒN 10 giá trị fallback mặc định cho local dev trong docker-compose.yml & application.yml cần thay bằng Secret Manager/biến môi trường bắt buộc khi lên Production.
- **Ghi chú xử lý**: Điều kiện bắt buộc trước khi Go-Live Production (Conditional Go).
- **Bằng chứng**: `evidence/RUN-W-C2-0115_TC-W-M07-SEC-001.json`

### DEF-W-030 [S4 / Deferred / Functional] — Mã chết / trang chưa gắn route: Wishlist.jsx & MockPaymentGateway.jsx chưa được nối trong App.jsx; LessonController.java & QuizController.java rỗng
- **Module**: `W-M07` · **Truy vết**: `NFR-10, CODE-WL-01` · **Verify Run**: `RUN-W-C2-0124`
- **Thực tế mới nhất**: Wishlist.jsx và MockPaymentGateway.jsx chưa gắn route trong App.jsx; LessonController.java và QuizController.java là file rỗng; oxlint 0 errors / 64 warnings. Dời sang backlog dọn mã kỹ thuật (S4 Trivial).
- **Ghi chú xử lý**: Mức S4 Trivial, không ảnh hưởng chức năng chạy thực tế; hoãn dọn dẹp ở sprint kế tiếp.
- **Bằng chứng**: `evidence/RUN-W-C2-0124_TC-W-M07-MNT-002.json`

### DEF-W-031 [S3 / Open / Functional] — Trang chi tiết khóa học (/courses/{id}) gọi trái quyền API giảng viên /co-instructors gây lỗi HTTP 403 trên console cho học viên; video mẫu BigBuckBunny.mp4 bị Chrome chặn ORB
- **Module**: `W-M07` · **Truy vết**: `NFR-05` · **Verify Run**: `RUN-W-C3-0130`
- **Thực tế mới nhất**: Tốc độ tải đạt tốt (LCP 290–903ms, CLS 0.00–0.10), nhưng /courses/49 gọi GET /api/v1/instructor/courses/49/co-instructors [403] khi đăng nhập Student HV1 (sinh console error) và /my-courses/47/learn tải video mẫu BigBuckBunny.mp4 bị net::ERR_BLOCKED_BY_ORB.
- **Ghi chú xử lý**: Frontend CourseDetail.jsx cần chỉ gọi /co-instructors khi user là Instructor/Admin (hoặc dùng endpoint công khai trả danh sách đồng GV) và thay URL video mẫu không bị ORB.
- **Bằng chứng**: `evidence/RUN-W-C3-0130_TC-W-M07-PERF-003.json`

### DEF-W-032 [S2 / Open / Usability] — Lỗi Responsive nghiêm trọng trên Mobile 360px & Tablet 768px: /cart tràn ngang cắt giá & nút thanh toán; /my-courses/{id}/learn mở sẵn sidebar đè player và cắt tab Hỏi & Đáp (Q&A); còn nhãn tiếng Anh chưa Việt hóa
- **Module**: `W-M07` · **Truy vết**: `NFR-07` · **Verify Run**: `RUN-W-C3-0131`
- **Thực tế mới nhất**: 4 trang (/, /courses, /courses/49, /instructor/courses/new) đạt 3 viewport; nhưng /cart (360px) tràn ngang cắt giá '300.000 ₫', nút 'Áp dụng' và 'Tiến hành thanh toán (1)'; /my-courses/47/learn (360px) mở sẵn sidebar đè player/cắt text tiến độ và (768px) cắt ngang tab thứ 4 'Hỏi & Đáp (Q&A)'. Còn nhãn tiếng Anh ('Instructor', 'Student', 'Draft', 'PendingApproval', 'Published', 'Hidden', 'NeedsReview').
- **Ghi chú xử lý**: Bắt buộc sửa CSS responsive cho Cart.jsx & CoursePlayer.jsx (đóng sidebar mặc định ở <1024px, flex-wrap tab bar) và Việt hóa badge vai trò/trạng thái.
- **Bằng chứng**: `evidence/RUN-W-C3-0131_TC-W-M07-USA-001.json`
