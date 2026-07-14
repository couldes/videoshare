<template>
  <RouterLink :to="to" class="sidebar-item" :class="{ collapsed }">
    <div class="item-icon-wrap">
      <!-- Home -->
      <svg v-if="icon === 'home'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
      </svg>
      <!-- Trending -->
      <svg v-else-if="icon === 'trending'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/>
      </svg>
      <!-- Subscriptions -->
      <svg v-else-if="icon === 'subscriptions'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M20 8H4V6h16v2zm-2-6H6v2h12V2zm4 10v8c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2v-8c0-1.1.9-2 2-2h16c1.1 0 2 .9 2 2zm-6 4l-6-3.27v6.53L16 16z"/>
      </svg>
      <!-- History -->
      <svg v-else-if="icon === 'history'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/>
      </svg>
      <!-- Favorites -->
      <svg v-else-if="icon === 'favorites'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
      </svg>
      <!-- Upload -->
      <svg v-else-if="icon === 'upload'" viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"/>
      </svg>
      <!-- Explore -->
      <svg v-else viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M12 10.9c-.61 0-1.1.49-1.1 1.1s.49 1.1 1.1 1.1c.61 0 1.1-.49 1.1-1.1s-.49-1.1-1.1-1.1zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm2.19 12.19L6 18l3.81-8.19L18 6l-3.81 8.19z"/>
      </svg>
    </div>
    <span v-if="!collapsed" class="item-label">{{ label }}</span>
  </RouterLink>
</template>

<script setup>
defineProps({
  icon:      { type: String,  required: true },
  label:     { type: String,  required: true },
  to:        { type: String,  required: true },
  collapsed: { type: Boolean, default: false }
})
</script>

<style scoped>
.sidebar-item {
  display: flex; align-items: center; gap: 20px;
  padding: 8px 12px; border-radius: 10px; text-decoration: none;
  color: var(--text-2); font-size: 13px; font-weight: 500;
  transition: var(--transition); white-space: nowrap;
}
.sidebar-item:hover { background: var(--bg-hover); color: var(--text-1); }
.item-icon-wrap { width: 22px; height: 22px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.sidebar-item.collapsed {
  justify-content: center; padding: 10px 0;
}
.item-label { line-height: 1; }
</style>
