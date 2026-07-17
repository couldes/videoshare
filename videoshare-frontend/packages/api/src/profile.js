/**
 * 用户个人主页接口工厂
 * 对应后端 UserProfileController
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createProfileApi(request) {
  return {
    /**
     * 获取用户公开信息
     * GET /user/profile/{userId}
     * @returns UserProfileVO { userId, nickName, bio, avatarUrl,
     *                          videoCount, followerCount, followingCount,
     *                          isFollowing }
     */
    getProfile: (userId) =>
      request({ method: 'GET', url: `/user/profile/${userId}` }),

    /**
     * 更新个人简介
     * POST /user/profile/update
     * @param {{ bio?, avatarUrl? }} data
     */
    updateProfile: (data) =>
      request({ method: 'POST', url: '/user/profile/update', data: new URLSearchParams(data) }),

    /**
     * 关注 / 取消关注
     * POST /user/follow
     * @returns {boolean} true=已关注 false=取消关注
     */
    toggleFollow: (followUserId) =>
      request({ method: 'POST', url: '/user/follow', data: new URLSearchParams({ followUserId }) }),

    /**
     * 查询是否已关注
     * GET /user/follow/status
     */
    checkFollow: (followUserId) =>
      request({ method: 'GET', url: '/user/follow/status', params: { followUserId } }),

    /**
     * 搜索用户（按昵称/邮箱模糊匹配）
     * GET /user/search?keyword=xxx
     * @returns [{ userId, nickName, email, joinTime }]
     */
    searchUsers: (keyword) =>
      request({ method: 'GET', url: '/user/search', params: { keyword } }),

    /**
     * 关注列表 / 粉丝列表
     * GET /user/{userId}/following
     * GET /user/{userId}/followers
     */
    getFollowingList: (userId) =>
      request({ method: 'GET', url: `/user/${userId}/following` }),
    getFollowerList: (userId) =>
      request({ method: 'GET', url: `/user/${userId}/followers` }),

    /**
     * 我的收藏视频列表
     * GET /user/favorites
     */
    getFavorites: (params) =>
      request({ method: 'GET', url: '/user/favorites', params }),

    /**
     * 上传头像或背景图
     * POST /user/uploadImage
     * @param {File} file
     * @param {"avatar"|"background"} type
     * @param {Function} onProgress
     * @returns {{ url: string }}
     */
    uploadImage: (file, type, onProgress) => {
      const form = new FormData()
      form.append('file', file)
      form.append('type', type)
      return request({
        method: 'POST',
        url: '/user/uploadImage',
        data: form,
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (e) => {
          if (onProgress && e.total) {
            onProgress(Math.round((e.loaded * 100) / e.total))
          }
        }
      })
    }
  }
}
