/**
 * 主题切换工具
 *
 * 用法：
 *   import { getTheme, setTheme, toggleTheme, initTheme } from '@videoshare/utils/theme'
 *   const isDark = computed(() => getTheme() === 'dark')
 *   <button @click="toggleTheme">切换</button>
 *   // 在 main.js 里调用 initTheme() 初始化
 */

const STORAGE_KEY = 'vs_theme'
const ATTR = 'data-theme'

export function getTheme() {
  return document.documentElement.getAttribute(ATTR) === 'light' ? 'light' : 'dark'
}

export function setTheme(theme) {
  document.documentElement.setAttribute(ATTR, theme)
  try { localStorage.setItem(STORAGE_KEY, theme) } catch {}
}

export function toggleTheme() {
  setTheme(getTheme() === 'dark' ? 'light' : 'dark')
}

export function initTheme() {
  let saved
  try { saved = localStorage.getItem(STORAGE_KEY) } catch {}
  setTheme(saved || 'dark')
}
