# 接口设计

## 接口分层

| 接口前缀 | 使用方 | 说明 |
| --- | --- | --- |
| `/admin-api/energy/**` | 管理端 | 后台管理接口 |
| `/app-api/energy/**` | 小程序 | 客户、司机、运维人员接口 |
| `/infra-api/energy/eiot/**` | EIOT worker | 数据接入和内部同步接口 |

## 管理端接口

### 设备

```text
GET    /admin-api/energy/device/page
GET    /admin-api/energy/device/get?id=
POST   /admin-api/energy/device/create
PUT    /admin-api/energy/device/update
DELETE /admin-api/energy/device/delete?id=
GET    /admin-api/energy/device/latest?id=
GET    /admin-api/energy/device/trend?deviceId=&from=&to=
```

### 告警

```text
GET  /admin-api/energy/alarm/page
GET  /admin-api/energy/alarm/get?id=
PUT  /admin-api/energy/alarm/ack
PUT  /admin-api/energy/alarm/close
GET  /admin-api/energy/alarm/statistics
```

### 客户和项目

```text
GET    /admin-api/energy/customer/page
POST   /admin-api/energy/customer/create
PUT    /admin-api/energy/customer/update
DELETE /admin-api/energy/customer/delete?id=

GET    /admin-api/energy/project/page
POST   /admin-api/energy/project/create
PUT    /admin-api/energy/project/update
DELETE /admin-api/energy/project/delete?id=
```

### 计费和会话

```text
GET    /admin-api/energy/pricing-rule/page
POST   /admin-api/energy/pricing-rule/create
PUT    /admin-api/energy/pricing-rule/update
DELETE /admin-api/energy/pricing-rule/delete?id=

GET    /admin-api/energy/session/page
GET    /admin-api/energy/session/get?id=
POST   /admin-api/energy/session/settle
GET    /admin-api/energy/session/export
```

### 调度任务

```text
GET  /admin-api/energy/dispatch/page
POST /admin-api/energy/dispatch/create
PUT  /admin-api/energy/dispatch/cancel
GET  /admin-api/energy/dispatch/get?id=
```

### 设备操控

```text
POST /admin-api/energy/device/control
GET  /admin-api/energy/device/control-log/page
```

### 充放电会话

```text
GET  /admin-api/energy/charge-session/page
GET  /admin-api/energy/charge-session/get?id=
POST /admin-api/energy/charge-session/start
POST /admin-api/energy/charge-session/stop
POST /admin-api/energy/charge-session/pause
POST /admin-api/energy/charge-session/resume
POST /admin-api/energy/charge-session/settle
GET  /admin-api/energy/charge-session/export
```

### 运营面板

```text
GET /admin-api/energy/dashboard/overview
GET /admin-api/energy/dashboard/device-status
GET /admin-api/energy/dashboard/alarm-trend
GET /admin-api/energy/dashboard/energy-trend
GET /admin-api/energy/dashboard/revenue-trend
```

## 小程序接口

### 首页

```text
GET /app-api/energy/home/overview
GET /app-api/energy/home/my-devices
GET /app-api/energy/home/recent-alarms
```

### 设备

```text
GET /app-api/energy/device/list
GET /app-api/energy/device/detail?id=
GET /app-api/energy/device/trend?deviceId=&range=
GET /app-api/energy/device/location?id=
```

### 告警

```text
GET /app-api/energy/alarm/list
GET /app-api/energy/alarm/detail?id=
PUT /app-api/energy/alarm/ack
```

### 设备操控

```text
POST /app-api/energy/device/control
```

说明：第一版小程序仅开放 SWITCH（正常启停），FORCESWITCH/RESET 仅管理端可用。按 `energy_user_scope` 校验设备操作权限。

### 记录和费用

```text
GET /app-api/energy/session/list
GET /app-api/energy/session/detail?id=
GET /app-api/energy/bill/monthly?month=
```

### 扫码放电

```text
POST /app-api/energy/charging-sessions/scan/verify
POST /app-api/energy/charging-sessions/scan/discharge
POST /infra-api/energy/eiot/card/verify
POST /infra-api/energy/eiot/card/discharge
```

说明：小程序扫码后先按设备/充电桩/仪表识别信息定位设备，例如 `deviceId`、`deviceNo`、`meterNo`、`gatewaySn + meterSn` 或包含这些参数的二维码链接；`energy_vehicle` 车辆档案仅作为旧二维码兼容。后端再校验当前微信登录账号或刷卡卡号是否已录入、已启用并拥有该设备/项目/客户权限；确认放电时后端再次校验并创建放电会话，复用充放电任务计费匹配和设备控制联动。设备侧刷卡使用 `/infra-api/energy/eiot/card/*`，请求头必须携带 `X-EIOT-Token`，不要求小程序登录态。每次扫码校验、开始放电和后续刷卡识别都写入 `energy_account_event`，供管理端“扫码刷卡记录”追溯。

## EIOT 接入接口

### 实时数据同步

```text
POST /infra-api/energy/eiot/realtime
```

请求示例：

```json
{
  "requestId": "20260529103000123",
  "gatewaySn": "GW001",
  "items": [
    {
      "meterSn": "METER001",
      "meterNo": "100001",
      "collectTime": "2026-05-29 10:30:00",
      "soc": 82.5,
      "soh": 96.1,
      "power": 35.2,
      "voltage": 752.3,
      "current": 46.8,
      "temperature": 31.6,
      "runMode": "DISCHARGING"
    }
  ]
}
```

### 告警同步

```text
POST /infra-api/energy/eiot/alarm
```

请求示例：

```json
{
  "requestId": "20260529103100123",
  "gatewaySn": "GW001",
  "alarms": [
    {
      "alarmNo": "ALM202605290001",
      "meterNo": "100001",
      "code": "BATTERY_TEMP_HIGH",
      "level": "MAJOR",
      "title": "电池温度过高",
      "content": "电池温度超过阈值",
      "occurTime": "2026-05-29 10:31:00"
    }
  ]
}
```

## 统一响应

遵循 RuoYi-Vue-Pro 现有响应格式：

```json
{
  "code": 0,
  "data": {},
  "msg": ""
}
```

## 鉴权建议

- 管理端：使用 RuoYi 后台登录令牌和权限标识
- 小程序：使用会员/用户登录令牌
- EIOT 入向接口：使用签名、固定 token、IP 白名单三层保护
- EIOT 出向调用：使用 EIOT 平台 Token（AES-128-CBC 加密登录获取），定时刷新

## EIOT 出向调用

本系统主动调用 EIOT 平台的接口（EIOT → 本系统为入向，本系统 → EIOT 为出向）。

### 认证登录

```text
POST http://{EIOT_IP}:{EIOT_PORT}/basic/prepayment/auth_user/login
Content-Type: application/x-www-form-urlencoded

params = AES-128-CBC({"LoginName":"admin","PassWord":"12345678"})
```

加密规范：AES/CBC/NoPadding，Key/IV 均为 `1234567890123456`，Base64 编码。

### 远程操控

```text
POST http://{EIOT_IP}:{EIOT_PORT}/basic/prepayment/entry/home/control
Content-Type: application/json
token: {EIOT_TOKEN}

{
  "gatewaySn": "SYZ21110520007",
  "meterSn": "1",
  "method": "SWITCH",
  "value": { "Switch": "1" }
}
```

出向调用必须：

- 记录全量操控日志（`energy_device_control_log`）
- Token 过期时自动刷新并重试一次
- 平台内部错误时指数退避重试，最多 3 次
- 业务错误（参数校验失败、设备不存在）不重试
