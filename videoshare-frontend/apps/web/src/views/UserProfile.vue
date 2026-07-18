<template>
  <DefaultLayout>
    <div class="profile-body" v-loading="profileLoading">
      <div v-if="profile">
        <ChannelHeader
          :profile="profile"
          :isSelf="isSelf"
          :isFollowing="isFollowing"
          @follow="handleFollow"
          @edit="showEditDialog = true"
        />

        <!-- Tab -->
        <div class="profile-tabs">
          <button v-for="tab in tabs" :key="tab.key" class="tab-btn"
            :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
            {{ tab.label }}
          </button>
        </div>

        <!-- Videos Tab -->
        <div v-if="activeTab === 'videos'" class="tab-content">
          <div v-if="videosLoading" class="tab-loading">加载中...</div>
          <div v-else-if="userVideos.length" class="video-grid">
            <VideoCard v-for="v in userVideos" :key="v.videoId" :video="v" />
          </div>
          <div v-else class="tab-empty">
            <p>{{ isSelf ? '你还没有发布视频' : '该用户还没有发布视频' }}</p>
          </div>
        </div>

        <!-- Playlists Tab -->
        <div v-if="activeTab === 'playlists'" class="tab-content">
          <div v-if="userPlaylists.length" class="playlist-grid">
            <RouterLink v-for="p in userPlaylists" :key="p.playlistId"
              :to="`/playlist/${p.playlistId}`" class="playlist-card">
              <div class="pl-card-cover">
                <img v-if="p.coverUrl" :src="p.coverUrl" :alt="p.title" />
                <div v-else class="pl-card-cover-placeholder">
                  <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor" opacity="0.3"><path d="M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-1 9h-4v4h-2v-4H9V9h4V5h2v4h4v2z"/></svg>
                </div>
                <span class="pl-card-count">{{ p.videoCount ?? 0 }} 个视频</span>
              </div>
              <div class="pl-card-info">
                <div class="pl-card-title">{{ p.title }}</div>
                <div class="pl-card-desc" v-if="p.description">{{ p.description }}</div>
              </div>
            </RouterLink>
          </div>
          <div v-else class="tab-empty">
            <p>{{ isSelf ? '你还没有播放列表' : '该用户还没有播放列表' }}</p>
          </div>
        </div>

        <!-- Favorites Tab (self only) -->
        <div v-if="activeTab === 'favorites'" class="tab-content">
          <div v-if="!userStore.isLoggedIn || !isSelf" class="tab-empty">
            <p>收藏列表仅本人可见</p>
          </div>
          <div v-else-if="favorites.length" class="video-grid">
            <VideoCard v-for="v in favorites" :key="v.videoId" :video="v" />
          </div>
          <div v-else class="tab-empty">
            <p>还没有收藏任何视频</p>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="520px"
      :close-on-click-modal="false">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="头像">
          <div class="avatar-upload-wrap">
            <div class="avatar-preview" :style="{ backgroundImage: avatarPreview ? `url(${avatarPreview})` : 'var(--color-accent)' }">
              <img v-if="avatarPreview" :src="avatarPreview" class="avatar-preview-img" alt="" />
              <span v-else class="avatar-preview-letter">{{ profile?.nickName?.charAt(0).toUpperCase() }}</span>
            </div>
            <el-button size="small" @click="$refs.avatarInput.click()">上传头像</el-button>
            <input ref="avatarInput" type="file" accept="image/*" hidden @change="handleAvatarSelect" />
            <span class="upload-hint">建议 200×200，不超过 2MB</span>
          </div>
        </el-form-item>
        <el-form-item label="频道背景图">
          <div class="bg-upload-row">
            <el-button size="small" @click="$refs.bgInput.click()">上传背景图</el-button>
            <input ref="bgInput" type="file" accept="image/*" hidden @change="handleBgSelect" />
            <span class="upload-hint">建议 1280×360，不超过 5MB</span>
          </div>
          <div v-if="bgPreview" class="bg-preview-wrap">
            <img :src="bgPreview" class="bg-preview-img" alt="" />
            <el-button size="small" text type="danger" @click="bgPreview = ''">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="3"
            placeholder="介绍一下自己..." :maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </DefaultLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import ChannelHeader from '@/components/ChannelHeader.vue'
import VideoCard from '@/components/VideoCard.vue'
import { useUserStore } from '@/stores/user'
import { auth } from '@/utils/auth'
import { profileApi, videoApi, playlistApi } from '@/api'

const route     = useRoute()
const userStore = useUserStore()

const profile        = ref(null)
const profileLoading = ref(false)
const isFollowing    = ref(false)
const userVideos     = ref([])
const userPlaylists  = ref([])
const favorites      = ref([])
const videosLoading  = ref(false)
const showEditDialog = ref(false)
const saving         = ref(false)
const editForm       = reactive({ bio: '', avatarUrl: '', backgroundUrl: '' })
const activeTab      = ref('videos')
const avatarPreview  = ref('')
const bgPreview      = ref('')

const isSelf = computed(() => userStore.isLoggedIn && profile.value?.userId === userStore.userInfo?.userId)
const tabs = computed(() => {
  const base = [{ key: 'videos', label: '视频' }, { key: 'playlists', label: '播放列表' }]
  if (isSelf.value) base.push({ key: 'favorites', label: '收藏' })
  return base
})

onMounted(loadProfile)
watch(() => route.params.userId, loadProfile)
watch(activeTab, (tab) => {
  if (tab === 'favorites' && isSelf.value && !favorites.value.length) loadFavorites()
  if (tab === 'playlists' && !userPlaylists.value.length) loadPlaylists()
})

