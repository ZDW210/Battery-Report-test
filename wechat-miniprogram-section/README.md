# 微信小程序板块

本目录是移动储能项目内独立的小程序板块，来源于：

```text
D:\code\ankerui\energy-storage-platform\current\miniprogram
```

后续微信小程序客户侧功能优先在本目录内开发和验证，避免直接修改原始 `current/miniprogram/src` 源文件。

## 当前定位

- 客户和司机侧只保留微信登录，不展示账号密码联调入口。
- 首次微信登录后，后端自动创建 App 用户；管理端在“用户授权”中给该用户开放客户、项目或设备权限。
- 小程序不展示客户范围、权限范围、客户管理、计费规则维护或设备基础资料编辑入口。
- 设备列表、设备详情、历史数据、报警信息和扫码启动均以当前登录 App 用户的后端授权范围为准。
- 扫码启动以设备、充电桩或仪表二维码为主，司机身份来自当前微信登录账户；未知账户或未授权账户不得开始放电。

## 微信开发者工具打开路径

```text
D:\code\ankerui\energy-storage-platform\current\wechat-miniprogram-section\dist\build\mp-weixin
```

## 本地构建

当前目录的 `node_modules` 是指向原小程序依赖目录的本地联接：

```text
D:\code\ankerui\energy-storage-platform\current\miniprogram\node_modules
```

构建命令：

```powershell
npm run build:mp-weixin
```

## 本地登录配置

后端小程序登录需要配置微信小程序密钥。`application-local.yaml` 不直接保存 AppSecret，本机启动后端前需要设置：

```powershell
$env:ENERGY_WECHAT_APP_ID="wxf43120ae9b42d6dd"
$env:ENERGY_WECHAT_APP_SECRET="<小程序 AppSecret>"
```

## 后续开发原则

- 新功能只在 `current/wechat-miniprogram-section` 内开发。
- 原始 `current/miniprogram` 暂作参考副本，不直接修改。
- 客户侧页面只做业务查看和扫码启动，后台维护能力留在管理端。
- 前端只负责发起请求和展示结果，授权、账户识别、设备范围和放电校验必须由后端再次判断。
