# 接口设计标准

## 接口前缀

| 前缀 | 使用方 | 说明 |
| --- | --- | --- |
| `/admin-api/energy/**` | 管理端 | 后台管理接口 |
| `/app-api/energy/**` | 小程序 | 客户、司机、运维人员接口 |
| `/infra-api/energy/**` | EIOT worker / 内部服务 | 数据接入和内部同步 |

## 响应格式

遵循 RuoYi-Vue-Pro 统一响应：

```json
{
  "code": 0,
  "data": {},
  "msg": ""
}
```

## 分页标准

列表接口统一使用分页：

```text
GET /admin-api/energy/device/page?pageNo=1&pageSize=10
```

分页响应遵循 RuoYi 现有分页结构。

## 管理端接口标准

管理端 CRUD 优先遵循：

```text
GET    /page
GET    /get?id=
POST   /create
PUT    /update
DELETE /delete?id=
GET    /export
```

管理端设备台账接口使用独立资源路径：

```text
/admin-api/energy/device/**
```

- 设备新增/修改必须要求 `gatewaySn`、`meterSn`、`meterNo`、`customerId`、`projectId`，避免出现无法匹配 EIOT 报文、扫码/刷卡设备和计费归属的空台账。
- 设备分页查询必须支持 `gatewaySn`、`meterSn`、`meterNo` 过滤；小程序和扫码解析中的关键字匹配必须覆盖 `meterNo`、`gatewaySn`、`meterSn`。
- 设备匹配优先使用 `meterNo`，其次使用 `gatewaySn + meterSn`；只有台账中存在对应设备时，实时数据、告警、扫码和刷卡流程才能继续进入授权或任务校验。

管理端用户授权接口使用独立资源路径：

```text
/admin-api/energy/user-scope/**
```

- 授权维护只允许后台令牌访问，权限标识使用 `energy:user-scope:*`。
- 每条授权记录必须且只能绑定一个范围：客户、项目或设备。
- 第一版不强依赖 member 模块，允许通过 App 用户 ID 手动维护；接入正式小程序登录后再补用户选择器。

管理端 EIOT 同步日志接口使用独立资源路径：

```text
/admin-api/energy/eiot-log/**
```

- 仅允许后台令牌访问，权限标识使用 `energy:eiot-log:query`。
- 日志查询必须支持同步类型、状态、请求编号、网关序列号、电表序列号和创建时间范围过滤。
- 失败日志必须展示 `errorMsg`，用于定位设备未匹配、时间格式错误、重复忽略等接入问题。

管理端计费规则接口使用独立资源路径：

```text
/admin-api/energy/pricing-rule/**
```

- 仅允许后台令牌访问，权限标识使用 `energy:pricing-rule:*`。
- 每条规则必须且只能绑定一个计费范围：客户、项目或设备。
- 保存前必须校验计费范围对象存在，并校验结束时间不早于开始时间。
- 保存请求体时间字段使用 `yyyy-MM-dd HH:mm:ss` 字符串，由 Service 层显式解析。
- 保存请求体必须支持上海工商业电价表类字段：`electricityCategory`、`pricingMode`、`voltageLevel`、`agentPurchasePrice`、`lineLossPrice`、`transmissionDistributionPrice`、`systemOperationFee`、`governmentFundSurcharge`、`sharpPeakRate`、`peakRate`、`flatRate`、`valleyRate`、`deepValleyRate`、`capacityBillingMode`、`maxDemandPrice`、`transformerCapacityKva`、`transformerCapacityPrice`。
- 保存请求体必须支持固定费用字段：`siteFee`、`maintenanceFee`、`communicationFee`、`platformServiceFee`、`batteryDepreciationCost`、`otherFixedFee`；这些字段由运营人员手动维护，用于数据面板利润测算。
- 分页响应必须聚合返回客户名称、项目名称、设备名称和设备编码，避免前端再做多次明细查询。
- 生效规则匹配接口使用 `GET /admin-api/energy/pricing-rule/match`，参数为 `deviceId` 和可选 `billingTime`。
- 计费匹配优先级固定为设备级 > 项目级 > 客户级；只允许返回启用且覆盖计费时间的规则，同一范围多条命中时取最新生效、最新编号的一条。
- 充放电任务、会话结算、后台试算等需要确定价格时，必须复用计费规则匹配服务，不得在其他模块重复写不同优先级。

