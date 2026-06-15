<!-- 移动储能设备表单 -->
<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="设备编码" prop="deviceNo">
            <el-input v-model="formData.deviceNo" maxlength="64" placeholder="请输入设备编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备名称" prop="deviceName">
            <el-input v-model="formData.deviceName" maxlength="128" placeholder="请输入设备名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备类型" prop="deviceType">
            <el-select v-model="formData.deviceType" class="!w-1/1" placeholder="请选择设备类型">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ENERGY_DEVICE_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备状态" prop="status">
            <el-select v-model="formData.status" class="!w-1/1" placeholder="请选择设备状态">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ENERGY_DEVICE_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="运行模式" prop="runMode">
            <el-select v-model="formData.runMode" clearable class="!w-1/1" placeholder="请选择运行模式">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.ENERGY_RUN_MODE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="网关序列号" prop="gatewaySn">
            <el-input v-model="formData.gatewaySn" maxlength="64" placeholder="例如 12207013690004" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电表序列号" prop="meterSn">
            <el-input v-model="formData.meterSn" maxlength="64" placeholder="例如 12207013690004" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="仪表编号" prop="meterNo">
            <el-input v-model="formData.meterNo" maxlength="128" placeholder="例如 网关序列号_电表序列号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属客户" prop="customerId">
            <el-select
              v-model="formData.customerId"
              class="!w-1/1"
              clearable
              filterable
              placeholder="请选择客户"
              @change="handleCustomerChange"
            >
              <el-option
                v-for="item in customerList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="项目场站" prop="projectId">
            <el-select
              v-model="formData.projectId"
              class="!w-1/1"
              clearable
              filterable
              placeholder="请选择项目"
            >
              <el-option
                v-for="item in projectList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="纬度" prop="latitude">
            <el-input-number
              v-model="formData.latitude"
              :precision="6"
              class="!w-1/1"
              controls-position="right"
              placeholder="请输入纬度"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="经度" prop="longitude">
            <el-input-number
              v-model="formData.longitude"
              :precision="6"
              class="!w-1/1"
              controls-position="right"
              placeholder="请输入经度"
            />
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
import { EnergyDeviceApi, EnergyDeviceVO } from '@/api/energy/device'
import { EnergyCustomerApi, EnergyCustomerVO } from '@/api/energy/customer'
import { EnergyProjectApi, EnergyProjectVO } from '@/api/energy/project'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

/** 移动储能设备表单 */
defineOptions({ name: 'EnergyDeviceForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const customerList = ref<EnergyCustomerVO[]>([])
const projectList = ref<EnergyProjectVO[]>([])
const formData = ref<EnergyDeviceVO>({
  id: undefined,
  deviceNo: undefined,
  deviceName: undefined,
  deviceType: undefined,
  gatewaySn: undefined,
  meterSn: undefined,
  meterNo: undefined,
  customerId: undefined,
  projectId: undefined,
  status: 1,
  runMode: undefined,
  latitude: undefined,
  longitude: undefined,
  remark: undefined
})
const formRules = reactive({
  deviceNo: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
  deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
  deviceType: [{ required: true, message: '设备类型不能为空', trigger: 'change' }],
  gatewaySn: [{ required: true, message: '网关序列号不能为空', trigger: 'blur' }],
  meterSn: [{ required: true, message: '电表序列号不能为空', trigger: 'blur' }],
  meterNo: [{ required: true, message: '仪表编号不能为空', trigger: 'blur' }],
  customerId: [{ required: true, message: '所属客户不能为空', trigger: 'change' }],
  projectId: [{ required: true, message: '项目场站不能为空', trigger: 'change' }],
  status: [{ required: true, message: '设备状态不能为空', trigger: 'change' }]
})
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  await loadCustomerList()
  await loadProjectList()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EnergyDeviceApi.getDevice(id)
      await loadProjectList(formData.value.customerId)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as EnergyDeviceVO
    if (formType.value === 'create') {
      await EnergyDeviceApi.createDevice(data)
      message.success(t('common.createSuccess'))
    } else {
      await EnergyDeviceApi.updateDevice(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    deviceNo: undefined,
    deviceName: undefined,
    deviceType: undefined,
    gatewaySn: undefined,
    meterSn: undefined,
    meterNo: undefined,
    customerId: undefined,
    projectId: undefined,
    status: 1,
    runMode: undefined,
    latitude: undefined,
    longitude: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}

const loadCustomerList = async () => {
  customerList.value = await EnergyCustomerApi.getCustomerSimpleList()
}

const loadProjectList = async (customerId?: number) => {
  projectList.value = await EnergyProjectApi.getProjectSimpleList(customerId)
}

const handleCustomerChange = async () => {
  formData.value.projectId = undefined
  await loadProjectList(formData.value.customerId)
}
</script>
