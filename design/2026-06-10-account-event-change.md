# 2026-06-10 扫码刷卡记录变更

## 变更原因

二维码语义明确为设备/充电桩识别，不再作为司机或车辆身份。司机身份来自微信小程序登录用户，刷卡身份来自卡号绑定的 App 用户。

## 本次调整

- 新增 `energy_account_event` 表，用于记录扫码校验、开始放电和后续刷卡识别事件。
- 后端在 `/app-api/energy/charging-sessions/scan/verify` 和 `/scan/discharge` 写入识别记录。
- 管理端新增“移动储能 / 扫码刷卡记录”页面。
- 小程序类型名从 `ScannedVehicle` 改为 `ScannedDevice`，旧车辆字段仅保留兼容。
- 错误提示从车辆语义改为设备语义，未知账户统一返回“未知账户”。

## 设计标准同步

- `design-standards/08-account-identification-standards.md` 增加扫码刷卡记录管理端标准。
- `design/15-account-scan-card-flow.md` 增加二维码推荐内容和记录页面说明。
- `design/sql/mysql/energy_schema.sql` 和 `design/sql/mysql/energy_account_event_menu.sql` 增加表结构与菜单脚本。
