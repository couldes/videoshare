<template>
  <div class="manage-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="page-body">
      <h1 class="page-title">我的视频</h1>

      <el-table :data="videoList" v-loading="loading" stripe>
        <el-table-column label="视频" min-width="280">
          <template #default="{ row }">
            <div class="video-cell">
              <img v-if="row.coverUrl" :src="row.coverUrl" class="cover-thumb" alt="" />
              <div v-else class="cover-thumb cover-placeholder" />
              <div class="video-info">
                <span class="video-title">{{ row.title }}</span>
                <span class="video-meta">{{ formatRelative(row.createTime) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span class="status-tag" :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="观看数" width="100" :formatter="(r) => formatViews(r.viewCount)" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 2" size="small" text type="success" @click="handleRepublish(row)">重新发布</el-button>
              <el-button v-if="row.status === 1" size="small" text type="warning" @click="handleUnpublish(row)">下架</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="videoList.length === 0 && !loading" class="empty-state">
        <p>暂无视频</p>
        <RouterLink to="/upload" class="upload-link">去发布第一个视频</RouterLink>
      </div>

      <div v-if="totalPages > 1" class="pagination-wrap">
        <el-pagination background layout="prev, pager, next"
          :total="total" :page-size="pageSize"
          v-model:current-page="pageNum" @current-change="loadVideos" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { videoApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { formatViews, formatRelative } from '@videoshare/utils/format'

const router = useRouter()
const userStore = useUserStore()

const videoList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(0)

const statusMap = { 0: '待审核', 1: '已发布', 2: '已下架' }
const statusClassMap = { 0: 'status-processing', 1: 'status-published', 2: 'status-unpublished' }

function statusLabel(status) { return statusMap[status] || '未知' }
function statusClass(status) { return statusClassMap[status] || '' }

onMounted(() => { loadVideos() })

async function loadVideos(p = 1) {
  loading.value = true
  try {
    const userId = userStore.userInfo?.userId
    const data = await videoApi.getUserVideos(userId, { pageNum: p, pageSize: pageSize.value })
    videoList.value = data.list || []
    total.value = data.total || 0
    totalPages.value = data.pages || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleEdit(row) {
  router.push(`/edit/${row.videoId}`)
}

async function handleRepublish(row) {
  try {
    await ElMessageBox.confirm('确定要重新发布该视频吗？', '确认发布', { confirmButtonText: '发布', cancelButtonText: '取消', type: 'info' })
    await videoApi.republishVideo(row.videoId)
    row.status = 1
    ElMessage.success('视频已重新发布')
  } catch { /* cancelled */ }
}

async function handleUnpublish(row) {
  try {
    await ElMessageBox.confirm('确定要下架该视频吗？', '确认下架', { confirmButtonText: '下架', cancelButtonText: '取消', type: 'warning' })
    await videoApi.unpublishVideo(row.videoId)
    row.status = 2
    ElMessage.success('视频已下架')
  } catch { /* cancelled */ }
}
</script>

<style scoped>
.manage-page { min-height: 100vh; background: var(--bg-base); }
.page-body { max-width: 1100px; margin: 0 auto; padding: 72px 20px 60px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; }

.video-cell { display: flex; align-items: center; gap: 12px; }
.cover-thumb { width: 100px; height: 56px; border-radius: 4px; object-fit: cover; background: #000; flex-shrink: 0; }
.cover-placeholder { background: var(--bg-hover); }
.video-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.video-title { font-size: 13px; font-weight: 600; color: var(--text-1); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.video-meta { font-size: 12px; color: var(--text-muted); }

.status-tag { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: 600; }
.status-processing { background: rgba(234,179,8,.15); color: #eab308; }
.status-published { background: rgba(34,197,94,.15); color: #22c55e; }
.status-unpublished { background: rgba(156,163,175,.15); color: #9ca3af; }

.action-btns { display: flex; gap: 4px; }

.empty-state { text-align: center; padding: 60px 0; color: var(--text-muted); }
.upload-link { color: var(--color-accent); text-decoration: none; font-weight: 600; font-size: 14px; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }

.manage-page :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: var(--bg-hover);
}
.manage-page :deep(.el-table__row:hover) {
  background: var(--bg-card);
}
</style>
