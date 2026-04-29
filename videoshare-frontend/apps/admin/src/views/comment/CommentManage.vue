<template>
  <div class="comment-manage">
    <div class="page-header">
      <div>
        <h1 class="page-title">评论管理</h1>
        <p class="page-sub mono">COMMENT MANAGEMENT / 共 {{ pagination.total }} 条记录</p>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.videoId" placeholder="按 Video ID 筛选..."
          clearable style="width:200px" :prefix-icon="Search"
          @keydown.enter="handleSearch" @clear="handleSearch"/>

        <el-select v-model="query.status" placeholder="评论状态"
          clearable style="width:130px" @change="handleSearch">
          <el-option label="全部状态" :value="null"/>
          <el-option v-for="(label, val) in COMMENT_STATUS_MAP"
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
      <el-table v-loading="loading" :data="commentList"
        element-loading-background="rgba(10,10,12,0.7)"
        element-loading-text="加载中..." style="width:100%" row-key="commentId">

        <el-table-column label="COMMENT ID" width="100">
          <template #default="{ row }">
            <span class="mono" style="font-size:11px;color:var(--text-muted)">{{ row.commentId }}</span>
          </template>
        </el-table-column>

        <el-table-column label="评论内容" min-width="300">
          <template #default="{ row }">
            <div class="comment-cell">
              <div class="cc-content">{{ row.content }}</div>
              <div class="cc-meta mono">
                <span>{{ row.userId }}</span>
                <span class="cc-dot">→</span>
                <span>VIDEO: {{ row.videoId }}</span>
                <span v-if="row.pCommentId && row.pCommentId !== 0" class="cc-dot">·</span>
                <span v-if="row.pCommentId && row.pCommentId !== 0" style="color:var(--color-accent)">
                  回复: #{{ row.pCommentId }}
                </span>
              </div>
            </div>
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
            <el-tag :type="COMMENT_STATUS_TAG[row.status]" size="small" effect="dark">
              {{ COMMENT_STATUS_MAP[row.status]?.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-tooltip
                :content="row.status === COMMENT_STATUS.APPROVED ? '驳回该评论' : '通过该评论'"
                placement="top">
                <button class="act-btn"
                  :class="row.status === COMMENT_STATUS.APPROVED ? 'act-warn' : 'act-success'"
                  @click="handleToggleStatus(row)">
                  <el-icon>
                    <component :is="row.status === COMMENT_STATUS.APPROVED ? 'CircleClose' : 'CircleCheck'"/>
                  </el-icon>
                  {{ row.status === COMMENT_STATUS.APPROVED ? '驳回' : '通过' }}
                </button>
              </el-tooltip>
              <el-tooltip content="删除评论（不可恢复）" placement="top">
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
        @current-change="fetchComments"
        @size-change="handleSearch"/>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { adminCommentApi } from '@/api'
import { COMMENT_STATUS, COMMENT_STATUS_MAP, COMMENT_STATUS_TAG, PAGE_DEFAULTS } from '@videoshare/constants'
import { formatDate } from '@videoshare/utils/format'

const loading  = ref(false)
const commentList = ref([])

const query = reactive({
  pageNum:  PAGE_DEFAULTS.PAGE_NUM,
  pageSize: PAGE_DEFAULTS.PAGE_SIZE,
  videoId:  '',
  status:   null
})

const pagination = reactive({ total: 0, pages: 0 })

onMounted(fetchComments)

async function fetchComments() {
  loading.value = true
  try {
    const result = await adminCommentApi.getCommentList({
      pageNum:  query.pageNum,
      pageSize: query.pageSize,
      videoId:  query.videoId || undefined,
      status:   query.status != null ? query.status : undefined
    })
    commentList.value = result.list
    pagination.total  = result.total
    pagination.pages  = result.pages
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = PAGE_DEFAULTS.PAGE_NUM
  fetchComments()
}

function handleReset() {
  query.videoId  = ''
  query.status   = null
  query.pageNum  = PAGE_DEFAULTS.PAGE_NUM
  query.pageSize = PAGE_DEFAULTS.PAGE_SIZE
  fetchComments()
}

async function handleToggleStatus(row) {
  const newStatus = row.status === COMMENT_STATUS.APPROVED
    ? COMMENT_STATUS.DELETED
    : COMMENT_STATUS.APPROVED
  const action = COMMENT_STATUS_MAP[newStatus]

  try {
    await ElMessageBox.confirm(
      `确定要${action}该评论吗？`,
      `${action}确认`,
      { confirmButtonText: `确定${action}`, cancelButtonText: '取消',
        type: newStatus === COMMENT_STATUS.DELETED ? 'warning' : 'info' }
    )
    await adminCommentApi.updateStatus({ commentId: row.commentId, status: newStatus })
    ElMessage.success(`已${action}评论 #${row.commentId}`)
    row.status = newStatus
  } catch (err) {
    if (err === 'cancel') return
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      '此操作将永久删除该评论，且无法恢复！',
      '高危操作确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
    await adminCommentApi.deleteComment(row.commentId)
    ElMessage.success(`已删除评论 #${row.commentId}`)
    const idx = commentList.value.findIndex(c => c.commentId === row.commentId)
    if (idx !== -1) { commentList.value.splice(idx, 1); pagination.total-- }
  } catch (err) {
    if (err === 'cancel') return
  }
}
</script>

<style scoped>
.comment-manage { max-width: 1300px; }
.page-header { margin-bottom: 20px; }
.page-title  { font-size: 22px; font-weight: 700; letter-spacing: -.02em; margin-bottom: 4px; }
.page-sub    { font-size: 11px; color: var(--text-muted); letter-spacing: .08em; }
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 14px 16px; background: var(--bg-panel); border: 1px solid var(--border); border-radius: var(--radius-lg) var(--radius-lg) 0 0; flex-wrap: wrap; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.count-tag { font-size: 11px; color: var(--text-muted); letter-spacing: .06em; padding: 4px 10px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 3px; }
.table-wrap { background: var(--bg-panel); border: 1px solid var(--border); border-top: none; }
.comment-cell { display: flex; flex-direction: column; gap: 4px; }
.cc-content { font-size: 13px; color: var(--text-1); line-height: 1.5; }
.cc-meta { font-size: 11px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; }
.cc-dot { color: var(--text-muted); }
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
