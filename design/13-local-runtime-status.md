# 本机运行基线

日期：2026-05-30

## 当前可运行结果

RuoYi-Vue-Pro 后端、Vue3 管理端和本机 Redis 兼容服务已经在本机打通。

| 项目 | 状态 | 地址/路径 |
| --- | --- | --- |
| 后端仓库 | 已切换到 `master-jdk17` | `D:\code\ankerui\energy-storage-platform\current\backend-ruoyi` |
| 后端服务 | 已构建并验证成功；目录整理时已停止，需按新路径重启 | `http://localhost:48080` |
| 移动储能后端模块 | 已接入 `yudao-server` 并随后端启动 | `yudao-module-energy` |
| 移动储能设备接口 | 已实现并进入 OpenAPI | `/admin-api/energy/device/*` |
| 移动储能客户接口 | 已实现并进入 OpenAPI | `/admin-api/energy/customer/*` |
| 移动储能项目接口 | 已实现并进入 OpenAPI | `/admin-api/energy/project/*` |
| 移动储能告警接口 | 已实现并进入 OpenAPI | `/admin-api/energy/alarm/*` |
| 管理端仓库 | 已安装依赖并构建成功；目录整理时已停止，需按新路径重启 | `D:\code\ankerui\energy-storage-platform\current\admin-vue3` |
| 管理端页面 | 已验证可启动 | `http://localhost/` |
| 移动储能设备页面 | 已实现并通过本地构建 | `src/views/energy/device/index.vue` |
| 移动储能客户页面 | 已实现并通过本地构建 | `src/views/energy/customer/index.vue` |
| 移动储能项目页面 | 已实现并通过本地构建 | `src/views/energy/project/index.vue` |
| 移动储能告警页面 | 已实现并通过本地构建 | `src/views/energy/alarm/index.vue` |
| MySQL | 已创建库和专用账号 | `127.0.0.1:3306/ruoyi-vue-pro` |
| Redis 兼容服务 | 已使用 Garnet 启动 | `127.0.0.1:6379` |

## 本机环境

- JDK：Microsoft OpenJDK 17，路径 `C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot`
- Maven：Apache Maven 3.9.16，路径 `D:\code\tools\apache-maven-3.9.16`
- Node：`v24.15.0`
- pnpm：`10.33.0`
- Redis 兼容服务：Microsoft Garnet

## MySQL 配置

数据库：

```text
ruoyi-vue-pro
```

后端运行账号：

```text
username: ruoyi
password: ruoyi123456
host: localhost / 127.0.0.1
```

已导入内容：

- RuoYi 基础 SQL：`sql/mysql/ruoyi-vue-pro.sql`
- 移动储能业务表：`current/design/sql/mysql/energy_schema.sql`
- 移动储能字典：`current/design/sql/mysql/energy_dict.sql`
- 移动储能菜单：`current/design/sql/mysql/energy_menu.sql`

验证结果：

- `energy_` 业务表：9 张
- 移动储能字典类型：8 个
- 移动储能菜单权限：29 条

## 后端启动

构建命令：

```powershell
$env:JAVA_HOME='C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;D:\code\tools\apache-maven-3.9.16\bin;$env:Path"
mvn -pl yudao-server -am -DskipTests package
```

构建结果：

```text
BUILD SUCCESS
输出文件：D:\code\ankerui\energy-storage-platform\current\backend-ruoyi\yudao-server\target\yudao-server.jar
```

移动储能模块构建命令：

```powershell
$env:JAVA_HOME='C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;D:\code\tools\apache-maven-3.9.16\bin;$env:Path"
mvn -pl yudao-module-energy,yudao-server -am -DskipTests package
```

移动储能模块验证结果：

```text
BUILD SUCCESS
后端进程：java.exe
后端端口：http://localhost:48080
模块探测接口：http://localhost:48080/admin-api/energy/module-info/runtime
接口状态：已进入 RuoYi 登录校验，返回 {"code":401,"msg":"账号未登录","data":null}
OpenAPI 验证：/admin-api/energy/module-info/runtime 已注册
说明：管理端接口受 RuoYi 登录保护，未登录时返回 401，符合默认安全边界。
```

设备中心接口验证结果：

```text
BUILD SUCCESS
OpenAPI 验证：
- /admin-api/energy/device/page
- /admin-api/energy/device/create
- /admin-api/energy/module-info/runtime
端口验证：http://localhost:48080 可连接
未登录访问：http://localhost:48080/admin-api/energy/device/page?pageNo=1&pageSize=10 返回 {"code":401,"msg":"账号未登录","data":null}
```

客户/项目接口验证结果：

```text
BUILD SUCCESS
OpenAPI 验证：
- /admin-api/energy/customer/page
- /admin-api/energy/customer/simple-list
- /admin-api/energy/project/page
- /admin-api/energy/project/simple-list
- /admin-api/energy/device/page
端口验证：http://localhost:48080 可连接
```

启动命令：

```powershell
$env:JAVA_HOME='C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -jar yudao-server\target\yudao-server.jar --spring.profiles.active=local
```

## 管理端启动

管理端 `.env.local` 已指向：

```text
VITE_BASE_URL='http://localhost:48080'
VITE_API_URL=/admin-api
```

依赖安装命令：

```powershell
pnpm install --ignore-workspace --registry=https://registry.npmmirror.com
```

说明：上级目录存在 `pnpm-workspace.yaml`，安装管理端依赖时必须带 `--ignore-workspace`，避免误处理其它项目。

启动命令：

```powershell
pnpm dev
```

当前端口：

```text
http://localhost/
```

移动储能设备页面：

```text
组件路径：src/views/energy/device/index.vue
表单组件：src/views/energy/device/DeviceForm.vue
接口封装：src/api/energy/device/index.ts
菜单组件名：EnergyDevice
菜单组件路径：energy/device/index
已接入字典：energy_device_status、energy_device_type、energy_run_mode
已接入下拉：客户 simple-list、项目 simple-list
已接入聚合展示：设备列表和详情返回 customerName、projectName
```

移动储能客户/项目页面：

```text
客户组件路径：src/views/energy/customer/index.vue
客户表单组件：src/views/energy/customer/CustomerForm.vue
客户接口封装：src/api/energy/customer/index.ts
客户菜单组件名：EnergyCustomer
客户菜单组件路径：energy/customer/index

项目组件路径：src/views/energy/project/index.vue
项目表单组件：src/views/energy/project/ProjectForm.vue
项目接口封装：src/api/energy/project/index.ts
项目菜单组件名：EnergyProject
项目菜单组件路径：energy/project/index
```

管理端验证结果：

```text
pnpm exec eslint src/api/energy/device/index.ts src/views/energy/device/index.vue src/views/energy/device/DeviceForm.vue src/api/energy/customer/index.ts src/api/energy/project/index.ts src/views/energy/customer/index.vue src/views/energy/customer/CustomerForm.vue src/views/energy/project/index.vue src/views/energy/project/ProjectForm.vue src/utils/dict.ts
结果：通过

pnpm build:local
结果：Build successful. Please see dist directory
```

说明：`pnpm ts:check` 当前会被仓库既有历史类型问题阻断，本次输出未发现新增 `energy` 文件相关错误。

最新验证记录（2026-05-30）：

```text
后端构建：mvn -pl yudao-module-energy,yudao-server -am -DskipTests package
结果：BUILD SUCCESS
后端端口：http://localhost:48080 可连接
OpenAPI 验证：/admin-api/energy/device/page、/admin-api/energy/customer/page、/admin-api/energy/project/page 均已注册
Redis 兼容服务：Garnet 127.0.0.1:6379 可连接
管理端构建：pnpm build:local
结果：Build successful. Please see dist directory
管理端页面：http://localhost/ 可连接
```

告警中心验证记录（2026-05-30）：

```text
后端模块编译：mvn -pl yudao-module-energy -am -DskipTests compile
结果：BUILD SUCCESS
后端完整打包：mvn -pl yudao-module-energy,yudao-server -am -DskipTests package
结果：BUILD SUCCESS
OpenAPI 验证：
- /admin-api/energy/alarm/page
- /admin-api/energy/alarm/get
- /admin-api/energy/alarm/ack
- /admin-api/energy/alarm/close
管理端 ESLint：pnpm exec eslint src/api/energy/alarm/index.ts src/views/energy/alarm/index.vue
结果：通过
管理端构建：pnpm build:local
结果：Build successful. Please see dist directory
```

EIOT 告警接入和处理记录验证记录（2026-05-31）：

