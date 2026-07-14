<template>
  <div class="layout">
    <NavBar @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed" />
    <div class="layout-body">
      <SideBar :collapsed="sidebarCollapsed" />
      <main class="main-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import NavBar from '@/components/NavBar.vue'
import SideBar from '@/components/SideBar.vue'

const sidebarCollapsed = ref(false)
</script>

<style scoped>
.layout { min-height: 100vh; background: var(--bg-base); }
.layout-body { display: flex; margin-top: 56px; min-height: calc(100vh - 56px); }
.main-content {
  flex: 1; min-width: 0; overflow: hidden;
  padding: 0 24px 40px; max-width: 100%;
}
</style>
