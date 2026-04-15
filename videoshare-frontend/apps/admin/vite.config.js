const { mergeConfig }    = require('vite')
const { createBaseConfig } = require('../../vite.config.base.js')

module.exports = mergeConfig(
  createBaseConfig(__dirname),
  {
    server: {
      port: 3001,
      proxy: {
        // admin 接口代理到后端 8081
        '/admin': { target: 'http://localhost:7070', changeOrigin: true }
      }
    }
  }
)
