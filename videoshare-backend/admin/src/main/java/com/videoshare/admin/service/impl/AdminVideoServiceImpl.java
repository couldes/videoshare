// 路径: admin/src/main/java/com/videoshare/admin/service/impl/AdminVideoServiceImpl.java
package com.videoshare.admin.service.impl;

import com.videoshare.admin.mapper.AdminVideoMapper;
import com.videoshare.admin.mapper.AdminUserMapper;
import com.videoshare.admin.mapper.CommentMapper;
import com.videoshare.admin.mapper.NotificationMapper;
import com.videoshare.admin.mapper.PlaylistVideoMapper;
import com.videoshare.admin.mapper.TranscodeJobMapper;
import com.videoshare.admin.mapper.UserActionMapper;
import com.videoshare.admin.mapper.WatchHistoryMapper;
import com.videoshare.admin.service.AdminVideoService;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.enums.VideoStatusEnum;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.query.VideoQuery;
import com.videoshare.common.search.VideoSearchService;
import com.videoshare.common.search.VideoSearchDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdminVideoServiceImpl implements AdminVideoService {

    private static final Logger log = LoggerFactory.getLogger(AdminVideoServiceImpl.class);

    @Resource
    private AdminVideoMapper adminVideoMapper;

    @Resource
    private AdminUserMapper adminUserMapper;

    @Resource
    private VideoSearchService videoSearchService;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserActionMapper userActionMapper;

    @Resource
    private WatchHistoryMapper watchHistoryMapper;

    @Resource
    private PlaylistVideoMapper playlistVideoMapper;

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private TranscodeJobMapper transcodeJobMapper;

    @Override
    public PaginationResultVO<VideoInfo> getVideoList(VideoQuery query) {
        List<VideoInfo> list  = adminVideoMapper.selectVideoList(query);
        Integer         total = adminVideoMapper.countVideos(query);
        return new PaginationResultVO<>(total, query.getPageSize(), query.getPageNum(), list);
    }

    @Override
    public VideoInfo getVideoDetail(String videoId) {
        VideoInfo video = adminVideoMapper.selectByVideoId(videoId);
        if (video == null) throw new BusinessException("视频不存在");
        return video;
    }

    @Override
    public void updateVideoStatus(String videoId, Integer status, String remark) {
        VideoStatusEnum target = VideoStatusEnum.fromValue(status);
        if (target == null || target == VideoStatusEnum.PENDING) {
            throw new BusinessException("非法状态值");
        }

        VideoInfo video = adminVideoMapper.selectByVideoId(videoId);
        if (video == null) throw new BusinessException("视频不存在");

        VideoStatusEnum current = VideoStatusEnum.fromValue(video.getStatus());
        if (current == null || !current.canTransitionTo(target)) {
            throw new BusinessException("当前状态不允许此操作");
        }

        Integer rows = adminVideoMapper.updateStatus(videoId, target.getValue());
        if (rows == 0) throw new BusinessException("视频不存在");
        // 下架/驳回时保存原因
        if (target == VideoStatusEnum.OFFLINE && remark != null && !remark.isEmpty()) {
            adminVideoMapper.updateRemark(videoId, remark);
        }
        // 同步 ES：发布时保存完整文档，下架时更新状态
        if (target == VideoStatusEnum.PUBLISHED) {
            syncToES(videoId);
        } else {
            videoSearchService.updateStatus(videoId, target.getValue());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVideo(String videoId) {
        if (adminVideoMapper.selectByVideoId(videoId) == null) {
            throw new BusinessException("视频不存在或已删除");
        }
        // 评论点赞先于评论删除：deleteCommentLikes 的子查询依赖 comment_info 行存在
        userActionMapper.deleteCommentLikes(videoId);
        userActionMapper.deleteVideoLikes(videoId);
        commentMapper.deleteByVideoId(videoId);
        watchHistoryMapper.deleteByVideoId(videoId);
        playlistVideoMapper.deleteByVideoId(videoId);
        notificationMapper.deleteByVideoId(videoId);
        transcodeJobMapper.deleteByVideoId(videoId);
        adminVideoMapper.deleteByVideoId(videoId);
        videoSearchService.deleteById(videoId);
    }

    @Override
    public Map<String, Object> getVideoStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> statusCounts = adminVideoMapper.countByStatus();
        int total = 0, published = 0, pending = 0, offline = 0;
        for (Map<String, Object> row : statusCounts) {
            if (row.get("status") == null || row.get("count") == null) continue;
            int s = ((Number) row.get("status")).intValue();
            int c = ((Number) row.get("count")).intValue();
            total += c;
            if (s == 0) pending   = c;
            if (s == 1) published = c;
            if (s == 2) offline   = c;
        }
        stats.put("totalVideos",     total);
        stats.put("publishedVideos", published);
        stats.put("pendingVideos",   pending);
        stats.put("offlineVideos",   offline);

        List<Map<String, Object>> daily = adminVideoMapper.countDailyPublish(7);
        for (Map<String, Object> row : daily) {
            if (row.get("count") != null) {
                row.put("count", ((Number) row.get("count")).intValue());
            }
        }
        stats.put("dailyPublish", daily);
        return stats;
    }

    @Override
    public int reindexAll() {
        List<VideoInfo> allVideos = adminVideoMapper.selectAllPublished();
        List<VideoSearchDocument> docs = new ArrayList<>();
        for (VideoInfo video : allVideos) {
            UserInfo user = adminUserMapper.selectByUserId(video.getUserId());
            String nickName = user != null ? user.getNickName() : "";
            docs.add(VideoSearchDocument.fromVideoInfo(video, nickName));
        }
        videoSearchService.saveAll(docs);
        log.info("ES 全量重建索引完成，共 {} 条", docs.size());
        return docs.size();
    }

    /** 同步单个视频到 ES */
    private void syncToES(String videoId) {
        try {
            VideoInfo video = adminVideoMapper.selectByVideoId(videoId);
            if (video == null) return;
            UserInfo user = adminUserMapper.selectByUserId(video.getUserId());
            String nickName = user != null ? user.getNickName() : "";
            videoSearchService.save(VideoSearchDocument.fromVideoInfo(video, nickName));
        } catch (Exception e) {
            log.error("同步视频到 ES 失败，videoId={}", videoId, e);
        }
    }
}