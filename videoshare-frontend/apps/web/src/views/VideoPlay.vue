<template>
  <div class="video-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="page-body">
      <div class="main-col">
        <div class="player-wrap">
          <template v-if="video && video.status === 1">
            <video ref="playerRef" class="player" controls preload="metadata"
              @play="onPlay" @ended="onEnded">
              你的浏览器不支持 HTML5 视频播放。
            </video>
          </template>
          <div v-else-if="video && video.status === 0" class="player-placeholder">
            <div class="placeholder-spinner" />
            <p>视频转码中，请稍后再试</p>
          </div>
          <div v-else-if="video && video.status === 2" class="player-placeholder">
            <p>视频已下架</p>
          </div>
          <div v-else class="player-skeleton">加载中...</div>
        </div>

        <div v-if="video" class="video-info-area">
          <h1 class="video-title">{{ video.title }}</h1>

          <div class="video-meta-row">
            <div class="meta-stats">
              <span>{{ formatViews(video.viewCount) }} 次观看</span>
              <span class="dot">·</span>
              <span>{{ formatRelative(video.createTime) }}</span>
            </div>
            <div class="action-group">
              <button class="action-btn" :class="{ active: userAction.liked }" @click="handleLike">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"/></svg>
                <span>{{ formatViews(video.likeCount) }}</span>
              </button>
              <button class="action-btn" :class="{ active: userAction.favorited }" @click="handleFavorite">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
                <span>{{ userAction.favorited ? '已收藏' : '收藏' }}</span>
              </button>
              <button class="action-btn" :class="{ active: showSaveModal }" @click="handleSave">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M14 10H2v2h12v-2zm0-4H2v2h12V6zm4 8v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zM2 16h8v-2H2v2z"/></svg>
                <span>保存</span>
              </button>
              <button class="action-btn" @click="handleShare">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92 1.61 0 2.92-1.31 2.92-2.92s-1.31-2.92-2.92-2.92z"/></svg>
                <span>分享</span>
              </button>
            </div>
          </div>

          <div class="divider" />

          <div class="author-row">
            <RouterLink :to="`/user/${video.userInfo?.userId}`" class="author-link">
              <div class="author-avatar">{{ video.userInfo?.nickName?.charAt(0).toUpperCase() }}</div>
              <div class="author-info">
                <div class="author-name">{{ video.userInfo?.nickName }}</div>
                <div class="author-fans">{{ formatViews(video.userInfo?.followerCount ?? 0) }} 位关注者</div>
              </div>
            </RouterLink>
            <button v-if="userStore.isLoggedIn && video.userInfo?.userId !== userStore.userInfo?.userId"
              class="follow-btn" :class="{ following: isFollowing }" @click="handleFollow">
              {{ isFollowing ? '已关注' : '关注' }}
            </button>
          </div>

          <div class="video-desc" v-if="video.description">
            <p :class="{ collapsed: !descExpanded }">{{ video.description }}</p>
            <button v-if="video.description?.length > 100" class="desc-toggle"
              @click="descExpanded = !descExpanded">
              {{ descExpanded ? '收起' : '展开' }}
            </button>
          </div>
        </div>

        <!-- Comments -->
        <div class="comment-section" v-if="video">
          <div class="comment-header">
            <h3 class="comment-title">评论 <span class="comment-count">{{ video?.commentCount ?? 0 }}</span></h3>
          </div>

          <div v-if="userStore.isLoggedIn" class="comment-input-row">
            <div class="ci-avatar">{{ userStore.nickName.charAt(0).toUpperCase() }}</div>
            <div class="ci-box">
              <el-input v-model="commentText" type="textarea" :rows="2"
                placeholder="发表你的评论..." :maxlength="500" show-word-limit resize="none" />
              <div class="ci-actions">
                <el-button size="small" @click="commentText = ''">取消</el-button>
                <el-button type="primary" size="small" :loading="submitting" @click="submitComment">发布</el-button>
              </div>
            </div>
          </div>
          <div v-else class="login-prompt">
            <RouterLink to="/login" class="link">登录</RouterLink> 后发表评论
          </div>

          <div class="comment-list" v-loading="commentsLoading">
            <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
              <div class="comment-main">
                <RouterLink :to="`/user/${comment.userId}`">
                  <div class="c-avatar">{{ comment.nickName?.charAt(0).toUpperCase() }}</div>
                </RouterLink>
                <div class="c-body">
                  <div class="c-name">{{ comment.nickName }}</div>
                  <div class="c-text">{{ comment.content }}</div>
                  <div class="c-footer">
                    <span class="c-time">{{ formatRelative(comment.createTime) }}</span>
                    <button class="c-like-btn" :class="{ active: comment.liked }" @click="likeComment(comment)">
                      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"/></svg>
                      <span>{{ comment.likeCount || '' }}</span>
                    </button>
                    <button class="c-reply-btn" @click="openReply(comment)">回复</button>
                    <button v-if="comment.userId === userStore.userInfo?.userId" class="c-del-btn" @click="deleteComment(comment)">删除</button>
                  </div>
                  <div v-if="replyTo?.commentId === comment.commentId" class="reply-input">
                    <el-input v-model="replyText" :placeholder="`回复 @${comment.nickName}`" size="small" style="flex:1" />
                    <el-button size="small" @click="replyTo = null">取消</el-button>
                    <el-button type="primary" size="small" @click="submitReply(comment)">回复</el-button>
                  </div>
                  <div v-if="comment.replies?.length" class="replies">
                    <div v-for="reply in comment.replies.slice(0, 3)" :key="reply.commentId" class="reply-item">
                      <div class="c-avatar c-avatar--sm">{{ reply.nickName?.charAt(0).toUpperCase() }}</div>
                      <div class="c-body">
                        <span class="c-name">{{ reply.nickName }}</span>
                        <span v-if="reply.replyNickName" class="reply-to">回复 <span class="c-name">@{{ reply.replyNickName }}</span></span>
                        <span class="c-text"> {{ reply.content }}</span>
                        <div class="c-footer"><span class="c-time">{{ formatRelative(reply.createTime) }}</span></div>
                      </div>
                    </div>
                    <button v-if="comment.replies.length > 3" class="more-replies">展开 {{ comment.replies.length - 3 }} 条回复</button>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="hasMoreComments" class="load-more-comments">
              <el-button text @click="loadMoreComments">加载更多评论</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- Side: Recommendations -->
      <aside class="side-col">
        <h4 class="side-title">推荐视频</h4>
        <div class="recommend-list">
          <VideoListItem v-for="v in recommendVideos" :key="v.videoId" :video="v" />
        </div>
      </aside>
    </div>

    <SaveToPlaylistModal v-if="video" :video-id="video.videoId" :visible="showSaveModal" @close="showSaveModal = false" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import VideoListItem from '@/components/VideoListItem.vue'
