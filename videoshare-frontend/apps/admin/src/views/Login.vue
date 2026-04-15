<template>
  <div class="login-page">
    <!-- 左侧品牌面板 -->
    <div class="brand-panel">
      <div class="brand-inner">
        <div class="brand-logo">
          <span class="logo-mark">▶</span>
          <span class="logo-name">VideoShare</span>
        </div>
        <div class="brand-badge">ADMIN CONSOLE</div>
        <h2 class="brand-headline">管理控制台</h2>
        <p class="brand-sub">安全、高效地管理你的视频平台</p>
        <div class="brand-stats">
          <div class="stat-item">
            <div class="stat-num">∞</div><div class="stat-label">视频内容</div>
          </div>
          <div class="stat-divider"/>
          <div class="stat-item">
            <div class="stat-num">24/7</div><div class="stat-label">全天候监控</div>
          </div>
          <div class="stat-divider"/>
          <div class="stat-item">
            <div class="stat-num">0ms</div><div class="stat-label">响应延迟</div>
          </div>
        </div>
      </div>
      <div class="grid-bg"/>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-panel">
      <div class="login-box">
        <div class="login-header">
          <p class="login-sys mono">SYS:ADMIN_AUTH_v2.0</p>
          <h1 class="login-title">身份验证</h1>
          <p class="login-sub">请输入管理员凭证以继续</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules"
          label-position="top" @keydown.enter="handleLogin">

          <el-form-item prop="account">
            <template #label>
              <span class="field-label">
                <span class="label-tag mono">01</span> 管理员账号
              </span>
            </template>
            <el-input v-model="form.account" placeholder="admin"
              size="large" :prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="password">
            <template #label>
              <span class="field-label">
                <span class="label-tag mono">02</span> 密码
              </span>
            </template>
            <el-input v-model="form.password" type="password"
              placeholder="••••••••" show-password size="large" :prefix-icon="Lock" />
          </el-form-item>

          <el-form-item prop="checkCode">
            <template #label>
              <span class="field-label">
                <span class="label-tag mono">03</span> 图形验证码
              </span>
            </template>
            <div class="captcha-row">
              <el-input v-model="form.checkCode" placeholder="不区分大小写"
                size="large" :prefix-icon="Key" class="captcha-input" />
              <div class="captcha-wrap" @click="fetchCaptcha" title="点击刷新">
                <img v-if="captchaUrl" :src="captchaUrl" class="captcha-img" alt="验证码" />
                <el-icon v-else class="spin"><Loading /></el-icon>
              </div>
            </div>
          </el-form-item>

          <el-button type="primary" size="large" class="login-btn"
            :loading="loading" @click="handleLogin">
            <template v-if="!loading">
              <el-icon style="margin-right:6px"><Unlock /></el-icon>进入管理后台
            </template>
            <span v-else>验证中...</span>
          </el-button>
        </el-form>

        <p class="login-hint">
          <el-icon><InfoFilled /></el-icon>
          此页面仅限授权管理员访问，操作日志将被记录
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'
import { adminAccountApi } from '@/api'
// ★ 共享校验规则：accountRules（不限邮箱格式）
import { accountRules, passwordRules, checkCodeRules } from '@videoshare/utils/validate'

const router     = useRouter()
const route      = useRoute()
const adminStore = useAdminStore()

const formRef    = ref(null)
const loading    = ref(false)
const captchaUrl = ref('')
const captchaKey = ref('')

const form = reactive({ account: '', password: '', checkCode: '' })

// ★ 直接使用共享规则，不重复编写
const rules = {
  account:   accountRules,
  password:  passwordRules,
  checkCode: checkCodeRules
}

onMounted(fetchCaptcha)

async function fetchCaptcha() {
  captchaUrl.value = ''
  try {
    // GET /admin/account/checkCode → AdminAccountController.getCheckCode()
    const d = await adminAccountApi.getCheckCode()
    captchaUrl.value = d.checkCode
    captchaKey.value = d.checkCodeKey
  } catch { ElMessage.error('验证码加载失败') }
}

