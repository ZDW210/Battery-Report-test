# 设计变更同步规则

## 基本规则

后续如果修改了 `current/design` 中的具体设计，必须同步检查本目录。

需要同步更新的情况包括：

- 新增或删除业务模块
- 修改后端模块边界
- 修改数据库表、字段、索引、字典
- 修改接口前缀、接口命名、响应结构
- 修改管理端菜单或页面结构
- 修改小程序角色、页面、权限范围
- 修改数据接入、归档、重试、幂等策略
- 修改实施路线和优先级

## 更新顺序

1. 先判断变更是否影响长期标准。
2. 如果影响标准，先更新 `current/design-standards`。
3. 再更新 `current/design` 中的具体方案。
4. 最后在变更说明中写明同步了哪些标准。

## 冲突处理

如果具体设计和标准冲突：

- 优先以本目录标准为准。
- 如果业务确实需要突破标准，必须先修改标准并说明原因。
- 不允许长期保留“设计文档一套、标准一套”的状态。

## 建议变更记录格式

```text
日期：
变更内容：
影响范围：
已同步标准：
备注：
```

```text
日期：2026-06-18
变更内容：修复 Worker 版管理端参数、设备字段和部署文档问题；前端 axios GET 参数序列化改回方括号索引格式，保证 collectTime[0]/collectTime[1] 被 Worker 正确解析；设备 CRUD 字段补充 runMode；最大需量费用直接使用逐规则累加金额取整，避免二次 demand*rate 产生 0.01 误差；README R2 桶名更新为 wrangler.jsonc 当前配置的 baobiao-eiot-archive。
影响范围：cloudflare-worker-package/admin-vue3/src/config/axios/service.ts，cloudflare-worker-package/worker/src/index.ts，cloudflare-worker-package/README.md，cloudflare-worker-package/design-standards/04-api-standards.md
已同步标准：04-api-standards.md 补充前端范围参数序列化格式和设备 runMode CRUD 要求。
备注：wrangler.jsonc 的实际 R2 bucket 配置未变，仅修正文档不一致。
```

```text
日期：2026-06-18
变更内容：修复 Worker 运行与部署隐患：EIOT 自动创建设备类型改为系统字典已有的电表类型 2；D1 初始迁移补齐计费规则费用和分时字段；客户老板账号创建/重置密码取消 123456 默认弱密码并增加前后端复杂度校验；设备在线状态刷新限定为当前设备列表结果，不再每次 GET 全表更新。
影响范围：cloudflare-worker-package/worker/src/index.ts，cloudflare-worker-package/migrations/0001_init.sql，cloudflare-worker-package/admin-vue3/src/views/energy/customerAccount，cloudflare-worker-package/design-standards/03-database-standards.md，cloudflare-worker-package/design-standards/04-api-standards.md，cloudflare-worker-package/design-standards/05-frontend-standards.md
已同步标准：03-database-standards.md 补充 D1 初始迁移字段完整性；04-api-standards.md 补充自动设备类型、在线状态刷新范围和客户密码规则；05-frontend-standards.md 补充客户账号密码表单规则。
备注：运行时 ensureColumns 继续作为旧库兼容兜底，但新库不应依赖首次页面访问补列。
```

```text
日期：2026-06-18
变更内容：修复 Worker EIOT 时间口径和乱序遥测区间计算问题；EIOT 采集/告警时间统一为中国本地业务时间，分时计费按本地小时匹配尖峰平谷，在线状态 cutoff 使用本地时间；遥测入库后重算当前采集点前后相邻区间，避免同设备报文乱序导致分时电量遗漏；旧报文晚到时不再覆盖设备最新采集时间。
影响范围：cloudflare-worker-package/worker/src/index.ts，cloudflare-worker-package/design-standards/04-api-standards.md
已同步标准：04-api-standards.md 补充 EIOT 本地时间口径和乱序遥测区间重算规则。
备注：系统 create_time/update_time 可继续使用 Worker UTC 文本，业务采集时间不得与其混用。
```

```text
日期：2026-06-18
变更内容：统一数据面板“用电量报表”和独立报表面板的账单数据来源；两处同名电量、购电成本、售电收入、放电等效电费和节约成本均以 /admin-api/energy/report/bill 返回值为准。Worker 账单接口改为按电表匹配计费规则后逐表计算分时购电成本、售电收入、最大需量费用和变压器容量费用，再汇总返回，避免按全范围平均电价导致场站/全部汇总失真。
影响范围：cloudflare-worker-package/worker/src/index.ts，cloudflare-worker-package/admin-vue3/src/api/energy/report/index.ts，cloudflare-worker-package/admin-vue3/src/views/energy/telemetry/index.vue，cloudflare-worker-package/admin-vue3/src/views/energy/report-panel/index.vue，cloudflare-worker-package/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充数据面板与报表面板同名指标必须共用 /energy/report/bill 的硬约束。
备注：数据面板仍可使用遥测接口绘制辅助曲线，但不得用前端本地计算覆盖核心账单指标。
```

```text
日期：2026-06-01
变更内容：补齐小程序首页旧统计请求的兼容接口，新增 /app-api/energy/devices/energy-delta、/app-api/energy/charging-sessions、/revenue-overview、/today-energy，当前返回授权范围内的 0 值或空列表
影响范围：current/backend-ruoyi/yudao-module-energy，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md
已同步标准：04-api-standards.md 补充小程序兼容版统计接口标准
备注：后端 compile、package、重启和四个接口 code=0 验证均已通过；真实充放电统计后续基于 energy_charge_session 落地
```

```text
日期：2026-06-01
变更内容：接入小程序正式登录第一版，新增 energy_app_user 表和 /app-api/energy/auth/login，登录成功后生成 RuoYi OAuth2 MEMBER token；小程序端切换到本地 /app-api/energy，登录、首页概览、设备列表使用真实 token。
影响范围：current/backend-ruoyi/yudao-module-energy，current/miniprogram/src，current/design/sql/mysql/energy_schema.sql，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/03-database-standards.md，current/design-standards/04-api-standards.md
已同步标准：03-database-standards.md 补充 App 用户表和密码存储标准；04-api-standards.md 补充 App 登录 token 标准
备注：该阶段曾使用本地演示账号验证；后续客户版已切换为微信登录，固定演示账号和授权种子数据已移除
```

```text
日期：2026-06-01
变更内容：管理端 Vue3 改为移动储能项目界面，新增运营首页，标题改为移动储能运营管理平台；登录页隐藏手机号/二维码/注册/第三方登录和开源外链；前端菜单过滤隐藏无关 RuoYi 示例/通用业务模块，仅默认展示移动储能和系统管理；移动储能二级菜单仅保留已落地的设备、告警、客户、项目、用户授权。
影响范围：current/admin-vue3/.env.local，current/admin-vue3/src/views/Home/Index.vue，current/admin-vue3/src/views/Login，current/admin-vue3/src/store/modules/permission.ts，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充管理端首页、登录页和菜单白名单标准
备注：ESLint 已通过；pnpm build:local 已通过；管理端 Vite 已重启，当前地址 http://localhost:81/
```

```text
日期：2026-06-01
变更内容：新增管理端“运营面板”页面，复用现有设备、客户、项目、告警接口聚合展示运营总览、运行负载、SOC 分布、功率排行、待处理告警和后续业务能力状态，并将移动储能菜单白名单放开 dashboard。
影响范围：current/admin-vue3/src/views/energy/dashboard/index.vue，current/admin-vue3/src/views/Home/Index.vue，current/admin-vue3/src/store/modules/permission.ts，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 将运营面板列为已落地默认入口
备注：当前运营面板先使用现有 CRUD/分页接口聚合，实时遥测、EIOT 日志、计费、充放电、调度后续逐步落地
```

