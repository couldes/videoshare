<template>
  <div class="profile-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="profile-body">
      <!-- 个人信息头部 -->
      <div class="profile-header" v-loading="profileLoading">
        <div v-if="profile" class="profile-content">
          <!-- 大头像 -->
          <div class="big-avatar">
            {{ profile.nickName?.charAt(0).toUpperCase() }}
          </div>

          <!-- 基本信息 -->
          <div class="profile-info">
            <h1 class="profile-name">{{ profile.nickName }}</h1>
            <p class="profile-uid mono">UID: {{ profile.userId }}</p>
            <p v-if="profile.bio" class="profile-bio">{{ profile.bio }}</p>

            <!-- 统计数字 -->
            <div class="profile-stats">
              <div class="stat-block">
                <span class="stat-num">{{ formatViews(profile.videoCount) }}</span>
                <span class="stat-label">视频</span>
              </div>
              <div class="stat-divider"/>
              <div class="stat-block">
                <span class="stat-num">{{ formatViews(profile.followerCount) }}</span>
                <span class="stat-label">粉丝</span>
              </div>
              <div class="stat-divider"/>
              <div class="stat-block">
                <span class="stat-num">{{ formatViews(profile.followingCount) }}</span>
                <span class="stat-label">关注</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="profile-actions">
              <!-- 自己的主页：显示编辑按钮 -->
              <template v-if="isSelf">
                <el-button @click="showEditDialog = true" :icon="Edit">编辑个人资料</el-button>
              </template>
              <!-- 他人主页：显示关注/私信 -->
              <template v-else-if="userStore.isLoggedIn">
                <el-button
                  :type="isFollowing ? 'default' : 'primary'"
                  @click="handleFollow"
                >
                  {{ isFollowing ? '已关注' : '+ 关注' }}
                </el-button>
              </template>
              <template v-else>
                <RouterLink to="/login">
                  <el-button type="primary">登录后关注</el-button>
                </RouterLink>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 内容区 -->
      <div class="profile-tabs-area">
        <div class="profile-tabs">
          <button v-for="tab in tabs" :key="tab.key" class="tab-btn"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key">
            {{ tab.label }}
          </button>
        </div>

        <!-- 视频列表 Tab -->
        <div v-if="activeTab === 'videos'" class="tab-content">
          <div v-if="videosLoading" class="tab-loading">
            <el-icon class="spin"><Loading /></el-icon>
          </div>
          <div v-else-if="userVideos.length" class="video-grid">
            <RouterLink
              v-for="v in userVideos"
              :key="v.videoId"
              :to="`/video/${v.videoId}`"
              class="video-thumb-card"
            >
              <div class="vtc-thumb">
                <img :src="v.coverUrl" :alt="v.title" loading="lazy" />
                <span class="vtc-duration">{{ v.duration }}</span>
              </div>
              <div class="vtc-info">
                <p class="vtc-title">{{ v.title }}</p>
                <p class="vtc-stats">
                  {{ formatViews(v.viewCount) }} 次观看
                  · {{ formatRelative(v.createTime) }}
                </p>
              </div>
            </RouterLink>
          </div>
          <div v-else class="tab-empty">
            <el-icon><VideoCamera /></el-icon>
            <p>{{ isSelf ? '你还没有发布视频' : '该用户还没有发布视频' }}</p>
            <RouterLink v-if="isSelf" to="/upload">
              <el-button type="primary" size="small">发布第一个视频</el-button>
            </RouterLink>
          </div>
        </div>

        <!-- 收藏 Tab（仅自己可见）-->
        <div v-if="activeTab === 'favorites'" class="tab-content">
          <div v-if="!userStore.isLoggedIn || !isSelf" class="tab-empty">
            <el-icon><Lock /></el-icon>
            <p>收藏列表仅本人可见</p>
          </div>
          <div v-else-if="favorites.length" class="video-grid">
            <RouterLink v-for="v in favorites" :key="v.videoId"
              :to="`/video/${v.videoId}`" class="video-thumb-card">
              <div class="vtc-thumb">
                <img :src="v.coverUrl" :alt="v.title" loading="lazy" />
              </div>
              <div class="vtc-info">
                <p class="vtc-title">{{ v.title }}</p>
                <p class="vtc-stats">{{ formatViews(v.viewCount) }} 次观看</p>
              </div>
            </RouterLink>
          </div>
          <div v-else class="tab-empty">
            <el-icon><Star /></el-icon>
            <p>还没有收藏任何视频</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="480px"
      :close-on-click-modal="false"
      custom-class="dark-dialog">
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { useUserStore } from '@/stores/user'
import { profileApi, videoApi } from '@/api'
import { formatViews, formatRelative } from '@videoshare/utils/format'

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

