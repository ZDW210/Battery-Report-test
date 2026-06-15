<template>
  <view class="page">
    <view class="summary">
      <text class="summary-title">{{ config.title }}</text>
      <view class="summary-main">
        <text class="summary-value">{{ summaryValue }}</text>
        <text class="summary-unit" v-if="config.unit">{{ config.unit }}</text>
      </view>
      <text class="summary-sub">{{ summarySub }}</text>
    </view>

    <view class="metric-grid" v-if="metrics.length">
      <view class="metric" v-for="m in metrics" :key="m.label">
        <text class="metric-value">{{ m.value }}</text>
        <text class="metric-label">{{ m.label }}</text>
      </view>
    </view>

    <view class="section-head">
      <text class="section-title">{{ config.listTitle }}</text>
      <text class="section-count">{{ rows.length }} 条</text>
    </view>

    <view class="list" v-if="rows.length">
      <view class="row" v-for="row in rows" :key="row.id" hover-class="row-hover" @tap="openRow(row)">
        <view class="row-main">
          <view class="row-title-line">
            <text class="row-title">{{ row.title }}</text>
            <text class="badge" v-if="row.badge">{{ row.badge }}</text>
          </view>
          <text class="row-meta">{{ row.meta }}</text>
        </view>
        <view class="row-side">
          <text class="row-value" :class="row.tone">{{ row.value }}</text>
          <text class="row-extra" v-if="row.extra">{{ row.extra }}</text>
        </view>
      </view>
    </view>

    <view class="empty" v-else-if="!loading">
      <text class="empty-title">暂无明细数据</text>
      <text class="empty-sub">当前统计范围内没有匹配记录</text>
    </view>

    <view class="empty" v-else>
      <text class="empty-title">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { getDevices, type Device } from '../../api/devices';
import { sessionApi, type ChargingSession } from '../../api/sessions';

type StatType =
  | 'today-income'
  | 'month-income'
  | 'active-sessions'
  | 'rental-rate'
  | 'today-charge'
  | 'today-discharge'
  | 'offline-devices'
  | 'fault-devices';

interface Row {
  id: string;
  title: string;
  meta: string;
  value: string;
  extra?: string;
  badge?: string;
  tone?: string;
  deviceId?: string;
}

const configs: Record<StatType, { title: string; unit: string; listTitle: string }> = {
  'today-income': { title: '今日收入', unit: '元', listTitle: '今日计费明细' },
  'month-income': { title: '本月收入', unit: '元', listTitle: '本月计费明细' },
  'active-sessions': { title: '进行中', unit: '单', listTitle: '进行中会话' },
  'rental-rate': { title: '出租率', unit: '%', listTitle: '进行中会话' },
  'today-charge': { title: '今日充电', unit: 'kWh', listTitle: '今日充电明细' },
  'today-discharge': { title: '今日放电', unit: 'kWh', listTitle: '今日放电明细' },
  'offline-devices': { title: '离线设备', unit: '台', listTitle: '离线设备' },
  'fault-devices': { title: '故障设备', unit: '台', listTitle: '故障设备' },
};

const type = ref<StatType>('today-income');
const loading = ref(false);
const rows = ref<Row[]>([]);
const summaryValue = ref('--');
const summarySub = ref('');
const metrics = ref<{ label: string; value: string }[]>([]);

const config = computed(() => configs[type.value]);

function startOfToday() {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  return d;
}

function startOfMonth() {
  const d = new Date();
  d.setDate(1);
  d.setHours(0, 0, 0, 0);
  return d;
}

function formatDate(value?: string) {
  if (!value) return '--';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '--';
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getMonth() + 1}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function money(v: number) {
  return v.toFixed(2);
}

function energy(v: number) {
  return v.toFixed(1);
}

function numeric(v: number | null | undefined) {
  return Number(v ?? 0);
}

function sessionCharge(s: ChargingSession) {
  if (s.sessionType === 0 && s.totalEnergy != null) return Math.max(0, s.totalEnergy);
  return Math.max(0, numeric(s.endEpi ?? s.startEpi) - numeric(s.startEpi));
}

function sessionDischarge(s: ChargingSession) {
  if (s.sessionType === 1 && s.totalEnergy != null) return Math.max(0, s.totalEnergy);
  return Math.max(0, numeric(s.endEpe ?? s.startEpe) - numeric(s.startEpe));
}

