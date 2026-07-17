// 路径: admin/src/main/java/com/videoshare/admin/service/impl/AdminVideoServiceImpl.java
package com.videoshare.admin.service.impl;

import com.videoshare.admin.mapper.AdminVideoMapper;
import com.videoshare.admin.service.AdminVideoService;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.enums.VideoStatusEnum;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.query.VideoQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class AdminVideoServiceImpl implements AdminVideoService {

    @Resource
    private AdminVideoMapper adminVideoMapper;

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
    }

    @Override
    public void deleteVideo(String videoId) {
        Integer rows = adminVideoMapper.deleteByVideoId(videoId);
        if (rows == 0) throw new BusinessException("视频不存在或已删除");
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
}