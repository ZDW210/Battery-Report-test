<template>
  <div class="energy-report-panel">
    <ContentWrap>
      <div class="report-header">
        <div>
          <h2>报表面板</h2>
          <p>按全部电表、项目场站或单个电表汇总用电量、任务电量、计费规则和费用组成。</p>
        </div>
        <div class="report-header__actions">
          <el-button :loading="loading" @click="loadReport">
            <Icon class="mr-5px" icon="ep:refresh" />
            刷新报表
          </el-button>
          <el-button type="primary" :disabled="selectedDevices.length === 0" @click="exportBillPdf">
            <Icon class="mr-5px" icon="ep:printer" />
            导出PDF
          </el-button>
        </div>
      </div>

      <el-form :inline="true" :model="query" class="report-filters" label-width="84px">
        <el-form-item label="查看范围">
          <el-radio-group v-model="query.scopeType" @change="handleScopeChange">
            <el-radio-button label="all">全部电表</el-radio-button>
            <el-radio-button label="project">按场地</el-radio-button>
            <el-radio-button label="device">单个电表</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="query.scopeType === 'project'" label="项目场地">
          <el-select v-model="query.projectId" class="!w-260px" filterable placeholder="请选择项目场地" @change="loadReport">
            <el-option v-for="item in projectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="query.scopeType === 'device'" label="电表">
          <el-select v-model="query.deviceId" class="!w-280px" filterable placeholder="请选择电表" @change="loadReport">
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
      <el-empty v-if="selectedDevices.length === 0" description="当前范围下暂无电表" />
      <div v-else v-loading="loading" class="report-body">
        <div class="bill-title">
          <div>
            <span>账单周期 {{ activeBillRangeText }}</span>
            <strong>{{ reportScopeTitle }}</strong>
          </div>
          <small>参考电费账单样式生成，未接入字段以“待录入”展示。</small>
        </div>

        <div class="summary-grid">
          <div v-for="item in summaryCards" :key="item.label" class="summary-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.hint }}</small>
          </div>
        </div>

        <el-row :gutter="14">
          <el-col :lg="14" :md="24" :xs="24">
            <section class="report-section">
              <div class="section-title section-title--bill">
                <h3>账单概况</h3>
                <span>单位：kWh、元/kWh、元</span>
              </div>
              <div class="bill-overview">
                <div class="bill-overview__head">
                  <span>费用组成</span>
                  <span>计费数量</span>
                  <span>计费标准</span>
                  <span>电费</span>
                </div>
                <div
                  v-for="item in billOverviewRows"
                  :key="item.category"
                  class="bill-overview__row"
                  :class="{ 'is-total': item.category === '合计' }"
                >
                  <span class="bill-overview__name">{{ item.category }}</span>
                  <span class="bill-overview__quantity">{{ item.quantity }}</span>
                  <span class="bill-overview__rate">{{ item.rate }}</span>
                  <strong class="bill-overview__amount">{{ item.amount }}</strong>
                </div>
                <div class="bill-overview__note">
                  <p>正向有功电量按当前范围内每块电表 EPI 首末差汇总；尖峰平谷分项优先读取 EPIJ、EPIF、EPIP、EPIG。</p>
                  <p>账单中未接入的电网字段不自动伪造，缺失项保持待录入或 ¥0。</p>
                </div>
              </div>
            </section>
          </el-col>
          <el-col :lg="10" :md="24" :xs="24">
            <section class="report-section">
              <div class="section-title section-title--bill">
                <h3>用能分析</h3>
                <span>按 EIOT 已接收字段测算</span>
              </div>
              <div class="energy-analysis">
                <p v-for="item in analysisRows" :key="item.title">
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.content }}</span>
                </p>
                <div class="analysis-chart">
                  <div class="analysis-chart__plot">
                    <div v-for="item in analysisBarRows" :key="item.label" class="analysis-bar">
                      <div class="analysis-bar__track">
                        <span :style="{ height: `${item.percent}%` }"></span>
                      </div>
                      <strong>{{ item.label }}</strong>
                    </div>
                  </div>
                  <div class="analysis-chart__legend">
                    <span>本期分时正向电量</span>
                    <b>{{ hasChargeTouData ? '来自电表分项字段' : '分项缺失' }}</b>
                  </div>
                </div>
              </div>
            </section>
          </el-col>
        </el-row>

        <section class="report-section">
          <div class="section-title">
            <h3>电表用电明细</h3>
            <span>期初/期末来自 EIOT 的 EPI 正向有功电能</span>
          </div>
          <el-table :data="deviceRows" border size="small" show-overflow-tooltip>
            <el-table-column label="电表" prop="deviceName" min-width="150" />
            <el-table-column label="项目场地" prop="projectName" min-width="130" />
            <el-table-column label="仪表编号" prop="meterNo" min-width="150" />
            <el-table-column label="期初累计电能" prop="startEpiText" align="right" width="140" />
            <el-table-column label="期末累计电能" prop="endEpiText" align="right" width="140" />
            <el-table-column label="本期电量" prop="purchasedEnergyText" align="right" width="120" />
            <el-table-column label="放出电量" prop="dischargeEnergyText" align="right" width="120" />
            <el-table-column label="购电成本" prop="purchaseCostText" align="right" width="120" />
          </el-table>
        </section>

        <section class="report-section">
          <div class="section-title">
            <h3>电量明细</h3>
            <span>接口按 EPI/EPE 与分时字段统一返回，未接入字段不伪造</span>
          </div>
          <el-table :data="energyDetailRows" border size="small" show-overflow-tooltip>
            <el-table-column label="示数类型" prop="label" min-width="150" />
            <el-table-column label="上期示数" prop="startReadingText" align="right" width="120" />
            <el-table-column label="本期示数" prop="endReadingText" align="right" width="120" />
            <el-table-column label="倍率" prop="multiplierText" align="right" width="90" />
            <el-table-column label="抄见电量" prop="copiedEnergyText" align="right" width="120" />
            <el-table-column label="变损" prop="transformerLossText" align="right" width="90" />
            <el-table-column label="线损" prop="lineLossText" align="right" width="90" />
            <el-table-column label="加减" prop="adjustmentText" align="right" width="90" />
            <el-table-column label="计费电量" prop="billingEnergyText" align="right" width="120" />
          </el-table>
        </section>

        <section class="report-section">
          <div class="section-title">
            <h3>电费明细</h3>
            <span>费用类别、费用组成、分时时段、计费电量、计费标准和电费均来自报表接口</span>
          </div>
          <el-table :data="feeDetailRows" border size="small" :row-class-name="feeDetailRowClassName" show-overflow-tooltip>
            <el-table-column label="费用类别" prop="category" min-width="160" />
            <el-table-column label="费用组成" prop="component" min-width="180" />
            <el-table-column label="分时时段" prop="period" align="center" width="100" />
            <el-table-column label="计费电量" prop="billingEnergyText" align="right" width="130" />
            <el-table-column label="计费标准" prop="rateText" align="right" width="130" />
            <el-table-column label="电费" prop="amountText" align="right" width="130" />
          </el-table>
        </section>

        <section v-if="unmatchedPricingRows.length" class="report-section">
          <div class="section-title">
            <h3>未匹配计费规则电量</h3>
            <span>以下场地或电表存在电量，但没有匹配到计费规则，暂不进入电费明细计算</span>
          </div>
          <el-table :data="unmatchedPricingRows" border size="small" show-overflow-tooltip>
            <el-table-column label="项目场地" prop="projectName" min-width="150" />
            <el-table-column label="电表" prop="deviceName" min-width="160" />
            <el-table-column label="仪表编号" prop="meterNo" min-width="160" />
            <el-table-column label="分时时段" prop="period" align="center" width="100" />
            <el-table-column label="充电电量" prop="chargeEnergyText" align="right" width="130" />
            <el-table-column label="放电电量" prop="dischargeEnergyText" align="right" width="130" />
            <el-table-column label="原因" prop="reason" min-width="140" />
          </el-table>
        </section>
      </div>
    </ContentWrap>
  </div>
</template>

