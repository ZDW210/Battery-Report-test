<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="84px">
      <el-form-item label="App用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          class="!w-220px"
          clearable
          placeholder="请输入App用户ID"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-160px" clearable placeholder="请选择状态">
          <el-option v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
        <el-button v-hasPermi="['energy:user-scope:create']" plain type="primary" @click="openUserScopeForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="App用户" min-width="220">
        <template #default="scope">{{ formatAppUserName(scope.row) }}</template>
      </el-table-column>
      <el-table-column align="center" label="用户类型" width="110" prop="userType">
        <template #default="scope">{{ formatUserType(scope.row.userType) }}</template>
      </el-table-column>
      <el-table-column align="center" label="授权类型" width="110">
        <template #default="scope">{{ formatScopeType(scope.row) }}</template>
      </el-table-column>
      <el-table-column align="center" label="授权范围" min-width="220">
        <template #default="scope">{{ formatScopeName(scope.row) }}</template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="100" prop="status">
        <template #default="scope"><dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column align="center" label="备注" min-width="160" prop="remark" />
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="160">
        <template #default="scope">
          <el-button v-hasPermi="['energy:user-scope:update']" link type="primary" @click="openUserScopeForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['energy:user-scope:delete']" link type="danger" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>

  <UserScopeForm ref="formRef" @success="handleUserScopeSuccess" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyUserScopeApi, EnergyUserScopeVO } from '@/api/energy/userScope'
import UserScopeForm from './UserScopeForm.vue'

defineOptions({ name: 'EnergyUserScope' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const list = ref<EnergyUserScopeVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: undefined,
  userType: 1,
  status: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyUserScopeApi.getUserScopePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  queryParams.userType = 1
  handleQuery()
}

const formRef = ref()
const openUserScopeForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleUserScopeSuccess = async () => {
  await getList()
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EnergyUserScopeApi.deleteUserScope(id)
    message.success(t('common.delSuccess'))
    await handleUserScopeSuccess()
  } catch {}
}

const formatUserType = (userType?: number) => {
  return userType === 1 ? 'App用户' : userType || '-'
}

const formatAppUserName = (row: EnergyUserScopeVO) => {
  const name = row.userNickname || row.userUsername || row.userId
  const parts = [name, row.userUsername, row.userMobile, row.userId].filter(Boolean)
  return Array.from(new Set(parts)).join(' / ')
}

const formatScopeType = (row: EnergyUserScopeVO) => {
  if (row.customerId) {
    return '客户'
  }
  if (row.projectId) {
    return '项目场站'
  }
  if (row.deviceId) {
    return '设备'
  }
  return '-'
}

const formatScopeName = (row: EnergyUserScopeVO) => {
  if (row.customerId) {
    return row.customerName || row.customerId
  }
  if (row.projectId) {
    return row.projectName || row.projectId
  }
  if (row.deviceId) {
    return row.deviceNo ? `${row.deviceName} / ${row.deviceNo}` : row.deviceName || row.deviceId
  }
  return '-'
}

onMounted(() => {
  getList()
})
</script>