```text
日期：2026-06-01
变更内容：根据安科瑞 EIOT 对接标准第 3 章（数据访问），补齐 EIOT 双向控制设计——本系统不仅被动接收 EIOT 推送，还需主动向 EIOT 下发 SWITCH/FORCESWITCH/REFRESH 等控制指令；新增 AES-128-CBC 加密登录、Token 管理、充放电会话 8 状态机、会话与 SWITCH 联动、电能快照计费闭环；新增 energy_eiot_credential 和 energy_device_control_log 两张表；管理端和小程序新增设备操控接口
影响范围：current/design/01-architecture.md（数据流改双向）、02-backend-modules.md（新增 EIOT 控制 Service）、03-database-design.md（会话表字段拆分 + 2 张新表 + 字典更新）、04-api-design.md（设备操控 + 会话启停 + EIOT 出向调用）、05-admin-ui-design.md（菜单新增设备操控/操控日志/充放电会话/EIOT 凭证）、06-miniprogram-design.md（设备操控能力）、07-implementation-roadmap.md（新增 Phase 2.5 EIOT 反向控制）、08-ruoyi-module-scaffold.md（EIOT 控制包结构 + 权限标识）、10-development-sequence.md（Step 5.5 反向控制）、11-api-contracts-v1.md（6 组新接口契约）；新增 14-eiot-bidirectional-control.md
已同步标准：03-database-standards.md 补充凭证表和操控日志表索引；04-api-standards.md 新增 EIOT 出向调用标准
备注：充放电会话 API、操控 API、EIOT 凭证管理 API 均待后端实现
```

```text
日期：2026-06-01
变更内容：新增管理端”实时监控”页面，基于设备最新采集值展示设备状态、SOC、功率、温度、电压、电流、更新时间、状态筛选和关键字搜索，并将移动储能菜单白名单放开 telemetry。
影响范围：current/admin-vue3/src/views/energy/telemetry/index.vue，current/admin-vue3/src/views/Home/Index.vue，current/admin-vue3/src/views/energy/dashboard/index.vue，current/admin-vue3/src/store/modules/permission.ts，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 将实时监控列为已落地默认入口，并补充实时监控第一版数据源标准
备注：当前实时监控先复用设备台账最新采集值，后续接入实时遥测流后替换数据源
```

```text
日期：2026-06-01
变更内容：新增管理端 EIOT 同步日志分页接口和 Vue3 列表页，支持按同步类型、状态、请求编号、网关序列号、电表序列号、创建时间过滤，并展示失败原因和报文归档地址；移动储能菜单白名单放开 eiot-log。
影响范围：current/backend-ruoyi/yudao-module-energy，current/admin-vue3/src/api/energy/eiotLog，current/admin-vue3/src/views/energy/eiot-log/index.vue，current/admin-vue3/src/store/modules/permission.ts，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充 EIOT 同步日志管理端接口标准；05-frontend-standards.md 将 EIOT 同步日志列为已落地默认入口
备注：后端 compile、server package、接口实测 code=0、前端 ESLint/build:local 均已通过
```

## 当前基准版本

```text
版本：v0.1
日期：2026-05-29
范围：移动储能系统基于 RuoYi-Vue-Pro 的初始设计标准
```

## 变更记录

```text
日期：2026-06-15
变更内容：新增客户老板网页账号权限；运营人员可预设客户老板后台登录账号、绑定客户并按板块开放网页端菜单。
影响范围：backend-ruoyi/yudao-module-energy、admin-vue3/src/views/energy/customerAccount、design/sql/mysql/energy_customer_account_menu.sql
已同步标准：04-api-standards.md 补充客户老板网页账号接口与权限边界；05-frontend-standards.md 补充客户账号权限页面规则；design/05-admin-ui-design.md 补充页面设计。
备注：本机 MySQL 已创建 energy_customer_account 并写入客户账号权限菜单；后端编译、管理端 ESLint 和 build:local 已通过。
```

```text
日期：2026-05-29
变更内容：新增 RuoYi 模块落地蓝图、MySQL 业务表脚本、字典脚本、菜单权限脚本
影响范围：ruoyi-energy-design/08-ruoyi-module-scaffold.md，ruoyi-energy-design/sql/mysql
已同步标准：03-database-standards.md 补充初始化脚本位置和设计 ID 号段
备注：SQL 执行前仍需按实际 RuoYi-Vue-Pro 版本核对系统表字段
```

```text
日期：2026-05-29
变更内容：新增现有项目迁移映射、开发实施顺序、首版接口契约
影响范围：ruoyi-energy-design/09-current-project-migration-map.md，10-development-sequence.md，11-api-contracts-v1.md
已同步标准：本变更属于具体落地方案补充，未改变长期标准；已记录变更
备注：下一步可进入 RuoYi 基座拉取和模块创建
```

```text
日期：2026-05-29
变更内容：记录 RuoYi 主仓库、Vue3 管理端仓库拉取结果和本机启动阻塞项
影响范围：ruoyi-energy-design/12-ruoyi-bootstrap-status.md
已同步标准：本变更属于环境状态记录，未改变长期标准；已记录变更
备注：当前阻塞为 MySQL 账号、Redis、Maven/JDK 方案
```

```text
日期：2026-05-29
变更内容：RuoYi 主仓库切换到 master-jdk17，并更新基座状态中的 Java/Spring Boot 版本和下一步环境建议
影响范围：ruoyi-energy-design/12-ruoyi-bootstrap-status.md
已同步标准：本变更属于环境状态记录，未改变长期标准；已记录变更
备注：后续建议安装 JDK 17 和 Maven
```

```text
日期：2026-05-29
变更内容：记录本机 RuoYi 后端、Vue3 管理端、MySQL、Garnet/Redis 的可运行基线；新增管理端 pnpm 安装约束
影响范围：ruoyi-energy-design/13-local-runtime-status.md，ruoyi-energy-design-standards/07-local-runtime-standards.md
已同步标准：新增 07-local-runtime-standards.md，明确 JDK 17、Maven、MySQL 专用账号、Redis 兼容服务、端口和 pnpm --ignore-workspace 要求
备注：当前后端地址 http://localhost:48080，管理端地址 http://localhost:81/
```

```text
日期：2026-05-30
变更内容：创建并接入 yudao-module-energy，完成后端 Maven 构建、后台启动和模块探测接口验证
影响范围：ruoyi-energy-design/08-ruoyi-module-scaffold.md，ruoyi-energy-design/13-local-runtime-status.md，ruoyi-vue-pro/pom.xml，ruoyi-vue-pro/yudao-server/pom.xml，ruoyi-vue-pro/yudao-module-energy
已同步标准：本变更沿用现有模块边界和运行标准，未改变长期标准；已记录变更
备注：/admin-api/energy/module-info/runtime 已注册并受 RuoYi 管理端登录校验保护；下一步进入 energy_device 设备中心接口实现
```

```text
日期：2026-05-30
变更内容：实现 energy_device 设备中心第一版后端接口，包含 DO、Mapper、Service、Controller、分页/详情/新增/修改/删除和唯一性校验
影响范围：ruoyi-energy-design/11-api-contracts-v1.md，ruoyi-energy-design/13-local-runtime-status.md，ruoyi-vue-pro/yudao-module-energy
已同步标准：本变更沿用现有接口命名、权限标识、多租户和运行标准，未改变长期标准；已记录变更
备注：设备接口已进入 OpenAPI；customerName/projectName 需等待客户/项目接口落地后补充聚合展示
```

