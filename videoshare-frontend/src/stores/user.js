/**
 * 用户状态管理（Pinia Store）
 *
 * Pinia 是 Vue3 的官方状态管理库，替代 Vuex
 * 作用：在所有组件之间共享"用户是否登录"、"用户信息"等全局状态
 *
 * 类比：公司公告板，所有部门都能看到，
 * 一个地方更新，所有看的地方都会刷新
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { setToken, setUserInfo, clearAuth, getUserInfo, getToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {

  // ============================================================
  // State（状态）：响应式数据
  // ============================================================

  // 用户信息，初始从 localStorage 恢复（刷新页面后不丢失登录状态）
  const userInfo = ref(getUserInfo())
  const token    = ref(getToken())

  // ============================================================
  // Getters（计算属性）：基于 state 派生的只读数据
  // ============================================================

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 用户昵称，未登录时返回空字符串 */
  const nickName = computed(() => userInfo.value?.nickName || '')

  /** 当前硬币数 */
  const coinCount = computed(() => userInfo.value?.currentCoinCount || 0)

  // ============================================================
  // Actions（方法）：修改状态的唯一入口
  // ============================================================

  /**
   * 登录成功后调用，保存用户数据
   * @param {Object} data - 后端返回的 UserInfoVO
   */
  function setLoginInfo(data) {
    token.value    = data.token
    userInfo.value = data
    // 同步持久化到 localStorage
    setToken(data.token)
    setUserInfo(data)
  }

  /** 登出：清除所有状态 */
  function logout() {
    token.value    = null
    userInfo.value = null
    clearAuth()
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    nickName,
    coinCount,
    setLoginInfo,
    logout
  }
})
