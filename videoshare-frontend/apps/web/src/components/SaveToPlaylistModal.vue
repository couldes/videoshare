<template>
  <el-dialog v-model="open" title="保存到播放列表" width="440px"
    :close-on-click-modal="true" @close="$emit('close')">
    <div class="modal-body">
      <div v-if="playlists.length" class="playlist-options">
        <div v-for="p in playlists" :key="p.playlistId" class="playlist-option"
          @click="togglePlaylist(p)">
          <el-checkbox :model-value="p.checked" @click.stop />
          <span class="pl-title">{{ p.title }}</span>
          <span class="pl-count">{{ p.videoCount ?? 0 }} 个视频</span>
        </div>
      </div>
      <div v-else class="no-playlists">还没有播放列表</div>
    </div>

    <div class="quick-create">
      <el-input v-model="newTitle" placeholder="新建播放列表..." size="small"
        @keydown.enter="createNew" :disabled="creating" />
      <el-button size="small" :loading="creating" @click="createNew">创建</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { playlistApi } from '@/api'

const props = defineProps({
  videoId: { type: [Number, String], required: true },
  visible: { type: Boolean, default: false }
})

defineEmits(['close'])

const userStore = useUserStore()

const open      = ref(false)
const playlists = ref([])
const newTitle  = ref('')
const creating  = ref(false)

watch(() => props.visible, async (val) => {
  open.value = val
  if (val && userStore.isLoggedIn) {
    await loadPlaylists()
  }
})

async function loadPlaylists() {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) return
    const list = await playlistApi.getUserPlaylists(userId)
    playlists.value = (Array.isArray(list) ? list : list.list || []).map(p => ({
      ...p,
      checked: false
    }))
  } catch {}
}

async function togglePlaylist(p) {
  try {
    if (p.checked) {
      await playlistApi.removeVideoFromPlaylist(p.playlistId, props.videoId)
      p.checked = false
      p.videoCount = Math.max(0, (p.videoCount || 1) - 1)
      ElMessage.success('已从播放列表移除')
    } else {
      await playlistApi.addVideoToPlaylist(p.playlistId, { videoId: props.videoId })
      p.checked = true
      p.videoCount = (p.videoCount || 0) + 1
      ElMessage.success('已添加到播放列表')
    }
  } catch {}
}

async function createNew() {
  const title = newTitle.value.trim()
  if (!title) return
  creating.value = true
  try {
    await playlistApi.createPlaylist({ title })
    newTitle.value = ''
    ElMessage.success(`播放列表「${title}」已创建`)
    await loadPlaylists()
  } catch {}
  finally { creating.value = false }
}
</script>

<style scoped>
.modal-body { max-height: 300px; overflow-y: auto; padding: 0 4px; }

.playlist-option {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 8px; cursor: pointer; border-radius: var(--radius-sm);
  transition: var(--transition);
}
.playlist-option:hover { background: var(--bg-hover); }
.pl-title { flex: 1; font-size: 14px; color: var(--text-1); }
.pl-count { font-size: 12px; color: var(--text-muted); flex-shrink: 0; }

.no-playlists { text-align: center; padding: 24px 0; color: var(--text-muted); font-size: 13px; }

.quick-create { display: flex; gap: 8px; margin-top: 16px; padding-top: 12px; border-top: 1px solid var(--border); }
.quick-create :deep(.el-input) { flex: 1; }
</style>
