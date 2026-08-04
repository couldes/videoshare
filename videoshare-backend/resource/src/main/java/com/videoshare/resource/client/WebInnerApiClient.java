package com.videoshare.resource.client;

import com.videoshare.common.dto.TranscodeJobInfo;
import com.videoshare.common.dto.TranscodeResultReq;
import com.videoshare.common.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * resource → web 内部接口客户端（web 直连，不经过 gateway）
 */
@Component
public class WebInnerApiClient {

    private static final Logger log = LoggerFactory.getLogger(WebInnerApiClient.class);

    @Value("${web.inner-api.base-url:http://127.0.0.1:7070}")
    private String webBaseUrl;

    @Resource
    private RestTemplate restTemplate;

    /** 拉取转码任务信息（inputPath/outputPath）；失败返回 null */
    public TranscodeJobInfo getTranscodeJob(String videoId) {
        String url = webBaseUrl + "/innerApi/video/transcodeJob/" + videoId;
        try {
            ResponseEntity<ResponseVO<TranscodeJobInfo>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ResponseVO<TranscodeJobInfo>>() {});
            ResponseVO<TranscodeJobInfo> vo = resp.getBody();
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

    /** 回写转码结果；失败记日志（重试留待后续） */
    public void reportResult(TranscodeResultReq req) {
        try {
            restTemplate.postForEntity(webBaseUrl + "/innerApi/video/transcodeResult", req, Void.class);
            log.info("reportResult ok: videoId={}, status={}", req.getVideoId(), req.getStatus());
        } catch (Exception e) {
            log.error("reportResult error: videoId={}", req.getVideoId(), e);
        }
    }
}
