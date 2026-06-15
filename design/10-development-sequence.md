# 开发实施顺序

本文档定义从设计进入实现时的推荐顺序。

## Step 1：准备 RuoYi 基座

目标：先让 RuoYi-Vue-Pro 能正常运行。

任务：

- 拉取 `YunaiV/ruoyi-vue-pro`
- 初始化数据库和 Redis
- 启动 `yudao-server`
- 启动 `yudao-ui`
- 登录管理端
- 确认目标数据库类型
- 确认是否开启租户

验收：

- 管理端可以登录
- 系统管理、菜单管理、字典管理可访问
- 后端接口文档可访问

## Step 2：导入移动储能基础 SQL

目标：让移动储能业务表、字典、菜单有基础结构。

任务：

- 核对目标 RuoYi 版本的系统表字段
- 调整 `energy_dict.sql`
- 调整 `energy_menu.sql`
- 执行 `energy_schema.sql`
- 执行字典和菜单 SQL
- 在管理端确认菜单和字典出现

验收：

- `energy_` 业务表存在
- 移动储能菜单出现
- 字典项可在系统字典中查看

## Step 3：创建 `yudao-module-energy`

目标：后端工程中有独立业务模块。

任务：

- 新建 `yudao-module-energy`
- 新建 `yudao-module-energy-api`
- 新建 `yudao-module-energy-biz`
- 在根 `pom.xml` 注册模块
- 在 `yudao-server` 引入 biz 依赖
- 添加模块自动配置或扫描配置

验收：

- 后端编译通过
- 启动无循环依赖和扫描错误

当前状态：

- 已采用单 Maven 模块 `yudao-module-energy` 完成第一阶段接入。
- 已加入根 `pom.xml` 和 `yudao-server/pom.xml`。
- 已通过后端构建、启动和 OpenAPI 验证。

## Step 4：生成第一批 CRUD

目标：快速形成管理端基础业务。

优先顺序：

1. 客户 `energy_customer`
2. 项目 `energy_project`
3. 设备 `energy_device`
4. 告警 `energy_alarm`
5. 计费规则 `energy_pricing_rule`
6. 充放电记录 `energy_charge_session`
7. EIOT 同步日志 `energy_eiot_sync_log`

验收：

- 管理端可增删改查客户、项目、设备
- 告警、计费、会话、同步日志可查询
- 权限按钮可按角色控制

当前状态：

- `energy_device` 后端第一版 CRUD 已完成。
- 管理端 `energy/device/index` 设备台账页面已完成。
- `energy_customer` 后端第一版 CRUD 已完成。
- 管理端 `energy/customer/index` 客户管理页面已完成。
- `energy_project` 后端第一版 CRUD 已完成。
- 管理端 `energy/project/index` 项目场站页面已完成。
- 告警、计费规则、充放电记录、EIOT 日志仍待实现。

## Step 5：打通 EIOT 接入

目标：worker 或安科瑞推送可以写入 RuoYi 后端。

任务：

- 实现 `/infra-api/energy/eiot/realtime`
- 实现 `/infra-api/energy/eiot/alarm`
- 实现请求鉴权
- 实现幂等处理
- 更新 `energy_device` 最新状态
- 写入 `energy_telemetry`
- 写入 `energy_alarm`
- 写入 `energy_eiot_sync_log`

验收：

- 模拟实时数据后，设备最新状态更新
- 模拟告警后，管理端告警列表出现记录
- 重复推送不会产生重复告警

## Step 5.5：打通 EIOT 反向控制

目标：本系统可以主动向 EIOT 下发控制指令。

任务：

- 建表 `energy_eiot_credential`、`energy_device_control_log`
- 实现 AES-128-CBC 加密工具类
- 实现 EIOT Token 获取和刷新
- 封装 SWITCH / REFRESH / FORCESWITCH 出向调用
- 实现操控日志写入
- 管理端设备操控页面和操控日志页面

验收：

- 管理端可对设备下发 SWITCH 合闸/分闸
- EIOT 返回结果正确记录到操控日志
- Token 过期自动刷新重试
- 操控日志可查询

## Step 6：管理端增强页面

目标：从 CRUD 变成可运营页面。

任务：

- 运营面板
- 设备实时详情
- 设备趋势图
- 告警确认/关闭
- 充放电记录结算
- 报表导出

验收：

- 运营人员能查看设备状态和告警
- 设备详情能看到关键实时参数
- 告警可以闭环处理

## Step 7：小程序接口适配

目标：当前 uni-app 小程序接入 RuoYi 后端。

任务：

- 调整登录方式
- 调整接口 baseUrl
- 调整设备接口字段
- 调整告警接口字段
- 调整记录/账单接口字段
- 做角色和数据权限过滤

验收：

- 小程序能登录
- 小程序能查看设备、告警、记录
- 客户只能看自己的数据

## Step 8：迁移历史数据

目标：逐步从 NestJS/Prisma 数据切换到 RuoYi。

任务：

- 导出客户、项目、设备
- 建立 UUID 到 bigint 的映射
- 导入客户、项目、设备
- 导入最近 30 天告警和会话
- 采集明细按需迁移
- 校验数量和关键字段

验收：

- RuoYi 端基础数据完整
- 小程序和管理端查询一致
- NestJS 后端可以进入只读或备用状态

## 推荐第一周目标

第一周不要做太多高级能力，目标压到可验收：

- RuoYi 基座跑起来
- 业务表建好
- `yudao-module-energy` 创建完成
- 客户、项目、设备 CRUD 可用
- EIOT 模拟实时数据能更新设备最新状态
