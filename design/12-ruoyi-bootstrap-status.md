# RuoYi 基座落地状态

日期：2026-05-29

## 已完成

### 后端主仓库

已拉取 RuoYi-Vue-Pro 主仓库：

```text
D:\code\ankerui\energy-storage-platform\current\backend-ruoyi
```

仓库信息：

- 分支：`master-jdk17`
- 后端：Java 17 + Spring Boot 3.5.9
- 版本：`2026.04-SNAPSHOT`
- 后端入口：`yudao-server`
- 默认端口：`48080`
- 默认数据库：MySQL `127.0.0.1:3306/ruoyi-vue-pro`
- 默认 Redis：`127.0.0.1:6379`

### Vue3 管理端完整仓库

已拉取 RuoYi Vue3 管理端完整仓库：

```text
D:\code\ankerui\energy-storage-platform\current\admin-vue3
```

说明：

- `D:\code\ankerui\energy-storage-platform\current\backend-ruoyi\yudao-ui\yudao-ui-admin-vue3` 只是主仓库里的业务源码片段。
- 真正可启动的 Vue3 管理端是独立仓库 `yudaocode/yudao-ui-admin-vue3`。
- 本机 Node 为 `v24.15.0`，pnpm 为 `10.33.0`，满足该前端仓库 `node >= 20.19.0`、`pnpm >= 8.6.0` 的要求。

## 已核对

### 系统表字段

已核对 `sql/mysql/ruoyi-vue-pro.sql` 中以下系统表：

- `system_dict_type`
- `system_dict_data`
- `system_menu`

结论：

- `energy_menu.sql` 字段与当前版本 `system_menu` 匹配。
- `energy_dict.sql` 字段与当前版本基本匹配。
- `system_dict_type` 当前版本多了 `deleted_time` 字段，但该字段允许为空，当前设计脚本指定字段插入时可不填写。

### Docker

本机 Docker Desktop 已启动成功。

Docker Hub 直连拉取 `redis:6-alpine`、`mysql:8` 失败，原因是访问 `registry-1.docker.io` 超时。

尝试使用 `docker.m.daocloud.io/library/redis:6-alpine` 拉取 Redis，下载过程中卡住，未形成可用镜像，已终止卡住的 docker CLI 进程。

## 当前阻塞

### MySQL

本机 MySQL 8.0 已安装并运行：

```text
127.0.0.1:3306
```

但以下账号不可用：

```text
root / 123456
root / 空密码
```

需要提供一个可用 MySQL 管理账号，用于：

- 创建 `ruoyi-vue-pro` 数据库
- 导入 RuoYi 初始化 SQL
- 后续导入 `energy_` 业务表、字典、菜单

### Redis

当前本机没有可用 Redis：

- 未发现本地 `redis-server`
- Docker Redis 镜像尚未拉取成功

RuoYi 后端启动依赖 Redis，需要解决其一：

1. 安装本机 Redis 兼容服务
2. 配置 Docker 镜像源后拉取 Redis
3. 使用已有内网镜像仓库中的 Redis 镜像

### Maven 和 JDK

当前环境：

```text
JAVA_HOME=D:\code\JDK\jdk26
Maven 未安装或不在 PATH
```

当前 RuoYi 主仓库已切换到 `master-jdk17`，要求 Java 17。当前本机 JDK 26 高于目标版本，可能存在兼容风险，建议安装 JDK 17 并将该项目运行环境切到 JDK 17。

## 下一步建议

优先处理顺序：

1. 安装 JDK 17，并将该项目运行环境切到 JDK 17。
2. 安装 Maven 或使用 Docker Maven 镜像构建。
3. 提供或创建 MySQL 可用账号。
4. 解决 Redis：本机安装或 Docker 镜像源。
5. 导入 RuoYi 初始化 SQL。
6. 启动 `yudao-server`。
7. 启动 `yudao-ui-admin-vue3`。
8. 再进入 `yudao-module-energy` 代码创建。
