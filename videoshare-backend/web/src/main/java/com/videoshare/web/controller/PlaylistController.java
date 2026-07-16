package com.videoshare.web.controller;

import com.videoshare.common.vo.ResponseVO;
import com.videoshare.web.service.PlaylistService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 播放列表接口
 *   POST   /playlist/create              → 创建播放列表
 *   POST   /playlist/update              → 更新播放列表
 *   DELETE /playlist/{playlistId}        → 删除播放列表
 *   POST   /playlist/{playlistId}/add-video    → 添加视频
 *   DELETE /playlist/{playlistId}/video/{videoId} → 移除视频
 *   POST   /playlist/{playlistId}/reorder      → 重新排序
 *   GET    /playlist/{playlistId}        → 播放列表详情
 *   GET    /playlist/user/{userId}       → 用户的播放列表
 */
@RestController
@RequestMapping("/playlist")
public class PlaylistController extends ABaseController {

    @Resource
    private PlaylistService playlistService;

    /** 创建播放列表 */
    @PostMapping("/create")
    public ResponseVO createPlaylist(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(
                playlistService.createPlaylist(userId, title, description));
    }

    /** 更新播放列表 */
    @PostMapping("/update")
    public ResponseVO updatePlaylist(
            @RequestParam Long playlistId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer isPrivate,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(
                playlistService.updatePlaylist(userId, playlistId, title, description, isPrivate));
    }

    /** 删除播放列表 */
    @DeleteMapping("/{playlistId}")
    public ResponseVO deletePlaylist(
            @PathVariable Long playlistId,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        playlistService.deletePlaylist(userId, playlistId);
        return getSuccessResponseVO(null);
    }

    /** 添加视频到播放列表 */
    @PostMapping("/{playlistId}/add-video")
    public ResponseVO addVideo(
            @PathVariable Long playlistId,
            @RequestParam String videoId,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        playlistService.addVideo(userId, playlistId, videoId);
        return getSuccessResponseVO(null);
    }

    /** 从播放列表移除视频 */
    @DeleteMapping("/{playlistId}/video/{videoId}")
    public ResponseVO removeVideo(
            @PathVariable Long playlistId,
            @PathVariable String videoId,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        playlistService.removeVideo(userId, playlistId, videoId);
        return getSuccessResponseVO(null);
    }

    /** 重新排序 */
    @PostMapping("/{playlistId}/reorder")
    public ResponseVO reorderVideos(
            @PathVariable Long playlistId,
            @RequestParam String videoIds,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        // 前端传入逗号分隔的 videoId 列表
        java.util.List<String> ids = java.util.Arrays.asList(videoIds.split(","));
        playlistService.reorderVideos(userId, playlistId, ids);
        return getSuccessResponseVO(null);
    }

    /** 播放列表详情 */
    @GetMapping("/{playlistId}")
    public ResponseVO getPlaylistDetail(
            @PathVariable Long playlistId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        String currentUserId = getUserIdFromToken(request);
        return getSuccessResponseVO(
                playlistService.getPlaylistDetail(playlistId, currentUserId, pageNum, pageSize));
    }

    /** 用户的播放列表 */
    @GetMapping("/user/{userId}")
    public ResponseVO getUserPlaylists(
            @PathVariable String userId,
            HttpServletRequest request) {
        String currentUserId = getUserIdFromToken(request);
        return getSuccessResponseVO(
                playlistService.getUserPlaylists(userId, currentUserId));
    }
}
