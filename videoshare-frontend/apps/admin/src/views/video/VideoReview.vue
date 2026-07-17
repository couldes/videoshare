<template>
  <div class="review-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">视频审核</h1>
        <p class="page-sub mono">VIDEO REVIEW / 待审核 {{ stats.pendingVideos }} 条</p>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card pending">
        <span class="stat-num">{{ stats.pendingVideos }}</span>
        <span class="stat-label">待审核</span>
      </div>
      <div class="stat-card published">
        <span class="stat-num">{{ stats.publishedVideos }}</span>
        <span class="stat-label">已发布</span>
      </div>
      <div class="stat-card offline">
        <span class="stat-num">{{ stats.offlineVideos }}</span>
        <span class="stat-label">已驳回</span>
      </div>
    </div>

    <!-- Tabs -->
    <div class="status-tabs">
      <button v-for="t in statusTabs" :key="t.value"
        class="tab-btn" :class="{ active: activeStatus === t.value }"
        @click="activeStatus = t.value; loadData()">
        {{ t.label }}
      </button>
    </div>

    <!-- Table -->
    <div class="table-wrap">
      <el-table v-loading="loading" :data="videoList" style="width:100%">
        <el-table-column label="VIDEO ID" width="120">
          <template #default="{ row }">
            <span class="mono" style="font-size:11px;color:var(--text-muted)">{{ row.videoId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="视频信息" min-width="260">
          <template #default="{ row }">
            <div class="video-cell">
              <div class="vc-thumb">
                <img v-if="row.coverUrl" :src="row.coverUrl" alt="" />
                <div v-else class="vc-placeholder" />
              </div>
              <div class="vc-meta">
                <div class="vc-title">{{ row.title }}</div>
                <div class="vc-sub mono">{{ row.userId }} · {{ row.category || '未分类' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="170">
          <template #default="{ row }">
            <span class="mono" style="font-size:12px">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" @click="showRejectDialog(row)">驳回</el-button>
            </template>
            <template v-else-if="row.status === 2">
              <el-button size="small" type="success" @click="handleApprove(row)">上架</el-button>
            </template>
            <span v-else class="mono" style="color:var(--text-muted);font-size:12px">已发布</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="pagination.total > 0" class="pagination-wrap">
      <el-pagination background layout="prev, pager, next"
        :total="pagination.total" :page-size="pagination.pageSize"
        v-model:current-page="pagination.pageNum" @current-change="loadData" />
    </div>

    <!-- Reject Dialog -->
    <el-dialog v-model="rejectDialog.visible" title="驳回原因" width="420px">
      <el-input v-model="rejectDialog.reason" type="textarea" :rows="3"
        placeholder="请输入驳回原因..." :maxlength="200" show-word-limit />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="handleReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminVideoApi } from '@/api'

const loading = ref(false)
const rejecting = ref(false)
const videoList = ref([])
const activeStatus = ref(null)
const pagination = reactive({ total: 0, pages: 0, pageNum: 1, pageSize: 15 })
const stats = reactive({ pendingVideos: 0, publishedVideos: 0, offlineVideos: 0 })
const rejectDialog = reactive({ visible: false, reason: '', videoId: '' })

const statusTabs = [
  { label: '全部', value: null },
  { label: '待审核', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已下架', value: 2 },
]

const VIDEO_STATUS_MAP = { 0: '待审核', 1: '已发布', 2: '已下架' }

function statusLabel(status) { return VIDEO_STATUS_MAP[status] || '未知' }
function statusType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return ''
}

onMounted(() => { loadData(); loadStats() })

async function loadData() {
  loading.value = true
  try {
    const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    if (activeStatus.value !== null) params.status = activeStatus.value
    const data = await adminVideoApi.getVideoList(params)
    videoList.value = data.list || []
    pagination.total = data.total || 0
    pagination.pages = data.pages || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function loadStats() {
  try {
    const data = await adminVideoApi.getStats()
    stats.pendingVideos = data.pendingVideos || 0
    stats.publishedVideos = data.publishedVideos || 0
    stats.offlineVideos = data.offlineVideos || 0
  } catch {}
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定要通过视频「${row.title}」吗？`, '审核确认',
      { confirmButtonText: '通过', cancelButtonText: '取消', type: 'success' })
    await adminVideoApi.updateStatus({ videoId: row.videoId, status: 1 })
    ElMessage.success('审核通过')
    row.status = 1
    loadStats()
  } catch {}
}

function showRejectDialog(row) {
  rejectDialog.videoId = row.videoId
  rejectDialog.reason = ''
  rejectDialog.visible = true
}

async function handleReject() {
  if (!rejectDialog.reason.trim()) { ElMessage.warning('请输入驳回原因'); return }
  rejecting.value = true
  try {
    await adminVideoApi.updateStatus({
      videoId: rejectDialog.videoId,
      status: 2,
      remark: rejectDialog.reason.trim()
    })
    ElMessage.success('已驳回')
    rejectDialog.visible = false
    loadData()
    loadStats()
  } catch { ElMessage.error('操作失败') }
  finally { rejecting.value = false }
}
</script>

<style scoped>
.review-page { padding: 20px; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; }
.page-sub { font-size: 13px; color: var(--text-muted); margin-top: 4px; }

.stats-row { display: flex; gap: 16px; margin-bottom: 24px; }
.stat-card {
  flex: 1; padding: 20px; border-radius: 8px; display: flex;
  flex-direction: column; gap: 4px; border: 1px solid var(--border);
}
.stat-num { font-size: 28px; font-weight: 800; }
.stat-label { font-size: 13px; color: var(--text-muted); }
.stat-card.pending .stat-num { color: #eab308; }
.stat-card.published .stat-num { color: #22c55e; }
.stat-card.offline .stat-num { color: #9ca3af; }

.status-tabs { display: flex; gap: 4px; border-bottom: 1px solid var(--border); margin-bottom: 16px; }
.tab-btn {
  padding: 8px 16px; background: none; border: none; border-bottom: 2px solid transparent;
  color: var(--text-2); font-size: 13px; cursor: pointer; font-family: var(--font-body);
  margin-bottom: -1px;
}
.tab-btn:hover { color: var(--text-1); }
.tab-btn.active { color: var(--text-1); border-bottom-color: var(--text-1); font-weight: 600; }

.table-wrap { background: var(--bg-surface); border: 1px solid var(--border); border-radius: 8px; overflow: hidden; }
.video-cell { display: flex; align-items: center; gap: 12px; }
.vc-thumb { width: 80px; height: 45px; border-radius: 4px; overflow: hidden; background: #000; flex-shrink: 0; }
.vc-thumb img { width: 100%; height: 100%; object-fit: cover; }
.vc-placeholder { width: 100%; height: 100%; background: var(--bg-hover); }
.vc-meta { min-width: 0; }
.vc-title { font-size: 13px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.vc-sub { font-size: 11px; color: var(--text-muted); margin-top: 2px; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }

.mono { font-family: 'SF Mono', 'Fira Code', monospace; }
</style>
