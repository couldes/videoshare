/**
 * Axios 请求封装
 *
 * 统一处理：
 *  - 请求头自动附加 token
 *  - 响应统一解包（后端返回 { status, info, data }）
 *  - 错误统一提示
 *
 * 类比：公司的统一收发室，所有快递都经这里，
 * 进来的拆包检查，出去的统一贴上公司标签
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/auth'
import router from '@/router'

// 创建 axios 实例，配置基础 URL 和超时时间
const service = axios.create({
  baseURL: '',      // 所有请求统一加 /api 前缀（vite proxy 会转发到后端）
  timeout: 10000        // 10秒超时
})

// ============================================================
// 请求拦截器：每次请求出发前自动执行
// ============================================================
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      // 把 token 放在请求头里，后端通过 Header 获取来鉴权
      // 后端读取方式：request.getHeader("Authorization")
      config.headers['Authorization'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

// ============================================================
// 响应拦截器：每次收到响应后自动执行
// ============================================================
service.interceptors.response.use(
  response => {
    const res = response.data  // 后端返回的完整数据 { status, info, data }

    // 后端约定：status === 'success' 表示成功
    if (res.status === 'success') {
      return res.data  // 直接返回 data 字段，调用方不用再 .data.data
    }

    // status === 'error' 时，info 里有错误信息
    ElMessage.error(res.info || '操作失败')
    return Promise.reject(new Error(res.info || '操作失败'))
  },
  error => {
    // HTTP 层面的错误（如 404、500、网络断开）
    const status = error.response?.status

    if (status === 401) {
      // 401 = 未授权（token 过期或无效）
      ElMessage.error('登录已过期，请重新登录')
      clearAuth()
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error('没有权限执行此操作')
    } else if (status >= 500) {
      ElMessage.error('服务器错误，请稍后再试')
    } else {
      ElMessage.error(error.message || '网络异常')
    }

    return Promise.reject(error)
  }
)

export default service
