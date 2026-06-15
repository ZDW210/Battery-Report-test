<template>
  <view class="page">
    <!-- 筛选 -->
    <view class="filters">
      <picker class="picker" mode="selector" :range="levelOptions" @change="onLevelChange">
        <view class="picker-item">
          <text>{{ levelFilter ? ('等级: ' + levelOptions[parseInt(levelFilter)]) : '全部等级' }}</text>
        </view>
      </picker>
    </view>

    <!-- 列表 -->
    <view class="list" v-if="alarmStore.list.length">
      <AlarmItem
        v-for="a in alarmStore.list"
        :key="a.id"
        :code="a.code"
        :level="a.level"
        :title-zh="a.titleZh"
        :device-name="a.device?.name || a.device?.meterNo || '未知设备'"
        :created-at="a.createdAt"
        @click="goDetail(a.id)"
      />
    </view>
    <EmptyState v-else text="暂无报警记录" icon="✅" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useAlarmStore } from '../../stores/alarm';
import AlarmItem from '../../components/AlarmItem.vue';
import EmptyState from '../../components/EmptyState.vue';
import { ALARM_LEVEL_MAP } from '../../utils/constants';

const alarmStore = useAlarmStore();
const levelFilter = ref('');
const ALARM_LEVELS = Object.keys(ALARM_LEVEL_MAP).sort((a, b) => parseInt(a) - parseInt(b));
const levelOptions = ['', ...ALARM_LEVELS.map(k => ALARM_LEVEL_MAP[k].label)];

let deviceId = '';

function onLevelChange(e: any) {
  const idx = e.detail.value;
  levelFilter.value = idx === 0 ? '' : ALARM_LEVELS[idx - 1];
  alarmStore.fetchAlarms({
    deviceId: deviceId || undefined,
    level: levelFilter.value || undefined,
  });
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/alarms/detail?id=${id}` });
}

onLoad((opts: any) => {
  if (opts.deviceId) deviceId = opts.deviceId;
  alarmStore.fetchAlarms({ deviceId: deviceId || undefined });
});
</script>

<style scoped>
.page {
  padding: 16rpx;
}
.filters {
  margin-bottom: 16rpx;
}
.picker-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  color: #666;
}
.list {
  padding-bottom: 32rpx;
}
</style>
