package com.videoshare.web.controller;

import com.videoshare.web.service.UserProfileService;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 用户个人主页接口
 *   GET  /user/profile/{userId}  → 获取用户公开信息（昵称/简介/统计数）
 *   POST /user/profile/update    → 更新自己的简介（需登录）
 *   POST /user/follow            → 关注 / 取消关注（需登录）
 *   GET  /user/follow/status     → 是否已关注某用户
 *   GET  /user/search            → 搜索用户（按昵称/邮箱）
 *   GET  /user/favorites         → 我的收藏列表（需登录）
 */
@RestController
@RequestMapping("/user")
public class UserProfileController extends ABaseController {

    @Resource
    private UserProfileService userProfileService;
    @Resource
    private UserInfoMapper     userInfoMapper;

    @Value("${project.folder:d:/webser/videoshare/}")
    private String projectFolder;

    /** 用户公开主页信息 */
    @GetMapping("/profile/{userId}")
    public ResponseVO getUserProfile(
            @PathVariable String userId,
            HttpServletRequest request) {
        String currentUserId = getUserIdFromToken(request); // 可能未登录
        return getSuccessResponseVO(userProfileService.getProfile(userId, currentUserId));
    }

    /** 上传头像或背景图 */
    @PostMapping("/uploadImage")
    public ResponseVO uploadImage(
            @RequestParam MultipartFile file,
            @RequestParam String type, // "avatar" | "background"
            HttpServletRequest request) {
        String userId = requireLogin(request);
        if (!"avatar".equals(type) && !"background".equals(type)) {
            return getFailureResponseVO("type 必须是 avatar 或 background");
        }
        long maxSize = "avatar".equals(type) ? 2 * 1024 * 1024 : 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return getFailureResponseVO("文件大小超过限制");
        }

        String ext = "jpg";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".") + 1);
        }

        String dir = projectFolder.endsWith("/") || projectFolder.endsWith("\\")
                ? projectFolder + "images/" + userId + "/"
                : projectFolder + "/images/" + userId + "/";
        String fileName = type + "." + ext;
        File dest = new File(dir + fileName);
        dest.getParentFile().mkdirs();

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            return getFailureResponseVO("上传失败");
        }

        String url = "/images/" + userId + "/" + fileName + "?t=" + System.currentTimeMillis();
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return getSuccessResponseVO(result);
    }

    /** 更新个人简介（仅限本人）*/
    @PostMapping("/profile/update")
    public ResponseVO updateProfile(
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) String backgroundUrl,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        userProfileService.updateProfile(userId, bio, avatarUrl, backgroundUrl);
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

    /** 搜索用户（按昵称/邮箱模糊匹配）*/
    @GetMapping("/search")
    public ResponseVO searchUsers(@RequestParam String keyword) {
        List<UserInfo> users = userInfoMapper.searchByKeyword(keyword);
        // 组装简化结果，只返回前端需要的字段
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("userId",   u.getUserId());
            m.put("nickName", u.getNickName());
            m.put("email",    u.getEmail());
            m.put("joinTime", u.getJoinTime());
            m.put("avatarUrl", u.getAvatarUrl());
            return m;
        }).collect(Collectors.toList());
        return getSuccessResponseVO(result);
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

    /** 关注列表 */
    @GetMapping("/{userId}/following")
    public ResponseVO getFollowingList(@PathVariable String userId) {
        return getSuccessResponseVO(userProfileService.getFollowingList(userId));
    }

    /** 粉丝列表 */
    @GetMapping("/{userId}/followers")
    public ResponseVO getFollowerList(@PathVariable String userId) {
        return getSuccessResponseVO(userProfileService.getFollowerList(userId));
    }
}