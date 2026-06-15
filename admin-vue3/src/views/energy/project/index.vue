<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="queryParams.name" class="!w-220px" clearable placeholder="请输入项目名称" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目编码" prop="code">
        <el-input v-model="queryParams.code" class="!w-180px" clearable placeholder="请输入项目编码" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-160px" clearable placeholder="请选择状态">
          <el-option v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
        <el-button v-hasPermi="['energy:project:create']" plain type="primary" @click="openProjectForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="项目名称" min-width="180" prop="name" />
      <el-table-column align="center" label="项目编码" min-width="130" prop="code" />
      <el-table-column align="center" label="客户编号" width="120" prop="customerId" />
      <el-table-column align="center" label="地址" min-width="220" prop="address" />
      <el-table-column align="center" label="状态" width="100" prop="status">
        <template #default="scope"><dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="160">
        <template #default="scope">
          <el-button v-hasPermi="['energy:project:update']" link type="primary" @click="openProjectForm('update', scope.row.id)">修改</el-button>
          <el-button v-hasPermi="['energy:project:delete']" link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>

  <ProjectForm ref="formRef" @success="handleProjectSuccess" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyProjectApi, EnergyProjectVO } from '@/api/energy/project'
import ProjectForm from './ProjectForm.vue'

defineOptions({ name: 'EnergyProject' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const list = ref<EnergyProjectVO[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, name: undefined, code: undefined, status: undefined })
const queryFormRef = ref()
const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyProjectApi.getProjectPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }
const formRef = ref()
const openProjectForm = (type: string, id?: number) => { formRef.value.open(type, id) }
const handleProjectSuccess = async () => { await getList() }
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EnergyProjectApi.deleteProject(id)
    message.success(t('common.delSuccess'))
    await handleProjectSuccess()
  } catch {}
}
onMounted(() => { getList() })
</script>
