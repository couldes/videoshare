<template>
  <div class="video-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="page-body">
      <!-- 左侧主区域 -->
      <div class="main-col">

        <!-- 视频播放器 -->
        <div class="player-wrap">
          <video
            v-if="video"
            ref="playerRef"
            class="player"
            :src="video.videoUrl"
            controls
            preload="metadata"
            @play="onPlay"
            @ended="onEnded"
          >
            你的浏览器不支持 HTML5 视频播放。
          </video>
          <div v-else class="player-skeleton">
            <el-icon class="skeleton-icon"><Loading /></el-icon>
          </div>
        </div>

        <!-- 视频信息区 -->
        <div v-if="video" class="video-info-area">
          <h1 class="video-title">{{ video.title }}</h1>

          <div class="video-meta-row">
            <!-- 播放量和时间 -->
            <div class="meta-stats">
              <span>{{ formatViews(video.viewCount) }} 次观看</span>
              <span class="dot">·</span>
              <span>{{ formatRelative(video.createTime) }}</span>
            </div>

            <!-- 操作按钮组 -->
            <div class="action-group">
              <!-- 点赞 -->
              <button class="action-btn" :class="{ active: userAction.liked }" @click="handleLike">
                <el-icon><CaretTop /></el-icon>
                <span>{{ formatViews(video.likeCount) }}</span>
              </button>

              <!-- 收藏 -->
              <button class="action-btn" :class="{ active: userAction.favorited }" @click="handleFavorite">
                <el-icon><Star /></el-icon>
                <span>{{ userAction.favorited ? '已收藏' : '收藏' }}</span>
              </button>

              <!-- 分享 -->
              <button class="action-btn" @click="handleShare">
                <el-icon><Share /></el-icon>
                <span>分享</span>
              </button>
            </div>
          </div>

          <!-- 分割线 -->
          <div class="divider"/>

          <!-- 发布者信息 -->
          <div class="author-row">
            <RouterLink :to="`/user/${video.userInfo?.userId}`" class="author-link">
              <div class="author-avatar">
                {{ video.userInfo?.nickName?.charAt(0).toUpperCase() }}
              </div>
              <div class="author-info">
                <div class="author-name">{{ video.userInfo?.nickName }}</div>
                <div class="author-fans">{{ video.userInfo?.followerCount ?? 0 }} 位关注者</div>
              </div>
            </RouterLink>

            <!-- 关注按钮（登录后且非本人显示） -->
            <button
              v-if="userStore.isLoggedIn && video.userInfo?.userId !== userStore.userInfo?.userId"
              class="follow-btn"
              :class="{ following: isFollowing }"
              @click="handleFollow"
            >
              {{ isFollowing ? '已关注' : '+ 关注' }}
            </button>
          </div>

          <!-- 视频描述（可展开） -->
          <div class="video-desc" v-if="video.description">
            <p :class="{ collapsed: !descExpanded }">{{ video.description }}</p>
            <button v-if="video.description?.length > 100" class="desc-toggle"
              @click="descExpanded = !descExpanded">
              {{ descExpanded ? '收起' : '展开' }}
            </button>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <div class="comment-header">
            <h3 class="comment-title">
              评论 <span class="comment-count">{{ video?.commentCount ?? 0 }}</span>
            </h3>
          </div>

          <!-- 发表评论（登录后显示）-->
          <div v-if="userStore.isLoggedIn" class="comment-input-row">
            <div class="ci-avatar">{{ userStore.nickName.charAt(0).toUpperCase() }}</div>
            <div class="ci-box">
              <el-input
                v-model="commentText"
                type="textarea"
                :rows="2"
                placeholder="发表你的评论..."
                :maxlength="500"
                show-word-limit
                resize="none"
              />
              <div class="ci-actions">
                <el-button size="small" @click="commentText = ''">取消</el-button>
                <el-button type="primary" size="small" :loading="submitting" @click="submitComment">
                  发布
                </el-button>
              </div>
            </div>
          </div>
          <div v-else class="login-prompt">
            <RouterLink to="/login" class="link">登录</RouterLink> 后发表评论
          </div>

          <!-- 评论列表 -->
          <div class="comment-list" v-loading="commentsLoading">
            <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
              <!-- 顶级评论 -->
              <div class="comment-main">
                <RouterLink :to="`/user/${comment.userId}`">
                  <div class="c-avatar">{{ comment.nickName?.charAt(0).toUpperCase() }}</div>
                </RouterLink>
                <div class="c-body">
                  <div class="c-name">{{ comment.nickName }}</div>
                  <div class="c-text">{{ comment.content }}</div>
                  <div class="c-footer">
                    <span class="c-time">{{ formatRelative(comment.createTime) }}</span>
                    <!-- 点赞评论 -->
                    <button class="c-like-btn" :class="{ active: comment.liked }" @click="likeComment(comment)">
                      <el-icon><CaretTop /></el-icon>
                      <span>{{ comment.likeCount || '' }}</span>
                    </button>
                    <!-- 回复 -->
                    <button class="c-reply-btn" @click="openReply(comment)">回复</button>
                    <!-- 删除（本人）-->
                    <button v-if="comment.userId === userStore.userInfo?.userId"
                      class="c-del-btn" @click="deleteComment(comment)">删除</button>
                  </div>

                  <!-- 回复输入框 -->
                  <div v-if="replyTo?.commentId === comment.commentId" class="reply-input">
                    <el-input v-model="replyText" :placeholder="`回复 @${comment.nickName}`"
                      size="small" style="flex:1" />
                    <el-button size="small" @click="replyTo = null">取消</el-button>
                    <el-button type="primary" size="small" @click="submitReply(comment)">回复</el-button>
                  </div>

                  <!-- 回复列表（最多显示3条）-->
                  <div v-if="comment.replies?.length" class="replies">
                    <div v-for="reply in comment.replies.slice(0, 3)" :key="reply.commentId" class="reply-item">
                      <div class="c-avatar c-avatar--sm">{{ reply.nickName?.charAt(0).toUpperCase() }}</div>
                      <div class="c-body">
                        <span class="c-name">{{ reply.nickName }}</span>
                        <span v-if="reply.replyNickName" class="reply-to">
                          回复 <span class="c-name">@{{ reply.replyNickName }}</span>
                        </span>
                        <span class="c-text"> {{ reply.content }}</span>
                        <div class="c-footer">
                          <span class="c-time">{{ formatRelative(reply.createTime) }}</span>
                        </div>
                      </div>
                    </div>
                    <button v-if="comment.replies.length > 3" class="more-replies">
                      展开 {{ comment.replies.length - 3 }} 条回复
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 加载更多 -->
            <div v-if="hasMoreComments" class="load-more-comments">
              <el-button text @click="loadMoreComments">加载更多评论</el-button>
            </div>
          </div>
        </div>

      </div>

      <!-- 右侧推荐视频 -->
      <aside class="side-col">
        <h4 class="side-title">推荐视频</h4>
        <div class="recommend-list">
          <RouterLink
            v-for="v in recommendVideos"
            :key="v.videoId"
            :to="`/video/${v.videoId}`"
            class="rec-item"
          >
            <div class="rec-thumb">
              <img :src="v.coverUrl" :alt="v.title" loading="lazy" />
              <span class="rec-duration">{{ v.duration }}</span>
            </div>
            <div class="rec-info">
              <p class="rec-title">{{ v.title }}</p>
              <p class="rec-channel">{{ v.channelName }}</p>
              <p class="rec-stats">{{ formatViews(v.viewCount) }} 次观看</p>
            </div>
          </RouterLink>
        </div>
      </aside>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { useUserStore } from '@/stores/user'
