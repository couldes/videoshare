# VideoShare 前端

基于 Vue 3 + Vite + Element Plus 构建的视频分享平台前端。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | ^3.4 | 核心框架（Composition API） |
| Vue Router 4 | ^4.3 | 客户端路由 |
| Pinia | ^2.1 | 全局状态管理 |
| Axios | ^1.6 | HTTP 请求封装 |
| Element Plus | ^2.6 | UI 组件库 |
| Vite 5 | ^5.0 | 构建工具 |

## 项目结构

```
src/
├── api/
│   ├── request.js       # Axios 实例 + 请求/响应拦截器
│   └── account.js       # 账号相关接口（登录、注册、验证码）
├── assets/
│   └── global.css       # CSS 变量 + 全局样式
├── components/
│   ├── NavBar.vue        # 顶部导航栏
│   ├── SideBar.vue       # 左侧导航栏
│   ├── SidebarItem.vue   # 导航项（含折叠态 tooltip）
│   └── VideoCard.vue     # 视频卡片
├── router/
│   └── index.js          # 路由配置 + 路由守卫
├── stores/
│   └── user.js           # 用户状态（Pinia）
├── utils/
│   └── auth.js           # Token / 用户信息的 localStorage 管理
└── views/
    ├── Home.vue           # 首页（YouTube 风格）
    ├── Login.vue          # 登录页
    └── Register.vue       # 注册页
```

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 3000 端口）
npm run dev

# 构建生产版本
npm run build
```

## 与后端对接

前端接口和后端 Controller 的对应关系：

| 前端函数（api/account.js） | 后端接口 | 说明 |
|----------------------------|----------|------|
| `getCheckCode()` | `GET /account/checkCode` | 获取验证码图片 |
| `register(data)` | `POST /account/register` | 用户注册 |
| `login(data)` | `POST /account/login` | 用户登录 |

### 开发环境代理配置（vite.config.js）

```js
server: {
  proxy: {
    '/account': {
      target: 'http://localhost:8080',  // ← 改成你的后端地址
      changeOrigin: true
    }
  }
}
```

### 请求/响应格式约定

后端统一返回格式：
```json
{
  "status": "success",   // 或 "error"
  "info": null,          // 错误时的提示信息
  "data": { ... }        // 业务数据
}
```

前端 `request.js` 拦截器会自动处理：
- `status === 'success'` → 直接返回 `data` 字段
- `status === 'error'`   → 弹出 `info` 中的错误信息，抛出异常

### Token 传递

登录成功后，后端返回 `token` 字段。
前端保存在 `localStorage`，后续每次请求自动附加到 Header：

```
Authorization: <token值>
```

后端从 `request.getHeader("Authorization")` 取出验证。
