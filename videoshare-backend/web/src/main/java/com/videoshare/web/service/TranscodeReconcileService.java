package com.videoshare.web.service;

/**
 * 转码任务对账：扫描卡死的 transcode_job，重触发/置失败，作为转码链路的兜底补偿
 */
public interface TranscodeReconcileService {

    /** 执行一轮对账，处置结果写日志 */
    void reconcile();
}
