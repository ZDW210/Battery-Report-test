<template>
  <view class="page">
    <!-- 顶部 -->
    <view class="top-bar">
      <text class="top-title">电表数据平台</text>
    </view>
    <view class="top-actions">
      <scroll-view class="module-nav" scroll-x :show-scrollbar="false">
        <view class="module-track">
          <text class="module-item active" @tap="handleModuleNav('devices')">设备</text>
          <text class="module-item" @tap="handleModuleNav('energy')">能耗分析</text>
          <text class="module-item" @tap="handleModuleNav('metering')">电力集抄</text>
          <text class="module-item" @tap="handleModuleNav('settlement')">结算报表</text>
          <text class="module-item" @tap="handleModuleNav('orders')">订单</text>
        </view>
      </scroll-view>
      <view class="action-buttons">
        <text class="act-btn driver" @tap="goDriverHome">司机端</text>
        <text class="act-btn scan" @tap="handleScanDischarge">扫码启动</text>
        <text class="act-btn exit" @tap="doLogout">退出</text>
      </view>
    </view>

    <!-- 概览 -->
    <view class="overview-bar">
      <view class="ov-item">
        <text class="ov-val">{{ overview?.totalDevices ?? '--' }}</text>
        <text class="ov-label">总计</text>
      </view>
      <view class="ov-item">
        <text class="ov-val green">{{ overview?.onlineCount ?? '--' }}</text>
        <text class="ov-label">在线</text>
      </view>
      <view class="ov-item">
        <text class="ov-val gray">{{ overview?.offlineCount ?? '--' }}</text>
        <text class="ov-label">离线</text>
      </view>
      <view class="ov-item">
        <text class="ov-val warn">{{ overview?.activeAlarms ?? '--' }}</text>
        <text class="ov-label">报警</text>
      </view>
    </view>

    <!-- 营收卡片 -->
    <view class="revenue-grid">
      <view class="rcard" hover-class="card-hover" @tap="goStats('today-income')">
        <text class="rcard-val">¥{{ deviceStore.revenueOverview?.todayIncome ?? '--' }}</text>
        <text class="rcard-label">今日收入</text>
      </view>
      <view class="rcard" hover-class="card-hover" @tap="goStats('month-income')">
        <text class="rcard-val">¥{{ deviceStore.revenueOverview?.monthIncome ?? '--' }}</text>
        <text class="rcard-label">本月收入</text>
      </view>
      <view class="rcard" hover-class="card-hover" @tap="goStats('active-sessions')">
        <text class="rcard-val">{{ deviceStore.revenueOverview?.activeSessions ?? '--' }}</text>
        <text class="rcard-label">进行中</text>
      </view>
      <view class="rcard" hover-class="card-hover" @tap="goStats('rental-rate')">
        <text class="rcard-val">{{ deviceStore.revenueOverview?.rentalRate ?? '--' }}%</text>
        <text class="rcard-label">出租率</text>
      </view>
    </view>

    <!-- 电量 + 风险 -->
    <view class="energy-grid">
      <view class="ecard" hover-class="card-hover" @tap="goStats('today-charge')">
        <text class="ecard-val charge">{{ fmtEnergy(deviceStore.todayEnergy?.todayCharge) }}</text>
        <text class="ecard-label">今日充电(kWh)</text>
      </view>
      <view class="ecard" hover-class="card-hover" @tap="goStats('today-discharge')">
        <text class="ecard-val discharge">{{ fmtEnergy(deviceStore.todayEnergy?.todayDischarge) }}</text>
        <text class="ecard-label">今日放电(kWh)</text>
      </view>
      <view class="ecard" hover-class="card-hover" @tap="goStats('offline-devices')">
        <text class="ecard-val warn">{{ deviceStore.offlineDeviceCount }}</text>
        <text class="ecard-label">离线设备</text>
      </view>
      <view class="ecard" hover-class="card-hover" @tap="goStats('fault-devices')">
        <text class="ecard-val danger">{{ deviceStore.faultCount }}</text>
        <text class="ecard-label">故障设备</text>
      </view>
    </view>

    <!-- 数据汇总 -->
    <view class="dash-card">
      <view class="dash-hd">
        <text class="dash-title">数据汇总</text>
        <view class="range-tabs">
          <text class="rtab" :class="{ active: rangeDays === 7 }" @click="setRange(7)">7天</text>
          <text class="rtab" :class="{ active: rangeDays === 15 }" @click="setRange(15)">15天</text>
          <text class="rtab" :class="{ active: rangeDays === 30 }" @click="setRange(30)">30天</text>
        </view>
        <text class="dash-sub" v-if="overview?.lastUpdatedAt">更新于 {{ timeText }}</text>
      </view>
      <view class="kpi-row">
        <view class="kpi-item">
          <text class="kpi-label">在线率</text>
          <text class="kpi-val">{{ onlineRateText }}</text>
        </view>
        <view class="kpi-div"></view>
        <view class="kpi-item">
          <text class="kpi-label">有功电能</text>
          <text class="kpi-val in">{{ fmtNum(overview?.totalEpi) }}<text class="kpi-unit">kWh</text></text>
        </view>
        <view class="kpi-div"></view>
        <view class="kpi-item">
          <text class="kpi-label">当前功率</text>
          <text class="kpi-val out">{{ fmtNum(overview?.totalPower) }}<text class="kpi-unit">kW</text></text>
        </view>
      </view>
    </view>

    <!-- 各设备对比 -->
    <view class="chart-card">
      <text class="chart-title">各设备对比</text>
      <view class="chart-group" v-for="d in comparisonDevices" :key="d.id">
        <view class="cg-head">
          <text class="cg-label">{{ deviceCompareName(d) }}</text>
        </view>
        <view class="cg-row">
          <text class="cg-tag epi-tag">电能</text>
          <view class="cg-track">
            <view class="cg-bar epi" :style="{ width: barPct(d.lastEpi) }"></view>
          </view>
          <text class="cg-val epi-val">{{ shortNum(d.lastEpi) }}</text>
        </view>
        <view class="cg-row">
          <text class="cg-tag power-tag">功率</text>
          <view class="cg-track">
            <view class="cg-bar power" :style="{ width: barPct(d.lastP) }"></view>
          </view>
          <text class="cg-val power-val">{{ shortNum(d.lastP) }}</text>
        </view>
      </view>
      <view v-if="!deviceStore.list.length" class="chart-empty">暂无设备数据</view>
    </view>

    <!-- 结算参考数据 -->
    <view class="settle-card">
      <view class="settle-hd">
        <text class="settle-title">结算参考数据</text>
        <text class="export-btn" @tap="doExportEnergyReport">导出</text>
        <view class="range-tabs settle-tabs">
          <text class="rtab" :class="{ active: settleRangeDays === 7 }" @click="setSettleRange(7)">7天</text>
          <text class="rtab" :class="{ active: settleRangeDays === 15 }" @click="setSettleRange(15)">15天</text>
          <text class="rtab" :class="{ active: settleRangeDays === 30 }" @click="setSettleRange(30)">30天</text>
          <text class="rtab" :class="{ active: settleRangeDays === 0 }" @click="setSettleRange(0)">自定义</text>
        </view>
      </view>
      <view class="date-picker-row" v-if="settleRangeDays === 0">
        <picker mode="date" :value="settleCustomFrom" @change="onSettleFromChange">
          <view class="date-tag">{{ settleCustomFrom || '开始日期' }}</view>
        </picker>
        <text class="date-sep">至</text>
        <picker mode="date" :value="settleCustomTo" @change="onSettleToChange">
          <view class="date-tag">{{ settleCustomTo || '结束日期' }}</view>
        </picker>
      </view>
      <text class="settle-sub" v-if="energyDelta">区间: {{ shortDateStr(energyDelta.from) }} ~ {{ shortDateStr(energyDelta.to) }}</text>
      <view class="settle-table" v-if="energyDelta?.devices?.length">
        <view class="st-row st-header">
          <text class="st-cell st-name">设备</text>
          <text class="st-cell st-val">正向电能(kWh)</text>
          <text class="st-cell st-val">净电量(kWh)</text>
        </view>
        <view class="st-row" v-for="d in energyDelta.devices" :key="d.deviceId">
          <text class="st-cell st-name">{{ d.deviceName || d.meterNo }}</text>
          <text class="st-cell st-val charge">{{ d.chargeEnergy.toFixed(1) }}</text>
          <text class="st-cell st-val" :class="d.netEnergy >= 0 ? 'positive' : 'negative'">{{ d.netEnergy.toFixed(1) }}</text>
        </view>
        <view class="st-row st-footer">
          <text class="st-cell st-name">合计 ({{ energyDelta.summary.deviceCount }}台)</text>
          <text class="st-cell st-val charge">{{ energyDelta.summary.totalCharge.toFixed(1) }}</text>
          <text class="st-cell st-val" :class="energyDelta.summary.totalNet >= 0 ? 'positive' : 'negative'">{{ energyDelta.summary.totalNet.toFixed(1) }}</text>
        </view>
      </view>
      <view v-else-if="deviceStore.list.length" class="settle-empty">加载中...</view>
      <view v-else class="settle-empty">暂无结算数据</view>
    </view>

    <!-- 会话计费明细 -->
    <view class="settle-card">
      <view class="settle-hd">
        <text class="settle-title">计费明细</text>
        <text class="export-btn" @tap="doExportChargingDetails">导出</text>
      </view>
      <view class="bill-table" v-if="deviceStore.sessions.length">
        <view class="st-row st-header">
          <text class="st-cell st-name">设备</text>
          <text class="st-cell st-sm">时长h</text>
          <text class="st-cell st-val">电量kWh</text>
          <text class="st-cell st-val">时长费</text>
          <text class="st-cell st-val">合计¥</text>
        </view>
        <view class="st-row" v-for="s in deviceStore.sessions" :key="s.id" @tap="goDetail(s.deviceId)">
          <text class="st-cell st-name">{{ s.device?.name || s.device?.meterNo || '--' }}</text>
          <text class="st-cell st-sm">{{ sessionDuration(s) }}</text>
          <text class="st-cell st-val discharge">{{ sessionEnergy(s).toFixed(1) }}</text>
          <text class="st-cell st-val">{{ (s.totalTimeFee ?? 0).toFixed(2) }}</text>
          <text class="st-cell st-val positive">¥{{ (s.totalFee ?? 0).toFixed(2) }}</text>
        </view>
        <view class="st-row st-footer">
          <text class="st-cell st-name">合计 ({{ deviceStore.sessions.length }}条)</text>
          <text class="st-cell st-sm">{{ totalSessionHours() }}</text>
          <text class="st-cell st-val discharge">{{ totalSessionEnergy().toFixed(1) }}</text>
          <text class="st-cell st-val">{{ totalSessionTimeFee().toFixed(2) }}</text>
          <text class="st-cell st-val positive">¥{{ totalSessionFee().toFixed(2) }}</text>
        </view>
      </view>
      <view v-else class="settle-empty">暂无计费记录</view>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <input class="search-input" v-model="search" placeholder="搜索设备名称/编号/项目" @confirm="onSearch" />
      <view class="filter-tabs">
        <text class="tab" :class="{ active: statusFilter === '' }" @click="statusFilter = ''; onSearch()">全部</text>
        <text class="tab" :class="{ active: statusFilter === 'ONLINE' }" @click="statusFilter = 'ONLINE'; onSearch()">在线</text>
        <text class="tab" :class="{ active: statusFilter === 'OFFLINE' }" @click="statusFilter = 'OFFLINE'; onSearch()">离线</text>
      </view>
    </view>

    <!-- 设备列表 -->
    <view class="list" v-if="deviceStore.list.length">
      <DeviceCard
        v-for="d in deviceStore.list" :key="d.id"
        :name="d.name" :meter-no="d.meterNo" :status="d.status"
        :project-name="d.projectName"
        :last-voltage="d.lastVoltage" :last-current="d.lastCurrent"
        :last-p="d.lastP" :last-pf="d.lastPf" :last-epi="d.lastEpi"
        :last-reading-at="d.lastReadingAt"
        :run-mode="d.runMode"
        :latitude="d.latitude" :longitude="d.longitude"
        @click="goDetail(d.id)"
      />
    </view>
    <EmptyState v-else text="暂无设备数据" icon="📡" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { useDeviceStore } from '../../stores/device';
