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
- `EnergyReportService`：统计报表、客户数据报表和导出

### 数据报表模块标准

- 数据报表必须与报表面板平级，不作为报表面板内的子区域。
- 数据报表面向客户账单，优先展示反向放电用电量、分时服务单价、保底电量核算和成本对比。
- 数据报表的约定保底用电量必须来自命中的计费规则；基础服务费必须汇总计费规则中的场地费、运维费、通信费、平台服务费、电池折旧成本和其他固定费用。
- 数据报表表头必须采用电费账单抬头样式：品牌标识、平台名称、账单标题、服务热线、账单周期、户号、户名、用电地址、用电类别、电压等级、供电服务单位、市场化属性、打印信息和统计摘要。
- 数据报表表头中的户号、用电地址、供电服务单位、市场化属性、客户服务、监督电话、打印人和交费截止日必须优先读取客户管理，不得在报表页硬编码。
- 数据报表视觉风格以正式电费账单为准，使用浅青色标题条、浅灰到浅青的摘要卡、青绿色重点数字和浅青虚线表格分隔；调整样式时不得随意改变既有数据模块和数据口径。
- 数据来源必须复用现有设备、计费规则和报表接口，不得在前端伪造已接入遥测数据；当前没有数据库字段的服务条款可先使用页面默认值，并在后续计费规则扩展时同步迁移。
- PDF 导出必须使用固定 A4 版式，手机端不能只依赖浏览器打印提示；分页必须优先发生在完整模块之间，不得把表格行、图表或模块标题直接截断到上下两页。
- 电量数据必须与数据面板平级且排在数据面板之前，面向客户账号展示充入电量、放出电量、本期电费、分时服务电费、基础服务费、保底核算和节约成本；其本期电费和节约成本必须复用数据报表同一套计算口径。
- 管理端低频模块应收纳在“其他”折叠菜单中；“其他”位于“计费规则”之后，其子菜单包含报警信息、车辆管理、充放电任务、小程序用户、用户授权、扫码刷卡记录和 EIOT 同步日志。

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
