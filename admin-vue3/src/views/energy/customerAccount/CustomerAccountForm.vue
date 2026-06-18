<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="112px"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属客户" prop="customerId">
            <el-select
              v-model="formData.customerId"
              class="!w-1/1"
              filterable
              placeholder="请选择客户"
            >
              <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
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
        <el-col :span="12">
          <el-form-item label="登录账号" prop="username">
            <el-input
              v-model="formData.username"
              :disabled="formType === 'update'"
              maxlength="30"
              placeholder="4-30位数字或字母"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="formType === 'create' ? '首次密码' : '密码'" prop="password">
            <el-input
              v-model="formData.password"
              :disabled="formType === 'update'"
              maxlength="32"
              placeholder="8-32位，需同时包含字母和数字"
              show-password
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="账号名称" prop="nickname">
            <el-input v-model="formData.nickname" maxlength="30" placeholder="例如：某某车队老板" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="mobile">
            <el-input v-model="formData.mobile" maxlength="32" placeholder="用于备注和联系" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="开放板块" prop="menuIds">
            <el-checkbox-group v-model="formData.menuIds" class="module-grid">
              <el-checkbox
                v-for="item in menuOptions"
                :key="item.id"
                :label="item.id"
                border
              >
                {{ item.name }}
              </el-checkbox>
            </el-checkbox-group>
            <div class="form-tip">勾选后客户老板登录网页端只显示这些板块；默认仅开放查询和导出能力。</div>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="formData.remark"
              maxlength="512"
              placeholder="可填写账号用途或交付记录"
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
import {
  EnergyCustomerAccountApi,
  EnergyCustomerAccountMenuOptionVO,
  EnergyCustomerAccountVO
} from '@/api/energy/customerAccount'

defineOptions({ name: 'EnergyCustomerAccountForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref<'create' | 'update'>('create')
const customerList = ref<EnergyCustomerVO[]>([])
const menuOptions = ref<EnergyCustomerAccountMenuOptionVO[]>([])
const formData = ref<EnergyCustomerAccountVO>({
  id: undefined,
  customerId: undefined,
  username: undefined,
  password: undefined,
  nickname: undefined,
  mobile: undefined,
  status: 0,
  menuIds: [],
  remark: undefined
})
const formRules = reactive({
  customerId: [{ required: true, message: '所属客户不能为空', trigger: 'change' }],
  username: [
    { required: true, message: '登录账号不能为空', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{4,30}$/, message: '登录账号由4-30位数字或字母组成', trigger: 'blur' }
  ],
  password: [{ validator: (_, value, callback) => validatePassword(value, callback), trigger: 'blur' }],
  nickname: [{ required: true, message: '账号名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
  menuIds: [{ required: true, message: '至少选择一个开放板块', trigger: 'change', type: 'array' }]
})
const formRef = ref()

const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增客户账号权限' : '编辑客户账号权限'
  formType.value = type
  resetForm()
  await loadOptions()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EnergyCustomerAccountApi.getCustomerAccount(id)
      formData.value.password = undefined
      formData.value.menuIds = formData.value.menuIds || []
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
    const data = formData.value
    if (formType.value === 'create') {
      await EnergyCustomerAccountApi.createCustomerAccount(data)
      message.success(t('common.createSuccess'))
    } else {
      await EnergyCustomerAccountApi.updateCustomerAccount(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const validatePassword = (value: string | undefined, callback: (error?: Error) => void) => {
  if (formType.value === 'update') {
    callback()
    return
  }
  if (!value) {
    callback(new Error('首次密码不能为空'))
    return
  }
  if (value.length < 8 || value.length > 32) {
    callback(new Error('密码长度为8-32位'))
    return
  }
  if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) {
    callback(new Error('密码必须同时包含字母和数字'))
    return
  }
  callback()
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    customerId: undefined,
    username: undefined,
    password: undefined,
    nickname: undefined,
    mobile: undefined,
    status: 0,
    menuIds: [],
    remark: undefined
  }
  formRef.value?.resetFields()
}

const loadOptions = async () => {
  const [customers, menus] = await Promise.all([
    EnergyCustomerApi.getCustomerSimpleList(),
    EnergyCustomerAccountApi.getMenuOptions()
  ])
  customerList.value = customers
  menuOptions.value = menus
}
</script>

<style scoped>
.module-grid {
  display: grid;
  width: 100%;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}

.module-grid :deep(.el-checkbox.is-bordered) {
  height: 36px;
  margin: 0;
  display: flex;
  align-items: center;
}

.form-tip {
  margin-top: 6px;
  color: #606266;
  font-size: 12px;
  line-height: 18px;
}
</style>
