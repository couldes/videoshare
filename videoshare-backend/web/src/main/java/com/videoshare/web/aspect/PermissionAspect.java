package com.videoshare.web.aspect;

import com.videoshare.common.annotation.RequireLogin;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.web.component.RedisComponent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class PermissionAspect {

    @Resource
    private RedisComponent redisComponent;

    @Around("@annotation(requireLogin)")
    public Object checkLogin(ProceedingJoinPoint pjp, RequireLogin requireLogin) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException("请先登录");
        }
        String userId = redisComponent.getUserIdByToken(token);
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        request.setAttribute("userId", userId);
        return pjp.proceed();
    }
}
