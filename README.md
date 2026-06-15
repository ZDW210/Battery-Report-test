# 移动储能平台 Cloudflare Worker 部署包

这个文件夹是准备提交到 GitHub 并部署到 Cloudflare Workers 的独立包。当前目标架构是 **Worker + D1 + R2**，不依赖本地 MySQL，也不把 Java 后端部署到 Cloudflare。

## 目录

- `admin-vue3/`：管理端 Vue3 源码。
- `wechat-miniprogram-section/`：微信小程序板块源码，供微信开发者工具导入和继续迁移。
- `worker/`：Cloudflare Worker API 和静态资源入口。
- `migrations/`：D1 初始化脚本。
- `design/`：业务设计文档。
- `design-standards/`：以后设计和修改必须同步维护的标准文档。
- `backend-reference/`：原 Java 能源模块参考代码，只作为迁移参考，不参与 Worker 部署。

## Cloudflare 资源

先在 Cloudflare 创建：

1. D1 数据库：`energy-storage-platform`
2. R2 存储桶：`energy-storage-platform-files`

然后把 D1 的 `database_id` 填入 `wrangler.jsonc`：

```jsonc
"database_id": "你的 D1 database_id"
```

微信小程序相关密钥不要提交到 GitHub，使用 Worker Secret：

```powershell
npx wrangler secret put WECHAT_APP_SECRET
npx wrangler secret put SESSION_SECRET
```

`WECHAT_APP_ID` 可以写在 `wrangler.jsonc` 的 `vars`，也可以在 Cloudflare 控制台配置。

## 初始化 D1

```powershell
npx wrangler d1 migrations apply energy-storage-platform --remote
```

初始化后默认管理账号：

```text
用户名：admin
密码：admin123
```

上线后请立刻在管理端修改密码。

## GitHub 部署

Cloudflare Workers 连接 GitHub 仓库时，建议设置：

```text
Root directory: 仓库根目录
Build command: pnpm install && pnpm build
Deploy command: pnpm deploy
```

本包的 `pnpm build` 会执行：

1. 安装并构建 `admin-vue3`
2. 把管理端产物复制到 `worker/public`
3. 由 Worker 统一托管静态页面和 `/admin-api`

## 本地验证

```powershell
pnpm install
pnpm build
npx wrangler d1 migrations apply energy-storage-platform --local
pnpm dev
```

本地启动后，浏览器打开 Wrangler 输出的地址即可。

## 后续开发原则

- 新增业务接口优先写在 `worker/src/index.ts` 或拆分到 `worker/src/modules/`。
- 新增表结构必须放入 `migrations/`，不要再使用 MySQL 脚本作为部署入口。
- 涉及页面、权限、接口、数据结构变化时，同步更新 `design/` 和 `design-standards/`。