```text
新增表：energy_alarm_handle_record
本地 MySQL 建表：已完成
后端主键策略：energy 模块 DO 已显式使用 @TableId(type = IdType.ASSIGN_ID)
后端模块编译：mvn -pl yudao-module-energy -am -DskipTests compile
结果：BUILD SUCCESS
后端完整打包：mvn -pl yudao-module-energy,yudao-server -am -DskipTests package
结果：BUILD SUCCESS
管理端 ESLint：pnpm exec eslint src/api/energy/alarm/index.ts src/views/energy/alarm/index.vue
结果：通过
管理端构建：pnpm build:local
结果：Build successful. Please see dist directory
EIOT 告警接入验证：POST /infra-api/energy/eiot/alarm，携带 tenant-id=1
结果：{"received":1,"created":0,"ignored":0,"failed":1}
说明：测试使用不存在的 meterNo，预期写入同步失败日志并返回 failed=1
成功路径验证：测试设备 meter_no=METER_TEST_20260531_SUCCESS，告警 TEST-ALARM-SUCCESS-20260531-002 创建成功
结果：首次推送 {"received":1,"created":1,"ignored":0,"failed":0}；重复推送 {"received":1,"created":0,"ignored":1,"failed":0}
落库结果：energy_alarm.occur_time=2026-05-31 11:30:00，energy_eiot_sync_log 记录成功和 duplicate alarmNo ignored
管理端处理验证：/admin-api/energy/alarm/ack 与 /close 均通过，energy_alarm_handle_record 写入 ack/close 两条处理记录
时间异常验证：occurTime=2026/05/31 11:30:00 返回 failed=1，不创建 energy_alarm，并写入 energy_eiot_sync_log 失败原因 invalid occurTime
当前端口：后端 http://localhost:48080，管理端 http://localhost/，Garnet 127.0.0.1:6379
```

小程序首页/设备列表接口验证记录（2026-05-31）：

```text
新增接口：
- GET /app-api/energy/home/overview
- GET /app-api/energy/device/list

构建验证：
- mvn -pl yudao-module-energy -am -DskipTests compile：通过
- mvn -pl yudao-module-energy,yudao-server -am -DskipTests package：通过

运行验证：
- 未登录访问返回 401，符合业务接口不公开要求。
- 后台 admin token 访问返回 403，确认 RuoYi 区分后台与 App 用户类型。
- 已接入 `energy_user_scope` 授权过滤；小程序端只返回当前 App 用户被授权的客户、项目或设备范围。
- 本地临时 member token `codexapptoken20260531` 绑定 user_id=202605310001，已授权 device_id=202605310003，仅用于联调。

返回结果：
- 首页概览返回 deviceCount=1、onlineCount=0、faultCount=0、unackedAlarmCount=1、todayChargeEnergy=0、todayDischargeEnergy=0、currentPower=0
- 设备列表返回测试设备 DEV-EIOT-TEST-20260531
- keyword=EIOT 过滤通过；status=0 过滤返回空列表
- 无授权范围 token `codexnoscope20260531` 访问通过但返回空业务数据：home/overview 全部计数为 0，device/list 返回空数组。

说明：当前本地库没有 member 用户表，临时 member token 直接写入 system_oauth2_access_token 用于验证 app-api 业务路由；后续接小程序登录时应替换为正式 App 登录流程。
当前服务状态：后端已从 `yudao-server/target/yudao-server.jar` 启动，端口 `http://localhost:48080` 正常监听，错误日志为空。
```

用户授权管理端入口验证记录（2026-05-31）：

```text
新增接口：
- GET /admin-api/energy/user-scope/page
- GET /admin-api/energy/user-scope/get
- POST /admin-api/energy/user-scope/create
- PUT /admin-api/energy/user-scope/update
- DELETE /admin-api/energy/user-scope/delete
- GET /admin-api/energy/device/simple-list

新增管理端页面：
- src/views/energy/userScope/index.vue
- src/views/energy/userScope/UserScopeForm.vue
- src/api/energy/userScope/index.ts

新增菜单：
- 移动储能 / 用户授权
- 权限标识：energy:user-scope:query、energy:user-scope:create、energy:user-scope:update、energy:user-scope:delete

构建验证：
- mvn -pl yudao-module-energy -am -DskipTests compile：通过
- mvn -pl yudao-module-energy,yudao-server -am -DskipTests package：通过
- pnpm exec eslint src/api/energy/device/index.ts src/api/energy/userScope/index.ts src/views/energy/userScope/index.vue src/views/energy/userScope/UserScopeForm.vue：通过
- pnpm build:local：通过

运行验证：
- 后端已重新打包并启动，当前端口 http://localhost:48080 正常监听，错误日志为空。
- 用户授权分页返回 code=0，当前有效授权 total=1，用户 202605310001 授权测试设备 202605310003。
- 设备精简列表返回 code=0，当前测试设备数量 1。
- 用户授权 create/get/update/delete 全流程返回 code=0；临时测试授权记录已软删除，active_temp_scope_count=0。
- 小程序接口复测通过：授权用户 codexapptoken20260531 返回 DEV-EIOT-TEST-20260531；无授权用户 codexnoscope20260531 返回空列表。
```

项目目录整理记录（2026-05-31）：

```text
统一根目录：D:\code\ankerui\energy-storage-platform

当前主线：
- current/backend-ruoyi：RuoYi-Vue-Pro 后端
- current/admin-vue3：Vue3 管理端
- current/miniprogram：uni-app 小程序源码与 mp-weixin 编译产物
- current/design：设计文档、接口契约、SQL
- current/design-standards：设计标准

旧项目归档：
- legacy/backend-nestjs
- legacy/eiot-worker
- legacy/minidatt
- legacy/xiaocode
- legacy/miniprogram-copy
- legacy/root-workspace

参考资料：
- references

说明：
- 因目录剪切需要释放文件锁，已停止后端服务、管理端 Vite 服务和微信开发者工具。
- 后续微信开发者工具打开路径：D:\code\ankerui\energy-storage-platform\current\miniprogram\dist\build\mp-weixin
- 后续后端启动路径：D:\code\ankerui\energy-storage-platform\current\backend-ruoyi\yudao-server
- 后续管理端启动路径：D:\code\ankerui\energy-storage-platform\current\admin-vue3
```

## 2026-06-01 小程序正式登录接入

本次已完成：

- 后端新增 `energy_app_user` App 用户表、Mapper、登录 Service 和 `POST /app-api/energy/auth/login`。
- 登录成功后复用 RuoYi `OAuth2TokenService`，生成 `UserTypeEnum.MEMBER = 1` 的真实 `system_oauth2_access_token`。
- 小程序请求基地址切换为 `http://127.0.0.1:48080/app-api/energy`。
- 小程序请求统一携带 `tenant-id: 1`；登录后使用 `Authorization: Bearer {accessToken}`。
- 小程序首页概览、设备列表已切换到 `/home/overview`、`/device/list`，可使用正式 token 访问。

历史说明：该阶段曾使用本地演示账号验证账号密码登录。当前客户版已经切换为微信登录，`energy_schema.sql` 不再初始化固定 App 演示账号和授权范围。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- `current/design/sql/mysql/energy_schema.sql` 已写入本机 MySQL。
- 历史验证：`POST /app-api/energy/auth/login` 曾用于本地联调；当前默认关闭，正式客户入口为微信登录。
- `GET /app-api/energy/home/overview`：`code=0`，当前授权设备数 `deviceCount=1`。
- `GET /app-api/energy/device/list`：`code=0`，当前返回 `1` 条设备。
- `npm run build:mp-weixin`：通过。

2026-06-01 补充验证：

- `GET /app-api/energy/devices/energy-delta`：`code=0`。
- `GET /app-api/energy/charging-sessions/revenue-overview`：`code=0`。
- `GET /app-api/energy/charging-sessions/today-energy`：`code=0`。
- `GET /app-api/energy/charging-sessions`：`code=0`。
- 以上接口当前为兼容版，返回 0 值或空列表，避免小程序首页旧统计请求报错。

当前运行状态：

- Garnet Redis 兼容服务：已启动，端口 `6379`。
- RuoYi 后端：已启动，端口 `48080`，日志 `current/backend-ruoyi/yudao-server-run.out.log`。
- Vue3 管理端：已启动，端口 `81`，地址 `http://localhost:81/`，日志 `current/admin-vue3/admin-vue3-dev.out.log`。
- 微信开发者工具打开路径：`D:\code\ankerui\energy-storage-platform\current\miniprogram\dist\build\mp-weixin`。

## 2026-06-01 管理端项目化界面调整

本次已完成：

- 管理端标题改为 `移动储能运营管理平台`，配置位于 `current/admin-vue3/.env.local`。
- 登录页改为移动储能项目文案，只保留后台账号登录和忘记密码入口，隐藏手机号登录、二维码登录、注册、第三方登录和开源文档外链。
- 首页替换为移动储能运营工作台，展示设备总数、客户数量、项目站点、待处理告警、设备运行概览、待处理告警和快捷入口。
- 前端动态菜单增加项目白名单，仅默认展示 `移动储能` 和 `系统管理` 两类一线菜单。
- AI、BPM、CRM、ERP、IoT、商城、会员、公众号、支付、报表、WMS、MES 等 RuoYi 示例/通用业务模块在前端菜单中过滤隐藏。
- 移动储能二级菜单仅保留当前已落地页面：设备台账、告警中心、客户管理、项目场站、用户授权。
- 管理端 Vite 已重启，当前访问地址为 `http://localhost:81/`。

