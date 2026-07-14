// 路径: common/src/main/java/com/videoshare/vo/VideoInfoVO.java
package com.videoshare.common.vo;

import java.util.Date;

/**
 * 视频信息 VO（前端展示用）
 * 比实体类多了 userInfo（发布者信息），少了内部字段
 */
public class VideoInfoVO {
    private String  videoId;
    private String  title;
    private String  description;
    private String  coverUrl;
    private String  videoUrl;
    private String  duration;      // 格式化后的时长 "8:24"
    private Integer durationSeconds; // 原始秒数
    private String  category;
    private String  tags;
    private Long    viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer status;
    private Date    createTime;

    // 发布者信息（JOIN 查询填充）
    private UserInfoVO userInfo;

    public String  getVideoId()        { return videoId; }
    public void    setVideoId(String v)        { this.videoId = v; }
    public String  getTitle()          { return title; }
    public void    setTitle(String v)          { this.title = v; }
    public String  getDescription()    { return description; }
    public void    setDescription(String v)    { this.description = v; }
    public String  getCoverUrl()       { return coverUrl; }
    public void    setCoverUrl(String v)       { this.coverUrl = v; }
    public String  getVideoUrl()       { return videoUrl; }
    public void    setVideoUrl(String v)       { this.videoUrl = v; }
    public String  getDuration()       { return duration; }
    public void    setDuration(String v)       { this.duration = v; }
    public Integer getDurationSeconds(){ return durationSeconds; }
    public void    setDurationSeconds(Integer v){ this.durationSeconds = v; }
    public String  getCategory()       { return category; }
    public void    setCategory(String v)       { this.category = v; }
    public String  getTags()           { return tags; }
    public void    setTags(String v)           { this.tags = v; }
    public Long    getViewCount()      { return viewCount; }
    public void    setViewCount(Long v)        { this.viewCount = v; }
    public Integer getLikeCount()      { return likeCount; }
    public void    setLikeCount(Integer v)     { this.likeCount = v; }
    public Integer getCommentCount()   { return commentCount; }
    public void    setCommentCount(Integer v)  { this.commentCount = v; }
    public Integer getFavoriteCount()  { return favoriteCount; }
    public void    setFavoriteCount(Integer v) { this.favoriteCount = v; }
    public Integer getStatus()         { return status; }
    public void    setStatus(Integer v)        { this.status = v; }
    public Date    getCreateTime()     { return createTime; }
    public void    setCreateTime(Date v)       { this.createTime = v; }
    public UserInfoVO getUserInfo()    { return userInfo; }
    public void    setUserInfo(UserInfoVO v)   { this.userInfo = v; }
}