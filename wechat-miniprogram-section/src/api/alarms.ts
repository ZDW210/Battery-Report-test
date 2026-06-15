import { get } from './request';

export interface AlarmItem {
  id: string;
  deviceId: string;
  alarmNo: string;
  code: string;
  level: string;
  titleZh: string | null;
  titleEn: string | null;
  messageZh: string | null;
  messageEn: string | null;
  acknowledgedAt: string | null;
  createdAt: string;
  device: {
    id: string;
    name: string | null;
    meterNo: string;
    projectName: string | null;
  };
}

export interface PaginatedResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

function toAlarm(row: any): AlarmItem {
  return {
    id: row.alarm_no,
    deviceId: row.meter_no,
    alarmNo: row.alarm_no,
    code: row.code,
    level: row.level,
    titleZh: row.title_zh,
    titleEn: row.title_en,
    messageZh: row.message_zh,
    messageEn: row.message_en,
    acknowledgedAt: null,
    createdAt: row.created_at,
    device: {
      id: row.meter_no,
      name: null,
      meterNo: row.meter_no,
      projectName: row.project_name,
    },
  };
}

export async function getAlarms(params?: {
  page?: number;
  pageSize?: number;
  deviceId?: string;
  level?: string;
  code?: string;
  from?: string;
  to?: string;
  sort?: string;
}) {
  const page = params?.page || 1;
  const pageSize = params?.pageSize || 20;
  const res = await get<{ list: any[] }>('/alarms', {
    meterNo: params?.deviceId,
    limit: page * pageSize,
  });
  let list = (res.list || []).map(toAlarm);
  if (params?.level) list = list.filter((item) => item.level === params.level);
  if (params?.code) list = list.filter((item) => item.code === params.code);
  const start = (page - 1) * pageSize;
  return {
    list: list.slice(start, start + pageSize),
    total: list.length,
    page,
    pageSize,
  } as PaginatedResult<AlarmItem>;
}

export async function getAlarmDetail(id: string) {
  const res = await get<{ list: any[] }>('/alarms', { limit: 200 });
  const row = (res.list || []).find((item) => item.alarm_no === id);
  if (!row) throw new Error('Alarm not found');
  return toAlarm(row);
}
