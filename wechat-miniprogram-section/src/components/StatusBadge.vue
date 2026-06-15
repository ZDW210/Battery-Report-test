<template>
  <view class="status-badge" :class="statusClass">
    <view class="dot"></view>
    <text>{{ statusText }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ status: string }>();

const statusClass = computed(() => (props.status === 'ONLINE' ? 'online' : 'offline'));
const statusText = computed(() => (props.status === 'ONLINE' ? '在线' : '离线'));
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}
.dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
}
.online {
  background: #e8f5e9;
  color: #2e7d32;
}
.online .dot {
  background: #4caf50;
}
.offline {
  background: #f5f5f5;
  color: #9e9e9e;
}
.offline .dot {
  background: #bdbdbd;
}
</style>