```text
日期：2026-05-30
变更内容：实现 Vue3 管理端移动储能设备台账页面，包含查询、分页、新增、修改、删除、字典渲染和设备表单
影响范围：ruoyi-energy-design/10-development-sequence.md，ruoyi-energy-design/13-local-runtime-status.md，yudao-ui-admin-vue3/src/api/energy/device，yudao-ui-admin-vue3/src/views/energy/device，yudao-ui-admin-vue3/src/utils/dict.ts
已同步标准：本变更沿用现有管理端页面结构、权限标识和字典标准，未改变长期标准；已记录变更
备注：管理端本地构建已通过；导出按钮暂未加入，等待后端 export-excel 接口落地
```

```text
日期：2026-05-30
变更内容：实现 energy_customer、energy_project 后端接口和 Vue3 管理端页面，并将设备表单客户/项目字段改为下拉选择
影响范围：ruoyi-energy-design/10-development-sequence.md，ruoyi-energy-design/13-local-runtime-status.md，ruoyi-vue-pro/yudao-module-energy，yudao-ui-admin-vue3/src/api/energy/customer，yudao-ui-admin-vue3/src/api/energy/project，yudao-ui-admin-vue3/src/views/energy/customer，yudao-ui-admin-vue3/src/views/energy/project，yudao-ui-admin-vue3/src/views/energy/device/DeviceForm.vue
已同步标准：本变更沿用现有接口命名、权限标识、字典和管理端页面标准，未改变长期标准；已记录变更
备注：后端 OpenAPI 和管理端本地构建已通过；下一步建议补设备列表客户名称/项目名称聚合展示和告警中心
```

```text
日期：2026-05-30
变更内容：补充设备分页/详情响应中的 customerName、projectName 聚合字段，并在管理端设备列表展示客户名称和项目名称
影响范围：ruoyi-energy-design/11-api-contracts-v1.md，ruoyi-energy-design/13-local-runtime-status.md，ruoyi-vue-pro/yudao-module-energy，yudao-ui-admin-vue3/src/api/energy/device/index.ts，yudao-ui-admin-vue3/src/views/energy/device/index.vue
已同步标准：本变更属于接口响应字段和管理端展示字段补充，沿用现有 API 契约、字段命名和页面展示标准，未改变长期标准；已记录变更
备注：后端 package、OpenAPI 验证和管理端 build:local 已通过；下一步进入告警中心
```

```text
日期：2026-05-30
变更内容：实现告警中心第一版后端接口和 Vue3 管理端页面，包含分页、详情、确认、关闭、设备名称聚合展示和字典渲染
影响范围：ruoyi-energy-design/11-api-contracts-v1.md，ruoyi-energy-design/13-local-runtime-status.md，ruoyi-vue-pro/yudao-module-energy，yudao-ui-admin-vue3/src/api/energy/alarm/index.ts，yudao-ui-admin-vue3/src/views/energy/alarm/index.vue
已同步标准：本变更沿用现有接口命名、权限标识、字典、租户基类和管理端页面结构标准；未改变长期标准；已记录变更
备注：后端 compile/package、OpenAPI 验证、管理端 ESLint/build:local 均已通过；确认/关闭备注当前作为兼容字段接收，待告警处理记录表落地后持久化
```

```text
日期：2026-05-31
变更内容：实现 EIOT 告警接入、同步日志写入、告警处理记录表和确认/关闭备注持久化；修复 energy 模块非自增 bigint 主键新增失败问题，统一显式使用 IdType.ASSIGN_ID
影响范围：ruoyi-energy-design/sql/mysql/energy_schema.sql，ruoyi-energy-design/11-api-contracts-v1.md，ruoyi-energy-design/13-local-runtime-status.md，ruoyi-energy-design-standards/03-database-standards.md，ruoyi-vue-pro/yudao-module-energy，yudao-ui-admin-vue3/src/views/energy/alarm/index.vue
已同步标准：03-database-standards.md 补充非自增 bigint 主键必须显式声明 @TableId(type = IdType.ASSIGN_ID)，并补充告警处理记录索引标准
备注：后端 compile/package、管理端 ESLint/build:local、本地 MySQL 建表和 /infra-api/energy/eiot/alarm 失败路径联调均已通过；成功创建路径待真实 meterNo 或测试设备数据联调
```

```text
日期：2026-05-31
变更内容：修复 EIOT 告警接入 occurTime 字符串被 RuoYi 全局 LocalDateTime epoch millis 反序列化为 1970 的问题；EIOT 请求体时间字段改为 String 接收并在 Service 层按 yyyy-MM-dd HH:mm:ss 显式解析
影响范围：ruoyi-vue-pro/yudao-module-energy，ruoyi-energy-design/11-api-contracts-v1.md，ruoyi-energy-design-standards/04-api-standards.md
已同步标准：04-api-standards.md 补充外部接入请求体时间字段标准，禁止将 yyyy-MM-dd HH:mm:ss 字符串直接绑定为 LocalDateTime
备注：该修复保持 EIOT occurTime 字符串契约不变，并补充明确 yyyy-MM-dd HH:mm:ss 格式与解析失败处理规则
```

```text
日期：2026-05-31
变更内容：实现小程序端首页概览和设备列表第一版后端接口，新增 /app-api/energy/home/overview 与 /app-api/energy/device/list
影响范围：ruoyi-vue-pro/yudao-module-energy，ruoyi-energy-design/11-api-contracts-v1.md，ruoyi-energy-design-standards/04-api-standards.md
已同步标准：04-api-standards.md 补充小程序第一版授权过渡标准；11-api-contracts-v1.md 标注当前按租户级返回数据，后续按用户-客户/项目/设备授权表收窄
备注：首页充/放电量在充放电记录/计费表落地前返回 0；设备列表第一版最多返回 100 条
```

```text
日期：2026-05-31
变更内容：新增 energy_user_scope 用户授权范围表，并将小程序首页概览、设备列表接口从租户级返回收窄为按登录 App 用户授权客户/项目/设备过滤
影响范围：ruoyi-vue-pro/yudao-module-energy，ruoyi-energy-design/sql/mysql/energy_schema.sql，ruoyi-energy-design/11-api-contracts-v1.md，ruoyi-energy-design-standards/03-database-standards.md，ruoyi-energy-design-standards/04-api-standards.md
已同步标准：03-database-standards.md 补充小程序用户授权索引标准；04-api-standards.md 补充 energy_user_scope 授权过滤标准
备注：无启用授权记录时返回空数据，不回退租户全量；本地已给临时 App 用户 202605310001 授权测试设备 202605310003
```

```text
日期：2026-05-31
变更内容：实现 energy_user_scope 管理端维护入口，新增用户授权后台接口、设备精简列表接口、Vue3 用户授权页面和菜单权限
影响范围：ruoyi-vue-pro/yudao-module-energy，yudao-ui-admin-vue3/src/api/energy，yudao-ui-admin-vue3/src/views/energy/userScope，ruoyi-energy-design/sql/mysql/energy_menu.sql，ruoyi-energy-design/11-api-contracts-v1.md，ruoyi-energy-design-standards/04-api-standards.md，ruoyi-energy-design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充用户授权后台接口标准；05-frontend-standards.md 补充用户授权菜单和页面原则
备注：第一版按 App 用户 ID 手工维护授权，正式小程序登录和会员用户选择器后续接入
```

