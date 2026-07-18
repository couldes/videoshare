package com.videoshare.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MonitorAspect {

    private static final Logger log = LoggerFactory.getLogger(MonitorAspect.class);
    private static final long THRESHOLD_MS = 2000;

    @Around("execution(* com.videoshare.web.service..*.*(..))")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result;
        try {
            result = pjp.proceed();
        } catch (Exception e) {
            long ms = System.currentTimeMillis() - start;
            String signature = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();
            if (ms > THRESHOLD_MS) {
                log.warn("[PERF_WARN] {} | {}ms", signature, ms);
            }
            throw e;
        }

        long ms = System.currentTimeMillis() - start;
        String signature = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();
        if (ms > THRESHOLD_MS) {
            log.warn("[PERF_WARN] {} | {}ms", signature, ms);
        } else {
            log.debug("[PERF] {} | {}ms", signature, ms);
        }

        return result;
    }
}
