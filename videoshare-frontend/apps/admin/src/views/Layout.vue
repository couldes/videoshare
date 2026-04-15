<template>
  <div class="admin-shell">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-logo">
        <span class="logo-icon">▶</span>
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">VS Admin</span>
        </transition>
      </div>

      <nav class="sidebar-nav">
        <RouterLink v-for="item in menuItems" :key="item.path"
          :to="item.path" class="nav-item"
          :class="{ active: $route.path.startsWith(item.path) }">
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <transition name="fade">
            <span v-if="!collapsed" class="nav-label">{{ item.label }}</span>
          </transition>
          <div v-if="collapsed" class="nav-tooltip">{{ item.label }}</div>
        </RouterLink>
      </nav>

      <button class="sidebar-toggle" @click="collapsed = !collapsed">
        <el-icon><component :is="collapsed ? 'DArrowRight' : 'DArrowLeft'" /></el-icon>
      </button>
    </aside>

    <!-- 主区域 -->
    <div class="main-area">
      <!-- 顶部栏 -->
      <header class="topbar">
        <div class="breadcrumb">
          <span class="bc-root">VideoShare</span>
          <span class="bc-sep">/</span>
          <span class="bc-current">{{ currentTitle }}</span>
        </div>
        <div class="topbar-right">
          <!-- 实时时钟 -->
          <div class="sys-time mono">{{ currentTime }}</div>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="admin-info">
              <div class="admin-avatar">{{ adminStore.account.charAt(0).toUpperCase() }}</div>
              <div class="admin-meta">
                <span class="admin-name">{{ adminStore.account }}</span>
                <span class="admin-role mono">ADMINISTRATOR</span>
              </div>
              <el-icon class="admin-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="admin-dropdown">
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { adminAccountApi } from '@/api'

const $route     = useRoute()
const router     = useRouter()
const adminStore = useAdminStore()
const collapsed  = ref(false)

const menuItems = [
  { path: '/dashboard', icon: 'DataAnalysis', label: '数据看板' },
  { path: '/user',      icon: 'User',         label: '用户管理' },
]

const currentTitle = computed(
  () => menuItems.find(m => $route.path.startsWith(m.path))?.label || '管理后台'
)

// 实时时钟
const currentTime = ref('')
let timer = null
function updateTime() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}
onMounted(() => { updateTime(); timer = setInterval(updateTime, 1000) })
onUnmounted(() => clearInterval(timer))

async function handleCommand(cmd) {
  if (cmd !== 'logout') return
  await ElMessageBox.confirm('确定要退出管理后台吗？', '退出确认', {
    confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning'
  })
  try {
    // POST /admin/account/logout → AdminAccountController.logout()
    await adminAccountApi.logout()
  } catch { /* 即使接口失败也继续本地登出 */ }
  adminStore.logout()
  ElMessage.success('已安全退出')
  router.push('/login')
}
</script>

<style scoped>
.admin-shell { display: flex; min-height: 100vh; background: var(--bg-base); }

/* 侧边栏 */
.sidebar { width: var(--sidebar-w); flex-shrink: 0; background: var(--bg-surface); border-right: 1px solid var(--border); display: flex; flex-direction: column; position: fixed; top: 0; left: 0; bottom: 0; z-index: 50; transition: width .22s cubic-bezier(.4,0,.2,1); overflow: hidden; }
.sidebar.collapsed { width: 60px; }
.sidebar-logo { display: flex; align-items: center; gap: 10px; padding: 0 16px; height: var(--header-h); border-bottom: 1px solid var(--border); flex-shrink: 0; overflow: hidden; }
.logo-icon { font-size: 16px; color: var(--color-accent); flex-shrink: 0; }
.logo-text { font-weight: 700; font-size: 15px; letter-spacing: -.01em; white-space: nowrap; color: var(--text-1); }
.sidebar-nav { flex: 1; padding: 12px 8px; display: flex; flex-direction: column; gap: 2px; overflow-y: auto; overflow-x: hidden; }
.nav-item { display: flex; align-items: center; gap: 12px; padding: 9px 12px; border-radius: var(--radius-md); text-decoration: none; color: var(--text-2); font-size: 13px; font-weight: 500; position: relative; transition: var(--transition); white-space: nowrap; overflow: hidden; }
.nav-item:hover { background: var(--bg-hover); color: var(--text-1); }
.nav-item.active { background: var(--color-accent-dim); color: var(--color-accent); border: 1px solid var(--border-accent); }
.nav-icon { font-size: 17px; flex-shrink: 0; }
.nav-tooltip { position: absolute; left: calc(100% + 12px); background: var(--bg-card); border: 1px solid var(--border-strong); color: var(--text-1); padding: 5px 10px; border-radius: var(--radius-sm); font-size: 12px; white-space: nowrap; opacity: 0; pointer-events: none; transition: opacity .15s; box-shadow: var(--shadow-card); z-index: 200; }
.sidebar.collapsed .nav-item:hover .nav-tooltip { opacity: 1; }
.sidebar.collapsed .nav-item { justify-content: center; padding: 10px; }
.sidebar-toggle { height: 44px; display: flex; align-items: center; justify-content: center; border: none; border-top: 1px solid var(--border); background: none; color: var(--text-muted); cursor: pointer; transition: var(--transition); font-size: 15px; flex-shrink: 0; }
.sidebar-toggle:hover { color: var(--text-1); background: var(--bg-hover); }

/* 主区域 */
.main-area { flex: 1; margin-left: var(--sidebar-w); transition: margin-left .22s cubic-bezier(.4,0,.2,1); display: flex; flex-direction: column; min-height: 100vh; }
.sidebar.collapsed ~ .main-area { margin-left: 60px; }
.topbar { height: var(--header-h); background: var(--bg-surface); border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; padding: 0 20px; position: sticky; top: 0; z-index: 40; flex-shrink: 0; }
.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.bc-root { color: var(--text-muted); } .bc-sep { color: var(--text-muted); font-size: 11px; } .bc-current { color: var(--text-1); font-weight: 600; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.sys-time { font-size: 12px; color: var(--text-muted); letter-spacing: .04em; }
.admin-info { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 5px 8px; border-radius: var(--radius-md); transition: var(--transition); border: 1px solid transparent; }
.admin-info:hover { background: var(--bg-hover); border-color: var(--border); }
.admin-avatar { width: 28px; height: 28px; border-radius: var(--radius-sm); background: var(--color-accent); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; color: #fff; flex-shrink: 0; }
.admin-meta { display: flex; flex-direction: column; align-items: flex-start; }
.admin-name { font-size: 12px; font-weight: 600; color: var(--text-1); line-height: 1.2; }
.admin-role { font-size: 10px; color: var(--text-muted); letter-spacing: .06em; }
.admin-arrow { font-size: 11px; color: var(--text-muted); }
:deep(.admin-dropdown) { background: var(--bg-card) !important; border-color: var(--border-strong) !important; }
:deep(.admin-dropdown .el-dropdown-menu__item) { color: var(--text-2) !important; border-radius: var(--radius-sm) !important; gap: 8px !important; font-size: 13px !important; }
:deep(.admin-dropdown .el-dropdown-menu__item:hover) { background: var(--bg-hover) !important; color: var(--danger) !important; }
.content { flex: 1; padding: 24px; overflow-y: auto; }

.fade-enter-active, .fade-leave-active { transition: opacity .15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
