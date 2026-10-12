# AOP-FIRST ARCHITECTURE RULES

## BẮT BUỘC
Mọi cross-cutting concern PHẢI được triển khai dưới dạng Aspect/Advice,
KHÔNG được viết inline trong business logic.

## Cross-cutting concerns (luôn là Aspect):
- Logging / tracing
- Authentication / Authorization
- Transaction management
- Caching
- Retry / Circuit breaker
- Validation
- Performance metrics
- Audit trail
- Error handling / exception mapping

## CẤM
- KHÔNG viết `logger.info(...)` trong service method → dùng @Loggable
- KHÔNG viết `if (!user.hasRole(...))` trong service → dùng @Secured
- KHÔNG viết `try/catch` cho transaction → dùng @Transactional
- KHÔNG viết `cache.get/put` thủ công → dùng @Cacheable
- KHÔNG viết retry loop → dùng @Retryable

## CẤU TRÚC BẮT BUỘC
src/
  aspects/          # tất cả aspect ở đây
    LoggingAspect
    SecurityAspect
    TransactionAspect
  annotations/      # custom annotation cho aspect
  services/         # CHỈ business logic thuần
  controllers/      # chỉ routing + delegate

## ANTI-PATTERNS — TUYỆT ĐỐI KHÔNG
- Tạo "helper" method để wrap logging quanh business → phải là aspect
- Dùng inheritance để tái sử dụng concern → phải là aspect
- Copy-paste auth check giữa các service → phải là aspect
- Viết decorator "mini-AOP" tự chế khi framework đã có AOP
- Đặt aspect trong thư mục service/controller

## KHI TẠO FEATURE MỚI
1. Xác định cross-cutting concerns liên quan
2. Kiểm tra aspect đã tồn tại chưa (aspects/)
3. Nếu chưa → tạo aspect + annotation mới
4. Business method CHỈ chứa logic nghiệp vụ
5. Áp annotation lên method/class

## CHECKLIST TRƯỚC KHI COMMIT
- [ ] Không có logging inline trong service
- [ ] Không có auth check inline
- [ ] Không có try/catch transaction
- [ ] Mọi concern lặp lại đã được aspect hóa

## SAU KHI VIẾT CODE — BẮT BUỘC TỰ KIỂM TRA
Trước khi trả lời, agent phải tự hỏi:
1. Có logging inline không? → Nếu có, refactor sang @Loggable
2. Có auth check inline không? → @Secured
3. Có try/catch transaction không? → @Transactional
4. Concern này có lặp lại ≥2 nơi không? → Tạo aspect
5. Đã chạy ArchUnit / lint chưa?

Nếu vi phạm → SỬA trước khi trả lời, không giải thích dài dòng.