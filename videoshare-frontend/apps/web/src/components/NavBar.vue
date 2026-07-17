<template>
  <header class="navbar">
    <div class="navbar-inner">
      <div class="navbar-left">
        <button class="menu-btn" @click="$emit('toggle-sidebar')">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/></svg>
        </button>
        <RouterLink to="/" class="logo">
          <svg class="logo-icon" viewBox="0 0 24 24" width="28" height="28">
            <path d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0C.488 3.45.029 5.804 0 12c.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0C23.512 20.55 23.971 18.196 24 12c-.029-6.185-.484-8.549-4.385-8.816zM9 16V8l8 4-8 4z" fill="#ff0033"/>
          </svg>
          <span class="logo-text">VideoShare</span>
        </RouterLink>
      </div>

      <div class="navbar-center">
        <div class="search-bar">
          <input v-model="searchQuery" class="search-input" placeholder="搜索"
            @keydown.enter="handleSearch" />
          <button class="search-btn" @click="handleSearch">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          </button>
        </div>
      </div>

      <div class="navbar-right">
        <template v-if="!userStore.isLoggedIn">
          <RouterLink to="/login" class="login-btn">登录</RouterLink>
        </template>
        <template v-else>
          <RouterLink to="/upload" class="upload-btn" title="发布视频">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z"/></svg>
          </RouterLink>

          <NotificationBell />

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-wrap">
              <div class="avatar">
                <img v-if="userStore.userInfo?.avatarUrl" :src="userStore.userInfo.avatarUrl" class="avatar-img" alt="" />
                <span v-else>{{ userStore.nickName.charAt(0).toUpperCase() }}</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <div class="dropdown-header">
                  <div class="dropdown-avatar">
                    <img v-if="userStore.userInfo?.avatarUrl" :src="userStore.userInfo.avatarUrl" class="avatar-img" alt="" />
                    <span v-else>{{ userStore.nickName.charAt(0).toUpperCase() }}</span>
                  </div>
                  <div>
                    <div class="dropdown-name">{{ userStore.nickName }}</div>
                    <div class="dropdown-email">{{ userStore.userInfo?.email }}</div>
                  </div>
                </div>
                <el-dropdown-item command="history">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg>
                  观看历史
                </el-dropdown-item>
                <el-dropdown-item command="profile">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                  个人主页
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
                  我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="analytics" divided>
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg>
                  创作者中心
                </el-dropdown-item>
                <el-dropdown-item command="myVideos">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M18 3v2h-2V3H8v2H6V3H4v18h2v-2h2v2h8v-2h2v2h2V3h-2zM8 17H6v-2h2v2zm0-4H6v-2h2v2zm0-4H6V7h2v2zm10 8h-2v-2h2v2zm0-4h-2v-2h2v2zm0-4h-2V7h2v2z"/></svg>
                  我的视频
                </el-dropdown-item>
                <el-dropdown-item command="theme" divided>
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M20 8.69V4h-4.69L12 .69 8.69 4H4v4.69L.69 12 4 15.31V20h4.69L12 23.31 15.31 20H20v-4.69L23.31 12 20 8.69zM12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6 6 2.69 6 6-2.69 6-6 6zm0-10c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4z"/></svg>
                  {{ isDark ? '浅色主题' : '深色主题' }}
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/></svg>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/NotificationBell.vue'
import { getTheme, toggleTheme as toggleAppTheme } from '@videoshare/utils/theme'

defineEmits(['toggle-sidebar'])
const router    = useRouter()
const userStore = useUserStore()
const searchQuery = ref('')
const isDark = ref(getTheme() === 'dark')

function toggleTheme() {
  toggleAppTheme(); isDark.value = !isDark.value
}

async function handleSearch() {
  const q = searchQuery.value.trim()
  if (q) router.push({ path: '/search', query: { keyword: q } })
}

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning'
    })
    userStore.logout(); ElMessage.success('已退出登录'); router.push('/login')
  } else if (cmd === 'history') {
    router.push('/history')
  } else if (cmd === 'profile') {
    router.push(`/user/${userStore.userInfo?.userId}`)
  } else if (cmd === 'favorites') {
    router.push('/favorites')
  } else if (cmd === 'upload') {
    router.push('/upload')
  } else if (cmd === 'analytics') {
    router.push('/analytics')
  } else if (cmd === 'myVideos') {
    router.push('/my-videos')
  } else if (cmd === 'theme') {
    toggleTheme()
  }
}
</script>

<style scoped>
.navbar {
  position: fixed; top: 0; left: 0; right: 0; height: 56px;
  background: var(--bg-base);
  z-index: 1000;
}
.navbar-inner {
  max-width: 100%;
  height: 100%; padding: 0 16px;
  display: flex; align-items: center; gap: 8px;
}
.navbar-left { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.menu-btn {
  width: 40px; height: 40px; border: none; background: none;
  color: var(--text-1); cursor: pointer; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.menu-btn:hover { background: var(--bg-hover); }
.logo { display: flex; align-items: center; gap: 2px; text-decoration: none; }
.logo-text {
  font-family: var(--font-display); font-size: 18px; font-weight: 700;
  color: var(--text-1); letter-spacing: -.5px;
}
.navbar-center { flex: 1; max-width: 640px; margin: 0 auto; padding: 0 40px; }
.search-bar {
  display: flex; height: 40px;
  border-radius: 20px; overflow: hidden;
  background: var(--bg-input);
  border: 1px solid var(--border);
  transition: var(--transition);
}
.search-bar:focus-within { border-color: #555; }
.search-input {
  flex: 1; padding: 0 16px; background: none; border: none;
  outline: none; color: var(--text-1);
  font-family: var(--font-body); font-size: 14px;
}
.search-input::placeholder { color: var(--text-muted); }
.search-btn {
  width: 56px; background: var(--bg-hover); border: none;
  border-left: 1px solid var(--border);
  color: var(--text-1); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.search-btn:hover { background: #333; }

.navbar-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }

.login-btn {
  padding: 6px 16px; border-radius: 20px; font-size: 13px;
  font-weight: 600; color: #fff; text-decoration: none;
  background: var(--color-accent); transition: var(--transition);
}
.login-btn:hover { background: #cc0029; }

.upload-btn {
  width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-1); text-decoration: none;
  transition: var(--transition);
}
.upload-btn:hover { background: var(--bg-hover); }

.avatar-wrap { display: flex; align-items: center; cursor: pointer; padding: 2px; }
.avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 13px; color: #fff;
  overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }

.user-dropdown {
  background: var(--bg-surface) !important;
  border: 1px solid var(--border) !important;
  padding: 4px !important; min-width: 210px;
}
:deep(.el-dropdown-menu__item) {
  color: var(--text-2) !important; border-radius: var(--radius-sm) !important;
  display: flex !important; align-items: center; gap: 10px; font-size: 13px;
  padding: 8px 14px !important;
}
:deep(.el-dropdown-menu__item:hover) { background: var(--bg-hover) !important; color: var(--text-1) !important; }

.dropdown-header {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px 14px; border-bottom: 1px solid var(--border);
  margin-bottom: 4px;
}
.dropdown-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 14px; color: #fff; flex-shrink: 0;
  overflow: hidden;
}
.dropdown-name  { font-weight: 600; color: var(--text-1); font-size: 13px; }
.dropdown-email { font-size: 11px; color: var(--text-muted); margin-top: 2px; }
</style>
