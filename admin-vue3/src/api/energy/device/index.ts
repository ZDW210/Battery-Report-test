import request from '@/config/axios'

// 移动储能设备 VO
export interface EnergyDeviceVO {
  id?: number
  deviceNo?: string
  deviceName?: string
  deviceType?: number
  gatewaySn?: string
  meterSn?: string
  meterNo?: string
  customerId?: number
  customerName?: string
  projectId?: number
  projectName?: string
  status?: number
  runMode?: number
  latitude?: number
  longitude?: number
  lastSoc?: number
  lastSoh?: number
  lastPower?: number
  lastVoltage?: number
  lastCurrent?: number
  lastTemp?: number
  lastReadingTime?: Date
  remark?: string
  createTime?: Date
}

export interface EnergyDeviceControlReqVO {
  deviceId: number
  method: 'SWITCH' | 'REFRESH'
  value?: Record<string, any>
  remark?: string
}

export interface EnergyDeviceControlRespVO {
  controlLogId?: number
  success?: boolean
  message?: string
}

// 移动储能设备 API
export const EnergyDeviceApi = {
  // 查询设备分页
  getDevicePage: async (params: any) => {
    return await request.get({ url: '/energy/device/page', params })
  },

  // 查询设备详情
  getDevice: async (id: number) => {
    return await request.get({ url: '/energy/device/get?id=' + id })
  },

  // 查询设备精简列表
  getDeviceSimpleList: async (params?: { customerId?: number; projectId?: number }) => {
    return await request.get({ url: '/energy/device/simple-list', params })
  },

  // 新增设备
  createDevice: async (data: EnergyDeviceVO) => {
    return await request.post({ url: '/energy/device/create', data })
  },

  // 修改设备
  updateDevice: async (data: EnergyDeviceVO) => {
    return await request.put({ url: '/energy/device/update', data })
  },

  // 删除设备
  deleteDevice: async (id: number) => {
    return await request.delete({ url: '/energy/device/delete?id=' + id })
  },

  controlDevice: async (data: EnergyDeviceControlReqVO): Promise<EnergyDeviceControlRespVO> => {
    return await request.post({ url: '/energy/device/control', data })
  }
}