验证结果：

- `pnpm exec eslint src\store\modules\permission.ts src\views\Home\Index.vue`：通过。
- `pnpm build:local`：通过。

## 2026-06-01 运营面板落地

本次已完成：

- 新增管理端页面 `current/admin-vue3/src/views/energy/dashboard/index.vue`。
- 移动储能菜单白名单放开 `dashboard`，侧边栏可显示“运营面板”。
- 首页快捷入口增加“运营面板”。
- 运营面板当前复用现有设备、客户、项目、告警接口聚合展示：
  - 设备总数、在线设备、客户数量、项目站点、待处理告警；
  - 当前总功率、平均 SOC、平均温度；
  - 设备功率排行、SOC 水位分布、待处理告警；
  - 实时监控、EIOT 同步日志、计费规则、充放电任务、调度任务的后续落地状态。

说明：

- 当前运营面板还不是最终实时大屏，先用现有管理端接口形成可用运营视图。
- 实时监控、EIOT 同步日志、计费规则、充放电任务、调度任务仍按后续顺序落地，未完成前不默认放开菜单入口。

## 2026-06-01 实时监控落地

本次已完成：

- 新增管理端页面 `current/admin-vue3/src/views/energy/telemetry/index.vue`。
- 移动储能菜单白名单放开 `telemetry`，侧边栏可显示“实时监控”。
- 首页快捷入口增加“实时监控”。
- 运营面板中的“实时监控”能力状态改为已落地。
- 实时监控当前复用设备台账最新采集字段展示：
  - 设备状态、SOC、功率、温度、电压、电流、更新时间；
  - 监控设备数、在线设备数、低电量设备数、总功率；
  - 设备/客户/项目关键字搜索和状态筛选。

说明：

- 当前实时监控第一版使用设备分页接口的最新采集值。
- 当前实时监控页下方已接入 EIOT 实时采集分页接口，用于展示收到的原始电表报文字段。
- 后续接入 WebSocket 后，应保持页面结构稳定，仅替换刷新机制。

## 2026-06-01 EIOT 同步日志管理页落地

本次已完成：

- 后端新增管理端接口 `GET /admin-api/energy/eiot-log/page`。
- 后端新增 `EnergyEiotSyncLogController`、分页 VO、响应 VO，并在 `EnergyEiotService` / `EnergyEiotSyncLogMapper` 中补齐分页查询。
- 前端新增接口封装 `current/admin-vue3/src/api/energy/eiotLog/index.ts`。
- 前端新增页面 `current/admin-vue3/src/views/energy/eiot-log/index.vue`。
- 移动储能菜单白名单放开 `eiot-log`，侧边栏可显示“EIOT 同步日志”。
- 首页快捷入口增加“EIOT 日志”。
- 运营面板中的“EIOT 同步日志”能力状态改为已落地。

页面能力：

- 支持按同步类型、状态、请求编号、网关序列号、电表序列号、创建时间范围过滤。
- 展示状态、同步类型、请求编号、网关序列号、电表序列号、错误信息、报文归档、创建时间。
- 当前页统计成功/失败数量，并展示匹配总数。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- `mvn -pl yudao-server -am -DskipTests package`：第一次因运行中的 `java.exe` 占用 jar 失败；停止旧进程后重跑通过。
- 后端已重新启动，当前 Java PID `36184`。
- `GET /admin-api/energy/eiot-log/page?pageNo=1&pageSize=5`：登录后返回 `code=0`，本地库当前匹配 `total=6`。
- `pnpm exec eslint src\store\modules\permission.ts src\api\energy\eiotLog\index.ts src\views\energy\eiot-log\index.vue src\views\Home\Index.vue src\views\energy\dashboard\index.vue`：通过。
- `pnpm build:local`：通过。

## 2026-06-04 计费规则管理页落地

本次已完成：

- 后端新增管理端计费规则 CRUD 接口：
  - `GET /admin-api/energy/pricing-rule/page`
  - `GET /admin-api/energy/pricing-rule/get?id={id}`
  - `POST /admin-api/energy/pricing-rule/create`
  - `PUT /admin-api/energy/pricing-rule/update`
  - `DELETE /admin-api/energy/pricing-rule/delete?id={id}`
- 后端新增计费规则 Controller、VO、Mapper、Service 和 DO，数据表使用 `energy_pricing_rule`。
- 计费范围支持客户、项目、设备三选一；后端保存前校验范围对象存在。
- 生效时间使用 `yyyy-MM-dd HH:mm:ss` 字符串接收，Service 层显式解析；结束时间不得早于开始时间。
- 前端新增接口封装 `current/admin-vue3/src/api/energy/pricingRule/index.ts`。
- 前端新增页面 `current/admin-vue3/src/views/energy/pricing-rule/index.vue` 和表单 `PricingRuleForm.vue`。
- 移动储能菜单白名单放开 `pricing-rule`，侧边栏可显示“计费规则”。
- 本地数据库已给超级管理员角色补齐移动储能菜单授权，`/system/auth/get-permission-info` 已返回 `pricing-rule` 和 `energy/pricing-rule/index`。
- 后端新增 `GET /admin-api/energy/pricing-rule/match`，用于按设备和计费时间匹配当前生效计费规则。
- 匹配优先级固定为设备级 > 项目级 > 客户级；只匹配启用且在生效区间内的规则。

页面能力：

- 支持按客户、项目、设备、状态、生效开始时间范围过滤。
- 列表展示计费范围、范围名称、时间单价、电量单价、生效开始、生效结束和状态。
- 新增/修改时通过单选切换客户/项目/设备范围，并确保提交时只有一个范围 ID。
- 页面新增按设备和计费时间进行规则匹配的试算区，命中后展示范围、单价、生效时间和规则编号。
- 删除操作保留二次确认。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- `mvn -pl yudao-server -am -DskipTests package`：通过，生成 `yudao-server.jar` 时间为 `2026-06-04 12:45:43`。
- 本地 Garnet Redis 兼容服务已启动，端口 `6379`。
- 后端已重新启动，端口 `48080`。
- `GET /admin-api/energy/pricing-rule/page?pageNo=1&pageSize=5`：登录后返回 `code=0`，本地库当前 `total=0`。
- `GET /admin-api/energy/pricing-rule/match?deviceId=202605310003&billingTime=2026-06-04%2012:00:00`：临时创建设备级规则后返回 `code=0`，命中规则编号与创建编号一致；测试后已删除临时规则。
- `GET /admin-api/system/auth/get-permission-info`：登录后返回 `pricing-rule` 和 `energy/pricing-rule/index`。
- `pnpm exec eslint src\store\modules\permission.ts src\api\energy\pricingRule\index.ts src\views\energy\pricing-rule\index.vue src\views\energy\pricing-rule\PricingRuleForm.vue src\views\energy\dashboard\index.vue`：通过。
- `pnpm build:local`：通过。

## 2026-06-04 EIOT 真实报文兼容

背景：

- 当前可收到的 EIOT 报文只有两类：实时电表数据和告警列表数据。
- 实时电表数据不包含 SOC/SOH/电池温度等储能 BMS 字段，包含三相电压电流、总有功功率、功率因数和 `EPI`。
- 告警报文字段为 `list[]`，标题和内容是 `zh_CN` / `en_US` 对象。

本次已完成：

- 新增 `POST /infra-api/energy/eiot/realtime`。
- 实时数据写入 `energy_telemetry`，并更新 `energy_device` 的在线状态、总功率、三相平均电压、三相平均电流和最新采集时间。
- `energy_telemetry` 扩展保存 `gatewaySn`、`meterSn`、`timestamp`、`source`、`state`、`Pa/Pb/Pc`，用于管理端直接展示收到的实时报文数据。
- 新增管理端实时采集数据接口 `GET /admin-api/energy/telemetry/page`，权限标识 `energy:telemetry:query`。
- 实时监控页新增“EIOT 实时报文数据”板块，支持设备、网关编号、仪表编号、数据类型、仪表状态、采集时间筛选，并展示三相功率/电压/电流、总功率、功率因数、EPI 和 timestamp。
- 告警接入兼容 EIOT 原始 `list[]` 字段，并优先解析中文标题和内容。
- 设备匹配策略调整为：优先按 `meterNo`，匹配不到再按 `gatewaySn + meterSn`。
- 新增 `EnergyTelemetryDO` / `EnergyTelemetryMapper`。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- `mvn -pl yudao-server -am -DskipTests package`：通过。
- `POST /infra-api/energy/eiot/realtime`：使用真实结构报文返回 `code=0`，`updated=1`。
- `GET /admin-api/energy/telemetry/page?pageNo=1&pageSize=3&meterNo=METER_TEST_20260531_SUCCESS`：登录后返回 `code=0`，可看到 `gatewaySn/meterSn/meterNo/source/state/pa/pb/pc/ua/ub/uc/ia/ib/ic/p/pf/epi/timestamp`。
- `POST /infra-api/energy/eiot/alarm`：使用真实结构 `list[]` 告警报文返回 `code=0`，`created=1`。
- 数据库验证：`energy_telemetry` 已写入采集明细，`energy_device` 已更新最新功率/电压/电流/采集时间，`energy_alarm` 已创建测试告警，`energy_eiot_sync_log` 已记录 realtime/alarm 成功日志。

