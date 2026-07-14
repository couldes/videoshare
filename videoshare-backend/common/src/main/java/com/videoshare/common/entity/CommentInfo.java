// 路径: common/src/main/java/com/videoshare/entity/CommentInfo.java
package com.videoshare.common.entity;

import java.util.Date;

/**
 * 评论实体，对应 comment_info 表
 * 支持二级结构：pCommentId=0 表示顶级评论，>0 表示回复
 */
public class CommentInfo {
    private Long    commentId;     // 评论ID（自增）
    private String  videoId;       // 所属视频ID
    private String  userId;        // 评论者ID
    private Long    pCommentId;    // 父评论ID（0=顶级）
    private String  replyUserId;   // 被回复者ID（回复时填写）
    private String  content;       // 评论内容
    private Integer likeCount;     // 点赞数
    /** 0=普通 1=置顶 */
    private Integer topType;
    /** 0=待审核 1=已通过 2=已删除 */
    private Integer status;
    private Date    createTime;

    public Long    getCommentId()    { return commentId; }
    public void    setCommentId(Long v)     { this.commentId = v; }
    public String  getVideoId()      { return videoId; }
    public void    setVideoId(String v)     { this.videoId = v; }
    public String  getUserId()       { return userId; }
    public void    setUserId(String v)      { this.userId = v; }
    public Long    getPCommentId()   { return pCommentId; }
    public void    setPCommentId(Long v)    { this.pCommentId = v; }
    public String  getReplyUserId()  { return replyUserId; }
    public void    setReplyUserId(String v) { this.replyUserId = v; }
    public String  getContent()      { return content; }
    public void    setContent(String v)     { this.content = v; }
    public Integer getLikeCount()    { return likeCount; }
    public void    setLikeCount(Integer v)  { this.likeCount = v; }
    public Integer getTopType()      { return topType; }
    public void    setTopType(Integer v)    { this.topType = v; }
    public Integer getStatus()       { return status; }
    public void    setStatus(Integer v)     { this.status = v; }
    public Date    getCreateTime()   { return createTime; }
    public void    setCreateTime(Date v)    { this.createTime = v; }
}