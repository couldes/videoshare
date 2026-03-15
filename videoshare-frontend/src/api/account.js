/**
 * 账号相关接口
 *
 * 每个函数对应后端 AccountController 的一个接口
 * 路径完全一一对应：
 *
 *   getCheckCode()  → GET  /account/checkCode
 *   register()      → POST /account/register
 *   login()         → POST /account/login
 */

import request from './request'

/**
 * 获取验证码
 * 对应后端：AccountController.getCheckCode()
 *
 * @returns Promise<{ checkCode: string, checkCodeKey: string }>
 *   - checkCode:    Base64 图片字符串，直接赋给 <img src>
 *   - checkCodeKey: Redis 凭证，后续提交时必须带上
 */
export function getCheckCode() {
  return request({
    method: 'GET',
    url: '/account/checkCode'
  })
}

/**
 * 用户注册
 * 对应后端：AccountController.register()
 *
 * @param {Object} data
 * @param {string} data.email            - 邮箱
 * @param {string} data.nickName         - 昵称
 * @param {string} data.registerPassword - 密码（明文，后端会 MD5 加密）
 * @param {string} data.checkCodeKey     - 验证码凭证
 * @param {string} data.checkCode        - 用户填写的验证码
 */
export function register(data) {
  // 后端用 @RequestParam 接收，所以用 URLSearchParams（form格式），不是 JSON
  return request({
    method: 'POST',
    url: '/account/register',
    data: new URLSearchParams(data)  // 转成 form 格式：key=val&key=val
  })
}

/**
 * 用户登录
 * 对应后端：AccountController.login()
 *
 * @param {Object} data
 * @param {string} data.email        - 邮箱
 * @param {string} data.password     - 密码（明文）
 * @param {string} data.checkCode    - 验证码
 * @param {string} data.checkCodeKey - 验证码凭证
 *
 * @returns Promise<UserInfoVO>
 *   { userId, nickName, email, sex, theme, currentCoinCount, token }
 */
export function login(data) {
  return request({
    method: 'POST',
    url: '/account/login',
    data: new URLSearchParams(data)
  })
}
