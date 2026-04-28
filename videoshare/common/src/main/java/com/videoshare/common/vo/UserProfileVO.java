// 路径: common/src/main/java/com/videoshare/vo/UserProfileVO.java
package com.videoshare.common.vo;

/**
 * 用户个人主页 VO（公开信息）
 * 不含 password / email 等敏感字段
 */
public class UserProfileVO {
    private String  userId;
    private String  nickName;
    private String  bio;           // 个人简介
    private String  avatarUrl;     // 头像URL（暂用首字母代替）
    private Integer sex;
    private Integer currentCoinCount;
    private Long    videoCount;    // 发布视频数
    private Long    followerCount; // 粉丝数
    private Long    followingCount;// 关注数
    private Long    totalLikeCount;// 累计被点赞数
    /** 当前登录用户是否已关注该用户（未登录为 false）*/
    private Boolean isFollowing;

    public String  getUserId()         { return userId; }
    public void    setUserId(String v)         { this.userId = v; }
    public String  getNickName()       { return nickName; }
    public void    setNickName(String v)       { this.nickName = v; }
    public String  getBio()            { return bio; }
    public void    setBio(String v)            { this.bio = v; }
    public String  getAvatarUrl()      { return avatarUrl; }
    public void    setAvatarUrl(String v)      { this.avatarUrl = v; }
    public Integer getSex()            { return sex; }
    public void    setSex(Integer v)           { this.sex = v; }
    public Integer getCurrentCoinCount(){ return currentCoinCount; }
    public void    setCurrentCoinCount(Integer v){ this.currentCoinCount = v; }
    public Long    getVideoCount()     { return videoCount; }
    public void    setVideoCount(Long v)       { this.videoCount = v; }
    public Long    getFollowerCount()  { return followerCount; }
    public void    setFollowerCount(Long v)    { this.followerCount = v; }
    public Long    getFollowingCount() { return followingCount; }
    public void    setFollowingCount(Long v)   { this.followingCount = v; }
    public Long    getTotalLikeCount() { return totalLikeCount; }
    public void    setTotalLikeCount(Long v)   { this.totalLikeCount = v; }
    public Boolean getIsFollowing()    { return isFollowing; }
    public void    setIsFollowing(Boolean v)   { this.isFollowing = v; }
}