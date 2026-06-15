<template>
  <view class="page">
    <view class="summary">
      <text class="summary-title">我的订单</text>
      <text class="summary-sub">仅显示当前微信账号授权范围内的放电/计费记录</text>
    </view>

    <view class="list" v-if="sessions.length">
      <view class="order-card" v-for="item in sessions" :key="item.id">
        <view class="order-head">
          <text class="device">{{ item.device?.name || item.device?.meterNo || '未命名设备' }}</text>
          <text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
        </view>
        <view class="row">
          <text>开始时间</text>
          <text>{{ formatDate(item.startTime) }}</text>
        </view>
        <view class="row">
          <text>结束时间</text>
          <text>{{ item.endTime ? formatDate(item.endTime) : '--' }}</text>
        </view>
        <view class="row">
          <text>电量</text>
          <text>{{ energyText(item.totalEnergy) }}</text>
        </view>
        <view class="row total">
          <text>费用</text>
          <text>¥{{ money(item.totalFee) }}</text>
        </view>
      </view>
    </view>

    <view class="empty" v-else-if="!loading">
      <text class="empty-title">暂无订单</text>
      <text class="empty-sub">扫码启动放电后，记录会显示在这里。</text>
    </view>

    <view class="empty" v-else>
      <text class="empty-title">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { sessionApi, type ChargingSession } from '../../api/sessions';
import { useAuthStore } from '../../stores/auth';

const auth = useAuthStore();
const sessions = ref<ChargingSession[]>([]);
const loading = ref(false);

function ensureLogin() {
  if (!auth.isLoggedIn) {
    uni.reLaunch({ url: '/pages/auth/login' });
    return false;
  }
  return true;
}

async function loadData() {
  if (!ensureLogin()) return;
  loading.value = true;
  try {
    sessions.value = await sessionApi.list({ limit: 100 });
  } finally {
    loading.value = false;
    uni.stopPullDownRefresh();
  }
}

function statusText(status: string) {
  const map: Record<string, string> = {
    ACTIVE: '进行中',
    COMPLETED: '已完成',
    SETTLED: '已结算',
    ABNORMAL: '异常',
  };
  return map[status] || status || '--';
}

function statusClass(status: string) {
  if (status === 'ACTIVE') return 'active';
  if (status === 'ABNORMAL') return 'danger';
  return 'done';
}

function formatDate(value?: string) {
  if (!value) return '--';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '--';
  const pad = (num: number) => String(num).padStart(2, '0');
  return `${date.getMonth() + 1}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function energyText(value?: number | null) {
  return value == null ? '--' : `${Number(value).toFixed(1)} kWh`;
}

function money(value?: number | null) {
  return Number(value ?? 0).toFixed(2);
}

onShow(loadData);
onPullDownRefresh(loadData);
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f3f4f6;
}
.summary {
  padding: 28rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #10b981, #34d399);
}
.summary-title {
  display: block;
  color: #ffffff;
  font-size: 40rpx;
  font-weight: 900;
}
.summary-sub {
  display: block;
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.88);
  font-size: 24rpx;
}
.list {
  margin-top: 20rpx;
}
.order-card {
  margin-bottom: 18rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  background: #ffffff;
}
.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}
.device {
  max-width: 460rpx;
  overflow: hidden;
  color: #111827;
  font-size: 30rpx;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.status {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}
.status.active {
  color: #0f766e;
  background: #ccfbf1;
}
.status.done {
  color: #2563eb;
  background: #dbeafe;
}
.status.danger {
  color: #dc2626;
  background: #fee2e2;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 0;
  color: #6b7280;
  font-size: 24rpx;
}
.row.total {
  color: #111827;
  font-size: 28rpx;
  font-weight: 800;
}
.empty {
  margin-top: 24rpx;
  padding: 80rpx 30rpx;
  border-radius: 16rpx;
  background: #ffffff;
  text-align: center;
}
.empty-title {
  display: block;
  color: #111827;
  font-size: 30rpx;
  font-weight: 800;
}
.empty-sub {
  display: block;
  margin-top: 12rpx;
  color: #9ca3af;
  font-size: 24rpx;
}
</style>