## 2026-06-04 实时监控数据板块重构

本次已完成：

- 管理端实时监控页从“大表格展示所有报文”调整为以电表卡片为入口的监控页面。
- 设备监控面板支持点击电表卡片选择当前电表，并通过“详情”定位到电表实时详情。
- 新增电表实时详情板块，展示 `gatewaySn`、`meterSn`、`meterNo`、`source`、`state`、采集时间、三相电压、三相电流、有功功率、功率因数和 `EPI`。
- 新增数据查看板块，支持按电表、日期范围、参数类型查看“日原始数据”和“逐日极值数据”。
- 新增详细实时数据板块，支持按电表、日期、起止时间、参数类型和一分钟/五分钟/十五分钟/半小时/一小时间隔查看明细表，缺失值显示 `--`。
- 后端新增 `GET /admin-api/energy/telemetry/chart`，用于返回指定时间范围内的原始采集点。
- 后端新增 `GET /admin-api/energy/telemetry/daily-stat`，用于返回选定指标的逐日最大值、最小值和平均值。
- 前端接口封装 `current/admin-vue3/src/api/energy/telemetry/index.ts` 已补充 `getTelemetryChart` 和 `getTelemetryDailyStat`。

约束：

- 当前可展示真实指标仅限 EIOT 已收到并入库的字段：`pa/pb/pc/p`、`ua/ub/uc`、`ia/ib/ic`、`pf`、`epi`。
- 线电压、频率、无功功率、视在功率、温度、开关量等字段当前没有报文来源，不在第一版真实数据视图中展示。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- `mvn -pl yudao-server -am -DskipTests package`：通过。
- `pnpm exec eslint src\api\energy\telemetry\index.ts src\views\energy\telemetry\index.vue`：通过。
- `pnpm build:local`：通过。
- 后端已使用新 jar 启动，端口 `48080`。
- Vue3 管理端已启动，访问地址 `http://localhost:81/`。
- Garnet Redis 兼容服务已启动，端口 `6379`。

## 2026-06-05 实时监控子板块布局调整

本次已完成：

- 实时监控页改为三个子板块：`设备监控面板`、`数据查看`、`详细实时数据`。
- 监控电表、在线电表、离线电表、总有功功率统计卡片已放到每个子板块顶部。
- `电表实时详情` 不再作为页面固定板块展示，改为设备监控面板卡片点击“详情”后打开弹窗查看。
- `日原始数据` 和 `逐日极值数据` 均增加“图表 / 数据”切换；数据视图提供表格和 CSV 导出。
- `日原始数据` 改为按查询时间范围固定 5 分钟一行生成时间轴，无采集值时显示 `--`，导出保留空时间点。
- 该调整不涉及数据库结构变更，也不改变 EIOT 报文字段范围。

## 2026-06-05 逐日极值数据表格调整

本次已完成：

- `逐日极值数据` 增加参数单选区，按当前参数类型选择单个指标查看。
- 逐日极值图表、表格和 CSV 导出统一使用按查询日期范围补齐后的逐日数据；没有采集值的日期也显示一行，空值显示 `--`。
- 逐日极值数据表改为多级表头：采集时间、选定参数、最大值（数值/发生时间）、最小值（数值/发生时间）、平均值。
- 后端 `GET /admin-api/energy/telemetry/daily-stat` 响应新增 `maxTime` 和 `minTime`，用于展示最大值和最小值的真实发生时间。

约束：

- 逐日极值仍只允许选择 EIOT 已入库的真实指标：`pa/pb/pc/p`、`ua/ub/uc`、`ia/ib/ic`、`pf`、`epi`。
- 前端只补齐日期展示，不伪造采集值；后端没有返回的日期统一显示 `--`。

## 2026-06-05 充放电会话第一版

本次已完成：

- 新增管理端充放电会话后端接口：分页、详情、开始、结束、结算。
- 开始会话时校验设备无进行中会话，并复用计费规则匹配服务；未命中生效规则时不创建会话。
- 开始/结束会话使用 `energy_telemetry` 最新 `EPI` 作为第一版通用电能读数，写入 `start_energy/end_energy`。
- 结束会话时计算总电量、时长、电量费用、时间费用和总费用。
- 新增管理端 Vue3 页面 `充放电记录`，支持筛选、开始任务、结束任务、结算和详情查看。
- 移动储能菜单白名单放开 `charge-session`，菜单 SQL 补充开始、结束、结算权限。

约束：

- 第一版暂未对接 EIOT SWITCH 分合闸实控；待设备控制服务落地后接入 start/stop。
- 当前真实 EIOT 实时报文只有 `EPI`，暂不能严格计算反向放电电量；后续需要确认 `EPE` 或 PCS/BMS 放电电能来源。

## 2026-06-06 充放电开始任务异常修复

本次已完成：

- 修复开始任务时手动选择不适用计费规则可能返回 `系统异常` 的问题。
- 后端开始会话新增设备客户归属校验：设备未绑定客户时返回“开始充放电任务前，请先给设备绑定客户”，不再进入数据库插入异常。
- 后端开始会话新增手动计费规则适用性校验：传入 `pricingRuleId` 时必须匹配当前设备、所属项目或所属客户。
- 管理端充放电记录页开始任务区域按所选设备过滤计费规则；设备未绑定客户时显示提示并禁用开始按钮。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- `mvn -pl yudao-server -am -DskipTests package`：通过，后端已重启。
- `pnpm exec eslint src\views\energy\charge-session\index.vue`：通过。
- `pnpm build:local`：通过。
- 接口验证：未绑定客户的真实 EIOT 设备返回业务提示 `1061107006`；测试设备可正常开始会话，验证会话已清理。

## 2026-06-06 实时监控工作台布局调整

本次已完成：

- 管理端实时监控页顶部改为“能源物联网云平台”风格标题栏，并展示一般、紧急、严重告警计数入口。
- `设备监控面板` 改为左右工作台布局：左侧展示当前移动电源/客户/项目、正常/离线/故障/报警计数和电能/功率/在线率指标；右侧展示状态按钮、节点类型、节点、能源类型、网关/仪表搜索。
- 设备列表默认使用参考图中的矩阵卡片样式，卡片展示电表名称、状态、网关识别号、仪表地址、仪表型号和详情按钮。
- 新增矩阵模式/表格模式切换；电表实时详情仍由设备卡片“详情”触发，不作为固定常驻板块。
- 数据查看、详细实时数据两个子板块保留原有查询、曲线、表格和导出能力。

约束：

- 左侧指标优先使用真实可用字段：`EPI`、设备最新功率、在线状态；暂不伪造没有报文来源的日用电、碳排等数据。

## 2026-06-06 电表实时详情页式布局调整

本次已完成：

- 设备监控面板点击“详情”后，电表实时详情从普通弹窗说明式布局调整为实时数据详情页式布局。
- 详情顶部展示返回、设备名称、客户/项目、在线状态和数据更新时间。
- 详情数据区使用青色标题参数卡片展示当前可接收字段：相电压、相电流、有功功率、功率因数、有功电能。
- 报文信息区展示 `gatewaySn`、`meterSn`、`meterNo`、`source`、`state`、`timestamp`。

约束：

- 不展示线电压、无功功率、视在功率、频率、温度、开关量等当前 EIOT 实时报文没有的字段。

验证结果：

- `pnpm exec eslint src\views\energy\telemetry\index.vue`：通过。
- `pnpm build:local`：通过。

## 2026-06-06 管理端首屏加载动画调整

本次已完成：

- 管理端 `index.html` 首屏加载动画从 RuoYi/Yudao 默认 Logo 和双圈旋转改为移动储能项目风格。
- 新动画使用浅色后台背景、电池形能量条、四色能量脉冲和进度条。
- 首屏文案保留项目标题，并显示“系统初始化中”。

验证结果：

- `pnpm build:local`：通过。

## 2026-06-06 管理端侧边栏平台标识调整

本次已完成：

