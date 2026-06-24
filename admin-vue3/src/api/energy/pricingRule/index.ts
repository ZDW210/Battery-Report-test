import request from '@/config/axios'

export interface EnergyPricingRuleVO {
  id?: number
  customerId?: number
  customerName?: string
  projectId?: number
  projectName?: string
  deviceId?: number
  deviceName?: string
  deviceNo?: string
  electricityCategory?: string
  pricingMode?: string
  voltageLevel?: string
  agentPurchasePrice?: number
  lineLossPrice?: number
  transmissionDistributionPrice?: number
  systemOperationFee?: number
  governmentFundSurcharge?: number
  sharpPeakRate?: number
  peakRate?: number
  flatRate?: number
  valleyRate?: number
  deepValleyRate?: number
  touPeriods?: string
  feeConfigJson?: string
  serviceMarkupPercent?: number
  capacityBillingMode?: string
  maxDemandPrice?: number
  transformerCapacityKva?: number
  transformerCapacityPrice?: number
  timeRate?: number
  energyRate?: number
  siteFee?: number
  maintenanceFee?: number
  communicationFee?: number
  platformServiceFee?: number
  batteryDepreciationCost?: number
  otherFixedFee?: number
  guaranteeEnergy?: number
  effectiveStart?: string | Date
  effectiveEnd?: string | Date
  status?: number
  remark?: string
  createTime?: Date
}

export const EnergyPricingRuleApi = {
  getPricingRulePage: async (params: any) => {
    return await request.get({ url: '/energy/pricing-rule/page', params })
  },
  getPricingRule: async (id: number) => {
    return await request.get({ url: '/energy/pricing-rule/get?id=' + id })
  },
  matchPricingRule: async (deviceId: number, billingTime?: string) => {
    return await request.get({
      url: '/energy/pricing-rule/match',
      params: { deviceId, billingTime }
    })
  },
  createPricingRule: async (data: EnergyPricingRuleVO) => {
    return await request.post({ url: '/energy/pricing-rule/create', data })
  },
  updatePricingRule: async (data: EnergyPricingRuleVO) => {
    return await request.put({ url: '/energy/pricing-rule/update', data })
  },
  deletePricingRule: async (id: number) => {
    return await request.delete({ url: '/energy/pricing-rule/delete?id=' + id })
  }
}
