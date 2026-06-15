# 车辆绑定设计标准

日期：2026-06-10

## 当前定位

- 车辆绑定是可选车辆资产绑定模块，主要用于内部资产管理和旧车辆二维码兼容。
- 新的司机扫码放电主流程不依赖车辆绑定；二维码应优先定位设备/充电桩/仪表。
- 司机身份必须来自当前小程序登录账户或刷卡卡号绑定账户，不能来自车辆二维码。

## 页面标准

- 车辆绑定可保留为移动储能管理端子板块，菜单名固定为“车辆绑定”。
- 页面采用 RuoYi Vue3 后台列表 + 弹窗表单风格，不做营销式或大卡片式布局。
- 列表支持车辆编号、车牌号、二维码内容、绑定设备、状态筛选。
- 列表展示车辆编号、车牌号、二维码内容、绑定设备、仪表编号、客户、项目、状态、备注和创建时间。
- 新增/修改表单只允许选择绑定设备；客户和项目必须由后端根据设备台账自动同步。
- 停用车辆不得在旧车辆二维码兼容流程中被视为可用。

## 接口标准

管理端车辆绑定接口固定使用：

```text
GET    /admin-api/energy/vehicle/page
GET    /admin-api/energy/vehicle/get?id=
POST   /admin-api/energy/vehicle/create
PUT    /admin-api/energy/vehicle/update
DELETE /admin-api/energy/vehicle/delete?id=
```

权限标识固定使用：

```text
energy:vehicle:query
energy:vehicle:create
energy:vehicle:update
energy:vehicle:delete
```

## 数据标准

- 车辆编号 `vehicleNo` 必填且唯一。
- 二维码内容 `qrCode` 必填且唯一，仅用于旧车辆二维码兼容。
- 车牌号 `plateNo` 必填。
- 绑定设备 `deviceId` 必填。
- `customerId` 和 `projectId` 由后端从绑定设备复制，不由前端提交决定。
- 删除使用 RuoYi 逻辑删除能力，不能物理删除业务历史。

## 小程序联动标准

- 小程序扫码后必须先调用后端扫码校验接口，不能在前端自行判断设备或账户是否可用。
- 新二维码内容优先承载 `deviceId`、`deviceNo`、`meterNo`、`gatewaySn + meterSn`。
- `qrCode`、`vehicleId`、`vehicleNo`、`plateNo` 仅作为旧车辆二维码兼容。
- 当前微信 App 用户或刷卡卡号绑定用户必须通过 `energy_user_scope` 授权到目标设备、设备所属项目或设备所属客户，才允许放电。
- 放电前后仍由后端二次校验，不信任前端缓存的扫码信息。
