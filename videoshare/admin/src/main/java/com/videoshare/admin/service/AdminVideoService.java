// =============================================
// AdminVideoService.java
// 路径: admin/src/main/java/com/videoshare/admin/service/AdminVideoService.java
// =============================================
package com.videoshare.admin.service;

import com.videoshare.common.query.UserInfoQuery;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.entity.VideoInfo;
import com.videoshare.common.query.VideoQuery;

import java.util.Map;

public interface AdminVideoService {
    PaginationResultVO<VideoInfo> getVideoList(VideoQuery query);
    void updateVideoStatus(String videoId, Integer status);
    void deleteVideo(String videoId);
    Map<String, Object> getVideoStats();
}