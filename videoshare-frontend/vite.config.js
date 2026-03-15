import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      // 开发时代理到后端，避免跨域
      // 后端运行在 8080 端口
      '/account': {
        target: 'http://localhost:7075',
        changeOrigin: true
      }
    }
  }
})
