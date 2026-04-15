/**
 * 全局共享常量
 *
 * 与后端枚举一一对应：
 *   UserStatusEnum → USER_STATUS / USER_STATUS_MAP / USER_STATUS_TAG
 *   UserSexEnum    → USER_SEX   / USER_SEX_MAP
 *
 * 修改枚举值只改这一处，web 和 admin 自动同步。
 */

// ===== 用户状态（对应后端 UserStatusEnum）=====
export const USER_STATUS = {
  DISABLE: 0,
  ENABLE:  1
}

/** status 值 → 中文 */
export const USER_STATUS_MAP = {
  [USER_STATUS.DISABLE]: '禁用',
  [USER_STATUS.ENABLE]:  '启用'
}

/** status 值 → Element Plus Tag type */
export const USER_STATUS_TAG = {
  [USER_STATUS.DISABLE]: 'danger',
  [USER_STATUS.ENABLE]:  'success'
}

// ===== 用户性别（对应后端 UserSexEnum）=====
export const USER_SEX = { MALE: 0, FEMALE: 1, SECRECY: 2 }

export const USER_SEX_MAP = {
  [USER_SEX.MALE]:    '男',
  [USER_SEX.FEMALE]:  '女',
  [USER_SEX.SECRECY]: '保密'
}

// ===== 分页默认值 =====
export const PAGE_DEFAULTS = {
  PAGE_NUM:  1,
  PAGE_SIZE: 15
}

// ===== localStorage Key 前缀（防止两端冲突）=====
export const AUTH_KEYS = {
  WEB:   'vs_web',    // web 端前缀  → 'vs_web_token' / 'vs_web_info'
  ADMIN: 'vs_admin'   // admin 端前缀 → 'vs_admin_token' / 'vs_admin_info'
}
