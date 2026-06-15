# Cloudflare Worker + D1 + R2 架构说明

这个部署包不再运行 Java 后端，也不依赖 MySQL。Cloudflare Worker 同时承担三件事：

1. 托管管理端 Vue3 编译后的静态文件。
2. 提供 `/admin-api` 接口，直接读写 D1。
3. 通过 R2 保存导出文件、上传文件和后续 EIOT 原始报文归档。

## 当前范围

- 管理端登录、刷新令牌、权限菜单。
- 客户账号权限：可以预设车队老板账号，并控制可见业务板块。
- 客户、项目、设备、告警、实时数据基础 CRUD 和查询。
- 文件上传到 R2 的基础接口。

## 数据设计

D1 使用 SQLite 语法，初始化脚本在 `migrations/0001_init.sql`。当前保留了后续小程序和扫码刷卡要用到的基础表：

- `energy_app_user`：微信小程序用户。
- `energy_vehicle`：车辆和客户关系。
- `energy_account_event`：扫码或刷卡识别事件。
- `energy_customer_account`：车队老板管理端账号。
- `energy_customer_account_menu`：车队老板可见菜单。

## 权限原则

首次部署后默认管理员账号为：

```text
用户名：admin
密码：admin123
```

管理员进入“客户账号权限”后，可以为车队老板预设账号、重置密码、分配可见板块。车队老板登录网页端时，只能看到被授权的菜单。

## R2 用法

当前 Worker 暴露：

- `POST /admin-api/infra/file/upload`
- `GET /admin-api/infra/file/get/:key`

后续 EIOT 原始报文建议按日期写入 R2，例如：

```text
eiot/raw/2026-06-15/{gatewaySn}-{timestamp}.json
```

D1 只保存结构化查询字段和 R2 key，避免把大报文或导出文件直接塞进数据库。
