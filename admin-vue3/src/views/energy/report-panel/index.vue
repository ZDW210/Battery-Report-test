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
            <span>账单周期 {{ billRangeText }}</span>
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
              <div class="section-title">
                <h3>账单概况</h3>
                <span>单位：kWh、元、元/kWh</span>
              </div>
              <el-table :data="overviewRows" border size="small">
                <el-table-column label="项目" prop="label" min-width="180" />
                <el-table-column label="数值" prop="value" align="right" min-width="160" />
                <el-table-column label="说明" prop="remark" min-width="220" />
              </el-table>
            </section>
          </el-col>
          <el-col :lg="10" :md="24" :xs="24">
            <section class="report-section">
              <div class="section-title">
                <h3>用能分析</h3>
                <span>按当前项目已有字段测算</span>
              </div>
              <div class="analysis-list">
                <p v-for="item in analysisRows" :key="item.title">
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.content }}</span>
                </p>
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
            <el-table-column label="放电任务电量" prop="dischargeEnergyText" align="right" width="130" />
            <el-table-column label="购电单价" prop="buyRateText" align="right" width="110" />
            <el-table-column label="购电成本" prop="purchaseCostText" align="right" width="120" />
          </el-table>
        </section>

        <section class="report-section">
          <div class="section-title">
            <h3>费用组成</h3>
            <span>参考电费账单的费用组成结构，缺失项不强行伪造</span>
          </div>
          <el-table :data="feeRows" border size="small">
            <el-table-column label="费用类别" prop="category" min-width="160" />
            <el-table-column label="计费数量" prop="quantity" align="right" width="130" />
            <el-table-column label="计费标准" prop="rate" align="right" width="130" />
            <el-table-column label="金额" prop="amount" align="right" width="130" />
            <el-table-column label="说明" prop="remark" min-width="260" />
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
  return selectedDevices.value.map((device) => {
    const rows = scopedTelemetryRows.value
      .filter((row) => Number(row.deviceId) === Number(device.id))
      .filter((row) => row.collectTime)
      .sort((a, b) => dayjs(a.collectTime as string).valueOf() - dayjs(b.collectTime as string).valueOf())
    const startEpi = firstNumber(rows.map((row) => numberOrNull(row.epi)))
    const endEpi = lastNumber(rows.map((row) => numberOrNull(row.epi)))
    const epiDelta = startEpi !== null && endEpi !== null ? Math.max(0, endEpi - startEpi) : 0
    const deviceSessions = scopedSessions.value.filter((item) => Number(item.deviceId) === Number(device.id))
    const dischargeEnergy = sumBy(deviceSessions.filter((item) => Number(item.sessionType) === 1), 'totalEnergy')
    const purchasedEnergy = Number(epiDelta.toFixed(2))
    const rule = matchRuleForDevice(device)
    const buyRate = numberOrNull(rule?.energyRate)
    const purchaseCost = buyRate !== null ? Number((purchasedEnergy * buyRate).toFixed(2)) : null
    return {
      deviceId: device.id,
      deviceName: device.deviceName || device.deviceNo || `电表 ${device.id}`,
      projectName: device.projectName || '-',
      meterNo: device.meterNo || '-',
      startEpi,
      endEpi,
      purchasedEnergy,
      dischargeEnergy,
      buyRate,
      purchaseCost,
      startEpiText: startEpi === null ? '--' : kwhText(startEpi),
      endEpiText: endEpi === null ? '--' : kwhText(endEpi),
      purchasedEnergyText: kwhText(purchasedEnergy),
      dischargeEnergyText: kwhText(dischargeEnergy),
      buyRateText: buyRate === null ? '待录入' : `${numText(buyRate)} 元/kWh`,
      purchaseCostText: purchaseCost === null ? '待录入' : moneyText(purchaseCost)
    }
  })
})

