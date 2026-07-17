package com.videoshare.web.service;

import com.videoshare.common.vo.AnalyticsOverviewVO;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.VideoStatVO;

import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    AnalyticsOverviewVO getOverview(String userId, int days);
    PaginationResultVO<VideoStatVO> getVideoStats(String userId, int pageNum, int pageSize);
    List<Map<String, Object>> getAudience(String userId);
}
