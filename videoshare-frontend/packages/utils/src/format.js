/**
 * 格式化工具函数（两端共用）
 */

/**
 * 格式化日期时间
 * @param {string|Date|number} date
 * @param {'datetime'|'date'|'time'} mode
 * @returns {string}
 * @example formatDate('2025-01-15T08:30:00Z') // '2025-01-15 08:30'
 */
export function formatDate(date, mode = 'datetime') {
  if (!date) return '—'
  const d   = new Date(date)
  const pad = n => String(n).padStart(2, '0')
  const ymd = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  const hm  = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  if (mode === 'date') return ymd
  if (mode === 'time') return hm
  return `${ymd} ${hm}`
}

/**
 * 格式化播放量
 * @example formatViews(120000) // '12.0万'
 */
export function formatViews(views) {
  if (!views && views !== 0) return '0'
  if (views >= 100000000) return `${(views / 100000000).toFixed(1)}亿`
  if (views >= 10000)     return `${(views / 10000).toFixed(1)}万`
  return String(views)
}

/**
 * 相对时间
 * @example formatRelative(new Date(Date.now() - 90000)) // '1分钟前'
 */
export function formatRelative(date) {
  if (!date) return ''
  const diff  = Date.now() - new Date(date).getTime()
  const mins  = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days  = Math.floor(diff / 86400000)
  if (diff < 60000) return '刚刚'
  if (mins  < 60)   return `${mins}分钟前`
  if (hours < 24)   return `${hours}小时前`
  if (days  < 30)   return `${days}天前`
  return formatDate(date, 'date')
}

/**
 * 格式化视频时长（秒 → MM:SS 或 HH:MM:SS）
 * @example formatDuration(225) // '3:45'
 */
export function formatDuration(seconds) {
  if (!seconds) return '0:00'
  const pad = n => String(n).padStart(2, '0')
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return h > 0 ? `${h}:${pad(m)}:${pad(s)}` : `${m}:${pad(s)}`
}
