package com.videoshare.common.entity;

import java.util.Date;

public class WatchHistory {
    private String userId;
    private String videoId;
    private Date watchTime;
    private Date createTime;

    public String getUserId() { return userId; }
    public void setUserId(String v) { this.userId = v; }
    public String getVideoId() { return videoId; }
    public void setVideoId(String v) { this.videoId = v; }
    public Date getWatchTime() { return watchTime; }
    public void setWatchTime(Date v) { this.watchTime = v; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date v) { this.createTime = v; }
}
