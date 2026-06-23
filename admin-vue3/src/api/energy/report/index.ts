import request from '@/config/axios'

export interface EnergyReportTouVO {
  sharpPeak: number
  peak: number
  flat: number
  valley: number
  deepValley: number
}

export interface EnergyReportBillSummaryVO {
  deviceCount: number
  totalChargeEnergy: number
  totalDischargeEnergy: number
  totalFee: number
  chargeCost?: number | null
  averageBuyRate?: number | null
  salesRevenue: number
  averageSellRate?: number | null
  savedCost?: number | null
  cycleCount?: number
  touSource?: string
}

export interface EnergyReportDeviceDetailVO {
  deviceId?: number
  deviceName?: string
  deviceNo?: string
  meterNo?: string
  projectId?: number
  projectName?: string
  customerId?: number
  customerName?: string
  startEpi?: number | null
  endEpi?: number | null
  startEpe?: number | null
  endEpe?: number | null
  chargeEnergy: number
  dischargeEnergy: number
  chargeTou?: EnergyReportTouVO
  dischargeTou?: EnergyReportTouVO
  purchaseCost?: number | null
  salesRevenue?: number | null
  cycleCount?: number
  pricingRuleId?: number | null
}

export interface EnergyReportEnergyDetailVO {
  label: string
  startReading?: number | null
  endReading?: number | null
  multiplier?: number
  copiedEnergy?: number
  transformerLoss?: number
  lineLoss?: number
  adjustment?: number
  billingEnergy: number
  sourceField?: string
}

export interface EnergyReportFeeDetailVO {
  category: string
  component: string
  period?: string
  billingEnergy?: number | null
  rate?: number | null
  amount?: number | null
  source?: string
}

export interface EnergyReportUnmatchedPricingVO {
  customerId?: number | null
  customerName?: string
  projectId?: number | null
  projectName?: string
  deviceId?: number | null
  deviceName?: string
  deviceNo?: string
  meterNo?: string
  period?: string
  chargeEnergy?: number
  dischargeEnergy?: number
  reason?: string
}

export interface EnergyReportBillVO {
  billMonth: string
  billRange: { start: string; end: string }
  scopeType: string
  scopeName: string
  billHeader: Record<string, string>
  summary: EnergyReportBillSummaryVO
  deviceDetails: EnergyReportDeviceDetailVO[]
  energyDetails: EnergyReportEnergyDetailVO[]
  feeDetails: EnergyReportFeeDetailVO[]
  unmatchedPricingDetails?: EnergyReportUnmatchedPricingVO[]
  analysis: {
    chargeTou: EnergyReportTouVO
    dischargeTou: EnergyReportTouVO
    chargeConsistency?: string
    dischargeConsistency?: string
  }
}

export interface EnergyReportDailyCostRowVO {
  date: string
  totalChargeEnergy: number
  totalDischargeEnergy: number
  chargeCost?: number | null
  salesRevenue: number
  savedCost?: number | null
  deviceCount: number
  unmatchedPricingCount: number
}

export interface EnergyReportDailyCostVO {
  billMonth: string
  billRange: { start: string; end: string }
  scopeType: string
  scopeName: string
  rows: EnergyReportDailyCostRowVO[]
  summary: {
    chargeCost: number
    salesRevenue: number
    savedCost: number
    unmatchedDays: number
  }
}

export const EnergyReportApi = {
  getBillReport: async (params: {
    scopeType: 'all' | 'project' | 'device'
    projectId?: number
    deviceId?: number
    billMonth: string
  }) => {
    return await request.get({ url: '/energy/report/bill', params })
  },
  getDailyCostReport: async (params: {
    scopeType: 'all' | 'project' | 'device'
    projectId?: number
    deviceId?: number
    billMonth: string
  }): Promise<EnergyReportDailyCostVO> => {
    return await request.get({ url: '/energy/report/daily-cost', params })
  }
}
