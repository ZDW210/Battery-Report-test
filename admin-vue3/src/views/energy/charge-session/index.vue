<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="82px">
      <el-form-item label="会话编号" prop="sessionNo">
        <el-input
          v-model="queryParams.sessionNo"
          clearable
          placeholder="请输入会话编号"
          class="!w-220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备" prop="deviceId">
        <el-select v-model="queryParams.deviceId" clearable filterable placeholder="请选择设备" class="!w-240px">
          <el-option
            v-for="item in deviceList"
            :key="item.id"
            :label="getDeviceLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" clearable filterable placeholder="请选择客户" class="!w-200px">
          <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="sessionType">
        <el-select v-model="queryParams.sessionType" clearable placeholder="请选择类型" class="!w-140px">
          <el-option label="充电" :value="0" />
          <el-option label="放电" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" class="!w-150px">
          <el-option label="进行中" :value="0" />
          <el-option label="已结束" :value="1" />
          <el-option label="异常" :value="2" />
          <el-option label="已结算" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker
          v-model="queryParams.startTime"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="!w-360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-form
      ref="startFormRef"
      :inline="true"
      :model="startForm"
      :rules="startRules"
      class="-mb-15px"
      label-width="82px"
    >
      <el-form-item label="启动设备" prop="deviceId">
        <el-select v-model="startForm.deviceId" filterable placeholder="请选择设备" class="!w-260px">
          <el-option
            v-for="item in deviceList"
            :key="item.id"
            :label="getDeviceLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型" prop="sessionType">
        <el-radio-group v-model="startForm.sessionType">
          <el-radio-button :label="0">充电</el-radio-button>
          <el-radio-button :label="1">放电</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="计费规则" prop="pricingRuleId">
        <el-select v-model="startForm.pricingRuleId" clearable filterable placeholder="自动匹配" class="!w-220px">
          <el-option
            v-for="item in availablePricingRuleList"
            :key="item.id"
            :label="getPricingRuleLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          v-hasPermi="['energy:charge-session:start']"
          :disabled="startDeviceMissingCustomer"
          :loading="actionLoading"
          type="primary"
          @click="handleStart"
        >
          <Icon class="mr-5px" icon="ep:video-play" />开始任务
        </el-button>
      </el-form-item>
      <el-form-item v-if="startDeviceMissingCustomer">
        <el-alert
          :closable="false"
          title="当前设备未绑定客户，请先在设备台账绑定客户后再开始任务。"
          type="warning"
        />
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="会话编号" min-width="170" prop="sessionNo" />
      <el-table-column align="center" label="设备" min-width="180">
        <template #default="{ row }">{{ row.deviceName || row.deviceNo || row.deviceId }}</template>
      </el-table-column>
      <el-table-column align="center" label="客户" min-width="150">
        <template #default="{ row }">{{ row.customerName || row.customerId || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="row.sessionType === 0 ? 'success' : 'warning'" effect="light">
            {{ getSessionTypeText(row.sessionType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" effect="light">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="开始时间" width="170">
        <template #default="{ row }">{{ formatDateText(row.startTime) }}</template>
      </el-table-column>
      <el-table-column align="center" label="结束时间" width="170">
        <template #default="{ row }">{{ formatDateText(row.endTime) }}</template>
      </el-table-column>
      <el-table-column align="right" label="开始读数" width="110" prop="startEnergy" />
      <el-table-column align="right" label="结束读数" width="110" prop="endEnergy" />
      <el-table-column align="right" label="电量(kWh)" width="110" prop="totalEnergy" />
      <el-table-column align="right" label="时长(分钟)" width="110" prop="durationMinutes" />
      <el-table-column align="right" label="总费用" width="110" prop="totalFee" />
      <el-table-column align="center" fixed="right" label="操作" width="190">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 0"
            v-hasPermi="['energy:charge-session:stop']"
            link
            type="danger"
            @click="openStop(row)"
          >
            结束
          </el-button>
          <el-button
            v-if="row.status === 1 || row.status === 2"
            v-hasPermi="['energy:charge-session:settle']"
            link
            type="primary"
            @click="handleSettle(row)"
          >
            结算
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

  <el-dialog v-model="stopVisible" title="结束充放电任务" width="460px">
    <el-form ref="stopFormRef" :model="stopForm" label-width="110px">
      <el-form-item label="会话编号">{{ stopTarget?.sessionNo }}</el-form-item>
      <el-form-item label="结束电量读数">
        <el-input-number v-model="stopForm.endEnergy" :precision="3" :step="1" class="!w-220px" />
      </el-form-item>
      <el-alert
        :closable="false"
        show-icon
        title="不填写结束读数时，系统会使用该设备最新 EPI 读数。"
        type="info"
      />
    </el-form>
    <template #footer>
      <el-button @click="stopVisible = false">取消</el-button>
      <el-button :loading="actionLoading" type="primary" @click="handleStop">确认结束</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" title="充放电会话详情" width="760px">
    <el-descriptions v-if="detailData" :column="2" border>
      <el-descriptions-item label="会话编号">{{ detailData.sessionNo }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ getStatusText(detailData.status) }}</el-descriptions-item>
      <el-descriptions-item label="设备">{{ detailData.deviceName || detailData.deviceNo }}</el-descriptions-item>
      <el-descriptions-item label="客户">{{ detailData.customerName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ getSessionTypeText(detailData.sessionType) }}</el-descriptions-item>
      <el-descriptions-item label="计费规则">{{ detailData.pricingRuleId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="开始时间">{{ formatDateText(detailData.startTime) }}</el-descriptions-item>
      <el-descriptions-item label="结束时间">{{ formatDateText(detailData.endTime) }}</el-descriptions-item>
      <el-descriptions-item label="开始读数">{{ formatMoney(detailData.startEnergy, 3) }}</el-descriptions-item>
      <el-descriptions-item label="结束读数">{{ formatMoney(detailData.endEnergy, 3) }}</el-descriptions-item>
      <el-descriptions-item label="总电量">{{ formatMoney(detailData.totalEnergy, 3) }}</el-descriptions-item>
      <el-descriptions-item label="时长分钟">{{ detailData.durationMinutes ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="电量费用">{{ formatMoney(detailData.energyFee, 2) }}</el-descriptions-item>
      <el-descriptions-item label="时间费用">{{ formatMoney(detailData.timeFee, 2) }}</el-descriptions-item>
      <el-descriptions-item label="总费用">{{ formatMoney(detailData.totalFee, 2) }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatDateText(detailData.createTime) }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script lang="ts" setup>
import { EnergyChargeSessionApi, EnergyChargeSessionVO } from '@/api/energy/chargeSession'
import { EnergyCustomerApi } from '@/api/energy/customer'
import type { EnergyCustomerVO } from '@/api/energy/customer'
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyPricingRuleApi, EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import { formatNullableDate } from '@/utils/formatTime'

defineOptions({ name: 'EnergyChargeSession' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const actionLoading = ref(false)
const list = ref<EnergyChargeSessionVO[]>([])
const total = ref(0)
const deviceList = ref<EnergyDeviceVO[]>([])
const customerList = ref<EnergyCustomerVO[]>([])
const pricingRuleList = ref<EnergyPricingRuleVO[]>([])
const detailVisible = ref(false)
const detailData = ref<EnergyChargeSessionVO>()
const stopVisible = ref(false)
const stopTarget = ref<EnergyChargeSessionVO>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  sessionNo: undefined,
  deviceId: undefined,
  customerId: undefined,
  sessionType: undefined,
  status: undefined,
  startTime: undefined
})
const queryFormRef = ref()
const startFormRef = ref()
const startForm = reactive<{
  deviceId?: number
  sessionType: number
  pricingRuleId?: number
}>({
  deviceId: undefined,
  sessionType: 0,
  pricingRuleId: undefined
})
const startRules = reactive({
  deviceId: [{ required: true, message: '启动设备不能为空', trigger: 'change' }],
  sessionType: [{ required: true, message: '任务类型不能为空', trigger: 'change' }]
})
const stopForm = reactive<{ endEnergy?: number }>({ endEnergy: undefined })
const isSameId = (left?: number | string, right?: number | string) => {
  return left !== undefined && left !== null && right !== undefined && right !== null && String(left) === String(right)
}
const selectedStartDevice = computed(() => deviceList.value.find((item) => isSameId(item.id, startForm.deviceId)))
const startDeviceMissingCustomer = computed(
  () => !!selectedStartDevice.value && !selectedStartDevice.value.customerId
)
const availablePricingRuleList = computed(() => {
  const device = selectedStartDevice.value
  if (!device) {
    return pricingRuleList.value
  }
  return pricingRuleList.value.filter((rule) => {
    return (
      (!!rule.deviceId && isSameId(rule.deviceId, device.id)) ||
      (!!rule.projectId && !!device.projectId && isSameId(rule.projectId, device.projectId)) ||
      (!!rule.customerId && !!device.customerId && isSameId(rule.customerId, device.customerId))
    )
  })
})

watch(
  () => startForm.deviceId,
  () => {
    if (
      startForm.pricingRuleId &&
      !availablePricingRuleList.value.some((rule) => isSameId(rule.id, startForm.pricingRuleId))
    ) {
      startForm.pricingRuleId = undefined
    }
  }
)

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyChargeSessionApi.getChargeSessionPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const [devices, customers, pricingRules] = await Promise.all([
    EnergyDeviceApi.getDeviceSimpleList(),
    EnergyCustomerApi.getCustomerSimpleList(),
    EnergyPricingRuleApi.getPricingRulePage({ pageNo: 1, pageSize: 100, status: 0 })
  ])
  deviceList.value = devices || []
  customerList.value = customers || []
  pricingRuleList.value = pricingRules?.list || []
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const handleStart = async () => {
  await startFormRef.value.validate()
  actionLoading.value = true
  try {
    await EnergyChargeSessionApi.startChargeSession(startForm)
    message.success('充放电任务已开始，已下发合闸指令')
    startForm.deviceId = undefined
    startForm.pricingRuleId = undefined
    await getList()
  } finally {
    actionLoading.value = false
  }
}

const openStop = (row: EnergyChargeSessionVO) => {
  stopTarget.value = row
  stopForm.endEnergy = undefined
  stopVisible.value = true
}

const handleStop = async () => {
  if (!stopTarget.value?.id) {
    return
  }
  actionLoading.value = true
  try {
    await EnergyChargeSessionApi.stopChargeSession({
      sessionId: stopTarget.value.id,
      endEnergy: stopForm.endEnergy
    })
    message.success('充放电任务已结束，已下发开闸指令')
    stopVisible.value = false
    await getList()
  } finally {
    actionLoading.value = false
  }
}

const handleSettle = async (row: EnergyChargeSessionVO) => {
  await message.confirm('确认结算该充放电会话吗？结算后不可再修改。')
  await EnergyChargeSessionApi.settleChargeSession({ sessionId: row.id })
  message.success(t('common.success'))
  await getList()
}

const openDetail = (row: EnergyChargeSessionVO) => {
  detailData.value = row
  detailVisible.value = true
}

const getDeviceLabel = (device: EnergyDeviceVO) => {
  return `${device.deviceName || '-'} / ${device.deviceNo || device.meterNo || '-'}`
}

const getPricingRuleLabel = (rule: EnergyPricingRuleVO) => {
  return `#${rule.id} 电量 ${rule.energyRate ?? 0} / 时间 ${rule.timeRate ?? 0}`
}

const getSessionTypeText = (value?: number) => {
  return value === 1 ? '放电' : '充电'
}

const getStatusText = (value?: number) => {
  const statusMap: Record<number, string> = {
    0: '进行中',
    1: '已结束',
    2: '异常',
    3: '已结算'
  }
  return value === undefined || value === null ? '-' : statusMap[value] || `状态${value}`
}

const getStatusTagType = (value?: number) => {
  if (value === 0) {
    return 'success'
  }
  if (value === 1) {
    return 'warning'
  }
  if (value === 2) {
    return 'danger'
  }
  return 'info'
}

const formatDateText = (value?: string | Date) => formatNullableDate(value, 'YYYY-MM-DD HH:mm:ss', '-')

const formatMoney = (value?: number, precision = 2) => {
  return value === undefined || value === null ? '-' : Number(value).toFixed(precision)
}

onMounted(async () => {
  await loadOptions()
  await getList()
})
</script>
