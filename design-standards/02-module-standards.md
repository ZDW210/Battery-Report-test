# 后端模块与分层标准

## 模块标准

移动储能业务必须独立放在：

```text
yudao-module-energy
```

不要把移动储能业务直接写入 `yudao-module-system`、`yudao-module-infra`、`yudao-module-member` 等内置模块。

## 推荐结构

```text
yudao-module-energy
├── yudao-module-energy-api
└── yudao-module-energy-biz
    ├── controller
    │   ├── admin
    │   ├── app
    │   └── infra
    ├── service
    ├── dal
    │   ├── dataobject
    │   ├── mysql
    │   └── redis
    ├── convert
    ├── enums
    └── job
```

## Controller 标准

| 包 | 接口前缀 | 用途 |
| --- | --- | --- |
| `controller.admin` | `/admin-api/energy/**` | 管理后台 |
| `controller.app` | `/app-api/energy/**` | 小程序 |
| `controller.infra` | `/infra-api/energy/**` | EIOT worker 和内部接入 |

## Service 标准

核心服务按业务能力拆分：

- `EnergyDeviceService`：设备台账、设备状态、设备归属
- `EnergyTelemetryService`：实时数据、历史数据、最新快照
- `EnergyAlarmService`：告警接入、确认、关闭、通知
- `EnergyCustomerService`：客户、项目、场站
- `EnergyPricingService`：计费规则、费用计算
- `EnergySessionService`：充放电会话、费用汇总
- `EnergyDispatchService`：调度任务、下发、状态跟踪
- `EnergyEiotService`：安科瑞 EIOT 数据解析和同步
- `EnergyReportService`：统计报表和导出

## 字典标准

设备状态、告警等级、运行模式、任务状态等枚举优先落到系统字典，便于管理端维护和展示。

必须优先考虑的字典：

- `energy_device_status`
- `energy_device_type`
- `energy_run_mode`
- `energy_alarm_level`
- `energy_alarm_status`
- `energy_session_type`
- `energy_session_status`
- `energy_dispatch_status`

