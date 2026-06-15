import request from '@/config/axios'

export interface EnergyCustomerAccountVO {
  id?: number
  customerId?: number
  customerName?: string
  systemUserId?: number
  roleId?: number
  username?: string
  password?: string
  nickname?: string
  mobile?: string
  status?: number
  menuIds?: number[]
  remark?: string
  createTime?: Date
}

export interface EnergyCustomerAccountMenuOptionVO {
  id: number
  name: string
  permission?: string
  parentId?: number
  sort?: number
}

export const EnergyCustomerAccountApi = {
  getCustomerAccountPage: async (params: any) => {
    return await request.get({ url: '/energy/customer-account/page', params })
  },
  getCustomerAccount: async (id: number) => {
    return await request.get({ url: '/energy/customer-account/get?id=' + id })
  },
  createCustomerAccount: async (data: EnergyCustomerAccountVO) => {
    return await request.post({ url: '/energy/customer-account/create', data })
  },
  updateCustomerAccount: async (data: EnergyCustomerAccountVO) => {
    return await request.put({ url: '/energy/customer-account/update', data })
  },
  resetPassword: async (id: number, password: string) => {
    return await request.put({ url: '/energy/customer-account/reset-password', data: { id, password } })
  },
  getMenuOptions: async (): Promise<EnergyCustomerAccountMenuOptionVO[]> => {
    return await request.get({ url: '/energy/customer-account/menu-options' })
  }
}
