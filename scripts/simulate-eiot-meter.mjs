const DEFAULT_URL = 'https://report.battvest.com.cn/eiot/meter'
const DEFAULT_DEVICE = {
  gatewaySn: '20241204870083',
  meterSn: 'SH002',
  meterNo: '20241204870083_SH002'
}

const args = parseArgs(process.argv.slice(2))
const targetUrl = args.url || process.env.EIOT_URL || DEFAULT_URL
const device = {
  gatewaySn: args.gatewaySn || DEFAULT_DEVICE.gatewaySn,
  meterSn: args.meterSn || DEFAULT_DEVICE.meterSn,
  meterNo: args.meterNo || DEFAULT_DEVICE.meterNo
}

const chargeDelta = numberArg(args.chargeDelta, 12.6)
const dischargeDelta = numberArg(args.dischargeDelta, 8.4)
const startEpi = numberArg(args.startEpi, 1975.16)
const startEpe = numberArg(args.startEpe, 1288.7)
const startTime = args.startTime ? new Date(args.startTime) : new Date()
const endTime = new Date(startTime.getTime() + numberArg(args.intervalMinutes, 5) * 60 * 1000)

const chargeSplit = splitDelta(chargeDelta, {
  peak: numberArg(args.chargePeak, 4.2),
  flat: numberArg(args.chargeFlat, 5.1)
})
const dischargeSplit = splitDelta(dischargeDelta, {
  peak: numberArg(args.dischargePeak, 2.8),
  flat: numberArg(args.dischargeFlat, 3.4)
})

const baseline = meterPayload({
  ...device,
  date: startTime,
  epi: startEpi,
  epij: 0,
  epif: 700,
  epip: 850,
  epig: round4(startEpi - 1550),
  epe: startEpe,
  epej: 0,
  epef: 460,
  epep: 520,
  epeg: round4(startEpe - 980)
})

const current = meterPayload({
  ...device,
  date: endTime,
  epi: round4(startEpi + chargeDelta),
  epij: baseline.EPIJ,
  epif: round4(baseline.EPIF + chargeSplit.peak),
  epip: round4(baseline.EPIP + chargeSplit.flat),
  epig: round4(baseline.EPIG + chargeSplit.valley),
  epe: round4(startEpe + dischargeDelta),
  epej: baseline.EPEJ,
  epef: round4(baseline.EPEF + dischargeSplit.peak),
  epep: round4(baseline.EPEP + dischargeSplit.flat),
  epeg: round4(baseline.EPEG + dischargeSplit.valley)
})

assertDelta('charge', chargeDelta, baseline, current, ['EPIF', 'EPIP', 'EPIG'], 'EPI')
assertDelta('discharge', dischargeDelta, baseline, current, ['EPEF', 'EPEP', 'EPEG'], 'EPE')

await postPayload(targetUrl, [baseline])
await postPayload(targetUrl, [current])

console.log(JSON.stringify({
  accepted: true,
  url: targetUrl,
  device,
  chargeDelta,
  dischargeDelta,
  chargeSplit,
  dischargeSplit,
  note: 'EPIJ/EPEJ stay unchanged. Peak, flat, and valley deltas exactly match EPI/EPE deltas.'
}, null, 2))

function parseArgs(argv) {
  const parsed = {}
  for (let index = 0; index < argv.length; index += 1) {
    const arg = argv[index]
    if (!arg.startsWith('--')) continue
    const key = arg.slice(2)
    const next = argv[index + 1]
    if (!next || next.startsWith('--')) {
      parsed[key] = 'true'
    } else {
      parsed[key] = next
      index += 1
    }
  }
  return parsed
}

function numberArg(value, fallback) {
  if (value === undefined || value === null || value === '') return fallback
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) throw new Error(`Invalid number: ${value}`)
  return numberValue
}

function splitDelta(total, parts) {
  const roundedTotal = round4(total)
  const peak = round4(parts.peak)
  const flat = round4(parts.flat)
  const valley = round4(roundedTotal - peak - flat)
  if (valley < 0) {
    throw new Error(`Split exceeds total. total=${roundedTotal}, peak=${peak}, flat=${flat}`)
  }
  return { peak, flat, valley }
}

function meterPayload(row) {
  return {
    gatewaySn: row.gatewaySn,
    meterSn: row.meterSn,
    meterNo: row.meterNo,
    createTime: formatLocalDateTime(row.date),
    timestamp: Math.floor(row.date.getTime() / 1000),
    source: 'REALTIME',
    state: 'ONLINE',
    Pa: 12.3,
    Pb: 12.1,
    Pc: 12.4,
    Ua: 220.1,
    Ub: 219.8,
    Uc: 220.4,
    Ia: 18.2,
    Ib: 18,
    Ic: 18.4,
    P: 36.8,
    PF: 0.98,
    EPI: round4(row.epi),
    EPIJ: round4(row.epij),
    EPIF: round4(row.epif),
    EPIP: round4(row.epip),
    EPIG: round4(row.epig),
    EPE: round4(row.epe),
    EPEJ: round4(row.epej),
    EPEF: round4(row.epef),
    EPEP: round4(row.epep),
    EPEG: round4(row.epeg)
  }
}

function assertDelta(label, expectedTotal, previous, current, partFields, totalField) {
  const totalDelta = round4(current[totalField] - previous[totalField])
  const partDelta = round4(partFields.reduce((sum, field) => sum + current[field] - previous[field], 0))
  if (totalDelta !== round4(expectedTotal) || partDelta !== totalDelta) {
    throw new Error(`${label} delta mismatch. total=${totalDelta}, parts=${partDelta}, expected=${expectedTotal}`)
  }
}

async function postPayload(url, payload) {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  const text = await response.text()
  if (!response.ok) {
    throw new Error(`Push failed: ${response.status} ${text}`)
  }
}

function formatLocalDateTime(date) {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function round4(value) {
  return Number(Number(value || 0).toFixed(4))
}
