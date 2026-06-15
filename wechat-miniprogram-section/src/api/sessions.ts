import { get, post } from './request';

export interface ChargingSession {
  id: string;
  deviceId: string;
  customerId: string;
  startTime: string;
  endTime?: string;
  startEpi?: number | null;
  startEpe?: number | null;
  endEpi?: number | null;
  endEpe?: number | null;
  sessionType?: number;
  totalEnergy?: number | null;
  totalTimeFee?: number;
  totalEnergyFee?: number;
  totalFee?: number;
  status: string;
  device?: { id: string; name: string; meterNo: string };
  customer?: { id: string; name: string };
  rule?: { timeRate: number; energyRate: number };
}

export interface ScannedDevice {
  deviceId: string;
  accountKnown?: boolean;
  accountType?: string;
  accountId?: string;
  accountName?: string;
  accountMobile?: string;
  cardNo?: string;
  message?: string;
  vehicleId?: string;
  vehicleNo?: string;
  plateNo?: string;
  qrCode?: string;
  deviceNo?: string;
  deviceName?: string;
  meterNo?: string;
  gatewaySn?: string;
  meterSn?: string;
  customerId?: string;
  projectId?: string;
  status?: number;
  lastPower?: number;
  lastReadingTime?: string;
}

export interface RevenueOverview {
  todayIncome: number;
  monthIncome: number;
  activeSessions: number;
  rentalRate: number;
}

export interface TodayEnergy {
  todayCharge: number;
  todayDischarge: number;
}

export const sessionApi = {
  list(params?: { deviceId?: string; customerId?: string; status?: string; from?: string; to?: string; limit?: number }) {
    return get<ChargingSession[]>('/charging-sessions', params);
  },
  getRevenueOverview() {
    return get<RevenueOverview>('/charging-sessions/revenue-overview');
  },
  getTodayEnergy() {
    return get<TodayEnergy>('/charging-sessions/today-energy');
  },
  verifyScan(scanText: string) {
    return post<ScannedDevice>('/charging-sessions/scan/verify', { scanText, authType: 'WECHAT' });
  },
  startDischargeByScan(scanText: string) {
    return post<ChargingSession>('/charging-sessions/scan/discharge', { scanText, authType: 'WECHAT' });
  },
};
