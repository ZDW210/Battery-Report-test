<template>
  <div class="energy-telemetry">
    <el-tabs v-model="activeSection" class="telemetry-tabs">
      <el-tab-pane label="用电量报表" name="bill">
        <section class="bill-report-panel">
          <div class="bill-report-panel__header">
            <div>
              <h2>用电量汇总报表</h2>
              <span>默认汇总全部电表，可切换为按场站汇总或查看单个电表；未接入的成本参数会标记为待录入。</span>
            </div>
            <el-button type="primary" :loading="billLoading" @click="loadBillReport">
              <Icon class="mr-5px" icon="ep:refresh" />
              刷新报表
            </el-button>
          </div>

          <el-form :inline="true" :model="billQuery" class="telemetry-form bill-report__filters" label-width="84px">
            <el-form-item label="统计范围">
              <el-radio-group v-model="billQuery.scopeType" @change="handleBillScopeChange">
                <el-radio-button label="all">全部电表</el-radio-button>
                <el-radio-button label="project">按场站</el-radio-button>
                <el-radio-button label="device">单个电表</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="billQuery.scopeType === 'project'" label="场站">
              <el-select
                v-model="billQuery.projectId"
                filterable
                placeholder="请选择场站"
                class="!w-260px"
                @change="loadBillReport"
              >
                <el-option
                  v-for="item in billProjectOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="billQuery.scopeType === 'device'" label="电表">
              <el-select
                v-model="billQuery.deviceId"
                filterable
                placeholder="请选择电表"
                class="!w-280px"
                @change="handleBillDeviceChange"
              >
                <el-option
                  v-for="item in devices"
                  :key="item.id"
                  :label="getDeviceOptionLabel(item)"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="账单月">
              <el-date-picker
                v-model="billQuery.billMonth"
                type="month"
                value-format="YYYY-MM"
                class="!w-180px"
                @change="loadBillReport"
              />
            </el-form-item>
          </el-form>

          <el-empty v-if="selectedBillDevices.length === 0" description="当前范围下暂无电表数据" />
          <div v-else v-loading="billLoading" class="bill-report">
            <div class="bill-report__scope-summary">
              <strong>{{ billScopeTitle }}</strong>
              <span>共 {{ selectedBillDevices.length }} 块电表，账单月份 {{ billQuery.billMonth }}</span>
            </div>
            <div class="bill-report__kpis">
              <div v-for="item in billTopCards" :key="item.label" class="bill-report__kpi">
                <div>
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                  <small>{{ item.hint }}</small>
                </div>
                <Icon :icon="item.icon" :size="42" :style="{ color: item.color }" />
              </div>
            </div>

            <div class="bill-report__grid">
              <section class="bill-report__section bill-report__section--tall">
                <h3>① 电量汇总</h3>
                <div class="bill-report__list">
                  <div v-for="item in energyStatRows" :key="item.label">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
                <Echart :options="energyPieOptions" height="210px" />
              </section>

              <section class="bill-report__section">
                <h3>② 充电总成本</h3>
                <el-table :data="costRows" size="small">
                  <el-table-column label="时段" prop="period" min-width="90" />
                  <el-table-column label="电量(kWh)" prop="energy" align="right" width="110" />
                  <el-table-column label="单价(元)" prop="rate" align="right" width="100" />
                  <el-table-column label="金额" prop="amount" align="right" width="110" />
                </el-table>
                <div class="bill-report__total">
                  <div><span>总充入电量</span><strong>{{ formatKwh(monthlyPurchasedEnergy) }}</strong></div>
                  <div><span>充电总成本</span><strong>{{ formatCurrency(chargeTotalCost) }}</strong></div>
                </div>
              </section>

              <section class="bill-report__section">
                <h3>③ 放电等效电费</h3>
                <div class="bill-report__split">
                  <div class="bill-report__list">
                    <div v-for="item in revenueRows" :key="item.label">
                      <span>{{ item.label }}</span>
                      <strong>{{ item.value }}</strong>
                    </div>
                  </div>
                  <Echart :options="revenueBarOptions" height="210px" />
                </div>
              </section>

              <section class="bill-report__section">
                <h3>④ 节约成本</h3>
                <p class="bill-report__formula">节约成本 = 放电等效电费 - 充电总成本；按正向/反向有功电能分时时段测算。</p>
                <div class="bill-report__profit">
                  <div v-for="item in profitRows" :key="item.label">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
                <div class="bill-report__final">节约成本：{{ finalProfitText }}</div>
              </section>

              <section class="bill-report__section">
                <h3>⑤ 储能利用情况</h3>
                <div class="bill-report__battery">
                  <div v-for="item in batteryRows" :key="item.label">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
                <div class="bill-report__chart-title">电表正向有功电能曲线</div>
                <Echart :options="energyLineOptions" height="210px" />
              </section>
            </div>
          </div>
        </section>
      </el-tab-pane>

      <el-tab-pane label="设备监控面板" name="monitor">
        <section class="bill-report-panel monitor-report-panel">
          <div class="bill-report-panel__header">
            <div>
              <h2>设备监控面板</h2>
              <span>按客户、场站或电表筛选设备状态，点击详情查看电表实时数据、历史曲线和报警信息。</span>
            </div>
            <el-button type="primary" :loading="loading" @click="loadAll">
              <Icon class="mr-5px" icon="ep:refresh" />
              刷新监控
            </el-button>
          </div>

          <el-form :inline="true" class="telemetry-form bill-report__filters monitor-report__filters" label-width="84px">
            <el-form-item label="状态">
              <div class="monitor-report__status-filter">
                <el-button :type="statusFilter === undefined ? 'primary' : 'default'" @click="statusFilter = undefined">
                  全部 {{ baseFilteredDevices.length }}
                </el-button>
                <el-button
                  v-for="item in monitorStatusFilters"
                  :key="item.label"
                  :type="statusFilter === item.value ? 'primary' : 'default'"
                  @click="setStatusFilter(item.value)"
                >
                  {{ item.label }} {{ item.count }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="节点类型">
              <el-select v-model="nodeTypeFilter" clearable placeholder="请选择节点类型" class="!w-150px">
                <el-option label="客户" value="customer" />
                <el-option label="项目" value="project" />
                <el-option label="设备" value="device" />
              </el-select>
            </el-form-item>
            <el-form-item label="节点">
              <el-select v-model="nodeFilter" clearable filterable placeholder="请选择节点" class="!w-180px">
                <el-option
                  v-for="item in nodeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="能源类型">
              <el-select v-model="energyTypeFilter" clearable placeholder="能源类型" class="!w-150px">
                <el-option label="电表" value="meter" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键字">
              <el-input v-model="keyword" clearable placeholder="网关识别号/仪表型号" class="!w-220px">
                <template #prefix>
                  <Icon icon="ep:search" />
                </template>
              </el-input>
            </el-form-item>
          </el-form>

          <div class="bill-report">
            <div class="bill-report__scope-summary">
              <strong>{{ monitorStationName }}</strong>
              <span>
                {{ monitorCustomerName }} / {{ monitorProjectName }}，当前显示 {{ filteredDevices.length }} 块电表
              </span>
            </div>

            <div class="bill-report__kpis monitor-report__kpis">
              <div v-for="item in statCards" :key="item.label" class="bill-report__kpi monitor-report__kpi">
                <div>
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                  <small>按当前筛选范围统计</small>
                </div>
                <Icon :icon="item.icon" :size="42" :style="{ color: item.color }" />
              </div>
            </div>

            <section class="bill-report__section monitor-report__section">
              <div class="monitor-report__section-head">
                <div>
                  <h3>电表设备列表</h3>
                  <span>卡片模式适合快速查看，表格模式适合核对网关、仪表编号和最近采集时间。</span>
                </div>
                <div class="monitor-report__view-switch">
                  <el-button
                    :type="monitorViewMode === 'grid' ? 'primary' : 'default'"
                    @click="monitorViewMode = 'grid'"
                  >
                    <Icon class="mr-5px" icon="ep:grid" />
                    卡片
                  </el-button>
                  <el-button
                    :type="monitorViewMode === 'table' ? 'primary' : 'default'"
                    @click="monitorViewMode = 'table'"
                  >
                    <Icon class="mr-5px" icon="ep:list" />
                    表格
                  </el-button>
                </div>
              </div>

              <el-skeleton :loading="loading" animated>
                <el-empty v-if="filteredDevices.length === 0" description="暂无电表数据" />
                <div v-else-if="monitorViewMode === 'grid'" class="meter-grid monitor-report__meter-grid">
                  <button
                    v-for="device in filteredDevices"
                    :key="device.id || device.deviceNo"
                    type="button"
                    class="meter-card"
                    :class="{ 'is-active': device.id === selectedDeviceId }"
                    @click="selectDevice(device)"
                  >
                    <span :class="['meter-card__status', getDeviceStatusClass(device.status)]">
                      {{ getStatusText(device.status) }}
                    </span>
                    <span class="meter-card__name">{{ device.deviceName || device.deviceNo || '--' }}</span>
                    <div class="meter-card__body">
                      <div class="meter-card__icon">
                        <Icon icon="ep:odometer" :size="34" />
                      </div>
                      <dl>
                        <div>
                          <dt>网关识别号：</dt>
                          <dd>{{ device.gatewaySn || '--' }}</dd>
                        </div>
                        <div>
                          <dt>仪表地址：</dt>
                          <dd>{{ device.meterSn || device.meterNo || '--' }}</dd>
                        </div>
                        <div>
                          <dt>仪表型号：</dt>
                          <dd>{{ device.deviceNo || 'ADW300-IOT' }}</dd>
                        </div>
                      </dl>
                    </div>
                    <el-button class="meter-card__detail" @click.stop="openRealtimeDetail(device)">详情</el-button>
                  </button>
                </div>
                <el-table
                  v-else
                  :data="filteredDevices"
                  :show-overflow-tooltip="true"
                  :stripe="true"
                  class="monitor-table"
                >
                  <el-table-column align="center" label="设备名称" min-width="180">
                    <template #default="{ row }">{{ row.deviceName || row.deviceNo || '--' }}</template>
                  </el-table-column>
                  <el-table-column align="center" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getStatusType(row.status)" effect="light">{{ getStatusText(row.status) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column align="center" label="网关识别号" min-width="180" prop="gatewaySn" />
                  <el-table-column align="center" label="仪表地址" min-width="180">
                    <template #default="{ row }">{{ row.meterSn || row.meterNo || '--' }}</template>
                  </el-table-column>
                  <el-table-column align="right" label="总有功功率(kW)" width="150">
                    <template #default="{ row }">{{ formatCellValue(row.lastPower) }}</template>
                  </el-table-column>
                  <el-table-column align="center" label="最近采集" width="180">
                    <template #default="{ row }">{{ formatDateTime(row.lastReadingTime) }}</template>
                  </el-table-column>
                  <el-table-column align="center" fixed="right" label="操作" width="100">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="openRealtimeDetail(row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-skeleton>

              <div class="monitor-pagination monitor-report__pagination">
                <span>共 {{ filteredDevices.length }} 条</span>
                <span>当前页展示全部筛选结果</span>
                <strong>1</strong>
              </div>
            </section>
          </div>
        </section>
      </el-tab-pane>
      <el-tab-pane label="数据查看" name="trend">
        <section class="telemetry-panel">
          <el-row :gutter="12" class="telemetry-stats">
            <el-col v-for="item in statCards" :key="item.label" :lg="6" :md="12" :xs="24">
              <div class="energy-telemetry__stat">
                <div>
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
                <Icon :icon="item.icon" :style="{ color: item.color }" :size="30" />
              </div>
            </el-col>
          </el-row>

          <div class="telemetry-panel__header">
            <div>
              <h2>数据查看</h2>
              <span>按电表和参数类型查看日原始曲线或逐日极值</span>
            </div>
          </div>

          <el-form :inline="true" :model="trendQuery" class="telemetry-form" label-width="84px">
            <el-form-item label="电表">
              <el-select v-model="trendQuery.deviceId" filterable placeholder="请选择电表" class="!w-260px">
                <el-option
                  v-for="item in devices"
                  :key="item.id"
                  :label="getDeviceOptionLabel(item)"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="trendQuery.collectTime"
                type="datetimerange"
                value-format="YYYY-MM-DD HH:mm:ss"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                class="!w-360px"
              />
            </el-form-item>
            <el-form-item label="参数类型">
              <el-select v-model="trendQuery.metricGroup" class="!w-180px">
                <el-option v-for="group in metricGroups" :key="group.value" :label="group.label" :value="group.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTrendData">
                <Icon class="mr-5px" icon="ep:search" />
                查询
              </el-button>
            </el-form-item>
          </el-form>

          <el-tabs v-model="dataTab" @tab-change="loadTrendData">
            <el-tab-pane label="日原始数据" name="raw">
              <div class="data-view-toolbar">
                <el-tabs v-model="rawViewMode">
                  <el-tab-pane label="图表" name="chart" />
                  <el-tab-pane label="数据" name="table" />
                </el-tabs>
                <el-button v-if="rawViewMode === 'table'" type="primary" @click="exportRawData">
                  <Icon class="mr-5px" icon="ep:paperclip" />
                  导出
                </el-button>
              </div>
              <Echart
                v-if="rawViewMode === 'chart'"
                v-loading="trendLoading"
                :options="rawChartOptions"
                height="360px"
              />
              <el-table
                v-else
                v-loading="trendLoading"
                :data="rawTableRows"
                :show-overflow-tooltip="true"
                :stripe="true"
                max-height="460"
              >
                <el-table-column align="center" fixed="left" label="采集时间" min-width="170" prop="collectTime" />
                <el-table-column
                  v-for="field in activeTrendGroup.fields"
                  :key="field.key"
                  align="right"
                  :label="`${field.label}${field.unit ? `(${field.unit})` : ''}`"
                  min-width="150"
                >
                  <template #default="{ row }">{{ formatCellValue(row[field.key]) }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="逐日极值数据" name="daily">
              <div class="metric-radio-row">
                <span>参数：</span>
                <el-radio-group v-model="trendQuery.metric" @change="loadTrendData">
                  <el-radio
                    v-for="field in activeTrendGroup.fields"
                    :key="field.key"
                    :label="field.key"
                  >
                    {{ field.label }}
                  </el-radio>
                </el-radio-group>
              </div>
              <div class="data-view-toolbar">
                <el-tabs v-model="dailyViewMode">
                  <el-tab-pane label="图表" name="chart" />
                  <el-tab-pane label="数据" name="table" />
                </el-tabs>
                <el-button v-if="dailyViewMode === 'table'" type="primary" @click="exportDailyData">
                  <Icon class="mr-5px" icon="ep:paperclip" />
                  导出
                </el-button>
              </div>
              <Echart
                v-if="dailyViewMode === 'chart'"
                v-loading="trendLoading"
                :options="dailyChartOptions"
                height="360px"
              />
              <el-table
                v-else
                v-loading="trendLoading"
                :data="dailyTableRows"
                :show-overflow-tooltip="true"
                :stripe="true"
                class="daily-extreme-table"
                max-height="460"
              >
                <el-table-column align="center" fixed="left" label="采集时间" min-width="150" prop="date" />
                <el-table-column align="center" :label="dailyMetricLabel">
                  <el-table-column align="center" label="最大值">
                    <el-table-column align="right" label="数值" min-width="130">
                      <template #default="{ row }">{{ formatCellValue(row.max) }}</template>
                    </el-table-column>
                    <el-table-column align="center" label="发生时间" min-width="170">
                      <template #default="{ row }">{{ formatDateTime(row.maxTime) }}</template>
                    </el-table-column>
                  </el-table-column>
                  <el-table-column align="center" label="最小值">
                    <el-table-column align="right" label="数值" min-width="130">
                      <template #default="{ row }">{{ formatCellValue(row.min) }}</template>
                    </el-table-column>
                    <el-table-column align="center" label="发生时间" min-width="170">
                      <template #default="{ row }">{{ formatDateTime(row.minTime) }}</template>
                    </el-table-column>
                  </el-table-column>
                  <el-table-column align="right" label="平均值" min-width="130">
                    <template #default="{ row }">{{ formatCellValue(row.avg) }}</template>
                  </el-table-column>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </section>
      </el-tab-pane>

      <el-tab-pane label="详细实时数据" name="detail">
        <section class="telemetry-panel">
          <el-row :gutter="12" class="telemetry-stats">
            <el-col v-for="item in statCards" :key="item.label" :lg="6" :md="12" :xs="24">
              <div class="energy-telemetry__stat">
                <div>
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
                <Icon :icon="item.icon" :style="{ color: item.color }" :size="30" />
              </div>
            </el-col>
          </el-row>

          <div class="telemetry-panel__header">
            <div>
              <h2>详细实时数据</h2>
              <span>按指定时间间隔查看电表采集明细，没有数据的位置显示 --</span>
            </div>
          </div>

          <el-form :inline="true" :model="detailQuery" class="telemetry-form" label-width="84px">
            <el-form-item label="电表">
              <el-select v-model="detailQuery.deviceId" filterable placeholder="请选择电表" class="!w-260px">
                <el-option
                  v-for="item in devices"
                  :key="item.id"
                  :label="getDeviceOptionLabel(item)"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="日期">
              <el-date-picker v-model="detailQuery.date" type="date" value-format="YYYY-MM-DD" class="!w-160px" />
            </el-form-item>
            <el-form-item label="开始">
              <el-time-picker v-model="detailQuery.startTime" value-format="HH:mm:ss" class="!w-140px" />
            </el-form-item>
            <el-form-item label="结束">
              <el-time-picker v-model="detailQuery.endTime" value-format="HH:mm:ss" class="!w-140px" />
            </el-form-item>
            <el-form-item label="参数类型">
              <el-select v-model="detailQuery.metricGroup" class="!w-180px">
                <el-option v-for="group in metricGroups" :key="group.value" :label="group.label" :value="group.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间间隔">
              <el-select v-model="detailQuery.intervalMinutes" class="!w-140px">
                <el-option label="一分钟" :value="1" />
                <el-option label="五分钟" :value="5" />
                <el-option label="十五分钟" :value="15" />
                <el-option label="半小时" :value="30" />
                <el-option label="一小时" :value="60" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadDetailData">
                <Icon class="mr-5px" icon="ep:search" />
                查询
              </el-button>
            </el-form-item>
          </el-form>

          <el-table
            v-loading="detailLoading"
            :data="detailRows"
            :show-overflow-tooltip="true"
            :stripe="true"
            class="mt-12px"
          >
            <el-table-column align="center" fixed="left" label="采集时间" width="120" prop="time" />
            <el-table-column
              v-for="field in activeDetailGroup.fields"
              :key="field.key"
              align="right"
              :label="`${field.label}${field.unit ? `(${field.unit})` : ''}`"
              min-width="130"
            >
              <template #default="{ row }">{{ formatCellValue(row[field.key]) }}</template>
            </el-table-column>
          </el-table>
        </section>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="realtimeDetailVisible"
      class="realtime-detail-dialog"
      fullscreen
      :show-close="false"
    >
      <div v-loading="latestLoading">
        <el-empty v-if="!selectedDevice" description="请选择电表" />
        <div v-else class="realtime-detail">
          <div class="realtime-detail__toolbar">
            <el-button link @click="realtimeDetailVisible = false">
              <Icon class="mr-6px" icon="ep:back" />
              返回
            </el-button>
            <div>
              <strong>{{ selectedDevice.deviceName || selectedDevice.deviceNo || '-' }}</strong>
              <span>{{ selectedDevice.projectName || '未绑定项目' }} / {{ selectedDevice.customerName || '未绑定客户' }}</span>
            </div>
            <div class="realtime-detail__actions">
              <el-button
                type="success"
                :loading="controlLoading"
                @click="handleDeviceControl('SWITCH', '1')"
              >
                <Icon class="mr-6px" icon="ep:switch-button" />
                合闸
              </el-button>
              <el-button
                type="danger"
                plain
                :loading="controlLoading"
                @click="handleDeviceControl('SWITCH', '0')"
              >
                <Icon class="mr-6px" icon="ep:close" />
                开闸
              </el-button>
              <el-button plain :loading="controlLoading" @click="handleDeviceControl('REFRESH')">
                <Icon class="mr-6px" icon="ep:refresh" />
                刷新状态
              </el-button>
            </div>
          </div>

          <div class="realtime-detail__meta">
            <div>
              <span>{{ selectedDevice.deviceName || selectedDevice.deviceNo || '--' }}</span>
              <el-tag :type="getRealtimeStateTagType(latestTelemetry?.state, selectedDevice.status)" effect="plain">
                {{ getRealtimeStateText(latestTelemetry?.state, selectedDevice.status) }}
              </el-tag>
            </div>
            <div>
              数据更新时间：{{ formatDateTime(latestTelemetry?.collectTime || selectedDevice.lastReadingTime) }}
            </div>
          </div>

          <section>
            <h2>实时数据</h2>
            <div class="realtime-card-grid">
              <article v-for="card in realtimeCards" :key="card.title" class="realtime-data-card">
                <h3>{{ card.title }}</h3>
                <div>
                  <span v-for="item in card.items" :key="item.label">
                    <em>{{ item.label }}：</em>
                    <strong>{{ item.value }}</strong>
                  </span>
                </div>
              </article>
            </div>
          </section>

          <section>
            <h2>报文信息</h2>
            <div class="realtime-card-grid is-compact">
              <article class="realtime-data-card">
                <h3>设备识别</h3>
                <div>
                  <span>
                    <em>网关编号：</em>
                    <strong>{{ latestTelemetry?.gatewaySn || selectedDevice.gatewaySn || '--' }}</strong>
                  </span>
                  <span>
                    <em>仪表编号：</em>
                    <strong>{{ latestTelemetry?.meterSn || selectedDevice.meterSn || '--' }}</strong>
                  </span>
                  <span>
                    <em>全平台仪表号：</em>
                    <strong>{{ latestTelemetry?.meterNo || selectedDevice.meterNo || '--' }}</strong>
                  </span>
                </div>
              </article>
              <article class="realtime-data-card">
                <h3>采集来源</h3>
                <div>
                  <span>
                    <em>数据类型：</em>
                    <strong>{{ latestTelemetry?.source || '--' }}</strong>
                  </span>
                  <span>
                    <em>设备状态：</em>
                    <strong>{{ latestTelemetry?.state || getStatusText(selectedDevice.status) }}</strong>
                  </span>
                  <span>
                    <em>timestamp：</em>
                    <strong>{{ latestTelemetry?.timestamp || '--' }}</strong>
                  </span>
                </div>
              </article>
            </div>
          </section>

          <section class="realtime-detail-lower">
            <el-tabs v-model="realtimeDetailTab" @tab-change="loadRealtimeLowerData">
              <el-tab-pane label="历史曲线" name="history">
                <div class="realtime-history-toolbar">
                  <el-select v-model="realtimeHistoryMetricGroup" class="!w-180px" @change="loadRealtimeHistoryData">
                    <el-option
                      v-for="group in metricGroups"
                      :key="group.value"
                      :label="group.label"
                      :value="group.value"
                    />
                  </el-select>
                  <el-date-picker
                    v-model="realtimeHistoryDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    class="!w-160px"
                    @change="loadRealtimeHistoryData"
                  />
                  <el-button type="primary" @click="loadRealtimeHistoryData">
                    <Icon class="mr-5px" icon="ep:search" />
                    查询
                  </el-button>
                </div>
                <Echart
                  v-loading="realtimeHistoryLoading"
                  :options="realtimeHistoryChartOptions"
                  height="320px"
                />
              </el-tab-pane>
              <el-tab-pane label="报警信息" name="alarm">
                <div class="realtime-alarm-summary">共 {{ realtimeAlarmTotal }} 条报警记录</div>
                <el-table
                  v-loading="realtimeAlarmLoading"
                  :data="realtimeAlarmList"
                  :show-overflow-tooltip="true"
                  :stripe="true"
                  max-height="360"
                >
                  <el-table-column align="center" label="报警编号" min-width="170" prop="alarmNo" />
                  <el-table-column align="center" label="报警代码" min-width="140" prop="code" />
                  <el-table-column align="center" label="报警标题" min-width="180" prop="title" />
                  <el-table-column align="center" label="等级" width="90" prop="level">
                    <template #default="{ row }">
                      <dict-tag :type="DICT_TYPE.ENERGY_ALARM_LEVEL" :value="row.level" />
                    </template>
                  </el-table-column>
                  <el-table-column align="center" label="状态" width="90" prop="status">
                    <template #default="{ row }">
                      <dict-tag :type="DICT_TYPE.ENERGY_ALARM_STATUS" :value="row.status" />
                    </template>
                  </el-table-column>
                  <el-table-column align="center" label="发生时间" width="180">
                    <template #default="{ row }">{{ formatDateTime(row.occurTime) }}</template>
                  </el-table-column>
                  <el-table-column align="left" label="报警内容" min-width="260" prop="content" />
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </section>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { Echart } from '@/components/Echart'
import { EnergyDeviceApi } from '@/api/energy/device'
import type { EnergyDeviceVO } from '@/api/energy/device'
import { EnergyTelemetryApi } from '@/api/energy/telemetry'
import type { EnergyTelemetryDailyStatVO, EnergyTelemetryVO } from '@/api/energy/telemetry'
import { EnergyChargeSessionApi } from '@/api/energy/chargeSession'
import type { EnergyChargeSessionVO } from '@/api/energy/chargeSession'
import { EnergyPricingRuleApi } from '@/api/energy/pricingRule'
import type { EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import { EnergyReportApi } from '@/api/energy/report'
import type { EnergyReportBillVO } from '@/api/energy/report'
import { EnergyAlarmApi } from '@/api/energy/alarm'
import type { EnergyAlarmVO } from '@/api/energy/alarm'
import { DICT_TYPE } from '@/utils/dict'
import { archiveReport } from '@/utils/reportArchive'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'

defineOptions({ name: 'EnergyTelemetry' })

type MetricKey = 'pa' | 'pb' | 'pc' | 'p' | 'ua' | 'ub' | 'uc' | 'ia' | 'ib' | 'ic' | 'pf' | 'epi'
type MetricGroupValue = 'activePower' | 'phaseVoltage' | 'phaseCurrent' | 'powerFactor' | 'energy'
type BillScopeType = 'all' | 'project' | 'device'
type MetricField = { key: MetricKey; label: string; unit: string }
type RealtimeCard = { title: string; items: Array<{ label: string; value: string }> }
type DetailRow = { time: string } & Partial<Record<MetricKey, number | null>>
type DailyRow = {
  date: string
  max: number | null
  maxTime?: string | Date
  min: number | null
  minTime?: string | Date
  avg: number | null
}
type TouKey = 'sharpPeak' | 'peak' | 'flat' | 'valley' | 'deepValley'
type TouEnergy = Record<TouKey, number>
type TouSource = 'epi' | 'epe'
type TouPeriod = { type: TouKey; start: string; end: string }
type TelemetryInterval = {
  deviceId: number
  startTime: string | Date
  endTime: string | Date
  chargeTotal: number
  dischargeTotal: number
  chargeTou: TouEnergy
  dischargeTou: TouEnergy
}

const message = useMessage()
const today = dayjs().format('YYYY-MM-DD')

const loading = ref(true)
const latestLoading = ref(false)
const billLoading = ref(false)
const trendLoading = ref(false)
const detailLoading = ref(false)
const devices = ref<EnergyDeviceVO[]>([])
const selectedDeviceId = ref<number>()
const latestTelemetry = ref<EnergyTelemetryVO>()
const billReport = ref<EnergyReportBillVO>()
const billTelemetryRows = ref<EnergyTelemetryVO[]>([])
const chargeSessionRows = ref<EnergyChargeSessionVO[]>([])
const pricingRuleRows = ref<EnergyPricingRuleVO[]>([])
const trendRawList = ref<EnergyTelemetryVO[]>([])
const dailyStatList = ref<EnergyTelemetryDailyStatVO[]>([])
const detailRawList = ref<EnergyTelemetryVO[]>([])
const keyword = ref('')
const statusFilter = ref<number>()
const nodeTypeFilter = ref<'customer' | 'project' | 'device'>()
const nodeFilter = ref<string>()
const energyTypeFilter = ref<'meter'>()
const monitorViewMode = ref<'grid' | 'table'>('grid')
const dataTab = ref<'raw' | 'daily'>('raw')
const rawViewMode = ref<'chart' | 'table'>('chart')
const dailyViewMode = ref<'chart' | 'table'>('chart')
const activeSection = ref<'bill' | 'monitor' | 'trend' | 'detail'>('bill')
const realtimeDetailVisible = ref(false)
const realtimeDetailTab = ref<'history' | 'alarm'>('history')
const realtimeHistoryMetricGroup = ref<MetricGroupValue>('activePower')
const realtimeHistoryDate = ref(today)
const realtimeHistoryLoading = ref(false)
const realtimeHistoryRawList = ref<EnergyTelemetryVO[]>([])
const realtimeAlarmLoading = ref(false)
const realtimeAlarmList = ref<EnergyAlarmVO[]>([])
const realtimeAlarmTotal = ref(0)
const controlLoading = ref(false)

const metricGroups: Array<{ value: MetricGroupValue; label: string; fields: MetricField[] }> = [
  {
    value: 'activePower',
    label: '有功功率',
    fields: [
      { key: 'pa', label: 'A相有功功率', unit: 'kW' },
      { key: 'pb', label: 'B相有功功率', unit: 'kW' },
      { key: 'pc', label: 'C相有功功率', unit: 'kW' },
      { key: 'p', label: '总有功功率', unit: 'kW' }
    ]
  },
  {
    value: 'phaseVoltage',
    label: '相电压',
    fields: [
      { key: 'ua', label: 'A相电压', unit: 'V' },
      { key: 'ub', label: 'B相电压', unit: 'V' },
      { key: 'uc', label: 'C相电压', unit: 'V' }
    ]
  },
  {
    value: 'phaseCurrent',
    label: '相电流',
    fields: [
      { key: 'ia', label: 'A相电流', unit: 'A' },
      { key: 'ib', label: 'B相电流', unit: 'A' },
      { key: 'ic', label: 'C相电流', unit: 'A' }
    ]
  },
  {
    value: 'powerFactor',
    label: '功率因数',
    fields: [{ key: 'pf', label: '总功率因数', unit: '' }]
  },
  {
    value: 'energy',
    label: '有功电能',
    fields: [{ key: 'epi', label: '正向总有功电能', unit: 'kWh' }]
  }
]

const trendQuery = reactive<{
  deviceId?: number
  collectTime: string[]
  metricGroup: MetricGroupValue
  metric: MetricKey
}>({
  deviceId: undefined,
  collectTime: [`${today} 00:00:00`, `${today} 23:59:59`],
  metricGroup: 'activePower',
  metric: 'p'
})

const detailQuery = reactive<{
  deviceId?: number
  date: string
  startTime: string
  endTime: string
  metricGroup: MetricGroupValue
  intervalMinutes: number
}>({
  deviceId: undefined,
  date: today,
  startTime: '00:00:00',
  endTime: '23:59:59',
  metricGroup: 'phaseVoltage',
  intervalMinutes: 5
})

const billQuery = reactive<{
  scopeType: BillScopeType
  projectId?: number
  deviceId?: number
  billMonth: string
}>({
  scopeType: 'all',
  projectId: undefined,
  deviceId: undefined,
  billMonth: dayjs().format('YYYY-MM')
})

const billRange = computed(() => {
  const month = dayjs(billQuery.billMonth || dayjs().format('YYYY-MM'))
  return {
    start: month.startOf('month').format('YYYY-MM-DD HH:mm:ss'),
    end: month.endOf('month').format('YYYY-MM-DD HH:mm:ss')
  }
})

const selectedDevice = computed(() => devices.value.find((item) => item.id === selectedDeviceId.value))
const activeTrendGroup = computed(() => getMetricGroup(trendQuery.metricGroup))
const activeDetailGroup = computed(() => getMetricGroup(detailQuery.metricGroup))
const activeRealtimeHistoryGroup = computed(() => getMetricGroup(realtimeHistoryMetricGroup.value))
const dailyMetric = computed(() => activeTrendGroup.value.fields.find((item) => item.key === trendQuery.metric))
const dailyMetricLabel = computed(() => {
  const metric = dailyMetric.value
  if (!metric) {
    return '逐日极值'
  }
  return `${metric.label}${metric.unit ? `(${metric.unit})` : ''}`
})

const realtimeCards = computed<RealtimeCard[]>(() => [
  {
    title: '相电压',
    items: [
      { label: 'A相电压', value: formatValue(latestTelemetry.value?.ua, 'V') },
      { label: 'B相电压', value: formatValue(latestTelemetry.value?.ub, 'V') },
      { label: 'C相电压', value: formatValue(latestTelemetry.value?.uc, 'V') }
    ]
  },
  {
    title: '相电流',
    items: [
      { label: 'A相电流', value: formatValue(latestTelemetry.value?.ia, 'A') },
      { label: 'B相电流', value: formatValue(latestTelemetry.value?.ib, 'A') },
      { label: 'C相电流', value: formatValue(latestTelemetry.value?.ic, 'A') }
    ]
  },
  {
    title: '有功功率',
    items: [
      { label: 'A相有功功率', value: formatValue(latestTelemetry.value?.pa, 'kW') },
      { label: 'B相有功功率', value: formatValue(latestTelemetry.value?.pb, 'kW') },
      { label: 'C相有功功率', value: formatValue(latestTelemetry.value?.pc, 'kW') },
      { label: '总有功功率', value: formatValue(latestTelemetry.value?.p, 'kW') }
    ]
  },
  {
    title: '功率因数',
    items: [{ label: '总功率因数', value: formatValue(latestTelemetry.value?.pf) }]
  },
  {
    title: '有功电能',
    items: [{ label: '正向总有功电能', value: formatValue(latestTelemetry.value?.epi, 'kWh') }]
  }
])

const monitorDevice = computed(() => selectedDevice.value || devices.value[0])
const monitorStationName = computed(() => monitorDevice.value?.deviceName || '100kWh移动电源_001')
const monitorCustomerName = computed(() => monitorDevice.value?.customerName || '未绑定客户')
const monitorProjectName = computed(() => monitorDevice.value?.projectName || '未绑定项目')

const statusCounts = computed(() => {
  const source = baseFilteredDevices.value
  return {
    normal: source.filter((item) => item.status === 0).length,
    offline: source.filter((item) => item.status === 1).length,
    fault: source.filter((item) => item.status === 2).length,
    maintenance: source.filter((item) => item.status === 3).length,
    alarm: source.filter((item) => item.status === 3).length
  }
})

const monitorStatusFilters = computed(() => [
  { label: '在线', value: 0, tone: 'normal', count: statusCounts.value.normal },
  { label: '离线', value: 1, tone: 'offline', count: statusCounts.value.offline },
  { label: '故障', value: 2, tone: 'fault', count: statusCounts.value.fault },
  { label: '维护', value: 3, tone: 'alarm', count: statusCounts.value.maintenance }
])

const sidebarMetrics = computed(() => [
  {
    label: '正向总有功电能',
    value: formatValue(latestTelemetry.value?.epi, 'kW·h'),
    icon: 'ep:document'
  },
  {
    label: '当前总有功功率',
    value: formatNumber(devices.value.reduce((sum, item) => sum + Number(item.lastPower || 0), 0)) + ' kW',
    icon: 'ep:trend-charts'
  },
  {
    label: '当前在线率',
    value: formatPercent(statusCounts.value.normal, baseFilteredDevices.value.length),
    icon: 'ep:connection'
  }
])

const nodeOptions = computed(() => {
  const type = nodeTypeFilter.value
  if (!type) {
    return []
  }
  const map = new Map<string, string>()
  devices.value.forEach((device) => {
    if (type === 'customer' && device.customerId) {
      map.set(`customer:${device.customerId}`, device.customerName || String(device.customerId))
    }
    if (type === 'project' && device.projectId) {
      map.set(`project:${device.projectId}`, device.projectName || String(device.projectId))
    }
    if (type === 'device' && device.id) {
      map.set(`device:${device.id}`, device.deviceName || device.deviceNo || String(device.id))
    }
  })
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
})

const baseFilteredDevices = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return devices.value.filter((device) => {
    if (nodeFilter.value && !isDeviceInSelectedNode(device, nodeFilter.value)) {
      return false
    }
    if (energyTypeFilter.value && energyTypeFilter.value !== 'meter') {
      return false
    }
    if (!text) {
      return true
    }
    const searchable = [
      device.deviceName,
      device.deviceNo,
      device.customerName,
      device.projectName,
      device.gatewaySn,
      device.meterSn,
      device.meterNo
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return searchable.includes(text)
  })
})

const filteredDevices = computed(() => {
  return baseFilteredDevices.value.filter((device) => {
    return statusFilter.value === undefined || statusFilter.value === null || device.status === statusFilter.value
  })
})

const statCards = computed(() => [
  {
    label: '监控电表',
    value: filteredDevices.value.length,
    icon: 'ep:monitor',
    color: '#2563eb'
  },
  {
    label: '在线电表',
    value: filteredDevices.value.filter((item) => item.status === 0).length,
    icon: 'ep:connection',
    color: '#0f766e'
  },
  {
    label: '离线电表',
    value: filteredDevices.value.filter((item) => item.status === 1).length,
    icon: 'ep:warning',
    color: '#dc2626'
  },
  {
    label: '总有功功率(kW)',
    value: formatNumber(filteredDevices.value.reduce((sum, item) => sum + Number(item.lastPower || 0), 0)),
    icon: 'ep:lightning',
    color: '#ca8a04'
  }
])

const billProjectOptions = computed(() => {
  const map = new Map<number, string>()
  devices.value.forEach((device) => {
    const projectId = normalizeNumber(device.projectId)
    if (projectId === null) {
      return
    }
    map.set(projectId, device.projectName || `场站 ${projectId}`)
  })
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
})

const selectedBillDevices = computed(() => {
  if (billQuery.scopeType === 'device') {
    return devices.value.filter((device) => Number(device.id) === Number(billQuery.deviceId))
  }
  if (billQuery.scopeType === 'project') {
    return devices.value.filter((device) => Number(device.projectId) === Number(billQuery.projectId))
  }
  return devices.value
})

const selectedBillDeviceIds = computed(() => {
  return new Set(selectedBillDevices.value.map((device) => Number(device.id)).filter((id) => Number.isFinite(id)))
})

const billScopeTitle = computed(() => {
  if (billQuery.scopeType === 'device') {
    const device = selectedBillDevices.value[0]
    return device?.deviceName || device?.deviceNo || '单个电表'
  }
  if (billQuery.scopeType === 'project') {
    return billProjectOptions.value.find((item) => Number(item.value) === Number(billQuery.projectId))?.label || '场站汇总'
  }
  return '全部电表汇总'
})

const scopedBillTelemetryRows = computed(() => {
  const deviceIds = selectedBillDeviceIds.value
  if (billQuery.scopeType === 'all') {
    return billTelemetryRows.value
  }
  return billTelemetryRows.value.filter((item) => deviceIds.has(Number(item.deviceId)))
})

const sortedBillTelemetry = computed(() => {
  return [...scopedBillTelemetryRows.value]
    .filter((item) => item.collectTime)
    .sort((a, b) => dayjs(a.collectTime as string).valueOf() - dayjs(b.collectTime as string).valueOf())
})
const billTelemetryIntervals = computed(() => buildTelemetryIntervals(sortedBillTelemetry.value))

const startEpi = computed(() => billReport.value?.deviceDetails?.length ? sumNullableReportDeviceField('startEpi') : calculateEpiBoundary(sortedBillTelemetry.value, 'start'))
const endEpi = computed(() => billReport.value?.deviceDetails?.length ? sumNullableReportDeviceField('endEpi') : calculateEpiBoundary(sortedBillTelemetry.value, 'end'))
const epiDelta = computed(() => sumIntervals(billTelemetryIntervals.value, 'chargeTotal'))

const billSessions = computed(() => {
  const start = dayjs(billRange.value.start).valueOf()
  const end = dayjs(billRange.value.end).valueOf()
  const deviceIds = selectedBillDeviceIds.value
  return chargeSessionRows.value.filter((item) => {
    const time = dayjs(item.startTime || item.createTime).valueOf()
    return deviceIds.has(Number(item.deviceId)) && time >= start && time <= end
  })
})

const applicablePricingRules = computed(() => {
  const selectedDevices = selectedBillDevices.value
  const deviceIds = selectedBillDeviceIds.value
  const selectedProjectIds = new Set(selectedDevices.map((device) => Number(device.projectId)).filter((id) => Number.isFinite(id)))
  const selectedCustomerIds = new Set(selectedDevices.map((device) => Number(device.customerId)).filter((id) => Number.isFinite(id)))
  return pricingRuleRows.value.filter((rule) => {
    if (Number(rule.status) !== 0) return false
    if (rule.deviceId) return deviceIds.has(Number(rule.deviceId))
    if (rule.projectId) return selectedProjectIds.has(Number(rule.projectId))
    if (rule.customerId) return selectedCustomerIds.has(Number(rule.customerId))
    return !rule.deviceId && !rule.projectId && !rule.customerId
  })
})

const touRates = computed<TouEnergy>(() => ({
  sharpPeak: averageNumber(applicablePricingRules.value.map((item) => normalizeNumber(item.sharpPeakRate))),
  peak: averageNumber(applicablePricingRules.value.map((item) => normalizeNumber(item.peakRate))),
  flat: averageNumber(applicablePricingRules.value.map((item) => normalizeNumber(item.flatRate))),
  valley: averageNumber(applicablePricingRules.value.map((item) => normalizeNumber(item.valleyRate))),
  deepValley: averageNumber(applicablePricingRules.value.map((item) => normalizeNumber(item.deepValleyRate)))
}))

const dischargeSessions = computed(() => billSessions.value.filter((item) => Number(item.sessionType) === 1))
const monthlyPurchasedEnergy = computed(() => Number((billReport.value?.summary?.totalChargeEnergy ?? epiDelta.value).toFixed(2)))
const monthlySoldEnergy = computed(() => Number((billReport.value?.summary?.totalDischargeEnergy ?? sumIntervals(billTelemetryIntervals.value, 'dischargeTotal')).toFixed(2)))
const batteryCycleCount = computed(() => billReport.value?.summary?.cycleCount ?? countChargeCycles(billTelemetryIntervals.value))
const chargeTouEnergy = computed(() => billReport.value?.analysis?.chargeTou ? normalizeReportTou(billReport.value.analysis.chargeTou) : buildTouEnergy('epi'))
const dischargeTouEnergy = computed(() => billReport.value?.analysis?.dischargeTou ? normalizeReportTou(billReport.value.analysis.dischargeTou) : buildTouEnergy('epe'))
const chargeTouSum = computed(() => sumTouEnergy(chargeTouEnergy.value))
const dischargeTouSum = computed(() => sumTouEnergy(dischargeTouEnergy.value))
const chargeConsistencyText = computed(() => billReport.value?.analysis?.chargeConsistency || consistencyText(chargeTouSum.value, monthlyPurchasedEnergy.value, 'EPI'))
const dischargeConsistencyText = computed(() => billReport.value?.analysis?.dischargeConsistency || consistencyText(dischargeTouSum.value, monthlySoldEnergy.value, 'EPE'))
const chargeTotalCost = computed(() => Number((billReport.value?.summary?.chargeCost ?? calculateTouAmount(chargeTouEnergy.value)).toFixed(2)))
const dischargeEquivalentFee = computed(() => Number((billReport.value?.summary?.salesRevenue ?? calculateTouAmount(dischargeTouEnergy.value)).toFixed(2)))
const savedCost = computed(() => Number((billReport.value?.summary?.savedCost ?? (dischargeEquivalentFee.value - chargeTotalCost.value)).toFixed(2)))
const averageBuyRate = computed(() => billReport.value?.summary?.averageBuyRate ?? averageNumber(applicablePricingRules.value.map((item) => normalizeNumber(item.energyRate))))
const averageBuyRateText = computed(() => averageBuyRate.value > 0 ? `${averageBuyRate.value.toFixed(2)} 元/kWh` : '待录入')
const uniqueApplicablePricingRules = computed(() => uniqueRowsById(applicablePricingRules.value))
const siteFee = computed(() => sumPricingRuleField('siteFee'))
const maintenanceFee = computed(() => sumPricingRuleField('maintenanceFee'))
const communicationFee = computed(() => sumPricingRuleField('communicationFee'))
const platformServiceFee = computed(() => sumPricingRuleField('platformServiceFee'))
const batteryDepreciationCost = computed(() => sumPricingRuleField('batteryDepreciationCost'))
const otherFixedFee = computed(() => sumPricingRuleField('otherFixedFee'))
const fixedOperatingCost = computed(() => {
  return Number(
    (
      siteFee.value +
      maintenanceFee.value +
      communicationFee.value +
      platformServiceFee.value +
      otherFixedFee.value
    ).toFixed(2)
  )
})
const batteryEfficiencyText = computed(() => {
  if (!monthlyPurchasedEnergy.value) return '--'
  if (monthlySoldEnergy.value > monthlyPurchasedEnergy.value) return '待补充购电量'
  return formatPercent(monthlySoldEnergy.value, monthlyPurchasedEnergy.value)
})

const billTopCards = computed(() => [
  {
    label: '充入电量合计',
    value: formatKwh(monthlyPurchasedEnergy.value),
    hint: '按正向有功电能首末差汇总',
    icon: 'ep:connection',
    color: '#2088d8'
  },
  {
    label: '放出电量合计',
    value: formatKwh(monthlySoldEnergy.value),
    hint: '按反向有功电能首末差汇总',
    icon: 'ep:truck',
    color: '#0ea5a4'
  },
  {
    label: '放电等效电费',
    value: formatCurrency(dischargeEquivalentFee.value),
    hint: '按放电时段电价计算',
    icon: 'ep:money',
    color: '#16a34a'
  },
  {
    label: '节约成本',
    value: finalProfitText.value,
    hint: '放电等效电费减充电总成本',
    icon: 'ep:trophy',
    color: '#f59e0b'
  }
])

const energyStatRows = computed(() => [
  { label: '期初累计电能', value: formatNullableKwh(startEpi.value) },
  { label: '期末累计电能', value: formatNullableKwh(endEpi.value) },
  { label: '充入电量合计', value: formatKwh(monthlyPurchasedEnergy.value) },
  { label: '放出电量合计', value: formatKwh(monthlySoldEnergy.value) },
  { label: '未售出/自耗电量', value: formatKwh(Math.max(0, monthlyPurchasedEnergy.value - monthlySoldEnergy.value)) },
  { label: '损耗电量', value: formatKwh(Math.max(0, monthlyPurchasedEnergy.value - monthlySoldEnergy.value)) }
])

const costRows = computed(() => {
  return reportTouCostRows('市场化购电费', '零售交易电费') || touRows(chargeTouEnergy.value, 'charge')
})

const revenueRows = computed(() => [
  { label: '放出电量合计', value: formatKwh(monthlySoldEnergy.value) },
  { label: '尖峰放电量', value: formatKwh(dischargeTouEnergy.value.sharpPeak) },
  { label: '高峰放电量', value: formatKwh(dischargeTouEnergy.value.peak) },
  { label: '平时放电量', value: formatKwh(dischargeTouEnergy.value.flat) },
  { label: '低谷放电量', value: formatKwh(dischargeTouEnergy.value.valley) },
  { label: '放电等效电费', value: formatCurrency(dischargeEquivalentFee.value) }
])

const profitRows = computed(() => [
  { label: '放电等效电费', value: formatCurrency(dischargeEquivalentFee.value) },
  { label: '充电总成本', value: formatCurrency(chargeTotalCost.value) },
  { label: '节约成本', value: formatCurrency(savedCost.value) }
])

const finalProfitText = computed(() => {
  return formatCurrency(savedCost.value)
})

const batteryRows = computed(() => [
  { label: '充入电量合计', value: formatKwh(monthlyPurchasedEnergy.value) },
  { label: '放出电量合计', value: formatKwh(monthlySoldEnergy.value) },
  { label: '循环次数', value: `${batteryCycleCount.value} 次` }
])

const energyPieOptions = computed<EChartsOption>(() => ({
  color: ['#2088d8', '#14b8a6', '#8bdc65'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      data: [
        { name: '充入电量', value: monthlyPurchasedEnergy.value },
        { name: '放电量', value: monthlySoldEnergy.value },
        { name: '损耗/自耗', value: Math.max(0, monthlyPurchasedEnergy.value - monthlySoldEnergy.value) }
      ]
    }
  ]
}))

