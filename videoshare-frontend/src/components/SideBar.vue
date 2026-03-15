<template>
  <aside class="sidebar" :class="{ collapsed }">
    <nav class="sidebar-nav">

      <!-- 主导航 -->
      <div class="nav-section">
        <SidebarItem icon="HomeFilled"  label="首页"     to="/"          :collapsed="collapsed" active />
        <SidebarItem icon="VideoPlay"   label="探索"     to="/explore"   :collapsed="collapsed" />
        <SidebarItem icon="TrendCharts" label="热门"     to="/trending"  :collapsed="collapsed" />
        <SidebarItem icon="Star"        label="订阅"     to="/subscribe" :collapsed="collapsed" />
      </div>

      <div class="nav-divider" v-if="!collapsed" />

      <!-- 登录后显示 -->
      <template v-if="userStore.isLoggedIn && !collapsed">
        <div class="nav-section-label">我的</div>
        <div class="nav-section">
          <SidebarItem icon="Clock"      label="历史记录" to="/history"  :collapsed="collapsed" />
          <SidebarItem icon="Collection" label="收藏夹"   to="/favorites":collapsed="collapsed" />
          <SidebarItem icon="Upload"     label="上传视频" to="/upload"   :collapsed="collapsed" />
        </div>
        <div class="nav-divider" />
      </template>

      <!-- 分类标签 -->
      <template v-if="!collapsed">
        <div class="nav-section-label">分类</div>
        <div class="nav-section">
          <SidebarItem
            v-for="cat in categories"
            :key="cat.label"
            :icon="cat.icon"
            :label="cat.label"
            to="/"
            :collapsed="collapsed"
          />
        </div>
      </template>

    </nav>

    <!-- 底部版权 -->
    <div class="sidebar-footer" v-if="!collapsed">
      <p>© 2025 VideoShare</p>
    </div>
  </aside>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import SidebarItem from './SidebarItem.vue'

defineProps({
  collapsed: { type: Boolean, default: false }
})

const userStore = useUserStore()

const categories = [
  { icon: 'Headset',      label: '音乐' },
  { icon: 'Trophy',       label: '体育' },
  { icon: 'Monitor',      label: '游戏' },
  { icon: 'Film',         label: '影视' },
  { icon: 'Opportunity',  label: '科技' },
  { icon: 'Food',         label: '美食' },
]
</script>

<style scoped>
.sidebar {
  width: 220px;
  flex-shrink: 0;
  height: calc(100vh - 56px);
  position: sticky;
  top: 56px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  transition: width 0.25s cubic-bezier(0.4,0,0.2,1);
  border-right: 1px solid var(--border);
}
.sidebar.collapsed {
  width: 68px;
}
.sidebar::-webkit-scrollbar { width: 0; }

.nav-section { display: flex; flex-direction: column; gap: 2px; }
.nav-section-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-muted);
  padding: 4px 12px;
  margin-top: 4px;
}
.nav-divider {
  height: 1px;
  background: var(--border);
  margin: 8px 4px;
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px 12px 8px;
  font-size: 11px;
  color: var(--text-muted);
}
</style>
