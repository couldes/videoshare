package com.videoshare.web.service;

public interface TranscodeService {
    void createJob(String videoId, String inputPath, String outputDir);
    void notifyTranscode(String videoId, boolean needCover);
}
