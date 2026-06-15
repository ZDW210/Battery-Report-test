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
          <el-form-item label="车辆编号" prop="vehicleNo">
            <el-input v-model="formData.vehicleNo" maxlength="64" placeholder="请输入车辆编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="车牌号" prop="plateNo">
            <el-input v-model="formData.plateNo" maxlength="64" placeholder="请输入车牌号" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="二维码内容" prop="qrCode">
            <el-input v-model="formData.qrCode" maxlength="128" placeholder="请输入二维码内容" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="绑定设备" prop="deviceId">
            <el-select
              v-model="formData.deviceId"
              class="!w-1/1"
              filterable
              placeholder="请选择绑定设备"
            >
              <el-option
                v-for="item in deviceList"
                :key="item.id"
                :label="formatDeviceLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
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
import { EnergyDeviceApi, EnergyDeviceVO } from '@/api/energy/device'
import { EnergyVehicleApi, EnergyVehicleVO } from '@/api/energy/vehicle'

defineOptions({ name: 'EnergyVehicleForm' })

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const deviceList = ref<EnergyDeviceVO[]>([])
const formData = ref<EnergyVehicleVO>({
  id: undefined,
  vehicleNo: undefined,
  plateNo: undefined,
  qrCode: undefined,
  deviceId: undefined,
  status: 0,
  remark: undefined
})
const formRules = reactive({
  vehicleNo: [{ required: true, message: '车辆编号不能为空', trigger: 'blur' }],
  plateNo: [{ required: true, message: '车牌号不能为空', trigger: 'blur' }],
  qrCode: [{ required: true, message: '二维码内容不能为空', trigger: 'blur' }],
  deviceId: [{ required: true, message: '绑定设备不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  await loadDevices()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EnergyVehicleApi.getVehicle(id)
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
    const data = formData.value as EnergyVehicleVO
    if (formType.value === 'create') {
      await EnergyVehicleApi.createVehicle(data)
      message.success(t('common.createSuccess'))
    } else {
      await EnergyVehicleApi.updateVehicle(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    vehicleNo: undefined,
    plateNo: undefined,
    qrCode: undefined,
    deviceId: undefined,
    status: 0,
    remark: undefined
  }
  formRef.value?.resetFields()
}

const loadDevices = async () => {
  deviceList.value = await EnergyDeviceApi.getDeviceSimpleList()
}

const formatDeviceLabel = (device: EnergyDeviceVO) => {
  const name = device.deviceName || device.id
  const parts = [name, device.deviceNo, device.meterNo].filter(Boolean)
  return Array.from(new Set(parts)).join(' / ')
}
</script>
