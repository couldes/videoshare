/**
 * Axios 实例工厂
 *
 * 核心设计：通过依赖注入消除 web/request.js 和 admin/request.js 的重复。
 * 两端的差异（token 来源、401 跳转）都通过参数传入，核心逻辑只写一遍。
 *
 * @param {Object} options
 * @param {string}   options.baseURL       请求基础路径（web: '', admin: ''）
 * @param {Function} options.getToken      读取 token 的函数（由各端的 auth 实例提供）
 * @param {Function} options.onUnauthorized 401 时的回调（通常是跳登录页）
 *
 * @returns axios 实例（已挂载拦截器）
 *
 * 使用方式：
 *   // apps/web/src/api/index.js
 *   import { createRequest } from '@videoshare/api/request'
 *   import { auth } from '@/utils/auth'
 *   import router from '@/router'
 *   export const request = createRequest({
 *     getToken:        () => auth.getToken(),
 *     onUnauthorized:  () => router.push('/login')
 *   })
 *
 *   // apps/admin/src/api/index.js
 *   export const request = createRequest({
 *     getToken:        () => auth.getToken(),
 *     onUnauthorized:  () => router.push('/login')
 *   })
 */

import axios from 'axios'

export function createRequest({ baseURL = '', getToken, onUnauthorized }) {
  const service = axios.create({ baseURL, timeout: 10000 })

  // ===== 请求拦截：自动附加 token =====
  service.interceptors.request.use(
    config => {
      const token = getToken?.()
      if (token) config.headers['Authorization'] = token
      return config
    },
    err => Promise.reject(err)
  )

  // ===== 响应拦截：解包后端统一格式 { status, info, data } =====
  service.interceptors.response.use(
    res => {
      const data = res.data
      // 后端约定：status === 'success' 表示业务成功
      if (data.status === 'success') return data.data
      // 业务失败：弹出 info 里的错误信息
      // 注意：这里不直接 import ElMessage（避免 packages 依赖 element-plus）
      // 错误由上层（调用方）处理，或通过注入 onError 回调
      return Promise.reject(new Error(data.info || '操作失败'))
    },
    err => {
      const status = err.response?.status
      if (status === 401) {
        // token 过期 → 执行外部注入的跳转回调
        onUnauthorized?.()
      }
      return Promise.reject(err)
    }
  )

  return service
}
