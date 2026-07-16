package com.videoshare.common.entity;

import java.util.Date;

public class PlaylistVideo {
    private Long    id;
    private Long    playlistId;
    private String  videoId;
    private Integer sortOrder;
    private Date    createTime;

    public Long    getId()         { return id; }
    public void    setId(Long v)             { this.id = v; }
    public Long    getPlaylistId() { return playlistId; }
    public void    setPlaylistId(Long v)     { this.playlistId = v; }
    public String  getVideoId()    { return videoId; }
    public void    setVideoId(String v)      { this.videoId = v; }
    public Integer getSortOrder()  { return sortOrder; }
    public void    setSortOrder(Integer v)   { this.sortOrder = v; }
    public Date    getCreateTime() { return createTime; }
    public void    setCreateTime(Date v)     { this.createTime = v; }
}
