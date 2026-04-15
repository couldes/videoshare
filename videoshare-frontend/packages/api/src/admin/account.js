/**
 * 管理端账号接口
 *
 * 对应后端 AdminAccountController：
 *   getCheckCode() → GET  /admin/account/checkCode
 *   login()        → POST /admin/account/login
 *   logout()       → POST /admin/account/logout
 *
 * @param {import('axios').AxiosInstance} request
 */
export function createAdminAccountApi(request) {
  return {
    getCheckCode: () =>
      request({ method: 'GET', url: '/admin/account/checkCode' }),

    /**
     * @param {{ account, password, checkCode, checkCodeKey }} data
     * @returns {{ token, account }}
     */
    login: (data) =>
      request({ method: 'POST', url: '/admin/account/login', data: new URLSearchParams(data) }),

    logout: () =>
      request({ method: 'POST', url: '/admin/account/logout' })
  }
}
