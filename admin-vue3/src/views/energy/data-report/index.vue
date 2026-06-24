<template>
  <div class="energy-data-report">
    <ContentWrap>
      <div class="report-header">
        <div>
          <h2>数据报表</h2>
          <p>按客户用电账单口径展示放电用量、分时服务费、保底电量核算和成本对比。</p>
        </div>
        <div class="report-header__actions">
          <el-button :loading="loading" @click="loadReport">
            <Icon class="mr-5px" icon="ep:refresh" />
            刷新报表
          </el-button>
          <el-button type="primary" :disabled="!billReport || selectedDevices.length === 0" :loading="exporting" @click="exportPdf">
            <Icon class="mr-5px" icon="ep:download" />
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
      <div v-else v-loading="loading" class="report-scroll">
        <section ref="billSheetRef" class="data-bill-sheet">
          <header class="bill-head bill-head--receipt">
            <div class="bill-brand-block">
              <div class="brand-mark">ES</div>
              <div>
                <strong>移动储能</strong>
                <span>ENERGY STORAGE</span>
              </div>
            </div>
            <div class="bill-title-block">
              <div class="platform-title">移动储能运营管理平台</div>
              <div class="title-line"></div>
              <h1>电费账单</h1>
            </div>
            <div class="bill-contact-block">
              <div>
                <span>客户服务</span>
                <strong>{{ customerServiceText }}</strong>
              </div>
              <div>
                <span>监督电话</span>
                <strong>{{ supervisePhoneText }}</strong>
              </div>
            </div>
          </header>

          <section class="bill-receipt-meta">
            <div class="period-block">
              <div class="period-row">
                <span>账单周期</span>
                <strong>{{ billStartDate }}</strong>
              </div>
              <div class="period-separator">至</div>
              <div class="period-row period-row--end">
                <strong>{{ billEndDate }}</strong>
              </div>
            </div>
            <div class="info-list">
              <div><span>户号</span><strong>{{ accountNoText }}</strong></div>
              <div><span>户名</span><strong>{{ customerNameText }}</strong></div>
              <div><span>用电地址</span><strong>{{ usageAddressText }}</strong></div>
            </div>
            <div class="info-list">
              <div><span>用电类别</span><strong>{{ usageCategoryText }}</strong></div>
              <div><span>电压等级</span><strong>{{ voltageLevelText }}</strong></div>
              <div><span>供电服务单位</span><strong>{{ supplyOrgText }}</strong></div>
              <div><span>市场化属性</span><strong>{{ marketAttributeText }}</strong></div>
            </div>
          </section>

          <section class="bill-print-row">
            <span>打印人：{{ printPersonText }}</span>
            <span>账单打印日期：{{ printDateText }}</span>
            <span>报表编号：{{ billNo }}</span>
          </section>

          <section class="bill-summary-strip">
            <div>
              <span>账单周期：</span><strong>{{ billRangeText }}</strong>
            </div>
            <div>
              <span>统计范围：</span><strong>{{ servicePointText }}</strong>
            </div>
            <div>
              <span>电表数量：</span><strong>{{ selectedDevices.length }}</strong>
            </div>
            <div>
              <span>生成日期：</span><strong>{{ generatedAtText }}</strong>
            </div>
          </section>

          <div class="metric-grid">
            <div class="metric-card is-soft">
              <span>本期电量</span>
              <strong>{{ kwhNumberText(totalUsage) }}</strong>
            </div>
            <div class="metric-card">
              <span>本期平均单价</span>
              <strong>{{ rateText(averageServiceRate) }}</strong>
            </div>
            <div class="metric-card is-amount">
              <span>本期电费</span>
              <strong>{{ moneyText(payableAmount) }}</strong>
            </div>
            <div class="metric-card is-due">
              <span>交费截止日期</span>
              <strong>{{ paymentDueDate }}</strong>
            </div>
          </div>

          <section class="bill-section">
            <h3>① 您本期用了多少电（按时段）</h3>
            <table class="tou-table">
              <thead>
                <tr>
                  <th>时段</th>
                  <th>时间范围</th>
                  <th>用电量（kWh）</th>
                  <th>服务单价（元/kWh）</th>
                  <th>金额（元）</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in touRows" :key="row.key">
                  <td>{{ row.label }}</td>
                  <td>{{ row.timeRange }}</td>
                  <td>{{ numberText(row.energy) }}</td>
                  <td>{{ rateText(row.rate) }}</td>
                  <td>{{ amountNumberText(row.amount) }}</td>
                </tr>
                <tr class="is-total">
                  <td>合计</td>
                  <td>--</td>
                  <td>{{ numberText(totalUsage) }}</td>
                  <td>--</td>
                  <td>{{ amountNumberText(touAmount) }}</td>
                </tr>
              </tbody>
            </table>
            <div class="bar-chart">
              <div v-for="row in touRows" :key="`bar-${row.key}`" class="bar-item">
                <span class="bar-value">{{ numberText(row.energy) }}</span>
                <div class="bar-track">
                  <i :style="{ height: `${row.percent}%` }"></i>
                </div>
                <strong>{{ row.label }}</strong>
                <small>{{ rateText(row.rate) }}元</small>
              </div>
            </div>
          </section>

          <section class="bill-section">
            <h3>② 保底电量核算</h3>
            <div class="guarantee-grid">
              <div>
                <span>约定保底用电量 / 月</span>
                <strong>{{ kwhNumberText(guaranteeEnergy) }}</strong>
              </div>
              <div>
                <span>本期实际用电量</span>
                <strong class="highlight">{{ kwhNumberText(totalUsage) }}</strong>
                <em>{{ guaranteeDiffText }}</em>
              </div>
              <div>
                <span>计费方式</span>
                <strong>{{ billingModeText }}</strong>
              </div>
            </div>
          </section>

          <section class="bill-section">
            <h3>③ 应付金额计算方式</h3>
            <div class="formula-box">
              <p>应付金额 = MAX（保底电量 × 综合单价，实际分时电量 × 各时段单价之和） + 基础服务费</p>
              <p>保底金额 = {{ numberText(guaranteeEnergy) }} kWh × {{ rateText(averageServiceRate) }} 元/kWh = {{ moneyText(guaranteeAmount) }}</p>
              <p>实际分时金额 = {{ touFormulaText }} = {{ moneyText(touAmount) }}</p>
              <strong>取较高值 → 本期应付：{{ moneyText(payableAmount) }}（含基础服务费 {{ moneyText(baseServiceFee) }}）</strong>
            </div>
          </section>

          <section class="bill-section">
            <h3>④ 用能成本对比（增值服务）</h3>
            <div class="cost-grid">
              <div>
                <span>若自购柴油发电成本</span>
                <strong>{{ moneyText(dieselCost) }}</strong>
              </div>
              <div>
                <span>若直接接入电网（估算）</span>
                <strong>{{ moneyText(gridCost) }}</strong>
              </div>
              <div class="is-saving">
                <span>使用移动储能为您节省</span>
                <strong>{{ moneyText(savedCost) }} 起</strong>
              </div>
            </div>
          </section>

          <p class="bill-note">
            说明：本账单电量为移动储能站反向放电量，不等同于电网侧充电采购电量；电网采购成本及损耗已计入服务单价，不在本账单单独列示。分时时段依据当前计费规则展示，保底电量条款后续可接入计费规则统一维护。
          </p>
        </section>
      </div>
    </ContentWrap>
  </div>
