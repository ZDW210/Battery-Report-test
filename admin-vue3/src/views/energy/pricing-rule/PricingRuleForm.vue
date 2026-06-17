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
            <span class="mr-12px">电价组成（元/千瓦时）</span>
            <el-button link type="primary" @click="applyShanghaiJuneExample">
              套用上海 2026 年 6 月示例
            </el-button>
          </el-divider>
        </el-col>
        <el-col v-for="field in priceComponentFields" :key="field.prop" :span="8">
          <el-form-item :label="field.label" :prop="field.prop">
            <el-input-number
              v-model="formData[field.prop]"
              :min="0"
              :precision="6"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-divider content-position="left">分时电度电价（元/千瓦时）</el-divider>
        </el-col>
        <el-col v-for="field in timeOfUsePriceFields" :key="field.prop" :span="8">
          <el-form-item :label="field.label" :prop="field.prop">
            <el-input-number
              v-model="formData[field.prop]"
              :min="0"
              :precision="6"
              class="!w-1/1"
              controls-position="right"
            />
          </el-form-item>
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
type TouPeriodRow = {
  type: TouPeriodType
  start: string
  end: string
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
const priceComponentFields: Array<{ label: string; prop: PricingField }> = [
  { label: '代理购电价格', prop: 'agentPurchasePrice' },
  { label: '线损电价', prop: 'lineLossPrice' },
  { label: '电度输配电价', prop: 'transmissionDistributionPrice' },
  { label: '系统运行费折价', prop: 'systemOperationFee' },
  { label: '政府基金及附加', prop: 'governmentFundSurcharge' }
]
const timeOfUsePriceFields: Array<{ label: string; prop: PricingField }> = [
  { label: '尖峰时段', prop: 'sharpPeakRate' },
  { label: '高峰时段', prop: 'peakRate' },
  { label: '平时段', prop: 'flatRate' },
  { label: '低谷时段', prop: 'valleyRate' },
  { label: '深谷时段', prop: 'deepValleyRate' }
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
  maxDemandPrice: 40.8,
  transformerCapacityKva: 0,
  transformerCapacityPrice: 25.5,
  energyRate: 0.662376
}
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
    ...shanghaiJuneExample
  }
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
      scopeType.value = getScopeType(formData.value)
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
    const data = normalizeScope({
      ...formData.value,
      touPeriods: JSON.stringify(normalizeTouPeriods(touPeriodRows.value))
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
    effectiveStart: undefined,
    effectiveEnd: undefined,
    status: 0,
    remark: undefined
  }
  touPeriodRows.value = defaultTouPeriods.map((item) => ({ ...item }))
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
</style>
