import request from '@/config/axios'

export interface EnergyEiotSyncLogVO {
  id?: number
  syncType?: string
  requestId?: string
  gatewaySn?: string
  meterSn?: string
  payloadUrl?: string
  status?: number
  errorMsg?: string
  createTime?: Date
}

export const EnergyEiotSyncLogApi = {
  getSyncLogPage: async (params: any) => {
    return await request.get({ url: '/energy/eiot-log/page', params })
  }
}
