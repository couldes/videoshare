package com.videoshare.web.service.impl;

import com.videoshare.common.entity.Playlist;
import com.videoshare.common.entity.PlaylistVideo;
import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.vo.PlaylistVO;
import com.videoshare.common.vo.UserInfoVO;
import com.videoshare.common.vo.VideoInfoVO;
import com.videoshare.web.mapper.PlaylistMapper;
import com.videoshare.web.mapper.PlaylistVideoMapper;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import com.videoshare.web.service.PlaylistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private static final int MAX_VIDEOS_PER_PLAYLIST = 200;

    @Resource private PlaylistMapper      playlistMapper;
    @Resource private PlaylistVideoMapper playlistVideoMapper;
    @Resource private VideoInfoMapper     videoInfoMapper;
    @Resource private UserInfoMapper      userInfoMapper;

    // ============================================================
    //  创建播放列表
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlaylistVO createPlaylist(String userId, String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("播放列表标题不能为空");
        }

        Playlist p = new Playlist();
        p.setUserId(userId);
        p.setTitle(title.trim());
        p.setDescription(description != null ? description.trim() : "");
        p.setIsPrivate(0);
        playlistMapper.insert(p);

        return toPlaylistVO(p, 0, null);
    }

    // ============================================================
    //  更新播放列表
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlaylistVO updatePlaylist(String userId, Long playlistId, String title,
                                      String description, Integer isPrivate) {
        Playlist p = playlistMapper.selectByPlaylistId(playlistId);
        if (p == null) throw new BusinessException("播放列表不存在");
        if (!p.getUserId().equals(userId)) throw new BusinessException("无权修改此播放列表");

        if (title != null) p.setTitle(title.trim());
        if (description != null) p.setDescription(description.trim());
        if (isPrivate != null) p.setIsPrivate(isPrivate);

        playlistMapper.update(p);

        int videoCount = playlistMapper.countVideoById(playlistId);
        return toPlaylistVO(p, videoCount, null);
    }

    // ============================================================
    //  删除播放列表
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlaylist(String userId, Long playlistId) {
        Playlist p = playlistMapper.selectByPlaylistId(playlistId);
        if (p == null) throw new BusinessException("播放列表不存在");
        if (!p.getUserId().equals(userId)) throw new BusinessException("无权删除此播放列表");

        playlistVideoMapper.deleteByPlaylistId(playlistId);
        playlistMapper.deleteByPlaylistId(playlistId);
    }

    // ============================================================
    //  添加视频到播放列表
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addVideo(String userId, Long playlistId, String videoId) {
        Playlist p = playlistMapper.selectByPlaylistId(playlistId);
        if (p == null) throw new BusinessException("播放列表不存在");
        if (!p.getUserId().equals(userId)) throw new BusinessException("无权操作此播放列表");

        // 校验视频存在
        VideoInfo video = videoInfoMapper.selectByVideoId(videoId);
        if (video == null) throw new BusinessException("视频不存在");

        // 去重
        Integer exists = playlistVideoMapper.countByPlaylistIdAndVideoId(playlistId, videoId);
        if (exists > 0) throw new BusinessException("该视频已在播放列表中");

        // 上限校验
        Integer currentCount = playlistMapper.countVideoById(playlistId);
        if (currentCount >= MAX_VIDEOS_PER_PLAYLIST) {
            throw new BusinessException("播放列表已达上限（" + MAX_VIDEOS_PER_PLAYLIST + "个视频）");
        }

        // 计算排序号
        Integer maxSort = playlistVideoMapper.selectMaxSortOrder(playlistId);
        int sortOrder = (maxSort != null) ? maxSort + 1 : 0;

        PlaylistVideo pv = new PlaylistVideo();
        pv.setPlaylistId(playlistId);
        pv.setVideoId(videoId);
        pv.setSortOrder(sortOrder);
        playlistVideoMapper.insert(pv);

        // 如果是第一个视频，自动设置封面
        if (currentCount == 0 && video.getCoverUrl() != null) {
            playlistMapper.updateCoverUrl(playlistId, video.getCoverUrl());
        }
    }

    // ============================================================
    //  从播放列表移除视频
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeVideo(String userId, Long playlistId, String videoId) {
        Playlist p = playlistMapper.selectByPlaylistId(playlistId);
        if (p == null) throw new BusinessException("播放列表不存在");
        if (!p.getUserId().equals(userId)) throw new BusinessException("无权操作此播放列表");

        playlistVideoMapper.deleteByPlaylistIdAndVideoId(playlistId, videoId);
    }

    // ============================================================
    //  重新排序
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderVideos(String userId, Long playlistId, List<String> videoIds) {
        Playlist p = playlistMapper.selectByPlaylistId(playlistId);
        if (p == null) throw new BusinessException("播放列表不存在");
        if (!p.getUserId().equals(userId)) throw new BusinessException("无权操作此播放列表");

        // 查当前所有关联记录（建立 videoId → id 映射）
        List<PlaylistVideo> existing = playlistVideoMapper.selectByPlaylistId(
                playlistId, 0, Integer.MAX_VALUE);
        Map<String, Long> videoIdToRowId = existing.stream()
                .collect(Collectors.toMap(PlaylistVideo::getVideoId, PlaylistVideo::getId));

        for (int i = 0; i < videoIds.size(); i++) {
            Long rowId = videoIdToRowId.get(videoIds.get(i));
            if (rowId != null) {
                playlistVideoMapper.updateSortOrder(rowId, i);
            }
        }
    }

    // ============================================================
    //  获取播放列表详情
    // ============================================================
    @Override
    public PlaylistVO getPlaylistDetail(Long playlistId, String currentUserId,
                                        Integer pageNum, Integer pageSize) {
        Playlist p = playlistMapper.selectByPlaylistId(playlistId);
        if (p == null) throw new BusinessException("播放列表不存在");

        // 权限校验：私密列表仅创建者可见
        if (p.getIsPrivate() == 1 && !p.getUserId().equals(currentUserId)) {
            throw new BusinessException("播放列表不存在");
        }

        int offset = (pageNum - 1) * pageSize;
        List<PlaylistVideo> pvList = playlistVideoMapper.selectByPlaylistId(
                playlistId, offset, pageSize);
        Integer videoCount = playlistMapper.countVideoById(playlistId);

        // 批量查视频信息
        List<String> videoIds = pvList.stream()
                .map(PlaylistVideo::getVideoId)
                .collect(Collectors.toList());
        List<VideoInfoVO> videoVOList = batchGetVideoVOList(videoIds);

        PlaylistVO vo = toPlaylistVO(p, videoCount, videoVOList);
        return vo;
    }

    // ============================================================
    //  获取用户播放列表列表
    // ============================================================
    @Override
    public List<PlaylistVO> getUserPlaylists(String targetUserId, String currentUserId) {
        Integer isPrivate = null;
        // 只有查看自己的列表时包含私密
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            isPrivate = null; // 查全部
        } else {
            isPrivate = 0; // 只看公开
        }

        List<Playlist> list = playlistMapper.selectByUserId(targetUserId, isPrivate);
        return list.stream()
                .map(p -> {
                    int cnt = playlistMapper.countVideoById(p.getPlaylistId());
                    return toPlaylistVO(p, cnt, null);
                })
                .collect(Collectors.toList());
    }

    // ============================================================
    //  私有工具方法
    // ============================================================

    private PlaylistVO toPlaylistVO(Playlist p, Integer videoCount,
                                     List<VideoInfoVO> videoList) {
        PlaylistVO vo = new PlaylistVO();
        vo.setPlaylistId(p.getPlaylistId());
        vo.setUserId(p.getUserId());
        vo.setTitle(p.getTitle());
        vo.setDescription(p.getDescription());
        vo.setCoverUrl(p.getCoverUrl());
        vo.setIsPrivate(p.getIsPrivate());
        vo.setVideoCount(videoCount);
        vo.setVideoList(videoList);
        vo.setCreateTime(p.getCreateTime());
        vo.setUpdateTime(p.getUpdateTime());
        return vo;
    }

    private List<VideoInfoVO> batchGetVideoVOList(List<String> videoIds) {
        if (videoIds.isEmpty()) return Collections.emptyList();

        List<VideoInfo> videos = videoInfoMapper.selectByVideoIds(videoIds);
        Map<String, VideoInfo> videoMap = videos.stream()
                .collect(Collectors.toMap(VideoInfo::getVideoId, v -> v, (a, b) -> a));

        Set<String> userIds = videos.stream()
                .map(VideoInfo::getUserId)
                .collect(Collectors.toSet());
        Map<String, UserInfo> userMap = userIds.stream()
                .map(userInfoMapper::selectByUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserInfo::getUserId, u -> u, (a, b) -> a));

        return videoIds.stream()
                .map(id -> {
                    VideoInfo v = videoMap.get(id);
                    if (v == null) return null;
                    return convertToVO(v, userMap.get(v.getUserId()));
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
        vo.setDuration(formatDuration(v.getDuration()));
        vo.setDurationSeconds(v.getDuration());
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
