<template>
  <div class="notif-bell" v-if="userStore.isLoggedIn">
    <el-popover placement="bottom-end" :width="360" trigger="click"
      popper-class="notif-popover" @show="loadRecent" @hide="visible = false">
      <template #reference>
        <button class="bell-btn" @click="visible = !visible">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
            <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
          </svg>
          <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </button>
      </template>

      <div class="notif-panel">
        <div class="notif-panel-header">
          <span class="notif-panel-title">通知</span>
        </div>
        <div v-if="recentNotifs.length" class="notif-list">
          <div v-for="n in recentNotifs" :key="n.notificationId" class="notif-item"
            :class="{ unread: !n.isRead }" @click="handleClick(n)">
            <div class="notif-icon">
              <span v-if="n.type === 1">💬</span>
              <span v-else-if="n.type === 2">👍</span>
              <span v-else-if="n.type === 3">👤</span>
              <span v-else>📢</span>
            </div>
            <div class="notif-body">
              <div class="notif-text">{{ n.content }}</div>
              <div class="notif-time">{{ formatRelative(n.createTime) }}</div>
            </div>
            <div v-if="!n.isRead" class="notif-dot" />
          </div>
        </div>
        <div v-else class="notif-empty">暂无通知</div>
        <div class="notif-panel-footer">
          <RouterLink to="/notifications" class="notif-all-link" @click="visible = false">查看全部</RouterLink>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { notificationApi } from '@/api'
import { formatRelative } from '@videoshare/utils/format'

const router    = useRouter()
const userStore = useUserStore()

const unreadCount   = ref(0)
const recentNotifs  = ref([])
const visible       = ref(false)

let pollTimer = null

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchUnreadCount()
    pollTimer = setInterval(fetchUnreadCount, 30000)
  }
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

async function fetchUnreadCount() {
  try {
    const count = await notificationApi.getUnreadCount()
    unreadCount.value = count ?? 0
  } catch {}
}

async function loadRecent() {
  try {
    const result = await notificationApi.getNotificationList({ pageNum: 1, pageSize: 5 })
    recentNotifs.value = result.list || []
  } catch {}
}

async function handleClick(notif) {
  try {
    if (!notif.isRead) {
      await notificationApi.markRead(notif.notificationId)
      notif.isRead = true
      if (unreadCount.value > 0) unreadCount.value--
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
</script>

<style scoped>
.bell-btn {
  position: relative;
  width: 40px; height: 40px; border-radius: 50%;
  border: none; background: none;
  color: var(--text-1); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: var(--transition);
}
.bell-btn:hover { background: var(--bg-hover); }

.badge {
  position: absolute; top: 2px; right: 2px;
  min-width: 16px; height: 16px; padding: 0 4px;
  border-radius: 8px; background: #ef4444;
  color: #fff; font-size: 10px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  line-height: 1;
}
</style>

<style>
.notif-popover {
  padding: 0 !important;
  background: var(--bg-surface) !important;
  border: 1px solid var(--border) !important;
  border-radius: var(--radius-md) !important;
}
.notif-panel { min-width: 0; }
.notif-panel-header {
  padding: 12px 16px; border-bottom: 1px solid var(--border);
}
.notif-panel-title { font-size: 15px; font-weight: 600; color: var(--text-1); }
.notif-list { max-height: 320px; overflow-y: auto; }
.notif-item {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 10px 16px; cursor: pointer; transition: var(--transition);
  border-bottom: 1px solid var(--border);
}
.notif-item:hover { background: var(--bg-hover); }
.notif-item.unread { background: rgba(59,130,246,0.06); }
.notif-icon { font-size: 18px; flex-shrink: 0; margin-top: 2px; }
.notif-body { flex: 1; min-width: 0; }
.notif-text { font-size: 13px; color: var(--text-1); line-height: 1.5; word-break: break-word; }
.notif-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; }
.notif-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #3b82f6; flex-shrink: 0; margin-top: 6px;
}
.notif-empty {
  padding: 40px 16px; text-align: center;
  color: var(--text-muted); font-size: 13px;
}
.notif-panel-footer {
  padding: 10px 16px; border-top: 1px solid var(--border);
  text-align: center;
}
.notif-all-link {
  font-size: 13px; font-weight: 600; color: var(--color-accent);
  text-decoration: none;
}
.notif-all-link:hover { text-decoration: underline; }
</style>
