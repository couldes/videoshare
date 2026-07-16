package com.videoshare.web.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "videos")
public class VideoSearchDocument {

    @Id
    private String  videoId;

    @Field(type = FieldType.Text)
    private String  title;

    @Field(type = FieldType.Text)
    private String  description;

    @Field(type = FieldType.Keyword)
    private String  tags;

    @Field(type = FieldType.Keyword)
    private String  category;

    @Field(type = FieldType.Keyword)
    private String  userId;

    @Field(type = FieldType.Text)
    private String  nickName;

    @Field(type = FieldType.Keyword)
    private String  coverUrl;

    @Field(type = FieldType.Integer)
    private Integer duration;

    @Field(type = FieldType.Long)
    private Long    viewCount;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Integer)
    private Integer favoriteCount;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Date)
    private Date    createTime;

    public String  getVideoId()      { return videoId; }
    public void    setVideoId(String v)     { this.videoId = v; }
    public String  getTitle()        { return title; }
    public void    setTitle(String v)       { this.title = v; }
    public String  getDescription()  { return description; }
    public void    setDescription(String v) { this.description = v; }
    public String  getTags()         { return tags; }
    public void    setTags(String v)        { this.tags = v; }
    public String  getCategory()     { return category; }
    public void    setCategory(String v)    { this.category = v; }
    public String  getUserId()       { return userId; }
    public void    setUserId(String v)      { this.userId = v; }
    public String  getNickName()     { return nickName; }
    public void    setNickName(String v)    { this.nickName = v; }
    public String  getCoverUrl()     { return coverUrl; }
    public void    setCoverUrl(String v)    { this.coverUrl = v; }
    public Integer getDuration()     { return duration; }
    public void    setDuration(Integer v)   { this.duration = v; }
    public Long    getViewCount()    { return viewCount; }
    public void    setViewCount(Long v)     { this.viewCount = v; }
    public Integer getLikeCount()    { return likeCount; }
    public void    setLikeCount(Integer v)  { this.likeCount = v; }
    public Integer getCommentCount() { return commentCount; }
    public void    setCommentCount(Integer v) { this.commentCount = v; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void    setFavoriteCount(Integer v) { this.favoriteCount = v; }
    public Integer getStatus()       { return status; }
    public void    setStatus(Integer v)     { this.status = v; }
    public Date    getCreateTime()   { return createTime; }
    public void    setCreateTime(Date v)    { this.createTime = v; }
}
