/**
 * 管理端视频管理接口
 *
 * 对应后端 VideoManageController：
 *   getVideoList()   → GET    /admin/video/list
 *   getVideoDetail() → GET    /admin/video/{videoId}
 *   updateStatus()   → POST   /admin/video/updateStatus
 *   deleteVideo()    → DELETE /admin/video/delete/{videoId}
 *   getStats()       → GET    /admin/video/stats
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createAdminVideoApi(request) {
  return {
    /**
     * 分页查询视频列表（含多条件筛选）
     * @param {{ pageNum, pageSize, keyword?, status?, category?, userId? }} params
     */
    getVideoList: (params) =>
      request({ method: 'GET', url: '/admin/video/list', params }),

    /**
     * 视频详情
     * @param {string} videoId
     */
    getVideoDetail: (videoId) =>
      request({ method: 'GET', url: `/admin/video/${videoId}` }),

    /**
     * 修改视频状态（上架/下架）
     * @param {{ videoId: string, status: 1|2 }} data
     */
    updateStatus: (data) =>
      request({ method: 'POST', url: '/admin/video/updateStatus', data: new URLSearchParams(data) }),

    /**
     * 删除视频
     * @param {string} videoId
     */
    deleteVideo: (videoId) =>
      request({ method: 'DELETE', url: `/admin/video/delete/${videoId}` }),

    /**
     * 视频统计数据
     * @returns {{ totalVideos, publishedVideos, pendingVideos, offlineVideos, dailyPublish }}
     */
    getStats: () =>
      request({ method: 'GET', url: '/admin/video/stats' })
  }
}
