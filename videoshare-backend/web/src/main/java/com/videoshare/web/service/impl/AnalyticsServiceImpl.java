package com.videoshare.web.service.impl;

import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.vo.AnalyticsOverviewVO;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.VideoStatVO;
import com.videoshare.web.mapper.*;
import com.videoshare.web.service.AnalyticsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Resource private VideoInfoMapper     videoInfoMapper;
    @Resource private UserActionMapper    userActionMapper;
    @Resource private WatchHistoryMapper  watchHistoryMapper;
    @Resource private UserFollowMapper    userFollowMapper;

    @Override
    public AnalyticsOverviewVO getOverview(String userId, int days) {
        AnalyticsOverviewVO vo = new AnalyticsOverviewVO();
        vo.setTotalViews(videoInfoMapper.sumViewsByUserId(userId));
        vo.setTotalLikes(videoInfoMapper.sumLikesByUserId(userId));
        vo.setTotalComments(videoInfoMapper.sumCommentsByUserId(userId));
        vo.setFollowerCount(userFollowMapper.countFollowers(userId));
        vo.setVideoCount(videoInfoMapper.countByUserIdAll(userId));
        vo.setRecentViews(watchHistoryMapper.countRecentViewsForCreator(userId, days));
        vo.setRecentLikes(userActionMapper.countRecentLikesForCreator(userId, days));

        // 每日趋势数据
        List<java.util.Map<String, Object>> dailyViews = watchHistoryMapper.selectDailyViewsForCreator(userId, days);
        List<java.util.Map<String, Object>> dailyLikes = userActionMapper.selectDailyLikesForCreator(userId, days);

        vo.setViewsTrend(mapToTrendPoints(dailyViews));
        vo.setLikesTrend(mapToTrendPoints(dailyLikes));
        return vo;
    }

    private List<com.videoshare.common.vo.TrendPoint> mapToTrendPoints(List<java.util.Map<String, Object>> rows) {
        if (rows == null) return java.util.Collections.emptyList();
        return rows.stream().map(row -> {
            String date = row.get("date") != null ? row.get("date").toString() : "";
            long count = row.get("count") != null ? ((Number) row.get("count")).longValue() : 0L;
            return new com.videoshare.common.vo.TrendPoint(date, count);
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public PaginationResultVO<VideoStatVO> getVideoStats(String userId,
                                                         int pageNum,
                                                         int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<VideoInfo> list  = videoInfoMapper.selectVideoStats(userId, offset, pageSize);
        long            total = videoInfoMapper.countByUserIdAll(userId);

        List<VideoStatVO> voList = list.stream().map(v -> {
            VideoStatVO s = new VideoStatVO();
            s.setVideoId(v.getVideoId());
            s.setTitle(v.getTitle());
            s.setCoverUrl(v.getCoverUrl());
            s.setViewCount(v.getViewCount());
            s.setLikeCount(v.getLikeCount());
            s.setCommentCount(v.getCommentCount());
            s.setFavoriteCount(v.getFavoriteCount());
            s.setCreateTime(v.getCreateTime());
            return s;
        }).collect(Collectors.toList());

        return new PaginationResultVO<>((int) total, pageSize, pageNum, voList);
    }

    @Override
    public List<Map<String, Object>> getAudience(String userId) {
        return Collections.emptyList();
    }
}
