/**
 * Admin 端 Pinia Store
 *
 * ★ 放在 app 内（而非 packages/stores/）的原因：
 *   logout() 需要调用 router.push('/login')，
 *   router 是 app 级的，packages/ 里无法安全 import。
 *   在 app 内 import auth + router 不存在循环依赖。
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { auth } from '@/utils/auth'

export const useAdminStore = defineStore('admin', () => {
  // 初始从 localStorage 恢复（刷新页面不丢登录状态）
  const adminInfo = ref(auth.getInfo())
  const token     = ref(auth.getToken())

  const isLoggedIn = computed(() => !!token.value)
  const account    = computed(() => adminInfo.value?.account || '')

  /**
   * 登录成功后调用
   * @param {{ token, account }} data  后端 AdminAccountController.login() 返回值
   */
  function setLoginInfo(data) {
    token.value     = data.token
    adminInfo.value = data
    auth.setToken(data.token)  // ★ 经由 auth 实例持久化，key = 'vs_admin_token'
    auth.setInfo(data)
  }

  /** 登出：清除内存 + localStorage */
  function logout() {
    token.value     = null
    adminInfo.value = null
    auth.clear()
  }

  return { adminInfo, token, isLoggedIn, account, setLoginInfo, logout }
})
