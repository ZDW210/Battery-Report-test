<template>
  <view class="page">
    <!-- 设备名 -->
    <view class="device-info-bar">
      <text class="dname">{{ device?.name || device?.meterNo || '加载中...' }}</text>
    </view>

    <!-- 参数选择 -->
    <view class="param-selector">
      <text
        v-for="p in allParams"
        :key="p.key"
        class="param-chip"
        :class="{ active: selectedParams.includes(p.key) }"
        @click="toggleParam(p.key)"
      >{{ p.label }}</text>
    </view>

    <!-- 时间快捷 -->
    <view class="time-shortcuts">
      <text
        v-for="t in shortcuts"
        :key="t.label"
        class="time-chip"
        :class="{ active: activeShortcut === t.value }"
        @click="setTimeRange(t.value)"
      >{{ t.label }}</text>
    </view>

    <!-- 图表 -->
    <TrendChart v-if="chartData.length" :data="chartData" :params="selectedParams" :loading="loading" :labelMap="paramLabelMap" />
    <EmptyState v-else-if="loadError" text="加载失败" icon="⚠️">
      <button class="retry-btn" @click="initDevice(currentId)">重试</button>
    </EmptyState>
    <EmptyState v-else-if="!loading" text="选择参数后加载数据" icon="📊" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useDeviceStore } from '../../stores/device';
import { getDeviceReadings } from '../../api/devices';
import { PARAM_LABELS } from '../../utils/constants';
import TrendChart from '../../components/TrendChart.vue';
import EmptyState from '../../components/EmptyState.vue';

const deviceStore = useDeviceStore();
const device = computed(() => deviceStore.current);

const paramKeys = ['Ua', 'Ub', 'Uc', 'Ia', 'Ib', 'Ic', 'Pa', 'Pb', 'Pc', 'P', 'PF', 'EPI'];
const allParams = paramKeys.map(k => ({ key: k, label: PARAM_LABELS[k] || k }));
const paramLabelMap = PARAM_LABELS;
const selectedParams = ref(['Ua', 'Ub', 'Uc']);
const activeShortcut = ref('1d');
const chartData = ref<any[]>([]);
const loading = ref(false);
const loadError = ref(false);
let currentId = '';

const shortcuts = [
  { label: '1h', value: '1h' },
  { label: '6h', value: '6h' },
  { label: '1天', value: '1d' },
  { label: '7天', value: '7d' },
  { label: '30天', value: '30d' },
];

function getTimeRange(value: string) {
  const now = new Date();
  let from: Date;
  switch (value) {
    case '1h': from = new Date(now.getTime() - 3600000); break;
    case '6h': from = new Date(now.getTime() - 21600000); break;
    case '7d': from = new Date(now.getTime() - 604800000); break;
    case '30d': from = new Date(now.getTime() - 2592000000); break;
    default: from = new Date(now.getTime() - 86400000);
  }
  return { from: from.toISOString(), to: now.toISOString() };
}

async function loadData() {
  if (!device.value || !selectedParams.value.length) return;
  loading.value = true;
  try {
    const { from, to } = getTimeRange(activeShortcut.value);
    const res = await getDeviceReadings(device.value.id, {
      params: selectedParams.value.join(','),
      from,
      to,
    });
    chartData.value = res?.data || [];
  } finally {
    loading.value = false;
  }
}

function toggleParam(p: string) {
  const idx = selectedParams.value.indexOf(p);
  if (idx >= 0 && selectedParams.value.length > 1) {
    selectedParams.value.splice(idx, 1);
  } else if (idx < 0) {
    selectedParams.value.push(p);
  }
  loadData();
}

function setTimeRange(value: string) {
  activeShortcut.value = value;
  loadData();
}

async function initDevice(id: string) {
  loadError.value = false;
  try {
    await deviceStore.fetchDeviceDetail(id);
    loadData();
  } catch {
    loadError.value = true;
    uni.showToast({ title: '加载失败，请重试', icon: 'none' });
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
  initDevice(id);
});
</script>

<style scoped>
.page {
  padding: 16rpx;
}
.device-info-bar {
  margin-bottom: 16rpx;
}
.dname {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}
.param-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 12rpx;
}
.param-chip {
  padding: 8rpx 22rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
  background: #fff;
}
.param-chip.active {
  background: #e3f2fd;
  color: #2979ff;
}
.time-shortcuts {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.time-chip {
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
  background: #fff;
}
.time-chip.active {
  background: #2979ff;
  color: #fff;
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
