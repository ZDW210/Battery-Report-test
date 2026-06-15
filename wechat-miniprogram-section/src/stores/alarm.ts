import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getAlarms, type AlarmItem } from '../api/alarms';

export const useAlarmStore = defineStore('alarm', () => {
  const list = ref<AlarmItem[]>([]);
  const total = ref(0);

  async function fetchAlarms(params?: {
    page?: number;
    pageSize?: number;
    deviceId?: string;
    level?: string;
    code?: string;
    from?: string;
    to?: string;
  }) {
    const res = await getAlarms({ page: 1, pageSize: 20, ...params });
    list.value = res.list;
    total.value = res.total;
  }

  return { list, total, fetchAlarms };
});
