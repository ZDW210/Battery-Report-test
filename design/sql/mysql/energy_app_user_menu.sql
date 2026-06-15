-- App user account menu and card number support.

ALTER TABLE energy_app_user
  ADD COLUMN card_no VARCHAR(64) NULL COMMENT '刷卡卡号' AFTER mobile;

CREATE UNIQUE INDEX uk_energy_app_user_card_no ON energy_app_user (card_no, deleted);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
) VALUES (
  190010013000, '使用账户', 'energy:app-user:query', 2, 54, 190010000000, 'app-user', 'ep:user',
  'energy/appUser/index', 'EnergyAppUser', 0, b'1', b'1', b'1', 'system', NOW(), 'system', NOW(), b'0'
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
) VALUES (
  190010013001, '账户修改', 'energy:app-user:update', 3, 1, 190010013000, '', '', NULL, NULL,
  0, b'1', b'1', b'1', 'system', NOW(), 'system', NOW(), b'0'
) ON DUPLICATE KEY UPDATE
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
WHERE m.id IN (190010013000, 190010013001)
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = 1
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
  );
