package com.videoshare.web.service.impl;

import com.videoshare.common.entity.Notification;
import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.vo.NotificationVO;
import com.videoshare.common.vo.PaginationResultVO;
import com.videoshare.web.mapper.NotificationMapper;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.web.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private UserInfoMapper     userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(String userId, String fromUserId, String type,
                                  String videoId, String content) {
        if (userId == null || fromUserId == null) return;
        if (userId.equals(fromUserId)) return; // 自通知过滤

        Notification n = new Notification();
        n.setUserId(userId);
        n.setFromUserId(fromUserId);
        n.setType(type);
        n.setVideoId(videoId);
        n.setContent(content);
        notificationMapper.insert(n);
    }

    @Override
    public PaginationResultVO<NotificationVO> getNotificationList(String userId,
                                                                   Integer pageNum,
                                                                   Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;

        List<Notification> list = notificationMapper.selectByUserId(userId, offset, pageSize);
        Integer total = notificationMapper.countByUserId(userId);

        if (list.isEmpty()) {
            return new PaginationResultVO<>(total, pageSize, pageNum, Collections.emptyList());
        }

        // 批量查触发者信息
        Set<String> fromUserIds = list.stream()
                .map(Notification::getFromUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, UserInfo> userMap = fromUserIds.stream()
                .map(userInfoMapper::selectByUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserInfo::getUserId, u -> u, (a, b) -> a));

        List<NotificationVO> voList = list.stream()
                .map(n -> toVO(n, userMap.get(n.getFromUserId())))
                .collect(Collectors.toList());

        return new PaginationResultVO<>(total, pageSize, pageNum, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, String userId) {
        notificationMapper.markAsRead(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(String userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public Integer getUnreadCount(String userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    private NotificationVO toVO(Notification n, UserInfo fromUser) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setFromUserId(n.getFromUserId());
        if (fromUser != null) {
            vo.setFromNickName(fromUser.getNickName());
        }
        vo.setType(n.getType());
        vo.setVideoId(n.getVideoId());
        vo.setContent(n.getContent());
        vo.setIsRead(n.getIsRead());
        vo.setCreateTime(n.getCreateTime());
        return vo;
    }
}