import { videoApi, commentApi, profileApi } from '@/api'
import { formatViews, formatRelative } from '@videoshare/utils/format'
import { addHistory } from '@videoshare/utils/history'

const route     = useRoute()
const router    = useRouter()
const userStore = useUserStore()
const playerRef = ref(null)

// ===== 视频数据 =====
const video    = ref(null)
const loading  = ref(false)

// ===== 用户操作状态 =====
const userAction = reactive({ liked: false, favorited: false })
const isFollowing = ref(false)
const descExpanded = ref(false)

// ===== 评论 =====
const comments       = ref([])
const commentsLoading= ref(false)
const commentText    = ref('')
const replyText      = ref('')
const replyTo        = ref(null)
const submitting     = ref(false)
const commentPageNum = ref(1)
const hasMoreComments= ref(false)

// ===== 推荐视频（模拟数据）=====
const recommendVideos = ref([])

onMounted(async () => {
  await loadVideo()
  await loadComments()
  loadRecommend()
})

// 路由切换时重新加载
watch(() => route.params.videoId, async () => {
  if (route.params.videoId) {
    video.value = null
    comments.value = []
    commentPageNum.value = 1
    await loadVideo()
    await loadComments()
  }
})

async function loadVideo() {
  loading.value = true
  try {
    const data = await videoApi.getVideoDetail(route.params.videoId)
    video.value = data

    // 查询当前用户的点赞/收藏状态
    if (userStore.isLoggedIn) {
      const action = await videoApi.checkAction(route.params.videoId)
      userAction.liked     = action.liked
      userAction.favorited = action.favorited

      // 是否已关注视频作者
      if (data.userInfo?.userId && data.userInfo.userId !== userStore.userInfo?.userId) {
        const followed = await profileApi.checkFollow(data.userInfo.userId)
        isFollowing.value = followed
      }
    }
  } catch { ElMessage.error('视频加载失败') }
  finally { loading.value = false }
}

