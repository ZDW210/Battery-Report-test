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

const ENERGY_MENUS = [
  rootMenu(1000, '运营面板', '/energy', 'ep:monitor', '/energy/dashboard'),
  menu(1001, 1000, '运营面板', 'dashboard', 'energy/dashboard/index', 'EnergyDashboard', 'ep:data-analysis', 1),
  menu(1002, 1000, '数据面板', 'telemetry', 'energy/telemetry/index', 'EnergyTelemetry', 'ep:trend-charts', 2),
  menu(1003, 1000, '设备管理', 'device', 'energy/device/index', 'EnergyDevice', 'ep:cpu', 3),
  menu(1004, 1000, '车辆管理', 'vehicle', 'energy/vehicle/index', 'EnergyVehicle', 'ep:van', 4),
  menu(1005, 1000, '小程序用户', 'app-user', 'energy/appUser/index', 'EnergyAppUser', 'ep:user', 5),
  menu(1006, 1000, '扫码刷卡记录', 'account-event', 'energy/account-event/index', 'EnergyAccountEvent', 'ep:connection', 6),
  menu(1007, 1000, '报警信息', 'alarm', 'energy/alarm/index', 'EnergyAlarm', 'ep:warning', 7),
  menu(1008, 1000, '客户管理', 'customer', 'energy/customer/index', 'EnergyCustomer', 'ep:office-building', 8),
  menu(1009, 1000, '项目管理', 'project', 'energy/project/index', 'EnergyProject', 'ep:location', 9),
  menu(1010, 1000, '用户授权', 'user-scope', 'energy/userScope/index', 'EnergyUserScope', 'ep:key', 10),
  menu(1011, 1000, 'EIOT 同步日志', 'eiot-log', 'energy/eiotLog/index', 'EnergyEiotLog', 'ep:document', 11),
  menu(1012, 1000, '计费规则', 'pricing-rule', 'energy/pricing-rule/index', 'EnergyPricingRule', 'ep:money', 12),
  menu(1013, 1000, '充放电任务', 'charge-session', 'energy/charge-session/index', 'EnergyChargeSession', 'ep:switch-button', 13),
  menu(1014, 1000, '客户账号权限', 'customer-account', 'energy/customerAccount/index', 'EnergyCustomerAccount', 'ep:lock', 14)
]

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url)

    if (request.method === 'OPTIONS') return cors(new Response(null, { status: 204 }))

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
  const menus = buildMenuTree(ENERGY_MENUS.filter((item) => menuIds.includes(item.id) || item.parentId === 0))

  return ok({
    permissions: permissionsForMenus(menuIds),
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
  if (path === '/energy/device/control' && request.method === 'POST') {
    const body = await readJson(request)
    return ok({ controlLogId: Date.now(), success: true, message: `已记录 ${body.method || 'CONTROL'} 指令，等待设备网关执行` })
  }
  return crud(request, url, env, 'energy_device', [
    'deviceNo', 'deviceName', 'deviceType', 'gatewaySn', 'meterSn', 'meterNo', 'customerId', 'projectId', 'status',
    'runMode', 'latitude', 'longitude', 'lastSoc', 'lastSoh', 'lastPower', 'lastVoltage', 'lastCurrent', 'lastTemp',
    'lastReadingTime', 'remark'
  ])
}

async function alarmApi(request: Request, url: URL, env: Env, path: string) {
  if (path === '/energy/alarm/ack' && request.method === 'PUT') return updateStatus(request, env, 'energy_alarm', 1, 'ack_time')
  if (path === '/energy/alarm/close' && request.method === 'PUT') return updateStatus(request, env, 'energy_alarm', 2, 'close_time')
  return crud(request, url, env, 'energy_alarm', ['alarmNo', 'deviceId', 'code', 'level', 'title', 'content', 'status', 'occurTime'])
}

async function pricingRuleApi(request: Request, url: URL, env: Env, path: string) {
  if (path === '/energy/pricing-rule/match') {
    const row = await env.DB.prepare(
      `SELECT * FROM energy_pricing_rule
       WHERE status = 0 AND (? IS NULL OR device_id = ?)
       ORDER BY device_id DESC, id DESC LIMIT 1`
    )
      .bind(url.searchParams.get('deviceId'), url.searchParams.get('deviceId'))
      .first<AnyRecord>()
    return ok(row ? camel(row) : null)
  }
  return crud(request, url, env, 'energy_pricing_rule', ['customerId', 'projectId', 'deviceId', 'timeRate', 'energyRate', 'effectiveStart', 'effectiveEnd', 'status', 'remark'])
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
    const rows = await env.DB.prepare(
      'SELECT * FROM energy_telemetry WHERE (? IS NULL OR device_id = ?) ORDER BY collect_time ASC LIMIT 288'
    )
      .bind(url.searchParams.get('deviceId'), url.searchParams.get('deviceId'))
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
  const body = await readJson(request)
  const dbFields = fields.map(snake).filter((field) => body[camelKey(field)] !== undefined)
  const values = dbFields.map((field) => body[camelKey(field)])
  const sql = `INSERT INTO ${table}(${dbFields.join(',')}, create_time) VALUES (${dbFields.map(() => '?').join(',')}, ?) RETURNING id`
  const row = await env.DB.prepare(sql).bind(...values, nowText()).first<AnyRecord>()
  return ok(Number(row?.id))
}

async function updateRow(request: Request, env: Env, table: string, fields: string[]) {
  const body = await readJson(request)
  const dbFields = fields.map(snake).filter((field) => body[camelKey(field)] !== undefined)
  const values = dbFields.map((field) => body[camelKey(field)])
  const sql = `UPDATE ${table} SET ${dbFields.map((field) => `${field} = ?`).join(', ')}, update_time = ? WHERE id = ?`
  await env.DB.prepare(sql).bind(...values, nowText(), body.id).run()
  return ok(true)
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

function rootMenu(id: number, name: string, path: string, icon: string, redirect: string) {
  return { id, parentId: 0, name, path, component: '', componentName: 'Energy', icon, visible: true, keepAlive: true, alwaysShow: true, redirect, sort: 10 }
}

function menu(id: number, parentId: number, name: string, path: string, component: string, componentName: string, icon: string, sort: number) {
  return { id, parentId, name, path, component, componentName, icon, visible: true, keepAlive: true, alwaysShow: false, redirect: '', sort }
}

function buildMenuTree(rows: AnyRecord[], parentId = 0): AnyRecord[] {
  return rows
    .filter((row) => Number(row.parentId) === parentId)
    .sort((a, b) => Number(a.sort) - Number(b.sort))
    .map((row) => ({ ...row, children: buildMenuTree(rows, Number(row.id)) }))
}

function permissionsForMenus(menuIds: number[]) {
  const permissions = ['energy:dashboard:query', 'energy:telemetry:query']
  if (menuIds.includes(1014)) permissions.push('energy:customer-account:create', 'energy:customer-account:update', 'energy:customer-account:reset-password')
  if (menuIds.includes(1003)) permissions.push('energy:device:create', 'energy:device:update', 'energy:device:delete', 'energy:device:control')
  return permissions
}

function menuOption(item: AnyRecord) {
  return { id: item.id, name: item.name, permission: '', parentId: item.parentId, sort: item.sort }
}

async function replaceAccountMenus(env: Env, accountId: number, menuIds: number[]) {
  await env.DB.prepare('DELETE FROM energy_customer_account_menu WHERE account_id = ?').bind(accountId).run()
  const finalIds = Array.from(new Set([1000, ...menuIds.map(Number)]))
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
