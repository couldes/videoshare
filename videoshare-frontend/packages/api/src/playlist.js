/**
 * 播放列表接口工厂
 * 对应后端 PlaylistController
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createPlaylistApi(request) {
  return {
    /**
     * 创建播放列表
     * POST /playlist/create
     * @param {{ title, description?, coverUrl? }} data
     * @returns {number} playlistId
     */
    createPlaylist: (data) =>
      request({ method: 'POST', url: '/playlist/create', data: new URLSearchParams(data) }),

    /**
     * 更新播放列表
     * POST /playlist/update
     * @param {{ playlistId, title?, description?, coverUrl? }} data
     */
    updatePlaylist: (data) =>
      request({ method: 'POST', url: '/playlist/update', data: new URLSearchParams(data) }),

    /**
     * 删除播放列表
     * DELETE /playlist/{id}
     */
    deletePlaylist: (playlistId) =>
      request({ method: 'DELETE', url: `/playlist/${playlistId}` }),

    /**
     * 添加视频到播放列表
     * POST /playlist/{id}/add-video
     * @param {{ videoId, sortOrder? }} data
     */
    addVideoToPlaylist: (playlistId, data) =>
      request({ method: 'POST', url: `/playlist/${playlistId}/add-video`, data: new URLSearchParams(data) }),

    /**
     * 从播放列表移除视频
     * DELETE /playlist/{id}/video/{videoId}
     */
    removeVideoFromPlaylist: (playlistId, videoId) =>
      request({ method: 'DELETE', url: `/playlist/${playlistId}/video/${videoId}` }),

    /**
     * 播放列表详情（含视频列表）
     * GET /playlist/{id}
     * @returns {{ playlistId, userId, title, description, coverUrl, videoCount, createTime, videos: [...] }}
     */
    getPlaylistDetail: (playlistId) =>
      request({ method: 'GET', url: `/playlist/${playlistId}` }),

    /**
     * 某用户的所有播放列表
     * GET /playlist/user/{userId}
     */
    getUserPlaylists: (userId) =>
      request({ method: 'GET', url: `/playlist/user/${userId}` })
  }
}
