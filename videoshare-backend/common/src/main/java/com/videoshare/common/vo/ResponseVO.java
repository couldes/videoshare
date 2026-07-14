
package com.videoshare.common.vo;
public class ResponseVO<T> {

    //  响应包装字段 
    private String status;   // "success" 或 "error"
    private String info;     // 错误提示信息
    private T data;          // 通用数据载体

    //  用户信息字段
    private String userId;
    private String nickName;
    private String email;
    private Integer theme;
    private String token;


    //成功，带数据
    public static <T> ResponseVO<T> success(T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("success");
        vo.setData(data);
        return vo;
    }

    //成功，无数据
    public static ResponseVO<Void> success() {
        return success(null);
    }

    //失败，带错误信息
    public static <T> ResponseVO<T> error(String message) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("error");
        vo.setInfo(message);
        return vo;
    }

    //  响应包装字段 Getter/Setter
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    //  用户信息字段 Getter/Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


    public Integer getTheme() { return theme; }
    public void setTheme(Integer theme) { this.theme = theme; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}