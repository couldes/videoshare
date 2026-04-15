import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'

// ★ CSS 加载顺序很重要：
//   1. reset.css      → 最底层，浏览器默认样式清除
//   2. element-theme  → 中间层，Element Plus 组件深色覆盖（用 CSS 变量）
//   3. theme.css      → 最上层，web 端定义具体 CSS 变量值（红色调）
// 这样 element-theme 里的 var(--color-accent) 就会取到 theme.css 里的 #ff2d55
import '@videoshare/assets/reset.css'
import '@videoshare/assets/element-theme.css'
import './assets/theme.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia()).use(router).use(ElementPlus, { locale: zhCn })
app.mount('#app')
