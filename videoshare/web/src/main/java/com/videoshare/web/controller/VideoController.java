// 路径: web/src/main/java/com/videoshare/web/controller/VideoController.java
package com.videoshare.web.controller;

import com.videoshare.web.service.VideoService;
import com.videoshare.common.vo.ResponseVO;
import com.videoshare.common.query.VideoQuery;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 视频相关接口
 *
 * 接口清单（前端对应 packages/api/src/video.js）：
 *   GET  /video/list          → 首页视频列表（分页+分类）
 *   GET  /video/{videoId}     → 视频详情（含播放地址）
 *   POST /video/upload        → 上传视频（需登录）
 *   POST /video/publish       → 发布视频信息（需登录）
 *   GET  /video/user/{userId} → 某用户的视频列表（个人主页用）
 *   POST /video/action        → 点赞/收藏视频（需登录）
 *   GET  /video/action/check  → 查询当前用户对某视频的点赞/收藏状态
 */
@RestController
@RequestMapping("/video")
public class VideoController extends ABaseController {

    @Resource
    private VideoService videoService;

    /** 首页视频列表（分页 + 分类筛选）*/
    @GetMapping("/list")
    public ResponseVO getVideoList(VideoQuery query) {
        return getSuccessResponseVO(videoService.getVideoList(query));
    }

    /** 视频详情 + 播放量 +1 */
    @GetMapping("/{videoId}")
    public ResponseVO getVideoDetail(@PathVariable String videoId, HttpServletRequest request) {
        String userId = getUserIdFromToken(request); // 可能为 null（未登录也能看）
        return getSuccessResponseVO(videoService.getVideoDetail(videoId, userId));
    }

    /**
     * 上传视频文件（分片上传简化版）
     * 实际生产建议用 MinIO / OSS，这里返回文件访问 URL
     */
    @PostMapping("/upload")
    public ResponseVO uploadVideo(
            @RequestParam MultipartFile file,
            HttpServletRequest request) {
        String userId = requireLogin(request); // 未登录抛异常
        return getSuccessResponseVO(videoService.uploadVideoFile(file, userId));
    }

    /** 发布视频（填写标题、描述、封面、分类后调用）*/
    @PostMapping("/publish")
    public ResponseVO publishVideo(
            @RequestParam String  title,
            @RequestParam(required = false) String description,
            @RequestParam String  coverUrl,
            @RequestParam String  videoUrl,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        videoService.publishVideo(userId, title, description, coverUrl, videoUrl, category, tags);
        return getSuccessResponseVO(null);
    }

    /** 用户发布的视频列表（个人主页）*/
    @GetMapping("/user/{userId}")
    public ResponseVO getUserVideos(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1")  Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        return getSuccessResponseVO(videoService.getUserVideos(userId, pageNum, pageSize));
    }

    /** 点赞 / 收藏 / 取消（actionType: 1=点赞 2=收藏）*/
    @PostMapping("/action")
    public ResponseVO doAction(
            @RequestParam String  videoId,
            @RequestParam Integer actionType,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        boolean active = videoService.toggleAction(userId, videoId, actionType);
        return getSuccessResponseVO(active); // true=操作后为已点赞/收藏 false=取消
    }

    /** 查询当前用户对视频的操作状态（是否点赞/收藏）*/
    @GetMapping("/action/check")
    public ResponseVO checkAction(
            @RequestParam String videoId,
            HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return getSuccessResponseVO(videoService.checkUserAction(userId, videoId));
    }
}