- 管理端侧边栏“移动储能运营管理平台”标题旁图标从默认兔子头像替换为移动储能项目专用电池/能量标识。
- 新增图标资源 `current/admin-vue3/src/assets/imgs/energy-logo.svg`。
- 侧边栏 Logo 组件 `current/admin-vue3/src/layout/components/Logo/src/Logo.vue` 已改为引用新图标。
- 本次只调整侧边栏标题旁标识，登录页等仍引用原有 `logo.png` 的位置暂不变。
## 2026-06-06 电表实时详情下方信息补充

本次已完成：

- 管理端实时监控页的电表实时详情下方新增“历史曲线”和“报警信息”两个页签。
- 历史曲线按当前电表、日期和参数类型调用 `GET /admin-api/energy/telemetry/chart`，并按当天固定 5 分钟时间轴补齐展示；没有采集值的位置保持空值，不伪造数据。
- 报警信息按当前电表调用 `GET /admin-api/energy/alarm/page`，展示报警编号、代码、标题、等级、状态、发生时间和报警内容。
- 本次不新增后端接口和数据库字段，复用现有遥测曲线接口与告警分页接口。

约束：

- 详情页仍只展示当前 EIOT 已接收并入库的真实字段：`Ua/Ub/Uc`、`Ia/Ib/Ic`、`Pa/Pb/Pc/P`、`PF`、`EPI` 以及网关/仪表/报文识别信息。
## 2026-06-06 微信小程序独立板块建立

本次已完成：

- 在当前项目中新建独立小程序板块 `current/wechat-miniprogram-section`。
- 已从 `current/miniprogram` 复制 `src`、`dist`、`package.json`、`package-lock.json`、`vite.config.ts` 和 `index.html`。
- 新板块保留微信开发者工具可打开产物：`current/wechat-miniprogram-section/dist/build/mp-weixin`。
- 原始 `current/miniprogram/src` 未修改，后续微信登录、客户区分、授权范围选择优先在新板块内开发。
- 新增 `current/wechat-miniprogram-section/README.md`，记录来源、打开路径和后续开发原则。
## 2026-06-07 微信小程序客户范围初版

本次已完成：

- 历史阶段：新小程序板块 `current/wechat-miniprogram-section` 曾完成客户范围选择前端初版。
- 当前状态：客户版已改为微信登录后直接进入首页，不再展示客户范围选择、账号密码联调、客户管理或计费规则维护入口。
- 请求鉴权以微信登录生成的 App 用户 token 为准，业务数据范围统一由后端 `energy_user_scope` 校验。
- 新板块 `node_modules` 使用本地 Junction 指向原小程序依赖目录，已通过 `npm run build:mp-weixin` 构建。

约束：

- 后端当前仍需要补正式微信 code 登录接口 `/app-api/energy/auth/wechat-login`。
- App 端设备列表后续建议支持并校验 `customerId`，当前前端仅做请求头/参数预留和本地兼容过滤。

## 2026-06-07 微信小程序用户自动出现授权列表

本次已完成：

- 后端新增 `POST /app-api/energy/auth/wechat-login`，小程序通过微信 code 登录。
- 后端复用 RuoYi `SocialUserApi` 获取微信小程序 openid，并使用 `system_social_user` / `system_social_user_bind` 保存 openid 与 App 用户绑定关系。
- 首次微信登录时自动创建 `energy_app_user`，用户名使用 `wx_{openid}`，昵称优先使用前端传入昵称，其次使用微信昵称，最后使用“微信小程序用户”。
- 已存在绑定关系时直接复用原 App 用户并刷新登录信息，返回 RuoYi OAuth2 MEMBER token。
- 管理端用户授权已改为自动加载 App 用户选择器；运营人员只需要选择新出现的小程序用户，再开放客户、项目或设备权限。

约束：

- 微信 code 只能通过真实微信小程序运行环境获取；接口连通性可编译验证，完整登录需在微信开发者工具里触发。
- 新用户未授权前，小程序业务数据仍返回空范围，不回退租户全量。

## 2026-06-07 微信小程序 AppID 配置对齐

本次已完成：

- 将后端本地 `wx.miniapp.appid` 对齐为当前微信开发者工具项目使用的小程序 AppID。
- 将独立小程序板块 `current/wechat-miniprogram-section/src/manifest.json` 的 `mp-weixin.appid` 固定为同一个 AppID，避免重新构建后产物 AppID 被置空或回退。
- AppSecret 不再明文写入后端本地运行配置；后端通过 `ENERGY_WECHAT_APP_SECRET` 环境变量读取。

验证要求：

- 修改后必须重新构建小程序并重启后端。
- 微信 `code` 只能使用一次；重新打开登录页后需重新点“微信一键登录”，不能复用旧失败请求里的 code。

## 2026-06-07 小程序登录后直接进入首页

本次已完成：

- 微信登录和本地账号密码联调登录成功后，直接进入 `pages/index/index` 首页。
- 小程序不再向客户展示“客户范围/授权范围”页面，也不再在首页展示当前授权范围或客户范围入口。
- 设备列表不再按前端选择的客户范围二次过滤；数据范围统一以后端 `energy_user_scope` 授权结果为准。

约束：

- 新微信用户首次登录后会自动生成 App 用户；运营人员在管理端“用户授权”中给该用户开放客户、项目或设备权限。
- 未授权用户进入首页时只能看到空业务数据或暂无设备提示，不展示后台授权细节。

## 2026-06-07 管理端运营面板 SOC 标题调整

本次已完成：

- 管理端运营面板中的“SOC 水位分布”标题已改为“SOC”。
- 本次只调整展示文案，不改变图表数据来源和统计逻辑。

## 2026-06-07 管理端电表分合闸入口

本次已完成：

- 管理端电表实时详情顶部新增“合闸”“开闸”“刷新状态”操作入口，操作前必须二次确认。
- 后端新增 `POST /admin-api/energy/device/control`，权限标识为 `energy:device:control`。
- 新增 `energy_device_control_log` 操控日志表设计和后端 DO/Mapper，每次控制请求记录设备识别信息、控制方法、控制参数、EIOT 请求体、响应体、返回码、成功/失败状态、失败原因和操作人。
- 本地配置新增 `energy.eiot.control-url`、`energy.eiot.control-token` 占位；未配置控制地址时接口返回“EIOT 控制地址未配置”，不伪装成控制成功。

约束：

- 第一版只开放 `SWITCH` 合闸/开闸和 `REFRESH` 刷新状态；`FORCESWITCH`、`RESET` 暂不在页面开放。
- 真正下发 EIOT 控制需要先拿到现场控制接口地址、鉴权 Token 或登录凭证，再填写本地配置并验证 EIOT 响应格式。

## 2026-06-08 管理端运营面板命名调整

本次已完成：

- 管理端运营入口展示名称统一改为“运营面板”。
- 首页快捷入口、运营页面顶部标题、加载失败提示和移动储能菜单初始化 SQL 已同步调整。
- 当前本地 MySQL 菜单 `system_menu.id=190010001000` 需要同步为“运营面板”后，侧边栏菜单会显示新名称。

约束：

- 仅调整展示名称；`/energy/dashboard` 路由、`energy/dashboard/index` 组件、`/admin-api/energy/dashboard/*` 接口和 `energy:dashboard:query` 权限标识保持不变。

## 2026-06-08 运营面板新增数据面板

本次已完成：

- 在管理端运营面板下方新增“数据面板”，用于用图表快速展示已接收到并入库的电表遥测数据。
- 数据面板支持选择电表、时间范围和数据类型，数据类型包括有功功率、相电压、相电流、功率因数、有功电能。
- 图表复用 `GET /admin-api/energy/telemetry/chart`，按当前查询条件绘制折线曲线，并展示采集点数、最新时间、最新值和报文状态。
- 面板右上角提供“查看详细数据”入口，跳转到实时监控页继续查看日原始数据、逐日极值和详细实时数据。

约束：

- 本次仅新增运营面板快速图表，不新增后端接口和数据库字段。
- 运营面板只展示当前报文真实具备的字段：`Pa/Pb/Pc/P`、`Ua/Ub/Uc`、`Ia/Ib/Ic`、`PF`、`EPI`。

## 2026-06-09 充放电任务联动设备控制

本次已完成：

- 后端充放电开始任务时，完成设备归属、进行中会话、计费规则校验后，自动调用设备控制服务下发 `SWITCH=1` 合闸。
- 后端充放电结束任务时，完成会话状态、电能读数和费用计算校验后，自动调用设备控制服务下发 `SWITCH=0` 开闸。
- 设备控制服务会继续写入 `energy_device_control_log`，记录由任务触发的合闸/开闸请求、请求体、响应体和失败原因。
- 如果设备控制失败，例如 `energy.eiot.control-url` 未配置或 EIOT 返回失败，则开始任务不创建会话，结束任务不改成已结束，并返回“设备控制失败，原因：...”。

约束：

