const { mergeConfig }    = require('vite')
const { createBaseConfig } = require('../../vite.config.base.js')

// __dirname = 当前文件所在目录，即 apps/web/
module.exports = mergeConfig(
  createBaseConfig(__dirname),
  {
    server: {
      port: 3000,
      proxy: {
        // 用户端接口代理到后端 8080
        '/account': { target: 'http://localhost:7075', changeOrigin: true }
      }
    }
  }
)
