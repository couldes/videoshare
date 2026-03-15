// 路径: web/src/main/java/com/videoshare/web/controller/ABaseController.java
package com.videoshare.web.controller;

import com.videoshare.web.vo.ResponseVO;


public class ABaseController {


    protected <T> ResponseVO<T> getSuccessResponseVO(T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("success");  // 状态标记成功
        vo.setData(data);         // 把数据装进去
        return vo;
    }


    protected ResponseVO<String> getFailureResponseVO(String message) {
        ResponseVO<String> vo = new ResponseVO<>();
        vo.setStatus("error");    // 状态标记失败
        vo.setInfo(message);      // 把错误信息装进去
        return vo;
    }
}