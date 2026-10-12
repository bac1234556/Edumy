# PHẦN 3: THIẾT KẾ LUỒNG NGHIỆP VỤ CHI TIẾT
*(Business Process Workflows & Interaction Diagrams)*

Tài liệu này mô hình hóa chi tiết các luồng nghiệp vụ xương sống của hệ thống Edumy thông qua các sơ đồ tuần tự (Sequence Diagrams) và lưu đồ xử lý (Activity Flowcharts).

---

## 3.1. LUỒNG XÁC THỰC & LÀM MỚI PHIÊN HAI LỚP TOKEN (JWT + REFRESH TOKEN ROTATION)

Hệ thống kết hợp giữa **JWT Access Token** (lưu trên Client memory) và **Refresh Token** (lưu trong Cookie an toàn `HttpOnly`, `Secure`). Khi người dùng đăng nhập hoặc gia hạn phiên, hệ thống áp dụng cơ chế xoay vòng Refresh Token (Token Rotation) nhằm vô hiệu hóa token cũ, chống tấn công giả mạo phiên.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Trình duyệt (React Client)
    participant AuthCtrl as Auth Controller (Spring Boot)
    participant Security as BCrypt / JwtProvider
    participant DB as SQL Server (EduMyDb)

    Note over Client, DB: GIAI ĐOẠN 1: ĐĂNG NHẬP (LOGIN)
    Client->>AuthCtrl: POST /api/Auth/login {email, password}
    AuthCtrl->>DB: SELECT * FROM Users WHERE Email = ?
    DB-->>AuthCtrl: Trả về User (PasswordHash, IsActive)
    
    alt Tài khoản bị khóa hoặc không tồn tại
        AuthCtrl-->>Client: 401 Unauthorized ("Tài khoản không hợp lệ hoặc đã bị khóa")
    else Mật khẩu không khớp
        AuthCtrl->>Security: BCrypt.matches(password, hash) -> False
        AuthCtrl-->>Client: 401 Unauthorized ("Sai thông tin đăng nhập")
    else Xác thực thành công
        AuthCtrl->>Security: Sinh JWT Access Token (hạn 7 ngày)
        AuthCtrl->>Security: Sinh chuỗi ngẫu nhiên Refresh Token (64 bytes)
        AuthCtrl->>DB: INSERT INTO RefreshTokens (UserId, Token, Expires, Created)
        AuthCtrl-->>Client: Set-Cookie: refreshToken=... (HttpOnly, Secure, SameSite=Lax)<br/>Body: { token: JWT, email, role, fullName }
    end

    Note over Client, DB: GIAI ĐOẠN 2: LÀM MỚI PHIÊN (REFRESH TOKEN ROTATION)
    Client->>AuthCtrl: POST /api/Auth/refresh-token (Đính kèm Cookie refreshToken)
    AuthCtrl->>DB: SELECT * FROM RefreshTokens WHERE Token = ?
    DB-->>AuthCtrl: Bản ghi RefreshToken
    
    alt Token đã hết hạn hoặc đã bị thu hồi (Revoked != null)
        AuthCtrl-->>Client: 401 Unauthorized ("Token đã hết hạn hoặc không hợp lệ")
    else Token hợp lệ
        AuthCtrl->>DB: UPDATE RefreshTokens SET Revoked = NOW(), ReplacedByToken = newRefreshToken WHERE Id = ?
        AuthCtrl->>Security: Sinh JWT mới & Sinh Refresh Token mới
        AuthCtrl->>DB: INSERT INTO RefreshTokens (UserId, newRefreshToken, Expires, Created)
        AuthCtrl-->>Client: Set-Cookie: refreshToken=newRefreshToken<br/>Body: { token: newJWT, email, role }
    end
