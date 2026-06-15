<template>
  <view class="page">
    <view v-if="alarm" class="content">
      <!-- 报警头 -->
      <view class="header-card">
        <view class="badge-row">
          <view class="level-badge" :style="{ background: levelColor }">{{ levelLabel }}</view>
          <text class="code">{{ alarm.code }}</text>
        </view>
        <text class="title">{{ alarm.titleZh || alarm.code }}</text>
        <text class="time">报警时间: {{ alarm.createdAt }}</text>
      </view>

      <!-- 设备信息 -->
      <view class="info-card">
        <view class="info-row">
          <text class="info-label">设备</text>
          <text class="info-val">{{ alarm.device?.name || alarm.device?.meterNo || '--' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">项目</text>
          <text class="info-val">{{ alarm.device?.projectName || '--' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">报警编号</text>
          <text class="info-val">{{ alarm.alarmNo }}</text>
        </view>
      </view>

      <!-- 描述 -->
      <view class="desc-card" v-if="alarm.messageZh">
        <text class="desc-title">报警详情</text>
        <text class="desc-text">{{ alarm.messageZh }}</text>
      </view>

      <!-- 跳转设备 -->
      <view class="actions">
        <button class="btn" @click="goDevice">查看设备详情</button>
      </view>
    </view>
    <EmptyState v-else-if="loading" text="加载中..." icon="⏳" />
    <EmptyState v-else text="加载失败" icon="⚠️">
      <button class="retry-btn" @click="loadAlarm(currentId)">重试</button>
    </EmptyState>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getAlarmDetail, type AlarmItem } from '../../api/alarms';
import { ALARM_LEVEL_MAP } from '../../utils/constants';
import EmptyState from '../../components/EmptyState.vue';

const alarm = ref<AlarmItem | null>(null);
const loading = ref(true);
let currentId = '';

const levelLabel = computed(() => ALARM_LEVEL_MAP[alarm.value?.level || '']?.label || '未知');
const levelColor = computed(() => ALARM_LEVEL_MAP[alarm.value?.level || '']?.color || '#999');

function goDevice() {
  uni.navigateTo({ url: `/pages/device/detail?id=${alarm.value?.deviceId}` });
}

async function loadAlarm(id: string) {
  loading.value = true;
  try {
    alarm.value = await getAlarmDetail(id);
  } catch {
    uni.showToast({ title: '加载失败，请重试', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onLoad((opts: any) => {
  const id = opts?.id;
  if (!id || typeof id !== 'string' || !id.trim()) {
    uni.showToast({ title: '报警ID无效', icon: 'none' });
    uni.navigateBack();
    return;
  }
  currentId = id;
  loadAlarm(id);
});
</script>

<style scoped>
.page {
  padding: 16rpx;
}
.header-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}
.badge-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}
.level-badge {
  padding: 6rpx 20rpx;
  border-radius: 8rpx;
  color: #fff;
  font-size: 24rpx;
}
.code {
  font-size: 24rpx;
  color: #999;
}
.title {
  font-size: 32rpx;
  font-weight: 600;
  display: block;
  margin-bottom: 8rpx;
}
.time {
  font-size: 24rpx;
  color: #999;
}
.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 26rpx; color: #999; }
.info-val { font-size: 26rpx; color: #333; }
.desc-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}
.desc-title {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
  display: block;
}
.desc-text {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
}
.actions { margin-top: 32rpx; }
.btn {
  background: #2979ff;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
  padding: 20rpx 0;
  text-align: center;
}
.retry-btn {
  margin-top: 20rpx;
  background: #2979ff;
  color: #fff;
  border-radius: 8rpx;
  font-size: 24rpx;
  padding: 10rpx 32rpx;
}
</style>
