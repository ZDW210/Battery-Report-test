import request from '@/config/axios'

export interface EnergyUserScopeVO {
  id?: number
  userId?: number
  userType?: number
  userUsername?: string
  userNickname?: string
  userMobile?: string
  customerId?: number
  customerName?: string
  projectId?: number
  projectName?: string
  deviceId?: number
  deviceName?: string
  deviceNo?: string
  status?: number
  remark?: string
  createTime?: Date
}

export const EnergyUserScopeApi = {
  getUserScopePage: async (params: any) => {
    return await request.get({ url: '/energy/user-scope/page', params })
  },
  getUserScope: async (id: number) => {
    return await request.get({ url: '/energy/user-scope/get?id=' + id })
  },
  createUserScope: async (data: EnergyUserScopeVO) => {
    return await request.post({ url: '/energy/user-scope/create', data })
  },
  updateUserScope: async (data: EnergyUserScopeVO) => {
    return await request.put({ url: '/energy/user-scope/update', data })
  },
  deleteUserScope: async (id: number) => {
    return await request.delete({ url: '/energy/user-scope/delete?id=' + id })
  }
}
