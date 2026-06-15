<template>
  <view class="param-item">
    <text class="label">{{ label }}</text>
    <view class="value-row">
      <text class="value" :style="{ color: highlight ? '#2979ff' : '#333' }">{{ displayValue }}</text>
      <text v-if="unit" class="unit">{{ unit }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatNumber } from '../utils/format';

const props = defineProps<{
  label: string;
  value: number | null | undefined;
  unit?: string;
  highlight?: boolean;
}>();

const displayValue = computed(() => formatNumber(props.value, props.value != null && props.value < 10 ? 3 : 2));
</script>

<style scoped>
.param-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 8rpx;
}
.label {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 6rpx;
}
.value-row {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}
.value {
  font-size: 30rpx;
  font-weight: 600;
}
.unit {
  font-size: 20rpx;
  color: #999;
}
</style>
