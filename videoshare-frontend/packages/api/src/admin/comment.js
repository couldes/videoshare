/**
 * 管理端评论管理接口
 *
 * 对应后端 CommentManageController：
 *   getCommentList()  → GET    /admin/comment/list
 *   updateStatus()    → POST   /admin/comment/updateStatus
 *   deleteComment()   → DELETE /admin/comment/{commentId}
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createAdminCommentApi(request) {
  return {
    /**
     * 评论列表（可按视频ID筛选 + 分页）
     * @param {{ videoId: string, pageNum, pageSize, status? }} params
     */
    getCommentList: (params) =>
      request({ method: 'GET', url: '/admin/comment/list', params }),

    /**
     * 审核评论（通过/拒绝）
     * @param {{ commentId: number, status: 1|2 }} data
     */
    updateStatus: (data) =>
      request({ method: 'POST', url: '/admin/comment/updateStatus', data: new URLSearchParams(data) }),

    /**
     * 强制删除评论
     * @param {number} commentId
     */
    deleteComment: (commentId) =>
      request({ method: 'DELETE', url: `/admin/comment/${commentId}` })
  }
}
