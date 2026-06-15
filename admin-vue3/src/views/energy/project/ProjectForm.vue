<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="90px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属客户" prop="customerId">
            <el-select v-model="formData.customerId" class="!w-1/1" filterable placeholder="请选择客户">
              <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="项目编码" prop="code">
            <el-input v-model="formData.code" maxlength="64" placeholder="请输入项目编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="项目名称" prop="name">
            <el-input v-model="formData.name" maxlength="128" placeholder="请输入项目名称" />
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
        <el-col :span="24">
          <el-form-item label="地址" prop="address">
            <el-input v-model="formData.address" maxlength="255" placeholder="请输入地址" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="纬度" prop="latitude">
            <el-input-number v-model="formData.latitude" :precision="6" class="!w-1/1" controls-position="right" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="经度" prop="longitude">
            <el-input-number v-model="formData.longitude" :precision="6" class="!w-1/1" controls-position="right" />
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
import { EnergyCustomerApi, EnergyCustomerVO } from '@/api/energy/customer'
import { EnergyProjectApi, EnergyProjectVO } from '@/api/energy/project'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

defineOptions({ name: 'EnergyProjectForm' })

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const customerList = ref<EnergyCustomerVO[]>([])
const formData = ref<EnergyProjectVO>({ id: undefined, customerId: undefined, name: undefined, code: undefined, address: undefined, latitude: undefined, longitude: undefined, status: 0, remark: undefined })
const formRules = reactive({
  customerId: [{ required: true, message: '客户不能为空', trigger: 'change' }],
  name: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '项目编码不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  customerList.value = await EnergyCustomerApi.getCustomerSimpleList()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EnergyProjectApi.getProject(id)
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
    const data = formData.value as EnergyProjectVO
    if (formType.value === 'create') {
      await EnergyProjectApi.createProject(data)
      message.success(t('common.createSuccess'))
    } else {
      await EnergyProjectApi.updateProject(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = { id: undefined, customerId: undefined, name: undefined, code: undefined, address: undefined, latitude: undefined, longitude: undefined, status: 0, remark: undefined }
  formRef.value?.resetFields()
}
</script>