管理端充放电会话接口使用独立资源路径：

```text
/admin-api/energy/charge-session/**
```

- 仅允许后台令牌访问，权限标识使用 `energy:charge-session:*`。
- 第一版必须支持分页、详情、开始、结束、结算；暂停/恢复待设备控制服务和状态流转补齐后再开放。
- 开始会话必须校验设备没有进行中会话，并复用计费规则匹配服务；未命中生效计费规则时不得创建会话。
- 开始会话写入 `energy_charge_session` 前必须校验 `deviceId` 对应设备存在、`pricingRuleId` 对应计费规则存在；不得写入孤儿设备或孤儿计费规则引用。
- 开始会话必须校验设备已绑定客户；手动传入 `pricingRuleId` 时也必须确认该计费规则适用于当前设备、项目或客户，不能绕过自动匹配规则直接写入会话。
- 开始会话必须记录 `operator_user_id`；停止、结算、修改和删除会话前必须校验当前用户是否为会话操作人或具备对应范围权限，禁止仅凭会话 ID 更新。
- 结束会话必须计算总电量、时长、电量费用、时间费用和总费用；结束读数不能小于开始读数。
- 结束读数、开始读数等客户端传入电量必须在 Worker 后端校验为非负数；如果同时存在开始和结束读数，结束读数不得小于开始读数。
- 当前 EIOT 真实实时字段只有 `EPI`，第一版会话使用 `start_energy/end_energy` 保存通用电能读数；不得把未收到的 `EPE` 或 BMS/PCS 放电电能字段伪装成真实结算依据。

## 小程序接口标准

小程序接口只返回当前登录用户可访问的数据，禁止直接透出后台管理字段。

必须做权限过滤：

- 客户只能看自己名下客户、项目、设备、账单
- 运维只能看自己负责的项目或设备
- 司机/现场人员只能看授权服务中的设备

小程序扫码放电接口使用充放电会话资源路径：

```text
GET  /app-api/energy/charging-sessions
POST /app-api/energy/charging-sessions/scan/verify
POST /app-api/energy/charging-sessions/scan/discharge
```

设备侧刷卡接口使用 EIOT 入站资源路径，并必须携带 `X-EIOT-Token`：

```text
POST /infra-api/energy/eiot/card/verify
POST /infra-api/energy/eiot/card/discharge
```

- 扫码内容优先按设备/充电桩/仪表识别：`deviceId`、`deviceNo`、`meterNo`、`gatewaySn + meterSn` 或包含这些参数的二维码链接。
- `vehicleId`、`vehicleNo`、`plateNo`、`qrCode` 仅作为旧车辆绑定二维码的兼容字段，不作为新的主流程设计。
- `/scan/verify` 只做当前登录微信账号或刷卡卡号与扫码设备的账户识别和授权校验，并返回可展示的设备概要。
- `/scan/discharge` 必须再次解析扫码内容并校验授权，不能信任前端已经校验过的结果；校验通过后以 `sessionType=1` 创建放电会话并复用后端充放电任务和设备控制联动。
- `/charging-sessions` 必须返回当前 App 用户授权范围内的真实充放电会话，支持按 `deviceId`、`customerId`、`status`、`from`、`to`、`limit` 筛选；状态兼容小程序字段：`ACTIVE`、`COMPLETED`、`ABNORMAL`、`SETTLED`。
- 小程序统计页使用的收入、今日充电、今日放电、进行中会话和出租率必须优先来自 `/charging-sessions` 与授权设备列表，不得再使用租户全量或固定空数组伪装。
- `/eiot/card/verify` 和 `/eiot/card/discharge` 用于充电桩/刷卡设备上报 `cardNo + scanText`，不得要求小程序登录态。
- 当前账号无授权、设备不存在、设备未绑定客户、未匹配计费规则或 EIOT 控制失败时，均不得开始放电任务。
- 新流程不要求车辆档案；`energy_vehicle` 仅作为可选车辆资产绑定或旧二维码兼容，停用车辆兼容码不得通过扫码校验。
- 扫码/刷卡识别事件必须写入 `energy_account_event`，用于管理端“扫码刷卡记录”追溯。

