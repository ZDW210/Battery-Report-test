# RuoYi 模块落地蓝图

本文档把移动储能业务设计细化为 RuoYi-Vue-Pro 可落地的模块、包、类、接口和权限结构。

## 模块名称

```text
yudao-module-energy
```

## Maven 模块结构

当前第一步已采用单 Maven 模块接入主工程，便于先跑通后端启动和管理端接口验证：

```text
yudao-module-energy
├── pom.xml
└── src/main/java/cn/iocoder/yudao/module/energy
    ├── package-info.java
    └── controller/admin/info
        ├── EnergyModuleInfoController.java
        └── vo/EnergyModuleInfoRespVO.java
```

当前模块已加入：

- 根 `pom.xml` 的 `<modules>`
- `yudao-server/pom.xml` 的运行依赖
- 管理端探测接口 `/admin-api/energy/module-info/runtime`

后续业务复杂度上来后，再按下列目标结构拆分 `api` / `biz` 子模块：

```text
yudao-module-energy
├── pom.xml
├── yudao-module-energy-api
│   ├── pom.xml
│   └── src/main/java/cn/iocoder/yudao/module/energy/api
│       ├── device
│       ├── alarm
│       └── enums
└── yudao-module-energy-biz
    ├── pom.xml
    └── src/main/java/cn/iocoder/yudao/module/energy
        ├── controller
        │   ├── admin
        │   ├── app
        │   └── infra
        ├── convert
        ├── dal
        │   ├── dataobject
        │   ├── mysql
        │   └── redis
        ├── enums
        ├── framework
        ├── job
        └── service
```

## 后端包结构

### 设备中心

```text
controller/admin/device/EnergyDeviceController.java
controller/app/device/AppEnergyDeviceController.java
dal/dataobject/device/EnergyDeviceDO.java
dal/mysql/device/EnergyDeviceMapper.java
service/device/EnergyDeviceService.java
service/device/EnergyDeviceServiceImpl.java
convert/device/EnergyDeviceConvert.java
```

### 实时数据

```text
controller/admin/telemetry/EnergyTelemetryController.java
controller/app/telemetry/AppEnergyTelemetryController.java
dal/dataobject/telemetry/EnergyTelemetryDO.java
dal/mysql/telemetry/EnergyTelemetryMapper.java
service/telemetry/EnergyTelemetryService.java
service/telemetry/EnergyTelemetryServiceImpl.java
convert/telemetry/EnergyTelemetryConvert.java
```

### 告警中心

```text
controller/admin/alarm/EnergyAlarmController.java
controller/app/alarm/AppEnergyAlarmController.java
dal/dataobject/alarm/EnergyAlarmDO.java
dal/mysql/alarm/EnergyAlarmMapper.java
service/alarm/EnergyAlarmService.java
service/alarm/EnergyAlarmServiceImpl.java
convert/alarm/EnergyAlarmConvert.java
```

### 客户项目

```text
controller/admin/customer/EnergyCustomerController.java
controller/admin/project/EnergyProjectController.java
dal/dataobject/customer/EnergyCustomerDO.java
dal/dataobject/project/EnergyProjectDO.java
dal/mysql/customer/EnergyCustomerMapper.java
dal/mysql/project/EnergyProjectMapper.java
service/customer/EnergyCustomerService.java
service/project/EnergyProjectService.java
convert/customer/EnergyCustomerConvert.java
convert/project/EnergyProjectConvert.java
```

### 计费和会话

```text
controller/admin/pricing/EnergyPricingRuleController.java
controller/admin/session/EnergyChargeSessionController.java
controller/app/session/AppEnergyChargeSessionController.java
dal/dataobject/pricing/EnergyPricingRuleDO.java
dal/dataobject/session/EnergyChargeSessionDO.java
dal/mysql/pricing/EnergyPricingRuleMapper.java
dal/mysql/session/EnergyChargeSessionMapper.java
service/pricing/EnergyPricingRuleService.java
service/session/EnergyChargeSessionService.java
convert/pricing/EnergyPricingRuleConvert.java
convert/session/EnergyChargeSessionConvert.java
```

