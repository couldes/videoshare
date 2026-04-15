
package com.videoshare.admin.controller;

import com.videoshare.common.vo.ResponseVO;

import javax.servlet.http.HttpServletRequest;


//构建统一响应
//从 request 中取当前管理员账号（拦截器存进去的）

public class ABaseController {

    //成功响应（带数据
    protected <T> ResponseVO<T> success(T data) {
        return ResponseVO.success(data);
    }

    //成功响应（无数据）
    protected ResponseVO<Void> success() {//前面写void就是不返回了，但是ResponseVO的void就是没装东西
        return ResponseVO.success();
    }

    //失败响应
    protected <T> ResponseVO<T> error(String message) {
        return ResponseVO.error(message);
    }//失败的时候几乎从来不需要返回业务数据



    //获取当前登录的管理员账号
     //由拦截器在 preHandle 中通过 request.setAttribute("adminAccount", ...) 存入

    /*
    protected String getAdminAccount(HttpServletRequest request) {
        return (String) request.getAttribute("adminAccount");
    }
     */
}
