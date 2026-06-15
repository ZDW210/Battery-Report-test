import request from '@/config/axios'

export interface EnergyVehicleVO {
  id?: number
  vehicleNo?: string
  plateNo?: string
  qrCode?: string
  deviceId?: number
  deviceName?: string
  deviceNo?: string
  gatewaySn?: string
  meterNo?: string
  customerId?: number
  customerName?: string
  projectId?: number
  projectName?: string
  status?: number
  remark?: string
  createTime?: Date
}

export const EnergyVehicleApi = {
  getVehiclePage: async (params: any) => {
    return await request.get({ url: '/energy/vehicle/page', params })
  },
  getVehicle: async (id: number) => {
    return await request.get({ url: '/energy/vehicle/get?id=' + id })
  },
  createVehicle: async (data: EnergyVehicleVO) => {
    return await request.post({ url: '/energy/vehicle/create', data })
  },
  updateVehicle: async (data: EnergyVehicleVO) => {
    return await request.put({ url: '/energy/vehicle/update', data })
  },
  deleteVehicle: async (id: number) => {
    return await request.delete({ url: '/energy/vehicle/delete?id=' + id })
  }
}
