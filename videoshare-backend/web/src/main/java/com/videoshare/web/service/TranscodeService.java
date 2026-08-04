package com.videoshare.web.service;

public interface TranscodeService {
    void createJob(String videoId, String inputPath, String outputDir);
    /** 通知 resource 执行转码；成功（已通知并置 status=1）返回 true */
    boolean notifyTranscode(String videoId, boolean needCover);
}
