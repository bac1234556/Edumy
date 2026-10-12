package com.edumy.aspects;

import com.edumy.annotations.Monitor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MonitoringAspect {
    private final MeterRegistry meterRegistry;

    public MonitoringAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(monitor)")
    public Object monitorExecution(ProceedingJoinPoint joinPoint, Monitor monitor) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(Timer.builder("edumy.method.execution")
                    .tag("operation", monitor.value())
                    .tag("method", joinPoint.getSignature().toShortString())
                    .register(meterRegistry));
        }
    }
}
