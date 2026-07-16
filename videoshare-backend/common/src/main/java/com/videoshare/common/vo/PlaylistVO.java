package com.videoshare.common.vo;

import java.util.Date;
import java.util.List;

public class PlaylistVO {
    private Long       playlistId;
    private String     userId;
    private String     title;
    private String     description;
    private String     coverUrl;
    private Integer    isPrivate;
    private Integer    videoCount;
    private List<VideoInfoVO> videoList;
    private Date       createTime;
    private Date       updateTime;

    public Long       getPlaylistId()  { return playlistId; }
    public void       setPlaylistId(Long v)       { this.playlistId = v; }
    public String     getUserId()      { return userId; }
    public void       setUserId(String v)         { this.userId = v; }
    public String     getTitle()       { return title; }
    public void       setTitle(String v)          { this.title = v; }
    public String     getDescription() { return description; }
    public void       setDescription(String v)    { this.description = v; }
    public String     getCoverUrl()    { return coverUrl; }
    public void       setCoverUrl(String v)       { this.coverUrl = v; }
    public Integer    getIsPrivate()   { return isPrivate; }
    public void       setIsPrivate(Integer v)     { this.isPrivate = v; }
    public Integer    getVideoCount()  { return videoCount; }
    public void       setVideoCount(Integer v)    { this.videoCount = v; }
    public List<VideoInfoVO> getVideoList()       { return videoList; }
    public void       setVideoList(List<VideoInfoVO> v) { this.videoList = v; }
    public Date       getCreateTime()  { return createTime; }
    public void       setCreateTime(Date v)       { this.createTime = v; }
    public Date       getUpdateTime()  { return updateTime; }
    public void       setUpdateTime(Date v)       { this.updateTime = v; }
}
