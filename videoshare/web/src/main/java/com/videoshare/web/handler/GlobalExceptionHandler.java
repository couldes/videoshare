package com.videoshare.web.handler;

import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理 — 将业务异常转为 ResponseVO 格式返回，而非 Spring Boot 默认的 HTML 500 错误页
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：返回 status=error 的 JSON */
    @ExceptionHandler(BusinessException.class)
    public ResponseVO<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseVO.error(e.getMessage());
    }

    /** 兜底：未知异常返回 500 */
    @ExceptionHandler(Exception.class)
    public ResponseVO<Void> handleUnknown(Exception e) {
        log.error("系统异常", e);
        String msg = e.getMessage();
        if (msg == null && e.getCause() != null) msg = e.getCause().getMessage();
        return ResponseVO.error("服务器内部错误: " + (msg != null ? msg : e.getClass().getSimpleName()));
    }
}
