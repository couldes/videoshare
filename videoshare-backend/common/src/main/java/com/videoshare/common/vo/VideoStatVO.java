package com.videoshare.common.vo;

import java.util.Date;

public class VideoStatVO {
    private String videoId;
    private String title;
    private String coverUrl;
    private long   viewCount;
    private int    likeCount;
    private int    commentCount;
    private int    favoriteCount;
    private Date   createTime;

    public String getVideoId()      { return videoId; }
    public void   setVideoId(String v)   { this.videoId = v; }
    public String getTitle()        { return title; }
    public void   setTitle(String v)     { this.title = v; }
    public String getCoverUrl()     { return coverUrl; }
    public void   setCoverUrl(String v)  { this.coverUrl = v; }
    public long   getViewCount()    { return viewCount; }
    public void   setViewCount(long v)   { this.viewCount = v; }
    public int    getLikeCount()    { return likeCount; }
    public void   setLikeCount(int v)    { this.likeCount = v; }
    public int    getCommentCount() { return commentCount; }
    public void   setCommentCount(int v) { this.commentCount = v; }
    public int    getFavoriteCount() { return favoriteCount; }
    public void   setFavoriteCount(int v){ this.favoriteCount = v; }
    public Date   getCreateTime()   { return createTime; }
    public void   setCreateTime(Date v)  { this.createTime = v; }
}
