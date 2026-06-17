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

      </el-col>
    </el-row>

    <el-card shadow="never" class="energy-dashboard__data-panel">
      <template #header>
        <div class="energy-dashboard__card-header">
          <div>
            <span>数据面板</span>
            <span class="energy-dashboard__subtext ml-8px">按接收到的电表遥测数据绘制曲线</span>
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
        <el-form-item label="时间">
          <el-date-picker
            v-model="dataQuery.collectTime"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="!w-390px"
            @change="loadDataPanel"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="dataQuery.metricGroup">
            <el-radio-button
              v-for="group in dataMetricGroups"
              :key="group.value"
              :label="group.value"
            >
              {{ group.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="dataPanelLoading" @click="loadDataPanel">
            <Icon class="mr-5px" icon="ep:refresh" />
            刷新曲线
          </el-button>
        </el-form-item>
      </el-form>

      <div class="energy-dashboard__data-summary">
        <div v-for="item in dataPanelSummary" :key="item.label">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>

      <el-empty v-if="!dataQuery.deviceId" description="请先选择电表" />
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
import { EnergyProjectApi } from '@/api/energy/project'
import { EnergyTelemetryApi } from '@/api/energy/telemetry'
import type { EnergyTelemetryVO } from '@/api/energy/telemetry'
import { formatNullableDate } from '@/utils/formatTime'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

defineOptions({ name: 'Index' })

type MetricKey = 'pa' | 'pb' | 'pc' | 'p' | 'ua' | 'ub' | 'uc' | 'ia' | 'ib' | 'ic' | 'pf' | 'epi'
type MetricGroupValue = 'activePower' | 'phaseVoltage' | 'phaseCurrent' | 'powerFactor' | 'energy'
type MetricField = { key: MetricKey; label: string; unit: string }

const router = useRouter()
const message = useMessage()
const today = dayjs().format('YYYY-MM-DD')

const loading = ref(true)
const dataPanelLoading = ref(false)
const devices = ref<EnergyDeviceVO[]>([])
const latestAlarms = ref<EnergyAlarmVO[]>([])
const dataPanelRows = ref<EnergyTelemetryVO[]>([])
const deviceTotal = ref(0)
const projectTotal = ref(0)
const activeAlarmTotal = ref(0)

const dataMetricGroups: Array<{ value: MetricGroupValue; label: string; fields: MetricField[] }> = [
  {
    value: 'activePower',
    label: '有功功率',
    fields: [
      { key: 'pa', label: 'A相有功功率', unit: 'kW' },
      { key: 'pb', label: 'B相有功功率', unit: 'kW' },
      { key: 'pc', label: 'C相有功功率', unit: 'kW' },
      { key: 'p', label: '总有功功率', unit: 'kW' }
    ]
  },
  {
    value: 'phaseVoltage',
    label: '相电压',
    fields: [
      { key: 'ua', label: 'A相电压', unit: 'V' },
      { key: 'ub', label: 'B相电压', unit: 'V' },
      { key: 'uc', label: 'C相电压', unit: 'V' }
    ]
  },
  {
    value: 'phaseCurrent',
    label: '相电流',
    fields: [
      { key: 'ia', label: 'A相电流', unit: 'A' },
      { key: 'ib', label: 'B相电流', unit: 'A' },
      { key: 'ic', label: 'C相电流', unit: 'A' }
    ]
  },
  {
    value: 'powerFactor',
    label: '功率因数',
    fields: [{ key: 'pf', label: '总功率因数', unit: '' }]
  },
  {
    value: 'energy',
    label: '有功电能',
    fields: [{ key: 'epi', label: '正向总有功电能', unit: 'kWh' }]
  }
]

const dataQuery = reactive<{
  deviceId?: number
  collectTime: string[]
  metricGroup: MetricGroupValue
}>({
  deviceId: undefined,
  collectTime: [`${today} 00:00:00`, `${today} 23:59:59`],
  metricGroup: 'activePower'
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

const activeDataMetricGroup = computed(() => {
  return dataMetricGroups.find((item) => item.value === dataQuery.metricGroup) || dataMetricGroups[0]
})

const latestDataPanelRow = computed(() => {
  return dataPanelRows.value
    .filter((item) => item.collectTime)
    .sort((a, b) => dayjs(b.collectTime as string).valueOf() - dayjs(a.collectTime as string).valueOf())[0]
})

const dataPanelSummary = computed(() => {
  const latest = latestDataPanelRow.value
  const fields = activeDataMetricGroup.value.fields
  const latestValue = latest ? normalizeNumber(latest[fields[0].key]) : null
  return [
    { label: '采集点数', value: dataPanelRows.value.length },
    { label: '最新时间', value: formatDateText(latest?.collectTime) },
    {
      label: fields[0]?.label || '最新值',
      value: formatMetricValue(latestValue, fields[0]?.unit)
    },
    { label: '报文状态', value: latest?.state || '-' }
  ]
})

const dataPanelChartOptions = computed<EChartsOption>(() => {
  const fields = activeDataMetricGroup.value.fields
  const sortedRows = [...dataPanelRows.value]
    .filter((item) => item.collectTime)
    .sort((a, b) => dayjs(a.collectTime as string).valueOf() - dayjs(b.collectTime as string).valueOf())

  return buildLineOptions({
    xData: sortedRows.map((item) => formatAxisTime(item.collectTime)),
    series: fields.map((field) => ({
      name: field.label,
      data: sortedRows.map((item) => normalizeNumber(item[field.key]))
    })),
    yName: fields[0]?.unit || ''
  })
})

const statCards = computed(() => [
  {
    label: '设备总数',
    value: deviceTotal.value,
    hint: `在线 ${onlineTotal.value} 台`,
    icon: 'ep:monitor',
    color: '#2563eb'
  },
  {
    label: '在线率',
    value: `${onlineRate.value}%`,
    hint: '按5分钟报文判断',
    icon: 'ep:connection',
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

const loadDashboard = async () => {
  loading.value = true
  try {
    const [devicePage, alarmPage, projectPage] = await Promise.all([
      EnergyDeviceApi.getDevicePage({ pageNo: 1, pageSize: 100 }),
      EnergyAlarmApi.getAlarmPage({ pageNo: 1, pageSize: 8, status: 0 }),
      EnergyProjectApi.getProjectPage({ pageNo: 1, pageSize: 1 })
    ])

    devices.value = devicePage?.list || []
    latestAlarms.value = alarmPage?.list || []
    deviceTotal.value = devicePage?.total || 0
    activeAlarmTotal.value = alarmPage?.total || 0
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
  if (!dataQuery.deviceId || !dataQuery.collectTime?.[0] || !dataQuery.collectTime?.[1]) {
    dataPanelRows.value = []
    return
  }
  dataPanelLoading.value = true
  try {
    dataPanelRows.value = await EnergyTelemetryApi.getTelemetryChart({
      deviceId: dataQuery.deviceId,
      collectTime: dataQuery.collectTime,
      limit: 1200
    })
  } catch (error) {
    dataPanelRows.value = []
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

const formatMetricValue = (value: number | null, unit?: string) => {
  if (value === null) {
    return '-'
  }
  const text = Number.isInteger(value) ? String(value) : value.toFixed(2)
  return unit ? `${text} ${unit}` : text
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
    showSymbol: false,
    connectNulls: false,
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