## EIOT 接口标准

EIOT 接入接口必须具备：

- 请求鉴权
- 幂等键，例如 `requestId`
- 原始报文归档
- 同步日志
- 失败原因记录
- 可重试能力

推荐接口：

```text
POST /infra-api/energy/eiot/realtime
POST /infra-api/energy/eiot/alarm
```

当前 EIOT 实际可收到的报文只有两类：

- **实时电表数据**：单条 JSON，字段包含 `gatewaySn`、`meterSn`、`meterNo`、`createTime`、`timestamp`、`source`、`state`、三相电压电流、有功功率、功率因数、`EPI`。
- **告警列表数据**：单条 JSON，字段包含 `gatewaySn`、`meterSn`、`projectCode`、`projectName`、`createTime`、`timestamp`、`list[]`，告警标题和内容为中英文对象。

接入标准：

- 设备匹配优先使用 `meterNo`，其次使用 `gatewaySn + meterSn`。
- 告警 `list[]` 必须兼容，标题/内容优先取 `zh_CN`。
- 实时数据不包含 SOC/SOH/BMS 温度，管理端和小程序不能把这些字段作为 EIOT 电表接入的必填项。
- EIOT 接入口必须保证存在租户上下文；本地未传 `tenant-id` 时默认使用租户 `1`，正式接入可通过 token/signature 映射租户后由后端设置租户上下文。
- 实时电表数据入库必须保留 `gatewaySn`、`meterSn`、`meterNo`、`timestamp`、`source`、`state`、`Pa/Pb/Pc`、`Ua/Ub/Uc`、`Ia/Ib/Ic`、`P`、`PF`、`EPI`，管理端通过 `/admin-api/energy/telemetry/page` 查询展示。
- 管理端实时采集数据统一使用 `energy:telemetry:query` 权限；分页列表使用 `/admin-api/energy/telemetry/page`，曲线和间隔明细原始点使用 `/admin-api/energy/telemetry/chart`，逐日极值使用 `/admin-api/energy/telemetry/daily-stat`。
- 管理端 GET 请求日期范围数组必须按 `collectTime[0]=...&collectTime[1]=...`、`effectiveStart[0]=...&effectiveStart[1]=...` 这类方括号索引格式提交；前端 axios 参数序列化不得改成点号格式，否则 Worker `rangeParam` 无法解析。
- 逐日极值接口除返回每日 `max`、`min`、`avg` 外，必须返回 `maxTime` 和 `minTime`，用于管理端多级表头展示最大值/最小值的发生时间；没有采集值的日期由前端按查询日期范围补齐显示 `--`。
- 遥测图表接口只能开放当前真实入库字段：`pa/pb/pc/p`、`ua/ub/uc`、`ia/ib/ic`、`pf`、`epi`。未收到的线电压、频率、无功功率、视在功率、温度、开关量等字段不得作为真实指标返回。
- 充放电结算如需放电电能，必须确认 EIOT 是否能提供反向电能 `EPE` 或额外 BMS/PCS 数据；仅有 `EPI` 时只能稳定计算正向有功电能变化。

## 鉴权标准

- 管理端：RuoYi 后台令牌和权限标识
- 小程序：会员或用户令牌
- EIOT：固定 token、签名、IP 白名单按需组合

## 时间字段标准

