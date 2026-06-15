import request from '@/config/axios'

export interface EnergyAppUserSimpleVO {
  id: number
  username?: string
  nickname?: string
  mobile?: string
  cardNo?: string
  miniAdminEnabled?: boolean
  status?: number
  loginIp?: string
  loginDate?: Date
  remark?: string
  createTime?: Date
}

export const EnergyAppUserApi = {
  getAppUserPage: async (params: any) => {
    return await request.get({ url: '/energy/app-user/page', params })
  },
  getAppUser: async (id: number) => {
    return await request.get({ url: '/energy/app-user/get?id=' + id })
  },
  updateAppUser: async (data: EnergyAppUserSimpleVO) => {
    return await request.put({ url: '/energy/app-user/update', data })
  },
  getAppUserSimpleList: async (params?: { keyword?: string }) => {
    return await request.get({ url: '/energy/app-user/simple-list', params })
  }
}
