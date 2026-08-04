package com.videoshare.web.client;

import com.videoshare.common.dto.TranscodeNotifyReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * web → resource 转码通知客户端
 */
@Component
public class ResourceTranscodeClient {

    private static final Logger log = LoggerFactory.getLogger(ResourceTranscodeClient.class);

    @Value("${resource.base-url:http://127.0.0.1:7074}")
    private String resourceBaseUrl;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 通知 resource 执行转码（resource 入队后立即返回）
     */
    public void notifyTranscode(String videoId, boolean needCover) {
        TranscodeNotifyReq req = new TranscodeNotifyReq();
        req.setVideoId(videoId);
        req.setNeedCover(needCover);
        restTemplate.postForEntity(resourceBaseUrl + "/resource/transcode", req, Void.class);
        log.info("已通知 resource 转码 videoId={}, needCover={}", videoId, needCover);
    }
}