- 当前仍需要配置真实 EIOT 控制地址、鉴权 Token 和确认响应格式，才能完成现场实控闭环。
- 第一版只联动 `SWITCH` 合闸/开闸；`FORCESWITCH`、`RESET` 等高级控制暂不接入任务流。

## 2026-06-09 小程序扫码车辆校验和放电入口

> 历史记录：该方案已被 2026-06-10 的“设备/充电桩二维码 + 账户识别”流程替代。车辆字段仅保留旧二维码兼容，不再作为新扫码放电主流程。

本次已完成：

- 后端新增小程序扫码校验接口 `POST /app-api/energy/charging-sessions/scan/verify`。
- 后端新增小程序扫码放电接口 `POST /app-api/energy/charging-sessions/scan/discharge`。
- 新增本地数据库表 `energy_vehicle`，用于保存车辆编号、车牌号、二维码编码和绑定设备。
- 扫码内容优先按 `vehicleId`、`vehicleNo`、`plateNo`、`qrCode` 识别车辆，也兼容直接使用 `deviceId`、`deviceNo`、`meterNo`，以及二维码链接中携带 `deviceId/deviceNo/meterNo/gatewaySn/meterSn` 参数。
- 后端按当前微信登录 App 用户的 `energy_user_scope` 授权范围校验车辆/设备；只有授权到设备、所属项目或所属客户时才允许继续。
- 扫码放电接口会再次校验账号和设备，不信任前端校验结果；通过后以 `sessionType=1` 发起放电会话，并复用充放电任务的计费规则匹配和设备控制联动。
- 历史旧方案：小程序首页新增“扫码放电”入口，扫码后先展示车牌号、车辆编号、绑定设备和仪表信息，用户确认后再开始放电任务。

约束：

- 历史旧方案：车辆必须绑定 `device_id` 后才允许扫码放电；停用车辆不能通过扫码校验。
- 真实放电仍依赖 EIOT 控制地址/Token 配置和现场响应格式验证；控制失败时不会创建放电会话。

## 下一步

1. 填写真实 EIOT 控制地址/凭证，验证 `SWITCH` 合闸/开闸响应格式。
2. 在现场联调开始任务、结束任务和操控日志，确认任务状态与设备状态一致。
3. 确认放电电能来源，补充 `EPE` 或 PCS/BMS 放电电能字段后完善放电费用计算。
## 2026-06-10 扫码刷卡账户识别当前状态

当前扫码放电主流程已经从“车辆二维码”调整为“设备/充电桩二维码 + 账户识别”。

- 新二维码内容优先定位设备/充电桩/仪表：`deviceId`、`deviceNo`、`meterNo`、`gatewaySn + meterSn`。
- 旧车辆二维码字段 `vehicleId`、`vehicleNo`、`plateNo`、`qrCode` 仅作为兼容路径保留。
- 微信扫码时，司机身份来自当前小程序登录 App 用户。
- 刷卡时，司机身份来自 `cardNo` 绑定的 App 用户。
- 未录入、停用、未授权账户统一按“未知账户”处理，不允许开始放电。
- 后端新增 `energy_account_event`，记录扫码校验、开始放电和后续刷卡识别事件。
- 管理端新增“移动储能 / 扫码刷卡记录”，用于查看设备、账户、卡号、识别结果和发生时间。
- “车辆绑定”保留为可选车辆资产绑定和旧二维码兼容模块，不再作为新扫码放电主流程前置条件。
## 2026-06-12 审查后安全与小程序收口

- EIOT 入站接口已从裸 `@PermitAll` 收口为 `@PermitAll + X-EIOT-Token` 固定 token 校验，本地配置项为 `energy.eiot.inbound-token`。后续 EIOT 平台推送实时数据/报警时必须加该请求头。
- App 账号密码登录默认关闭，配置项为 `energy.app.password-login-enabled=false`；客户版小程序只保留微信登录。
- 小程序客户版已移除设置/用户入口，`pages.json` 不再注册客户管理和计费规则维护页面；设备详情移除编辑入口。
- 后端补齐小程序只读兼容接口：设备详情、最新遥测、历史遥测、报警列表/详情，均复用 App 用户授权范围过滤。
- 验证：`mvn -pl yudao-module-energy -am -DskipTests compile` 通过；`npm run build:mp-weixin` 通过。

## 2026-06-12 本地密钥与客户侧文档收口

本次已完成：

- 后端本地 `wx.miniapp.appid` 改为读取 `ENERGY_WECHAT_APP_ID`，默认仍使用当前小程序 AppID。
- 后端本地 `wx.miniapp.secret` 改为读取 `ENERGY_WECHAT_APP_SECRET`，不再把真实 AppSecret 明文保存在 `application-local.yaml`。
- 小程序板块 `wechat-miniprogram-section/README.md` 已同步为当前客户侧正式方案：仅微信登录、无客户范围页、无客户管理/计费规则维护入口、设备详情只读。
- `design/11-api-contracts-v1.md` 中 EIOT 入站 Header 已更新为必填 `X-EIOT-Token`，不再保留“第一版免登录”的旧表述。

本地启动后端前需要在 PowerShell 设置：

```powershell
$env:ENERGY_WECHAT_APP_ID="wxf43120ae9b42d6dd"
$env:ENERGY_WECHAT_APP_SECRET="<小程序 AppSecret>"
```

约束：

- 不在设计文档、README 或可提交配置里写真实 AppSecret。
- 如果未设置 `ENERGY_WECHAT_APP_SECRET`，小程序微信登录会在后端 code 换 openid 阶段失败。
- `design/sql/mysql/energy_schema.sql` 已移除固定 App 演示账号和演示授权，避免初始化数据库时产生可复用测试账户。
- 本机 MySQL 中历史遗留的 `appdemo` App 用户和对应设备授权已改为停用状态，用于避免旧测试账号继续访问业务数据。

## 2026-06-13 数据库与菜单同步检查

本机 MySQL `ruoyi-vue-pro` 已检查：

- 移动储能业务表已存在 15 张：客户、项目、设备、车辆绑定、App 用户、用户授权、遥测、告警、告警处理、计费规则、充放电记录、调度任务、EIOT 同步日志、设备控制日志、扫码刷卡记录。
- 关键新增字段/表已存在：`energy_app_user.card_no`、`energy_vehicle`、`energy_account_event`、`energy_device_control_log`。
- 移动储能菜单权限已存在 44 个，包含运营面板、设备台账、实时监控、告警中心、客户管理、项目场站、计费规则、充放电记录、调度任务、EIOT 同步日志、用户授权、车辆绑定、使用账户、扫码刷卡记录。
- `admin` 用户绑定 `超级管理员` 角色，该角色已拥有全部 44 个移动储能菜单权限。
- 移动储能字典已存在：设备状态、设备类型、运行模式、告警等级、告警状态、会话类型、会话状态、调度状态。

结论：当前本机数据库结构和菜单权限已经同步到移动储能当前功能阶段；下一步优先配置真实 EIOT 控制地址/凭证并跑通微信小程序登录授权闭环。

## 2026-06-13 微信登录授权闭环启动状态

本次已启动并验证：

- Redis 兼容服务使用 Garnet 启动，监听 `127.0.0.1:6379`。
- 后端使用 `java -jar yudao-server/target/yudao-server.jar --spring.profiles.active=local` 启动成功，监听 `48080`。
- 后端启动时通过环境变量注入 `ENERGY_WECHAT_APP_ID` 和 `ENERGY_WECHAT_APP_SECRET`，未写入配置文件。
- 管理端 Vue3 已启动，访问地址：
  - 本机：`http://localhost/`
  - 局域网：`http://192.168.5.46/`
- 小程序客户版请求地址已同步为 `http://192.168.5.46:48080/app-api/energy`，并已重新执行 `npm run build:mp-weixin`。
- 后端接口验证：
  - `POST /app-api/energy/auth/login` 带 `tenant-id: 1` 返回 `App password login is disabled`，符合客户版只允许微信登录的设计。
  - `POST /app-api/energy/auth/wechat-login` 带测试假 code 可到达微信授权逻辑并返回 `invalid code`，说明后端小程序 AppID/Secret 注入生效，真实 code 需从微信开发者工具获取。
- 当前数据库已有 1 个启用微信小程序用户 `wx_oxWxt13t5CjslAm1iZ3dLS_rH2rQ`，且已有 1 条启用授权；历史 `appdemo` 用户已停用且无启用授权。

微信开发者工具打开路径：

```text
D:\code\ankerui\energy-storage-platform\current\wechat-miniprogram-section\dist\build\mp-weixin
```

下一步验证动作：

1. 在微信开发者工具打开上面的 `mp-weixin` 目录。
2. 点击微信登录，确认能进入小程序首页。
3. 管理端进入“移动储能 / 使用账户”查看是否出现当前微信用户。
4. 如用户未授权，在“用户授权”给该用户开放设备、项目或客户权限。
5. 回到小程序刷新首页和设备列表，确认只展示授权范围内设备。

