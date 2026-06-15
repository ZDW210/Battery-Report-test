# 现有项目迁移映射

本文档用于把归档目录 `D:\code\ankerui\energy-storage-platform\legacy` 中的 NestJS、Prisma、uni-app、worker 设计映射到当前 RuoYi-Vue-Pro 体系。

## 当前项目结构

```text
D:\code\ankerui\energy-storage-platform
├── current
│   ├── backend-ruoyi       # RuoYi-Vue-Pro 后端
│   ├── admin-vue3          # Vue3 管理端
│   ├── miniprogram         # uni-app 小程序
│   ├── design              # 设计文档和 SQL
│   └── design-standards    # 设计标准
└── legacy
    ├── backend-nestjs      # 旧 NestJS + Prisma 后端
    ├── eiot-worker         # 旧 EIOT 接入与归档 worker
    ├── minidatt            # 旧 minidatt 项目
    ├── xiaocode            # 旧试验目录
    └── miniprogram-copy    # 小程序副本
```

## 迁移原则

- 不一次性重写所有系统。
- 先迁移后端业务模型和管理端基础 CRUD。
- 小程序先保留，只做接口适配。
- worker 先保留，继续负责 EIOT 接入、归档、异常缓冲。
- 等 RuoYi 后端稳定后，再逐步下线 NestJS 后端。

## Prisma 模型映射

| 当前 Prisma 模型 | RuoYi 目标表 | 目标服务 |
| --- | --- | --- |
| `Device` | `energy_device` | `EnergyDeviceService` |
| `Alarm` | `energy_alarm` | `EnergyAlarmService` |
| `Customer` | `energy_customer` | `EnergyCustomerService` |
| `PricingRule` | `energy_pricing_rule` | `EnergyPricingRuleService` |
| `ChargingSession` | `energy_charge_session` | `EnergyChargeSessionService` |
| `EiotSyncLog` | `energy_eiot_sync_log` | `EnergyEiotService` |
| `User` | RuoYi `system_users` | 复用 RuoYi 用户体系 |

## 字段映射

### Device -> energy_device

| 当前字段 | 目标字段 | 说明 |
| --- | --- | --- |
| `id` | `id` | 主键类型从 UUID 调整为 RuoYi bigint |
| `gatewaySn` | `gateway_sn` | 网关序列号 |
| `meterSn` | `meter_sn` | 电表序列号 |
| `meterNo` | `meter_no` | 仪表编号 |
| `name` | `device_name` | 设备名称 |
| `deviceType` | `device_type` | 转为字典值 |
| `status` | `status` | 转为字典值 |
| `projectCode` | `energy_project.code` | 拆到项目表 |
| `projectName` | `energy_project.name` | 拆到项目表 |
| `location` | `energy_project.address` 或设备备注 | 需要按真实业务确认 |
| `customerId` | `customer_id` | 客户编号 |
| `lastSoc` | `last_soc` | 最新 SOC |
| `lastSoh` | `last_soh` | 最新 SOH |
| `battVoltage` | `last_voltage` | 最新电池电压 |
| `battCurrent` | `last_current` | 最新电池电流 |
| `battTemp` | `last_temp` | 最新电池温度 |
| `lastP` | `last_power` | 最新有功功率 |
| `runMode` | `run_mode` | 转为字典值 |
| `latitude` | `latitude` | 纬度 |
| `longitude` | `longitude` | 经度 |
| `lastReadingAt` | `last_reading_time` | 最新采集时间 |

### Alarm -> energy_alarm

| 当前字段 | 目标字段 | 说明 |
| --- | --- | --- |
| `id` | `id` | 主键类型调整 |
| `deviceId` | `device_id` | 设备编号 |
| `alarmNo` | `alarm_no` | 告警业务编号 |
| `code` | `code` | 告警代码 |
| `level` | `level` | 转为字典值 |
| `titleZh` | `title` | 中文标题 |
| `messageZh` | `content` | 中文内容 |
| `acknowledgedAt` | `ack_time` | 确认时间 |
| `createdAt` | `occur_time` | 告警发生时间 |

