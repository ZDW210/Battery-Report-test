import request from '@/config/axios'

export interface EnergyAccountEventVO {
  id?: number
  eventScene?: string
  authType?: string
  scanText?: string
  cardNo?: string
  accountKnown?: boolean
  accountId?: number
  accountName?: string
  accountMobile?: string
  deviceId?: number
  deviceNo?: string
  deviceName?: string
  meterNo?: string
  gatewaySn?: string
  meterSn?: string
  customerId?: number
  projectId?: number
  resultMessage?: string
  createTime?: Date
}

export const EnergyAccountEventApi = {
  getAccountEventPage: async (params: any) => {
    return await request.get({ url: '/energy/account-event/page', params })
  }
}
