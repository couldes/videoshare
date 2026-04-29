<template>
  <div class="video-manage">
    <div class="page-header">
      <div>
        <h1 class="page-title">视频管理</h1>
        <p class="page-sub mono">VIDEO MANAGEMENT / 共 {{ pagination.total }} 条记录</p>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索标题 / 标签..."
          clearable style="width:220px" :prefix-icon="Search"
          @keydown.enter="handleSearch" @clear="handleSearch"/>

        <el-select v-model="query.status" placeholder="视频状态"
          clearable style="width:130px" @change="handleSearch">
          <el-option label="全部状态" :value="null"/>
          <el-option v-for="(label, val) in VIDEO_STATUS_MAP"
            :key="val" :label="label" :value="Number(val)"/>
        </el-select>

        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
      <div class="toolbar-right mono">
        <span class="count-tag">
          RESULT: {{ pagination.total }} records / {{ pagination.pages }} pages
        </span>
      </div>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="videoList"
        element-loading-background="rgba(10,10,12,0.7)"
        element-loading-text="加载中..." style="width:100%" row-key="videoId">

        <el-table-column label="VIDEO ID" width="120">
          <template #default="{ row }">
            <span class="mono" style="font-size:11px;color:var(--text-muted)">{{ row.videoId }}</span>
          </template>
        </el-table-column>

        <el-table-column label="视频信息" min-width="280">
          <template #default="{ row }">
            <div class="video-cell">
              <div class="vc-thumb">
                <template v-if="isValidCover(row.coverUrl) && !row._coverFailed">
                  <img :src="row.coverUrl" loading="lazy" @error="row._coverFailed = true" />
                </template>
                <el-icon v-else class="vc-placeholder"><VideoCamera /></el-icon>
              </div>
              <div class="vc-meta">
                <div class="vc-title">{{ row.title }}</div>
                <div class="vc-sub mono">
                  <span>{{ row.category || '未分类' }}</span>
                  <span class="vc-dot">·</span>
                  <span>{{ row.userId }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="播放量" width="100" align="right">
          <template #default="{ row }">
            <span class="mono" style="font-size:13px">{{ formatViews(row.viewCount) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="点赞" width="80" align="right">
          <template #default="{ row }">
            <span style="font-size:12px;color:var(--text-2)">{{ formatViews(row.likeCount) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="评论" width="80" align="right">
          <template #default="{ row }">
            <span style="font-size:12px;color:var(--text-2)">{{ row.commentCount ?? 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" width="155">
          <template #default="{ row }">
            <span class="mono" style="font-size:12px;color:var(--text-muted)">
              {{ formatDate(row.createTime) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="VIDEO_STATUS_TAG[row.status]" size="small" effect="dark">
              {{ VIDEO_STATUS_MAP[row.status]?.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-tooltip
                :content="row.status === VIDEO_STATUS.PUBLISHED ? '下架该视频' : '上架该视频'"
                placement="top">
                <button class="act-btn"
                  :class="row.status === VIDEO_STATUS.PUBLISHED ? 'act-warn' : 'act-success'"
                  @click="handleToggleStatus(row)">
                  <el-icon>
                    <component :is="row.status === VIDEO_STATUS.PUBLISHED ? 'VideoCamera' : 'CircleCheck'"/>
                  </el-icon>
                  {{ row.status === VIDEO_STATUS.PUBLISHED ? '下架' : '上架' }}
                </button>
              </el-tooltip>
              <el-tooltip content="删除视频（不可恢复）" placement="top">
                <button class="act-btn act-danger" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>删除
                </button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 15, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="fetchVideos"
        @size-change="handleSearch"/>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { adminVideoApi } from '@/api'
import { VIDEO_STATUS, VIDEO_STATUS_MAP, VIDEO_STATUS_TAG, PAGE_DEFAULTS } from '@videoshare/constants'
import { formatDate, formatViews } from '@videoshare/utils/format'

const loading  = ref(false)
const videoList = ref([])

const query = reactive({
  pageNum:  PAGE_DEFAULTS.PAGE_NUM,
  pageSize: PAGE_DEFAULTS.PAGE_SIZE,
  keyword:  '',
  status:   null
})

const pagination = reactive({ total: 0, pages: 0 })

function isValidCover(url) {
  return url && typeof url === 'string' && !url.startsWith('blob:') && url.length > 0
}

onMounted(fetchVideos)

async function fetchVideos() {
  loading.value = true
  try {
    const result = await adminVideoApi.getVideoList({
      pageNum:  query.pageNum,
      pageSize: query.pageSize,
      keyword:  query.keyword || undefined,
      status:   query.status != null ? query.status : undefined
    })
    videoList.value   = result.list
    pagination.total = result.total
    pagination.pages = result.pages
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = PAGE_DEFAULTS.PAGE_NUM
  fetchVideos()
}

function handleReset() {
  query.keyword  = ''
  query.status   = null
  query.pageNum  = PAGE_DEFAULTS.PAGE_NUM
  query.pageSize = PAGE_DEFAULTS.PAGE_SIZE
  fetchVideos()
}

async function handleToggleStatus(row) {
  const newStatus = row.status === VIDEO_STATUS.PUBLISHED
    ? VIDEO_STATUS.OFFLINE
    : VIDEO_STATUS.PUBLISHED
  const action = VIDEO_STATUS_MAP[newStatus]

  try {
    await ElMessageBox.confirm(
      `确定要${action}视频 "${row.title}" 吗？`,
      `${action}确认`,
      { confirmButtonText: `确定${action}`, cancelButtonText: '取消',
        type: newStatus === VIDEO_STATUS.OFFLINE ? 'warning' : 'info' }
    )
    await adminVideoApi.updateStatus({ videoId: row.videoId, status: newStatus })
    ElMessage.success(`已${action}视频 ${row.title}`)
    row.status = newStatus
  } catch (err) {
    if (err === 'cancel') return
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `此操作将永久删除视频 "${row.title}"，且无法恢复！`,
      '高危操作确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
    await adminVideoApi.deleteVideo(row.videoId)
    ElMessage.success(`已删除视频 ${row.title}`)
    const idx = videoList.value.findIndex(v => v.videoId === row.videoId)
    if (idx !== -1) { videoList.value.splice(idx, 1); pagination.total-- }
  } catch (err) {
    if (err === 'cancel') return
  }
}
</script>

<style scoped>
.video-manage { max-width: 1300px; }
.page-header { margin-bottom: 20px; }
.page-title  { font-size: 22px; font-weight: 700; letter-spacing: -.02em; margin-bottom: 4px; }
.page-sub    { font-size: 11px; color: var(--text-muted); letter-spacing: .08em; }
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 14px 16px; background: var(--bg-panel); border: 1px solid var(--border); border-radius: var(--radius-lg) var(--radius-lg) 0 0; flex-wrap: wrap; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.count-tag { font-size: 11px; color: var(--text-muted); letter-spacing: .06em; padding: 4px 10px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 3px; }
.table-wrap { background: var(--bg-panel); border: 1px solid var(--border); border-top: none; }
.video-cell { display: flex; align-items: center; gap: 10px; }
.vc-thumb { width: 100px; height: 56px; border-radius: var(--radius-sm); overflow: hidden; background: var(--bg-hover); flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.vc-thumb img { width: 100%; height: 100%; object-fit: cover; }
.vc-placeholder { font-size: 22px; color: var(--text-muted); }
.vc-meta { flex: 1; min-width: 0; }
.vc-title { font-size: 13px; font-weight: 600; color: var(--text-1); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.4; }
.vc-sub { font-size: 11px; color: var(--text-muted); margin-top: 3px; display: flex; align-items: center; gap: 4px; }
.vc-dot { color: var(--text-muted); }
.action-btns { display: flex; align-items: center; gap: 6px; justify-content: center; }
.act-btn { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 3px; border: 1px solid; font-size: 12px; font-weight: 500; font-family: var(--font-body); cursor: pointer; transition: var(--transition); }
.act-warn    { color: var(--warning); border-color: rgba(234,179,8,.3);  background: rgba(234,179,8,.08); }
.act-success { color: var(--success); border-color: rgba(34,197,94,.3);  background: rgba(34,197,94,.08); }
.act-danger  { color: var(--danger);  border-color: rgba(239,68,68,.3);  background: rgba(239,68,68,.08); }
.act-warn:hover    { background: rgba(234,179,8,.18); }
.act-success:hover { background: rgba(34,197,94,.18); }
.act-danger:hover  { background: rgba(239,68,68,.18); }
.pagination-bar { display: flex; justify-content: flex-end; padding: 12px 16px; background: var(--bg-panel); border: 1px solid var(--border); border-top: none; border-radius: 0 0 var(--radius-lg) var(--radius-lg); }
</style>
