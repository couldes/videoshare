<template>
  <aside class="sidebar" :class="{ collapsed }">
    <nav class="sidebar-nav">
      <div class="nav-section">
        <SidebarItem icon="home" label="首页" to="/" :collapsed="collapsed" />
        <SidebarItem icon="trending" label="热门" to="/trending" :collapsed="collapsed" />
        <SidebarItem icon="subscriptions" label="订阅" to="/subscriptions" :collapsed="collapsed" />
      </div>

      <template v-if="userStore.isLoggedIn">
        <div class="nav-divider" />
        <div class="nav-section">
          <SidebarItem icon="history" label="历史记录" to="/history" :collapsed="collapsed" />
          <SidebarItem icon="favorites" label="收藏夹" to="/favorites" :collapsed="collapsed" />
          <SidebarItem icon="upload" label="上传视频" to="/upload" :collapsed="collapsed" />
        </div>
      </template>

      <div class="nav-divider" />
      <div class="nav-section">
        <SidebarItem icon="explore" label="探索" to="/" :collapsed="collapsed" />
      </div>
    </nav>
  </aside>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import SidebarItem from './SidebarItem.vue'
defineProps({ collapsed: { type: Boolean, default: false } })
const userStore = useUserStore()
</script>

<style scoped>
.sidebar {
  width: 240px; flex-shrink: 0; height: calc(100vh - 56px);
  position: sticky; top: 56px; overflow-y: auto;
  padding: 12px 0; display: flex; flex-direction: column;
  transition: width .15s ease; background: var(--bg-base);
}
.sidebar.collapsed { width: 72px; }
.sidebar::-webkit-scrollbar { width: 0; }
.nav-section { display: flex; flex-direction: column; padding: 0 8px; }
.nav-divider { height: 1px; background: var(--border); margin: 8px 0; }
</style>
