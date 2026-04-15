<template>
  <div class="auth-layout">
    <div class="bg-decor"><div class="c1"/><div class="c2"/></div>
    <div class="auth-card">
      <RouterLink to="/" class="card-logo"><span class="logo-icon">▶</span><span class="logo-text">VideoShare</span></RouterLink>
      <h1 class="card-title">创建账号</h1>
      <p class="card-sub">加入我们，开始探索精彩内容</p>
      <el-form ref="formRef" :model="form" :rules="dynamicRules" label-position="top">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="将作为登录账号" :prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" placeholder="你的显示名称（3-16位）" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少 6 位" :prefix-icon="Lock" show-password size="large" />
          <div class="strength-bar">
            <div v-for="i in 4" :key="i" class="strength-block" :class="i <= strength ? strengthClass : ''" />
          </div>
          <span class="strength-text">{{ strengthLabel }}</span>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" :prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item label="验证码" prop="checkCode">
          <div class="captcha-row">
            <el-input v-model="form.checkCode" placeholder="不区分大小写" :prefix-icon="Key" size="large" class="captcha-input" />
            <div class="captcha-wrap" @click="fetchCaptcha">
              <img v-if="captchaUrl" :src="captchaUrl" class="captcha-img" alt="验证码" />
              <el-icon v-else class="spin"><Loading /></el-icon>
            </div>
          </div>
        </el-form-item>
        <div class="agree-row">
          <el-checkbox v-model="agreed" />
          <span class="agree-text">我已阅读并同意<a href="#" class="link">《用户协议》</a>和<a href="#" class="link">《隐私政策》</a></span>
        </div>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" :disabled="!agreed" @click="handleSubmit">
          {{ loading ? '注册中...' : '注 册' }}
        </el-button>
      </el-form>
      <p class="auth-footer">已有账号？<RouterLink to="/login" class="link">立即登录</RouterLink></p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key, User } from '@element-plus/icons-vue'
import { accountApi } from '@/api'
import {
  emailRules, nickNameRules, passwordRules, checkCodeRules,
  confirmPasswordRules, calcPasswordStrength,
  PASSWORD_STRENGTH_LABELS, PASSWORD_STRENGTH_CLASSES
} from '@videoshare/utils/validate'

const router = useRouter()
const formRef = ref(null); const loading = ref(false); const agreed = ref(false)
const captchaUrl = ref(''); const captchaKey = ref('')
const form = reactive({ email: '', nickName: '', password: '', confirmPassword: '', checkCode: '' })

const strength      = computed(() => calcPasswordStrength(form.password))
const strengthLabel = computed(() => PASSWORD_STRENGTH_LABELS[strength.value])
const strengthClass = computed(() => PASSWORD_STRENGTH_CLASSES[strength.value])

// confirmPasswordRules 需要动态读取 form.password，用 computed 保持响应式
const dynamicRules = computed(() => ({
  email:           emailRules,
  nickName:        nickNameRules,
  password:        passwordRules,
  confirmPassword: confirmPasswordRules({ get value() { return form.password } }),
  checkCode:       checkCodeRules
}))

onMounted(fetchCaptcha)

async function fetchCaptcha() {
  captchaUrl.value = ''
  try { const d = await accountApi.getCheckCode(); captchaUrl.value = d.checkCode; captchaKey.value = d.checkCodeKey }
  catch { ElMessage.error('验证码加载失败') }
}

async function handleSubmit() {
  if (!agreed.value) { ElMessage.warning('请先同意用户协议'); return }
  if (!await formRef.value.validate().catch(() => false)) return
  loading.value = true
  try {
    await accountApi.register({ email: form.email, nickName: form.nickName, registerPassword: form.password, checkCode: form.checkCode, checkCodeKey: captchaKey.value })
    ElMessage.success('注册成功！请登录')
    router.push({ name: 'Login' })
  } catch { await fetchCaptcha(); form.checkCode = '' }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-layout { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px; position: relative; overflow: hidden; }
.c1, .c2 { position: fixed; border-radius: 50%; filter: blur(80px); pointer-events: none; }
.c1 { width: 500px; height: 500px; background: rgba(124,58,237,.08); top: -200px; left: -100px; }
.c2 { width: 400px; height: 400px; background: rgba(255,45,85,.06); bottom: -150px; right: -100px; }
.auth-card { width: 100%; max-width: 420px; background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-xl); padding: 36px 32px; position: relative; z-index: 1; box-shadow: 0 24px 80px rgba(0,0,0,.5); animation: card-in .4s cubic-bezier(.34,1.56,.64,1) both; }
@keyframes card-in { from { opacity: 0; transform: translateY(20px) scale(.97); } to { opacity: 1; transform: none; } }
.card-logo { display: flex; align-items: center; gap: 6px; text-decoration: none; color: var(--text-1); margin-bottom: 24px; }
.logo-icon { font-size: 20px; color: var(--color-accent); } .logo-text { font-family: var(--font-display); font-size: 18px; font-weight: 700; }
.card-title { font-family: var(--font-display); font-size: 26px; font-weight: 800; letter-spacing: -.03em; margin-bottom: 4px; }
.card-sub { font-size: 13px; color: var(--text-2); margin-bottom: 28px; }
.submit-btn { width: 100%; height: 44px; font-size: 15px; font-weight: 600; border-radius: var(--radius-md) !important; margin-top: 8px; }
.captcha-row { display: flex; gap: 10px; width: 100%; } .captcha-input { flex: 1; }
.captcha-wrap { width: 120px; height: 40px; flex-shrink: 0; border-radius: var(--radius-sm); overflow: hidden; cursor: pointer; border: 1px solid var(--border); background: var(--bg-card); display: flex; align-items: center; justify-content: center; }
.captcha-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.spin { animation: rotate 1s linear infinite; } @keyframes rotate { to { transform: rotate(360deg); } }
.strength-bar { display: flex; gap: 4px; margin-top: 6px; }
.strength-block { flex: 1; height: 3px; border-radius: 2px; background: var(--bg-hover); transition: background .3s; }
.strength-block.weak { background: #ef4444; } .strength-block.fair { background: #f59e0b; }
.strength-block.good { background: #3b82f6; } .strength-block.strong { background: #10b981; }
.strength-text { font-size: 11px; color: var(--text-muted); display: block; margin-top: 3px; }
.agree-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.agree-text { font-size: 12px; color: var(--text-2); }
.auth-footer { text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-2); }
.link { color: var(--color-accent); text-decoration: none; font-weight: 600; }
</style>
