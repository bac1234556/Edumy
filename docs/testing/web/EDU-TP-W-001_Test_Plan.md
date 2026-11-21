# EDU-TP-W-001 — KẾ HOẠCH KIỂM THỬ HỆ THỐNG EDUMY WEB (SYSTEM TEST PLAN)

| Thuộc tính | Giá trị |
| :--- | :--- |
| **Mã tài liệu** | `EDU-TP-W-001` |
| **Phiên bản** | `1.0` |
| **Trạng thái** | `Draft — Chờ phê duyệt (Entry Gate)` |
| **Ngày lập** | `2026-11-21` (Asia/Ho_Chi_Minh) |
| **Vai trò lập** | QA / Test Lead độc lập |
| **Chuẩn áp dụng** | ISO/IEC/IEEE 29119-2 (Quy trình), 29119-3 (Tài liệu), 29119-4 (Kỹ thuật thiết kế test); ISO/IEC 25010 (Chất lượng phi chức năng); OWASP Top 10 (2021) & OWASP ASVS (mức tham chiếu L1) |
| **Repository / Build cơ sở** | `Edumy-Project` commit `f2f5240` · `Edumy-main` (Web & Backend) commit `bf4ac6c` (`feat(authoring): merge course creation wizard into single page`) |

---

## 1. Bối cảnh dự án (Context)

**Edumy** là nền tảng học trực tuyến (E-Learning Platform) vận hành theo kiến trúc **Modular Monolith (Spring Modulith 1.1.5)** trên nền **Java 17 + Spring Boot 3.2.0**, phục vụ Web SPA (**React 19.2 + Vite 8.1 / Nginx**) và Mobile Client (Expo/REST Bearer Token) trên cùng một lõi nghiệp vụ, tích hợp cơ sở dữ liệu **Microsoft SQL Server 2022**, hộp thư kiểm thử **Mailpit SMTP**, thanh toán **VietQR / SePay Webhook / VNPay / Stripe**, xuất chứng chỉ PDF tiếng Việt (**OpenPDF 1.3.43**), sổ cái doanh thu giảng viên (**Payout Ledger**) và hệ thống giám sát **Prometheus + Grafana**.

Trong các commit gần nhất trước đợt kiểm thử này (`e3e4446`, `811d729`, `0fc449e`, `bf4ac6c`), hệ thống đã thực hiện hai thay đổi lớn:
1. **Gỡ bỏ hoàn toàn tính năng AI/ML** (bảng phân tích ML, cờ `NeedsReanalysis`, trạng thái `Analyzing`, gợi ý danh mục AI).
2. **Gộp quy trình tạo khóa học thành 1 trang duy nhất** (`CourseCreate.jsx` kèm `CurriculumDraftBuilder.jsx` và `CoInstructorDraftList.jsx`).

Mục tiêu của kế hoạch này là tổ chức kiểm thử độc lập, có hệ thống và có bằng chứng thực thi thật cho toàn bộ chức năng và phi chức năng của **Edumy Web**, nhằm đánh giá mức độ sẵn sàng phát hành (Go / No-Go / Go có điều kiện).

---

## 2. Đối tượng kiểm thử (Test Items)

### 2.1. Phân chia theo 8 Module kiểm thử Web

