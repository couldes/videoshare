<template>
  <RouterLink :to="to" class="sidebar-item" :class="{ active, collapsed }">
    <el-icon class="item-icon"><component :is="icon" /></el-icon>
    <span class="item-label" v-if="!collapsed">{{ label }}</span>
    <!-- 折叠时鼠标悬停显示 tooltip -->
    <div class="item-tooltip" v-if="collapsed">{{ label }}</div>
  </RouterLink>
</template>

<script setup>
defineProps({
  icon:      { type: String,  required: true },
  label:     { type: String,  required: true },
  to:        { type: String,  required: true },
  collapsed: { type: Boolean, default: false },
  active:    { type: Boolean, default: false }
})
</script>

<style scoped>
.sidebar-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  transition: var(--transition);
  position: relative;
  white-space: nowrap;
}
.sidebar-item:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}
.sidebar-item.active {
  background: var(--color-primary-dim);
  color: var(--color-primary);
}
.sidebar-item.active .item-icon {
  color: var(--color-primary);
}
.item-icon {
  font-size: 18px;
  flex-shrink: 0;
}

/* 折叠态：居中显示图标 */
.sidebar-item.collapsed {
  justify-content: center;
  padding: 10px;
}

/* Tooltip（折叠态悬停显示文字） */
.item-tooltip {
  position: absolute;
  left: calc(100% + 10px);
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-primary);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s;
  z-index: 200;
  box-shadow: var(--shadow-card);
}
.sidebar-item.collapsed:hover .item-tooltip {
  opacity: 1;
}
</style>
