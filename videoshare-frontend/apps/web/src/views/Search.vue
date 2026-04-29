<template>
  <div class="layout">
    <NavBar @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed" />
    <div class="layout-body">
      <SideBar :collapsed="sidebarCollapsed" />
      <main class="main-content">
        <div class="search-header">
          <div class="search-bar-large">
            <el-icon class="search-icon"><Search /></el-icon>
            <input v-model="keyword" class="search-input-large"
              :placeholder="activeTab === 'video' ? '搜索视频标题、标签...' : '搜索用户昵称、邮箱...'"
              @keydown.enter="doSearch" autofocus />
            <button v-if="keyword" class="search-clear" @click="keyword='';doSearch()">
              <el-icon><Close /></el-icon>
            </button>
            <button class="search-btn" @click="doSearch">
              <el-icon><Search /></el-icon>
            </button>
          </div>
        </div>

        <!-- 搜索分类 Tab -->
        <div v-if="searchedKeyword" class="search-tabs">
          <button class="search-tab" :class="{ active: activeTab === 'video' }"
            @click="switchTab('video')">
            <el-icon><VideoCamera /></el-icon>视频
            <span v-if="videoTotal > 0" class="tab-count">{{ videoTotal }}</span>
          </button>
          <button class="search-tab" :class="{ active: activeTab === 'user' }"
            @click="switchTab('user')">
            <el-icon><User /></el-icon>用户
            <span v-if="userResults.length > 0" class="tab-count">{{ userResults.length }}</span>
          </button>
        </div>

        <!-- 视频结果 -->
        <template v-if="activeTab === 'video'">
          <div v-if="searchedKeyword" class="results-header">
            <span class="results-label">"{{ searchedKeyword }}" - 视频</span>
            <span class="results-count mono">共 {{ videoTotal }} 条</span>
            <el-select v-model="orderBy" class="order-select" @change="searchVideos(true)">
              <el-option label="最新发布" value="create_time" />
              <el-option label="最多播放" value="view_count" />
              <el-option label="最多点赞" value="like_count" />
            </el-select>
          </div>

          <div v-if="videos.length > 0" class="video-grid">
            <VideoCard v-for="video in videos" :key="video.videoId" :video="video" />
          </div>

          <div v-else-if="!videoLoading && searchedKeyword" class="empty-state">
            <el-icon class="empty-icon"><Search /></el-icon>
            <p>没有找到相关视频</p>
            <p class="empty-hint">试试其他关键词</p>
          </div>

          <div v-if="hasMore" class="load-more">
            <button class="load-more-btn" :disabled="videoLoading" @click="loadMore">
              <el-icon v-if="videoLoading" class="spin"><Loading /></el-icon>
              <span>{{ videoLoading ? '搜索中...' : '加载更多' }}</span>
            </button>
          </div>
        </template>

        <!-- 用户结果 -->
        <template v-if="activeTab === 'user'">
          <div v-if="searchedKeyword" class="results-header">
            <span class="results-label">"{{ searchedKeyword }}" - 用户</span>
            <span class="results-count mono">共 {{ userResults.length }} 人</span>
          </div>

          <div v-if="userResults.length > 0" class="user-list">
            <RouterLink v-for="user in userResults" :key="user.userId"
              :to="`/user/${user.userId}`" class="user-card">
              <div class="user-avatar">{{ user.nickName?.charAt(0).toUpperCase() }}</div>
              <div class="user-info">
                <span class="user-name">{{ user.nickName }}</span>
                <span class="user-email mono">{{ user.email }}</span>
              </div>
              <el-icon class="user-arrow"><ArrowRight /></el-icon>
            </RouterLink>
          </div>

          <div v-else-if="!userLoading && searchedKeyword" class="empty-state">
            <el-icon class="empty-icon"><User /></el-icon>
            <p>没有找到相关用户</p>
          </div>
        </template>

        <!-- 空状态：尚未搜索 -->
        <div v-if="!searchedKeyword && !videoLoading && !userLoading" class="empty-state">
          <el-icon class="empty-icon"><Search /></el-icon>
          <p>输入关键词开始搜索</p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import SideBar from '@/components/SideBar.vue'
import VideoCard from '@/components/VideoCard.vue'
import { videoApi, profileApi } from '@/api'
import { PAGE_DEFAULTS } from '@videoshare/constants'

const route  = useRoute()
const router = useRouter()

const sidebarCollapsed = ref(false)
const keyword          = ref('')
const searchedKeyword  = ref('')
const activeTab        = ref('video')
const videoLoading     = ref(false)
const userLoading      = ref(false)
const videos           = ref([])
const videoTotal       = ref(0)
const currentPage      = ref(1)
const hasMore          = ref(false)
const orderBy          = ref('create_time')
const userResults      = ref([])

if (route.query.keyword) {
  keyword.value = route.query.keyword
  doSearch()
}

watch(() => route.query.keyword, (val) => {
  if (val) {
    keyword.value = val
    doSearch()
  }
})

