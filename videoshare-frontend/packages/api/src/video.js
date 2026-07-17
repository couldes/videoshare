/**
 * 视频相关接口工厂
 * 对应后端 VideoController
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createVideoApi(request) {
  return {
    /**
     * 首页视频列表
     * GET /video/list
     * @param {{ pageNum, pageSize, category? }} params
     */
    getVideoList: (params) =>
      request({ method: 'GET', url: '/video/list', params }),

    /**
     * 视频详情（含播放地址）
     * GET /video/{videoId}
     * @returns VideoDetailVO { videoId, title, videoUrl, coverUrl, duration,
     *                          viewCount, likeCount, commentCount, favoriteCount,
     *                          status, userInfo: { userId, nickName, avatarUrl } }
     */
    getVideoDetail: (videoId) =>
      request({ method: 'GET', url: `/video/${videoId}` }),

    /**
     * 上传视频文件
     * POST /video/upload
     * @param {File} file
     * @param {Function} onProgress  上传进度回调 (percent: number) => void
     * @returns {{ videoId, status }}  status: 0=处理中 1=就绪
     */
    uploadVideo: (file, onProgress) => {
      const form = new FormData()
      form.append('file', file)
      return request({
        method: 'POST',
        url: '/video/upload',
        data: form,
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (e) => {
          if (onProgress && e.total) {
            onProgress(Math.round((e.loaded * 100) / e.total))
          }
        }
      })
    },

    /**
     * 发布视频
     * POST /video/publish
     * @param {{ title, description, coverUrl, videoUrl, category, tags }} data
     */
    publishVideo: (data) =>
      request({ method: 'POST', url: '/video/publish', data: new URLSearchParams(data) }),

    /**
     * 重新发布已下架视频（用户端，复用已有信息）
     * POST /video/republish
     */
    republishVideo: (videoId) =>
      request({ method: 'POST', url: '/video/republish', data: new URLSearchParams({ videoId }) }),

    /**
     * 下架自己的已发布视频（用户端）
     * POST /video/unpublish
     */
    unpublishVideo: (videoId) =>
      request({ method: 'POST', url: '/video/unpublish', data: new URLSearchParams({ videoId }) }),

    /**
     * 某用户的视频列表（个人主页）
     * GET /video/user/{userId}
     */
    getUserVideos: (userId, params) =>
      request({ method: 'GET', url: `/video/user/${userId}`, params }),

    /**
     * 编辑视频信息
     * POST /video/update
     * @param {{ videoId, title, description?, category?, tags? }} data
     */
    updateVideo: (data) =>
      request({ method: 'POST', url: '/video/update', data: new URLSearchParams(data) }),

    /**
     * 点赞 / 收藏 / 取消
     * POST /video/action
     * @param {{ videoId, actionType: 1|2 }} data  1=点赞 2=收藏
     * @returns {boolean} true=已操作 false=已取消
     */
    doAction: (data) =>
      request({ method: 'POST', url: '/video/action', data: new URLSearchParams(data) }),

    /**
     * 查询当前用户对视频的操作状态
     * GET /video/action/check
     * @returns {{ liked: boolean, favorited: boolean }}
     */
    checkAction: (videoId) =>
      request({ method: 'GET', url: '/video/action/check', params: { videoId } }),

    /**
     * 热门视频列表（按 heat 降序）
     * GET /video/trending
     * @param {{ pageNum, pageSize }} params
     */
    getTrendingList: (params) =>
      request({ method: 'GET', url: '/video/trending', params }),

    /**
     * 订阅视频列表（已关注用户的视频）
     * GET /video/subscriptions（需要登录）
     * @param {{ pageNum, pageSize }} params
     */
    getSubscriptionVideos: (params) =>
      request({ method: 'GET', url: '/video/subscriptions', params }),

    /**
     * 搜索视频
     * GET /video/search
     * @param {{ keyword, orderBy, pageNum, pageSize }} params
     */
    searchVideos: (params) =>
      request({ method: 'GET', url: '/video/search', params }),

    /**
     * 个性化推荐视频列表（未登录时返回热门）
     * GET /video/recommend
     * @param {{ pageNum, pageSize }} params
     */
    getRecommendedVideos: (params) =>
      request({ method: 'GET', url: '/video/recommend', params }),

    /**
     * 观看历史列表
     * GET /user/history（需要登录）
     * @param {{ pageNum, pageSize }} params
     */
    getWatchHistory: (params) =>
      request({ method: 'GET', url: '/user/history', params }),

    /**
     * 清空观看历史
     * DELETE /user/history（需要登录）
     */
    clearWatchHistory: () =>
      request({ method: 'DELETE', url: '/user/history' }),

    /**
     * 删除单条观看记录
     * DELETE /user/history/{videoId}（需要登录）
     */
    deleteWatchHistoryRecord: (videoId) =>
      request({ method: 'DELETE', url: `/user/history/${videoId}` })
  }
}