| Mã Module | Tên Module | Thành phần Backend (`com.edumy.*`) | Thành phần Frontend / Hạ tầng | Phạm vi nghiệp vụ cốt lõi |
| :--- | :--- | :--- | :--- | :--- |
| **W-M01** | Tài khoản & Phân quyền | `identity` (`AuthController`, `MeController`, `UsersController`, `AdminController`, `SpecUseCaseController`) | `Login`, `Register`, `VerifyEmail`, `ForgotPassword`, `ResetPassword`, `UserProfile`, `AdminDashboard`, `ProtectedRoute` | UC-G05..G10, G12, G13, A01..A05; Google OAuth; refresh cookie web; xác thực email; khóa/mở tài khoản; cấp/thu hồi quyền GV |
| **W-M02** | Khám phá khóa học | `catalog` (`CategoriesController`, `TagsController`, `CourseRecommendationController`), `authoring` (`CoursesController`, `CurriculumController`), `learning` (`LessonAccessController`, `LessonsController`) | `HomePage`, `CourseList`, `CourseDetail`, `Navbar` (instant search) | UC-G01..G04, G11, G14; công khai D-08; tìm không dấu D-09; sắp xếp/lọc/phân trang; xem trước bài học; gợi ý khóa học & combo; tag |
| **W-M03** | Biên soạn, Cộng tác, Xét duyệt | `authoring` (`CoursesController`, `CurriculumController`, `MediaController`, `UploadsController`, `SpecUseCaseController`), `identity` (`AdminController`, `InstructorController`), `catalog` (`CategoriesController`) | `InstructorDashboard`, `CourseCreate`, `CurriculumDraftBuilder`, `CoInstructorDraftList`, `CourseCurriculumEditor`, `AdminDashboard`, `AdminCategoriesTab` | UC-I01..I20, A06..A12, A16; tạo khóa 1 trang; upload media; vòng đời 5 trạng thái D-13; checklist BR-07; đồng GV & chia sẻ doanh thu; duyệt/từ chối/ẩn/hiện; quản lý danh mục D-21 |
| **W-M04** | Giỏ hàng, Đơn hàng, Thanh toán | `commerce` (`CartController`, `CouponsController`, `OrdersController`, `PaymentController`), `authoring` (`SpecUseCaseController`), `identity` (`AdminController`) | `Cart`, `OrderHistory`, `PaymentQR`, `MockPaymentGateway`, `PaymentSuccess`, `PaymentCancel`, `AdminOrdersTab` | UC-S01..S07, S16, A13, A14, A18; giỏ hàng; coupon; chốt giá; BR-29; VietQR/SePay webhook idempotency; VNPay; mock gateway; đối soát Admin |
| **W-M05** | Học tập, Tương tác, Chứng nhận | `learning` (`LessonsController`, `LessonAccessController`, `CertificatesController`, `MeCertificatesController`, `WishlistController`, `QuestionController`, `InstructorQuestionController`, `QuizzesController`, `QuizAttemptsController`, `ReviewsController`), `authoring` (`CoursesController`, `SpecUseCaseController`) | `MyLearning`, `CoursePlayer`, `CourseQaTab`, `QuizTaker`, `CertificateView`, `Wishlist`, `AdminDashboard` | UC-S08..S20, I21, I22, A15, A17, A19; ghi danh miễn phí; tiến độ làm tròn xuống; chứng chỉ PDF tiếng Việt & snapshot; xác minh công khai; đánh giá; hỏi đáp Q&A & kiểm duyệt; quiz; wishlist |
| **W-M06** | Doanh thu & Rút tiền *(code-only ngoài SRS v3)* | `payout` (`EarningsController`, `PayoutAccountController`, `WithdrawalController`, `AdminWithdrawalController`), `identity` (`InstructorChannelController`) | `InstructorDashboard`, `AdminDashboard` (hoặc kiểm API nếu UI web chưa có tab) | Sổ cái doanh thu (`EarningEntry`), `BR-PAY-HOLD` (`hold-days=7`), phí nền tảng 30%, chia đồng GV, tài khoản nhận tiền (mask 4 số cuối, yêu cầu mật khẩu), rút tiền (min 1.000.000 VNĐ, max 1 lần/tháng, tối đa 1 lệnh mở), hủy lệnh, Admin duyệt/từ chối |
| **W-M07** | Vận hành & Hạ tầng | `notification` (`MailService`, `MailComposer`, `SmtpMailGateway`), `shared` (`SecurityConfig`, `Actuator`) | `docker-compose.yml` (6 services), Mailpit (`8025`), Prometheus (`9090`), Grafana (`3001`), `Jenkinsfile`, SQL Server backup/restore | 10 mẫu email Mailpit & cô lập lỗi mail; Actuator/Prometheus/Grafana; Docker stack health; CI/CD Jenkinsfile local verification; Backup/Restore RTO/RPO |
| **W-REG** | Hồi quy thay đổi gần nhất | `authoring`, `identity`, `Database/scripts/06..07` | `CourseCreate.jsx`, `CourseCurriculumEditor.jsx`, `InstructorDashboard.jsx`, `AdminDashboard.jsx` | Tạo khóa 1 trang toàn luồng (validation, partial failure, double-submit); gỡ bỏ hoàn toàn dấu vết AI (`MLTest`, `ml-analyses`, `NeedsReanalysis`, `Analyzing`, `ml-monitoring`) |

### 2.2. Thành phần ngoài phạm vi (Out of Scope)

- **Giao diện Mobile App (`Edumy-App/`)**: Đã có kế hoạch kiểm thử riêng cho ứng dụng di động.
- **Luồng nghiệp vụ chi tiết của API dành riêng cho Mobile** (`/api/v1/auth/mobile/*`, `/api/v1/catalog/*`, `/api/v1/learn/*`): Chỉ kiểm thử phân quyền & chống IDOR trong **Ma trận IDOR (Bảng 8)**, không kiểm thử giao diện mobile.
- **Tấn công hệ thống bên ngoài & thanh toán tiền thật**: Mọi thử nghiệm thanh toán chỉ thực hiện qua webhook giả lập có chữ ký, VNPay sandbox/mock và email chỉ đi vào Mailpit cục bộ (`http://localhost:8025`).
- **Máy chủ Jenkins CI/CD thực tế**: Không có Jenkins Server cài sẵn ở môi trường kiểm thử cục bộ; chỉ chạy xác minh cục bộ các lệnh trong từng stage của `Jenkinsfile` (phần chạy trên Jenkins Server được đánh dấu `BLOCKED` có lý do rõ ràng).

---

## 3. Cơ sở kiểm thử (Test Basis)

