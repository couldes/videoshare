<template>
  <DefaultLayout>
    <div class="playlist-body" v-loading="loading">
      <template v-if="playlist">
        <div class="playlist-header">
          <div class="header-cover" v-if="playlist.coverUrl">
            <img :src="playlist.coverUrl" :alt="playlist.title" />
          </div>
          <div class="header-info">
            <h1 class="playlist-title">{{ playlist.title }}</h1>
            <p class="playlist-desc" v-if="playlist.description">{{ playlist.description }}</p>
            <div class="playlist-meta">
              <span>{{ playlist.videoCount || videos.length }} 个视频</span>
            </div>
          </div>
        </div>

        <div class="divider" />

        <div v-if="videos.length" class="video-grid">
          <VideoCard v-for="v in videos" :key="v.videoId" :video="v" />
        </div>
        <div v-else-if="!loading" class="empty-state">
          <p>这个播放列表还没有视频</p>
        </div>
      </template>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import VideoCard from '@/components/VideoCard.vue'
import { playlistApi } from '@/api'

const route = useRoute()

const playlist = ref(null)
const videos   = ref([])
const loading  = ref(false)

onMounted(loadPlaylist)
watch(() => route.params.playlistId, loadPlaylist)

async function loadPlaylist() {
  const id = route.params.playlistId
  if (!id) return
  loading.value = true
  try {
    const data = await playlistApi.getPlaylistDetail(id)
    playlist.value = data
    videos.value = data.videoList || data.list || data.videos || []
  } catch {
    ElMessage.error('播放列表加载失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.playlist-body { padding-top: 16px; }
.playlist-header {
  display: flex; gap: 20px; align-items: flex-start;
  padding: 16px 0;
}
.header-cover {
  width: 200px; height: 120px; border-radius: var(--radius-md);
  overflow: hidden; flex-shrink: 0; background: var(--bg-hover);
}
.header-cover img { width: 100%; height: 100%; object-fit: cover; }
.header-info { flex: 1; }
.playlist-title { font-size: 22px; font-weight: 700; margin-bottom: 8px; }
.playlist-desc { font-size: 14px; color: var(--text-2); line-height: 1.6; margin-bottom: 8px; }
.playlist-meta { font-size: 13px; color: var(--text-muted); }

.divider { height: 1px; background: var(--border); margin: 8px 0 20px; }

.video-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(210px, 1fr)); gap: 16px; }

.empty-state {
  display: flex; justify-content: center; padding: 60px 0;
  color: var(--text-muted); font-size: 14px;
}
</style>
