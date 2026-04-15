/**
 * 用户端账号接口
 *
 * 对应后端 AccountController：
 *   getCheckCode() → GET  /account/checkCode
 *   register()     → POST /account/register
 *   login()        → POST /account/login
 *
 * 注意：函数接收 request 实例作为第一参数（由各 app 注入），
 * 不在这里直接 import request，保持包的独立性。
 *
 * 使用方式（apps/web/src/api/index.js）：
 *   import { createAccountApi } from '@videoshare/api/account'
 *   import { request } from './request'
 *   export const accountApi = createAccountApi(request)
 *   // 然后：accountApi.getCheckCode()
 */

/**
 * @param {import('axios').AxiosInstance} request  已配置好拦截器的 axios 实例
 */
export function createAccountApi(request) {
  return {
    /** 获取验证码 → { checkCode: base64图片, checkCodeKey: Redis凭证 } */
    getCheckCode: () =>
      request({ method: 'GET', url: '/account/checkCode' }),

    /**
     * 用户注册
     * @param {{ email, nickName, registerPassword, checkCode, checkCodeKey }} data
     */
    register: (data) =>
      request({ method: 'POST', url: '/account/register', data: new URLSearchParams(data) }),

    /**
     * 用户登录
     * @param {{ email, password, checkCode, checkCodeKey }} data
     * @returns {Promise<UserInfoVO>}  { userId, nickName, email, sex, theme, currentCoinCount, token }
     */
    login: (data) =>
      request({ method: 'POST', url: '/account/login', data: new URLSearchParams(data) })
  }
}