| # | Tài liệu / Mã nguồn | Vị trí trong workspace | Tình trạng |
| :--- | :--- | :--- | :--- |
| 1 | Tóm tắt yêu cầu SRS v3.0 trong yêu cầu kiểm thử (75 UC, BR-01..BR-41, AC-01..AC-37, NFR-01..NFR-10, D-08/09/13/21) | Prompt yêu cầu QA chính thức | Có sẵn (dạng tóm tắt) |
| 2 | `README.md` gốc & `Edumy-main/README.md` | `README.md`, `Edumy-main/README.md` | Có sẵn |
| 3 | Hợp đồng API v2 & OpenAPI Spec | `Edumy-main/docs/system/api-contract-v2.md`, `Edumy-main/docs/system/openapi.json` | Có sẵn |
| 4 | Tài liệu hệ thống nội bộ | `Edumy-main/Edumy/docs/system/{API_SPEC,DATA_FLOW_API_MAP,CURRENT_SYSTEM_AUDIT,RISK_ANALYSIS,TEST_REPORT,walkthrough}.md` | Có sẵn |
| 5 | Kịch bản CSDL SQL Server | `Edumy-main/Edumy/Database/scripts/{06_Script_Tao_Database_EduMyDb,07_Script_Seed_Data_EduMyDb,08_Fix_Payout_And_Sync}.sql` | Có sẵn |
| 6 | Cấu hình & Hạ tầng | `Edumy-main/Edumy/Backend/src/main/resources/application.yml`, `Edumy-main/Edumy/docker-compose.yml`, `Edumy-main/Jenkinsfile`, `Edumy-main/Edumy/monitoring/**` | Có sẵn |
| 7 | Mã nguồn Backend & Frontend | 35 Controllers trong `Edumy-main/Edumy/Backend/src/main/java/com/edumy/**/controller`, `SecurityConfig.java`, `MailService.java`, `App.jsx` và 25+ pages trong `Frontend/src/pages/` | Có sẵn |
| 8 | Quy tắc nghiệp vụ tham chiếu chung (Payout/Mobile) | `Edumy-App/.agents/rules/40-business-rules.md` | Tham chiếu bổ sung cho W-M06 |
| 9 | `EDU-SRS-001_v3.0`, `EDU-DDD-001`, `EDU-MTP-001` | *Không tồn tại trong workspace* | **Thiếu tài liệu gốc** — đã được Stakeholder chấp thuận tiến hành dựa trên các nguồn #1–#8 và ghi nhận minh bạch trong RTM/Giả định |

---

## 4. Giả định & Ràng buộc (Assumptions & Constraints)

### 4.1. Giả định (Assumptions)
- **ASM-01**: Do `EDU-SRS-001_v3.0` và `EDU-DDD-001` chưa có bản gốc trong workspace, các mã `UC`, `BR`, `AC` không có nguyên văn trong prompt được suy diễn có kiểm soát từ hợp đồng API, mã nguồn (`SpecUseCaseController`, `CoursesController`, `PaymentController`, `QuestionController`...) và tài liệu `Edumy/docs/system/*`, đồng thời đánh dấu rõ cột `trang_thai_dac_ta` trong `requirements_inventory.csv` là `Tom tat tu prompt` hoặc `Suy dien tu code/API (thieu SRS goc)`.
- **ASM-02**: Chưa tồn tại `docs/testing/master/EDU-MTP-001_Master_Test_Plan.md`; mọi quy ước định danh, severity, công thức và schema báo cáo tuân theo đúng bản tóm tắt bắt buộc trong yêu cầu QA.
- **ASM-03**: Docker Desktop chạy ổn định trên Windows host và đủ tài nguyên (RAM/CPU) để vận hành stack 6 containers `edumy-test` tách biệt với môi trường dev.
- **ASM-04**: Kết quả kiểm thử hiệu năng (NFR-05) đo trên môi trường máy trạm cục bộ chạy Docker; cấu hình phần cứng (CPU, RAM, OS) và quy mô dữ liệu seed sẽ được ghi rõ trong `EDU-TER-W-001` và báo cáo.

### 4.2. Ràng buộc (Constraints)
- **CON-01 (Trung thực tuyệt đối)**: Không suy đoán hay bịa đặt Pass/Fail. Mọi kết quả chỉ được ghi sau lần thực thi thật kèm bằng chứng lưu trong `docs/testing/web/evidence/<RUN-ID>_*`.
- **CON-02 (Không sửa mã nguồn ứng dụng trong chu kỳ thực thi)**: Trong toàn bộ chu kỳ C1 (và các chu kỳ kiểm thử), QA không sửa code ứng dụng; lỗi phát hiện được ghi nhận defect `DEF-W-NNN` và chờ Stakeholder quyết định.
- **CON-03 (Không cài thêm dependency vào dự án)**: Không chỉnh sửa `pom.xml` hay `package.json`. `openpyxl` được Stakeholder cho phép cài trong virtualenv tách biệt ngoài repository để xuất file `.xlsx`. Nếu cần Playwright/k6/newman/axe sẽ hỏi Stakeholder trước.
- **CON-04 (Bảo mật thông tin nhạy cảm)**: Tuyệt đối không ghi mật khẩu thật, JWT token, API key hay secret thật vào tài liệu và evidence (che bằng `***`).
- **CON-05 (An toàn môi trường)**: Chỉ chạy trên stack Docker local `edumy-test`, không tác động dữ liệu dev và không gửi request/email ra bên ngoài.

---

## 5. Các bên liên quan & Trách nhiệm (Stakeholders)

| Vai trò | Trách nhiệm chính | Điểm tương tác |
| :--- | :--- | :--- |
| **Product Owner / Project Sponsor (User)** | Phê duyệt Test Plan & Test Case; quyết định ưu tiên sửa lỗi (Fix/Defer) sau C1; xác nhận Go/No-Go cuối đợt | Điểm dừng cuối Pha 2, cuối C1 (`EDU-TSR-W-001`), cuối Pha 6 (`EDU-TCR-W-001`) |
| **QA / Test Lead độc lập (Antigravity)** | Lập Test Plan, Requirements Inventory, Test Design, Test Cases, RTM; dựng môi trường test + baseline; thực thi C1/C2/C3; ghi defect + evidence; sinh số liệu tự động & báo cáo hoàn tất | Báo cáo tóm tắt 3 dòng sau mỗi module; báo cáo đầy đủ tại các mốc dừng |
| **Development Team** | Sửa các defect được duyệt giữa C1 → C2 → C3; cung cấp commit build mới để kiểm thử lại | Sau khi nhận `EDU-TSR-W-001` và danh sách `DEF-W-NNN` |

---

