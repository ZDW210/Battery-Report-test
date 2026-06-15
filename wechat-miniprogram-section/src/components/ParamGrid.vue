<template>
  <view class="param-grid">
    <ParamItem
      v-for="item in params"
      :key="item.label"
      :label="item.label"
      :value="item.value"
      :unit="item.unit"
      :highlight="item.highlight"
    />
  </view>
</template>

<script setup lang="ts">
import ParamItem from './ParamItem.vue';

defineProps<{
  params: { label: string; value: number | null | undefined; unit?: string; highlight?: boolean }[];
}>();
</script>

<style scoped>
.param-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  background: #fff;
  border-radius: 16rpx;
}
</style>
