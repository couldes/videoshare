/**
 * 管理端用户管理接口
 *
 * 对应后端 UserManageController：
 *   getUserList()      → GET    /admin/user/list
 *   updateUserStatus() → POST   /admin/user/updateStatus
 *   deleteUser()       → DELETE /admin/user/delete/{userId}
 *   getDashboard()     → GET    /admin/user/dashboard
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createAdminUserApi(request) {
  return {
    /**
     * 分页查询用户列表
     * @param {{ pageNum, pageSize, keyword?, status? }} params
     * @returns {Promise<PaginationResultVO>}  { total, pageSize, pageNum, pages, list }
     */
    getUserList: (params) =>
      request({ method: 'GET', url: '/admin/user/list', params }),

    /**
     * 修改用户状态
     * @param {{ userId: string, status: 0|1 }} data
     */
    updateUserStatus: (data) =>
      request({ method: 'POST', url: '/admin/user/updateStatus', data: new URLSearchParams(data) }),

    /**
     * 删除用户
     * @param {string} userId
     */
    deleteUser: (userId) =>
      request({ method: 'DELETE', url: `/admin/user/delete/${userId}` }),

    /**
     * Dashboard 统计数据
     * @returns {{ totalUsers, activeUsers, disabledUsers, dailyRegister }}
     */
    getDashboard: () =>
      request({ method: 'GET', url: '/admin/user/dashboard' }),

    /**
     * 用户操作记录（点赞/收藏）
     * @param {string} userId
     * @param {number} pageNum
     * @param {number} pageSize
     * @returns {{ likes, favorites }}
     */
    getUserActions: (userId, pageNum = 1, pageSize = 10) =>
      request({ method: 'GET', url: `/admin/user/${userId}/actions`, params: { pageNum, pageSize } }),

    /**
     * 用户关注/粉丝列表
     * @param {string} userId
     * @param {number} pageNum
     * @param {number} pageSize
     * @returns {{ following, followers, followingCount, followerCount }}
     */
    getUserFollows: (userId, pageNum = 1, pageSize = 10) =>
      request({ method: 'GET', url: `/admin/user/${userId}/follows`, params: { pageNum, pageSize } })
  }
}
