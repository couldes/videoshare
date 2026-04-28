// 路径: common/src/main/java/com/videoshare/entity/VideoInfo.java
package com.videoshare.common.entity;

import java.util.Date;

/**
 * 视频信息实体，对应数据库表 video_info
 */
public class VideoInfo {
    private String  videoId;        // 视频ID（随机10位）
    private String  userId;         // 发布者用户ID
    private String  title;          // 标题
    private String  description;    // 描述
    private String  coverUrl;       // 封面图URL
    private String  videoUrl;       // 视频文件URL
    private Integer duration;       // 时长（秒）
    private String  category;       // 分类
    private String  tags;           // 标签（逗号分隔）
    private Long    viewCount;      // 播放量
    private Integer likeCount;      // 点赞数
    private Integer commentCount;   // 评论数
    private Integer favoriteCount;  // 收藏数
    /** 0=审核中 1=已发布 2=已下架 */
    private Integer status;
    private Date    createTime;
    private Date    updateTime;

    public String  getVideoId()       { return videoId; }
    public void    setVideoId(String v)       { this.videoId = v; }
    public String  getUserId()        { return userId; }
    public void    setUserId(String v)        { this.userId = v; }
    public String  getTitle()         { return title; }
    public void    setTitle(String v)         { this.title = v; }
    public String  getDescription()   { return description; }
    public void    setDescription(String v)   { this.description = v; }
    public String  getCoverUrl()      { return coverUrl; }
    public void    setCoverUrl(String v)      { this.coverUrl = v; }
    public String  getVideoUrl()      { return videoUrl; }
    public void    setVideoUrl(String v)      { this.videoUrl = v; }
    public Integer getDuration()      { return duration; }
    public void    setDuration(Integer v)     { this.duration = v; }
    public String  getCategory()      { return category; }
    public void    setCategory(String v)      { this.category = v; }
    public String  getTags()          { return tags; }
    public void    setTags(String v)          { this.tags = v; }
    public Long    getViewCount()     { return viewCount; }
    public void    setViewCount(Long v)       { this.viewCount = v; }
    public Integer getLikeCount()     { return likeCount; }
    public void    setLikeCount(Integer v)    { this.likeCount = v; }
    public Integer getCommentCount()  { return commentCount; }
    public void    setCommentCount(Integer v) { this.commentCount = v; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void    setFavoriteCount(Integer v){ this.favoriteCount = v; }
    public Integer getStatus()        { return status; }
    public void    setStatus(Integer v)       { this.status = v; }
    public Date    getCreateTime()    { return createTime; }
    public void    setCreateTime(Date v)      { this.createTime = v; }
    public Date    getUpdateTime()    { return updateTime; }
    public void    setUpdateTime(Date v)      { this.updateTime = v; }
}