## 6. Sổ rủi ro (Risk Register)

> **Lưu ý kiểm thử độc lập**: Các mục `RSK-W-01` đến `RSK-W-07` là các **điểm nghi vấn từ rà soát mã nguồn (mục 3.2)** và chưa được kết luận là lỗi cho đến khi kiểm chứng bằng thực thi thật ở Pha 4.

| Mã rủi ro | Phân loại | Module | Mô tả rủi ro / Điểm nghi vấn cần kiểm chứng | Khả năng (L) | Tác động (I) | Mức độ | Chiến lược kiểm thử & TC kiểm chứng |
| :--- | :--- | :--- | :--- | :---: | :---: | :---: | :--- |
| **RSK-W-01** | Product (Logic/Sec) | W-M03 | `PUT /api/courses/{id}/status`: giảng viên set `"PendingApproval"` có thể bỏ qua toàn bộ checklist BR-07 (tiêu đề, mô tả, giá, chương, bài học có nội dung) so với `POST /instructor/courses/{id}/submit` | High | High | **Critical** | Thiết kế TC API/JUnit gọi thẳng `PUT /courses/{id}/status` với khóa rỗng chương/bài (`TC-W-M03-FN-014`, `TC-W-M03-SEC-003`) |
| **RSK-W-02** | Product (Logic) | W-M03 | `POST /api/courses` khi không truyền `categoryId` có nguy cơ tự fallback về `categoryId = 1` thay vì yêu cầu chọn danh mục đang hoạt động (UC-I01) | High | Medium | **High** | Kiểm thử gửi payload thiếu `categoryId`, `categoryId=null`, `categoryId=0` và danh mục ngừng sử dụng (`TC-W-M03-FN-003`, `TC-W-M03-FN-004`) |
| **RSK-W-03** | Product (Consistency) | W-M03 | Bất nhất quy tắc giá: form tạo khóa (`CourseCreate.jsx`) nhận mọi giá `>= 0` (vd. 5.000đ, 12.500đ), trong khi endpoint gửi duyệt yêu cầu `>= 10.000` và bội số `1.000` | High | Medium | **High** | BVA trên giá tạo khóa vs giá gửi duyệt: 0, 1, 9999, 10000, 10500, -1000 (`TC-W-M03-FN-005`, `TC-W-M03-FN-013`) |
| **RSK-W-04** | Product (Security) | W-M01 | NFR-02 yêu cầu BCrypt cost `>= 12`; cần kiểm tra cost thực tế của hash `$2a$XX$` lưu trong SQL Server (`RestClientConfig` khởi tạo `new BCryptPasswordEncoder()` mặc định cost 10) | High | High | **High** | Đọc trực tiếp `PasswordHash` trong bảng `dbo.Users` sau khi đăng ký mới (`TC-W-M01-SEC-005`) |
| **RSK-W-05** | Product (Security) | W-M01, W-M05 | NFR-03 chống brute-force & rate-limit: lần đăng nhập sai thứ 6 trong 15 phút và lần tra cứu chứng chỉ thứ 31 trong 1 phút có bị chặn không | High | High | **Critical** | Script Node gửi 6 lần login sai liên tiếp và 31 request `/certificates/verify/{code}` trong 60s (`TC-W-M01-SEC-006`, `TC-W-M05-SEC-002`) |
| **RSK-W-06** | Product (Reliability) | W-M04 | UC-S16 / NFR-09: hệ thống có bộ hẹn giờ (`@Scheduled`) tự hủy đơn hàng quá hạn và đóng mã VietQR hết hạn không | High | High | **High** | Kiểm tra mã nguồn `@Scheduled` + kiểm chứng thực tế với đơn hàng hết hạn QR (`TC-W-M04-REL-003`) |
| **RSK-W-07** | Product (Security) | W-M01 | Thời hạn JWT / Refresh (`jwt.access-ttl-minutes: 60`, `jwt.expire-days: 7`): đánh giá rủi ro theo OWASP ASVS L1 (token lifetime, revocation khi đổi mật khẩu / khóa tài khoản) | Medium | High | **High** | Giải mã JWT `exp`, kiểm tra hành vi token cũ sau đổi mật khẩu và sau khi Admin khóa tài khoản (`TC-W-M01-SEC-007`, `TC-W-M01-FN-012`, `TC-W-M01-FN-016`) |
| **RSK-W-08** | Product (Security) | Toàn hệ thống | **IDOR & Broken Access Control (OWASP A01)**: Nhiều endpoint mở `permitAll()` ở `SecurityConfig` hoặc thiếu kiểm tra chủ sở hữu (`SpecUseCaseController`, `CurriculumController`, `LessonsController`, `CartController`, `OrdersController`, `PaymentController`, `Payout`) | High | High | **Critical** | Chạy ma trận IDOR đầy đủ 8 vai trò × 13 nhóm endpoint (`TC-W-M01..M06-SEC-*`, sheet `IDOR`) |
| **RSK-W-09** | Product (Payment) | W-M04 | Gian lận thanh toán, sai chữ ký webhook, replay webhook 10 lần, thao túng giá client, sai số tiền / webhook trễ cần đối soát `needs_review` (UC-A18) | Medium | High | **Critical** | Kiểm thử idempotency 10x, sai key/chữ ký, giả mạo số tiền, kiểm tra đối soát thủ công (`TC-W-M04-FN-*`, `TC-W-M04-SEC-*`, `TC-W-M04-REL-*`) |
| **RSK-W-10** | Product (Data/Payout) | W-M06 | Sai lệch chia doanh thu (30% phí sàn + % đồng GV), hold-days chưa đến hạn nhưng cho rút, race condition gửi 2 lệnh rút cùng lúc hoặc lần thứ 2 trong tháng | Medium | High | **Critical** | Kiểm thử công thức chia tiền, hold-days, gửi đồng thời 2 lệnh rút, giới hạn 1 lần/tháng (`TC-W-M06-FN-*`, `TC-W-M06-REL-001`) |
| **RSK-W-11** | Product (Security) | W-M07 | Cấu hình bảo mật mặc định (secrets trong `application.yml`, Grafana `admin/***`, SQL Server SA `***`, actuator exposure, thiếu security headers, upload file độc hại/SVG XSS/path traversal) | High | High | **High** | Quét secrets trong repo, kiểm tra Actuator/Swagger/CORS/Headers, test upload độc hại (`TC-W-M07-SEC-*`, `TC-W-M03-SEC-002`) |
| **RSK-W-12** | Product (Recovery) | W-M07 | NFR-06: Thiếu cơ chế sao lưu tự động hằng ngày (RPO 24h) hoặc bản backup không thể phục hồi vẹn toàn (RTO <= 4h) | High | High | **High** | Thực thi BACKUP → RESTORE trên container SQL Server tạm, đối chiếu số bản ghi + kiểm tra lịch backup (`TC-W-M07-REC-001`, `TC-W-M07-REC-002`) |
| **RSK-W-13** | Product (Regression) | W-REG | Dấu vết AI/ML còn sót (`AdminController /ml-monitoring`, `SentimentLabel` trên Review, hoặc UI cũ) và lỗi một phần (partial failure) khi tạo khóa 1 trang (tạo course thành công nhưng tạo section/lesson/mời đồng GV lỗi) | Medium | Medium | **Medium** | Grep toàn mã nguồn & UI + kiểm thử tạo khóa 1 trang đầy đủ kịch bản lỗi (`TC-W-REG-FN-*`, `TC-W-REG-MNT-001`) |
| **RSK-W-14** | Project | Toàn dự án | Thiếu bản gốc `EDU-SRS-001_v3.0` và `EDU-DDD-001` trong workspace dẫn đến nguy cơ sai lệch kỳ vọng với nguyên văn tài liệu đặc tả | Medium | Medium | **Medium** | Đánh dấu minh bạch nguồn yêu cầu trong `requirements_inventory.csv` & RTM; gửi bảng thống kê xin Stakeholder duyệt tại cuối Pha 2 |