- 管理端查询参数继续使用 RuoYi 约定的 `yyyy-MM-dd HH:mm:ss` 字符串，并在 PageReqVO 上使用 `@DateTimeFormat`。
- EIOT 等外部系统请求体如果约定传 `yyyy-MM-dd HH:mm:ss` 字符串，VO 不直接使用 `LocalDateTime` 接收；RuoYi 当前全局 Jackson 对 `LocalDateTime` 按 epoch millis 反序列化，字符串会被读成 0。
- 外部接入请求体应先用 `String` 接收时间字段，再在 Service 层按契约格式显式解析，并把解析失败写入同步日志。

## EIOT 出向调用标准

本系统主动调用 EIOT 平台的接口（出向）必须满足：

- **凭证管理**：EIOT 登录凭证存储在 `energy_eiot_credential`，密码明文存储用于构造 AES 加密参数。
- **Token 生命周期**：懒加载获取，控制请求失败时自动刷新重试一次，定时任务提前 5 分钟刷新。
- **加密标准**：AES-128-CBC / Zero Padding / Base64，Key 和 IV 默认 `1234567890123456`，后续支持按平台配置。
- **操控日志**：每次出向调用必须写入 `energy_device_control_log`，记录请求体、响应体、EIOT 返回码、成功/失败状态，不可删除。
- **重试策略**：Token 错误（10004/10005/10006）刷新 Token 后重试一次；平台内部错误（4151/4152/4153）指数退避重试最多 3 次；业务错误（2102/2022）不重试。
- **超时设置**：HTTP 连接超时 5 秒，读取超时 15 秒。
- **安全约束**：FORCESWITCH 和 RESET 仅管理端可用，小程序端仅开放 SWITCH。
- **第一版落地约束**：管理端设备控制接口为 `POST /admin-api/energy/device/control`，权限为 `energy:device:control`；未配置 `energy.eiot.control-url` 时必须返回明确失败信息并写入操控日志，不得返回成功。

## 小程序第一版授权过渡标准

- 在用户-客户/项目/设备授权关系表落地前，小程序接口只允许作为第一版租户级接口实现。
- 业务数据接口必须保留登录令牌校验，不允许加 `@PermitAll` 公开设备、告警、账单等业务数据。
- 接口契约必须标注当前租户级范围，以及后续 ACL 收窄点。

## 小程序用户授权标准

- 小程序登录接口使用 `/app-api/energy/auth/login`，返回 RuoYi OAuth2 `accessToken` / `refreshToken`。
- 小程序请求必须携带 `tenant-id`；登录后的业务接口必须携带 `Authorization: Bearer {accessToken}`。
- 当前阶段 App 用户由 `energy_app_user` 维护，token 用户类型使用 `UserTypeEnum.MEMBER = 1`；暂不启用完整 member 模块。
- 小程序业务数据接口必须优先按 `energy_user_scope` 过滤数据范围。
- 授权范围支持客户、项目、设备三个粒度；设备粒度优先级最高，项目/客户粒度用于批量授权。
- 登录 App 用户没有任何启用授权记录时，业务列表和汇总接口返回空数据，不回退到租户全量。
- `energy_user_scope.user_type` 必须参与查询条件，避免后台用户和 App 用户 ID 相同导致串权。
- 小程序已有页面调用但后端真实业务尚未落地的统计接口，可以先提供兼容版空数据接口；兼容接口必须登录校验，且不得返回租户全量敏感数据。已落地真实业务的接口，例如 `/app-api/energy/charging-sessions`，必须返回授权范围内真实数据。
- 微信小程序正式登录接口使用 `POST /app-api/energy/auth/wechat-login`；后端必须用微信 code 换取 openid，优先查找已绑定 App 用户，未绑定时自动创建 `energy_app_user` 并写入 `system_social_user` / `system_social_user_bind` 绑定关系。
- 管理端用户授权不允许要求运营人员手动录入新增小程序用户；授权表单必须通过 `GET /admin-api/energy/app-user/simple-list` 自动加载已登录/已创建的 App 用户，再由运营人员开放客户、项目或设备权限。
- 微信首次登录自动创建的 App 用户必须默认 `miniAdminEnabled=false`，即司机端账号；管理端“使用账户”中开启小程序管理权限后，登录响应和 `/app-api/energy/auth/profile` 才允许返回 `miniAdminEnabled=true`。
- 小程序管理页接口仍必须按 `energy_user_scope` 做数据范围过滤；`miniAdminEnabled` 只决定是否显示/进入小程序内管理页面，不替代客户、项目、设备授权。

