<template>
  <div class="user-manage">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-sub mono">USER MANAGEMENT / 共 {{ pagination.total }} 条记录</p>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索邮箱 / 昵称..."
          clearable style="width:240px" :prefix-icon="Search"
          @keydown.enter="handleSearch" @clear="handleSearch"/>

        <!-- ★ 选项从 USER_STATUS_MAP 动态生成，不硬编码 -->
        <el-select v-model="query.status" placeholder="账号状态"
          clearable style="width:130px" @change="handleSearch">
          <el-option label="全部状态" :value="null"/>
          <el-option v-for="(label, val) in USER_STATUS_MAP"
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
      <el-table v-loading="loading" :data="userList"
        element-loading-background="rgba(10,10,12,0.7)"
        element-loading-text="加载中..." style="width:100%" row-key="userId">

        <el-table-column label="USER ID" width="130">
          <template #default="{ row }">
            <span class="mono" style="font-size:12px;color:var(--text-muted)">{{ row.userId }}</span>
          </template>
        </el-table-column>

        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar">{{ row.nickName?.charAt(0).toUpperCase() }}</div>
              <div class="user-meta">
                <div class="user-name">{{ row.nickName }}</div>
                <div class="user-email mono">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="性别" width="80" align="center">
          <template #default="{ row }">
            <!-- sexDesc 由后端 AdminUserServiceImpl.convertToVO() 填充 -->
            <span style="font-size:12px;color:var(--text-muted)">{{ row.sexDesc }}</span>
          </template>
        </el-table-column>

        <el-table-column label="硬币" width="110" align="right">
          <template #default="{ row }">
            <span class="mono" style="color:var(--warning);font-size:13px">{{ row.currentCoinCount }}</span>
            <span style="font-size:11px;color:var(--text-muted)"> / {{ row.totalCoinCount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">
            <!-- ★ formatDate 来自 shared，格式统一 -->
            <span class="mono" style="font-size:12px;color:var(--text-muted)">
              {{ formatDate(row.joinTime) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <!-- ★ tag type 和文字全部来自共享常量，不硬编码 -->
            <el-tag :type="USER_STATUS_TAG[row.status]" size="small" effect="dark">
              {{ USER_STATUS_MAP[row.status]?.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-tooltip
                :content="row.status === USER_STATUS.ENABLE ? '禁用该用户' : '启用该用户'"
                placement="top">
                <button class="act-btn"
                  :class="row.status === USER_STATUS.ENABLE ? 'act-warn' : 'act-success'"
                  @click="handleToggleStatus(row)">
                  <el-icon>
                    <component :is="row.status === USER_STATUS.ENABLE ? 'VideoCamera' : 'CircleCheck'"/>
                  </el-icon>
                  <!-- ★ 按钮文字也从共享常量取，显示切换后的状态名 -->
                  {{ USER_STATUS_MAP[row.status === USER_STATUS.ENABLE ? USER_STATUS.DISABLE : USER_STATUS.ENABLE] }}
                </button>
              </el-tooltip>
              <el-tooltip content="删除用户（不可恢复）" placement="top">
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
        @current-change="fetchUsers"
        @size-change="handleSearch"/>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { adminUserApi } from '@/api'
// ★ 所有业务常量从 shared 引入，消除硬编码
import { USER_STATUS, USER_STATUS_MAP, USER_STATUS_TAG, PAGE_DEFAULTS } from '@videoshare/constants'
import { formatDate } from '@videoshare/utils/format'

const loading  = ref(false)
const userList = ref([])

// ★ PAGE_DEFAULTS 来自 shared，不再手写 pageNum:1, pageSize:15
const query = reactive({
  pageNum:  PAGE_DEFAULTS.PAGE_NUM,
  pageSize: PAGE_DEFAULTS.PAGE_SIZE,
  keyword:  '',
  status:   null
})

const pagination = reactive({ total: 0, pages: 0 })

onMounted(fetchUsers)

// GET /admin/user/list → UserManageController.getUserList()
async function fetchUsers() {
  loading.value = true
  try {
    const result = await adminUserApi.getUserList({
      pageNum:  query.pageNum,
      pageSize: query.pageSize,
      keyword:  query.keyword || undefined,
      status:   query.status != null ? query.status : undefined
    })
    userList.value   = result.list
    pagination.total = result.total
    pagination.pages = result.pages
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = PAGE_DEFAULTS.PAGE_NUM
  fetchUsers()
}

function handleReset() {
  query.keyword  = ''
  query.status   = null
  query.pageNum  = PAGE_DEFAULTS.PAGE_NUM
  query.pageSize = PAGE_DEFAULTS.PAGE_SIZE
  fetchUsers()
}

// POST /admin/user/updateStatus → UserManageController.updateUserStatus()
async function handleToggleStatus(row) {
  // ★ 使用 USER_STATUS 枚举，不用魔法数字 0/1
  const newStatus = row.status === USER_STATUS.ENABLE
    ? USER_STATUS.DISABLE
    : USER_STATUS.ENABLE
  const action = USER_STATUS_MAP[newStatus]

  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.nickName}"（${row.email}）吗？`,
      `${action}确认`,
      { confirmButtonText: `确定${action}`, cancelButtonText: '取消',
        type: newStatus === USER_STATUS.DISABLE ? 'warning' : 'info' }
    )
    await adminUserApi.updateUserStatus({ userId: row.userId, status: newStatus })
    ElMessage.success(`已${action}用户 ${row.nickName}`)
    row.status = newStatus  // 局部更新，不重新请求
  } catch (err) {
    if (err === 'cancel') return
  }
}

// DELETE /admin/user/delete/{userId} → UserManageController.deleteUser()
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `此操作将永久删除用户 "${row.nickName}"，且无法恢复！`,
      '高危操作确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
    await adminUserApi.deleteUser(row.userId)
    ElMessage.success(`已删除用户 ${row.nickName}`)
    const idx = userList.value.findIndex(u => u.userId === row.userId)
    if (idx !== -1) { userList.value.splice(idx, 1); pagination.total-- }
  } catch (err) {
    if (err === 'cancel') return
  }
}
</script>

<style scoped>
.user-manage { max-width: 1200px; }
.page-header { margin-bottom: 20px; }
.page-title  { font-size: 22px; font-weight: 700; letter-spacing: -.02em; margin-bottom: 4px; }
.page-sub    { font-size: 11px; color: var(--text-muted); letter-spacing: .08em; }
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 14px 16px; background: var(--bg-panel); border: 1px solid var(--border); border-radius: var(--radius-lg) var(--radius-lg) 0 0; flex-wrap: wrap; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.count-tag { font-size: 11px; color: var(--text-muted); letter-spacing: .06em; padding: 4px 10px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 3px; }
.table-wrap { background: var(--bg-panel); border: 1px solid var(--border); border-top: none; }
.user-cell  { display: flex; align-items: center; gap: 10px; }
.user-avatar { width: 30px; height: 30px; border-radius: var(--radius-sm); background: linear-gradient(135deg, var(--color-accent), #7c3aed); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; color: #fff; flex-shrink: 0; }
.user-name   { font-size: 13px; font-weight: 600; color: var(--text-1); }
.user-email  { font-size: 11px; color: var(--text-muted); margin-top: 1px; }
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
