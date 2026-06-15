import request from '@/config/axios'

export interface EnergyTelemetryVO {
  id?: number
  deviceId?: number
  deviceName?: string
  deviceNo?: string
  gatewaySn?: string
  meterSn?: string
  meterNo?: string
  collectTime?: string | Date
  timestamp?: number
  source?: string
  state?: string
  pa?: number
  pb?: number
  pc?: number
  ua?: number
  ub?: number
  uc?: number
  ia?: number
  ib?: number
  ic?: number
  p?: number
  pf?: number
  epi?: number
  createTime?: string | Date
}

export interface EnergyTelemetryDailyStatVO {
  date?: string
  max?: number
  maxTime?: string | Date
  min?: number
  minTime?: string | Date
  avg?: number
}

export const EnergyTelemetryApi = {
  getTelemetryPage: async (params: any) => {
    return await request.get({ url: '/energy/telemetry/page', params })
  },
  getTelemetryChart: async (params: any) => {
    return await request.get({ url: '/energy/telemetry/chart', params })
  },
  getTelemetryDailyStat: async (params: any) => {
    return await request.get({ url: '/energy/telemetry/daily-stat', params })
  }
}
