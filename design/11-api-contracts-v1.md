# 首版接口契约

本文档定义首版需要优先实现的接口字段。字段命名以 RuoYi 后端 Java DTO 和前端 JSON 使用为准。

## 管理端设备分页

落地状态：已实现第一版后端接口，OpenAPI 已确认注册。

```text
GET /admin-api/energy/device/page
```

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNo | integer | 是 | 页码 |
| pageSize | integer | 是 | 每页条数 |
| deviceName | string | 否 | 设备名称 |
| deviceNo | string | 否 | 设备编码 |
| deviceType | integer | 否 | 设备类型 |
| gatewaySn | string | 否 | 网关序列号 |
| customerId | long | 否 | 客户编号 |
| projectId | long | 否 | 项目编号 |
| status | integer | 否 | 设备状态 |

响应 `data.list[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 设备编号 |
| deviceNo | string | 设备编码 |
| deviceName | string | 设备名称 |
| deviceType | integer | 设备类型 |
| gatewaySn | string | 网关序列号 |
| meterSn | string | 电表序列号 |
| meterNo | string | 仪表编号 |
| customerId | long | 客户编号 |
| customerName | string | 客户名称 |
| projectId | long | 项目编号 |
| projectName | string | 项目名称 |
| status | integer | 设备状态 |
| runMode | integer | 运行模式 |
| lastSoc | number | 最新 SOC |
| lastSoh | number | 最新 SOH |
| lastPower | number | 最新功率 |
| lastVoltage | number | 最新电压 |
| lastCurrent | number | 最新电流 |
| lastTemp | number | 最新温度 |
| lastReadingTime | string | 最新采集时间 |

## 管理端设备详情

```text
GET /admin-api/energy/device/get?id={id}
```

响应 `data`：

```json
{
  "id": 1900001,
  "deviceNo": "ESS-001",
  "deviceName": "移动储能车 001",
  "deviceType": 0,
  "gatewaySn": "GW001",
  "meterSn": "METER001",
  "meterNo": "100001",
  "customerId": 1900101,
  "customerName": "示例客户",
  "projectId": 1900201,
  "projectName": "示例场站",
  "status": 0,
  "runMode": 1,
  "latitude": 31.230416,
  "longitude": 121.473701,
  "lastSoc": 82.5,
  "lastSoh": 96.1,
  "lastPower": 35.2,
  "lastVoltage": 752.3,
  "lastCurrent": 46.8,
  "lastTemp": 31.6,
  "lastReadingTime": "2026-05-29 10:30:00"
}
```

说明：`customerName`、`projectName` 已通过客户/项目 simple-list 聚合补充，设备分页和详情接口均返回编号与名称，便于管理端列表直接展示。

## 管理端设备新增/修改/删除

```text
POST /admin-api/energy/device/create
PUT /admin-api/energy/device/update
DELETE /admin-api/energy/device/delete?id={id}
```

新增/修改请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 修改必填 | 设备编号 |
| deviceNo | string | 是 | 设备编码，租户内唯一 |
| deviceName | string | 是 | 设备名称 |
| deviceType | integer | 是 | 设备类型 |
| gatewaySn | string | 否 | 网关序列号 |
| meterSn | string | 否 | 电表序列号 |
| meterNo | string | 否 | 仪表编号，填写时租户内唯一 |
| customerId | long | 否 | 客户编号 |
| projectId | long | 否 | 项目编号 |
| status | integer | 是 | 设备状态 |
| runMode | integer | 否 | 运行模式 |
| latitude | number | 否 | 纬度 |
| longitude | number | 否 | 经度 |
| remark | string | 否 | 备注 |

## 管理端设备精简列表

落地状态：已实现，用于管理端下拉选择。

```text
GET /admin-api/energy/device/simple-list
```

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| customerId | long | 否 | 客户编号 |
| projectId | long | 否 | 项目编号 |

响应 `data[]`：字段同设备详情，主要使用 `id`、`deviceName`、`deviceNo`。

## 管理端用户授权

落地状态：已实现第一版后端接口和 Vue3 管理端页面，菜单为“移动储能 / 用户授权”。

```text
GET    /admin-api/energy/user-scope/page
GET    /admin-api/energy/user-scope/get?id={id}
POST   /admin-api/energy/user-scope/create
PUT    /admin-api/energy/user-scope/update
DELETE /admin-api/energy/user-scope/delete?id={id}
```

分页请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNo | integer | 是 | 页码 |
| pageSize | integer | 是 | 每页条数 |
| userId | long | 否 | App 用户编号 |
| userType | integer | 否 | 用户类型，第一版固定使用 1 |
| customerId | long | 否 | 客户授权过滤 |
| projectId | long | 否 | 项目授权过滤 |
| deviceId | long | 否 | 设备授权过滤 |
| status | integer | 否 | 状态：0 启用，1 禁用 |

新增/修改请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 修改必填 | 授权编号 |
| userId | long | 是 | App 用户编号 |
| userType | integer | 是 | 用户类型，第一版固定使用 1 |
| customerId | long | 三选一 | 授权客户 |
| projectId | long | 三选一 | 授权项目 |
| deviceId | long | 三选一 | 授权设备 |
| status | integer | 是 | 状态：0 启用，1 禁用 |
| remark | string | 否 | 备注 |

处理规则：

- `customerId`、`projectId`、`deviceId` 必须且只能填写一个。
- 同一 `userType + userId + 授权范围` 不允许重复。
- App 接口只读取 `status = 0` 的授权记录。

## 管理端告警分页

落地状态：已实现第一版后端接口和 Vue3 管理端页面，OpenAPI 已确认注册。

```text
GET /admin-api/energy/alarm/page
```

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNo | integer | 是 | 页码 |
| pageSize | integer | 是 | 每页条数 |
| deviceId | long | 否 | 设备编号 |
| level | integer | 否 | 告警等级 |
| status | integer | 否 | 告警状态 |
| code | string | 否 | 告警代码 |
| title | string | 否 | 告警标题 |
| occurTime | array | 否 | 发生时间范围 |

响应 `data.list[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 告警编号 |
| alarmNo | string | 告警业务编号 |
| deviceId | long | 设备编号 |
| deviceName | string | 设备名称 |
| code | string | 告警代码 |
| level | integer | 告警等级 |
| title | string | 告警标题 |
| content | string | 告警内容 |
| status | integer | 告警状态 |
| occurTime | string | 发生时间 |
| ackUserId | long | 确认人 |
| ackTime | string | 确认时间 |
| closeTime | string | 关闭时间 |

## 管理端告警详情

```text
GET /admin-api/energy/alarm/get?id={id}
```

响应 `data`：字段同告警分页 `data.list[]`。

## 管理端告警确认

```text
PUT /admin-api/energy/alarm/ack
```

请求体：

```json
{
  "id": 1900001,
  "remark": "现场已确认，持续观察"
}
```

处理规则：

- 只有未确认告警允许确认。
- 确认时写入当前后台用户和确认时间。
- 写操作日志。

## 管理端告警关闭

```text
PUT /admin-api/energy/alarm/close
```

请求体：

```json
{
  "id": 1900001,
  "remark": "现场处理完成"
}
```

处理规则：

- 已关闭告警不允许重复关闭。
- 关闭时写入关闭时间，状态改为已关闭。
- `remark` 写入 `energy_alarm_handle_record`，用于追溯确认/关闭处理过程。

## 小程序账号密码登录（本地兼容）

```text
POST /app-api/energy/auth/login
```

落地状态：已实现但默认关闭。正式客户版小程序不展示该入口；如本地联调确需使用，必须显式配置 `energy.app.password-login-enabled=true`。正式入口见 `POST /app-api/energy/auth/wechat-login`，首次微信登录会自动创建 `energy_app_user`。

请求头：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| tenant-id | 是 | 当前本地默认为 `1` |

请求体：

```json
{
  "username": "wx_openid_user",
  "password": "temporary-password"
}
```

响应 `data`：

```json
{
  "userId": 202606010001,
  "accessToken": "RuoYi OAuth2 access token",
  "refreshToken": "RuoYi OAuth2 refresh token",
  "expiresTime": 1780283354170,
  "user": {
    "id": 202606010001,
    "username": "wx_openid_user",
    "displayName": "微信小程序用户",
    "mobile": "13800138000",
    "role": "app"
  }
}
```

小程序端存储 `accessToken`，后续业务请求统一发送 `Authorization: Bearer {accessToken}` 和 `tenant-id: 1`。

正式客户登录：

```text
POST /app-api/energy/auth/wechat-login
```

请求体：

```json
{
  "code": "wx.login 返回的临时 code"
}
```

## 小程序首页概览

```text
GET /app-api/energy/home/overview
```

落地状态：已实现第一版后端接口，按 `energy_user_scope` 授权范围返回登录 App 用户可访问的客户/项目/设备数据；无授权记录时返回空汇总。

响应 `data`：

```json
{
  "deviceCount": 12,
  "onlineCount": 10,
  "faultCount": 1,
  "unackedAlarmCount": 3,
  "todayChargeEnergy": 120.5,
  "todayDischargeEnergy": 86.3,
  "currentPower": 35.2
}
```

说明：
- `deviceCount`、`onlineCount`、`faultCount` 来自 `energy_device`。
- `unackedAlarmCount` 来自 `energy_alarm.status = 0`。
- `currentPower` 为当前租户设备 `lastPower` 汇总。
- `todayChargeEnergy`、`todayDischargeEnergy` 第一版返回 `0`，待充放电记录/计费表落地后改为真实统计。

## 小程序设备列表

```text
GET /app-api/energy/device/list
```

落地状态：已实现第一版后端接口，按 `energy_user_scope` 授权范围返回登录 App 用户可访问的设备，最多返回 100 条；无授权记录时返回空列表。

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| status | integer | 否 | 设备状态 |
| keyword | string | 否 | 设备名称/编号 |

响应 `data[]`：

```json
[
  {
    "id": 1900001,
    "deviceName": "移动储能车 001",
    "deviceNo": "ESS-001",
    "status": 0,
    "runMode": 1,
    "lastSoc": 82.5,
    "lastPower": 35.2,
    "lastTemp": 31.6,
    "lastReadingTime": "2026-05-29 10:30:00"
  }
]
```

## 小程序设备趋势

```text
GET /app-api/energy/telemetry/trend?deviceId={id}&range=24h
```

响应 `data`：

```json
{
  "times": ["10:00", "10:05", "10:10"],
  "soc": [82.1, 82.3, 82.5],
  "power": [33.8, 34.6, 35.2],
  "temperature": [31.2, 31.4, 31.6]
}
```

## 小程序首页兼容统计接口

```text
GET /app-api/energy/devices/energy-delta
GET /app-api/energy/charging-sessions
GET /app-api/energy/charging-sessions/revenue-overview
GET /app-api/energy/charging-sessions/today-energy
```

落地状态：已实现兼容版。当前用于消除小程序旧首页请求报错，返回授权设备范围内的空充放电/收益统计；真实充放电会话、收益、能量差额将在 `energy_charge_session` 业务逻辑落地后替换为数据库统计。

当前约定：

- `energy-delta.summary.totalCharge`、`totalDischarge`、`totalNet` 暂返回 `0`。
- `charging-sessions` 暂返回空数组。
- `revenue-overview.todayIncome`、`monthIncome`、`activeSessions`、`rentalRate` 暂返回 `0`。
- `today-energy.todayCharge`、`todayDischarge` 暂返回 `0`。

## EIOT 实时数据接入

```text
POST /infra-api/energy/eiot/realtime
```

请求头：

| 字段 | 说明 |
| --- | --- |
| tenant-id | 可选；未传时 EIOT 接入口默认使用本地租户 `1`，正式接入可由签名或接入配置映射租户 |
| Authorization | `Bearer {token}` |
| X-EIOT-Signature | 可选签名 |

请求体：

```json
{
  "gatewaySn": "12207013690004",
  "meterSn": "12207013690004",
  "meterNo": "12207013690004_12207013690004",
  "createTime": "2022-07-08 16:55:00",
  "timestamp": 1722441615,
  "source": "REALTIME",
  "state": "ONLINE",
  "Pa": 0,
  "Pb": 0,
  "Pc": 0,
  "Ua": 99.9,
  "Ub": 100,
  "Uc": 99.9,
  "Ia": 0,
  "Ib": 0,
  "Ic": 0,
  "P": 0,
  "PF": 1,
  "EPI": 0.9
}
```

响应：

```json
{
  "code": 0,
  "data": {
    "received": 1,
    "updated": 1,
    "ignored": 0
  },
  "msg": ""
}
```

处理规则：

- 当前 EIOT 只能提供电表实时数据，不提供 SOC/SOH/电池温度等储能 BMS 字段。
- `requestId` 由后端按 `realtime_{meterNo}_{timestamp/createTime}` 生成，用于同步日志追踪。
- 优先按 `meterNo` 匹配设备；匹配不到时按 `gatewaySn + meterSn` 匹配。
- 写入 `energy_telemetry`，保留 `gatewaySn`、`meterSn`、`meterNo`、`timestamp`、`source`、`state`、`Pa/Pb/Pc`、`Ua/Ub/Uc`、`Ia/Ib/Ic`、`P`、`PF`、`EPI`。
- 更新 `energy_device` 最新状态、总有功功率、三相平均电压、三相平均电流和采集时间。
- `state=ONLINE` 映射设备在线，`state=OFFLINE` 映射设备离线。
- 写入 `energy_eiot_sync_log`。
- 充放电结算当前可使用 `EPI` 作为正向电能快照；反向电能 `EPE` 暂无来源时不能计算放电电量。

## EIOT 告警接入

```text
POST /infra-api/energy/eiot/alarm
```

请求头：

| 字段 | 说明 |
| --- | --- |
| tenant-id | 租户编号，必填 |
| X-EIOT-Token | 必填；必须与后端 `energy.eiot.inbound-token` 配置一致 |

请求体：

```json
{
  "gatewaySn": "20241204870083",
  "meterSn": "20241204870083",
  "projectCode": "2101",
  "projectName": "XX项目",
  "createTime": "2024-08-01 00:00:15",
  "timestamp": 1722441615,
  "list": [
    {
      "code": "PFLOW1",
      "level": "1",
      "alarmNo": "983168117022773248",
      "title": {
        "en_US": "PF Low Warning",
        "zh_CN": "总功率因数低限预警"
      },
      "message": {
        "en_US": "PF Low Warning,settingValue：0.9,currentValue：0.791",
        "zh_CN": "总功率因数低限预警,设定值：0.9，当前值：0.791"
      }
    }
  ]
}
```

响应：

```json
{
  "code": 0,
  "data": {
    "received": 1,
    "created": 1,
    "ignored": 0,
    "failed": 0
  },
  "msg": ""
}
```

处理规则：

- `alarmNo` 用于告警幂等。
- 优先按告警项 `meterNo` 匹配设备；未提供时使用 `gatewaySn + '_' + meterSn` 推导仪表唯一编号；仍未匹配时再按 `gatewaySn + meterSn` 匹配设备台账。
- `title` / `message` 优先取 `zh_CN`，无中文时取 `en_US` 或首个可用文本。
- EIOT 原始字段 `list` 已兼容；历史字段 `alarms` 仍保留兼容。
- `occurTime` 未提供时使用根部 `createTime`。
- 未匹配到设备时写同步失败日志。
- 新告警默认状态为未确认。
- 同一 `alarmNo` 重复推送不重复入库。
- EIOT 告警接入写入 `energy_eiot_sync_log`，成功、重复忽略、失败均记录。
- 时间必须使用 `yyyy-MM-dd HH:mm:ss` 字符串格式；后端按该格式显式解析，解析失败按单条告警失败处理并写同步失败日志。

## 管理端实时采集数据

```text
GET /admin-api/energy/telemetry/page
GET /admin-api/energy/telemetry/chart
GET /admin-api/energy/telemetry/daily-stat
```

权限标识：

```text
energy:telemetry:query
```

查询参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNo | number | 页码 |
| pageSize | number | 每页条数 |
| deviceId | long | 设备编号 |
| gatewaySn | string | 网关序列号 |
| meterSn | string | 仪表序列号 |
| meterNo | string | 仪表全平台编号 |
| source | string | 数据类型：`REALTIME` / `HISTORY` |
| state | string | 仪表状态：`ONLINE` / `OFFLINE` |
| collectTime | string[] | 采集时间范围，格式 `yyyy-MM-dd HH:mm:ss` |

响应数据：

| 字段 | 说明 |
| --- | --- |
| id | 采集数据编号 |
| deviceId / deviceName / deviceNo | 设备编号、名称、编码 |
| gatewaySn / meterSn / meterNo | 网关、仪表和仪表全平台编号 |
| collectTime / timestamp | 设备本地采集时间和 Unix 时间戳 |
| source / state | 数据类型和仪表状态 |
| pa / pb / pc | A/B/C 相有功功率 |
| ua / ub / uc | A/B/C 相电压 |
| ia / ib / ic | A/B/C 相电流 |
| p / pf / epi | 总有功功率、总功率因数、正向总有功电能 |

### 曲线原始数据

```text
GET /admin-api/energy/telemetry/chart
```

用途：管理端“数据查看-日原始数据”和“详细实时数据”复用该接口获取指定电表、指定时间范围内的原始采集点，后端按 `collectTime` 升序返回，默认限制 2000 条，最大限制 5000 条。

查询参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| deviceId | long | 设备编号 |
| gatewaySn | string | 网关序列号 |
| meterSn | string | 仪表序列号 |
| meterNo | string | 仪表全平台编号 |
| source | string | 数据类型：`REALTIME` / `HISTORY` |
| state | string | 仪表状态：`ONLINE` / `OFFLINE` |
| collectTime | string[] | 采集时间范围，格式 `yyyy-MM-dd HH:mm:ss` |
| limit | number | 返回条数限制，默认 2000，最大 5000 |

响应 `data[]` 字段同 `/page` 的 `data.list[]`。

### 逐日极值数据

```text
GET /admin-api/energy/telemetry/daily-stat
```

用途：管理端“数据查看-逐日极值数据”按选定电表、时间范围和单个指标计算每日最大值、最小值、发生时间和平均值。

查询参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| deviceId | long | 否 | 设备编号 |
| gatewaySn | string | 否 | 网关序列号 |
| meterNo | string | 否 | 仪表全平台编号 |
| collectTime | string[] | 是 | 采集时间范围，格式 `yyyy-MM-dd HH:mm:ss` |
| metric | string | 是 | 统计指标：`pa`/`pb`/`pc`/`p`/`ua`/`ub`/`uc`/`ia`/`ib`/`ic`/`pf`/`epi` |

响应 `data[]`：

| 字段 | 说明 |
| --- | --- |
| date | 统计日期 |
| max | 当日最大值 |
| maxTime | 当日最大值发生时间 |
| min | 当日最小值 |
| minTime | 当日最小值发生时间 |
| avg | 当日平均值，保留 4 位小数 |

说明：当前 EIOT 报文没有线电压、频率、无功功率、视在功率、温度、开关量等字段，第一版接口不把这些指标作为可查询真实数据。

## 管理端 EIOT 同步日志

```text
GET /admin-api/energy/eiot-log/page
```

权限标识：

```text
energy:eiot-log:query
```

查询参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNo | number | 页码 |
| pageSize | number | 每页条数 |
| syncType | string | 同步类型：`alarm` / `realtime` |
| status | number | 状态：0 成功，1 失败 |
| requestId | string | 请求编号，支持模糊查询 |
| gatewaySn | string | 网关序列号，支持模糊查询 |
| meterSn | string | 电表序列号，支持模糊查询 |
| createTime | string[] | 创建时间范围，格式 `yyyy-MM-dd HH:mm:ss` |

响应数据：

| 字段 | 说明 |
| --- | --- |
| id | 同步日志编号 |
| syncType | 同步类型 |
| requestId | 请求编号 |
| gatewaySn | 网关序列号 |
| meterSn | 电表序列号 |
| payloadUrl | 原始报文归档地址 |
| status | 状态：0 成功，1 失败 |
| errorMsg | 失败原因或重复忽略说明 |
| createTime | 创建时间 |

## 管理端计费规则

落地状态：已实现第一版后端接口和 Vue3 管理端页面，本地接口验证通过。

```text
GET    /admin-api/energy/pricing-rule/page
GET    /admin-api/energy/pricing-rule/get?id={id}
GET    /admin-api/energy/pricing-rule/match?deviceId={deviceId}&billingTime={yyyy-MM-dd HH:mm:ss}
POST   /admin-api/energy/pricing-rule/create
PUT    /admin-api/energy/pricing-rule/update
DELETE /admin-api/energy/pricing-rule/delete?id={id}
```

权限标识：

```text
energy:pricing-rule:query
energy:pricing-rule:create
energy:pricing-rule:update
energy:pricing-rule:delete
```

查询参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNo | number | 页码 |
| pageSize | number | 每页条数 |
| customerId | long | 客户编号 |
| projectId | long | 项目编号 |
| deviceId | long | 设备编号 |
| status | number | 状态：0 启用，1 停用 |
| effectiveStart | string[] | 生效开始时间范围，格式 `yyyy-MM-dd HH:mm:ss` |

匹配查询参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| deviceId | long | 是 | 设备编号 |
| billingTime | string | 否 | 计费时间，格式 `yyyy-MM-dd HH:mm:ss`；不传按当前时间匹配 |

新增/修改请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 修改必填 | 计费规则编号 |
| customerId | long | 三选一 | 客户级计费范围 |
| projectId | long | 三选一 | 项目级计费范围 |
| deviceId | long | 三选一 | 设备级计费范围 |
| timeRate | number | 是 | 时间单价，不能小于 0 |
| energyRate | number | 是 | 电量单价，不能小于 0 |
| effectiveStart | string | 是 | 生效开始时间，格式 `yyyy-MM-dd HH:mm:ss` |
| effectiveEnd | string | 否 | 生效结束时间，格式 `yyyy-MM-dd HH:mm:ss`；不填表示长期有效 |
| status | number | 是 | 状态：0 启用，1 停用 |
| remark | string | 否 | 备注 |

响应数据：

| 字段 | 说明 |
| --- | --- |
| id | 计费规则编号 |
| customerId / customerName | 客户范围编号和名称 |
| projectId / projectName | 项目范围编号和名称 |
| deviceId / deviceName / deviceNo | 设备范围编号、名称和编码 |
| timeRate | 时间单价 |
| energyRate | 电量单价 |
| effectiveStart | 生效开始时间 |
| effectiveEnd | 生效结束时间 |
| status | 状态 |
| remark | 备注 |
| createTime | 创建时间 |

处理规则：

- 每条计费规则必须且只能绑定一个计费范围：客户、项目或设备。
- 后端保存前必须校验绑定对象存在。
- `effectiveEnd` 不得早于 `effectiveStart`。
- 保存接口的时间字段使用字符串接收，并在 Service 层按 `yyyy-MM-dd HH:mm:ss` 显式解析。
- 匹配接口用于充放电会话结算前查找生效规则，优先级固定为：设备级 > 项目级 > 客户级。
- 匹配时仅返回 `status = 0`、`effectiveStart <= billingTime` 且 `effectiveEnd` 为空或 `effectiveEnd >= billingTime` 的规则；同一范围多条命中时取生效开始时间最新、编号最新的一条。

## 管理端设备操控

落地状态：待实现。

```text
POST /admin-api/energy/device/control
```

权限标识：`energy:device:control`

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| deviceId | long | 是 | 设备编号 |
| method | string | 是 | 控制方法：SWITCH / REFRESH / FORCESWITCH / RESET / SELCHK |
| value | object | 是 | 方法参数，结构因 method 而异 |
| sessionId | long | 否 | 关联会话编号 |

`SWITCH` 的 value：

```json
{ "Switch": "1" }
```

`Switch = "1"` 合闸，`"0"` 分闸。

`FORCESWITCH` 的 value：

```json
{ "ForceSwitch": "1" }
```

`REFRESH` 的 value：

```json
{}
```

处理规则：

- 调用 EIOT `/basic/prepayment/entry/home/control` 接口。
- 全量写入 `energy_device_control_log`。
- Token 过期时自动刷新并重试一次。
- 平台内部错误指数退避重试，最多 3 次。
- FORCESWITCH 和 RESET 仅管理端可用。

## 管理端操控日志

```text
GET /admin-api/energy/device/control-log/page
```

权限标识：`energy:device:control-log:query`

查询参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNo | integer | 是 | 页码 |
| pageSize | integer | 是 | 每页条数 |
| deviceId | long | 否 | 设备编号 |
| method | string | 否 | 控制方法 |
| status | integer | 否 | 状态：0 成功，1 失败 |
| operateTime | array | 否 | 操作时间范围 |

响应 `data.list[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 日志编号 |
| deviceId | long | 设备编号 |
| deviceName | string | 设备名称 |
| sessionId | long | 关联会话编号 |
| method | string | 控制方法 |
| requestBody | string | 请求体 |
| responseBody | string | 响应体 |
| eiotSuccess | integer | EIOT 返回 success |
| eiotErrorCode | string | EIOT 返回 errorCode |
| status | integer | 状态 |
| errorMsg | string | 失败原因 |
| operatorId | long | 操作人 |
| operateTime | string | 操作时间 |

## 充放电会话启停

落地状态：已实现第一版管理端接口和 Vue3 页面。第一版支持分页、开始、结束、结算；暂不包含 pause/resume，暂未对接 EIOT SWITCH 实控。

```text
GET  /admin-api/energy/charge-session/page
GET  /admin-api/energy/charge-session/get?id={id}
POST /admin-api/energy/charge-session/start
POST /admin-api/energy/charge-session/stop
POST /admin-api/energy/charge-session/settle
```

### start 开始充放电

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| deviceId | long | 是 | 设备编号 |
| sessionType | integer | 是 | 0 充电，1 放电 |
| pricingRuleId | long | 否 | 计费规则，不传按客户/项目默认规则查找 |

处理规则：

- 校验设备无进行中的会话。
- 匹配计费规则；显式传 `pricingRuleId` 时使用指定规则，不传时复用 `getMatchedPricingRule` 按设备/项目/客户优先级匹配。
- 从 `energy_telemetry` 最新记录快照 `start_energy`，第一版取 EIOT `EPI`。
- 会话状态变为进行中。
- `customer_id` 从 `energy_device` 自动关联。
- EIOT SWITCH 合闸待设备控制服务落地后接入。

响应 `data`：

```json
{
  "id": 1900501,
  "sessionNo": "SES202606010001",
  "status": 0,
  "startTime": "2026-06-01 14:30:00",
  "startEnergy": 12345.6
}
```

### stop 结束充放电

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| sessionId | long | 是 | 会话编号 |
| endEnergy | number | 否 | 结束电能读数，不传自动取最新 EPI |

处理规则：

- 未传 `endEnergy` 时从 `energy_telemetry` 最新记录自动取 `EPI`。
- `total_energy` = `endEnergy - startEnergy`，结束读数不能小于开始读数。
- `duration_minutes` = `endTime - startTime`。
- `energy_fee` = `total_energy × energy_rate`，`time_fee` = `duration_hours × time_rate`。
- 会话状态变为已结束。
- EIOT SWITCH 分闸待设备控制服务落地后接入。

响应 `data`：

```json
{
  "id": 1900501,
  "status": 1,
  "endTime": "2026-06-01 16:30:00",
  "totalEnergy": 4.6,
  "durationMinutes": 120,
  "energyFee": 4.14,
  "timeFee": 0.00,
  "totalFee": 4.14
}
```

### settle 结算

请求体：

```json
{ "sessionId": 1900501 }
```

处理规则：

- 只有已结束或异常状态可结算。
- 结算后状态变为已结算，不可再修改。
- 后续账单表落地后，结算动作需要同步写入结算记录。

说明：当前 EIOT 真实报文只有 `EPI`，第一版使用 `start_energy/end_energy` 保存通用电能读数。若需要严格区分放电电量，必须确认 EIOT 是否提供 `EPE` 或 PCS/BMS 放电电能字段，再扩展为正反向电能字段。

## 小程序设备操控

```text
POST /app-api/energy/device/control
```

第一版限制：

- 仅允许 SWITCH（正常分合闸），FORCESWITCH / RESET 仅管理端可用。
- 按 `energy_user_scope` 校验设备操作权限。
- 请求体和响应同管理端。

## EIOT 凭证管理

```text
GET    /admin-api/energy/eiot-credential/page
GET    /admin-api/energy/eiot-credential/get?id={id}
POST   /admin-api/energy/eiot-credential/create
PUT    /admin-api/energy/eiot-credential/update
DELETE /admin-api/energy/eiot-credential/delete?id={id}
```

权限标识：`energy:eiot-credential:*`

新增/修改请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 修改必填 | 凭证编号 |
| platformName | string | 是 | EIOT 平台名称 |
| baseUrl | string | 是 | EIOT 基础地址 |
| loginName | string | 是 | 登录账号 |
| loginPassword | string | 是 | 登录密码 |
| status | integer | 是 | 状态：0 启用，1 禁用 |

响应数据包含 `currentToken` 和 `tokenExpireTime` 但不返回 `loginPassword`。
## 2026-06-12 小程序只读兼容接口与安全约束

### EIOT 入站鉴权

`POST /infra-api/energy/eiot/realtime`、`POST /infra-api/energy/eiot/alarm`、`POST /infra-api/energy/eiot/card/verify` 与 `POST /infra-api/energy/eiot/card/discharge` 必须携带：

| Header | 说明 |
| --- | --- |
| X-EIOT-Token | 与后端 `energy.eiot.inbound-token` 配置一致 |

未配置 token 或请求 token 不匹配时返回 403，不写入实时数据、报警或同步日志。

### 设备侧刷卡接口

```text
POST /infra-api/energy/eiot/card/verify
POST /infra-api/energy/eiot/card/discharge
```

请求体：

```json
{
  "scanText": "deviceNo/deviceId/meterNo 或带参数链接",
  "cardNo": "CARD-0001"
}
```

说明：设备侧刷卡接口不依赖小程序登录态，后端使用 `cardNo` 匹配启用的 App 用户，并按 `energy_user_scope` 校验设备、项目或客户授权；未知账户、停用账户、未授权账户均不得启动放电，并写入“扫码刷卡记录”。

### 小程序客户侧只读接口

以下接口均要求 App 登录 token，并按 `energy_user_scope` 过滤：

```text
GET /app-api/energy/devices/{id}
GET /app-api/energy/devices/{id}/latest
GET /app-api/energy/devices/{id}/readings?from=&to=&limit=
GET /app-api/energy/alarms?deviceId=&limit=
GET /app-api/energy/alarms/{id}
```

小程序客户侧不提供客户创建、计费规则维护、设备基础资料修改接口。账号密码登录默认关闭，客户正式入口使用 `POST /app-api/energy/auth/wechat-login`。

### 本地密钥配置

后端本地配置不直接保存微信小程序 AppSecret。启动 `yudao-server` 前应通过环境变量注入：

```powershell
$env:ENERGY_WECHAT_APP_ID="wxf43120ae9b42d6dd"
$env:ENERGY_WECHAT_APP_SECRET="<小程序 AppSecret>"
```

`wx.miniapp.secret` 为空时，微信 code 换 openid 会失败，不能用账号密码登录绕过客户侧正式流程。