async function loadComments(append = false) {
  commentsLoading.value = true
  try {
    const result = await commentApi.getCommentList(route.params.videoId, {
      pageNum: commentPageNum.value, pageSize: 20
    })
    if (append) comments.value.push(...result.list)
    else        comments.value = result.list
    hasMoreComments.value = commentPageNum.value < result.pages
  } finally {
    commentsLoading.value = false
  }
}

function loadMoreComments() {
  commentPageNum.value++
  loadComments(true)
}

function loadRecommend() {
  // 实际项目调用 videoApi.getVideoList({ pageSize: 10 })
  recommendVideos.value = Array.from({ length: 8 }, (_, i) => ({
    videoId:     `rec_${i}`,
    title:       ['Vue 3 完整入门', 'Spring Boot 实战', 'Redis 缓存设计', 'Docker 部署', 'CSS 动画进阶', 'Java 并发编程', 'MySQL 优化实战', 'TypeScript 高级'][i],
    coverUrl:    `https://picsum.photos/seed/${i + 50}/320/180`,
    duration:    ['8:24', '15:03', '22:47', '6:31', '11:15', '31:08', '9:55', '18:22'][i],
    channelName: ['极客出发', 'CodeWithMe', '技术老狗', '前端匠人'][i % 4],
    viewCount:   Math.floor(Math.random() * 200000)
  }))
}

// ===== 交互处理 =====
function onPlay() {
  if (video.value) addHistory(video.value)
}
function onEnded() {
  // 播放结束后自动播放推荐
}

async function handleLike() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try {
    const active = await videoApi.doAction({ videoId: route.params.videoId, actionType: 1 })
    userAction.liked = active
    video.value.likeCount += active ? 1 : -1
    ElMessage.success(active ? '点赞成功' : '已取消点赞')
  } catch {}
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try {
    const active = await videoApi.doAction({ videoId: route.params.videoId, actionType: 2 })
    userAction.favorited = active
    video.value.favoriteCount += active ? 1 : -1
    ElMessage.success(active ? '收藏成功' : '已取消收藏')
  } catch {}
}

function handleShare() {
  navigator.clipboard?.writeText(window.location.href)
  ElMessage.success('链接已复制到剪贴板')
}

async function handleFollow() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try {
    const active = await profileApi.toggleFollow(video.value.userInfo.userId)
    isFollowing.value = active
    ElMessage.success(active ? '关注成功' : '已取消关注')
  } catch {}
}

