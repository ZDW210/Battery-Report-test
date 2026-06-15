<template>
  <div class="energy-home">
    <div class="energy-home__header">
      <div>
        <div class="energy-home__eyebrow">运营工作台</div>
        <h1>移动储能运营管理平台</h1>
        <p>聚焦设备状态、客户项目、告警处理和小程序用户授权。</p>
      </div>
      <div class="energy-home__actions">
        <el-button type="primary" @click="go('/energy/dashboard')">
          <Icon class="mr-5px" icon="ep:data-analysis" />
          运营面板
        </el-button>
        <el-button @click="loadDashboard">
          <Icon class="mr-5px" icon="ep:refresh" />
          刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="12" class="mb-12px">
      <el-col v-for="item in statCards" :key="item.label" :lg="6" :md="12" :sm="12" :xs="24">
        <el-card shadow="never" class="energy-home__stat">
          <div class="energy-home__stat-main">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
          <Icon :icon="item.icon" :style="{ color: item.color }" :size="30" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :lg="16" :md="24" :xs="24">
        <el-card shadow="never" class="mb-12px">
          <template #header>
            <div class="energy-home__card-header">
              <span>设备运行概览</span>
              <el-button link type="primary" @click="go('/energy/device')">查看全部</el-button>
            </div>
          </template>

          <el-skeleton :loading="loading" animated>
            <el-row :gutter="12" class="mb-12px">
              <el-col :span="8">
                <div class="energy-home__metric">
                  <span>在线率</span>
                  <strong>{{ onlineRate }}%</strong>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="energy-home__metric">
                  <span>当前功率</span>
                  <strong>{{ totalPower }} kW</strong>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="energy-home__metric">
                  <span>最新采集</span>
                  <strong>{{ latestReadingText }}</strong>
                </div>
              </el-col>
            </el-row>

            <el-table :data="latestDevices" :show-overflow-tooltip="true" :stripe="true">
              <el-table-column label="设备名称" min-width="150" prop="deviceName" />
              <el-table-column label="设备编码" min-width="130" prop="deviceNo" />
              <el-table-column label="客户" min-width="120" prop="customerName">
                <template #default="{ row }">{{ row.customerName || '-' }}</template>
              </el-table-column>
              <el-table-column label="项目" min-width="120" prop="projectName">
                <template #default="{ row }">{{ row.projectName || '-' }}</template>
              </el-table-column>
              <el-table-column align="center" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getDeviceStatusType(row.status)" effect="light">
                    {{ getDeviceStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column align="right" label="功率(kW)" width="100">
                <template #default="{ row }">{{ valueOrDash(row.lastPower) }}</template>
              </el-table-column>
              <el-table-column label="更新时间" width="170">
                <template #default="{ row }">{{ formatDateText(row.lastReadingTime) }}</template>
              </el-table-column>
            </el-table>
          </el-skeleton>
        </el-card>
      </el-col>

      <el-col :lg="8" :md="24" :xs="24">
        <el-card shadow="never" class="mb-12px">
          <template #header>
            <div class="energy-home__card-header">
              <span>待处理告警</span>
              <el-button link type="primary" @click="go('/energy/alarm')">进入处理</el-button>
            </div>
          </template>

          <el-skeleton :loading="loading" animated>
            <el-empty v-if="latestAlarms.length === 0" description="暂无待处理告警" />
            <div v-for="alarm in latestAlarms" v-else :key="alarm.id" class="energy-home__alarm">
              <div>
                <strong>{{ alarm.title || alarm.code || '未命名告警' }}</strong>
                <span>{{ alarm.deviceName || '-' }}</span>
              </div>
              <el-tag :type="getAlarmTagType(alarm.level)" effect="light">
                {{ getAlarmLevelText(alarm.level) }}
              </el-tag>
            </div>
          </el-skeleton>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div class="energy-home__card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div class="energy-home__quick">
            <el-button v-for="item in quickLinks" :key="item.path" @click="go(item.path)">
              <Icon class="mr-5px" :icon="item.icon" />
              {{ item.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { EnergyAlarmApi, EnergyAlarmVO } from '@/api/energy/alarm'
import { EnergyCustomerApi } from '@/api/energy/customer'
import { EnergyDeviceApi, EnergyDeviceVO } from '@/api/energy/device'
import { EnergyProjectApi } from '@/api/energy/project'
import { formatNullableDate } from '@/utils/formatTime'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

defineOptions({ name: 'Index' })

const router = useRouter()
const message = useMessage()

const loading = ref(true)
const deviceTotal = ref(0)
const onlineTotal = ref(0)
const customerTotal = ref(0)
const projectTotal = ref(0)
const activeAlarmTotal = ref(0)
const latestDevices = ref<EnergyDeviceVO[]>([])
const latestAlarms = ref<EnergyAlarmVO[]>([])

const quickLinks = [
  { label: '运营面板', path: '/energy/dashboard', icon: 'ep:data-analysis' },
  { label: '实时监控', path: '/energy/telemetry', icon: 'ep:odometer' },
  { label: 'EIOT 日志', path: '/energy/eiot-log', icon: 'ep:connection' },
  { label: '客户管理', path: '/energy/customer', icon: 'ep:user' },
  { label: '项目站点', path: '/energy/project', icon: 'ep:office-building' },
  { label: '设备台账', path: '/energy/device', icon: 'ep:monitor' },
  { label: '告警中心', path: '/energy/alarm', icon: 'ep:warning' },
  { label: '用户授权', path: '/energy/user-scope', icon: 'ep:key' },
  { label: '后台用户', path: '/system/user', icon: 'ep:avatar' }
]

const statCards = computed(() => [
  { label: '设备总数', value: deviceTotal.value, icon: 'ep:monitor', color: '#2563eb' },
  { label: '客户数量', value: customerTotal.value, icon: 'ep:user', color: '#0f766e' },
  { label: '项目站点', value: projectTotal.value, icon: 'ep:office-building', color: '#7c3aed' },
  { label: '待处理告警', value: activeAlarmTotal.value, icon: 'ep:warning', color: '#dc2626' }
])

const onlineRate = computed(() => {
  if (!latestDevices.value.length) return 0
  return Number(((onlineTotal.value / latestDevices.value.length) * 100).toFixed(1))
})

const totalPower = computed(() => {
  const value = latestDevices.value.reduce((sum, item) => sum + Number(item.lastPower || 0), 0)
  return Number(value.toFixed(1))
})

const latestReadingText = computed(() => {
  const latest = [...latestDevices.value]
    .filter((item) => item.lastReadingTime)
    .sort((a, b) => dayjs(b.lastReadingTime).valueOf() - dayjs(a.lastReadingTime).valueOf())[0]
  return latest ? formatDateText(latest.lastReadingTime) : '-'
})

const loadDashboard = async () => {
  loading.value = true
  try {
    const [devicePage, alarmPage, customerPage, projectPage] = await Promise.all([
      EnergyDeviceApi.getDevicePage({ pageNo: 1, pageSize: 8 }),
      EnergyAlarmApi.getAlarmPage({ pageNo: 1, pageSize: 6, status: 0 }),
      EnergyCustomerApi.getCustomerPage({ pageNo: 1, pageSize: 1 }),
      EnergyProjectApi.getProjectPage({ pageNo: 1, pageSize: 1 })
    ])

    latestDevices.value = devicePage?.list || []
    latestAlarms.value = alarmPage?.list || []
    deviceTotal.value = devicePage?.total || 0
    activeAlarmTotal.value = alarmPage?.total || 0
    customerTotal.value = customerPage?.total || 0
    projectTotal.value = projectPage?.total || 0
    onlineTotal.value = latestDevices.value.filter((item) => item.status === 0).length
  } catch (error) {
    message.error('运营首页数据加载失败，请检查后端服务和登录状态')
  } finally {
    loading.value = false
  }
}

const go = (path: string) => {
  router.push(path)
}

const valueOrDash = (value?: number) => {
  return value === undefined || value === null ? '-' : value
}

const formatDateText = (value?: Date) => {
  return formatNullableDate(value)
}

const getDeviceStatusText = (status?: number) => {
  const statusMap: Record<number, string> = {
    0: '在线',
    1: '离线',
    2: '故障',
    3: '维护'
  }
  return status === undefined || status === null ? '未知' : statusMap[status] || `状态${status}`
}

const getDeviceStatusType = (status?: number) => {
  if (status === 0) return 'success'
  if (status === 2) return 'danger'
  if (status === 3) return 'warning'
  return 'info'
}

const getAlarmLevelText = (level?: number) => {
  const levelMap: Record<number, string> = {
    1: '提示',
    2: '一般',
    3: '严重',
    4: '紧急'
  }
  return level ? levelMap[level] || `等级 ${level}` : '未知'
}

const getAlarmTagType = (level?: number) => {
  if (level === 4 || level === 3) {
    return 'danger'
  }
  if (level === 2) {
    return 'warning'
  }
  return 'info'
}

onMounted(() => {
  loadDashboard()
})
</script>

<style lang="scss" scoped>
.energy-home {
  &__header {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 12px;
    padding: 18px 20px;
    background: #ffffff;
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;

    h1 {
      margin: 6px 0;
      color: var(--el-text-color-primary);
      font-size: 24px;
      font-weight: 700;
      line-height: 32px;
    }

    p {
      margin: 0;
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }
  }

  &__eyebrow {
    color: var(--el-color-primary);
    font-size: 13px;
    font-weight: 600;
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 8px;
  }

  &__stat {
    margin-bottom: 12px;

    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      justify-content: space-between;
      min-height: 86px;
    }
  }

  &__stat-main,
  &__metric {
    span {
      display: block;
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }

    strong {
      display: block;
      margin-top: 8px;
      color: var(--el-text-color-primary);
      font-size: 24px;
      line-height: 30px;
    }
  }

  &__metric {
    padding: 12px;
    background: var(--el-fill-color-lighter);
    border-radius: 6px;
  }

  &__card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 600;
  }

  &__alarm {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: 0;
    }

    strong,
    span {
      display: block;
    }

    strong {
      color: var(--el-text-color-primary);
      font-size: 14px;
    }

    span {
      margin-top: 4px;
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }
  }

  &__quick {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;

    .el-button {
      width: 100%;
      margin-left: 0;
      justify-content: flex-start;
    }
  }
}

@media (max-width: 768px) {
  .energy-home {
    &__header {
      align-items: flex-start;
      flex-direction: column;
    }

    &__actions {
      justify-content: flex-start;
      width: 100%;
    }

    &__quick {
      grid-template-columns: 1fr;
    }
  }
}
</style>
