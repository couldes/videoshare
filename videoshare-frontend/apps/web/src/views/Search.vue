<template>
  <DefaultLayout>
    <div class="search-header">
      <div class="search-bar-large">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" class="search-icon"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
        <input v-model="keyword" class="search-input-large"
          :placeholder="activeTab === 'video' ? '搜索视频' : '搜索用户'"
          @keydown.enter="doSearch" autofocus />
        <button v-if="keyword" class="search-clear" @click="clearSearch">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
        </button>
        <button class="search-btn" @click="doSearch">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
        </button>
      </div>
    </div>

    <!-- Search Tabs -->
    <div v-if="searchedKeyword" class="search-tabs">
      <button class="search-tab" :class="{ active: activeTab === 'video' }" @click="switchTab('video')">
        视频 <span v-if="videoTotal > 0" class="tab-count">{{ videoTotal }}</span>
      </button>
      <button class="search-tab" :class="{ active: activeTab === 'user' }" @click="switchTab('user')">
        用户 <span v-if="userResults.length > 0" class="tab-count">{{ userResults.length }}</span>
      </button>
    </div>

    <!-- Video Results -->
    <template v-if="activeTab === 'video'">
      <div v-if="searchedKeyword" class="results-header">
        <span class="results-label">搜索结果</span>
        <el-select v-model="orderBy" class="order-select" @change="searchVideos(true)" size="small">
          <el-option label="相关度" value="relevance" />
          <el-option label="最多播放" value="view_count" />
          <el-option label="最新发布" value="createTime" />
        </el-select>
      </div>
      <div v-if="videos.length > 0" class="video-list">
        <VideoListItem v-for="video in videos" :key="video.videoId" :video="video" />
      </div>
      <div v-else-if="!videoLoading && searchedKeyword" class="empty-state">
        <p>没有找到相关视频</p>
        <p class="empty-hint">试试其他关键词</p>
      </div>
      <div v-if="hasMore" class="load-more">
        <button class="load-more-btn" :disabled="videoLoading" @click="loadMore">
          {{ videoLoading ? '搜索中...' : '加载更多' }}
        </button>
      </div>
    </template>

    <!-- User Results -->
    <template v-if="activeTab === 'user'">
      <div v-if="userResults.length > 0" class="user-list">
        <RouterLink v-for="user in userResults" :key="user.userId"
          :to="`/user/${user.userId}`" class="user-card">
          <img v-if="user.avatarUrl" :src="user.avatarUrl" class="user-avatar-img" alt="" />
          <div v-else class="user-avatar">{{ user.nickName?.charAt(0).toUpperCase() }}</div>
          <div class="user-info">
            <span class="user-name">{{ user.nickName }}</span>
            <span class="user-email">{{ user.email }}</span>
          </div>
        </RouterLink>
      </div>
      <div v-else-if="!userLoading && searchedKeyword" class="empty-state">
        <p>没有找到相关用户</p>
      </div>
    </template>

    <!-- Initial state -->
    <div v-if="!searchedKeyword && !videoLoading && !userLoading" class="empty-state">
      <p>输入关键词开始搜索</p>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import VideoListItem from '@/components/VideoListItem.vue'
import { videoApi, profileApi } from '@/api'
import { PAGE_DEFAULTS } from '@videoshare/constants'

const route  = useRoute()
const router = useRouter()

const keyword         = ref('')
const searchedKeyword = ref('')
const activeTab       = ref('video')
const videoLoading    = ref(false)
const userLoading     = ref(false)
const videos          = ref([])
const videoTotal      = ref(0)
const currentPage     = ref(1)
const hasMore         = ref(false)
const orderBy         = ref('relevance')
const userResults     = ref([])

if (route.query.keyword) {
  keyword.value = route.query.keyword
  doSearch()
}

watch(() => route.query.keyword, (val) => {
  if (val) { keyword.value = val; doSearch() }
})

async function doSearch() {
  const q = keyword.value.trim()
  router.replace({ path: '/search', query: q ? { keyword: q } : {} })
  if (!q) { searchedKeyword.value = ''; videos.value = []; videoTotal.value = 0; hasMore.value = false; userResults.value = []; return }
  searchedKeyword.value = q
  await Promise.all([searchVideos(true), searchUsers()])
}

