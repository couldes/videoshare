package com.videoshare.web.controller;

import com.videoshare.web.service.UserProfileService;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户个人主页接口
 *   GET  /user/profile/{userId}  → 获取用户公开信息（昵称/简介/统计数）
 *   POST /user/profile/update    → 更新自己的简介（需登录）
 *   POST /user/follow            → 关注 / 取消关注（需登录）
 *   GET  /user/follow/status     → 是否已关注某用户
 *   GET  /user/favorites         → 我的收藏列表（需登录）
 */
@RestController
@RequestMapping("/user")
public class UserProfileController extends ABaseController {

    @Resource
    private UserProfileService userProfileService;

    /** 用户公开主页信息 */
    @GetMapping("/profile/{userId}")
    public ResponseVO getUserProfile(
            @PathVariable String userId,
            HttpServletRequest request) {
        String currentUserId = getUserIdFromToken(request); // 可能未登录
        return getSuccessResponseVO(userProfileService.getProfile(userId, currentUserId));
    }

    /** 更新个人简介（仅限本人）*/
    @PostMapping("/profile/update")
    public ResponseVO updateProfile(
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String avatarUrl,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        userProfileService.updateProfile(userId, bio, avatarUrl);
        return getSuccessResponseVO(null);
    }

    /** 关注 / 取消关注 */
    @PostMapping("/follow")
    public ResponseVO toggleFollow(
            @RequestParam String followUserId,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        boolean active = userProfileService.toggleFollow(userId, followUserId);
        return getSuccessResponseVO(active);
    }

    /** 查询是否已关注 */
    @GetMapping("/follow/status")
    public ResponseVO followStatus(
            @RequestParam String followUserId,
            HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return getSuccessResponseVO(userProfileService.isFollowing(userId, followUserId));
    }

    /** 我的收藏视频列表 */
    @GetMapping("/favorites")
    public ResponseVO getFavorites(
            @RequestParam(defaultValue = "1")  Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(userProfileService.getFavorites(userId, pageNum, pageSize));
    }
}