## 客户老板网页账号标准

- 客户老板账号不是小程序司机账号，不写入 `energy_app_user`；必须走后台登录用户 `system_user` 和专属角色 `system_role`。
- 管理端新增客户老板账号使用 `/admin-api/energy/customer-account/**`，权限标识为 `energy:customer-account:*`。
- 创建客户老板账号时必须绑定一个 `energy_customer`，并同步创建后台登录用户、专属角色、用户角色关系和角色菜单权限。
- 客户老板账号的“开放板块”只控制网页端菜单呈现和后端接口权限，默认只赋予所选板块的查询与导出类权限；新增、删除、设备控制、开始/结束任务等高风险操作不得默认开放。
- 客户老板账号创建和重置密码接口不得使用硬编码默认密码；必须由管理员显式输入初始/临时密码，并在前后端校验 8-32 位且同时包含字母和数字。
- 运营人员把账号密码发给客户老板登录网页端；客户老板登录后左侧菜单只能看到其角色已开放的板块。
- 后续如需要客户老板只能看自己客户下的数据，必须在对应业务查询接口补充客户维度数据权限过滤，不能只依赖菜单隐藏。
## 2026-06-12 App/EIOT 安全收口补充

- EIOT 入站接口 `/infra-api/energy/eiot/realtime` 和 `/infra-api/energy/eiot/alarm` 必须校验 `X-EIOT-Token` 请求头，服务端配置项为 `energy.eiot.inbound-token`；未配置或不匹配时必须拒绝接收报文。
- 小程序正式客户入口只允许微信登录，账号密码 App 登录默认关闭；如本地联调确需打开，必须显式设置 `energy.app.password-login-enabled=true`，不得在客户版页面暴露默认账号密码。
- 小程序 App 设备详情、最新遥测、历史遥测、报警列表接口必须复用当前登录 App 用户的 `energy_user_scope` 授权范围，不允许仅靠前端隐藏入口控制数据边界。
- 当前小程序兼容接口包括：`GET /app-api/energy/devices/{id}`、`GET /app-api/energy/devices/{id}/latest`、`GET /app-api/energy/devices/{id}/readings`、`GET /app-api/energy/alarms`。这些接口只读，客户侧不得提供客户、计费规则、设备基础资料维护能力。
- 微信小程序 AppSecret 不得明文写入可提交的配置文件；本地启动通过 `ENERGY_WECHAT_APP_SECRET` 注入，设计文档和变更记录只写变量名，不写真实密钥。
## 2026-06-14 小程序统计/账单接口补充

- 小程序首页和统计页使用的收入、电量、进行中会话、出租率等汇总接口，必须基于当前 App 用户 `energy_user_scope` 授权范围内的真实会话和设备计算，不得返回固定 0、租户全量数据或前端拼装假汇总。
- `/app-api/energy/charging-sessions/revenue-overview` 应只汇总已完成会话收入；今日收入按当天范围计算，月收入按当月范围计算，进行中会话按授权范围内 `ACTIVE` 会话计算。
- `/app-api/energy/charging-sessions/today-energy` 应按会话类型区分充电与放电，优先使用会话结算后的 `totalEnergy`；无真实会话电量时返回 0，不得使用未接入字段推算。
- 小程序列表查询的 `from/to` 可以传 ISO 8601 时间字符串；后端解析带 `Z` 或时区偏移的时间时，必须按偏移量转换到系统本地时区后再查询，避免出现 8 小时偏移。
- 当前 EIOT 报文只有 `EPI` 时，只能作为正向有功电能来源；后端响应不得把缺失的 `EPE`、PCS/BMS 放电电量伪装成真实字段。放电会话应使用通用 `totalEnergy` 表达已结算电量。
## 2026-06-14 小程序设备遥测兼容接口补充

