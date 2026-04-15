<template>
  <div class="layout">
    <NavBar @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed" />
    <div class="layout-body">
      <SideBar :collapsed="sidebarCollapsed" />
      <main class="main-content">
        <div class="category-bar">
          <div class="category-scroll">
            <button v-for="cat in categoryTabs" :key="cat" class="category-tab"
              :class="{ active: activeCategory === cat }" @click="activeCategory = cat">
              {{ cat }}
            </button>
          </div>
        </div>
        <div class="video-grid">
          <VideoCard v-for="video in filteredVideos" :key="video.id" :video="video" />
        </div>
        <div class="load-more">
          <button class="load-more-btn" @click="loadMore">
            <span v-if="!loading">加载更多</span>
            <el-icon v-else class="spin"><Loading /></el-icon>
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import NavBar from '@/components/NavBar.vue'
import SideBar from '@/components/SideBar.vue'
import VideoCard from '@/components/VideoCard.vue'

const sidebarCollapsed = ref(false)
const activeCategory   = ref('全部')
const loading          = ref(false)
const categoryTabs = ['全部','音乐','游戏','科技','体育','美食','教育','影视','生活']
const allVideos = ref(generateMockVideos(24))

const filteredVideos = computed(() =>
  activeCategory.value === '全部' ? allVideos.value
    : allVideos.value.filter((_, i) => i % 3 === categoryTabs.indexOf(activeCategory.value) % 3)
)

async function loadMore() {
  loading.value = true
  await new Promise(r => setTimeout(r, 800))
  allVideos.value.push(...generateMockVideos(8, allVideos.value.length))
  loading.value = false
}

function generateMockVideos(count, offset = 0) {
  const titles   = ['Vue 3 完整入门教程 — 从零到项目实战','Spring Boot 搭建视频平台后端','2025 年最值得学习的编程语言','Redis 缓存实战：验证码与限流','UI 设计趋势分析 — 暗色极简主义','算法刷题：二叉树遍历全解析','Docker + Nginx 部署前后端分离','MySQL 索引优化，慢查询从3s到0.1s','Vite + Vue3 + TypeScript 工程化','MyBatis Plus 使用技巧与常见坑','CSS 动画进阶 — 只用CSS的60个效果','Java 并发编程深入理解 ThreadPool']
  const channels = ['极客出发','CodeWithMe','技术老狗','前端匠人','后端小站','架构师说']
  const durations= ['8:24','15:03','22:47','6:31','11:15','31:08','9:55','18:22']
  return Array.from({ length: count }, (_, i) => ({
    id:          offset + i + 1,
    title:       titles[(offset + i) % titles.length],
    thumbnail:   `https://picsum.photos/seed/${offset + i + 10}/640/360`,
    duration:    durations[(offset + i) % durations.length],
    channelName: channels[(offset + i) % channels.length],
    views:       Math.floor(Math.random() * 500000) + 1000,
    createTime:  new Date(Date.now() - Math.random() * 30 * 86400000).toISOString()
  }))
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.layout-body { display: flex; margin-top: 56px; min-height: calc(100vh - 56px); }
.main-content { flex: 1; overflow: hidden; padding: 0 16px 40px; }
.category-bar { position: sticky; top: 56px; background: rgba(10,10,12,.9); backdrop-filter: blur(10px); padding: 10px 16px; margin: 0 -16px; border-bottom: 1px solid var(--border); z-index: 50; }
.category-scroll { display: flex; gap: 8px; overflow-x: auto; scrollbar-width: none; }
.category-scroll::-webkit-scrollbar { display: none; }
.category-tab { padding: 6px 14px; border-radius: 100px; border: 1px solid var(--border); background: var(--bg-card); color: var(--text-2); font-size: 13px; font-weight: 500; cursor: pointer; white-space: nowrap; transition: var(--transition); font-family: var(--font-body); }
.category-tab:hover { background: var(--bg-hover); color: var(--text-1); }
.category-tab.active { background: var(--text-1); color: var(--bg-base); border-color: var(--text-1); font-weight: 600; }
.video-grid { margin-top: 20px; display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px 12px; }
.load-more { display: flex; justify-content: center; padding: 32px 0 16px; }
.load-more-btn { padding: 10px 32px; border-radius: 100px; border: 1px solid var(--border); background: var(--bg-card); color: var(--text-2); font-size: 13px; cursor: pointer; transition: var(--transition); font-family: var(--font-body); min-width: 120px; display: flex; align-items: center; justify-content: center; }
.load-more-btn:hover { background: var(--bg-hover); color: var(--text-1); }
.spin { font-size: 18px; animation: rotate .8s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }
</style>
