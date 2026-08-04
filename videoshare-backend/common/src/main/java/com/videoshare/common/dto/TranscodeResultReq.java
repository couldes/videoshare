package com.videoshare.common.dto;

/**
 * resource → web 转码结果回写请求体
 */
public class TranscodeResultReq {

    private String videoId;
    /** 2=完成 3=失败（沿用 transcode_job.status 语义） */
    private Integer status;
    /** 成功时的视频时长（秒），失败为 null */
    private Integer duration;
    /** 成功时封面路径（如 /hls/{videoId}/cover.jpg），无需封面时为 null */
    private String coverPath;
    /** 失败原因（截断到 500 字符），成功为空 */
    private String errorMsg;

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
