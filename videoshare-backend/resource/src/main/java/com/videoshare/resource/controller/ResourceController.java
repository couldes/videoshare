package com.videoshare.resource.controller;

import com.videoshare.common.dto.TranscodeNotifyReq;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.resource.dto.UploadResult;
import com.videoshare.resource.service.ResourceFileService;
import com.videoshare.resource.service.TranscodeWorker;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 资源服务接口：视频/头像上传、静态资源（经 gateway 鉴权）、转码通知
 */
@RestController
@RequestMapping("/resource")
public class ResourceController {

    private static final long AVATAR_MAX = 2 * 1024 * 1024;
    private static final long BACKGROUND_MAX = 5 * 1024 * 1024;

    @Resource private ResourceFileService resourceFileService;
    @Resource private TranscodeWorker     transcodeWorker;

    /** 视频上传（经 gateway 的 UserAuth 过滤器要求登录） */
    @PostMapping("/upload")
    public ResponseVO<UploadResult> upload(@RequestParam MultipartFile file) {
        return ResponseVO.success(resourceFileService.uploadVideo(file));
    }

    /** 头像/背景上传（X-User-Id 由 gateway 注入） */
    @PostMapping("/uploadImage")
    public ResponseVO<Map<String, String>> uploadImage(
            @RequestParam MultipartFile file,
            @RequestParam String type,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException("请先登录");
        }
        if (!"avatar".equals(type) && !"background".equals(type)) {
            throw new BusinessException("type 必须是 avatar 或 background");
        }
        long maxSize = "avatar".equals(type) ? AVATAR_MAX : BACKGROUND_MAX;
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小超过限制");
        }
        return ResponseVO.success(resourceFileService.uploadImage(file, type, userId));
    }

    /** 转码通知（web 直连调用，不经过 gateway；异步执行后立即返回） */
    @PostMapping("/transcode")
    public ResponseVO<Void> transcode(@RequestBody TranscodeNotifyReq req) {
        transcodeWorker.execute(req.getVideoId(), Boolean.TRUE.equals(req.getNeedCover()));
        return ResponseVO.success();
    }
}
