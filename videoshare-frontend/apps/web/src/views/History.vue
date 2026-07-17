<template>
  <DefaultLayout>
    <div class="page-header">
      <div>
        <h1 class="page-title">观看历史</h1>
        <p class="page-sub">{{ totalCount }} 条记录</p>
      </div>
      <button v-if="list.length" class="clear-btn" @click="handleClear">
        清空历史
      </button>
    </div>

    <div v-if="list.length" class="history-list">
      <VideoListItem v-for="v in list" :key="v.videoId" :video="v" />
    </div>

    <div v-else-if="!loading" class="empty-state">
      <p>还没有观看记录</p>
      <RouterLink to="/">去发现视频</RouterLink>
    </div>

    <div v-if="hasMore" class="load-more">
      <button class="load-more-btn" :disabled="loading" @click="loadMore">
        {{ loading ? '加载中...' : '加载更多' }}
      </button>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import VideoListItem from '@/components/VideoListItem.vue'
import { videoApi } from '@/api'
import { PAGE_DEFAULTS } from '@videoshare/constants'

const list       = ref([])
const totalCount = ref(0)
const currentPage = ref(1)
const hasMore    = ref(false)
const loading    = ref(false)

onMounted(() => fetchHistory(true))

async function fetchHistory(reset = false) {
  loading.value = true
  try {
    if (reset) currentPage.value = 1
    const params = { pageNum: currentPage.value, pageSize: PAGE_DEFAULTS.PAGE_SIZE }
    const result = await videoApi.getWatchHistory(params)
    if (reset) list.value = result.list || []
    else list.value.push(...(result.list || []))
    totalCount.value = result.total || 0
    hasMore.value = list.value.length < totalCount.value
  } finally { loading.value = false }
}

async function loadMore() {
  currentPage.value++
  await fetchHistory(false)
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空所有观看记录吗？', '确认', {
      confirmButtonText: '清空', cancelButtonText: '取消', type: 'warning'
    })
    await videoApi.clearWatchHistory()
    list.value = []
    totalCount.value = 0
    hasMore.value = false
    ElMessage.success('观看记录已清空')
  } catch {}
}
</script>

<style scoped>
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
.load-more { display: flex; justify-content: center; padding: 32px 0 16px; }
.load-more-btn {
  padding: 8px 24px; border-radius: 20px; border: none;
  background: var(--bg-hover); color: var(--text-1); font-size: 13px;
  cursor: pointer; transition: var(--transition); font-family: var(--font-body);
}
.load-more-btn:hover:not(:disabled) { background: #3a3a3a; }
.load-more-btn:disabled { opacity: 0.6; cursor: default; }
</style>