function sessionHours(s: ChargingSession) {
  const start = new Date(s.startTime).getTime();
  const end = s.endTime ? new Date(s.endTime).getTime() : Date.now();
  if (Number.isNaN(start) || Number.isNaN(end) || end < start) return '0.0h';
  return `${((end - start) / 3600000).toFixed(1)}h`;
}

function sessionTitle(s: ChargingSession) {
  return s.device?.name || s.device?.meterNo || '未命名设备';
}

function sessionMeta(s: ChargingSession) {
  const customer = s.customer?.name || '未关联客户';
  return `${customer} · ${formatDate(s.startTime)}`;
}

function rowsFromSessions(sessions: ChargingSession[], mode: 'fee' | 'charge' | 'discharge' | 'active') {
  return sessions.map((s) => {
    const charge = sessionCharge(s);
    const discharge = sessionDischarge(s);
    const fee = s.totalFee ?? 0;
    let value = `¥${money(fee)}`;
    let tone = 'income';

    if (mode === 'charge') {
      value = `${energy(charge)} kWh`;
      tone = 'charge';
    } else if (mode === 'discharge') {
      value = `${energy(discharge)} kWh`;
      tone = 'discharge';
    } else if (mode === 'active') {
      value = sessionHours(s);
      tone = 'active';
    }

    return {
      id: s.id,
      title: sessionTitle(s),
      meta: sessionMeta(s),
      value,
      extra: mode === 'fee' ? sessionHours(s) : `¥${money(fee)}`,
      badge: s.status === 'ACTIVE' ? '进行中' : '已完成',
      tone,
      deviceId: s.deviceId,
    };
  });
}

function rowsFromDevices(devices: Device[], mode: 'offline' | 'fault') {
  return devices.map((d) => ({
    id: d.id,
    title: d.name || d.meterNo,
    meta: `${d.projectName || '未关联项目'} · ${d.meterNo}`,
    value: mode === 'offline' ? statusText(d.status) : (d.runMode || 'FAULT'),
    extra: d.lastReadingAt ? formatDate(d.lastReadingAt) : '暂无更新时间',
    badge: d.status === 'ONLINE' ? '在线' : '离线',
    tone: mode === 'offline' ? 'warning' : 'danger',
    deviceId: d.id,
  }));
}

function statusText(status: Device['status']) {
  return status === 'ONLINE' ? '在线' : '离线';
}

async function loadData() {
  loading.value = true;
  rows.value = [];
  metrics.value = [];
  try {
    const now = new Date();
    if (type.value === 'today-income' || type.value === 'today-charge' || type.value === 'today-discharge') {
      const sessions = await sessionApi.list({
        status: 'COMPLETED',
        from: startOfToday().toISOString(),
        to: now.toISOString(),
        limit: 500,
      });
      const totalFee = sessions.reduce((sum, s) => sum + (s.totalFee ?? 0), 0);
      const totalCharge = sessions.reduce((sum, s) => sum + sessionCharge(s), 0);
      const totalDischarge = sessions.reduce((sum, s) => sum + sessionDischarge(s), 0);

      if (type.value === 'today-income') {
        summaryValue.value = money(totalFee);
        rows.value = rowsFromSessions(sessions, 'fee');
      } else if (type.value === 'today-charge') {
        summaryValue.value = energy(totalCharge);
        rows.value = rowsFromSessions(sessions, 'charge').filter((r) => !r.value.startsWith('0.0'));
      } else {
        summaryValue.value = energy(totalDischarge);
        rows.value = rowsFromSessions(sessions, 'discharge').filter((r) => !r.value.startsWith('0.0'));
      }
      summarySub.value = `今日 ${sessions.length} 条已完成会话`;
      metrics.value = [
        { label: '收入', value: `¥${money(totalFee)}` },
        { label: '充电', value: `${energy(totalCharge)}kWh` },
        { label: '放电', value: `${energy(totalDischarge)}kWh` },
      ];
      return;
    }

    if (type.value === 'month-income') {
      const sessions = await sessionApi.list({
        status: 'COMPLETED',
        from: startOfMonth().toISOString(),
        to: now.toISOString(),
        limit: 500,
      });
      const totalFee = sessions.reduce((sum, s) => sum + (s.totalFee ?? 0), 0);
      summaryValue.value = money(totalFee);
      summarySub.value = `本月 ${sessions.length} 条已完成会话`;
      rows.value = rowsFromSessions(sessions, 'fee');
      metrics.value = [{ label: '完成会话', value: String(sessions.length) }];
      return;
    }

    if (type.value === 'active-sessions' || type.value === 'rental-rate') {
      const [sessions, deviceRes] = await Promise.all([
        sessionApi.list({ status: 'ACTIVE', limit: 500 }),
        getDevices({ page: 1, pageSize: 100, sortBy: 'lastReadingAt', order: 'desc' }),
      ]);
      const online = deviceRes.list.filter((d) => d.status === 'ONLINE').length;
      const rate = online > 0 ? Math.round((sessions.length / online) * 100) : 0;
      summaryValue.value = type.value === 'rental-rate' ? String(rate) : String(sessions.length);
      summarySub.value = `在线设备 ${online} 台，进行中 ${sessions.length} 单`;
      rows.value = rowsFromSessions(sessions, 'active');
      metrics.value = [
        { label: '在线设备', value: String(online) },
        { label: '进行中', value: String(sessions.length) },
        { label: '出租率', value: `${rate}%` },
      ];
      return;
    }

    const deviceRes = await getDevices({ page: 1, pageSize: 100, sortBy: 'lastReadingAt', order: 'desc' });
    if (type.value === 'offline-devices') {
      const devices = deviceRes.list.filter((d) => d.status !== 'ONLINE');
      summaryValue.value = String(devices.length);
      summarySub.value = '当前非在线状态的设备';
      rows.value = rowsFromDevices(devices, 'offline');
      metrics.value = [{ label: '设备总数', value: String(deviceRes.total) }];
      return;
    }

    const devices = deviceRes.list.filter((d) => d.runMode === 'FAULT');
    summaryValue.value = String(devices.length);
    summarySub.value = '运行模式为故障的设备';
    rows.value = rowsFromDevices(devices, 'fault');
    metrics.value = [{ label: '设备总数', value: String(deviceRes.total) }];
  } finally {
    loading.value = false;
    uni.stopPullDownRefresh();
  }
}

