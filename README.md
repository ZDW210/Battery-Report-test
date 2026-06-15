# 移动储能平台 Cloudflare Worker 部署包

这个仓库用于部署到 Cloudflare Workers，当前架构是 **Worker + D1 + R2**。

## 重要配置

部署前必须先在 Cloudflare 创建：

1. D1 数据库：`energy-storage-platform`
2. R2 存储桶：`energy-storage-platform-files`

然后进入 D1 数据库详情页，把真实的 `database_id` 填入 `wrangler.jsonc`：

```jsonc
"database_id": "你的 D1 database_id"
```

当前部署失败的原因就是这里还不是有效 ID：

```text
binding DB of type d1 must have a valid `database_id` specified
```

## Cloudflare GitHub 部署设置

Cloudflare Workers 连接 GitHub 后，建议这样设置：

```text
Build command: 留空
Deploy command: bun run deploy
```

不能只填 `npx wrangler deploy`。如果只执行 Wrangler 部署，管理端 Vue3 页面不会被构建进去。当前 `wrangler.jsonc` 的 Assets 目录直接指向 `admin-vue3/dist-worker`，所以没有构建时部署会失败，避免再次发布空页面。

## 初始化 D1

首次部署前或首次部署后执行：

```powershell
npx wrangler d1 migrations apply energy-storage-platform --remote
```

初始化后默认管理账号：

```text
用户名：admin
密码：admin123
```

上线后请立刻修改密码。

## 密钥

微信小程序密钥不要提交到 GitHub，使用 Cloudflare Worker Secret：

```powershell
npx wrangler secret put WECHAT_APP_SECRET
npx wrangler secret put SESSION_SECRET
```

`WECHAT_APP_ID` 已写在 `wrangler.jsonc`：

```text
wxf43120ae9b42d6dd
```

## 目录

- `admin-vue3/`：管理端 Vue3 源码。
- `wechat-miniprogram-section/`：微信小程序板块源码。
- `worker/`：Cloudflare Worker API 和静态资源入口。
- `migrations/`：D1 初始化脚本。
- `design/`：业务设计文档。
- `design-standards/`：设计和修改必须同步维护的标准文档。
- `backend-reference/`：原 Java 能源模块参考代码，只作为迁移参考，不参与 Worker 部署。

## 本地验证

```powershell
pnpm install
pnpm build
npx wrangler d1 migrations apply energy-storage-platform --local
pnpm dev
```

## 管理端瘦身规则

Cloudflare Worker 包里的 `admin-vue3/` 只保留移动储能项目需要的能源业务、系统基础、登录、首页和错误页。原 RuoYi Pro 的 BPM、商城、CRM、ERP、IoT、MES、WMS、公众号、支付、报表设计器等演示/通用业务模块已经移除。

后续新增管理端功能时，优先放在 `admin-vue3/src/views/energy/` 和 `admin-vue3/src/api/energy/`，不要重新引入未使用的大模块；如果确实需要恢复某个模块，需要同步更新本文档和 `design-standards/09-cloudflare-deployment-standards.md`。
