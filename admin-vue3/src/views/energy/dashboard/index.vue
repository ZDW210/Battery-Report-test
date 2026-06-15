<template>
  <div class="energy-dashboard">
    <div class="energy-dashboard__header">
      <div>
        <div class="energy-dashboard__eyebrow">运营面板</div>
        <h1>移动储能全局运营概览</h1>
        <p>汇总设备规模、在线状态、运行功率、告警压力和项目覆盖情况。</p>
      </div>
      <div class="energy-dashboard__actions">
        <el-button type="primary" @click="go('/energy/device')">
          <Icon class="mr-5px" icon="ep:monitor" />
          设备台账
        </el-button>
        <el-button @click="loadDashboard">
          <Icon class="mr-5px" icon="ep:refresh" />
          刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="12">
      <el-col v-for="item in statCards" :key="item.label" :lg="6" :md="12" :xs="24">
        <el-card shadow="never" class="energy-dashboard__stat">
          <div>
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.hint }}</small>
          </div>
          <Icon :icon="item.icon" :style="{ color: item.color }" :size="32" />
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

        <el-card shadow="never">
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

        <el-card shadow="never">
          <template #header>
            <div class="energy-dashboard__card-header">
              <span>业务能力状态</span>
            </div>
          </template>
          <div class="energy-dashboard__capability">
            <div v-for="item in capabilityItems" :key="item.label">
              <span>{{ item.label }}</span>
              <el-tag :type="item.type" effect="light">{{ item.status }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="energy-dashboard__data-panel energy-dashboard__bill-panel">
      <template #header>
        <div class="energy-dashboard__card-header">
          <div>
            <span>数据面板 · 用电量报表</span>
            <span class="energy-dashboard__subtext ml-8px">参数来自电表遥测、充放电任务和计费规则</span>
          </div>
          <el-button link type="primary" @click="go('/energy/telemetry')">查看详细数据</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="dataQuery" class="energy-dashboard__data-form" label-width="72px">
        <el-form-item label="电表">
          <el-select
            v-model="dataQuery.deviceId"
            class="!w-260px"
            filterable
            placeholder="选择电表"
            @change="loadDataPanel"
          >
            <el-option
              v-for="device in devices"
              :key="device.id"
              :label="device.deviceName || device.deviceNo || device.meterNo || `设备${device.id}`"
              :value="device.id"
            >
              <span>{{ device.deviceName || device.deviceNo || device.meterNo || `设备${device.id}` }}</span>
              <span class="energy-dashboard__option-extra">{{ device.projectName || device.meterNo || '' }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="账单月">
          <el-date-picker
            v-model="dataQuery.billMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择账单月份"
            class="!w-180px"
            @change="loadDataPanel"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="dataPanelLoading" @click="loadDataPanel">
            <Icon class="mr-5px" icon="ep:refresh" />
            刷新报表
          </el-button>
        </el-form-item>
      </el-form>

      <el-empty v-if="!dataQuery.deviceId" description="请先选择电表" />
      <div v-else v-loading="dataPanelLoading" class="energy-dashboard__bill">
        <div class="energy-dashboard__bill-kpis">
          <div v-for="item in billTopCards" :key="item.label" class="energy-dashboard__bill-kpi">
            <div>
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <Icon :icon="item.icon" :size="42" :style="{ color: item.color }" />
          </div>
        </div>

        <div class="energy-dashboard__bill-grid">
          <section class="energy-dashboard__bill-section energy-dashboard__bill-section--tall">
            <h3>① 电量统计（核心）</h3>
            <div class="energy-dashboard__bill-list">
              <div v-for="item in energyStatRows" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
            <Echart :options="energyPieOptions" height="210px" />
          </section>

          <section class="energy-dashboard__bill-section">
            <h3>② 电费成本</h3>
            <el-table :data="costRows" size="small" :show-header="true">
              <el-table-column label="时段" prop="period" min-width="90" />
              <el-table-column label="电量(kWh)" prop="energy" align="right" width="110" />
              <el-table-column label="单价(元)" prop="rate" align="right" width="100" />
              <el-table-column label="金额" prop="amount" align="right" width="110" />
            </el-table>
            <div class="energy-dashboard__bill-total">
              <div><span>总购电量</span><strong>{{ formatKwh(monthlyPurchasedEnergy) }}</strong></div>
              <div><span>平均购电单价</span><strong>{{ averageBuyRateText }}</strong></div>
              <div><span>购电成本</span><strong>{{ formatCurrency(purchaseCost) }}</strong></div>
            </div>
          </section>

          <section class="energy-dashboard__bill-section">
            <h3>③ 充电收益</h3>
            <div class="energy-dashboard__bill-split">
              <div class="energy-dashboard__bill-list">
                <div v-for="item in revenueRows" :key="item.label">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
              </div>
              <Echart :options="revenueBarOptions" height="210px" />
            </div>
          </section>

          <section class="energy-dashboard__bill-section">
            <h3>④ 利润分析</h3>
            <p class="energy-dashboard__formula">收益公式：利润 = 售电收入 - 购电成本 - 运营成本</p>
            <div class="energy-dashboard__profit-list">
              <div v-for="item in profitRows" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
            <div class="energy-dashboard__final-profit">最终利润：{{ finalProfitText }}</div>
          </section>

          <section class="energy-dashboard__bill-section">
            <h3>⑤ 电池收益分析</h3>
            <div class="energy-dashboard__battery-grid">
              <div v-for="item in batteryRows" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
            <div class="energy-dashboard__soc-title">电量曲线（EPI）</div>
            <Echart :options="energyLineOptions" height="210px" />
          </section>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { Echart } from '@/components/Echart'
import { EnergyAlarmApi } from '@/api/energy/alarm'
import type { EnergyAlarmVO } from '@/api/energy/alarm'
import { EnergyChargeSessionApi } from '@/api/energy/chargeSession'
import type { EnergyChargeSessionVO } from '@/api/energy/chargeSession'
import { EnergyCustomerApi } from '@/api/energy/customer'
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyPricingRuleApi } from '@/api/energy/pricingRule'
import type { EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import { EnergyProjectApi } from '@/api/energy/project'
import { EnergyTelemetryApi } from '@/api/energy/telemetry'
import type { EnergyTelemetryVO } from '@/api/energy/telemetry'
import { formatNullableDate } from '@/utils/formatTime'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

defineOptions({ name: 'EnergyDashboard' })

const router = useRouter()
const message = useMessage()

const loading = ref(true)
const dataPanelLoading = ref(false)
const devices = ref<EnergyDeviceVO[]>([])
const latestAlarms = ref<EnergyAlarmVO[]>([])
const dataPanelRows = ref<EnergyTelemetryVO[]>([])
const chargeSessionRows = ref<EnergyChargeSessionVO[]>([])
const pricingRuleRows = ref<EnergyPricingRuleVO[]>([])
const deviceTotal = ref(0)
const customerTotal = ref(0)
const projectTotal = ref(0)
const activeAlarmTotal = ref(0)

const dataQuery = reactive<{
  deviceId?: number
  billMonth: string
}>({
  deviceId: undefined,
  billMonth: dayjs().format('YYYY-MM')
})

const billRange = computed(() => {
  const month = dayjs(dataQuery.billMonth || dayjs().format('YYYY-MM'))
  return {
    start: month.startOf('month').format('YYYY-MM-DD HH:mm:ss'),
    end: month.endOf('month').format('YYYY-MM-DD HH:mm:ss')
  }
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

const sortedBillTelemetry = computed(() => {
  return [...dataPanelRows.value]
    .filter((item) => item.collectTime)
    .sort((a, b) => dayjs(a.collectTime as string).valueOf() - dayjs(b.collectTime as string).valueOf())
})

const startEpi = computed(() => firstNumber(sortedBillTelemetry.value.map((item) => normalizeNumber(item.epi))))
const endEpi = computed(() => lastNumber(sortedBillTelemetry.value.map((item) => normalizeNumber(item.epi))))
const epiDelta = computed(() => {
  if (startEpi.value === null || endEpi.value === null) return 0
  return Math.max(0, Number((endEpi.value - startEpi.value).toFixed(2)))
})

const billSessions = computed(() => {
  const start = dayjs(billRange.value.start).valueOf()
  const end = dayjs(billRange.value.end).valueOf()
  return chargeSessionRows.value.filter((item) => {
    const time = dayjs(item.startTime || item.createTime).valueOf()
    return Number(item.deviceId) === Number(dataQuery.deviceId) && time >= start && time <= end
  })
})

const selectedDevice = computed(() => devices.value.find((item) => Number(item.id) === Number(dataQuery.deviceId)))
const applicablePricingRules = computed(() => {
  const device = selectedDevice.value
  return pricingRuleRows.value.filter((rule) => {
    if (Number(rule.status) !== 0) return false
    if (rule.deviceId) return Number(rule.deviceId) === Number(dataQuery.deviceId)
    if (rule.projectId && device?.projectId) return Number(rule.projectId) === Number(device.projectId)
    if (rule.customerId && device?.customerId) return Number(rule.customerId) === Number(device.customerId)
    return !rule.deviceId && !rule.projectId && !rule.customerId
  })
})
const chargeSessions = computed(() => billSessions.value.filter((item) => Number(item.sessionType) === 2))
const dischargeSessions = computed(() => billSessions.value.filter((item) => Number(item.sessionType) === 1))

const sessionChargeEnergy = computed(() => sumBy(chargeSessions.value, 'totalEnergy'))
const sessionDischargeEnergy = computed(() => sumBy(dischargeSessions.value, 'totalEnergy'))
const monthlyPurchasedEnergy = computed(() => Number((sessionChargeEnergy.value || epiDelta.value).toFixed(2)))
const monthlySoldEnergy = computed(() => Number(sessionDischargeEnergy.value.toFixed(2)))
const monthlyRevenue = computed(() => Number(sumBy(dischargeSessions.value, 'totalFee').toFixed(2)))
const averageSellRate = computed(() => (monthlySoldEnergy.value > 0 ? monthlyRevenue.value / monthlySoldEnergy.value : 0))
const averageBuyRate = computed(() => average(applicablePricingRules.value.map((item) => normalizeNumber(item.energyRate) || undefined)))
const averageBuyRateText = computed(() => averageBuyRate.value > 0 ? `${averageBuyRate.value.toFixed(2)} 元/kWh` : '待录入')
const purchaseCost = computed(() => Number((monthlyPurchasedEnergy.value * averageBuyRate.value).toFixed(2)))
const knownProfit = computed(() => Number((monthlyRevenue.value - purchaseCost.value).toFixed(2)))
const batteryEfficiencyText = computed(() => {
  if (!monthlyPurchasedEnergy.value) return '--'
  if (monthlySoldEnergy.value > monthlyPurchasedEnergy.value) return '待补充购电量'
  return formatPercent(monthlySoldEnergy.value, monthlyPurchasedEnergy.value)
})

const billTopCards = computed(() => [
  { label: '本月购电', value: formatKwh(monthlyPurchasedEnergy.value), icon: 'ep:connection', color: '#2088d8' },
  { label: '本月售电', value: formatKwh(monthlySoldEnergy.value), icon: 'ep:truck', color: '#0ea5a4' },
  { label: '本月收益', value: formatCurrency(monthlyRevenue.value), icon: 'ep:money', color: '#16a34a' },
  { label: '本月利润', value: finalProfitText.value, icon: 'ep:trophy', color: '#f59e0b' }
])

const energyStatRows = computed(() => [
  { label: '期初电量', value: formatNullableKwh(startEpi.value) },
  { label: '期末电量', value: formatNullableKwh(endEpi.value) },
  { label: '本期充电量', value: formatKwh(monthlyPurchasedEnergy.value) },
  { label: '本期放电量', value: formatKwh(monthlySoldEnergy.value) },
  { label: '自耗电量', value: formatKwh(Math.max(0, monthlyPurchasedEnergy.value - monthlySoldEnergy.value)) },
  { label: '充放电效率', value: batteryEfficiencyText.value },
  { label: '损耗电量', value: formatKwh(Math.max(0, monthlyPurchasedEnergy.value - monthlySoldEnergy.value)) },
  { label: '峰电量', value: '待录入分时' },
  { label: '平电量', value: '待录入分时' },
  { label: '谷电量', value: '待录入分时' },
  { label: '深谷电量', value: '待录入分时' }
])

const costRows = computed(() => {
  if (averageBuyRate.value <= 0) {
    return [
      { period: '统一', energy: formatKwh(monthlyPurchasedEnergy.value), rate: '待录入', amount: '待录入' },
      { period: '峰/平/谷', energy: '待录入', rate: '待录入', amount: '待录入' }
    ]
  }
  return [
    {
      period: '统一',
      energy: numberText(monthlyPurchasedEnergy.value),
      rate: averageBuyRate.value.toFixed(2),
      amount: numberText(purchaseCost.value)
    },
    { period: '峰/平/谷', energy: '待录入', rate: '待录入', amount: '待录入' }
  ]
})

const revenueRows = computed(() => [
  { label: '服务车辆数', value: '待接入车辆' },
  { label: '充电订单数', value: billSessions.value.length },
  { label: '售电量', value: formatKwh(monthlySoldEnergy.value) },
  { label: '平均售电价', value: averageSellRate.value > 0 ? `${averageSellRate.value.toFixed(2)} 元/kWh` : '待录入' },
  { label: '售电收入', value: formatCurrency(monthlyRevenue.value) }
])

const profitRows = computed(() => [
  { label: '售电收入', value: formatCurrency(monthlyRevenue.value) },
  { label: '购电成本', value: averageBuyRate.value > 0 ? formatCurrency(purchaseCost.value) : '待录入' },
  { label: '场地费', value: '待录入' },
  { label: '运维费', value: '待录入' },
  { label: '通信费', value: '待录入' },
  { label: '平台服务费', value: '待录入' }
])

const finalProfitText = computed(() => {
  if (averageBuyRate.value <= 0) return '待录入成本'
  return `${formatCurrency(knownProfit.value)}`
})

const batteryRows = computed(() => [
  { label: '充电量', value: formatKwh(monthlyPurchasedEnergy.value) },
  { label: '放电量', value: formatKwh(monthlySoldEnergy.value) },
  { label: '循环次数', value: '待录入容量' },
  { label: '当月任务次数', value: `${billSessions.value.length} 次` },
  { label: '电池效率', value: batteryEfficiencyText.value },
  { label: '电池折旧成本', value: '待录入' },
  { label: '折旧费用', value: '待录入' }
])

const energyPieOptions = computed<EChartsOption>(() => ({
  color: ['#2088d8', '#14b8a6', '#8bdc65'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      data: [
        { name: '充电量', value: monthlyPurchasedEnergy.value },
        { name: '放电量', value: monthlySoldEnergy.value },
        { name: '损耗/自耗', value: Math.max(0, monthlyPurchasedEnergy.value - monthlySoldEnergy.value) }
      ]
    }
  ]
}))

const revenueBarOptions = computed<EChartsOption>(() => {
  const days = Array.from({ length: 7 }).map((_, index) => dayjs(billRange.value.end).subtract(6 - index, 'day'))
  const energy = days.map((day) => sumDaily(dischargeSessions.value, day, 'totalEnergy'))
  const revenue = days.map((day) => sumDaily(dischargeSessions.value, day, 'totalFee'))
  return {
    color: ['#2088d8', '#22c55e'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 36, right: 16, top: 36, bottom: 28 },
    xAxis: { type: 'category', data: days.map((day) => day.format('MM-DD')) },
    yAxis: { type: 'value' },
    series: [
      { name: '售电量', type: 'bar', data: energy },
      { name: '收入', type: 'bar', data: revenue }
    ]
  }
})

const energyLineOptions = computed<EChartsOption>(() => ({
  color: ['#2088d8'],
  tooltip: { trigger: 'axis' },
  grid: { left: 44, right: 16, top: 24, bottom: 32 },
  xAxis: { type: 'category', data: sortedBillTelemetry.value.map((item) => formatAxisTime(item.collectTime)) },
  yAxis: { type: 'value', name: 'kWh' },
  series: [
    {
      name: 'EPI',
      type: 'line',
      smooth: true,
      showSymbol: false,
      areaStyle: { opacity: 0.16 },
      data: sortedBillTelemetry.value.map((item) => normalizeNumber(item.epi))
    }
  ]
}))

const statCards = computed(() => [
  {
    label: '设备总数',
    value: deviceTotal.value,
    hint: `在线 ${onlineTotal.value} 台`,
    icon: 'ep:monitor',
    color: '#2563eb'
  },
  {
    label: '客户数量',
    value: customerTotal.value,
    hint: '已建档客户',
    icon: 'ep:user',
    color: '#0f766e'
  },
  {
    label: '项目站点',
    value: projectTotal.value,
    hint: '已建档项目',
    icon: 'ep:office-building',
    color: '#7c3aed'
  },
  {
    label: '待处理告警',
    value: activeAlarmTotal.value,
    hint: '未确认/未关闭',
    icon: 'ep:warning',
    color: '#dc2626'
  }
])

const statusBands = computed(() => {
  const bands = [
    { label: '在线', status: 0, color: '#0f766e' },
    { label: '离线', status: 1, color: '#64748b' },
    { label: '故障', status: 2, color: '#dc2626' },
    { label: '维护', status: 3, color: '#ca8a04' }
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
    2: '故障',
    3: '维护'
  }
  return status === undefined || status === null ? '未知' : statusMap[status] || `状态${status}`
}

const getDeviceStatusType = (status?: number) => {
  if (status === 0) return 'success'
  if (status === 2) return 'danger'
  if (status === 3) return 'warning'
  return 'info'
}

const capabilityItems = [
  { label: '实时监控', status: '已落地', type: 'success' },
  { label: 'EIOT 同步日志', status: '已落地', type: 'success' },
  { label: '计费规则', status: '已落地', type: 'success' },
  { label: '充放电任务', status: '待建模', type: 'info' },
  { label: '调度任务', status: '待建模', type: 'info' }
] as const

const loadDashboard = async () => {
  loading.value = true
  try {
    const [devicePage, alarmPage, customerPage, projectPage] = await Promise.all([
      EnergyDeviceApi.getDevicePage({ pageNo: 1, pageSize: 100 }),
      EnergyAlarmApi.getAlarmPage({ pageNo: 1, pageSize: 8, status: 0 }),
      EnergyCustomerApi.getCustomerPage({ pageNo: 1, pageSize: 1 }),
      EnergyProjectApi.getProjectPage({ pageNo: 1, pageSize: 1 })
    ])

    devices.value = devicePage?.list || []
    latestAlarms.value = alarmPage?.list || []
    deviceTotal.value = devicePage?.total || 0
    activeAlarmTotal.value = alarmPage?.total || 0
    customerTotal.value = customerPage?.total || 0
    projectTotal.value = projectPage?.total || 0
    if (!dataQuery.deviceId && devices.value[0]?.id) {
      dataQuery.deviceId = devices.value[0].id
    }
    await loadDataPanel()
  } catch (error) {
    message.error('运营面板数据加载失败，请检查后端服务和登录状态')
  } finally {
    loading.value = false
  }
}

const loadDataPanel = async () => {
  if (!dataQuery.deviceId || !dataQuery.billMonth) {
    dataPanelRows.value = []
    chargeSessionRows.value = []
    pricingRuleRows.value = []
    return
  }
  dataPanelLoading.value = true
  try {
    const [telemetryRows, chargeSessions, pricingRules] = await Promise.all([
      EnergyTelemetryApi.getTelemetryChart({
        deviceId: dataQuery.deviceId,
        collectTime: [billRange.value.start, billRange.value.end],
        limit: 1200
      }),
      EnergyChargeSessionApi.getChargeSessionPage({ pageNo: 1, pageSize: 1000, deviceId: dataQuery.deviceId }),
      EnergyPricingRuleApi.getPricingRulePage({ pageNo: 1, pageSize: 1000, deviceId: dataQuery.deviceId })
    ])
    dataPanelRows.value = telemetryRows || []
    chargeSessionRows.value = chargeSessions?.list || []
    pricingRuleRows.value = pricingRules?.list || []
  } catch (error) {
    dataPanelRows.value = []
    chargeSessionRows.value = []
    pricingRuleRows.value = []
    message.error('数据面板加载失败，请检查遥测数据接口')
  } finally {
    dataPanelLoading.value = false
  }
}

const average = (values: Array<number | undefined>) => {
  const valid = values.filter((value): value is number => typeof value === 'number')
  if (valid.length === 0) {
    return 0
  }
  return Number((valid.reduce((sum, value) => sum + value, 0) / valid.length).toFixed(1))
}

const valueOrDash = (value?: number) => {
  return value === undefined || value === null ? '-' : value
}

const formatDateText = (value?: string | Date) => {
  return formatNullableDate(value)
}

const formatAxisTime = (value?: string | Date) => {
  if (!value) {
    return ''
  }
  const date = dayjs(value)
  return date.isValid() ? date.format('MM-DD HH:mm') : ''
}

const normalizeNumber = (value: unknown): number | null => {
  if (value === undefined || value === null || value === '') {
    return null
  }
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : null
}

const firstNumber = (values: Array<number | null>) => values.find((value) => value !== null) ?? null
const lastNumber = (values: Array<number | null>) => [...values].reverse().find((value) => value !== null) ?? null

const sumBy = <T extends Record<string, any>>(rows: T[], key: keyof T) => {
  return rows.reduce((sum, item) => sum + Number(item[key] || 0), 0)
}

const sumDaily = (rows: EnergyChargeSessionVO[], day: dayjs.Dayjs, key: keyof EnergyChargeSessionVO) => {
  return Number(
    rows
      .filter((item) => dayjs(item.startTime || item.createTime).isSame(day, 'day'))
      .reduce((sum, item) => sum + Number(item[key] || 0), 0)
      .toFixed(2)
  )
}

const numberText = (value: number) => {
  return Number.isInteger(value) ? String(value) : value.toFixed(2)
}

const formatKwh = (value: number) => `${numberText(value)} kWh`
const formatNullableKwh = (value: number | null) => (value === null ? '--' : formatKwh(value))
const formatCurrency = (value: number) => `¥${numberText(value)}`
const formatPercent = (numerator: number, denominator: number) => {
  if (!denominator) return '--'
  return `${((numerator / denominator) * 100).toFixed(1)}%`
}

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
  &__header {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 12px;
    padding: 18px 20px;
    background: #ffffff;
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;

    h1 {
      margin: 6px 0;
      color: var(--el-text-color-primary);
      font-size: 24px;
      font-weight: 700;
      line-height: 32px;
    }

    p {
      margin: 0;
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }
  }

  &__eyebrow {
    color: var(--el-color-primary);
    font-size: 13px;
    font-weight: 600;
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 8px;
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

  &__data-panel {
    margin-top: 12px;
  }

  &__data-form {
    padding: 2px 0 8px;

    :deep(.el-form-item) {
      margin-bottom: 10px;
    }
  }

  &__option-extra {
    float: right;
    max-width: 140px;
    overflow: hidden;
    color: var(--el-text-color-secondary);
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
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

  &__bill-panel {
    :deep(.el-card__body) {
      background:
        radial-gradient(circle at 12% 0, rgba(56, 189, 248, 0.1), transparent 28%),
        radial-gradient(circle at 92% 8%, rgba(34, 197, 94, 0.12), transparent 24%),
        #f8fbff;
    }
  }

  &__bill-kpis {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 12px;
  }

  &__bill-kpi {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 116px;
    padding: 18px 20px;
    overflow: hidden;
    background:
      radial-gradient(circle at 100% 100%, rgba(34, 197, 94, 0.16), transparent 38%),
      #ffffff;
    border: 1px solid #e5edf6;
    border-radius: 8px;
    box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);

    span,
    strong {
      display: block;
    }

    span {
      color: #111827;
      font-size: 18px;
      font-weight: 700;
    }

    strong {
      margin-top: 14px;
      color: #000000;
      font-size: 32px;
      font-weight: 800;
      line-height: 38px;
      white-space: nowrap;
    }
  }

  &__bill-grid {
    display: grid;
    grid-template-columns: 1fr 1.06fr 1.06fr;
    gap: 12px;
  }

  &__bill-section {
    min-width: 0;
    padding: 14px;
    background: #ffffff;
    border: 1px solid #e5edf6;
    border-radius: 8px;
    box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);

    h3 {
      margin: 0 0 12px;
      color: #111827;
      font-size: 18px;
      font-weight: 800;
    }

    :deep(.el-table th.el-table__cell) {
      color: #1f2937;
      background: #eaf4ff;
      font-weight: 700;
    }
  }

  &__bill-section--tall {
    grid-row: span 2;
  }

  &__bill-list,
  &__profit-list {
    display: grid;
    gap: 0;

    div {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      min-height: 34px;
      padding: 7px 10px;
      border-bottom: 1px solid #edf2f7;

      &:first-child {
        background: #eaf4ff;
      }
    }

    span {
      color: #1f2937;
      font-weight: 600;
    }

    strong {
      color: #111827;
      font-weight: 700;
      text-align: right;
      white-space: nowrap;
    }
  }

  &__bill-total {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    margin-top: 10px;
    overflow: hidden;
    background: #eaf4ff;
    border-radius: 6px;

    div {
      padding: 12px;
      text-align: center;
      border-right: 1px solid #d8e7f5;

      &:last-child {
        border-right: 0;
      }
    }

    span,
    strong {
      display: block;
    }

    span {
      color: #475569;
      font-size: 13px;
      font-weight: 600;
    }

    strong {
      margin-top: 6px;
      color: #0f172a;
      font-size: 18px;
      font-weight: 800;
    }
  }

  &__bill-split {
    display: grid;
    grid-template-columns: minmax(180px, 0.85fr) minmax(240px, 1.15fr);
    gap: 12px;
    align-items: stretch;
  }

  &__formula {
    margin: -4px 0 10px;
    color: #111827;
    font-weight: 700;
  }

  &__final-profit {
    margin-top: 10px;
    padding: 12px;
    color: #000000;
    background: #eaf4ff;
    border-radius: 6px;
    font-size: 26px;
    font-weight: 900;
    text-align: center;
  }

  &__battery-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 8px;
    margin-bottom: 12px;

    div {
      min-height: 66px;
      padding: 10px;
      background: #eaf4ff;
      border-radius: 6px;
    }

    span,
    strong {
      display: block;
    }

    span {
      color: #475569;
      font-size: 13px;
      font-weight: 600;
    }

    strong {
      margin-top: 6px;
      color: #0f172a;
      font-size: 18px;
      font-weight: 800;
    }
  }

  &__soc-title {
    margin-bottom: 6px;
    color: #111827;
    font-size: 15px;
    font-weight: 700;
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

  &__capability {
    display: grid;
    gap: 10px;

    div {
      display: flex;
      align-items: center;
      justify-content: space-between;
      min-height: 36px;
      padding: 8px 10px;
      background: var(--el-fill-color-lighter);
      border-radius: 6px;
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
    &__data-summary,
    &__bill-kpis,
    &__bill-grid,
    &__bill-total,
    &__bill-split,
    &__battery-grid {
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
