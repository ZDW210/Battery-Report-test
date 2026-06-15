<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="84px">
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          class="!w-260px"
          clearable
          placeholder="账号/昵称/手机号/卡号"
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
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="账号" min-width="220" prop="username" />
      <el-table-column align="center" label="昵称" min-width="140" prop="nickname" />
      <el-table-column align="center" label="手机号" min-width="130" prop="mobile">
        <template #default="scope">{{ scope.row.mobile || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="卡号" min-width="160" prop="cardNo">
        <template #default="scope">{{ scope.row.cardNo || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="小程序管理" width="120" prop="miniAdminEnabled">
        <template #default="scope">
          <el-tag :type="scope.row.miniAdminEnabled ? 'success' : 'info'">
            {{ scope.row.miniAdminEnabled ? '已开放' : '司机端' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="100" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="最近登录 IP" min-width="140" prop="loginIp">
        <template #default="scope">{{ scope.row.loginIp || '-' }}</template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="最近登录" prop="loginDate" width="180" />
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="120">
        <template #default="scope">
          <el-button
            v-hasPermi="['energy:app-user:update']"
            link
            type="primary"
            @click="openForm(scope.row.id)"
          >
            编辑
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

  <AppUserForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyAppUserApi, EnergyAppUserSimpleVO } from '@/api/energy/appUser'
import AppUserForm from './AppUserForm.vue'

defineOptions({ name: 'EnergyAppUser' })

const loading = ref(true)
const list = ref<EnergyAppUserSimpleVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined,
  status: undefined
})
const queryFormRef = ref()
const formRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyAppUserApi.getAppUserPage(queryParams)
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

const openForm = (id: number) => {
  formRef.value.open(id)
}

onMounted(() => {
  getList()
})
</script>