function openRow(row: Row) {
  if (row.deviceId) {
    uni.navigateTo({ url: `/pages/device/detail?id=${row.deviceId}` });
  }
}

onLoad((opts: any) => {
  const nextType = opts?.type as StatType;
  if (nextType && configs[nextType]) type.value = nextType;
  uni.setNavigationBarTitle({ title: config.value.title });
  loadData();
});

onPullDownRefresh(() => {
  loadData();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 16rpx;
}

.summary {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 30rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.summary-title {
  font-size: 26rpx;
  color: #666;
}
.summary-main {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: 10rpx;
}
.summary-value {
  font-size: 54rpx;
  font-weight: 800;
  color: #0f3460;
}
.summary-unit {
  font-size: 24rpx;
  color: #999;
}
.summary-sub {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  margin-bottom: 16rpx;
}
.metric {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx 10rpx;
  text-align: center;
}
.metric-value {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a1a;
}
.metric-label {
  display: block;
  font-size: 20rpx;
  color: #999;
  margin-top: 6rpx;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8rpx 4rpx 14rpx;
}
.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1a1a;
}
.section-count {
  font-size: 22rpx;
  color: #999;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  background: #fff;
  border-radius: 12rpx;
  padding: 22rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.row-hover {
  opacity: 0.82;
}
.row-main {
  flex: 1;
  min-width: 0;
}
.row-title-line {
  display: flex;
  align-items: center;
  gap: 10rpx;
}
.row-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.badge {
  flex-shrink: 0;
  font-size: 20rpx;
  color: #2979ff;
  background: #e3f2fd;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}
.row-meta {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.row-side {
  flex-shrink: 0;
  min-width: 140rpx;
  text-align: right;
}
.row-value {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f3460;
}
.row-value.charge {
  color: #2979ff;
}
.row-value.discharge {
  color: #ff9100;
}
.row-value.warning {
  color: #f0a500;
}
.row-value.danger {
  color: #e94560;
}
.row-value.active {
  color: #2e7d32;
}
.row-extra {
  display: block;
  font-size: 20rpx;
  color: #aaa;
  margin-top: 6rpx;
}

.empty {
  text-align: center;
  padding: 120rpx 30rpx;
}
.empty-title {
  display: block;
  font-size: 28rpx;
  color: #666;
}
.empty-sub {
  display: block;
  font-size: 22rpx;
  color: #aaa;
  margin-top: 10rpx;
}
</style>