- 小程序设备兼容接口只能返回当前 EIOT 已稳定接收并入库的电表字段：`gatewaySn`、`meterSn`、`meterNo`、`collectTime/timestamp`、`source`、`state`、`Pa/Pb/Pc/P`、`Ua/Ub/Uc`、`Ia/Ib/Ic`、`PF`、`EPI`。
- `/app-api/energy/devices/{id}/latest` 和 `/app-api/energy/devices/{id}/readings` 不得为了兼容旧页面继续透出 `EPE`、`Q`、`Fr`、`Temp`、SOC/SOH、BMS 电压电流温度等当前报文未提供字段。
- `/app-api/energy/devices/energy-delta` 如需提供区间电量变化，在当前只有 `EPI` 的阶段只能计算正向有功电能首尾差；不得返回固定 0 的放电/反向电能字段伪装成真实指标，未接入前应省略或保持空状态。
- 所有小程序账单刷新、导出和结算明细查询必须显式携带完成态条件，例如 `status=COMPLETED`；扫码启动后刷新列表也不得把新建的进行中会话混入账单。
## 2026-06-14 设备状态枚举补充

- 移动储能设备状态统一使用：`0=在线`、`1=离线`、`2=故障`、`3=维护`。
- 管理端、小程序、实时监控和首页统计不得沿用其他 IoT 示例模块的 `1=在线/2=离线` 枚举。
- EIOT 实时报文 `state=ONLINE` 映射设备状态 `0`，`state=OFFLINE` 映射设备状态 `1`。
- 告警状态和设备状态必须分开处理；设备状态 `3` 表示维护，不表示报警。
## 2026-06-14 小程序 App 设备列表真实字段补充

- `/app-api/energy/device/list` 面向小程序首页设备列表时，只能返回客户侧当前可展示的真实字段；不得继续透出 SOC/SOH、BMS 温度、电池电压电流等未接入字段。
- 列表页允许使用设备表中由 EIOT 实时报文更新出的 `lastPower`、`lastVoltage`、`lastCurrent`、`lastReadingTime`，并可由后端补充最新遥测 `EPI`，用于客户侧快速查看。
- 设备详情接口 `/app-api/energy/devices/{id}` 继续以最新遥测为准展示三相电压、三相电流、功率、功率因数和 `EPI`；列表和详情不得恢复未接入字段。
## 2026-06-14 小程序司机/管理分流补充

- `energy_app_user.mini_admin_enabled` 是小程序管理端入口权限字段，默认 false；后台开启后才允许客户在小程序“我的”页看到“管理”入口。
- `/app-api/energy/auth/wechat-login` 和 `/app-api/energy/auth/profile` 必须返回当前用户 `role` 和 `miniAdminEnabled`，其中司机端为 `role=driver`，已开放小程序管理权限为 `role=manager`。
- 后端不得因为 `miniAdminEnabled=true` 放开租户全量数据；管理页内所有设备、订单、报表、导出仍按当前 App 用户授权范围查询。
# 2026-06-16 安科瑞 EIOT Worker 推送接入补充