---

## 7. Chiến lược kiểm thử (Test Strategy)

### 7.1. Chiến lược ưu tiên theo rủi ro (Risk-Based Testing)
- **Ưu tiên Cao (High)**: Thanh toán & webhook idempotency (W-M04), Cấp quyền học & Chứng chỉ PDF (W-M05), Xác thực & Phân quyền / Ma trận IDOR (W-M01 + SEC), Vòng đời duyệt khóa học & BR-07 (W-M03), Doanh thu & Rút tiền (W-M06), Gỡ bỏ AI & Tạo khóa 1 trang (W-REG).
- **Ưu tiên Trung bình (Medium)**: Khám phá, tìm kiếm không dấu, sắp xếp, gợi ý (W-M02), Q&A & Đánh giá & Quiz & Wishlist (W-M05), Email Mailpit & Monitoring (W-M07), Khả dụng/Hiệu năng/Tương thích.
- **Ưu tiên Thấp (Low)**: Biểu tượng danh mục, mã chết/route không dùng (S4), thông điệp giao diện phụ.

### 7.2. Các cấp độ kiểm thử (Test Levels)
1. **Unit & Architecture Testing**: Chạy bộ test hiện có bằng `.\mvnw.cmd verify` (gồm `ModularityTest`, `AuthServiceImplTest`, `MobileAuthControllerTest`, `EarningServiceImplTest`, `WithdrawalServiceImplTest`, `JwtTokenProviderTest`, `Phase1PaywallTest`) + JaCoCo coverage report. Bổ sung test tự động mới trong `Backend/src/test/java/com/edumy/**` cho 4 vùng rủi ro cao: phân quyền, idempotency thanh toán, state transition khóa học và checklist BR-07.
2. **Integration / API Testing**: Sử dụng script Node.js thuần (`fetch`, Node v24) đặt tại `docs/testing/web/scripts/` để kiểm thử toàn bộ REST contract, ma trận IDOR (8 vai trò × 13 nhóm endpoint), webhook SePay/VNPay, cô lập email Mailpit API (`http://localhost:8025/api/v1/messages`), lưu toàn bộ request/response (đã che token/secret) vào `evidence/`.
3. **System / E2E UI Testing**: Sử dụng **Browser Subagent** của Antigravity kiểm thử trên trình duyệt thực tế (`http://localhost:3000` qua Nginx Docker), chụp screenshot/bằng chứng cho luồng E2E của Guest, Student, Instructor, Co-Instructor và Admin, kiểm tra responsive 360/768/1366px và kiểm tra chéo trình duyệt (Chrome, Edge, Firefox) cho nhóm TC High.
4. **Acceptance Testing**: Đối chiếu và thực thi 37 tiêu chí chấp nhận `AC-01..AC-37` theo cấu trúc Given/When/Then.

