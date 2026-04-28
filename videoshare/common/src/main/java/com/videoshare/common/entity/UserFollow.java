// 路径: common/src/main/java/com/videoshare/entity/UserFollow.java
package com.videoshare.common.entity;

import java.util.Date;

/** 用户关注关系，对应 user_follow 表 */
public class UserFollow {
    private Long   id;
    private String userId;        // 关注者
    private String followUserId;  // 被关注者
    private Date   createTime;

    public Long   getId()           { return id; }
    public void   setId(Long v)             { this.id = v; }
    public String getUserId()       { return userId; }
    public void   setUserId(String v)       { this.userId = v; }
    public String getFollowUserId() { return followUserId; }
    public void   setFollowUserId(String v) { this.followUserId = v; }
    public Date   getCreateTime()   { return createTime; }
    public void   setCreateTime(Date v)     { this.createTime = v; }
}