## 2026-06-13 使用账户授权选择器收口验证

本次已完成：

- 后端重新打包并使用最新 `yudao-server.jar` 启动成功，监听 `http://localhost:48080`。
- 管理端 `admin/admin123` 登录验证通过。
- `GET /admin-api/energy/app-user/simple-list` 已改为只返回启用状态 App 用户，避免历史停用账号进入“用户授权”的选择器。
- 当前接口返回 1 个启用微信小程序用户：`2063502199719153666 / 微信小程序用户`。
- 历史停用账号 `appdemo` 不再出现在授权下拉列表中。

结论：后续新增微信小程序用户会自动出现在“移动储能 / 使用账户”；管理员只需要在“用户授权”中给启用用户开放设备、项目或客户范围，不需要手动新增 App 用户。

## 2026-06-13 扫码放电记录结果收口

本次已完成：

- `POST /app-api/energy/charging-sessions/scan/discharge` 的开始放电记录从“只记录账户已识别”调整为“成功/失败都记录任务结果”。
- 当账户已识别但后续启动失败，例如未匹配计费规则、已有进行中任务、设备未绑定客户、EIOT 控制地址未配置或设备控制失败时，管理端“扫码刷卡记录”的 `resultMessage` 会写入“账户已识别，放电启动失败：具体原因”。
- 放电真正创建成功时，记录提示为“账户已识别，放电任务已开始”。
- 后端验证：`mvn -pl yudao-module-energy -am -DskipTests compile` 通过，`mvn -pl yudao-server -am -DskipTests package` 通过。
- 新 jar 已重启，后端监听 `http://localhost:48080`；管理端 `admin/admin123` 登录验证通过。
- 当前 `GET /admin-api/energy/app-user/simple-list` 仍只返回 1 个启用微信小程序用户，授权选择器不会显示停用 `appdemo`。

说明：本机历史小程序 token 已过期，未直接伪造 App 登录态做放电接口运行时写入测试；需要用户在微信开发者工具中重新微信登录后，再扫码验证记录落库。

## 2026-06-13 扫码启动前端反馈完善

本次已完成：

- 管理端“扫码刷卡记录”页面将原“结果”列调整为“账户结果”，并新增“任务结果”列。
- “任务结果”会按记录内容显示“仅校验、已拒绝、已启动、启动失败”，避免把账户已识别误看成放电已启动。
- 页面统计卡新增“启动失败”，方便运营人员快速发现账户识别成功但任务未下发的情况。
- 小程序扫码启动时，如果开始放电接口失败，会弹窗展示后端返回的具体失败原因。

验证结果：

- 管理端：`pnpm exec eslint src/views/energy/account-event/index.vue` 通过。
- 管理端：`pnpm build:local` 通过。
- 小程序：`npm run build:mp-weixin` 通过，产物已更新到 `wechat-miniprogram-section/dist/build/mp-weixin`。

## 2026-06-13 设备侧刷卡接口落地

本次已完成：

- 新增 EIOT 设备侧刷卡校验接口：`POST /infra-api/energy/eiot/card/verify`。
- 新增 EIOT 设备侧刷卡启动放电接口：`POST /infra-api/energy/eiot/card/discharge`。
- 两个接口都必须携带 `X-EIOT-Token`，不依赖小程序登录态。
- 请求体统一为 `scanText + cardNo`；`scanText` 可为 `deviceNo/deviceId/meterNo` 或带参数链接。
- 后端使用 `cardNo` 匹配启用的 App 用户，并复用 `energy_user_scope` 校验设备、项目或客户授权。
- 未知卡号、停用账户、未授权账户均不得启动放电，并写入“扫码刷卡记录”。

验证结果：

- 后端：`mvn -pl yudao-module-energy -am -DskipTests compile` 通过。
- 后端：`mvn -pl yudao-server -am -DskipTests package` 通过。
- 新 jar 已重启，后端监听 `http://localhost:48080`。
- 本地调用 `POST /infra-api/energy/eiot/card/verify`，使用不存在卡号 `CARD-CODEX-UNKNOWN-20260613`，返回 `accountKnown=false`、`message=未知账户`。
- 管理端 `GET /admin-api/energy/account-event/page?authType=CARD` 可查到该刷卡记录，记录内容包含卡号、设备、识别结果和提示。

## 2026-06-13 设备台账补全

本次完成现场设备台账补全要求：

- 管理端设备新增/修改要求补齐网关序列号、电表序列号、仪表编号、所属客户和项目场站。
- 管理端设备列表新增电表序列号、仪表编号搜索，并展示“台账状态”：客户、项目、网关序列号、电表序列号、仪表编号齐全时为“完整”，否则为“待补全”。
- 后端设备分页接口支持 `meterSn`、`meterNo` 查询，小程序/扫码关键字匹配覆盖 `meterNo`、`gatewaySn`、`meterSn`。
- 本地库已补齐当前两类 EIOT 报文来源对应的设备台账：`12207013690004_12207013690004`、`20241204870083_20241204870083`。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- `pnpm exec eslint src/views/energy/device/index.vue src/views/energy/device/DeviceForm.vue src/api/energy/device/index.ts`：通过。
- `pnpm build:local`：通过。
- `mvn -pl yudao-server -am -DskipTests package`：通过。
- 新后端 jar 已重启，监听 `http://localhost:48080`。
- 管理端登录 `admin/admin123` 验证通过；`GET /admin-api/energy/device/page?pageNo=1&pageSize=10&meterNo=20241204870083_20241204870083` 可查到新增台账。
- 缺少台账必填字段时，设备新增接口返回 `code=400`，提示 `网关序列号不能为空`。

## 2026-06-14 小程序 Step 7 记录/账单接口适配

本次继续完成 Step 7“小程序接口适配”中的记录/账单部分：

- `GET /app-api/energy/charging-sessions` 从固定空数组改为返回当前 App 用户授权范围内的真实充放电会话。
- 会话列表支持 `deviceId`、`customerId`、`status`、`from`、`to`、`limit` 筛选，状态兼容小程序使用的 `ACTIVE`、`COMPLETED`、`ABNORMAL`、`SETTLED`。
- 返回结构适配小程序统计详情页当前字段：设备、客户、计费规则单价、开始/结束时间、开始/结束电能读数、总电量、时长、费用和状态。
- 权限边界复用 `energy_user_scope`：未授权用户返回空会话；已授权用户只能看到授权设备、项目或客户下的会话。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- 首次 `mvn -pl yudao-server -am -DskipTests package` 因旧后端占用 `yudao-server.jar` 打包失败；停止旧 Java 进程后重新打包通过。
- 新后端 jar 已启动，监听 `http://localhost:48080`。
- 未登录访问 `GET /app-api/energy/charging-sessions` 返回 `code=401`，仍保持小程序业务接口必须登录的边界。
- 本地库历史小程序 token 已过期，未使用临时伪造 token 做授权数据深测；需要在微信开发者工具重新微信登录后验证统计详情页真实会话数据。
## 2026-06-14 小程序统计/账单 bug 修复

本次在 Step 7 小程序接口适配基础上继续修复统计与账单展示问题：

- 小程序首页收入、今日充电量、今日放电量、进行中会话、出租率不再使用固定空数据或旧兼容假数据，后端按当前 App 用户授权范围内的真实会话和授权设备计算。
- `/app-api/energy/charging-sessions/revenue-overview` 按今日已完成会话计算今日收入，按本月已完成会话计算月收入，并按进行中会话与在线授权设备计算出租率。
- `/app-api/energy/charging-sessions/today-energy` 按今日已完成会话区分 `sessionType=0` 充电、`sessionType=1` 放电，返回真实会话电量汇总。
- 修复小程序传入 ISO 时间字符串（例如 `2026-06-13T16:00:00.000Z`）时被后端误当成本地时间解析的问题；带 `Z` 或时区偏移的时间统一按偏移量转换为系统本地时区。
- 后端不再把当前 EIOT 未接收到的 `EPE` 伪装为放电电能；会话返回中 `totalEnergy` 作为通用结算电量，`startEpe/endEpe` 在无真实来源时保持空值。
- 小程序首页账单明细按 `status=COMPLETED` 查询已完成会话，避免把进行中或异常会话混入账单；列表增加 `limit`，减少默认条数过小导致的统计缺失。
- 小程序统计详情页和首页账单显示增加空值保护，避免 `startEpi/startEpe/endEpi/endEpe` 为空时出现 `NaN`。

验证结果：

- `mvn -pl yudao-module-energy -am -DskipTests compile`：通过。
- `npm run build:mp-weixin`：通过，产物已更新到 `wechat-miniprogram-section/dist/build/mp-weixin`。
- `mvn -pl yudao-server -am -DskipTests package`：通过。
- 新后端 jar 已重新启动，监听 `http://localhost:48080`。
- 未登录访问 `GET /app-api/energy/charging-sessions` 返回 `{"code":401,"msg":"账号未登录","data":null}`，小程序业务接口仍需要登录。

