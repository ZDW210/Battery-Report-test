# 2026-06-09 车辆绑定管理变更记录

## 变更内容

- 新增管理端“移动储能 / 车辆绑定”页面。
- 新增管理端车辆绑定 CRUD 接口。
- 新增车辆绑定菜单和权限 SQL。
- 前端能源菜单白名单加入 `vehicle`，确保“车辆绑定”不会被项目菜单过滤逻辑隐藏。
- 车辆绑定保存时由后端根据绑定设备自动同步客户和项目。
- 车辆编号和二维码内容唯一校验落地。

## 影响范围

- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/controller/admin/vehicle`
- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/service/vehicle`
- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/dal/mysql/vehicle/EnergyVehicleMapper.java`
- `backend-ruoyi/yudao-module-energy/src/main/java/cn/iocoder/yudao/module/energy/enums/ErrorCodeConstants.java`
- `admin-vue3/src/api/energy/vehicle/index.ts`
- `admin-vue3/src/store/modules/permission.ts`
- `admin-vue3/src/views/energy/vehicle`
- `design/14-vehicle-binding-admin.md`
- `design-standards/07-vehicle-binding-standards.md`
- `design/sql/mysql/energy_vehicle_menu.sql`

## 验证记录

- 后端模块编译：`mvn -pl yudao-module-energy -am -DskipTests compile` 通过。
- 前端新增文件 ESLint：`pnpm exec eslint src/api/energy/vehicle/index.ts src/views/energy/vehicle/index.vue src/views/energy/vehicle/VehicleForm.vue` 通过。
