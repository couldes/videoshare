/**
 * Admin 端 API 组装
 *
 * ★ 与 web 端的 api/index.js 结构完全对称：
 *   1. createRequest()           → 注入 admin 端 getToken / onUnauthorized
 *   2. createAdminAccountApi()   → 注入 request 实例
 *   3. createAdminUserApi()      → 注入 request 实例
 *
 * 依赖注入让 packages/api/ 下的代码完全不感知"用谁的 token"，
 * 这里是唯一"知道"admin 端身份的地方。
 *
 * 用法（在 Vue 组件或 store 中）：
 *   import { adminAccountApi, adminUserApi } from '@/api'
 *   await adminAccountApi.login({ account, password, ... })
 *   await adminUserApi.getUserList({ pageNum: 1, pageSize: 15 })
 */
import { createRequest }          from '@videoshare/api/request'
import { createAdminAccountApi }  from '@videoshare/api/admin/account'
import { createAdminUserApi }     from '@videoshare/api/admin/user'
import { auth }                   from '@/utils/auth'
import router                     from '@/router'
import { ElMessage }              from 'element-plus'

// ★ 注入 admin 端的 token 读取函数 和 401 处理
export const request = createRequest({
  getToken:       () => auth.getToken(),
  onUnauthorized: () => {
    ElMessage.error('登录已过期，请重新登录')
    auth.clear()
    router.push('/login')
  }
})

// 业务层错误统一提示（非 401 错误在此弹出）
request.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status !== 401) {
      ElMessage.error(err.message || '操作失败')
    }
    return Promise.reject(err)
  }
)

// ★ 组装并导出业务 API 实例
export const adminAccountApi = createAdminAccountApi(request)
export const adminUserApi    = createAdminUserApi(request)
