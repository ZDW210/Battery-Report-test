# 数据库设计标准

## 表命名

移动储能业务表统一使用：

```text
energy_
```

示例：

- `energy_device`
- `energy_alarm`
- `energy_customer`
- `energy_charge_session`

## 字段标准

所有业务表应尽量使用 RuoYi 标准审计字段：

- `creator`
- `create_time`
- `updater`
- `update_time`
- `deleted`
- `tenant_id`，如果启用多租户

## 主键标准

- RuoYi 业务表优先使用 `bigint` 自增或雪花 ID 风格主键。
- 如果表结构使用非自增 `bigint` 主键，后端 DO 必须显式声明 `@TableId(type = IdType.ASSIGN_ID)`，避免新增时依赖数据库默认值。
- 外部设备编号、仪表编号、网关编号不要直接作为主键，应作为唯一业务字段。

## 高频数据标准

采集明细、功率曲线、SOC 曲线等高频数据必须和设备主表分离。

可选方案：

- 低频或开发期：`energy_telemetry`
- 中高频：按月分区明细表
- 高频生产：TDengine、TimescaleDB 或其他时序库

## 最新状态标准

设备列表和详情常用的最新值允许冗余到 `energy_device`：

- `last_soc`
- `last_soh`
- `last_power`
- `last_voltage`
- `last_current`
- `last_temp`
- `last_reading_time`
- `status`
- `run_mode`
- `latitude`
- `longitude`

## 原始报文标准

EIOT 原始推送报文不建议只存数据库 JSON 字段。推荐：

- 原始完整报文存对象存储或文件存储
- 数据库存索引、状态、错误信息、归档地址
- 失败数据可重放

## 索引标准

核心索引至少覆盖：

- 设备：`device_no`、`meter_no`、`customer_id`、`project_id`、`status`
- 采集：`device_id + collect_time`、`meter_no + collect_time`
- 告警：`device_id`、`level`、`status`、`occur_time`
- 告警处理记录：`alarm_id`、`action`、`operate_time`
- 小程序用户授权：`user_type + user_id + status`、`customer_id`、`project_id`、`device_id`
- 小程序 App 用户：`username + deleted` 必须唯一，`status`、`tenant_id` 必须建索引。
- App 用户密码必须使用 Spring Security `PasswordEncoder` 兼容的 BCrypt 密文存储，禁止明文或可逆加密。
- 会话：`device_id`、`customer_id`、`operator_user_id`、`start_time`、`status`、`session_no`
- 同步日志：`request_id`、`sync_type`、`create_time`
- EIOT 凭证：`tenant_id`、`status`
- 操控日志：`device_id`、`session_id`、`method`、`status`、`operate_time`

## 初始化脚本标准

移动储能设计阶段的 MySQL 初始化脚本统一放在：

```text
current/design/sql/mysql
```

当前脚本包括：

- `energy_schema.sql`：业务表
- `energy_dict.sql`：系统字典
- `energy_menu.sql`：菜单和权限

设计脚本中的演示 ID 暂使用 `190000000000` 号段。真正落库前，需要结合目标 RuoYi 数据库已有 ID 和表结构进行核对。
# 2026-06-16 安科瑞 EIOT 入站数据字段补充

- `energy_telemetry` 必须保留实时曲线高频字段，同时新增 `data_json` 和 `payload_url`，用于保存单条仪表报文 JSON 和 R2 原始请求体归档地址。
- D1 初始迁移脚本必须直接包含运行时业务所需字段；计费规则表不得依赖页面首次访问时批量 `ALTER TABLE` 补齐核心字段，避免并发请求触发 D1 锁冲突。运行时 `ensureColumns` 只能作为旧库兼容兜底。
- `energy_alarm` 必须支持 EIOT 报警来源字段：`gateway_sn`、`meter_sn`、`meter_no`、`timestamp`、`data_json`、`payload_url`。
- D1 生产环境允许 Worker 在启动接收 EIOT 报文时执行幂等补列和 `CREATE INDEX IF NOT EXISTS`，但后续整理迁移脚本时必须把这些字段同步回正式 migrations。
- 遥测和报警的原始完整请求体不得只存在 D1；必须归档到 R2，D1 仅保存结构化查询字段、单条报文摘要 JSON 和 R2 地址。
