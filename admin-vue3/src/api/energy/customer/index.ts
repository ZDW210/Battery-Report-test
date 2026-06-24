import request from '@/config/axios'

export interface EnergyCustomerVO {
  id?: number
  name?: string
  contactName?: string
  contactMobile?: string
  region?: string
  accountNo?: string
  usageAddress?: string
  supplyOrg?: string
  marketAttribute?: string
  customerService?: string
  supervisePhone?: string
  printPerson?: string
  paymentDueDay?: number
  status?: number
  remark?: string
  createTime?: Date
}

export const EnergyCustomerApi = {
  getCustomerPage: async (params: any) => {
    return await request.get({ url: '/energy/customer/page', params })
  },
  getCustomerSimpleList: async () => {
    return await request.get({ url: '/energy/customer/simple-list' })
  },
  getCustomer: async (id: number) => {
    return await request.get({ url: '/energy/customer/get?id=' + id })
  },
  createCustomer: async (data: EnergyCustomerVO) => {
    return await request.post({ url: '/energy/customer/create', data })
  },
  updateCustomer: async (data: EnergyCustomerVO) => {
    return await request.put({ url: '/energy/customer/update', data })
  },
  deleteCustomer: async (id: number) => {
    return await request.delete({ url: '/energy/customer/delete?id=' + id })
  }
}
