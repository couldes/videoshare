package com.videoshare.web.client;

import com.videoshare.common.dto.TranscodeNotifyReq;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * web → resource 转码通知 Feign 客户端（经 Nacos 服务发现按服务名 resource 解析实例）
 */
@FeignClient(name = "resource")
public interface ResourceTranscodeClient {

    /** 通知 resource 执行转码（resource 入队后立即返回） */
    @PostMapping("/resource/transcode")
    ResponseVO<Void> notifyTranscode(@RequestBody TranscodeNotifyReq req);
}