import { formatTime } from '../../utils/format';
import { exportEnergyReport, exportChargingDetails } from '../../api/export';
import { sessionApi } from '../../api/sessions';
import { downloadAndOpenExcel } from '../../utils/export';
import DeviceCard from '../../components/DeviceCard.vue';
import EmptyState from '../../components/EmptyState.vue';
import { useAuthStore } from '../../stores/auth';

const deviceStore = useDeviceStore();
const authStore = useAuthStore();
const overview = computed(() => deviceStore.overview);
const energyDelta = computed(() => deviceStore.energyDelta);
const search = ref('');
const statusFilter = ref('');
const rangeDays = ref(7);
const settleRangeDays = ref(7);
const settleCustomFrom = ref('');
const settleCustomTo = ref('');

const comparisonDevices = computed(() => {
  const list = [...deviceStore.list];
  list.sort((a, b) => {
    if (a.status === 'ONLINE' && b.status !== 'ONLINE') return -1;
    if (a.status !== 'ONLINE' && b.status === 'ONLINE') return 1;
    return new Date(b.lastReadingAt || 0).getTime() - new Date(a.lastReadingAt || 0).getTime();
  });
  return list.slice(0, 5);
});

function getRangeParams(): { from: string; to: string } {
  const now = new Date();
  const to = now.toISOString();
  const from = new Date(now.getTime() - rangeDays.value * 24 * 60 * 60 * 1000).toISOString();
  return { from, to };
}

