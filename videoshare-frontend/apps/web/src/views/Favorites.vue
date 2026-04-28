<template>
  <div class="layout">
    <NavBar @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed" />
    <div class="layout-body">
      <SideBar :collapsed="sidebarCollapsed" />
      <main class="main-content">

        <div class="page-header">
          <h1 class="page-title">我的收藏</h1>
          <p class="page-sub">共 {{ total }} 个视频</p>
        </div>

        <div v-if="loading" class="loading-state">
          <el-icon class="spin"><Loading /></el-icon>
        </div>

        <div v-else-if="videos.length" class="video-grid">
          <div v-for="v in videos" :key="v.videoId" class="fav-card">
            <RouterLink :to="`/video/${v.videoId}`" class="fav-thumb">
              <img :src="v.coverUrl" :alt="v.title" loading="lazy" />
              <span class="fav-duration">{{ v.duration }}</span>
            </RouterLink>
            <div class="fav-info">
              <RouterLink :to="`/video/${v.videoId}`" class="fav-title">
                {{ v.title }}
              </RouterLink>
              <RouterLink :to="`/user/${v.userInfo?.userId}`" class="fav-channel">
                {{ v.userInfo?.nickName }}
              </RouterLink>
              <p class="fav-stats">
                {{ formatViews(v.viewCount) }} 次观看
                · {{ formatRelative(v.createTime) }}
              </p>
            </div>
            <!-- 取消收藏 -->
            <button class="unfav-btn" @click="handleUnfavorite(v)" title="取消收藏">
              <el-icon><StarFilled /></el-icon>
            </button>
          </div>
        </div>

        <div v-else class="empty-state">
          <el-icon class="empty-icon"><Star /></el-icon>
          <h3>还没有收藏任何视频</h3>
          <p>在视频播放页点击"收藏"即可保存到这里</p>
          <RouterLink to="/"><el-button type="primary">去发现视频</el-button></RouterLink>
        </div>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination">
          <el-pagination v-model:current-page="pageNum" :page-size="pageSize"
            :total="total" background layout="prev, pager, next"
            @current-change="loadFavorites" />
        </div>

      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import SideBar from '@/components/SideBar.vue'
import { profileApi, videoApi } from '@/api'
import { formatViews, formatRelative } from '@videoshare/utils/format'

const sidebarCollapsed = ref(false)
const videos   = ref([])
const loading  = ref(false)
const pageNum  = ref(1)
const pageSize = ref(24)
const total    = ref(0)

onMounted(loadFavorites)

async function loadFavorites() {
  loading.value = true
  try {
    // GET /user/favorites → UserProfileController.getFavorites()
    const result = await profileApi.getFavorites({
      pageNum:  pageNum.value,
      pageSize: pageSize.value
    })
    videos.value = result.list || []
    total.value  = result.total || 0
  } finally {
    loading.value = false
  }
}

async function handleUnfavorite(video) {
  try {
    await ElMessageBox.confirm(`确定要取消收藏「${video.title}」吗？`, '提示', {
      confirmButtonText: '取消收藏', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return // 用户取消
  }

  try {
    // POST /video/action { actionType: 2 } → 取消收藏
    await videoApi.doAction({ videoId: video.videoId, actionType: 2 })
    videos.value = videos.value.filter(v => v.videoId !== video.videoId)
    total.value--
    ElMessage.success('已取消收藏')
  } catch {}
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.layout-body  { display: flex; margin-top: 56px; min-height: calc(100vh - 56px); }
.main-content { flex: 1; padding: 24px; overflow: hidden; }

.page-header { margin-bottom: 24px; }
.page-title  { font-size: 22px; font-weight: 700; letter-spacing: -.02em; margin-bottom: 4px; }
.page-sub    { font-size: 13px; color: var(--text-2); }

.loading-state { display: flex; justify-content: center; padding: 80px 0; }
.spin { font-size: 28px; animation: rotate 1s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }

/* 收藏列表（横向卡片）*/
.video-grid { display: flex; flex-direction: column; gap: 12px; }
.fav-card {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 12px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  transition: var(--transition);
}
.fav-card:hover { background: var(--bg-card); border-color: var(--border-strong); }

.fav-thumb {
  position: relative;
  width: 200px;
  height: 112px;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--bg-card);
}
.fav-thumb img { width: 100%; height: 100%; object-fit: cover; }
.fav-duration  { position: absolute; bottom: 4px; right: 4px; background: rgba(0,0,0,.8); color: #fff; font-size: 10px; padding: 1px 5px; border-radius: 3px; }

.fav-info { flex: 1; min-width: 0; }
.fav-title { display: block; font-size: 15px; font-weight: 600; color: var(--text-1); text-decoration: none; margin-bottom: 6px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.fav-title:hover { color: var(--color-accent); }
.fav-channel { font-size: 13px; color: var(--text-2); text-decoration: none; display: block; margin-bottom: 4px; }
.fav-channel:hover { color: var(--text-1); }
.fav-stats { font-size: 12px; color: var(--text-muted); }

.unfav-btn { flex-shrink: 0; width: 32px; height: 32px; border: none; background: none; color: var(--color-accent); cursor: pointer; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; font-size: 18px; transition: var(--transition); }
.unfav-btn:hover { background: var(--bg-hover); }

/* 空状态 */
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; padding: 80px 0; text-align: center; }
.empty-icon  { font-size: 56px; color: var(--text-muted); }
.empty-state h3 { font-size: 18px; font-weight: 600; }
.empty-state p  { font-size: 14px; color: var(--text-2); }

.pagination { display: flex; justify-content: center; padding: 24px 0; }
</style>
