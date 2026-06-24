<template>
  <div class="electricity-data">
    <ContentWrap>
      <div class="electricity-data__header">
        <div>
          <h2>电量数据</h2>
          <p>面向客户查看本期充放电量、服务电费、基础服务费和节约成本，计算口径与数据报表保持一致。</p>
        </div>
        <div class="electricity-data__actions">
          <el-button :loading="loading" @click="loadReport">
            <Icon class="mr-5px" icon="ep:refresh" />
            刷新数据
          </el-button>
          <el-button type="primary" @click="go('/energy/data-report')">
            <Icon class="mr-5px" icon="ep:tickets" />
            查看数据报表
          </el-button>
        </div>
      </div>

      <el-form :inline="true" :model="query" class="electricity-data__filters" label-width="84px">
        <el-form-item label="统计范围">
          <el-radio-group v-model="query.scopeType" @change="handleScopeChange">
            <el-radio-button label="all">全部电表</el-radio-button>
            <el-radio-button label="project">按场地</el-radio-button>
            <el-radio-button label="device">单个电表</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="query.scopeType === 'project'" label="项目场地">
          <el-select
            v-model="query.projectId"
            class="!w-260px"
            filterable
            placeholder="请选择项目场地"
            @change="loadReport"
          >
            <el-option v-for="item in projectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="query.scopeType === 'device'" label="电表">
          <el-select
            v-model="query.deviceId"
            class="!w-280px"
            filterable
            placeholder="请选择电表"
            @change="loadReport"
          >
            <el-option v-for="item in deviceOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单月份">
          <el-date-picker
            v-model="query.billMonth"
            class="!w-180px"
            type="month"
            value-format="YYYY-MM"
            @change="loadReport"
          />
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-empty v-if="selectedDevices.length === 0" description="当前范围下暂无电表数据" />
      <div v-else v-loading="loading" class="electricity-data__body">
        <div class="electricity-data__scope">
          <strong>{{ scopeTitle }}</strong>
          <span>共 {{ selectedDevices.length }} 块电表，账单月份 {{ query.billMonth }}</span>
        </div>

        <div class="electricity-data__kpis">
          <div
            v-for="item in topCards"
            :key="item.label"
            class="electricity-data__kpi"
            :style="{ '--card-color': item.color }"
          >
            <div>
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.hint }}</small>
            </div>
            <Icon :icon="item.icon" :size="38" />
          </div>
        </div>

        <div class="electricity-data__grid">
          <section class="electricity-data__section">
            <h3>电量汇总</h3>
            <div class="electricity-data__list">
              <div v-for="item in energyRows" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
            <Echart :options="energyPieOptions" height="220px" />
          </section>

          <section class="electricity-data__section">
            <h3>分时服务电费</h3>
            <el-table :data="metrics.touRows" size="small" class="electricity-data__table">
              <el-table-column label="分时时段" prop="label" min-width="90" />
              <el-table-column label="计费电量(kWh)" align="right" min-width="120">
                <template #default="{ row }">{{ numberText(row.energy) }}</template>
              </el-table-column>
              <el-table-column label="计费标准(元/kWh)" align="right" min-width="130">
                <template #default="{ row }">{{ rateText(row.rate) }}</template>
              </el-table-column>
              <el-table-column label="电费(元)" align="right" min-width="110">
                <template #default="{ row }">{{ numberText(row.amount) }}</template>
              </el-table-column>
            </el-table>
            <div class="electricity-data__total">
              <div><span>分时合计</span><strong>{{ moneyText(metrics.touAmount) }}</strong></div>
              <div><span>本期电费</span><strong>{{ moneyText(metrics.payableAmount) }}</strong></div>
            </div>
          </section>

          <section class="electricity-data__section">
            <h3>保底与基础服务费</h3>
            <div class="electricity-data__list">
              <div v-for="item in serviceRows" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
            <p class="electricity-data__formula">
              本期电费 = MAX（约定保底用电量 × 本期平均单价，实际分时服务电费） + 基础服务费。
            </p>
          </section>

          <section class="electricity-data__section">
            <h3>用能成本对比</h3>
            <div class="electricity-data__compare">
              <div>
                <span>若自购柴油发电成本</span>
                <strong>{{ moneyText(metrics.dieselCost) }}</strong>
              </div>
              <div>
                <span>若直接接入电网估算</span>
                <strong>{{ moneyText(metrics.gridCost) }}</strong>
              </div>
              <div class="is-saving">
                <span>使用移动储能节约成本</span>
                <strong>{{ moneyText(metrics.savedCost) }}</strong>
              </div>
            </div>
          </section>
        </div>

        <section class="electricity-data__trend">
          <div class="electricity-data__section-head">
            <h3>按日费用趋势</h3>
            <span>用于查看每日充电总成本与节约成本变化，月度合计以数据报表算法为准。</span>
          </div>
          <el-empty v-if="dailyRows.length === 0" description="暂无按日费用数据" />
          <Echart v-else :options="dailyChartOptions" height="320px" />
        </section>
      </div>
    </ContentWrap>
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
import {
  buildProjectOptionsFromDevices,
  calculateCustomerBillMetrics,
  deviceDisplayLabel,
  kwhText,
  moneyText,
  numberOrNull,
  numberText,
  rateText,
  selectDevicesByScope
} from '@/views/energy/shared/billMetrics'
import type { ElectricityScopeType } from '@/views/energy/shared/billMetrics'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

