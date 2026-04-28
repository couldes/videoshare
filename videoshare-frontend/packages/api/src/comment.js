/**
 * 评论相关接口工厂
 * 对应后端 CommentController
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createCommentApi(request) {
  return {
    /**
     * 视频评论列表
     * GET /comment/list
     * @returns PaginationResultVO<CommentVO>
     *   CommentVO: { commentId, userId, nickName, avatarUrl, content,
     *                likeCount, createTime, replies: [...] }
     */
    getCommentList: (videoId, params) =>
      request({ method: 'GET', url: '/comment/list', params: { videoId, ...params } }),

    /**
     * 发布评论
     * POST /comment/post
     * @param {{ videoId, content, pCommentId?, replyUserId? }} data
     * @returns CommentVO  新评论的完整信息（含 commentId）
     */
    postComment: (data) =>
      request({ method: 'POST', url: '/comment/post', data: new URLSearchParams(data) }),

    /**
     * 点赞 / 取消点赞评论
     * POST /comment/action
     * @returns {boolean} true=已点赞 false=取消
     */
    likeComment: (commentId) =>
      request({ method: 'POST', url: '/comment/action', data: new URLSearchParams({ commentId }) }),

    /**
     * 删除评论
     * DELETE /comment/{commentId}
     */
    deleteComment: (commentId) =>
      request({ method: 'DELETE', url: `/comment/${commentId}` })
  }
}