async function searchVideos(reset = false) {
  if (reset) currentPage.value = 1
  videoLoading.value = true
  try {
    const result = await videoApi.searchVideos({
      pageNum: currentPage.value, pageSize: PAGE_DEFAULTS.PAGE_SIZE,
      keyword: searchedKeyword.value, orderBy: orderBy.value
    })
    if (reset) videos.value = result.list || []
    else videos.value.push(...(result.list || []))
    videoTotal.value = result.total || 0
    hasMore.value = videos.value.length < videoTotal.value
  } finally { videoLoading.value = false }
}

async function searchUsers() {
  userLoading.value = true
  try { const result = await profileApi.searchUsers(searchedKeyword.value); userResults.value = result || [] }
  finally { userLoading.value = false }
}

function clearSearch() { keyword.value = ''; doSearch() }
function switchTab(tab) { activeTab.value = tab }
async function loadMore() { currentPage.value++; await searchVideos(false) }
</script>

<style scoped>
.search-header { padding: 24px 0 16px; }
.search-bar-large {
  display: flex; align-items: center; height: 44px;
  border-radius: 22px; overflow: hidden;
  background: var(--bg-input); border: 1px solid var(--border);
  transition: var(--transition); max-width: 600px;
}
.search-bar-large:focus-within { border-color: #555; }
.search-icon { font-size: 18px; color: var(--text-muted); margin-left: 16px; flex-shrink: 0; }
.search-input-large {
  flex: 1; padding: 0 12px; background: none; border: none;
  outline: none; color: var(--text-1); font-family: var(--font-body); font-size: 14px;
}
.search-input-large::placeholder { color: var(--text-muted); }
.search-clear {
  width: 32px; height: 32px; border: none; background: none;
  color: var(--text-muted); cursor: pointer; display: flex;
  align-items: center; justify-content: center; border-radius: 50%;
  transition: var(--transition); flex-shrink: 0;
}
.search-clear:hover { color: var(--text-1); }
.search-btn {
  width: 56px; height: 44px; background: var(--bg-hover);
  border: none; border-left: 1px solid var(--border);
  color: var(--text-1); cursor: pointer; display: flex;
  align-items: center; justify-content: center; flex-shrink: 0;
}
.search-btn:hover { background: #333; }

.search-tabs { display: flex; gap: 4px; padding: 12px 0; border-bottom: 1px solid var(--border); }
.search-tab {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px; border: none;
  background: none; color: var(--text-2); font-size: 14px;
  font-family: var(--font-body); cursor: pointer; transition: var(--transition);
}
.search-tab:hover { background: var(--bg-hover); color: var(--text-1); }
.search-tab.active { background: var(--text-1); color: var(--bg-base); }
.tab-count { font-size: 12px; padding: 1px 7px; border-radius: 8px; background: rgba(0,0,0,.15); }

.results-header { display: flex; align-items: center; gap: 10px; padding: 12px 0; }
.results-label { font-size: 14px; font-weight: 600; }
.order-select { margin-left: auto; width: 130px; }

.video-list { display: flex; flex-direction: column; gap: 8px; margin-top: 8px; }

.user-list { margin-top: 16px; display: flex; flex-direction: column; gap: 4px; }
.user-card {
  display: flex; align-items: center; gap: 12px; padding: 10px 12px;
  border-radius: var(--radius-md); text-decoration: none; transition: var(--transition);
}
.user-card:hover { background: var(--bg-hover); }
.user-avatar-img { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.user-avatar {
  width: 44px; height: 44px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 16px; color: #fff; flex-shrink: 0;
}
.user-info { flex: 1; overflow: hidden; }
.user-name { display: block; font-size: 14px; font-weight: 600; color: var(--text-1); }
.user-email { display: block; font-size: 12px; color: var(--text-muted); margin-top: 2px; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80px 0; color: var(--text-muted); }
.empty-hint { margin-top: 6px; font-size: 12px; opacity: 0.7; }
.load-more { display: flex; justify-content: center; padding: 32px 0 16px; }
.load-more-btn {
  padding: 8px 24px; border-radius: 20px; border: none;
  background: var(--bg-hover); color: var(--text-1); font-size: 13px;
  cursor: pointer; transition: var(--transition); font-family: var(--font-body);
}
.load-more-btn:hover:not(:disabled) { background: #3a3a3a; }
.load-more-btn:disabled { opacity: 0.6; cursor: default; }
</style>