后续验证：

1. 在微信开发者工具中重新微信登录，刷新首页。
2. 确认首页收入、今日电量、账单明细和统计详情页使用同一批授权会话数据。
3. 如放电电量需要现场精确结算，需 EIOT 后续提供真实反向电能 `EPE` 或 PCS/BMS 放电电量字段后再接入。
## 2026-06-14 小程序真实字段与结算参考收口

本次继续修复小程序侧另一些容易误导现场用户的问题：

- 小程序首页、设备卡片、设备详情和历史趋势参数选择，移除当前 EIOT 报文没有稳定提供的数据展示入口，包括反向有功电能 `EPE`、无功功率 `Q`、频率 `Fr`、温度 `Temp` 和 BMS/电池温度等字段。
- 设备详情保留当前真实电表报文字段：三相电压、三相电流、有功功率、功率因数、正向有功电能。
- 历史趋势参数保留 `Ua/Ub/Uc`、`Ia/Ib/Ic`、`Pa/Pb/Pc/P`、`PF`、`EPI`。
- `/app-api/energy/devices/{id}/latest` 和 `/app-api/energy/devices/{id}/readings` 不再向小程序兼容层透出未接入字段，避免前端后续误用。
- `/app-api/energy/devices/energy-delta` 不再固定返回 0；在当前只有 `EPI` 的前提下，按授权设备在查询时间范围内的首尾 `EPI` 计算正向有功电能变化，放电/反向电能继续保持 0，不伪造 `EPE`。
- 小程序扫码启动成功后的账单刷新补齐 `status=COMPLETED` 和 `limit=500`，避免刚创建的进行中会话混入首页账单明细。

验证结果：

- `D:\code\tools\apache-maven-3.9.16\bin\mvn.cmd -pl yudao-module-energy -am -DskipTests compile`：通过。
- `D:\code\tools\apache-maven-3.9.16\bin\mvn.cmd -pl yudao-server -am -DskipTests package`：第一次因旧后端占用 jar 失败；停止旧 Java 进程后重新执行通过。
- `npm run build:mp-weixin`：通过，产物已更新到 `wechat-miniprogram-section/dist/build/mp-weixin`。
- 新后端 jar 已重启，监听 `http://localhost:48080`；未登录访问 `GET /app-api/energy/charging-sessions` 返回 `401`。
- `D:\code\tools\apache-maven-3.9.16\bin\mvn.cmd -pl yudao-server -am -DskipTests package`：通过。
- 新后端 jar 已重新启动，监听 `http://localhost:48080`。
- 未登录访问 `GET /app-api/energy/charging-sessions` 返回 `{"code":401,"msg":"账号未登录","data":null}`。

约束：

- 当前仍不能显示真实放电电能读数；需要 EIOT 后续提供 `EPE` 或 PCS/BMS 放电电量后再接入。
- 源码中保留的历史兼容组件如果未注册/未引用，不作为当前客户版小程序页面入口。
## 2026-06-14 管理端真实字段与在线状态修正

本次继续收口管理端展示与状态统计问题：

- 管理端首页、运营面板、设备台账和实时监控统一修正设备状态枚举：`0=在线`、`1=离线`、`2=故障`、`3=维护`。
- 修复首页、运营面板、实时监控中在线设备/离线设备统计反向的问题。
- 首页和运营面板移除当前 EIOT 电表报文未稳定提供的 SOC、平均温度等运营展示，改为在线率、最新采集时间、当前总功率和真实设备状态。
- 设备台账列表移除 SOC、温度列，保留设备状态、功率、台账完整性、客户/项目、网关序列号、电表序列号、仪表编号和最新采集时间。
- 实时监控工作台的状态筛选和状态标签改为在线、离线、故障、维护，不再把 `3` 显示为“报警”。

验证结果：

- `pnpm exec eslint src/views/Home/Index.vue src/views/energy/dashboard/index.vue src/views/energy/device/index.vue src/views/energy/telemetry/index.vue`：通过。
- `pnpm build:local`：通过，管理端构建产物已更新。

约束：

- 管理端仍可在实时监控中展示 EIOT 报警列表；但设备 `status=3` 按设备维护状态处理，不再混同为报警状态。
- 如后续接入 BMS/SOC/温度等真实字段，需先更新接口标准、数据库字段含义和页面设计，再恢复展示。
## 2026-06-14 小程序真实字段残留二次修正

本次继续检查小程序客户侧和 App 设备接口，修复上一轮遗漏的 SOC/假放电字段残留：

- 小程序设备卡片移除 SOC 圆环，改为展示当前功率、电压、电流、功率因数、正向有功电能和更新时间。
- 小程序设备详情头部移除 SOC，改为当前功率状态块；详情参数仍只展示三相电压、三相电流、有功功率、功率因数和 `EPI`。
- 首页“低电量”入口改为“离线设备”，统计详情页对应改为按真实在线状态筛选。
- 首页“数据汇总”中的平均 SOC 改为在线率；设备对比只展示正向有功电能和功率，不再展示 SOC。
- 结算参考数据表移除固定 0 的放电量列，只保留当前可由 `EPI` 计算的正向电能和净电量。
- `/app-api/energy/device/list` 的 App 设备响应移除 SOC/SOH/温度字段，并由后端补充最新遥测中的 `EPI`、功率、平均电压、平均电流和采集时间。
- `/app-api/energy/devices/energy-delta` 不再返回固定 0 的 `dischargeEnergy/totalDischarge` 字段，避免客户侧误以为已接入真实反向电能。

验证结果：

- `D:\code\tools\apache-maven-3.9.16\bin\mvn.cmd -pl yudao-module-energy -am -DskipTests compile`：通过。
- `npm run build:mp-weixin`：通过，产物已更新到 `wechat-miniprogram-section/dist/build/mp-weixin`。
## 2026-06-14 小程序司机端/管理端分流

本次按新的客户使用方式调整微信登录后的页面分流：

- 微信首次扫码/授权登录后，后端仍自动创建或复用 `energy_app_user`，但新增 `mini_admin_enabled` 字段，默认 false，表示司机端账号。
- 管理端“使用账户”列表新增“小程序管理”列，编辑弹窗新增“小程序管理”开关；运营人员打开后，该微信账号才具备小程序内管理入口。
- 小程序新增司机端首页 `pages/driver/index`，登录成功默认进入司机首页，页面提供扫码放电、订单、账户和服务入口。
- 小程序新增订单页 `pages/orders/index`，展示当前微信账号授权范围内的会话记录。
- 小程序新增我的页 `pages/mine/index`，刷新 `/app-api/energy/auth/profile` 获取最新权限；`miniAdminEnabled=true` 时显示“管理”入口。
- 原 `pages/index/index` 保留为小程序内管理页，仍包含设备概览、数据汇总、结算参考数据和导出能力；直接进入时会刷新权限，未开放管理权限会退回司机首页。
- 本机 MySQL 已执行表结构升级：`energy_app_user` 新增 `mini_admin_enabled BIT(1) NOT NULL DEFAULT b'0'` 和索引 `idx_energy_app_user_mini_admin`。

待验证：

- 后端 `mvn -pl yudao-module-energy -am -DskipTests compile`。
- 管理端 `pnpm exec eslint src/views/energy/appUser/index.vue src/views/energy/appUser/AppUserForm.vue` 和 `pnpm build:local`。
- 小程序 `npm run build:mp-weixin`。
## 2026-06-15 客户老板网页账号权限

本次新增“车队老板/客户方管理人员”的网页端账号预设能力，与小程序司机账号分离：

- 新增后端接口 `/admin-api/energy/customer-account/**`，用于创建、编辑、查询、重置客户老板账号密码和获取可开放板块。
- 新增业务表 `energy_customer_account`，绑定 `energy_customer`、后台 `system_user`、专属 `system_role` 和开放板块菜单。
- 新增管理端页面 `energy/customerAccount/index`，菜单为“移动储能 / 客户账号权限”。
- 创建客户老板账号时，后端自动创建后台登录用户、专属角色并分配所选板块权限；客户老板登录网页端后只显示被开放的板块。
- 默认只把所选板块的查询/导出权限分配给客户老板，不默认开放新增、删除、设备控制和任务启停。
- 本机 MySQL 已执行 `design/sql/mysql/energy_customer_account_menu.sql`，已写入菜单 `190010015000` 及按钮权限。

验证结果：

- `D:\code\tools\apache-maven-3.9.16\bin\mvn.cmd -pl yudao-module-energy -am -DskipTests compile`：通过。
- `pnpm exec eslint src/views/energy/customerAccount/index.vue src/views/energy/customerAccount/CustomerAccountForm.vue src/api/energy/customerAccount/index.ts`：通过。
- `pnpm build:local`：通过。
