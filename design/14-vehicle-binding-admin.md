# 车辆绑定管理落地说明

日期：2026-06-10

## 当前定位

管理端“移动储能 / 车辆绑定”保留为可选车辆资产绑定模块，用于内部资产管理和旧车辆二维码兼容。

当前司机扫码放电主流程已经调整为“设备/充电桩二维码 + 微信或刷卡账户识别”：

- 新二维码优先定位设备、充电桩或仪表。
- 司机身份来自当前小程序登录账户或刷卡卡号绑定账户。
- 车辆绑定不是新扫码放电流程的前置条件。
- 旧车辆二维码仍可通过 `vehicleId`、`vehicleNo`、`plateNo`、`qrCode` 兼容定位绑定设备。

## 管理端页面

- 路由：`/energy/vehicle`
- 组件：`energy/vehicle/index`
- 组件名：`EnergyVehicle`
- 页面文件：`admin-vue3/src/views/energy/vehicle/index.vue`
- 表单文件：`admin-vue3/src/views/energy/vehicle/VehicleForm.vue`
- API 文件：`admin-vue3/src/api/energy/vehicle/index.ts`

列表字段：

- 车辆编号
- 车牌号
- 二维码内容
- 绑定设备
- 仪表编号
- 客户
- 项目
- 状态
- 备注
- 创建时间

筛选条件：

- 车辆编号
- 车牌号
- 二维码内容
- 绑定设备
- 状态

表单规则：

- 车辆编号必填，最大 64 字符。
- 车牌号必填，最大 64 字符。
- 二维码内容必填，最大 128 字符，仅用于旧车辆二维码兼容。
- 绑定设备必填。
- 状态必填，使用通用启用/停用状态。
- 客户、项目不在表单手动选择，由后端根据绑定设备自动同步，避免车辆与设备归属不一致。

## 后端接口

管理端接口：

```text
GET    /admin-api/energy/vehicle/page
GET    /admin-api/energy/vehicle/get?id=
POST   /admin-api/energy/vehicle/create
PUT    /admin-api/energy/vehicle/update
DELETE /admin-api/energy/vehicle/delete?id=
```

权限标识：

```text
energy:vehicle:query
energy:vehicle:create
energy:vehicle:update
energy:vehicle:delete
```

后端文件：

- `controller/admin/vehicle/EnergyVehicleController.java`
- `controller/admin/vehicle/vo/EnergyVehiclePageReqVO.java`
- `controller/admin/vehicle/vo/EnergyVehicleRespVO.java`
- `controller/admin/vehicle/vo/EnergyVehicleSaveReqVO.java`
- `service/vehicle/EnergyVehicleService.java`
- `service/vehicle/EnergyVehicleServiceImpl.java`
- `dal/mysql/vehicle/EnergyVehicleMapper.java`
- `dal/dataobject/vehicle/EnergyVehicleDO.java`

## 数据库与菜单

业务表：`energy_vehicle`

关键约束：

- `vehicle_no + deleted` 唯一。
- `qr_code + deleted` 唯一。
- `device_id` 必填。
- `status=0` 表示启用，`status=1` 表示停用。

菜单 SQL：

- `design/sql/mysql/energy_vehicle_menu.sql`

本地已写入菜单：

```text
190010012000 车辆绑定
190010012001 车辆新增
190010012002 车辆修改
190010012003 车辆删除
```

## 与小程序扫码放电的关系

新小程序扫码内容优先匹配：

- `deviceId`
- `deviceNo`
- `meterNo`
- `gatewaySn + meterSn`

旧车辆二维码兼容匹配：

- `vehicleId`
- `vehicleNo`
- `plateNo`
- `qrCode`

车辆停用、未绑定设备、当前 App 用户或刷卡卡号绑定用户未被授权到对应设备/项目/客户时，扫码校验必须失败。前端不能只依赖扫码结果直接放行。

扫码校验、开始放电和后续刷卡识别事件统一写入 `energy_account_event`，在管理端“扫码刷卡记录”中查看。
