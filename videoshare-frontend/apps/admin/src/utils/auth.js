/**
 * Admin 端 Auth 实例
 *
 * ★ 与 web 端完全对称，只有 prefix 不同：
 *   web:   createAuth('vs_web')   → 'vs_web_token'  / 'vs_web_info'
 *   admin: createAuth('vs_admin') → 'vs_admin_token' / 'vs_admin_info'
 *
 * 两端 token 存在不同的 localStorage key，即使同一浏览器打开也不会互相覆盖。
 */
import { createAuth } from '@videoshare/utils/auth'
import { AUTH_KEYS }  from '@videoshare/constants'

// AUTH_KEYS.ADMIN = 'vs_admin'
export const auth = createAuth(AUTH_KEYS.ADMIN)
