import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '@/utils/auth'

const routes = [
  // 公开页面
  { path: '/',         name: 'Home',     component: () => import('@/views/Home.vue'),         meta: { title: 'VideoShare' } },
  { path: '/login',    name: 'Login',    component: () => import('@/views/Login.vue'),         meta: { title: '登录',  guestOnly: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'),      meta: { title: '注册',  guestOnly: true } },
  { path: '/trending', name: 'Trending', component: () => import('@/views/Trending.vue'),       meta: { title: '热门' } },

  // 搜索页
  { path: '/search',   name: 'Search',   component: () => import('@/views/Search.vue'),        meta: { title: '搜索' } },

  // 视频播放页
  { path: '/video/:videoId', name: 'VideoPlay',   component: () => import('@/views/VideoPlay.vue'),  meta: { title: '视频播放' } },

  // 个人主页
  { path: '/user/:userId',   name: 'UserProfile', component: () => import('@/views/UserProfile.vue'), meta: { title: '个人主页' } },

  // 需要登录的页面
  { path: '/subscriptions', name: 'Subscriptions', component: () => import('@/views/Subscriptions.vue'), meta: { title: '订阅', requiresAuth: true } },
  { path: '/history',  name: 'History',  component: () => import('@/views/History.vue'),       meta: { title: '观看历史', requiresAuth: true } },
  { path: '/upload',   name: 'Upload',   component: () => import('@/views/Upload.vue'),        meta: { title: '发布视频', requiresAuth: true } },
  { path: '/favorites',name: 'Favorites',component: () => import('@/views/Favorites.vue'),     meta: { title: '我的收藏', requiresAuth: true } },

  // 播放列表详情页
  { path: '/playlist/:playlistId', name: 'PlaylistDetail', component: () => import('@/views/PlaylistDetail.vue'), meta: { title: '播放列表' } },

  // 关注列表
  { path: '/user/:userId/following', name: 'Following', component: () => import('@/views/FollowingList.vue'), meta: { title: '关注列表' } },

  // 通知列表页（需登录）
  { path: '/notifications', name: 'Notifications', component: () => import('@/views/Notifications.vue'), meta: { title: '通知', requiresAuth: true } },

  // 创作者中心
  { path: '/analytics', name: 'Analytics', component: () => import('@/views/CreatorAnalytics.vue'), meta: { title: '创作者中心', requiresAuth: true } },
  { path: '/my-videos', name: 'MyVideos', component: () => import('@/views/VideoManage.vue'), meta: { title: '我的视频', requiresAuth: true } },
  { path: '/edit/:videoId', name: 'VideoEdit', component: () => import('@/views/VideoEdit.vue'), meta: { title: '编辑视频', requiresAuth: true } },

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
