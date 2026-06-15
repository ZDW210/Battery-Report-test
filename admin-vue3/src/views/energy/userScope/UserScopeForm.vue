<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="108px"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="App用户" prop="userId">
            <el-select
              v-model="formData.userId"
              class="!w-1/1"
              filterable
              placeholder="请选择App用户"
            >
              <el-option
                v-for="item in appUserList"
                :key="item.id"
                :label="formatAppUserLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用户类型" prop="userType">
            <el-select v-model="formData.userType" class="!w-1/1" placeholder="请选择用户类型">
              <el-option label="App用户" :value="1" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="授权类型" prop="scopeType">
            <el-select v-model="scopeType" class="!w-1/1" placeholder="请选择授权类型" @change="handleScopeTypeChange">
              <el-option label="客户" value="customer" />
              <el-option label="项目场站" value="project" />
              <el-option label="设备" value="device" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :value="dict.value">
                {{ dict.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col v-if="scopeType === 'customer'" :span="24">
          <el-form-item label="授权客户" prop="customerId">
            <el-select v-model="formData.customerId" class="!w-1/1" filterable placeholder="请选择客户">
              <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-if="scopeType === 'project'" :span="24">
          <el-form-item label="授权项目" prop="projectId">
            <el-select v-model="formData.projectId" class="!w-1/1" filterable placeholder="请选择项目场站">
              <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-if="scopeType === 'device'" :span="24">
          <el-form-item label="授权设备" prop="deviceId">
            <el-select v-model="formData.deviceId" class="!w-1/1" filterable placeholder="请选择设备">
              <el-option
                v-for="item in deviceList"
                :key="item.id"
                :label="formatDeviceLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="formData.remark"
              maxlength="512"
              placeholder="请输入备注"
              show-word-limit
              type="textarea"
            />
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
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyCustomerApi, EnergyCustomerVO } from '@/api/energy/customer'
import { EnergyDeviceApi, EnergyDeviceVO } from '@/api/energy/device'
import { EnergyProjectApi, EnergyProjectVO } from '@/api/energy/project'
import { EnergyUserScopeApi, EnergyUserScopeVO } from '@/api/energy/userScope'
import { EnergyAppUserApi, EnergyAppUserSimpleVO } from '@/api/energy/appUser'

defineOptions({ name: 'EnergyUserScopeForm' })

const { t } = useI18n()
const message = useMessage()

type ScopeType = 'customer' | 'project' | 'device'

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const scopeType = ref<ScopeType>('device')
const appUserList = ref<EnergyAppUserSimpleVO[]>([])
const customerList = ref<EnergyCustomerVO[]>([])
const projectList = ref<EnergyProjectVO[]>([])
const deviceList = ref<EnergyDeviceVO[]>([])
const formData = ref<EnergyUserScopeVO>({
  id: undefined,
  userId: undefined,
  userType: 1,
  customerId: undefined,
  projectId: undefined,
  deviceId: undefined,
  status: 0,
  remark: undefined
})
const formRules = reactive({
  userId: [{ required: true, message: 'App用户不能为空', trigger: 'change' }],
  userType: [{ required: true, message: '用户类型不能为空', trigger: 'change' }],
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
      formData.value = await EnergyUserScopeApi.getUserScope(id)
      scopeType.value = resolveScopeType(formData.value)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  normalizeScopeData()
  formLoading.value = true
  try {
    const data = formData.value as EnergyUserScopeVO
    if (formType.value === 'create') {
      await EnergyUserScopeApi.createUserScope(data)
      message.success(t('common.createSuccess'))
    } else {
      await EnergyUserScopeApi.updateUserScope(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  scopeType.value = 'device'
  formData.value = {
    id: undefined,
    userId: undefined,
    userType: 1,
    customerId: undefined,
    projectId: undefined,
    deviceId: undefined,
    status: 0,
    remark: undefined
  }
  formRef.value?.resetFields()
}

const loadOptions = async () => {
  const [appUsers, customers, projects, devices] = await Promise.all([
    EnergyAppUserApi.getAppUserSimpleList(),
    EnergyCustomerApi.getCustomerSimpleList(),
    EnergyProjectApi.getProjectSimpleList(),
    EnergyDeviceApi.getDeviceSimpleList()
  ])
  appUserList.value = appUsers
  customerList.value = customers
  projectList.value = projects
  deviceList.value = devices
}

const resolveScopeType = (data: EnergyUserScopeVO): ScopeType => {
  if (data.customerId) {
    return 'customer'
  }
  if (data.projectId) {
    return 'project'
  }
  return 'device'
}

const handleScopeTypeChange = () => {
  formData.value.customerId = undefined
  formData.value.projectId = undefined
  formData.value.deviceId = undefined
}

const normalizeScopeData = () => {
  if (scopeType.value !== 'customer') {
    formData.value.customerId = undefined
  }
  if (scopeType.value !== 'project') {
    formData.value.projectId = undefined
  }
  if (scopeType.value !== 'device') {
    formData.value.deviceId = undefined
  }
}

const formatDeviceLabel = (device: EnergyDeviceVO) => {
  return device.deviceNo ? `${device.deviceName} / ${device.deviceNo}` : device.deviceName
}

const formatAppUserLabel = (user: EnergyAppUserSimpleVO) => {
  const name = user.nickname || user.username || user.id
  const parts = [name, user.username, user.mobile, user.id].filter(Boolean)
  return Array.from(new Set(parts)).join(' / ')
}
</script>
