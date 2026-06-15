import request from '@/config/axios'

export interface EnergyProjectVO {
  id?: number
  customerId?: number
  name?: string
  code?: string
  address?: string
  latitude?: number
  longitude?: number
  status?: number
  remark?: string
  createTime?: Date
}

export const EnergyProjectApi = {
  getProjectPage: async (params: any) => {
    return await request.get({ url: '/energy/project/page', params })
  },
  getProjectSimpleList: async (customerId?: number) => {
    return await request.get({ url: '/energy/project/simple-list', params: { customerId } })
  },
  getProject: async (id: number) => {
    return await request.get({ url: '/energy/project/get?id=' + id })
  },
  createProject: async (data: EnergyProjectVO) => {
    return await request.post({ url: '/energy/project/create', data })
  },
  updateProject: async (data: EnergyProjectVO) => {
    return await request.put({ url: '/energy/project/update', data })
  },
  deleteProject: async (id: number) => {
    return await request.delete({ url: '/energy/project/delete?id=' + id })
  }
}