```

---

## 3.2. LUỒNG GIẢNG VIÊN TẠO KHÓA HỌC, AI KIỂM DUYỆT & ADMIN DUYỆT XUẤT BẢN

Luồng xuất bản khóa học bảo vệ nền tảng khỏi nội dung xấu độc thông qua bước quét tự động từ AI Microservice trước khi chuyển sang cho Quản trị viên duyệt lần cuối:

```mermaid
sequenceDiagram
    autonumber
    actor Instructor as Giảng viên (Instructor)
    participant Backend as Backend CoursesController
    participant AIService as FastAPI ML Microservice
    participant DB as SQL Server
    actor Admin as Quản trị viên (Admin)

    Note over Instructor, Admin: BƯỚC 1: KHỞI TẠO NỘI DUNG KHÓA HỌC
    Instructor->>Backend: POST /api/Courses {title, description, price, level}
    Backend->>Backend: Tự động tạo SEO Slug: slugify(title) + randomUUID()
    Backend->>DB: INSERT INTO Courses (Status = 'Draft', NeedsReanalysis = false)
    DB-->>Backend: CourseId
    Backend-->>Instructor: Tạo khóa học thành công (CourseId)

    Instructor->>Backend: POST /api/courses/{id}/sections & /lessons (Thêm giáo trình)
    Backend->>DB: INSERT INTO CourseSections, Lessons

    Note over Instructor, Admin: BƯỚC 2: KÍCH HOẠT QUÉT AI & KIỂM DUYỆT
    Instructor->>Backend: POST /api/Courses/{id}/status ("Analyzing")
    Backend->>AIService: POST /classify/course {title, description}
    AIService->>AIService: 1. Text Preprocessing & TF-IDF/MLP<br/>2. Kiểm tra từ khóa độc hại (Toxicity)<br/>3. Chấm điểm chất lượng (Quality Score)
    AIService-->>Backend: Trả về: {predictedCategory, confidence, is_toxic, riskLevel, tags}
    
    Backend->>DB: INSERT INTO CourseMlAnalyses, CourseMlAnalysisTags
    
    alt Nội dung vi phạm / Rủi ro cao (is_toxic == true HOẶC riskLevel == 'High')
        Backend->>DB: UPDATE Courses SET Status = 'NeedsReview' WHERE CourseId = ?
        Backend-->>Instructor: Cảnh báo: Khóa học bị gắn cờ cần kiểm duyệt thủ công
        Admin->>Backend: POST /api/Admin/ml-analyses/{id}/approve (hoặc /override)
        Backend->>DB: Ghi đè trạng thái phân tích sang Approved/Overridden
    else Nội dung an toàn
        Backend->>DB: UPDATE Courses SET Status = 'PendingApproval' WHERE CourseId = ?
        Backend-->>Instructor: Khóa học đã chuyển sang trạng thái Chờ duyệt
    end

    Note over Instructor, Admin: BƯỚC 3: ADMIN DUYỆT XUẤT BẢN
    Admin->>Backend: PUT /api/Admin/courses/{id}/status ("Published")
    Backend->>DB: UPDATE Courses SET Status = 'Published', UpdatedAt = NOW()
    Backend-->>Admin: Khóa học đã chính thức mở bán trên sàn
```

---

## 3.3. LUỒNG GIỎ HÀNG, ĐẶT HÀNG & THANH TOÁN TỰ ĐỘNG GHI DANH

Mô tả trọn vẹn chu trình thương mại điện tử từ lúc thêm khóa học vào giỏ, áp mã giảm giá, chuyển hướng thanh toán đến khi nhận phản hồi và kích hoạt quyền học:

```mermaid
sequenceDiagram
    autonumber
    actor Student as Học viên (Student)
    participant CartAPI as CartController
    participant OrderAPI as OrdersController
    participant PaymentAPI as PaymentController / Stripe
    participant DB as SQL Server

    Student->>CartAPI: POST /api/Cart/add/{courseId}
    CartAPI->>DB: Kiểm tra: Course tồn tại? Status = 'Published'? Chưa ghi danh?
    CartAPI->>DB: INSERT INTO CartItems (CartId, CourseId, AddedAt)
    CartAPI-->>Student: Đã thêm khóa học vào giỏ hàng

    Student->>OrderAPI: POST /api/Orders/checkout {couponCode: "WELCOME20"}
    OrderAPI->>DB: Lấy CartItems & tính tổng tiền (totalAmount)
    opt Có mã Coupon
        OrderAPI->>DB: SELECT * FROM Coupons WHERE Code = 'WELCOME20' AND IsActive = 1
        OrderAPI->>OrderAPI: Giảm trừ tiền: totalAmount = totalAmount - discount
    end
    OrderAPI->>DB: INSERT INTO Orders (UserId, TotalAmount, Status = 'Pending')
    OrderAPI->>DB: INSERT INTO OrderItems (OrderId, CourseId, Price)
    DB-->>OrderAPI: OrderId
    OrderAPI-->>Student: Trả về OrderId & Redirect URL cổng thanh toán

    Note over Student, DB: GIAI ĐOẠN THANH TOÁN & GHI DANH
    Student->>PaymentAPI: Thực hiện thanh toán (Stripe / Mock Gateway)
    PaymentAPI->>PaymentAPI: Xử lý trừ tiền thẻ
    PaymentAPI->>PaymentAPI: POST /api/Payment/callback {orderId, success: true}
    
    alt Thanh toán Thành công
        PaymentAPI->>DB: UPDATE Orders SET Status = 'Completed' WHERE OrderId = ?
        loop Cho từng CourseId trong OrderItems
            PaymentAPI->>DB: INSERT INTO Enrollments (UserId, CourseId, ProgressPercentage = 0)
            PaymentAPI->>DB: UPDATE Courses SET StudentCount = StudentCount + 1 WHERE CourseId = ?
        end
        PaymentAPI->>DB: DELETE FROM CartItems WHERE CartId = (SELECT Id FROM Carts WHERE UserId = ?)
        PaymentAPI-->>Student: Thanh toán thành công! Khóa học đã được mở vào "My Learning"
    else Thanh toán Thất bại / Người dùng hủy
        PaymentAPI->>DB: UPDATE Orders SET Status = 'Cancelled' WHERE OrderId = ?
        PaymentAPI-->>Student: Đơn hàng bị hủy, giỏ hàng vẫn được giữ nguyên
    end
