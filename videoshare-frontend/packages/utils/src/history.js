/**
 * 观看历史工具 (localStorage-based)
 *
 * 存储格式 (JSON):
 *   [{ videoId, title, coverUrl, viewCount, duration, authorName, timestamp }]
 *
 * 最多保留 100 条，超出后删除最旧的
 */

const MAX_SIZE = 100
const STORAGE_KEY = 'vs_watch_history'

export function getHistory() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch { return [] }
}

export function addHistory(video) {
  let list = getHistory()
  list = list.filter(item => item.videoId !== video.videoId)
  list.unshift({
    videoId:    video.videoId,
    title:      video.title || '',
    coverUrl:   video.coverUrl || '',
    viewCount:  video.viewCount || 0,
    duration:   video.duration || '',
    authorName: video.userInfo?.nickName || '',
    timestamp:  Date.now()
  })
  if (list.length > MAX_SIZE) list = list.slice(0, MAX_SIZE)
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(list)) } catch {}
}

export function clearHistory() {
  try { localStorage.removeItem(STORAGE_KEY) } catch {}
}