import SaveToPlaylistModal from '@/components/SaveToPlaylistModal.vue'
import { useUserStore } from '@/stores/user'
import { videoApi, commentApi, profileApi } from '@/api'
import { formatViews, formatRelative } from '@videoshare/utils/format'
import { addHistory } from '@videoshare/utils/history'

const route     = useRoute()
const router    = useRouter()
const userStore = useUserStore()
const playerRef = ref(null)

const video    = ref(null)
const loading  = ref(false)
let hlsInstance = null

const userAction = reactive({ liked: false, favorited: false })
const isFollowing = ref(false)
const descExpanded = ref(false)
const showSaveModal = ref(false)

const comments       = ref([])
const commentsLoading= ref(false)
const commentText    = ref('')
const replyText      = ref('')
const replyTo        = ref(null)
const submitting     = ref(false)
const commentPageNum = ref(1)
const hasMoreComments= ref(false)
const recommendVideos = ref([])

onMounted(async () => {
  await loadVideo()
  await loadComments()
  loadRecommend()
})

watch(() => route.params.videoId, async () => {
  if (route.params.videoId) {
    video.value = null; comments.value = []; commentPageNum.value = 1
    await loadVideo(); await loadComments()
  }
})

async function loadVideo() {
  loading.value = true
  try {
    const data = await videoApi.getVideoDetail(route.params.videoId)
    video.value = data
    if (data.status === 1) {
      await nextTick()
      await initHls()
    }
    if (userStore.isLoggedIn) {
      const action = await videoApi.checkAction(route.params.videoId)
      userAction.liked = action.liked; userAction.favorited = action.favorited
      if (data.userInfo?.userId && data.userInfo.userId !== userStore.userInfo?.userId)
        isFollowing.value = await profileApi.checkFollow(data.userInfo.userId)
    }
  } catch { ElMessage.error('视频加载失败') }
  finally { loading.value = false }
}