### 7.3. Kỹ thuật thiết kế kiểm thử (ISO/IEC/IEEE 29119-4)
Mọi Test Case đều ghi rõ kỹ thuật áp dụng:
- **EP (Equivalence Partitioning)**: Phân vùng hợp lệ / không hợp lệ cho email, mật khẩu, rating (1..5), giá, trạng thái, loại file upload.
- **BVA (Boundary Value Analysis)**: Giá khóa học (`-1`, `0`, `9.999`, `10.000`, `10.500`), rút tiền (`999.999`, `1.000.000`, `availableBalance`, `availableBalance + 1`), phân trang (`pageSize = 20`), cooldown gửi mail (`59s` vs `60s`), đăng nhập sai lần thứ `5` vs `6`, tra cứu chứng chỉ lần thứ `30` vs `31`, kích thước file upload (`<= 50MB` vs `> 50MB`), độ dài tiêu đề (`4` vs `5` ký tự), mô tả (`9` vs `10` ký tự), tỷ lệ chia đồng GV (`-1`, `0`, `10000`, `10001`).
- **Decision Table**: Quyền truy cập bài học (preview / enrolled / instructor / hidden / anonymous), hành động trên giỏ hàng (AC-29), kiểm duyệt khóa học & hoàn tiền/đối soát, rút tiền (số dư × tối thiểu × hạn mức tháng × lệnh đang mở × tài khoản nhận tiền).
- **State Transition**: Vòng đời khóa học 5 trạng thái (`Draft → PendingApproval → Published | NeedsReview(Rejected) → Hidden → Published`), vòng đời đơn hàng (`Pending → Paid | Cancelled | Expired | NeedsReview`), vòng đời yêu cầu rút tiền (`requested → paid | rejected | cancelled`), vòng đời lời mời đồng GV (`INVITED → ACCEPTED | DECLINED | REMOVED`).
- **Use Case Testing**: Bao phủ luồng chính + mọi luồng thay thế (A) và ngoại lệ (E) cho 75 UC.
- **Pairwise Testing**: Tổ hợp lọc & sắp xếp danh mục (danh mục × miễn phí/trả phí × sắp xếp × từ khóa không dấu/có dấu); tổ hợp trình duyệt × viewport.
- **Error Guessing**: Double-submit, replay webhook 10x, ngắt backend giữa giao dịch thanh toán, path traversal tên file, SVG chứa `<script>`, SQLi/XSS, JWT `alg: none` & sửa chữ ký, gửi request khi Mailpit tắt.

### 7.4. Kiểm thử Phi chức năng (ISO/IEC 25010 ↔ NFR-01..NFR-10)
- **SEC (Bảo mật — NFR-01, NFR-02, NFR-03, NFR-08)**: OWASP Top 10 (2021) A01..A09 + OWASP ASVS L1 + Upload độc hại + Quét secrets repo.
- **PERF (Hiệu năng — NFR-05)**: Script Node thuần (`Promise.all`) mô phỏng **50 người dùng đồng thời**, mỗi kịch bản `>= 3` lượt sau warm-up (danh sách khóa, chi tiết khóa, lưu tiến độ, đăng câu hỏi: `p95 <= 3s`; tạo PDF chứng chỉ `<= 5s`; tạo khóa 5 chương × 3 bài) + Chrome DevTools Lighthouse (LCP, TBT).
- **REL (Độ tin cậy — NFR-04, NFR-09)**: Idempotency webhook 10x; giết/ngắt container backend giữa lúc xử lý thanh toán; race condition 2 tiến trình xử lý cùng đơn/cùng lệnh rút; double-click UI.
- **REC (Khả năng phục hồi — NFR-06)**: `BACKUP DATABASE EduMyDb` → `RESTORE` sang container SQL Server tạm độc lập → đối chiếu row count các bảng chính, đo RTO (`<= 4h`), kiểm chứng RPO (`24h` — kiểm tra có lịch sao lưu tự động hằng ngày không).
- **OBS (Khả năng theo dõi — NFR-08)**: Truy vết 1 đơn hàng end-to-end qua log (không lộ secret/token/password), kiểm `/actuator/health`, `/actuator/prometheus`, Prometheus targets `UP`, Grafana dashboard có dữ liệu.
- **USA (Khả dụng — NFR-07)**: Responsive `360px / 768px / 1366px`, tiếng Việt nhất quán, thông báo lỗi rõ từng trường, trạng thái rỗng/lỗi rõ ràng, điều hướng bàn phím, Lighthouse Accessibility (`>= 90` đề xuất).
- **COMP (Tương thích)**: Chạy lại nhóm TC High trên Chrome, Edge, Firefox phiên bản mới nhất.
- **MNT (Bảo trì)**: `.\mvnw.cmd verify` + JaCoCo, Frontend `npm run lint` & `npm run build`, grep 0 dấu vết AI, rà soát mã chết / route không dùng (ghi nhận S4).
- **DEP (Triển khai)**: Dựng stack Docker mới từ cấu hình sạch, đo thời gian 6 services healthy, chạy xác minh cục bộ các bước trong `Jenkinsfile`.

---

## 8. Môi trường & Dữ liệu kiểm thử (Test Environment & Test Data)

### 8.1. Môi trường kiểm thử độc lập
- Khởi chạy stack Docker tách biệt bằng compose project name riêng (`docker compose -p edumy-test ...`) kèm file override port/container name (không sửa `docker-compose.yml` gốc của dự án) để tránh xung đột tên container `edumy_*` và bảo toàn 100% dữ liệu dev.
- 6 dịch vụ mục tiêu: `database` (SQL Server 2022), `backend` (Spring Boot 3.2.0), `mailpit` (SMTP + Web UI), `frontend` (React 19 + Nginx), `prometheus`, `grafana`.
- Mọi cổng ánh xạ thực tế, phiên bản OS/Docker/Java/Node/Browser và cấu hình phần cứng sẽ được ghi chi tiết trong `EDU-TER-W-001_Test_Environment.md` ở Pha 3.