const revenueBarOptions = computed<EChartsOption>(() => {
  const days = Array.from({ length: 7 }).map((_, index) => dayjs(billRange.value.end).subtract(6 - index, 'day'))
  const dailyRows = days.map((day) => sortedBillTelemetry.value.filter((item) => dayjs(item.collectTime).isSame(day, 'day')))
  const energy = dailyRows.map((rows) => calculateTelemetryDeltaInRows(rows, 'epe'))
  const equivalentFee = dailyRows.map((rows) => calculateTouAmount(buildTouEnergyFromRows(rows, 'epe')))
  return {
    color: ['#2088d8', '#22c55e'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 36, right: 16, top: 36, bottom: 28 },
    xAxis: { type: 'category', data: days.map((day) => day.format('MM-DD')) },
    yAxis: { type: 'value' },
    series: [
      { name: '放出电量', type: 'bar', data: energy },
      { name: '等效电费', type: 'bar', data: equivalentFee }
    ]
  }
})

const billEnergyLineRows = computed(() => {
  const map = new Map<string, number>()
  sortedBillTelemetry.value.forEach((item) => {
    const time = formatAxisTime(item.collectTime)
    const value = normalizeNumber(item.epi)
    if (!time || value === null) {
      return
    }
    map.set(time, (map.get(time) || 0) + value)
  })
  return Array.from(map.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([time, value]) => ({ time, value: Number(value.toFixed(2)) }))
})

