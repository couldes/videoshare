<template>
  <div class="history-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="history-body">
      <div class="page-header">
        <div>
          <h1 class="page-title">观看历史</h1>
          <p class="page-sub mono">{{ list.length }} 条记录</p>
        </div>
        <button v-if="list.length" class="clear-btn" @click="handleClear">
          <el-icon><Delete /></el-icon>
          清空历史
        </button>
      </div>

      <div v-if="list.length" class="video-grid">
        <RouterLink
          v-for="v in list"
          :key="v.videoId"
          :to="`/video/${v.videoId}`"
          class="video-thumb-card">
          <div class="vtc-thumb">
            <img :src="v.coverUrl" :alt="v.title" loading="lazy"
              @error="$event.target.parentElement.innerHTML='<span style=color:var(--text-muted)>&#9654;</span>'" />
            <span class="vtc-duration">{{ v.duration }}</span>
          </div>
          <div class="vtc-info">
            <p class="vtc-title">{{ v.title }}</p>
            <p class="vtc-author">{{ v.authorName }}</p>
            <p class="vtc-stats">
              {{ formatViews(v.viewCount) }} 次观看
              · {{ formatRelative(v.timestamp) }}
            </p>
          </div>
        </RouterLink>
      </div>

      <div v-else class="history-empty">
        <el-icon class="empty-icon"><VideoCamera /></el-icon>
        <p>还没有观看记录</p>
        <RouterLink to="/">
          <el-button type="primary" size="small">去发现视频</el-button>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { getHistory, clearHistory } from '@videoshare/utils/history'
import { formatViews, formatRelative } from '@videoshare/utils/format'

const list = ref([])

onMounted(() => { list.value = getHistory() })

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空所有观看记录吗？', '确认', {
      confirmButtonText: '清空', cancelButtonText: '取消', type: 'warning'
    })
    clearHistory()
    list.value = []
    ElMessage.success('观看记录已清空')
  } catch {}
}
</script>

<style scoped>
.history-page { min-height: 100vh; background: var(--bg-base); }
.history-body { max-width: 1400px; margin: 0 auto; padding: 80px 20px 40px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24px; }
.page-title  { font-size: 22px; font-weight: 700; letter-spacing: -.02em; margin-bottom: 4px; }
.page-sub    { font-size: 11px; color: var(--text-muted); letter-spacing: .08em; }
.clear-btn { display: flex; align-items: center; gap: 6px; padding: 7px 14px; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-md); color: var(--danger); font-size: 13px; font-family: var(--font-body); cursor: pointer; transition: var(--transition); }
.clear-btn:hover { background: rgba(239,68,68,.1); border-color: rgba(239,68,68,.3); }

.video-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.video-thumb-card { text-decoration: none; border-radius: var(--radius-md); overflow: hidden; transition: var(--transition); }
.video-thumb-card:hover { transform: translateY(-2px); }
.vtc-thumb { position: relative; width: 100%; aspect-ratio: 16/9; overflow: hidden; background: var(--bg-card); border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; }
.vtc-thumb img { width: 100%; height: 100%; object-fit: cover; }
.vtc-duration { position: absolute; bottom: 4px; right: 4px; background: rgba(0,0,0,.8); color: #fff; font-size: 10px; padding: 1px 5px; border-radius: 3px; }
.vtc-info { padding: 8px 4px; }
.vtc-title { font-size: 13px; font-weight: 600; color: var(--text-1); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 4px; }
.vtc-author { font-size: 12px; color: var(--text-2); margin-bottom: 2px; }
.vtc-stats { font-size: 11px; color: var(--text-muted); }

.history-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; padding: 80px 0; color: var(--text-muted); }
.empty-icon { font-size: 56px; }
</style>
