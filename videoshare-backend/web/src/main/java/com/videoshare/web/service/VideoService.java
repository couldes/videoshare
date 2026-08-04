// 路径: web/src/main/java/com/videoshare/web/service/VideoService.java
package com.videoshare.web.service;

import com.videoshare.common.query.VideoQuery;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.VideoInfoVO;

import java.util.Map;

public interface VideoService {
    PaginationResultVO<VideoInfoVO> getVideoList(VideoQuery query);
    VideoInfoVO getVideoDetail(String videoId, String currentUserId);
    void publishVideo(String userId, String videoId, String title, String description,
                      String coverUrl, String videoUrl, String category, String tags);
    void updateVideo(String userId, String videoId, String title, String description,
                     String category, String tags);
    PaginationResultVO<VideoInfoVO> getUserVideos(String userId, Integer pageNum, Integer pageSize);
    boolean toggleAction(String userId, String videoId, Integer actionType);
    Map<String, Boolean> checkUserAction(String userId, String videoId);

    PaginationResultVO<VideoInfoVO> getTrendingList(Integer pageNum, Integer pageSize, String category);

    PaginationResultVO<VideoInfoVO> getSubscriptionVideos(String userId, Integer pageNum, Integer pageSize);

    PaginationResultVO<VideoInfoVO> searchVideos(String keyword, String orderBy, Integer pageNum, Integer pageSize);

    PaginationResultVO<VideoInfoVO> getRecommendList(String userId, Integer pageNum, Integer pageSize);

    void republishVideo(String userId, String videoId);

    void unpublishVideo(String userId, String videoId);
}