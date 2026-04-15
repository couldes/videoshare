<template>
  <header class="navbar">
    <div class="navbar-inner">
      <div class="navbar-left">
        <button class="icon-btn" @click="$emit('toggle-sidebar')"><el-icon><Operation /></el-icon></button>
        <RouterLink to="/" class="logo">
          <span class="logo-icon">▶</span><span class="logo-text">VideoShare</span>
        </RouterLink>
      </div>
      <div class="navbar-center">
        <div class="search-bar">
          <input v-model="searchQuery" class="search-input" placeholder="搜索视频、频道..." @keydown.enter="handleSearch" />
          <button class="search-btn" @click="handleSearch"><el-icon><Search /></el-icon></button>
        </div>
      </div>
      <div class="navbar-right">
        <template v-if="!userStore.isLoggedIn">
          <RouterLink to="/login"    class="btn-ghost">登录</RouterLink>
          <RouterLink to="/register" class="btn-primary">注册</RouterLink>
        </template>
        <template v-else>
          <div class="coin-badge"><el-icon><Coin /></el-icon><span>{{ userStore.coinCount }}</span></div>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-wrap">
              <div class="avatar">{{ userStore.nickName.charAt(0).toUpperCase() }}</div>
              <el-icon class="avatar-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <div class="dropdown-header">
                  <div class="dropdown-avatar">{{ userStore.nickName.charAt(0).toUpperCase() }}</div>
                  <div>
                    <div class="dropdown-name">{{ userStore.nickName }}</div>
                    <div class="dropdown-email">{{ userStore.userInfo?.email }}</div>
                  </div>
                </div>
                <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
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
defineEmits(['toggle-sidebar'])
const router = useRouter(); const userStore = useUserStore()
const searchQuery = ref('')
function handleSearch() { if (searchQuery.value.trim()) ElMessage.info(`搜索：${searchQuery.value}`) }
async function handleCommand(cmd) {
  if (cmd !== 'logout') return
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning' })
  userStore.logout(); ElMessage.success('已退出登录'); router.push('/login')
}
</script>

<style scoped>
.navbar { position: fixed; top: 0; left: 0; right: 0; height: 56px; background: rgba(10,10,12,.92); backdrop-filter: blur(12px); border-bottom: 1px solid var(--border); z-index: 100; }
.navbar-inner { max-width: 1600px; margin: 0 auto; height: 100%; padding: 0 16px; display: flex; align-items: center; gap: 16px; }
.navbar-left { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.icon-btn { width: 36px; height: 36px; border: none; background: none; color: var(--text-2); cursor: pointer; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; transition: var(--transition); font-size: 18px; }
.icon-btn:hover { background: var(--bg-hover); color: var(--text-1); }
.logo { display: flex; align-items: center; gap: 6px; text-decoration: none; color: var(--text-1); }
.logo-icon { font-size: 18px; color: var(--color-accent); }
.logo-text { font-family: var(--font-display); font-size: 17px; font-weight: 700; letter-spacing: -.02em; }
.navbar-center { flex: 1; max-width: 540px; margin: 0 auto; }
.search-bar { display: flex; height: 36px; border-radius: 100px; overflow: hidden; background: var(--bg-input); border: 1px solid var(--border); transition: var(--transition); }
.search-bar:focus-within { border-color: var(--color-accent); }
.search-input { flex: 1; padding: 0 14px; background: none; border: none; outline: none; color: var(--text-1); font-family: var(--font-body); font-size: 13px; }
.search-input::placeholder { color: var(--text-muted); }
.search-btn { width: 42px; background: none; border: none; border-left: 1px solid var(--border); color: var(--text-2); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: var(--transition); }
.search-btn:hover { color: var(--text-1); background: var(--bg-hover); }
.navbar-right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.btn-ghost { padding: 6px 14px; border-radius: 100px; font-size: 13px; font-weight: 500; color: var(--text-1); text-decoration: none; border: 1px solid var(--border); transition: var(--transition); }
.btn-ghost:hover { background: var(--bg-hover); }
.btn-primary { padding: 6px 14px; border-radius: 100px; font-size: 13px; font-weight: 600; color: #fff; text-decoration: none; background: var(--color-accent); transition: var(--transition); }
.btn-primary:hover { filter: brightness(1.1); }
.coin-badge { display: flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 100px; background: var(--bg-card); border: 1px solid var(--border); font-size: 12px; font-weight: 600; color: #f5c542; }
.avatar-wrap { display: flex; align-items: center; gap: 4px; cursor: pointer; padding: 4px; border-radius: var(--radius-md); transition: var(--transition); }
.avatar-wrap:hover { background: var(--bg-hover); }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, var(--color-accent), #7c3aed); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 13px; color: #fff; }
.avatar-arrow { font-size: 11px; color: var(--text-muted); }
:deep(.user-dropdown) { background: var(--bg-card) !important; border: 1px solid var(--border) !important; padding: 4px !important; min-width: 200px; }
:deep(.el-dropdown-menu__item) { color: var(--text-2) !important; border-radius: var(--radius-sm) !important; gap: 8px; }
:deep(.el-dropdown-menu__item:hover) { background: var(--bg-hover) !important; color: var(--text-1) !important; }
.dropdown-header { display: flex; align-items: center; gap: 10px; padding: 10px 12px 14px; }
.dropdown-avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, var(--color-accent), #7c3aed); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; color: #fff; flex-shrink: 0; }
.dropdown-name { font-weight: 600; color: var(--text-1); font-size: 13px; }
.dropdown-email { font-size: 11px; color: var(--text-muted); margin-top: 2px; }
</style>
