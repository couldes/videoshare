/**
 * Auth 工厂函数
 *
 * 核心设计：通过传入 prefix 区分 web 和 admin 的 localStorage key，
 * 两端共用同一套逻辑，彻底消除 web/auth.js 和 admin/auth.js 的重复。
 *
 * 使用方式：
 *   // apps/web/src/utils/auth.js
 *   import { createAuth } from '@videoshare/utils/auth'
 *   import { AUTH_KEYS } from '@videoshare/constants'
 *   export const auth = createAuth(AUTH_KEYS.WEB)
 *   //  → 读写 'vs_web_token' / 'vs_web_info'
 *
 *   // apps/admin/src/utils/auth.js
 *   import { createAuth } from '@videoshare/utils/auth'
 *   import { AUTH_KEYS } from '@videoshare/constants'
 *   export const auth = createAuth(AUTH_KEYS.ADMIN)
 *   //  → 读写 'vs_admin_token' / 'vs_admin_info'
 *
 * @param {string} prefix  key 前缀，如 'vs_web' 或 'vs_admin'
 */
export function createAuth(prefix) {
  const TOKEN_KEY = `${prefix}_token`
  const INFO_KEY  = `${prefix}_info`

  return {
    /** 保存 token */
    setToken: (t) =>
      localStorage.setItem(TOKEN_KEY, t),

    /** 读取 token */
    getToken: () =>
      localStorage.getItem(TOKEN_KEY),

    /** 删除 token */
    removeToken: () =>
      localStorage.removeItem(TOKEN_KEY),

    /** 保存用户/管理员信息（序列化为 JSON 字符串）*/
    setInfo: (info) =>
      localStorage.setItem(INFO_KEY, JSON.stringify(info)),

    /** 读取信息（自动反序列化）*/
    getInfo: () => {
      const raw = localStorage.getItem(INFO_KEY)
      return raw ? JSON.parse(raw) : null
    },

    /** 删除信息 */
    removeInfo: () =>
      localStorage.removeItem(INFO_KEY),

    /** 清除全部（登出时调用）*/
    clear: () => {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(INFO_KEY)
    },

    /** 是否已登录（token 非空即视为已登录）*/
    isLoggedIn: () =>
      !!localStorage.getItem(TOKEN_KEY)
  }
}
