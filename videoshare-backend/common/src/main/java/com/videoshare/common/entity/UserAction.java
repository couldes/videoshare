// 路径: common/src/main/java/com/videoshare/entity/UserAction.java
package com.videoshare.common.entity;

import java.util.Date;

/**
 * 用户行为实体，对应 user_action 表
 *
 * actionType：
 *   1 = 点赞视频
 *   2 = 收藏视频
 *   3 = 点赞评论
 *
 * 用一张表统一管理，targetId 根据 actionType 对应
 * 不同的目标（视频ID 或 评论ID）。
 */
public class UserAction {
    private Long    actionId;
    private String  userId;
    private String  targetId;    // 视频ID 或 评论ID（评论ID转成字符串）
    private Integer actionType;  // 1=点赞视频 2=收藏视频 3=点赞评论
    private Date    createTime;

    // 兼容旧代码的常量
    public static final int TYPE_LIKE_VIDEO     = 1;
    public static final int TYPE_FAVORITE_VIDEO = 2;
    public static final int TYPE_LIKE_COMMENT   = 3;

    public Long    getActionId()  { return actionId; }
    public void    setActionId(Long v)    { this.actionId = v; }
    public String  getUserId()    { return userId; }
    public void    setUserId(String v)    { this.userId = v; }
    public String  getTargetId()  { return targetId; }
    public void    setTargetId(String v)  { this.targetId = v; }
    public Integer getActionType(){ return actionType; }
    public void    setActionType(Integer v){ this.actionType = v; }
    public Date    getCreateTime(){ return createTime; }
    public void    setCreateTime(Date v)  { this.createTime = v; }
}