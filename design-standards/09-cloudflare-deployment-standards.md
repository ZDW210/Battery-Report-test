# Cloudflare 部署标准

## 适用范围

项目部署到 Cloudflare Workers 时，线上运行模式按 Worker + D1 + R2 设计，不依赖本地 MySQL，也不把 Java 后端作为线上运行服务。

## 目录约束

Cloudflare 部署相关内容统一放在 `cloudflare-worker-package/` 内：

- Worker 入口：`worker/`
- D1 迁移：`migrations/`
- 管理端源码：`admin-vue3/`
- 小程序板块：`wechat-miniprogram-section/`
- 部署说明：`README.md`

不要把 Cloudflare 部署脚本散放到 `current/`、桌面或其它临时目录。

## 管理端瘦身规则

- `admin-vue3/` 只保留移动储能项目需要的能源业务、系统基础、登录、首页和错误页。
- 不保留原 RuoYi Pro 的 BPM、商城、CRM、ERP、IoT、MES、WMS、公众号、支付、报表设计器等演示/通用业务模块。
- 新增管理端页面优先放入 `admin-vue3/src/views/energy/`，新增接口封装优先放入 `admin-vue3/src/api/energy/`。
- 如果后续确实要恢复被移除的大模块，必须说明业务原因、评估 Cloudflare 构建耗时影响，并同步更新 `README.md`、`docs/cloudflare-worker-d1-r2.md` 和本文档。

## D1 规则

- 新表、新字段必须通过 `migrations/` 增量脚本维护。
- SQL 使用 SQLite/D1 语法，不使用 MySQL 专属语法。
- D1 保存结构化业务字段、索引字段和权限字段。
- 大体积原始报文、导出文件、图片和附件放入 R2，D1 只保存 R2 key 和摘要信息。

## R2 规则

- 上传文件统一通过 Worker API 写入 R2。
- EIOT 原始报文后续按日期目录归档，例如 `eiot/raw/yyyy-MM-dd/{gatewaySn}-{timestamp}.json`。
- 管理端导出文件可生成到 R2，再返回临时下载地址或受控下载接口。

## 权限规则

- 平台管理员使用 `system_user` 登录。
- 车队老板账号使用 `energy_customer_account` 预设，并通过 `energy_customer_account_menu` 控制可见板块。
- 微信小程序司机用户和车队老板账号分离管理，不混用同一套登录入口。

## 同步要求

任何 Cloudflare 部署、D1 表结构、R2 文件策略、账号权限模型变化，都必须同步更新：

- `README.md`
- `docs/cloudflare-worker-d1-r2.md`
- 本标准文件
