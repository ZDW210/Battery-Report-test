<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="1040px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="132px"
    >
      <el-row :gutter="16">
        <el-col :span="24">
          <el-form-item label="计费范围" prop="scopeType">
            <el-radio-group v-model="scopeType" @change="handleScopeTypeChange">
              <el-radio value="customer">客户</el-radio>
              <el-radio value="project">项目</el-radio>
              <el-radio value="device">设备</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col v-if="scopeType === 'customer'" :span="24">
          <el-form-item label="所属客户" prop="customerId">
            <el-select v-model="formData.customerId" filterable class="!w-1/1" placeholder="请选择客户">
              <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-if="scopeType === 'project'" :span="24">
          <el-form-item label="项目场站" prop="projectId">
            <el-select v-model="formData.projectId" filterable class="!w-1/1" placeholder="请选择项目">
              <el-option
                v-for="item in projectList"
                :key="item.id"
                :label="`${item.name || '-'} / ${item.code || '-'}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-if="scopeType === 'device'" :span="24">
          <el-form-item label="设备" prop="deviceId">
            <el-select v-model="formData.deviceId" filterable class="!w-1/1" placeholder="请选择设备">
              <el-option
                v-for="item in deviceList"
                :key="item.id"
                :label="`${item.deviceName || '-'} / ${item.deviceNo || '-'}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-divider content-position="left">电价表基础信息</el-divider>
        </el-col>
        <el-col :span="8">
          <el-form-item label="用电分类" prop="electricityCategory">
            <el-select v-model="formData.electricityCategory" class="!w-1/1">
              <el-option
                v-for="item in electricityCategoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="计价方式" prop="pricingMode">
            <el-select v-model="formData.pricingMode" class="!w-1/1">
              <el-option
                v-for="item in pricingModeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="电压等级" prop="voltageLevel">
            <el-select v-model="formData.voltageLevel" class="!w-1/1">
              <el-option
                v-for="item in voltageLevelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-divider content-position="left">
            <span class="mr-12px">费用设置（元/千瓦时）</span>
            <el-button link type="primary" @click="applyShanghaiJuneExample">套用上海 2026 年 6 月示例</el-button>
            <el-button link type="primary" @click="applySavedFeeTemplate">套用当前模板</el-button>
            <el-button link type="primary" @click="saveCurrentFeeTemplate">保存为套用模板</el-button>
            <el-button link type="warning" @click="resetFeeTemplateToSystem">恢复系统模板</el-button>
          </el-divider>
        </el-col>
        <el-col :span="8">
          <el-form-item label="服务增值比例(%)" prop="serviceMarkupPercent">
            <el-input-number
              v-model="formData.serviceMarkupPercent"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
              @change="syncLegacyFieldsFromFeeConfig"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <div class="fee-config-table">
            <div class="fee-config-table__head">
              <span>费用类别</span>
              <span>费用组成</span>
              <span v-for="period in feePeriodFields" :key="period.key">{{ period.label }}</span>
            </div>
            <div v-for="(row, rowIndex) in feeConfigRows" :key="`${row.category}-${row.component}`" class="fee-config-table__row">
              <strong>{{ row.category }}</strong>
              <span>{{ row.component }}</span>
              <template v-for="period in feePeriodFields" :key="period.key">
                <el-input-number
                  v-if="isFeePeriodEditable(row, period.key)"
                  v-model="row.rates[period.key]"
                  :precision="6"
                  class="fee-config-table__input"
                  controls-position="right"
                  @change="syncLegacyFieldsFromFeeConfig"
                />
                <span v-else class="fee-config-table__disabled">--</span>
              </template>
              <el-button class="fee-config-table__copy" link type="primary" @click="copyPreviousFeeRow(rowIndex)">复制上行</el-button>
            </div>
          </div>
          <div class="fee-config-tip">
            市场化购电电费的“零售交易电费”会在保存时保留原始电价，报表计算时再叠加服务增值比例。
          </div>
        </el-col>
        <el-col :span="24">
          <el-divider content-position="left">
            <span class="mr-12px">分时时段配置</span>
            <el-button link type="primary" @click="resetTouPeriodsToDefault">套用默认时段</el-button>
            <el-button link type="primary" @click="addTouPeriod">新增时段</el-button>
          </el-divider>
          <div class="tou-period-list">
            <div v-for="(item, index) in touPeriodRows" :key="index" class="tou-period-row">
              <el-select v-model="item.type" class="tou-period-row__type" placeholder="时段类型">
                <el-option v-for="option in touPeriodTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
              <el-time-picker
                v-model="item.start"
                class="tou-period-row__time"
                format="HH:mm"
                placeholder="开始"
                value-format="HH:mm"
              />
              <span class="tou-period-row__separator">至</span>
              <el-time-picker
                v-model="item.end"
                class="tou-period-row__time"
                format="HH:mm"
                placeholder="结束"
                value-format="HH:mm"
              />
              <el-button link type="danger" @click="removeTouPeriod(index)">删除</el-button>
            </div>
          </div>
        </el-col>
        <el-col :span="24">
          <el-divider content-position="left">容量/需量用电价格</el-divider>
        </el-col>
        <el-col :span="24">
          <el-form-item label="容需量计费方式" prop="capacityBillingMode">
            <el-radio-group v-model="formData.capacityBillingMode">
              <el-radio value="none">不计容需量</el-radio>
              <el-radio value="maxDemand">按最大需量</el-radio>
              <el-radio value="transformerCapacity">按变压器容量</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col v-for="field in capacityPriceFields" :key="field.prop" :span="8">
          <el-form-item :label="field.label" :prop="field.prop">
            <el-input-number
              v-model="formData[field.prop]"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-divider content-position="left">运营结算参数</el-divider>
        </el-col>
        <el-col :span="12">
          <el-form-item label="时间单价" prop="timeRate">
            <el-input-number
              v-model="formData.timeRate"
              :min="0"
              :precision="4"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结算电量单价" prop="energyRate">
            <el-input-number
              v-model="formData.energyRate"
              :min="0"
              :precision="6"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-divider content-position="left">固定费用参数</el-divider>
        </el-col>
        <el-col :span="12">
          <el-form-item label="场地费" prop="siteFee">
            <el-input-number
              v-model="formData.siteFee"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="运维费" prop="maintenanceFee">
            <el-input-number
              v-model="formData.maintenanceFee"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="通信费" prop="communicationFee">
            <el-input-number
              v-model="formData.communicationFee"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="平台服务费" prop="platformServiceFee">
            <el-input-number
              v-model="formData.platformServiceFee"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电池折旧成本" prop="batteryDepreciationCost">
            <el-input-number
              v-model="formData.batteryDepreciationCost"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="其他固定费用" prop="otherFixedFee">
            <el-input-number
              v-model="formData.otherFixedFee"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="约定保底用电量" prop="guaranteeEnergy">
            <el-input-number
              v-model="formData.guaranteeEnergy"
              :min="0"
              :precision="2"
              class="!w-1/1"
              controls-position="right"
            />
            <div class="fee-config-tip">单位：kWh/月，用于数据报表基础服务费保底核算</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生效开始" prop="effectiveStart">
            <el-date-picker
              v-model="formData.effectiveStart"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开始时间"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生效结束" prop="effectiveEnd">
            <el-date-picker
              v-model="formData.effectiveEnd"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="不填表示长期有效"
              class="!w-1/1"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio
                v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                :key="dict.value"
                :value="dict.value"
              >
                {{ dict.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" maxlength="512" show-word-limit type="textarea" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { EnergyCustomerApi } from '@/api/energy/customer'
import type { EnergyCustomerVO } from '@/api/energy/customer'
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyPricingRuleApi } from '@/api/energy/pricingRule'
import type { EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import { EnergyProjectApi } from '@/api/energy/project'
import type { EnergyProjectVO } from '@/api/energy/project'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

defineOptions({ name: 'EnergyPricingRuleForm' })

type ScopeType = 'customer' | 'project' | 'device'
type PricingField = keyof EnergyPricingRuleVO
type TouPeriodType = 'sharpPeak' | 'peak' | 'flat' | 'valley' | 'deepValley'
type FeePeriodKey = 'peak' | 'flat' | 'valley' | 'deepValley' | 'peakFloat' | 'valleyFloat'
type TouPeriodRow = {
  type: TouPeriodType
  start: string
  end: string
}
type FeeConfigRow = {
  category: string
  component: string
  rates: Record<FeePeriodKey, number>
}

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const scopeType = ref<ScopeType>('customer')
const customerList = ref<EnergyCustomerVO[]>([])
const projectList = ref<EnergyProjectVO[]>([])
const deviceList = ref<EnergyDeviceVO[]>([])
const touPeriodRows = ref<TouPeriodRow[]>([])
const feeConfigRows = ref<FeeConfigRow[]>([])
const FEE_TEMPLATE_STORAGE_KEY = 'energy-pricing-rule-fee-template-v1'
const electricityCategoryOptions = [
  { label: '一般工商业用电', value: 'general_commercial' },
  { label: '大工业用电', value: 'large_industrial' }
]
const pricingModeOptions = [
  { label: '单一制', value: 'single' },
  { label: '两部制', value: 'two_part' }
]
const voltageLevelOptions = [
  { label: '不满1千伏', value: 'under_1kv' },
  { label: '10千伏', value: '10kv' },
  { label: '35千伏', value: '35kv' },
  { label: '110千伏', value: '110kv' },
  { label: '220千伏及以上', value: '220kv_plus' }
]
const feePeriodFields: Array<{ label: string; key: FeePeriodKey }> = [
  { label: '峰', key: 'peak' },
  { label: '平', key: 'flat' },
  { label: '谷', key: 'valley' },
  { label: '深谷', key: 'deepValley' },
  { label: '峰浮动', key: 'peakFloat' },
  { label: '谷浮动', key: 'valleyFloat' }
]
const touPeriodTypeOptions: Array<{ label: string; value: TouPeriodType }> = [
  { label: '尖峰', value: 'sharpPeak' },
  { label: '高峰', value: 'peak' },
  { label: '平时', value: 'flat' },
  { label: '低谷', value: 'valley' },
  { label: '深谷', value: 'deepValley' }
]
const defaultTouPeriods: TouPeriodRow[] = [
  { type: 'peak', start: '08:00', end: '11:00' },
  { type: 'flat', start: '11:00', end: '18:00' },
  { type: 'peak', start: '18:00', end: '21:00' },
  { type: 'valley', start: '21:00', end: '08:00' }
]
const capacityPriceFields: Array<{ label: string; prop: PricingField }> = [
  { label: '最大需量（元/千瓦·月）', prop: 'maxDemandPrice' },
  { label: '变压器容量（kVA）', prop: 'transformerCapacityKva' },
  { label: '变压器容量（元/千伏安·月）', prop: 'transformerCapacityPrice' }
]
const shanghaiJuneExample: Partial<EnergyPricingRuleVO> = {
  electricityCategory: 'general_commercial',
  pricingMode: 'two_part',
  voltageLevel: 'under_1kv',
  agentPurchasePrice: 0.43508,
  lineLossPrice: 0.011863,
  transmissionDistributionPrice: 0.1456,
  systemOperationFee: 0.040718,
  governmentFundSurcharge: 0.029115,
  sharpPeakRate: 0,
  peakRate: 1.010784,
  flatRate: 0.662376,
  valleyRate: 0.372036,
  deepValleyRate: 0,
  touPeriods: JSON.stringify(defaultTouPeriods),
  feeConfigJson: '',
  serviceMarkupPercent: 0,
  capacityBillingMode: 'none',
  maxDemandPrice: 40.8,
  transformerCapacityKva: 0,
  transformerCapacityPrice: 25.5,
  energyRate: 0.662376
}
function feeRowTemplate(
  category: string,
  component: string,
  rates: Partial<Record<FeePeriodKey, number>> = {}
): FeeConfigRow {
  return {
    category,
    component,
    rates: {
      peak: Number(rates.peak || 0),
      flat: Number(rates.flat || 0),
      valley: Number(rates.valley || 0),
      deepValley: Number(rates.deepValley || 0),
      peakFloat: Number(rates.peakFloat || 0),
      valleyFloat: Number(rates.valleyFloat || 0)
    }
  }
}
const systemFeeTemplate: FeeConfigRow[] = [
  feeRowTemplate('市场化购电电费', '零售交易电费', { peak: 1.010784, flat: 0.662376, valley: 0.372036 }),
  feeRowTemplate('上网环节线损费用', '上网环节线损费用', { peak: 0.011863, flat: 0.011863, valley: 0.011863 }),
  feeRowTemplate('输配电量电费', '电量电费', { peak: 0.1456, flat: 0.1456, valley: 0.1456 }),
  feeRowTemplate('系统运行费用', '煤电容量电费', { peak: 0.0154, flat: 0.0154, valley: 0.0154 }),
  feeRowTemplate('系统运行费用', '上网环节线损代理采购损益'),
  feeRowTemplate('系统运行费用', '力调电费损益'),
  feeRowTemplate('系统运行费用', '峰谷分时电价损益'),
  feeRowTemplate('系统运行费用', '电价交叉补贴新增损益'),
  feeRowTemplate('系统运行费用', '天然气发电容量电费（含气电联动）'),
  feeRowTemplate('系统运行费用', '抽水蓄能容量电费'),
  feeRowTemplate('政府性基金及附加', '库区移民基金', { peak: 0.0062, flat: 0.0062, valley: 0.0062 }),
  feeRowTemplate('政府性基金及附加', '可再生能源附加', { peak: 0.019, flat: 0.019, valley: 0.019 }),
  feeRowTemplate('政府性基金及附加', '国家重大水利工程建设基金', { peak: 0.0042, flat: 0.0042, valley: 0.0042 })
]
const formData = ref<EnergyPricingRuleVO>({
  id: undefined,
  customerId: undefined,
  projectId: undefined,
  deviceId: undefined,
  electricityCategory: 'general_commercial',
  pricingMode: 'two_part',
  voltageLevel: 'under_1kv',
  agentPurchasePrice: 0,
  lineLossPrice: 0,
  transmissionDistributionPrice: 0,
  systemOperationFee: 0,
  governmentFundSurcharge: 0,
  sharpPeakRate: 0,
  peakRate: 0,
  flatRate: 0,
  valleyRate: 0,
  deepValleyRate: 0,
  touPeriods: JSON.stringify(defaultTouPeriods),
  feeConfigJson: JSON.stringify(systemFeeTemplate),
  serviceMarkupPercent: 0,
  capacityBillingMode: 'none',
  maxDemandPrice: 0,
  transformerCapacityKva: 0,
  transformerCapacityPrice: 0,
  timeRate: 0,
  energyRate: 0,
  siteFee: 0,
  maintenanceFee: 0,
  communicationFee: 0,
  platformServiceFee: 0,
  batteryDepreciationCost: 0,
  otherFixedFee: 0,
  guaranteeEnergy: 2500,
  effectiveStart: undefined,
  effectiveEnd: undefined,
  status: 0,
  remark: undefined
})
const formRules = reactive({
  customerId: [{ required: true, message: '客户不能为空', trigger: 'change' }],
  projectId: [{ required: true, message: '项目不能为空', trigger: 'change' }],
  deviceId: [{ required: true, message: '设备不能为空', trigger: 'change' }],
  timeRate: [{ required: true, message: '时间单价不能为空', trigger: 'blur' }],
  energyRate: [{ required: true, message: '电量单价不能为空', trigger: 'blur' }],
  effectiveStart: [{ required: true, message: '生效开始时间不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const applyShanghaiJuneExample = () => {
  formData.value = {
    ...formData.value,
    ...shanghaiJuneExample,
    feeConfigJson: JSON.stringify(cloneFeeRows(systemFeeTemplate))
  }
  feeConfigRows.value = cloneFeeRows(systemFeeTemplate)
  syncLegacyFieldsFromFeeConfig()
  touPeriodRows.value = parseTouPeriods(formData.value.touPeriods)
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  await loadOptions()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EnergyPricingRuleApi.getPricingRule(id)
      touPeriodRows.value = parseTouPeriods(formData.value.touPeriods)
      feeConfigRows.value = parseFeeConfigRows(formData.value.feeConfigJson)
      scopeType.value = getScopeType(formData.value)
      syncLegacyFieldsFromFeeConfig()
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    syncLegacyFieldsFromFeeConfig()
    const data = normalizeScope({
      ...formData.value,
      touPeriods: JSON.stringify(normalizeTouPeriods(touPeriodRows.value)),
      feeConfigJson: JSON.stringify(normalizeFeeConfigRows(feeConfigRows.value))
    })
    if (formType.value === 'create') {
      await EnergyPricingRuleApi.createPricingRule(data)
      message.success(t('common.createSuccess'))
    } else {
      await EnergyPricingRuleApi.updatePricingRule(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const loadOptions = async () => {
  const [customers, projects, devices] = await Promise.all([
    EnergyCustomerApi.getCustomerSimpleList(),
    EnergyProjectApi.getProjectSimpleList(),
    EnergyDeviceApi.getDeviceSimpleList()
  ])
  customerList.value = customers
  projectList.value = projects
  deviceList.value = devices
}

const handleScopeTypeChange = () => {
  formData.value.customerId = undefined
  formData.value.projectId = undefined
  formData.value.deviceId = undefined
}

const getScopeType = (data: EnergyPricingRuleVO): ScopeType => {
  if (data.deviceId) return 'device'
  if (data.projectId) return 'project'
  return 'customer'
}

const normalizeScope = (data: EnergyPricingRuleVO): EnergyPricingRuleVO => {
  return {
    ...data,
    customerId: scopeType.value === 'customer' ? data.customerId : undefined,
    projectId: scopeType.value === 'project' ? data.projectId : undefined,
    deviceId: scopeType.value === 'device' ? data.deviceId : undefined
  }
}

const resetForm = () => {
  scopeType.value = 'customer'
  formData.value = {
    id: undefined,
    customerId: undefined,
    projectId: undefined,
    deviceId: undefined,
    electricityCategory: 'general_commercial',
    pricingMode: 'two_part',
    voltageLevel: 'under_1kv',
    agentPurchasePrice: 0,
    lineLossPrice: 0,
    transmissionDistributionPrice: 0,
    systemOperationFee: 0,
    governmentFundSurcharge: 0,
    sharpPeakRate: 0,
    peakRate: 0,
    flatRate: 0,
    valleyRate: 0,
    deepValleyRate: 0,
    touPeriods: JSON.stringify(defaultTouPeriods),
    feeConfigJson: JSON.stringify(systemFeeTemplate),
    serviceMarkupPercent: 0,
    capacityBillingMode: 'none',
    maxDemandPrice: 0,
    transformerCapacityKva: 0,
    transformerCapacityPrice: 0,
    timeRate: 0,
    energyRate: 0,
    siteFee: 0,
    maintenanceFee: 0,
    communicationFee: 0,
    platformServiceFee: 0,
    batteryDepreciationCost: 0,
    otherFixedFee: 0,
    guaranteeEnergy: 2500,
    effectiveStart: undefined,
    effectiveEnd: undefined,
    status: 0,
    remark: undefined
  }
  touPeriodRows.value = defaultTouPeriods.map((item) => ({ ...item }))
  feeConfigRows.value = cloneFeeRows(systemFeeTemplate)
  syncLegacyFieldsFromFeeConfig()
  formRef.value?.resetFields()
}

const addTouPeriod = () => {
  touPeriodRows.value.push({ type: 'flat', start: '00:00', end: '00:00' })
}

const removeTouPeriod = (index: number) => {
  touPeriodRows.value.splice(index, 1)
}

const resetTouPeriodsToDefault = () => {
  touPeriodRows.value = defaultTouPeriods.map((item) => ({ ...item }))
}

const normalizeTouPeriods = (rows: TouPeriodRow[]) => {
  return rows
    .filter((item) => item.type && item.start && item.end)
    .map((item) => ({ type: item.type, start: item.start, end: item.end }))
}

const parseTouPeriods = (value?: string) => {
  try {
    const parsed = JSON.parse(value || '[]')
    if (!Array.isArray(parsed) || parsed.length === 0) return defaultTouPeriods.map((item) => ({ ...item }))
    return parsed
      .filter((item) => item?.type && item?.start && item?.end)
      .map((item) => ({ type: item.type, start: item.start, end: item.end })) as TouPeriodRow[]
  } catch {
    return defaultTouPeriods.map((item) => ({ ...item }))
  }
}

const cloneFeeRows = (rows: FeeConfigRow[]) => rows.map((row) => ({
  category: row.category,
  component: row.component,
  rates: { ...row.rates }
}))

const normalizeFeeConfigRows = (rows: FeeConfigRow[]) => rows.map((row) => ({
  category: row.category,
  component: row.component,
  rates: feePeriodFields.reduce((rates, period) => {
    rates[period.key] = isFeePeriodEditable(row, period.key) ? Number(row.rates?.[period.key] || 0) : 0
    return rates
  }, {} as Record<FeePeriodKey, number>)
}))

const parseFeeConfigRows = (value?: string) => {
  try {
    const parsed = JSON.parse(value || '[]')
    if (!Array.isArray(parsed) || parsed.length === 0) return cloneFeeRows(systemFeeTemplate)
    const fallback = cloneFeeRows(systemFeeTemplate)
    return fallback.map((base) => {
      const found = parsed.find((item) => item?.category === base.category && item?.component === base.component)
      return found
        ? feeRowTemplate(base.category, base.component, { ...base.rates, ...(found.rates || {}) })
        : base
    })
  } catch {
    return cloneFeeRows(systemFeeTemplate)
  }
}

const applySavedFeeTemplate = () => {
  feeConfigRows.value = parseFeeConfigRows(localStorage.getItem(FEE_TEMPLATE_STORAGE_KEY) || '')
  syncLegacyFieldsFromFeeConfig()
  message.success('已套用当前保存的费用模板')
}

const saveCurrentFeeTemplate = () => {
  localStorage.setItem(FEE_TEMPLATE_STORAGE_KEY, JSON.stringify(normalizeFeeConfigRows(feeConfigRows.value)))
  message.success('已保存为套用模板')
}

const resetFeeTemplateToSystem = () => {
  feeConfigRows.value = cloneFeeRows(systemFeeTemplate)
  syncLegacyFieldsFromFeeConfig()
  message.success('已恢复系统费用模板')
}

const copyPreviousFeeRow = (index: number) => {
  if (index <= 0) return
  feeConfigRows.value[index].rates = { ...feeConfigRows.value[index - 1].rates }
  if (!isMarketRetailFeeRow(feeConfigRows.value[index])) {
    feeConfigRows.value[index].rates.peakFloat = 0
    feeConfigRows.value[index].rates.valleyFloat = 0
  }
  syncLegacyFieldsFromFeeConfig()
}

const isMarketRetailFeeRow = (row: Pick<FeeConfigRow, 'category' | 'component'>) =>
  row.category === '市场化购电电费' && row.component === '零售交易电费'

const isFeePeriodEditable = (row: Pick<FeeConfigRow, 'category' | 'component'>, periodKey: FeePeriodKey) =>
  periodKey !== 'peakFloat' && periodKey !== 'valleyFloat' ? true : isMarketRetailFeeRow(row)

const syncLegacyFieldsFromFeeConfig = () => {
  const rows = feeConfigRows.value
  const serviceMultiplier = 1 + Number(formData.value.serviceMarkupPercent || 0) / 100
  const marketRow = rows.find((row) => row.category === '市场化购电电费' && row.component === '零售交易电费')
  const lineLossRow = rows.find((row) => row.category === '上网环节线损费用')
  const transmissionRow = rows.find((row) => row.category === '输配电量电费')
  const systemRows = rows.filter((row) => row.category === '系统运行费用')
  const fundRows = rows.filter((row) => row.category === '政府性基金及附加')
  formData.value.agentPurchasePrice = rateByPeriod(marketRow, 'flat') * serviceMultiplier
  formData.value.lineLossPrice = rateByPeriod(lineLossRow, 'flat')
  formData.value.transmissionDistributionPrice = rateByPeriod(transmissionRow, 'flat')
  formData.value.systemOperationFee = sumRowsByPeriod(systemRows, 'flat')
  formData.value.governmentFundSurcharge = sumRowsByPeriod(fundRows, 'flat')
  formData.value.peakRate = totalRateByPeriod(rows, 'peak', serviceMultiplier)
  formData.value.sharpPeakRate = formData.value.peakRate
  formData.value.flatRate = totalRateByPeriod(rows, 'flat', serviceMultiplier)
  formData.value.valleyRate = totalRateByPeriod(rows, 'valley', serviceMultiplier)
  formData.value.deepValleyRate = totalRateByPeriod(rows, 'deepValley', serviceMultiplier)
  formData.value.energyRate = formData.value.flatRate
}

const rateByPeriod = (row: FeeConfigRow | undefined, period: FeePeriodKey) => Number(row?.rates?.[period] || 0)
const sumRowsByPeriod = (rows: FeeConfigRow[], period: FeePeriodKey) => Number(rows.reduce((sum, row) => sum + rateByPeriod(row, period), 0).toFixed(6))
const totalRateByPeriod = (rows: FeeConfigRow[], period: FeePeriodKey, serviceMultiplier: number) => Number(rows.reduce((sum, row) => {
  const multiplier = row.category === '市场化购电电费' && row.component === '零售交易电费' ? serviceMultiplier : 1
  return sum + rateByPeriod(row, period) * multiplier
}, 0).toFixed(6))
</script>

<style lang="scss" scoped>
.tou-period-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 2px 0 10px;
}

.tou-period-row {
  display: flex;
  align-items: center;
  gap: 10px;

  &__type {
    width: 140px;
  }

  &__time {
    width: 132px;
  }

  &__separator {
    color: #64748b;
  }
}

.fee-config-table {
  width: 100%;
  overflow-x: auto;
  border: 1px solid #dbeafe;
  border-radius: 6px;

  &__head,
  &__row {
    display: grid;
    grid-template-columns: 150px 220px repeat(6, 128px) 72px;
    align-items: center;
    min-width: 1210px;
    column-gap: 8px;
    padding: 8px 10px;
  }

  &__head {
    position: sticky;
    top: 0;
    z-index: 1;
    color: #0f172a;
    font-weight: 700;
    background: #eff6ff;
    border-bottom: 1px solid #dbeafe;
  }

  &__row {
    border-bottom: 1px dashed #cbd5e1;

    &:last-child {
      border-bottom: 0;
    }

    strong {
      color: #0f766e;
      font-size: 13px;
    }

    span {
      color: #334155;
      font-size: 13px;
    }
  }

  &__input {
    width: 128px;
  }

  &__disabled {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 128px;
    height: 32px;
    color: #94a3b8;
    background: #f8fafc;
    border: 1px dashed #cbd5e1;
    border-radius: 4px;
  }

  &__copy {
    justify-self: start;
  }
}

.fee-config-tip {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}
</style>
