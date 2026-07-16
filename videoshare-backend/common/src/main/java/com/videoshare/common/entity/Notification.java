package com.videoshare.common.entity;

import java.util.Date;

public class Notification {
    private Long    id;
    private String  userId;
    private String  fromUserId;
    /** like / comment / reply / follow */
    private String  type;
    private String  videoId;
    private String  content;
    /** 0=未读 1=已读 */
    private Integer isRead;
    private Date    createTime;

    public Long    getId()          { return id; }
    public void    setId(Long v)               { this.id = v; }
    public String  getUserId()      { return userId; }
    public void    setUserId(String v)         { this.userId = v; }
    public String  getFromUserId()  { return fromUserId; }
    public void    setFromUserId(String v)     { this.fromUserId = v; }
    public String  getType()        { return type; }
    public void    setType(String v)           { this.type = v; }
    public String  getVideoId()     { return videoId; }
    public void    setVideoId(String v)        { this.videoId = v; }
    public String  getContent()     { return content; }
    public void    setContent(String v)        { this.content = v; }
    public Integer getIsRead()      { return isRead; }
    public void    setIsRead(Integer v)        { this.isRead = v; }
    public Date    getCreateTime()  { return createTime; }
    public void    setCreateTime(Date v)       { this.createTime = v; }
}
