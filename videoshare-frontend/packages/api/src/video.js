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
     *                          userInfo: { userId, nickName, avatarUrl } }
     */
    getVideoDetail: (videoId) =>
      request({ method: 'GET', url: `/video/${videoId}` }),

    /**
     * 上传视频文件
     * POST /video/upload
     * @param {File} file
     * @param {Function} onProgress  上传进度回调 (percent: number) => void
     * @returns {{ videoUrl, duration }}
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
     * 某用户的视频列表（个人主页）
     * GET /video/user/{userId}
     */
    getUserVideos: (userId, params) =>
      request({ method: 'GET', url: `/video/user/${userId}`, params }),

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
      request({ method: 'GET', url: '/video/action/check', params: { videoId } })
  }
}
