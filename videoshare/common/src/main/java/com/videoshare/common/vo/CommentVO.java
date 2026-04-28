// 路径: common/src/main/java/com/videoshare/vo/CommentVO.java
package com.videoshare.common.vo;

import java.util.Date;
import java.util.List;

/**
 * 评论 VO
 * 顶级评论会携带 replies 列表（最多3条）
 */
public class CommentVO {
    private Long    commentId;
    private String  videoId;
    private String  userId;
    private String  nickName;       // 评论者昵称（JOIN 填充）
    private String  avatarUrl;      // 评论者头像（暂时用首字母代替）
    private Long    pCommentId;
    private String  replyUserId;
    private String  replyNickName;  // 被回复者昵称（JOIN 填充）
    private String  content;
    private Integer likeCount;
    private Integer topType;
    private Integer status;
    private Date    createTime;

    /** 顶级评论携带回复列表 */
    private List<CommentVO> replies;

    public Long    getCommentId()    { return commentId; }
    public void    setCommentId(Long v)     { this.commentId = v; }
    public String  getVideoId()      { return videoId; }
    public void    setVideoId(String v)     { this.videoId = v; }
    public String  getUserId()       { return userId; }
    public void    setUserId(String v)      { this.userId = v; }
    public String  getNickName()     { return nickName; }
    public void    setNickName(String v)    { this.nickName = v; }
    public String  getAvatarUrl()    { return avatarUrl; }
    public void    setAvatarUrl(String v)   { this.avatarUrl = v; }
    public Long    getPCommentId()   { return pCommentId; }
    public void    setPCommentId(Long v)    { this.pCommentId = v; }
    public String  getReplyUserId()  { return replyUserId; }
    public void    setReplyUserId(String v) { this.replyUserId = v; }
    public String  getReplyNickName(){ return replyNickName; }
    public void    setReplyNickName(String v){ this.replyNickName = v; }
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
    public List<CommentVO> getReplies()     { return replies; }
    public void    setReplies(List<CommentVO> v) { this.replies = v; }
}