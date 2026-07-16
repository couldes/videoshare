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
    public AnalyticsOverviewVO getOverview(String userId) {
        AnalyticsOverviewVO vo = new AnalyticsOverviewVO();
        vo.setTotalViews(videoInfoMapper.sumViewsByUserId(userId));
        vo.setTotalLikes(videoInfoMapper.sumLikesByUserId(userId));
        vo.setTotalComments(videoInfoMapper.sumCommentsByUserId(userId));
        vo.setFollowerCount(userFollowMapper.countFollowers(userId));
        vo.setVideoCount(videoInfoMapper.countByUserIdAll(userId));
        vo.setRecentViews(watchHistoryMapper.countRecentViewsForCreator(userId, 30));
        vo.setRecentLikes(userActionMapper.countRecentLikesForCreator(userId, 30));
        return vo;
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
