-- 移动储能系统管理端菜单与权限
-- 注意：不同 RuoYi-Vue-Pro 版本 system_menu 字段可能略有差异，执行前请按实际版本核对。

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (190010000000, '移动储能', '', 1, 50, 0, '/energy', 'ep:lightning', NULL, NULL, 0, b'1', b'1', b'1', 'system', NOW(), 'system', NOW(), b'0'),

  (190010001000, '运营面板', 'energy:dashboard:query', 2, 1, 190010000000, 'dashboard', 'ep:data-analysis', 'energy/dashboard/index', 'EnergyDashboard', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010002000, '设备台账', 'energy:device:query', 2, 10, 190010000000, 'device', 'ep:monitor', 'energy/device/index', 'EnergyDevice', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010002001, '设备新增', 'energy:device:create', 3, 1, 190010002000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010002002, '设备修改', 'energy:device:update', 3, 2, 190010002000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010002003, '设备删除', 'energy:device:delete', 3, 3, 190010002000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010002004, '设备导出', 'energy:device:export', 3, 4, 190010002000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010003000, '实时监控', 'energy:telemetry:query', 2, 20, 190010000000, 'telemetry', 'ep:odometer', 'energy/telemetry/index', 'EnergyTelemetry', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010004000, '告警中心', 'energy:alarm:query', 2, 30, 190010000000, 'alarm', 'ep:warning', 'energy/alarm/index', 'EnergyAlarm', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010004001, '告警确认', 'energy:alarm:ack', 3, 1, 190010004000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010004002, '告警关闭', 'energy:alarm:close', 3, 2, 190010004000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010005000, '客户管理', 'energy:customer:query', 2, 40, 190010000000, 'customer', 'ep:user', 'energy/customer/index', 'EnergyCustomer', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010005001, '客户新增', 'energy:customer:create', 3, 1, 190010005000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010005002, '客户修改', 'energy:customer:update', 3, 2, 190010005000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010005003, '客户删除', 'energy:customer:delete', 3, 3, 190010005000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010006000, '项目场站', 'energy:project:query', 2, 50, 190010000000, 'project', 'ep:office-building', 'energy/project/index', 'EnergyProject', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010006001, '项目新增', 'energy:project:create', 3, 1, 190010006000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010006002, '项目修改', 'energy:project:update', 3, 2, 190010006000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010006003, '项目删除', 'energy:project:delete', 3, 3, 190010006000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010007000, '计费规则', 'energy:pricing-rule:query', 2, 60, 190010000000, 'pricing-rule', 'ep:money', 'energy/pricing-rule/index', 'EnergyPricingRule', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010007001, '计费规则新增', 'energy:pricing-rule:create', 3, 1, 190010007000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010007002, '计费规则修改', 'energy:pricing-rule:update', 3, 2, 190010007000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010007003, '计费规则删除', 'energy:pricing-rule:delete', 3, 3, 190010007000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010008000, '充放电记录', 'energy:charge-session:query', 2, 70, 190010000000, 'charge-session', 'ep:list', 'energy/charge-session/index', 'EnergyChargeSession', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010008001, '记录导出', 'energy:charge-session:export', 3, 1, 190010008000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010008002, '开始任务', 'energy:charge-session:start', 3, 2, 190010008000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010008003, '结束任务', 'energy:charge-session:stop', 3, 3, 190010008000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010008004, '会话结算', 'energy:charge-session:settle', 3, 4, 190010008000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010009000, '调度任务', 'energy:dispatch:query', 2, 80, 190010000000, 'dispatch', 'ep:operation', 'energy/dispatch/index', 'EnergyDispatch', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010009001, '调度新增', 'energy:dispatch:create', 3, 1, 190010009000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010009002, '调度取消', 'energy:dispatch:cancel', 3, 2, 190010009000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),

  (190010010000, 'EIOT 同步日志', 'energy:eiot-log:query', 2, 90, 190010000000, 'eiot-log', 'ep:connection', 'energy/eiot-log/index', 'EnergyEiotLog', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0');

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (190010011000, '用户授权', 'energy:user-scope:query', 2, 55, 190010000000, 'user-scope', 'ep:key', 'energy/userScope/index', 'EnergyUserScope', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010011001, '授权新增', 'energy:user-scope:create', 3, 1, 190010011000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010011002, '授权修改', 'energy:user-scope:update', 3, 2, 190010011000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010011003, '授权删除', 'energy:user-scope:delete', 3, 3, 190010011000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0');

INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (190010003001, '设备操控', 'energy:device:control', 3, 1, 190010003000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), permission = VALUES(permission), updater = VALUES(updater), update_time = NOW(), deleted = b'0';

-- 本地默认给超级管理员角色授权移动储能菜单。管理端仍会通过前端白名单隐藏未落地页面。
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, m.id, 'system', NOW(), 'system', NOW(), b'0', 1
FROM system_menu m
WHERE (m.id = 190010000000
    OR m.parent_id = 190010000000
    OR m.parent_id IN (SELECT id FROM system_menu WHERE parent_id = 190010000000))
  AND m.deleted = b'0'
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = 1
      AND rm.menu_id = m.id
      AND rm.tenant_id = 1
      AND rm.deleted = b'0'
  );