async function handleLogin() {
  if (!await formRef.value.validate().catch(() => false)) return
  loading.value = true
  try {
    // POST /admin/account/login → AdminAccountController.login()
    // 返回：{ token, account }
    const data = await adminAccountApi.login({
      account:      form.account,
      password:     form.password,
      checkCode:    form.checkCode,
      checkCodeKey: captchaKey.value
    })
    adminStore.setLoginInfo(data)
    ElMessage.success('登录成功，欢迎回来')
    router.push(route.query.redirect || '/')
  } catch {
    await fetchCaptcha()
    form.checkCode = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; background: var(--bg-void, var(--bg-base)); }

/* 左侧品牌 */
.brand-panel { width: 420px; flex-shrink: 0; background: var(--bg-surface); border-right: 1px solid var(--border); position: relative; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.brand-inner { position: relative; z-index: 2; padding: 48px; }
.brand-logo  { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
.logo-mark { font-size: 22px; color: var(--color-accent); }
.logo-name { font-family: var(--font-body); font-size: 20px; font-weight: 700; color: var(--text-1); letter-spacing: -.02em; }
.brand-badge { display: inline-block; font-size: 10px; font-weight: 600; letter-spacing: .12em; color: var(--color-accent); background: var(--color-accent-dim); border: 1px solid var(--border-accent); padding: 3px 10px; border-radius: 3px; margin-bottom: 28px; }
.brand-headline { font-size: 32px; font-weight: 700; letter-spacing: -.03em; color: var(--text-1); line-height: 1.2; margin-bottom: 10px; }
.brand-sub { font-size: 14px; color: var(--text-2); line-height: 1.6; margin-bottom: 48px; }
.brand-stats { display: flex; align-items: center; gap: 24px; }
.stat-item { text-align: center; }
.stat-num { font-size: 20px; font-weight: 600; color: var(--text-1); margin-bottom: 4px; }
.stat-label { font-size: 11px; color: var(--text-muted); letter-spacing: .04em; }
.stat-divider { width: 1px; height: 32px; background: var(--border-strong); }
.grid-bg { position: absolute; inset: 0; background-image: linear-gradient(var(--border) 1px, transparent 1px), linear-gradient(90deg, var(--border) 1px, transparent 1px); background-size: 40px 40px; opacity: .5; z-index: 1; mask-image: radial-gradient(ellipse at center, black 0%, transparent 75%); }

/* 右侧表单 */
.login-panel { flex: 1; display: flex; align-items: center; justify-content: center; padding: 40px 24px; }
.login-box { width: 100%; max-width: 400px; animation: slide-in .35s cubic-bezier(.4,0,.2,1) both; }
@keyframes slide-in { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: none; } }
.login-header { margin-bottom: 32px; }
.login-sys   { font-size: 11px; color: var(--color-accent); letter-spacing: .08em; margin-bottom: 10px; }
.login-title { font-size: 28px; font-weight: 700; letter-spacing: -.03em; color: var(--text-1); margin-bottom: 6px; }
.login-sub   { font-size: 13px; color: var(--text-2); }

.field-label { display: flex; align-items: center; gap: 8px; font-size: 12px; font-weight: 600; color: var(--text-2); letter-spacing: .03em; }
.label-tag   { font-size: 10px; color: var(--color-accent); background: var(--color-accent-dim); padding: 1px 5px; border-radius: 2px; }

.captcha-row  { display: flex; gap: 10px; width: 100%; }
.captcha-input { flex: 1; }
.captcha-wrap { width: 120px; height: 40px; flex-shrink: 0; border: 1px solid var(--border-strong); border-radius: var(--radius-sm); overflow: hidden; cursor: pointer; background: var(--bg-panel); display: flex; align-items: center; justify-content: center; transition: border-color var(--transition); }
.captcha-wrap:hover { border-color: var(--color-accent); }
.captcha-img  { width: 100%; height: 100%; object-fit: cover; display: block; }
.spin { animation: rotate 1s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }

.login-btn  { width: 100%; height: 46px; font-size: 14px; font-weight: 600; letter-spacing: .04em; border-radius: var(--radius-md) !important; margin-top: 8px; }
.login-hint { display: flex; align-items: center; gap: 6px; margin-top: 20px; font-size: 11px; color: var(--text-muted); line-height: 1.5; }

@media (max-width: 768px) { .brand-panel { display: none; } }
</style>
