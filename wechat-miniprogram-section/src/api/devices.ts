import { get } from './request';

export interface Device {
  id: string;
  gatewaySn: string;
  meterSn: string;
  meterNo: string;
  name: string | null;
  projectCode: string | null;
  projectName: string | null;
  status: 'ONLINE' | 'OFFLINE';
  location: string | null;
  deviceType: string | null;
  lastUa: number | null;
  lastUb: number | null;
  lastUc: number | null;
  lastIa: number | null;
  lastIb: number | null;
  lastIc: number | null;
  lastVoltage: number | null;
  lastCurrent: number | null;
  lastP: number | null;
  lastPf: number | null;
  lastEpi: number | null;
  runMode: string | null;
  latitude: number | null;
  longitude: number | null;
  lastReadingAt: string | null;
  customerId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DeviceDetail extends Device {
  recentAlarms: any[];
}

export interface Overview {
  totalDevices: number;
  onlineCount: number;
  offlineCount: number;
  activeAlarms: number;
  totalEpi: number;
  totalPower: number;
  chargingCount: number;
  dischargingCount: number;
  standbyCount: number;
  faultCount: number;
  lastUpdatedAt: string | null;
}

export interface PaginatedResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface EnergyDeltaDevice {
  deviceId: string;
  deviceName: string;
  meterNo: string;
  chargeEnergy: number;
  netEnergy: number;
}

export interface EnergyDelta {
  from: string;
  to: string;
  devices: EnergyDeltaDevice[];
  summary: {
    totalCharge: number;
    totalNet: number;
    deviceCount: number;
  };
}

function modeToApp(mode: string | null | undefined): string | null {
  if (mode === 'CHARGE') return 'CHARGING';
  if (mode === 'DISCHARGE') return 'DISCHARGING';
  return mode || null;
}

function statusToApp(status: number | string | null | undefined): 'ONLINE' | 'OFFLINE' {
  return Number(status) === 0 ? 'ONLINE' : 'OFFLINE';
}

function runModeToApp(mode: number | string | null | undefined): string | null {
  const value = Number(mode);
  if (value === 1) return 'CHARGING';
  if (value === 2) return 'DISCHARGING';
  if (value === 3) return 'STANDBY';
  if (value === 4) return 'FAULT';
  return modeToApp(typeof mode === 'string' ? mode : null);
}

function toDevice(row: any): Device {
  return {
    id: String(row.id ?? row.meter_no ?? row.deviceNo ?? ''),
    gatewaySn: row.gatewaySn || row.gateway_sn || '',
    meterSn: row.meterSn || row.meter_sn || '',
    meterNo: row.meterNo || row.meter_no || row.deviceNo || '',
    name: row.deviceName || row.name || null,
    projectCode: row.projectCode || row.project_code || null,
    projectName: row.projectName || row.project_name || null,
    status: row.state || statusToApp(row.status),
    location: row.location || null,
    deviceType: row.deviceType || row.device_type || null,
    lastUa: row.lastUa ?? row.last_ua ?? null,
    lastUb: row.lastUb ?? row.last_ub ?? null,
    lastUc: row.lastUc ?? row.last_uc ?? null,
    lastIa: row.lastIa ?? row.last_ia ?? null,
    lastIb: row.lastIb ?? row.last_ib ?? null,
    lastIc: row.lastIc ?? row.last_ic ?? null,
    lastVoltage: row.lastVoltage ?? row.last_voltage ?? null,
    lastCurrent: row.lastCurrent ?? row.last_current ?? null,
    lastP: row.lastPower ?? row.lastP ?? row.last_p ?? null,
    lastPf: row.lastPf ?? row.last_pf ?? null,
    lastEpi: row.lastEpi ?? row.last_epi ?? null,
    runMode: runModeToApp(row.runMode ?? row.run_mode),
    latitude: row.latitude,
    longitude: row.longitude,
    lastReadingAt: row.lastReadingTime || row.last_reading_at || null,
    customerId: row.customerId || row.customer_id || null,
    createdAt: row.createTime || row.created_at || '',
    updatedAt: row.updateTime || row.updated_at || '',
  };
}

function toAlarm(row: any) {
  return {
    id: row.alarm_no,
    alarmNo: row.alarm_no,
    code: row.code,
    level: row.level,
    titleZh: row.title_zh,
    titleEn: row.title_en,
    messageZh: row.message_zh,
    messageEn: row.message_en,
    createdAt: row.created_at,
  };
}

function toReading(row: any) {
  const ts = row.ts ? new Date(Number(row.ts) * 1000).toISOString() : row.create_time;
  return {
    ...row,
    ts,
    Ua: row.ua,
    Ub: row.ub,
    Uc: row.uc,
    Ia: row.ia,
    Ib: row.ib,
    Ic: row.ic,
    P: row.p,
    Pa: row.pa,
    Pb: row.pb,
    Pc: row.pc,
    PF: row.pf,
    EPI: row.epi,
  };
}

export async function getDevices(params?: {
  page?: number;
  pageSize?: number;
  status?: string;
  search?: string;
  sortBy?: string;
  order?: string;
}, options?: { skipCustomerScope?: boolean }) {
  const list = await get<any[]>('/device/list', params, options);
  return { list: (list || []).map(toDevice), total: list?.length || 0, page: params?.page || 1, pageSize: params?.pageSize || 20 };
}

export async function getDeviceDetail(id: string) {
  const row = await get<any>(`/devices/${encodeURIComponent(id)}`);
  return {
    ...toDevice(row),
    recentAlarms: (row.recentAlarms || []).map(toAlarm),
  } as DeviceDetail;
}

export async function getDeviceLatest(id: string) {
  const row = await get<any>(`/devices/${encodeURIComponent(id)}/latest`);
  return row ? toReading(row) : null;
}

export async function getDeviceReadings(
  id: string,
  params?: { params?: string; from?: string; to?: string; interval?: string },
) {
  const res = await get<any>(`/devices/${encodeURIComponent(id)}/readings`, params);
  return { ...res, data: (res.data || []).map(toReading) };
}

export async function getOverview(params?: { from?: string; to?: string }) {
  const row = await get<any>('/home/overview', params);
  const totalDevices = Number(row.deviceCount ?? row.totalDevices ?? 0);
  const onlineCount = Number(row.onlineCount ?? 0);
  const faultCount = Number(row.faultCount ?? 0);
  return {
    totalDevices,
    onlineCount,
    offlineCount: Math.max(totalDevices - onlineCount - faultCount, 0),
    activeAlarms: row.unackedAlarmCount ?? row.activeAlarms ?? 0,
    totalEpi: row.todayChargeEnergy ?? row.totalEpi ?? 0,
    totalPower: row.currentPower ?? row.totalPower ?? 0,
    chargingCount: row.chargingCount || 0,
    dischargingCount: row.dischargingCount || 0,
    standbyCount: row.standbyCount || 0,
    faultCount,
    lastUpdatedAt: row.lastUpdatedAt || null,
  };
}

export function getEnergyDelta(params?: { from?: string; to?: string }) {
  return get<EnergyDelta>('/devices/energy-delta', params);
}
