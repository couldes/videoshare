/**
 * 用户端 Pinia Store
 *
 * ★ store 放在 app 内而非 packages/ 的原因：
 *   store 里的 logout() 需要调用 router.push('/login')，
 *   而 router 是 app 级别的，packages/ 里不能引用它。
 *   放在 app 内，auth + router 都可以直接 import，无循环依赖。
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { auth } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  // 初始值从 localStorage 恢复（刷新页面不丢失登录状态）
  const userInfo = ref(auth.getInfo())
  const token    = ref(auth.getToken())

  const isLoggedIn = computed(() => !!token.value)
  const nickName   = computed(() => userInfo.value?.nickName || '')
  const coinCount  = computed(() => userInfo.value?.currentCoinCount || 0)

  /**
   * 登录成功后调用
   * @param {UserInfoVO} data  后端返回的 { userId, nickName, email, ..., token }
   */
  function setLoginInfo(data) {
    token.value    = data.token
    userInfo.value = data
    auth.setToken(data.token)  // ★ 通过 auth 实例持久化，不直接操作 localStorage
    auth.setInfo(data)
  }

  /** 登出：清除内存状态 + localStorage */
  function logout() {
    token.value    = null
    userInfo.value = null
    auth.clear()
  }

  return { userInfo, token, isLoggedIn, nickName, coinCount, setLoginInfo, logout }
})
