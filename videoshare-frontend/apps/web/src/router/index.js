import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '@/utils/auth'

const routes = [
  { path: '/',         name: 'Home',     component: () => import('@/views/Home.vue'),     meta: { title: 'VideoShare' } },
  { path: '/login',    name: 'Login',    component: () => import('@/views/Login.vue'),    meta: { title: '登录',  guestOnly: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { title: '注册',  guestOnly: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title || 'VideoShare'
  if (to.meta.guestOnly   && auth.isLoggedIn()) return next('/')
  if (to.meta.requiresAuth && !auth.isLoggedIn()) return next({ name: 'Login', query: { redirect: to.fullPath } })
  next()
})

export default router