</template>

<script lang="ts" setup>
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyPricingRuleApi } from '@/api/energy/pricingRule'
import type { EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import { EnergyReportApi } from '@/api/energy/report'
import type { EnergyReportBillVO, EnergyReportTouVO } from '@/api/energy/report'
import { archiveReport } from '@/utils/reportArchive'
import dayjs from 'dayjs'
import html2canvas from 'html2canvas'
import { jsPDF } from 'jspdf'

defineOptions({ name: 'EnergyDataReport' })

type ScopeType = 'all' | 'project' | 'device'
type TouKey = 'peak' | 'flat' | 'valley' | 'deepValley'

const message = useMessage()
const loading = ref(false)
const exporting = ref(false)
const devices = ref<EnergyDeviceVO[]>([])
const pricingRules = ref<EnergyPricingRuleVO[]>([])
const billReport = ref<EnergyReportBillVO>()
const billSheetRef = ref<HTMLElement>()

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

const projectOptions = computed(() => {
  const map = new Map<number, string>()
  devices.value.forEach((device) => {
    const id = numberOrNull(device.projectId)
    if (id !== null) map.set(id, device.projectName || `场地 ${id}`)
  })
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
})

const deviceOptions = computed(() => devices.value
  .filter((device) => Number.isFinite(Number(device.id)))
  .map((device) => ({ value: Number(device.id), label: deviceLabel(device) })))

const selectedDevices = computed(() => {
  if (query.scopeType === 'device') return devices.value.filter((device) => Number(device.id) === Number(query.deviceId))
  if (query.scopeType === 'project') return devices.value.filter((device) => Number(device.projectId) === Number(query.projectId))
  return devices.value
})

const applicableRules = computed(() => uniqueRowsById(selectedDevices.value.map(matchRuleForDevice).filter(Boolean) as EnergyPricingRuleVO[]))
const headerInfo = computed(() => billReport.value?.billHeader || {})
const billRangeText = computed(() => {
  const range = billReport.value?.billRange
  if (range) return `${range.start.slice(0, 10)} 至 ${range.end.slice(0, 10)}`
  const month = dayjs(query.billMonth)
  return `${month.startOf('month').format('YYYY-MM-DD')} 至 ${month.endOf('month').format('YYYY-MM-DD')}`
})
const billNo = computed(() => `ES-CB-${String(query.billMonth || '').replace('-', '')}-${String(selectedDevices.value[0]?.id || '0000').padStart(4, '0')}`)
const servicePointText = computed(() => {
  if (query.scopeType === 'device') return selectedDevices.value[0]?.deviceName || selectedDevices.value[0]?.deviceNo || '移动储能服务点'
  if (query.scopeType === 'project') return projectOptions.value.find((item) => Number(item.value) === Number(query.projectId))?.label || '移动储能服务点'
  return '移动储能服务点'
})
const paymentDueDate = computed(() => firstText([
  headerInfo.value.paymentDueDate,
  headerInfo.value.paymentDeadline,
  dayjs(query.billMonth || dayjs().format('YYYY-MM')).add(1, 'month').date(12).format('YYYY-MM-DD')
]))
const billStartDate = computed(() => billReport.value?.billRange?.start?.slice(0, 10) || dayjs(query.billMonth).startOf('month').format('YYYY-MM-DD'))
const billEndDate = computed(() => billReport.value?.billRange?.end?.slice(0, 10) || dayjs(query.billMonth).endOf('month').format('YYYY-MM-DD'))
const customerNameText = computed(() => firstText([
  headerInfo.value.customerName,
  selectedDevices.value[0]?.customerName,
  query.scopeType === 'all' ? '全部客户' : ''
]) || '待录入')
const usageAddressText = computed(() => firstText([
  headerInfo.value.usageAddress,
  headerInfo.value.address,
  query.scopeType === 'project' ? servicePointText.value : '',
  selectedDevices.value[0]?.projectName
]) || '待录入')
const accountNoText = computed(() => firstText([
  headerInfo.value.accountNo,
  headerInfo.value.accountNumber,
  headerInfo.value.userNo,
  selectedDevices.value[0]?.meterNo
]) || '待录入')
const usageCategoryText = computed(() => firstText([
  headerInfo.value.electricityCategory,
  headerInfo.value.usageCategory,
  applicableRules.value[0]?.electricityCategory
]) || 'general_commercial')
const voltageLevelText = computed(() => firstText([
  headerInfo.value.voltageLevel,
  applicableRules.value[0]?.voltageLevel
]) || 'under_1kv')
const supplyOrgText = computed(() => firstText([
  headerInfo.value.supplyOrg,
  headerInfo.value.supplyCompany,
  (applicableRules.value[0] as Record<string, unknown> | undefined)?.supplyOrg
]) || '待录入')
const marketAttributeText = computed(() => firstText([
  headerInfo.value.marketAttribute,
  headerInfo.value.marketType,
  query.scopeType === 'all' ? '全部电表汇总' : servicePointText.value
]))
const customerServiceText = computed(() => firstText([headerInfo.value.customerService, headerInfo.value.servicePhone]) || '待录入')
const supervisePhoneText = computed(() => firstText([headerInfo.value.supervisePhone, headerInfo.value.hotline]) || '待录入')
const printPersonText = computed(() => firstText([headerInfo.value.printPerson, headerInfo.value.operator]) || 'system')
const printDateText = computed(() => firstText([headerInfo.value.printDate]) || dayjs().format('YYYY-MM-DD'))
const generatedAtText = computed(() => firstText([headerInfo.value.generatedAt, headerInfo.value.printTime]) || dayjs().format('YYYY-MM-DD HH:mm:ss'))

const dischargeTou = computed(() => toLocalTou(billReport.value?.analysis?.dischargeTou))
const totalUsage = computed(() => round2(billReport.value?.summary?.totalDischargeEnergy || sumTou(dischargeTou.value)))
const touRates = computed(() => ({
  peak: avgRuleField(['peakRate', 'sharpPeakRate'], 1.62),
  flat: avgRuleField(['flatRate'], 1.38),
  valley: avgRuleField(['valleyRate'], 1.21),
  deepValley: avgRuleField(['deepValleyRate'], avgRuleField(['valleyRate'], 1.05))
}))
const touRows = computed(() => {
  const rawRows = [
    { key: 'peak' as TouKey, label: '峰', timeRange: '10:00-15:00, 18:00-21:00', energy: dischargeTou.value.peak + dischargeTou.value.sharpPeak, rate: touRates.value.peak },
    { key: 'flat' as TouKey, label: '平', timeRange: '07:00-10:00, 15:00-18:00, 21:00-23:00', energy: dischargeTou.value.flat, rate: touRates.value.flat },
    { key: 'valley' as TouKey, label: '谷', timeRange: '23:00-07:00', energy: dischargeTou.value.valley, rate: touRates.value.valley },
    { key: 'deepValley' as TouKey, label: '深谷', timeRange: '--', energy: dischargeTou.value.deepValley, rate: touRates.value.deepValley }
  ]
  const max = Math.max(...rawRows.map((row) => row.energy), 1)
  return rawRows.map((row) => ({
    ...row,
    energy: round2(row.energy),
    amount: round2(row.energy * row.rate),
    percent: Math.max(row.energy > 0 ? 8 : 2, Math.round((row.energy / max) * 100))
  }))
})
const touAmount = computed(() => round2(touRows.value.reduce((sum, row) => sum + row.amount, 0)))
const guaranteeEnergy = computed(() => {
  const value = sumRuleField('guaranteeEnergy')
  return value > 0 ? value : 2500
})
const averageServiceRate = computed(() => totalUsage.value > 0 ? round4(touAmount.value / totalUsage.value) : avgRuleField(['flatRate'], 1.36))
const guaranteeAmount = computed(() => round2(guaranteeEnergy.value * averageServiceRate.value))
const baseServiceFee = computed(() => round2(
  sumRuleField('siteFee') +
  sumRuleField('maintenanceFee') +
  sumRuleField('communicationFee') +
  sumRuleField('platformServiceFee') +
  sumRuleField('batteryDepreciationCost') +
  sumRuleField('otherFixedFee')
))
const payableAmount = computed(() => round2(Math.max(guaranteeAmount.value, touAmount.value) + baseServiceFee.value))
const guaranteeDiffText = computed(() => {
  const diff = round2(totalUsage.value - guaranteeEnergy.value)
  if (diff > 0) return `超出保底 ${numberText(diff)} kWh`
  if (diff < 0) return `未达保底 ${numberText(Math.abs(diff))} kWh`
  return '刚好达到保底'
})
const billingModeText = computed(() => totalUsage.value >= guaranteeEnergy.value ? '按实际用电量计费' : '按保底用电量计费')
const touFormulaText = computed(() => touRows.value
  .filter((row) => row.energy > 0)
  .map((row) => `${numberText(row.energy)}×${rateText(row.rate)}`)
  .join(' + ') || '0')
const dieselCost = computed(() => round2(totalUsage.value * 2))
const gridCost = computed(() => round2(totalUsage.value * Math.max(averageServiceRate.value + 0.18, 1.5)))
const savedCost = computed(() => round2(Math.max(0, Math.min(dieselCost.value, gridCost.value) - payableAmount.value)))

const loadOptions = async () => {
  devices.value = await EnergyDeviceApi.getDeviceSimpleList()
  ensureScopeSelection()
}

const loadReport = async () => {
  ensureScopeSelection()
  if (!query.billMonth || selectedDevices.value.length === 0) return
  loading.value = true
  try {
    const reportParams = {
      scopeType: query.scopeType,
      projectId: query.scopeType === 'project' ? query.projectId : undefined,
      deviceId: query.scopeType === 'device' ? query.deviceId : undefined,
      billMonth: query.billMonth
    }
    const [rules, report] = await Promise.all([
      EnergyPricingRuleApi.getPricingRulePage({ pageNo: 1, pageSize: 5000 }),
      EnergyReportApi.getBillReport(reportParams)
    ])
    pricingRules.value = rules?.list || []
    billReport.value = report
  } catch (error) {
    billReport.value = undefined
    message.error('数据报表加载失败，请检查电表、遥测和计费规则数据')
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

const exportPdf = async () => {
  if (!billSheetRef.value) return
  exporting.value = true
  let pdfElement: HTMLElement | null = null
  try {
    pdfElement = await createPagedPdfElement(billSheetRef.value)
    const canvas = await html2canvas(pdfElement, {
      backgroundColor: '#ffffff',
      scale: Math.min(2, Math.max(1.5, window.devicePixelRatio || 1)),
      useCORS: true,
      width: 794,
      windowWidth: 794,
      windowHeight: pdfElement.scrollHeight,
      scrollX: 0,
      scrollY: 0,
      height: pdfElement.scrollHeight
    })
    const pdf = new jsPDF({ orientation: 'p', unit: 'pt', format: 'a4', compress: true })
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const imageHeight = canvas.height * pageWidth / canvas.width
    const imageData = canvas.toDataURL('image/jpeg', 0.92)
    let remainingHeight = imageHeight
    let y = 0
    pdf.addImage(imageData, 'JPEG', 0, y, pageWidth, imageHeight, undefined, 'FAST')
    remainingHeight -= pageHeight
    while (remainingHeight > 0) {
      y -= pageHeight
      pdf.addPage()
      pdf.addImage(imageData, 'JPEG', 0, y, pageWidth, imageHeight, undefined, 'FAST')
      remainingHeight -= pageHeight
    }
    const blob = pdf.output('blob')
    const filename = `客户电费账单_${query.billMonth}_${servicePointText.value}.pdf`.replace(/[\\/:*?"<>|]/g, '_')
    void archiveReport(filename, blob, '数据报表导出').catch(() => undefined)
    const url = URL.createObjectURL(blob)
    if (/Android|iPhone|iPad|iPod|Mobile|Windows Phone/i.test(navigator.userAgent)) {
      window.open(url, '_blank')
    } else {
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      document.body.appendChild(link)
      link.click()
      link.remove()
    }
    setTimeout(() => URL.revokeObjectURL(url), 30000)
    message.success('PDF已生成')
  } catch (error) {
    console.error(error)
    message.error('PDF生成失败，请稍后重试')
  } finally {
    pdfElement?.remove()
    exporting.value = false
  }
}

const createPagedPdfElement = async (source: HTMLElement) => {
  const wrapper = document.createElement('div')
  wrapper.className = 'energy-data-report pdf-export-wrapper'
  source.getAttributeNames()
    .filter((name) => name.startsWith('data-v-'))
    .forEach((name) => wrapper.setAttribute(name, source.getAttribute(name) || ''))
  Object.assign(wrapper.style, {
    position: 'fixed',
    left: '-10000px',
    top: '0',
    width: '794px',
    margin: '0',
    zIndex: '0',
    background: '#ffffff'
  })
  const clone = source.cloneNode(true) as HTMLElement
  clone.classList.add('pdf-export-mode')
  Object.assign(clone.style, {
    width: '794px',
    margin: '0'
  })
  wrapper.appendChild(clone)
  document.body.appendChild(wrapper)
  await nextTick()
  await waitForFonts()
  insertPdfPageSpacers(clone)
  fillPdfLastPage(clone)
  await nextTick()
  return wrapper
}

const waitForFonts = async () => {
  const fonts = (document as Document & { fonts?: { ready?: Promise<FontFaceSet> } }).fonts
  await fonts?.ready?.catch(() => undefined)
}

const insertPdfPageSpacers = (root: HTMLElement) => {
  const pageHeight = 1123
  const topSafe = 28
  const bottomSafe = 96
  let pageStart = 0
  const blocks = Array.from(root.children).filter((child) => !child.classList.contains('pdf-page-spacer')) as HTMLElement[]

  for (const block of blocks) {
    const blockHeight = block.offsetHeight
    const blockTop = block.offsetTop
    const pageBottom = pageStart + pageHeight - bottomSafe
    if (blockTop > pageStart + topSafe && blockTop + blockHeight > pageBottom && blockHeight < pageHeight - topSafe - bottomSafe) {
      const spacer = document.createElement('div')
      spacer.className = 'pdf-page-spacer'
      spacer.style.height = `${Math.max(0, pageStart + pageHeight - blockTop)}px`
      block.parentElement?.insertBefore(spacer, block)
      pageStart += pageHeight
    }

    while (block.offsetTop + block.offsetHeight > pageStart + pageHeight) {
      pageStart += pageHeight
    }
  }
}

const fillPdfLastPage = (root: HTMLElement) => {
  const pageHeight = 1123
  const remainder = root.scrollHeight % pageHeight
  if (remainder === 0) return
  const spacer = document.createElement('div')
  spacer.className = 'pdf-page-spacer pdf-page-tail-spacer'
  spacer.style.height = `${pageHeight - remainder}px`
  root.appendChild(spacer)
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
    .sort((a, b) => Number(Boolean(b.deviceId)) - Number(Boolean(a.deviceId)) || Number(Boolean(b.projectId)) - Number(Boolean(a.projectId)))[0]
}

const avgRuleField = (fields: Array<keyof EnergyPricingRuleVO>, fallback: number) => {
  const values = applicableRules.value.flatMap((rule) => fields.map((field) => numberOrNull(rule[field]))).filter((value): value is number => value !== null && value > 0)
  if (!values.length) return fallback
  return round4(values.reduce((sum, value) => sum + value, 0) / values.length)
}
const sumRuleField = (field: keyof EnergyPricingRuleVO) => round2(applicableRules.value.reduce((sum, rule) => sum + Number(rule[field] || 0), 0))
const toLocalTou = (value?: EnergyReportTouVO | null) => ({
  sharpPeak: Number(value?.sharpPeak || 0),
  peak: Number(value?.peak || 0),
  flat: Number(value?.flat || 0),
  valley: Number(value?.valley || 0),
  deepValley: Number(value?.deepValley || 0)
})
const sumTou = (value: ReturnType<typeof toLocalTou>) => value.sharpPeak + value.peak + value.flat + value.valley + value.deepValley
const numberOrNull = (value: unknown) => {
  if (value === null || value === undefined || value === '') return null
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}
const firstText = (values: Array<unknown>) => values.map((value) => String(value || '').trim()).find(Boolean) || ''
const uniqueRowsById = <T extends { id?: number }>(rows: T[]) => Array.from(new Map(rows.map((row, index) => [row.id || index, row])).values())
const deviceLabel = (device?: EnergyDeviceVO) => device ? `${device.deviceName || device.deviceNo || `电表 ${device.id}`} / ${device.meterNo || '-'}` : ''
const round2 = (value: number) => Number((Number(value) || 0).toFixed(2))
const round4 = (value: number) => Number((Number(value) || 0).toFixed(4))
const numberText = (value: number) => Number.isInteger(value) ? String(value) : value.toFixed(2)
const kwhNumberText = (value: number) => `${numberText(value)} kWh`
const amountNumberText = (value: number) => numberText(value)
const moneyText = (value: number) => `¥${numberText(value)}`
const rateText = (value: number) => Number.isInteger(value) ? String(value) : value.toFixed(4).replace(/\.?0+$/, '')

onMounted(async () => {
  await loadOptions()
  await loadReport()
})
</script>

<style lang="scss" scoped>
.energy-data-report {
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

  .report-scroll {
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .data-bill-sheet {
    width: 794px;
    min-height: 1123px;
    margin: 0 auto;
    padding: 32px 42px;
    box-sizing: border-box;
    background: #fff;
    color: #0f172a;
    border: 1px solid #dbeafe;
    border-radius: 6px;
    font-family: "Times New Roman", SimSun, "宋体", serif;
  }

  .bill-head--receipt {
    display: grid;
    grid-template-columns: 170px minmax(0, 1fr) 178px;
    gap: 16px;
    align-items: start;
    padding: 8px 0 24px;
  }

  .bill-brand-block {
    display: flex;
    align-items: center;
    gap: 12px;

    .brand-mark {
      display: grid;
      place-items: center;
      width: 52px;
      height: 52px;
      border: 3px solid #0f766e;
      border-radius: 50%;
      color: #0f766e;
      font-size: 19px;
      font-weight: 900;
      line-height: 1;
    }

    strong,
    span {
      display: block;
      color: #00716d;
      letter-spacing: 0;
      line-height: 1.15;
    }

    strong {
      font-size: 19px;
      font-weight: 900;
    }

    span {
      margin-top: 4px;
      font-size: 12px;
      font-weight: 700;
    }
  }

  .bill-title-block {
    padding-top: 6px;
    text-align: center;

    .platform-title {
      color: #00716d;
      font-size: 18px;
      font-weight: 700;
      letter-spacing: 7px;
      line-height: 1.3;
    }

    .title-line {
      width: 320px;
      max-width: 100%;
      height: 3px;
      margin: 8px auto 10px;
      background: #334155;
    }

    h1 {
      margin: 0;
      color: #00716d;
      font-size: 25px;
      font-weight: 900;
      letter-spacing: 8px;
      line-height: 1.1;
    }
  }

  .bill-contact-block {
    display: flex;
    justify-content: flex-end;
    gap: 10px;

    div {
      width: 78px;
      min-width: 0;
      padding: 8px 6px;
      border: 1px solid #7ee8df;
      border-radius: 18px;
      text-align: center;
      box-sizing: border-box;
    }

    span,
    strong {
      display: block;
      color: #00716d;
      line-height: 1.2;
    }

    span {
      font-size: 11px;
    }

    strong {
      margin-top: 3px;
      font-size: 13px;
      font-weight: 900;
    }
  }

  .bill-receipt-meta {
    display: grid;
    grid-template-columns: 230px 1fr 1.2fr;
    gap: 38px;
    margin: 28px 0 26px;
    font-size: 13px;
  }

  .period-block {
    display: grid;
    align-content: start;
    gap: 22px;
    padding-left: 24px;

    .period-row {
      display: flex;
      align-items: center;
      gap: 3px;
    }

    .period-row--end {
      padding-left: 50px;
    }

    .period-separator,
    span {
      color: #94a3b8;
    }

    strong {
      color: #00716d;
      font-size: 14px;
      font-weight: 700;
    }
  }

  .info-list {
    display: grid;
    gap: 18px;

    div {
      display: grid;
      grid-template-columns: 88px minmax(0, 1fr);
      align-items: center;
      gap: 8px;
    }

    span {
      color: #94a3b8;
      text-align: right;
    }

    strong {
      color: #0f172a;
      font-weight: 700;
      word-break: break-word;
    }
  }

  .bill-print-row {
    display: flex;
    justify-content: center;
    gap: 44px;
    margin: 6px 0 22px;
    color: #64748b;
    font-size: 13px;
    letter-spacing: .5px;
  }

  .bill-summary-strip {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px 80px;
    padding: 12px 0 10px;
    border-top: 3px solid #0f766e;
    border-bottom: 1px solid #cbd5e1;
    color: #0f172a;
    font-size: 13px;

    div {
      min-width: 0;
    }

    strong {
      font-weight: 700;
    }
  }

  .metric-grid,
  .cost-grid,
  .guarantee-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .metric-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .metric-card {
    position: relative;
    display: grid;
    grid-template-columns: minmax(96px, 1fr) auto;
    align-items: center;
    min-height: 62px;
    overflow: hidden;
    padding: 12px 14px;
    border: 1px solid #c9f3f0;
    border-radius: 6px;
    background: linear-gradient(120deg, #f6f6f6 0%, #f6f6f6 42%, #eefcfb 100%);
    text-align: left;

    &::after {
      position: absolute;
      right: 22px;
      bottom: -34px;
      width: 108px;
      height: 108px;
      border-radius: 999px;
      background: rgba(45, 212, 191, 0.16);
      content: '';
    }

    &.is-soft {
      background: linear-gradient(120deg, #f3f3f3 0%, #f3f3f3 45%, #d9fbf8 100%);
    }

    &:nth-child(2) {
      background: linear-gradient(120deg, #d8fbf8 0%, #c8f6f2 55%, #f6f6f6 100%);
    }

    &.is-amount strong {
      color: #0f766e;
    }

    &.is-due {
      background: linear-gradient(120deg, #f6f6f6 0%, #eefcfb 58%, #d8fbf8 100%);

      strong {
        color: #334155;
      }
    }

    span,
    small {
      display: block;
      position: relative;
      z-index: 1;
      color: #00716d;
      font-size: 15px;
      font-weight: 800;
    }

    strong {
      display: block;
      position: relative;
      z-index: 1;
      min-width: 0;
      color: #334155;
      font-family: "Times New Roman", SimSun, serif;
      font-size: 15px;
      font-weight: 500;
      text-align: right;
      white-space: nowrap;
    }
  }

  .bill-section {
    margin-top: 20px;
    break-inside: avoid;
    page-break-inside: avoid;

    h3 {
      margin: 0 0 12px;
      padding: 9px 12px;
      border: 1px solid #bcefeb;
      border-radius: 8px 8px 0 0;
      background: #d5fbfa;
      color: #00716d;
      font-size: 15px;
      font-weight: 800;
    }
  }

  .tou-table {
    width: 100%;
    border-collapse: collapse;

    th,
    td {
      padding: 10px 12px;
      border-bottom: 1px dashed #bdebe8;
      text-align: left;
    }

    th {
      background: #eafafa;
      color: #00716d;
      font-weight: 800;
    }

    th:nth-child(n+3),
    td:nth-child(n+3) {
      text-align: right;
    }

    .is-total td {
      background: #f7ffff;
      color: #0f172a;
      font-weight: 800;
    }
  }

  .bar-chart {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 34px;
    align-items: end;
    min-height: 190px;
    padding: 20px 54px 2px;
  }

  .bar-item {
    display: grid;
    justify-items: center;
    gap: 6px;

    .bar-value {
      color: #334155;
      font-size: 12px;
    }

    .bar-track {
      display: flex;
      align-items: end;
      width: 42px;
      height: 110px;
    }

    i {
      display: block;
      width: 100%;
      min-height: 4px;
      border-radius: 6px 6px 0 0;
      background: linear-gradient(180deg, #2dd4bf, #0f766e);
    }

    strong {
      color: #475569;
    }

    small {
      color: #18a9a0;
    }
  }

  .guarantee-grid,
  .cost-grid {
    border: 1px solid #bdebe8;
    border-radius: 0 0 8px 8px;
    overflow: hidden;

    div {
      min-height: 80px;
      padding: 14px;
      border-right: 1px dashed #bdebe8;

      &:last-child {
        border-right: 0;
      }
    }

    span,
    em {
      display: block;
      color: #94a3b8;
      font-style: normal;
    }

    strong {
      display: block;
      margin-top: 8px;
      color: #0f172a;
      font-size: 18px;
      font-weight: 900;
    }

    .highlight,
    .is-saving strong {
      color: #00716d;
    }
  }

  .formula-box {
    padding: 16px 18px;
    border: 1px dashed #bdebe8;
    border-radius: 0 0 8px 8px;
    background: #fbffff;

    p {
      margin: 0 0 8px;
      color: #475569;
      line-height: 1.7;
    }

    strong {
      display: block;
      margin-top: 10px;
      color: #00716d;
      font-size: 16px;
    }
  }

  .cost-grid {
    div {
      text-align: center;
      background: #fbffff;

      &.is-saving {
        background: linear-gradient(120deg, #d8fbf8, #fbffff);
      }
    }
  }

  .bill-note {
    margin: 22px 0 0;
    padding-top: 12px;
    border-top: 1px solid #e2e8f0;
    color: #64748b;
    font-size: 12px;
    line-height: 1.7;
  }

  .pdf-export-mode {
    min-height: 0;
    border: 0;
    border-radius: 0;
    box-shadow: none;
  }

  .pdf-export-mode > * {
    break-inside: avoid;
    page-break-inside: avoid;
  }

  .pdf-page-spacer {
    display: block;
    flex: 0 0 auto;
  }

  @media (max-width: 900px) {
    .report-header {
      align-items: flex-start;
      flex-direction: column;
    }

    .data-bill-sheet {
      margin: 0;
    }

    .metric-grid,
    .cost-grid,
    .guarantee-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 560px) {
    .metric-grid,
    .cost-grid,
    .guarantee-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
