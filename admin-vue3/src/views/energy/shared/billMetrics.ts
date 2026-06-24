import type { EnergyDeviceVO } from '@/api/energy/device'
import type { EnergyPricingRuleVO } from '@/api/energy/pricingRule'
import type { EnergyReportBillVO, EnergyReportTouVO } from '@/api/energy/report'

export type ElectricityScopeType = 'all' | 'project' | 'device'
export type ElectricityTouKey = 'peak' | 'flat' | 'valley' | 'deepValley'

export interface ElectricityTouRow {
  key: ElectricityTouKey
  label: string
  energy: number
  rate: number
  amount: number
  percent: number
}

export interface CustomerBillMetrics {
  chargeTou: Required<EnergyReportTouVO>
  dischargeTou: Required<EnergyReportTouVO>
  totalChargeEnergy: number
  totalDischargeEnergy: number
  totalUsage: number
  chargeCost: number
  touRows: ElectricityTouRow[]
  touAmount: number
  guaranteeEnergy: number
  averageServiceRate: number
  guaranteeAmount: number
  baseServiceFee: number
  payableAmount: number
  dieselCost: number
  gridCost: number
  savedCost: number
  applicableRules: EnergyPricingRuleVO[]
}

export const calculateCustomerBillMetrics = ({
  billReport,
  pricingRules,
  devices
}: {
  billReport?: EnergyReportBillVO
  pricingRules: EnergyPricingRuleVO[]
  devices: EnergyDeviceVO[]
}): CustomerBillMetrics => {
  const applicableRules = uniqueRowsById(devices.map((device) => matchRuleForDevice(device, pricingRules)).filter(Boolean) as EnergyPricingRuleVO[])
  const chargeTou = toLocalTou(billReport?.analysis?.chargeTou)
  const dischargeTou = toLocalTou(billReport?.analysis?.dischargeTou)
  const totalChargeEnergy = round2(numberOrZero(billReport?.summary?.totalChargeEnergy) || sumTou(chargeTou))
  const totalUsage = round2(numberOrZero(billReport?.summary?.totalDischargeEnergy) || sumTou(dischargeTou))
  const totalDischargeEnergy = totalUsage
  const rates = {
    peak: avgRuleField(applicableRules, ['peakRate', 'sharpPeakRate'], 1.62),
    flat: avgRuleField(applicableRules, ['flatRate'], 1.38),
    valley: avgRuleField(applicableRules, ['valleyRate'], 1.21),
    deepValley: avgRuleField(applicableRules, ['deepValleyRate'], avgRuleField(applicableRules, ['valleyRate'], 1.05))
  }
  const rawRows = [
    { key: 'peak' as ElectricityTouKey, label: '峰', energy: dischargeTou.peak + dischargeTou.sharpPeak, rate: rates.peak },
    { key: 'flat' as ElectricityTouKey, label: '平', energy: dischargeTou.flat, rate: rates.flat },
    { key: 'valley' as ElectricityTouKey, label: '谷', energy: dischargeTou.valley, rate: rates.valley },
    { key: 'deepValley' as ElectricityTouKey, label: '深谷', energy: dischargeTou.deepValley, rate: rates.deepValley }
  ]
  const maxEnergy = Math.max(...rawRows.map((row) => row.energy), 1)
  const touRows = rawRows.map((row) => ({
    ...row,
    energy: round2(row.energy),
    amount: round2(row.energy * row.rate),
    percent: Math.max(row.energy > 0 ? 8 : 2, Math.round((row.energy / maxEnergy) * 100))
  }))
  const touAmount = round2(touRows.reduce((sum, row) => sum + row.amount, 0))
  const guaranteeRuleEnergy = sumRuleField(applicableRules, 'guaranteeEnergy')
  const guaranteeEnergy = guaranteeRuleEnergy > 0 ? guaranteeRuleEnergy : 2500
  const averageServiceRate = totalUsage > 0 ? round4(touAmount / totalUsage) : avgRuleField(applicableRules, ['flatRate'], 1.36)
  const guaranteeAmount = round2(guaranteeEnergy * averageServiceRate)
  const baseServiceFee = round2(
    sumRuleField(applicableRules, 'siteFee') +
    sumRuleField(applicableRules, 'maintenanceFee') +
    sumRuleField(applicableRules, 'communicationFee') +
    sumRuleField(applicableRules, 'platformServiceFee') +
    sumRuleField(applicableRules, 'batteryDepreciationCost') +
    sumRuleField(applicableRules, 'otherFixedFee')
  )
  const payableAmount = round2(Math.max(guaranteeAmount, touAmount) + baseServiceFee)
  const dieselGenerationRate = avgRuleField(applicableRules, ['dieselGenerationRate'], 2)
  const gridEstimateBaseRate = avgRuleField(applicableRules, ['gridEstimateBaseRate'], 1.5)
  const gridEstimateExtraRate = avgRuleField(applicableRules, ['gridEstimateExtraRate'], 0.18)
  const dieselCost = round2(totalUsage * dieselGenerationRate)
  const gridCost = round2(totalUsage * Math.max(averageServiceRate + gridEstimateExtraRate, gridEstimateBaseRate))
  const savedCost = round2(Math.max(0, Math.min(dieselCost, gridCost) - payableAmount))

  return {
    chargeTou,
    dischargeTou,
    totalChargeEnergy,
    totalDischargeEnergy,
    totalUsage,
    chargeCost: round2(numberOrZero(billReport?.summary?.chargeCost)),
    touRows,
    touAmount,
    guaranteeEnergy,
    averageServiceRate,
    guaranteeAmount,
    baseServiceFee,
    payableAmount,
    dieselCost,
    gridCost,
    savedCost,
    applicableRules
  }
}

