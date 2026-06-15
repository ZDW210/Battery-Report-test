# 数据库设计

## 命名约定

业务表统一使用前缀：

```text
energy_
```

示例：

```text
energy_device
energy_alarm
energy_customer
```

## 核心表

### energy_customer 客户表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| name | varchar(128) | 客户名称 |
| contact_name | varchar(64) | 联系人 |
| contact_mobile | varchar(32) | 联系电话 |
| region | varchar(64) | 区域 |
| status | tinyint | 状态 |
| remark | varchar(512) | 备注 |
| tenant_id | bigint | 租户编号 |
| creator/create_time/updater/update_time/deleted | RuoYi 标准字段 | 审计字段 |

### energy_project 项目/场站表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| customer_id | bigint | 客户编号 |
| name | varchar(128) | 项目名称 |
| code | varchar(64) | 项目编码 |
| address | varchar(255) | 地址 |
| latitude | decimal(10,6) | 纬度 |
| longitude | decimal(10,6) | 经度 |
| status | tinyint | 状态 |

### energy_device 设备表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| device_no | varchar(64) | 设备编号 |
| device_name | varchar(128) | 设备名称 |
| device_type | varchar(32) | 设备类型 |
| gateway_sn | varchar(64) | 网关序列号 |
| meter_sn | varchar(64) | 电表序列号 |
| meter_no | varchar(128) | 仪表编号 |
| customer_id | bigint | 客户编号 |
| project_id | bigint | 项目编号 |
| status | tinyint | 在线状态 |
| run_mode | tinyint | 运行模式 |
| latitude | decimal(10,6) | 纬度 |
| longitude | decimal(10,6) | 经度 |
| last_soc | decimal(6,2) | 最新 SOC |
| last_soh | decimal(6,2) | 最新 SOH |
| last_power | decimal(12,3) | 最新功率 |
| last_voltage | decimal(12,3) | 最新电压 |
| last_current | decimal(12,3) | 最新电流 |
| last_temp | decimal(8,2) | 最新温度 |
| last_reading_time | datetime | 最新采集时间 |

### energy_telemetry 采集明细表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| device_id | bigint | 设备编号 |
| gateway_sn | varchar(64) | 网关唯一编号 |
| meter_sn | varchar(64) | 网关下仪表编号 |
| meter_no | varchar(128) | 仪表编号 |
| collect_time | datetime | 采集时间 |
| timestamp | bigint | Unix 时间戳 |
| source | varchar(32) | 数据类型：REALTIME/HISTORY |
| state | varchar(32) | 仪表状态：ONLINE/OFFLINE |
| pa/pb/pc | decimal(12,3) | 三相有功功率 |
| ua/ub/uc | decimal(12,3) | 三相电压 |
| ia/ib/ic | decimal(12,3) | 三相电流 |
| p/q/pf | decimal(12,3) | 有功、无功、功率因数 |
| epi/epe | decimal(14,3) | 正/反向电能 |
| soc/soh | decimal(6,2) | 电池健康数据 |
| batt_voltage | decimal(12,3) | 电池电压 |
| batt_current | decimal(12,3) | 电池电流 |
| batt_temp | decimal(8,2) | 电池温度 |
| run_mode | tinyint | 运行模式 |

高频数据量大时，建议将此表迁移到 TDengine、TimescaleDB，或按月分区。

### energy_alarm 告警表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| alarm_no | varchar(64) | 告警编号 |
| device_id | bigint | 设备编号 |
| code | varchar(64) | 告警代码 |
| level | tinyint | 告警等级 |
| title | varchar(128) | 告警标题 |
| content | varchar(1024) | 告警内容 |
| status | tinyint | 状态：未确认、已确认、已关闭 |
| occur_time | datetime | 发生时间 |
| ack_user_id | bigint | 确认人 |
| ack_time | datetime | 确认时间 |
| close_time | datetime | 关闭时间 |

