
package com.videoshare.web.controller;

import com.videoshare.common.vo.ResponseVO;


public class ABaseController {


    protected <T> ResponseVO<T> getSuccessResponseVO(T data) {
        return ResponseVO.success(data);
    }


    protected ResponseVO<String> getFailureResponseVO(String message) {
        return ResponseVO.error(message);
    }
}