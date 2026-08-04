package com.videoshare.resource.client;

import com.videoshare.common.dto.TranscodeJobInfo;
import com.videoshare.common.dto.TranscodeResultReq;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * resource → web 内部接口 Feign 客户端（经 Nacos 服务发现按服务名 web 解析实例）
 */
@FeignClient(name = "web")
public interface WebInnerApiClient {

    /** 拉取转码任务信息（inputPath/outputPath） */
    @GetMapping("/innerApi/video/transcodeJob/{videoId}")
    ResponseVO<TranscodeJobInfo> getTranscodeJob(@PathVariable("videoId") String videoId);

    /** 回写转码结果 */
    @PostMapping("/innerApi/video/transcodeResult")
    ResponseVO<Void> reportResult(@RequestBody TranscodeResultReq req);
}
