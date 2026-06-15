<template>
  <view class="card" @click="$emit('click')">
    <view class="top">
      <view class="left">
        <view class="power-pill">
          <text class="power-num">{{ pText }}</text>
          <text class="power-label">功率</text>
        </view>
      </view>
      <view class="right">
        <view class="title-row">
          <text class="name">{{ displayName }}</text>
          <StatusBadge :status="status" />
        </view>
        <text class="mode-tag" v-if="modeLabel" :class="modeClass">{{ modeLabel }}</text>
        <view class="info-grid">
          <text class="info">电压 {{ voltageText }}</text>
          <text class="info">电流 {{ currentText }}</text>
          <text class="info">功率 {{ pText }}</text>
          <text class="info">PF {{ pfText }}</text>
        </view>
        <view class="energy-row">
          <text class="en-text" v-if="lastEpi != null">有功电能 {{ fmt(lastEpi) }}kWh</text>
        </view>
        <view class="bottom">
          <text class="project" v-if="projectName">{{ projectName }}</text>
          <text class="coord" v-if="lat != null">定位 {{ lat }},{{ lng }}</text>
          <text class="time">{{ timeText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatTimeAgo } from '../utils/format';
import StatusBadge from './StatusBadge.vue';

const props = defineProps<{
  name: string | null;
  meterNo: string;
  status: string;
  projectName?: string | null;
  lastUa?: number | null;
  lastUb?: number | null;
  lastUc?: number | null;
  lastVoltage?: number | null;
  lastCurrent?: number | null;
  lastP?: number | null;
  lastPf?: number | null;
  lastEpi?: number | null;
  lastReadingAt?: string | null;
  runMode?: string | null;
  latitude?: number | null;
  longitude?: number | null;
}>();

defineEmits(['click']);

const displayName = computed(() => props.name || props.meterNo);

const modeLabel = computed(() => {
  const m = props.runMode;
  if (!m) return '';
  const map: Record<string, string> = {
    CHARGING: '充电中', DISCHARGING: '放电中', STANDBY: '待机', FAULT: '故障',
  };
  return map[m] || m;
});

const modeClass = computed(() => {
  const m = props.runMode;
  if (m === 'CHARGING') return 'mode-charge';
  if (m === 'DISCHARGING') return 'mode-discharge';
  if (m === 'FAULT') return 'mode-fault';
  return 'mode-standby';
});

const voltageText = computed(() => props.lastVoltage != null ? props.lastVoltage.toFixed(1) + 'V' : '--');
const currentText = computed(() => props.lastCurrent != null ? props.lastCurrent.toFixed(2) + 'A' : '--');
const pText = computed(() => props.lastP != null ? props.lastP.toFixed(2) + 'kW' : '--');
const pfText = computed(() => props.lastPf != null ? props.lastPf.toFixed(3) : '--');

function fmt(v: number): string {
  if (v >= 10000) return (v / 10000).toFixed(1) + '万';
  if (v >= 1000) return (v / 1000).toFixed(1) + 'k';
  return v.toFixed(0);
}

const timeText = computed(() => formatTimeAgo(props.lastReadingAt));
const lat = computed(() => props.latitude?.toFixed(4));
const lng = computed(() => props.longitude?.toFixed(4));
</script>

<style scoped>
.card {
  background: #fff; border-radius: 16rpx; padding: 20rpx;
  margin-bottom: 16rpx; box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.06);
}
.top { display: flex; gap: 16rpx; align-items: flex-start; }

.power-pill {
  width: 112rpx; height: 96rpx; border-radius: 14rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8f0fe, #f8fbff);
  border: 2rpx solid #dbeafe; flex-shrink: 0;
}
.power-num { font-size: 24rpx; font-weight: 700; color: #1d4ed8; max-width: 100rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.power-label { font-size: 18rpx; color: #64748b; margin-top: 4rpx; }

.right { flex: 1; min-width: 0; }
.title-row { display: flex; align-items: center; justify-content: space-between; }
.name { font-size: 30rpx; font-weight: 600; color: #1a1a1a; }

.mode-tag {
  display: inline-block; font-size: 20rpx; padding: 2rpx 14rpx; border-radius: 4rpx; margin: 6rpx 0 8rpx;
}
.mode-charge { background: #e8f5e9; color: #2e7d32; }
.mode-discharge { background: #fff3e0; color: #e65100; }
.mode-standby { background: #f5f5f5; color: #999; }
.mode-fault { background: #fce4ec; color: #c62828; }

.info-grid { display: flex; flex-wrap: wrap; gap: 8rpx 20rpx; }
.info { font-size: 22rpx; color: #666; }

.energy-row { display: flex; gap: 20rpx; margin: 6rpx 0; }
.en-text { font-size: 22rpx; color: #888; }

.bottom { display: flex; justify-content: space-between; margin-top: 4rpx; }
.project { font-size: 20rpx; color: #999; }
.coord { font-size: 20rpx; color: #bbb; }
.time { font-size: 20rpx; color: #bbb; flex-shrink: 0; }
</style>
