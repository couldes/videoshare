/**
 * Web 端 Auth 实例
 *
 * ★ 使用工厂函数 createAuth，传入 web 端的 key 前缀
 *   最终读写 localStorage 的 key 为：
 *     'vs_web_token'  → token
 *     'vs_web_info'   → 用户信息
 *
 * 组件/store 调用示例：
 *   import { auth } from '@/utils/auth'
 *   auth.setToken(token)
 *   auth.getToken()
 *   auth.isLoggedIn()
 *   auth.clear()
 */
import { createAuth } from '@videoshare/utils/auth'
import { AUTH_KEYS }  from '@videoshare/constants'

// AUTH_KEYS.WEB = 'vs_web'
export const auth = createAuth(AUTH_KEYS.WEB)
