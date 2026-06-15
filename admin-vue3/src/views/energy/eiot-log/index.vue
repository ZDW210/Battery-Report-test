<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="92px"
    >
      <el-form-item label="同步类型" prop="syncType">
        <el-select v-model="queryParams.syncType" clearable placeholder="请选择类型" class="!w-160px">
          <el-option label="告警" value="alarm" />
          <el-option label="实时数据" value="realtime" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" class="!w-140px">
          <el-option label="成功" :value="0" />
          <el-option label="失败" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="请求编号" prop="requestId">
        <el-input
          v-model="queryParams.requestId"
          clearable
          placeholder="请输入请求编号"
          class="!w-220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="网关序列号" prop="gatewaySn">
        <el-input
          v-model="queryParams.gatewaySn"
          clearable
          placeholder="请输入网关序列号"
          class="!w-220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="电表序列号" prop="meterSn">
        <el-input
          v-model="queryParams.meterSn"
          clearable
          placeholder="请输入电表序列号"
          class="!w-220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="!w-360px"
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
      <el-card shadow="never" class="eiot-log__stat">
        <div>
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
        <Icon :icon="item.icon" :style="{ color: item.color }" :size="28" />
      </el-card>
    </el-col>
  </el-row>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="状态" width="90" prop="status">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" effect="light">
            {{ row.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="同步类型" width="110" prop="syncType">
        <template #default="{ row }">{{ getSyncTypeText(row.syncType) }}</template>
      </el-table-column>
      <el-table-column label="请求编号" min-width="160" prop="requestId">
        <template #default="{ row }">{{ row.requestId || '-' }}</template>
      </el-table-column>
      <el-table-column label="网关序列号" min-width="150" prop="gatewaySn">
        <template #default="{ row }">{{ row.gatewaySn || '-' }}</template>
      </el-table-column>
      <el-table-column label="电表序列号" min-width="150" prop="meterSn">
        <template #default="{ row }">{{ row.meterSn || '-' }}</template>
      </el-table-column>
      <el-table-column label="错误信息" min-width="260" prop="errorMsg">
        <template #default="{ row }">
          <span :class="{ 'eiot-log__error': row.status === 1 }">{{ row.errorMsg || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="报文归档" min-width="180" prop="payloadUrl">
        <template #default="{ row }">
          <el-link
            v-if="row.payloadUrl"
            type="primary"
            :href="row.payloadUrl"
            target="_blank"
            :underline="false"
          >
            查看报文
          </el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="创建时间"
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
import { EnergyEiotSyncLogApi } from '@/api/energy/eiotLog'
import type { EnergyEiotSyncLogVO } from '@/api/energy/eiotLog'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'EnergyEiotLog' })

const loading = ref(true)
const list = ref<EnergyEiotSyncLogVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  syncType: undefined,
  requestId: undefined,
  gatewaySn: undefined,
  meterSn: undefined,
  status: undefined,
  createTime: undefined
})

const statCards = computed(() => {
  const success = list.value.filter((item) => item.status === 0).length
  const failed = list.value.filter((item) => item.status === 1).length
  return [
    { label: '当前页日志', value: list.value.length, icon: 'ep:document', color: '#2563eb' },
    { label: '当前页成功', value: success, icon: 'ep:circle-check', color: '#0f766e' },
    { label: '当前页失败', value: failed, icon: 'ep:warning', color: '#dc2626' },
    { label: '全部匹配', value: total.value, icon: 'ep:data-line', color: '#7c3aed' }
  ]
})

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyEiotSyncLogApi.getSyncLogPage(queryParams)
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

const getSyncTypeText = (syncType?: string) => {
  const typeMap: Record<string, string> = {
    alarm: '告警',
    realtime: '实时数据'
  }
  return syncType ? typeMap[syncType] || syncType : '-'
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.eiot-log {
  &__stat {
    margin-bottom: 12px;

    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      justify-content: space-between;
      min-height: 80px;
    }

    span,
    strong {
      display: block;
    }

    span {
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }

    strong {
      margin-top: 6px;
      color: var(--el-text-color-primary);
      font-size: 22px;
      line-height: 28px;
    }
  }

  &__error {
    color: var(--el-color-danger);
  }
}
</style>
