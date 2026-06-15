<template>
  <view class="page">
    <view v-if="device" class="content">
      <!-- 设备信息 -->
      <view class="header-card">
        <view class="hc-top">
          <view class="status-side">
            <view class="live-tile">
              <text class="live-value">{{ powerText }}</text>
              <text class="live-label">当前功率</text>
            </view>
            <text class="mode-tag" v-if="modeLabel" :class="modeClass">{{ modeLabel }}</text>
          </view>
          <view class="info-side">
            <view class="title-row">
              <text class="name">{{ device.name || device.meterNo }}</text>
              <StatusBadge :status="device.status" />
            </view>
            <text class="meter-no">{{ device.meterNo }}</text>
            <text class="project" v-if="device.projectName">{{ device.projectName }}</text>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">基础信息</text>
        </view>
        <view class="base-card">
          <view class="base-row">
            <text class="base-label">项目编号</text>
            <text class="base-value">{{ device.projectCode || '--' }}</text>
          </view>
          <view class="base-row">
            <text class="base-label">安装位置</text>
            <text class="base-value">{{ device.location || '--' }}</text>
          </view>
          <view class="base-row">
            <text class="base-label">设备类型</text>
            <text class="base-value">{{ device.deviceType || '--' }}</text>
          </view>
          <view class="base-row">
            <text class="base-label">归属客户</text>
            <text class="base-value">{{ customerName || '--' }}</text>
          </view>
          <view class="base-row">
            <text class="base-label">网关编号</text>
            <text class="base-value">{{ device.gatewaySn }}</text>
          </view>
          <view class="base-row">
            <text class="base-label">电表编号</text>
            <text class="base-value">{{ device.meterSn }}</text>
          </view>
        </view>
      </view>

      <!-- GPS 位置 -->
      <view class="section" v-if="device.latitude != null">
        <text class="section-title">当前位置</text>
        <view class="gps-row">
          <text class="gps-text">{{ device.latitude?.toFixed(6) }}, {{ device.longitude?.toFixed(6) }}</text>
        </view>
      </view>

      <!-- 三相电压 -->
      <view class="section">
        <text class="section-title">交流侧 — 三相电压</text>
        <ParamGrid
          :params="[
            { label: 'A相电压', value: device.lastUa, unit: 'V' },
            { label: 'B相电压', value: device.lastUb, unit: 'V' },
            { label: 'C相电压', value: device.lastUc, unit: 'V' },
          ]"
        />
      </view>

      <!-- 三相电流 -->
      <view class="section">
        <text class="section-title">交流侧 — 三相电流</text>
        <ParamGrid
          :params="[
            { label: 'A相电流', value: device.lastIa, unit: 'A' },
            { label: 'B相电流', value: device.lastIb, unit: 'A' },
            { label: 'C相电流', value: device.lastIc, unit: 'A' },
          ]"
        />
      </view>

      <!-- 功率与电能 -->
      <view class="section">
        <text class="section-title">功率与电能</text>
        <ParamGrid
          :params="[
            { label: '有功功率', value: device.lastP, unit: 'kW', highlight: true },
            { label: '功率因数', value: device.lastPf, unit: '' },
            { label: '正向有功电能', value: device.lastEpi, unit: 'kWh', highlight: true },
          ]"
        />
      </view>

      <!-- 最近报警 -->
      <view class="section" v-if="device.recentAlarms?.length">
        <view class="section-header">
          <text class="section-title">最近报警</text>
          <text class="link" @click="goAlarms">全部 ></text>
        </view>
        <AlarmItem
          v-for="a in device.recentAlarms"
          :key="a.id"
          :code="a.code"
          :level="a.level"
          :title-zh="a.titleZh"
          :device-name="device.name || device.meterNo"
          :created-at="a.createdAt"
          @click="goAlarmDetail(a.id)"
        />
      </view>

      <!-- 操作按钮 -->
      <view class="actions">
        <button class="btn" @click="goTrends">查看历史趋势</button>
      </view>
    </view>
    <EmptyState v-else-if="loading" text="加载中..." icon="⏳" />
    <EmptyState v-else text="加载失败" icon="⚠️">
      <button class="retry-btn" @tap="loadDevice">重试</button>
    </EmptyState>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useDeviceStore } from '../../stores/device';
import StatusBadge from '../../components/StatusBadge.vue';
import ParamGrid from '../../components/ParamGrid.vue';
import AlarmItem from '../../components/AlarmItem.vue';
import EmptyState from '../../components/EmptyState.vue';

const deviceStore = useDeviceStore();
const device = computed(() => deviceStore.current);
const loading = ref(true);
let timer: any = null;
let currentId = '';

const customerName = computed(() => {
  const id = device.value?.customerId;
  return id ? `客户 ${id}` : '';
});
const powerText = computed(() => {
  const power = device.value?.lastP;
  return power != null ? `${power.toFixed(2)}kW` : '--';
});

