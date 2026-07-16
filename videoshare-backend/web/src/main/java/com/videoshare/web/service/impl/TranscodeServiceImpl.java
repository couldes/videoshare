package com.videoshare.web.service.impl;

import com.videoshare.common.entity.TranscodeJob;
import com.videoshare.web.component.VideoTranscoder;
import com.videoshare.web.mapper.TranscodeJobMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import com.videoshare.web.service.TranscodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TranscodeServiceImpl implements TranscodeService {

    private static final Logger log = LoggerFactory.getLogger(TranscodeServiceImpl.class);

    @Value("${project.folder:d:/webser/videoshare/}")
    private String projectFolder;

    @Resource private TranscodeJobMapper transcodeJobMapper;
    @Resource private VideoInfoMapper   videoInfoMapper;
    @Resource private VideoTranscoder   videoTranscoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createJob(String videoId, String inputPath, String outputDir) {
        TranscodeJob job = new TranscodeJob();
        job.setVideoId(videoId);
        job.setStatus(0);
        job.setInputPath(inputPath);
        job.setOutputPath(outputDir);
        transcodeJobMapper.insert(job);
    }

    @Override
    @Async("transcodeExecutor")
    public void transcodeAsync(String videoId, String inputPath, String outputDir, boolean needCover) {
        TranscodeJob job = transcodeJobMapper.selectByVideoId(videoId);
        if (job == null) {
            // 事务可能还没提交，重试一次
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            job = transcodeJobMapper.selectByVideoId(videoId);
        }
        if (job == null) {
            log.error("TranscodeJob not found for videoId: {}", videoId);
            return;
        }

        try {
            transcodeJobMapper.updateStatus(job.getJobId(), 1, "");

            int duration = videoTranscoder.transcodeToHLS(inputPath, outputDir);

            String coverPath = null;
            if (needCover) {
                String coverOutput = outputDir + "cover.jpg";
                int midPoint = duration / 2 > 0 ? duration / 2 : 1;
                videoTranscoder.generateThumbnail(inputPath, coverOutput, midPoint);
                coverPath = "/hls/" + videoId + "/cover.jpg";
            }

            String videoUrl = "/hls/" + videoId + "/index.m3u8";
            videoInfoMapper.updateTranscodeResult(videoId, videoUrl, duration, coverPath, 2);

            transcodeJobMapper.updateStatus(job.getJobId(), 2, "");
            log.info("Transcode complete for videoId: {}", videoId);

        } catch (Exception e) {
            log.error("Transcode failed for videoId: {}", videoId, e);
            String errMsg = e.getMessage();
            if (errMsg != null && errMsg.length() > 500) {
                errMsg = errMsg.substring(0, 500);
            }
            transcodeJobMapper.updateStatus(job.getJobId(), 3, errMsg);
        }
    }
}
