// 路径: common/src/main/java/com/videoshare/entity/UserInfo.java
package com.videoshare.common.entity;

import java.util.Date;

/**
 * 用户信息实体类，与数据库表 user_info 字段一一对应
 */
public class UserInfo {
    private String userId;          // 用户唯一ID（10位随机数字）
    private String nickName;        // 昵称
    private String email;           // 邮箱（登录账号）
    private String password;        // 密码（MD5加密后存储）
    private Date joinTime;          // 注册时间
    private Integer status;         // 账号状态（0禁用/1启用）
    private Integer sex;            // 性别（0男/1女/2保密）
    private Integer theme;          // 主题（界面风格）

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Date getJoinTime() { return joinTime; }
    public void setJoinTime(Date joinTime) { this.joinTime = joinTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getSex() { return sex; }
    public void setSex(Integer sex) { this.sex = sex; }

    public Integer getTheme() { return theme; }
    public void setTheme(Integer theme) { this.theme = theme; }
}