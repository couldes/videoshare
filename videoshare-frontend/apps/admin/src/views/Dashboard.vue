<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">数据看板</h1>
        <!-- ★ formatDate 来自 shared -->
        <p class="page-sub mono">SYSTEM OVERVIEW / {{ today }}</p>
      </div>
      <button class="refresh-btn" @click="loadData" :class="{ spinning: loading }">
        <el-icon><Refresh /></el-icon> 刷新数据
      </button>
    </div>

    <!-- 统计卡片 -->
    <div v-loading="loading" class="stat-cards" element-loading-background="transparent">
      <div v-for="card in statCards" :key="card.key" class="stat-card"
        :style="{ '--card-accent': card.color }">
        <div class="card-left">
          <div class="card-label">{{ card.label }}</div>
          <div class="card-value">
            <span class="value-num mono">{{ stats[card.key] ?? '—' }}</span>
          </div>
          <div class="card-desc">{{ card.desc }}</div>
        </div>
        <div class="card-icon-wrap">
          <el-icon class="card-icon"><component :is="card.icon" /></el-icon>
        </div>
        <div class="card-stripe"/>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-section">
      <!-- 折线图：近7天新增 -->
      <div class="chart-card wide">
        <div class="chart-header">
          <h3 class="chart-title">近7天新增用户</h3>
          <p class="chart-sub mono">DAILY REGISTRATION TREND</p>
        </div>
        <div class="chart-body">
          <div v-if="loading" class="chart-placeholder">
            <el-icon class="spin"><Loading /></el-icon>
          </div>
          <template v-else-if="chartPoints.length">
            <svg class="line-chart" viewBox="0 0 600 160" preserveAspectRatio="none">
              <line v-for="y in [32,64,96,128]" :key="y" x1="0" :y1="y" x2="600" :y2="y"
                stroke="rgba(255,255,255,0.04)" stroke-width="1"/>
              <path :d="areaPath" fill="url(#chartGrad)"/>
              <path :d="linePath" fill="none" stroke="var(--color-accent)" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round"/>
              <circle v-for="pt in chartPoints" :key="pt.label"
                :cx="pt.x" :cy="pt.y" r="4"
                fill="var(--color-accent)" stroke="var(--bg-card)" stroke-width="2"/>
              <defs>
                <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%"   stop-color="var(--color-accent)" stop-opacity="0.25"/>
                  <stop offset="100%" stop-color="var(--color-accent)" stop-opacity="0"/>
                </linearGradient>
              </defs>
            </svg>
            <div class="chart-labels">
              <span v-for="pt in chartPoints" :key="pt.label" class="chart-label mono">
                {{ pt.label }}
              </span>
            </div>
          </template>
          <div v-else class="chart-empty">暂无注册数据</div>
        </div>
      </div>

      <!-- 环形图：用户状态 -->
      <div class="chart-card narrow">
        <div class="chart-header">
          <h3 class="chart-title">用户状态分布</h3>
          <p class="chart-sub mono">USER STATUS RATIO</p>
        </div>
        <div class="chart-body donut-body">
          <div class="donut-wrap">
            <svg viewBox="0 0 100 100" class="donut-svg">
              <circle cx="50" cy="50" r="38" fill="none" stroke="var(--bg-hover)" stroke-width="10"/>
              <circle cx="50" cy="50" r="38" fill="none" stroke="var(--success)" stroke-width="10"
                stroke-dasharray="238.76" :stroke-dashoffset="donutOffset"
                stroke-linecap="round" transform="rotate(-90 50 50)"
                style="transition: stroke-dashoffset 0.8s ease"/>
              <text x="50" y="46" text-anchor="middle" font-size="16" font-weight="700"
                fill="var(--text-1)" font-family="var(--font-mono)">{{ stats.totalUsers ?? 0 }}</text>
              <text x="50" y="58" text-anchor="middle" font-size="7"
                fill="var(--text-muted)" font-family="var(--font-body)">总用户数</text>
            </svg>
          </div>
          <!-- ★ 图例文字来自共享常量 USER_STATUS_MAP -->
          <div class="donut-legend">
            <div v-for="item in legendItems" :key="item.key" class="legend-item">
              <span class="legend-dot" :style="{ background: item.color }"/>
              <span class="legend-label">{{ item.label }}</span>
              <span class="legend-val mono">{{ stats[item.key] ?? 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <div class="qa-header">快捷操作</div>
      <div class="qa-list">
        <RouterLink to="/user" class="qa-item">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
          <el-icon class="qa-arrow"><ArrowRight /></el-icon>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminUserApi } from '@/api'
import { formatDate } from '@videoshare/utils/format'
import { USER_STATUS_MAP } from '@videoshare/constants'

// ★ 图例从共享常量动态构建，新增状态只改 constants 一处
const legendItems = [
  { key: 'activeUsers',   label: USER_STATUS_MAP[1], color: 'var(--success)' },
  { key: 'disabledUsers', label: USER_STATUS_MAP[0], color: 'var(--danger)'  },
]

const loading = ref(false)
const stats   = ref({ totalUsers: 0, activeUsers: 0, disabledUsers: 0, dailyRegister: [] })

// ★ 今日日期由 shared formatDate 生成，格式统一
const today = formatDate(new Date(), 'date')

const statCards = [
  { key: 'totalUsers',    label: '总用户数', desc: '平台注册总量', icon: 'User',        color: '#3b82f6' },
  { key: 'activeUsers',   label: '活跃用户', desc: '账号状态正常', icon: 'UserFilled',  color: 'var(--success)' },
  { key: 'disabledUsers', label: '禁用用户', desc: '账号被封禁',   icon: 'CircleClose', color: 'var(--danger)' },
]

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    // GET /admin/user/dashboard → UserManageController.getDashboard()
    const data = await adminUserApi.getDashboard()
    stats.value = data
  } finally {
    loading.value = false
  }
}

