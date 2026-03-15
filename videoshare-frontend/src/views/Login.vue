<template>
  <div class="auth-layout">
    <!-- 背景装饰 -->
    <div class="bg-decor">
      <div class="bg-circle c1" />
      <div class="bg-circle c2" />
    </div>

    <!-- 卡片 -->
    <div class="auth-card">

      <!-- Logo -->
      <RouterLink to="/" class="card-logo">
        <span class="logo-icon">▶</span>
        <span class="logo-text">VideoShare</span>
      </RouterLink>

      <h1 class="card-title">欢迎回来</h1>
      <p class="card-sub">登录你的账号继续探索</p>

      <!-- 表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="auth-form"
        @keydown.enter="handleSubmit"
      >
        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入注册邮箱"
            :prefix-icon="Message"
            size="large"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item label="验证码" prop="checkCode">
          <div class="captcha-row">
            <el-input
              v-model="form.checkCode"
              placeholder="不区分大小写"
              :prefix-icon="Key"
              size="large"
              class="captcha-input"
            />
            <!-- 点击图片刷新验证码 -->
            <div class="captcha-img-wrap" @click="fetchCaptcha" title="点击刷新">
              <img
                v-if="captchaBase64"
                :src="captchaBase64"
                alt="验证码"
                class="captcha-img"
              />
              <div v-else class="captcha-placeholder">
                <el-icon class="rotate-icon"><Refresh /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>

      <!-- 底部跳转 -->
      <p class="auth-footer">
        还没有账号？
        <RouterLink to="/register" class="link">立即注册</RouterLink>
      </p>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCheckCode, login } from '@/api/account'

const router    = useRouter()
const route     = useRoute()
const userStore = useUserStore()

// ============================================================
// 表单数据
// ============================================================
const formRef      = ref(null)   // 表单实例引用（用于触发校验）
const loading      = ref(false)
const captchaBase64 = ref('')    // 验证码图片的 Base64 字符串
const checkCodeKey  = ref('')    // 验证码的 Redis Key，提交时带上

const form = reactive({
  email:     '',
  password:  '',
  checkCode: ''
})

// ============================================================
// 表单校验规则
// trigger: 'blur' 表示失焦后触发校验
// ============================================================
const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email',  message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  checkCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// ============================================================
// 获取验证码（组件挂载时自动调用一次）
// ============================================================
onMounted(fetchCaptcha)

async function fetchCaptcha() {
  captchaBase64.value = ''  // 清空旧图，显示加载状态
  try {
    // 调用 GET /account/checkCode
    // 返回：{ checkCode: 'data:image/...', checkCodeKey: 'CHECK_CODE_xxx' }
    const data = await getCheckCode()
    captchaBase64.value = data.checkCode    // 图片赋给 <img src>
    checkCodeKey.value  = data.checkCodeKey // 保存 key，提交时带上
  } catch {
    ElMessage.error('验证码加载失败，请刷新')
  }
}

// ============================================================
// 提交登录
// ============================================================
async function handleSubmit() {
  // 先触发所有表单项校验，valid=true 才继续
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 调用 POST /account/login，对应后端 AccountController.login()
    const userInfoVO = await login({
      email:        form.email,
      password:     form.password,
      checkCode:    form.checkCode,
      checkCodeKey: checkCodeKey.value
    })

    // 登录成功：把用户信息和 token 存入 Pinia + localStorage
    userStore.setLoginInfo(userInfoVO)

    ElMessage.success(`欢迎回来，${userInfoVO.nickName}！`)

    // 跳转：如果有 redirect 参数则跳回原页面，否则去首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)

  } catch (err) {
    // 错误已在 request.js 拦截器里统一 ElMessage.error 了
    // 这里只需要刷新验证码（因为后端用完就删了）
    await fetchCaptcha()
    form.checkCode = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰球 */
.bg-circle {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.c1 {
  width: 500px; height: 500px;
  background: rgba(255, 45, 85, 0.08);
  top: -200px; right: -100px;
}
.c2 {
  width: 400px; height: 400px;
  background: rgba(124, 58, 237, 0.06);
  bottom: -150px; left: -100px;
}

/* 卡片 */
.auth-card {
  width: 100%;
  max-width: 400px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: 36px 32px;
  position: relative;
  z-index: 1;
  box-shadow: 0 24px 80px rgba(0,0,0,0.5);
  animation: card-in 0.4s cubic-bezier(0.34,1.56,0.64,1) both;
}
@keyframes card-in {
  from { opacity: 0; transform: translateY(20px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.card-logo {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: var(--text-primary);
  margin-bottom: 24px;
}
.logo-icon {
  font-size: 20px;
  color: var(--color-primary);
}
.logo-text {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
}

.card-title {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.card-sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 28px;
}

.auth-form { width: 100%; }

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.05em;
  border-radius: var(--radius-md) !important;
  margin-top: 4px;
}

/* 验证码行 */
.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.captcha-input { flex: 1; }
.captcha-img-wrap {
  width: 120px;
  height: 40px;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border);
  background: var(--bg-card);
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}
.captcha-img-wrap:hover { border-color: rgba(255,255,255,0.15); }
.captcha-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.captcha-placeholder { color: var(--text-muted); }
.rotate-icon { font-size: 18px; }

.auth-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: var(--text-secondary);
}
.link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 600;
}
.link:hover { text-decoration: underline; }
</style>
