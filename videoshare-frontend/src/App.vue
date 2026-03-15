<template>
  <!-- RouterView 是当前路由对应的组件的渲染占位符 -->
  <!-- 访问 /       → 渲染 Home.vue  -->
  <!-- 访问 /login  → 渲染 Login.vue -->
  <RouterView />
</template>

<script setup>
// App.vue 只做一件事：作为路由出口
// 所有真正的页面布局都在各自的 View 里
</script>
