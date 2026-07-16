package com.videoshare.web.service.impl;

import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.entity.WatchHistory;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.UserInfoVO;
import com.videoshare.common.vo.VideoInfoVO;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import com.videoshare.web.mapper.WatchHistoryMapper;
import com.videoshare.web.service.WatchHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WatchHistoryServiceImpl implements WatchHistoryService {

    @Resource private WatchHistoryMapper watchHistoryMapper;
    @Resource private VideoInfoMapper    videoInfoMapper;
    @Resource private UserInfoMapper     userInfoMapper;

    @Override
    public void recordWatch(String userId, String videoId) {
        watchHistoryMapper.upsert(userId, videoId);
        watchHistoryMapper.cleanupExcess(userId, 200);
    }

    @Override
    public PaginationResultVO<VideoInfoVO> getHistory(String userId,
                                                       Integer pageNum,
                                                       Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<WatchHistory> records = watchHistoryMapper.selectPage(userId, offset, pageSize);
        Integer total = watchHistoryMapper.countByUser(userId);

        List<VideoInfoVO> voList = toVideoVOList(records);
        return new PaginationResultVO<>(total, pageSize, pageNum, voList);
    }

    @Override
    public PaginationResultVO<VideoInfoVO> searchHistory(String userId, String keyword,
                                                          Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<WatchHistory> records = watchHistoryMapper.searchByKeyword(userId, keyword, offset, pageSize);
        Integer total = watchHistoryMapper.countSearch(userId, keyword);

        List<VideoInfoVO> voList = toVideoVOList(records);
        return new PaginationResultVO<>(total, pageSize, pageNum, voList);
    }

    @Override
    public void deleteRecord(String userId, String videoId) {
        watchHistoryMapper.deleteByUserAndVideo(userId, videoId);
    }

    @Override
    public void clearAll(String userId) {
        watchHistoryMapper.deleteByUser(userId);
    }

    // ============================================================
    //  私有辅助方法
    // ============================================================

    /** 将 WatchHistory 记录列表转为 VideoInfoVO 列表 */
    private List<VideoInfoVO> toVideoVOList(List<WatchHistory> records) {
        if (records.isEmpty()) return Collections.emptyList();

        // 收集视频ID并批量查视频信息
        List<String> videoIds = records.stream()
                .map(WatchHistory::getVideoId)
                .collect(Collectors.toList());
        List<VideoInfo> videoList = videoInfoMapper.selectByVideoIds(videoIds);
        Map<String, VideoInfo> videoMap = videoList.stream()
                .collect(Collectors.toMap(VideoInfo::getVideoId, v -> v, (a, b) -> a));

        // 收集发布者ID并批量查用户信息
        Set<String> userIds = videoList.stream()
                .map(VideoInfo::getUserId)
                .collect(Collectors.toSet());
        Map<String, UserInfo> userMap = userIds.stream()
                .map(userInfoMapper::selectByUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserInfo::getUserId, u -> u, (a, b) -> a));

        // 按 records 顺序组装 VO
        return records.stream()
                .map(r -> {
                    VideoInfo video = videoMap.get(r.getVideoId());
                    if (video == null) return null;
                    return convertToVO(video, userMap.get(video.getUserId()));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

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
        vo.setHeat(v.getHeat());
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

    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds <= 0) return "0:00";
        int m = seconds / 60, s = seconds % 60;
        return m + ":" + String.format("%02d", s);
    }
}