- Cloudflare Worker 必须开放 `POST /eiot/meter` 和 `POST /eiot/alarm`，也允许 `POST /eiot` 按报文结构自动识别：数组为仪表实时数据，对象且包含 `list[]` 为报警消息。
- 安科瑞 EIOT 标准中 HTTP 推送地址由接收方提供，要求 `method=POST`、`Content-Type=application/json`；本项目同时兼容历史设计入口 `POST /infra-api/energy/eiot/realtime` 和 `POST /infra-api/energy/eiot/alarm`。
- EIOT 推送接口收到请求后必须立即返回 HTTP 200；R2 归档、D1 写入、设备状态刷新和同步日志写入必须放入后台任务处理，任何写入失败不得影响 EIOT 本次 HTTP 响应。
- 实时报文必须遍历数组逐条写入 `energy_telemetry`，高频查询字段继续单独保存 `gatewaySn`、`meterSn`、`meterNo`、`timestamp`、`state`、`Pa/Pb/Pc/P`、`Ua/Ub/Uc`、`Ia/Ib/Ic`、`PF`、`EPI`，完整单条报文同步保存到 `dataJson`。
- EIOT 入库的 `collect_time/occur_time/last_reading_time` 必须统一为业务本地时间文本（中国时区，格式 `YYYY-MM-DD HH:mm:ss`）：优先使用 EIOT `createTime/CreateTime`，缺失时用 Unix `timestamp` 转为中国本地时间，最后才使用当前中国本地时间兜底。分时计费、在线状态和报表筛选都以该业务本地时间口径为准，不得混用 UTC 文本。
- EIOT 遥测可能乱序到达。每条遥测入库后必须按该设备重新计算当前采集点前后相邻两段 `energy_telemetry_interval`，不能只按“入库时查到的上一条”生成区间；旧报文晚到时也不得覆盖设备最新采集时间和最新遥测值。
- EIOT 首次接收到未知电表并自动创建设备时，`device_type` 必须使用系统字典中已定义的电表类型值 `2`，不得写入前端无法翻译的临时枚举值。
- 设备在线状态刷新不得在每次列表查询时全表更新；应限定在当前查询范围或当前页设备 ID 内，并依赖 EIOT 最新报文实时维护单台设备状态。
- 报警报文必须遍历 `list[]` 写入 `energy_alarm`，保留 `alarmNo`、`code`、`level`、中文标题、中文内容、发生时间、`gatewaySn`、`meterSn`、`meterNo`、`timestamp` 和 `dataJson`。
- 原始完整请求体必须归档到 R2，key 使用 `eiot/{type}/{timestamp}-{gatewaySn}-{uuid}.json`，D1 只保存 `payloadUrl`。
- 设备匹配必须优先使用 `meterNo`，其次使用 `gatewaySn + meterSn`，不得把 `meterNo` 按下划线拆分。

## 2026-06-16 设备台账关联字段补充

- `GET /admin-api/energy/device/page` 和 `GET /admin-api/energy/device/simple-list` 必须关联 `energy_customer`、`energy_project` 返回 `customerName`、`projectName`。
- 首页运行负载、设备管理列表、数据面板电表选择器都以设备接口返回的关联名称为准，不得只返回 `customerId/projectId` 后让前端二次查询。
- 设备分页过滤继续支持 `deviceName`、`deviceNo`、`deviceType`、`gatewaySn`、`meterSn`、`meterNo`、`status`，简表接口至少支持 `customerId`、`projectId`、`status` 过滤。
- 设备标准 CRUD 字段必须包含 `runMode`，允许管理端维护运行模式；运行模式仍可由 EIOT 最新报文或控制流程刷新，但不能阻止后台人工维护。

## 2026-06-16 计费规则范围匹配补充

- `GET /admin-api/energy/pricing-rule/page` 必须关联返回 `customerName`、`projectName`、`deviceName`、`deviceNo`，计费规则列表不得只显示范围 ID 或 `-`。
- 计费规则新增/修改时必须保证 `customerId`、`projectId`、`deviceId` 三者只能有一个有效值；切换范围保存时必须清空旧范围字段。
- `GET /admin-api/energy/pricing-rule/match` 必须先读取设备所属客户和项目，再按设备级 > 项目级 > 客户级匹配启用且在生效期内的规则。
- 数据面板、充放电任务和计费试算使用项目场站汇总时，必须能命中项目级计费规则，不允许只匹配设备级规则。
## 2026-06-18 安全补充标准：客户账号后端隔离

