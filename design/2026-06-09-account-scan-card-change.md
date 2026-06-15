# 2026-06-09 扫码/刷卡账户识别变更记录

## 变更内容

- 将小程序扫码语义从“扫码车辆”调整为“扫码设备/充电桩并识别账户”。
- 后端扫码校验返回账户识别结果：`accountKnown`、`accountType`、`accountId`、`accountName`、`accountMobile`、`cardNo`、`message`。
- `energy_app_user` 增加 `card_no` 字段，用于后续刷卡识别账户。
- 管理端新增“使用账户”页面，自动显示微信小程序登录生成的账户，可维护手机号、昵称、卡号和启停状态。
- 小程序首页按钮改为“扫码启动”，校验失败时显示“未知账户”并阻止放电。

## 影响范围

- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/auth`
- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/app/session`
- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/app`
- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/dataobject/auth/EnergyAppUserDO.java`
- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/mysql/auth/EnergyAppUserMapper.java`
- `admin-vue3/src/api/energy/appUser/index.ts`
- `admin-vue3/src/views/energy/appUser`
- `wechat-miniprogram-section/src/api/customers.ts`
- `wechat-miniprogram-section/src/pages/index/index.vue`
- `design/15-account-scan-card-flow.md`
- `design-standards/08-account-identification-standards.md`
