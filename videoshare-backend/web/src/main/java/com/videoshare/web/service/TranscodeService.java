package com.videoshare.web.service;

public interface TranscodeService {
    void createJob(String videoId, String inputPath, String outputDir);
    void transcodeAsync(String videoId, String inputPath, String outputDir, boolean needCover);
}