async function loadProfile() {
  const userId = route.params.userId
  if (!userId) return
  profileLoading.value = true
  try {
    profile.value = await profileApi.getProfile(userId)
    if (userStore.isLoggedIn && !isSelf.value) isFollowing.value = await profileApi.checkFollow(userId)
    editForm.bio = profile.value.bio || ''
    editForm.avatarUrl = profile.value.avatarUrl || ''
    editForm.backgroundUrl = profile.value.backgroundUrl || ''
    avatarPreview.value = profile.value.avatarUrl || ''
    bgPreview.value = profile.value.backgroundUrl || ''
    // ★ 查看自己的主页时同步 store，确保 NavBar 等组件头像实时更新
    if (isSelf && profile.value.avatarUrl && profile.value.avatarUrl !== userStore.userInfo?.avatarUrl) {
      userStore.userInfo.avatarUrl = profile.value.avatarUrl
      auth.setInfo(userStore.userInfo)
    }
    await loadUserVideos()
  } finally { profileLoading.value = false }
}

async function loadUserVideos() {
  videosLoading.value = true
  try {
    const result = await videoApi.getUserVideos(route.params.userId, { pageNum: 1, pageSize: 24 })
    userVideos.value = result.list || []
  } finally { videosLoading.value = false }
}

async function loadFavorites() {
  try {
    const result = await profileApi.getFavorites({ pageNum: 1, pageSize: 24 })
    favorites.value = result.list || []
  } catch {}
}

async function loadPlaylists() {
  try {
    const result = await playlistApi.getUserPlaylists(route.params.userId)
    userPlaylists.value = Array.isArray(result) ? result : (result.list || [])
  } catch {}
}

async function handleAvatarSelect(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) { ElMessage.error('头像不能超过 2MB'); return }
  try {
    const result = await profileApi.uploadImage(file, 'avatar')
    editForm.avatarUrl = result.url
    avatarPreview.value = result.url
  } catch { ElMessage.error('头像上传失败') }
}

async function handleBgSelect(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('背景图不能超过 5MB'); return }
  try {
    const result = await profileApi.uploadImage(file, 'background')
    editForm.backgroundUrl = result.url
    bgPreview.value = result.url
  } catch { ElMessage.error('背景图上传失败') }
}

async function handleFollow() {
  try {
    const active = await profileApi.toggleFollow(profile.value.userId)
    isFollowing.value = active
    profile.value.followerCount += active ? 1 : -1
    ElMessage.success(active ? '关注成功' : '已取消关注')
  } catch {}
}

async function saveProfile() {
  saving.value = true
  try {
    const data = { bio: editForm.bio }
    if (editForm.avatarUrl) data.avatarUrl = editForm.avatarUrl
    if (editForm.backgroundUrl) data.backgroundUrl = editForm.backgroundUrl
    await profileApi.updateProfile(data)
    profile.value.bio = editForm.bio
    profile.value.avatarUrl = editForm.avatarUrl || null
    profile.value.backgroundUrl = editForm.backgroundUrl || null
    // ★ 同步更新 store 和 localStorage，确保 NavBar 等组件读到新头像
    if (userStore.userInfo) {
      userStore.userInfo.avatarUrl = editForm.avatarUrl || null
      auth.setInfo(userStore.userInfo)
    }
    showEditDialog.value = false
    ElMessage.success('资料已更新')
  } finally { saving.value = false }
}
</script>

<style scoped>
.profile-body { padding-top: 16px; }
.profile-tabs { display: flex; gap: 4px; border-bottom: 1px solid var(--border); margin-bottom: 20px; }
.tab-btn {
  padding: 10px 20px; background: none; border: none;
  border-bottom: 2px solid transparent; color: var(--text-2);
  font-size: 14px; font-weight: 500; cursor: pointer;
  transition: var(--transition); font-family: var(--font-body);
  margin-bottom: -1px;
}
.tab-btn:hover { color: var(--text-1); }
.tab-btn.active { color: var(--text-1); border-bottom-color: var(--text-1); }

.video-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(210px, 1fr)); gap: 16px; }

.tab-empty { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 60px 0; color: var(--text-muted); font-size: 14px; }
.tab-loading { display: flex; justify-content: center; padding: 60px 0; color: var(--text-muted); }

.playlist-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
.playlist-card {
  display: block; text-decoration: none; border-radius: var(--radius-md);
  overflow: hidden; background: var(--bg-hover); transition: var(--transition);
}
.playlist-card:hover { background: #333; }
.pl-card-cover {
  position: relative; width: 100%; height: 110px;
  background: linear-gradient(135deg, #1e293b, #334155);
  overflow: hidden;
}
.pl-card-cover img { width: 100%; height: 100%; object-fit: cover; }
.pl-card-cover-placeholder {
  width: 100%; height: 100%; display: flex; align-items: center;
  justify-content: center; color: var(--text-muted);
}
.pl-card-count {
  position: absolute; bottom: 6px; right: 6px;
  padding: 2px 6px; border-radius: 4px; background: rgba(0,0,0,0.75);
  color: #fff; font-size: 11px; font-weight: 600;
}
.pl-card-info { padding: 10px 12px; }
.pl-card-title { font-size: 14px; font-weight: 600; color: var(--text-1); margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pl-card-desc { font-size: 12px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.avatar-upload-wrap { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.avatar-preview {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 24px; color: #fff;
  overflow: hidden; flex-shrink: 0;
  background-size: cover !important; background-position: center !important;
}
.avatar-preview-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-preview-letter { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
.upload-hint { font-size: 12px; color: var(--text-muted); }
.bg-upload-row { display: flex; align-items: center; gap: 12px; }
.bg-preview-wrap { margin-top: 8px; display: flex; align-items: center; gap: 12px; }
.bg-preview-img { max-width: 300px; max-height: 80px; border-radius: 4px; object-fit: cover; }
</style>
