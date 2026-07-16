package com.videoshare.web.service;

import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.common.vo.VideoInfoVO;

public interface WatchHistoryService {

    void recordWatch(String userId, String videoId);

    PaginationResultVO<VideoInfoVO> getHistory(String userId, Integer pageNum, Integer pageSize);

    PaginationResultVO<VideoInfoVO> searchHistory(String userId, String keyword,
                                                   Integer pageNum, Integer pageSize);

    void deleteRecord(String userId, String videoId);

    void clearAll(String userId);
}
