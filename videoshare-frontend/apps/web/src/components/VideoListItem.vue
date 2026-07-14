<template>
  <RouterLink :to="`/video/${video.videoId || video.id}`" class="video-list-item">
    <div class="vli-thumb">
      <img :src="video.thumbnail || video.coverUrl" :alt="video.title" loading="lazy" />
      <span class="vli-duration">{{ video.duration || formatDuration(video.durationSeconds) }}</span>
    </div>
    <div class="vli-info">
      <h3 class="vli-title">{{ video.title }}</h3>
      <p class="vli-channel">{{ video.channelName || video.userInfo?.nickName }}</p>
      <p class="vli-stats">
        {{ formatViews(video.views || video.viewCount) }} 次观看
        <span class="dot">·</span>
        {{ formatRelative(video.createTime) }}
      </p>
    </div>
  </RouterLink>
</template>

<script setup>
import { formatViews, formatRelative, formatDuration } from '@videoshare/utils/format'
defineProps({ video: { type: Object, required: true } })
</script>

<style scoped>
.video-list-item {
  display: flex; gap: 12px; text-decoration: none;
  padding: 6px; border-radius: var(--radius-md);
  transition: var(--transition);
}
.video-list-item:hover { background: var(--bg-hover); }
.vli-thumb {
  position: relative; width: 200px; height: 112px;
  flex-shrink: 0; border-radius: var(--radius-md);
  overflow: hidden; background: var(--bg-card);
}
.vli-thumb img { width: 100%; height: 100%; object-fit: cover; }
.vli-duration {
  position: absolute; bottom: 4px; right: 4px;
  background: rgba(0,0,0,0.8); color: #fff;
  font-size: 12px; font-weight: 500; padding: 1px 6px; border-radius: 4px;
}
.vli-info { flex: 1; min-width: 0; }
.vli-title {
  font-size: 14px; font-weight: 600; color: var(--text-1);
  line-height: 1.4; margin-bottom: 4px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.vli-channel { font-size: 12px; color: var(--text-2); margin-bottom: 2px; }
.vli-stats { font-size: 12px; color: var(--text-muted); }
.dot { color: var(--text-muted); }
</style>
