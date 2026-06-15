<template>
  <el-alert
    class="mb-12px"
    :closable="false"
    show-icon
    title="车辆绑定仅用于可选车辆资产管理和旧车辆二维码兼容；新的扫码启动请优先使用设备/充电桩/仪表二维码，并在“使用账户”和“用户授权”中开放账户权限。"
    type="info"
  />

  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="84px">
      <el-form-item label="车辆编号" prop="vehicleNo">
        <el-input
          v-model="queryParams.vehicleNo"
          class="!w-220px"
          clearable
          placeholder="请输入车辆编号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="车牌号" prop="plateNo">
        <el-input
          v-model="queryParams.plateNo"
          class="!w-180px"
          clearable
          placeholder="请输入车牌号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="二维码" prop="qrCode">
        <el-input
          v-model="queryParams.qrCode"
          class="!w-220px"
          clearable
          placeholder="请输入二维码内容"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="绑定设备" prop="deviceId">
        <el-select
          v-model="queryParams.deviceId"
          class="!w-240px"
          clearable
          filterable
          placeholder="请选择绑定设备"
        >
          <el-option
            v-for="item in deviceList"
            :key="item.id"
            :label="formatDeviceLabel(item)"
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
          v-hasPermi="['energy:vehicle:create']"
          plain
          type="primary"
          @click="openVehicleForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="车辆编号" min-width="150" prop="vehicleNo" />
      <el-table-column align="center" label="车牌号" min-width="120" prop="plateNo" />
      <el-table-column align="center" label="二维码内容" min-width="180" prop="qrCode" />
      <el-table-column align="center" label="绑定设备" min-width="220">
        <template #default="scope">
          {{ formatBoundDevice(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="仪表编号" min-width="160" prop="meterNo">
        <template #default="scope">{{ scope.row.meterNo || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="客户" min-width="140" prop="customerName">
        <template #default="scope">{{ scope.row.customerName || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="项目" min-width="140" prop="projectName">
        <template #default="scope">{{ scope.row.projectName || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="100" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="备注" min-width="160" prop="remark" />
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="160">
        <template #default="scope">
          <el-button
            v-hasPermi="['energy:vehicle:update']"
            link
            type="primary"
            @click="openVehicleForm('update', scope.row.id)"
          >
            修改
          </el-button>
          <el-button
            v-hasPermi="['energy:vehicle:delete']"
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

  <VehicleForm ref="formRef" @success="handleVehicleSuccess" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { EnergyDeviceApi, EnergyDeviceVO } from '@/api/energy/device'
import { EnergyVehicleApi, EnergyVehicleVO } from '@/api/energy/vehicle'
import VehicleForm from './VehicleForm.vue'

defineOptions({ name: 'EnergyVehicle' })

const message = useMessage()
const { t } = useI18n()
const loading = ref(true)
const list = ref<EnergyVehicleVO[]>([])
const total = ref(0)
const deviceList = ref<EnergyDeviceVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  vehicleNo: undefined,
  plateNo: undefined,
  qrCode: undefined,
  deviceId: undefined,
  status: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyVehicleApi.getVehiclePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const loadDevices = async () => {
  deviceList.value = await EnergyDeviceApi.getDeviceSimpleList()
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
const openVehicleForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleVehicleSuccess = async () => {
  await Promise.all([loadDevices(), getList()])
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EnergyVehicleApi.deleteVehicle(id)
    message.success(t('common.delSuccess'))
    await handleVehicleSuccess()
  } catch {}
}

const formatDeviceLabel = (device: EnergyDeviceVO) => {
  return device.deviceNo ? `${device.deviceName} / ${device.deviceNo}` : device.deviceName || device.id
}

const formatBoundDevice = (row: EnergyVehicleVO) => {
  const name = row.deviceName || row.deviceId
  return row.deviceNo ? `${name} / ${row.deviceNo}` : name || '-'
}

onMounted(async () => {
  await loadDevices()
  await getList()
})
</script>
