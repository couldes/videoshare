/**
 * 共用 Vite 基础配置（CommonJS 格式，兼容 Node 14/16/18）
 *
 * Vite 4 的配置文件默认以 CJS 执行，__dirname 可以直接使用。
 *
 * 子应用使用方式：
 *   const { mergeConfig } = require('vite')
 *   const { createBaseConfig } = require('../../vite.config.base.js')
 *   module.exports = mergeConfig(createBaseConfig(__dirname), { ... })
 *
 * @param {string} appDir  子应用目录的绝对路径，直接传 __dirname
 */
const vue     = require('@vitejs/plugin-vue')
const { resolve } = require('path')

function createBaseConfig(appDir) {
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        // @ → 当前 app 的 src 目录
        '@': resolve(appDir, 'src'),

        // 共享包直接指向源码，Vite 的 alias 会拦截 import 路径
        '@videoshare/api':                resolve(appDir, '../../packages/api/src'),
        '@videoshare/api/request':        resolve(appDir, '../../packages/api/src/request.js'),
        '@videoshare/api/account':        resolve(appDir, '../../packages/api/src/account.js'),
        '@videoshare/api/video':          resolve(appDir, '../../packages/api/src/video.js'),
        '@videoshare/api/comment':        resolve(appDir, '../../packages/api/src/comment.js'),
        '@videoshare/api/profile':        resolve(appDir, '../../packages/api/src/profile.js'),
        '@videoshare/api/admin/account':  resolve(appDir, '../../packages/api/src/admin/account.js'),
        '@videoshare/api/admin/user':     resolve(appDir, '../../packages/api/src/admin/user.js'),
        '@videoshare/utils':              resolve(appDir, '../../packages/utils/src'),
        '@videoshare/utils/auth':         resolve(appDir, '../../packages/utils/src/auth.js'),
        '@videoshare/utils/format':       resolve(appDir, '../../packages/utils/src/format.js'),
        '@videoshare/utils/validate':     resolve(appDir, '../../packages/utils/src/validate.js'),
        '@videoshare/constants':          resolve(appDir, '../../packages/constants/src/index.js'),
        '@videoshare/assets':             resolve(appDir, '../../packages/assets')
      }
    }
  }
}

module.exports = { createBaseConfig }
