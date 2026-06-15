<template>
  <view class="energy-card">
    <text class="title">电量统计</text>

    <!-- 输出电量 -->
    <view class="bar-row">
      <text class="bar-label">正向有功电能（输出）</text>
      <view class="bar-track">
        <view class="bar-fill output" :style="{ width: outputPercent + '%' }"></view>
      </view>
      <text class="bar-val">{{ outputText }}</text>
    </view>

    <!-- 输入电量 -->
    <view class="bar-row">
      <text class="bar-label">反向有功电能（输入）</text>
      <view class="bar-track">
        <view class="bar-fill input" :style="{ width: inputPercent + '%' }"></view>
      </view>
      <text class="bar-val">{{ inputText }}</text>
    </view>

    <!-- 底部统计 -->
    <view class="footer-stats">
      <text class="stat">输出占比 {{ ratioText }}</text>
      <text class="stat">合计 {{ totalText }} kWh</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  epi: number | null | undefined;
  epe: number | null | undefined;
}>();

function fmt(v: number | null | undefined): string {
  if (v == null) return '--';
  if (v >= 1000000) return (v / 1000000).toFixed(1) + 'M';
  if (v >= 1000) return (v / 1000).toFixed(1) + 'k';
  return v.toFixed(1);
}

const epiVal = computed(() => props.epi ?? 0);
const epeVal = computed(() => props.epe ?? 0);
const total = computed(() => epiVal.value + epeVal.value);
const maxVal = computed(() => Math.max(epiVal.value, epeVal.value, 1));
const outputPercent = computed(() => (epiVal.value / maxVal.value) * 100);
const inputPercent = computed(() => (epeVal.value / maxVal.value) * 100);
const outputText = computed(() => fmt(props.epi) + ' kWh');
const inputText = computed(() => fmt(props.epe) + ' kWh');
const totalText = computed(() => fmt(total.value));
const ratioText = computed(() => {
  if (total.value <= 0) return '--%';
  return ((epiVal.value / total.value) * 100).toFixed(1) + '%';
});
</script>

<style scoped>
.energy-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
}
.title {
  font-size: 30rpx;
  font-weight: 700;
  color: #333;
  display: block;
  margin-bottom: 24rpx;
}
.bar-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.bar-label {
  width: 280rpx;
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;
}
.bar-track {
  flex: 1;
  height: 36rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin: 0 16rpx;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 8rpx;
  min-width: 8rpx;
  transition: width 0.3s ease;
}
.bar-fill.output {
  background: linear-gradient(90deg, #2979ff, #448aff);
}
.bar-fill.input {
  background: linear-gradient(90deg, #ff9100, #ffa726);
}
.bar-val {
  width: 160rpx;
  font-size: 26rpx;
  font-weight: 500;
  color: #333;
  text-align: right;
  flex-shrink: 0;
}
.footer-stats {
  display: flex;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  margin-top: 8rpx;
}
.stat {
  font-size: 24rpx;
  color: #999;
}
</style>