```text
日期：2026-05-31
变更内容：整理移动储能项目目录，将后端、管理端、小程序、设计文档、设计标准、参考资料统一迁移到 D:\code\ankerui\energy-storage-platform
影响范围：项目物理路径、current/design/13-local-runtime-status.md、current/design/12-ruoyi-bootstrap-status.md、current/design/09-current-project-migration-map.md、current/design/README.md、current/design-standards/07-local-runtime-standards.md
已同步标准：07-local-runtime-standards.md 更新设计文档和 SQL 的新路径要求；README 更新设计文档目录说明
备注：当前主线位于 current，旧 NestJS/Worker/minidatt 等项目归档到 legacy；微信开发者工具后续打开 current/miniprogram/dist/build/mp-weixin
```

```text
日期：2026-06-04
变更内容：实现管理端计费规则第一版后端 CRUD 接口和 Vue3 管理页面，支持客户/项目/设备三选一计费范围、时间单价、电量单价、生效时间、启停状态和删除确认；移动储能菜单白名单放开 pricing-rule，并补齐本地超级管理员移动储能菜单授权。
影响范围：current/backend-ruoyi/yudao-module-energy，current/admin-vue3/src/api/energy/pricingRule，current/admin-vue3/src/views/energy/pricing-rule，current/admin-vue3/src/store/modules/permission.ts，current/design/sql/mysql/energy_menu.sql，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充计费规则管理端接口标准；05-frontend-standards.md 将计费规则列为已落地默认入口并补充页面原则
备注：后端 compile/package、计费规则分页接口实测、前端 ESLint/build:local 均已通过；本地后端启动前需先启动 Garnet/Redis 6379
```

```text
日期：2026-06-04
变更内容：根据现场可收到的 EIOT 两类真实报文调整接入实现：新增实时电表数据 /infra-api/energy/eiot/realtime，兼容告警 list[] 与中英文 title/message；实时数据写入 energy_telemetry 并刷新设备最新功率/电压/电流/状态；设备匹配支持 meterNo 和 gatewaySn+meterSn 两种方式。
影响范围：current/backend-ruoyi/yudao-module-energy，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md
已同步标准：04-api-standards.md 补充当前 EIOT 真实可接收报文标准，明确实时数据不包含 SOC/SOH/BMS 温度，告警标题内容优先取 zh_CN
备注：后端 compile/package 通过；使用真实结构实时和告警报文联调通过；仅有 EPI 时不能完整计算放电电量，后续需确认 EIOT 是否可提供 EPE 或额外 BMS/PCS 数据
```

```text
日期：2026-06-04
变更内容：扩展计费规则开发，新增按设备和计费时间匹配当前生效规则的后端接口、Service 匹配方法和管理端试算入口；运营面板将计费规则状态调整为已落地。
影响范围：current/backend-ruoyi/yudao-module-energy，current/admin-vue3/src/api/energy/pricingRule，current/admin-vue3/src/views/energy/pricing-rule，current/admin-vue3/src/views/energy/dashboard，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 固定计费匹配接口、优先级和复用要求；05-frontend-standards.md 补充计费规则页试算入口要求
备注：后端重新打包并重启通过；临时创建设备级规则后 `/admin-api/energy/pricing-rule/match` 命中正确规则，测试后已删除临时规则；前端 ESLint 和 `pnpm build:local` 均通过
```

```text
日期：2026-06-04
变更内容：补齐实时监控页的 EIOT 实时报文数据板块，新增管理端实时采集数据分页接口；energy_telemetry 扩展保存 gatewaySn、meterSn、timestamp、source、state、Pa/Pb/Pc 等实时报文字段。
影响范围：current/backend-ruoyi/yudao-module-energy，current/admin-vue3/src/api/energy/telemetry，current/admin-vue3/src/views/energy/telemetry，current/design/sql/mysql/energy_schema.sql，current/design/03-database-design.md，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充 EIOT 租户上下文和实时采集数据保留字段；05-frontend-standards.md 补充实时监控页实时报文数据板块要求
备注：本地 MySQL 已补字段和索引；EIOT 接入口未传 tenant-id 时默认租户 1；后端 compile/package/restart 通过；使用本地测试设备发送实时电表报文后 `/admin-api/energy/telemetry/page` 返回完整字段；前端 ESLint 和 `pnpm build:local` 均通过。现场 `122070...` 报文需先在设备台账建立匹配的 meterNo 或 gatewaySn+meterSn 才会入库展示

---

日期：2026-06-04
变更内容：按用户确认的设计重构管理端实时监控页，新增电表卡片入口、电表实时详情、数据查看（日原始数据/逐日极值数据）和详细实时数据板块；后端新增 `/admin-api/energy/telemetry/chart` 原始采集点接口和 `/admin-api/energy/telemetry/daily-stat` 逐日极值接口。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/telemetry，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/telemetry，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/mysql/telemetry，current/admin-vue3/src/api/energy/telemetry，current/admin-vue3/src/views/energy/telemetry，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充遥测图表/极值接口标准和真实字段限制；05-frontend-standards.md 补充实时监控四个板块的页面设计标准；11-api-contracts-v1.md 补充 `/chart` 与 `/daily-stat` 契约；13-local-runtime-status.md 补充本次板块重构记录
备注：当前真实指标仅限 `pa/pb/pc/p`、`ua/ub/uc`、`ia/ib/ic`、`pf`、`epi`；线电压、频率、无功功率、视在功率、温度、开关量等字段暂无报文来源，不展示为真实数据

---

日期：2026-06-05
变更内容：调整管理端实时监控页布局，将设备监控面板、数据查看、详细实时数据设置为实时监控下的三个子板块；统计卡片放入每个子板块顶部；电表实时详情改为仅在设备卡片点击“详情”后弹窗查看，不再作为固定板块常驻页面；日原始数据和逐日极值数据增加“图表 / 数据”切换、表格展示和 CSV 导出；日原始数据按查询时间范围固定 5 分钟一行生成时间轴，缺失值显示 `--`。
影响范围：current/admin-vue3/src/views/energy/telemetry/index.vue，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md，current/design-standards/06-change-sync-rules.md
已同步标准：05-frontend-standards.md 明确实时监控三个子板块、统计卡片位置、详情触发方式和数据表格导出要求；13-local-runtime-status.md 补充布局调整记录
备注：本次为前端布局和交互调整，不涉及后端接口和数据库结构变更
```

```text
日期：2026-06-05
变更内容：按现场参考图调整管理端“逐日极值数据”，新增参数单选区、连续日期补齐、多级表头和最大/最小值发生时间展示；后端逐日极值接口补充 `maxTime`、`minTime`。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/telemetry/vo/EnergyTelemetryDailyStatRespVO.java，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/telemetry/EnergyTelemetryServiceImpl.java，current/admin-vue3/src/api/energy/telemetry/index.ts，current/admin-vue3/src/views/energy/telemetry/index.vue，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 明确逐日极值接口返回发生时间；05-frontend-standards.md 明确逐日极值参数单选、连续日期补齐、多级表头和导出字段
备注：前端只补齐日期行，不伪造采集值；缺失日期的最大值、最小值、发生时间和平均值统一显示 `--`
```

