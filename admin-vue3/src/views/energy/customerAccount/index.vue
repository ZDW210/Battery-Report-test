<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="84px">
      <el-form-item label="所属客户" prop="customerId">
        <el-select
          v-model="queryParams.customerId"
          class="!w-220px"
          clearable
          filterable
          placeholder="请选择客户"
        >
          <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键字" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          class="!w-240px"
          clearable
          placeholder="账号/名称/手机号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-140px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
        <el-button
          v-hasPermi="['energy:customer-account:create']"
          plain
          type="primary"
          @click="openForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          新增客户账号
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="所属客户" min-width="180" prop="customerName" />
      <el-table-column align="center" label="登录账号" min-width="150" prop="username" />
      <el-table-column align="center" label="账号名称" min-width="140" prop="nickname" />
      <el-table-column align="center" label="手机号" min-width="130" prop="mobile">
        <template #default="scope">{{ scope.row.mobile || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="开放板块" min-width="260">
        <template #default="scope">
          <el-space wrap>
            <el-tag v-for="name in formatMenuNames(scope.row.menuIds)" :key="name" type="success">
              {{ name }}
            </el-tag>
          </el-space>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="100" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="190">
        <template #default="scope">
          <el-button
            v-hasPermi="['energy:customer-account:update']"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['energy:customer-account:reset-password']"
            link
            type="primary"
            @click="handleResetPassword(scope.row)"
          >
            重置密码
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <CustomerAccountForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyCustomerApi, EnergyCustomerVO } from '@/api/energy/customer'
import {
  EnergyCustomerAccountApi,
  EnergyCustomerAccountMenuOptionVO,
  EnergyCustomerAccountVO
} from '@/api/energy/customerAccount'
import CustomerAccountForm from './CustomerAccountForm.vue'

defineOptions({ name: 'EnergyCustomerAccount' })

const message = useMessage()
const loading = ref(true)
const list = ref<EnergyCustomerAccountVO[]>([])
const total = ref(0)
const customerList = ref<EnergyCustomerVO[]>([])
const menuOptions = ref<EnergyCustomerAccountMenuOptionVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  customerId: undefined,
  keyword: undefined,
  status: undefined
})
const queryFormRef = ref()
const formRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyCustomerAccountApi.getCustomerAccountPage(queryParams)
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
  handleQuery()
}

const openForm = (type: 'create' | 'update', id?: number) => {
  formRef.value.open(type, id)
}

const handleResetPassword = async (row: EnergyCustomerAccountVO) => {
  const password = await message.prompt('请输入新的临时密码，8-32位且包含字母和数字', '重置密码', {
    inputPattern: /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/,
    inputErrorMessage: '密码必须为8-32位，并同时包含字母和数字'
  })
  await EnergyCustomerAccountApi.resetPassword(row.id!, password.value)
  message.success('密码已重置')
}

const formatMenuNames = (menuIds?: number[]) => {
  if (!menuIds?.length) {
    return ['未开放']
  }
  const menuMap = new Map(menuOptions.value.map((item) => [item.id, item.name]))
  return menuIds.map((id) => menuMap.get(id) || String(id))
}

const loadOptions = async () => {
  const [customers, menus] = await Promise.all([
    EnergyCustomerApi.getCustomerSimpleList(),
    EnergyCustomerAccountApi.getMenuOptions()
  ])
  customerList.value = customers
  menuOptions.value = menus
}

onMounted(async () => {
  await loadOptions()
  await getList()
})
</script>
