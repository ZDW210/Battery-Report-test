# 后端模块设计

## 模块命名

推荐新增模块：

```text
yudao-module-energy
```

该模块负责移动储能业务，不直接修改 RuoYi 内置的 `system`、`infra`、`member` 等模块。

## 模块结构

```text
yudao-module-energy
├── yudao-module-energy-api
│   └── 对外 RPC / API DTO / 枚举
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

## 业务子域

| 子域 | 说明 | 优先级 |
| --- | --- | --- |
| 设备中心 | 储能设备、网关、电表、BMS、PCS、充电枪 | P0 |
| 实时监控 | 最新状态、SOC、功率、温度、GPS、在线状态 | P0 |
| 告警中心 | 告警接入、确认、关闭、通知 | P0 |
| 客户项目 | 客户、项目、场站、设备归属 | P0 |
| 充放电记录 | 任务、会话、电量、时长、费用 | P0 |
| 计费规则 | 客户计价、区域计价、设备计价 | P1 |
| 调度任务 | 充电、放电、待机、应急供电 | P1 |
| 报表中心 | 电量、收益、利用率、告警趋势 | P1 |
| 运维工单 | 巡检、维修、备件、处理记录 | P2 |

## Controller 分层

```text
admin-api/energy/**
```

管理后台接口，面向运营人员和管理员。

```text
app-api/energy/**
```

小程序接口，面向客户、司机、运维人员。

```text
infra-api/energy/eiot/**
```

内部或接入接口，面向 EIOT worker、网关推送、数据同步任务。

## 与 RuoYi 内置模块关系

| 能力 | 使用方式 |
| --- | --- |
| 用户权限 | 复用 `yudao-module-system` |
| 租户隔离 | 如需要 SaaS，可复用租户字段和租户拦截器 |
| 字典配置 | 设备状态、告警等级、运行模式使用系统字典 |
| 操作日志 | 调度、告警确认、计费规则修改写入操作日志 |
| 定时任务 | 离线检测、日报统计、费用结算使用 job |
| 代码生成 | 设备、客户、计费规则等 CRUD 可先用代码生成 |
| 会员体系 | 小程序客户侧账号可对接 `member` |
| 支付能力 | 后期如做线上支付，可对接 `pay` |
| 公众号/微信 | 模板消息、公众号能力可对接 `mp` |

## 核心服务

| Service | 职责 |
| --- | --- |
| EnergyDeviceService | 设备台账、状态更新、设备归属 |
| EnergyTelemetryService | 实时数据写入、最新快照、历史查询 |
| EnergyAlarmService | 告警入库、确认、关闭、通知 |
| EnergyCustomerService | 客户、项目、场站 |
| EnergyPricingService | 计费规则、费用计算 |
| EnergySessionService | 充放电会话、费用汇总 |
| EnergyDispatchService | 调度任务创建、下发、状态跟踪 |
| EnergyEiotService | 安科瑞 EIOT 数据解析、同步、认证与反向控制 |
| EnergyDeviceControlService | 设备远程操控指令下发、操控日志 |
| EnergyReportService | 报表统计、导出 |