```text
日期：2026-06-05
变更内容：实现管理端充放电会话第一版，新增后端分页/详情/开始/结束/结算接口和 Vue3 充放电记录页面；会话创建复用计费规则匹配，结束时计算电量、时长和费用；移动储能菜单白名单放开 charge-session，并补充开始/结束/结算权限。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/session，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/session，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/dataobject/session，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/mysql/session，current/admin-vue3/src/api/energy/chargeSession，current/admin-vue3/src/views/energy/charge-session，current/admin-vue3/src/store/modules/permission.ts，current/design/sql/mysql/energy_menu.sql，current/design/03-database-design.md，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充充放电会话接口和结算规则；05-frontend-standards.md 补充充放电记录页面入口和页面原则；13-local-runtime-status.md 记录第一版能力和约束
备注：第一版暂未对接 EIOT SWITCH 实控；当前 EIOT 真实报文只有 `EPI`，放电电量需待 `EPE` 或 PCS/BMS 放电电能字段确认后严格计算
```

```text
日期：2026-06-06
变更内容：调整管理端进入网页时的首屏加载动画，将 RuoYi/Yudao 默认 Logo 和双圈旋转替换为移动储能项目风格的电池能量条动效。
影响范围：current/admin-vue3/index.html，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充管理端首屏加载动画必须使用移动储能项目风格
备注：`pnpm build:local` 已通过；刷新管理端页面即可看到新加载动画
```

```text
日期：2026-06-06
变更内容：替换管理端侧边栏“移动储能运营管理平台”标题旁的默认兔子头像，改为移动储能项目专用电池/能量标识。
影响范围：current/admin-vue3/src/layout/components/Logo/src/Logo.vue，current/admin-vue3/src/assets/imgs/energy-logo.svg，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充侧边栏平台标识不得使用默认头像或与能源设备无关素材
备注：本次只替换侧边栏标题旁标识，暂不影响登录页等仍引用原 logo.png 的位置
```

```text
日期：2026-06-06
变更内容：修复管理端充放电记录页开始任务 500 问题；后端补充设备客户归属和手动计费规则适用性校验，前端按所选设备过滤计费规则并提示未绑定客户设备不能开始任务。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/enums/ErrorCodeConstants.java，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/session/EnergyChargeSessionServiceImpl.java，current/admin-vue3/src/views/energy/charge-session/index.vue，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充开始会话客户归属和计费规则适用性校验；05-frontend-standards.md 补充开始任务计费规则过滤和未绑定客户提示要求
备注：后端 compile/package/restart 通过；前端 ESLint 和 `pnpm build:local` 通过；接口验证未绑定客户设备返回业务提示 `1061107006`，不再返回 500
```

```text
日期：2026-06-06
变更内容：按参考图调整管理端实时监控页设备监控面板，改为能源物联网监控工作台布局：顶部平台标题和告警计数、左侧项目概览栏、右侧状态/节点/能源类型筛选、矩阵卡片和表格模式切换。
影响范围：current/admin-vue3/src/views/energy/telemetry/index.vue，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充实时监控工作台布局、矩阵卡片和表格模式辅助查看要求
备注：电表实时详情仍由卡片详情触发；未收到来源的日用电、碳排等指标不伪造为真实数据
```

```text
日期：2026-06-06
变更内容：调整电表实时详情为参考图风格的实时数据详情页，使用青色标题参数卡片展示当前真实可接收字段，并保留报文识别信息。
影响范围：current/admin-vue3/src/views/energy/telemetry/index.vue，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充电表实时详情页式布局和真实字段限制
备注：只展示 `Ua/Ub/Uc`、`Ia/Ib/Ic`、`Pa/Pb/Pc/P`、`PF`、`EPI` 以及网关/仪表/报文信息；不展示未收到的线电压、无功功率、视在功率、频率、温度、开关量等字段
```

```text
日期：2026-06-06
变更内容：补充电表实时详情下方“历史曲线”和“报警信息”页签；历史曲线按当前电表、日期和参数类型查询遥测曲线并按 5 分钟时间轴展示，报警信息按当前电表查询真实告警分页。
影响范围：current/admin-vue3/src/views/energy/telemetry/index.vue，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充电表实时详情下方历史曲线和报警信息展示要求
备注：本次复用现有 telemetry chart 与 alarm page 接口，不新增后端接口和数据库字段；缺失采集值保持空值，不伪造数据
```

```text
日期：2026-06-06
变更内容：在当前项目中新建独立微信小程序板块 current/wechat-miniprogram-section，复制现有小程序 src、dist 和工程配置，作为后续微信登录、客户区分和授权范围选择的开发位置。
影响范围：current/wechat-miniprogram-section，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充微信小程序改造优先在独立板块内开发，避免直接修改原始 current/miniprogram/src
备注：原始 current/miniprogram/src 未修改；微信开发者工具打开路径为 current/wechat-miniprogram-section/dist/build/mp-weixin
```

```text
日期：2026-06-07
变更内容：在独立微信小程序板块内完成微信登录和客户范围选择前端初版；登录页改为微信登录优先，账号密码仅作本地联调；新增客户范围页，首页展示当前客户范围并新增客户入口；请求层预留 x-energy-customer-id 请求头和 customerId 查询参数。
影响范围：current/wechat-miniprogram-section/src，current/wechat-miniprogram-section/README.md，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充微信登录主入口、客户范围确认和后端授权校验要求
备注：本次已通过 npm run build:mp-weixin；后端仍需补 /app-api/energy/auth/wechat-login，并在 App 端接口中正式校验 customerId
```

```text
日期：2026-06-07
变更内容：补齐微信小程序正式登录后端闭环，新增 /app-api/energy/auth/wechat-login；首次微信登录按 openid 自动创建 energy_app_user 并复用 RuoYi system_social_user 绑定，管理端用户授权改为自动加载 App 用户选择器，只需给新用户开放客户/项目/设备权限。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/auth，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/auth，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/mysql/auth，current/admin-vue3/src/views/energy/userScope，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充微信登录自动建 App 用户和用户授权自动选择标准；05-frontend-standards.md 补充用户授权不得手动录入 App 用户 ID
备注：微信 code 换 openid 复用 RuoYi 社交用户能力；运营端后续只做授权开放，不再手动新增小程序用户
```

```text
日期：2026-06-07
变更内容：对齐微信小程序 AppID 配置，将后端 wx.miniapp.appid 和独立小程序板块 manifest.json 的 mp-weixin.appid 统一为当前微信开发者工具使用的小程序 AppID，修复微信 code 换 openid 时因 AppID/Secret 不匹配导致 invalid code 的问题。
影响范围：current/backend-ruoyi/yudao-server/src/main/resources/application-local.yaml，current/wechat-miniprogram-section/src/manifest.json，current/design/13-local-runtime-status.md
已同步标准：13-local-runtime-status.md 记录本地小程序 AppID 对齐和重启/重构建要求
备注：AppSecret 属于本地运行密钥，不写入设计标准正文；后续已调整为通过 ENERGY_WECHAT_APP_SECRET 环境变量注入
```

```text
日期：2026-06-07
变更内容：按客户视角调整微信小程序登录流程，登录成功后直接进入首页；移除客户范围页路由、首页授权范围提示和客户范围入口，设备列表不再按前端客户选择二次过滤，统一以后端用户授权结果控制数据范围。
影响范围：current/wechat-miniprogram-section/src/pages/auth/login.vue，current/wechat-miniprogram-section/src/pages.json，current/wechat-miniprogram-section/src/pages/index/index.vue，current/wechat-miniprogram-section/src/stores/device.ts，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充微信登录成功直接进首页、客户不展示授权范围配置页
备注：新用户仍会在首次微信登录时自动创建 App 用户；管理端负责授权开放，未授权时小程序只显示空业务数据
```

