// 路径: admin/src/main/java/com/videoshare/admin/vo/AdminUserInfoVO.java
package com.videoshare.common.vo;

import java.util.Date;

/**
 * 管理端用户信息 VO
 *
 * 比用户端 VO 多一些管理字段（joinTime, status 等）
 * 管理员需要看到这些信息来管理用户
 */
public class UserInfoVO {

    private String  userId;           // 用户 ID
    private String  nickName;         // 昵称
    private String  email;            // 邮箱
    private Integer sex;              // 性别（0男/1女/2保密）
    private String  sexDesc;          // 性别描述（"男"/"女"/"保密"）
    private Integer status;           // 状态（0禁用/1启用）
    private String  statusDesc;       // 状态描述（"启用"/"禁用"）
    private Date    joinTime;         // 注册时间

    // ===== Getters & Setters =====
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getSex() { return sex; }
    public void setSex(Integer sex) { this.sex = sex; }

    public String getSexDesc() { return sexDesc; }
    public void setSexDesc(String sexDesc) { this.sexDesc = sexDesc; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusDesc() { return statusDesc; }
    public void setStatusDesc(String statusDesc) { this.statusDesc = statusDesc; }

    public Date getJoinTime() { return joinTime; }
    public void setJoinTime(Date joinTime) { this.joinTime = joinTime; }
}
