<template>
  <view class="chart-wrap" v-for="g in groups" :key="g.unit">
    <view class="strip-hd">
      <text class="strip-unit">{{ g.unit }}</text>
      <view class="strip-tags">
        <view class="tag" v-for="(p, i) in g.params" :key="p">
          <view class="dot" :style="{ background: chartColors[i % chartColors.length] }"></view>
          <text>{{ labelMap?.[p] || p }}</text>
        </view>
      </view>
      <text class="strip-range">{{ g.rangeText }}</text>
    </view>
    <canvas
      type="2d"
      :id="'grp_' + g.unit"
      class="strip-canvas"
      :style="{ width: canvasWidth + 'px', height: stripH + 'px' }"
    ></canvas>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick, getCurrentInstance } from 'vue';

const props = defineProps<{
  data: any[];
  params: string[];
  loading?: boolean;
  labelMap?: Record<string, string>;
}>();

const chartColors = ['#2979ff', '#00c853', '#ff9100', '#ff1744', '#9c27b0', '#00bcd4'];
const unitMap: Record<string, string> = {
  Ua: 'V', Ub: 'V', Uc: 'V',
  Ia: 'A', Ib: 'A', Ic: 'A',
  P: 'kW', Pa: 'kW', Pb: 'kW', Pc: 'kW',
  PF: '', EPI: 'kWh',
};

const canvasWidth = ref(340);
const stripH = ref(180);
const instance = getCurrentInstance();

interface Group {
  unit: string;
  params: string[];
  rangeText: string;
}
interface ParamRange { min: number; max: number; }

const ranges = ref<Record<string, ParamRange>>({});

const groups = computed<Group[]>(() => {
  const map = new Map<string, string[]>();
  for (const p of props.params) {
    const u = unitMap[p] || '?';
    if (!map.has(u)) map.set(u, []);
    map.get(u)!.push(p);
  }
  return [...map.entries()].map(([unit, params]) => {
    let allMin = Infinity, allMax = -Infinity;
    for (const p of params) {
      const r = ranges.value[p];
      if (r) { if (r.min < allMin) allMin = r.min; if (r.max > allMax) allMax = r.max; }
    }
    const rt = isFinite(allMin) ? `${allMin.toFixed(1)} ~ ${allMax.toFixed(1)}` : '';
    return { unit, params, rangeText: rt };
  });
});

function computeRanges() {
  const result: Record<string, ParamRange> = {};
  for (const p of props.params) {
    let min = Infinity, max = -Infinity;
    for (const row of props.data) {
      const v = row[p] ?? row[p.toLowerCase()];
      if (v != null) { if (v < min) min = v; if (v > max) max = v; }
    }
    const range = max - min || 1;
    result[p] = { min: min - range * 0.08, max: max + range * 0.08 };
  }
  ranges.value = result;
}

function drawAll() {
  computeRanges();
  nextTick(() => {
    for (const g of groups.value) {
      drawGroup(g);
    }
  });
}

function drawGroup(g: Group) {
  const query = uni.createSelectorQuery().in(instance);
  query.select('#grp_' + g.unit).fields({ node: true, size: true }).exec((res) => {
    if (!res[0]?.node) return;
    const canvas = res[0].node;
    const ctx = canvas.getContext('2d');
    const dpr = uni.getSystemInfoSync().pixelRatio;
    const w = parseInt(res[0].width);
    const h = parseInt(res[0].height);
    canvas.width = w * dpr;
    canvas.height = h * dpr;
    ctx.scale(dpr, dpr);

    const pad = { top: 8, right: 16, bottom: 22, left: 50 };
    const plotW = w - pad.left - pad.right;
    const plotH = h - pad.top - pad.bottom;
    ctx.clearRect(0, 0, w, h);

    const { data } = props;
    if (!data?.length) return;

    // 计算整组的 Y 轴范围
    let groupMin = Infinity, groupMax = -Infinity;
    for (const p of g.params) {
      const r = ranges.value[p];
      if (r) { if (r.min < groupMin) groupMin = r.min; if (r.max > groupMax) groupMax = r.max; }
    }
    if (!isFinite(groupMin)) return;
    const yRange = groupMax - groupMin || 1;

    // Y 轴刻度
    ctx.fillStyle = '#999';
    ctx.font = '9px sans-serif';
    ctx.textAlign = 'right';
    for (let i = 0; i <= 2; i++) {
      const val = groupMax - (yRange / 2) * i;
      const y = pad.top + (plotH / 2) * i;
      let text: string;
      if (Math.abs(val) >= 10000) text = (val / 1000).toFixed(0) + 'k';
      else if (Math.abs(val) >= 1000) text = (val / 1000).toFixed(1) + 'k';
      else if (Math.abs(val) >= 10) text = val.toFixed(1);
      else if (Math.abs(val) >= 1) text = val.toFixed(2);
      else text = val.toFixed(3);
      ctx.fillText(text, pad.left - 4, y + 3);
      ctx.strokeStyle = '#f5f5f5';
      ctx.beginPath();
      ctx.moveTo(pad.left, y);
      ctx.lineTo(w - pad.right, y);
      ctx.stroke();
    }

    // 每条数据线
    const stepX = plotW / Math.max(data.length - 1, 1);
    for (let pi = 0; pi < g.params.length; pi++) {
      const p = g.params[pi];
      ctx.strokeStyle = chartColors[pi % chartColors.length];
      ctx.lineWidth = 1.8;
      ctx.beginPath();
      let first = true;
      for (let i = 0; i < data.length; i++) {
        const v = data[i][p] ?? data[i][p.toLowerCase()];
        if (v == null) { first = true; continue; }
        const x = pad.left + i * stepX;
        const y = pad.top + plotH - ((v - groupMin) / yRange) * plotH;
        if (first) { ctx.moveTo(x, y); first = false; }
        else ctx.lineTo(x, y);
      }
      ctx.stroke();
    }

    // X 轴时间
    ctx.fillStyle = '#bbb';
    ctx.font = '9px sans-serif';
    ctx.textAlign = 'center';
    const n = Math.min(4, data.length);
    for (let i = 0; i < n; i++) {
      const idx = Math.round((i / (n - 1 || 1)) * (data.length - 1));
      const ts = data[idx]?.ts;
      if (!ts) continue;
      const d = new Date(ts);
      const label = `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
      const x = pad.left + idx * (plotW / Math.max(data.length - 1, 1));
      ctx.fillText(label, x, h - 3);
    }
  });
}

onMounted(drawAll);
watch(() => [props.data, props.params], drawAll, { deep: true });
</script>

<style scoped>
.chart-wrap {
  background: #fff;
  border-radius: 12rpx;
  padding: 12rpx 16rpx 8rpx;
  margin-bottom: 12rpx;
}
.strip-hd {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 4rpx;
}
.strip-unit {
  font-size: 22rpx; font-weight: 700; color: #555; flex-shrink: 0; min-width: 50rpx;
}
.strip-tags {
  display: flex; gap: 12rpx; flex: 1; flex-wrap: wrap;
}
.tag {
  display: flex; align-items: center; gap: 4rpx; font-size: 20rpx; color: #666;
}
.dot {
  width: 10rpx; height: 10rpx; border-radius: 50%; flex-shrink: 0;
}
.strip-range {
  font-size: 20rpx; color: #bbb; flex-shrink: 0;
}
.strip-canvas {
  width: 100%;
  height: 340rpx;
}
</style>