const energyLineOptions = computed<EChartsOption>(() => ({
  color: ['#2088d8'],
  tooltip: { trigger: 'axis' },
  grid: { left: 44, right: 16, top: 24, bottom: 32 },
  xAxis: { type: 'category', data: billEnergyLineRows.value.map((item) => item.time) },
  yAxis: { type: 'value', name: 'kWh' },
  series: [
    {
      name: billQuery.scopeType === 'device' ? '正向有功电能' : '正向有功电能合计',
      type: 'line',
      smooth: true,
      showSymbol: false,
      areaStyle: { opacity: 0.16 },
      data: billEnergyLineRows.value.map((item) => item.value)
    }
  ]
}))

const rawChartOptions = computed<EChartsOption>(() => {
  const fields = activeTrendGroup.value.fields
  return buildLineOptions({
    xData: rawTableRows.value.map((item) => formatAxisTime(item.collectTime as string)),
    series: fields.map((field) => ({
      name: field.label,
      data: rawTableRows.value.map((item) => normalizeNumber(item[field.key]))
    })),
    yName: fields[0]?.unit || ''
  })
})

const dailyChartOptions = computed<EChartsOption>(() => {
  return buildLineOptions({
    xData: dailyTableRows.value.map((item) => item.date),
    series: [
      { name: '最大值', data: dailyTableRows.value.map((item) => item.max) },
      { name: '最小值', data: dailyTableRows.value.map((item) => item.min) },
      { name: '平均值', data: dailyTableRows.value.map((item) => item.avg) }
    ],
    yName: dailyMetric.value?.unit || ''
  })
})