- 所有密码创建、重置、修改接口必须在 Worker 后端调用统一密码强度校验，要求 8-32 位且同时包含字母和数字；前端校验只做交互提示，不能作为安全边界。
- 客户老板账号的数据权限必须在 Worker 后端强制执行，不能只依赖菜单隐藏或前端路由隐藏。
- `/admin-api/energy/customer/**`、`/admin-api/energy/project/**`、`/admin-api/energy/device/**`、`/admin-api/energy/vehicle/**`、`/admin-api/energy/account-event/**`、`/admin-api/energy/user-scope/**`、`/admin-api/energy/pricing-rule/**`、`/admin-api/energy/charge-session/**` 的列表、详情和精简列表接口必须按当前客户账号绑定的 `customer_id` 过滤。
- `/admin-api/energy/alarm/ack` 和 `/admin-api/energy/alarm/close` 必须复用告警列表同一客户归属表达式做后端过滤；普通告警 CRUD 写操作不得对客户账号开放，避免绕过页面权限操作其他客户告警。
- 当业务记录未直接保存 `customer_id` 时，必须通过项目或设备反查客户归属，例如 `COALESCE(record.customer_id, project.customer_id, device.customer_id, device_project.customer_id)`，避免客户账号通过直接请求 ID 越权读取其他客户数据。
- 客户老板账号默认只允许查询、导出自己客户范围内的数据；客户资料、项目、设备、车辆、扫码刷卡记录、用户授权、计费规则、充放电任务、客户账号权限等维护类写操作必须由平台管理员执行。
- `/admin-api/system/auth/refresh-token` 必须重新校验 `system_user.status = 0`；用户被禁用或删除后必须撤销对应 session，禁止继续签发新的 access token。
- `system_session` 必须有过期清理触发点；登录、刷新或其他高频安全入口应执行 `expires_time < now` 的批量清理，避免过期 session 长期堆积。

## 2026-06-18 报表费用明细标准补充

- 报表接口默认账单月份必须使用业务本地时区（Asia/Shanghai）的当前月份；不得使用 UTC 时间直接截取月份，避免北京时间月初 00:00-07:59 默认到上个月。
- 报表面板“电费明细”的“零售交易电费”必须使用计费规则中的 `sharpPeakRate`、`peakRate`、`flatRate`、`valleyRate`、`deepValleyRate` 分时电价，不得用固定基础电价替代。
- 同一报表范围内存在未匹配计费规则的场地或设备电量时，不得把这部分电量混入已匹配规则的费用行；电费明细主表只展示已匹配计费规则且可计费的费用明细，未匹配规则的场地或设备必须通过独立的“未匹配计费规则电量”数据返回并展示电量。
- 费用明细的计费标准应按已匹配规则电量加权计算；没有本期电量但需要展示固定分时时段时，可展示当前范围内已匹配规则的平均标准。
- 计费规则 `effectiveEnd` 如果保存为日期当天 `00:00:00` 或仅日期格式，规则匹配时必须按当天 `23:59:59` 处理，避免运营人员选择“结束日期”后当天整日被误判为过期。
- 报表电费明细中的电量型费用类别必须按固定分时时段展示，至少包含尖、峰、平、谷、深谷；市场化购电费、上网环节线损费用、输配电量电费、系统运行费用、政府性基金及附加不得只输出本期有电量的时段。

## 2026-06-22 首页按日费用趋势接口

- `/admin-api/energy/report/daily-cost` 必须按 `scopeType=all|project|device`、`projectId`、`deviceId`、`billMonth` 返回当前账号有权查看范围内的每日费用统计；客户账号权限过滤必须与 `/energy/report/bill` 保持一致。
- 每日 `chargeCost`、`salesRevenue`、`savedCost` 必须复用账单接口同一套 EPI/EPE、分时电量、计费规则匹配和分时电价计算逻辑；不得在首页前端根据遥测曲线二次估算费用。
- 接口返回的 `rows` 至少包含 `date`、`totalChargeEnergy`、`totalDischargeEnergy`、`chargeCost`、`salesRevenue`、`savedCost`、`deviceCount`、`unmatchedPricingCount`，用于首页趋势图和后续按日费用表格。