const modeLabel = computed(() => {
  const m = device.value?.runMode;
  if (!m) return '';
  const map: Record<string, string> = { CHARGING: '充电中', DISCHARGING: '放电中', STANDBY: '待机', FAULT: '故障' };
  return map[m] || m;
});
const modeClass = computed(() => {
  const m = device.value?.runMode;
  if (m === 'CHARGING') return 'mode-charge';
  if (m === 'DISCHARGING') return 'mode-discharge';
  if (m === 'FAULT') return 'mode-fault';
  return 'mode-standby';
});

async function loadDevice(id: string) {
  loading.value = true;
  try {
    await deviceStore.fetchDeviceDetail(id);
  } catch {
    uni.showToast({ title: '加载失败，请重试', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onLoad((opts: any) => {
  const id = opts?.id;
  if (!id || typeof id !== 'string' || !id.trim()) {
    uni.showToast({ title: '设备ID无效', icon: 'none' });
    uni.navigateBack();
    return;
  }
  currentId = id;
  loadDevice(id);
  timer = setInterval(() => {
    deviceStore.fetchLatestReading(id).catch(() => {});
  }, 30000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

function goTrends() {
  uni.navigateTo({ url: `/pages/device/trends?id=${device.value?.id}` });
}
function goAlarms() {
  uni.navigateTo({ url: `/pages/alarms/list?deviceId=${device.value?.id}` });
}
function goAlarmDetail(id: string) {
  uni.navigateTo({ url: `/pages/alarms/detail?id=${id}` });
}

</script>

<style scoped>
.page { padding: 16rpx; }

.header-card {
  background: #fff; border-radius: 16rpx; padding: 24rpx;
  margin-bottom: 16rpx; box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.hc-top { display: flex; gap: 20rpx; align-items: flex-start; }

.status-side { display: flex; flex-direction: column; align-items: center; gap: 10rpx; flex-shrink: 0; }
.live-tile {
  width: 132rpx; height: 100rpx; border-radius: 14rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8f0fe, #f8fbff);
  border: 2rpx solid #dbeafe;
}
.live-value { font-size: 24rpx; font-weight: 700; color: #1d4ed8; max-width: 120rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.live-label { font-size: 18rpx; color: #64748b; margin-top: 4rpx; }

.mode-tag { font-size: 20rpx; padding: 2rpx 14rpx; border-radius: 4rpx; }
.mode-charge { background: #e8f5e9; color: #2e7d32; }
.mode-discharge { background: #fff3e0; color: #e65100; }
.mode-standby { background: #f5f5f5; color: #999; }
.mode-fault { background: #fce4ec; color: #c62828; }

.info-side { flex: 1; min-width: 0; }
.title-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8rpx; }
.name { font-size: 32rpx; font-weight: 700; }
.meter-no { font-size: 22rpx; color: #bbb; display: block; margin-bottom: 4rpx; }
.project { font-size: 24rpx; color: #999; }

.section { margin-bottom: 16rpx; }
.section-title { font-size: 26rpx; color: #555; margin-bottom: 12rpx; display: block; font-weight: 600; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.link { font-size: 24rpx; color: #2979ff; }

.base-card { background: #fff; border-radius: 12rpx; padding: 8rpx 24rpx; }
.base-row {
  display: flex; justify-content: space-between; align-items: center;
  gap: 24rpx; padding: 18rpx 0;
}
.base-row:not(:last-child) { border-bottom: 1rpx solid #f0f0f0; }
.base-label { font-size: 24rpx; color: #999; flex-shrink: 0; }
.base-value {
  font-size: 24rpx; color: #333; text-align: right;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.gps-row { background: #f8f9fb; border-radius: 10rpx; padding: 16rpx; }
.gps-text { font-size: 26rpx; color: #555; }

.actions { margin-top: 32rpx; }
.btn { background: #2979ff; color: #fff; border-radius: 12rpx; font-size: 28rpx; padding: 20rpx 0; text-align: center; }
.retry-btn {
  margin-top: 24rpx; background: #2979ff; color: #fff;
  border-radius: 8rpx; font-size: 24rpx; padding: 10rpx 40rpx;
}
.mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center;
  z-index: 100;
}
.dialog {
  background: #fff; border-radius: 16rpx; padding: 36rpx;
  width: 620rpx; max-width: 84vw; max-height: 86vh; overflow-y: auto;
}
.dlg-title { font-size: 32rpx; font-weight: 700; display: block; margin-bottom: 24rpx; text-align: center; }
.dlg-label { font-size: 22rpx; color: #666; display: block; margin: 12rpx 0 8rpx; }
.dlg-input,
.picker-value {
  background: #f5f5f5; border-radius: 8rpx; padding: 16rpx;
  font-size: 26rpx; min-height: 38rpx; color: #333;
}
.dlg-btns { display: flex; gap: 16rpx; margin-top: 28rpx; }
.dlg-btn { flex: 1; border-radius: 10rpx; font-size: 26rpx; padding: 16rpx 0; text-align: center; }
.dlg-btn.cancel { background: #f5f5f5; color: #666; }
.dlg-btn.confirm { background: #0f3460; color: #fff; }
</style>