const realtimeHistoryChartOptions = computed<EChartsOption>(() => {
  const fields = activeRealtimeHistoryGroup.value.fields
  return buildLineOptions({
    xData: realtimeHistoryRows.value.map((item) => formatAxisTime(item.collectTime as string)),
    series: fields.map((field) => ({
      name: field.label,
      data: realtimeHistoryRows.value.map((item) => normalizeNumber(item[field.key]))
    })),
    yName: fields[0]?.unit || ''
  })
})

const rawTableRows = computed(() => {
  const rows: Array<Record<string, string | number | null>> = []
  const range = trendQuery.collectTime
  if (!range?.[0] || !range?.[1]) {
    return rows
  }
  const start = dayjs(range[0])
  const end = dayjs(range[1])
  if (!start.isValid() || !end.isValid() || end.isBefore(start)) {
    return rows
  }

  const records = trendRawList.value
    .filter((item) => item.collectTime)
    .map((item) => ({ ...item, timeValue: dayjs(item.collectTime as string) }))
    .filter((item) => item.timeValue.isValid())

  for (let cursor = start; !cursor.isAfter(end); cursor = cursor.add(5, 'minute')) {
    const next = cursor.add(5, 'minute')
    const bucket = records.filter((item) => {
      return !item.timeValue.isBefore(cursor) && item.timeValue.isBefore(next)
    })
    const row: Record<string, string | number | null> = {
      collectTime: cursor.format('YYYY-MM-DD HH:mm:ss')
    }
    activeTrendGroup.value.fields.forEach((field) => {
      row[field.key] = average(bucket.map((item) => normalizeNumber(item[field.key])).filter(isNumber))
    })
    rows.push(row)
  }
  return rows
})

