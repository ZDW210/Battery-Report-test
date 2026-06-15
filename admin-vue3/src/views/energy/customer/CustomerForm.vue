<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="90px">
      <el-form-item label="客户名称" prop="name">
        <el-input v-model="formData.name" maxlength="128" placeholder="请输入客户名称" />
      </el-form-item>
      <el-form-item label="联系人" prop="contactName">
        <el-input v-model="formData.contactName" maxlength="64" placeholder="请输入联系人" />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactMobile">
        <el-input v-model="formData.contactMobile" maxlength="32" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="区域" prop="region">
        <el-input v-model="formData.region" maxlength="64" placeholder="请输入区域" />
      </el-form-item>
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
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" maxlength="512" show-word-limit type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { EnergyCustomerApi, EnergyCustomerVO } from '@/api/energy/customer'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

defineOptions({ name: 'EnergyCustomerForm' })

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<EnergyCustomerVO>({ id: undefined, name: undefined, contactName: undefined, contactMobile: undefined, region: undefined, status: 0, remark: undefined })
const formRules = reactive({
  name: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EnergyCustomerApi.getCustomer(id)
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
    const data = formData.value as EnergyCustomerVO
    if (formType.value === 'create') {
      await EnergyCustomerApi.createCustomer(data)
      message.success(t('common.createSuccess'))
    } else {
      await EnergyCustomerApi.updateCustomer(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = { id: undefined, name: undefined, contactName: undefined, contactMobile: undefined, region: undefined, status: 0, remark: undefined }
  formRef.value?.resetFields()
}
</script>
