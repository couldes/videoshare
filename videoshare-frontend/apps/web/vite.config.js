process.noDeprecation = true

const { mergeConfig }    = require('vite')
const { createBaseConfig } = require('../../vite.config.base.js')

// SPA bypass：浏览器导航（Accept: text/html）直接返回 index.html，
// 避免 SPA 路由被 proxy 错误转发到后端导致 404 或返回 JSON
function spaBypass(req) {
  if (req.headers.accept?.startsWith('text/html')) {
    return '/index.html'
  }
}

// __dirname = 当前文件所在目录，即 apps/web/
module.exports = mergeConfig(
  createBaseConfig(__dirname),
  {
    server: {
      port: 3000,
      proxy: {
        // 用户端接口代理到后端 8080
        '/account':      { target: 'http://localhost:7075', changeOrigin: true },
        '/video':        { target: 'http://localhost:7075', changeOrigin: true, bypass: spaBypass },
        '/user':         { target: 'http://localhost:7075', changeOrigin: true, bypass: spaBypass },
        '/comment':      { target: 'http://localhost:7075', changeOrigin: true },
        '/playlist':     { target: 'http://localhost:7075', changeOrigin: true, bypass: spaBypass },
        // 用 /** 确保只代理 API 路径（/notification/list 等），不匹配前端路由 /notifications
        '/notification/': { target: 'http://localhost:7075', changeOrigin: true },
        '/analytics/':   { target: 'http://localhost:7075', changeOrigin: true },
        '/hls/':         { target: 'http://localhost:7075', changeOrigin: true },
        '/images/':      { target: 'http://localhost:7075', changeOrigin: true }
      }
    }
  }
)
