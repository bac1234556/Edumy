package com.edumy.aspects;

import com.edumy.annotations.Loggable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(com.edumy.annotations.Loggable)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            log.info("{} completed in {} ms",
                    joinPoint.getSignature().toShortString(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            return result;
        } catch (Throwable error) {
            log.error("{} failed", joinPoint.getSignature().toShortString(), error);
            throw error;
        }
    }
}
