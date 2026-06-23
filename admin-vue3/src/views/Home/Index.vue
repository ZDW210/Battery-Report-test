<template>
  <div class="energy-dashboard">
    <el-row :gutter="12" class="energy-dashboard__top-row">
      <el-col :lg="6" :md="8" :sm="24" :xs="24">
        <el-card shadow="never" class="energy-dashboard__stat">
          <div>
            <span>设备总数</span>
            <strong>{{ deviceTotal }}</strong>
            <small>在线 {{ onlineTotal }} 台</small>
          </div>
          <Icon icon="ep:monitor" style="color: #2563eb" :size="32" />
        </el-card>
      </el-col>
      <el-col :lg="18" :md="16" :sm="24" :xs="24">
        <el-card shadow="never" class="energy-dashboard__status-card">
          <template #header>
            <div class="energy-dashboard__card-header">
              <span>设备在线状态</span>
            </div>
          </template>
          <el-skeleton :loading="loading" animated>
            <div class="energy-dashboard__soc-grid">
              <div v-for="item in statusBands" :key="item.label" class="energy-dashboard__soc-item">
                <div class="energy-dashboard__soc-top">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.count }}</strong>
                </div>
                <el-progress :percentage="item.percent" :stroke-width="10" :color="item.color" />
              </div>
            </div>
          </el-skeleton>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :lg="16" :md="24" :xs="24">
        <el-card shadow="never" class="mb-12px">
          <template #header>
            <div class="energy-dashboard__card-header">
              <span>运行负载</span>
              <span class="energy-dashboard__subtext">按当前设备最新采集值聚合</span>
            </div>
          </template>
          <el-skeleton :loading="loading" animated>
            <div class="energy-dashboard__load">
              <div>
                <span>当前总功率</span>
                <strong>{{ totalPower }} kW</strong>
              </div>
              <div>
                <span>在线率</span>
                <strong>{{ onlineRate }}%</strong>
              </div>
              <div>
                <span>最新采集</span>
                <strong>{{ latestReadingText }}</strong>
              </div>
            </div>
            <el-table :data="powerRank" :show-overflow-tooltip="true" :stripe="true" class="mt-12px">
              <el-table-column label="设备" min-width="160" prop="deviceName" />
              <el-table-column label="项目" min-width="130" prop="projectName">
                <template #default="{ row }">{{ row.projectName || '-' }}</template>
              </el-table-column>
              <el-table-column align="center" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getDeviceStatusType(row.status)" effect="light">
                    {{ getDeviceStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column align="right" label="功率(kW)" width="110">
                <template #default="{ row }">{{ valueOrDash(row.lastPower) }}</template>
              </el-table-column>
              <el-table-column label="更新时间" width="170">
                <template #default="{ row }">{{ formatDateText(row.lastReadingTime) }}</template>
              </el-table-column>
            </el-table>
          </el-skeleton>
        </el-card>
      </el-col>

      <el-col :lg="8" :md="24" :xs="24">
        <el-card shadow="never" class="mb-12px">
          <template #header>
            <div class="energy-dashboard__card-header">
              <span>待处理告警</span>
              <el-button link type="primary" @click="go('/energy/alarm')">处理告警</el-button>
            </div>
          </template>
          <el-skeleton :loading="loading" animated>
            <el-empty v-if="latestAlarms.length === 0" description="暂无待处理告警" />
            <div
              v-for="alarm in latestAlarms"
              v-else
              :key="alarm.id"
              class="energy-dashboard__alarm"
            >
              <div>
                <strong>{{ alarm.title || alarm.code || '未命名告警' }}</strong>
                <span>{{ alarm.deviceName || '-' }} · {{ formatDateText(alarm.occurTime) }}</span>
              </div>
              <el-tag :type="getAlarmTagType(alarm.level)" effect="light">
                {{ getAlarmLevelText(alarm.level) }}
              </el-tag>
            </div>
          </el-skeleton>
        </el-card>

      </el-col>
    </el-row>

    <el-card shadow="never" class="energy-dashboard__data-panel">
      <template #header>
        <div class="energy-dashboard__card-header">
          <div>
            <span>数据面板</span>
            <span class="energy-dashboard__subtext ml-8px">按天统计充电总成本和节约成本</span>
          </div>
          <el-button link type="primary" @click="go('/energy/report-panel')">查看报表面板</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="dataQuery" class="energy-dashboard__data-form" label-width="72px">
        <el-form-item label="月份">
          <el-date-picker
            v-model="dataQuery.billMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择月份"
            class="!w-180px"
            @change="loadDataPanel"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="dataPanelLoading" @click="loadDataPanel">
            <Icon class="mr-5px" icon="ep:refresh" />
            刷新费用
          </el-button>
        </el-form-item>
      </el-form>

      <div class="energy-dashboard__data-summary">
        <div v-for="item in dataPanelSummary" :key="item.label">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>

      <el-empty v-if="dailyCostRows.length === 0" description="暂无费用统计数据" />
      <Echart
        v-else
        v-loading="dataPanelLoading"
        :options="dataPanelChartOptions"
        height="340px"
      />
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { Echart } from '@/components/Echart'
import { EnergyAlarmApi } from '@/api/energy/alarm'
import type { EnergyAlarmVO } from '@/api/energy/alarm'
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyReportApi } from '@/api/energy/report'
import type { EnergyReportDailyCostRowVO } from '@/api/energy/report'
import { formatNullableDate } from '@/utils/formatTime'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

defineOptions({ name: 'Index' })

const router = useRouter()
const message = useMessage()
const currentMonth = dayjs().format('YYYY-MM')

const loading = ref(true)
const dataPanelLoading = ref(false)
const devices = ref<EnergyDeviceVO[]>([])
const latestAlarms = ref<EnergyAlarmVO[]>([])
const dailyCostRows = ref<EnergyReportDailyCostRowVO[]>([])
const deviceTotal = ref(0)

const dataQuery = reactive<{
  billMonth: string
}>({
  billMonth: currentMonth
})

const totalPower = computed(() => {
  const value = devices.value.reduce((sum, item) => sum + Number(item.lastPower || 0), 0)
  return Number(value.toFixed(1))
})

const onlineTotal = computed(() => devices.value.filter((item) => item.status === 0).length)

const onlineRate = computed(() => {
  if (!devices.value.length) return 0
  return Number(((onlineTotal.value / devices.value.length) * 100).toFixed(1))
})

const latestReadingText = computed(() => {
  const latest = [...devices.value]
    .filter((item) => item.lastReadingTime)
    .sort((a, b) => dayjs(b.lastReadingTime).valueOf() - dayjs(a.lastReadingTime).valueOf())[0]
  return latest ? formatDateText(latest.lastReadingTime) : '-'
})

const powerRank = computed(() => {
  return [...devices.value]
    .sort((a, b) => Math.abs(Number(b.lastPower || 0)) - Math.abs(Number(a.lastPower || 0)))
    .slice(0, 8)
})

const dataPanelSummary = computed(() => {
  const validChargeCosts = dailyCostRows.value
    .map((item) => normalizeNumber(item.chargeCost))
    .filter((value): value is number => value !== null)
  const validSavedCosts = dailyCostRows.value
    .map((item) => normalizeNumber(item.savedCost))
    .filter((value): value is number => value !== null)
  const latest = [...dailyCostRows.value].reverse().find((item) => normalizeNumber(item.chargeCost) !== null || normalizeNumber(item.savedCost) !== null)
  return [
    { label: '统计天数', value: dailyCostRows.value.length },
    { label: '充电总成本', value: formatCurrency(sumNumbers(validChargeCosts)) },
    { label: '节约成本', value: formatCurrency(sumNumbers(validSavedCosts)) },
    { label: '最近日期', value: latest?.date || '-' }
  ]
})

const dataPanelChartOptions = computed<EChartsOption>(() => {
  const sortedRows = [...dailyCostRows.value].sort((a, b) => dayjs(a.date).valueOf() - dayjs(b.date).valueOf())

  return buildLineOptions({
    xData: sortedRows.map((item) => dayjs(item.date).format('MM-DD')),
    series: [
      {
        name: '充电总成本',
        data: sortedRows.map((item) => normalizeNumber(item.chargeCost))
      },
      {
        name: '节约成本',
        data: sortedRows.map((item) => normalizeNumber(item.savedCost))
      }
    ],
    yName: '元'
  })
})

const statusBands = computed(() => {
  const bands = [
    { label: '在线', status: 0, color: '#0f766e' },
    { label: '离线', status: 1, color: '#64748b' },
    { label: '故障', status: 2, color: '#dc2626' }
  ]
  return bands.map((band) => {
    const count = devices.value.filter((item) => item.status === band.status).length
    return {
      ...band,
      count,
      percent: devices.value.length === 0 ? 0 : Math.round((count / devices.value.length) * 100)
    }
  })
})

const getDeviceStatusText = (status?: number) => {
  const statusMap: Record<number, string> = {
    0: '在线',
    1: '离线',
    2: '故障'
  }
  return status === undefined || status === null ? '未知' : statusMap[status] || `状态${status}`
}

const getDeviceStatusType = (status?: number) => {
  if (status === 0) return 'success'
  if (status === 2) return 'danger'
  if (status === 3) return 'warning'
  return 'info'
}

const loadDashboard = async () => {
  loading.value = true
  try {
    const [devicePage, alarmPage] = await Promise.all([
      EnergyDeviceApi.getDevicePage({ pageNo: 1, pageSize: 100 }),
      EnergyAlarmApi.getAlarmPage({ pageNo: 1, pageSize: 8, status: 0 })
    ])

    devices.value = devicePage?.list || []
    latestAlarms.value = alarmPage?.list || []
    deviceTotal.value = devicePage?.total || devices.value.length
    await loadDataPanel()
  } catch (error) {
    message.error('运营面板数据加载失败，请检查后端服务和登录状态')
  } finally {
    loading.value = false
  }
}

const loadDataPanel = async () => {
  if (!dataQuery.billMonth) {
    dailyCostRows.value = []
    return
  }
  dataPanelLoading.value = true
  try {
    const report = await EnergyReportApi.getDailyCostReport({
      scopeType: 'all',
      billMonth: dataQuery.billMonth
    })
    dailyCostRows.value = report?.rows || []
  } catch (error) {
    dailyCostRows.value = []
    message.error('数据面板加载失败，请检查报表费用接口')
  } finally {
    dataPanelLoading.value = false
  }
}

const valueOrDash = (value?: number) => {
  return value === undefined || value === null ? '-' : value
}

const formatDateText = (value?: string | Date) => {
  return formatNullableDate(value)
}

const normalizeNumber = (value: unknown): number | null => {
  if (value === undefined || value === null || value === '') {
    return null
  }
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : null
}

const sumNumbers = (values: number[]) => {
  return Number(values.reduce((sum, value) => sum + value, 0).toFixed(2))
}

const formatCurrency = (value: number | null) => {
  if (value === null || value === undefined) {
    return '-'
  }
  return `¥${Number(value || 0).toFixed(2)}`
}

const buildLineOptions = ({
  xData,
  series,
  yName
}: {
  xData: string[]
  series: Array<{ name: string; data: Array<number | null> }>
  yName: string
}): EChartsOption => ({
  color: ['#0ea5a4', '#2563eb', '#f59e0b', '#7c3aed', '#dc2626'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { left: 52, right: 24, top: 48, bottom: 36 },
  xAxis: { type: 'category', data: xData, boundaryGap: false },
  yAxis: { type: 'value', name: yName },
  series: series.map((item) => ({
    name: item.name,
    type: 'line',
    smooth: true,
    showSymbol: true,
    connectNulls: true,
    data: item.data
  }))
})

const getAlarmLevelText = (level?: number) => {
  const levelMap: Record<number, string> = {
    1: '提示',
    2: '一般',
    3: '严重',
    4: '紧急'
  }
  return level ? levelMap[level] || `等级 ${level}` : '未知'
}

const getAlarmTagType = (level?: number) => {
  if (level === 4 || level === 3) {
    return 'danger'
  }
  if (level === 2) {
    return 'warning'
  }
  return 'info'
}

const go = (path: string) => {
  router.push(path)
}

onMounted(() => {
  loadDashboard()
})
</script>

<style lang="scss" scoped>
.energy-dashboard {
  &__top-row {
    margin-bottom: 12px;

    :deep(.el-card) {
      height: 100%;
    }
  }

  &__stat {
    margin-bottom: 12px;

    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      justify-content: space-between;
      min-height: 96px;
    }

    span,
    small {
      display: block;
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }

    strong {
      display: block;
      margin: 8px 0 4px;
      color: var(--el-text-color-primary);
      font-size: 26px;
      line-height: 32px;
    }
  }

  &__card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 600;
  }

  &__subtext {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    font-weight: 400;
  }

  &__load {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;

    div {
      padding: 14px;
      background: var(--el-fill-color-lighter);
      border-radius: 6px;
    }

    span,
    strong {
      display: block;
    }

    span {
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }

    strong {
      margin-top: 8px;
      color: var(--el-text-color-primary);
      font-size: 24px;
      line-height: 30px;
    }
  }

  &__soc-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  &__soc-item {
    padding: 14px;
    background: var(--el-fill-color-lighter);
    border-radius: 6px;
  }

  &__soc-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    span {
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }

    strong {
      color: var(--el-text-color-primary);
      font-size: 20px;
    }
  }

  &__status-card {
    margin-bottom: 12px;

    :deep(.el-card__body) {
      min-height: 96px;
    }
  }

  &__data-panel {
    margin-top: 12px;
  }

  &__data-form {
    padding: 2px 0 8px;

    :deep(.el-form-item) {
      margin-bottom: 10px;
    }
  }

  &__data-summary {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
    margin: 0 0 10px;

    div {
      min-height: 58px;
      padding: 10px 12px;
      background: var(--el-fill-color-lighter);
      border-radius: 6px;
    }

    span,
    strong {
      display: block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    span {
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }

    strong {
      margin-top: 6px;
      color: var(--el-text-color-primary);
      font-size: 16px;
      line-height: 22px;
    }
  }

  &__alarm {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: 0;
    }

    strong,
    span {
      display: block;
    }

    strong {
      color: var(--el-text-color-primary);
      font-size: 14px;
    }

    span {
      margin-top: 4px;
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }
  }

}

@media (max-width: 1200px) {
  .energy-dashboard {
    &__load,
    &__soc-grid,
    &__data-summary {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
}

@media (max-width: 768px) {
  .energy-dashboard {
    &__header {
      align-items: flex-start;
      flex-direction: column;
    }

    &__load,
    &__soc-grid,
    &__data-summary {
      grid-template-columns: 1fr;
    }

    &__data-form {
      :deep(.el-date-editor),
      :deep(.el-select),
      :deep(.el-radio-group) {
        width: 100% !important;
      }
    }
  }
}
</style>