async function submitComment() {
  if (!commentText.value.trim()) return
  submitting.value = true
  try {
    const newComment = await commentApi.postComment({
      videoId: route.params.videoId,
      content: commentText.value
    })
    comments.value.unshift({ ...newComment, replies: [] })
    video.value.commentCount++
    commentText.value = ''
    ElMessage.success('评论发布成功')
  } catch {}
  finally { submitting.value = false }
}

function openReply(comment) {
  replyTo.value = comment
  replyText.value = ''
}

async function submitReply(parentComment) {
  if (!replyText.value.trim()) return
  try {
    const newReply = await commentApi.postComment({
      videoId:     route.params.videoId,
      content:     replyText.value,
      pCommentId:  parentComment.commentId,
      replyUserId: parentComment.userId
    })
    if (!parentComment.replies) parentComment.replies = []
    parentComment.replies.push(newReply)
    replyTo.value  = null
    replyText.value = ''
    ElMessage.success('回复成功')
  } catch {}
}

async function likeComment(comment) {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try {
    const active = await commentApi.likeComment(comment.commentId)
    comment.liked = active
    comment.likeCount = (comment.likeCount || 0) + (active ? 1 : -1)
  } catch {}
}

async function deleteComment(comment) {
  try {
    await commentApi.deleteComment(comment.commentId)
    const idx = comments.value.findIndex(c => c.commentId === comment.commentId)
    if (idx !== -1) { comments.value.splice(idx, 1); video.value.commentCount-- }
    ElMessage.success('评论已删除')
  } catch {}
}
</script>

<style scoped>
.video-page { min-height: 100vh; background: var(--bg-base); }
.page-body  { display: flex; gap: 24px; max-width: 1400px; margin: 0 auto; padding: 72px 20px 40px; }

