// 路径: web/src/main/java/com/videoshare/web/service/VideoService.java
package com.videoshare.web.service;

import com.videoshare.common.query.VideoQuery;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.VideoInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VideoService {
    PaginationResultVO<VideoInfoVO> getVideoList(VideoQuery query);
    VideoInfoVO getVideoDetail(String videoId, String currentUserId);
    Map<String, Object> uploadVideoFile(MultipartFile file, String userId);
    void publishVideo(String userId, String title, String description,
                      String coverUrl, String videoUrl, String category, String tags);
    PaginationResultVO<VideoInfoVO> getUserVideos(String userId, Integer pageNum, Integer pageSize);
    boolean toggleAction(String userId, String videoId, Integer actionType);
    Map<String, Boolean> checkUserAction(String userId, String videoId);
}