const totalPurchasedEnergy = computed(() => Number(deviceRows.value.reduce((sum, row) => sum + row.purchasedEnergy, 0).toFixed(2)))
const totalDischargeEnergy = computed(() => Number(deviceRows.value.reduce((sum, row) => sum + row.dischargeEnergy, 0).toFixed(2)))
const totalPurchaseCost = computed(() => nullableSum(deviceRows.value.map((row) => row.purchaseCost)))
const averageBuyRate = computed(() => {
  if (!totalPurchasedEnergy.value || totalPurchaseCost.value === null) return null
  return Number((totalPurchaseCost.value / totalPurchasedEnergy.value).toFixed(4))
})
const totalRevenue = computed(() => Number(sumBy(scopedSessions.value.filter((item) => Number(item.sessionType) === 1), 'totalFee').toFixed(2)))
const averageSellRate = computed(() => (totalDischargeEnergy.value ? Number((totalRevenue.value / totalDischargeEnergy.value).toFixed(4)) : null))
const applicableRules = computed(() => uniqueRowsById(selectedDevices.value.map(matchRuleForDevice).filter(Boolean) as EnergyPricingRuleVO[]))
const billableFixedFeeRules = computed(() => {
  if (totalPurchasedEnergy.value <= 0) return []
  if (query.scopeType !== 'device') return applicableRules.value
  return applicableRules.value.filter((rule) => Number.isFinite(Number(rule.deviceId)))
})
const billHeaderInfo = computed(() => {
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
    maxDemandPrice: sumFixedRuleField('maxDemandPrice'),
    transformerCapacityPrice: sumFixedRuleField('transformerCapacityPrice')
  }
})
const totalBillAmount = computed(() => {
  const values = Object.values(feeTotals.value)
  const composition = values.reduce((sum, value) => sum + Number(value || 0), 0)
  return Number((composition || totalPurchaseCost.value || 0).toFixed(2))
})

const summaryCards = computed(() => [
  { label: '本期电量', value: kwhText(totalPurchasedEnergy.value), hint: '按当前范围内电表 EPI 首末差汇总' },
  { label: '本期电费', value: moneyText(totalBillAmount.value), hint: '按现有计费规则可用字段测算' },
  { label: '平均购电单价', value: averageBuyRate.value === null ? '待录入' : `${numText(averageBuyRate.value)} 元/kWh`, hint: '购电成本 / 本期电量' },
  { label: '售电收入', value: moneyText(totalRevenue.value), hint: '来自放电任务费用合计' }
])

const overviewRows = computed(() => [
  { label: '本期电量', value: kwhText(totalPurchasedEnergy.value), remark: '按当前范围内每块电表 EPI 首末差汇总' },
  { label: '放电任务电量', value: kwhText(totalDischargeEnergy.value), remark: '用于售电收入测算' },
  { label: '平均购电单价', value: averageBuyRate.value === null ? '待录入' : `${numText(averageBuyRate.value)} 元/kWh`, remark: '来自计费规则电量单价' },
  { label: '平均售电单价', value: averageSellRate.value === null ? '待录入' : `${numText(averageSellRate.value)} 元/kWh`, remark: '售电收入 / 放电任务电量' },
  { label: '本期电费测算', value: moneyText(totalBillAmount.value), remark: '费用组成可用项合计' }
])

const feeRows = computed(() => [
  feeRow('市场化购电费', totalPurchasedEnergy.value, avgRuleField('agentPurchasePrice'), feeTotals.value.agentPurchase, '代理购电价格 × 本期电量'),
  feeRow('上网环节线损费用', totalPurchasedEnergy.value, avgRuleField('lineLossPrice'), feeTotals.value.lineLoss, '线损电价 × 本期电量'),
  feeRow('输配电量电费', totalPurchasedEnergy.value, avgRuleField('transmissionDistributionPrice'), feeTotals.value.transmission, '输配电价 × 本期电量'),
  feeRow('系统运行费', totalPurchasedEnergy.value, avgRuleField('systemOperationFee'), feeTotals.value.systemOperation, '系统运行费折价 × 本期电量'),
  feeRow('政府性基金及附加', totalPurchasedEnergy.value, avgRuleField('governmentFundSurcharge'), feeTotals.value.governmentFund, '基金及附加 × 本期电量'),
  fixedFeeRow('最大需量费用', feeTotals.value.maxDemandPrice, fixedFeeRemark('当前按规则录入值展示')),
  fixedFeeRow('变压器容量费用', feeTotals.value.transformerCapacityPrice, fixedFeeRemark('当前按规则录入值展示')),
  fixedFeeRow('场地费', feeTotals.value.siteFee, fixedFeeRemark('固定费用')),
  fixedFeeRow('运维费', feeTotals.value.maintenanceFee, fixedFeeRemark('固定费用')),
  fixedFeeRow('通信费', feeTotals.value.communicationFee, fixedFeeRemark('固定费用')),
  fixedFeeRow('平台服务费', feeTotals.value.platformServiceFee, fixedFeeRemark('固定费用')),
  fixedFeeRow('电池折旧成本', feeTotals.value.batteryDepreciationCost, fixedFeeRemark('固定费用')),
  fixedFeeRow('其他固定费用', feeTotals.value.otherFixedFee, fixedFeeRemark('固定费用')),
  { category: '合计', quantity: kwhText(totalPurchasedEnergy.value), rate: '--', amount: moneyText(totalBillAmount.value), remark: '当前项目可用字段合计' }
])

