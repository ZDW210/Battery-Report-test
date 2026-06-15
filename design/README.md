# 移动储能系统 RuoYi-Vue-Pro 设计包

本目录用于收纳基于 `YunaiV/ruoyi-vue-pro` 的移动储能系统设计方案。设计目标是把后端、管理端、小程序端的边界先定清楚，后续再按模块逐步落到代码。

## 设计标准

后续所有设计应遵循平级目录 [design-standards](../design-standards/README.md) 中的标准。

如果本目录中的业务设计、数据库、接口、页面或实施路线发生变化，需要同步检查并更新 `current/design-standards` 中对应标准。

## 目录

- [01-architecture.md](01-architecture.md)：整体架构、系统边界、部署建议
- [02-backend-modules.md](02-backend-modules.md)：RuoYi 后端模块设计
- [03-database-design.md](03-database-design.md)：核心数据库表设计
- [04-api-design.md](04-api-design.md)：管理端、小程序端、EIOT 接入接口设计
- [05-admin-ui-design.md](05-admin-ui-design.md)：管理端菜单、页面、功能设计
- [06-miniprogram-design.md](06-miniprogram-design.md)：小程序端页面与业务流程
- [07-implementation-roadmap.md](07-implementation-roadmap.md)：实施路线与迁移建议
- [08-ruoyi-module-scaffold.md](08-ruoyi-module-scaffold.md)：RuoYi 模块落地蓝图
- [09-current-project-migration-map.md](09-current-project-migration-map.md)：现有 NestJS/Prisma/uni-app/worker 到 RuoYi 的迁移映射
- [10-development-sequence.md](10-development-sequence.md)：开发实施顺序与验收标准
- [11-api-contracts-v1.md](11-api-contracts-v1.md)：首版接口契约
- [12-ruoyi-bootstrap-status.md](12-ruoyi-bootstrap-status.md)：RuoYi 基座拉取与本机启动状态
- [13-local-runtime-status.md](13-local-runtime-status.md)：本机运行基线、端口、账号与启动命令
- [sql/mysql/energy_schema.sql](sql/mysql/energy_schema.sql)：MySQL 业务表初始化脚本
- [sql/mysql/energy_dict.sql](sql/mysql/energy_dict.sql)：系统字典初始化脚本
- [sql/mysql/energy_menu.sql](sql/mysql/energy_menu.sql)：管理端菜单权限初始化脚本

## 推荐总方案

使用 RuoYi-Vue-Pro 承接后台系统能力：

- 后端：基于 `yudao-server`，新增 `yudao-module-energy`
- 管理端：基于 `yudao-ui`，新增移动储能相关菜单与页面
- 小程序端：继续使用独立 uni-app 项目，通过 `/app-api/energy/**` 对接后端
- 数据接入：保留独立 EIOT worker 或接入服务，用于安科瑞推送、原始报文归档、异常重试

## 设计原则

- 管理端和小程序端共享核心业务数据，但接口分层隔离
- 实时采集数据和业务主数据分离，避免高频数据拖慢管理业务
- 设备最新状态冗余到设备主表，曲线和历史数据进入时序/明细表
- 原始报文必须归档，方便审计、追溯、补偿计算
- 先完成设备、告警、客户、计费、充放电记录五个闭环，再扩展调度和工单
