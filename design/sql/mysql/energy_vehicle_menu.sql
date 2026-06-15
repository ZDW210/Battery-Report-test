-- Vehicle binding menu for the mobile energy storage module.
-- Execute after the base energy menu SQL.

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
) VALUES (
  190010012000, '车辆绑定', 'energy:vehicle:query', 2, 56, 190010000000, 'vehicle', 'ep:van',
  'energy/vehicle/index', 'EnergyVehicle', 0, b'1', b'1', b'1', 'system', NOW(), 'system', NOW(), b'0'
) ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = VALUES(type),
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = VALUES(component),
  component_name = VALUES(component_name),
  status = VALUES(status),
  visible = VALUES(visible),
  keep_alive = VALUES(keep_alive),
  always_show = VALUES(always_show),
  updater = 'system',
  update_time = NOW(),
  deleted = b'0';

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
) VALUES
  (190010012001, '车辆新增', 'energy:vehicle:create', 3, 1, 190010012000, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'system', NOW(), 'system', NOW(), b'0'),
  (190010012002, '车辆修改', 'energy:vehicle:update', 3, 2, 190010012000, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'system', NOW(), 'system', NOW(), b'0'),
  (190010012003, '车辆删除', 'energy:vehicle:delete', 3, 3, 190010012000, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'system', NOW(), 'system', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = VALUES(type),
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  updater = 'system',
  update_time = NOW(),
  deleted = b'0';

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, m.id, 'system', NOW(), 'system', NOW(), b'0', 1
FROM system_menu m
WHERE m.id IN (190010012000, 190010012001, 190010012002, 190010012003)
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = 1
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
  );
