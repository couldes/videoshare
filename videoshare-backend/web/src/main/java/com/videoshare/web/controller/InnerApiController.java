package com.videoshare.web.controller;

import com.videoshare.common.dto.TranscodeJobInfo;
import com.videoshare.common.dto.TranscodeResultReq;
import com.videoshare.common.entity.TranscodeJob;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.web.mapper.TranscodeJobMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 服务间内部接口（仅供 resource 直连调用，Gateway 已拦截 /innerApi 外部访问）
 * 不加 @RequireLogin，允许无 token 的服务间调用
 */
@RestController
@RequestMapping("/innerApi/video")
public class InnerApiController {

    private static final Logger log = LoggerFactory.getLogger(InnerApiController.class);

    @Resource private TranscodeJobMapper transcodeJobMapper;
    @Resource private VideoInfoMapper   videoInfoMapper;

    /** resource 拉取转码任务信息 */
    @GetMapping("/transcodeJob/{videoId}")
    public ResponseVO<TranscodeJobInfo> getTranscodeJob(@PathVariable String videoId) {
        TranscodeJob job = transcodeJobMapper.selectByVideoId(videoId);
        if (job == null) {
            return ResponseVO.error("转码任务不存在: " + videoId);
        }
        TranscodeJobInfo info = new TranscodeJobInfo();
        info.setVideoId(job.getVideoId());
        info.setInputPath(job.getInputPath());
        info.setOutputPath(job.getOutputPath());
        return ResponseVO.success(info);
    }

    /** resource 回写转码结果（不改变 VideoInfo.status，审核状态由 admin 管理） */
    @PostMapping("/transcodeResult")
    public ResponseVO<Void> reportTranscodeResult(@RequestBody TranscodeResultReq req) {
        TranscodeJob job = transcodeJobMapper.selectByVideoId(req.getVideoId());
        if (job == null) {
            log.warn("transcodeResult 未找到任务: videoId={}", req.getVideoId());
            return ResponseVO.error("转码任务不存在: " + req.getVideoId());
        }
        if (Integer.valueOf(2).equals(req.getStatus())) {
            String videoUrl = "/hls/" + req.getVideoId() + "/index.m3u8";
            videoInfoMapper.updateTranscodeResult(req.getVideoId(), videoUrl,
                    req.getDuration(), req.getCoverPath(), null);
            transcodeJobMapper.updateStatus(job.getJobId(), 2, "");
            log.info("转码完成回写: videoId={}, duration={}, cover={}",
                    req.getVideoId(), req.getDuration(), req.getCoverPath());
        } else {
            transcodeJobMapper.updateStatus(job.getJobId(), 3, req.getErrorMsg());
            log.warn("转码失败回写: videoId={}, error={}", req.getVideoId(), req.getErrorMsg());
        }
        return ResponseVO.success();
    }
}