<script lang="ts" setup>
import { EnergyChargeSessionApi } from '@/api/energy/chargeSession'
import type { EnergyChargeSessionVO } from '@/api/energy/chargeSession'
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyPricingRuleApi } from '@/api/energy/pricingRule'
import type { EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import { EnergyReportApi } from '@/api/energy/report'
import type { EnergyReportBillVO } from '@/api/energy/report'
import { EnergyTelemetryApi } from '@/api/energy/telemetry'
import type { EnergyTelemetryVO } from '@/api/energy/telemetry'
import { archiveReport } from '@/utils/reportArchive'
import dayjs from 'dayjs'

defineOptions({ name: 'EnergyReportPanel' })

type ScopeType = 'all' | 'project' | 'device'

const message = useMessage()
const loading = ref(false)
const devices = ref<EnergyDeviceVO[]>([])
const telemetryRows = ref<EnergyTelemetryVO[]>([])
const chargeSessions = ref<EnergyChargeSessionVO[]>([])
const pricingRules = ref<EnergyPricingRuleVO[]>([])
const billReport = ref<EnergyReportBillVO>()

const query = reactive<{
  scopeType: ScopeType
  projectId?: number
  deviceId?: number
  billMonth: string
}>({
  scopeType: 'all',
  projectId: undefined,
  deviceId: undefined,
  billMonth: dayjs().format('YYYY-MM')
})

const billRange = computed(() => {
  const month = dayjs(query.billMonth || dayjs().format('YYYY-MM'))
  return {
    start: month.startOf('month').format('YYYY-MM-DD HH:mm:ss'),
    end: month.endOf('month').format('YYYY-MM-DD HH:mm:ss')
  }
})
const billRangeText = computed(() => `${billRange.value.start.slice(0, 10)} 至 ${billRange.value.end.slice(0, 10)}`)
const activeBillRangeText = computed(() => {
  const range = billReport.value?.billRange
  return range ? `${range.start.slice(0, 10)} 至 ${range.end.slice(0, 10)}` : billRangeText.value
})

const projectOptions = computed(() => {
  const map = new Map<number, string>()
  devices.value.forEach((device) => {
    const id = numberOrNull(device.projectId)
    if (id !== null) map.set(id, device.projectName || `场地 ${id}`)
  })
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
})
const deviceOptions = computed(() => {
  return devices.value
    .filter((device) => Number.isFinite(Number(device.id)))
    .map((device) => ({ value: Number(device.id), label: deviceLabel(device) }))
})

const selectedDevices = computed(() => {
  if (query.scopeType === 'device') return devices.value.filter((device) => Number(device.id) === Number(query.deviceId))
  if (query.scopeType === 'project') return devices.value.filter((device) => Number(device.projectId) === Number(query.projectId))
  return devices.value
})
const selectedDeviceIds = computed(() => new Set(selectedDevices.value.map((device) => Number(device.id)).filter(Number.isFinite)))
const reportScopeTitle = computed(() => {
  if (billReport.value?.scopeName) return billReport.value.scopeName
  if (query.scopeType === 'device') return deviceLabel(selectedDevices.value[0]) || '单个电表'
  if (query.scopeType === 'project') return projectOptions.value.find((item) => Number(item.value) === Number(query.projectId))?.label || '场地汇总'
  return '全部电表汇总'
})

const scopedTelemetryRows = computed(() => {
  if (query.scopeType === 'all') return telemetryRows.value
  return telemetryRows.value.filter((row) => selectedDeviceIds.value.has(Number(row.deviceId)))
})
const scopedSessions = computed(() => {
  const start = dayjs(billRange.value.start).valueOf()
  const end = dayjs(billRange.value.end).valueOf()
  return chargeSessions.value.filter((item) => {
    const time = dayjs(item.startTime || item.createTime).valueOf()
    return selectedDeviceIds.value.has(Number(item.deviceId)) && time >= start && time <= end
  })
})

const deviceRows = computed(() => {
  if (billReport.value?.deviceDetails?.length) {
    return billReport.value.deviceDetails.map((item) => ({
      deviceId: item.deviceId,
      deviceName: item.deviceName || item.deviceNo || `电表 ${item.deviceId}`,
      projectName: item.projectName || '-',
      meterNo: item.meterNo || '-',
      startEpi: item.startEpi ?? null,
      endEpi: item.endEpi ?? null,
      purchasedEnergy: Number(item.chargeEnergy || 0),
      dischargeEnergy: Number(item.dischargeEnergy || 0),
      purchaseCost: item.purchaseCost ?? null,
      startEpiText: item.startEpi === null || item.startEpi === undefined ? '--' : kwhText(item.startEpi),
      endEpiText: item.endEpi === null || item.endEpi === undefined ? '--' : kwhText(item.endEpi),
      purchasedEnergyText: kwhText(Number(item.chargeEnergy || 0)),
      dischargeEnergyText: kwhText(Number(item.dischargeEnergy || 0)),
      purchaseCostText: item.purchaseCost === null || item.purchaseCost === undefined ? '待录入' : moneyText(item.purchaseCost)
    }))
  }
  return selectedDevices.value.map((device) => {
    const rows = scopedTelemetryRows.value
      .filter((row) => Number(row.deviceId) === Number(device.id))
      .filter((row) => row.collectTime)
      .sort((a, b) => dayjs(a.collectTime as string).valueOf() - dayjs(b.collectTime as string).valueOf())
    const startEpi = firstNumber(rows.map((row) => numberOrNull(row.epi)))
    const endEpi = lastNumber(rows.map((row) => numberOrNull(row.epi)))
    const startEpe = firstNumber(rows.map((row) => numberOrNull(row.epe)))
    const endEpe = lastNumber(rows.map((row) => numberOrNull(row.epe)))
    const epiDelta = startEpi !== null && endEpi !== null ? Math.max(0, endEpi - startEpi) : 0
    const epeDelta = startEpe !== null && endEpe !== null ? Math.max(0, endEpe - startEpe) : null
    const deviceSessions = scopedSessions.value.filter((item) => Number(item.deviceId) === Number(device.id))
    const sessionDischargeEnergy = sumBy(deviceSessions.filter((item) => Number(item.sessionType) === 1), 'totalEnergy')
    const dischargeEnergy = epeDelta !== null ? epeDelta : sessionDischargeEnergy
    const purchasedEnergy = Number(epiDelta.toFixed(2))
    const rule = matchRuleForDevice(device)
    const chargeTou = calcDeviceTouEnergy(device, ['epij', 'epif', 'epip', 'epig'])
    const purchaseCost = rule && Object.values(chargeTou).some((value) => value > 0)
      ? Number((
          chargeTou.sharp * (numberOrNull(rule.sharpPeakRate) || 0)
          + chargeTou.peak * (numberOrNull(rule.peakRate) || 0)
          + chargeTou.flat * (numberOrNull(rule.flatRate) || 0)
          + chargeTou.valley * (numberOrNull(rule.valleyRate) || 0)
        ).toFixed(2))
      : null
    return {
      deviceId: device.id,
      deviceName: device.deviceName || device.deviceNo || `电表 ${device.id}`,
      projectName: device.projectName || '-',
      meterNo: device.meterNo || '-',
      startEpi,
      endEpi,
      purchasedEnergy,
      dischargeEnergy,
      purchaseCost,
      startEpiText: startEpi === null ? '--' : kwhText(startEpi),
      endEpiText: endEpi === null ? '--' : kwhText(endEpi),
      purchasedEnergyText: kwhText(purchasedEnergy),
      dischargeEnergyText: kwhText(dischargeEnergy),
      purchaseCostText: purchaseCost === null ? '待录入' : moneyText(purchaseCost)
    }
  })
})

const totalPurchasedEnergy = computed(() => billReport.value?.summary ? Number(billReport.value.summary.totalChargeEnergy || 0) : Number(deviceRows.value.reduce((sum, row) => sum + row.purchasedEnergy, 0).toFixed(2)))
const totalDischargeEnergy = computed(() => billReport.value?.summary ? Number(billReport.value.summary.totalDischargeEnergy || 0) : Number(deviceRows.value.reduce((sum, row) => sum + row.dischargeEnergy, 0).toFixed(2)))
const totalPurchaseCost = computed(() => {
  if (billReport.value?.summary?.chargeCost !== undefined && billReport.value.summary.chargeCost !== null) return Number(billReport.value.summary.chargeCost || 0)
  return nullableSum(deviceRows.value.map((row) => row.purchaseCost))
})
const chargeTouEnergy = computed(() => billReport.value?.analysis?.chargeTou
  ? toLocalTou(billReport.value.analysis.chargeTou)
  : calcTouEnergy(['epij', 'epif', 'epip', 'epig']))
const dischargeTouEnergy = computed(() => billReport.value?.analysis?.dischargeTou
  ? toLocalTou(billReport.value.analysis.dischargeTou)
  : calcTouEnergy(['epej', 'epef', 'epep', 'epeg']))
const hasChargeTouData = computed(() => Object.values(chargeTouEnergy.value).some((value) => value > 0))
const hasDischargeTouData = computed(() => Object.values(dischargeTouEnergy.value).some((value) => value > 0))
const chargeTouTotal = computed(() => Number(Object.values(chargeTouEnergy.value).reduce((sum, value) => sum + value, 0).toFixed(2)))
const maxDemandKw = computed(() => calcMaxDemandKw(selectedDevices.value))
const maxDemandFee = computed(() => calcMaxDemandFee())
const maxDemandEnabled = computed(() => billableFixedFeeRules.value.some((rule) => rule.capacityBillingMode === 'maxDemand'))
const transformerCapacityEnabled = computed(() => billableFixedFeeRules.value.some((rule) => rule.capacityBillingMode === 'transformerCapacity'))
const transformerCapacityKva = computed(() => sumCapacityBillingRuleField('transformerCapacityKva'))
const transformerCapacityFee = computed(() => calcTransformerCapacityFee())
const averageBuyRate = computed(() => {
  if (!totalPurchasedEnergy.value || totalPurchaseCost.value === null) return null
  return Number((totalPurchaseCost.value / totalPurchasedEnergy.value).toFixed(4))
})
const totalRevenue = computed(() => {
  if (billReport.value?.summary?.salesRevenue !== undefined) return Number(billReport.value.summary.salesRevenue || 0)
  return calcDischargeTouRevenue()
})
const averageSellRate = computed(() => (totalDischargeEnergy.value ? Number((totalRevenue.value / totalDischargeEnergy.value).toFixed(4)) : null))
const applicableRules = computed(() => uniqueRowsById(selectedDevices.value.map(matchRuleForDevice).filter(Boolean) as EnergyPricingRuleVO[]))
const billableFixedFeeRules = computed(() => {
  if (totalPurchasedEnergy.value <= 0) return []
  if (query.scopeType !== 'device') return applicableRules.value
  return applicableRules.value.filter((rule) => Number.isFinite(Number(rule.deviceId)))
})
const billHeaderInfo = computed(() => {
  if (billReport.value?.billHeader) {
    return billReport.value.billHeader
  }
  const primaryDevice = selectedDevices.value[0]
  const primaryRule = applicableRules.value[0]
  const customerName = firstText([
    ...selectedDevices.value.map((device) => device.customerName),
    ...applicableRules.value.map((rule) => rule.customerName)
  ])
  const projectName = firstText([
    query.scopeType === 'project' ? reportScopeTitle.value : '',
    primaryDevice?.projectName,
    primaryRule?.projectName
  ])
  return {
    billStartDate: billRange.value.start.slice(0, 10),
    billEndDate: billRange.value.end.slice(0, 10),
    accountNo: '待录入',
    customerName: customerName || '待录入',
    usageAddress: projectName || '待录入',
    electricityCategory: getElectricityCategoryText(primaryRule?.electricityCategory),
    voltageLevel: getVoltageLevelText(primaryRule?.voltageLevel),
    serviceUnit: '待录入',
    marketType: query.scopeType === 'device' ? '单表统计' : query.scopeType === 'project' ? '场站汇总' : '全部电表汇总',
    printer: 'system',
    printDate: dayjs().format('YYYY-MM-DD'),
    reportNo: `ES-${dayjs().format('YYYYMMDDHHmmss')}`
  }
})

const feeTotals = computed(() => {
  const energy = totalPurchasedEnergy.value
  return {
    agentPurchase: energy * avgRuleField('agentPurchasePrice'),
    lineLoss: energy * avgRuleField('lineLossPrice'),
    transmission: energy * avgRuleField('transmissionDistributionPrice'),
    systemOperation: energy * avgRuleField('systemOperationFee'),
    governmentFund: energy * avgRuleField('governmentFundSurcharge'),
    siteFee: sumFixedRuleField('siteFee'),
    maintenanceFee: sumFixedRuleField('maintenanceFee'),
    communicationFee: sumFixedRuleField('communicationFee'),
    platformServiceFee: sumFixedRuleField('platformServiceFee'),
    batteryDepreciationCost: sumFixedRuleField('batteryDepreciationCost'),
    otherFixedFee: sumFixedRuleField('otherFixedFee'),
    maxDemandPrice: maxDemandFee.value,
    transformerCapacityPrice: transformerCapacityFee.value
  }
})
const totalBillAmount = computed(() => {
  if (billReport.value?.summary) return Number(billReport.value.summary.totalFee || 0)
  const values = Object.values(feeTotals.value)
  const composition = values.reduce((sum, value) => sum + Number(value || 0), 0)
  return Number((composition || totalPurchaseCost.value || 0).toFixed(2))
})

const summaryCards = computed(() => [
  { label: '本期电量', value: kwhText(totalPurchasedEnergy.value), hint: '按当前范围内电表 EPI 首末差汇总' },
  { label: '本期电费', value: moneyText(totalBillAmount.value), hint: '按现有计费规则可用字段测算' },
  { label: '平均购电单价', value: averageBuyRate.value === null ? '待录入' : `${numText(averageBuyRate.value)} 元/kWh`, hint: '购电成本 / 本期电量' },
  { label: '售电收入', value: moneyText(totalRevenue.value), hint: averageSellRate.value === null ? 'EPE 分时电量 × 尖峰平谷电价' : `平均售电单价 ${numText(averageSellRate.value)} 元/kWh` }
])

const energyDetailRows = computed(() => {
  if (billReport.value?.energyDetails?.length) {
    return billReport.value.energyDetails.map((row) => ({
      ...row,
      startReadingText: row.startReading === null || row.startReading === undefined ? '--' : numText(Number(row.startReading)),
      endReadingText: row.endReading === null || row.endReading === undefined ? '--' : numText(Number(row.endReading)),
      multiplierText: row.multiplier === null || row.multiplier === undefined ? '--' : numText(Number(row.multiplier)),
      copiedEnergyText: kwhText(Number(row.copiedEnergy || 0)),
      transformerLossText: kwhText(Number(row.transformerLoss || 0)),
      lineLossText: kwhText(Number(row.lineLoss || 0)),
      adjustmentText: kwhText(Number(row.adjustment || 0)),
      billingEnergyText: kwhText(Number(row.billingEnergy || 0)),
      sourceField: row.sourceField || '--'
    }))
  }
  return [
    {
      label: '正向有功（总）',
      startReadingText: deviceRows.value.length ? deviceRows.value.map((row) => row.startEpiText).join(' / ') : '--',
      endReadingText: deviceRows.value.length ? deviceRows.value.map((row) => row.endEpiText).join(' / ') : '--',
      multiplierText: '1',
      copiedEnergyText: kwhText(totalPurchasedEnergy.value),
      transformerLossText: kwhText(0),
      lineLossText: kwhText(0),
      adjustmentText: kwhText(0),
      billingEnergyText: kwhText(totalPurchasedEnergy.value),
      sourceField: 'EPI'
    },
    {
      label: '反向有功（总）',
      startReadingText: '--',
      endReadingText: '--',
      multiplierText: '1',
      copiedEnergyText: kwhText(totalDischargeEnergy.value),
      transformerLossText: kwhText(0),
      lineLossText: kwhText(0),
      adjustmentText: kwhText(0),
      billingEnergyText: kwhText(totalDischargeEnergy.value),
      sourceField: 'EPE'
    }
  ]
})

const feeDetailRows = computed(() => {
  if (billReport.value?.feeDetails?.length) return billReport.value.feeDetails.map((row) => feeDetailToRow(row))
  return feeRows.value.map((row) => ({
    category: row.category,
    component: row.remark || row.category,
    period: '--',
    billingEnergyText: row.quantity,
    rateText: row.rate,
    amountText: row.amount,
    source: '前端兼容测算'
  }))
})
const unmatchedPricingRows = computed(() => (billReport.value?.unmatchedPricingDetails || []).map((row) => ({
  projectName: row.projectName || '--',
  deviceName: row.deviceName || row.deviceNo || '--',
  meterNo: row.meterNo || '--',
  period: row.period || '--',
  chargeEnergyText: kwhText(Number(row.chargeEnergy || 0)),
  dischargeEnergyText: kwhText(Number(row.dischargeEnergy || 0)),
  reason: row.reason || '未匹配计费规则'
})))

const feeRows = computed(() => [
  ...(billReport.value?.feeDetails?.length
    ? buildBillOverviewRowsFromDetails(billReport.value.feeDetails)
    : [
        feeRow('市场化购电费', totalPurchasedEnergy.value, avgRuleField('agentPurchasePrice'), feeTotals.value.agentPurchase, '代理购电价格 × 本期电量'),
        feeRow('上网环节线损费用', totalPurchasedEnergy.value, avgRuleField('lineLossPrice'), feeTotals.value.lineLoss, '线损电价 × 本期电量'),
        feeRow('输配电量电费', totalPurchasedEnergy.value, avgRuleField('transmissionDistributionPrice'), feeTotals.value.transmission, '输配电价 × 本期电量'),
        feeRow('系统运行费', totalPurchasedEnergy.value, avgRuleField('systemOperationFee'), feeTotals.value.systemOperation, '系统运行费折价 × 本期电量'),
        feeRow('政府性基金及附加', totalPurchasedEnergy.value, avgRuleField('governmentFundSurcharge'), feeTotals.value.governmentFund, '基金及附加 × 本期电量'),
        demandFeeRow(),
        transformerCapacityFeeRow(),
        fixedFeeRow('场地费', feeTotals.value.siteFee, fixedFeeRemark('固定费用')),
        fixedFeeRow('运维费', feeTotals.value.maintenanceFee, fixedFeeRemark('固定费用')),
        fixedFeeRow('通信费', feeTotals.value.communicationFee, fixedFeeRemark('固定费用')),
        fixedFeeRow('平台服务费', feeTotals.value.platformServiceFee, fixedFeeRemark('固定费用')),
        fixedFeeRow('电池折旧成本', feeTotals.value.batteryDepreciationCost, fixedFeeRemark('固定费用')),
        fixedFeeRow('其他固定费用', feeTotals.value.otherFixedFee, fixedFeeRemark('固定费用')),
        { category: '合计', quantity: kwhText(totalPurchasedEnergy.value), rate: '--', amount: moneyText(totalBillAmount.value), remark: '当前项目可用字段合计' }
      ])
])

const billOverviewRows = computed(() => feeRows.value.map((row) => ({
  category: row.category,
  quantity: row.quantity,
  rate: row.rate,
  amount: row.amount
})))

const analysisRows = computed(() => [
  {
    title: '1. 本期电量',
    content: `本期 ${billRangeText.value} 的电量为 ${kwhText(totalPurchasedEnergy.value)}，当前范围包含 ${selectedDevices.value.length} 块电表。`
  },
  {
    title: '2. 峰谷比例',
    content: hasChargeTouData.value
      ? `已接收分时字段，尖峰 ${kwhText(chargeTouEnergy.value.sharp)}、高峰 ${kwhText(chargeTouEnergy.value.peak)}、平时 ${kwhText(chargeTouEnergy.value.flat)}、低谷 ${kwhText(chargeTouEnergy.value.valley)}；分项合计 ${kwhText(chargeTouTotal.value)}。`
      : '当前范围内未接收到 EPIJ、EPIF、EPIP、EPIG 的有效差值，先按 EPI 总量展示。'
  },
  {
    title: '3. 功率因数',
    content: `本期最新功率因数 ${latestPowerFactorText()}，可用于后续功率因数调整电费扩展。`
  },
  {
    title: '4. 平均电价',
    content: `本期平均购电单价 ${averageBuyRate.value === null ? '待录入' : `${numText(averageBuyRate.value)} 元/kWh`}。`
  },
  {
    title: '5. 放电收益',
    content: hasDischargeTouData.value
      ? `本期放电量 ${kwhText(totalDischargeEnergy.value)}，售电收入 ${moneyText(totalRevenue.value)}，按 EPEJ×尖电价、EPEF×峰电价、EPEP×平电价、EPEG×谷电价计算。`
      : `当前范围内未接收到 EPEJ、EPEF、EPEP、EPEG 的有效差值，售电收入暂为 ${moneyText(totalRevenue.value)}。`
  }
])

const analysisBarRows = computed(() => {
  const rows = [
    { label: '尖', value: chargeTouEnergy.value.sharp },
    { label: '峰', value: chargeTouEnergy.value.peak },
    { label: '平', value: chargeTouEnergy.value.flat },
    { label: '谷', value: chargeTouEnergy.value.valley }
  ]
  const max = Math.max(...rows.map((row) => row.value), 1)
  return rows.map((row) => ({
    ...row,
    percent: Math.max(4, Math.round((row.value / max) * 100))
  }))
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
    const telemetryParams: Record<string, unknown> = {
      collectTime: [billRange.value.start, billRange.value.end],
      limit: 5000
    }
    if (query.scopeType === 'device' && query.deviceId) telemetryParams.deviceId = query.deviceId
    const reportParams = {
      scopeType: query.scopeType,
      projectId: query.scopeType === 'project' ? query.projectId : undefined,
      deviceId: query.scopeType === 'device' ? query.deviceId : undefined,
      billMonth: query.billMonth
    }
    const [telemetry, sessions, rules, report] = await Promise.all([
      EnergyTelemetryApi.getTelemetryChart(telemetryParams),
      EnergyChargeSessionApi.getChargeSessionPage({ pageNo: 1, pageSize: 5000 }),
      EnergyPricingRuleApi.getPricingRulePage({ pageNo: 1, pageSize: 5000 }),
      EnergyReportApi.getBillReport(reportParams)
    ])
    telemetryRows.value = telemetry || []
    chargeSessions.value = sessions?.list || []
    pricingRules.value = rules?.list || []
    billReport.value = report
  } catch (error) {
    billReport.value = undefined
    message.error('报表加载失败，请检查电表、遥测、任务和计费规则数据')
  } finally {
    loading.value = false
  }
}

const handleScopeChange = () => {
  ensureScopeSelection()
  loadReport()
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

const exportBillPdf = async () => {
  const html = buildPrintableBillHtml()
  const filename = `用电电量报表_${query.billMonth}_${reportScopeTitle.value}.html`.replace(/[\\/:*?"<>|]/g, '_')
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  void archiveReport(filename, blob, '报表面板导出').catch(() => undefined)
  const win = window.open('', '_blank')
  if (!win) {
    message.warning('浏览器拦截了导出窗口，请允许弹窗后重试')
    return
  }
  win.document.open()
  win.document.write(html)
  win.document.close()
  win.focus()
  setTimeout(() => win.print(), 300)
}

const buildPrintableBillHtml = () => {
  const header = billHeaderInfo.value
  const detailRows = deviceRows.value
    .map(
      (row) => `<tr><td>${escapeHtml(row.deviceName)}</td><td>${escapeHtml(row.projectName)}</td><td>${escapeHtml(row.meterNo)}</td><td>${row.startEpiText}</td><td>${row.endEpiText}</td><td>${row.purchasedEnergyText}</td><td>${row.purchaseCostText}</td></tr>`
    )
    .join('')
  const energyHtml = energyDetailRows.value
    .map((row) => `<tr><td>${escapeHtml(row.label)}</td><td>${escapeHtml(row.startReadingText)}</td><td>${escapeHtml(row.endReadingText)}</td><td>${escapeHtml(row.multiplierText)}</td><td>${escapeHtml(row.copiedEnergyText)}</td><td>${escapeHtml(row.transformerLossText)}</td><td>${escapeHtml(row.lineLossText)}</td><td>${escapeHtml(row.adjustmentText)}</td><td>${escapeHtml(row.billingEnergyText)}</td></tr>`)
    .join('')
  const feeHtml = feeDetailRows.value
    .map((row) => `<tr class="${row.category === '合计' ? 'total' : row.source === '分组标题' ? 'group' : ''}"><td>${escapeHtml(row.category)}</td><td>${escapeHtml(row.component)}</td><td>${escapeHtml(row.period)}</td><td>${escapeHtml(row.billingEnergyText)}</td><td>${escapeHtml(row.rateText)}</td><td>${escapeHtml(row.amountText)}</td></tr>`)
    .join('')
  const unmatchedHtml = unmatchedPricingRows.value
    .map((row) => `<tr><td>${escapeHtml(row.projectName)}</td><td>${escapeHtml(row.deviceName)}</td><td>${escapeHtml(row.meterNo)}</td><td>${escapeHtml(row.period)}</td><td>${escapeHtml(row.chargeEnergyText)}</td><td>${escapeHtml(row.dischargeEnergyText)}</td><td>${escapeHtml(row.reason)}</td></tr>`)
    .join('')
  const unmatchedSectionHtml = unmatchedHtml
    ? `<div class="section-title">未匹配计费规则电量</div><table><thead><tr><th>项目场地</th><th>电表</th><th>仪表编号</th><th>分时时段</th><th>充电电量</th><th>放电电量</th><th>原因</th></tr></thead><tbody>${unmatchedHtml}</tbody></table>`
    : ''
  const overviewHtml = billOverviewRows.value
    .map((row) => `<tr class="${row.category === '合计' ? 'total' : ''}"><td>${escapeHtml(row.category)}</td><td>${escapeHtml(row.quantity)}</td><td>${escapeHtml(row.rate)}</td><td>${escapeHtml(row.amount)}</td></tr>`)
    .join('')
  const analysisHtml = analysisRows.value.map((row) => `<p><b>${row.title}</b><br>${row.content}</p>`).join('')
  const analysisBarsHtml = analysisBarRows.value
    .map((row) => {
      const barHeight = Math.max(4, Math.round((Number(row.percent) || 0) * 0.82))
      const barY = 90 - barHeight
      return `<div class="print-bar">
        <svg class="print-bar-svg" viewBox="0 0 58 96" role="img" aria-label="${escapeHtml(row.label)} ${kwhText(row.value)}">
          <line x1="4" y1="90" x2="54" y2="90" stroke="#94a3b8" stroke-width="1.2" />
          <rect x="19" y="${barY}" width="20" height="${barHeight}" rx="3" fill="#48c6c8" stroke="#0f766e" stroke-width="1" />
        </svg>
        <b>${escapeHtml(row.label)}</b>
        <em>${kwhText(row.value)}</em>
      </div>`
    })
    .join('')
  return `<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <title>用电电量报表</title>
  <style>
    @page { size: A4; margin: 16mm; }
    * { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    body { font-family: "Microsoft YaHei", Arial, sans-serif; color: #1f2937; font-size: 12px; }
    .bill-head { color: #0f766e; padding-bottom: 22px; }
    .brand-row { display: grid; grid-template-columns: 210px 1fr 220px; align-items: start; gap: 22px; }
    .brand { display: flex; align-items: center; gap: 12px; }
    .brand-mark { width: 54px; height: 54px; border-radius: 50%; border: 3px solid #0f766e; display: grid; place-items: center; font-weight: 800; font-size: 18px; }
    .brand-name strong { display: block; color: #075985; font-size: 22px; line-height: 1.15; }
    .brand-name span { display: block; margin-top: 3px; color: #0f766e; font-size: 11px; letter-spacing: 1px; }
    .bill-title-main { text-align: center; color: #006b68; }
    .bill-title-main .company { display: inline-block; min-width: 360px; border-bottom: 2px solid #334155; padding-bottom: 7px; font-size: 18px; letter-spacing: 10px; }
    .bill-title-main h1 { margin: 8px 0 0; font-size: 22px; letter-spacing: 12px; font-weight: 700; }
    .hotline { display: flex; justify-content: flex-end; gap: 10px; color: #0f766e; }
    .hotline-item { min-width: 76px; padding: 6px 9px; border: 1px solid #99f6e4; border-radius: 20px; text-align: center; }
    .hotline-item span { display: block; font-size: 11px; color: #64748b; }
    .hotline-item b { font-size: 16px; color: #0f766e; }
    .bill-info { display: grid; grid-template-columns: 250px 1fr 310px; gap: 30px; margin-top: 46px; color: #111827; }
    .period { display: grid; grid-template-columns: 76px 1fr; row-gap: 10px; align-items: center; }
    .period .date { color: #0f766e; letter-spacing: 1px; }
    .info-grid { display: grid; grid-template-columns: 86px 1fr; gap: 12px 14px; }
    .info-grid .label, .period .label { color: #9ca3af; text-align: right; letter-spacing: 1px; }
    .info-grid .value { color: #111827; line-height: 1.55; }
    .print-row { display: flex; justify-content: flex-end; gap: 34px; margin-top: 36px; color: #64748b; letter-spacing: 1px; }
    .meta { display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px 24px; border-top: 2px solid #0f766e; border-bottom: 1px solid #d1d5db; padding: 10px 0; }
    .cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin: 14px 0; }
    .card { background: #eef6ff; border: 1px solid #bfdbfe; padding: 10px; text-align: center; }
    .card span { display: block; color: #64748b; }
    .card b { display: block; font-size: 18px; margin-top: 6px; color: #0f172a; }
    .section-title { font-weight: 700; font-size: 16px; margin: 16px 0 8px; border-left: 4px solid #2563eb; padding-left: 8px; }
    table { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
    th { background: #e8f3ff; text-align: left; }
    th, td { border: 1px solid #dbe4ee; padding: 7px 8px; }
    td:nth-child(n+4), th:nth-child(n+4) { text-align: right; }
    .bill-layout { display: grid; grid-template-columns: 1.35fr .85fr; gap: 14px; align-items: start; }
    .bill-box { border: 1px solid #b7ecec; }
    .bill-box-title { display: flex; justify-content: space-between; background: #d9fbfb; color: #007273; padding: 9px 12px; font-weight: 800; font-size: 16px; }
    .bill-overview-table th { color: #334155; background: #f8ffff; }
    .bill-overview-table td:nth-child(2), .bill-overview-table td:nth-child(3), .bill-overview-table td:nth-child(4), .bill-overview-table th:nth-child(2), .bill-overview-table th:nth-child(3), .bill-overview-table th:nth-child(4) { text-align: right; }
    .bill-overview-table .total td { font-weight: 800; color: #0f172a; background: #f8ffff; }
    tr.group td { background: #f8ffff; color: #0f766e; font-weight: 800; }
    tr.total td { background: #f8ffff; color: #0f172a; font-weight: 800; }
    .analysis { padding: 10px 12px 12px; }
    .analysis p { margin: 0 0 9px; line-height: 1.55; }
    .analysis p b { color: #007273; }
    .print-bars { display: flex; gap: 18px; align-items: end; margin-top: 10px; padding: 8px 12px 0; border-top: 1px dashed #9adcdc; }
    .print-bar { flex: 1; text-align: center; color: #475569; }
    .print-bar-svg { display: block; width: 58px; height: 96px; margin: 0 auto; overflow: visible; }
    .print-bar b, .print-bar em { display: block; margin-top: 4px; font-style: normal; }
    .note { color: #64748b; margin-top: 14px; line-height: 1.7; }
  </style>
</head>
<body>
  <header class="bill-head">
    <div class="brand-row">
      <div class="brand">
        <div class="brand-mark">ES</div>
        <div class="brand-name">
          <strong>移动储能</strong>
          <span>ENERGY STORAGE</span>
        </div>
      </div>
      <div class="bill-title-main">
        <div class="company">移动储能运营管理平台</div>
        <h1>电费账单</h1>
      </div>
      <div class="hotline">
        <div class="hotline-item"><span>客户服务</span><b>待录入</b></div>
        <div class="hotline-item"><span>监管电话</span><b>待录入</b></div>
      </div>
    </div>
    <div class="bill-info">
      <div class="period">
        <div class="label">账单周期</div>
        <div class="date">${header.billStartDate}</div>
        <div></div>
        <div class="label" style="text-align:left;">至</div>
        <div></div>
        <div class="date">${header.billEndDate}</div>
      </div>
      <div class="info-grid">
        <div class="label">户号</div><div class="value">${escapeHtml(header.accountNo)}</div>
        <div class="label">户名</div><div class="value">${escapeHtml(header.customerName)}</div>
        <div class="label">用电地址</div><div class="value">${escapeHtml(header.usageAddress)}</div>
      </div>
      <div class="info-grid">
        <div class="label">用电类别</div><div class="value">${escapeHtml(header.electricityCategory)}</div>
        <div class="label">电压等级</div><div class="value">${escapeHtml(header.voltageLevel)}</div>
        <div class="label">供电服务单位</div><div class="value">${escapeHtml(header.serviceUnit)}</div>
        <div class="label">市场化属性</div><div class="value">${escapeHtml(header.marketType)}</div>
      </div>
    </div>
    <div class="print-row">
      <span>打印人：${escapeHtml(header.printer)}</span>
      <span>账单打印日期：${header.printDate}</span>
      <span>报表编号：${header.reportNo}</span>
    </div>
  </header>
  <div class="meta">
    <div>账单周期：${billRangeText.value}</div>
    <div>统计范围：${escapeHtml(reportScopeTitle.value)}</div>
    <div>电表数量：${selectedDevices.value.length}</div>
    <div>生成日期：${dayjs().format('YYYY-MM-DD HH:mm:ss')}</div>
  </div>
  <div class="cards">
    <div class="card"><span>本期电量</span><b>${kwhText(totalPurchasedEnergy.value)}</b></div>
    <div class="card"><span>本期电费</span><b>${moneyText(totalBillAmount.value)}</b></div>
    <div class="card"><span>平均购电单价</span><b>${averageBuyRate.value === null ? '待录入' : `${numText(averageBuyRate.value)} 元/kWh`}</b></div>
    <div class="card"><span>售电收入</span><b>${moneyText(totalRevenue.value)}</b></div>
  </div>
  <div class="bill-layout">
    <section class="bill-box">
      <div class="bill-box-title"><span>账单概况</span><small>单位：kWh、元/kWh、元</small></div>
      <table class="bill-overview-table"><thead><tr><th>费用组成</th><th>计费数量</th><th>计费标准</th><th>电费</th></tr></thead><tbody>${overviewHtml}</tbody></table>
    </section>
    <section class="bill-box">
      <div class="bill-box-title"><span>用能分析</span><small>${hasChargeTouData.value ? '来自电表分项字段' : '分项缺失'}</small></div>
      <div class="analysis">${analysisHtml}</div>
      <div class="print-bars">${analysisBarsHtml}</div>
    </section>
  </div>
  <div class="section-title">电量明细</div>
  <table><thead><tr><th>示数类型</th><th>上期示数</th><th>本期示数</th><th>倍率</th><th>抄见电量</th><th>变损</th><th>线损</th><th>加减</th><th>计费电量</th></tr></thead><tbody>${energyHtml}</tbody></table>
  <div class="section-title">电表用电明细</div>
  <table><thead><tr><th>电表</th><th>项目场地</th><th>仪表编号</th><th>期初累计</th><th>期末累计</th><th>本期电量</th><th>购电成本</th></tr></thead><tbody>${detailRows}</tbody></table>
  <div class="section-title">电费明细</div>
  <table><thead><tr><th>费用类别</th><th>费用组成</th><th>分时时段</th><th>计费电量</th><th>计费标准</th><th>电费</th></tr></thead><tbody>${feeHtml}</tbody></table>
  ${unmatchedSectionHtml}
  <div class="note">备注：当前项目未接入电网账单中的上期示数、倍率、变损、线损、峰平谷真实分项、功率因数调整等字段，因此导出报表仅展示项目已接入和已录入的数据。</div>
</body>
</html>`
}

const matchRuleForDevice = (device: EnergyDeviceVO) => {
  const deviceId = Number(device.id)
  const projectId = Number(device.projectId)
  const customerId = Number(device.customerId)
  return pricingRules.value
    .filter((rule) => Number(rule.status) === 0)
    .filter((rule) => {
      if (rule.deviceId) return Number(rule.deviceId) === deviceId
      if (rule.projectId) return Number(rule.projectId) === projectId
      if (rule.customerId) return Number(rule.customerId) === customerId
      return false
    })
    .sort((a, b) => scopeWeight(b) - scopeWeight(a) || Number(b.id || 0) - Number(a.id || 0))[0]
}

const scopeWeight = (rule: EnergyPricingRuleVO) => (rule.deviceId ? 3 : rule.projectId ? 2 : rule.customerId ? 1 : 0)
const avgRuleField = (field: keyof EnergyPricingRuleVO) => average(applicableRules.value.map((rule) => numberOrNull(rule[field]))) || 0
const sumFixedRuleField = (field: keyof EnergyPricingRuleVO) => Number(billableFixedFeeRules.value.reduce((sum, rule) => sum + Number(rule[field] || 0), 0).toFixed(2))
const sumCapacityBillingRuleField = (field: keyof EnergyPricingRuleVO) => Number(billableFixedFeeRules.value
  .filter((rule) => rule.capacityBillingMode === 'transformerCapacity')
  .reduce((sum, rule) => sum + Number(rule[field] || 0), 0)
  .toFixed(2))
const calcTouEnergy = (fields: Array<keyof EnergyTelemetryVO>) => {
  const [sharpField, peakField, flatField, valleyField] = fields
  return {
    sharp: calcTelemetryDelta(sharpField),
    peak: calcTelemetryDelta(peakField),
    flat: calcTelemetryDelta(flatField),
    valley: calcTelemetryDelta(valleyField)
  }
}
const calcTelemetryDelta = (field: keyof EnergyTelemetryVO) => {
  const total = selectedDevices.value.reduce((sum, device) => sum + calcDeviceTelemetryDelta(device, field), 0)
  return Number(total.toFixed(2))
}
const calcDeviceTouEnergy = (device: EnergyDeviceVO, fields: Array<keyof EnergyTelemetryVO>) => {
  const [sharpField, peakField, flatField, valleyField] = fields
  return {
    sharp: calcDeviceTelemetryDelta(device, sharpField),
    peak: calcDeviceTelemetryDelta(device, peakField),
    flat: calcDeviceTelemetryDelta(device, flatField),
    valley: calcDeviceTelemetryDelta(device, valleyField)
  }
}
const calcDeviceTelemetryDelta = (device: EnergyDeviceVO, field: keyof EnergyTelemetryVO) => {
  const rows = scopedTelemetryRows.value
    .filter((row) => Number(row.deviceId) === Number(device.id))
    .filter((row) => row.collectTime)
    .sort((a, b) => dayjs(a.collectTime as string).valueOf() - dayjs(b.collectTime as string).valueOf())
  const startValue = firstNumber(rows.map((row) => numberOrNull(row[field])))
  const endValue = lastNumber(rows.map((row) => numberOrNull(row[field])))
  if (startValue === null || endValue === null) return 0
  return Number(Math.max(0, endValue - startValue).toFixed(2))
}
const calcDischargeTouRevenue = () => {
  const total = selectedDevices.value.reduce((sum, device) => {
    const rule = matchRuleForDevice(device)
    const energy = calcDeviceTouEnergy(device, ['epej', 'epef', 'epep', 'epeg'])
    return sum
      + energy.sharp * (numberOrNull(rule?.sharpPeakRate) || 0)
      + energy.peak * (numberOrNull(rule?.peakRate) || 0)
      + energy.flat * (numberOrNull(rule?.flatRate) || 0)
      + energy.valley * (numberOrNull(rule?.valleyRate) || 0)
  }, 0)
  return Number(total.toFixed(2))
}
const calcMaxDemandFee = () => {
  const groups = new Map<string, { rule?: EnergyPricingRuleVO; devices: EnergyDeviceVO[] }>()
  selectedDevices.value.forEach((device) => {
    const rule = matchRuleForDevice(device)
    if (rule?.capacityBillingMode !== 'maxDemand') return
    const key = rule?.id ? `rule:${rule.id}` : `device:${device.id}`
    const group = groups.get(key) || { rule, devices: [] }
    group.devices.push(device)
    groups.set(key, group)
  })
  const total = Array.from(groups.values()).reduce((sum, group) => {
    const demand = calcMaxDemandKw(group.devices)
    const price = numberOrNull(group.rule?.maxDemandPrice) || 0
    return sum + demand * price
  }, 0)
  return Number(total.toFixed(2))
}
const calcTransformerCapacityFee = () => {
  const total = billableFixedFeeRules.value
    .filter((rule) => rule.capacityBillingMode === 'transformerCapacity')
    .reduce((sum, rule) => {
    const capacity = numberOrNull(rule.transformerCapacityKva) || 0
    const price = numberOrNull(rule.transformerCapacityPrice) || 0
    return sum + capacity * price
  }, 0)
  return Number(total.toFixed(2))
}
const calcMaxDemandKw = (devicesForDemand: EnergyDeviceVO[]) => {
  const deviceIds = new Set(devicesForDemand.map((device) => Number(device.id)).filter(Number.isFinite))
  const bucketMs = 5 * 60 * 1000
  const windowMs = 15 * 60 * 1000
  const buckets = new Map<number, number>()
  scopedTelemetryRows.value.forEach((row) => {
    if (!deviceIds.has(Number(row.deviceId))) return
    if (!row.collectTime) return
    const power = numberOrNull(row.p)
    if (power === null) return
    const time = dayjs(row.collectTime as string).valueOf()
    if (!Number.isFinite(time)) return
    const bucket = Math.floor(time / bucketMs) * bucketMs
    buckets.set(bucket, (buckets.get(bucket) || 0) + Math.max(0, power))
  })
  const points = Array.from(buckets.entries())
    .map(([time, power]) => ({ time, power }))
    .sort((a, b) => a.time - b.time)
  if (!points.length) return 0
  const maxDemand = points.reduce((max, point, index) => {
    const windowStart = point.time - windowMs + bucketMs
    const window = points.slice(0, index + 1).filter((item) => item.time >= windowStart)
    const demand = window.reduce((sum, item) => sum + item.power, 0) / window.length
    return Math.max(max, demand)
  }, 0)
  return Number(maxDemand.toFixed(2))
}
const fixedFeeRemark = (remark: string) => {
  if (totalPurchasedEnergy.value <= 0) return '本期电量为 0，固定费用不计入本期电费'
  if (query.scopeType === 'device' && billableFixedFeeRules.value.length === 0) return '单个电表不重复分摊场地级固定费用'
  return remark
}
const feeRow = (category: string, quantity: number, rate: number, amount: number, remark: string) => ({
  category,
  quantity: kwhText(quantity),
  rate: rate > 0 ? numText(rate) : '待录入',
  amount: amount > 0 ? moneyText(amount) : '¥0',
  remark
})
const fixedFeeRow = (category: string, amount: number, remark: string) => ({
  category,
  quantity: '--',
  rate: '--',
  amount: amount > 0 ? moneyText(amount) : '¥0',
  remark
})
const demandFeeRow = () => {
  const rate = avgRuleField('maxDemandPrice')
  if (!maxDemandEnabled.value) {
    return {
      category: '最大需量费用',
      quantity: '未启用',
      rate: rate > 0 ? `${numText(rate)} 元/kW·月` : '待录入',
      amount: '¥0',
      remark: '当前计费规则未选择“按最大需量”'
    }
  }
  return {
    category: '最大需量费用',
    quantity: `${numText(maxDemandKw.value)} kW`,
    rate: rate > 0 ? `${numText(rate)} 元/kW·月` : '待录入',
    amount: maxDemandFee.value > 0 ? moneyText(maxDemandFee.value) : '¥0',
    remark: '本期遥测 P 按 5 分钟聚合，并取约 15 分钟窗口平均最大需量 × 最大需量单价'
  }
}
const transformerCapacityFeeRow = () => {
  const rate = avgRuleField('transformerCapacityPrice')
  if (!transformerCapacityEnabled.value) {
    return {
      category: '变压器容量费用',
      quantity: '未启用',
      rate: rate > 0 ? `${numText(rate)} 元/kVA·月` : '待录入',
      amount: '¥0',
      remark: '当前计费规则未选择“按变压器容量”'
    }
  }
  return {
    category: '变压器容量费用',
    quantity: transformerCapacityKva.value > 0 ? `${numText(transformerCapacityKva.value)} kVA` : '待录入',
    rate: rate > 0 ? `${numText(rate)} 元/kVA·月` : '待录入',
    amount: transformerCapacityFee.value > 0 ? moneyText(transformerCapacityFee.value) : '¥0',
    remark: '按计费规则录入的变压器容量 × 变压器容量单价'
  }
}
const buildBillOverviewRowsFromDetails = (rows: Array<{
  category: string
  component: string
  period?: string
  billingEnergy?: number | null
  rate?: number | null
  amount?: number | null
  source?: string
}>) => {
  const grouped = new Map<string, { category: string; energy: number; amount: number; rates: number[] }>()
  rows.forEach((row) => {
    if (!row.component && row.source === '分组标题') return
    const key = row.category || '未分类'
    const current = grouped.get(key) || { category: key, energy: 0, amount: 0, rates: [] }
    current.energy += Number(row.billingEnergy || 0)
    current.amount += Number(row.amount || 0)
    if (row.rate !== null && row.rate !== undefined && Number.isFinite(Number(row.rate))) current.rates.push(Number(row.rate))
    grouped.set(key, current)
  })
  return Array.from(grouped.values()).map((row) => ({
    category: row.category,
    quantity: row.energy > 0 ? kwhText(row.energy) : '--',
    rate: row.rates.length ? numText(row.rates.reduce((sum, value) => sum + value, 0) / row.rates.length) : '--',
    amount: moneyText(row.amount),
    remark: '接口费用明细小计'
  }))
}

const feeDetailToRow = (row: {
  category: string
  component: string
  period?: string
  billingEnergy?: number | null
  rate?: number | null
  amount?: number | null
  source?: string
}) => ({
  category: row.category || '--',
  component: row.component || (row.source === '分组标题' ? '分组标题' : '--'),
  period: row.period || '--',
  billingEnergyText: row.billingEnergy === null || row.billingEnergy === undefined ? '--' : (row.category === '容需量费用' ? numText(Number(row.billingEnergy)) : kwhText(Number(row.billingEnergy))),
  rateText: row.rate === null || row.rate === undefined ? '--' : numText(Number(row.rate)),
  amountText: row.amount === null || row.amount === undefined ? '--' : moneyText(Number(row.amount)),
  source: row.source || ''
})
const feeDetailRowClassName = ({ row }: { row: { source?: string; category?: string } }) => {
  if (row.category === '合计') return 'is-total-row'
  if (row.source === '分组标题') return 'is-group-row'
  return ''
}
const toLocalTou = (value: { sharpPeak?: number; peak?: number; flat?: number; valley?: number; deepValley?: number }) => ({
  sharp: Number(value.sharpPeak || 0),
  peak: Number(value.peak || 0),
  flat: Number(value.flat || 0),
  valley: Number(value.valley || 0)
})
const firstText = (values: Array<unknown>) => values.map((value) => String(value || '').trim()).find(Boolean) || ''
const deviceLabel = (device?: EnergyDeviceVO) => device ? `${device.deviceName || device.deviceNo || `电表 ${device.id}`} / ${device.meterNo || '-'}` : ''
const getElectricityCategoryText = (value?: string) => {
  const options: Record<string, string> = {
    general_commercial: '一般工商业用电',
    large_industrial: '大工业用电'
  }
  return options[value || ''] || '待录入'
}
const getVoltageLevelText = (value?: string) => {
  const options: Record<string, string> = {
    under_1kv: '不满1千伏',
    '10kv': '10千伏',
    '35kv': '35千伏',
    '110kv': '110千伏',
    '220kv_plus': '220千伏及以上'
  }
  return options[value || ''] || '待录入'
}
const numberOrNull = (value: unknown) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}
const firstNumber = (values: Array<number | null>) => values.find((value) => value !== null) ?? null
const lastNumber = (values: Array<number | null>) => [...values].reverse().find((value) => value !== null) ?? null
const average = (values: Array<number | null>) => {
  const nums = values.filter((value): value is number => value !== null)
  return nums.length ? nums.reduce((sum, value) => sum + value, 0) / nums.length : null
}
const sumBy = <T extends Record<string, any>>(rows: T[], key: keyof T) => rows.reduce((sum, row) => sum + Number(row[key] || 0), 0)
const nullableSum = (values: Array<number | null>) => {
  const nums = values.filter((value): value is number => value !== null)
  return nums.length ? Number(nums.reduce((sum, value) => sum + value, 0).toFixed(2)) : null
}
const uniqueRowsById = <T extends { id?: number }>(rows: T[]) => Array.from(new Map(rows.map((row, index) => [row.id || index, row])).values())
const numText = (value: number) => Number.isInteger(value) ? String(value) : value.toFixed(2)
const kwhText = (value: number) => `${numText(value)} kWh`
const moneyText = (value: number) => `¥${numText(value)}`
const latestPowerFactorText = () => {
  const value = lastNumber(scopedTelemetryRows.value.map((row) => numberOrNull(row.pf)))
  return value === null ? '待接入' : String(value)
}
const escapeHtml = (value: unknown) => String(value ?? '').replace(/[&<>"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char] || char)

onMounted(async () => {
  await loadOptions()
  await loadReport()
})
</script>

<style lang="scss" scoped>
.energy-report-panel {
  .report-header {
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

  .report-header__actions {
    display: flex;
    gap: 10px;
  }

  .report-filters {
    margin-top: 18px;
  }

  .bill-title {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    padding-bottom: 14px;
    border-bottom: 1px solid #dbe4ee;

    span,
    small {
      color: #64748b;
    }

    strong {
      display: block;
      margin-top: 6px;
      color: #0f172a;
      font-size: 20px;
    }
  }

  .summary-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
    margin: 14px 0;
  }

  .summary-card {
    padding: 14px;
    border: 1px solid #d8e7f7;
    border-radius: 6px;
    background: #eef6ff;

    span,
    small {
      color: #64748b;
    }

    strong {
      display: block;
      margin: 8px 0 4px;
      color: #0f172a;
      font-size: 22px;
      font-weight: 800;
    }
  }

  .report-section {
    margin-top: 14px;
    padding: 14px;
    border: 1px solid #dbe4ee;
    border-radius: 6px;
    background: #fff;
  }

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    h3 {
      margin: 0;
      color: #0f172a;
      font-size: 16px;
      font-weight: 800;
    }

    span {
      color: #64748b;
      font-size: 13px;
    }
  }

  .section-title--bill {
    margin: -14px -14px 12px;
    padding: 12px 14px;
    border-bottom: 1px solid #b9eeee;
    border-radius: 6px 6px 0 0;
    background: #dffbfb;

    h3 {
      color: #007273;
      font-size: 20px;
      letter-spacing: 0;
    }
  }

  .bill-overview {
    border: 1px solid #c9f0ef;
    border-radius: 4px;
    overflow: hidden;
  }

  .bill-overview__head,
  .bill-overview__row {
    display: grid;
    grid-template-columns: minmax(170px, 1.2fr) minmax(110px, .65fr) minmax(120px, .75fr) minmax(100px, .55fr);
    align-items: center;
    column-gap: 12px;
    min-height: 34px;
    padding: 0 12px;
  }

  .bill-overview__head {
    background: #f7ffff;
    color: #334155;
    font-weight: 700;

    span:nth-child(2),
    span:nth-child(3),
    span:nth-child(4) {
      text-align: right;
    }
  }

  .bill-overview__row {
    border-top: 1px dashed #bdeeed;
    color: #334155;
    font-size: 14px;

    &.is-total {
      min-height: 42px;
      background: #f7ffff;
      color: #0f172a;
      font-size: 16px;
      font-weight: 800;
    }
  }

  :deep(.is-group-row) {
    td {
      background: #f8ffff;
      color: #0f766e;
      font-weight: 700;
    }
  }

  :deep(.is-total-row) {
    td {
      background: #f7ffff;
      color: #0f172a;
      font-weight: 800;
    }
  }

  .bill-overview__name {
    font-weight: 700;
  }

  .bill-overview__quantity,
  .bill-overview__rate,
  .bill-overview__amount {
    text-align: right;
  }

  .bill-overview__amount {
    color: #007273;
    font-weight: 800;
  }

  .bill-overview__note {
    padding: 10px 12px;
    border-top: 1px solid #c9f0ef;
    background: #fbffff;
    color: #64748b;
    font-size: 12px;
    line-height: 1.55;

    p {
      margin: 0;
    }
  }

  .energy-analysis {
    display: grid;
    gap: 8px;

    p {
      margin: 0;
      padding-bottom: 8px;
      border-bottom: 1px dashed #bdeeed;
    }

    strong {
      display: block;
      margin-bottom: 4px;
      color: #007273;
      font-size: 15px;
    }

    span {
      color: #475569;
      line-height: 1.6;
    }
  }

  .analysis-chart {
    margin-top: 4px;
    padding: 10px 10px 8px;
    border: 1px solid #d8e7f7;
    border-radius: 4px;
    background: #f8fbff;
  }

  .analysis-chart__plot {
    display: flex;
    align-items: flex-end;
    justify-content: space-around;
    height: 128px;
    gap: 12px;
    padding: 8px 6px 0;
    background:
      linear-gradient(to top, rgba(148, 163, 184, .28) 1px, transparent 1px) 0 0 / 100% 32px;
  }

  .analysis-bar {
    display: grid;
    justify-items: center;
    gap: 5px;
    width: 54px;
    color: #475569;
  }

  .analysis-bar__track {
    display: flex;
    align-items: flex-end;
    justify-content: center;
    width: 34px;
    height: 92px;
    border-bottom: 1px solid #94a3b8;

    span {
      width: 24px;
      min-height: 4px;
      border-radius: 3px 3px 0 0;
      background: #48c6c8;
      box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .45);
    }
  }

  .analysis-chart__legend {
    display: flex;
    justify-content: space-between;
    gap: 10px;
    margin-top: 8px;
    color: #64748b;
    font-size: 12px;

    b {
      color: #007273;
    }
  }
}

@media (max-width: 1200px) {
  .energy-report-panel .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .energy-report-panel {
    .bill-main,
    .bill-grid,
    .bill-layout {
      grid-template-columns: 1fr;
    }

    .bill-cards,
    .cards {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .bill-info,
    .brand-row {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .energy-report-panel {
    .report-header,
    .bill-title {
      flex-direction: column;
      align-items: flex-start;
    }

    .summary-grid {
      grid-template-columns: 1fr;
    }

    .bill-cards,
    .cards,
    .meta {
      grid-template-columns: 1fr;
    }

    .bill-overview {
      overflow-x: auto;
    }

    .bill-overview__head,
    .bill-overview__row {
      grid-template-columns: minmax(150px, 1fr) minmax(90px, .7fr) minmax(100px, .8fr) minmax(96px, .7fr);
      min-width: 480px;
      padding: 0 10px;
      column-gap: 8px;
      font-size: 12px;
    }

    .bill-overview__row.is-total {
      font-size: 14px;
    }

    .analysis-chart__plot {
      gap: 8px;
      overflow-x: auto;
      justify-content: flex-start;
    }

    .analysis-bar {
      flex: 0 0 48px;
      width: 48px;
    }
  }
}
</style>
