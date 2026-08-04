const { mergeConfig }    = require('vite')
const { createBaseConfig } = require('../../vite.config.base.js')

module.exports = mergeConfig(
  createBaseConfig(__dirname),
  {
    server: {
      port: 3001,
      proxy: {
        // admin 接口通过 Gateway(:7071) 统一入口
        '/admin': { target: 'http://localhost:7071', changeOrigin: true }
      }
    }
  }
)
