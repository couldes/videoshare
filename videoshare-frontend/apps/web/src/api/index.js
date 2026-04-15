/**
 * Web 端 API 组装
 *
 * ★ 两个工厂函数在这里完成"组装"：
 *   1. createRequest()    → 注入 web 端的 getToken 和 onUnauthorized
 *   2. createAccountApi() → 注入上面创建好的 request 实例
 *
 * 这是"依赖注入"模式的体现：
 *   - packages/api/request.js  只定义"如何做"（拦截器逻辑）
 *   - packages/api/account.js  只定义"做什么"（接口路径）
 *   - 这个文件       决定"用谁的 token"和"401 时去哪"
 *
 * 用法（在 Vue 组件或 store 中）：
 *   import { accountApi } from '@/api'
 *   const data = await accountApi.login({ email, password, ... })
 */
import { createRequest }     from '@videoshare/api/request'
import { createAccountApi }  from '@videoshare/api/account'
import { auth }              from '@/utils/auth'
import router                from '@/router'
import { ElMessage }         from 'element-plus'

// ★ 注入 web 端的 token 读取函数 和 401 处理逻辑
export const request = createRequest({
  getToken:       () => auth.getToken(),
  onUnauthorized: () => {
    ElMessage.error('登录已过期，请重新登录')
    auth.clear()
    router.push('/login')
  }
})

// 业务层错误提示（request 包不依赖 element-plus，在这里统一处理）
request.interceptors.response.use(
  res => res,
  err => {
    const msg = err.message || '网络异常'
    if (err.response?.status !== 401) ElMessage.error(msg)
    return Promise.reject(err)
  }
)

// ★ 注入 request 实例，得到带类型提示的接口对象
export const accountApi = createAccountApi(request)
