<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="92px"
    >
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" clearable filterable placeholder="请选择客户" class="!w-200px">
          <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="项目" prop="projectId">
        <el-select v-model="queryParams.projectId" clearable filterable placeholder="请选择项目" class="!w-200px">
          <el-option
            v-for="item in projectList"
            :key="item.id"
            :label="`${item.name || '-'} / ${item.code || '-'}`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备" prop="deviceId">
        <el-select v-model="queryParams.deviceId" clearable filterable placeholder="请选择设备" class="!w-220px">
          <el-option
            v-for="item in deviceList"
            :key="item.id"
            :label="`${item.deviceName || '-'} / ${item.deviceNo || '-'}`"
            :value="item.id"
          />
        </el-select>
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
      <el-form-item label="生效开始" prop="effectiveStart">
        <el-date-picker
          v-model="queryParams.effectiveStart"
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
        <el-button
          v-hasPermi="['energy:pricing-rule:create']"
          plain
          type="primary"
          @click="openPricingRuleForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-form
      ref="matchFormRef"
      :inline="true"
      :model="matchParams"
      :rules="matchRules"
      class="-mb-15px"
      label-width="92px"
    >
      <el-form-item label="试算设备" prop="deviceId">
        <el-select v-model="matchParams.deviceId" filterable placeholder="请选择设备" class="!w-260px">
          <el-option
            v-for="item in deviceList"
            :key="item.id"
            :label="`${item.deviceName || '-'} / ${item.deviceNo || '-'}`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计费时间" prop="billingTime">
        <el-date-picker
          v-model="matchParams.billingTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="默认当前时间"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item>
        <el-button :loading="matchLoading" type="primary" @click="handleMatchPricingRule">
          <Icon class="mr-5px" icon="ep:price-tag" />匹配规则
        </el-button>
        <el-button @click="resetMatch"><Icon class="mr-5px" icon="ep:refresh" />清空</el-button>
      </el-form-item>
    </el-form>

    <el-descriptions
      v-if="matchedRule"
      :column="4"
      border
      class="mt-16px"
      size="small"
    >
      <el-descriptions-item label="命中范围">{{ getScopeTypeText(matchedRule) }}</el-descriptions-item>
      <el-descriptions-item label="范围名称">{{ getScopeName(matchedRule) }}</el-descriptions-item>
      <el-descriptions-item label="时间单价">{{ matchedRule.timeRate }}</el-descriptions-item>
      <el-descriptions-item label="电量单价">{{ matchedRule.energyRate }}</el-descriptions-item>
      <el-descriptions-item label="固定费用合计">{{ formatCurrency(getFixedFeeTotal(matchedRule)) }}</el-descriptions-item>
      <el-descriptions-item label="场地费">{{ formatCurrency(matchedRule.siteFee) }}</el-descriptions-item>
      <el-descriptions-item label="运维费">{{ formatCurrency(matchedRule.maintenanceFee) }}</el-descriptions-item>
      <el-descriptions-item label="通信费">{{ formatCurrency(matchedRule.communicationFee) }}</el-descriptions-item>
      <el-descriptions-item label="平台服务费">{{ formatCurrency(matchedRule.platformServiceFee) }}</el-descriptions-item>
      <el-descriptions-item label="电池折旧成本">{{ formatCurrency(matchedRule.batteryDepreciationCost) }}</el-descriptions-item>
      <el-descriptions-item label="其他固定费用">{{ formatCurrency(matchedRule.otherFixedFee) }}</el-descriptions-item>
      <el-descriptions-item label="生效开始">
        {{ formatDateText(matchedRule.effectiveStart) }}
      </el-descriptions-item>
      <el-descriptions-item label="生效结束">
        {{ formatDateText(matchedRule.effectiveEnd) }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="matchedRule.status" />
      </el-descriptions-item>
      <el-descriptions-item label="规则编号">{{ matchedRule.id }}</el-descriptions-item>
    </el-descriptions>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="计费范围" min-width="180">
        <template #default="{ row }">
          <el-tag effect="light" class="mr-6px">{{ getScopeTypeText(row) }}</el-tag>
          <span>{{ getScopeName(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="right" label="时间单价" width="120" prop="timeRate" />
      <el-table-column align="right" label="电量单价" width="120" prop="energyRate" />
      <el-table-column align="right" label="固定费用合计" width="130">
        <template #default="{ row }">{{ formatCurrency(getFixedFeeTotal(row)) }}</template>
      </el-table-column>
      <el-table-column align="right" label="场地费" width="110">
        <template #default="{ row }">{{ formatCurrency(row.siteFee) }}</template>
      </el-table-column>
      <el-table-column align="right" label="运维费" width="110">
        <template #default="{ row }">{{ formatCurrency(row.maintenanceFee) }}</template>
      </el-table-column>
      <el-table-column align="right" label="通信费" width="110">
        <template #default="{ row }">{{ formatCurrency(row.communicationFee) }}</template>
      </el-table-column>
      <el-table-column align="right" label="平台服务费" width="120">
        <template #default="{ row }">{{ formatCurrency(row.platformServiceFee) }}</template>
      </el-table-column>
      <el-table-column align="right" label="电池折旧成本" width="130">
        <template #default="{ row }">{{ formatCurrency(row.batteryDepreciationCost) }}</template>
      </el-table-column>
      <el-table-column align="right" label="其他固定费用" width="130">
        <template #default="{ row }">{{ formatCurrency(row.otherFixedFee) }}</template>
      </el-table-column>
      <el-table-column align="center" label="生效开始" width="180" prop="effectiveStart">
        <template #default="{ row }">{{ formatDateText(row.effectiveStart) }}</template>
      </el-table-column>
      <el-table-column align="center" label="生效结束" width="180" prop="effectiveEnd">
        <template #default="{ row }">{{ formatDateText(row.effectiveEnd) }}</template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="100" prop="status">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" fixed="right" label="操作" width="160">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['energy:pricing-rule:update']"
            link
            type="primary"
            @click="openPricingRuleForm('update', row.id)"
          >
            修改
          </el-button>
          <el-button
            v-hasPermi="['energy:pricing-rule:delete']"
            link
            type="danger"
            @click="handleDelete(row.id)"
          >
            删除
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

  <PricingRuleForm ref="formRef" @success="handlePricingRuleSuccess" />
</template>

<script lang="ts" setup>
import { EnergyCustomerApi } from '@/api/energy/customer'
import type { EnergyCustomerVO } from '@/api/energy/customer'
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyPricingRuleApi } from '@/api/energy/pricingRule'
import type { EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import { EnergyProjectApi } from '@/api/energy/project'
import type { EnergyProjectVO } from '@/api/energy/project'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { formatNullableDate } from '@/utils/formatTime'
import PricingRuleForm from './PricingRuleForm.vue'

defineOptions({ name: 'EnergyPricingRule' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const list = ref<EnergyPricingRuleVO[]>([])
const total = ref(0)
const customerList = ref<EnergyCustomerVO[]>([])
const projectList = ref<EnergyProjectVO[]>([])
const deviceList = ref<EnergyDeviceVO[]>([])
const matchLoading = ref(false)
const matchedRule = ref<EnergyPricingRuleVO>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  customerId: undefined,
  projectId: undefined,
  deviceId: undefined,
  status: undefined,
  effectiveStart: undefined
})
const queryFormRef = ref()
const matchFormRef = ref()
const matchParams = reactive<{
  deviceId?: number
  billingTime?: string
}>({
  deviceId: undefined,
  billingTime: undefined
})
const matchRules = reactive({
  deviceId: [{ required: true, message: '试算设备不能为空', trigger: 'change' }]
})

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyPricingRuleApi.getPricingRulePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const [customers, projects, devices] = await Promise.all([
    EnergyCustomerApi.getCustomerSimpleList(),
    EnergyProjectApi.getProjectSimpleList(),
    EnergyDeviceApi.getDeviceSimpleList()
  ])
  customerList.value = customers
  projectList.value = projects
  deviceList.value = devices
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openPricingRuleForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handlePricingRuleSuccess = async () => {
  await getList()
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EnergyPricingRuleApi.deletePricingRule(id)
    message.success(t('common.delSuccess'))
    await handlePricingRuleSuccess()
  } catch {}
}

const handleMatchPricingRule = async () => {
  await matchFormRef.value.validate()
  matchLoading.value = true
  try {
    const data = await EnergyPricingRuleApi.matchPricingRule(
      matchParams.deviceId as number,
      matchParams.billingTime
    )
    matchedRule.value = data
    if (!data) {
      message.warning('当前设备没有匹配到生效计费规则')
    }
  } finally {
    matchLoading.value = false
  }
}

const resetMatch = () => {
  matchFormRef.value?.resetFields()
  matchedRule.value = undefined
}

const getScopeTypeText = (row: EnergyPricingRuleVO) => {
  if (row.deviceId) return '设备'
  if (row.projectId) return '项目'
  return '客户'
}

const getScopeName = (row: EnergyPricingRuleVO) => {
  if (row.deviceId) return `${row.deviceName || '-'} / ${row.deviceNo || '-'}`
  if (row.projectId) return row.projectName || '-'
  return row.customerName || '-'
}

const getFixedFeeTotal = (row: EnergyPricingRuleVO) => {
  return [
    row.siteFee,
    row.maintenanceFee,
    row.communicationFee,
    row.platformServiceFee,
    row.batteryDepreciationCost,
    row.otherFixedFee
  ].reduce((sum, value) => sum + Number(value || 0), 0)
}

const formatCurrency = (value?: number) => {
  const amount = Number(value || 0)
  return amount > 0 ? `¥${amount.toFixed(2)}` : '¥0.00'
}

const formatDateText = (value?: string | Date) => {
  return formatNullableDate(value as Date)
}

onMounted(async () => {
  await loadOptions()
  await getList()
})
</script>
