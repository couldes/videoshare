// 路径: web/src/main/java/com/videoshare/web/service/impl/VideoServiceImpl.java
package com.videoshare.web.service.impl;

import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.query.VideoQuery;
import com.videoshare.common.utils.StringTools;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.UserInfoVO;
import com.videoshare.common.vo.VideoInfoVO;
import com.videoshare.web.mapper.UserActionMapper;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import com.videoshare.web.service.VideoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {

    /** 视频文件存储根路径（从 application.yml project.folder 读取） */
    @Value("${project.folder:d:/webser/videoshare/}")
    private String projectFolder;

    private String getUploadDir() {
        return projectFolder.endsWith("/") || projectFolder.endsWith("\\")
                ? projectFolder + "videos/"
                : projectFolder + "/videos/";
    }

    @Resource private VideoInfoMapper  videoInfoMapper;
    @Resource private UserInfoMapper   userInfoMapper;
    @Resource private UserActionMapper userActionMapper;

    // ============================================================
    //  视频列表（首页 + 个人主页通用）
    // ============================================================
    @Override
    public PaginationResultVO<VideoInfoVO> getVideoList(VideoQuery query) {
        // 默认只查已发布
        if (query.getStatus() == null) query.setStatus(1);

        List<VideoInfo> list  = videoInfoMapper.selectVideoList(query);
        Integer         total = videoInfoMapper.countVideos(query);

        // 批量获取发布者信息（避免 N+1 查询）
        Set<String> userIds = list.stream().map(VideoInfo::getUserId).collect(Collectors.toSet());
        Map<String, UserInfo> userMap = batchGetUsers(userIds);

        List<VideoInfoVO> voList = list.stream()
                .map(v -> convertToVO(v, userMap.get(v.getUserId())))
                .collect(Collectors.toList());

        return new PaginationResultVO<>(total, query.getPageSize(), query.getPageNum(), voList);
    }

    // ============================================================
    //  视频详情（播放时调用，自动 +1 播放量）
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoInfoVO getVideoDetail(String videoId, String currentUserId) {
        VideoInfo video = videoInfoMapper.selectByVideoId(videoId);
        if (video == null || video.getStatus() != 1) {
            throw new BusinessException("视频不存在或已下架");
        }
        // 播放量 +1（简化版，高并发场景应改为 Redis 计数 + 定时落库）
        videoInfoMapper.increaseViewCount(videoId);
        video.setViewCount(video.getViewCount() + 1);

        UserInfo author = userInfoMapper.selectByUserId(video.getUserId());
        return convertToVO(video, author);
    }

    // ============================================================
    //  上传视频文件（简化版，生产环境替换为 OSS）
    // ============================================================
    @Override
    public Map<String, Object> uploadVideoFile(MultipartFile file, String userId) {
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".mp4";

        // 生成唯一文件名，防止覆盖
        String fileName = userId + "_" + System.currentTimeMillis() + ext;
        File dest = new File(getUploadDir() + fileName);
        dest.getParentFile().mkdirs();

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("视频上传失败，请重试");
        }

        // 返回前端期望的 { videoUrl, duration } 格式
        Map<String, Object> result = new HashMap<>();
        result.put("videoUrl", "http://localhost:8080/videos/" + fileName);
        result.put("duration", 0);
        return result;
    }

    // ============================================================
    //  发布视频
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishVideo(String userId, String title, String description,
                             String coverUrl, String videoUrl, String category, String tags) {
        VideoInfo video = new VideoInfo();
        video.setVideoId(StringTools.getRandomNumber(10));
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description);
        video.setCoverUrl(coverUrl);
        video.setVideoUrl(videoUrl);
        video.setCategory(category);
        video.setTags(tags);
        video.setStatus(1); // 直接发布（实际项目可加审核流程 status=0）
        video.setViewCount(0L);
        video.setLikeCount(0);
        video.setCommentCount(0);
        video.setFavoriteCount(0);
        videoInfoMapper.insert(video);
    }

    // ============================================================
    //  用户视频列表（个人主页）
    // ============================================================
    @Override
    public PaginationResultVO<VideoInfoVO> getUserVideos(String userId,
                                                         Integer pageNum,
                                                         Integer pageSize) {
        VideoQuery query = new VideoQuery();
        query.setUserId(userId);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setStatus(1);
        return getVideoList(query);
    }

    // ============================================================
    //  点赞 / 收藏 Toggle
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleAction(String userId, String videoId, Integer actionType) {
        if (userId == null) throw new BusinessException("请先登录");

        Integer exists = userActionMapper.checkAction(userId, videoId, actionType);
        boolean willAdd = (exists == 0); // 当前没有 → 操作后变"有"

        if (willAdd) {
            userActionMapper.insert(userId, videoId, actionType);
            // 同步更新视频计数
            if (actionType == 1) videoInfoMapper.updateLikeCount(videoId, 1);
            if (actionType == 2) videoInfoMapper.updateFavoriteCount(videoId, 1);
        } else {
            userActionMapper.delete(userId, videoId, actionType);
            if (actionType == 1) videoInfoMapper.updateLikeCount(videoId, -1);
            if (actionType == 2) videoInfoMapper.updateFavoriteCount(videoId, -1);
        }
        return willAdd;
    }

    // ============================================================
    //  查询当前用户对视频的点赞/收藏状态
    // ============================================================
    @Override
    public Map<String, Boolean> checkUserAction(String userId, String videoId) {
        Map<String, Boolean> result = new HashMap<>();
        if (userId == null) {
            result.put("liked",     false);
            result.put("favorited", false);
            return result;
        }
        result.put("liked",     userActionMapper.checkAction(userId, videoId, 1) > 0);
        result.put("favorited", userActionMapper.checkAction(userId, videoId, 2) > 0);
        return result;
    }

    // ============================================================
    //  私有辅助方法
    // ============================================================

    /** 批量查用户（避免在循环里查库） */
    private Map<String, UserInfo> batchGetUsers(Set<String> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        return userIds.stream()
                .map(userInfoMapper::selectByUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserInfo::getUserId, u -> u, (a, b) -> a));
    }

    /** 格式化秒数为 MM:SS */
    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds <= 0) return "0:00";
        int m = seconds / 60, s = seconds % 60;
        return m + ":" + String.format("%02d", s);
    }

    /** VideoInfo → VideoInfoVO */
    private VideoInfoVO convertToVO(VideoInfo v, UserInfo author) {
        VideoInfoVO vo = new VideoInfoVO();
        vo.setVideoId(v.getVideoId());
        vo.setTitle(v.getTitle());
        vo.setDescription(v.getDescription());
        vo.setCoverUrl(v.getCoverUrl());
        vo.setVideoUrl(v.getVideoUrl());
        vo.setDurationSeconds(v.getDuration());
        vo.setDuration(formatDuration(v.getDuration()));
        vo.setCategory(v.getCategory());
        vo.setTags(v.getTags());
        vo.setViewCount(v.getViewCount());
        vo.setLikeCount(v.getLikeCount());
        vo.setCommentCount(v.getCommentCount());
        vo.setFavoriteCount(v.getFavoriteCount());
        vo.setStatus(v.getStatus());
        vo.setCreateTime(v.getCreateTime());

        if (author != null) {
            UserInfoVO userVO = new UserInfoVO();
            userVO.setUserId(author.getUserId());
            userVO.setNickName(author.getNickName());
            vo.setUserInfo(userVO);
        }
        return vo;
    }
}