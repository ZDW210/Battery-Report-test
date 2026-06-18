export interface Env {
  ASSETS: Fetcher
  DB: D1Database
  BUCKET: R2Bucket
  SESSION_SECRET?: string
  WECHAT_APP_ID?: string
  WECHAT_APP_SECRET?: string
}

type AnyRecord = Record<string, any>
type TouKey = 'sharpPeak' | 'peak' | 'flat' | 'valley' | 'deepValley'
type TouEnergy = Record<TouKey, number>
type TouSource = 'epi' | 'epe'
type AccessScope = { isCustomerAccount: boolean; customerId: number | null }

const ADMIN_PREFIX = '/admin-api'
const DAILY_FIELDS = new Set([
  'pa', 'pb', 'pc', 'ua', 'ub', 'uc', 'ia', 'ib', 'ic', 'p', 'pf',
  'epi', 'epij', 'epif', 'epip', 'epig', 'epe', 'epej', 'epef', 'epep', 'epeg'
])
const EIOT_PUSH_PATHS = new Set([
  '/eiot',
  '/eiot/meter',
  '/eiot/alarm',
  '/infra-api/energy/eiot/realtime',
  '/infra-api/energy/eiot/alarm'
])
const PRICING_RULE_FIELDS = [
  'customerId',
  'projectId',
  'deviceId',
  'electricityCategory',
  'pricingMode',
  'voltageLevel',
  'agentPurchasePrice',
  'lineLossPrice',
  'transmissionDistributionPrice',
  'systemOperationFee',
  'governmentFundSurcharge',
  'sharpPeakRate',
  'peakRate',
  'flatRate',
  'valleyRate',
  'deepValleyRate',
  'touPeriods',
  'capacityBillingMode',
  'maxDemandPrice',
  'transformerCapacityKva',
  'transformerCapacityPrice',
  'timeRate',
  'energyRate',
  'siteFee',
  'maintenanceFee',
  'communicationFee',
  'platformServiceFee',
  'batteryDepreciationCost',
  'otherFixedFee',
  'effectiveStart',
  'effectiveEnd',
  'status',
  'remark'
]
const PRICING_RULE_FEE_COLUMNS: Record<string, string> = {
  electricity_category: "TEXT NOT NULL DEFAULT 'general_commercial'",
  pricing_mode: "TEXT NOT NULL DEFAULT 'two_part'",
  voltage_level: "TEXT NOT NULL DEFAULT 'under_1kv'",
  agent_purchase_price: 'REAL NOT NULL DEFAULT 0',
  line_loss_price: 'REAL NOT NULL DEFAULT 0',
  transmission_distribution_price: 'REAL NOT NULL DEFAULT 0',
  system_operation_fee: 'REAL NOT NULL DEFAULT 0',
  government_fund_surcharge: 'REAL NOT NULL DEFAULT 0',
  sharp_peak_rate: 'REAL NOT NULL DEFAULT 0',
  peak_rate: 'REAL NOT NULL DEFAULT 0',
  flat_rate: 'REAL NOT NULL DEFAULT 0',
  valley_rate: 'REAL NOT NULL DEFAULT 0',
  deep_valley_rate: 'REAL NOT NULL DEFAULT 0',
  tou_periods: 'TEXT NOT NULL DEFAULT \'[]\'',
  capacity_billing_mode: "TEXT NOT NULL DEFAULT 'none'",
  max_demand_price: 'REAL NOT NULL DEFAULT 0',
  transformer_capacity_kva: 'REAL NOT NULL DEFAULT 0',
  transformer_capacity_price: 'REAL NOT NULL DEFAULT 0',
  site_fee: 'REAL NOT NULL DEFAULT 0',
  maintenance_fee: 'REAL NOT NULL DEFAULT 0',
  communication_fee: 'REAL NOT NULL DEFAULT 0',
  platform_service_fee: 'REAL NOT NULL DEFAULT 0',
  battery_depreciation_cost: 'REAL NOT NULL DEFAULT 0',
  other_fixed_fee: 'REAL NOT NULL DEFAULT 0'
}
const DEVICE_FIELDS = [
  'deviceNo', 'deviceName', 'deviceType', 'runMode', 'gatewaySn', 'meterSn', 'meterNo', 'customerId', 'projectId',
  'latitude', 'longitude', 'lastSoc', 'lastSoh', 'lastPower', 'lastVoltage', 'lastCurrent', 'lastTemp',
  'lastReadingTime', 'remark'
]

const ENERGY_MENUS = [
  menu(1002, 0, '数据面板', '/energy/telemetry', 'energy/telemetry/index', 'EnergyTelemetry', 'ep:trend-charts', 1),
  menu(1015, 0, '报表面板', '/energy/report-panel', 'energy/report-panel/index', 'EnergyReportPanel', 'ep:document-checked', 2),
  menu(1007, 0, '报警信息', '/energy/alarm', 'energy/alarm/index', 'EnergyAlarm', 'ep:warning', 3),
  menu(1003, 0, '设备管理', '/energy/device', 'energy/device/index', 'EnergyDevice', 'ep:cpu', 4),
  menu(1009, 0, '项目管理', '/energy/project', 'energy/project/index', 'EnergyProject', 'ep:location', 5),
  menu(1008, 0, '客户管理', '/energy/customer', 'energy/customer/index', 'EnergyCustomer', 'ep:office-building', 6),
  menu(1014, 0, '客户账号管理', '/energy/customer-account', 'energy/customerAccount/index', 'EnergyCustomerAccount', 'ep:lock', 7),
  menu(1004, 0, '车辆管理', '/energy/vehicle', 'energy/vehicle/index', 'EnergyVehicle', 'ep:van', 8),
  menu(1012, 0, '计费规则', '/energy/pricing-rule', 'energy/pricing-rule/index', 'EnergyPricingRule', 'ep:money', 9),
  menu(1013, 0, '充放电任务', '/energy/charge-session', 'energy/charge-session/index', 'EnergyChargeSession', 'ep:switch-button', 10),
  menu(1005, 0, '小程序用户', '/energy/app-user', 'energy/appUser/index', 'EnergyAppUser', 'ep:user', 11),
  menu(1010, 0, '用户授权', '/energy/user-scope', 'energy/userScope/index', 'EnergyUserScope', 'ep:key', 12),
  menu(1006, 0, '扫码刷卡记录', '/energy/account-event', 'energy/account-event/index', 'EnergyAccountEvent', 'ep:connection', 13),
  menu(1011, 0, 'EIOT 同步日志', '/energy/eiot-log', 'energy/eiotLog/index', 'EnergyEiotLog', 'ep:document', 14)
]

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url)

    if (request.method === 'OPTIONS') return cors(new Response(null, { status: 204 }))

    if (EIOT_PUSH_PATHS.has(url.pathname) && request.method === 'POST') {
      return await handleEiotPush(request, env, ctx, url.pathname)
    }

    if (url.pathname.startsWith(`${ADMIN_PREFIX}/`)) {
      return cors(await handleAdminApi(request, env, ctx))
    }

    const assetResponse = await env.ASSETS.fetch(request)
    if (assetResponse.status !== 404) return assetResponse

    const indexUrl = new URL(request.url)
    indexUrl.pathname = '/index.html'
    indexUrl.search = ''
    return env.ASSETS.fetch(new Request(indexUrl, request))
  }
}

async function handleEiotPush(request: Request, env: Env, ctx: ExecutionContext, path: string) {
  const receivedAt = Date.now()
  const rawBody = await request.text()
  ctx.waitUntil(processEiotPush(rawBody, env, path, receivedAt))
  return ok({ accepted: true, receivedAt: new Date(receivedAt).toISOString() })
}

async function processEiotPush(rawBody: string, env: Env, path: string, receivedAt: number) {
  let payload: unknown = null
  let pushType = path === '/eiot/alarm' ? 'alarm' : path === '/eiot/meter' ? 'meter' : 'unknown'
  let payloadUrl = ''
  let gatewaySn = ''
  let meterSn = ''
  const requestId = crypto.randomUUID()

  try {
    payload = rawBody ? JSON.parse(rawBody) : null
    pushType = pushType === 'unknown' ? detectEiotPushType(payload) : pushType
    const summary = getEiotPayloadSummary(payload)
    gatewaySn = summary.gatewaySn
    meterSn = summary.meterSn
    payloadUrl = await archiveEiotPayload(env, pushType, rawBody, summary, receivedAt)
    await ensureEiotReceiveColumns(env)

    if (pushType === 'meter') {
      await saveEiotMeterPayload(env, payload, payloadUrl)
    } else if (pushType === 'alarm') {
      await saveEiotAlarmPayload(env, payload, payloadUrl)
    } else {
      throw new Error('Unsupported EIOT payload type')
    }

    await writeEiotSyncLog(env, {
      syncType: pushType,
      requestId,
      gatewaySn,
      meterSn,
      payloadUrl,
      status: 0,
      errorMsg: ''
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error)
    console.error('EIOT push failed', message)
    await writeEiotSyncLog(env, {
      syncType: pushType,
      requestId,
      gatewaySn,
      meterSn,
      payloadUrl,
      status: 1,
      errorMsg: message.slice(0, 500)
    })
  }
}

async function handleAdminApi(request: Request, env: Env, ctx: ExecutionContext) {
  try {
    const url = new URL(request.url)
    const path = url.pathname.slice(ADMIN_PREFIX.length)

    if (path === '/system/auth/login' && request.method === 'POST') return login(request, env)
    if (path === '/system/auth/get-permission-info' && request.method === 'GET') return permissionInfo(request, env)
    if (path === '/system/auth/logout' && request.method === 'POST') return logout(request, env)
    if (path === '/system/auth/refresh-token' && request.method === 'POST') return refreshToken(request, env)
    if (path === '/system/tenant/get-id-by-name') return ok(1)
    if (path === '/system/tenant/get-by-website') return ok(null)

    const user = await requireUser(request, env)
    if (!user) return fail('未登录或登录已过期', 401, 401)

    const systemCompatResponse = await systemCompatApi(request, url, env, path, user)
    if (systemCompatResponse) return systemCompatResponse

    const accessScope = await getAccessScope(env, user)

    if (path.startsWith('/energy/customer-account/') && accessScope.isCustomerAccount) return customerAccountForbidden()
    if (path === '/energy/customer-account/page' && request.method === 'GET') return customerAccountPage(url, env)
    if (path === '/energy/customer-account/get' && request.method === 'GET') return customerAccountGet(url, env)
    if (path === '/energy/customer-account/create' && request.method === 'POST') return createCustomerAccount(request, env)
    if (path === '/energy/customer-account/update' && request.method === 'PUT') return updateCustomerAccount(request, env)
    if (path === '/energy/customer-account/reset-password' && request.method === 'PUT') return resetCustomerPassword(request, env)
    if (path === '/energy/customer-account/menu-options' && request.method === 'GET') return ok(ENERGY_MENUS.map(menuOption))

    if (path.startsWith('/energy/customer/')) return customerApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/project/')) return projectApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/device/')) return deviceApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/vehicle/')) return vehicleApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/app-user/')) return crud(request, url, env, 'energy_app_user', ['username', 'nickname', 'mobile', 'cardNo', 'miniAdminEnabled', 'status', 'loginIp', 'loginDate', 'remark'])
    if (path.startsWith('/energy/account-event/')) return accountEventApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/user-scope/')) return userScopeApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/pricing-rule/')) return pricingRuleApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/charge-session/')) return chargeSessionApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/eiot-log/')) return crud(request, url, env, 'energy_eiot_sync_log', ['syncType', 'requestId', 'gatewaySn', 'meterSn', 'payloadUrl', 'status', 'errorMsg'])
    if (path.startsWith('/energy/alarm/')) return alarmApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/telemetry/')) return telemetryApi(request, url, env, path, accessScope)
    if (path.startsWith('/energy/report/')) return reportApi(request, url, env, path, accessScope)

    if (path === '/infra/file/upload' && request.method === 'POST') return uploadFile(request, env, ctx)
    if (path.startsWith('/infra/file/get/') && request.method === 'GET') return getFile(path, env)

    return fail(`未实现的接口：${path}`, 404, 404)
  } catch (error) {
    return fail(error instanceof Error ? error.message : '系统异常', 500, 500)
  }
}

async function login(request: Request, env: Env) {
  const body = await readJson(request)
  const username = String(body.username || '')
  const password = String(body.password || '')
  const user = await env.DB.prepare('SELECT * FROM system_user WHERE username = ? AND status = 0').bind(username).first<AnyRecord>()

  if (!user || !(await verifyPassword(password, String(user.password_hash || '')))) {
    return fail('账号或密码错误', 400)
  }

  const now = Date.now()
  const expiresTime = now + 7 * 24 * 60 * 60 * 1000
  const accessToken = await token('ak')
  const refreshTokenValue = await token('rk')
  await env.DB.prepare(
    'INSERT INTO system_session(access_token, refresh_token, user_id, expires_time, create_time) VALUES (?, ?, ?, ?, ?)'
  )
    .bind(accessToken, refreshTokenValue, user.id, expiresTime, now)
    .run()

  return ok({
    id: now,
    accessToken,
    refreshToken: refreshTokenValue,
    userId: user.id,
    userType: user.user_type || 2,
    clientId: 'worker-admin',
    expiresTime
  })
}

async function permissionInfo(request: Request, env: Env) {
  const user = await requireUser(request, env)
  if (!user) return fail('未登录或登录已过期', 401, 401)

  const account = await env.DB.prepare('SELECT * FROM energy_customer_account WHERE system_user_id = ?')
    .bind(user.id)
    .first<AnyRecord>()
  const menuIds = account
    ? (
        await env.DB.prepare('SELECT menu_id FROM energy_customer_account_menu WHERE account_id = ?')
          .bind(account.id)
          .all<AnyRecord>()
      ).results.map((row) => Number(row.menu_id))
    : ENERGY_MENUS.map((item) => item.id)
  const menus = buildMenuTree(account ? ENERGY_MENUS.filter((item) => menuIds.includes(item.id)) : ENERGY_MENUS)

  return ok({
    permissions: account ? permissionsForMenus(menuIds) : ['*:*:*'],
    roles: account ? ['customer_owner'] : ['admin'],
    user: {
      id: user.id,
      username: user.username,
      nickname: user.nickname || user.username,
      avatar: '',
      deptId: 0
    },
    menus
  })
}

async function logout(request: Request, env: Env) {
  const accessToken = bearer(request)
  if (accessToken) await env.DB.prepare('DELETE FROM system_session WHERE access_token = ?').bind(accessToken).run()
  return ok(true)
}

async function refreshToken(request: Request, env: Env) {
  const url = new URL(request.url)
  const refresh = url.searchParams.get('refreshToken') || ''
  const session = await env.DB.prepare('SELECT * FROM system_session WHERE refresh_token = ?').bind(refresh).first<AnyRecord>()
  if (!session) return fail('Invalid refresh token', 401, 401)
  const refreshedUser = await env.DB.prepare('SELECT id, user_type, status FROM system_user WHERE id = ? AND status = 0')
    .bind(session.user_id)
    .first<AnyRecord>()
  if (!refreshedUser) {
    await env.DB.prepare('DELETE FROM system_session WHERE refresh_token = ?').bind(refresh).run()
    return fail('User disabled or not found', 401, 401)
  }
  const accessToken = await token('ak')
  const expiresTime = Date.now() + 7 * 24 * 60 * 60 * 1000
  await env.DB.prepare('UPDATE system_session SET access_token = ?, expires_time = ? WHERE refresh_token = ?')
    .bind(accessToken, expiresTime, refresh)
    .run()
  return ok({ accessToken, refreshToken: refresh, userId: refreshedUser.id, userType: Number(refreshedUser.user_type || 2), clientId: 'worker-admin', expiresTime })
}