### 8.2. Bộ dữ liệu kiểm thử & Baseline tái lập
- **10 tài khoản kiểm thử** (không ghi mật khẩu trong tài liệu/evidence):
  `ADM1`, `ADM2`, `GV-A`, `GV-B`, `GV-CO` (đồng GV), `GV-REVOKED` (GV bị thu hồi), `HV1`, `HV2`, `HV-LOCKED` (đã bị khóa), `HV-UNVERIFIED` (chưa xác thực email).
- **Danh mục (`>= 4`)**: Tối thiểu 3 danh mục đang hoạt động + 1 danh mục ngừng sử dụng (nếu DB hỗ trợ trạng thái inactive) + 1 danh mục trống (0 khóa học) để test D-21.
- **Khóa học**: Đủ 5 trạng thái (`Draft`, `PendingApproval`, `Published`, `NeedsReview`/`Rejected`, `Hidden`), gồm khóa miễn phí (`0 VNĐ`), khóa trả phí (`>= 10.000 VNĐ`), khóa có bài học preview, khóa có **10 bài học** để kiểm thử làm tròn xuống tiến độ và cấp chứng chỉ PDF, khóa có quiz và câu hỏi Q&A.
- **Coupon & Payout**: Tối thiểu 1 coupon hợp lệ, 1 coupon hết hạn, 1 coupon ngừng hoạt động; dữ liệu doanh thu mẫu kiểm chứng `hold-days` và rút tiền.
- **Baseline**: Sau khi seed xong, tạo bản sao lưu `edumy_test_baseline.bak`; khôi phục baseline trước mỗi chu kỳ (C1, C2, C3) và sau các kịch bản phá hủy để đảm bảo tính tái lập.

---

## 9. Tiêu chí kiểm thử (Entry, Exit, Suspension, Resumption Criteria)

### 9.1. Entry Criteria (Tiêu chí bắt đầu Pha 3–4)
1. `.\mvnw.cmd verify` (Backend, gồm `ModularityTest`) và `npm run build` (Frontend) thành công.
2. Stack Docker `edumy-test` khởi chạy đủ 6 dịch vụ ở trạng thái healthy/running và truy cập được.
3. Đã seed đủ bộ dữ liệu kiểm thử và tạo thành công file baseline `edumy_test_baseline.bak`.
4. `EDU-TP-W-001` và danh sách Test Cases (`EDU-TCS-W-001`) được Stakeholder phê duyệt.

### 9.2. Exit Criteria (Tiêu chí kết thúc — đề xuất theo Q-09 SRS)
1. **100%** Test Case ưu tiên `High` đã được thực thi (0 `Not Run`).
2. **Pass rate >= 95%** trên tổng số TC đã thực thi.
3. **0 defect Severity S1 hoặc S2** còn ở trạng thái mở (`New`/`Open`/`Reopened`).
4. **Requirement Coverage = 100%**: Mọi `UC` (75), `BR` (41), `AC` (37), `NFR` (10) đều có ít nhất 1 TC truy vết trong RTM.
5. Mọi `NFR-01..NFR-10` đều có kết quả đo cụ thể bằng bằng chứng thực thi.
6. Mọi defect S3/S4 còn mở đều có quyết định `Deferred` được Stakeholder xác nhận cùng đánh giá rủi ro còn lại.

### 9.3. Suspension Criteria (Tiêu chí tạm dừng)
- Môi trường kiểm thử (`database`, `backend`, `frontend` hoặc `mailpit`) sập không khôi phục được ngay.
- `>= 30%` số TC của một module rơi vào trạng thái `BLOCKED`.
- Phát hiện defect `S1 Critical` chặn hoàn toàn luồng nghiệp vụ chính của module đang kiểm thử (ví dụ: không đăng nhập được, không thanh toán được, crash backend hàng loạt).

### 9.4. Resumption Criteria (Tiêu chí tiếp tục)
- Nguyên nhân gây tạm dừng đã được khắc phục trên môi trường `edumy-test`.
- Bộ **Smoke Test (10 TC High xuyên 8 module)** chạy lại đạt **100% Pass**.

---

## 10. Quy ước định danh, Phân loại lỗi & Công thức đo lường

### 10.1. Quy ước mã định danh
- **Tài liệu**: `EDU-TP-W-001` (Test Plan), `EDU-TDS-W-001` (Test Design), `EDU-TCS-W-001` (Test Cases), `EDU-TER-W-001` (Test Environment), `EDU-TEL-W-001` (Execution Log), `EDU-TIR-W-001` (Defect Report), `EDU-TSR-W-xxx` (Status Report), `EDU-TCR-W-001` (Completion Report).
- **Kịch bản**: `TS-W-<Mxx|REG>-<NNN>` (ví dụ `TS-W-M01-001`).
- **Test Case**: `TC-W-<Mxx|REG>-<LOẠI>-<NNN>` với `<LOẠI>` thuộc `{FN, SEC, PERF, REL, REC, OBS, USA, COMP, MNT, DEP}`.
- **Lần thực thi**: `RUN-W-C<n>-<NNNN>` (ví dụ `RUN-W-C1-0001`).
- **Lỗi (Defect)**: `DEF-W-<NNN>` (ví dụ `DEF-W-001`).