### energy_pricing_rule 计费规则表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| customer_id | bigint | 客户编号 |
| project_id | bigint | 项目编号 |
| device_id | bigint | 设备编号 |
| time_rate | decimal(10,4) | 时间单价 |
| energy_rate | decimal(10,4) | 电量单价 |
| effective_start | datetime | 生效开始 |
| effective_end | datetime | 生效结束 |
| status | tinyint | 状态 |

### energy_charge_session 充放电会话表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| session_no | varchar(64) | 会话编号 |
| device_id | bigint | 设备编号 |
| customer_id | bigint | 客户编号 |
| pricing_rule_id | bigint | 计费规则 |
| session_type | tinyint | 充电/放电 |
| start_time | datetime | 开始时间 |
| end_time | datetime | 结束时间 |
| start_energy | decimal(14,3) | 开始电能读数，第一版取 EIOT `EPI` |
| end_energy | decimal(14,3) | 结束电能读数，第一版取 EIOT `EPI` |
| total_energy | decimal(14,3) | 总电量 |
| duration_minutes | int | 时长 |
| energy_fee | decimal(12,2) | 电量费用 |
| time_fee | decimal(12,2) | 时间费用 |
| total_fee | decimal(12,2) | 总费用 |
| status | tinyint | 第一版状态：0 进行中，1 已结束，2 异常，3 已结算 |

### energy_eiot_sync_log 同步日志表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| sync_type | varchar(32) | realtime/alarm |
| gateway_sn | varchar(64) | 网关序列号 |
| meter_sn | varchar(64) | 电表序列号 |
| request_id | varchar(64) | 请求编号 |
| payload_url | varchar(512) | 原始报文归档地址 |
| status | tinyint | 成功/失败 |
| error_msg | varchar(1024) | 错误信息 |
| create_time | datetime | 创建时间 |

### energy_eiot_credential EIOT 平台凭证表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| platform_name | varchar(64) | EIOT 平台名称 |
| base_url | varchar(256) | EIOT 基础地址 |
| login_name | varchar(64) | 登录账号 |
| login_password | varchar(128) | 登录密码 |
| current_token | varchar(2048) | 当前有效 Token |
| token_expire_time | datetime | Token 过期时间 |
| status | tinyint | 状态：0 启用，1 禁用 |
| tenant_id | bigint | 租户编号 |
| creator/create_time/updater/update_time/deleted | RuoYi 标准字段 | 审计字段 |

### energy_device_control_log 设备操控日志表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| device_id | bigint | 设备编号 |
| session_id | bigint | 关联会话编号（可为空） |
| credential_id | bigint | 使用的 EIOT 凭证编号 |
| method | varchar(32) | 控制方法：SWITCH/FORCESWITCH/REFRESH 等 |
| request_body | varchar(2048) | 发给 EIOT 的请求体 |
| response_body | varchar(2048) | EIOT 返回的响应体 |
| eiot_success | tinyint | EIOT 返回 success：0 失败，1 成功 |
| eiot_error_code | varchar(32) | EIOT 返回 errorCode |
| status | tinyint | 状态：0 成功，1 失败 |
| error_msg | varchar(1024) | 失败原因 |
| operator_id | bigint | 操作人 |
| operate_time | datetime | 操作时间 |
| tenant_id | bigint | 租户编号 |
| creator/create_time | RuoYi 标准字段 | 审计字段 |

## 字典建议

| 字典编码 | 说明 |
| --- | --- |
| energy_device_status | 在线、离线、故障、维护 |
| energy_device_type | 储能车、储能柜、电池包、PCS、BMS、电表、网关 |
| energy_run_mode | 充电、放电、待机、故障 |
| energy_alarm_level | 提示、一般、严重、紧急 |
| energy_alarm_status | 未确认、已确认、已关闭 |
| energy_session_type | 充电、放电 |
| energy_session_status | 待开始、充电中、放电中、暂停、已完成、异常结束、已结算、已取消 |
| energy_dispatch_status | 待下发、执行中、成功、失败、取消 |
| energy_control_method | SWITCH、FORCESWITCH、REFRESH、STRONGSWITCH、RESET、SELCHK、RLYREP |
