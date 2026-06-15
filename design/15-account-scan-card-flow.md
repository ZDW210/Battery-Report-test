# 扫码/刷卡账户识别流程

日期：2026-06-09

## 业务含义

二维码内容不再理解为“车辆二维码”。当前主流程中，司机扫的是设备/充电桩二维码，二维码只负责定位被操作的设备或仪表。

推荐二维码内容：

- 直接设备编号：`deviceNo=DEV-001`
- 直接仪表编号：`meterNo=12207013690004_12207013690004`
- 网关和电表组合：`gatewaySn=12207013690004&meterSn=12207013690004`
- 带参数链接：`https://example.com/pile/scan?deviceNo=DEV-001`

二维码不保存司机身份，不保存微信用户信息，也不代表车辆。司机身份由当前小程序登录态或刷卡卡号识别。

账户识别分两类：

- 微信扫码：小程序已经登录，后端使用当前微信登录生成的 App 用户识别使用人。
- 刷卡：充电桩后续上报 `cardNo + deviceNo/deviceId/meterNo`，后端用卡号匹配 App 用户。

如果账号不存在、账号停用、卡号未绑定，或账号没有该设备/项目/客户权限，则判定为未知账户。当前阶段先在小程序显示“未知账户”，充电桩显示屏联动后续再接。

## 后端接口

小程序扫码：

```text
POST /app-api/energy/charging-sessions/scan/verify
POST /app-api/energy/charging-sessions/scan/discharge
```

请求字段：

```json
{
  "scanText": "deviceNo/deviceId/meterNo 或带参数链接",
  "authType": "WECHAT"
}
```

设备侧刷卡：

```text
POST /infra-api/energy/eiot/card/verify
POST /infra-api/energy/eiot/card/discharge
Header: X-EIOT-Token: <EIOT 入站 Token>
```

请求字段：

```json
{
  "scanText": "deviceNo/deviceId/meterNo 或带参数链接",
  "cardNo": "CARD-0001"
}
```

说明：

- 小程序扫码接口使用当前微信登录态识别司机。
- 设备侧刷卡接口使用 `cardNo` 识别司机，不要求小程序登录态。
- 两类入口都写入“扫码刷卡记录”，并复用同一套 App 用户启停状态和 `energy_user_scope` 授权范围校验。

校验返回新增字段：

- `accountKnown`：是否识别到账户且拥有当前设备权限。
- `accountType`：`WECHAT` 或 `CARD`。
- `accountId`：App 用户编号。
- `accountName`：App 用户昵称。
- `accountMobile`：手机号。
- `cardNo`：卡号。
- `message`：识别结果说明。

## 管理端

新增“移动储能 / 使用账户”：

- 路由：`/energy/app-user`
- 组件：`energy/appUser/index`
- 权限：`energy:app-user:query`、`energy:app-user:update`

用途：

- 自动显示微信小程序首次登录后创建的 App 用户。
- 维护手机号、昵称、刷卡卡号、启停状态。
- 后续在“用户授权”中给该 App 用户开放客户、项目或设备权限。

新增“移动储能 / 扫码刷卡记录”：

- 路由：`/energy/account-event`
- 组件：`energy/account-event/index`
- 权限：`energy:account-event:query`
- 用途：展示每次扫码校验、开始放电、后续刷卡识别的记录，包含设备、仪表、识别方式、账户、卡号、识别结果、结果提示和发生时间。
- 开始放电记录必须区分任务结果：账户识别成功但因计费规则、进行中任务、设备控制配置或 EIOT 控制失败导致未启动时，`resultMessage` 记录具体失败原因，避免管理端误判为已成功放电。
- 页面需要同时展示“账户结果”和“任务结果”：账户结果用于判断是否为已录入/已授权账户；任务结果用于判断是否已启动、启动失败、已拒绝或仅校验。

## 小程序

首页按钮改为“扫码启动”。

扫码后流程：

1. 调用后端校验设备和当前微信账户。
2. 如果 `accountKnown=false`，显示“未知账户”，不允许开始放电。
3. 如果 `accountKnown=true`，显示账户、设备、仪表和设备状态，司机确认后开始放电。
4. 后端再次校验账户和设备授权，不信任前端校验结果；开始放电成功或失败都会写入“扫码刷卡记录”。
5. 开始放电失败时，小程序必须展示后端返回的具体失败原因，例如未配置 EIOT 控制地址、没有计费规则或设备已有进行中任务。
