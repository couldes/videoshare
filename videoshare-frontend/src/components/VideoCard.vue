<template>
  <div class="video-card" @click="handleClick">
    <!-- 封面 -->
    <div class="thumb-wrap">
      <img :src="video.thumbnail" :alt="video.title" class="thumb" loading="lazy" />
      <!-- 时长标签 -->
      <span class="duration">{{ video.duration }}</span>
      <!-- 悬停时显示的播放按钮 -->
      <div class="play-overlay">
        <el-icon class="play-icon"><VideoPlay /></el-icon>
      </div>
    </div>

    <!-- 信息 -->
    <div class="video-info">
      <!-- 频道头像 -->
      <div class="channel-avatar">
        {{ video.channelName?.charAt(0).toUpperCase() }}
      </div>
      <!-- 文字信息 -->
      <div class="video-meta">
        <h3 class="video-title" :title="video.title">{{ video.title }}</h3>
        <p class="channel-name">{{ video.channelName }}</p>
        <p class="video-stats">
          <span>{{ formatViews(video.views) }} 次观看</span>
          <span class="dot">·</span>
          <span>{{ video.uploadTime }}</span>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const props = defineProps({
  video: {
    type: Object,
    required: true
    // 期望的 video 对象结构：
    // { id, title, thumbnail, duration, channelName, views, uploadTime }
  }
})

function handleClick() {
  ElMessage.info(`视频播放功能开发中：${props.video.title}`)
}

/** 格式化播放量：10000 → 1万 */
function formatViews(views) {
  if (!views) return '0'
  if (views >= 10000) return `${(views / 10000).toFixed(1)}万`
  return views.toString()
}
</script>

<style scoped>
.video-card {
  cursor: pointer;
  transition: var(--transition);
}
.video-card:hover .thumb { transform: scale(1.03); }
.video-card:hover .play-overlay { opacity: 1; }

/* 封面 */
.thumb-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-card);
}
.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
  display: block;
}
.duration {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(0,0,0,0.85);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  letter-spacing: 0.03em;
}
.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}
.play-icon {
  font-size: 48px;
  color: rgba(255,255,255,0.9);
  filter: drop-shadow(0 2px 8px rgba(0,0,0,0.5));
}

/* 信息区 */
.video-info {
  display: flex;
  gap: 10px;
  padding: 10px 4px 0;
}
.channel-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, var(--color-primary));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
  color: #fff;
  flex-shrink: 0;
  margin-top: 2px;
}
.video-meta { flex: 1; overflow: hidden; }
.video-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 4px;
}
.channel-name {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}
.video-stats {
  font-size: 11px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
.dot { color: var(--text-muted); }
</style>
