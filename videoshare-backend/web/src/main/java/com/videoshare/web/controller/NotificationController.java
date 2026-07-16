package com.videoshare.web.controller;

import com.videoshare.common.vo.ResponseVO;
import com.videoshare.web.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 消息通知接口
 *   GET  /notification/list           → 通知列表（分页）
 *   POST /notification/read/{id}      → 标记已读
 *   POST /notification/read-all       → 全部已读
 *   GET  /notification/unread-count   → 未读通知数
 */
@RestController
@RequestMapping("/notification")
public class NotificationController extends ABaseController {

    @Resource
    private NotificationService notificationService;

    /** 通知列表（分页，按时间倒序） */
    @GetMapping("/list")
    public ResponseVO getNotificationList(
            @RequestParam(defaultValue = "1")  Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(
                notificationService.getNotificationList(userId, pageNum, pageSize));
    }

    /** 标记单条通知已读 */
    @PostMapping("/read/{id}")
    public ResponseVO markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        String userId = requireLogin(request);
        notificationService.markAsRead(id, userId);
        return getSuccessResponseVO(null);
    }

    /** 一键全部已读 */
    @PostMapping("/read-all")
    public ResponseVO markAllAsRead(HttpServletRequest request) {
        String userId = requireLogin(request);
        notificationService.markAllAsRead(userId);
        return getSuccessResponseVO(null);
    }

    /** 未读通知数（Badge 用） */
    @GetMapping("/unread-count")
    public ResponseVO getUnreadCount(HttpServletRequest request) {
        String userId = requireLogin(request);
        return getSuccessResponseVO(notificationService.getUnreadCount(userId));
    }
}
