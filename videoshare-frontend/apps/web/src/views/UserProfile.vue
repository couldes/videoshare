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

    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="480px"
      :close-on-click-modal="false">
      <el-form :model="editForm" label-position="top">
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
import { profileApi, videoApi } from '@/api'

const route     = useRoute()
const userStore = useUserStore()

const profile        = ref(null)
const profileLoading = ref(false)
const isFollowing    = ref(false)
const userVideos     = ref([])
const favorites      = ref([])
const videosLoading  = ref(false)
const showEditDialog = ref(false)
const saving         = ref(false)
const editForm       = reactive({ bio: '' })
const activeTab      = ref('videos')

const isSelf = computed(() => userStore.isLoggedIn && profile.value?.userId === userStore.userInfo?.userId)
const tabs = computed(() => {
  const base = [{ key: 'videos', label: '视频' }]
  if (isSelf.value) base.push({ key: 'favorites', label: '收藏' })
  return base
})

onMounted(loadProfile)
watch(() => route.params.userId, loadProfile)
watch(activeTab, (tab) => { if (tab === 'favorites' && isSelf.value && !favorites.value.length) loadFavorites() })

async function loadProfile() {
  const userId = route.params.userId
  if (!userId) return
  profileLoading.value = true
  try {
    profile.value = await profileApi.getProfile(userId)
    if (userStore.isLoggedIn && !isSelf.value) isFollowing.value = await profileApi.checkFollow(userId)
    editForm.bio = profile.value.bio || ''
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
    await profileApi.updateProfile({ bio: editForm.bio })
    profile.value.bio = editForm.bio
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
</style>
