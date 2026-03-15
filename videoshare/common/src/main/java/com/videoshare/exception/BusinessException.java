// 路径: common/src/main/java/com/videoshare/exception/BusinessException.java
package com.videoshare.exception;

/**
 * 自定义业务异常
 * 用于抛出"业务逻辑错误"，比如"邮箱已存在"
 */
public class BusinessException extends RuntimeException {

    // 调用父类构造，把错误信息传给 RuntimeException
    public BusinessException(String message) {
        super(message);  // message 就是错误提示，比如"邮箱账号已经存在"
    }
}