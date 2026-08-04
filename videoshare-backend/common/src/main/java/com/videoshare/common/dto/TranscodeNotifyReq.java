package com.videoshare.common.dto;

/**
 * web → resource 转码通知请求体
 */
public class TranscodeNotifyReq {

    private String videoId;
    /** 是否需要在转码时生成封面（用户未上传封面时为 true） */
    private Boolean needCover;

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public Boolean getNeedCover() { return needCover; }
    public void setNeedCover(Boolean needCover) { this.needCover = needCover; }
}