/* 播放器 */
.main-col { flex: 1; min-width: 0; }
.player-wrap { width: 100%; background: #000; border-radius: var(--radius-md); overflow: hidden; aspect-ratio: 16/9; }
.player { width: 100%; height: 100%; display: block; }
.player-skeleton { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: var(--bg-card); }
.skeleton-icon { font-size: 40px; color: var(--text-muted); animation: rotate 1s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }

/* 视频信息 */
.video-info-area { padding: 16px 0; }
.video-title { font-size: 18px; font-weight: 700; letter-spacing: -.02em; margin-bottom: 12px; }
.video-meta-row { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.meta-stats { font-size: 13px; color: var(--text-2); display: flex; align-items: center; gap: 6px; }
.dot { color: var(--text-muted); }
.action-group { display: flex; gap: 8px; }
.action-btn { display: flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 100px; border: 1px solid var(--border); background: var(--bg-card); color: var(--text-2); font-size: 13px; font-weight: 500; cursor: pointer; transition: var(--transition); font-family: var(--font-body); }
.action-btn:hover { background: var(--bg-hover); color: var(--text-1); border-color: var(--border-strong); }
.action-btn.active { background: var(--color-accent-dim); color: var(--color-accent); border-color: var(--color-accent); }

.divider { height: 1px; background: var(--border); margin: 16px 0; }

/* 作者 */
.author-row { display: flex; align-items: center; justify-content: space-between; }
.author-link { display: flex; align-items: center; gap: 12px; text-decoration: none; }
.author-avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, var(--color-accent), #7c3aed); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 15px; color: #fff; flex-shrink: 0; }
.author-name { font-size: 14px; font-weight: 600; color: var(--text-1); }
.author-fans { font-size: 12px; color: var(--text-2); margin-top: 2px; }
.follow-btn { padding: 7px 18px; border-radius: 100px; border: none; font-size: 13px; font-weight: 600; cursor: pointer; transition: var(--transition); font-family: var(--font-body); background: var(--color-accent); color: #fff; }
.follow-btn.following { background: var(--bg-hover); color: var(--text-2); border: 1px solid var(--border); }
.follow-btn:hover { filter: brightness(1.1); }

/* 描述 */
.video-desc { margin-top: 14px; }
.video-desc p { font-size: 13px; color: var(--text-2); line-height: 1.7; white-space: pre-wrap; }
.video-desc p.collapsed { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.desc-toggle { background: none; border: none; color: var(--color-accent); font-size: 13px; cursor: pointer; padding: 4px 0; font-family: var(--font-body); }

/* 评论区 */
.comment-section { margin-top: 24px; }
.comment-header { margin-bottom: 20px; }
.comment-title { font-size: 16px; font-weight: 600; }
.comment-count { color: var(--text-2); font-weight: 400; margin-left: 6px; }

.comment-input-row { display: flex; gap: 12px; margin-bottom: 24px; }
.ci-avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, var(--color-accent), #7c3aed); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 13px; color: #fff; flex-shrink: 0; margin-top: 4px; }
.ci-box { flex: 1; }
.ci-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }

.login-prompt { font-size: 13px; color: var(--text-2); padding: 16px 0; }
.link { color: var(--color-accent); text-decoration: none; font-weight: 600; }

.comment-list { display: flex; flex-direction: column; gap: 0; }
.comment-item { padding: 16px 0; border-bottom: 1px solid var(--border); }
.comment-main { display: flex; gap: 12px; }
.c-avatar { width: 34px; height: 34px; border-radius: 50%; background: var(--bg-hover); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; color: var(--text-2); flex-shrink: 0; }
.c-avatar--sm { width: 26px; height: 26px; font-size: 10px; }
.c-body { flex: 1; }
.c-name  { font-size: 13px; font-weight: 600; color: var(--text-1); margin-bottom: 4px; }
.c-text  { font-size: 14px; color: var(--text-1); line-height: 1.6; }
.c-footer { display: flex; align-items: center; gap: 14px; margin-top: 8px; }
.c-time   { font-size: 12px; color: var(--text-muted); }
.c-like-btn, .c-reply-btn, .c-del-btn { background: none; border: none; font-size: 12px; cursor: pointer; font-family: var(--font-body); color: var(--text-2); display: flex; align-items: center; gap: 4px; padding: 2px 0; transition: var(--transition); }
.c-like-btn:hover, .c-reply-btn:hover { color: var(--text-1); }
.c-like-btn.active { color: var(--color-accent); }
.c-del-btn:hover { color: var(--danger); }
.reply-input { display: flex; gap: 8px; align-items: center; margin-top: 10px; }
.replies { margin-top: 12px; display: flex; flex-direction: column; gap: 10px; }
.reply-item { display: flex; gap: 8px; }
.reply-to { color: var(--text-muted); font-size: 13px; }
.more-replies { background: none; border: none; color: var(--color-accent); font-size: 13px; cursor: pointer; font-family: var(--font-body); padding: 4px 0; }
.load-more-comments { text-align: center; padding: 16px 0; }

/* 右侧推荐 */
.side-col { width: 380px; flex-shrink: 0; }
.side-title { font-size: 14px; font-weight: 600; margin-bottom: 14px; color: var(--text-2); }
.recommend-list { display: flex; flex-direction: column; gap: 12px; }
.rec-item { display: flex; gap: 10px; text-decoration: none; border-radius: var(--radius-md); padding: 6px; transition: var(--transition); }
.rec-item:hover { background: var(--bg-hover); }
.rec-thumb { position: relative; width: 160px; height: 90px; flex-shrink: 0; border-radius: var(--radius-sm); overflow: hidden; background: var(--bg-card); }
.rec-thumb img { width: 100%; height: 100%; object-fit: cover; }
.rec-duration { position: absolute; bottom: 4px; right: 4px; background: rgba(0,0,0,.8); color: #fff; font-size: 10px; padding: 1px 5px; border-radius: 3px; }
.rec-info { flex: 1; min-width: 0; }
.rec-title   { font-size: 13px; font-weight: 600; color: var(--text-1); line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 4px; }
.rec-channel { font-size: 12px; color: var(--text-2); margin-bottom: 2px; }
.rec-stats   { font-size: 11px; color: var(--text-muted); }

@media (max-width: 1100px) { .side-col { display: none; } }
@media (max-width: 768px)  { .page-body { padding-top: 64px; } .video-title { font-size: 16px; } }
</style>