const analysisRows = computed(() => [
  {
    title: '1. 本期电量',
    content: `本期 ${billRangeText.value} 的电量为 ${kwhText(totalPurchasedEnergy.value)}，当前范围包含 ${selectedDevices.value.length} 块电表。`
  },
  {
    title: '2. 峰谷比例',
    content: '当前 EIOT 报文暂未提供分时电量，峰、平、谷、深谷电量先标记为待录入。'
  },
  {
    title: '3. 功率因数',
    content: `本期最新功率因数 ${latestPowerFactorText()}，可用于后续功率因数调整电费扩展。`
  },
  {
    title: '4. 平均电价',
    content: `本期平均购电单价 ${averageBuyRate.value === null ? '待录入' : `${numText(averageBuyRate.value)} 元/kWh`}。`
  }
])

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
    const [telemetry, sessions, rules] = await Promise.all([
      EnergyTelemetryApi.getTelemetryChart(telemetryParams),
      EnergyChargeSessionApi.getChargeSessionPage({ pageNo: 1, pageSize: 5000 }),
      EnergyPricingRuleApi.getPricingRulePage({ pageNo: 1, pageSize: 5000 })
    ])
    telemetryRows.value = telemetry || []
    chargeSessions.value = sessions?.list || []
    pricingRules.value = rules?.list || []
  } catch (error) {
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
      (row) => `<tr><td>${escapeHtml(row.deviceName)}</td><td>${escapeHtml(row.projectName)}</td><td>${escapeHtml(row.meterNo)}</td><td>${row.startEpiText}</td><td>${row.endEpiText}</td><td>${row.purchasedEnergyText}</td><td>${row.buyRateText}</td><td>${row.purchaseCostText}</td></tr>`
    )
    .join('')
  const feeHtml = feeRows.value
    .map((row) => `<tr><td>${row.category}</td><td>${row.quantity}</td><td>${row.rate}</td><td>${row.amount}</td><td>${row.remark}</td></tr>`)
    .join('')
  const overviewHtml = overviewRows.value.map((row) => `<tr><td>${row.label}</td><td>${row.value}</td><td>${row.remark}</td></tr>`).join('')
  const analysisHtml = analysisRows.value.map((row) => `<p><b>${row.title}</b><br>${row.content}</p>`).join('')
  return `<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <title>用电电量报表</title>
  <style>
    @page { size: A4; margin: 16mm; }
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
    .analysis { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px 16px; }
    .analysis p { margin: 0; padding: 8px; border: 1px solid #e5e7eb; min-height: 54px; }
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
  <div class="section-title">账单概况</div>
  <table><thead><tr><th>项目</th><th>数值</th><th>说明</th></tr></thead><tbody>${overviewHtml}</tbody></table>
  <div class="section-title">用能分析</div>
  <div class="analysis">${analysisHtml}</div>
  <div class="section-title">电量明细</div>
  <table><thead><tr><th>电表</th><th>项目场地</th><th>仪表编号</th><th>期初累计</th><th>期末累计</th><th>本期电量</th><th>购电单价</th><th>购电成本</th></tr></thead><tbody>${detailRows}</tbody></table>
  <div class="section-title">费用组成</div>
  <table><thead><tr><th>费用类别</th><th>计费数量</th><th>计费标准</th><th>金额</th><th>说明</th></tr></thead><tbody>${feeHtml}</tbody></table>
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

  .analysis-list {
    display: grid;
    gap: 10px;

    p {
      margin: 0;
      padding: 10px;
      border: 1px solid #e5edf5;
      border-radius: 4px;
      background: #f8fbff;
    }

    strong {
      display: block;
      margin-bottom: 4px;
      color: #0f172a;
    }

    span {
      color: #475569;
      line-height: 1.6;
    }
  }
}

@media (max-width: 1200px) {
  .energy-report-panel .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
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
  }
}
</style>
