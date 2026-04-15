<template>
  <div class="video-card" @click="handleClick">
    <div class="thumb-wrap">
      <img :src="video.thumbnail" :alt="video.title" class="thumb" loading="lazy" />
      <span class="duration">{{ video.duration || formatDuration(video.durationSeconds) }}</span>
      <div class="play-overlay"><el-icon class="play-icon"><VideoPlay /></el-icon></div>
    </div>
    <div class="video-info">
      <div class="channel-avatar">{{ video.channelName?.charAt(0).toUpperCase() }}</div>
      <div class="video-meta">
        <h3 class="video-title" :title="video.title">{{ video.title }}</h3>
        <p class="channel-name">{{ video.channelName }}</p>
        <p class="video-stats">
          <!-- ★ 共享格式化工具 -->
          <span>{{ formatViews(video.views) }} 次观看</span>
          <span class="dot">·</span>
          <span>{{ video.uploadTime || formatRelative(video.createTime) }}</span>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { formatViews, formatRelative, formatDuration } from '@videoshare/utils/format'

const props = defineProps({ video: { type: Object, required: true } })
function handleClick() { ElMessage.info(`播放功能开发中：${props.video.title}`) }
</script>

<style scoped>
.video-card { cursor: pointer; transition: var(--transition); }
.video-card:hover .thumb { transform: scale(1.03); }
.video-card:hover .play-overlay { opacity: 1; }
.thumb-wrap { position: relative; width: 100%; aspect-ratio: 16/9; border-radius: var(--radius-md); overflow: hidden; background: var(--bg-card); }
.thumb { width: 100%; height: 100%; object-fit: cover; transition: transform .3s ease; display: block; }
.duration { position: absolute; bottom: 6px; right: 6px; background: rgba(0,0,0,.85); color: #fff; font-size: 11px; font-weight: 600; padding: 2px 6px; border-radius: 4px; }
.play-overlay { position: absolute; inset: 0; background: rgba(0,0,0,.35); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity .2s; }
.play-icon { font-size: 48px; color: rgba(255,255,255,.9); }
.video-info { display: flex; gap: 10px; padding: 10px 4px 0; }
.channel-avatar { width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, #7c3aed, var(--color-accent)); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; color: #fff; flex-shrink: 0; margin-top: 2px; }
.video-meta { flex: 1; overflow: hidden; }
.video-title { font-size: 13px; font-weight: 600; color: var(--text-1); line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 4px; }
.channel-name { font-size: 12px; color: var(--text-2); margin-bottom: 2px; }
.video-stats { font-size: 11px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; }
.dot { color: var(--text-muted); }
</style>
