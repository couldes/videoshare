<template>
  <DefaultLayout>
    <div class="notif-page">
      <div class="notif-page-header">
        <h2>通知</h2>
        <button v-if="notifs.length && hasUnread" class="read-all-btn" @click="handleMarkAllRead">
          全部已读
        </button>
      </div>

      <div class="notif-page-list" v-loading="loading">
        <div v-if="notifs.length">
          <div v-for="n in notifs" :key="n.notificationId" class="notif-page-item"
            :class="{ unread: !n.isRead }" @click="handleClick(n)">
            <div class="notif-page-icon">
              <span v-if="n.type === 1">💬</span>
              <span v-else-if="n.type === 2">👍</span>
              <span v-else-if="n.type === 3">👤</span>
              <span v-else>📢</span>
            </div>
            <div class="notif-page-body">
              <div class="notif-page-text">
                <span v-if="!n.isRead" class="notif-page-dot" />
                {{ n.content }}
              </div>
              <div class="notif-page-time">{{ formatRelative(n.createTime) }}</div>
            </div>
          </div>

          <div v-if="hasMore" class="load-more">
            <el-button text :loading="loadingMore" @click="loadMore">加载更多</el-button>
          </div>
        </div>
        <div v-else-if="!loading" class="empty-state">
          <p>暂无通知</p>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { notificationApi } from '@/api'
import { formatRelative } from '@videoshare/utils/format'

const router = useRouter()

const notifs      = ref([])
const loading     = ref(false)
const loadingMore = ref(false)
const pageNum     = ref(1)
const totalPages  = ref(0)

const hasMore   = computed(() => pageNum.value < totalPages.value)
const hasUnread = computed(() => notifs.value.some(n => !n.isRead))

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const result = await notificationApi.getNotificationList({ pageNum: 1, pageSize: 20 })
    notifs.value = result.list || []
    totalPages.value = result.pages || 0
    pageNum.value = 1
  } catch {
    ElMessage.error('通知加载失败')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value) return
  loadingMore.value = true
  try {
    const next = pageNum.value + 1
    const result = await notificationApi.getNotificationList({ pageNum: next, pageSize: 20 })
    notifs.value.push(...(result.list || []))
    totalPages.value = result.pages || 0
    pageNum.value = next
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loadingMore.value = false
  }
}

async function handleClick(notif) {
  try {
    if (!notif.isRead) {
      await notificationApi.markRead(notif.notificationId)
      notif.isRead = true
    }
  } catch {}
  if (notif.targetId) {
    if (notif.type === 3) {
      router.push(`/user/${notif.targetId}`)
    } else {
      router.push(`/video/${notif.targetId}`)
    }
  }
}

async function handleMarkAllRead() {
  try {
    await notificationApi.markAllRead()
    notifs.value.forEach(n => { n.isRead = true })
    ElMessage.success('全部已读')
  } catch {}
}
</script>

<style scoped>
.notif-page { max-width: 720px; margin: 0 auto; padding-top: 16px; }
.notif-page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.notif-page-header h2 { font-size: 20px; font-weight: 700; }

.read-all-btn {
  padding: 6px 14px; border-radius: 20px; border: 1px solid var(--border);
  background: var(--bg-hover); color: var(--text-2); font-size: 13px;
  cursor: pointer; font-family: var(--font-body); transition: var(--transition);
}
.read-all-btn:hover { background: #3a3a3a; color: var(--text-1); }

.notif-page-item {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 14px 16px; cursor: pointer; transition: var(--transition);
  border-bottom: 1px solid var(--border);
}
.notif-page-item:hover { background: var(--bg-hover); }
.notif-page-item.unread { background: rgba(59,130,246,0.05); }

.notif-page-icon { font-size: 20px; flex-shrink: 0; margin-top: 2px; }
.notif-page-body { flex: 1; min-width: 0; }
.notif-page-text { font-size: 14px; color: var(--text-1); line-height: 1.6; display: flex; align-items: flex-start; gap: 8px; }
.notif-page-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #3b82f6; flex-shrink: 0; margin-top: 6px;
}
.notif-page-time { font-size: 12px; color: var(--text-muted); margin-top: 6px; }

.load-more { text-align: center; padding: 20px 0; }
.empty-state { display: flex; justify-content: center; padding: 60px 0; color: var(--text-muted); font-size: 14px; }
</style>
