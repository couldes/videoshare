<template>
  <RouterLink :to="to" class="sidebar-item" :class="{ active, collapsed }">
    <el-icon class="item-icon"><component :is="icon" /></el-icon>
    <span v-if="!collapsed" class="item-label">{{ label }}</span>
    <div v-if="collapsed" class="item-tooltip">{{ label }}</div>
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
  display: flex; align-items: center; gap: 14px; padding: 8px 12px;
  border-radius: var(--radius-md); text-decoration: none;
  color: var(--text-2); font-size: 13px; font-weight: 500;
  transition: var(--transition); position: relative; white-space: nowrap;
}
.sidebar-item:hover { background: var(--bg-hover); color: var(--text-1); }
.sidebar-item.active { background: var(--color-accent-dim); color: var(--color-accent); }
.item-icon { font-size: 18px; flex-shrink: 0; }
.sidebar-item.collapsed { justify-content: center; padding: 10px; }
.item-tooltip {
  position: absolute; left: calc(100% + 10px);
  background: var(--bg-card); border: 1px solid var(--border-strong);
  color: var(--text-1); padding: 4px 10px; border-radius: var(--radius-sm);
  font-size: 12px; white-space: nowrap; opacity: 0; pointer-events: none;
  transition: opacity .15s; z-index: 200; box-shadow: var(--shadow-card);
}
.sidebar-item.collapsed:hover .item-tooltip { opacity: 1; }
</style>
