<template>
  <DefaultLayout>
    <div class="following-page">
      <div class="page-header">
        <RouterLink :to="`/user/${userId}`" class="back-link">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
          返回个人主页
        </RouterLink>
        <h1 class="page-title">{{ title }}</h1>
      </div>

      <div v-if="loading" class="loading-state" v-loading="true">加载中...</div>

      <div v-else-if="users.length" class="user-list">
        <div v-for="user in users" :key="user.userId" class="user-card">
          <RouterLink :to="`/user/${user.userId}`" class="user-avatar">
            <img v-if="user.avatarUrl" :src="user.avatarUrl" alt="" />
            <span v-else class="avatar-letter">{{ user.nickName?.charAt(0).toUpperCase() }}</span>
          </RouterLink>
          <div class="user-info">
            <RouterLink :to="`/user/${user.userId}`" class="user-name">{{ user.nickName }}</RouterLink>
            <p class="user-bio" v-if="user.bio">{{ user.bio }}</p>
          </div>
          <el-button v-if="userStore.isLoggedIn && user.userId !== userStore.userInfo?.userId"
            size="small" :type="user.isFollowing ? 'default' : 'danger'"
            @click="toggleFollow(user)">
            {{ user.isFollowing ? '已关注' : '关注' }}
          </el-button>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>{{ emptyText }}</p>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { profileApi } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const userId = computed(() => route.params.userId)
const users = ref([])
const loading = ref(false)

const isFollowingPage = computed(() => route.path.endsWith('/following'))
const title = computed(() => isFollowingPage.value ? '关注列表' : '粉丝列表')
const emptyText = computed(() => isFollowingPage.value ? '还没有关注任何人' : '还没有粉丝')

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const data = isFollowingPage.value
      ? await profileApi.getFollowingList(userId.value)
      : await profileApi.getFollowerList(userId.value)
    users.value = (data || []).map(u => ({ ...u, isFollowing: false }))
  } catch { users.value = [] }
  finally { loading.value = false }
}

async function toggleFollow(user) {
  try {
    user.isFollowing = await profileApi.toggleFollow(user.userId)
    ElMessage.success(user.isFollowing ? '关注成功' : '已取消关注')
  } catch { /* ignore */ }
}
</script>

<style scoped>
.following-page { max-width: 800px; margin: 0 auto; padding: 72px 20px 60px; }
.page-header { margin-bottom: 24px; }
.back-link {
  display: inline-flex; align-items: center; gap: 6px;
  color: var(--text-2); text-decoration: none; font-size: 13px;
  margin-bottom: 12px; transition: var(--transition);
}
.back-link:hover { color: var(--color-accent); }
.page-title { font-size: 24px; font-weight: 700; }

.user-list { display: flex; flex-direction: column; gap: 8px; }
.user-card {
  display: flex; align-items: center; gap: 14px;
  padding: 12px 16px; background: var(--bg-surface);
  border: 1px solid var(--border); border-radius: var(--radius-md);
  transition: var(--transition);
}
.user-card:hover { border-color: var(--border-strong); }

.user-avatar {
  width: 48px; height: 48px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 18px; color: #fff;
  overflow: hidden; text-decoration: none;
}
.user-avatar img { width: 100%; height: 100%; object-fit: cover; }

.user-info { flex: 1; min-width: 0; }
.user-name { font-size: 15px; font-weight: 600; color: var(--text-1); text-decoration: none; }
.user-name:hover { color: var(--color-accent); }
.user-bio { font-size: 13px; color: var(--text-2); margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.loading-state, .empty-state { text-align: center; padding: 60px 0; color: var(--text-muted); font-size: 14px; }
</style>
