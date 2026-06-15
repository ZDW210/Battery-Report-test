<template>
  <view class="alarm-item" @click="$emit('click')">
    <view class="left">
      <view class="badge" :style="{ background: levelColor }">{{ levelLabel }}</view>
    </view>
    <view class="center">
      <text class="title">{{ title }}</text>
      <text class="subtitle">{{ deviceName }} · {{ timeText }}</text>
    </view>
    <view class="right">
      <text class="arrow">></text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ALARM_LEVEL_MAP, ALARM_CODE_MAP } from '../utils/constants';
import { formatTime } from '../utils/format';

const props = defineProps<{
  code: string;
  level: string;
  titleZh: string | null;
  deviceName: string;
  createdAt: string;
}>();

defineEmits(['click']);

const levelInfo = computed(() => ALARM_LEVEL_MAP[props.level] || { label: '未知', color: '#999' });
const levelLabel = computed(() => levelInfo.value.label);
const levelColor = computed(() => levelInfo.value.color);
const title = computed(() => props.titleZh || ALARM_CODE_MAP[props.code] || props.code);
const timeText = computed(() => formatTime(props.createdAt));
</script>

<style scoped>
.alarm-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
.left {
  margin-right: 20rpx;
}
.badge {
  width: 72rpx;
  height: 40rpx;
  line-height: 40rpx;
  text-align: center;
  font-size: 20rpx;
  color: #fff;
  border-radius: 6rpx;
}
.center {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}
.subtitle {
  font-size: 22rpx;
  color: #999;
}
.right .arrow {
  font-size: 28rpx;
  color: #ccc;
}
</style>
