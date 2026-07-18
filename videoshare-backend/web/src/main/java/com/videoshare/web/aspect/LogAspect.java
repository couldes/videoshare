package com.videoshare.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Around("execution(* com.videoshare.web.controller..*.*(..))")
    public Object logAccess(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();

        Object result;
        try {
            result = pjp.proceed();
        } catch (Exception e) {
            long ms = System.currentTimeMillis() - start;
            log.info("[ACCESS_LOG] {} {} | {}ms | exception={}", httpMethod, uri, ms, e.getMessage());
            throw e;
        }

        long ms = System.currentTimeMillis() - start;
        if (result != null) {
            log.info("[ACCESS_LOG] {} {} | {}ms | result={}", httpMethod, uri, ms, result.getClass().getSimpleName());
        } else {
            log.info("[ACCESS_LOG] {} {} | {}ms", httpMethod, uri, ms);
        }

        return result;
    }
}