```text
日期：2026-06-07
变更内容：将管理端运营面板图表标题“SOC 水位分布”调整为“SOC”。
影响范围：current/admin-vue3/src/views/energy/dashboard/index.vue，current/design/13-local-runtime-status.md
已同步标准：13-local-runtime-status.md 记录本次展示文案调整
备注：仅调整页面标题文案，不改变统计逻辑
```

```text
日期：2026-06-07
变更内容：落地管理端电表分合闸第一版，实时详情顶部新增合闸/开闸/刷新状态按钮；后端新增 /admin-api/energy/device/control 和 energy_device_control_log 操控日志；本地配置新增 energy.eiot.control-url/control-token 占位。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/device，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/device，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal，current/admin-vue3/src/api/energy/device，current/admin-vue3/src/views/energy/telemetry，current/backend-ruoyi/yudao-server/src/main/resources/application-local.yaml，current/design/sql/mysql/energy_schema.sql，current/design/sql/mysql/energy_menu.sql，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充设备控制接口和未配置 EIOT 地址的失败规则；05-frontend-standards.md 补充详情顶部设备控制按钮和二次确认要求
备注：第一版只开放 SWITCH 合闸/开闸和 REFRESH；未配置真实 EIOT 控制地址时写日志并返回明确失败提示，不伪造控制成功
```

```text
日期：2026-06-08
变更内容：将管理端运营入口展示名称统一调整为“运营面板”，同步首页快捷入口、移动储能侧边栏菜单初始化 SQL、运营页面标题和加载失败提示。
影响范围：current/admin-vue3/src/views/energy/dashboard/index.vue，current/admin-vue3/src/views/Home/Index.vue，current/design/sql/mysql/energy_menu.sql，current/design，current/design-standards
已同步标准：05-frontend-standards.md 默认菜单名称改为“运营面板”；06-change-sync-rules.md 记录本次命名调整
备注：仅调整展示名称；路由 path、组件名、接口路径仍保持 dashboard，避免影响已有权限和接口调用。
```

```text
日期：2026-06-08
变更内容：在管理端运营面板下方新增“数据面板”，支持选择电表、时间范围和数据类型，并用折线图展示 EIOT 已接收并入库的遥测数据。
影响范围：current/admin-vue3/src/views/energy/dashboard/index.vue，current/design/05-admin-ui-design.md，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充运营面板数据面板要求；05-admin-ui-design.md 补充数据面板设计
备注：复用 /admin-api/energy/telemetry/chart，不新增后端接口；仅展示 Pa/Pb/Pc/P、Ua/Ub/Uc、Ia/Ib/Ic、PF、EPI 等真实接收字段。
```

```text
日期：2026-06-09
变更内容：充放电任务开始/结束联动设备控制服务；开始任务自动下发 SWITCH=1 合闸，结束任务自动下发 SWITCH=0 开闸，控制失败时阻止任务状态流转并返回明确失败原因。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/session，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/session，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/enums/ErrorCodeConstants.java，current/design/13-local-runtime-status.md，current/design-standards/05-frontend-standards.md
已同步标准：05-frontend-standards.md 补充充放电任务必须由后端联动设备 SWITCH 控制且失败不流转任务状态
备注：复用已落地的 /admin-api/energy/device/control 服务和 energy_device_control_log；仍需配置真实 EIOT 控制地址/Token 后进行现场联调。
```

```text
日期：2026-06-09
变更内容：新增小程序扫码车辆/设备校验和扫码放电入口；新增 energy_vehicle 车辆绑定表，扫码后按当前微信登录账号的 energy_user_scope 授权范围校验绑定设备，确认后发起放电会话并复用后端任务控制联动。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/session，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/app，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal，current/wechat-miniprogram-section/src/api/customers.ts，current/wechat-miniprogram-section/src/pages/index/index.vue，current/design/sql/mysql/energy_schema.sql，current/design/04-api-design.md，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充小程序扫码放电接口；05-frontend-standards.md 补充扫码必须后端校验账号和车辆/设备授权
备注：二维码优先携带 vehicleId/vehicleNo/plateNo/qrCode，也兼容 deviceId/deviceNo/meterNo/gatewaySn/meterSn；后端再次校验，不信任前端扫码结果。
```

```text
日期：2026-06-10
变更内容：将扫码主语义统一为设备/充电桩二维码 + 账户识别；新增 energy_account_event 记录扫码校验、开始放电和后续刷卡识别事件；管理端新增“扫码刷卡记录”页面。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/auth，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/app，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal，current/admin-vue3/src/api/energy/accountEvent，current/admin-vue3/src/views/energy/account-event，current/wechat-miniprogram-section/src/api/customers.ts，current/design/sql/mysql/energy_schema.sql，current/design/sql/mysql/energy_account_event_menu.sql，current/design/15-account-scan-card-flow.md，current/design-standards/08-account-identification-standards.md
已同步标准：08-account-identification-standards.md 补充扫码刷卡记录要求；15-account-scan-card-flow.md 补充二维码推荐内容和管理端记录页面。
备注：车辆绑定字段仅作为旧二维码兼容；新二维码不保存司机身份，不代表车辆，只负责定位设备/仪表。
```

```text
日期：2026-06-13
变更内容：新增 EIOT 设备侧刷卡校验与刷卡启动放电接口，充电桩可使用 cardNo + scanText 上报刷卡事件，不依赖小程序登录态；接口仍复用 App 用户启停状态、用户授权范围、扫码刷卡记录和充放电任务控制联动。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/eiot，current/design/04-api-design.md，current/design/11-api-contracts-v1.md，current/design/15-account-scan-card-flow.md，current/design-standards/04-api-standards.md
已同步标准：04-api-standards.md 补充 /infra-api/energy/eiot/card/verify 与 /infra-api/energy/eiot/card/discharge；15-account-scan-card-flow.md 补充设备侧刷卡请求和鉴权说明。
备注：后端 yudao-module-energy compile 通过；yudao-server package 通过；本地已用未知卡号验证 /infra-api/energy/eiot/card/verify 可返回 accountKnown=false 并写入扫码刷卡记录。
```

```text
日期：2026-06-12
变更内容：后端扫码解析顺序正式改为设备/充电桩/仪表优先，旧车辆字段仅兜底兼容；车辆绑定管理页增加可选资产/旧二维码兼容提示；小程序扫码失败默认文案改为“扫码启动失败”。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/app/EnergyAppServiceImpl.java，current/admin-vue3/src/views/energy/vehicle/index.vue，current/wechat-miniprogram-section/src/pages/index/index.vue，current/design/2026-06-12-scan-device-first-change.md
已同步标准：04-api-standards.md、05-frontend-standards.md、07-vehicle-binding-standards.md 已在上一轮明确设备二维码主流程和车辆绑定兼容定位；本次补充变更记录。
备注：`vehicleId/vehicleNo/plateNo/qrCode` 仍保留旧兼容，不删除历史数据。
```
```text
日期：2026-06-12
变更内容：收口项目审查中发现的高风险点：EIOT 入站报文新增 X-EIOT-Token 校验；App 账号密码登录默认关闭；小程序客户版移除账号密码联调、设置/用户入口、客户管理/计费规则页面注册和设备详情编辑入口；后端补齐小程序设备详情、最新遥测、历史遥测、报警列表只读兼容接口，并统一按 App 用户授权范围过滤。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/eiot，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/app，current/backend-ruoyi/yudao-server/src/main/resources/application-local.yaml，current/wechat-miniprogram-section/src/pages.json，current/wechat-miniprogram-section/src/pages/auth/login.vue，current/wechat-miniprogram-section/src/pages/index/index.vue，current/wechat-miniprogram-section/src/pages/device/detail.vue
已同步标准：04-api-standards.md 补充 EIOT token、App 密码登录默认关闭、小程序只读兼容接口和授权过滤标准；05-frontend-standards.md 补充客户版小程序只保留微信登录和只读业务页面的标准
备注：后端 yudao-module-energy compile 通过；小程序 npm run build:mp-weixin 通过。EIOT 平台后续推送报文需携带 X-EIOT-Token，token 值以运行配置为准，不写入正式设计正文。
```

