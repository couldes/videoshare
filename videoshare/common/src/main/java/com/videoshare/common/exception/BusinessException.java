
package com.videoshare.common.exception;


//自定义业务异常。区分业务规则违规和系统规则违规

public class BusinessException extends RuntimeException {
    // 调用父类构造，把错误信息传给 RuntimeException

    public BusinessException(String message) {
        super(message);  //传递到RuntimeException
    }

}