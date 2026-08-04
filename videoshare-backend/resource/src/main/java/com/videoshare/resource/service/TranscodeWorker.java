package com.videoshare.resource.service;

import com.videoshare.common.dto.TranscodeJobInfo;
import com.videoshare.common.dto.TranscodeResultReq;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.resource.client.WebInnerApiClient;
import com.videoshare.resource.component.VideoTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 转码异步编排：拉任务信息 → ffmpeg → 回调 web 回写结果
 */
@Component
public class TranscodeWorker {

    private static final Logger log = LoggerFactory.getLogger(TranscodeWorker.class);
    private static final int MAX_ERROR_MSG_LEN = 500;

    @Resource private WebInnerApiClient webInnerApiClient;
    @Resource private VideoTranscoder  videoTranscoder;

    @Async("transcodeExecutor")
    public void execute(String videoId, boolean needCover) {
        TranscodeJobInfo job = fetchJob(videoId);
        if (job == null) {
            log.error("转码任务信息获取失败 videoId={}", videoId);
            reportResult(videoId, 3, null, null, "转码任务信息获取失败");
            return;
        }
        try {
            int duration = videoTranscoder.transcodeToHLS(job.getInputPath(), job.getOutputPath());

            String coverPath = null;
            if (needCover) {
                videoTranscoder.generateThumbnail(job.getInputPath(), job.getOutputPath() + "cover.jpg", duration / 2);
                coverPath = "/hls/" + videoId + "/cover.jpg";
            }

            reportResult(videoId, 2, duration, coverPath, "");
            log.info("转码完成 videoId={}, duration={}", videoId, duration);
        } catch (Exception e) {
            log.error("转码失败 videoId={}", videoId, e);
            String msg = e.getMessage();
            if (msg != null && msg.length() > MAX_ERROR_MSG_LEN) {
                msg = msg.substring(0, MAX_ERROR_MSG_LEN);
            }
            reportResult(videoId, 3, null, null, msg);
        }
    }

    /** 经 Feign 拉取任务并解包 ResponseVO；失败返回 null（沿用直连时代语义） */
    private TranscodeJobInfo fetchJob(String videoId) {
        try {
            ResponseVO<TranscodeJobInfo> vo = webInnerApiClient.getTranscodeJob(videoId);
            if (vo == null || !"success".equals(vo.getStatus()) || vo.getData() == null) {
                log.warn("getTranscodeJob failed: videoId={}, status={}, info={}",
                        videoId, vo == null ? null : vo.getStatus(), vo == null ? null : vo.getInfo());
                return null;
            }
            return vo.getData();
        } catch (Exception e) {
            log.error("getTranscodeJob error: videoId={}", videoId, e);
            return null;
        }
    }

    private void reportResult(String videoId, Integer status, Integer duration, String coverPath, String errorMsg) {
        TranscodeResultReq req = new TranscodeResultReq();
        req.setVideoId(videoId);
        req.setStatus(status);
        req.setDuration(duration);
        req.setCoverPath(coverPath);
        req.setErrorMsg(errorMsg);
        try {
            webInnerApiClient.reportResult(req);
            log.info("reportResult ok: videoId={}, status={}", req.getVideoId(), req.getStatus());
        } catch (Exception e) {
            log.error("reportResult error: videoId={}", req.getVideoId(), e);
        }
    }
}
