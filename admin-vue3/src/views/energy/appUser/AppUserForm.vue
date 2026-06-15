<template>
  <Dialog v-model="dialogVisible" title="编辑使用账户" width="620px">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="96px">
      <el-form-item label="账号">
        <el-input v-model="formData.username" disabled />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="formData.nickname" maxlength="64" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="formData.mobile" maxlength="32" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="卡号" prop="cardNo">
        <el-input v-model="formData.cardNo" maxlength="64" placeholder="请输入刷卡卡号" />
      </el-form-item>
      <el-form-item label="小程序管理" prop="miniAdminEnabled">
        <el-switch
          v-model="formData.miniAdminEnabled"
          active-text="开放"
          inactive-text="司机"
          inline-prompt
        />
        <div class="form-tip">默认司机账号；开放后，小程序“我的”页会显示“管理”入口。</div>
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
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyAppUserApi, EnergyAppUserSimpleVO } from '@/api/energy/appUser'

defineOptions({ name: 'EnergyAppUserForm' })

const message = useMessage()
const { t } = useI18n()
const dialogVisible = ref(false)
const formLoading = ref(false)
const formData = ref<EnergyAppUserSimpleVO>({
  id: 0,
  username: undefined,
  nickname: undefined,
  mobile: undefined,
  cardNo: undefined,
  miniAdminEnabled: false,
  status: 0,
  remark: undefined
})
const formRules = reactive({
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (id: number) => {
  dialogVisible.value = true
  formLoading.value = true
  try {
    formData.value = await EnergyAppUserApi.getAppUser(id)
  } finally {
    formLoading.value = false
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    await EnergyAppUserApi.updateAppUser(formData.value)
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>

<style scoped>
.form-tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  line-height: 18px;
}
</style>
