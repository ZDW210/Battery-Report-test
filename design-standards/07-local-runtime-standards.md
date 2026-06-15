# 本机运行环境与启动标准

## 适用范围

本标准用于约束本机开发、联调和演示环境。后续如果调整 JDK、Maven、数据库、Redis、端口、启动命令或前端代理配置，需要同步更新本文件。

## 基准环境

- RuoYi 后端分支固定使用 `master-jdk17`。
- 后端运行 JDK 固定使用 Java 17，不使用更高版本 JDK 作为默认运行环境。
- Maven 使用独立安装目录，不依赖系统全局 Maven 是否存在。
- MySQL 使用本机服务，业务库名为 `ruoyi-vue-pro`。
- 后端应用使用专用 MySQL 账号，不直接使用 `root` 作为日常开发账号。
- Redis 可使用 Redis Server 或兼容服务；当前本机基线使用 Microsoft Garnet。

## 端口标准

| 服务 | 默认端口 | 说明 |
| --- | --- | --- |
| 后端服务 | `48080` | RuoYi `yudao-server` 默认端口 |
| Redis 兼容服务 | `6379` | 本机缓存、登录、验证码、Redisson 使用 |
| 管理端 | `80` 优先，冲突时顺延 | 当前本机实际使用 `81` |

## 数据库标准

本机开发数据库：

```text
127.0.0.1:3306/ruoyi-vue-pro
```

后端运行账号：

```text
username: ruoyi
password: ruoyi123456
```

标准要求：

- 基础 RuoYi SQL 先导入，再导入移动储能业务 SQL。
- 移动储能 SQL 必须继续放在 `current/design/sql/mysql` 下。
- 后续业务表、字典、菜单调整后，必须同时更新设计 SQL 和本标准的相关说明。

## 管理端安装标准

由于工作区上级目录存在 `pnpm-workspace.yaml`，管理端依赖安装必须使用：

```powershell
pnpm install --ignore-workspace --registry=https://registry.npmmirror.com
```

不得直接在管理端目录执行无参数 `pnpm install`，避免误处理其它 workspace 项目。

管理端本机代理配置以 `.env.local` 为准：

```text
VITE_BASE_URL='http://localhost:48080'
VITE_API_URL=/admin-api
```

## 变更要求

只要后续改变以下任一内容，必须同步更新 `current/design/13-local-runtime-status.md` 和本文件：

- 后端分支、JDK、Maven 版本
- MySQL 库名、账号、导入脚本顺序
- Redis 服务类型或端口
- 后端、管理端、小程序端端口
- 管理端代理配置
- 本机启动命令
