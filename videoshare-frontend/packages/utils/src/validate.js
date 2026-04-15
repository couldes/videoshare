/**
 * 共享表单校验规则（Element Plus 格式）
 */

export const emailRules = [
  { required: true, message: '请输入邮箱',     trigger: 'blur' },
  { type: 'email',  message: '邮箱格式不正确', trigger: 'blur' }
]

export const passwordRules = [
  { required: true, message: '请输入密码',          trigger: 'blur' },
  { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
]

export const nickNameRules = [
  { required: true, message: '请输入昵称', trigger: 'blur' },
  { min: 3, max: 16, message: '昵称长度为 3-16 位', trigger: 'blur' },
  { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/, message: '只能含中文/字母/数字/下划线', trigger: 'blur' }
]

export const checkCodeRules = [
  { required: true, message: '请输入验证码', trigger: 'blur' }
]

export const accountRules = [
  { required: true, message: '请输入管理员账号', trigger: 'blur' }
]

/**
 * 确认密码校验（动态对比原密码）
 * @param {{ value: string } | string} passwordRef  原密码响应式引用或字符串
 */
export function confirmPasswordRules(passwordRef) {
  return [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const pwd = typeof passwordRef === 'object' ? passwordRef.value : passwordRef
        if (value !== pwd) callback(new Error('两次输入密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

/** 密码强度计算（0-4）*/
export function calcPasswordStrength(password) {
  if (!password) return 0
  let score = 0
  if (password.length >= 6)                                     score++
  if (password.length >= 10)                                    score++
  if (/[A-Z]/.test(password) && /[a-z]/.test(password))        score++
  if (/[0-9]/.test(password) && /[^a-zA-Z0-9]/.test(password)) score++
  return score
}

export const PASSWORD_STRENGTH_LABELS  = ['', '弱', '一般', '较强', '强']
export const PASSWORD_STRENGTH_CLASSES = ['', 'weak', 'fair', 'good', 'strong']