const isSelf = computed(
  () => userStore.isLoggedIn && profile.value?.userId === userStore.userInfo?.userId
)

const tabs = computed(() => {
  const base = [{ key: 'videos', label: '视频' }]
  if (isSelf.value) base.push({ key: 'favorites', label: '收藏' })
  return base
})

onMounted(loadProfile)

watch(() => route.params.userId, loadProfile)
watch(activeTab, (tab) => {
  if (tab === 'favorites' && isSelf.value && !favorites.value.length) loadFavorites()
})

async function loadProfile() {
  const userId = route.params.userId
  if (!userId) return
  profileLoading.value = true
  try {
    profile.value = await profileApi.getProfile(userId)
    if (userStore.isLoggedIn && !isSelf.value) {
      isFollowing.value = await profileApi.checkFollow(userId)
    }
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
.profile-page { min-height: 100vh; background: var(--bg-base); }
.profile-body { max-width: 1100px; margin: 0 auto; padding: 80px 20px 40px; }

/* 头部 */
.profile-content { display: flex; gap: 32px; align-items: flex-start; padding: 32px; background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-lg); }
.big-avatar { width: 96px; height: 96px; border-radius: 50%; background: linear-gradient(135deg, var(--color-accent), #7c3aed); display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 36px; color: #fff; flex-shrink: 0; }
.profile-info { flex: 1; }
.profile-name { font-size: 24px; font-weight: 700; letter-spacing: -.02em; margin-bottom: 4px; }
.profile-uid  { font-size: 12px; color: var(--text-muted); margin-bottom: 10px; }
.profile-bio  { font-size: 14px; color: var(--text-2); line-height: 1.6; margin-bottom: 16px; }
.profile-stats { display: flex; align-items: center; gap: 20px; margin-bottom: 20px; }
.stat-block { text-align: center; }
.stat-num   { display: block; font-size: 20px; font-weight: 700; color: var(--text-1); }
.stat-label { font-size: 12px; color: var(--text-2); }
.stat-divider { width: 1px; height: 28px; background: var(--border); }
.profile-actions { display: flex; gap: 10px; }

/* Tab */
.profile-tabs-area { margin-top: 24px; }
.profile-tabs { display: flex; gap: 4px; border-bottom: 1px solid var(--border); margin-bottom: 20px; }
.tab-btn { padding: 10px 20px; background: none; border: none; border-bottom: 2px solid transparent; color: var(--text-2); font-size: 14px; font-weight: 500; cursor: pointer; transition: var(--transition); font-family: var(--font-body); margin-bottom: -1px; }
.tab-btn:hover { color: var(--text-1); }
.tab-btn.active { color: var(--color-accent); border-bottom-color: var(--color-accent); }

/* 视频网格 */
.video-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.video-thumb-card { text-decoration: none; border-radius: var(--radius-md); overflow: hidden; transition: var(--transition); }
.video-thumb-card:hover { transform: translateY(-2px); }
.vtc-thumb { position: relative; width: 100%; aspect-ratio: 16/9; overflow: hidden; background: var(--bg-card); border-radius: var(--radius-sm); }
.vtc-thumb img { width: 100%; height: 100%; object-fit: cover; }
.vtc-duration { position: absolute; bottom: 4px; right: 4px; background: rgba(0,0,0,.8); color: #fff; font-size: 10px; padding: 1px 5px; border-radius: 3px; }
.vtc-info { padding: 8px 4px; }
.vtc-title { font-size: 13px; font-weight: 600; color: var(--text-1); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 4px; }
.vtc-stats { font-size: 11px; color: var(--text-muted); }

/* 空状态 */
.tab-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; padding: 60px 0; color: var(--text-muted); font-size: 14px; }
.tab-empty .el-icon { font-size: 40px; }
.tab-loading { display: flex; justify-content: center; padding: 60px 0; }
.spin { font-size: 24px; animation: rotate 1s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }

/* 编辑弹窗 */
:deep(.dark-dialog) { background: var(--bg-card) !important; border: 1px solid var(--border-strong) !important; }
:deep(.dark-dialog .el-dialog__title) { color: var(--text-1) !important; }
:deep(.dark-dialog .el-dialog__headerbtn .el-dialog__close) { color: var(--text-2) !important; }
</style>