async function systemCompatApi(request: Request, url: URL, env: Env, path: string, user: AnyRecord) {
  if (path === '/system/dict-data/simple-list' && request.method === 'GET') return ok(systemDictData())
  if (path === '/system/dict-data/type' && request.method === 'GET') {
    const type = url.searchParams.get('type')
    return ok(systemDictData().filter((item) => item.dictType === type))
  }
  if (path === '/system/dict-data/page' && request.method === 'GET') return ok(pageList(systemDictData(), url))
  if (path === '/system/dict-data/get' && request.method === 'GET') {
    const id = Number(url.searchParams.get('id'))
    return ok(systemDictData().find((item) => item.id === id) || null)
  }
  if (path === '/system/dict-type/simple-list' && request.method === 'GET') return ok(systemDictTypes())
  if (path === '/system/dict-type/page' && request.method === 'GET') return ok(pageList(systemDictTypes(), url))
  if (path === '/system/dict-type/get' && request.method === 'GET') {
    const id = Number(url.searchParams.get('id'))
    return ok(systemDictTypes().find((item) => item.id === id) || null)
  }

  if (path === '/system/notify-message/get-unread-count' && request.method === 'GET') return ok(0)
  if (path === '/system/notify-message/get-unread-list' && request.method === 'GET') return ok([])
  if ((path === '/system/notify-message/page' || path === '/system/notify-message/my-page') && request.method === 'GET') {
    return ok({ list: [], total: 0 })
  }
  if ((path === '/system/notify-message/update-read' || path === '/system/notify-message/update-all-read') && request.method === 'PUT') {
    return ok(true)
  }

  if (path === '/system/user/profile/get' && request.method === 'GET') return ok(profile(user))
  if (path === '/system/user/profile/update' && request.method === 'PUT') {
    const body = await readJson(request)
    await env.DB.prepare('UPDATE system_user SET nickname = ?, mobile = ?, update_time = ? WHERE id = ?')
      .bind(body.nickname || user.nickname || user.username, body.mobile || user.mobile || '', nowText(), user.id)
      .run()
    return ok(true)
  }
  if (path === '/system/user/profile/update-password' && request.method === 'PUT') {
    const body = await readJson(request)
    if (!(await verifyPassword(String(body.oldPassword || ''), String(user.password_hash || '')))) return fail('旧密码不正确', 400)
    const passwordError = validatePlainPassword(body.newPassword, true)
    if (passwordError) return fail(passwordError, 400)
    await env.DB.prepare('UPDATE system_user SET password_hash = ?, update_time = ? WHERE id = ?')
      .bind(await hashPassword(String(body.newPassword || '')), nowText(), user.id)
      .run()
    return ok(true)
  }
  if (path === '/system/user/simple-list' && request.method === 'GET') return systemUserSimpleList(env)
  if (path === '/system/user/list' && request.method === 'GET') return systemUserSimpleList(env)
  if (path === '/system/user/page' && request.method === 'GET') return systemUserPage(url, env)
  if (path === '/system/user/get' && request.method === 'GET') {
    const row = await env.DB.prepare('SELECT id, username, nickname, mobile, status, user_type, create_time FROM system_user WHERE id = ?')
      .bind(url.searchParams.get('id'))
      .first<AnyRecord>()
    return ok(row ? camel(row) : null)
  }

  if (path === '/system/dept/simple-list' || path === '/system/dept/list') return ok([])
  if (path === '/system/post/simple-list') return ok([])
  if (path === '/system/post/page') return ok({ list: [], total: 0 })
  if (path === '/system/role/simple-list') return ok([{ id: 1, name: '管理员', code: 'admin', sort: 1, status: 0 }])
  if (path === '/system/role/page') return ok({ list: [{ id: 1, name: '管理员', code: 'admin', sort: 1, status: 0 }], total: 1 })
  if (path === '/system/menu/simple-list' || path === '/system/menu/list') return ok(ENERGY_MENUS)
  if (path === '/system/tenant/simple-list') return ok([{ id: 1, name: '默认租户' }])
  if (path === '/system/tenant-package/simple-list') return ok([])
  if (path === '/system/area/tree') return ok([])
  if (path === '/system/social-user/get-bind-list') return ok([])
  if (path === '/infra/config/get-value-by-key') return ok(null)
  if (path === '/infra/redis/get-monitor-info') return ok(null)

  return null
}

function pageList(rows: AnyRecord[], url: URL) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const start = (pageNo - 1) * pageSize
  return { list: rows.slice(start, start + pageSize), total: rows.length }
}

function profile(user: AnyRecord) {
  return {
    id: user.id,
    username: user.username,
    nickname: user.nickname || user.username,
    dept: { id: 0, name: '移动储能平台' },
    roles: [{ id: 1, name: '管理员' }],
    posts: [],
    email: '',
    mobile: user.mobile || '',
    sex: 0,
    avatar: '',
    status: user.status ?? 0,
    remark: '',
    loginIp: '',
    loginDate: user.update_time || user.create_time || '',
    createTime: user.create_time || ''
  }
}

