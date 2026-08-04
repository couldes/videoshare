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

// 将请求路径加上 /web 前缀，以便 Gateway 路由分发
function rewriteToGateway(path) {
  return '/web' + path
}

// __dirname = 当前文件所在目录，即 apps/web/
module.exports = mergeConfig(
  createBaseConfig(__dirname),
  {
    server: {
      port: 3000,
      proxy: {
        // 用户端接口通过 Gateway(:7071) 统一入口
        '/account':      { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway },
        '/video':        { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway, bypass: spaBypass },
        '/user':         { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway, bypass: spaBypass },
        '/comment':      { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway },
        '/playlist':     { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway, bypass: spaBypass },
        // 用 /** 确保只代理 API 路径（/notification/list 等），不匹配前端路由 /notifications
        '/notification/': { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway },
        '/analytics/':   { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway },
        '/hls/':         { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway },
        '/images/':      { target: 'http://localhost:7071', changeOrigin: true, rewrite: rewriteToGateway },
        // 上传路径直通 gateway /resource/**（不加大 /web 前缀，resource-route 原样转发）
        '/resource':     { target: 'http://localhost:7071', changeOrigin: true }
      }
    }
  }
)
