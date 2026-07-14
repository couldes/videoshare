<template>
  <RouterLink :to="`/video/${video.videoId || video.id}`" class="video-card">
    <div class="thumb-wrap">
      <img :src="video.thumbnail || video.coverUrl" :alt="video.title" class="thumb" loading="lazy" />
      <span class="duration">{{ video.duration || formatDuration(video.durationSeconds) }}</span>
    </div>
    <div class="video-info">
      <RouterLink :to="`/user/${video.userInfo?.userId || video.userId}`"
        class="channel-avatar" @click.stop>
        {{ (video.channelName || video.userInfo?.nickName)?.charAt(0).toUpperCase() }}
      </RouterLink>
      <div class="video-meta">
        <h3 class="video-title" :title="video.title">{{ video.title }}</h3>
        <p class="channel-name">{{ video.channelName || video.userInfo?.nickName }}</p>
        <p class="video-stats">
          <span>{{ formatViews(video.views || video.viewCount) }} 次观看</span>
          <span class="dot">·</span>
          <span>{{ video.uploadTime || formatRelative(video.createTime) }}</span>
        </p>
      </div>
    </div>
  </RouterLink>
</template>

<script setup>
import { formatViews, formatRelative, formatDuration } from '@videoshare/utils/format'
defineProps({ video: { type: Object, required: true } })
</script>

<style scoped>
.video-card { display: block; text-decoration: none; cursor: pointer; }
.thumb-wrap {
  position: relative; width: 100%; aspect-ratio: 16/9;
  border-radius: var(--radius-md); overflow: hidden; background: var(--bg-card);
}
.thumb { width: 100%; height: 100%; object-fit: cover; display: block; }
.duration {
  position: absolute; bottom: 4px; right: 4px;
  background: rgba(0,0,0,0.8); color: #fff;
  font-size: 12px; font-weight: 500; padding: 1px 6px; border-radius: 4px;
}
.video-info { display: flex; gap: 10px; padding: 10px 0 0; }
.channel-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, var(--color-accent));
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 13px; color: #fff;
  flex-shrink: 0; margin-top: 2px; text-decoration: none;
}
.video-meta { flex: 1; min-width: 0; }
.video-title {
  font-size: 14px; font-weight: 600; color: var(--text-1);
  line-height: 1.4; margin-bottom: 4px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.channel-name {
  font-size: 12px; color: var(--text-2); margin-bottom: 2px;
  text-decoration: none; transition: var(--transition);
}
.channel-name:hover { color: var(--text-1); }
.video-stats { font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; }
.dot { color: var(--text-muted); }
</style>