const dailyTableRows = computed<DailyRow[]>(() => {
  const range = trendQuery.collectTime
  if (!range?.[0] || !range?.[1]) {
    return []
  }
  const start = dayjs(range[0]).startOf('day')
  const end = dayjs(range[1]).startOf('day')
  if (!start.isValid() || !end.isValid() || end.isBefore(start)) {
    return []
  }
  const statMap = new Map(dailyStatList.value.map((item) => [String(item.date || ''), item]))
  const rows: DailyRow[] = []
  for (let cursor = start; !cursor.isAfter(end); cursor = cursor.add(1, 'day')) {
    const date = cursor.format('YYYY-MM-DD')
    const stat = statMap.get(date)
    rows.push({
      date,
      max: normalizeNumber(stat?.max),
      maxTime: stat?.maxTime,
      min: normalizeNumber(stat?.min),
      minTime: stat?.minTime,
      avg: normalizeNumber(stat?.avg)
    })
  }
  return rows
})

const realtimeHistoryRows = computed(() => {
  const rows: Array<Record<string, string | number | null>> = []
  if (!realtimeHistoryDate.value) {
    return rows
  }
  const start = dayjs(`${realtimeHistoryDate.value} 00:00:00`)
  const end = dayjs(`${realtimeHistoryDate.value} 23:59:59`)
  if (!start.isValid() || !end.isValid()) {
    return rows
  }

  const records = realtimeHistoryRawList.value
    .filter((item) => item.collectTime)
    .map((item) => ({ ...item, timeValue: dayjs(item.collectTime as string) }))
    .filter((item) => item.timeValue.isValid())

  for (let cursor = start; !cursor.isAfter(end); cursor = cursor.add(5, 'minute')) {
    const next = cursor.add(5, 'minute')
    const bucket = records.filter((item) => {
      return !item.timeValue.isBefore(cursor) && item.timeValue.isBefore(next)
    })
    const row: Record<string, string | number | null> = {
      collectTime: cursor.format('YYYY-MM-DD HH:mm:ss')
    }
    activeRealtimeHistoryGroup.value.fields.forEach((field) => {
      row[field.key] = average(bucket.map((item) => normalizeNumber(item[field.key])).filter(isNumber))
    })
    rows.push(row)
  }
  return rows
})

const detailRows = computed<DetailRow[]>(() => {
  if (!detailQuery.date || !detailQuery.startTime || !detailQuery.endTime) {
    return []
  }
  const rows: DetailRow[] = []
  const start = dayjs(`${detailQuery.date} ${detailQuery.startTime}`)
  const end = dayjs(`${detailQuery.date} ${detailQuery.endTime}`)
  if (!start.isValid() || !end.isValid() || !end.isAfter(start)) {
    return rows
  }
  const records = detailRawList.value
    .filter((item) => item.collectTime)
    .map((item) => ({ ...item, timeValue: dayjs(item.collectTime as string) }))
    .filter((item) => item.timeValue.isValid())

  for (let cursor = start; !cursor.isAfter(end); cursor = cursor.add(detailQuery.intervalMinutes, 'minute')) {
    const next = cursor.add(detailQuery.intervalMinutes, 'minute')
    const bucket = records.filter((item) => {
      return !item.timeValue.isBefore(cursor) && item.timeValue.isBefore(next)
    })
    const row: DetailRow = { time: cursor.format('HH:mm') }
    activeDetailGroup.value.fields.forEach((field) => {
      row[field.key] = average(bucket.map((item) => normalizeNumber(item[field.key])).filter(isNumber))
    })
    rows.push(row)
  }
  return rows
})

const loadDevices = async () => {
  loading.value = true
  try {
    const data = await EnergyDeviceApi.getDevicePage({ pageNo: 1, pageSize: 100 })
    devices.value = data?.list || []
    if (!selectedDeviceId.value && devices.value[0]?.id) {
      selectedDeviceId.value = devices.value[0].id
      trendQuery.deviceId = devices.value[0].id
      detailQuery.deviceId = devices.value[0].id
    }
    ensureBillScopeSelection()
  } catch (error) {
    message.error('电表数据加载失败，请检查后端服务和登录状态')
  } finally {
    loading.value = false
  }
}

const loadLatestTelemetry = async () => {
  if (!selectedDeviceId.value) {
    latestTelemetry.value = undefined
    return
  }
  latestLoading.value = true
  try {
    const data = await EnergyTelemetryApi.getTelemetryPage({
      pageNo: 1,
      pageSize: 1,
      deviceId: selectedDeviceId.value
    })
    latestTelemetry.value = data?.list?.[0]
  } catch (error) {
    message.error('电表实时详情加载失败')
  } finally {
    latestLoading.value = false
  }
}

const loadBillReport = async () => {
  ensureBillScopeSelection()
  if (!billQuery.billMonth || selectedBillDevices.value.length === 0) {
    billReport.value = undefined
    billTelemetryRows.value = []
    chargeSessionRows.value = []
    pricingRuleRows.value = []
    return
  }
  billLoading.value = true
  try {
    const telemetryParams: Record<string, unknown> = {
      collectTime: [billRange.value.start, billRange.value.end],
      limit: 5000
    }
    if (billQuery.scopeType === 'device' && billQuery.deviceId) {
      telemetryParams.deviceId = billQuery.deviceId
    }
    const reportParams = {
      scopeType: billQuery.scopeType,
      projectId: billQuery.scopeType === 'project' ? billQuery.projectId : undefined,
      deviceId: billQuery.scopeType === 'device' ? billQuery.deviceId : undefined,
      billMonth: billQuery.billMonth
    }
    const [telemetryRows, chargeSessions, pricingRules, report] = await Promise.all([
      EnergyTelemetryApi.getTelemetryChart(telemetryParams),
      EnergyChargeSessionApi.getChargeSessionPage({ pageNo: 1, pageSize: 5000 }),
      EnergyPricingRuleApi.getPricingRulePage({ pageNo: 1, pageSize: 5000 }),
      EnergyReportApi.getBillReport(reportParams)
    ])
    billTelemetryRows.value = telemetryRows || []
    chargeSessionRows.value = chargeSessions?.list || []
    pricingRuleRows.value = pricingRules?.list || []
    billReport.value = report
  } catch (error) {
    billReport.value = undefined
    billTelemetryRows.value = []
    chargeSessionRows.value = []
    pricingRuleRows.value = []
    message.error('用电量报表加载失败，请检查遥测、任务和计费规则接口')
  } finally {
    billLoading.value = false
  }
}

const loadTrendData = async () => {
  if (!trendQuery.deviceId || !trendQuery.collectTime?.length) {
    return
  }
  trendLoading.value = true
  try {
    if (dataTab.value === 'raw') {
      trendRawList.value = await EnergyTelemetryApi.getTelemetryChart({
        deviceId: trendQuery.deviceId,
        collectTime: trendQuery.collectTime,
        limit: 3000
      })
    } else {
      dailyStatList.value = await EnergyTelemetryApi.getTelemetryDailyStat({
        deviceId: trendQuery.deviceId,
        collectTime: trendQuery.collectTime,
        metric: trendQuery.metric
      })
    }
  } catch (error) {
    message.error('数据查看加载失败')
  } finally {
    trendLoading.value = false
  }
}

const loadDetailData = async () => {
  if (!detailQuery.deviceId || !detailQuery.date) {
    return
  }
  detailLoading.value = true
  try {
    detailRawList.value = await EnergyTelemetryApi.getTelemetryChart({
      deviceId: detailQuery.deviceId,
      collectTime: [
        `${detailQuery.date} ${detailQuery.startTime}`,
        `${detailQuery.date} ${detailQuery.endTime}`
      ],
      limit: 3000
    })
  } catch (error) {
    message.error('详细实时数据加载失败')
  } finally {
    detailLoading.value = false
  }
}

const loadRealtimeHistoryData = async () => {
  if (!selectedDeviceId.value || !realtimeHistoryDate.value) {
    realtimeHistoryRawList.value = []
    return
  }
  realtimeHistoryLoading.value = true
  try {
    realtimeHistoryRawList.value = await EnergyTelemetryApi.getTelemetryChart({
      deviceId: selectedDeviceId.value,
      collectTime: [
        `${realtimeHistoryDate.value} 00:00:00`,
        `${realtimeHistoryDate.value} 23:59:59`
      ],
      limit: 3000
    })
  } catch (error) {
    message.error('历史曲线加载失败')
  } finally {
    realtimeHistoryLoading.value = false
  }
}

const loadRealtimeAlarmData = async () => {
  if (!selectedDeviceId.value) {
    realtimeAlarmList.value = []
    realtimeAlarmTotal.value = 0
    return
  }
  realtimeAlarmLoading.value = true
  try {
    const data = await EnergyAlarmApi.getAlarmPage({
      pageNo: 1,
      pageSize: 10,
      deviceId: selectedDeviceId.value
    })
    realtimeAlarmList.value = data?.list || []
    realtimeAlarmTotal.value = data?.total || 0
  } catch (error) {
    message.error('报警信息加载失败')
  } finally {
    realtimeAlarmLoading.value = false
  }
}

const loadRealtimeLowerData = async () => {
  if (realtimeDetailTab.value === 'alarm') {
    await loadRealtimeAlarmData()
    return
  }
  await loadRealtimeHistoryData()
}

const loadAll = async () => {
  await loadDevices()
  await Promise.all([loadLatestTelemetry(), loadBillReport(), loadTrendData(), loadDetailData()])
}

const selectDevice = async (device: EnergyDeviceVO) => {
  selectedDeviceId.value = device.id
  if (billQuery.scopeType === 'device') {
    billQuery.deviceId = device.id
  }
  trendQuery.deviceId = device.id
  detailQuery.deviceId = device.id
  await Promise.all([loadLatestTelemetry(), loadBillReport(), loadTrendData(), loadDetailData()])
}

const handleBillScopeChange = async () => {
  ensureBillScopeSelection()
  await loadBillReport()
}

const handleBillDeviceChange = async (deviceId?: number) => {
  selectedDeviceId.value = deviceId
  trendQuery.deviceId = deviceId
  detailQuery.deviceId = deviceId
  await Promise.all([loadLatestTelemetry(), loadBillReport()])
}

const openRealtimeDetail = async (device: EnergyDeviceVO) => {
  await selectDevice(device)
  realtimeDetailVisible.value = true
  await Promise.all([loadRealtimeHistoryData(), loadRealtimeAlarmData()])
}

const handleDeviceControl = async (method: 'SWITCH' | 'REFRESH', switchValue?: '0' | '1') => {
  if (!selectedDevice.value?.id || controlLoading.value) {
    return
  }
  const actionText =
    method === 'REFRESH' ? '刷新设备状态' : switchValue === '1' ? '合闸' : '开闸'
  await message.confirm(`确认对 ${selectedDevice.value.deviceName || selectedDevice.value.deviceNo || '当前电表'} 执行${actionText}吗？`)
  controlLoading.value = true
  try {
    const result = await EnergyDeviceApi.controlDevice({
      deviceId: selectedDevice.value.id,
      method,
      value: method === 'SWITCH' ? { Switch: switchValue } : undefined,
      remark: actionText
    })
    if (result.success) {
      message.success(result.message || `${actionText}指令已下发`)
      await loadLatestTelemetry()
      return
    }
    message.warning(result.message || `${actionText}指令下发失败`)
  } finally {
    controlLoading.value = false
  }
}

const getMetricGroup = (value: MetricGroupValue) => {
  return metricGroups.find((item) => item.value === value) || metricGroups[0]
}

const getDeviceOptionLabel = (device: EnergyDeviceVO) => {
  return `${device.deviceName || device.deviceNo || '未命名电表'} / ${device.meterNo || device.gatewaySn || '-'}`
}

const ensureBillScopeSelection = () => {
  if (billQuery.scopeType === 'all') {
    billQuery.projectId = undefined
    billQuery.deviceId = undefined
    return
  }
  if (billQuery.scopeType === 'project') {
    billQuery.deviceId = undefined
    const hasProject = billProjectOptions.value.some((item) => Number(item.value) === Number(billQuery.projectId))
    if (!hasProject) {
      billQuery.projectId = billProjectOptions.value[0]?.value
    }
    return
  }
  const hasDevice = devices.value.some((device) => Number(device.id) === Number(billQuery.deviceId))
  if (!hasDevice) {
    billQuery.deviceId = devices.value[0]?.id
  }
}

