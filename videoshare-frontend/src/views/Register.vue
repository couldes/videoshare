<template>
  <div class="auth-layout">
    <div class="bg-decor">
      <div class="bg-circle c1" />
      <div class="bg-circle c2" />
    </div>

    <div class="auth-card">
      <RouterLink to="/" class="card-logo">
        <span class="logo-icon">▶</span>
        <span class="logo-text">VideoShare</span>
      </RouterLink>

      <h1 class="card-title">创建账号</h1>
      <p class="card-sub">加入我们，开始探索精彩内容</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="auth-form"
      >
        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="将作为登录账号"
            :prefix-icon="Message"
            size="large"
          />
        </el-form-item>

        <!-- 昵称 -->
        <el-form-item label="昵称" prop="nickName">
          <el-input
            v-model="form.nickName"
            placeholder="你的显示名称（3-16位）"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="至少 6 位"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
          <!-- 密码强度指示器 -->
          <div class="strength-bar">
            <div
              v-for="i in 4"
              :key="i"
              class="strength-block"
              :class="getStrengthClass(i)"
            />
          </div>
          <span class="strength-text">{{ strengthLabel }}</span>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
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

        <!-- 注册须知 -->
        <div class="agree-row">
          <el-checkbox v-model="agreed" />
          <span class="agree-text">
            我已阅读并同意
            <a href="#" class="link">《用户协议》</a>
            和
            <a href="#" class="link">《隐私政策》</a>
          </span>
        </div>

        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="loading"
          :disabled="!agreed"
          @click="handleSubmit"
        >
          {{ loading ? '注册中...' : '注 册' }}
        </el-button>
      </el-form>

      <p class="auth-footer">
        已有账号？
        <RouterLink to="/login" class="link">立即登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key, User, Refresh } from '@element-plus/icons-vue'
import { getCheckCode, register } from '@/api/account'

const router = useRouter()

const formRef       = ref(null)
const loading       = ref(false)
const captchaBase64 = ref('')
const checkCodeKey  = ref('')
const agreed        = ref(false)

const form = reactive({
  email:           '',
  nickName:        '',
  password:        '',
  confirmPassword: '',
  checkCode:       ''
})

// ============================================================
// 密码强度计算
// ============================================================
const passwordStrength = computed(() => {
  const p = form.password
  if (!p) return 0
  let score = 0
  if (p.length >= 6)                     score++
  if (p.length >= 10)                    score++
  if (/[A-Z]/.test(p) && /[a-z]/.test(p)) score++
  if (/[0-9]/.test(p) && /[^a-zA-Z0-9]/.test(p)) score++
  return score
})

const strengthLabel = computed(() => {
  const labels = ['', '弱', '一般', '较强', '强']
  return labels[passwordStrength.value]
})

function getStrengthClass(i) {
  if (!form.password) return ''
  if (i > passwordStrength.value) return ''
  if (passwordStrength.value <= 1)   return 'weak'
  if (passwordStrength.value === 2)  return 'fair'
  if (passwordStrength.value === 3)  return 'good'
  return 'strong'
}

// ============================================================
// 校验规则
// ============================================================
// 自定义"两次密码一致"校验器
const validateConfirmPwd = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  nickName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 3, max: 16, message: '昵称长度为 3-16 位', trigger: 'blur' },
    // 自定义正则：只允许中文、字母、数字、下划线
    { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/, message: '昵称只能包含中文、字母、数字、下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ],
  checkCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// ============================================================
// 生命周期
// ============================================================
onMounted(fetchCaptcha)

async function fetchCaptcha() {
  captchaBase64.value = ''
  try {
    const data = await getCheckCode()
    captchaBase64.value = data.checkCode
    checkCodeKey.value  = data.checkCodeKey
  } catch {
    ElMessage.error('验证码加载失败')
  }
}

// ============================================================
// 提交注册
// ============================================================
async function handleSubmit() {
  if (!agreed.value) {
    ElMessage.warning('请先同意用户协议')
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 调用 POST /account/register，对应后端 AccountController.register()
    // 后端接收：email, nickName, registerPassword, checkCode, checkCodeKey
    await register({
      email:            form.email,
      nickName:         form.nickName,
      registerPassword: form.password,   // 注意：后端字段名是 registerPassword
      checkCode:        form.checkCode,
      checkCodeKey:     checkCodeKey.value
    })

    ElMessage.success('注册成功！请登录')
    // 注册成功 → 跳转到登录页，并带上邮箱方便用户直接填
    router.push({ name: 'Login' })

  } catch {
    // 错误已在拦截器里提示，这里只刷新验证码
    await fetchCaptcha()
    form.checkCode = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 和 Login.vue 共用背景装饰和卡片样式 */
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}
.bg-circle {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.c1 {
  width: 500px; height: 500px;
  background: rgba(124, 58, 237, 0.08);
  top: -200px; left: -100px;
}
.c2 {
  width: 400px; height: 400px;
  background: rgba(255, 45, 85, 0.06);
  bottom: -150px; right: -100px;
}
.auth-card {
  width: 100%;
  max-width: 420px;
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
  display: flex; align-items: center; gap: 6px;
  text-decoration: none; color: var(--text-primary);
  margin-bottom: 24px;
}
.logo-icon { font-size: 20px; color: var(--color-primary); }
.logo-text { font-family: var(--font-display); font-size: 18px; font-weight: 700; }
.card-title {
  font-family: var(--font-display);
  font-size: 26px; font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--text-primary); margin-bottom: 4px;
}
.card-sub { font-size: 13px; color: var(--text-secondary); margin-bottom: 28px; }
.auth-form { width: 100%; }
.submit-btn {
  width: 100%; height: 44px;
  font-size: 15px; font-weight: 600;
  letter-spacing: 0.05em;
  border-radius: var(--radius-md) !important;
  margin-top: 8px;
}

/* 密码强度 */
.strength-bar {
  display: flex; gap: 4px; margin-top: 6px;
}
.strength-block {
  flex: 1; height: 3px; border-radius: 2px;
  background: var(--bg-hover);
  transition: background 0.3s;
}
.strength-block.weak   { background: #ef4444; }
.strength-block.fair   { background: #f59e0b; }
.strength-block.good   { background: #3b82f6; }
.strength-block.strong { background: #10b981; }
.strength-text {
  font-size: 11px; color: var(--text-muted);
  display: block; margin-top: 3px;
}

/* 验证码 */
.captcha-row { display: flex; gap: 10px; width: 100%; }
.captcha-input { flex: 1; }
.captcha-img-wrap {
  width: 120px; height: 40px; flex-shrink: 0;
  border-radius: var(--radius-sm); overflow: hidden;
  cursor: pointer; border: 1px solid var(--border);
  background: var(--bg-card); transition: var(--transition);
  display: flex; align-items: center; justify-content: center;
}
.captcha-img-wrap:hover { border-color: rgba(255,255,255,0.15); }
.captcha-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.captcha-placeholder { color: var(--text-muted); }
.rotate-icon { font-size: 18px; }

/* 同意协议 */
.agree-row {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 4px;
}
.agree-text { font-size: 12px; color: var(--text-secondary); }

.auth-footer {
  text-align: center; margin-top: 20px;
  font-size: 13px; color: var(--text-secondary);
}
.link { color: var(--color-primary); text-decoration: none; font-weight: 600; }
.link:hover { text-decoration: underline; }
</style>