async function initHls() {
  if (!playerRef.value) return
  const Hls = (await import('hls.js')).default
  if (Hls.isSupported()) {
    hlsInstance = new Hls()
    hlsInstance.loadSource(`/hls/${route.params.videoId}/index.m3u8`)
    hlsInstance.attachMedia(playerRef.value)
  } else if (playerRef.value.canPlayType('application/vnd.apple.mpegurl')) {
    playerRef.value.src = `/hls/${route.params.videoId}/index.m3u8`
  }
}

onUnmounted(() => {
  if (hlsInstance) hlsInstance.destroy()
})

async function loadComments(append = false) {
  commentsLoading.value = true
  try {
    const result = await commentApi.getCommentList(route.params.videoId, { pageNum: commentPageNum.value, pageSize: 20 })
    if (append) comments.value.push(...result.list)
    else comments.value = result.list
    hasMoreComments.value = commentPageNum.value < result.pages
  } finally { commentsLoading.value = false }
}

function loadMoreComments() { commentPageNum.value++; loadComments(true) }

function loadRecommend() {
  videoApi.getVideoList({ pageSize: 10 }).then(r => { recommendVideos.value = r.list || [] }).catch(() => {})
}

function onPlay() { if (video.value) addHistory(video.value) }
function onEnded() {}

async function handleLike() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try { const active = await videoApi.doAction({ videoId: route.params.videoId, actionType: 1 }); userAction.liked = active; video.value.likeCount += active ? 1 : -1; ElMessage.success(active ? '点赞成功' : '已取消点赞') } catch {}
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try { const active = await videoApi.doAction({ videoId: route.params.videoId, actionType: 2 }); userAction.favorited = active; video.value.favoriteCount += active ? 1 : -1; ElMessage.success(active ? '收藏成功' : '已取消收藏') } catch {}
}

function handleShare() { navigator.clipboard?.writeText(window.location.href); ElMessage.success('链接已复制到剪贴板') }

function handleSave() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  showSaveModal.value = true
}

async function handleFollow() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try { const active = await profileApi.toggleFollow(video.value.userInfo.userId); isFollowing.value = active; ElMessage.success(active ? '关注成功' : '已取消关注') } catch {}
}

async function submitComment() {
  if (!commentText.value.trim()) return
  submitting.value = true
  try { const newComment = await commentApi.postComment({ videoId: route.params.videoId, content: commentText.value }); comments.value.unshift({ ...newComment, replies: [] }); video.value.commentCount++; commentText.value = ''; ElMessage.success('评论发布成功') } catch {}
  finally { submitting.value = false }
}

function openReply(comment) { replyTo.value = comment; replyText.value = '' }

async function submitReply(parentComment) {
  if (!replyText.value.trim()) return
  try { const newReply = await commentApi.postComment({ videoId: route.params.videoId, content: replyText.value, pCommentId: parentComment.commentId, replyUserId: parentComment.userId }); if (!parentComment.replies) parentComment.replies = []; parentComment.replies.push(newReply); replyTo.value = null; replyText.value = ''; ElMessage.success('回复成功') } catch {}
}

async function likeComment(comment) {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try { const active = await commentApi.likeComment(comment.commentId); comment.liked = active; comment.likeCount = (comment.likeCount || 0) + (active ? 1 : -1) } catch {}
}

async function deleteComment(comment) {
  try { await commentApi.deleteComment(comment.commentId); const idx = comments.value.findIndex(c => c.commentId === comment.commentId); if (idx !== -1) { comments.value.splice(idx, 1); video.value.commentCount-- } ElMessage.success('评论已删除') } catch {}
}
</script>

<style scoped>
.video-page { min-height: 100vh; background: var(--bg-base); }
.page-body { display: flex; gap: 20px; max-width: 1400px; margin: 0 auto; padding: 72px 20px 40px; }
.main-col { flex: 1; min-width: 0; }

