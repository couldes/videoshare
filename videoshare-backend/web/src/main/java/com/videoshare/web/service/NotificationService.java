package com.videoshare.web.service;

import com.videoshare.common.vo.NotificationVO;
import com.videoshare.common.vo.PaginationResultVO;

public interface NotificationService {
    void sendNotification(String userId, String fromUserId, String type,
                          String videoId, String content);

    PaginationResultVO<NotificationVO> getNotificationList(String userId,
                                                            Integer pageNum,
                                                            Integer pageSize);

    void markAsRead(Long id, String userId);

    void markAllAsRead(String userId);

    Integer getUnreadCount(String userId);
}
