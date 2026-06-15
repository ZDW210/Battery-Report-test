import request from '@/config/axios'

export interface EnergyChargeSessionVO {
  id?: number
  sessionNo?: string
  deviceId?: number
  deviceName?: string
  deviceNo?: string
  customerId?: number
  customerName?: string
  pricingRuleId?: number
  sessionType?: number
  startTime?: string | Date
  endTime?: string | Date
  startEnergy?: number
  endEnergy?: number
  totalEnergy?: number
  durationMinutes?: number
  energyFee?: number
  timeFee?: number
  totalFee?: number
  status?: number
  createTime?: string | Date
}

export const EnergyChargeSessionApi = {
  getChargeSessionPage: async (params: any) => {
    return await request.get({ url: '/energy/charge-session/page', params })
  },
  getChargeSession: async (id: number) => {
    return await request.get({ url: '/energy/charge-session/get?id=' + id })
  },
  startChargeSession: async (data: { deviceId?: number; sessionType?: number; pricingRuleId?: number }) => {
    return await request.post({ url: '/energy/charge-session/start', data })
  },
  stopChargeSession: async (data: { sessionId?: number; endEnergy?: number }) => {
    return await request.post({ url: '/energy/charge-session/stop', data })
  },
  settleChargeSession: async (data: { sessionId?: number }) => {
    return await request.post({ url: '/energy/charge-session/settle', data })
  }
}