async function systemUserSimpleList(env: Env) {
  const rows = await env.DB.prepare('SELECT id, username, nickname, mobile, status, user_type, create_time FROM system_user ORDER BY id ASC LIMIT 200').all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function systemUserPage(url: URL, env: Env) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const total = await scalar(env, 'SELECT COUNT(*) FROM system_user', [])
  const rows = await env.DB.prepare('SELECT id, username, nickname, mobile, status, user_type, create_time FROM system_user ORDER BY id ASC LIMIT ? OFFSET ?')
    .bind(pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

function systemDictTypes() {
  return Array.from(new Set(systemDictData().map((item) => item.dictType))).map((type, index) => ({
    id: index + 1,
    name: dictTypeName(type),
    type,
    status: 0,
    remark: '',
    createTime: '2026-06-15 00:00:00'
  }))
}

function systemDictData() {
  let id = 1
  const row = (dictType: string, label: string, value: string, sort: number, colorType = 'default') => ({
    id: id++,
    sort,
    label,
    value,
    dictType,
    status: 0,
    colorType,
    cssClass: '',
    remark: '',
    createTime: '2026-06-15 00:00:00'
  })
  return [
    row('common_status', '启用', '0', 1, 'success'),
    row('common_status', '禁用', '1', 2, 'danger'),
    row('user_type', '管理端用户', '2', 1, 'primary'),
    row('energy_device_status', '在线', '0', 1, 'success'),
    row('energy_device_status', '离线', '1', 2, 'info'),
    row('energy_device_status', '故障', '2', 3, 'danger'),
    row('energy_device_status', '维护', '3', 4, 'warning'),
    row('energy_device_type', '储能柜', '1', 1, 'primary'),
    row('energy_device_type', '电表', '2', 2, 'success'),
    row('energy_device_type', '网关', '3', 3, 'info'),
    row('energy_run_mode', '待机', '1', 1, 'info'),
    row('energy_run_mode', '充电', '2', 2, 'success'),
    row('energy_run_mode', '放电', '3', 3, 'warning'),
    row('energy_run_mode', '故障', '4', 4, 'danger'),
    row('energy_alarm_level', '一般', '1', 1, 'warning'),
    row('energy_alarm_level', '紧急', '2', 2, 'danger'),
    row('energy_alarm_level', '严重', '3', 3, 'danger'),
    row('energy_alarm_status', '未处理', '0', 1, 'danger'),
    row('energy_alarm_status', '已确认', '1', 2, 'warning'),
    row('energy_alarm_status', '已关闭', '2', 3, 'success'),
    row('energy_session_type', '放电', '1', 1, 'warning'),
    row('energy_session_type', '充电', '2', 2, 'success'),
    row('energy_session_status', '进行中', '1', 1, 'primary'),
    row('energy_session_status', '已结束', '2', 2, 'info'),
    row('energy_session_status', '已结算', '3', 3, 'success'),
    row('energy_session_status', '异常', '4', 4, 'danger'),
    row('energy_dispatch_status', '待执行', '0', 1, 'info'),
    row('energy_dispatch_status', '执行中', '1', 2, 'primary'),
    row('energy_dispatch_status', '已完成', '2', 3, 'success'),
    row('energy_dispatch_status', '已取消', '3', 4, 'info')
  ]
}

function dictTypeName(type: string) {
  const names: Record<string, string> = {
    common_status: '通用状态',
    user_type: '用户类型',
    energy_device_status: '移动储能设备状态',
    energy_device_type: '移动储能设备类型',
    energy_run_mode: '移动储能运行模式',
    energy_alarm_level: '移动储能告警等级',
    energy_alarm_status: '移动储能告警状态',
    energy_session_type: '移动储能会话类型',
    energy_session_status: '移动储能会话状态',
    energy_dispatch_status: '移动储能调度状态'
  }
  return names[type] || type
}

async function customerAccountPage(url: URL, env: Env) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  like(url, where, args, 'a.username', 'username')
  like(url, where, args, 'a.nickname', 'nickname')
  like(url, where, args, 'a.mobile', 'mobile')
  exact(url, where, args, 'a.status', 'status')
  const suffix = where.length ? ` WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(env, `SELECT COUNT(*) FROM energy_customer_account a${suffix}`, args)
  const rows = await env.DB.prepare(
    `SELECT a.*, c.name AS customer_name FROM energy_customer_account a
     LEFT JOIN energy_customer c ON c.id = a.customer_id${suffix}
     ORDER BY a.id DESC LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

async function customerAccountGet(url: URL, env: Env) {
  const account = await env.DB.prepare(
    `SELECT a.*, c.name AS customer_name FROM energy_customer_account a
     LEFT JOIN energy_customer c ON c.id = a.customer_id WHERE a.id = ?`
  )
    .bind(url.searchParams.get('id'))
    .first<AnyRecord>()
  if (!account) return ok(null)
  const menus = await env.DB.prepare('SELECT menu_id FROM energy_customer_account_menu WHERE account_id = ?')
    .bind(account.id)
    .all<AnyRecord>()
  return ok({ ...camel(account), menuIds: menus.results.map((row) => Number(row.menu_id)) })
}

async function createCustomerAccount(request: Request, env: Env) {
  const body = await readJson(request)
  const username = String(body.username || '').trim()
  if (!username) return fail('账号不能为空', 400)
  const passwordError = validatePlainPassword(body.password, true)
  if (passwordError) return fail(passwordError, 400)
  const exists = await env.DB.prepare('SELECT id FROM system_user WHERE username = ?').bind(username).first()
  if (exists) return fail('账号已存在', 400)

  const passwordHash = await hashPassword(String(body.password))
  const user = await env.DB.prepare(
    'INSERT INTO system_user(username, password_hash, nickname, mobile, status, user_type, create_time) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id'
  )
    .bind(username, passwordHash, body.nickname || username, body.mobile || '', Number(body.status ?? 0), 2, nowText())
    .first<AnyRecord>()
  const account = await env.DB.prepare(
    `INSERT INTO energy_customer_account(customer_id, system_user_id, username, nickname, mobile, status, remark, create_time)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id`
  )
    .bind(body.customerId || null, user?.id, username, body.nickname || username, body.mobile || '', Number(body.status ?? 0), body.remark || '', nowText())
    .first<AnyRecord>()
  await replaceAccountMenus(env, Number(account?.id), Array.isArray(body.menuIds) ? body.menuIds : [])
  return ok(Number(account?.id))
}

async function updateCustomerAccount(request: Request, env: Env) {
  const body = await readJson(request)
  await env.DB.prepare(
    'UPDATE energy_customer_account SET customer_id = ?, nickname = ?, mobile = ?, status = ?, remark = ?, update_time = ? WHERE id = ?'
  )
    .bind(body.customerId || null, body.nickname || '', body.mobile || '', Number(body.status ?? 0), body.remark || '', nowText(), body.id)
    .run()
  const account = await env.DB.prepare('SELECT system_user_id FROM energy_customer_account WHERE id = ?').bind(body.id).first<AnyRecord>()
  if (account?.system_user_id) {
    await env.DB.prepare('UPDATE system_user SET nickname = ?, mobile = ?, status = ? WHERE id = ?')
      .bind(body.nickname || '', body.mobile || '', Number(body.status ?? 0), account.system_user_id)
      .run()
  }
  await replaceAccountMenus(env, Number(body.id), Array.isArray(body.menuIds) ? body.menuIds : [])
  return ok(true)
}

async function resetCustomerPassword(request: Request, env: Env) {
  const body = await readJson(request)
  const passwordError = validatePlainPassword(body.password, true)
  if (passwordError) return fail(passwordError, 400)
  const account = await env.DB.prepare('SELECT system_user_id FROM energy_customer_account WHERE id = ?').bind(body.id).first<AnyRecord>()
  if (!account) return fail('账号不存在', 404)
  await env.DB.prepare('UPDATE system_user SET password_hash = ? WHERE id = ?')
    .bind(await hashPassword(String(body.password)), account.system_user_id)
    .run()
  return ok(true)
}

function customerAccountForbidden() {
  return fail('Customer account has no permission for this operation', 403, 403)
}

async function customerApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/customer/page' && request.method === 'GET') return customerPage(url, env, accessScope)
  if (path === '/energy/customer/simple-list' && request.method === 'GET') return customerSimpleList(url, env, accessScope)
  if (path === '/energy/customer/get' && request.method === 'GET') return customerGet(url, env, accessScope)
  if (accessScope.isCustomerAccount) return customerAccountForbidden()
  return crud(request, url, env, 'energy_customer', ['name', 'contactName', 'contactMobile', 'region', 'status', 'remark'])
}

async function customerPage(url: URL, env: Env, accessScope: AccessScope) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  like(url, where, args, 'c.name', 'name')
  like(url, where, args, 'c.contact_name', 'contactName')
  like(url, where, args, 'c.contact_mobile', 'contactMobile')
  like(url, where, args, 'c.region', 'region')
  exact(url, where, args, 'c.status', 'status')
  applyCustomerScope(where, args, 'c.id', accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(env, `SELECT COUNT(*) FROM energy_customer c ${whereSql}`, args)
  const rows = await env.DB.prepare(`SELECT c.* FROM energy_customer c ${whereSql} ORDER BY c.id DESC LIMIT ? OFFSET ?`)
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

async function customerSimpleList(url: URL, env: Env, accessScope: AccessScope) {
  const where: string[] = []
  const args: any[] = []
  exact(url, where, args, 'c.status', 'status')
  applyCustomerScope(where, args, 'c.id', accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(`SELECT c.* FROM energy_customer c ${whereSql} ORDER BY c.id DESC LIMIT 200`)
    .bind(...args)
    .all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function customerGet(url: URL, env: Env, accessScope: AccessScope) {
  const where = ['c.id = ?']
  const args: any[] = [url.searchParams.get('id')]
  applyCustomerScope(where, args, 'c.id', accessScope)
  const row = await env.DB.prepare(`SELECT c.* FROM energy_customer c WHERE ${where.join(' AND ')}`).bind(...args).first<AnyRecord>()
  return ok(row ? camel(row) : null)
}

async function projectApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/project/page' && request.method === 'GET') return projectPage(url, env, accessScope)
  if (path === '/energy/project/simple-list' && request.method === 'GET') return projectSimpleList(url, env, accessScope)
  if (path === '/energy/project/get' && request.method === 'GET') return projectGet(url, env, accessScope)
  if (accessScope.isCustomerAccount) return customerAccountForbidden()
  return crud(request, url, env, 'energy_project', ['customerId', 'name', 'code', 'address', 'latitude', 'longitude', 'status', 'remark'])
}

async function projectPage(url: URL, env: Env, accessScope: AccessScope) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  like(url, where, args, 'p.name', 'name')
  like(url, where, args, 'p.code', 'code')
  like(url, where, args, 'p.address', 'address')
  exact(url, where, args, 'p.customer_id', 'customerId')
  exact(url, where, args, 'p.status', 'status')
  applyCustomerScope(where, args, 'p.customer_id', accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(env, `SELECT COUNT(*) FROM energy_project p ${whereSql}`, args)
  const rows = await env.DB.prepare(
    `SELECT p.*, c.name AS customer_name
     FROM energy_project p
     LEFT JOIN energy_customer c ON c.id = p.customer_id
     ${whereSql}
     ORDER BY p.id DESC LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

async function projectSimpleList(url: URL, env: Env, accessScope: AccessScope) {
  const where: string[] = []
  const args: any[] = []
  exact(url, where, args, 'p.customer_id', 'customerId')
  exact(url, where, args, 'p.status', 'status')
  applyCustomerScope(where, args, 'p.customer_id', accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(
    `SELECT p.*, c.name AS customer_name
     FROM energy_project p
     LEFT JOIN energy_customer c ON c.id = p.customer_id
     ${whereSql}
     ORDER BY p.id DESC LIMIT 200`
  )
    .bind(...args)
    .all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function projectGet(url: URL, env: Env, accessScope: AccessScope) {
  const where = ['p.id = ?']
  const args: any[] = [url.searchParams.get('id')]
  applyCustomerScope(where, args, 'p.customer_id', accessScope)
  const row = await env.DB.prepare(
    `SELECT p.*, c.name AS customer_name
     FROM energy_project p
     LEFT JOIN energy_customer c ON c.id = p.customer_id
     WHERE ${where.join(' AND ')}`
  )
    .bind(...args)
    .first<AnyRecord>()
  return ok(row ? camel(row) : null)
}

async function deviceApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/device/page' && request.method === 'GET') return devicePage(url, env, accessScope)
  if (path === '/energy/device/simple-list' && request.method === 'GET') return deviceSimpleList(url, env, accessScope)
  if (path === '/energy/device/get' && request.method === 'GET') return deviceGet(url, env, accessScope)
  if (path === '/energy/device/control' && request.method === 'POST') {
    if (accessScope.isCustomerAccount) return customerAccountForbidden()
    const body = await readJson(request)
    return ok({ controlLogId: Date.now(), success: true, message: `已记录 ${body.method || 'CONTROL'} 指令，等待设备网关执行` })
  }
  if (path === '/energy/device/create' && request.method === 'POST') {
    if (accessScope.isCustomerAccount) return customerAccountForbidden()
    const body = await readJson(request)
    const validation = await validateDeviceUnique(env, body)
    if (validation) return validation
    return createRowFromBody(body, env, 'energy_device', DEVICE_FIELDS)
  }
  if (path === '/energy/device/update' && request.method === 'PUT') {
    if (accessScope.isCustomerAccount) return customerAccountForbidden()
    const body = await readJson(request)
    const validation = await validateDeviceUnique(env, body, Number(body.id))
    if (validation) return validation
    return updateRowFromBody(body, env, 'energy_device', DEVICE_FIELDS)
  }
  if (accessScope.isCustomerAccount) return customerAccountForbidden()
  return crud(request, url, env, 'energy_device', DEVICE_FIELDS)
}

async function devicePage(url: URL, env: Env, accessScope: AccessScope) {
  await refreshStaleOnlineDevices(env, accessScope)
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []

  like(url, where, args, 'd.device_name', 'deviceName')
  like(url, where, args, 'd.device_no', 'deviceNo')
  like(url, where, args, 'd.gateway_sn', 'gatewaySn')
  like(url, where, args, 'd.meter_sn', 'meterSn')
  like(url, where, args, 'd.meter_no', 'meterNo')
  exact(url, where, args, 'd.device_type', 'deviceType')
  exact(url, where, args, 'd.status', 'status')
  exact(url, where, args, 'd.customer_id', 'customerId')
  exact(url, where, args, 'd.project_id', 'projectId')
  applyCustomerScope(where, args, 'COALESCE(d.customer_id, p.customer_id)', accessScope)

  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(
    env,
    `SELECT COUNT(*)
     FROM energy_device d
     LEFT JOIN energy_customer c ON c.id = d.customer_id
     LEFT JOIN energy_project p ON p.id = d.project_id
     ${whereSql}`,
    args
  )
  const rows = await env.DB.prepare(
    `SELECT d.*, c.name AS customer_name, p.name AS project_name
     FROM energy_device d
     LEFT JOIN energy_customer c ON c.id = d.customer_id
     LEFT JOIN energy_project p ON p.id = d.project_id
     ${whereSql}
     ORDER BY d.id DESC
     LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()

  const pageRows = rows.results || []
  await refreshDeviceOnlineStatus(env, pageRows.map((row) => Number(row.id)).filter(Number.isFinite))
  return ok({ list: camelRows(applyDeviceOnlineStatus(pageRows)), total })
}

async function deviceSimpleList(url: URL, env: Env, accessScope: AccessScope) {
  await refreshStaleOnlineDevices(env, accessScope)
  const where: string[] = []
  const args: any[] = []

  exact(url, where, args, 'd.customer_id', 'customerId')
  exact(url, where, args, 'd.project_id', 'projectId')
  exact(url, where, args, 'd.status', 'status')
  applyCustomerScope(where, args, 'COALESCE(d.customer_id, p.customer_id)', accessScope)

  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(
    `SELECT d.*, c.name AS customer_name, p.name AS project_name
     FROM energy_device d
     LEFT JOIN energy_customer c ON c.id = d.customer_id
     LEFT JOIN energy_project p ON p.id = d.project_id
     ${whereSql}
     ORDER BY d.id DESC
     LIMIT 200`
  )
    .bind(...args)
    .all<AnyRecord>()

  const listRows = rows.results || []
  await refreshDeviceOnlineStatus(env, listRows.map((row) => Number(row.id)).filter(Number.isFinite))
  return ok(camelRows(applyDeviceOnlineStatus(listRows)))
}

async function deviceGet(url: URL, env: Env, accessScope: AccessScope) {
  await refreshStaleOnlineDevices(env, accessScope)
  const where = ['d.id = ?']
  const args: any[] = [url.searchParams.get('id')]
  applyCustomerScope(where, args, 'COALESCE(d.customer_id, p.customer_id)', accessScope)
  const row = await env.DB.prepare(
    `SELECT d.*, c.name AS customer_name, p.name AS project_name
     FROM energy_device d
     LEFT JOIN energy_customer c ON c.id = d.customer_id
     LEFT JOIN energy_project p ON p.id = d.project_id
     WHERE ${where.join(' AND ')}`
  )
    .bind(...args)
    .first<AnyRecord>()
  if (row?.id) await refreshDeviceOnlineStatus(env, [Number(row.id)])
  return ok(row ? camel(applyDeviceOnlineStatus([row])[0]) : null)
}

async function alarmApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/alarm/ack' && request.method === 'PUT') return updateStatus(request, env, 'energy_alarm', 1, 'ack_time')
  if (path === '/energy/alarm/close' && request.method === 'PUT') return updateStatus(request, env, 'energy_alarm', 2, 'close_time')
  if (path === '/energy/alarm/page' && request.method === 'GET') return alarmPage(url, env, accessScope)
  return crud(request, url, env, 'energy_alarm', ['alarmNo', 'deviceId', 'code', 'level', 'title', 'content', 'status', 'occurTime'])
}

async function alarmPage(url: URL, env: Env, accessScope: AccessScope) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []

  like(url, where, args, 'a.alarm_no', 'alarmNo')
  like(url, where, args, 'a.code', 'code')
  like(url, where, args, 'a.title', 'title')
  exact(url, where, args, 'a.device_id', 'deviceId')
  exact(url, where, args, 'a.level', 'level')
  exact(url, where, args, 'a.status', 'status')
  applyCustomerScope(where, args, 'COALESCE(d.customer_id, p.customer_id)', accessScope)

  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(
    env,
    `SELECT COUNT(*)
     FROM energy_alarm a
     LEFT JOIN energy_device d ON d.id = a.device_id
     LEFT JOIN energy_project p ON p.id = d.project_id
     ${whereSql}`,
    args
  )
  const rows = await env.DB.prepare(
    `SELECT a.*, d.device_name, d.device_no, d.customer_id, d.project_id, c.name AS customer_name, p.name AS project_name
     FROM energy_alarm a
     LEFT JOIN energy_device d ON d.id = a.device_id
     LEFT JOIN energy_customer c ON c.id = d.customer_id
     LEFT JOIN energy_project p ON p.id = d.project_id
     ${whereSql}
     ORDER BY a.id DESC
     LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()

  return ok({ list: camelRows(rows.results), total })
}

async function vehicleApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/vehicle/page' && request.method === 'GET') return vehiclePage(url, env, accessScope)
  if (path === '/energy/vehicle/simple-list' && request.method === 'GET') return vehicleSimpleList(url, env, accessScope)
  if (path === '/energy/vehicle/get' && request.method === 'GET') return vehicleGet(url, env, accessScope)
  if (accessScope.isCustomerAccount) return customerAccountForbidden()
  return crud(request, url, env, 'energy_vehicle', ['vehicleNo', 'plateNo', 'qrCode', 'deviceId', 'customerId', 'projectId', 'status', 'remark'])
}

async function vehiclePage(url: URL, env: Env, accessScope: AccessScope) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  like(url, where, args, 'v.vehicle_no', 'vehicleNo')
  like(url, where, args, 'v.plate_no', 'plateNo')
  like(url, where, args, 'v.card_no', 'cardNo')
  exact(url, where, args, 'v.device_id', 'deviceId')
  exact(url, where, args, 'v.customer_id', 'customerId')
  exact(url, where, args, 'v.project_id', 'projectId')
  exact(url, where, args, 'v.status', 'status')
  applyCustomerScope(where, args, vehicleCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(env, `SELECT COUNT(*) FROM energy_vehicle v ${vehicleScopeJoins()} ${whereSql}`, args)
  const rows = await env.DB.prepare(
    `SELECT v.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_vehicle v
     ${vehicleScopeJoins()}
     ${whereSql}
     ORDER BY v.id DESC LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

async function vehicleSimpleList(url: URL, env: Env, accessScope: AccessScope) {
  const where: string[] = []
  const args: any[] = []
  exact(url, where, args, 'v.customer_id', 'customerId')
  exact(url, where, args, 'v.project_id', 'projectId')
  exact(url, where, args, 'v.status', 'status')
  applyCustomerScope(where, args, vehicleCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(
    `SELECT v.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_vehicle v
     ${vehicleScopeJoins()}
     ${whereSql}
     ORDER BY v.id DESC LIMIT 200`
  )
    .bind(...args)
    .all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function vehicleGet(url: URL, env: Env, accessScope: AccessScope) {
  const where = ['v.id = ?']
  const args: any[] = [url.searchParams.get('id')]
  applyCustomerScope(where, args, vehicleCustomerColumn(), accessScope)
  const row = await env.DB.prepare(
    `SELECT v.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_vehicle v
     ${vehicleScopeJoins()}
     WHERE ${where.join(' AND ')}`
  )
    .bind(...args)
    .first<AnyRecord>()
  return ok(row ? camel(row) : null)
}

async function accountEventApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/account-event/page' && request.method === 'GET') return accountEventPage(url, env, accessScope)
  if (path === '/energy/account-event/simple-list' && request.method === 'GET') return accountEventSimpleList(url, env, accessScope)
  if (path === '/energy/account-event/get' && request.method === 'GET') return accountEventGet(url, env, accessScope)
  if (accessScope.isCustomerAccount) return customerAccountForbidden()
  return crud(request, url, env, 'energy_account_event', ['eventScene', 'authType', 'scanText', 'cardNo', 'accountKnown', 'accountId', 'accountName', 'accountMobile', 'deviceId', 'customerId', 'projectId', 'resultMessage'])
}

async function accountEventPage(url: URL, env: Env, accessScope: AccessScope) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  like(url, where, args, 'e.scan_text', 'scanText')
  like(url, where, args, 'e.card_no', 'cardNo')
  like(url, where, args, 'e.account_name', 'accountName')
  exact(url, where, args, 'e.event_scene', 'eventScene')
  exact(url, where, args, 'e.auth_type', 'authType')
  exact(url, where, args, 'e.account_known', 'accountKnown')
  exact(url, where, args, 'e.device_id', 'deviceId')
  exact(url, where, args, 'e.customer_id', 'customerId')
  exact(url, where, args, 'e.project_id', 'projectId')
  applyCustomerScope(where, args, eventCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(env, `SELECT COUNT(*) FROM energy_account_event e ${eventScopeJoins()} ${whereSql}`, args)
  const rows = await env.DB.prepare(
    `SELECT e.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_account_event e
     ${eventScopeJoins()}
     ${whereSql}
     ORDER BY e.id DESC LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

async function accountEventSimpleList(url: URL, env: Env, accessScope: AccessScope) {
  const where: string[] = []
  const args: any[] = []
  exact(url, where, args, 'e.customer_id', 'customerId')
  exact(url, where, args, 'e.project_id', 'projectId')
  exact(url, where, args, 'e.device_id', 'deviceId')
  applyCustomerScope(where, args, eventCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(
    `SELECT e.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_account_event e
     ${eventScopeJoins()}
     ${whereSql}
     ORDER BY e.id DESC LIMIT 200`
  )
    .bind(...args)
    .all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function accountEventGet(url: URL, env: Env, accessScope: AccessScope) {
  const where = ['e.id = ?']
  const args: any[] = [url.searchParams.get('id')]
  applyCustomerScope(where, args, eventCustomerColumn(), accessScope)
  const row = await env.DB.prepare(
    `SELECT e.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_account_event e
     ${eventScopeJoins()}
     WHERE ${where.join(' AND ')}`
  )
    .bind(...args)
    .first<AnyRecord>()
  return ok(row ? camel(row) : null)
}

async function userScopeApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/user-scope/page' && request.method === 'GET') return userScopePage(url, env, accessScope)
  if (path === '/energy/user-scope/simple-list' && request.method === 'GET') return userScopeSimpleList(url, env, accessScope)
  if (path === '/energy/user-scope/get' && request.method === 'GET') return userScopeGet(url, env, accessScope)
  if (accessScope.isCustomerAccount) return customerAccountForbidden()
  return crud(request, url, env, 'energy_user_scope', ['userId', 'userType', 'customerId', 'projectId', 'deviceId', 'status', 'remark'])
}

async function userScopePage(url: URL, env: Env, accessScope: AccessScope) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  exact(url, where, args, 's.user_id', 'userId')
  exact(url, where, args, 's.user_type', 'userType')
  exact(url, where, args, 's.customer_id', 'customerId')
  exact(url, where, args, 's.project_id', 'projectId')
  exact(url, where, args, 's.device_id', 'deviceId')
  exact(url, where, args, 's.status', 'status')
  applyCustomerScope(where, args, userScopeCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(env, `SELECT COUNT(*) FROM energy_user_scope s ${userScopeJoins()} ${whereSql}`, args)
  const rows = await env.DB.prepare(
    `SELECT s.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_user_scope s
     ${userScopeJoins()}
     ${whereSql}
     ORDER BY s.id DESC LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

async function userScopeSimpleList(url: URL, env: Env, accessScope: AccessScope) {
  const where: string[] = []
  const args: any[] = []
  exact(url, where, args, 's.customer_id', 'customerId')
  exact(url, where, args, 's.project_id', 'projectId')
  exact(url, where, args, 's.device_id', 'deviceId')
  exact(url, where, args, 's.status', 'status')
  applyCustomerScope(where, args, userScopeCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(
    `SELECT s.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_user_scope s
     ${userScopeJoins()}
     ${whereSql}
     ORDER BY s.id DESC LIMIT 200`
  )
    .bind(...args)
    .all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function userScopeGet(url: URL, env: Env, accessScope: AccessScope) {
  const where = ['s.id = ?']
  const args: any[] = [url.searchParams.get('id')]
  applyCustomerScope(where, args, userScopeCustomerColumn(), accessScope)
  const row = await env.DB.prepare(
    `SELECT s.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_user_scope s
     ${userScopeJoins()}
     WHERE ${where.join(' AND ')}`
  )
    .bind(...args)
    .first<AnyRecord>()
  return ok(row ? camel(row) : null)
}

async function pricingRuleApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  await ensurePricingRuleFeeColumns(env)
  if (path === '/energy/pricing-rule/page' && request.method === 'GET') return pricingRulePage(url, env, accessScope)
  if (path === '/energy/pricing-rule/simple-list' && request.method === 'GET') return pricingRuleSimpleList(url, env, accessScope)
  if (path === '/energy/pricing-rule/get' && request.method === 'GET') return pricingRuleGet(url, env, accessScope)
  if (path === '/energy/pricing-rule/match') {
    return matchPricingRule(url, env, accessScope)
  }
  if (accessScope.isCustomerAccount) return customerAccountForbidden()
  if (path === '/energy/pricing-rule/create' && request.method === 'POST') return createPricingRule(request, env)
  if (path === '/energy/pricing-rule/update' && request.method === 'PUT') return updatePricingRule(request, env)
  return crud(request, url, env, 'energy_pricing_rule', PRICING_RULE_FIELDS)
}

async function pricingRulePage(url: URL, env: Env, accessScope: AccessScope) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  const { start, end } = rangeParam(url, 'effectiveStart')

  exact(url, where, args, 'r.customer_id', 'customerId')
  exact(url, where, args, 'r.project_id', 'projectId')
  exact(url, where, args, 'r.device_id', 'deviceId')
  exact(url, where, args, 'r.status', 'status')
  applyCustomerScope(where, args, pricingRuleCustomerColumn(), accessScope)
  if (start) {
    where.push('r.effective_start >= ?')
    args.push(start)
  }
  if (end) {
    where.push('r.effective_start <= ?')
    args.push(end)
  }

  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(
    env,
    `SELECT COUNT(*)
     FROM energy_pricing_rule r
     LEFT JOIN energy_customer c ON c.id = r.customer_id
     LEFT JOIN energy_project p ON p.id = r.project_id
     LEFT JOIN energy_device d ON d.id = r.device_id
     LEFT JOIN energy_project dp ON dp.id = d.project_id
     ${whereSql}`,
    args
  )
  const rows = await env.DB.prepare(
    `SELECT r.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_pricing_rule r
     LEFT JOIN energy_customer c ON c.id = r.customer_id
     LEFT JOIN energy_project p ON p.id = r.project_id
     LEFT JOIN energy_device d ON d.id = r.device_id
     LEFT JOIN energy_project dp ON dp.id = d.project_id
     ${whereSql}
     ORDER BY r.id DESC
     LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()

  return ok({ list: camelRows(rows.results), total })
}

async function pricingRuleSimpleList(url: URL, env: Env, accessScope: AccessScope) {
  const where: string[] = []
  const args: any[] = []
  exact(url, where, args, 'r.customer_id', 'customerId')
  exact(url, where, args, 'r.project_id', 'projectId')
  exact(url, where, args, 'r.device_id', 'deviceId')
  exact(url, where, args, 'r.status', 'status')
  applyCustomerScope(where, args, pricingRuleCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(
    `SELECT r.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_pricing_rule r
     LEFT JOIN energy_customer c ON c.id = r.customer_id
     LEFT JOIN energy_project p ON p.id = r.project_id
     LEFT JOIN energy_device d ON d.id = r.device_id
     LEFT JOIN energy_project dp ON dp.id = d.project_id
     ${whereSql}
     ORDER BY r.id DESC LIMIT 200`
  )
    .bind(...args)
    .all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function pricingRuleGet(url: URL, env: Env, accessScope: AccessScope) {
  const where = ['r.id = ?']
  const args: any[] = [url.searchParams.get('id')]
  applyCustomerScope(where, args, pricingRuleCustomerColumn(), accessScope)
  const row = await env.DB.prepare(
    `SELECT r.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_pricing_rule r
     LEFT JOIN energy_customer c ON c.id = r.customer_id
     LEFT JOIN energy_project p ON p.id = r.project_id
     LEFT JOIN energy_device d ON d.id = r.device_id
     LEFT JOIN energy_project dp ON dp.id = d.project_id
     WHERE ${where.join(' AND ')}`
  )
    .bind(...args)
    .first<AnyRecord>()
  return ok(row ? camel(row) : null)
}

async function matchPricingRule(url: URL, env: Env, accessScope: AccessScope) {
  const deviceId = url.searchParams.get('deviceId')
  if (!deviceId) return ok(null)

  const device = await env.DB.prepare(
    `SELECT d.id, d.customer_id, d.project_id, p.customer_id AS project_customer_id
     FROM energy_device d
     LEFT JOIN energy_project p ON p.id = d.project_id
     WHERE d.id = ?`
  )
    .bind(deviceId)
    .first<AnyRecord>()
  if (!device) return ok(null)
  if (accessScope.isCustomerAccount && Number(device.customer_id || device.project_customer_id || 0) !== Number(accessScope.customerId || 0)) return ok(null)

  const billingTime = cleanText(url.searchParams.get('billingTime')) || nowText()
  const customerId = device.customer_id || null
  const projectId = device.project_id || null
  const row = await env.DB.prepare(
    `SELECT r.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_pricing_rule r
     LEFT JOIN energy_customer c ON c.id = r.customer_id
     LEFT JOIN energy_project p ON p.id = r.project_id
     LEFT JOIN energy_device d ON d.id = r.device_id
     WHERE r.status = 0
       AND (? IS NULL OR r.effective_start IS NULL OR r.effective_start <= ?)
        AND (? IS NULL OR r.effective_end IS NULL OR ${pricingRuleEffectiveEndSql()} >= ?)
       AND (
         r.device_id = ?
         OR (r.device_id IS NULL AND r.project_id = ?)
         OR (r.device_id IS NULL AND r.project_id IS NULL AND r.customer_id = ?)
       )
     ORDER BY
       CASE
         WHEN r.device_id = ? THEN 3
         WHEN r.device_id IS NULL AND r.project_id = ? THEN 2
         WHEN r.device_id IS NULL AND r.project_id IS NULL AND r.customer_id = ? THEN 1
         ELSE 0
       END DESC,
       r.effective_start DESC,
       r.id DESC
     LIMIT 1`
  )
    .bind(billingTime, billingTime, billingTime, billingTime, deviceId, projectId, customerId, deviceId, projectId, customerId)
    .first<AnyRecord>()

  return ok(row ? camel(row) : null)
}

async function createPricingRule(request: Request, env: Env) {
  const body = normalizePricingRuleScope(await readJson(request))
  const validation = validatePricingRuleScope(body)
  if (validation) return validation
  return createRowFromBody(body, env, 'energy_pricing_rule', PRICING_RULE_FIELDS)
}

async function updatePricingRule(request: Request, env: Env) {
  const body = normalizePricingRuleScope(await readJson(request))
  const validation = validatePricingRuleScope(body)
  if (validation) return validation
  return updateRowFromBody(body, env, 'energy_pricing_rule', PRICING_RULE_FIELDS)
}

function normalizePricingRuleScope(body: AnyRecord) {
  const deviceId = positiveId(body.deviceId)
  const projectId = positiveId(body.projectId)
  const customerId = positiveId(body.customerId)
  return {
    ...body,
    deviceId,
    projectId: deviceId ? null : projectId,
    customerId: deviceId || projectId ? null : customerId
  }
}

function validatePricingRuleScope(body: AnyRecord) {
  const count = [body.customerId, body.projectId, body.deviceId].filter((value) => positiveId(value)).length
  if (count !== 1) return fail('计费规则必须且只能绑定一个客户、项目或设备', 400)
  return null
}

async function ensurePricingRuleFeeColumns(env: Env) {
  const info = await env.DB.prepare('PRAGMA table_info(energy_pricing_rule)').all<AnyRecord>()
  const existing = new Set((info.results || []).map((row) => String(row.name)))
  for (const [column, definition] of Object.entries(PRICING_RULE_FEE_COLUMNS)) {
    if (!existing.has(column)) {
      try {
        await env.DB.prepare(`ALTER TABLE energy_pricing_rule ADD COLUMN ${column} ${definition}`).run()
      } catch (error) {
        const message = error instanceof Error ? error.message : String(error)
        if (!message.toLowerCase().includes('duplicate column')) {
          throw error
        }
      }
    }
  }
}

async function chargeSessionApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/charge-session/page' && request.method === 'GET') return chargeSessionPage(url, env, accessScope)
  if (path === '/energy/charge-session/simple-list' && request.method === 'GET') return chargeSessionSimpleList(url, env, accessScope)
  if (path === '/energy/charge-session/get' && request.method === 'GET') return chargeSessionGet(url, env, accessScope)
  if (accessScope.isCustomerAccount) return customerAccountForbidden()
  if (path === '/energy/charge-session/start' && request.method === 'POST') {
    const body = await readJson(request)
    const sessionNo = `CS${Date.now()}`
    const row = await env.DB.prepare(
      `INSERT INTO energy_charge_session(session_no, device_id, pricing_rule_id, session_type, start_time, status, create_time)
       VALUES (?, ?, ?, ?, ?, 1, ?) RETURNING id`
    )
      .bind(sessionNo, body.deviceId || null, body.pricingRuleId || null, body.sessionType || 1, nowText(), nowText())
      .first<AnyRecord>()
    return ok(Number(row?.id))
  }
  if (path === '/energy/charge-session/stop' && request.method === 'POST') {
    const body = await readJson(request)
    await env.DB.prepare('UPDATE energy_charge_session SET end_time = ?, end_energy = ?, status = 2, update_time = ? WHERE id = ?')
      .bind(nowText(), body.endEnergy || null, nowText(), body.sessionId)
      .run()
    return ok(true)
  }
  if (path === '/energy/charge-session/settle' && request.method === 'POST') {
    const body = await readJson(request)
    await env.DB.prepare('UPDATE energy_charge_session SET status = 3, update_time = ? WHERE id = ?')
      .bind(nowText(), body.sessionId)
      .run()
    return ok(true)
  }
  return crud(request, url, env, 'energy_charge_session', ['sessionNo', 'deviceId', 'customerId', 'pricingRuleId', 'sessionType', 'startTime', 'endTime', 'startEnergy', 'endEnergy', 'totalEnergy', 'durationMinutes', 'energyFee', 'timeFee', 'totalFee', 'status'])
}

async function chargeSessionPage(url: URL, env: Env, accessScope: AccessScope) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  const { start, end } = rangeParam(url, 'startTime')
  like(url, where, args, 's.session_no', 'sessionNo')
  exact(url, where, args, 's.device_id', 'deviceId')
  exact(url, where, args, 's.customer_id', 'customerId')
  exact(url, where, args, 's.pricing_rule_id', 'pricingRuleId')
  exact(url, where, args, 's.session_type', 'sessionType')
  exact(url, where, args, 's.status', 'status')
  if (start) {
    where.push('s.start_time >= ?')
    args.push(start)
  }
  if (end) {
    where.push('s.start_time <= ?')
    args.push(end)
  }
  applyCustomerScope(where, args, chargeSessionCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const total = await scalar(env, `SELECT COUNT(*) FROM energy_charge_session s ${chargeSessionJoins()} ${whereSql}`, args)
  const rows = await env.DB.prepare(
    `SELECT s.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_charge_session s
     ${chargeSessionJoins()}
     ${whereSql}
     ORDER BY s.id DESC LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

async function chargeSessionSimpleList(url: URL, env: Env, accessScope: AccessScope) {
  const where: string[] = []
  const args: any[] = []
  exact(url, where, args, 's.device_id', 'deviceId')
  exact(url, where, args, 's.customer_id', 'customerId')
  exact(url, where, args, 's.status', 'status')
  applyCustomerScope(where, args, chargeSessionCustomerColumn(), accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(
    `SELECT s.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_charge_session s
     ${chargeSessionJoins()}
     ${whereSql}
     ORDER BY s.id DESC LIMIT 200`
  )
    .bind(...args)
    .all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function chargeSessionGet(url: URL, env: Env, accessScope: AccessScope) {
  const where = ['s.id = ?']
  const args: any[] = [url.searchParams.get('id')]
  applyCustomerScope(where, args, chargeSessionCustomerColumn(), accessScope)
  const row = await env.DB.prepare(
    `SELECT s.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_charge_session s
     ${chargeSessionJoins()}
     WHERE ${where.join(' AND ')}`
  )
    .bind(...args)
    .first<AnyRecord>()
  return ok(row ? camel(row) : null)
}

async function telemetryApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/telemetry/chart') {
    const { start, end } = collectTimeRange(url)
    const where: string[] = ['(? IS NULL OR t.device_id = ?)']
    const args: any[] = [url.searchParams.get('deviceId'), url.searchParams.get('deviceId')]
  applyCustomerScope(where, args, 'COALESCE(d.customer_id, p.customer_id)', accessScope)
    const rows = await env.DB.prepare(
      `SELECT t.*
       FROM energy_telemetry t
       LEFT JOIN energy_device d ON d.id = t.device_id
       LEFT JOIN energy_project p ON p.id = d.project_id
       WHERE ${where.join(' AND ')}
       AND (? IS NULL OR collect_time >= ?)
       AND (? IS NULL OR collect_time <= ?)
       ORDER BY collect_time ASC LIMIT ?`
    )
      .bind(...args, start, start, end, end, num(url.searchParams.get('limit'), 288))
      .all<AnyRecord>()
    return ok(camelRows(rows.results))
  }
  if (path === '/energy/telemetry/daily-stat') {
    const requested = snake(String(url.searchParams.get('field') || 'p'))
    const field = DAILY_FIELDS.has(requested) ? requested : 'p'
    const where: string[] = ['(? IS NULL OR t.device_id = ?)']
    const args: any[] = [url.searchParams.get('deviceId'), url.searchParams.get('deviceId')]
  applyCustomerScope(where, args, 'COALESCE(d.customer_id, p.customer_id)', accessScope)
    const rows = await env.DB.prepare(
      `SELECT substr(t.collect_time, 1, 10) AS date, MAX(t.${field}) AS max, MIN(t.${field}) AS min, AVG(t.${field}) AS avg
       FROM energy_telemetry t
       LEFT JOIN energy_device d ON d.id = t.device_id
       LEFT JOIN energy_project p ON p.id = d.project_id
       WHERE ${where.join(' AND ')}
       GROUP BY substr(t.collect_time, 1, 10) ORDER BY date DESC LIMIT 31`
    )
      .bind(...args)
      .all<AnyRecord>()
    return ok(camelRows(rows.results))
  }
  return crud(request, url, env, 'energy_telemetry', ['deviceId', 'gatewaySn', 'meterSn', 'meterNo', 'collectTime', 'timestamp', 'source', 'state', 'pa', 'pb', 'pc', 'ua', 'ub', 'uc', 'ia', 'ib', 'ic', 'p', 'pf', 'epi', 'epij', 'epif', 'epip', 'epig', 'epe', 'epej', 'epef', 'epep', 'epeg'])
}

async function reportApi(request: Request, url: URL, env: Env, path: string, accessScope: AccessScope) {
  if (path === '/energy/report/bill' && request.method === 'GET') return reportBill(url, env, accessScope)
  return fail('接口方法不匹配', 405, 405)
}

async function reportBill(url: URL, env: Env, accessScope: AccessScope) {
  const billMonth = cleanText(url.searchParams.get('billMonth')) || localNowText().slice(0, 7)
  const monthStart = `${billMonth}-01 00:00:00`
  const monthEnd = endOfMonthText(billMonth)
  const scopeType = cleanText(url.searchParams.get('scopeType')) || 'all'
  const devices = await reportDevices(url, env, accessScope, scopeType)
  const deviceIds = devices.map((device) => Number(device.id)).filter(Number.isFinite)
  if (!deviceIds.length) {
    return ok(emptyReportBill(billMonth, monthStart, monthEnd, scopeType))
  }

  const rulesByDevice = new Map<number, AnyRecord | null>()
  for (const device of devices) {
    rulesByDevice.set(Number(device.id), await findPricingRuleForDevice(env, Number(device.id), monthEnd))
  }

  const deviceDetails = await Promise.all(devices.map((device) => buildReportDeviceDetail(env, device, rulesByDevice.get(Number(device.id)) || null, monthStart, monthEnd)))
  const telemetryRows = await reportTelemetryRows(env, deviceIds, monthStart, monthEnd)
  const chargeEnergy = sumDeviceTouEnergy(deviceDetails, 'chargeTou')
  const dischargeEnergy = sumDeviceTouEnergy(deviceDetails, 'dischargeTou')
  const totalChargeEnergy = round4(sumTouEnergy(chargeEnergy) || deviceDetails.reduce((sum, item) => sum + item.chargeEnergy, 0))
  const totalDischargeEnergy = round4(sumTouEnergy(dischargeEnergy) || deviceDetails.reduce((sum, item) => sum + item.dischargeEnergy, 0))
  const ruleRows = uniqueRules(Array.from(rulesByDevice.values()).filter(Boolean) as AnyRecord[])
  const feeDetails = buildReportFeeDetails(deviceDetails, ruleRows, telemetryRows)
  const unmatchedPricingDetails = buildUnmatchedPricingDetails(deviceDetails)
  const totalFee = reportBillingFeeTotal(feeDetails)
  const purchaseCosts = deviceDetails.map((item) => numberOrNull(item.purchaseCost))
  const chargeCost = purchaseCosts.some((value) => value !== null)
    ? round2(purchaseCosts.reduce((sum, value) => sum + Number(value || 0), 0))
    : null
  const salesRevenue = round2(deviceDetails.reduce((sum, item) => sum + Number(item.salesRevenue || 0), 0))

  return ok({
    billMonth,
    billRange: { start: monthStart, end: monthEnd },
    scopeType,
    scopeName: reportScopeName(scopeType, devices),
    billHeader: buildReportHeader(devices, ruleRows, billMonth, monthStart, monthEnd, scopeType),
    summary: {
      deviceCount: devices.length,
      totalChargeEnergy,
      totalDischargeEnergy,
      totalFee,
      chargeCost,
      averageBuyRate: chargeCost !== null && totalChargeEnergy > 0 ? round4(chargeCost / totalChargeEnergy) : null,
      salesRevenue,
      averageSellRate: totalDischargeEnergy > 0 ? round4(salesRevenue / totalDischargeEnergy) : null,
      savedCost: chargeCost !== null ? round2(salesRevenue - chargeCost) : null,
      touSource: deviceDetails.some((item) => item.touSource === 'interval') ? 'interval' : 'telemetry'
    },
    deviceDetails,
    energyDetails: buildReportEnergyDetails(deviceDetails, chargeEnergy, dischargeEnergy),
    feeDetails,
    unmatchedPricingDetails,
    analysis: {
      chargeTou: chargeEnergy,
      dischargeTou: dischargeEnergy,
      chargeConsistency: consistencyStatus(chargeEnergy, totalChargeEnergy),
      dischargeConsistency: consistencyStatus(dischargeEnergy, totalDischargeEnergy)
    }
  })
}

async function reportDevices(url: URL, env: Env, accessScope: AccessScope, scopeType: string) {
  const where: string[] = []
  const args: any[] = []
  if (scopeType === 'project') {
    where.push('d.project_id = ?')
    args.push(url.searchParams.get('projectId'))
  }
  if (scopeType === 'device') {
    where.push('d.id = ?')
    args.push(url.searchParams.get('deviceId'))
  }
  applyCustomerScope(where, args, 'COALESCE(d.customer_id, p.customer_id)', accessScope)
  const whereSql = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const rows = await env.DB.prepare(
    `SELECT d.*, c.name AS customer_name, p.name AS project_name, p.customer_id AS project_customer_id
     FROM energy_device d
     LEFT JOIN energy_project p ON p.id = d.project_id
     LEFT JOIN energy_customer c ON c.id = COALESCE(d.customer_id, p.customer_id)
     ${whereSql}
     ORDER BY d.id DESC
     LIMIT 1000`
  )
    .bind(...args)
    .all<AnyRecord>()
  return rows.results || []
}

async function buildReportDeviceDetail(env: Env, device: AnyRecord, rule: AnyRecord | null, start: string, end: string) {
  const first = await firstTelemetryInRange(env, Number(device.id), start, end)
  const last = await lastTelemetryInRange(env, Number(device.id), start, end)
  const startEpi = numberOrNull(first?.epi)
  const endEpi = numberOrNull(last?.epi)
  const startEpe = numberOrNull(first?.epe)
  const endEpe = numberOrNull(last?.epe)
  const chargeEnergy = startEpi !== null && endEpi !== null ? round4(Math.max(0, endEpi - startEpi)) : 0
  const dischargeEnergy = startEpe !== null && endEpe !== null ? round4(Math.max(0, endEpe - startEpe)) : 0
  let chargeTou = await reportDeviceTouEnergy(env, Number(device.id), start, end, 'charge')
  let dischargeTou = await reportDeviceTouEnergy(env, Number(device.id), start, end, 'discharge')
  let touSource = sumTouEnergy(chargeTou) > 0 || sumTouEnergy(dischargeTou) > 0 ? 'interval' : 'telemetry'
  if (sumTouEnergy(chargeTou) <= 0 && chargeEnergy > 0) {
    chargeTou = splitEnergyByPricingTime(
      cleanText(first?.collect_time) || start,
      cleanText(last?.collect_time) || end,
      chargeEnergy,
      parseTouPeriods(rule?.tou_periods)
    )
    touSource = 'timeRule'
  }
  if (sumTouEnergy(dischargeTou) <= 0 && dischargeEnergy > 0) {
    dischargeTou = splitEnergyByPricingTime(
      cleanText(first?.collect_time) || start,
      cleanText(last?.collect_time) || end,
      dischargeEnergy,
      parseTouPeriods(rule?.tou_periods)
    )
    touSource = 'timeRule'
  }
  const hasChargeTou = sumTouEnergy(chargeTou) > 0
  const hasDischargeTou = sumTouEnergy(dischargeTou) > 0
  const rates = rule ? averageTouRates([rule]) : emptyTouEnergy()
  return {
    deviceId: device.id,
    deviceName: device.device_name || device.device_no || `电表 ${device.id}`,
    deviceNo: device.device_no || '',
    meterNo: device.meter_no || '',
    projectId: device.project_id || null,
    projectName: device.project_name || '',
    customerId: device.customer_id || device.project_customer_id || null,
    customerName: device.customer_name || '',
    startEpi,
    endEpi,
    startEpe,
    endEpe,
    chargeEnergy: hasChargeTou ? sumTouEnergy(chargeTou) : chargeEnergy,
    dischargeEnergy: hasDischargeTou ? sumTouEnergy(dischargeTou) : dischargeEnergy,
    chargeTou,
    dischargeTou,
    purchaseCost: rule && hasChargeTou ? sumByTou(chargeTou, rates) : null,
    salesRevenue: rule && hasDischargeTou ? sumByTou(dischargeTou, rates) : 0,
    touSource,
    pricingRuleId: rule?.id || null
  }
}

async function reportDeviceTouEnergy(env: Env, deviceId: number, start: string, end: string, type: 'charge' | 'discharge') {
  const intervals = await reportIntervals(env, [deviceId], start, end)
  const intervalSummary = summarizeIntervals(intervals)
  const intervalEnergy = type === 'charge' ? intervalSummary.charge : intervalSummary.discharge
  if (sumTouEnergy(intervalEnergy) > 0) return intervalEnergy

  const first = await firstTelemetryInRange(env, deviceId, start, end)
  const last = await lastTelemetryInRange(env, deviceId, start, end)
  const fields = type === 'charge'
    ? ['epij', 'epif', 'epip', 'epig']
    : ['epej', 'epef', 'epep', 'epeg']
  return roundTouEnergy({
    sharpPeak: positiveEnergyDelta(first || {}, last || {}, fields[0]),
    peak: positiveEnergyDelta(first || {}, last || {}, fields[1]),
    flat: positiveEnergyDelta(first || {}, last || {}, fields[2]),
    valley: positiveEnergyDelta(first || {}, last || {}, fields[3]),
    deepValley: 0
  })
}

function sumDeviceTouEnergy(rows: AnyRecord[], field: 'chargeTou' | 'dischargeTou') {
  const total = emptyTouEnergy()
  rows.forEach((row) => {
    const energy = row[field] || {}
    ;(Object.keys(total) as TouKey[]).forEach((key) => {
      total[key] += Number(energy[key] || 0)
    })
  })
  return roundTouEnergy(total)
}

async function firstTelemetryInRange(env: Env, deviceId: number, start: string, end: string) {
  return env.DB.prepare(
    `SELECT * FROM energy_telemetry WHERE device_id = ? AND collect_time >= ? AND collect_time <= ? ORDER BY collect_time ASC, id ASC LIMIT 1`
  ).bind(deviceId, start, end).first<AnyRecord>()
}

async function lastTelemetryInRange(env: Env, deviceId: number, start: string, end: string) {
  return env.DB.prepare(
    `SELECT * FROM energy_telemetry WHERE device_id = ? AND collect_time >= ? AND collect_time <= ? ORDER BY collect_time DESC, id DESC LIMIT 1`
  ).bind(deviceId, start, end).first<AnyRecord>()
}

async function reportIntervals(env: Env, deviceIds: number[], start: string, end: string) {
  const placeholders = deviceIds.map(() => '?').join(',')
  const rows = await env.DB.prepare(
    `SELECT * FROM energy_telemetry_interval
     WHERE device_id IN (${placeholders}) AND end_time >= ? AND end_time <= ?
     ORDER BY end_time ASC`
  )
    .bind(...deviceIds, start, end)
    .all<AnyRecord>()
  return rows.results || []
}

async function reportTelemetryRows(env: Env, deviceIds: number[], start: string, end: string) {
  const placeholders = deviceIds.map(() => '?').join(',')
  const rows = await env.DB.prepare(
    `SELECT device_id, collect_time, p
     FROM energy_telemetry
     WHERE device_id IN (${placeholders}) AND collect_time >= ? AND collect_time <= ?
     ORDER BY collect_time ASC, id ASC`
  )
    .bind(...deviceIds, start, end)
    .all<AnyRecord>()
  return rows.results || []
}

function summarizeIntervals(intervals: AnyRecord[]) {
  const charge = emptyTouEnergy()
  const discharge = emptyTouEnergy()
  intervals.forEach((row) => {
    charge.sharpPeak += Number(row.charge_sharp_peak || 0)
    charge.peak += Number(row.charge_peak || 0)
    charge.flat += Number(row.charge_flat || 0)
    charge.valley += Number(row.charge_valley || 0)
    charge.deepValley += Number(row.charge_deep_valley || 0)
    discharge.sharpPeak += Number(row.discharge_sharp_peak || 0)
    discharge.peak += Number(row.discharge_peak || 0)
    discharge.flat += Number(row.discharge_flat || 0)
    discharge.valley += Number(row.discharge_valley || 0)
    discharge.deepValley += Number(row.discharge_deep_valley || 0)
  })
  return { charge: roundTouEnergy(charge), discharge: roundTouEnergy(discharge), count: intervals.length }
}

function buildReportEnergyDetails(deviceDetails: AnyRecord[], charge: TouEnergy, discharge: TouEnergy) {
  const totalStartEpi = nullableNumberSum(deviceDetails.map((row) => row.startEpi))
  const totalEndEpi = nullableNumberSum(deviceDetails.map((row) => row.endEpi))
  const totalStartEpe = nullableNumberSum(deviceDetails.map((row) => row.startEpe))
  const totalEndEpe = nullableNumberSum(deviceDetails.map((row) => row.endEpe))
  return [
    energyDetailRow('正向有功（总）', totalStartEpi, totalEndEpi, sumTouEnergy(charge), 'EPI'),
    energyDetailRow('正向有功（尖）', null, null, charge.sharpPeak, 'EPIJ'),
    energyDetailRow('正向有功（峰）', null, null, charge.peak, 'EPIF'),
    energyDetailRow('正向有功（平）', null, null, charge.flat, 'EPIP'),
    energyDetailRow('正向有功（谷）', null, null, charge.valley, 'EPIG'),
    energyDetailRow('正向有功（深谷）', null, null, charge.deepValley, '计费时段/深谷'),
    energyDetailRow('反向有功（总）', totalStartEpe, totalEndEpe, sumTouEnergy(discharge), 'EPE'),
    energyDetailRow('反向有功（尖）', null, null, discharge.sharpPeak, 'EPEJ'),
    energyDetailRow('反向有功（峰）', null, null, discharge.peak, 'EPEF'),
    energyDetailRow('反向有功（平）', null, null, discharge.flat, 'EPEP'),
    energyDetailRow('反向有功（谷）', null, null, discharge.valley, 'EPEG'),
    energyDetailRow('反向有功（深谷）', null, null, discharge.deepValley, '计费时段/深谷')
  ]
}

function energyDetailRow(label: string, startReading: number | null, endReading: number | null, billingEnergy: number, sourceField: string) {
  return {
    label,
    startReading,
    endReading,
    multiplier: 1,
    copiedEnergy: billingEnergy,
    transformerLoss: 0,
    lineLoss: 0,
    adjustment: 0,
    billingEnergy: round4(billingEnergy),
    sourceField
  }
}

function buildReportFeeDetails(deviceDetails: AnyRecord[], rules: AnyRecord[], telemetryRows: AnyRecord[]) {
  const rows = [
    feeDetailHeader('市场化购电费'),
    ...touFeeRowsByDevice(deviceDetails, rules, '市场化购电费', '零售交易电费', 'chargeTou', (rule, key) => Number(averageTouRates(rule ? [rule] : [])[key] || 0), 1, true),
    feeDetailHeader('上网环节线损费用'),
    ...touFeeRowsByDevice(deviceDetails, rules, '上网环节线损费用', '上网环节线损费用', 'chargeTou', (rule) => numberOrNull(rule?.line_loss_price) || 0, 1, true),
    feeDetailHeader('输配电量电费'),
    ...touFeeRowsByDevice(deviceDetails, rules, '输配电量电费', '电量电费', 'chargeTou', (rule) => numberOrNull(rule?.transmission_distribution_price) || 0, 1, true),
    feeDetailHeader('系统运行费用'),
    ...touFeeRowsByDevice(deviceDetails, rules, '系统运行费用', '煤电容量电费', 'chargeTou', (rule) => numberOrNull(rule?.system_operation_fee) || 0, 1, true),
    ...touFeeRowsByDevice(deviceDetails, rules, '系统运行费用', '上网环节线损代理采购损益', 'chargeTou', () => 0, 1, true),
    ...touFeeRowsByDevice(deviceDetails, rules, '系统运行费用', '力调电费损益', 'chargeTou', () => 0, 1, true),
    ...touFeeRowsByDevice(deviceDetails, rules, '系统运行费用', '峰谷分时电价损益', 'chargeTou', () => 0, 1, true),
    ...touFeeRowsByDevice(deviceDetails, rules, '系统运行费用', '电价交叉补贴新增损益', 'chargeTou', () => 0, 1, true),
    ...touFeeRowsByDevice(deviceDetails, rules, '系统运行费用', '天然气发电容量电费（含气电联动）', 'chargeTou', () => 0, 1, true),
    ...touFeeRowsByDevice(deviceDetails, rules, '系统运行费用', '抽水蓄能容量电费', 'chargeTou', () => 0, 1, true),
    feeDetailHeader('政府性基金及附加'),
    ...touFeeRowsByDevice(deviceDetails, rules, '政府性基金及附加', '库区移民基金', 'chargeTou', (rule) => numberOrNull(rule?.government_fund_surcharge) || 0, 1, true),
    ...touFeeRowsByDevice(deviceDetails, rules, '政府性基金及附加', '可再生能源附加', 'chargeTou', () => 0, 1, true),
    ...touFeeRowsByDevice(deviceDetails, rules, '政府性基金及附加', '国家重大水利工程建设基金', 'chargeTou', () => 0, 1, true),
    feeDetailHeader('售电收入'),
    ...touFeeRowsByDevice(deviceDetails, rules, '售电收入', '放电售电收入', 'dischargeTou', (rule, key) => Number(averageTouRates(rule ? [rule] : [])[key] || 0), -1),
    demandFeeDetailRow(rules, deviceDetails, telemetryRows),
    transformerFeeDetailRow(rules)
  ].filter(Boolean)
  rows.push({ category: '合计', component: '本期电费合计', period: '', billingEnergy: null, rate: null, amount: reportBillingFeeTotal(rows), source: '汇总，不含售电收入' })
  return rows
}

function buildUnmatchedPricingDetails(deviceDetails: AnyRecord[]) {
  const periods: Array<{ key: TouKey; label: string }> = [
    { key: 'sharpPeak', label: '尖' },
    { key: 'peak', label: '峰' },
    { key: 'flat', label: '平' },
    { key: 'valley', label: '谷' },
    { key: 'deepValley', label: '深谷' }
  ]
  return deviceDetails
    .filter((detail) => !detail.pricingRuleId)
    .flatMap((detail) => periods.map((period) => {
      const chargeEnergy = Number(detail?.chargeTou?.[period.key] || 0)
      const dischargeEnergy = Number(detail?.dischargeTou?.[period.key] || 0)
      return {
        customerId: detail.customerId || null,
        customerName: detail.customerName || '',
        projectId: detail.projectId || null,
        projectName: detail.projectName || '',
        deviceId: detail.deviceId || null,
        deviceName: detail.deviceName || '',
        deviceNo: detail.deviceNo || '',
        meterNo: detail.meterNo || '',
        period: period.label,
        chargeEnergy: round4(chargeEnergy),
        dischargeEnergy: round4(dischargeEnergy),
        reason: '未匹配计费规则'
      }
    }))
    .filter((row) => Number(row.chargeEnergy || 0) > 0 || Number(row.dischargeEnergy || 0) > 0)
}

function feeDetailHeader(category: string) {
  return { category, component: '', period: '', billingEnergy: null, rate: null, amount: null, source: '分组标题' }
}

function touFeeRowsByDevice(
  deviceDetails: AnyRecord[],
  rules: AnyRecord[],
  category: string,
  component: string,
  energyField: 'chargeTou' | 'dischargeTou',
  rateGetter: (rule: AnyRecord | null, key: TouKey) => number,
  sign = 1,
  includeZeroRows = false
) {
  const periods: Array<{ key: TouKey; label: string }> = [
    { key: 'sharpPeak', label: '尖' },
    { key: 'peak', label: '峰' },
    { key: 'flat', label: '平' },
    { key: 'valley', label: '谷' },
    { key: 'deepValley', label: '深谷' }
  ]
  return periods
    .flatMap((period) => {
      let matchedEnergy = 0
      let amount = 0
      const fallbackRates: Array<number | null> = []
      deviceDetails.forEach((detail) => {
        const rule = findRuleById(rules, detail.pricingRuleId)
        const energy = Number(detail?.[energyField]?.[period.key] || 0)
        if (!rule) return
        const rate = rateGetter(rule, period.key)
        matchedEnergy += energy
        amount += energy * rate * sign
        fallbackRates.push(Number.isFinite(rate) ? rate : null)
      })
      const displayRate = matchedEnergy > 0 ? Math.abs(amount) / matchedEnergy : averageNumber(fallbackRates)
      const rows: AnyRecord[] = []
      if (includeZeroRows || matchedEnergy > 0) rows.push({
        category,
        component,
        period: period.label,
        billingEnergy: round4(matchedEnergy),
        rate: round8(displayRate),
        amount: round2(amount),
        source: '计费规则 × EIOT分时电量'
      })
      return rows
    })
    .filter((row) => includeZeroRows || Number(row.billingEnergy || 0) > 0)
}

function findRuleById(rules: AnyRecord[], id: unknown) {
  const ruleId = Number(id)
  if (!Number.isFinite(ruleId)) return null
  return rules.find((rule) => Number(rule.id) === ruleId) || null
}

function demandFeeDetailRow(rules: AnyRecord[], deviceDetails: AnyRecord[], telemetryRows: AnyRecord[]) {
  const enabled = rules.filter((rule) => rule.capacity_billing_mode === 'maxDemand')
  if (!enabled.length) return { category: '容需量费用', component: '最大需量费用', period: '', billingEnergy: 0, rate: null, amount: 0, source: '未启用' }
  let demand = 0
  let amount = 0
  enabled.forEach((rule) => {
    const deviceIds = deviceDetails
      .filter((detail) => Number(detail.pricingRuleId) === Number(rule.id))
      .map((detail) => Number(detail.deviceId))
      .filter(Number.isFinite)
    const rows = telemetryRows.filter((row) => deviceIds.includes(Number(row.device_id)))
    const ruleDemand = round4(maxDemandFromTelemetry(rows))
    const price = numberOrNull(rule.max_demand_price) || 0
    demand += ruleDemand
    amount += ruleDemand * price
  })
  const rate = demand > 0 ? amount / demand : averageNumber(enabled.map((rule) => numberOrNull(rule.max_demand_price)))
  return { category: '容需量费用', component: '最大需量费用', period: '', billingEnergy: demand, rate, amount: round2(amount), source: '遥测P按15分钟窗口聚合最大需量 × 单价' }
}

function transformerFeeDetailRow(rules: AnyRecord[]) {
  const enabled = rules.filter((rule) => rule.capacity_billing_mode === 'transformerCapacity')
  if (!enabled.length) return { category: '容需量费用', component: '变压器容量费用', period: '', billingEnergy: 0, rate: null, amount: 0, source: '未启用' }
  const capacity = round4(enabled.reduce((sum, rule) => sum + Number(rule.transformer_capacity_kva || 0), 0))
  const amount = enabled.reduce((sum, rule) => sum + Number(rule.transformer_capacity_kva || 0) * Number(rule.transformer_capacity_price || 0), 0)
  const rate = capacity > 0 ? amount / capacity : averageNumber(enabled.map((rule) => numberOrNull(rule.transformer_capacity_price)))
  return { category: '容需量费用', component: '变压器容量费用', period: '', billingEnergy: capacity, rate, amount: round2(amount), source: '容量 × 单价' }
}

function averageTouRates(rules: AnyRecord[]): TouEnergy {
  return {
    sharpPeak: averageNumber(rules.map((rule) => numberOrNull(rule.sharp_peak_rate))),
    peak: averageNumber(rules.map((rule) => numberOrNull(rule.peak_rate))),
    flat: averageNumber(rules.map((rule) => numberOrNull(rule.flat_rate))),
    valley: averageNumber(rules.map((rule) => numberOrNull(rule.valley_rate))),
    deepValley: averageNumber(rules.map((rule) => numberOrNull(rule.deep_valley_rate)))
  }
}

function sumByTou(energy: TouEnergy, rates: TouEnergy) {
  return round2((Object.keys(energy) as TouKey[]).reduce((sum, key) => sum + Number(energy[key] || 0) * Number(rates[key] || 0), 0))
}

function reportBillingFeeTotal(rows: AnyRecord[]) {
  return round2(rows.reduce((sum, row) => {
    if (!row || row.category === '售电收入' || row.category === '合计') return sum
    return sum + Number(row.amount || 0)
  }, 0))
}

function buildReportHeader(devices: AnyRecord[], rules: AnyRecord[], billMonth: string, start: string, end: string, scopeType: string) {
  const device = devices[0] || {}
  const rule = rules[0] || {}
  return {
    billStartDate: start.slice(0, 10),
    billEndDate: end.slice(0, 10),
    accountNo: '待录入',
    customerName: device.customer_name || rule.customer_name || '待录入',
    usageAddress: scopeType === 'project' ? device.project_name || '待录入' : device.project_name || '待录入',
    electricityCategory: rule.electricity_category || '待录入',
    voltageLevel: rule.voltage_level || '待录入',
    serviceUnit: '待录入',
    marketType: scopeType === 'device' ? '单表统计' : scopeType === 'project' ? '场站汇总' : '全部电表汇总',
    printer: 'system',
    printDate: nowText().slice(0, 10),
    reportNo: `ES-${billMonth.replace('-', '')}-${Date.now()}`
  }
}

function emptyReportBill(billMonth: string, start: string, end: string, scopeType: string) {
  return {
    billMonth,
    billRange: { start, end },
    scopeType,
    scopeName: '暂无电表',
    billHeader: buildReportHeader([], [], billMonth, start, end, scopeType),
    summary: { deviceCount: 0, totalChargeEnergy: 0, totalDischargeEnergy: 0, totalFee: 0, chargeCost: 0, averageBuyRate: null, salesRevenue: 0, averageSellRate: null, savedCost: 0, touSource: 'empty' },
    deviceDetails: [],
    energyDetails: [],
    feeDetails: [],
    analysis: { chargeTou: emptyTouEnergy(), dischargeTou: emptyTouEnergy(), chargeConsistency: '无数据', dischargeConsistency: '无数据' }
  }
}

function detectEiotPushType(payload: unknown) {
  if (Array.isArray(payload)) return 'meter'
  if (payload && typeof payload === 'object' && Array.isArray((payload as AnyRecord).list)) return 'alarm'
  return 'unknown'
}

function getEiotPayloadSummary(payload: unknown) {
  const first = Array.isArray(payload) ? payload[0] : payload
  const row = first && typeof first === 'object' ? (first as AnyRecord) : {}
  return {
    gatewaySn: cleanText(row.gatewaySn),
    meterSn: cleanText(row.meterSn),
    meterNo: cleanText(row.meterNo),
    timestamp: Number(row.timestamp || 0)
  }
}

async function archiveEiotPayload(env: Env, type: string, rawBody: string, summary: AnyRecord, receivedAt: number) {
  const timestamp = Number(summary.timestamp || 0) || Math.floor(receivedAt / 1000)
  const gatewaySn = sanitizeKeyPart(summary.gatewaySn || 'unknown-gateway')
  const key = `eiot/${type}/${timestamp}-${gatewaySn}-${crypto.randomUUID()}.json`
  await env.BUCKET.put(key, rawBody || '', {
    httpMetadata: { contentType: 'application/json; charset=utf-8' }
  })
  return `/admin-api/infra/file/get/${encodeURIComponent(key)}`
}

async function ensureEiotReceiveColumns(env: Env) {
  await env.DB.prepare(
    `CREATE TABLE IF NOT EXISTS energy_telemetry_interval (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      telemetry_id INTEGER UNIQUE,
      device_id INTEGER,
      gateway_sn TEXT,
      meter_sn TEXT,
      meter_no TEXT,
      start_time TEXT,
      end_time TEXT,
      duration_minutes REAL,
      charge_total REAL NOT NULL DEFAULT 0,
      charge_sharp_peak REAL NOT NULL DEFAULT 0,
      charge_peak REAL NOT NULL DEFAULT 0,
      charge_flat REAL NOT NULL DEFAULT 0,
      charge_valley REAL NOT NULL DEFAULT 0,
      charge_deep_valley REAL NOT NULL DEFAULT 0,
      discharge_total REAL NOT NULL DEFAULT 0,
      discharge_sharp_peak REAL NOT NULL DEFAULT 0,
      discharge_peak REAL NOT NULL DEFAULT 0,
      discharge_flat REAL NOT NULL DEFAULT 0,
      discharge_valley REAL NOT NULL DEFAULT 0,
      discharge_deep_valley REAL NOT NULL DEFAULT 0,
      calc_method TEXT,
      consistency_status TEXT,
      create_time TEXT
    )`
  ).run()
  await ensureColumns(env, 'energy_telemetry', {
    epij: 'REAL',
    epif: 'REAL',
    epip: 'REAL',
    epig: 'REAL',
    epe: 'REAL',
    epej: 'REAL',
    epef: 'REAL',
    epep: 'REAL',
    epeg: 'REAL',
    data_json: 'TEXT',
    payload_url: 'TEXT'
  })
  await ensureColumns(env, 'energy_alarm', {
    gateway_sn: 'TEXT',
    meter_sn: 'TEXT',
    meter_no: 'TEXT',
    timestamp: 'INTEGER',
    data_json: 'TEXT',
    payload_url: 'TEXT'
  })
  await env.DB.prepare('CREATE INDEX IF NOT EXISTS idx_energy_telemetry_gateway_meter_time ON energy_telemetry(gateway_sn, meter_sn, collect_time)').run()
  await env.DB.prepare('CREATE INDEX IF NOT EXISTS idx_energy_device_status_reading ON energy_device(status, last_reading_time)').run()
  await env.DB.prepare('CREATE INDEX IF NOT EXISTS idx_energy_alarm_gateway_meter_time ON energy_alarm(gateway_sn, meter_sn, occur_time)').run()
  await env.DB.prepare('CREATE INDEX IF NOT EXISTS idx_energy_interval_device_time ON energy_telemetry_interval(device_id, end_time)').run()
  await env.DB.prepare('CREATE INDEX IF NOT EXISTS idx_energy_interval_meter_time ON energy_telemetry_interval(meter_no, end_time)').run()
}

async function ensureColumns(env: Env, table: string, columns: Record<string, string>) {
  const info = await env.DB.prepare(`PRAGMA table_info(${table})`).all<AnyRecord>()
  const existing = new Set((info.results || []).map((row) => String(row.name)))
  for (const [column, definition] of Object.entries(columns)) {
    if (existing.has(column)) continue
    try {
      await env.DB.prepare(`ALTER TABLE ${table} ADD COLUMN ${column} ${definition}`).run()
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error)
      if (!message.toLowerCase().includes('duplicate column')) throw error
    }
  }
}

async function saveEiotMeterPayload(env: Env, payload: unknown, payloadUrl: string) {
  const rows = Array.isArray(payload) ? payload : payload && typeof payload === 'object' ? [payload] : []
  if (!rows.length) throw new Error('Meter payload must be a non-empty array or object')

  for (const item of rows) {
    const row = item && typeof item === 'object' ? (item as AnyRecord) : {}
    const gatewaySn = cleanText(row.gatewaySn)
    const meterSn = cleanText(row.meterSn)
    const meterNo = resolveMeterNo(row.meterNo, gatewaySn, meterSn)
    const timestamp = numberOrNull(row.timestamp)
    const collectTime = normalizeEiotCollectTime(row.createTime || row.CreateTime, timestamp)
    const device = await findOrCreateDeviceByEiot(env, { ...row, gatewaySn, meterSn, meterNo }, collectTime)
    const deviceId = device?.id ? Number(device.id) : null

    const createdTelemetry = await env.DB.prepare(
      `INSERT INTO energy_telemetry(
        device_id, gateway_sn, meter_sn, meter_no, collect_time, timestamp, source, state,
        pa, pb, pc, ua, ub, uc, ia, ib, ic, p, pf, epi,
        epij, epif, epip, epig, epe, epej, epef, epep, epeg,
        data_json, payload_url, create_time
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      RETURNING id`
    )
      .bind(
        deviceId,
        gatewaySn || null,
        meterSn || null,
        meterNo || null,
        collectTime || nowText(),
        timestamp,
        cleanText(row.source) || null,
        cleanText(row.state) || null,
        numberOrNull(row.Pa),
        numberOrNull(row.Pb),
        numberOrNull(row.Pc),
        numberOrNull(row.Ua),
        numberOrNull(row.Ub),
        numberOrNull(row.Uc),
        numberOrNull(row.Ia),
        numberOrNull(row.Ib),
        numberOrNull(row.Ic),
        numberOrNull(row.P),
        numberOrNull(row.PF),
        numberOrNull(row.EPI),
        numberOrNull(row.EPIJ),
        numberOrNull(row.EPIF),
        numberOrNull(row.EPIP),
        numberOrNull(row.EPIG),
        numberOrNull(row.EPE),
        numberOrNull(row.EPEJ),
        numberOrNull(row.EPEF),
        numberOrNull(row.EPEP),
        numberOrNull(row.EPEG),
        JSON.stringify(row),
        payloadUrl,
        nowText()
      )
      .first<AnyRecord>()

    if (deviceId) {
      await updateDeviceLatestFromTelemetry(env, deviceId, row, collectTime)
      if (createdTelemetry?.id) {
        await rebuildTelemetryIntervalsAround(env, deviceId, Number(createdTelemetry.id))
      }
    }
  }
}

async function saveEiotAlarmPayload(env: Env, payload: unknown, payloadUrl: string) {
  const body = payload && typeof payload === 'object' ? (payload as AnyRecord) : {}
  const list = Array.isArray(body.list) ? body.list : []
  if (!list.length) throw new Error('Alarm payload must contain list[]')

  const gatewaySn = cleanText(body.gatewaySn)
  const meterSn = cleanText(body.meterSn)
  const meterNo = resolveMeterNo(body.meterNo, gatewaySn, meterSn)
  const timestamp = numberOrNull(body.timestamp)
  const occurTime = normalizeEiotCollectTime(body.createTime || body.CreateTime, timestamp)
  const device = await findOrCreateDeviceByEiot(env, { ...body, gatewaySn, meterSn, meterNo }, occurTime)
  const deviceId = device?.id ? Number(device.id) : null

  for (const item of list) {
    const alarm = item && typeof item === 'object' ? (item as AnyRecord) : {}
    await env.DB.prepare(
      `INSERT INTO energy_alarm(
        alarm_no, device_id, code, level, title, content, status, occur_time,
        gateway_sn, meter_sn, meter_no, timestamp, data_json, payload_url, create_time
      ) VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?, ?)`
    )
      .bind(
        cleanText(alarm.alarmNo) || `AL${Date.now()}${Math.floor(Math.random() * 1000)}`,
        deviceId,
        cleanText(alarm.code) || null,
        numberOrNull(alarm.level),
        localizedText(alarm.title),
        localizedText(alarm.message),
        occurTime,
        gatewaySn || null,
        meterSn || null,
        meterNo || null,
        timestamp,
        JSON.stringify({ ...body, list: [alarm] }),
        payloadUrl,
        nowText()
      )
      .run()
  }
}

async function findDeviceByMeter(env: Env, meterNo: string, gatewaySn: string, meterSn: string) {
  if (!meterNo && (!gatewaySn || !meterSn)) return null
  return env.DB.prepare(
    `SELECT id, status FROM energy_device
     WHERE (? <> '' AND meter_no = ?)
        OR (? <> '' AND ? <> '' AND gateway_sn = ? AND meter_sn = ?)
     ORDER BY CASE WHEN meter_no = ? THEN 0 ELSE 1 END
     LIMIT 1`
  )
    .bind(meterNo, meterNo, gatewaySn, meterSn, gatewaySn, meterSn, meterNo)
    .first<AnyRecord>()
}

async function findPreviousTelemetry(env: Env, deviceId: number, collectTime: string) {
  return env.DB.prepare(
    `SELECT * FROM energy_telemetry
     WHERE device_id = ? AND collect_time < ?
     ORDER BY collect_time DESC, id DESC
     LIMIT 1`
  )
    .bind(deviceId, collectTime)
    .first<AnyRecord>()
}

async function findTelemetryById(env: Env, telemetryId: number) {
  return env.DB.prepare('SELECT * FROM energy_telemetry WHERE id = ?')
    .bind(telemetryId)
    .first<AnyRecord>()
}

async function findNextTelemetry(env: Env, deviceId: number, collectTime: string) {
  return env.DB.prepare(
    `SELECT * FROM energy_telemetry
     WHERE device_id = ? AND collect_time > ?
     ORDER BY collect_time ASC, id ASC
     LIMIT 1`
  )
    .bind(deviceId, collectTime)
    .first<AnyRecord>()
}

async function rebuildTelemetryIntervalsAround(env: Env, deviceId: number, telemetryId: number) {
  const current = await findTelemetryById(env, telemetryId)
  const collectTime = cleanText(current?.collect_time)
  if (!current || !collectTime) return

  const previous = await findPreviousTelemetry(env, deviceId, collectTime)
  const next = await findNextTelemetry(env, deviceId, collectTime)

  await env.DB.prepare('DELETE FROM energy_telemetry_interval WHERE telemetry_id = ?')
    .bind(telemetryId)
    .run()
  if (next?.id) {
    await env.DB.prepare('DELETE FROM energy_telemetry_interval WHERE telemetry_id = ?')
      .bind(next.id)
      .run()
  }

  if (previous) {
    await saveTelemetryInterval(env, previous, current, telemetryId)
  }
  if (next?.id) {
    await saveTelemetryInterval(env, current, next, Number(next.id))
  }
}

async function saveTelemetryInterval(env: Env, previous: AnyRecord, current: AnyRecord, telemetryId: number) {
  const deviceId = Number(current.deviceId || current.device_id || previous.device_id || 0)
  const startTime = cleanText(previous.collect_time || previous.collectTime)
  const endTime = cleanText(current.collectTime || current.collect_time)
  if (!deviceId || !startTime || !endTime) return

  const chargeTotal = positiveEnergyDelta(previous, current, 'epi')
  const dischargeTotal = positiveEnergyDelta(previous, current, 'epe')
  if (chargeTotal <= 0 && dischargeTotal <= 0) return

  const charge = await calculateIntervalTou(env, previous, current, 'epi', chargeTotal, deviceId, startTime, endTime)
  const discharge = await calculateIntervalTou(env, previous, current, 'epe', dischargeTotal, deviceId, startTime, endTime)

  await env.DB.prepare(
    `INSERT OR REPLACE INTO energy_telemetry_interval(
      telemetry_id, device_id, gateway_sn, meter_sn, meter_no, start_time, end_time, duration_minutes,
      charge_total, charge_sharp_peak, charge_peak, charge_flat, charge_valley, charge_deep_valley,
      discharge_total, discharge_sharp_peak, discharge_peak, discharge_flat, discharge_valley, discharge_deep_valley,
      calc_method, consistency_status, create_time
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`
  )
      .bind(
      telemetryId,
      deviceId,
      cleanText(current.gatewaySn || current.gateway_sn || previous.gateway_sn) || null,
      cleanText(current.meterSn || current.meter_sn || previous.meter_sn) || null,
      cleanText(current.meterNo || current.meter_no || previous.meter_no) || null,
      startTime,
      endTime,
      intervalMinutes(startTime, endTime),
      chargeTotal,
      charge.energy.sharpPeak,
      charge.energy.peak,
      charge.energy.flat,
      charge.energy.valley,
      charge.energy.deepValley,
      dischargeTotal,
      discharge.energy.sharpPeak,
      discharge.energy.peak,
      discharge.energy.flat,
      discharge.energy.valley,
      discharge.energy.deepValley,
      `charge:${charge.method};discharge:${discharge.method}`,
      `charge:${charge.status};discharge:${discharge.status}`,
      nowText()
    )
    .run()
}

async function calculateIntervalTou(
  env: Env,
  previous: AnyRecord,
  current: AnyRecord,
  source: TouSource,
  total: number,
  deviceId: number,
  startTime: string,
  endTime: string
) {
  if (total <= 0) return { energy: emptyTouEnergy(), method: 'none', status: 'none' }
  const payload = payloadTouDelta(previous, current, source, total)
  if (payload) return { energy: payload, method: 'payload', status: 'consistent' }
  const rule = await findPricingRuleForDevice(env, deviceId, endTime)
  return {
    energy: splitEnergyByPricingTime(startTime, endTime, total, parseTouPeriods(rule?.tou_periods)),
    method: 'pricing-rule',
    status: 'fallback'
  }
}

function payloadTouDelta(previous: AnyRecord, current: AnyRecord, source: TouSource, total: number) {
  const fields = source === 'epi'
    ? { sharpPeak: 'epij', peak: 'epif', flat: 'epip', valley: 'epig', deepValley: '' }
    : { sharpPeak: 'epej', peak: 'epef', flat: 'epep', valley: 'epeg', deepValley: '' }
  const energy = emptyTouEnergy()
  let hasPart = false
  for (const key of Object.keys(energy) as TouKey[]) {
    const field = fields[key]
    if (!field) continue
    const delta = positiveEnergyDelta(previous, current, field)
    energy[key] = delta
    if (delta > 0) hasPart = true
  }
  if (!hasPart) return null
  const partTotal = sumTouEnergy(energy)
  const tolerance = Math.max(0.02, total * 0.02)
  return Math.abs(partTotal - total) <= tolerance ? energy : null
}

async function findPricingRuleForDevice(env: Env, deviceId: number, billingTime: string) {
  const device = await env.DB.prepare(
    `SELECT d.id, COALESCE(d.customer_id, p.customer_id) AS customer_id, d.project_id
     FROM energy_device d
     LEFT JOIN energy_project p ON p.id = d.project_id
     WHERE d.id = ?`
  )
    .bind(deviceId)
    .first<AnyRecord>()
  if (!device) return null
  return env.DB.prepare(
    `SELECT r.*
     FROM energy_pricing_rule r
     WHERE r.status = 0
       AND (? IS NULL OR r.effective_start IS NULL OR r.effective_start <= ?)
        AND (? IS NULL OR r.effective_end IS NULL OR ${pricingRuleEffectiveEndSql()} >= ?)
       AND (
         r.device_id = ?
         OR (r.device_id IS NULL AND r.project_id = ?)
         OR (r.device_id IS NULL AND r.project_id IS NULL AND r.customer_id = ?)
       )
     ORDER BY
       CASE
         WHEN r.device_id = ? THEN 3
         WHEN r.device_id IS NULL AND r.project_id = ? THEN 2
         WHEN r.device_id IS NULL AND r.project_id IS NULL AND r.customer_id = ? THEN 1
         ELSE 0
       END DESC,
       r.effective_start DESC,
       r.id DESC
     LIMIT 1`
  )
    .bind(billingTime, billingTime, billingTime, billingTime, deviceId, device.project_id || null, device.customer_id || null, deviceId, device.project_id || null, device.customer_id || null)
    .first<AnyRecord>()
}

async function findOrCreateDeviceByEiot(env: Env, row: AnyRecord, collectTime: string) {
  const gatewaySn = cleanText(row.gatewaySn)
  const meterSn = cleanText(row.meterSn)
  const meterNo = resolveMeterNo(row.meterNo, gatewaySn, meterSn)
  const existing = await findDeviceByMeter(env, meterNo, gatewaySn, meterSn)
  if (existing) return existing
  if (!meterNo && (!gatewaySn || !meterSn)) return null

  const state = cleanText(row.state)
  const status = state === 'ONLINE' ? 0 : state === 'OFFLINE' ? 1 : 0
  const voltage = averageNumbers([row.Ua, row.Ub, row.Uc])
  const current = averageNumbers([row.Ia, row.Ib, row.Ic])
  const label = meterNo || meterSn || gatewaySn
  const deviceNo = await buildAutoDeviceNo(label)
  const remark = 'EIOT推送首次接收时自动生成，客户、场地等信息待人工完善'

  try {
    const created = await env.DB.prepare(
      `INSERT INTO energy_device(
        device_no, device_name, device_type, gateway_sn, meter_sn, meter_no, status,
        last_soc, last_power, last_voltage, last_current, last_reading_time, remark, create_time, update_time
      ) VALUES (?, ?, 2, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      RETURNING id, status`
    )
      .bind(
        deviceNo,
        `EIOT电表-${label}`,
        gatewaySn || null,
        meterSn || null,
        meterNo || null,
        status,
        numberOrNull(row.SOC ?? row.soc),
        numberOrNull(row.P),
        voltage,
        current,
        collectTime,
        remark,
        nowText(),
        nowText()
      )
      .first<AnyRecord>()
    return created
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error)
    if (!message.toLowerCase().includes('unique')) throw error
    return findDeviceByMeter(env, meterNo, gatewaySn, meterSn)
  }
}

async function refreshDeviceOnlineStatus(env: Env, deviceIds: number[]) {
  const ids = Array.from(new Set(deviceIds.filter(Number.isFinite)))
  if (!ids.length) return
  const cutoff = deviceOnlineCutoffText()
  const placeholders = ids.map(() => '?').join(',')
  await env.DB.prepare(
    `UPDATE energy_device
     SET status = CASE
       WHEN last_reading_time IS NOT NULL AND last_reading_time >= ? THEN 0
       ELSE 1
     END,
     update_time = ?
     WHERE id IN (${placeholders}) AND status IN (0, 1)`
  )
    .bind(cutoff, nowText(), ...ids)
    .run()
}

async function refreshStaleOnlineDevices(env: Env, accessScope: AccessScope) {
  const where = ['status = 0', '(last_reading_time IS NULL OR last_reading_time < ?)']
  const args: any[] = [deviceOnlineCutoffText()]
  applyCustomerScope(where, args, 'COALESCE(customer_id, (SELECT customer_id FROM energy_project WHERE id = energy_device.project_id))', accessScope)
  await env.DB.prepare(
    `UPDATE energy_device
     SET status = 1, update_time = ?
     WHERE ${where.join(' AND ')}`
  )
    .bind(nowText(), ...args)
    .run()
}

function applyDeviceOnlineStatus(rows: AnyRecord[]) {
  const cutoff = deviceOnlineCutoffText()
  return rows.map((row) => {
    if (Number(row.status) !== 0 && Number(row.status) !== 1) return row
    return {
      ...row,
      status: cleanText(row.last_reading_time) && cleanText(row.last_reading_time) >= cutoff ? 0 : 1
    }
  })
}

async function updateDeviceLatestFromTelemetry(env: Env, deviceId: number, row: AnyRecord, collectTime: string) {
  const state = cleanText(row.state)
  const status = state === 'ONLINE' ? 0 : state === 'OFFLINE' ? 1 : null
  const voltage = averageNumbers([row.Ua, row.Ub, row.Uc])
  const current = averageNumbers([row.Ia, row.Ib, row.Ic])
  await env.DB.prepare(
    `UPDATE energy_device
      SET status = COALESCE(?, status),
          last_soc = COALESCE(?, last_soc),
          last_power = COALESCE(?, last_power),
          last_voltage = COALESCE(?, last_voltage),
          last_current = COALESCE(?, last_current),
          last_reading_time = ?,
          update_time = ?
      WHERE id = ? AND (last_reading_time IS NULL OR last_reading_time <= ?)`
  )
    .bind(status, numberOrNull(row.SOC ?? row.soc), numberOrNull(row.P), voltage, current, collectTime, nowText(), deviceId, collectTime)
    .run()
}

async function writeEiotSyncLog(env: Env, log: AnyRecord) {
  try {
    await env.DB.prepare(
      `INSERT INTO energy_eiot_sync_log(sync_type, request_id, gateway_sn, meter_sn, payload_url, status, error_msg, create_time)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`
    )
      .bind(
        log.syncType || 'unknown',
        log.requestId || crypto.randomUUID(),
        log.gatewaySn || null,
        log.meterSn || null,
        log.payloadUrl || null,
        Number(log.status || 0),
        log.errorMsg || '',
        nowText()
      )
      .run()
  } catch (error) {
    console.error('EIOT sync log failed', error)
  }
}

async function crud(request: Request, url: URL, env: Env, table: string, fields: string[]) {
  const action = url.pathname.split('/').pop()
  if (action === 'page') return page(url, env, table)
  if (action === 'simple-list') return simpleList(env, table)
  if (action === 'get') return getById(url, env, table)
  if (action === 'create' && request.method === 'POST') return createRow(request, env, table, fields)
  if (action === 'update' && request.method === 'PUT') return updateRow(request, env, table, fields)
  if (action === 'delete' && request.method === 'DELETE') return deleteRow(url, env, table)
  return fail('接口方法不匹配', 405, 405)
}

async function page(url: URL, env: Env, table: string) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const total = await scalar(env, `SELECT COUNT(*) FROM ${table}`, [])
  const rows = await env.DB.prepare(`SELECT * FROM ${table} ORDER BY id DESC LIMIT ? OFFSET ?`)
    .bind(pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()
  return ok({ list: camelRows(rows.results), total })
}

async function simpleList(env: Env, table: string) {
  const rows = await env.DB.prepare(`SELECT * FROM ${table} ORDER BY id DESC LIMIT 200`).all<AnyRecord>()
  return ok(camelRows(rows.results))
}

async function getById(url: URL, env: Env, table: string) {
  const row = await env.DB.prepare(`SELECT * FROM ${table} WHERE id = ?`).bind(url.searchParams.get('id')).first<AnyRecord>()
  return ok(row ? camel(row) : null)
}

async function createRow(request: Request, env: Env, table: string, fields: string[]) {
  return createRowFromBody(await readJson(request), env, table, fields)
}

async function createRowFromBody(body: AnyRecord, env: Env, table: string, fields: string[]) {
  const dbFields = fields.map(snake).filter((field) => body[camelKey(field)] !== undefined)
  const values = dbFields.map((field) => body[camelKey(field)])
  const sql = `INSERT INTO ${table}(${dbFields.join(',')}, create_time) VALUES (${dbFields.map(() => '?').join(',')}, ?) RETURNING id`
  const row = await env.DB.prepare(sql).bind(...values, nowText()).first<AnyRecord>()
  return ok(Number(row?.id))
}

async function updateRow(request: Request, env: Env, table: string, fields: string[]) {
  return updateRowFromBody(await readJson(request), env, table, fields)
}

async function updateRowFromBody(body: AnyRecord, env: Env, table: string, fields: string[]) {
  const dbFields = fields.map(snake).filter((field) => body[camelKey(field)] !== undefined)
  const values = dbFields.map((field) => body[camelKey(field)])
  const sql = `UPDATE ${table} SET ${dbFields.map((field) => `${field} = ?`).join(', ')}, update_time = ? WHERE id = ?`
  await env.DB.prepare(sql).bind(...values, nowText(), body.id).run()
  return ok(true)
}

async function validateDeviceUnique(env: Env, body: AnyRecord, id?: number) {
  const currentId = Number.isFinite(id) && id ? id : null
  const deviceNo = cleanText(body.deviceNo)
  const meterNo = cleanText(body.meterNo)
  if (deviceNo) {
    body.deviceNo = deviceNo
    const row = await env.DB.prepare('SELECT id FROM energy_device WHERE device_no = ? AND (? IS NULL OR id <> ?) LIMIT 1')
      .bind(deviceNo, currentId, currentId)
      .first<AnyRecord>()
    if (row) return fail('设备编码已存在，请更换后再保存', 400)
  }
  if (meterNo) {
    body.meterNo = meterNo
    const row = await env.DB.prepare('SELECT id FROM energy_device WHERE meter_no = ? AND (? IS NULL OR id <> ?) LIMIT 1')
      .bind(meterNo, currentId, currentId)
      .first<AnyRecord>()
    if (row) return fail('仪表编号已存在，请更换后再保存', 400)
  }
  return null
}

async function deleteRow(url: URL, env: Env, table: string) {
  await env.DB.prepare(`DELETE FROM ${table} WHERE id = ?`).bind(url.searchParams.get('id')).run()
  return ok(true)
}

async function updateStatus(request: Request, env: Env, table: string, status: number, timeField: string) {
  const body = await readJson(request)
  await env.DB.prepare(`UPDATE ${table} SET status = ?, ${timeField} = ? WHERE id = ?`).bind(status, nowText(), body.id).run()
  return ok(true)
}

async function uploadFile(request: Request, env: Env, ctx: ExecutionContext) {
  const form = await request.formData()
  const file = form.get('file')
  if (!(file instanceof File)) return fail('没有接收到文件', 400)
  const key = `${new Date().toISOString().slice(0, 10)}/${crypto.randomUUID()}-${file.name}`
  ctx.waitUntil(env.BUCKET.put(key, file.stream(), { httpMetadata: { contentType: file.type || 'application/octet-stream' } }))
  return ok({ name: file.name, url: `/admin-api/infra/file/get/${encodeURIComponent(key)}`, path: key })
}

async function getFile(path: string, env: Env) {
  const key = decodeURIComponent(path.replace('/infra/file/get/', ''))
  const object = await env.BUCKET.get(key)
  if (!object) return new Response('Not found', { status: 404 })
  const headers = new Headers()
  object.writeHttpMetadata(headers)
  headers.set('etag', object.httpEtag)
  return new Response(object.body, { headers })
}

async function requireUser(request: Request, env: Env) {
  const accessToken = bearer(request)
  if (!accessToken) return null
  const session = await env.DB.prepare('SELECT * FROM system_session WHERE access_token = ? AND expires_time > ?')
    .bind(accessToken, Date.now())
    .first<AnyRecord>()
  if (!session) return null
  return env.DB.prepare('SELECT * FROM system_user WHERE id = ? AND status = 0').bind(session.user_id).first<AnyRecord>()
}

async function getAccessScope(env: Env, user: AnyRecord): Promise<AccessScope> {
  const account = await env.DB.prepare(
    'SELECT customer_id FROM energy_customer_account WHERE system_user_id = ? AND status = 0 LIMIT 1'
  )
    .bind(user.id)
    .first<AnyRecord>()
  if (!account) return { isCustomerAccount: false, customerId: null }
  return {
    isCustomerAccount: true,
    customerId: account.customer_id === null || account.customer_id === undefined ? null : Number(account.customer_id)
  }
}

function applyCustomerScope(where: string[], args: any[], column: string, accessScope: AccessScope) {
  if (!accessScope.isCustomerAccount) return
  if (!accessScope.customerId) {
    where.push('1 = 0')
    return
  }
  where.push(`${column} = ?`)
  args.push(accessScope.customerId)
}

function vehicleCustomerColumn() {
  return 'COALESCE(v.customer_id, p.customer_id, d.customer_id, dp.customer_id)'
}

function vehicleScopeJoins() {
  return `
     LEFT JOIN energy_device d ON d.id = v.device_id
     LEFT JOIN energy_project p ON p.id = v.project_id
     LEFT JOIN energy_project dp ON dp.id = d.project_id
     LEFT JOIN energy_customer c ON c.id = ${vehicleCustomerColumn()}`
}

function eventCustomerColumn() {
  return 'COALESCE(e.customer_id, p.customer_id, d.customer_id, dp.customer_id)'
}

function eventScopeJoins() {
  return `
     LEFT JOIN energy_device d ON d.id = e.device_id
     LEFT JOIN energy_project p ON p.id = e.project_id
     LEFT JOIN energy_project dp ON dp.id = d.project_id
     LEFT JOIN energy_customer c ON c.id = ${eventCustomerColumn()}`
}

function userScopeCustomerColumn() {
  return 'COALESCE(s.customer_id, p.customer_id, d.customer_id, dp.customer_id)'
}

function userScopeJoins() {
  return `
     LEFT JOIN energy_device d ON d.id = s.device_id
     LEFT JOIN energy_project p ON p.id = s.project_id
     LEFT JOIN energy_project dp ON dp.id = d.project_id
     LEFT JOIN energy_customer c ON c.id = ${userScopeCustomerColumn()}`
}

function pricingRuleCustomerColumn() {
  return 'COALESCE(r.customer_id, p.customer_id, d.customer_id, dp.customer_id)'
}

function pricingRuleEffectiveEndSql() {
  return `CASE
    WHEN length(r.effective_end) = 10 THEN r.effective_end || ' 23:59:59'
    WHEN substr(r.effective_end, 12, 8) = '00:00:00' THEN substr(r.effective_end, 1, 10) || ' 23:59:59'
    ELSE r.effective_end
  END`
}

function chargeSessionCustomerColumn() {
  return 'COALESCE(s.customer_id, d.customer_id, p.customer_id)'
}

function chargeSessionJoins() {
  return `
     LEFT JOIN energy_device d ON d.id = s.device_id
     LEFT JOIN energy_project p ON p.id = d.project_id
     LEFT JOIN energy_customer c ON c.id = ${chargeSessionCustomerColumn()}`
}

function menu(id: number, parentId: number, name: string, path: string, component: string, componentName: string, icon: string, sort: number) {
  return { id, parentId, name, path, component, componentName, icon, visible: true, keepAlive: true, alwaysShow: false, redirect: '', sort }
}

function buildMenuTree(rows: AnyRecord[], parentId = 0): AnyRecord[] {
  return rows
    .filter((row) => Number(row.parentId) === parentId)
    .sort((a, b) => Number(a.sort) - Number(b.sort))
    .map((row) => {
      const children = buildMenuTree(rows, Number(row.id))
      return children.length > 0 ? { ...row, children } : { ...row }
    })
}

function permissionsForMenus(menuIds: number[]) {
  const permissions = ['energy:telemetry:query']
  if (menuIds.includes(1014)) permissions.push('energy:customer-account:create', 'energy:customer-account:update', 'energy:customer-account:reset-password')
  if (menuIds.includes(1003)) permissions.push('energy:device:create', 'energy:device:update', 'energy:device:delete', 'energy:device:control')
  return permissions
}

function menuOption(item: AnyRecord) {
  return { id: item.id, name: item.name, permission: '', parentId: item.parentId, sort: item.sort }
}

async function replaceAccountMenus(env: Env, accountId: number, menuIds: number[]) {
  await env.DB.prepare('DELETE FROM energy_customer_account_menu WHERE account_id = ?').bind(accountId).run()
  const finalIds = Array.from(new Set(menuIds.map(Number)))
  for (const menuId of finalIds) {
    await env.DB.prepare('INSERT INTO energy_customer_account_menu(account_id, menu_id) VALUES (?, ?)').bind(accountId, menuId).run()
  }
}

function bearer(request: Request) {
  const value = request.headers.get('authorization') || ''
  return value.startsWith('Bearer ') ? value.slice(7) : ''
}

async function readJson(request: Request) {
  return (await request.json().catch(() => ({}))) as AnyRecord
}

async function hashPassword(password: string) {
  return `sha256:${await sha256Hex(password)}`
}

async function verifyPassword(password: string, stored: string) {
  if (stored.startsWith('sha256:')) return (await hashPassword(password)) === stored
  return password === stored
}

function validatePlainPassword(value: unknown, required = false) {
  const password = String(value || '')
  if (!password) return required ? '密码不能为空' : ''
  if (password.length < 8 || password.length > 32) return '密码长度必须为8-32位'
  if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) return '密码必须同时包含字母和数字'
  if (/^\d+$/.test(password) || /^[A-Za-z]+$/.test(password)) return '密码不能为纯数字或纯字母'
  return ''
}

async function sha256Hex(value: string) {
  const data = new TextEncoder().encode(value)
  const hash = await crypto.subtle.digest('SHA-256', data)
  return [...new Uint8Array(hash)].map((item) => item.toString(16).padStart(2, '0')).join('')
}

async function buildAutoDeviceNo(seed: string) {
  return `AUTO-${(await sha256Hex(seed)).slice(0, 12).toUpperCase()}`
}

async function token(prefix: string) {
  const bytes = new Uint8Array(24)
  crypto.getRandomValues(bytes)
  return `${prefix}_${btoa(String.fromCharCode(...bytes)).replaceAll('+', '-').replaceAll('/', '_').replaceAll('=', '')}`
}

function ok(data: unknown) {
  return json({ code: 0, data, msg: '' })
}

function fail(msg: string, code = 500, status = 200) {
  return json({ code, data: null, msg }, status)
}

function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { 'content-type': 'application/json; charset=utf-8' }
  })
}

function cors(response: Response) {
  const headers = new Headers(response.headers)
  headers.set('access-control-allow-origin', '*')
  headers.set('access-control-allow-methods', 'GET,POST,PUT,DELETE,OPTIONS')
  headers.set('access-control-allow-headers', 'content-type,authorization,tenant-id')
  return new Response(response.body, { status: response.status, statusText: response.statusText, headers })
}

function nowText() {
  return new Date().toISOString().replace('T', ' ').slice(0, 19)
}

function localNowText() {
  return localTextFromMs(Date.now())
}

function deviceOnlineCutoffText() {
  return localTextFromMs(Date.now() - 5 * 60 * 1000)
}

function num(value: string | null, fallback: number) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
}

function cleanText(value: unknown) {
  return typeof value === 'string' ? value.trim() : ''
}

function resolveMeterNo(value: unknown, gatewaySn: string, meterSn: string) {
  const meterNo = cleanText(value)
  return meterNo || (gatewaySn && meterSn ? `${gatewaySn}_${meterSn}` : '')
}

function emptyTouEnergy(): TouEnergy {
  return { sharpPeak: 0, peak: 0, flat: 0, valley: 0, deepValley: 0 }
}

function sumTouEnergy(energy: TouEnergy) {
  return Number((energy.sharpPeak + energy.peak + energy.flat + energy.valley + energy.deepValley).toFixed(4))
}

function positiveEnergyDelta(previous: AnyRecord, current: AnyRecord, field: string) {
  const start = telemetryNumber(previous, field)
  const end = telemetryNumber(current, field)
  return start !== null && end !== null ? Number(Math.max(0, end - start).toFixed(4)) : 0
}

function telemetryNumber(row: AnyRecord, field: string) {
  const aliases = [field, snake(field), field.toUpperCase()]
  for (const alias of aliases) {
    const value = numberOrNull(row[alias])
    if (value !== null) return value
  }
  return null
}

function splitEnergyByPricingTime(startTime: string, endTime: string, total: number, periods: AnyRecord[]): TouEnergy {
  const energy = emptyTouEnergy()
  const start = parseLocalDateTime(startTime)
  const end = parseLocalDateTime(endTime)
  if (!start || !end || end <= start || total <= 0) {
    energy.flat = total
    return energy
  }
  const totalSeconds = (end - start) / 1000
  let cursor = start
  let guard = 0
  while (cursor < end && guard < 10000) {
    const nextMinute = Math.floor(cursor / 60000) * 60000 + 60000
    const next = Math.min(end, nextMinute > cursor ? nextMinute : cursor + 60000)
    const key = resolveTouKey(cursor, periods)
    energy[key] += total * ((next - cursor) / 1000 / totalSeconds)
    cursor = next
    guard += 1
  }
  if (cursor < end) {
    energy[resolveTouKey(cursor, periods)] += total * ((end - cursor) / 1000 / totalSeconds)
  }
  return roundTouEnergy(energy)
}

function parseTouPeriods(value: unknown): AnyRecord[] {
  if (typeof value !== 'string' || !value) return []
  try {
    const rows = JSON.parse(value)
    return Array.isArray(rows) ? rows.filter((item) => item?.type && item?.start && item?.end) : []
  } catch {
    return []
  }
}

function resolveTouKey(timeMs: number, periods: AnyRecord[]): TouKey {
  const minute = localMinuteOfDay(timeMs)
  const matched = periods.find((period) => isMinuteInPeriod(minute, period))
  return (matched?.type as TouKey) || 'flat'
}

function localMinuteOfDay(timeMs: number) {
  const dayMs = 24 * 60 * 60 * 1000
  const minuteMs = ((timeMs % dayMs) + dayMs) % dayMs
  return Math.floor(minuteMs / 60000)
}

function isMinuteInPeriod(minute: number, period: AnyRecord) {
  const start = timeToMinute(cleanText(period.start))
  const end = timeToMinute(cleanText(period.end))
  if (start === end) return false
  return start < end ? minute >= start && minute < end : minute >= start || minute < end
}

function timeToMinute(value: string) {
  const [hour, minute] = value.split(':').map(Number)
  return Number(hour || 0) * 60 + Number(minute || 0)
}

function parseLocalDateTime(value: string) {
  const match = normalizeDateTimeText(value).match(/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/)
  if (!match) return 0
  const [, year, month, day, hour, minute, second] = match
  return Date.UTC(Number(year), Number(month) - 1, Number(day), Number(hour), Number(minute), Number(second || 0))
}

function intervalMinutes(startTime: string, endTime: string) {
  const start = parseLocalDateTime(startTime)
  const end = parseLocalDateTime(endTime)
  return start && end && end > start ? Number(((end - start) / 60000).toFixed(2)) : null
}

function roundTouEnergy(energy: TouEnergy): TouEnergy {
  return {
    sharpPeak: Number(energy.sharpPeak.toFixed(4)),
    peak: Number(energy.peak.toFixed(4)),
    flat: Number(energy.flat.toFixed(4)),
    valley: Number(energy.valley.toFixed(4)),
    deepValley: Number(energy.deepValley.toFixed(4))
  }
}

function numberOrNull(value: unknown) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

function positiveId(value: unknown) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

function averageNumbers(values: unknown[]) {
  const numbers = values.map(numberOrNull).filter((value): value is number => value !== null)
  if (!numbers.length) return null
  return numbers.reduce((sum, value) => sum + value, 0) / numbers.length
}

function normalizeEiotCollectTime(value: unknown, timestamp?: unknown) {
  return normalizeDateTimeText(value) || localTextFromUnix(timestamp) || localNowText()
}

function normalizeDateTimeText(value: unknown) {
  const text = cleanText(value)
  const match = text.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?/)
  if (!match) return ''
  const [, year, month, day, hour, minute, second] = match
  return `${year}-${month}-${day} ${hour}:${minute}:${second || '00'}`
}

function localTextFromUnix(value: unknown) {
  const timestamp = numberOrNull(value)
  if (!timestamp) return ''
  const milliseconds = timestamp > 100000000000 ? timestamp : timestamp * 1000
  return localTextFromMs(milliseconds)
}

function localTextFromMs(milliseconds: number) {
  return new Date(milliseconds + 8 * 60 * 60 * 1000).toISOString().replace('T', ' ').slice(0, 19)
}

function endOfMonthText(monthValue: string) {
  const match = cleanText(monthValue).match(/^(\d{4})-(\d{2})$/)
  if (!match) return nowText()
  const year = Number(match[1])
  const month = Number(match[2])
  const end = new Date(Date.UTC(year, month, 0, 23, 59, 59))
  return end.toISOString().replace('T', ' ').slice(0, 19)
}

function round2(value: number) {
  return Number(Number(value || 0).toFixed(2))
}

function round4(value: number) {
  return Number(Number(value || 0).toFixed(4))
}

function round8(value: number) {
  return Number(Number(value || 0).toFixed(8))
}

function nullableNumberSum(values: Array<number | null | undefined>) {
  const nums = values.filter((value): value is number => typeof value === 'number' && Number.isFinite(value))
  return nums.length ? round4(nums.reduce((sum, value) => sum + value, 0)) : null
}

function averageNumber(values: Array<number | null>) {
  const nums = values.filter((value): value is number => value !== null && Number.isFinite(value))
  return nums.length ? round8(nums.reduce((sum, value) => sum + value, 0) / nums.length) : 0
}

function uniqueRules(rules: AnyRecord[]) {
  return Array.from(new Map(rules.map((rule, index) => [rule.id || index, rule])).values())
}

function reportScopeName(scopeType: string, devices: AnyRecord[]) {
  if (!devices.length) return '暂无电表'
  if (scopeType === 'device') return devices[0].device_name || devices[0].device_no || `电表 ${devices[0].id}`
  if (scopeType === 'project') return devices[0].project_name || '场站汇总'
  return '全部电表汇总'
}

function consistencyStatus(energy: TouEnergy, total: number) {
  const subtotal = sumTouEnergy(energy)
  if (total <= 0 && subtotal <= 0) return '无数据'
  const tolerance = Math.max(0.02, total * 0.02)
  return Math.abs(subtotal - total) <= tolerance ? '一致' : '分项缺失'
}

function maxDemandFromTelemetry(rows: AnyRecord[]) {
  const bucketMs = 5 * 60 * 1000
  const windowMs = 15 * 60 * 1000
  const buckets = new Map<number, number>()
  rows.forEach((row) => {
    const time = parseLocalDateTime(cleanText(row.collect_time))
    if (!time) return
    const power = numberOrNull(row.p)
    if (power === null) return
    const bucket = Math.floor(time / bucketMs) * bucketMs
    buckets.set(bucket, (buckets.get(bucket) || 0) + Math.max(0, power))
  })
  const points = Array.from(buckets.entries()).map(([time, power]) => ({ time, power })).sort((a, b) => a.time - b.time)
  return points.reduce((max, point, index) => {
    const windowStart = point.time - windowMs + bucketMs
    const window = points.slice(0, index + 1).filter((item) => item.time >= windowStart)
    const demand = window.reduce((sum, item) => sum + item.power, 0) / Math.max(window.length, 1)
    return Math.max(max, demand)
  }, 0)
}

function localizedText(value: unknown) {
  if (!value) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'object') {
    const record = value as AnyRecord
    return cleanText(record.zh_CN || record.en_US || Object.values(record)[0])
  }
  return String(value)
}

