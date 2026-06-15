# 移动储能系统设计标准

本目录是移动储能系统的设计标准基准。后续所有后端、管理端、小程序端、Cloudflare 部署相关设计，都应优先遵守这里的内容。

如果后续修改了业务设计、数据库、接口、页面、模块边界或实施路线，必须同步检查并更新本目录中的对应标准，避免设计文档和执行标准脱节。

## 标准目录

- [01-design-principles.md](01-design-principles.md)：总体设计原则
- [02-module-standards.md](02-module-standards.md)：后端模块与分层标准
- [03-database-standards.md](03-database-standards.md)：数据库设计标准
- [04-api-standards.md](04-api-standards.md)：接口设计标准
- [05-frontend-standards.md](05-frontend-standards.md)：管理端和小程序端设计标准
- [06-change-sync-rules.md](06-change-sync-rules.md)：设计变更同步规则
- [07-local-runtime-standards.md](07-local-runtime-standards.md)：本机运行环境与启动标准
- [07-vehicle-binding-standards.md](07-vehicle-binding-standards.md)：车辆绑定与扫码刷卡标准
- [08-account-identification-standards.md](08-account-identification-standards.md)：账号识别与授权标准
- [09-cloudflare-deployment-standards.md](09-cloudflare-deployment-standards.md)：Cloudflare Worker + D1 + R2 部署标准

## 使用方式

1. 新增设计前，先阅读本目录对应标准。
2. 修改已有设计时，同时检查标准是否需要更新。
3. 如果业务设计和标准冲突，先修改标准并说明原因，再修改具体设计。
4. Cloudflare 部署包内的设计变更，优先同步到本目录和 `docs/`。