```

---

## 3.4. LUỒNG HỌC TẬP, LÀM BÀI QUIZ TRẮC NGHIỆM & CẤP CHỨNG CHỈ TỰ ĐỘNG

Mô hình hóa quá trình người học tương tác với bài giảng video, tích lũy tiến trình hoàn thành, vượt qua bài kiểm tra đánh giá và nhận chứng chỉ tự động:

```mermaid
sequenceDiagram
    autonumber
    actor Student as Học viên (Student)
    participant CoursesAPI as CoursesController
    participant QuizAPI as QuizAttemptsController
    participant DB as SQL Server

    Note over Student, DB: GIAI ĐOẠN 1: HỌC TẬP & TÍCH LŨY TIẾN ĐỘ
    Student->>CoursesAPI: POST /api/Courses/{courseId}/lessons/{lessonId}/complete
    CoursesAPI->>DB: Kiểm tra Enrollment của User với Course
    CoursesAPI->>DB: INSERT INTO LessonProgresses (UserId, CourseId, LessonId, CompletedAt)
    CoursesAPI->>DB: Đếm: TotalLessons của khóa & CompletedLessons của User
    
    alt Đã hoàn thành 100% bài học (CompletedLessons == TotalLessons)
        CoursesAPI->>DB: UPDATE Enrollments SET ProgressPercentage = 100, CompletedAt = NOW()
        CoursesAPI->>DB: Kiểm tra bảng Certificates
        opt Chưa có chứng chỉ
            CoursesAPI->>DB: INSERT INTO Certificates (UserId, CourseId, CertificateUrl = UUID(), IssuedAt = NOW())
        end
        CoursesAPI-->>Student: 100% hoàn thành! Tự động cấp Chứng chỉ thành công!
    else Tiến độ < 100%
        CoursesAPI->>DB: UPDATE Enrollments SET ProgressPercentage = (Completed / Total) * 100
        CoursesAPI-->>Student: Ghi nhận hoàn thành bài học thành công
    end

    Note over Student, DB: GIAI ĐOẠN 2: LÀM BÀI KIỂM TRA TRẮC NGHIỆM (QUIZ)
    Student->>QuizAPI: POST /api/QuizAttempts/submit {quizId, selectedAnswers: {q1: a2, q2: a1}}
    QuizAPI->>DB: SELECT * FROM Questions WHERE QuizId = ?
    QuizAPI->>DB: SELECT * FROM Answers WHERE QuestionId IN (...) AND IsCorrect = 1
    QuizAPI->>QuizAPI: So khớp từng câu, tính tổng điểm (Score / TotalPoints)
    QuizAPI->>DB: INSERT INTO QuizAttempts (UserId, QuizId, Score, StartedAt, CompletedAt)
    QuizAPI->>DB: INSERT INTO QuizAttemptAnswers (QuizAttemptId, QuestionId, AnswerId)
    QuizAPI-->>Student: Trả về kết quả: Điểm số, Đạt/Không đạt, Lời giải thích từng câu
```

---

## 3.5. LUỒNG ĐÁNH GIÁ KHÓA HỌC & PHÂN TÍCH CẢM XÚC NHẬN XÉT BẰNG AI

```mermaid
sequenceDiagram
    autonumber
    actor Student as Học viên (Student)
    participant ReviewAPI as ReviewsController
    participant AIService as FastAPI ML (BiLSTM Sentiment)
    participant DB as SQL Server

    Student->>ReviewAPI: POST /api/Courses/{courseId}/reviews {rating: 5, comment: "Khóa học rất bổ ích và dễ hiểu!"}
    ReviewAPI->>DB: Kiểm tra: User đã Enrolled khóa học chưa? Đã từng Review chưa?
    
    ReviewAPI->>AIService: POST /analyze/sentiment {text: comment}
    AIService->>AIService: Tokenize văn bản & Đưa qua mô hình BiLSTM
    AIService-->>ReviewAPI: Trả về: {label: "Positive", score: 0.94}
    
    ReviewAPI->>DB: INSERT INTO Reviews (UserId, CourseId, Rating, Comment, SentimentLabel = 'Positive', SentimentScore = 0.94)
    ReviewAPI->>DB: Tính toán lại AverageRating của Course
    ReviewAPI->>DB: UPDATE Courses SET AverageRating = (AVG rating) WHERE CourseId = ?
    ReviewAPI-->>Student: Đánh giá thành công! Cảm ơn nhận xét của bạn.
```
