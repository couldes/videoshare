<template>
  <DefaultLayout>
    <div class="page-header">
      <div>
        <h1 class="page-title">我的收藏</h1>
        <p class="page-sub">共 {{ total }} 个视频</p>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <span>加载中...</span>
    </div>

    <div v-else-if="videos.length" class="fav-list">
      <div v-for="v in videos" :key="v.videoId" class="fav-item">
        <VideoListItem :video="v" />
        <button class="unfav-btn" @click="handleUnfavorite(v)" title="取消收藏">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
        </button>
      </div>
    </div>

    <div v-else class="empty-state">
      <h3>还没有收藏任何视频</h3>
      <p>在视频播放页点击"收藏"即可保存到这里</p>
      <RouterLink to="/">去发现视频</RouterLink>
    </div>

    <div v-if="total > pageSize" class="pagination">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize"
        :total="total" background layout="prev, pager, next"
        @current-change="loadFavorites" />
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import VideoListItem from '@/components/VideoListItem.vue'
import { profileApi, videoApi } from '@/api'

const videos   = ref([])
const loading  = ref(false)
const pageNum  = ref(1)
const pageSize = ref(24)
const total    = ref(0)

onMounted(loadFavorites)

async function loadFavorites() {
  loading.value = true
  try {
    const result = await profileApi.getFavorites({ pageNum: pageNum.value, pageSize: pageSize.value })
    videos.value = result.list || []
    total.value  = result.total || 0
  } finally { loading.value = false }
}

async function handleUnfavorite(video) {
  try {
    await ElMessageBox.confirm(`确定要取消收藏「${video.title}」吗？`, '提示', {
      confirmButtonText: '取消收藏', cancelButtonText: '取消', type: 'warning'
    })
  } catch { return }
  try {
    await videoApi.doAction({ videoId: video.videoId, actionType: 2 })
    videos.value = videos.value.filter(v => v.videoId !== video.videoId)
    total.value--
    ElMessage.success('已取消收藏')
  } catch {}
}
</script>

<style scoped>
.page-header { margin-bottom: 24px; padding-top: 16px; }
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
.page-sub { font-size: 13px; color: var(--text-2); }

.loading-state { display: flex; justify-content: center; padding: 80px 0; color: var(--text-muted); }

.fav-list { display: flex; flex-direction: column; gap: 4px; }
.fav-item {
  display: flex; align-items: flex-start; gap: 8px;
  padding: 6px; border-radius: var(--radius-md); transition: var(--transition);
}
.fav-item:hover { background: var(--bg-hover); }
.fav-item :deep(.video-list-item) { flex: 1; padding: 0; }
.fav-item :deep(.video-list-item:hover) { background: none; }

.unfav-btn {
  flex-shrink: 0; width: 36px; height: 36px; border: none; background: none;
  color: var(--color-accent); cursor: pointer; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-top: 38px; transition: var(--transition);
}
.unfav-btn:hover { background: var(--bg-hover); }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 80px 0; text-align: center; color: var(--text-muted); }
.empty-state h3 { font-size: 16px; font-weight: 600; color: var(--text-1); }
.empty-state p { font-size: 13px; }
.empty-state a { color: var(--color-accent); text-decoration: none; font-weight: 600; }
.pagination { display: flex; justify-content: center; padding: 24px 0; }
</style>