function sanitizeKeyPart(value: unknown) {
  return cleanText(value).replace(/[^a-zA-Z0-9_.-]/g, '_').slice(0, 80) || 'unknown'
}

async function scalar(env: Env, sql: string, args: any[]) {
  const row = await env.DB.prepare(sql).bind(...args).first<AnyRecord>()
  return Number(Object.values(row || { count: 0 })[0] || 0)
}

function like(url: URL, where: string[], args: any[], column: string, key: string) {
  const value = url.searchParams.get(key)
  if (value) {
    where.push(`${column} LIKE ?`)
    args.push(`%${value}%`)
  }
}

function exact(url: URL, where: string[], args: any[], column: string, key: string) {
  const value = url.searchParams.get(key)
  if (value !== null && value !== '') {
    where.push(`${column} = ?`)
    args.push(value)
  }
}

function collectTimeRange(url: URL) {
  return rangeParam(url, 'collectTime')
}

function rangeParam(url: URL, key: string) {
  const start =
    url.searchParams.get(`${key}[0]`) ||
    url.searchParams.get(`${key}.0`) ||
    url.searchParams.getAll(key)[0] ||
    null
  const end =
    url.searchParams.get(`${key}[1]`) ||
    url.searchParams.get(`${key}.1`) ||
    url.searchParams.getAll(key)[1] ||
    null
  return { start, end }
}

function camelRows(rows: AnyRecord[]) {
  return rows.map(camel)
}

function camel(row: AnyRecord) {
  return Object.fromEntries(Object.entries(row).map(([key, value]) => [camelKey(key), value]))
}

function camelKey(key: string) {
  return key.replace(/_([a-z])/g, (_, char) => char.toUpperCase())
}

function snake(key: string) {
  return key.replace(/[A-Z]/g, (char) => `_${char.toLowerCase()}`)
}
