<template>
  <div class="auth-layout">
    <div class="auth-card">
      <RouterLink to="/" class="card-logo">
        <svg viewBox="0 0 24 24" width="28" height="28">
          <path d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0C.488 3.45.029 5.804 0 12c.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0C23.512 20.55 23.971 18.196 24 12c-.029-6.185-.484-8.549-4.385-8.816zM9 16V8l8 4-8 4z" fill="var(--color-accent)"/>
        </svg>
        <span class="logo-text">VideoShare</span>
      </RouterLink>
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
        <el-button type="danger" size="large" class="submit-btn" :loading="loading" :disabled="!agreed" @click="handleSubmit">
          {{ loading ? '注册中...' : '注册' }}
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
.auth-layout {
  min-height: 100vh; display: flex; align-items: center;
  justify-content: center; padding: 24px;
  background: var(--bg-base);
}
.auth-card {
  width: 100%; max-width: 380px;
  background: var(--bg-surface); border: 1px solid var(--border);
  border-radius: var(--radius-lg); padding: 32px;
}
.card-logo { display: flex; align-items: center; gap: 4px; text-decoration: none; margin-bottom: 24px; }
.logo-text { font-family: var(--font-display); font-size: 18px; font-weight: 700; color: var(--text-1); }
.card-title { font-size: 22px; font-weight: 700; margin-bottom: 4px; }
.card-sub { font-size: 13px; color: var(--text-2); margin-bottom: 24px; }
.submit-btn { width: 100%; height: 40px; font-size: 14px; font-weight: 600; border-radius: 20px !important; margin-top: 4px; }
.captcha-row { display: flex; gap: 10px; width: 100%; }
.captcha-input { flex: 1; }
.captcha-wrap { width: 120px; height: 40px; flex-shrink: 0; border-radius: var(--radius-sm); overflow: hidden; cursor: pointer; border: 1px solid var(--border); background: var(--bg-card); display: flex; align-items: center; justify-content: center; }
.captcha-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.spin { animation: rotate 1s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }
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
