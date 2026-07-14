<template>
  <div class="auth-layout">
    <div class="auth-card">
      <RouterLink to="/" class="card-logo">
        <svg viewBox="0 0 24 24" width="28" height="28">
          <path d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0C.488 3.45.029 5.804 0 12c.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0C23.512 20.55 23.971 18.196 24 12c-.029-6.185-.484-8.549-4.385-8.816zM9 16V8l8 4-8 4z" fill="var(--color-accent)"/>
        </svg>
        <span class="logo-text">VideoShare</span>
      </RouterLink>
      <h1 class="card-title">登录</h1>
      <p class="card-sub">登录你的账号继续探索</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keydown.enter="handleSubmit">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入注册邮箱" :prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password size="large" />
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
        <el-button type="danger" size="large" class="submit-btn" :loading="loading" @click="handleSubmit">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>
      <p class="auth-footer">还没有账号？<RouterLink to="/register" class="link">立即注册</RouterLink></p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { accountApi } from '@/api'
import { emailRules, passwordRules, checkCodeRules } from '@videoshare/utils/validate'

const router = useRouter(); const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null); const loading = ref(false)
const captchaUrl = ref(''); const captchaKey = ref('')
const form = reactive({ email: '', password: '', checkCode: '' })
const rules = { email: emailRules, password: passwordRules, checkCode: checkCodeRules }

onMounted(fetchCaptcha)

async function fetchCaptcha() {
  captchaUrl.value = ''
  try { const d = await accountApi.getCheckCode(); captchaUrl.value = d.checkCode; captchaKey.value = d.checkCodeKey }
  catch { ElMessage.error('验证码加载失败') }
}

async function handleSubmit() {
  if (!await formRef.value.validate().catch(() => false)) return
  loading.value = true
  try {
    const vo = await accountApi.login({ email: form.email, password: form.password, checkCode: form.checkCode, checkCodeKey: captchaKey.value })
    userStore.setLoginInfo(vo)
    ElMessage.success(`欢迎回来，${vo.nickName}！`)
    router.push(route.query.redirect || '/')
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
.auth-footer { text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-2); }
.link { color: var(--color-accent); text-decoration: none; font-weight: 600; }
</style>
