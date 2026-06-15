# 2026-06-12 扫码解析设备优先调整

## 变更原因

设计标准已经明确二维码主语义为设备/充电桩/仪表识别，车辆字段只作为旧兼容。后端扫码解析需要与设计保持一致。

## 本次调整

- 后端扫码解析顺序调整为先匹配 `deviceId`、`deviceNo`、`meterNo`、`gatewaySn + meterSn`。
- 旧车辆字段 `vehicleId`、`vehicleNo`、`plateNo`、`qrCode` 仅在设备未匹配时兜底兼容。
- 后端内部命名从 `ScannedVehicleDevice` 调整为 `ScannedDevice`，避免继续误导后续开发。
- 小程序扫码失败默认提示从“扫码放电失败”调整为“扫码启动失败”。
- 管理端车辆绑定页面增加提示，说明该页面仅用于可选车辆资产管理和旧车辆二维码兼容。

## 设计标准同步

- `design-standards/04-api-standards.md` 已要求设备/充电桩/仪表二维码优先。
- `design-standards/05-frontend-standards.md` 已要求小程序扫码展示账户、设备、仪表和设备状态。
- `design-standards/07-vehicle-binding-standards.md` 已明确车辆绑定不是新扫码放电主流程前置条件。
