<template>
  <div class="layout">
    <NavBar @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed" />
    <div class="layout-body">
      <SideBar :collapsed="sidebarCollapsed" />
      <main class="main-content">
        <div class="category-bar">
          <div class="category-scroll">
            <button v-for="cat in categoryTabs" :key="cat" class="category-tab"
              :class="{ active: activeCategory === cat }" @click="switchCategory(cat)">
              {{ cat }}
            </button>
          </div>
        </div>

        <div v-if="videos.length > 0" class="video-grid">
          <VideoCard v-for="video in videos" :key="video.videoId" :video="video" />
        </div>

        <div v-else-if="!loading" class="empty-state">
          <el-icon class="empty-icon"><VideoCamera /></el-icon>
          <p>暂无视频内容</p>
        </div>

        <div v-if="hasMore" class="load-more">
          <button class="load-more-btn" :disabled="loading" @click="loadMore">
            <el-icon v-if="loading" class="spin"><Loading /></el-icon>
            <span>{{ loading ? '加载中...' : '加载更多' }}</span>
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import SideBar from '@/components/SideBar.vue'
import VideoCard from '@/components/VideoCard.vue'
import { videoApi } from '@/api'
import { PAGE_DEFAULTS } from '@videoshare/constants'

const sidebarCollapsed = ref(false)
const activeCategory   = ref('全部')
const loading          = ref(false)
const videos           = ref([])
const totalCount       = ref(0)
const currentPage      = ref(1)
const hasMore          = ref(false)

const categoryTabs = ['全部','音乐','游戏','科技','体育','美食','教育','影视','生活']

onMounted(() => fetchVideos(true))

function buildQuery(reset) {
  if (reset) currentPage.value = 1
  const params = {
    pageNum:  currentPage.value,
    pageSize: PAGE_DEFAULTS.PAGE_SIZE
  }
  if (activeCategory.value !== '全部') params.category = activeCategory.value
  return params
}

async function fetchVideos(reset = false) {
  loading.value = true
  try {
    const params = buildQuery(reset)
    const result = await videoApi.getVideoList(params)
    if (reset) {
      videos.value = result.list || []
    } else {
      videos.value.push(...(result.list || []))
    }
    totalCount.value = result.total || 0
    hasMore.value = videos.value.length < totalCount.value
  } finally {
    loading.value = false
  }
}

function switchCategory(cat) {
  activeCategory.value = cat
  fetchVideos(true)
}

async function loadMore() {
  currentPage.value++
  await fetchVideos(false)
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.layout-body { display: flex; margin-top: 56px; min-height: calc(100vh - 56px); }
.main-content { flex: 1; overflow: hidden; padding: 0 16px 40px; }
.category-bar { position: sticky; top: 56px; background: var(--bg-blur); backdrop-filter: blur(10px); padding: 10px 16px; margin: 0 -16px; border-bottom: 1px solid var(--border); z-index: 50; }
.category-scroll { display: flex; gap: 8px; overflow-x: auto; scrollbar-width: none; }
.category-scroll::-webkit-scrollbar { display: none; }
.category-tab { padding: 6px 14px; border-radius: 100px; border: 1px solid var(--border); background: var(--bg-card); color: var(--text-2); font-size: 13px; font-weight: 500; cursor: pointer; white-space: nowrap; transition: var(--transition); font-family: var(--font-body); }
.category-tab:hover { background: var(--bg-hover); color: var(--text-1); }
.category-tab.active { background: var(--text-1); color: var(--bg-base); border-color: var(--text-1); font-weight: 600; }
.video-grid { margin-top: 20px; display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px 12px; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 0; color: var(--text-muted); }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-state p { font-size: 14px; }
.load-more { display: flex; justify-content: center; padding: 32px 0 16px; }
.load-more-btn { padding: 10px 32px; border-radius: 100px; border: 1px solid var(--border); background: var(--bg-card); color: var(--text-2); font-size: 13px; cursor: pointer; transition: var(--transition); font-family: var(--font-body); min-width: 120px; display: flex; align-items: center; justify-content: center; gap: 6px; }
.load-more-btn:hover:not(:disabled) { background: var(--bg-hover); color: var(--text-1); }
.load-more-btn:disabled { opacity: 0.6; cursor: default; }
.spin { font-size: 18px; animation: rotate .8s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }
</style>
