package com.videoshare.web.schedule;

import com.videoshare.web.service.TranscodeReconcileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 转码任务对账调度：周期性扫描卡死的 transcode_job（status=0/1），重触发或置失败
 */
@Component
public class TranscodeReconcileTask {

    private static final Logger log = LoggerFactory.getLogger(TranscodeReconcileTask.class);

    @Resource private TranscodeReconcileService reconcileService;

    @Value("${transcode.reconcile.enabled:true}")
    private boolean enabled;

    @Scheduled(fixedRateString = "${transcode.reconcile.fixed-rate-ms:60000}")
    public void reconcile() {
        if (!enabled) {
            return;
        }
        try {
            reconcileService.reconcile();
        } catch (Exception e) {
            log.error("转码对账任务执行失败", e);
        }
    }
}
