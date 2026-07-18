/**
 * 创作者数据分析接口工厂
 * 对应后端 AnalyticsController
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createAnalyticsApi(request) {
  return {
    /**
     * 数据概览（总观看/点赞/评论/订阅者 + 30天趋势）
     * GET /analytics/overview?userId=X
     * @returns {{ totalViews, totalLikes, totalComments, subscriberCount,
     *            viewsTrend: Array<{ date, count }>,
     *            likesTrend: Array<{ date, count }> }}
     */
    getOverview: (userId, days = 30) =>
      request({ method: 'GET', url: '/analytics/overview', params: { userId, days } }),

    /**
     * 单视频数据列表（分页）
     * GET /analytics/videos?userId=X&pageNum=1&pageSize=20
     * @returns {{ list: Array<{ videoId, title, coverUrl, viewCount,
     *            likeCount, commentCount, engagementRate }>, total, pages }}
     */
    getVideos: (userId, pageNum = 1, pageSize = 20) =>
      request({ method: 'GET', url: '/analytics/videos', params: { userId, pageNum, pageSize } }),

    /**
     * 受众地域分布
     * GET /analytics/audience?userId=X
     * @returns {{ topRegions: Array<{ region, percentage }> }}
     */
    getAudience: (userId) =>
      request({ method: 'GET', url: '/analytics/audience', params: { userId } })
  }
}