// 折线图计算（纯 SVG）
const chartData   = computed(() => stats.value.dailyRegister || [])
const chartPoints = computed(() => {
  const data = chartData.value
  if (!data.length) return []
  const maxCount = Math.max(...data.map(d => d.count), 1)
  const W = 600, H = 140, padX = 30
  return data.map((d, i) => ({
    x:     padX + (i / Math.max(data.length - 1, 1)) * (W - padX * 2),
    y:     H - (d.count / maxCount) * (H - 20) - 10,
    // ★ 日期格式化统一使用 shared formatDate
    label: formatDate(d.date, 'date').slice(5)
  }))
})
const linePath = computed(() =>
  chartPoints.value.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ')
)
const areaPath = computed(() => {
  const pts = chartPoints.value
  if (!pts.length) return ''
  const line = pts.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ')
  return `${line} L ${pts[pts.length-1].x} 155 L ${pts[0].x} 155 Z`
})
const donutOffset = computed(() => {
  const total  = stats.value.totalUsers  || 0
  const active = stats.value.activeUsers || 0
  return 238.76 * (1 - (total > 0 ? active / total : 0))
})
</script>

<style scoped>
.dashboard { max-width: 1200px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24px; }
.page-title  { font-size: 22px; font-weight: 700; letter-spacing: -.02em; margin-bottom: 4px; }
.page-sub    { font-size: 11px; color: var(--text-muted); letter-spacing: .08em; }
.refresh-btn { display: flex; align-items: center; gap: 6px; padding: 7px 14px; background: var(--bg-panel); border: 1px solid var(--border); border-radius: var(--radius-md); color: var(--text-2); font-size: 13px; font-family: var(--font-body); cursor: pointer; transition: var(--transition); }
.refresh-btn:hover { border-color: var(--color-accent); color: var(--color-accent); }
.refresh-btn.spinning .el-icon { animation: rotate 1s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }
.stat-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 14px; margin-bottom: 20px; }
.stat-card  { background: var(--bg-panel); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 20px; display: flex; align-items: center; justify-content: space-between; position: relative; overflow: hidden; transition: var(--transition); }
.stat-card:hover { border-color: var(--card-accent); transform: translateY(-1px); box-shadow: var(--shadow-card); }
.card-stripe { position: absolute; top: 0; left: 0; right: 0; height: 2px; background: var(--card-accent); }
.card-label { font-size: 11px; font-weight: 600; color: var(--text-muted); letter-spacing: .06em; text-transform: uppercase; margin-bottom: 8px; }
.value-num  { font-size: 32px; font-weight: 600; color: var(--text-1); letter-spacing: -.02em; line-height: 1; }
.card-desc  { font-size: 11px; color: var(--text-muted); margin-top: 6px; }
.card-icon-wrap { width: 44px; height: 44px; border-radius: var(--radius-md); background: color-mix(in srgb, var(--card-accent) 12%, transparent); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.card-icon { font-size: 22px; color: var(--card-accent); }
.chart-section { display: grid; grid-template-columns: 1fr 300px; gap: 14px; margin-bottom: 20px; }
.chart-card { background: var(--bg-panel); border: 1px solid var(--border); border-radius: var(--radius-lg); overflow: hidden; }
.chart-header { padding: 16px 20px 12px; border-bottom: 1px solid var(--border); }
.chart-title { font-size: 14px; font-weight: 600; color: var(--text-1); margin-bottom: 2px; }
.chart-sub   { font-size: 10px; color: var(--text-muted); letter-spacing: .08em; }
.chart-body  { padding: 20px; }
.line-chart  { width: 100%; height: 110px; display: block; }
.chart-labels { display: flex; justify-content: space-between; margin-top: 8px; }
.chart-label  { font-size: 10px; color: var(--text-muted); }
.chart-placeholder, .chart-empty { height: 120px; display: flex; align-items: center; justify-content: center; color: var(--text-muted); font-size: 13px; }
.spin { animation: rotate 1s linear infinite; font-size: 20px; }
.donut-body   { display: flex; flex-direction: column; align-items: center; gap: 20px; }
.donut-wrap   { width: 140px; height: 140px; }
.donut-svg    { width: 100%; height: 100%; }
.donut-legend { width: 100%; display: flex; flex-direction: column; gap: 10px; }
.legend-item  { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: var(--bg-card); border-radius: var(--radius-sm); }
.legend-dot   { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.legend-label { flex: 1; font-size: 12px; color: var(--text-2); }
.legend-val   { font-size: 13px; font-weight: 600; color: var(--text-1); }
.quick-actions { background: var(--bg-panel); border: 1px solid var(--border); border-radius: var(--radius-lg); overflow: hidden; }
.qa-header { padding: 12px 20px; font-size: 11px; font-weight: 600; letter-spacing: .08em; text-transform: uppercase; color: var(--text-muted); border-bottom: 1px solid var(--border); }
.qa-list   { padding: 8px; }
.qa-item   { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: var(--radius-md); text-decoration: none; color: var(--text-2); font-size: 13px; transition: var(--transition); }
.qa-item:hover { background: var(--bg-hover); color: var(--text-1); }
.qa-arrow  { margin-left: auto; font-size: 12px; color: var(--text-muted); }
@media (max-width: 900px) { .chart-section { grid-template-columns: 1fr; } }
</style>
