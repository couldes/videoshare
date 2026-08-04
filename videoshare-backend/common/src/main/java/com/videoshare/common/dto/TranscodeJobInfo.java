package com.videoshare.common.dto;

/**
 * web → resource 转码任务信息（resource 据此定位文件）
 */
public class TranscodeJobInfo {

    private String videoId;
    /** 输入视频绝对路径，如 d:/webser/videoshare/videos/{videoId}.mp4 */
    private String inputPath;
    /** 输出目录绝对路径，如 d:/webser/videoshare/hls/{videoId}/ */
    private String outputPath;

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getInputPath() { return inputPath; }
    public void setInputPath(String inputPath) { this.inputPath = inputPath; }

    public String getOutputPath() { return outputPath; }
    public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
}
