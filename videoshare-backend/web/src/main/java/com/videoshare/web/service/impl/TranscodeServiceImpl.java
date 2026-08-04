package com.videoshare.web.service.impl;

import com.videoshare.common.dto.TranscodeNotifyReq;
import com.videoshare.common.entity.TranscodeJob;
import com.videoshare.web.client.ResourceTranscodeClient;
import com.videoshare.web.mapper.TranscodeJobMapper;
import com.videoshare.web.service.TranscodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TranscodeServiceImpl implements TranscodeService {

    private static final Logger log = LoggerFactory.getLogger(TranscodeServiceImpl.class);

    @Resource private TranscodeJobMapper transcodeJobMapper;
    @Resource private ResourceTranscodeClient resourceTranscodeClient;

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
    public boolean notifyTranscode(String videoId, boolean needCover) {
        TranscodeJob job = transcodeJobMapper.selectByVideoId(videoId);
        if (job == null) {
            log.error("TranscodeJob not found for videoId: {}", videoId);
            return false;
        }
        try {
            TranscodeNotifyReq req = new TranscodeNotifyReq();
            req.setVideoId(videoId);
            req.setNeedCover(needCover);
            resourceTranscodeClient.notifyTranscode(req);
            transcodeJobMapper.updateStatus(job.getJobId(), 1, "");
            log.info("Transcode job handed off to resource: videoId={}", videoId);
            return true;
        } catch (Exception e) {
            // resource 不可用时不阻断发布，job 保持待处理(0)，交由对账任务重试
            log.error("通知 resource 转码失败 videoId={}", videoId, e);
            return false;
        }
    }
}
