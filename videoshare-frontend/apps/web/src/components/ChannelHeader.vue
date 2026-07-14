<template>
  <div class="channel-header">
    <div class="channel-banner" />
    <div class="channel-info">
      <div class="channel-avatar">
        {{ profile.nickName?.charAt(0).toUpperCase() }}
      </div>
      <div class="channel-details">
        <h1 class="channel-name">{{ profile.nickName }}</h1>
        <p class="channel-meta">
          <span>{{ profile.nickName }}</span>
          <span class="dot">·</span>
          <span>{{ formatViews(profile.followerCount) }} 位关注者</span>
        </p>
      </div>
      <div class="channel-actions">
        <template v-if="isSelf">
          <el-button size="small" @click="$emit('edit')">编辑资料</el-button>
        </template>
        <template v-else-if="userStore.isLoggedIn">
          <el-button
            :type="isFollowing ? 'default' : 'danger'"
            size="small"
            @click="$emit('follow')"
          >
            {{ isFollowing ? '已关注' : '关注' }}
          </el-button>
        </template>
        <template v-else>
          <RouterLink to="/login">
            <el-button type="danger" size="small">关注</el-button>
          </RouterLink>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { formatViews } from '@videoshare/utils/format'

const userStore = useUserStore()
defineProps({
  profile:     { type: Object, required: true },
  isSelf:      { type: Boolean, default: false },
  isFollowing: { type: Boolean, default: false }
})
defineEmits(['follow', 'edit'])
</script>

<style scoped>
.channel-header { margin-bottom: 24px; }
.channel-banner {
  height: 140px; border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  opacity: 0.6; margin-bottom: 16px;
}
.channel-info { display: flex; align-items: center; gap: 20px; }
.channel-avatar {
  width: 80px; height: 80px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent), #7c3aed);
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 32px; color: #fff; flex-shrink: 0;
}
.channel-details { flex: 1; }
.channel-name { font-size: 22px; font-weight: 700; margin-bottom: 4px; }
.channel-meta { font-size: 13px; color: var(--text-2); }
.dot { margin: 0 4px; }
.channel-actions { flex-shrink: 0; }
</style>
