<template>
  <div class="history-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="history-body">
      <div class="page-header">
        <div>
          <h1 class="page-title">观看历史</h1>
          <p class="page-sub">{{ list.length }} 条记录</p>
        </div>
        <button v-if="list.length" class="clear-btn" @click="handleClear">
          清空历史
        </button>
      </div>

      <div v-if="list.length" class="history-list">
        <VideoListItem v-for="v in list" :key="v.videoId" :video="v" />
      </div>

      <div v-else class="empty-state">
        <p>还没有观看记录</p>
        <RouterLink to="/">去发现视频</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import VideoListItem from '@/components/VideoListItem.vue'
import { getHistory, clearHistory } from '@videoshare/utils/history'

const list = ref([])
onMounted(() => { list.value = getHistory() })

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空所有观看记录吗？', '确认', {
      confirmButtonText: '清空', cancelButtonText: '取消', type: 'warning'
    })
    clearHistory(); list.value = []; ElMessage.success('观看记录已清空')
  } catch {}
}
</script>

<style scoped>
.history-page { min-height: 100vh; background: var(--bg-base); }
.history-body { max-width: 1000px; margin: 0 auto; padding: 72px 20px 40px; }
.page-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  margin-bottom: 24px; padding-top: 8px;
}
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
.page-sub { font-size: 13px; color: var(--text-muted); }
.clear-btn {
  padding: 6px 16px; border-radius: 20px; border: 1px solid var(--border);
  background: none; color: var(--text-2); font-size: 13px;
  font-family: var(--font-body); cursor: pointer; transition: var(--transition);
}
.clear-btn:hover { background: var(--bg-hover); color: var(--text-1); }
.history-list { display: flex; flex-direction: column; gap: 8px; }
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 80px 0; color: var(--text-muted); }
.empty-state a { color: var(--color-accent); text-decoration: none; font-weight: 600; }
</style>
