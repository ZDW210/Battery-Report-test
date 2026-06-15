<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="104px"
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
          <el-form-item label="电量单价" prop="energyRate">
            <el-input-number
              v-model="formData.energyRate"
              :min="0"
              :precision="4"
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
const formData = ref<EnergyPricingRuleVO>({
  id: undefined,
  customerId: undefined,
  projectId: undefined,
  deviceId: undefined,
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
    const data = normalizeScope(formData.value)
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
  formRef.value?.resetFields()
}
</script>