defineOptions({ name: 'EnergyElectricityData' })

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const devices = ref<EnergyDeviceVO[]>([])
const pricingRules = ref<EnergyPricingRuleVO[]>([])
const billReport = ref<EnergyReportBillVO>()
const dailyRows = ref<EnergyReportDailyCostRowVO[]>([])

const query = reactive<{
  scopeType: ElectricityScopeType
  projectId?: number
  deviceId?: number
  billMonth: string
}>({
  scopeType: 'all',
  projectId: undefined,
  deviceId: undefined,
  billMonth: dayjs().format('YYYY-MM')
})

const projectOptions = computed(() => buildProjectOptionsFromDevices(devices.value))
const deviceOptions = computed(() => devices.value.map((device) => ({ value: Number(device.id), label: deviceDisplayLabel(device) })))
const selectedDevices = computed(() => selectDevicesByScope(devices.value, query.scopeType, query.projectId, query.deviceId))
const metrics = computed(() => calculateCustomerBillMetrics({
  billReport: billReport.value,
  pricingRules: pricingRules.value,
  devices: selectedDevices.value
}))

const scopeTitle = computed(() => {
  if (billReport.value?.scopeName) return billReport.value.scopeName
  if (query.scopeType === 'device') return selectedDevices.value[0]?.deviceName || selectedDevices.value[0]?.deviceNo || '单个电表'
  if (query.scopeType === 'project') return projectOptions.value.find((item) => Number(item.value) === Number(query.projectId))?.label || '项目场地'
  return '全部电表汇总'
})

const topCards = computed(() => [
  {
    label: '充入电量合计',
    value: kwhText(metrics.value.totalChargeEnergy),
    hint: '按正向有功电能汇总',
    icon: 'ep:connection',
    color: '#2563eb'
  },
  {
    label: '放出电量合计',
    value: kwhText(metrics.value.totalDischargeEnergy),
    hint: '按反向有功电能汇总',
    icon: 'ep:truck',
    color: '#0f766e'
  },
  {
    label: '本期电费',
    value: moneyText(metrics.value.payableAmount),
    hint: '分时服务电费、保底和基础服务费合计',
    icon: 'ep:money',
    color: '#16a34a'
  },
  {
    label: '节约成本',
    value: moneyText(metrics.value.savedCost),
    hint: '按数据报表成本对比口径计算',
    icon: 'ep:trophy',
    color: '#f59e0b'
  }
])

const energyRows = computed(() => [
  { label: '充入电量', value: kwhText(metrics.value.totalChargeEnergy) },
  { label: '放出电量', value: kwhText(metrics.value.totalDischargeEnergy) },
  { label: '本期计费电量', value: kwhText(metrics.value.totalUsage) },
  { label: '本期平均单价', value: `${rateText(metrics.value.averageServiceRate)} 元/kWh` }
])

const serviceRows = computed(() => [
  { label: '约定保底用电量', value: kwhText(metrics.value.guaranteeEnergy) },
  { label: '保底金额', value: moneyText(metrics.value.guaranteeAmount) },
  { label: '实际分时金额', value: moneyText(metrics.value.touAmount) },
  { label: '基础服务费', value: moneyText(metrics.value.baseServiceFee) }
])

const energyPieOptions = computed<EChartsOption>(() => ({
  color: ['#2563eb', '#0f766e', '#f59e0b'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '电量汇总',
      type: 'pie',
      radius: ['46%', '70%'],
      center: ['50%', '45%'],
      label: { formatter: '{b}\n{c} kWh' },
      data: [
        { name: '充入电量', value: metrics.value.totalChargeEnergy },
        { name: '放出电量', value: metrics.value.totalDischargeEnergy },
        { name: '计费电量', value: metrics.value.totalUsage }
      ]
    }
  ]
}))

const dailyChartOptions = computed<EChartsOption>(() => {
  const sortedRows = [...dailyRows.value].sort((a, b) => dayjs(a.date).valueOf() - dayjs(b.date).valueOf())
  return {
    color: ['#2563eb', '#f59e0b'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 52, right: 24, top: 48, bottom: 36 },
    xAxis: { type: 'category', data: sortedRows.map((item) => dayjs(item.date).format('MM-DD')), boundaryGap: false },
    yAxis: { type: 'value', name: '元' },
    series: [
      {
        name: '充电总成本',
        type: 'line',
        smooth: true,
        showSymbol: true,
        data: sortedRows.map((item) => numberOrNull(item.chargeCost))
      },
      {
        name: '节约成本',
        type: 'line',
        smooth: true,
        showSymbol: true,
        data: sortedRows.map((item) => numberOrNull(item.savedCost))
      }
    ]
  }
})

