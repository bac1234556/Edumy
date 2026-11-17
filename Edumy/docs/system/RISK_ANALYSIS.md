# Risk Analysis & Issues Register — EduMy

**Updated**: 2026-11-17

---

## 1. Historical Risks Resolved by Refactoring

Các rủi ro từ giai đoạn kiến trúc cũ (`.NET 10` + `MLService` FastAPI) đã được giải quyết khi chuyển sang **Spring Modulith**:
- ✅ **Loại bỏ ML Service giả lập / fallback tĩnh**: Không còn phụ thuộc `MLService/main.py` trả về giá trị heuristic cố định.
- ✅ **Ranh giới module rõ ràng**: Tách biệt `identity`, `catalog`, `authoring`, `commerce`, `learning`, `payout`, `notification`, `shared` bằng `@ApplicationModule` và `@NamedInterface("api")`.
- ✅ **Hệ thống Email giao dịch thực tế**: Đã tích hợp `Mailpit` SMTP + 10 template HTML gửi bất đồng bộ (`@Async` + `@TransactionalEventListener`) thay cho log console tạm thời.
- ✅ **Có bộ Unit Test nền tảng**: Đã bổ sung `ModularityTest`, `AuthServiceImplTest`, `MobileAuthControllerTest`, `EarningServiceImplTest`, `WithdrawalServiceImplTest`.

---

## 2. Current Technical & Operational Considerations

| ID | Mức độ | Phân hệ | Mô tả | Khuyến nghị xử lý |
|---|---|---|---|---|
| **RISK-CUR-01** | 🟠 HIGH | Security / Config | `application.yml` có giá trị fallback mặc định cho `JWT_SECRET`, `SPRING_DATASOURCE_PASSWORD`, `PAYMENT_WEBHOOK_API_KEY`, `VNPAY_HASH_SECRET` và `SecurityConfig` cấu hình CORS `addAllowedOriginPattern("*")` | Khi triển khai production, bắt buộc ghi đè toàn bộ biến môi trường và thu hẹp `allowedOriginPatterns` theo domain thực tế |
| **RISK-CUR-02** | 🟡 MEDIUM | Authoring (`SpecUseCaseController`) | Một số metadata phụ trợ (`COURSE_REVIEW_NOTES`, `LESSON_EXTRA_CONTENT`, `REVIEW_MODERATION`, `COURSE_CO_INSTRUCTORS`, `LAST_VISITED_LESSON`) đang lưu bằng `ConcurrentHashMap` trong bộ nhớ RAM | Sẽ bị mất khi restart container Backend; nên chuyển thành bảng JPA bền vững trong `EduMyDb` cho môi trường production |
| **RISK-CUR-03** | 🟡 MEDIUM | Database | `spring.jpa.hibernate.ddl-auto: update` tiện cho phát triển nhưng không kiểm soát rollback schema | Nên chuyển sang Flyway/Liquibase migration kết hợp `ddl-auto: validate` khi lên production |
| **RISK-CUR-04** | 🟡 MEDIUM | Backend Tests | Chưa có integration test cho toàn bộ controller của `commerce`, `learning` (Q&A, OpenPDF) và chưa chạy `ApplicationModules.verify()` bắt buộc trong `ModularityTest` | Bổ sung `modules.verify()` trong `ModularityTest` và mở rộng test cho luồng SePay webhook, Q&A và PDF rendering |
| **RISK-CUR-05** | 🟢 LOW | Infra | Docker Backend copy `target/*.jar` từ máy build trước khi đóng gói container (`Backend/Dockerfile`) | Luôn chạy `.\mvnw.cmd clean package` (hoặc build qua `Jenkinsfile`) trước `docker compose build` |
