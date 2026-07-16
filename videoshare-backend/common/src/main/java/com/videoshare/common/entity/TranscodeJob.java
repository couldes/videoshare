package com.videoshare.common.entity;

import java.util.Date;

/**
 * 转码任务实体，对应数据库表 transcode_job
 */
public class TranscodeJob {
    private Long    jobId;
    private String  videoId;
    /** 0=待处理 1=处理中 2=完成 3=失败 */
    private Integer status;
    private String  inputPath;
    private String  outputPath;
    private String  errorMsg;
    private Date    createTime;
    private Date    updateTime;

    public Long    getJobId()      { return jobId; }
    public void    setJobId(Long v)       { this.jobId = v; }
    public String  getVideoId()    { return videoId; }
    public void    setVideoId(String v)   { this.videoId = v; }
    public Integer getStatus()     { return status; }
    public void    setStatus(Integer v)   { this.status = v; }
    public String  getInputPath()  { return inputPath; }
    public void    setInputPath(String v) { this.inputPath = v; }
    public String  getOutputPath() { return outputPath; }
    public void    setOutputPath(String v){ this.outputPath = v; }
    public String  getErrorMsg()   { return errorMsg; }
    public void    setErrorMsg(String v)  { this.errorMsg = v; }
    public Date    getCreateTime() { return createTime; }
    public void    setCreateTime(Date v)  { this.createTime = v; }
    public Date    getUpdateTime() { return updateTime; }
    public void    setUpdateTime(Date v)  { this.updateTime = v; }
}
