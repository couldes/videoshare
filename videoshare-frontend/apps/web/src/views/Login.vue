<template>
  <div class="auth-layout">
    <div class="bg-decor"><div class="c1"/><div class="c2"/></div>
    <div class="auth-card">
      <RouterLink to="/" class="card-logo"><span class="logo-icon">▶</span><span class="logo-text">VideoShare</span></RouterLink>
      <h1 class="card-title">欢迎回来</h1>
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
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleSubmit">
          {{ loading ? '登录中...' : '登 录' }}
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
.auth-layout { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px; position: relative; overflow: hidden; }
.c1, .c2 { position: fixed; border-radius: 50%; filter: blur(80px); pointer-events: none; }
.c1 { width: 500px; height: 500px; background: rgba(255,45,85,.08); top: -200px; right: -100px; }
.c2 { width: 400px; height: 400px; background: rgba(124,58,237,.06); bottom: -150px; left: -100px; }
.auth-card { width: 100%; max-width: 400px; background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-xl); padding: 36px 32px; position: relative; z-index: 1; box-shadow: 0 24px 80px rgba(0,0,0,.5); animation: card-in .4s cubic-bezier(.34,1.56,.64,1) both; }
@keyframes card-in { from { opacity: 0; transform: translateY(20px) scale(.97); } to { opacity: 1; transform: none; } }
.card-logo { display: flex; align-items: center; gap: 6px; text-decoration: none; color: var(--text-1); margin-bottom: 24px; }
.logo-icon { font-size: 20px; color: var(--color-accent); } .logo-text { font-family: var(--font-display); font-size: 18px; font-weight: 700; }
.card-title { font-family: var(--font-display); font-size: 26px; font-weight: 800; letter-spacing: -.03em; margin-bottom: 4px; }
.card-sub { font-size: 13px; color: var(--text-2); margin-bottom: 28px; }
.submit-btn { width: 100%; height: 44px; font-size: 15px; font-weight: 600; border-radius: var(--radius-md) !important; margin-top: 4px; }
.captcha-row { display: flex; gap: 10px; width: 100%; } .captcha-input { flex: 1; }
.captcha-wrap { width: 120px; height: 40px; flex-shrink: 0; border-radius: var(--radius-sm); overflow: hidden; cursor: pointer; border: 1px solid var(--border); background: var(--bg-card); display: flex; align-items: center; justify-content: center; }
.captcha-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.spin { animation: rotate 1s linear infinite; } @keyframes rotate { to { transform: rotate(360deg); } }
.auth-footer { text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-2); }
.link { color: var(--color-accent); text-decoration: none; font-weight: 600; }
</style>