### 10.2. Phân loại Severity, Priority, Loại lỗi & Vòng đời lỗi
- **Severity**:
  - `S1 Critical`: Crash hệ thống, mất dữ liệu, lỗ hổng bảo mật nghiêm trọng (IDOR, bypass auth, SQLi, giả mạo webhook), sai tiền/doanh thu/rút tiền.
  - `S2 Major`: Sai lệch chức năng chính hoặc quy tắc nghiệp vụ cốt lõi (BR/AC), không có giải pháp thay thế chấp nhận được, chức năng SRS chưa hiện thực.
  - `S3 Minor`: Lỗi chức năng phụ, thông báo chưa rõ ràng, lệch UI nhẹ có workaround.
  - `S4 Trivial`: Lỗi chính tả, căn lề nhỏ, mã chết/route không dùng không ảnh hưởng vận hành.
- **Priority**: `P1` (sửa ngay), `P2` (sửa trong chu kỳ), `P3` (có thể hoãn/Deferred).
- **Loại lỗi**: `Functional`, `Security`, `Performance`, `Usability`, `Not Implemented`, `Spec Mismatch`.
- **Vòng đời**: `New → Open → Fixed → Retest → Closed | Reopened | Deferred`.

### 10.3. Công thức thống kê (tự động hóa bằng Python, không nhập tay)
- $\text{Execution rate} = \frac{\text{Đã thực thi}}{\text{Tổng TC}}$
- $\text{Pass rate} = \frac{\text{Pass}}{\text{Đã thực thi}}$
- $\text{First-time pass rate} = \frac{\text{TC Pass ngay lần đầu}}{\text{Đã thực thi}}$
- $\text{Requirement coverage} = \frac{\text{Yêu cầu có } \ge 1 \text{ TC}}{\text{Tổng yêu cầu}}$
- $\text{Defect density} = \frac{\text{Số defect}}{\text{Module}}$
- $\text{Defect fix rate} = \frac{\text{Defect Closed}}{\text{Tổng defect}}$

---

## 11. Sản phẩm bàn giao (Deliverables)

Toàn bộ tài liệu và bằng chứng lưu tại `Edumy-main/docs/testing/web/`:
- `EDU-TP-W-001_Test_Plan.md`
- `requirements_inventory.csv`
- `EDU-TDS-W-001_Test_Design.md`
- `EDU-TCS-W-001_Test_Cases.xlsx` (8 sheet: `Cover`, `TestCases`, `ExecutionLog`, `Defects`, `Summary`, `RTM`, `IDOR`, `NFR`)
- `EDU-TER-W-001_Test_Environment.md`
- `EDU-TEL-W-001_Execution_Log.csv`
- `EDU-TIR-W-001_Defect_Report.md`
- `EDU-TSR-W-001_Status_Report.md`
- `EDU-TCR-W-001_Test_Completion_Report.md`
- `PROGRESS.md`, `charts/*.png`, `evidence/<RUN-ID>_*`, `scripts/` (API, IDOR, Performance, Metrics + `README.md`)
- Bộ test tự động mới trong `Edumy-main/Edumy/Backend/src/test/java/com/edumy/**` (phân quyền, idempotency, state transition, BR-07).

---

## 12. Lịch trình & Ước lượng (Schedule & Estimation)

| Pha | Nội dung công việc chính | Đầu ra | Ước lượng nỗ lực | Điểm dừng / Phê duyệt |
| :--- | :--- | :--- | :---: | :--- |
| **Pha 1** | Đọc Test Basis, rà soát rủi ro mã nguồn, lập Test Plan | `EDU-TP-W-001_Test_Plan.md` | 0.5 ngày | Gộp phê duyệt cùng Pha 2 |
| **Pha 2** | Trích danh mục yêu cầu, thiết kế kịch bản, viết toàn bộ Test Cases, RTM, Ma trận IDOR & NFR | `requirements_inventory.csv`, `EDU-TDS-W-001`, `EDU-TCS-W-001.xlsx` | 1.5 ngày | **>>> DỪNG**: Duyệt Test Plan + Bảng số TC & khoảng trống |
| **Pha 3** | Chạy build/verify, dựng stack `edumy-test`, seed dữ liệu + baseline `.bak`, chạy Smoke Test 10 TC | `EDU-TER-W-001`, `edumy_test_baseline.bak` | 0.5 ngày | Entry Gate trước C1 |
| **Pha 4 (C1)** | Thực thi chu kỳ C1 lần lượt `W-M01 → W-M07 → W-REG → Phi chức năng`; bổ sung JUnit tests; cập nhật `PROGRESS.md` sau từng module | `EDU-TEL-W-001.csv`, `EDU-TIR-W-001`, `EDU-TSR-W-001`, `evidence/`, `scripts/` | 3.0 ngày | **>>> DỪNG**: Gửi `EDU-TSR-W-001` chờ quyết định sửa lỗi |
| **Pha 5 (C2/C3)** | Re-test mọi TC Fail sau khi Dev sửa lỗi + hồi quy TC High module liên quan (cộng dồn lần chạy) | Cập nhật Execution Log, Defects, Status Report | 1.0 ngày | Xác nhận đóng defect |
| **Pha 6** | Sinh 9 bảng thống kê & 4 biểu đồ PNG bằng script, tổng hợp báo cáo hoàn tất & đề xuất Go/No-Go | `EDU-TCR-W-001`, `charts/*.png`, commit `test(qa-web): ...` | 0.5 ngày | Bàn giao nghiệm thu |
| **Tổng cộng** | | | **7.0 ngày công** | |
