<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="客户名称" prop="name">
        <el-input v-model="queryParams.name" class="!w-220px" clearable placeholder="请输入客户名称" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="联系人" prop="contactName">
        <el-input v-model="queryParams.contactName" class="!w-180px" clearable placeholder="请输入联系人" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-160px" clearable placeholder="请选择状态">
          <el-option v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
        <el-button v-hasPermi="['energy:customer:create']" plain type="primary" @click="openCustomerForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="客户名称" min-width="180" prop="name" />
      <el-table-column align="center" label="联系人" min-width="120" prop="contactName" />
      <el-table-column align="center" label="联系电话" min-width="140" prop="contactMobile" />
      <el-table-column align="center" label="区域" min-width="120" prop="region" />
      <el-table-column align="center" label="状态" width="100" prop="status">
        <template #default="scope"><dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="160">
        <template #default="scope">
          <el-button v-hasPermi="['energy:customer:update']" link type="primary" @click="openCustomerForm('update', scope.row.id)">修改</el-button>
          <el-button v-hasPermi="['energy:customer:delete']" link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>

  <CustomerForm ref="formRef" @success="handleCustomerSuccess" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyCustomerApi, EnergyCustomerVO } from '@/api/energy/customer'
import CustomerForm from './CustomerForm.vue'

defineOptions({ name: 'EnergyCustomer' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const list = ref<EnergyCustomerVO[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, name: undefined, contactName: undefined, status: undefined })
const queryFormRef = ref()
const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyCustomerApi.getCustomerPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }
const formRef = ref()
const openCustomerForm = (type: string, id?: number) => { formRef.value.open(type, id) }
const handleCustomerSuccess = async () => { await getList() }
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EnergyCustomerApi.deleteCustomer(id)
    message.success(t('common.delSuccess'))
    await handleCustomerSuccess()
  } catch {}
}
onMounted(() => { getList() })
</script>
