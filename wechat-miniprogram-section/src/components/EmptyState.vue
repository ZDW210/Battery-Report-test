<template>
  <view class="empty">
    <text class="icon">{{ icon }}</text>
    <text class="text">{{ text }}</text>
    <slot />
  </view>
</template>

<script setup lang="ts">
defineProps<{ text?: string; icon?: string }>();
</script>

<style scoped>
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}
.icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}
.text {
  font-size: 26rpx;
  color: #bbb;
}
</style>