const isDeviceInSelectedNode = (device: EnergyDeviceVO, selectedNode: string) => {
  const [type, id] = selectedNode.split(':')
  if (type === 'customer') {
    return String(device.customerId || '') === id
  }
  if (type === 'project') {
    return String(device.projectId || '') === id
  }
  if (type === 'device') {
    return String(device.id || '') === id
  }
  return true
}

const setStatusFilter = (value: number) => {
  statusFilter.value = statusFilter.value === value ? undefined : value
}

const getStatusText = (status?: number) => {
  const statusMap: Record<number, string> = {
    0: '在线',
    1: '离线',
    2: '故障',
    3: '维护'
  }
  return status === undefined || status === null ? '未知' : statusMap[status] || `状态${status}`
}

const getStatusType = (status?: number) => {
  if (status === 0) {
    return 'success'
  }
  if (status === 2) {
    return 'danger'
  }
  if (status === 3) {
    return 'warning'
  }
  return 'info'
}

const getDeviceStatusClass = (status?: number) => {
  if (status === 0) {
    return 'is-online'
  }
  if (status === 2) {
    return 'is-fault'
  }
  if (status === 3) {
    return 'is-alarm'
  }
  return 'is-offline'
}

const getRealtimeStateText = (state?: string, status?: number) => {
  if (state === 'ONLINE') {
    return '在线'
  }
  if (state === 'OFFLINE') {
    return '离线'
  }
  return getStatusText(status)
}

const getRealtimeStateTagType = (state?: string, status?: number) => {
  if (state === 'ONLINE') {
    return 'success'
  }
  if (state === 'OFFLINE') {
    return 'info'
  }
  return getStatusType(status)
}

const formatValue = (value?: number | string | Date, unit = '') => {
  const number = normalizeNumber(value)
  if (number === null) {
    return '--'
  }
  return `${formatNumber(number)}${unit ? ` ${unit}` : ''}`
}

const formatCellValue = (value?: number | null) => {
  return value === undefined || value === null ? '--' : formatNumber(value)
}

const formatNumber = (value: number) => {
  return Number(value.toFixed(4)).toString()
}

const formatPercent = (value: number, total: number) => {
  if (!total) {
    return '0%'
  }
  return `${Number(((value / total) * 100).toFixed(1))}%`
}

const normalizeNumber = (value: unknown): number | null => {
  if (value === undefined || value === null || value === '') {
    return null
  }
  const number = Number(value)
  return Number.isFinite(number) ? number : null
}

const isNumber = (value: number | null): value is number => value !== null

const average = (values: number[]) => {
  if (!values.length) {
    return null
  }
  return values.reduce((sum, item) => sum + item, 0) / values.length
}

const averageNumber = (values: Array<number | null>) => {
  const valid = values.filter(isNumber)
  if (!valid.length) return 0
  return Number((valid.reduce((sum, item) => sum + item, 0) / valid.length).toFixed(2))
}

const firstNumber = (values: Array<number | null>) => values.find((value) => value !== null) ?? null
const lastNumber = (values: Array<number | null>) => [...values].reverse().find((value) => value !== null) ?? null

const groupTelemetryByDevice = (rows: EnergyTelemetryVO[]) => {
  const groups = new Map<number, EnergyTelemetryVO[]>()
  rows.forEach((row) => {
    const deviceId = normalizeNumber(row.deviceId)
    if (deviceId === null) {
      return
    }
    if (!groups.has(deviceId)) {
      groups.set(deviceId, [])
    }
    groups.get(deviceId)!.push(row)
  })
  groups.forEach((items) => {
    items.sort((a, b) => dayjs(a.collectTime as string).valueOf() - dayjs(b.collectTime as string).valueOf())
  })
  return groups
}

const calculateEpiBoundary = (rows: EnergyTelemetryVO[], boundary: 'start' | 'end') => {
  const groups = groupTelemetryByDevice(rows)
  let total = 0
  let hasValue = false
  groups.forEach((items) => {
    const values = items.map((item) => normalizeNumber(item.epi))
    const value = boundary === 'start' ? firstNumber(values) : lastNumber(values)
    if (value !== null) {
      total += value
      hasValue = true
    }
  })
  return hasValue ? Number(total.toFixed(2)) : null
}

const touEnergyFieldMap: Record<TouSource, Record<TouKey, keyof EnergyTelemetryVO | null>> = {
  epi: {
    sharpPeak: 'epij',
    peak: 'epif',
    flat: 'epip',
    valley: 'epig',
    deepValley: null
  },
  epe: {
    sharpPeak: 'epej',
    peak: 'epef',
    flat: 'epep',
    valley: 'epeg',
    deepValley: null
  }
}

const touPeriodLabels: Record<TouKey, string> = {
  sharpPeak: '尖峰',
  peak: '高峰',
  flat: '平时',
  valley: '低谷',
  deepValley: '深谷'
}

const calculateTelemetryDeltaInRows = (rows: EnergyTelemetryVO[], field: keyof EnergyTelemetryVO) => {
  const groups = groupTelemetryByDevice(rows)
  let total = 0
  groups.forEach((items) => {
    const values = items.map((item) => normalizeNumber(item[field]))
    const start = firstNumber(values)
    const end = lastNumber(values)
    if (start !== null && end !== null) {
      total += Math.max(0, end - start)
    }
  })
  return Number(total.toFixed(2))
}

const calculateTelemetryDelta = (field: keyof EnergyTelemetryVO) => {
  return calculateTelemetryDeltaInRows(sortedBillTelemetry.value, field)
}

const buildTouEnergy = (source: TouSource): TouEnergy => {
  return sumIntervalTou(billTelemetryIntervals.value, source)
}

const buildTouEnergyFromRows = (rows: EnergyTelemetryVO[], source: TouSource): TouEnergy => {
  return sumIntervalTou(buildTelemetryIntervals(rows), source)
}

const emptyTouEnergy = (): TouEnergy => ({
  sharpPeak: 0,
  peak: 0,
  flat: 0,
  valley: 0,
  deepValley: 0
})

const normalizeReportTou = (energy?: Partial<TouEnergy>): TouEnergy => ({
  sharpPeak: normalizeNumber(energy?.sharpPeak) || 0,
  peak: normalizeNumber(energy?.peak) || 0,
  flat: normalizeNumber(energy?.flat) || 0,
  valley: normalizeNumber(energy?.valley) || 0,
  deepValley: normalizeNumber(energy?.deepValley) || 0
})

const sumNullableReportDeviceField = (field: 'startEpi' | 'endEpi') => {
  let total = 0
  let hasValue = false
  ;(billReport.value?.deviceDetails || []).forEach((item) => {
    const value = normalizeNumber(item[field])
    if (value !== null) {
      total += value
      hasValue = true
    }
  })
  return hasValue ? Number(total.toFixed(2)) : null
}

const buildTelemetryIntervals = (rows: EnergyTelemetryVO[]): TelemetryInterval[] => {
  const intervals: TelemetryInterval[] = []
  const groups = groupTelemetryByDevice(rows)
  groups.forEach((items, deviceId) => {
    for (let index = 1; index < items.length; index += 1) {
      const prev = items[index - 1]
      const curr = items[index]
      if (!prev.collectTime || !curr.collectTime) continue
      const chargeTotal = positiveDelta(prev.epi, curr.epi)
      const dischargeTotal = positiveDelta(prev.epe, curr.epe)
      intervals.push({
        deviceId,
        startTime: prev.collectTime,
        endTime: curr.collectTime,
        chargeTotal,
        dischargeTotal,
        chargeTou: buildIntervalTou(prev, curr, 'epi', chargeTotal),
        dischargeTou: buildIntervalTou(prev, curr, 'epe', dischargeTotal)
      })
    }
  })
  return intervals
}

const buildIntervalTou = (prev: EnergyTelemetryVO, curr: EnergyTelemetryVO, source: TouSource, total: number): TouEnergy => {
  if (total <= 0) return emptyTouEnergy()
  const payloadTou = payloadTouDelta(prev, curr, source, total)
  if (payloadTou) return payloadTou
  return splitEnergyByPricingTime(curr.deviceId, prev.collectTime, curr.collectTime, total)
}

const payloadTouDelta = (prev: EnergyTelemetryVO, curr: EnergyTelemetryVO, source: TouSource, total: number): TouEnergy | null => {
  const fields = touEnergyFieldMap[source]
  const energy = emptyTouEnergy()
  let hasPart = false
  ;(Object.keys(fields) as TouKey[]).forEach((key) => {
    const field = fields[key]
    if (!field) return
    const delta = positiveDelta(prev[field], curr[field])
    if (delta > 0) hasPart = true
    energy[key] = delta
  })
  if (!hasPart) return null
  const partTotal = sumTouEnergy(energy)
  const tolerance = Math.max(0.02, total * 0.02)
  return Math.abs(partTotal - total) <= tolerance ? energy : null
}

const splitEnergyByPricingTime = (
  deviceId: number | undefined,
  startValue: string | Date | undefined,
  endValue: string | Date | undefined,
  total: number
): TouEnergy => {
  const energy = emptyTouEnergy()
  const start = dayjs(startValue)
  const end = dayjs(endValue)
  if (!start.isValid() || !end.isValid() || !end.isAfter(start)) {
    energy.flat = total
    return energy
  }
  const periods = parseTouPeriods(matchPricingRuleForDevice(deviceId)?.touPeriods)
  const totalSeconds = end.diff(start, 'second')
  if (totalSeconds <= 0) {
    energy.flat = total
    return energy
  }
  let cursor = start
  while (cursor.isBefore(end)) {
    const nextMinute = cursor.add(1, 'minute').startOf('minute')
    const next = nextMinute.isAfter(cursor) && nextMinute.isBefore(end) ? nextMinute : end
    const seconds = Math.max(0, next.diff(cursor, 'second'))
    const key = resolveTouKey(cursor, periods)
    energy[key] += total * (seconds / totalSeconds)
    cursor = next
  }
  return roundTouEnergy(energy)
}

const matchPricingRuleForDevice = (deviceId?: number) => {
  const device = devices.value.find((item) => Number(item.id) === Number(deviceId))
  if (!device) return undefined
  const customerId = Number(device.customerId)
  const projectId = Number(device.projectId)
  return pricingRuleRows.value
    .filter((rule) => Number(rule.status) === 0)
    .filter((rule) => {
      if (rule.deviceId) return Number(rule.deviceId) === Number(device.id)
      if (rule.projectId) return Number(rule.projectId) === projectId
      if (rule.customerId) return Number(rule.customerId) === customerId
      return false
    })
    .sort((a, b) => pricingRulePriority(b, device) - pricingRulePriority(a, device) || Number(b.id || 0) - Number(a.id || 0))[0]
}

const pricingRulePriority = (rule: EnergyPricingRuleVO, device: EnergyDeviceVO) => {
  if (rule.deviceId && Number(rule.deviceId) === Number(device.id)) return 3
  if (rule.projectId && Number(rule.projectId) === Number(device.projectId)) return 2
  if (rule.customerId && Number(rule.customerId) === Number(device.customerId)) return 1
  return 0
}

const parseTouPeriods = (value?: string): TouPeriod[] => {
  if (!value) return []
  try {
    const rows = JSON.parse(value)
    return Array.isArray(rows)
      ? rows.filter((item) => item?.type && item?.start && item?.end)
      : []
  } catch {
    return []
  }
}

const resolveTouKey = (time: dayjs.Dayjs, periods: TouPeriod[]): TouKey => {
  const minute = time.hour() * 60 + time.minute()
  const matched = periods.find((period) => isMinuteInPeriod(minute, period))
  return matched?.type || 'flat'
}

const isMinuteInPeriod = (minute: number, period: TouPeriod) => {
  const start = timeToMinute(period.start)
  const end = timeToMinute(period.end)
  if (start === end) return false
  return start < end ? minute >= start && minute < end : minute >= start || minute < end
}

const timeToMinute = (value: string) => {
  const [hour, minute] = value.split(':').map(Number)
  return Number(hour || 0) * 60 + Number(minute || 0)
}

const positiveDelta = (previous: unknown, current: unknown) => {
  const start = normalizeNumber(previous)
  const end = normalizeNumber(current)
  return start !== null && end !== null ? Number(Math.max(0, end - start).toFixed(4)) : 0
}

const roundTouEnergy = (energy: TouEnergy): TouEnergy => ({
  sharpPeak: Number(energy.sharpPeak.toFixed(4)),
  peak: Number(energy.peak.toFixed(4)),
  flat: Number(energy.flat.toFixed(4)),
  valley: Number(energy.valley.toFixed(4)),
  deepValley: Number(energy.deepValley.toFixed(4))
})

const sumIntervals = (intervals: TelemetryInterval[], key: 'chargeTotal' | 'dischargeTotal') => {
  return Number(intervals.reduce((sum, item) => sum + item[key], 0).toFixed(2))
}

const countChargeCycles = (intervals: TelemetryInterval[]) => {
  let cycles = 0
  let canStartNextChargeCycle = true
  intervals.forEach((interval) => {
    if (interval.chargeTotal > 0.0001 && canStartNextChargeCycle) {
      cycles += 1
      canStartNextChargeCycle = false
    }
    if (interval.dischargeTotal > 0.0001) {
      canStartNextChargeCycle = true
    }
  })
  return cycles
}

