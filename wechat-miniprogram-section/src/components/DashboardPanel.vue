<template>
  <view class="dashboard">
    <text class="dash-title">数据汇总</text>

    <!-- 总览卡片 -->
    <view class="stat-cards">
      <view class="stat-card">
        <text class="stat-val">{{ fmt(overview?.totalEpi) }}</text>
        <text class="stat-unit">kWh</text>
        <text class="stat-label">有功电能</text>
      </view>
      <view class="stat-card">
        <text class="stat-val orange">{{ overview?.activeAlarms ?? 0 }}</text>
        <text class="stat-unit">条</text>
        <text class="stat-label">未处理告警</text>
      </view>
      <view class="stat-card">
        <text class="stat-val green">{{ fmt(overview?.totalPower) }}</text>
        <text class="stat-unit">kW</text>
        <text class="stat-label">当前总功率</text>
      </view>
    </view>

    <view class="ratio-section">
      <view class="ratio-header">
        <text class="ratio-label">在线率</text>
        <text class="ratio-pct">{{ onlineRateText }}</text>
      </view>
      <view class="ratio-bar">
        <view class="ratio-fill output" :style="{ width: onlineRate + '%' }"></view>
      </view>
      <view class="ratio-legend">
        <view class="legend-item">
          <view class="dot blue"></view>
          <text>在线 {{ overview?.onlineCount ?? 0 }} 台</text>
        </view>
        <view class="legend-item">
          <view class="dot orange"></view>
          <text>离线 {{ overview?.offlineCount ?? 0 }} 台</text>
        </view>
      </view>
    </view>

    <!-- 更新时间 -->
    <view class="update-time" v-if="overview?.lastUpdatedAt">
      <text>最近数据更新: {{ timeText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { type Overview } from '../api/devices';
import { formatTime } from '../utils/format';

const props = defineProps<{
  overview: Overview | null;
}>();

function fmt(v: number | null | undefined): string {
  if (v == null) return '--';
  if (v >= 1000000) return (v / 1000000).toFixed(2) + ' M';
  if (v >= 10000) return (v / 10000).toFixed(2) + ' 万';
  return v.toFixed(1);
}

const onlineRate = computed(() => {
  const total = props.overview?.totalDevices ?? 0;
  if (total <= 0) return 0;
  return ((props.overview?.onlineCount ?? 0) / total) * 100;
});
const onlineRateText = computed(() => props.overview?.totalDevices ? `${onlineRate.value.toFixed(1)}%` : '--');
const timeText = computed(() => formatTime(props.overview?.lastUpdatedAt));
</script>

<style scoped>
.dashboard {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.dash-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
  display: block;
  margin-bottom: 24rpx;
}
.stat-cards {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.stat-card {
  flex: 1;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}
.stat-val {
  font-size: 40rpx;
  font-weight: 700;
  color: #2979ff;
  line-height: 1.2;
}
.stat-val.orange { color: #ff9100; }
.stat-val.green { color: #2e7d32; }
.stat-unit {
  font-size: 20rpx;
  color: #999;
  margin-top: -4rpx;
}
.stat-label {
  font-size: 22rpx;
  color: #888;
  margin-top: 6rpx;
}
.ratio-section {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx;
}
.ratio-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.ratio-label {
  font-size: 26rpx;
  color: #666;
}
.ratio-pct {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
}
.ratio-bar {
  height: 24rpx;
  border-radius: 12rpx;
  overflow: hidden;
  display: flex;
}
.ratio-fill.output {
  background: linear-gradient(90deg, #2979ff, #448aff);
  height: 100%;
  border-radius: 12rpx 0 0 12rpx;
}
.ratio-fill.input {
  background: linear-gradient(90deg, #ffa726, #ff9100);
  height: 100%;
  border-radius: 0 12rpx 12rpx 0;
}
.ratio-legend {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  margin-top: 16rpx;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 22rpx;
  color: #888;
}
.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 4rpx;
}
.dot.blue { background: #2979ff; }
.dot.orange { background: #ff9100; }
.update-time {
  margin-top: 20rpx;
  text-align: center;
  font-size: 22rpx;
  color: #bbb;
}
</style>
