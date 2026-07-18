/**
 * 通知接口工厂
 * 对应后端 NotificationController
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createNotificationApi(request) {
  return {
    /**
     * 通知列表（分页）
     * GET /notification/list
     * @param {{ pageNum, pageSize }} params
     */
    getNotificationList: (params) =>
      request({ method: 'GET', url: '/notification/list', params }),

    /**
     * 标记单条通知已读
     * POST /notification/read/{id}
     */
    markRead: (notificationId) =>
      request({ method: 'POST', url: `/notification/read/${notificationId}` }),

    /**
     * 全部标记已读
     * POST /notification/read-all
     */
    markAllRead: () =>
      request({ method: 'POST', url: '/notification/read-all' }),

    /**
     * 未读通知数量
     * GET /notification/unread-count
     * @returns {number}
     */
    getUnreadCount: () =>
      request({ method: 'GET', url: '/notification/unread-count' })
  }
}
