// 路径: web/src/main/java/com/videoshare/web/service/UserProfileService.java
package com.videoshare.web.service;

import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.UserProfileVO;
import com.videoshare.common.vo.VideoInfoVO;

import java.util.List;

public interface UserProfileService {
    UserProfileVO getProfile(String userId, String currentUserId);
    void updateProfile(String userId, String bio, String avatarUrl, String backgroundUrl);
    boolean toggleFollow(String userId, String followUserId);
    boolean isFollowing(String userId, String followUserId);
    PaginationResultVO<VideoInfoVO> getFavorites(String userId, Integer pageNum, Integer pageSize);

    List<UserInfo> getFollowingList(String userId);

    List<UserInfo> getFollowerList(String userId);
}