async function doSearch() {
  const q = keyword.value.trim()
  router.replace({ path: '/search', query: q ? { keyword: q } : {} })

  if (!q) {
    searchedKeyword.value = ''
    videos.value = []
    videoTotal.value = 0
    hasMore.value = false
    userResults.value = []
    return
  }

  searchedKeyword.value = q
  // 搜索视频和用户并行
  await Promise.all([searchVideos(true), searchUsers()])
}

async function searchVideos(reset = false) {
  if (reset) currentPage.value = 1
  videoLoading.value = true
  try {
    const result = await videoApi.getVideoList({
      pageNum: currentPage.value,
      pageSize: PAGE_DEFAULTS.PAGE_SIZE,
      keyword: searchedKeyword.value,
      orderBy: orderBy.value
    })
    if (reset) {
      videos.value = result.list || []
    } else {
      videos.value.push(...(result.list || []))
    }
    videoTotal.value = result.total || 0
    hasMore.value = videos.value.length < videoTotal.value
  } finally {
    videoLoading.value = false
  }
}

async function searchUsers() {
  userLoading.value = true
  try {
    const result = await profileApi.searchUsers(searchedKeyword.value)
    userResults.value = result || []
  } finally {
    userLoading.value = false
  }
}

function switchTab(tab) {
  activeTab.value = tab
}

async function loadMore() {
  currentPage.value++
  await searchVideos(false)
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.layout-body { display: flex; margin-top: 56px; min-height: calc(100vh - 56px); }
.main-content { flex: 1; max-width: 1200px; margin: 0 auto; width: 100%; padding: 0 16px 40px; }

.search-header { padding: 24px 0 16px; }
.search-bar-large { display: flex; align-items: center; height: 48px; border-radius: 100px; overflow: hidden; background: var(--bg-input); border: 1px solid var(--border); transition: var(--transition); max-width: 640px; }
.search-bar-large:focus-within { border-color: var(--color-accent); }
.search-icon { font-size: 18px; color: var(--text-muted); margin-left: 18px; flex-shrink: 0; }
.search-input-large { flex: 1; padding: 0 12px; background: none; border: none; outline: none; color: var(--text-1); font-family: var(--font-body); font-size: 14px; }
.search-input-large::placeholder { color: var(--text-muted); }
.search-clear { width: 32px; height: 32px; border: none; background: none; color: var(--text-muted); cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 50%; transition: var(--transition); flex-shrink: 0; }
.search-clear:hover { background: var(--bg-hover); color: var(--text-1); }
.search-btn { width: 52px; height: 48px; background: var(--bg-hover); border: none; border-left: 1px solid var(--border); color: var(--text-2); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: var(--transition); flex-shrink: 0; }
.search-btn:hover { background: var(--color-accent); color: #fff; }

/* Search Tabs */
.search-tabs { display: flex; gap: 4px; padding: 16px 0; border-bottom: 1px solid var(--border); }
.search-tab { display: flex; align-items: center; gap: 6px; padding: 8px 18px; border-radius: 100px; border: none; background: none; color: var(--text-2); font-size: 14px; font-family: var(--font-body); cursor: pointer; transition: var(--transition); }
.search-tab:hover { background: var(--bg-hover); color: var(--text-1); }
.search-tab.active { background: var(--color-accent); color: #fff; }
.tab-count { font-size: 12px; padding: 1px 7px; border-radius: 100px; background: rgba(255,255,255,.2); }

.results-header { display: flex; align-items: center; gap: 10px; padding: 12px 0 8px; flex-wrap: wrap; }
.results-label { font-size: 14px; font-weight: 600; color: var(--text-1); }
.results-count { font-size: 13px; color: var(--text-muted); }
.order-select { margin-left: auto; width: 130px; }

.video-grid { margin-top: 20px; display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px 12px; }

/* User Results */
.user-list { margin-top: 16px; display: flex; flex-direction: column; gap: 4px; }
.user-card { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-radius: var(--radius-md); text-decoration: none; transition: var(--transition); }
.user-card:hover { background: var(--bg-hover); }
.user-avatar { width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, var(--color-accent), #7c3aed); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; color: #fff; flex-shrink: 0; }
.user-info { flex: 1; overflow: hidden; }
.user-name { display: block; font-size: 14px; font-weight: 600; color: var(--text-1); }
.user-email { display: block; font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.user-arrow { color: var(--text-muted); flex-shrink: 0; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 100px 0 60px; color: var(--text-muted); }
.empty-icon { font-size: 56px; margin-bottom: 16px; opacity: 0.6; }
.empty-state p { font-size: 14px; }
.empty-hint { margin-top: 6px; font-size: 12px !important; opacity: 0.7; }

.load-more { display: flex; justify-content: center; padding: 32px 0 16px; }
.load-more-btn { padding: 10px 32px; border-radius: 100px; border: 1px solid var(--border); background: var(--bg-card); color: var(--text-2); font-size: 13px; cursor: pointer; transition: var(--transition); font-family: var(--font-body); min-width: 120px; display: flex; align-items: center; justify-content: center; gap: 6px; }
.load-more-btn:hover:not(:disabled) { background: var(--bg-hover); color: var(--text-1); }
.load-more-btn:disabled { opacity: 0.6; cursor: default; }
.spin { font-size: 18px; animation: rotate .8s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }
</style>
