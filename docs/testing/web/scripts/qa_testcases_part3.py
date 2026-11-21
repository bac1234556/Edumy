# -*- coding: utf-8 -*-
"""
Additional Dedicated Exception / Alternative Flow Test Cases so that:
- EVERY ONE of the 75 UCs has >= 2 distinct TCs (Main Flow + Alt/Exception Flow)
- EVERY ONE of the code-only features (W-M06, Wishlist, Coupon, Quiz, OAuth, Recommendation) has >= 2 distinct TCs (Happy + Error)
"""

TEST_CASES_PART3 = [
    # W-M01: UC-G08, UC-S20, CODE-OAUTH-01
    ("TC-W-M01-FN-018", "W-M01", "TS-W-M01-002", "UC-G08",
     "Gửi lại email xác thực ngoại lệ: email không tồn tại, email rỗng hoặc tài khoản đã xác thực từ trước",
     "FN", "Integration/API", "EP", "Medium", "Khách / HV1",
     "HV1 đã xác thực email",
     "1) email=''; 2) email='missing_verify@edumy.test'; 3) email=HV1 (đã xác thực)",
     "Gửi POST /api/v1/auth/verification-email/resend cho 3 trường hợp ngoại lệ trên.",
     "Email rỗng -> 400; email không tồn tại -> 404 (hoặc trung lập an toàn); tài khoản đã xác thực -> không tạo lỗi 500, thông báo đã xác thực."),

    ("TC-W-M01-FN-019", "W-M01", "TS-W-M01-005", "UC-S20",
     "Đăng ký làm Giảng viên luồng chính cho học viên hợp lệ chưa từng bị thu hồi (và ngoại lệ đã là Giảng viên)",
     "FN", "Integration/API", "Decision Table", "Medium", "HV2 / GV-A",
     "HV2 là Student bình thường chưa từng bị thu hồi; GV-A đã là Instructor",
     "POST /api/users/become-instructor bởi HV2 và bởi GV-A",
     "1. HV2 gọi POST /api/users/become-instructor.\n2. GV-A (đã là Instructor) gọi lại POST /api/users/become-instructor.",
     "HV2 được xử lý nâng cấp hợp lệ (không crash 500); GV-A gọi lại được xử lý idempotent/thông báo đã là giảng viên."),

    ("TC-W-M01-FN-020", "W-M01", "TS-W-M01-003", "CODE-OAUTH-01",
     "Đăng nhập Google OAuth luồng UI & tích hợp frontend: hiển thị nút đăng nhập Google, xử lý phản hồi lỗi từ backend không treo màn hình",
     "FN", "System/E2E", "Use Case", "Low", "Khách",
     "Truy cập trang /login trên trình duyệt",
     "Kiểm tra cấu hình @react-oauth/google và xử lý lỗi khi backend trả 401/400",
     "1. Mở /login, kiểm tra hiển thị thành phần đăng nhập Google.\n2. Gửi payload Google OAuth hợp lệ cấu trúc nhưng không có tài khoản Google thật (sandbox/mock) hoặc kiểm tra thông báo lỗi UI.",
     "Giao diện hiển thị nút Google rõ ràng, không văng lỗi JS; khi OAuth thất bại hiển thị thông báo tiếng Việt thân thiện."),

    # W-M02: UC-G11, UC-G14, CODE-REC-01
    ("TC-W-M02-FN-008", "W-M02", "TS-W-M02-004", "UC-G11, BR-22",
     "Ngoại lệ xem trước bài học: chặn bài không đánh dấu preview (403) và trả 404 với bài thuộc khóa Hidden hoặc ID không tồn tại",
     "FN", "Integration/API", "Decision Table", "High", "Khách / HV2",
     "Khóa trả phí có bài L_LOCKED (isPreview=false) và khóa Hidden có bài L_HID",
     "GET /api/v1/lessons/{L_LOCKED}/access, GET /api/v1/catalog/courses/{id}/lessons/{L_LOCKED}/preview, GET /api/v1/lessons/999999/access",
     "1. Khách/HV2 gọi truy cập bài L_LOCKED (không preview).\n2. Khách gọi preview bài của khóa Hidden.\n3. Gọi lessonId=999999.",
     "L_LOCKED trả 401/403 LESSON_NOT_PREVIEWABLE; khóa Hidden & lessonId=999999 trả 404."),

    ("TC-W-M02-FN-009", "W-M02", "TS-W-M02-005", "UC-G14, CODE-REC-01",
     "Ngoại lệ gợi ý khóa học & Tag: khóa gợi ý không bao gồm khóa Draft/Hidden, tìm tag không khớp trả mảng rỗng, courseId không tồn tại không crash 500",
     "FN", "Integration/API", "Error Guessing", "Medium", "Khách",
     "Có khóa Draft/Hidden trong cùng danh mục với khóa Published",
     "GET /api/v1/courses/{id_pub}/similar, /bundle-recommendation, /recommend; GET /api/tags/search?q=NONEXISTENT999; GET /api/v1/courses/999999/similar",
     "1. Kiểm tra danh sách recommend/similar/bundle chỉ chứa khóa Published.\n2. Tìm tag không tồn tại.\n3. Gọi similar & bundle cho courseId=999999.",
     "0 khóa Draft/Hidden xuất hiện trong gợi ý; tag không khớp trả []; courseId=999999 trả 404/rỗng không 500."),

    # W-M03: UC-I02, UC-I10, UC-I11, UC-I13, UC-I14, UC-I18, UC-I19, UC-A06..A10, UC-A12, UC-A16
    ("TC-W-M03-FN-020", "W-M03", "TS-W-M03-002", "UC-I02, UC-I19",
     "Ngoại lệ trang giảng viên: GV mới chưa có khóa học/học viên hiển thị trạng thái rỗng thân thiện; GV-B bị chặn xem danh sách học viên khóa của GV-A",
     "FN", "Integration/API", "EP, Decision Table", "Medium", "GV-B",
     "GV-B không sở hữu khóa C_GVA của GV-A",
     "GET /api/v1/instructor/courses/{C_GVA}/students bởi GV-B; GET /api/instructor/courses/999999/students",
     "1. GV-B gọi xem danh sách học viên của khóa C_GVA thuộc GV-A.\n2. Gọi với courseId=999999.\n3. Kiểm tra hiển thị dashboard khi GV chưa có khóa nào.",
     "GV-B bị từ chối 403 FORBIDDEN; courseId=999999 trả 404; trạng thái rỗng hiển thị sạch đẹp không crash."),

    ("TC-W-M03-FN-021", "W-M03", "TS-W-M03-003", "UC-I10, UC-I11",
     "Ngoại lệ tạo Quiz & Tag: chặn tạo Quiz với passingScore âm/>100 hoặc sectionId không tồn tại; chặn Student tạo Quiz/Tag",
     "FN", "Integration/API", "BVA, Decision Table", "Medium", "GV-A / HV1",
     "GV-A và HV1 đã đăng nhập",
     "1) POST /api/v1/quizzes với sectionId=999999, passingScore=150; 2) HV1 gọi POST /api/v1/quizzes và POST /api/tags; 3) DELETE /api/v1/quizzes/999999",
     "Gửi các yêu cầu tạo/xóa Quiz và Tag không hợp lệ hoặc sai vai trò.",
     "HV1 bị chặn 403; dữ liệu sai/không tồn tại trả 400/404, không tạo bản ghi rác."),

    ("TC-W-M03-FN-022", "W-M03", "TS-W-M03-004", "UC-I13, UC-I14",
     "Ngoại lệ rút yêu cầu duyệt & xem review-note: chặn rút duyệt khóa đang Published/Hidden hoặc khóa không phải của mình; kiểm tra khóa không tồn tại",
     "FN", "Integration/API", "State Transition, Error Guessing", "High", "GV-A / GV-B",
     "Khóa C_PUB đang Published của GV-A; khóa C_PEND đang PendingApproval của GV-A",
     "1) GV-A gọi withdraw trên C_PUB (Published); 2) GV-B gọi withdraw trên C_PEND của GV-A; 3) GET review-note cho courseId=999999",
     "1. GV-A thử rút duyệt khóa đã Published (C_PUB).\n2. GV-B thử rút duyệt khóa C_PEND của GV-A.\n3. Gọi withdraw & review-note cho courseId=999999.",
     "Rút duyệt khóa đã Published bị chặn 400/409 (không tự hạ khóa công khai về Draft!); GV-B bị chặn 403; id=999999 trả 404."),

    ("TC-W-M03-FN-023", "W-M03", "TS-W-M03-005", "UC-I18, BR-18",
     "Ngoại lệ đồng giảng viên rời khóa: kiểm tra chặn GV chính tự rời khóa và xử lý đồng GV chưa chấp nhận/không thuộc khóa",
     "FN", "Integration/API", "Decision Table", "Medium", "GV-A / GV-B",
     "GV-A là giảng viên chính của C_GVA; GV-B không phải đồng GV của C_GVA",
     "Gọi thao tác rời khóa từ GV-A (chính) và GV-B (người ngoài)",
     "1. Kiểm tra cơ chế rời khóa đối với GV-A (GV chính không được tự rời khóa làm mồ côi khóa).\n2. GV-B (không thuộc khóa) thử gọi rời khóa.",
     "GV chính bị chặn không cho rời khóa; người ngoài trả 403/404."),

    ("TC-W-M03-FN-024", "W-M03", "TS-W-M03-006", "UC-A09, UC-A10, UC-A12, UC-A16",
     "Ngoại lệ xét duyệt Admin: duyệt/từ chối/ẩn/hiện khóa ID không tồn tại (404) và chuyển trạng thái sai vòng đời (vd. duyệt thẳng khóa Draft rỗng)",
     "FN", "Integration/API", "State Transition, Error Guessing", "High", "Admin",
     "ADM1 đã đăng nhập; có khóa C_DRAFT_EMPTY đang ở trạng thái Draft",
     "POST /api/v1/admin/courses/999999/{approve,reject,hide,unhide}; POST /api/v1/admin/courses/{C_DRAFT_EMPTY}/approve; POST /unhide trên khóa Draft",
     "1. Gọi approve/reject/hide/unhide cho courseId=999999.\n2. Thử approve khóa đang Draft (chưa gửi duyệt) và unhide khóa đang Draft.\n3. Kiểm tra GET /api/v1/admin/courses lọc/hiển thị đúng trạng thái.",
     "courseId=999999 trả 404; chuyển trạng thái sai vòng đời (Draft -> unhide/approve trái D-13) bị chặn 400/409."),

    ("TC-W-M03-FN-025", "W-M03", "TS-W-M03-007", "UC-A06, UC-A07, UC-A08, D-21",
     "Ngoại lệ quản lý danh mục Admin: trùng tên danh mục, tên rỗng/quá dài, cập nhật/xóa categoryId không tồn tại (404), và kiểm đếm coursesCount chính xác",
     "FN", "Integration/API", "EP, BVA", "Medium", "Admin",
     "ADM1 đã đăng nhập",
     "POST /api/v1/admin/categories tên rỗng / trùng tên; PUT & DELETE /api/v1/admin/categories/999999; GET /api/v1/admin/categories/stats",
     "1. Tạo danh mục tên rỗng và tên trùng danh mục đã có.\n2. Cập nhật và xóa categoryId=999999.\n3. Kiểm tra coursesCount trong /categories/stats khớp SQL COUNT.",
     "Tên rỗng -> 400; categoryId=999999 -> 404; coursesCount khớp tuyệt đối dữ liệu DB."),

    # W-M04: UC-S01, UC-S02, UC-A13, UC-A14, UC-A18, CODE-CPN-01
    ("TC-W-M04-FN-011", "W-M04", "TS-W-M04-001", "UC-S01, UC-S02",
     "Ngoại lệ giỏ hàng: thêm khóa không tồn tại (404), thêm khóa Draft/Hidden, xóa mục không có trong giỏ và Khách mở giỏ chưa đăng nhập",
     "FN", "Integration/API", "EP, Error Guessing", "High", "HV1 / Khách",
     "Có khóa C_DRAFT và C_HIDDEN",
     "POST /api/v1/cart/add/999999, /add/{C_DRAFT}, /add/{C_HIDDEN}; DELETE /api/v1/cart/items/999999; Khách gọi GET /api/v1/cart",
     "1. Khách gọi GET /api/v1/cart -> kiểm tra 401.\n2. HV1 thêm courseId=999999, C_DRAFT, C_HIDDEN.\n3. HV1 xóa itemId=999999.",
     "Khách -> 401; thêm khóa không tồn tại/Draft/Hidden bị chặn 400/404; xóa item không tồn tại xử lý sạch không 500."),

    ("TC-W-M04-FN-012", "W-M04", "TS-W-M04-002", "CODE-CPN-01",
     "Quản lý Coupon phía Admin & ngoại lệ: tạo coupon hợp lệ, chặn tạo coupon trùng mã / % giảm ngoài [1..100] và chặn Student tạo coupon",
     "FN", "Integration/API", "BVA, Decision Table", "Medium", "Admin / HV1",
     "ADM1 và HV1 đã đăng nhập",
     "POST /api/coupons với discountPercentage=0, 101, trùng code; HV1 gọi POST /api/coupons",
     "1. Admin tạo coupon mới hợp lệ và kiểm tra GET /api/coupons.\n2. Admin thử tạo coupon trùng mã và % ngoài biên (0, 101).\n3. HV1 gọi POST /api/coupons và GET /api/coupons.",
     "Admin tạo hợp lệ -> 200; trùng mã / % sai biên -> 400; HV1 bị chặn 403."),

    ("TC-W-M04-FN-013", "W-M04", "TS-W-M04-006", "UC-A13, UC-A14, UC-A18",
     "Ngoại lệ quản trị đơn hàng & đối soát Admin: lọc needsReconciliation=true/false, đối soát ID không tồn tại (404), và kiểm chứng số liệu đơn khớp DB",
     "FN", "Integration/API", "EP, Error Guessing", "High", "Admin",
     "ADM1 đã đăng nhập",
     "GET /api/v1/admin/orders, GET /api/v1/admin/payments?needsReconciliation=true & false; đối soát txId=999999",
     "1. Gọi GET /api/v1/admin/orders đối chiếu tổng số đơn và trạng thái với DB.\n2. Gọi /admin/payments với needsReconciliation=true và false.\n3. Thử đối soát giao dịch không tồn tại (999999).",
     "Bộ lọc needsReconciliation hoạt động đúng; txId không tồn tại trả 404; không trả 500."),

    # W-M05: UC-S08, UC-S09, UC-S19, UC-I21, UC-I22, UC-A15, UC-A17, UC-A19, CODE-QZ-01, CODE-WL-01
    ("TC-W-M05-FN-014", "W-M05", "TS-W-M05-001", "UC-S08, UC-S09",
     "Ngoại lệ ghi danh miễn phí & Khóa học của tôi: khóa không tồn tại / Draft / Hidden trả 404; học viên mới chưa có khóa hiển thị danh sách rỗng đúng chuẩn",
     "FN", "Integration/API", "EP", "Medium", "HV2",
     "HV2 chưa ghi danh khóa nào",
     "POST /api/v1/learning/enrollments/free với courseId=999999, C_DRAFT, C_HIDDEN; GET /api/courses/enrolled",
     "1. HV2 thử ghi danh miễn phí vào courseId=999999, C_DRAFT, C_HIDDEN.\n2. HV2 gọi GET /api/courses/enrolled và mở /my-courses.",
     "Khóa không tồn tại/Draft/Hidden đều trả 404 COURSE_NOT_FOUND; /my-courses hiển thị trạng thái rỗng kèm nút khám phá khóa học."),

    ("TC-W-M05-FN-015", "W-M05", "TS-W-M05-005", "UC-I21, UC-I22",
     "Ngoại lệ Q&A phía Giảng viên: GV-B không thấy câu hỏi khóa của GV-A trong hàng đợi và bị chặn phản hồi câu hỏi của khóa không phụ trách",
     "FN", "Integration/API", "Decision Table", "High", "GV-B",
     "HV1 có câu hỏi Q1 trên khóa C_GVA của GV-A; GV-B không phải GV/đồng GV của C_GVA",
     "GET /api/v1/instructor/questions?courseId={C_GVA} bởi GV-B; POST /api/v1/questions/{Q1}/answers bởi GV-B",
     "1. GV-B mở hàng đợi câu hỏi lọc theo C_GVA.\n2. GV-B thử gửi phản hồi cho câu hỏi Q1 của khóa GV-A.\n3. GV-A gửi phản hồi nội dung rỗng cho Q1.",
     "GV-B không thấy dữ liệu câu hỏi của GV-A và bị chặn 403 khi trả lời; nội dung phản hồi rỗng bị chặn 400."),

    ("TC-W-M05-FN-016", "W-M05", "TS-W-M05-004", "UC-A15, UC-A17, UC-A19",
     "Ngoại lệ kiểm duyệt Admin cho Đánh giá & Q&A: ẩn/khôi phục reviewId, questionId, answerId không tồn tại (404) và chặn vai trò không phải Admin",
     "FN", "Integration/API", "EP, Decision Table", "Medium", "Admin / HV1",
     "ADM1 và HV1 đã đăng nhập",
     "POST /admin/reviews/999999/hide; POST /admin/questions/999999/{hide,restore}; POST /admin/answers/999999/{hide,restore}; HV1 gọi các endpoint trên",
     "1. ADM1 gọi hide/restore với ID=999999.\n2. HV1 và GV-A thử gọi các endpoint kiểm duyệt Admin.",
     "ID không tồn tại trả 404; HV1/GV-A bị chặn 403 FORBIDDEN."),

    ("TC-W-M05-FN-017", "W-M05", "TS-W-M05-006", "UC-S19, CODE-QZ-01",
     "Ngoại lệ làm Quiz: học viên chưa ghi danh bị chặn nộp bài (403); nộp bài rỗng/thiếu đáp án không crash 500",
     "FN", "Integration/API", "Decision Table, Error Guessing", "Medium", "HV2 / HV1",
     "HV2 chưa ghi danh khóa chứa QZ1; HV1 đã ghi danh",
     "1) HV2 gọi POST /api/v1/quiz-attempts/{QZ1}/submit; 2) HV1 nộp danh sách đáp án rỗng []",
     "1. HV2 (chưa ghi danh) thử nộp bài Quiz QZ1.\n2. HV1 nộp mảng đáp án rỗng cho QZ1.",
     "HV2 bị chặn 403 (nếu cho người chưa mua làm quiz -> FAIL + defect Security/Logic); nộp rỗng trả 400 hoặc điểm 0 an toàn, không 500."),

    ("TC-W-M05-FN-018", "W-M05", "TS-W-M05-007", "CODE-WL-01",
     "Ngoại lệ Wishlist: Khách chưa đăng nhập gọi API Wishlist nhận 401; xóa khóa không có trong Wishlist xử lý an toàn không 500",
     "FN", "Integration/API", "Error Guessing", "Low", "Khách / HV1",
     "HV1 có Wishlist trống",
     "Khách gọi GET /api/wishlist; HV1 gọi DELETE /api/wishlist/remove/999999",
     "1. Khách không token gọi GET /api/wishlist và POST /api/wishlist/add/1.\n2. HV1 gọi DELETE xóa khóa không có trong wishlist.",
     "Khách nhận 401 UNAUTHORIZED; HV1 nhận 200/404 an toàn, không lỗi 500."),

    # W-M06: Ensure CODE-PAY-01..04 each have >= 2 TCs (happy + error)
    ("TC-W-M06-FN-006", "W-M06", "TS-W-M06-001", "CODE-PAY-01, CODE-PAY-02, CODE-PAY-04",
     "Ngoại lệ Payout: tham số lọc thu nhập không hợp lệ (year/month), tài khoản nhận tiền thiếu trường bắt buộc, Admin duyệt/từ chối lệnh rút đã xử lý hoặc ID không tồn tại",
     "FN", "Integration/API", "EP, State Transition", "High", "GV-A / Admin",
     "Có 1 lệnh rút WD_DONE đã ở trạng thái paid/rejected",
     "1) GET /me/earnings/entries?month=invalid; 2) PUT /me/payout-account thiếu bankName/accountNumber; 3) Admin gọi /pay & /reject trên WD_DONE và id=999999",
     "1. GV-A gửi filter thu nhập sai định dạng & PUT tài khoản nhận tiền rỗng.\n2. Admin thử /pay và /reject cho lệnh WD_DONE đã xử lý xong và id=999999.",
     "Dữ liệu rỗng/sai -> 400 VALIDATION_ERROR; xử lý lại lệnh đã hoàn tất -> 409 ALREADY_PROCESSED; id=999999 -> 404."),
]
