<template>
  <DefaultLayout>
    <div class="page-header">
      <h1 class="page-title">订阅</h1>
      <p class="page-sub">你关注频道的最新视频</p>
    </div>

    <div v-if="videos.length > 0" class="video-grid">
      <VideoCard v-for="video in videos" :key="video.videoId" :video="video" />
    </div>

    <div v-else class="empty-state">
      <p>还没有订阅任何频道</p>
      <RouterLink to="/" class="explore-link">去发现频道</RouterLink>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import VideoCard from '@/components/VideoCard.vue'
import { videoApi } from '@/api'

const videos = ref([])

onMounted(async () => {
  try {
    const result = await videoApi.getSubscribedVideos?.()
    videos.value = result?.list || []
  } catch {
    videos.value = []
  }
})
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

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  gap: 12px; padding: 80px 0; color: var(--text-muted);
}
.explore-link {
  color: var(--color-accent); text-decoration: none; font-weight: 600; font-size: 14px;
}
</style>