const sumIntervalTou = (intervals: TelemetryInterval[], source: TouSource): TouEnergy => {
  const energy = emptyTouEnergy()
  intervals.forEach((interval) => {
    const sourceEnergy = source === 'epi' ? interval.chargeTou : interval.dischargeTou
    ;(Object.keys(energy) as TouKey[]).forEach((key) => {
      energy[key] += sourceEnergy[key]
    })
  })
  return roundTouEnergy(energy)
}

const sumTouEnergy = (energy: TouEnergy) => {
  return Number(
    (energy.sharpPeak + energy.peak + energy.flat + energy.valley + energy.deepValley).toFixed(2)
  )
}

const consistencyText = (partTotal: number, total: number, totalLabel: string) => {
  if (total <= 0 && partTotal <= 0) {
    return '无数据'
  }
  if (total > 0 && partTotal <= 0) {
    return '分项缺失'
  }
  const diff = Number(Math.abs(partTotal - total).toFixed(2))
  const tolerance = Math.max(0.01, total * 0.001)
  if (diff <= tolerance) {
    return `一致（${totalLabel}）`
  }
  return `差异 ${formatKwh(diff)}`
}

const calculateTouAmount = (energy: TouEnergy) => {
  const rates = touRates.value
  return Number(
    (
      energy.sharpPeak * rates.sharpPeak +
      energy.peak * rates.peak +
      energy.flat * rates.flat +
      energy.valley * rates.valley +
      energy.deepValley * rates.deepValley
    ).toFixed(2)
  )
}

const uniqueRowsById = <T extends { id?: number }>(rows: T[]) => {
  const map = new Map<string | number, T>()
  rows.forEach((row, index) => {
    map.set(row.id ?? `row-${index}`, row)
  })
  return Array.from(map.values())
}

const sumPricingRuleField = (field: keyof EnergyPricingRuleVO) => {
  return Number(
    uniqueApplicablePricingRules.value
      .reduce((sum, rule) => sum + Number(rule[field] || 0), 0)
      .toFixed(2)
  )
}

const sumBy = <T extends Record<string, any>>(rows: T[], key: keyof T) => {
  return rows.reduce((sum, item) => sum + Number(item[key] || 0), 0)
}

const sumDaily = (rows: EnergyChargeSessionVO[], day: dayjs.Dayjs, key: keyof EnergyChargeSessionVO) => {
  return Number(
    rows
      .filter((item) => dayjs(item.startTime || item.createTime).isSame(day, 'day'))
      .reduce((sum, item) => sum + Number(item[key] || 0), 0)
      .toFixed(2)
  )
}

const numberText = (value: number) => {
  return Number.isInteger(value) ? String(value) : value.toFixed(2)
}

const formatKwh = (value: number) => `${numberText(value)} kWh`
const formatNullableKwh = (value: number | null) => (value === null ? '--' : formatKwh(value))
const formatCurrency = (value: number) => `¥${numberText(value)}`
const formatPricingFee = (value: number) => {
  return uniqueApplicablePricingRules.value.length > 0 ? formatCurrency(value) : '待录入'
}

const formatTouRate = (value: number) => {
  return uniqueApplicablePricingRules.value.length > 0 ? `${numberText(value)} 元/kWh` : '待录入'
}

const formatTouAmount = (energy: number, rate: number) => {
  return uniqueApplicablePricingRules.value.length > 0 ? formatCurrency(Number((energy * rate).toFixed(2))) : '待录入'
}

const reportTouCostRows = (category: string, component: string) => {
  const rows = billReport.value?.feeDetails
    ?.filter((row) => row.category === category && row.component === component)
    ?.map((row) => ({
      period: `${row.period || '--'}充电`,
      energy: formatKwh(Number(row.billingEnergy || 0)),
      rate: row.rate === null || row.rate === undefined ? '待录入' : `${numberText(Number(row.rate))} 元/kWh`,
      amount: row.amount === null || row.amount === undefined ? '待录入' : formatCurrency(Number(row.amount))
    }))
  return rows?.length ? rows : null
}

const touRows = (energy: TouEnergy, type: 'charge' | 'discharge') => {
  const rates = touRates.value
  return (Object.keys(touPeriodLabels) as TouKey[]).map((key) => ({
    period: `${touPeriodLabels[key]}${type === 'charge' ? '充电' : '放电'}`,
    energy: formatKwh(energy[key]),
    rate: formatTouRate(rates[key]),
    amount: formatTouAmount(energy[key], rates[key])
  }))
}

const exportRawData = () => {
  const headers = [
    { label: '采集时间', key: 'collectTime' },
    ...activeTrendGroup.value.fields.map((field) => ({
      label: `${field.label}${field.unit ? `(${field.unit})` : ''}`,
      key: field.key
    }))
  ]
  downloadCsv(`日原始数据_${dayjs().format('YYYYMMDDHHmmss')}.csv`, headers, rawTableRows.value)
}

const exportDailyData = () => {
  const metric = dailyMetric.value
  const unit = metric?.unit ? `(${metric.unit})` : ''
  downloadCsv(
    `逐日极值数据_${dayjs().format('YYYYMMDDHHmmss')}.csv`,
    [
      { label: '采集时间', key: 'date' },
      { label: `最大值${unit}`, key: 'max' },
      { label: '最大值发生时间', key: 'maxTime' },
      { label: `最小值${unit}`, key: 'min' },
      { label: '最小值发生时间', key: 'minTime' },
      { label: `平均值${unit}`, key: 'avg' }
    ],
    dailyTableRows.value
  )
}

const downloadCsv = (
  filename: string,
  headers: Array<{ label: string; key: string }>,
  rows: Array<Record<string, unknown>>
) => {
  const content = [
    headers.map((item) => escapeCsvCell(item.label)).join(','),
    ...rows.map((row) =>
      headers
        .map((header) => {
          const value = row[header.key]
          return escapeCsvCell(value === undefined || value === null || value === '' ? '--' : value)
        })
        .join(',')
    )
  ].join('\n')
  const blob = new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' })
  void archiveReport(filename, blob, '实时监控导出').catch(() => undefined)
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

const escapeCsvCell = (value: unknown) => {
  const text = String(value).replace(/"/g, '""')
  return `"${text}"`
}

const formatDateTime = (value?: string | Date) => {
  if (!value) {
    return '--'
  }
  const date = dayjs(value)
  return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : '--'
}

const formatAxisTime = (value?: string | Date) => {
  if (!value) {
    return ''
  }
  const date = dayjs(value)
  return date.isValid() ? date.format('MM-DD HH:mm') : ''
}

const buildLineOptions = ({
  xData,
  series,
  yName
}: {
  xData: string[]
  series: Array<{ name: string; data: Array<number | null> }>
  yName: string
}): EChartsOption => ({
  color: ['#0ea5a4', '#2563eb', '#f59e0b', '#7c3aed', '#dc2626'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { left: 48, right: 24, top: 48, bottom: 36 },
  xAxis: { type: 'category', data: xData, boundaryGap: false },
  yAxis: { type: 'value', name: yName },
  series: series.map((item) => ({
    name: item.name,
    type: 'line',
    smooth: true,
    showSymbol: false,
    connectNulls: false,
    data: item.data
  }))
})

watch(
  () => trendQuery.metricGroup,
  () => {
    trendQuery.metric = activeTrendGroup.value.fields[0].key
  }
)

watch(
  () => nodeTypeFilter.value,
  () => {
    nodeFilter.value = undefined
  }
)

onMounted(() => {
  loadAll()
})
</script>

<style lang="scss" scoped>
.energy-telemetry {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.monitor-workbench {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  min-height: calc(100vh - 190px);
  background: #ffffff;
  border: 1px solid #edf2f4;
  border-radius: 6px;
}

.monitor-sidebar {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #ffffff;
  border-right: 1px solid #edf2f4;

  &__switch {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    min-height: 46px;
    padding: 0 12px 0 14px;
    background: #e7fbfb;

    strong {
      overflow: hidden;
      color: #0f172a;
      font-size: 14px;
      font-weight: 800;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .el-button {
      --el-button-bg-color: #0bb5b1;
      --el-button-border-color: #0bb5b1;
      --el-button-hover-bg-color: #079d9a;
      --el-button-hover-border-color: #079d9a;
      min-width: 56px;
      height: 30px;
      padding: 0 14px;
      font-weight: 700;
    }
  }

  &__collapse {
    align-self: flex-end;
    margin: 10px 18px 0 0;
    padding: 0;
    color: #8a94a3;
    font-size: 13px;
    cursor: pointer;
    background: transparent;
    border: 0;
  }

  &__avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 82px;
    height: 82px;
    margin: 10px auto 0;
    color: #0fb8b2;
    background: linear-gradient(180deg, #dffafa 0%, #f7ffff 100%);
    border-radius: 50%;
  }

  &__identity {
    display: grid;
    gap: 10px;
    justify-items: center;
    margin: 18px 18px 22px;
    text-align: center;

    strong {
      color: #0f172a;
      font-size: 14px;
      line-height: 20px;
    }

    span {
      color: #4b5563;
      font-size: 13px;
      line-height: 18px;
    }
  }

  &__status {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px 34px;
    padding: 22px 42px;
    border-top: 1px solid #edf2f4;
    border-bottom: 1px solid #edf2f4;
  }

  &__metrics {
    display: grid;
    gap: 10px;
    padding: 16px 14px;
  }
}

.monitor-status-cell {
  display: grid;
  gap: 4px;
  justify-items: start;
  padding: 0;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 0;

  span {
    position: relative;
    padding-left: 12px;
    color: #111827;
    font-size: 13px;
    font-weight: 700;

    &::before {
      position: absolute;
      top: 6px;
      left: 0;
      width: 7px;
      height: 7px;
      content: '';
      border-radius: 2px;
    }
  }

  strong {
    padding-left: 12px;
    color: #0f172a;
    font-size: 15px;
    line-height: 20px;

    &::after {
      margin-left: 4px;
      color: #94a3b8;
      font-weight: 400;
      content: '个';
    }
  }

  &.is-normal span::before {
    background: #0bb5b1;
  }

  &.is-offline span::before {
    background: #a7a9ac;
  }

  &.is-fault span::before {
    background: #e4b20a;
  }

  &.is-alarm span::before {
    background: #ef3338;
  }
}

.monitor-metric-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 66px;
  padding: 10px 12px 10px 14px;
  color: #0bb5b1;
  background: #ffffff;
  border: 1px solid #edf2f4;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgb(15 23 42 / 12%);

  div {
    display: grid;
    gap: 4px;
    justify-items: end;
  }

  span {
    color: #6b7280;
    font-size: 13px;
  }

  strong {
    color: #006a6a;
    font-size: 15px;
    line-height: 20px;
  }
}

.monitor-main {
  min-width: 0;
  padding: 16px 14px 14px;
}

.monitor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 16px;

  &__status,
  &__filters {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
  }

  &__status .el-button {
    min-width: 72px;
    height: 36px;
    color: #ffffff;
    font-size: 14px;
    font-weight: 800;
    border: 0;
    border-radius: 4px;

    &.is-normal {
      background: #0bb5b1;
    }

    &.is-offline {
      background: #9fa1a4;
    }

    &.is-fault {
      background: #e1af08;
    }

    &.is-alarm {
      background: #ef3338;
    }

    &.is-selected {
      box-shadow: 0 0 0 3px rgb(11 181 177 / 18%);
    }
  }

  &__filters {
    justify-content: flex-end;

    .el-button {
      height: 34px;
      font-size: 13px;
      font-weight: 700;
      border-radius: 3px;
    }
  }
}

.monitor-pagination {
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 16px;
  color: #1f2937;
  font-size: 13px;

  strong {
    color: #0bb5b1;
    font-size: 14px;
  }
}

.monitor-report-panel {
  .bill-report-panel__header {
    margin-bottom: 12px;
  }
}

.monitor-report {
  &__filters {
    :deep(.el-form-item) {
      margin-bottom: 12px;
    }
  }

  &__status-filter,
  &__view-switch {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .el-button {
      height: 32px;
      margin-left: 0;
      border-radius: 6px;
      font-weight: 700;
    }
  }

  &__kpis {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  &__kpi {
    min-height: 104px;

    strong {
      font-size: 26px;
      line-height: 32px;
    }
  }

  &__section {
    margin-top: 12px;
  }

  &__section-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;

    h3 {
      margin-bottom: 4px;
    }

    span {
      color: #64748b;
      font-size: 13px;
      line-height: 20px;
    }
  }

  &__meter-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }

  &__pagination {
    margin-top: 14px;
    padding-top: 12px;
    border-top: 1px solid #edf2f7;
  }
}

.energy-telemetry__stat {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 86px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;

  span,
  strong {
    display: block;
  }

  span {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  strong {
    margin-top: 8px;
    color: var(--el-text-color-primary);
    font-size: 24px;
    line-height: 30px;
  }
}

.telemetry-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 12px;
    padding: 0 12px;
    background: #ffffff;
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;
  }
}

.telemetry-stats {
  margin-bottom: 14px;
}

.telemetry-panel {
  padding: 16px;
  background: #ffffff;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;

    h2 {
      margin: 0;
      color: var(--el-text-color-primary);
      font-size: 16px;
      line-height: 24px;
    }

    span {
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }
  }
}