```text
日期：2026-06-12
变更内容：进一步清理小程序客户侧残留源码，删除未注册的客户范围选择、客户管理、计费规则维护页面及 customerScope 相关 API/store；将原 customers.ts 中仍被客户首页使用的会话/扫码接口拆分为 sessions.ts，避免客户版小程序继续保留客户管理命名和维护导出。
影响范围：current/wechat-miniprogram-section/src/pages，current/wechat-miniprogram-section/src/api，current/wechat-miniprogram-section/src/stores，current/wechat-miniprogram-section/src/pages/index/index.vue，current/wechat-miniprogram-section/src/pages/stats/detail.vue
已同步标准：05-frontend-standards.md 已要求客户版小程序不注册、不展示客户管理/计费规则维护页面
备注：清理后已执行 npm run build:mp-weixin 通过；搜索确认无 customerScope、customerApi、pricingApi、账号密码联调默认账号等残留引用。
```

```text
日期：2026-06-12
变更内容：进一步收口本地密钥和文档残留；application-local.yaml 不再明文保存微信小程序 AppSecret，改由 ENERGY_WECHAT_APP_SECRET 环境变量注入；小程序板块 README 改为当前客户侧正式方案；接口契约中 EIOT 入站 Header 更新为必填 X-EIOT-Token；后端 Swagger 示例账号去除 appdemo/admin123 残留；初始化 SQL 移除固定 App 演示账号和演示授权。
影响范围：current/backend-ruoyi/yudao-server/src/main/resources/application-local.yaml，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/auth/vo，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/permission/vo，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/auth/vo，current/wechat-miniprogram-section/README.md，current/design/11-api-contracts-v1.md，current/design/13-local-runtime-status.md，current/design/sql/mysql/energy_schema.sql，current/design-standards/04-api-standards.md
已同步标准：04-api-standards.md 补充微信小程序 AppSecret 必须通过环境变量注入，不写入可提交配置；13-local-runtime-status.md 记录本地启动前需要设置 ENERGY_WECHAT_APP_SECRET。
备注：真实 AppSecret 不写入本记录；本地运行时在 PowerShell 会话或用户环境变量中设置。
```

```text
日期：2026-06-13
变更内容：补全现场设备台账要求；设备新增/修改强制要求网关序列号、电表序列号、仪表编号、所属客户和项目场站；设备列表新增电表序列号、仪表编号查询和台账状态展示；本地库补齐 20241204870083_20241204870083 这一路 EIOT 报文来源设备。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/device/vo，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/mysql/device，current/admin-vue3/src/views/energy/device，current/design/05-admin-ui-design.md，current/design/13-local-runtime-status.md
已同步标准：04-api-standards.md 补充设备台账接口必填和查询标准；05-frontend-standards.md 补充设备台账完整性和核心识别字段维护标准；05-admin-ui-design.md 补充台账列表、表单必填和台账约束。
备注：设备匹配仍优先使用 meterNo，其次 gatewaySn + meterSn；台账补齐后才便于 EIOT 实时报文、告警、扫码、刷卡、授权和计费流程稳定关联同一台设备。
```

```text
日期：2026-06-14
变更内容：继续 Step 7 小程序接口适配，补齐小程序记录/账单接口；/app-api/energy/charging-sessions 从固定空数组改为返回当前 App 用户授权范围内真实充放电会话，并适配小程序统计详情页字段。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/session，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/app，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充小程序会话列表真实数据和授权过滤要求；05-frontend-standards.md 补充记录/统计详情页不得伪造收入、电量或会话记录。
备注：后端 energy 模块 compile 通过；yudao-server package 在停止旧后端释放 jar 锁后通过；新 jar 已启动。旧小程序 token 已过期，后续需用微信开发者工具重新登录后做授权数据深测。
```
```text
日期：2026-06-14
变更内容：修复小程序首页收入/今日电量兼容接口返回固定空数据、ISO 时间带 Z 被误解析为本地时间、放电电量伪造 EPE、账单混入非完成会话和前端空值 NaN 问题；后端汇总接口改为按当前 App 用户授权范围内真实会话与授权设备计算，小程序账单和统计页改为优先展示 totalEnergy。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/session，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/device，current/wechat-miniprogram-section/src/api/sessions.ts，current/wechat-miniprogram-section/src/stores/device.ts，current/wechat-miniprogram-section/src/pages/index/index.vue，current/wechat-miniprogram-section/src/pages/stats/detail.vue，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充小程序汇总接口真实授权数据、ISO 时间解析和不得伪造 EPE；05-frontend-standards.md 补充 totalEnergy 优先、账单只展示已完成会话和空值保护要求
备注：后端 energy 模块 compile、yudao-server package、小程序 npm run build:mp-weixin 均已通过；新 jar 已重启并验证未登录访问小程序会话接口返回 401。
```
```text
日期：2026-06-14
变更内容：继续收口小程序真实字段展示和结算参考数据；小程序首页、设备卡片、设备详情、历史趋势参数选择移除当前 EIOT 未提供的 EPE、Q、频率、温度、BMS 等字段；设备遥测兼容接口不再透出未接入字段；energy-delta 改为基于授权设备区间内 EPI 首尾差计算正向有功电能变化；扫码启动成功后账单刷新继续限定 COMPLETED。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/device/AppEnergyDeviceCompatController.java，current/wechat-miniprogram-section/src/api/devices.ts，current/wechat-miniprogram-section/src/components/DeviceCard.vue，current/wechat-miniprogram-section/src/components/DashboardPanel.vue，current/wechat-miniprogram-section/src/components/TrendChart.vue，current/wechat-miniprogram-section/src/pages/device/detail.vue，current/wechat-miniprogram-section/src/pages/device/trends.vue，current/wechat-miniprogram-section/src/pages/index/index.vue，current/wechat-miniprogram-section/src/utils/constants.ts，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充小程序遥测兼容接口真实字段和 energy-delta 计算约束；05-frontend-standards.md 补充小程序仅展示真实遥测字段、扫码后账单仍限定完成态的要求。
备注：后端 energy 模块 compile、yudao-server package、小程序 npm run build:mp-weixin 均已通过；新 jar 已重启并验证未登录访问小程序会话接口返回 401。
```
```text
日期：2026-06-14
变更内容：修正管理端首页、运营面板、设备台账、实时监控中的设备状态枚举和真实字段展示；统一 0=在线、1=离线、2=故障、3=维护，修复在线/离线统计反向；移除管理端首页、运营面板、设备台账中当前 EIOT 未提供的 SOC、平均温度、温度列等展示。
影响范围：current/admin-vue3/src/views/Home/Index.vue，current/admin-vue3/src/views/energy/dashboard/index.vue，current/admin-vue3/src/views/energy/device/index.vue，current/admin-vue3/src/views/energy/telemetry/index.vue，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充设备状态枚举；05-frontend-standards.md 补充管理端首页、运营面板、设备台账只能展示真实可得指标。
备注：管理端相关文件 eslint 通过；pnpm build:local 通过。
```
```text
日期：2026-06-14
变更内容：继续修复小程序客户侧真实字段残留；设备卡片和详情头部移除 SOC 展示，首页低 SOC 入口改为离线设备，设备对比只展示正向有功电能和功率，结算参考数据移除固定 0 的放电列；App 设备列表接口移除 SOC/SOH/温度字段并补充最新 EPI。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/device，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/app，current/wechat-miniprogram-section/src/api/devices.ts，current/wechat-miniprogram-section/src/stores/device.ts，current/wechat-miniprogram-section/src/components/DeviceCard.vue，current/wechat-miniprogram-section/src/pages/index/index.vue，current/wechat-miniprogram-section/src/pages/device/detail.vue，current/wechat-miniprogram-section/src/pages/stats/detail.vue，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充小程序 App 设备列表真实字段要求；05-frontend-standards.md 补充设备卡片、离线入口和结算参考数据展示规则。
备注：后端 yudao-module-energy compile 通过；yudao-server package 通过并已重启；小程序 npm run build:mp-weixin 通过。当前仍不能展示真实反向放电电能，需 EIOT 后续提供 `EPE` 或 PCS/BMS 放电电量后再接入。
```
```text
日期：2026-06-14
变更内容：新增微信小程序司机端/管理端分流；微信首次登录自动录入 App 用户并默认进入司机首页，后台“使用账户”新增小程序管理权限开关，开通后“我的”页显示“管理”入口并进入原设备概览/报表导出页面。
影响范围：current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/auth，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/auth，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/auth/vo，current/backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/dataobject/auth，current/admin-vue3/src/api/energy/appUser，current/admin-vue3/src/views/energy/appUser，current/wechat-miniprogram-section/src/api/auth.ts，current/wechat-miniprogram-section/src/stores/auth.ts，current/wechat-miniprogram-section/src/pages.json，current/wechat-miniprogram-section/src/pages/driver，current/wechat-miniprogram-section/src/pages/orders，current/wechat-miniprogram-section/src/pages/mine，current/design/sql/mysql/energy_schema.sql，current/design/13-local-runtime-status.md，current/design-standards/04-api-standards.md，current/design-standards/05-frontend-standards.md
已同步标准：04-api-standards.md 补充 miniAdminEnabled 接口和权限边界；05-frontend-standards.md 补充司机首页、我的页和管理入口显示规则。
备注：本机 MySQL `energy_app_user` 已新增 `mini_admin_enabled` 字段和索引；待执行后端/管理端/小程序构建验证。
```
```text
日期：2026-06-18
变更内容：修复 Worker 报表默认月份和个人修改密码安全校验；报表接口未传 billMonth 时改用 Asia/Shanghai 本地月份，避免北京时间月初默认到上个月；/system/user/profile/update-password 后端补充统一密码强度校验，禁止绕过前端直接设置弱密码。
影响范围：worker/src/index.ts，design-standards/04-api-standards.md，design-standards/06-change-sync-rules.md
已同步标准：04-api-standards.md 补充报表默认月份必须使用业务本地时区、密码创建/重置/修改必须后端统一强校验。
备注：前端 report-panel 已显式传 billMonth，本修复主要覆盖脚本或其他客户端直接调用 API 的场景。
```

