export interface Env {
  ASSETS: Fetcher
  DB: D1Database
  BUCKET: R2Bucket
  SESSION_SECRET?: string
  WECHAT_APP_ID?: string
  WECHAT_APP_SECRET?: string
}

type AnyRecord = Record<string, any>

const ADMIN_PREFIX = '/admin-api'
const DAILY_FIELDS = new Set(['pa', 'pb', 'pc', 'ua', 'ub', 'uc', 'ia', 'ib', 'ic', 'p', 'pf', 'epi'])
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
  'maxDemandPrice',
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
  max_demand_price: 'REAL NOT NULL DEFAULT 0',
  transformer_capacity_price: 'REAL NOT NULL DEFAULT 0',
  site_fee: 'REAL NOT NULL DEFAULT 0',
  maintenance_fee: 'REAL NOT NULL DEFAULT 0',
  communication_fee: 'REAL NOT NULL DEFAULT 0',
  platform_service_fee: 'REAL NOT NULL DEFAULT 0',
  battery_depreciation_cost: 'REAL NOT NULL DEFAULT 0',
  other_fixed_fee: 'REAL NOT NULL DEFAULT 0'
}
const DEVICE_FIELDS = [
  'deviceNo', 'deviceName', 'deviceType', 'gatewaySn', 'meterSn', 'meterNo', 'customerId', 'projectId', 'status',
  'runMode', 'latitude', 'longitude', 'lastSoc', 'lastSoh', 'lastPower', 'lastVoltage', 'lastCurrent', 'lastTemp',
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
      return handleEiotPush(request, env, ctx, url.pathname)
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

function handleEiotPush(request: Request, env: Env, ctx: ExecutionContext, path: string) {
  const receivedAt = Date.now()
  ctx.waitUntil(processEiotPush(request.clone(), env, path, receivedAt))
  return ok({ accepted: true, receivedAt: new Date(receivedAt).toISOString() })
}

async function processEiotPush(request: Request, env: Env, path: string, receivedAt: number) {
  let rawBody = ''
  let payload: unknown = null
  let pushType = path === '/eiot/alarm' ? 'alarm' : path === '/eiot/meter' ? 'meter' : 'unknown'
  let payloadUrl = ''
  let gatewaySn = ''
  let meterSn = ''
  const requestId = crypto.randomUUID()

  try {
    rawBody = await request.text()
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

    if (path === '/energy/customer-account/page' && request.method === 'GET') return customerAccountPage(url, env)
    if (path === '/energy/customer-account/get' && request.method === 'GET') return customerAccountGet(url, env)
    if (path === '/energy/customer-account/create' && request.method === 'POST') return createCustomerAccount(request, env)
    if (path === '/energy/customer-account/update' && request.method === 'PUT') return updateCustomerAccount(request, env)
    if (path === '/energy/customer-account/reset-password' && request.method === 'PUT') return resetCustomerPassword(request, env)
    if (path === '/energy/customer-account/menu-options' && request.method === 'GET') return ok(ENERGY_MENUS.map(menuOption))

    if (path.startsWith('/energy/customer/')) return crud(request, url, env, 'energy_customer', ['name', 'contactName', 'contactMobile', 'region', 'status', 'remark'])
    if (path.startsWith('/energy/project/')) return crud(request, url, env, 'energy_project', ['customerId', 'name', 'code', 'address', 'latitude', 'longitude', 'status', 'remark'])
    if (path.startsWith('/energy/device/')) return deviceApi(request, url, env, path)
    if (path.startsWith('/energy/vehicle/')) return crud(request, url, env, 'energy_vehicle', ['vehicleNo', 'plateNo', 'qrCode', 'deviceId', 'customerId', 'projectId', 'status', 'remark'])
    if (path.startsWith('/energy/app-user/')) return crud(request, url, env, 'energy_app_user', ['username', 'nickname', 'mobile', 'cardNo', 'miniAdminEnabled', 'status', 'loginIp', 'loginDate', 'remark'])
    if (path.startsWith('/energy/account-event/')) return crud(request, url, env, 'energy_account_event', ['eventScene', 'authType', 'scanText', 'cardNo', 'accountKnown', 'accountId', 'accountName', 'accountMobile', 'deviceId', 'customerId', 'projectId', 'resultMessage'])
    if (path.startsWith('/energy/user-scope/')) return crud(request, url, env, 'energy_user_scope', ['userId', 'userType', 'customerId', 'projectId', 'deviceId', 'status', 'remark'])
    if (path.startsWith('/energy/pricing-rule/')) return pricingRuleApi(request, url, env, path)
    if (path.startsWith('/energy/charge-session/')) return chargeSessionApi(request, url, env, path)
    if (path.startsWith('/energy/eiot-log/')) return crud(request, url, env, 'energy_eiot_sync_log', ['syncType', 'requestId', 'gatewaySn', 'meterSn', 'payloadUrl', 'status', 'errorMsg'])
    if (path.startsWith('/energy/alarm/')) return alarmApi(request, url, env, path)
    if (path.startsWith('/energy/telemetry/')) return telemetryApi(request, url, env, path)

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
  if (!session) return fail('无效的刷新令牌', 401, 401)
  const accessToken = await token('ak')
  const expiresTime = Date.now() + 7 * 24 * 60 * 60 * 1000
  await env.DB.prepare('UPDATE system_session SET access_token = ?, expires_time = ? WHERE refresh_token = ?')
    .bind(accessToken, expiresTime, refresh)
    .run()
  return ok({ accessToken, refreshToken: refresh, userId: session.user_id, userType: 2, clientId: 'worker-admin', expiresTime })
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
  const exists = await env.DB.prepare('SELECT id FROM system_user WHERE username = ?').bind(username).first()
  if (exists) return fail('账号已存在', 400)

  const passwordHash = await hashPassword(String(body.password || '123456'))
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
  const account = await env.DB.prepare('SELECT system_user_id FROM energy_customer_account WHERE id = ?').bind(body.id).first<AnyRecord>()
  if (!account) return fail('账号不存在', 404)
  await env.DB.prepare('UPDATE system_user SET password_hash = ? WHERE id = ?')
    .bind(await hashPassword(String(body.password || '123456')), account.system_user_id)
    .run()
  return ok(true)
}

async function deviceApi(request: Request, url: URL, env: Env, path: string) {
  if (path === '/energy/device/page' && request.method === 'GET') return devicePage(url, env)
  if (path === '/energy/device/simple-list' && request.method === 'GET') return deviceSimpleList(url, env)
  if (path === '/energy/device/control' && request.method === 'POST') {
    const body = await readJson(request)
    return ok({ controlLogId: Date.now(), success: true, message: `已记录 ${body.method || 'CONTROL'} 指令，等待设备网关执行` })
  }
  if (path === '/energy/device/create' && request.method === 'POST') {
    const body = await readJson(request)
    const validation = await validateDeviceUnique(env, body)
    if (validation) return validation
    return createRowFromBody(body, env, 'energy_device', DEVICE_FIELDS)
  }
  if (path === '/energy/device/update' && request.method === 'PUT') {
    const body = await readJson(request)
    const validation = await validateDeviceUnique(env, body, Number(body.id))
    if (validation) return validation
    return updateRowFromBody(body, env, 'energy_device', DEVICE_FIELDS)
  }
  return crud(request, url, env, 'energy_device', DEVICE_FIELDS)
}

async function devicePage(url: URL, env: Env) {
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

  return ok({ list: camelRows(rows.results), total })
}

async function deviceSimpleList(url: URL, env: Env) {
  const where: string[] = []
  const args: any[] = []

  exact(url, where, args, 'd.customer_id', 'customerId')
  exact(url, where, args, 'd.project_id', 'projectId')
  exact(url, where, args, 'd.status', 'status')

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

  return ok(camelRows(rows.results))
}

async function alarmApi(request: Request, url: URL, env: Env, path: string) {
  if (path === '/energy/alarm/ack' && request.method === 'PUT') return updateStatus(request, env, 'energy_alarm', 1, 'ack_time')
  if (path === '/energy/alarm/close' && request.method === 'PUT') return updateStatus(request, env, 'energy_alarm', 2, 'close_time')
  return crud(request, url, env, 'energy_alarm', ['alarmNo', 'deviceId', 'code', 'level', 'title', 'content', 'status', 'occurTime'])
}

async function pricingRuleApi(request: Request, url: URL, env: Env, path: string) {
  await ensurePricingRuleFeeColumns(env)
  if (path === '/energy/pricing-rule/page' && request.method === 'GET') return pricingRulePage(url, env)
  if (path === '/energy/pricing-rule/match') {
    return matchPricingRule(url, env)
  }
  if (path === '/energy/pricing-rule/create' && request.method === 'POST') return createPricingRule(request, env)
  if (path === '/energy/pricing-rule/update' && request.method === 'PUT') return updatePricingRule(request, env)
  return crud(request, url, env, 'energy_pricing_rule', PRICING_RULE_FIELDS)
}

async function pricingRulePage(url: URL, env: Env) {
  const pageNo = num(url.searchParams.get('pageNo'), 1)
  const pageSize = num(url.searchParams.get('pageSize'), 10)
  const where: string[] = []
  const args: any[] = []
  const { start, end } = rangeParam(url, 'effectiveStart')

  exact(url, where, args, 'r.customer_id', 'customerId')
  exact(url, where, args, 'r.project_id', 'projectId')
  exact(url, where, args, 'r.device_id', 'deviceId')
  exact(url, where, args, 'r.status', 'status')
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
     ${whereSql}`,
    args
  )
  const rows = await env.DB.prepare(
    `SELECT r.*, c.name AS customer_name, p.name AS project_name, d.device_name, d.device_no
     FROM energy_pricing_rule r
     LEFT JOIN energy_customer c ON c.id = r.customer_id
     LEFT JOIN energy_project p ON p.id = r.project_id
     LEFT JOIN energy_device d ON d.id = r.device_id
     ${whereSql}
     ORDER BY r.id DESC
     LIMIT ? OFFSET ?`
  )
    .bind(...args, pageSize, (pageNo - 1) * pageSize)
    .all<AnyRecord>()

  return ok({ list: camelRows(rows.results), total })
}

async function matchPricingRule(url: URL, env: Env) {
  const deviceId = url.searchParams.get('deviceId')
  if (!deviceId) return ok(null)

  const device = await env.DB.prepare('SELECT id, customer_id, project_id FROM energy_device WHERE id = ?')
    .bind(deviceId)
    .first<AnyRecord>()
  if (!device) return ok(null)

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
       AND (? IS NULL OR r.effective_end IS NULL OR r.effective_end >= ?)
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

async function chargeSessionApi(request: Request, url: URL, env: Env, path: string) {
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

async function telemetryApi(request: Request, url: URL, env: Env, path: string) {
  if (path === '/energy/telemetry/chart') {
    const { start, end } = collectTimeRange(url)
    const rows = await env.DB.prepare(
      `SELECT * FROM energy_telemetry
       WHERE (? IS NULL OR device_id = ?)
       AND (? IS NULL OR collect_time >= ?)
       AND (? IS NULL OR collect_time <= ?)
       ORDER BY collect_time ASC LIMIT ?`
    )
      .bind(url.searchParams.get('deviceId'), url.searchParams.get('deviceId'), start, start, end, end, num(url.searchParams.get('limit'), 288))
      .all<AnyRecord>()
    return ok(camelRows(rows.results))
  }
  if (path === '/energy/telemetry/daily-stat') {
    const requested = snake(String(url.searchParams.get('field') || 'p'))
    const field = DAILY_FIELDS.has(requested) ? requested : 'p'
    const rows = await env.DB.prepare(
      `SELECT substr(collect_time, 1, 10) AS date, MAX(${field}) AS max, MIN(${field}) AS min, AVG(${field}) AS avg
       FROM energy_telemetry WHERE (? IS NULL OR device_id = ?) GROUP BY substr(collect_time, 1, 10) ORDER BY date DESC LIMIT 31`
    )
      .bind(url.searchParams.get('deviceId'), url.searchParams.get('deviceId'))
      .all<AnyRecord>()
    return ok(camelRows(rows.results))
  }
  return crud(request, url, env, 'energy_telemetry', ['deviceId', 'gatewaySn', 'meterSn', 'meterNo', 'collectTime', 'timestamp', 'source', 'state', 'pa', 'pb', 'pc', 'ua', 'ub', 'uc', 'ia', 'ib', 'ic', 'p', 'pf', 'epi'])
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
  await ensureColumns(env, 'energy_telemetry', {
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
  await env.DB.prepare('CREATE INDEX IF NOT EXISTS idx_energy_alarm_gateway_meter_time ON energy_alarm(gateway_sn, meter_sn, occur_time)').run()
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
    const meterNo = cleanText(row.meterNo)
    const timestamp = numberOrNull(row.timestamp)
    const collectTime = cleanText(row.createTime || row.CreateTime) || textFromUnix(timestamp)
    const device = await findDeviceByMeter(env, meterNo, gatewaySn, meterSn)
    const deviceId = device?.id ? Number(device.id) : null

    await env.DB.prepare(
      `INSERT INTO energy_telemetry(
        device_id, gateway_sn, meter_sn, meter_no, collect_time, timestamp, source, state,
        pa, pb, pc, ua, ub, uc, ia, ib, ic, p, pf, epi, data_json, payload_url, create_time
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`
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
        JSON.stringify(row),
        payloadUrl,
        nowText()
      )
      .run()

    if (deviceId) {
      await updateDeviceLatestFromTelemetry(env, deviceId, row, collectTime || nowText())
    }
  }
}

async function saveEiotAlarmPayload(env: Env, payload: unknown, payloadUrl: string) {
  const body = payload && typeof payload === 'object' ? (payload as AnyRecord) : {}
  const list = Array.isArray(body.list) ? body.list : []
  if (!list.length) throw new Error('Alarm payload must contain list[]')

  const gatewaySn = cleanText(body.gatewaySn)
  const meterSn = cleanText(body.meterSn)
  const meterNo = cleanText(body.meterNo)
  const timestamp = numberOrNull(body.timestamp)
  const occurTime = cleanText(body.createTime || body.CreateTime) || textFromUnix(timestamp) || nowText()
  const device = await findDeviceByMeter(env, meterNo, gatewaySn, meterSn)
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

async function updateDeviceLatestFromTelemetry(env: Env, deviceId: number, row: AnyRecord, collectTime: string) {
  const state = cleanText(row.state)
  const status = state === 'ONLINE' ? 0 : state === 'OFFLINE' ? 1 : null
  const voltage = averageNumbers([row.Ua, row.Ub, row.Uc])
  const current = averageNumbers([row.Ia, row.Ib, row.Ic])
  await env.DB.prepare(
    `UPDATE energy_device
     SET status = COALESCE(?, status),
         last_power = COALESCE(?, last_power),
         last_voltage = COALESCE(?, last_voltage),
         last_current = COALESCE(?, last_current),
         last_reading_time = ?,
         update_time = ?
     WHERE id = ?`
  )
    .bind(status, numberOrNull(row.P), voltage, current, collectTime, nowText(), deviceId)
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

async function sha256Hex(value: string) {
  const data = new TextEncoder().encode(value)
  const hash = await crypto.subtle.digest('SHA-256', data)
  return [...new Uint8Array(hash)].map((item) => item.toString(16).padStart(2, '0')).join('')
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

function num(value: string | null, fallback: number) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
}

function cleanText(value: unknown) {
  return typeof value === 'string' ? value.trim() : ''
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

function textFromUnix(value: unknown) {
  const timestamp = numberOrNull(value)
  if (!timestamp) return ''
  const milliseconds = timestamp > 100000000000 ? timestamp : timestamp * 1000
  return new Date(milliseconds).toISOString().replace('T', ' ').slice(0, 19)
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
