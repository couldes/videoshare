// 路径: web/src/main/java/com/videoshare/web/service/impl/UserProfileServiceImpl.java
package com.videoshare.web.service.impl;

import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.query.VideoQuery;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.UserProfileVO;
import com.videoshare.common.vo.UserInfoVO;
import com.videoshare.common.vo.VideoInfoVO;
import com.videoshare.web.mapper.UserActionMapper;
import com.videoshare.web.mapper.UserFollowMapper;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.web.mapper.VideoInfoMapper;
import com.videoshare.web.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Resource private UserInfoMapper   userInfoMapper;
    @Resource private UserFollowMapper userFollowMapper;
    @Resource private UserActionMapper userActionMapper;
    @Resource private VideoInfoMapper  videoInfoMapper;

    // ============================================================
    //  个人主页信息
    // ============================================================
    @Override
    public UserProfileVO getProfile(String userId, String currentUserId) {
        UserInfo user = userInfoMapper.selectByUserId(userId);
        if (user == null) throw new BusinessException("用户不存在");

        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getUserId());
        vo.setNickName(user.getNickName());
        vo.setBio(user.getBio());
        vo.setSex(user.getSex());

        // 统计数据
        vo.setVideoCount(videoInfoMapper.countByUserId(userId));
        vo.setFollowerCount(userFollowMapper.countFollowers(userId));
        vo.setFollowingCount(userFollowMapper.countFollowing(userId));
        vo.setTotalLikeCount(videoInfoMapper.sumLikeCountByUserId(userId));

        // 当前用户是否已关注
        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(userId)) {
            isFollowing = userFollowMapper.checkFollow(currentUserId, userId) > 0;
        }
        vo.setIsFollowing(isFollowing);

        return vo;
    }

    // ============================================================
    //  更新个人资料
    // ============================================================
    @Override
    public void updateProfile(String userId, String bio, String avatarUrl) {
        // UserInfoMapper 需要补充 updateProfile 方法（见下方）
        userInfoMapper.updateProfile(userId, bio, avatarUrl);
    }

    // ============================================================
    //  关注 / 取消关注
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFollow(String userId, String followUserId) {
        if (userId.equals(followUserId)) throw new BusinessException("不能关注自己");

        Integer exists = userFollowMapper.checkFollow(userId, followUserId);
        boolean willFollow = (exists == 0);

        if (willFollow) {
            userFollowMapper.insert(userId, followUserId);
        } else {
            userFollowMapper.delete(userId, followUserId);
        }
        return willFollow;
    }

    // ============================================================
    //  是否已关注
    // ============================================================
    @Override
    public boolean isFollowing(String userId, String followUserId) {
        if (userId == null) return false;
        return userFollowMapper.checkFollow(userId, followUserId) > 0;
    }

    // ============================================================
    //  我的收藏（分页）
    // ============================================================
    @Override
    public PaginationResultVO<VideoInfoVO> getFavorites(String userId,
                                                        Integer pageNum,
                                                        Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;

        // 1. 查收藏的视频ID列表
        List<String> videoIds = userActionMapper.selectFavoriteVideoIds(userId, offset, pageSize);
        Integer total = userActionMapper.countFavorites(userId);

        if (videoIds.isEmpty()) {
            return new PaginationResultVO<>(total, pageSize, pageNum, Collections.emptyList());
        }

        // 2. 按ID逐个查视频详情（数量少，可接受）
        List<VideoInfoVO> voList = videoIds.stream()
                .map(videoId -> {
                    VideoInfo v = videoInfoMapper.selectByVideoId(videoId);
                    if (v == null) return null;
                    UserInfo author = userInfoMapper.selectByUserId(v.getUserId());
                    return convertVideoToVO(v, author);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PaginationResultVO<>(total, pageSize, pageNum, voList);
    }

    // ============================================================
    //  私有工具方法
    // ============================================================

    private VideoInfoVO convertVideoToVO(VideoInfo v, UserInfo author) {
        VideoInfoVO vo = new VideoInfoVO();
        vo.setVideoId(v.getVideoId());
        vo.setTitle(v.getTitle());
        vo.setCoverUrl(v.getCoverUrl());
        vo.setVideoUrl(v.getVideoUrl());
        vo.setDurationSeconds(v.getDuration());
        vo.setDuration(formatDuration(v.getDuration()));
        vo.setViewCount(v.getViewCount());
        vo.setLikeCount(v.getLikeCount());
        vo.setCommentCount(v.getCommentCount());
        vo.setFavoriteCount(v.getFavoriteCount());
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