function setRange(days: number) {
  rangeDays.value = days;
  deviceStore.fetchOverview(getRangeParams());
}

function fmtNum(v: number | null | undefined): string {
  if (v == null) return '--';
  if (v >= 1000000) return (v / 1000000).toFixed(2) + 'M';
  if (v >= 10000) return (v / 10000).toFixed(2) + '万';
  if (v >= 1000) return (v / 1000).toFixed(1) + 'k';
  return v.toFixed(1);
}

function shortNum(v: number | null | undefined): string {
  if (v == null) return '--';
  if (v >= 1000000) return (v / 1000000).toFixed(1) + 'M';
  if (v >= 10000) return (v / 10000).toFixed(1) + '万';
  if (v >= 1000) return (v / 1000).toFixed(1) + 'k';
  return v.toFixed(0);
}

function fmtEnergy(v: number | null | undefined): string {
  return v == null ? '--' : v.toFixed(1);
}

function deviceCompareName(d: { name?: string | null; meterNo?: string | null }): string {
  return d.name || d.meterNo || '--';
}

const timeText = computed(() => formatTime(overview.value?.lastUpdatedAt));
const onlineRateText = computed(() => {
  const total = overview.value?.totalDevices ?? 0;
  if (!total) return '--';
  return `${(((overview.value?.onlineCount ?? 0) / total) * 100).toFixed(1)}%`;
});

