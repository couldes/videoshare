package com.videoshare.web.service.impl;

import com.videoshare.common.entity.TranscodeJob;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.web.mapper.TranscodeJobMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import com.videoshare.web.service.TranscodeReconcileService;
import com.videoshare.web.service.TranscodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@RefreshScope
public class TranscodeReconcileServiceImpl implements TranscodeReconcileService {

    private static final Logger log = LoggerFactory.getLogger(TranscodeReconcileServiceImpl.class);

    private static final String MSG_MAXED = "对账重试次数超限，转码未触发";

    @Resource private TranscodeJobMapper transcodeJobMapper;
    @Resource private VideoInfoMapper    videoInfoMapper;
    @Resource private TranscodeService   transcodeService;

    @Value("${transcode.reconcile.pending-stale-minutes:1}")
    private int pendingStaleMinutes;

    @Value("${transcode.reconcile.processing-timeout-minutes:30}")
    private int processingTimeoutMinutes;

    @Value("${transcode.reconcile.max-retries:3}")
    private int maxRetries;

    @Override
    public void reconcile() {
        int reTriggered = reconcileStalePending();
        int maxed = failMaxedPending();
        int timedOut = failStuckProcessing();
        log.info("转码对账完成: 重触发={}, 超限置失败={}, 超时置失败={}", reTriggered, maxed, timedOut);
    }

    /** status=0 卡死超阈值且未达上限 → 认领后重新通知转码 */
    private int reconcileStalePending() {
        int count = 0;
        for (TranscodeJob job : transcodeJobMapper.selectStalePending(pendingStaleMinutes, maxRetries)) {
            if (transcodeJobMapper.claimPending(job.getJobId(), maxRetries) != 1) {
                continue; // 已被并发扫描认领，跳过
            }
            boolean ok = transcodeService.notifyTranscode(job.getVideoId(), deriveNeedCover(job.getVideoId()));
            log.info("转码对账: 重触发 videoId={}, 通知={}", job.getVideoId(), ok ? "成功" : "失败");
            if (ok) {
                count++;
            }
        }
        return count;
    }

    /** status=0 且重试已达上限 → 置失败 */
    private int failMaxedPending() {
        int count = 0;
        for (TranscodeJob job : transcodeJobMapper.selectMaxedPending(maxRetries)) {
            if (transcodeJobMapper.markFailed(job.getJobId(), 0, MSG_MAXED) == 1) {
                count++;
                log.warn("转码对账: 重试超限置失败 videoId={}, jobId={}", job.getVideoId(), job.getJobId());
            }
        }
        return count;
    }

    /** status=1 超过超时阈值仍未完成 → 置失败 */
    private int failStuckProcessing() {
        int count = 0;
        String msg = "对账检测：转码超时（超过 " + processingTimeoutMinutes + " 分钟未完成）";
        for (TranscodeJob job : transcodeJobMapper.selectStaleProcessing(processingTimeoutMinutes)) {
            if (transcodeJobMapper.markFailed(job.getJobId(), 1, msg) == 1) {
                count++;
                log.warn("转码对账: 超时置失败 videoId={}, jobId={}", job.getVideoId(), job.getJobId());
            }
        }
        return count;
    }

    /** 重触发需复现发布时意图：cover_url 为空 → 需要生成封面 */
    private boolean deriveNeedCover(String videoId) {
        VideoInfo video = videoInfoMapper.selectByVideoId(videoId);
        String coverUrl = video == null ? null : video.getCoverUrl();
        return coverUrl == null || coverUrl.isEmpty();
    }
}
