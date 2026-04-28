import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '@/utils/auth'

const routes = [
  // 公开页面
  { path: '/',         name: 'Home',     component: () => import('@/views/Home.vue'),         meta: { title: 'VideoShare' } },
  { path: '/login',    name: 'Login',    component: () => import('@/views/Login.vue'),         meta: { title: '登录',  guestOnly: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'),      meta: { title: '注册',  guestOnly: true } },

  // 视频播放页（未登录也能看）
  { path: '/video/:videoId', name: 'VideoPlay',   component: () => import('@/views/VideoPlay.vue'),  meta: { title: '视频播放' } },

  // 个人主页（未登录可查看他人主页）
  { path: '/user/:userId',   name: 'UserProfile', component: () => import('@/views/UserProfile.vue'), meta: { title: '个人主页' } },

  // 需要登录的页面
  { path: '/upload',   name: 'Upload',   component: () => import('@/views/Upload.vue'),        meta: { title: '发布视频', requiresAuth: true } },
  { path: '/favorites',name: 'Favorites',component: () => import('@/views/Favorites.vue'),     meta: { title: '我的收藏', requiresAuth: true } },

  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'VideoShare'}`
  if (to.meta.guestOnly    && auth.isLoggedIn())  return next('/')
  if (to.meta.requiresAuth && !auth.isLoggedIn()) return next({ name: 'Login', query: { redirect: to.fullPath } })
  next()
})

export default router
