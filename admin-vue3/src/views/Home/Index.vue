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

      <section class="energy-dashboard__bill-summary">
        <div class="energy-dashboard__bill-scope">
          <strong>{{ billScopeTitle }}</strong>
          <span>共 {{ billDeviceCount }} 块电表，账单月份 {{ dataQuery.billMonth }}</span>
        </div>
        <el-skeleton :loading="dataPanelLoading" animated>
          <div class="energy-dashboard__bill-kpis">
            <div
              v-for="item in billTopCards"
              :key="item.label"
              class="energy-dashboard__bill-kpi"
              :style="{ '--card-color': item.color }"
            >
              <div>
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.hint }}</small>
              </div>
              <Icon :icon="item.icon" :size="34" />
            </div>
          </div>
        </el-skeleton>
      </section>

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
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyPricingRuleApi } from '@/api/energy/pricingRule'
import type { EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import { EnergyReportApi } from '@/api/energy/report'
import type { EnergyReportBillVO, EnergyReportDailyCostRowVO } from '@/api/energy/report'
import { calculateCustomerBillMetrics, moneyText } from '@/views/energy/shared/billMetrics'
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
const pricingRules = ref<EnergyPricingRuleVO[]>([])
const billReport = ref<EnergyReportBillVO>()
const dailyCostRows = ref<EnergyReportDailyCostRowVO[]>([])
const deviceTotal = ref(0)

const dataQuery = reactive<{
  billMonth: string
}>({
  billMonth: currentMonth
})

const onlineTotal = computed(() => devices.value.filter((item) => item.status === 0).length)

const billScopeTitle = computed(() => billReport.value?.scopeName || '全部电表汇总')
const billDeviceCount = computed(() => billReport.value?.summary?.deviceCount ?? deviceTotal.value)
const billMetrics = computed(() => calculateCustomerBillMetrics({
  billReport: billReport.value,
  pricingRules: pricingRules.value,
  devices: devices.value
}))

const billTopCards = computed(() => [
  {
    label: '充入电量合计',
    value: formatKwh(billMetrics.value.totalChargeEnergy),
    hint: '按正向有功电能首末差汇总',
    icon: 'ep:connection',
    color: '#2088d8'
  },
  {
    label: '放出电量合计',
    value: formatKwh(billMetrics.value.totalDischargeEnergy),
    hint: '按反向有功电能首末差汇总',
    icon: 'ep:truck',
    color: '#0ea5a4'
  },
  {
    label: '本期电费',
    value: moneyText(billMetrics.value.payableAmount),
    hint: '按数据报表保底和基础服务费口径',
    icon: 'ep:money',
    color: '#16a34a'
  }
])

const dataPanelSummary = computed(() => {
  const validChargeCosts = dailyCostRows.value
    .map((item) => normalizeNumber(item.chargeCost))
    .filter((value): value is number => value !== null)
  const latest = [...dailyCostRows.value].reverse().find((item) => normalizeNumber(item.chargeCost) !== null || normalizeNumber(item.savedCost) !== null)
  return [
    { label: '统计天数', value: dailyCostRows.value.length },
    { label: '充电总成本', value: moneyText(billMetrics.value.chargeCost || sumNumbers(validChargeCosts)) },
    { label: '最近日期', value: latest?.date || '-' }
  ]
})

const sortedDailyCostRows = computed(() => {
  return [...dailyCostRows.value].sort((a, b) => dayjs(a.date).valueOf() - dayjs(b.date).valueOf())
})

const dataPanelChartOptions = computed<EChartsOption>(() => {
  const sortedRows = sortedDailyCostRows.value

  return buildLineOptions({
    xData: sortedRows.map((item) => dayjs(item.date).format('MM-DD')),
    series: [
      {
        name: '充电总成本',
        data: sortedRows.map((item) => normalizeNumber(item.chargeCost))
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

const loadDashboard = async () => {
  loading.value = true
  try {
    const devicePage = await EnergyDeviceApi.getDevicePage({ pageNo: 1, pageSize: 100 })

    devices.value = devicePage?.list || []
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
    billReport.value = undefined
    dailyCostRows.value = []
    return
  }
  dataPanelLoading.value = true
  try {
    const [dailyReport, bill, rules] = await Promise.all([
      EnergyReportApi.getDailyCostReport({
        scopeType: 'all',
        billMonth: dataQuery.billMonth
      }),
      EnergyReportApi.getBillReport({
        scopeType: 'all',
        billMonth: dataQuery.billMonth
      }),
      EnergyPricingRuleApi.getPricingRulePage({ pageNo: 1, pageSize: 5000 })
    ])
    dailyCostRows.value = dailyReport?.rows || []
    billReport.value = bill
    pricingRules.value = rules?.list || []
  } catch (error) {
    billReport.value = undefined
    dailyCostRows.value = []
    pricingRules.value = []
    message.error('数据面板加载失败，请检查报表费用接口')
  } finally {
    dataPanelLoading.value = false
  }
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

const formatKwh = (value: number | null) => {
  if (value === null || value === undefined) {
    return '-'
  }
  return `${Number(value || 0).toFixed(2)} kWh`
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

  &__bill-summary {
    margin-bottom: 12px;
  }

  &__bill-scope {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 12px 16px;
    color: #0f172a;
    background: #eaf4ff;
    border: 1px solid #d8ecff;
    border-radius: 8px;

    strong {
      font-size: 18px;
      line-height: 24px;
    }

    span {
      color: #334155;
      font-size: 14px;
      font-weight: 600;
    }
  }

  &__bill-kpis {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
    margin-top: 12px;
  }

  &__bill-kpi {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
    min-height: 112px;
    padding: 18px 20px;
    overflow: hidden;
    background: linear-gradient(110deg, #ffffff 0%, #ffffff 62%, #e5faef 100%);
    border: 1px solid #e2e8f0;
    border-radius: 8px;

    span,
    small {
      display: block;
    }

    span {
      color: #0f172a;
      font-size: 15px;
      font-weight: 700;
    }

    strong {
      display: block;
      margin-top: 10px;
      color: #020617;
      font-size: 30px;
      font-weight: 800;
      line-height: 36px;
      white-space: nowrap;
    }

    small {
      margin-top: 8px;
      color: #334155;
      font-size: 13px;
      font-weight: 600;
      line-height: 18px;
    }

    .iconify {
      flex: none;
      color: var(--card-color);
    }
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
    &__bill-kpis,
    &__data-summary {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
}

@media (max-width: 768px) {
  .energy-dashboard {
    &__bill-scope {
      align-items: flex-start;
      flex-direction: column;
    }

    &__load,
    &__soc-grid,
    &__bill-kpis,
    &__data-summary {
      grid-template-columns: 1fr;
    }

    &__bill-kpi {
      min-height: auto;

      strong {
        font-size: 24px;
        line-height: 30px;
      }
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