const loadOptions = async () => {
  devices.value = await EnergyDeviceApi.getDeviceSimpleList()
  ensureScopeSelection()
}

const loadReport = async () => {
  ensureScopeSelection()
  if (!query.billMonth || selectedDevices.value.length === 0) return
  loading.value = true
  try {
    const params = {
      scopeType: query.scopeType,
      projectId: query.scopeType === 'project' ? query.projectId : undefined,
      deviceId: query.scopeType === 'device' ? query.deviceId : undefined,
      billMonth: query.billMonth
    }
    const [rules, report, dailyReport] = await Promise.all([
      EnergyPricingRuleApi.getPricingRulePage({ pageNo: 1, pageSize: 5000 }),
      EnergyReportApi.getBillReport(params),
      EnergyReportApi.getDailyCostReport(params)
    ])
    pricingRules.value = rules?.list || []
    billReport.value = report
    dailyRows.value = dailyReport?.rows || []
  } catch (error) {
    billReport.value = undefined
    dailyRows.value = []
    message.error('电量数据加载失败，请检查电表、计费规则和报表接口')
  } finally {
    loading.value = false
  }
}

const ensureScopeSelection = () => {
  if (query.scopeType === 'project' && !projectOptions.value.some((item) => Number(item.value) === Number(query.projectId))) {
    query.projectId = projectOptions.value[0]?.value
  }
  if (query.scopeType === 'device' && !devices.value.some((item) => Number(item.id) === Number(query.deviceId))) {
    query.deviceId = devices.value[0]?.id
  }
  if (query.scopeType === 'all') {
    query.projectId = undefined
    query.deviceId = undefined
  }
}

const handleScopeChange = () => {
  ensureScopeSelection()
  loadReport()
}

const go = (path: string) => {
  router.push(path)
}

onMounted(async () => {
  await loadOptions()
  await loadReport()
})
</script>

<style lang="scss" scoped>
.electricity-data {
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;

    h2 {
      margin: 0;
      color: #0f172a;
      font-size: 22px;
      font-weight: 800;
    }

    p {
      margin: 6px 0 0;
      color: #64748b;
    }
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 10px;
  }

  &__filters {
    margin-top: 18px;
  }

  &__scope {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
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

  &__kpis {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 12px;
  }

  &__kpi {
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

  &__grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  &__section,
  &__trend {
    padding: 16px;
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 8px;

    h3 {
      margin: 0 0 14px;
      color: #0f172a;
      font-size: 16px;
      font-weight: 800;
    }
  }

  &__section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;

    h3 {
      margin: 0;
    }

    span {
      color: #64748b;
      font-size: 13px;
    }
  }

  &__list,
  &__compare {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;

    div {
      min-height: 66px;
      padding: 12px;
      background: #f8fafc;
      border-radius: 6px;
    }

    span,
    strong {
      display: block;
    }

    span {
      color: #64748b;
      font-size: 13px;
    }

    strong {
      margin-top: 8px;
      color: #0f172a;
      font-size: 20px;
      line-height: 26px;
    }

    .is-saving {
      background: #ecfdf5;
      border: 1px solid #bbf7d0;

      strong {
        color: #047857;
      }
    }
  }

  &__table {
    margin-bottom: 12px;
  }

  &__total {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;

    div {
      padding: 12px;
      background: #f8fafc;
      border-radius: 6px;
    }

    span,
    strong {
      display: block;
    }

    span {
      color: #64748b;
      font-size: 13px;
    }

    strong {
      margin-top: 8px;
      color: #0f172a;
      font-size: 20px;
    }
  }

  &__formula {
    margin: 12px 0 0;
    padding: 12px;
    color: #334155;
    background: #f8fafc;
    border-radius: 6px;
    line-height: 1.6;
  }

  &__trend {
    margin-top: 12px;
  }
}

@media (max-width: 1200px) {
  .electricity-data {
    &__kpis,
    &__grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
}

@media (max-width: 768px) {
  .electricity-data {
    &__header,
    &__scope,
    &__section-head {
      align-items: flex-start;
      flex-direction: column;
    }

    &__kpis,
    &__grid,
    &__list,
    &__compare,
    &__total {
      grid-template-columns: 1fr;
    }

    &__filters {
      :deep(.el-date-editor),
      :deep(.el-select),
      :deep(.el-radio-group) {
        width: 100% !important;
      }
    }
  }
}
</style>