```text
日期：2026-06-18
变更内容：统一报表面板电费明细的分时时段展示规则；电量型费用类别按尖、峰、平、谷、深谷固定展示，市场化购电费、上网环节线损费用、输配电量电费、系统运行费用、政府性基金及附加不再只输出本期有电量的时段。
影响范围：worker/src/index.ts，design-standards/04-api-standards.md，design-standards/06-change-sync-rules.md
已同步标准：04-api-standards.md 补充电量型费用类别固定分时时段展示要求。
备注：本次修复的是报表展示原则，后续新增客户、场地或设备时同样适用。
```

```text
日期：2026-06-18
变更内容：修复计费规则有效结束时间边界匹配问题；当 effectiveEnd 保存为日期当天 00:00:00 或仅日期格式时，规则匹配统一按当天 23:59:59 处理，确保客户级规则能继承到该客户下所有场地和设备，不会在结束日期当天被误判过期。
影响范围：worker/src/index.ts，design-standards/04-api-standards.md，design-standards/06-change-sync-rules.md
已同步标准：04-api-standards.md 补充 effectiveEnd 整日有效的匹配规则。
备注：本次修复的是通用匹配原则，适用于后续新增客户、项目、设备的客户级/项目级/设备级计费规则。
```

```text
日期：2026-06-18
变更内容：按业务口径调整未匹配计费规则数据展示；报表接口新增 unmatchedPricingDetails，电费明细主表只展示已匹配规则并可正常计费的费用行，未匹配规则的场地或设备在独立“未匹配计费规则电量”板块展示项目、电表、仪表编号、分时时段、充电电量和放电电量。
影响范围：worker/src/index.ts，admin-vue3/src/api/energy/report/index.ts，admin-vue3/src/views/energy/report-panel/index.vue，design-standards/04-api-standards.md，design-standards/06-change-sync-rules.md
已同步标准：04-api-standards.md 明确未匹配规则电量不得混入电费明细主表，必须单独返回并展示。
备注：该板块用于提示运营人员给对应场地或设备补计费规则。
```

```text
日期：2026-06-18
变更内容：调整报表面板电费明细未匹配计费规则的展示方式；电费明细主表只展示已匹配计费规则且可计费的费用行，未匹配规则的设备电量不再输出为同表重复行，也不会按 0 单价混入正常计费标准。
影响范围：worker/src/index.ts，design-standards/04-api-standards.md，design-standards/06-change-sync-rules.md
已同步标准：04-api-standards.md 明确未匹配计费规则电量不得混入电费明细主表，也不得生成重复的未匹配规则行。
备注：后续如需要展示未匹配设备，应单独做“数据完整性提示/待配置设备”板块，而不是放入电费明细。
```

```text
日期：2026-06-18
变更内容：修正报表面板电费明细计费标准展示；零售交易电费改为使用计费规则的尖/峰/平/谷/深谷分时电价，未匹配计费规则的设备电量不再混入费用行按 0 单价冲低平、谷等分时时段计费标准。
影响范围：worker/src/index.ts，design-standards/04-api-standards.md，design-standards/06-change-sync-rules.md
已同步标准：04-api-standards.md 补充报表费用明细分时电价来源和未匹配规则电量处理要求。
备注：远端 D1 中平/谷电价已保存；截图中的 0 主要来自未匹配计费规则设备电量被混算到费用明细。
```

```text
日期：2026-06-18
变更内容：修复 Worker 客户账号权限绕过和刷新令牌状态校验问题；客户老板账号访问 customer、project、device、vehicle、account-event、user-scope、pricing-rule、charge-session 等接口时均按绑定 customer_id 做后端强过滤，维护类写操作由平台管理员执行；refresh-token 重新校验 system_user.status，禁用或删除账号会撤销 session 并拒绝续签。
影响范围：worker/src/index.ts，design-standards/04-api-standards.md，design-standards/06-change-sync-rules.md
已同步标准：04-api-standards.md 补充客户账号后端强隔离、关联客户归属反查、客户账号只读边界和 refresh-token 禁用用户校验要求。
备注：客户账号权限不能只依赖菜单隐藏；后续新增客户可访问的 API 必须同时补后端 accessScope 过滤。
```