.player-wrap { width: 100%; background: #000; border-radius: var(--radius-md); overflow: hidden; aspect-ratio: 16/9; }
.player { width: 100%; height: 100%; display: block; }
.player-skeleton { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: var(--text-muted); font-size: 14px; }
.player-placeholder { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--text-muted); font-size: 14px; gap: 12px; }
.placeholder-spinner {
  width: 28px; height: 28px;
  border: 3px solid var(--border);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.video-info-area { padding: 16px 0; }
.video-title { font-size: 18px; font-weight: 700; margin-bottom: 12px; }
.video-meta-row { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.meta-stats { font-size: 13px; color: var(--text-2); display: flex; align-items: center; gap: 6px; }
.dot { color: var(--text-muted); }
.action-group { display: flex; gap: 6px; }
.action-btn {
  display: flex; align-items: center; gap: 4px;
  padding: 6px 12px; border-radius: 20px;
  border: 1px solid var(--border); background: var(--bg-hover);
  color: var(--text-2); font-size: 13px; font-weight: 500;
  cursor: pointer; transition: var(--transition); font-family: var(--font-body);
}
.action-btn:hover { background: #3a3a3a; color: var(--text-1); }
.action-btn.active { background: var(--color-accent-dim); color: var(--color-accent); border-color: var(--color-accent); }

.divider { height: 1px; background: var(--border); margin: 16px 0; }

.author-row { display: flex; align-items: center; justify-content: space-between; }
.author-link { display: flex; align-items: center; gap: 12px; text-decoration: none; }
.author-avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, var(--color-accent), #7c3aed); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 15px; color: #fff; flex-shrink: 0; }
.author-name { font-size: 14px; font-weight: 600; color: var(--text-1); }
.author-fans { font-size: 12px; color: var(--text-2); margin-top: 2px; }
.follow-btn { padding: 6px 16px; border-radius: 20px; border: none; font-size: 13px; font-weight: 600; cursor: pointer; transition: var(--transition); font-family: var(--font-body); background: var(--color-accent); color: #fff; }
.follow-btn.following { background: var(--bg-hover); color: var(--text-2); border: 1px solid var(--border); }

.video-desc { margin-top: 14px; background: var(--bg-hover); border-radius: var(--radius-md); padding: 12px; }
.video-desc p { font-size: 13px; color: var(--text-2); line-height: 1.7; white-space: pre-wrap; }
.video-desc p.collapsed { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.desc-toggle { background: none; border: none; color: var(--text-1); font-size: 13px; font-weight: 600; cursor: pointer; padding: 4px 0; font-family: var(--font-body); }

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

.comment-list { margin-top: 8px; }
.comment-item { padding: 16px 0; border-bottom: 1px solid var(--border); }
.comment-main { display: flex; gap: 12px; }
.c-avatar { width: 34px; height: 34px; border-radius: 50%; background: var(--bg-hover); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; color: var(--text-2); flex-shrink: 0; }
.c-avatar--sm { width: 26px; height: 26px; font-size: 10px; }
.c-body { flex: 1; }
.c-name { font-size: 13px; font-weight: 600; color: var(--text-1); margin-bottom: 4px; }
.c-text { font-size: 14px; color: var(--text-1); line-height: 1.6; }
.c-footer { display: flex; align-items: center; gap: 14px; margin-top: 8px; }
.c-time { font-size: 12px; color: var(--text-muted); }
.c-like-btn, .c-reply-btn, .c-del-btn { background: none; border: none; font-size: 12px; cursor: pointer; font-family: var(--font-body); color: var(--text-2); display: flex; align-items: center; gap: 4px; padding: 2px 0; transition: var(--transition); }
.c-like-btn:hover, .c-reply-btn:hover { color: var(--text-1); }
.c-like-btn.active { color: var(--color-accent); }
.c-del-btn:hover { color: #ef4444; }
.reply-input { display: flex; gap: 8px; align-items: center; margin-top: 10px; }
.replies { margin-top: 12px; display: flex; flex-direction: column; gap: 10px; }
.reply-item { display: flex; gap: 8px; }
.reply-to { color: var(--text-muted); font-size: 13px; }
.more-replies { background: none; border: none; color: var(--color-accent); font-size: 13px; cursor: pointer; font-family: var(--font-body); padding: 4px 0; }
.load-more-comments { text-align: center; padding: 16px 0; }

.side-col { width: 380px; flex-shrink: 0; }
.side-title { font-size: 14px; font-weight: 600; margin-bottom: 14px; }
.recommend-list { display: flex; flex-direction: column; gap: 8px; }

@media (max-width: 1100px) { .side-col { display: none; } }
@media (max-width: 768px) { .page-body { padding-top: 64px; } .video-title { font-size: 16px; } }
</style>
