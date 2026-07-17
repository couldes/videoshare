<template>
  <div class="analytics-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="page-body">
      <h1 class="page-title">创作者中心</h1>

      <div v-if="error" class="error-banner">{{ error }}</div>

      <template v-if="overview">
        <!-- Overview cards -->
        <div class="overview-grid">
          <div class="stat-card">
            <div class="stat-label">总观看数</div>
            <div class="stat-value">{{ formatViews(overview.totalViews) }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">总点赞数</div>
            <div class="stat-value">{{ formatViews(overview.totalLikes) }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">总评论数</div>
            <div class="stat-value">{{ formatViews(overview.totalComments) }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">订阅者</div>
            <div class="stat-value">{{ formatViews(overview.subscriberCount) }}</div>
          </div>
        </div>

        <!-- Trend charts -->
        <div class="charts-row">
          <div class="chart-box">
            <div class="chart-header">
              <h3 class="chart-title">观看量趋势</h3>
              <el-radio-group v-model="trendDays" size="small" @change="reloadOverview">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
            <canvas ref="viewsChartRef" />
          </div>
          <div class="chart-box">
            <div class="chart-header">
              <h3 class="chart-title">点赞量趋势</h3>
              <el-radio-group v-model="trendDays" size="small" @change="reloadOverview">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
            <canvas ref="likesChartRef" />
          </div>
        </div>

        <!-- Video table -->
        <div class="table-section">
          <h3 class="section-title">视频数据</h3>
          <el-table :data="videoList" v-loading="tableLoading" stripe>
            <el-table-column prop="title" label="视频标题" min-width="200" />
            <el-table-column prop="viewCount" label="观看数" width="110" :formatter="(r) => formatViews(r.viewCount)" />
            <el-table-column prop="likeCount" label="点赞数" width="100" :formatter="(r) => formatViews(r.likeCount)" />
            <el-table-column prop="commentCount" label="评论数" width="100" />
            <el-table-column label="互动率" width="100">
              <template #default="{ row }">{{ (row.engagementRate * 100).toFixed(1) }}%</template>
            </el-table-column>
          </el-table>
          <div v-if="totalPages > 1" class="pagination-wrap">
            <el-pagination background layout="prev, pager, next"
              :total="videoTotal" :page-size="videoPageSize"
              v-model:current-page="videoPageNum" @current-change="loadVideos" />
          </div>
        </div>
      </template>

      <div v-else-if="!error" class="loading-state" v-loading="true">加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { analyticsApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { formatViews } from '@videoshare/utils/format'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()
const overview = ref(null)
const videoList = ref([])
const error = ref('')
const tableLoading = ref(false)
const videoPageNum = ref(1)
const videoPageSize = ref(20)
const videoTotal = ref(0)
const totalPages = ref(0)

const viewsChartRef = ref(null)
const likesChartRef = ref(null)
let viewsChart = null
let likesChart = null
const trendDays = ref(30)

onMounted(() => loadOverview())

async function loadOverview() {
  try {
    const userId = userStore.userInfo?.userId
    const data = await analyticsApi.getOverview(userId, trendDays.value)
    overview.value = data
    await nextTick()
    renderCharts(data)
  } catch (e) {
    error.value = e.response?.data?.message || '数据加载失败'
    return
  }
  loadVideos()
}

async function reloadOverview() {
  if (viewsChart) viewsChart.destroy()
  if (likesChart) likesChart.destroy()
  await loadOverview()
}

async function loadVideos(pageNum = 1) {
  tableLoading.value = true
  try {
    const userId = userStore.userInfo?.userId
    const data = await analyticsApi.getVideos(userId, pageNum, videoPageSize.value)
    videoList.value = data.list || []
    videoTotal.value = data.total || 0
    totalPages.value = data.pages || 0
  } catch { /* ignore */ }
  finally { tableLoading.value = false }
}

async function renderCharts(data) {
  const { Chart, Line, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler } = await import('chart.js')
  Chart.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler)

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { ticks: { color: '#aaa', maxTicksLimit: 7 } },
      y: { ticks: { color: '#aaa' }, beginAtZero: true }
    }
  }

  if (viewsChartRef.value) {
    viewsChart = new Chart(viewsChartRef.value, {
      type: 'line',
      data: {
        labels: data.viewsTrend?.map(d => d.date) || [],
        datasets: [{ data: data.viewsTrend?.map(d => d.count) || [], borderColor: '#ff0033', backgroundColor: 'rgba(255,0,51,0.1)', fill: true, tension: 0.3, pointRadius: 0 }]
      },
      options: chartOptions
    })
  }

  if (likesChartRef.value) {
    likesChart = new Chart(likesChartRef.value, {
      type: 'line',
      data: {
        labels: data.likesTrend?.map(d => d.date) || [],
        datasets: [{ data: data.likesTrend?.map(d => d.count) || [], borderColor: '#7c3aed', backgroundColor: 'rgba(124,58,237,0.1)', fill: true, tension: 0.3, pointRadius: 0 }]
      },
      options: chartOptions
    })
  }
}

onUnmounted(() => {
  if (viewsChart) viewsChart.destroy()
  if (likesChart) likesChart.destroy()
})
</script>

<style scoped>
.analytics-page { min-height: 100vh; background: var(--bg-base); }
.page-body { max-width: 1100px; margin: 0 auto; padding: 72px 20px 60px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; }

.error-banner { padding: 16px; background: rgba(239,68,68,.1); border: 1px solid rgba(239,68,68,.3); border-radius: var(--radius-md); color: #ef4444; font-size: 14px; margin-bottom: 20px; }

.overview-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 28px; }
.stat-card { background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 20px; }
.stat-label { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-value { font-size: 28px; font-weight: 700; color: var(--text-1); }

.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 28px; }
.chart-box { background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 20px; }
.chart-box canvas { width: 100% !important; height: 240px !important; }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.chart-title { font-size: 14px; font-weight: 600; margin: 0; }

.table-section { background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 20px; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }

.loading-state { text-align: center; padding: 60px 0; color: var(--text-muted); }

@media (max-width: 768px) {
  .overview-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}
</style>
