/**
 * 路由配置
 *
 * 路由 = URL 和 页面组件 的对应关系
 * 例：访问 /login → 显示 Login.vue 组件
 *
 * 路由守卫：类似门卫，进某些页面前要检查资格
 */

import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/utils/auth'

// 路由懒加载：只有访问到这个路由时才加载对应的组件文件
// 好处：首屏加载更快，不会一次性加载所有页面
const Home     = () => import('@/views/Home.vue')
const Login    = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')

// 路由表：定义 URL → 组件 的映射关系
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: 'VideoShare - 首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录 - VideoShare',
      guestOnly: true  // 已登录用户不能访问（再访问会跳转回首页）
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: '注册 - VideoShare',
      guestOnly: true
    }
  },
  // 404 兜底：访问不存在的路由，重定向到首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),  // 使用 HTML5 history 模式（URL 不带 #）
  routes,
  // 每次切换路由，滚动到顶部
  scrollBehavior: () => ({ top: 0 })
})

// ============================================================
// 全局前置路由守卫
// 每次路由跳转前执行，相当于"门卫检查"
// ============================================================
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || 'VideoShare'

  const loggedIn = isLoggedIn()

  // guestOnly 页面（登录/注册）：已登录用户直接跳回首页
  if (to.meta.guestOnly && loggedIn) {
    next('/')
    return
  }

  // requiresAuth 页面：未登录用户跳转到登录页
  // 并保存"原本想去的地址"，登录后可以自动跳回
  if (to.meta.requiresAuth && !loggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  next() // 放行
})

export default router
