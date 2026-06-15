<!-- 移动储能设备台账 -->
<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="84px"
    >
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          class="!w-220px"
          clearable
          placeholder="请输入设备名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备编码" prop="deviceNo">
        <el-input
          v-model="queryParams.deviceNo"
          class="!w-220px"
          clearable
          placeholder="请输入设备编码"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-select
          v-model="queryParams.deviceType"
          class="!w-180px"
          clearable
          placeholder="请选择设备类型"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.ENERGY_DEVICE_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备状态" prop="status">
        <el-select
          v-model="queryParams.status"
          class="!w-180px"
          clearable
          placeholder="请选择设备状态"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.ENERGY_DEVICE_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="网关序列号" prop="gatewaySn">
        <el-input
          v-model="queryParams.gatewaySn"
          class="!w-220px"
          clearable
          placeholder="请输入网关序列号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="电表序列号" prop="meterSn">
        <el-input
          v-model="queryParams.meterSn"
          class="!w-220px"
          clearable
          placeholder="请输入电表序列号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仪表编号" prop="meterNo">
        <el-input
          v-model="queryParams.meterNo"
          class="!w-240px"
          clearable
          placeholder="请输入仪表编号"
          @keyup.enter="handleQuery"
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
        <el-button
          v-hasPermi="['energy:device:create']"
          plain
          type="primary"
          @click="openDeviceForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="设备名称" min-width="160" prop="deviceName" />
      <el-table-column align="center" label="设备编码" min-width="140" prop="deviceNo" />
      <el-table-column align="center" label="台账状态" width="110">
        <template #default="scope">
          <el-tag :type="isLedgerComplete(scope.row) ? 'success' : 'warning'" effect="light">
            {{ isLedgerComplete(scope.row) ? '完整' : '待补全' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="客户" min-width="140" prop="customerName">
        <template #default="scope">{{ scope.row.customerName || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="项目场站" min-width="140" prop="projectName">
        <template #default="scope">{{ scope.row.projectName || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="设备类型" min-width="100" prop="deviceType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.ENERGY_DEVICE_TYPE" :value="scope.row.deviceType" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="设备状态" min-width="100" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.ENERGY_DEVICE_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="功率(kW)" prop="lastPower" width="110">
        <template #default="scope">{{ formatNumber(scope.row.lastPower) }}</template>
      </el-table-column>
      <el-table-column align="center" label="网关序列号" min-width="150" prop="gatewaySn" />
      <el-table-column align="center" label="电表序列号" min-width="150" prop="meterSn" />
      <el-table-column align="center" label="仪表编号" min-width="150" prop="meterNo" />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="最新采集时间"
        prop="lastReadingTime"
        width="180"
      />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="创建时间"
        prop="createTime"
        width="180"
      />
      <el-table-column align="center" fixed="right" label="操作" width="160">
        <template #default="scope">
          <el-button
            v-hasPermi="['energy:device:update']"
            link
            type="primary"
            @click="openDeviceForm('update', scope.row.id)"
          >
            修改
          </el-button>
          <el-button
            v-hasPermi="['energy:device:delete']"
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
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

  <DeviceForm ref="formRef" @success="handleDeviceSuccess" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyDeviceApi, EnergyDeviceVO } from '@/api/energy/device'
import DeviceForm from './DeviceForm.vue'

/** 移动储能设备台账 */
defineOptions({ name: 'EnergyDevice' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<EnergyDeviceVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  deviceNo: undefined,
  deviceName: undefined,
  deviceType: undefined,
  gatewaySn: undefined,
  meterSn: undefined,
  meterNo: undefined,
  status: undefined
})
const queryFormRef = ref()

/** 查询设备列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyDeviceApi.getDevicePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openDeviceForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 设备保存成功 */
const handleDeviceSuccess = async () => {
  await getList()
}

/** 删除设备 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EnergyDeviceApi.deleteDevice(id)
    message.success(t('common.delSuccess'))
    await handleDeviceSuccess()
  } catch {}
}

const formatNumber = (value?: number) => {
  return value === undefined || value === null ? '-' : value
}

const isLedgerComplete = (row: EnergyDeviceVO) => {
  return !!row.customerId && !!row.projectId && !!row.gatewaySn && !!row.meterSn && !!row.meterNo
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
