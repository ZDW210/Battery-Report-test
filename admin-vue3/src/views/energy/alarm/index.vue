<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="72px"
    >
      <el-form-item label="设备编号" prop="deviceId">
        <el-input
          v-model="queryParams.deviceId"
          class="!w-160px"
          clearable
          placeholder="请输入设备编号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="告警代码" prop="code">
        <el-input
          v-model="queryParams.code"
          class="!w-200px"
          clearable
          placeholder="请输入告警代码"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="告警标题" prop="title">
        <el-input
          v-model="queryParams.title"
          class="!w-200px"
          clearable
          placeholder="请输入告警标题"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="告警等级" prop="level">
        <el-select v-model="queryParams.level" class="!w-150px" clearable placeholder="请选择等级">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.ENERGY_ALARM_LEVEL)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-150px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.ENERGY_ALARM_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发生时间" prop="occurTime">
        <el-date-picker
          v-model="queryParams.occurTime"
          class="!w-240px"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          end-placeholder="结束日期"
          start-placeholder="开始日期"
          type="daterange"
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

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="告警编号" min-width="160" prop="alarmNo" />
      <el-table-column align="center" label="设备" min-width="160" prop="deviceName">
        <template #default="scope">{{ scope.row.deviceName || scope.row.deviceId }}</template>
      </el-table-column>
      <el-table-column align="center" label="告警代码" min-width="170" prop="code" />
      <el-table-column align="center" label="告警标题" min-width="180" prop="title" />
      <el-table-column align="center" label="等级" width="100" prop="level">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.ENERGY_ALARM_LEVEL" :value="scope.row.level" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="100" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.ENERGY_ALARM_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="发生时间"
        prop="occurTime"
        width="180"
      />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="确认时间"
        prop="ackTime"
        width="180"
      />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="关闭时间"
        prop="closeTime"
        width="180"
      />
      <el-table-column align="center" fixed="right" label="操作" width="210">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row.id)">详情</el-button>
          <el-button
            v-if="scope.row.status === 0"
            v-hasPermi="['energy:alarm:ack']"
            link
            type="primary"
            @click="handleAck(scope.row.id)"
          >
            确认
          </el-button>
          <el-button
            v-if="scope.row.status !== 2"
            v-hasPermi="['energy:alarm:close']"
            link
            type="danger"
            @click="handleClose(scope.row.id)"
          >
            关闭
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

  <el-dialog v-model="detailVisible" title="告警详情" width="720px">
    <el-descriptions v-if="detailData" :column="2" border>
      <el-descriptions-item label="告警编号">{{ detailData.alarmNo }}</el-descriptions-item>
      <el-descriptions-item label="设备">{{ detailData.deviceName || detailData.deviceId }}</el-descriptions-item>
      <el-descriptions-item label="告警代码">{{ detailData.code }}</el-descriptions-item>
      <el-descriptions-item label="告警等级">
        <dict-tag :type="DICT_TYPE.ENERGY_ALARM_LEVEL" :value="detailData.level" />
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <dict-tag :type="DICT_TYPE.ENERGY_ALARM_STATUS" :value="detailData.status" />
      </el-descriptions-item>
      <el-descriptions-item label="发生时间">{{ formatTime(detailData.occurTime) }}</el-descriptions-item>
      <el-descriptions-item label="确认人">{{ detailData.ackUserId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="确认时间">{{ formatTime(detailData.ackTime) }}</el-descriptions-item>
      <el-descriptions-item label="关闭时间">{{ formatTime(detailData.closeTime) }}</el-descriptions-item>
      <el-descriptions-item label="告警标题" :span="2">{{ detailData.title }}</el-descriptions-item>
      <el-descriptions-item label="告警内容" :span="2">{{ detailData.content || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script lang="ts" setup>
import { dateFormatter, formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyAlarmApi, EnergyAlarmVO } from '@/api/energy/alarm'

defineOptions({ name: 'EnergyAlarm' })

const message = useMessage()

const loading = ref(true)
const list = ref<EnergyAlarmVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  deviceId: undefined,
  code: undefined,
  title: undefined,
  level: undefined,
  status: undefined,
  occurTime: undefined
})
const queryFormRef = ref()
const detailVisible = ref(false)
const detailData = ref<EnergyAlarmVO>()

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyAlarmApi.getAlarmPage(queryParams)
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

const openDetail = async (id: number) => {
  detailData.value = await EnergyAlarmApi.getAlarm(id)
  detailVisible.value = true
}

const handleAck = async (id: number) => {
  const { value: remark } = await ElMessageBox.prompt('请输入确认备注', '确认告警', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  await EnergyAlarmApi.ackAlarm({ id, remark })
  message.success('确认成功')
  await getList()
}

const handleClose = async (id: number) => {
  const { value: remark } = await ElMessageBox.prompt('请输入关闭备注', '关闭告警', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  await EnergyAlarmApi.closeAlarm({ id, remark })
  message.success('关闭成功')
  await getList()
}

const formatTime = (time?: Date) => {
  return time ? formatDate(time) : '-'
}

onMounted(() => {
  getList()
})
</script>
