<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="860px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="118px">
      <el-form-item label="客户名称" prop="name">
        <el-input v-model="formData.name" maxlength="128" placeholder="请输入客户名称" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="formData.contactName" maxlength="64" placeholder="请输入联系人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactMobile">
            <el-input v-model="formData.contactMobile" maxlength="32" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="区域" prop="region">
        <el-input v-model="formData.region" maxlength="64" placeholder="请输入区域" />
      </el-form-item>
      <el-divider content-position="left">账单表头信息</el-divider>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="户号" prop="accountNo">
            <el-input v-model="formData.accountNo" maxlength="64" placeholder="请输入账单户号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="供电服务单位" prop="supplyOrg">
            <el-input v-model="formData.supplyOrg" maxlength="128" placeholder="请输入供电服务单位" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="市场化属性" prop="marketAttribute">
            <el-select v-model="formData.marketAttribute" allow-create clearable filterable placeholder="请选择或输入市场化属性" class="!w-1/1">
              <el-option label="市场化交易" value="市场化交易" />
              <el-option label="代理购电" value="代理购电" />
              <el-option label="非市场化" value="非市场化" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交费截止日" prop="paymentDueDay">
            <el-input-number v-model="formData.paymentDueDay" :min="1" :max="31" :precision="0" class="!w-1/1" controls-position="right" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户服务" prop="customerService">
            <el-input v-model="formData.customerService" maxlength="64" placeholder="请输入客户服务电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="监督电话" prop="supervisePhone">
            <el-input v-model="formData.supervisePhone" maxlength="64" placeholder="请输入监督电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="打印人" prop="printPerson">
            <el-input v-model="formData.printPerson" maxlength="64" placeholder="默认 system" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="用电地址" prop="usageAddress">
            <el-input v-model="formData.usageAddress" maxlength="256" placeholder="请输入账单用电地址" />
          </el-form-item>
        </el-col>
      </el-row>
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
const emptyFormData = (): EnergyCustomerVO => ({
  id: undefined,
  name: undefined,
  contactName: undefined,
  contactMobile: undefined,
  region: undefined,
  accountNo: undefined,
  usageAddress: undefined,
  supplyOrg: undefined,
  marketAttribute: undefined,
  customerService: undefined,
  supervisePhone: undefined,
  printPerson: undefined,
  paymentDueDay: 12,
  status: 0,
  remark: undefined
})
const formData = ref<EnergyCustomerVO>(emptyFormData())
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
      formData.value = { ...emptyFormData(), ...(await EnergyCustomerApi.getCustomer(id)) }
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
  formData.value = emptyFormData()
  formRef.value?.resetFields()
}
</script>
