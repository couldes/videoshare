<template>
  <DefaultLayout>
    <div class="page-header">
      <h1 class="page-title">热门</h1>
      <p class="page-sub">发现当前最受欢迎的视频</p>
    </div>

    <div v-if="videos.length > 0" class="video-grid">
      <VideoCard v-for="video in videos" :key="video.videoId" :video="video" />
    </div>

    <div v-else-if="!loading" class="empty-state">
      <p>暂无热门视频</p>
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
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import VideoCard from '@/components/VideoCard.vue'
import { videoApi } from '@/api'
import { PAGE_DEFAULTS } from '@videoshare/constants'

const loading     = ref(false)
const videos      = ref([])
const totalCount  = ref(0)
const currentPage = ref(1)
const hasMore     = ref(false)

onMounted(() => fetchVideos(true))

async function fetchVideos(reset = false) {
  loading.value = true
  try {
    if (reset) currentPage.value = 1
    const result = await videoApi.getTrendingList({
      pageNum: currentPage.value,
      pageSize: PAGE_DEFAULTS.PAGE_SIZE
    })
    if (reset) videos.value = result.list || []
    else videos.value.push(...(result.list || []))
    totalCount.value = result.total || 0
    hasMore.value = videos.value.length < totalCount.value
  } finally { loading.value = false }
}

async function loadMore() {
  currentPage.value++
  await fetchVideos(false)
}
</script>

<style scoped>
.page-header { padding: 16px 0 8px; }
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
.page-sub { font-size: 13px; color: var(--text-2); }

.video-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 16px 12px;
}

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80px 0; color: var(--text-muted); }
.load-more { display: flex; justify-content: center; padding: 32px 0 16px; }
.load-more-btn {
  padding: 8px 24px; border-radius: 20px; border: none;
  background: var(--bg-hover); color: var(--text-1); font-size: 13px;
  cursor: pointer; transition: var(--transition); font-family: var(--font-body);
}
.load-more-btn:hover:not(:disabled) { background: #3a3a3a; }
.load-more-btn:disabled { opacity: 0.6; cursor: default; }
</style>