export const selectDevicesByScope = (
  devices: EnergyDeviceVO[],
  scopeType: ElectricityScopeType,
  projectId?: number,
  deviceId?: number
) => {
  if (scopeType === 'device') return devices.filter((device) => Number(device.id) === Number(deviceId))
  if (scopeType === 'project') return devices.filter((device) => Number(device.projectId) === Number(projectId))
  return devices
}

export const buildProjectOptionsFromDevices = (devices: EnergyDeviceVO[]) => {
  const map = new Map<number, string>()
  devices.forEach((device) => {
    const id = numberOrNull(device.projectId)
    if (id !== null) map.set(id, device.projectName || `场地 ${id}`)
  })
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
}

export const deviceDisplayLabel = (device?: EnergyDeviceVO) => {
  if (!device) return ''
  return `${device.deviceName || device.deviceNo || `电表 ${device.id}`} / ${device.meterNo || '-'}`
}

export const round2 = (value: number) => Number((Number(value) || 0).toFixed(2))
export const round4 = (value: number) => Number((Number(value) || 0).toFixed(4))
export const numberText = (value: number) => Number.isInteger(value) ? String(value) : value.toFixed(2)
export const kwhText = (value: number) => `${numberText(value)} kWh`
export const moneyText = (value: number) => `¥${numberText(value)}`
export const rateText = (value: number) => Number.isInteger(value) ? String(value) : value.toFixed(4).replace(/\.?0+$/, '')

export const numberOrNull = (value: unknown) => {
  if (value === null || value === undefined || value === '') return null
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

const matchRuleForDevice = (device: EnergyDeviceVO, pricingRules: EnergyPricingRuleVO[]) => {
  const deviceId = Number(device.id)
  const projectId = Number(device.projectId)
  const customerId = Number(device.customerId)
  return pricingRules
    .filter((rule) => Number(rule.status) === 0)
    .filter((rule) => {
      if (rule.deviceId) return Number(rule.deviceId) === deviceId
      if (rule.projectId) return Number(rule.projectId) === projectId
      if (rule.customerId) return Number(rule.customerId) === customerId
      return false
    })
    .sort((a, b) => Number(Boolean(b.deviceId)) - Number(Boolean(a.deviceId)) || Number(Boolean(b.projectId)) - Number(Boolean(a.projectId)))[0]
}

const avgRuleField = (rules: EnergyPricingRuleVO[], fields: Array<keyof EnergyPricingRuleVO>, fallback: number) => {
  const values = rules
    .flatMap((rule) => fields.map((field) => numberOrNull(rule[field])))
    .filter((value): value is number => value !== null && value > 0)
  if (!values.length) return fallback
  return round4(values.reduce((sum, value) => sum + value, 0) / values.length)
}

const sumRuleField = (rules: EnergyPricingRuleVO[], field: keyof EnergyPricingRuleVO) => round2(
  rules.reduce((sum, rule) => sum + Number(rule[field] || 0), 0)
)

const toLocalTou = (value?: EnergyReportTouVO | null): Required<EnergyReportTouVO> => ({
  sharpPeak: Number(value?.sharpPeak || 0),
  peak: Number(value?.peak || 0),
  flat: Number(value?.flat || 0),
  valley: Number(value?.valley || 0),
  deepValley: Number(value?.deepValley || 0)
})

const sumTou = (value: Required<EnergyReportTouVO>) => value.sharpPeak + value.peak + value.flat + value.valley + value.deepValley
const numberOrZero = (value: unknown) => numberOrNull(value) || 0
const uniqueRowsById = <T extends { id?: number }>(rows: T[]) => Array.from(new Map(rows.map((row, index) => [row.id || index, row])).values())
