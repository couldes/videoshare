package com.videoshare.common.vo;

public class AnalyticsOverviewVO {
    private long totalViews;
    private long totalLikes;
    private long totalComments;
    private long followerCount;
    private long videoCount;
    private long recentViews;
    private long recentLikes;

    public long getTotalViews()     { return totalViews; }
    public void setTotalViews(long v)     { this.totalViews = v; }
    public long getTotalLikes()     { return totalLikes; }
    public void setTotalLikes(long v)     { this.totalLikes = v; }
    public long getTotalComments()  { return totalComments; }
    public void setTotalComments(long v)  { this.totalComments = v; }
    public long getFollowerCount()  { return followerCount; }
    public void setFollowerCount(long v)  { this.followerCount = v; }
    public long getVideoCount()     { return videoCount; }
    public void setVideoCount(long v)     { this.videoCount = v; }
    public long getRecentViews()    { return recentViews; }
    public void setRecentViews(long v)    { this.recentViews = v; }
    public long getRecentLikes()    { return recentLikes; }
    public void setRecentLikes(long v)    { this.recentLikes = v; }
}