### Customer -> energy_customer

| 当前字段 | 目标字段 | 说明 |
| --- | --- | --- |
| `id` | `id` | 主键类型调整 |
| `name` | `name` | 客户名称 |
| `contact` | `contact_name` | 联系人 |
| `phone` | `contact_mobile` | 手机号 |
| `region` | `region` | 区域 |
| `remark` | `remark` | 备注 |
| `status` | `status` | 转为字典值 |

### PricingRule -> energy_pricing_rule

| 当前字段 | 目标字段 | 说明 |
| --- | --- | --- |
| `id` | `id` | 主键类型调整 |
| `customerId` | `customer_id` | 客户编号 |
| `deviceId` | `device_id` | 设备编号 |
| `region` | 暂不落表或扩展字段 | 建议优先通过项目/场站表达 |
| `timeRate` | `time_rate` | 时间单价 |
| `energyRate` | `energy_rate` | 电量单价 |
| `effectiveFrom` | `effective_start` | 生效开始 |
| `effectiveTo` | `effective_end` | 生效结束 |
| `isActive` | `status` | 转为启用/停用 |

### ChargingSession -> energy_charge_session

| 当前字段 | 目标字段 | 说明 |
| --- | --- | --- |
| `id` | `id` | 主键类型调整 |
| `deviceId` | `device_id` | 设备编号 |
| `customerId` | `customer_id` | 客户编号 |
| `pricingRuleId` | `pricing_rule_id` | 计费规则 |
| `startTime` | `start_time` | 开始时间 |
| `endTime` | `end_time` | 结束时间 |
| `startEpi` | `start_epi` | 开始正向电能读数 |
| `startEpe` | `start_epe` | 开始反向电能读数 |
| `endEpi` | `end_epi` | 结束正向电能读数 |
| `endEpe` | `end_epe` | 结束反向电能读数 |
| `totalTimeFee` | `time_fee` | 时间费用 |
| `totalEnergyFee` | `energy_fee` | 电量费用 |
| `totalFee` | `total_fee` | 总费用 |
| `status` | `status` | 转为字典值 |

## NestJS 模块映射

| 当前模块 | 目标 RuoYi 模块 |
| --- | --- |
| `auth` | RuoYi `system` 权限体系 |
| `devices` | `energy/device` |
| `readings` | `energy/telemetry` |
| `alarms` | `energy/alarm` |
| `customers` | `energy/customer`、`energy/project`、`energy/pricing-rule`、`energy/charge-session` |
| `eiot` | `energy/eiot` |
| `export` | RuoYi 导出能力 + `energy/report` |

## 小程序迁移映射

| 当前页面/模块 | 目标接口 |
| --- | --- |
| 首页 `pages/index/index.vue` | `/app-api/energy/home/overview` |
| 设备列表/详情 | `/app-api/energy/device/list`、`/detail` |
| 趋势页 | `/app-api/energy/telemetry/trend` |
| 告警列表/详情 | `/app-api/energy/alarm/list`、`/detail` |
| 设置/计费 | `/app-api/energy/bill/monthly`、`/charge-session/list` |

## worker 迁移映射

| 当前 worker 能力 | 保留/迁移建议 |
| --- | --- |
| EIOT 实时数据接收 | 保留 |
| EIOT 告警接收 | 保留 |
| D1 查询接口 | 后续逐步迁移到 RuoYi 后端 |
| R2 原始报文归档 | 保留 |
| 归档索引 | 可同步到 `energy_eiot_sync_log` |
| 对外 dashboard API | 后续由 RuoYi 接管 |

## 数据迁移注意事项

- UUID 主键迁移到 bigint 时，需要建立旧 ID 到新 ID 的映射表或临时 CSV。
- 设备 `meter_no` 必须保持唯一，是迁移关联的关键字段。
- 告警迁移优先使用 `alarm_no` 幂等。
- 采集明细数据量大时，不建议一次性迁移历史全量，优先迁最近 30 天。
- 用户账号迁移优先重建账号和角色，不直接迁移密码哈希。
