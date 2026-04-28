
package com.videoshare.web.controller;

import com.videoshare.common.vo.ResponseVO;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.web.component.RedisComponent;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;



public class ABaseController {


    @Resource
    private RedisComponent redisComponent;

    protected <T> ResponseVO<T> getSuccessResponseVO(T data) {
        return ResponseVO.success(data);
    }


    protected ResponseVO<String> getFailureResponseVO(String message) {
        return ResponseVO.error(message);
    }

    /**
     * 从请求头 Authorization 中解析 userId
     * 未登录返回 null（适用于"可匿名访问"的接口）
     */
    protected String getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token == null || token.trim().isEmpty()) return null;
        return redisComponent.getUserIdByToken(token);
    }

    /**
     * 强制要求登录，未登录抛 BusinessException
     * 适用于"必须登录"的接口
     */
    protected String requireLogin(HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        if (userId == null) throw new BusinessException("请先登录");
        return userId;
    }
}