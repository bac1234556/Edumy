// aspects/LoggableAspect.java
@Aspect
@Component
@Slf4j
public class LoggableAspect {

    @Around("@annotation(Loggable)")
    public Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        var sig = pjp.getSignature();
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log.info("✅ {} hoàn thành ({}ms)", sig, 
                System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            log.error("❌ {} thất bại: {}", sig, e.getMessage());
            throw e;
        }
    }
}