.bill-report-panel {
  padding: 16px;
  background:
    radial-gradient(circle at 12% 0, rgba(56, 189, 248, 0.1), transparent 28%),
    radial-gradient(circle at 92% 8%, rgba(34, 197, 94, 0.12), transparent 24%),
    #f8fbff;
  border: 1px solid #e5edf6;
  border-radius: 8px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;

    h2 {
      margin: 0;
      color: #111827;
      font-size: 20px;
      font-weight: 800;
      line-height: 28px;
    }

    span {
      color: #64748b;
      font-size: 14px;
      line-height: 20px;
    }
  }
}

.bill-report {
  &__filters {
    margin-bottom: 12px;
    padding: 12px 12px 0;
    background: #ffffff;
    border: 1px solid #e5edf6;
    border-radius: 8px;
  }

  &__scope-summary {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
    padding: 12px 14px;
    background: #eaf4ff;
    border: 1px solid #d8e7f5;
    border-radius: 8px;

    strong {
      color: #0f172a;
      font-size: 18px;
      font-weight: 800;
      line-height: 24px;
    }

    span {
      color: #334155;
      font-size: 14px;
      font-weight: 600;
      line-height: 20px;
    }
  }

  &__kpis {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 12px;
  }

  &__kpi {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 112px;
    padding: 16px 18px;
    overflow: hidden;
    background:
      radial-gradient(circle at 100% 100%, rgba(34, 197, 94, 0.16), transparent 38%),
      #ffffff;
    border: 1px solid #e5edf6;
    border-radius: 8px;
    box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);

    span,
    strong,
    small {
      display: block;
    }

    span {
      color: #111827;
      font-size: 17px;
      font-weight: 700;
    }

    strong {
      margin-top: 10px;
      color: #000000;
      font-size: 28px;
      font-weight: 800;
      line-height: 34px;
      white-space: nowrap;
    }

    small {
      margin-top: 8px;
      color: #475569;
      font-size: 12px;
      font-weight: 600;
      line-height: 18px;
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1.06fr 1.06fr;
    gap: 12px;
  }

  &__section {
    min-width: 0;
    padding: 14px;
    background: #ffffff;
    border: 1px solid #e5edf6;
    border-radius: 8px;
    box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);

    h3 {
      margin: 0 0 12px;
      color: #111827;
      font-size: 18px;
      font-weight: 800;
    }

    :deep(.el-table th.el-table__cell) {
      color: #1f2937;
      background: #eaf4ff;
      font-weight: 700;
    }
  }

  &__section--tall {
    grid-row: span 2;
  }

  &__list,
  &__profit {
    display: grid;
    gap: 0;

    div {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      min-height: 34px;
      padding: 7px 10px;
      border-bottom: 1px solid #edf2f7;

      &:first-child {
        background: #eaf4ff;
      }
    }

    span {
      color: #1f2937;
      font-weight: 600;
    }

    strong {
      color: #111827;
      font-weight: 700;
      text-align: right;
      white-space: nowrap;
    }
  }

  &__total {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    margin-top: 10px;
    overflow: hidden;
    background: #eaf4ff;
    border-radius: 6px;

    div {
      padding: 12px;
      text-align: center;
      border-right: 1px solid #d8e7f5;

      &:last-child {
        border-right: 0;
      }
    }

    span,
    strong {
      display: block;
    }

    span {
      color: #475569;
      font-size: 13px;
      font-weight: 600;
    }

    strong {
      margin-top: 6px;
      color: #0f172a;
      font-size: 18px;
      font-weight: 800;
    }
  }

  &__split {
    display: grid;
    grid-template-columns: minmax(180px, 0.85fr) minmax(240px, 1.15fr);
    gap: 12px;
    align-items: stretch;
  }

  &__formula {
    margin: -4px 0 10px;
    color: #111827;
    font-weight: 700;
  }

  &__final {
    margin-top: 10px;
    padding: 12px;
    color: #000000;
    background: #eaf4ff;
    border-radius: 6px;
    font-size: 26px;
    font-weight: 900;
    text-align: center;
  }

  &__battery {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 8px;
    margin-bottom: 12px;

    div {
      min-height: 66px;
      padding: 10px;
      background: #eaf4ff;
      border-radius: 6px;
    }

    span,
    strong {
      display: block;
    }

    span {
      color: #475569;
      font-size: 13px;
      font-weight: 600;
    }

    strong {
      margin-top: 6px;
      color: #0f172a;
      font-size: 18px;
      font-weight: 800;
    }
  }

  &__chart-title {
    margin-bottom: 6px;
    color: #111827;
    font-size: 15px;
    font-weight: 700;
  }
}

.meter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 10px;
}

.meter-card {
  position: relative;
  display: grid;
  gap: 10px;
  width: 100%;
  min-height: 132px;
  padding: 16px 64px 14px 18px;
  text-align: left;
  cursor: pointer;
  background:
    radial-gradient(circle at 100% 100%, rgba(56, 189, 248, 0.1), transparent 34%),
    #ffffff;
  border: 1px solid #e5edf6;
  border-radius: 8px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:hover,
  &.is-active {
    border-color: #2088d8;
    box-shadow: 0 10px 24px rgb(15 23 42 / 10%);
  }

  &__name {
    overflow: hidden;
    color: #344054;
    font-size: 14px;
    font-weight: 800;
    line-height: 18px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__status {
    position: absolute;
    top: 12px;
    right: 12px;
    min-width: 44px;
    height: 24px;
    color: #ffffff;
    font-size: 13px;
    font-weight: 800;
    line-height: 24px;
    text-align: center;
    background: #9fa1a4;
    border-radius: 999px;

    &.is-online {
      background: #0bb5b1;
    }

    &.is-fault {
      background: #e1af08;
    }

    &.is-alarm {
      background: #ef3338;
    }
  }

  &__body {
    display: flex;
    gap: 14px;
    align-items: center;
  }

  &__icon {
    display: flex;
    flex: 0 0 40px;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 48px;
    color: #2088d8;
  }

  dl {
    display: flex;
    flex-direction: column;
    gap: 5px;
    margin: 0;
  }

  dl div {
    display: flex;
    gap: 4px;
    min-width: 0;
  }

  dt {
    flex: 0 0 auto;
    color: #344054;
    font-size: 14px;
    font-weight: 800;
    line-height: 19px;
  }

  dd {
    min-width: 0;
    margin: 0;
    overflow-wrap: anywhere;
    color: #344054;
    font-size: 14px;
    line-height: 19px;
  }

  &__detail {
    position: absolute;
    right: 12px;
    bottom: 12px;
    min-width: 44px;
    height: 30px;
    padding: 0 8px;
    color: #2088d8;
    font-size: 14px;
    font-weight: 700;
    background: #ffffff;
    border-color: #b8d8f7;
    border-radius: 6px;
  }
}

.monitor-table {
  :deep(.el-table__header-wrapper th.el-table__cell) {
    color: #5f6b7a;
    background: #f7fafb;
  }
}

:deep(.realtime-detail-dialog) {
  background: #f5f7fa;

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    height: 100%;
    padding: 18px 22px;
  }
}

.realtime-detail {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: calc(100vh - 36px);
  padding: 14px;
  background: #ffffff;
  border: 1px solid #e8edf0;
  border-radius: 4px;

  &__toolbar {
    display: flex;
    gap: 16px;
    align-items: center;
    min-height: 42px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e8edf0;

    .el-button.is-link {
      color: #1f2937;
      font-size: 16px;
      font-weight: 700;
    }

    strong,
    span {
      display: block;
    }

    strong {
      color: #0f172a;
      font-size: 17px;
      line-height: 22px;
    }

    span {
      margin-top: 2px;
      color: #64748b;
      font-size: 12px;
      line-height: 18px;
    }
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
    margin-left: auto;

    .el-button {
      height: 32px;
      margin-left: 0;
      font-size: 13px;
      font-weight: 700;
      border-radius: 4px;
    }
  }

  &__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    color: #0f172a;
    font-size: 15px;

    > div {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }

  h2 {
    margin: 0 0 12px;
    color: #0f172a;
    font-size: 16px;
    font-weight: 800;
    line-height: 22px;
  }
}

.realtime-card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;

  &.is-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.realtime-data-card {
  min-height: 164px;
  background: #ffffff;
  border: 1px solid #d8dde3;

  h3 {
    height: 42px;
    margin: 0;
    padding: 0 14px;
    color: #ffffff;
    font-size: 15px;
    font-weight: 800;
    line-height: 42px;
    background: #0caea9;
  }

  div {
    display: grid;
    gap: 10px;
    padding: 18px 16px;
  }

  span {
    display: flex;
    gap: 8px;
    align-items: baseline;
    min-width: 0;
    color: #111827;
    font-size: 14px;
    line-height: 20px;
  }

  em {
    flex: 0 0 auto;
    font-style: normal;
    font-weight: 700;
  }

  strong {
    min-width: 0;
    overflow-wrap: anywhere;
    color: #007a78;
    font-weight: 800;
  }
}

.realtime-detail-lower {
  padding: 14px 16px 16px;
  background: #ffffff;
  border: 1px solid #d8dde3;

  :deep(.el-tabs__header) {
    margin-bottom: 12px;
  }

  :deep(.el-tabs__item.is-active) {
    color: #0b8f8c;
  }

  :deep(.el-tabs__active-bar) {
    background-color: #0bb5b1;
  }
}

.realtime-history-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.realtime-alarm-summary {
  margin-bottom: 10px;
  color: #5f6b7a;
  font-size: 13px;
  line-height: 20px;
}

.telemetry-form {
  margin-bottom: 8px;
}

.data-view-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 0;
  }
}

.metric-radio-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin: 4px 0 10px;
  color: var(--el-text-color-primary);
  font-size: 14px;

  > span {
    font-weight: 600;
  }
}

.daily-extreme-table {
  :deep(.el-table__header-wrapper th.el-table__cell),
  :deep(.el-table__fixed-header-wrapper th.el-table__cell) {
    color: #8a94a3 !important;
    font-weight: 700;
    background-color: #ffffff !important;
    border-bottom-color: #e8edf3 !important;
  }

  :deep(.el-table__header-wrapper th.el-table__cell .cell),
  :deep(.el-table__fixed-header-wrapper th.el-table__cell .cell) {
    color: #8a94a3 !important;
    line-height: 22px;
  }

  :deep(.el-table__row--striped td.el-table__cell) {
    background-color: #f6f8fb !important;
  }

  :deep(td.el-table__cell) {
    color: #5f6b7a;
    border-bottom-color: #eef2f6;
  }
}

@media (max-width: 1200px) {
  .monitor-workbench {
    grid-template-columns: 300px minmax(0, 1fr);
  }

  .bill-report {
    &__kpis,
    &__battery,
    &__total {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    &__grid,
    &__split {
      grid-template-columns: 1fr;
    }

    &__section--tall {
      grid-row: auto;
    }
  }

  .monitor-toolbar {
    align-items: flex-start;
    flex-direction: column;

    &__filters {
      justify-content: flex-start;
    }
  }

  .meter-grid,
  .realtime-card-grid,
  .realtime-card-grid.is-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .monitor-platform-bar {
    align-items: flex-start;
    flex-direction: column;
    padding: 14px;

    &__title strong {
      font-size: 22px;
      line-height: 30px;
    }
  }

  .monitor-workbench {
    grid-template-columns: 1fr;
  }

  .monitor-sidebar {
    border-right: 0;
    border-bottom: 1px solid #edf2f4;
  }

  .monitor-sidebar__status {
    padding: 28px 48px;
  }

  .bill-report-panel {
    padding: 12px;

    &__header {
      align-items: flex-start;
      flex-direction: column;

      h2 {
        font-size: 18px;
        line-height: 24px;
      }
    }
  }

  .bill-report {
    &__scope-summary {
      align-items: flex-start;
      flex-direction: column;
    }

    &__kpis,
    &__grid,
    &__total,
    &__split,
    &__battery {
      grid-template-columns: 1fr;
    }

    &__kpi {
      min-height: 92px;
      padding: 14px;

      strong {
        font-size: 22px;
        line-height: 28px;
        white-space: normal;
      }
    }

    &__list,
    &__profit {
      div {
        align-items: flex-start;
        flex-direction: column;
        gap: 4px;
      }

      strong {
        text-align: left;
        white-space: normal;
      }
    }

    &__final {
      font-size: 20px;
      line-height: 28px;
    }
  }

  .data-view-toolbar,
  .realtime-detail__toolbar,
  .realtime-detail__meta {
    align-items: flex-start;
    flex-direction: column;
  }

  .realtime-detail__actions {
    justify-content: flex-start;
    margin-left: 0;
  }

  .realtime-detail {
    min-height: auto;
    padding: 10px;
  }

  :deep(.realtime-detail-dialog) {
    .el-dialog__body {
      padding: 10px;
    }
  }

  .monitor-toolbar__filters,
  .monitor-toolbar__filters .el-input,
  .monitor-toolbar__filters .el-select,
  .monitor-toolbar__filters .el-button {
    width: 100% !important;
  }

  .meter-grid,
  .realtime-card-grid,
  .realtime-card-grid.is-compact {
    grid-template-columns: 1fr;
  }
}
</style>
