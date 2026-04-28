/**
 * Web 端 API 组装（完整版，含新功能接口）
 */
import { createRequest }      from '@videoshare/api/request'
import { createAccountApi }   from '@videoshare/api/account'
import { createVideoApi }     from '@videoshare/api/video'
import { createCommentApi }   from '@videoshare/api/comment'
import { createProfileApi }   from '@videoshare/api/profile'
import { auth }               from '@/utils/auth'
import router                 from '@/router'
import { ElMessage }          from 'element-plus'

export const request = createRequest({
  getToken:       () => auth.getToken(),
  onUnauthorized: () => {
    ElMessage.error('登录已过期，请重新登录')
    auth.clear()
    router.push('/login')
  }
})

// 统一错误提示（非 401）
request.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status !== 401) {
      ElMessage.error(err.message || '操作失败')
    }
    return Promise.reject(err)
  }
)

// ★ 导出所有业务 API 实例
export const accountApi = createAccountApi(request)
export const videoApi   = createVideoApi(request)
export const commentApi = createCommentApi(request)
export const profileApi = createProfileApi(request)