const chartMax = computed(() => {
  let max = 0;
  for (const d of comparisonDevices.value) {
    if (d.lastEpi != null && d.lastEpi > max) max = d.lastEpi;
    if (d.lastP != null && d.lastP > max) max = d.lastP;
  }
  return max || 1;
});

function barPct(v: number | null | undefined): string {
  if (v == null || v <= 0) return '0%';
  return Math.max(3, (v / chartMax.value) * 100).toFixed(1) + '%';
}

function onSearch() {
  return deviceStore.fetchDevices({ search: search.value || undefined, status: statusFilter.value || undefined });
}

function goStats(type: string) {
  uni.navigateTo({ url: `/pages/stats/detail?type=${type}` });
}

function handleModuleNav(key: string) {
  const actions: Record<string, () => void> = {
    devices: () => {},
    energy: () => goStats('today-charge'),
    settlement: () => goStats('month-income'),
    orders: () => goStats('active-sessions'),
  };
  if (actions[key]) {
    actions[key]();
    return;
  }
  uni.showToast({ title: '功能建设中', icon: 'none' });
}

function doLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出吗？',
        success: (res) => {
      if (res.confirm) {
        authStore.logout();
      }
    },
  });
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/device/detail?id=${id}` });
}

function goDriverHome() {
  uni.switchTab({ url: '/pages/driver/index' });
}

function scanCode(): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      onlyFromCamera: true,
      success: (res) => resolve(res.result || ''),
      fail: reject,
    });
  });
}

async function handleScanDischarge() {
  try {
    const scanText = await scanCode();
    if (!scanText) {
      uni.showToast({ title: '未识别到二维码内容', icon: 'none' });
      return;
    }
    uni.showLoading({ title: '校验账户...' });
    const scanResult = await sessionApi.verifyScan(scanText);
    uni.hideLoading();
    if (!scanResult.accountKnown) {
      uni.showModal({
        title: '未知账户',
        content: scanResult.message || '当前微信账户未录入或未开放该设备权限，请联系管理员处理。',
        showCancel: false,
        confirmText: '知道了',
      });
      return;
    }
    const title = scanResult.deviceName || scanResult.deviceNo || '扫码设备';
    const content = [
      `账户：${scanResult.accountName || scanResult.accountMobile || scanResult.accountId || '--'}`,
      `设备：${title}`,
      `仪表：${scanResult.meterNo || '--'}`,
      `状态：${scanResult.status === 0 ? '在线' : '非在线'}`,
      '确认后将发起放电任务'
    ].join('\n');
    uni.showModal({
      title: '确认放电',
      content,
      confirmText: '开始放电',
      success: async (res) => {
        if (!res.confirm) return;
        uni.showLoading({ title: '下发放电...' });
        try {
          await sessionApi.startDischargeByScan(scanText);
          uni.showToast({ title: '放电任务已开始', icon: 'success' });
          await Promise.all([
            deviceStore.fetchRevenueOverview(),
            deviceStore.fetchTodayEnergy(),
            deviceStore.fetchSessions({ ...getSettleRangeParams(), status: 'COMPLETED', limit: 500 }),
            onSearch(),
          ]);
        } catch (error: any) {
          uni.showModal({
            title: '启动失败',
            content: getErrorMessage(error),
            showCancel: false,
            confirmText: '知道了',
          });
        } finally {
          uni.hideLoading();
        }
      },
    });
  } catch (error: any) {
    uni.hideLoading();
    if (error?.errMsg?.includes('cancel')) return;
    uni.showToast({ title: getErrorMessage(error), icon: 'none' });
  }
}

function getErrorMessage(error: any) {
  return error?.message || error?.msg || error?.data?.msg || '扫码启动失败';
}

// --- 结算参考数据 ---
function isValidDateStr(s: string): boolean {
  return !!s && !isNaN(new Date(s).getTime());
}

function default7dRange(): { from: string; to: string } {
  const now = new Date();
  return {
    from: new Date(now.getTime() - 7 * 86400000).toISOString(),
    to: now.toISOString(),
  };
}

function getSettleRangeParams(): { from: string; to: string } {
  if (settleRangeDays.value === 0) {
    if (!isValidDateStr(settleCustomFrom.value) || !isValidDateStr(settleCustomTo.value)) {
      return default7dRange();
    }
    return {
      from: new Date(settleCustomFrom.value).toISOString(),
      to: new Date(settleCustomTo.value + 'T23:59:59').toISOString(),
    };
  }
  const now = new Date();
  return {
    from: new Date(now.getTime() - settleRangeDays.value * 86400000).toISOString(),
    to: now.toISOString(),
  };
}

function setSettleRange(days: number) {
  settleRangeDays.value = days;
  if (days === 0) {
    const now = new Date();
    settleCustomTo.value = now.toISOString().slice(0, 10);
    settleCustomFrom.value = new Date(now.getTime() - 7 * 86400000).toISOString().slice(0, 10);
  }
  deviceStore.fetchEnergyDelta(getSettleRangeParams());
}

function onSettleFromChange(e: any) {
  settleCustomFrom.value = e.detail.value;
  if (settleCustomTo.value) deviceStore.fetchEnergyDelta(getSettleRangeParams());
}

function onSettleToChange(e: any) {
  settleCustomTo.value = e.detail.value;
  if (settleCustomFrom.value) deviceStore.fetchEnergyDelta(getSettleRangeParams());
}

function shortDateStr(s: string): string {
  if (!s) return '--';
  return s.slice(0, 10);
}

// --- 计费明细辅助 ---
function sessionDuration(s: any): string {
  if (!s.endTime) return '--';
  const h = (new Date(s.endTime).getTime() - new Date(s.startTime).getTime()) / 3600000;
  return h.toFixed(1);
}
function sessionEnergy(s: any): number {
  if (s.totalEnergy != null) return Math.max(0, Number(s.totalEnergy));
  const start = Number(s.startEpi ?? 0);
  const end = Number(s.endEpi ?? s.startEpi ?? 0);
  return Math.max(0, end - start);
}
function totalSessionHours() {
  return deviceStore.sessions.reduce((sum, s) => {
    if (!s.endTime) return sum;
    return sum + (new Date(s.endTime).getTime() - new Date(s.startTime).getTime()) / 3600000;
  }, 0).toFixed(1);
}
function totalSessionEnergy() {
  return deviceStore.sessions.reduce((sum, s) => sum + sessionEnergy(s), 0);
}
function totalSessionTimeFee() {
  return deviceStore.sessions.reduce((sum, s) => sum + (s.totalTimeFee ?? 0), 0);
}
function totalSessionFee() {
  return deviceStore.sessions.reduce((sum, s) => sum + (s.totalFee ?? 0), 0);
}

async function doExportEnergyReport() {
  uni.showLoading({ title: '生成报表...' });
  try {
    const result = await exportEnergyReport(getSettleRangeParams());
    downloadAndOpenExcel(result.data, result.filename);
  } catch { uni.showToast({ title: '导出失败', icon: 'none' }); }
  finally { uni.hideLoading(); }
}

async function doExportChargingDetails() {
  uni.showLoading({ title: '生成明细...' });
  try {
    const result = await exportChargingDetails(getSettleRangeParams());
    downloadAndOpenExcel(result.data, result.filename);
  } catch { uni.showToast({ title: '导出失败', icon: 'none' }); }
  finally { uni.hideLoading(); }
}

onShow(async () => {
  try {
    await authStore.refreshProfile();
  } catch {
    uni.reLaunch({ url: '/pages/auth/login' });
    return;
  }
  if (!authStore.user?.miniAdminEnabled) {
    uni.showToast({ title: '未开放管理权限', icon: 'none' });
    uni.switchTab({ url: '/pages/driver/index' });
    return;
  }
  await Promise.all([
    deviceStore.fetchOverview(getRangeParams()),
    deviceStore.fetchEnergyDelta(getSettleRangeParams()),
    deviceStore.fetchRevenueOverview(),
    deviceStore.fetchTodayEnergy(),
    deviceStore.fetchSessions({ ...getSettleRangeParams(), status: 'COMPLETED', limit: 500 }),
    onSearch(),
  ]);
  deviceStore.recomputeStatusCounts(deviceStore.list);
});

onPullDownRefresh(() => {
  Promise.all([
      deviceStore.fetchOverview(getRangeParams()),
      deviceStore.fetchEnergyDelta(getSettleRangeParams()),
      deviceStore.fetchRevenueOverview(),
      deviceStore.fetchTodayEnergy(),
      deviceStore.fetchSessions({ ...getSettleRangeParams(), status: 'COMPLETED', limit: 500 }),
      onSearch(),
    ]).then(() => {
    deviceStore.recomputeStatusCounts(deviceStore.list);
  }).catch((e) => {
    console.error('Pull-down refresh failed:', e);
  }).finally(() => {
    uni.stopPullDownRefresh();
  });
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 32rpx;
  overflow-x: hidden;
}

/* 概览栏 */
.overview-bar {
  display: flex; background: #fff; border-radius: 16rpx;
  padding: 20rpx 0; margin: 16rpx 16rpx 12rpx;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.ov-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4rpx; }
.ov-val { font-size: 36rpx; font-weight: 700; color: #333; }
.ov-val.green { color: #2e7d32; }
.ov-val.gray { color: #999; }
.ov-val.warn { color: #ff6d00; }
.ov-label { font-size: 22rpx; color: #999; }

/* 数据汇总 */
.dash-card {
  background: #fff; border-radius: 16rpx; padding: 22rpx 24rpx;
  margin: 0 16rpx 16rpx; box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.dash-hd {
  display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 16rpx;
}
.dash-title { font-size: 28rpx; font-weight: 600; color: #1a1a1a; }
.dash-sub { font-size: 20rpx; color: #bbb; }
.range-tabs { display: flex; gap: 8rpx; }
.rtab {
  font-size: 22rpx; color: #999; padding: 4rpx 16rpx;
  border-radius: 6rpx; background: #f5f5f5;
}
.rtab.active { background: #2979ff; color: #fff; }

.kpi-row { display: flex; align-items: center; }
.kpi-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 2rpx; }
.kpi-label { font-size: 22rpx; color: #999; }
.kpi-val { font-size: 38rpx; font-weight: 700; color: #1a1a1a; }
.kpi-val.in { color: #2979ff; }
.kpi-val.out { color: #ff9100; }
.kpi-unit { font-size: 22rpx; font-weight: 400; }
.kpi-div { width: 2rpx; height: 52rpx; background: #f0f0f0; }

/* 各设备对比图 */
.chart-card {
  background: #fff; border-radius: 16rpx; padding: 22rpx 24rpx;
  margin: 0 16rpx 16rpx; box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.chart-title { font-size: 28rpx; font-weight: 600; color: #1a1a1a; display: block; margin-bottom: 14rpx; }
.chart-empty { text-align: center; font-size: 24rpx; color: #bbb; padding: 20rpx; }

.chart-group { margin-bottom: 14rpx; }
.chart-group:last-child { margin-bottom: 0; }
.cg-head { margin-bottom: 4rpx; }
.cg-label { font-size: 24rpx; font-weight: 600; color: #1a1a1a; }

.cg-row { display: flex; align-items: center; gap: 8rpx; margin: 4rpx 0; }
.cg-tag { font-size: 20rpx; width: 56rpx; text-align: center; padding: 2rpx 0; border-radius: 4rpx; flex-shrink: 0; }
.epi-tag { background: #e8f0fe; color: #1a56db; }
.power-tag { background: #fff3e0; color: #e65100; }

.cg-track { flex: 1; height: 22rpx; background: #f2f2f2; border-radius: 5rpx; overflow: hidden; }
.cg-bar { height: 22rpx; border-radius: 5rpx; min-width: 4rpx; }
.cg-bar.epi { background: #3b82f6; }
.cg-bar.power { background: #f97316; }

.cg-val { font-size: 22rpx; width: 80rpx; text-align: right; flex-shrink: 0; color: #555; }
.epi-val { color: #3b82f6; }
.power-val { color: #f97316; }

/* 搜索 */
.search-bar { margin: 0 16rpx 16rpx; }
.search-input {
  background: #fff; border-radius: 12rpx; padding: 16rpx 24rpx;
  font-size: 28rpx; margin-bottom: 12rpx;
}
.filter-tabs { display: flex; gap: 16rpx; }
.tab {
  font-size: 26rpx; color: #666; padding: 8rpx 28rpx;
  border-radius: 20rpx; background: #fff;
}
.tab.active { background: #2979ff; color: #fff; }
.list { padding: 0 16rpx 32rpx; }

/* 顶部栏 */
.top-bar {
  background: linear-gradient(135deg, #1a1a2e, #0f3460);
  padding: 20rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 40rpx);
}
.top-title { font-size: 34rpx; font-weight: bold; color: #fff; display: block; }
.top-actions {
  background: linear-gradient(135deg, #1a1a2e, #0f3460);
  display: flex; align-items: center; gap: 16rpx;
  padding: 0 20rpx 24rpx 30rpx;
}
.module-nav {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
}
.module-track {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  min-width: max-content;
}
.module-item {
  display: inline-flex;
  align-items: center;
  height: 56rpx;
  padding: 0 22rpx;
  border-radius: 28rpx;
  font-size: 24rpx;
  color: rgba(255,255,255,0.72);
  background: rgba(255,255,255,0.08);
}
.module-item.active {
  color: #0f3460;
  background: #fff;
  font-weight: 600;
}
.action-buttons {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}
.act-btn {
  font-size: 24rpx; color: #fff; padding: 8rpx 18rpx;
  border-radius: 20rpx; border: 2rpx solid rgba(255,255,255,0.4);
}
.act-btn.scan { border-color: rgba(94,234,212,0.8); color: #99f6e4; }
.act-btn.driver { border-color: rgba(255,255,255,0.6); color: #fff; }
.act-btn.exit { border-color: rgba(255,100,100,0.6); color: #ff8a80; }

/* 营收卡片 */
.revenue-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12rpx;
  padding: 0 16rpx 12rpx;
}
.rcard {
  background: #fff; border-radius: 12rpx; padding: 20rpx;
  text-align: center; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}
.card-hover { opacity: 0.82; transform: scale(0.99); }
.rcard-val { font-size: 36rpx; font-weight: 700; color: #0f3460; }
.rcard-label { font-size: 22rpx; color: #999; margin-top: 6rpx; display: block; }

/* 电量风险卡片 */
.energy-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12rpx;
  padding: 0 16rpx 12rpx;
}
.ecard {
  background: #fff; border-radius: 12rpx; padding: 20rpx;
  text-align: center; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}
.ecard-val { font-size: 36rpx; font-weight: 700; }
.ecard-val.charge { color: #2979ff; }
.ecard-val.discharge { color: #ff9100; }
.ecard-val.warn { color: #f0a500; }
.ecard-val.danger { color: #e94560; }
.ecard-label { font-size: 22rpx; color: #999; margin-top: 6rpx; display: block; }

/* 结算参考数据 */
.settle-card {
  background: #fff; border-radius: 16rpx; padding: 22rpx 24rpx;
  margin: 0 16rpx 16rpx; box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.settle-hd {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 6rpx;
}
.settle-title { font-size: 28rpx; font-weight: 600; color: #1a1a1a; }
.export-btn {
  font-size: 22rpx; color: #2979ff; padding: 4rpx 16rpx;
  border: 1rpx solid #2979ff; border-radius: 6rpx;
  flex-shrink: 0;
}
.settle-sub { font-size: 20rpx; color: #bbb; display: block; margin-bottom: 12rpx; }
.settle-tabs { margin-bottom: 0; }

.date-picker-row {
  display: flex; justify-content: center; align-items: center; gap: 12rpx;
  margin: 12rpx 0;
}
.date-tag {
  font-size: 22rpx; padding: 6rpx 20rpx; border-radius: 8rpx;
  background: #f5f5f5; color: #666;
}
.date-sep { font-size: 22rpx; color: #999; }

.settle-table { border-radius: 8rpx; overflow: hidden; }
.st-row { display: flex; padding: 12rpx 8rpx; }
.st-row:not(:last-child) { border-bottom: 1rpx solid #f0f0f0; }
.st-header { background: #0f3460; }
.st-footer { background: #f5f5f5; }
.st-cell { font-size: 22rpx; text-align: center; color: #333; }
.st-header .st-cell { color: #fff; font-weight: 600; }
.st-footer .st-cell { color: #1a1a2e; font-weight: 600; }
.st-name { flex: 2.5; text-align: left; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.st-val { flex: 1.5; }
.st-sm { flex: 1; font-size: 20rpx; }
.st-val.charge { color: #2979ff; }
.st-val.discharge { color: #ff9100; }
.positive { color: #4caf50 !important; }
.negative { color: #f44336 !important; }
.settle-empty { text-align: center; font-size: 24rpx; color: #bbb; padding: 20rpx; }
</style>

