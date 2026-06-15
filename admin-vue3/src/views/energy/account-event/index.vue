<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="92px">
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          class="!w-260px"
          clearable
          placeholder="账户/设备/仪表/卡号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作场景" prop="eventScene">
        <el-select v-model="queryParams.eventScene" class="!w-140px" clearable placeholder="请选择">
          <el-option label="扫码校验" value="VERIFY" />
          <el-option label="开始放电" value="DISCHARGE" />
        </el-select>
      </el-form-item>
      <el-form-item label="识别方式" prop="authType">
        <el-select v-model="queryParams.authType" class="!w-140px" clearable placeholder="请选择">
          <el-option label="微信扫码" value="WECHAT" />
          <el-option label="刷卡" value="CARD" />
        </el-select>
      </el-form-item>
      <el-form-item label="识别结果" prop="accountKnown">
        <el-select v-model="queryParams.accountKnown" class="!w-140px" clearable placeholder="请选择">
          <el-option label="已识别" :value="true" />
          <el-option label="未知账户" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item label="发生时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          class="!w-360px"
          end-placeholder="结束时间"
          start-placeholder="开始时间"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
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

  <el-row :gutter="12" class="mb-12px">
    <el-col v-for="item in statCards" :key="item.label" :lg="6" :md="12" :xs="24">
      <el-card class="account-event__stat" shadow="never">
        <div>
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
        <Icon :color="item.color" :icon="item.icon" :size="26" />
      </el-card>
    </el-col>
  </el-row>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="账户结果" prop="accountKnown" width="110">
        <template #default="{ row }">
          <el-tag :type="row.accountKnown ? 'success' : 'danger'" effect="light">
            {{ row.accountKnown ? '已识别' : '未知账户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="任务结果" min-width="110">
        <template #default="{ row }">
          <el-tag :type="getTaskResult(row).type" effect="light">
            {{ getTaskResult(row).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="场景" prop="eventScene" width="110">
        <template #default="{ row }">{{ getEventSceneText(row.eventScene) }}</template>
      </el-table-column>
      <el-table-column align="center" label="方式" prop="authType" width="110">
        <template #default="{ row }">{{ getAuthTypeText(row.authType) }}</template>
      </el-table-column>
      <el-table-column label="账户" min-width="170">
        <template #default="{ row }">
          {{ row.accountName || row.accountMobile || row.accountId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="卡号" min-width="150" prop="cardNo">
        <template #default="{ row }">{{ row.cardNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="设备" min-width="200">
        <template #default="{ row }">
          {{ row.deviceName || row.deviceNo || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="仪表编号" min-width="170" prop="meterNo">
        <template #default="{ row }">{{ row.meterNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="网关/电表" min-width="210">
        <template #default="{ row }">
          {{ row.gatewaySn || '-' }} / {{ row.meterSn || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="提示" min-width="150" prop="resultMessage">
        <template #default="{ row }">{{ row.resultMessage || '-' }}</template>
      </el-table-column>
      <el-table-column label="二维码内容" min-width="220" prop="scanText">
        <template #default="{ row }">{{ row.scanText || '-' }}</template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="发生时间"
        prop="createTime"
        width="180"
      />
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { EnergyAccountEventApi, EnergyAccountEventVO } from '@/api/energy/accountEvent'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'EnergyAccountEvent' })

const loading = ref(true)
const list = ref<EnergyAccountEventVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined,
  eventScene: undefined,
  authType: undefined,
  accountKnown: undefined,
  createTime: undefined
})

const statCards = computed(() => {
  const known = list.value.filter((item) => item.accountKnown).length
  const unknown = list.value.filter((item) => item.accountKnown === false).length
  const card = list.value.filter((item) => item.authType === 'CARD').length
  const failed = list.value.filter((item) => isDischargeFailed(item)).length
  return [
    { label: '当前页记录', value: list.value.length, icon: 'ep:tickets', color: '#2563eb' },
    { label: '已识别账户', value: known, icon: 'ep:circle-check', color: '#0f766e' },
    { label: '未知账户', value: unknown, icon: 'ep:warning', color: '#dc2626' },
    { label: '启动失败', value: failed, icon: 'ep:circle-close', color: '#ea580c' },
    { label: '刷卡记录', value: card, icon: 'ep:postcard', color: '#7c3aed' }
  ]
})

const getEventSceneText = (scene?: string) => {
  const map: Record<string, string> = {
    VERIFY: '扫码校验',
    DISCHARGE: '开始放电'
  }
  return scene ? map[scene] || scene : '-'
}

const getAuthTypeText = (type?: string) => {
  const map: Record<string, string> = {
    WECHAT: '微信扫码',
    CARD: '刷卡'
  }
  return type ? map[type] || type : '-'
}

const isDischargeFailed = (row: EnergyAccountEventVO) =>
  row.eventScene === 'DISCHARGE' && !!row.resultMessage?.includes('失败')

const getTaskResult = (row: EnergyAccountEventVO): { text: string; type: 'success' | 'warning' | 'danger' | 'info' } => {
  if (isDischargeFailed(row)) {
    return { text: '启动失败', type: 'warning' }
  }
  if (row.eventScene === 'DISCHARGE' && row.resultMessage?.includes('已开始')) {
    return { text: '已启动', type: 'success' }
  }
  if (row.accountKnown === false) {
    return { text: '已拒绝', type: 'danger' }
  }
  return { text: '仅校验', type: 'info' }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyAccountEventApi.getAccountEventPage(queryParams)
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

onMounted(() => {
  getList()
})
</script>

<style scoped>
.account-event__stat {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.account-event__stat :deep(.el-card__body) {
  align-items: center;
  display: flex;
  justify-content: space-between;
  padding: 14px 16px;
}

.account-event__stat span {
  color: #64748b;
  display: block;
  font-size: 13px;
  line-height: 20px;
}

.account-event__stat strong {
  color: #0f172a;
  display: block;
  font-size: 24px;
  line-height: 30px;
  margin-top: 4px;
}
</style>