### EIOT 接入

```text
controller/infra/eiot/EnergyEiotController.java
dal/dataobject/eiot/EnergyEiotSyncLogDO.java
dal/mysql/eiot/EnergyEiotSyncLogMapper.java
service/eiot/EnergyEiotService.java
service/eiot/EnergyEiotServiceImpl.java
convert/eiot/EnergyEiotConvert.java
```

### EIOT 凭证和反向控制

```text
controller/admin/eiot/EnergyEiotCredentialController.java
controller/admin/eiot/EnergyDeviceControlLogController.java
controller/app/eiot/AppEnergyDeviceControlController.java
dal/dataobject/eiot/EnergyEiotCredentialDO.java
dal/dataobject/eiot/EnergyDeviceControlLogDO.java
dal/mysql/eiot/EnergyEiotCredentialMapper.java
dal/mysql/eiot/EnergyDeviceControlLogMapper.java
service/eiot/EnergyEiotCredentialService.java
service/eiot/EnergyDeviceControlService.java
```

### 运营面板和报表

```text
controller/admin/dashboard/EnergyDashboardController.java
controller/admin/report/EnergyReportController.java
service/dashboard/EnergyDashboardService.java
service/report/EnergyReportService.java
```

## 管理端权限标识

| 功能 | 权限标识 |
| --- | --- |
| 运营面板 | `energy:dashboard:query` |
| 设备查询 | `energy:device:query` |
| 设备新增 | `energy:device:create` |
| 设备修改 | `energy:device:update` |
| 设备删除 | `energy:device:delete` |
| 设备导出 | `energy:device:export` |
| 采集数据查询 | `energy:telemetry:query` |
| 告警查询 | `energy:alarm:query` |
| 告警确认 | `energy:alarm:ack` |
| 告警关闭 | `energy:alarm:close` |
| 客户管理 | `energy:customer:*` |
| 项目管理 | `energy:project:*` |
| 计费规则 | `energy:pricing-rule:*` |
| 充放电记录 | `energy:charge-session:*` |
| 设备操控 | `energy:device:control` |
| 操控日志 | `energy:device:control-log:query` |
| 调度任务 | `energy:dispatch:*` |
| EIOT 凭证 | `energy:eiot-credential:*` |
| EIOT 日志 | `energy:eiot-log:query` |

## API 路由落地

### 管理端

```text
/admin-api/energy/dashboard
/admin-api/energy/device
/admin-api/energy/telemetry
/admin-api/energy/alarm
/admin-api/energy/customer
/admin-api/energy/project
/admin-api/energy/pricing-rule
/admin-api/energy/charge-session
/admin-api/energy/dispatch
/admin-api/energy/eiot-log
/admin-api/energy/module-info/runtime
```

### 小程序

```text
/app-api/energy/home
/app-api/energy/device
/app-api/energy/telemetry
/app-api/energy/alarm
/app-api/energy/charge-session
/app-api/energy/bill
```

### 内部接入

```text
/infra-api/energy/eiot/realtime
/infra-api/energy/eiot/alarm
```

### EIOT 出向（本系统 → EIOT 平台）

```text
POST http://{EIOT}/basic/prepayment/auth_user/login
POST http://{EIOT}/basic/prepayment/entry/home/control
```

## 第一批建议生成的 CRUD

可先使用 RuoYi 代码生成器处理：

1. `energy_customer`
2. `energy_project`
3. `energy_device`
4. `energy_alarm`
5. `energy_pricing_rule`
6. `energy_charge_session`
7. `energy_eiot_sync_log`

`energy_telemetry` 建议手写查询接口，因为后续可能迁移到时序库或分区表。
