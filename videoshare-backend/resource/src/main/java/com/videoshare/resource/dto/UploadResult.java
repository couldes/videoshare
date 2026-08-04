package com.videoshare.resource.dto;

/**
 * 视频上传返回体（与拆解前 web 返回结构一致）
 */
public class UploadResult {

    private String videoId;
    private String videoUrl;
    private Integer duration;

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
