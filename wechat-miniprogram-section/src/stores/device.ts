import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getDevices, getDeviceDetail, getDeviceLatest, getOverview, getEnergyDelta, type Device, type DeviceDetail, type Overview, type EnergyDelta } from '../api/devices';
import { sessionApi, type RevenueOverview, type TodayEnergy, type ChargingSession } from '../api/sessions';

export const useDeviceStore = defineStore('device', () => {
  const list = ref<Device[]>([]);
  const total = ref(0);
  const current = ref<DeviceDetail | null>(null);
  const overview = ref<Overview | null>(null);
  const latestReading = ref<any>(null);
  const energyDelta = ref<EnergyDelta | null>(null);
  const revenueOverview = ref<RevenueOverview | null>(null);
  const todayEnergy = ref<TodayEnergy | null>(null);
  const offlineDeviceCount = ref(0);
  const faultCount = ref(0);
  const sessions = ref<ChargingSession[]>([]);

  async function fetchDevices(params?: {
    page?: number;
    pageSize?: number;
    status?: string;
    search?: string;
  }) {
    const res = await getDevices({ page: 1, pageSize: 20, ...params });
    list.value = res.list;
    total.value = res.list.length;
  }

  async function fetchOverview(params?: { from?: string; to?: string }) {
    overview.value = await getOverview(params);
  }

  async function fetchDeviceDetail(id: string) {
    current.value = await getDeviceDetail(id);
  }

  async function fetchLatestReading(id: string) {
    latestReading.value = await getDeviceLatest(id);
  }

  async function fetchEnergyDelta(params?: { from?: string; to?: string }) {
    energyDelta.value = await getEnergyDelta(params);
  }

  async function fetchRevenueOverview() {
    try {
      revenueOverview.value = await sessionApi.getRevenueOverview();
    } catch (e) {
      console.error('fetchRevenueOverview failed:', e);
    }
  }

  async function fetchTodayEnergy() {
    try {
      todayEnergy.value = await sessionApi.getTodayEnergy();
    } catch (e) {
      console.error('fetchTodayEnergy failed:', e);
    }
  }

  async function fetchSessions(params?: { from?: string; to?: string; status?: string; limit?: number }) {
    try {
      sessions.value = await sessionApi.list(params);
    } catch (e) {
      console.error('fetchSessions failed:', e);
    }
  }

  function recomputeStatusCounts(devices: Device[]) {
    offlineDeviceCount.value = devices.filter(d => d.status !== 'ONLINE').length;
    faultCount.value = devices.filter(d => d.runMode === 'FAULT').length;
  }

  return {
    list, total, current, overview, latestReading, energyDelta,
    revenueOverview, todayEnergy, offlineDeviceCount, faultCount, sessions,
    fetchDevices, fetchOverview, fetchDeviceDetail, fetchLatestReading,
    fetchEnergyDelta, fetchRevenueOverview, fetchTodayEnergy, fetchSessions, recomputeStatusCounts,
  };
});
