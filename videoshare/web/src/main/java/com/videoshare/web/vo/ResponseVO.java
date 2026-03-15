
package com.videoshare.web.vo;
//额外塞常用用户信息 → 减少接口调用、方便前端展示
public class ResponseVO<T> {

    // ===== 响应包装字段 =====
    private String status;   // "success" 或 "error"
    private String info;     // 错误提示信息
    private T data;          // 通用数据载体

    // ===== 用户信息字段（补回来）=====
    private String userId;
    private String nickName;
    private String email;
    private Integer sex;
    private Integer theme;
    private String token;

    // ===== 响应包装字段 Getter/Setter =====
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    // ===== 用户信息字段 Getter/Setter（补回来）=====
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getSex() { return sex; }
    public void setSex(Integer sex) { this.sex = sex; }

    public Integer getTheme() { return theme; }
    public void setTheme(Integer theme) { this.theme = theme; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}