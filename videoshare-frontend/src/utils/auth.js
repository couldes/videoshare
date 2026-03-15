/**
 * Token 管理工具
 *
 * 统一管理 localStorage 中的用户凭证
 * 所有组件通过这里存取 token，不直接操作 localStorage
 *
 * 类比：公司的门禁卡系统，统一由前台管理，
 * 员工不自己刻卡，通过前台领取/归还
 */

const TOKEN_KEY   = 'vs_token'    // localStorage 中存 token 的键名
const USER_KEY    = 'vs_userinfo' // localStorage 中存用户信息的键名

// ---------- Token ----------

/** 保存 token */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/** 读取 token */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/** 删除 token（登出时调用）*/
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

// ---------- 用户信息 ----------

/** 保存用户信息（对象会被序列化为JSON字符串）*/
export function setUserInfo(userInfo) {
  localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
}

/** 读取用户信息（自动反序列化）*/
export function getUserInfo() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

/** 删除用户信息 */
export function removeUserInfo() {
  localStorage.removeItem(USER_KEY)
}

// ---------- 组合操作 ----------

/** 登出：同时清除 token 和用户信息 */
export function clearAuth() {
  removeToken()
  removeUserInfo()
}

/** 判断是否已登录 */
export function isLoggedIn() {
  return !!getToken()
}
