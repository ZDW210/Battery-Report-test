-- Account scan/card event table and admin menu.

CREATE TABLE IF NOT EXISTS energy_account_event (
  id BIGINT NOT NULL COMMENT 'Account event id',
  event_scene VARCHAR(32) NOT NULL COMMENT 'Event scene: VERIFY/DISCHARGE',
  auth_type VARCHAR(16) NOT NULL COMMENT 'Auth type: WECHAT/CARD',
  scan_text VARCHAR(512) NULL COMMENT 'Scanned device QR content',
  card_no VARCHAR(64) NULL COMMENT 'Card number',
  account_known BIT(1) NOT NULL DEFAULT b'0' COMMENT 'Whether account is known and authorized',
  account_id BIGINT NULL COMMENT 'App user id',
  account_name VARCHAR(64) NULL COMMENT 'App user name',
  account_mobile VARCHAR(32) NULL COMMENT 'App user mobile',
  device_id BIGINT NULL COMMENT 'Device id',
  device_no VARCHAR(64) NULL COMMENT 'Device number',
  device_name VARCHAR(128) NULL COMMENT 'Device name',
  meter_no VARCHAR(128) NULL COMMENT 'Meter number',
  gateway_sn VARCHAR(128) NULL COMMENT 'Gateway serial number',
  meter_sn VARCHAR(128) NULL COMMENT 'Meter serial number',
  customer_id BIGINT NULL COMMENT 'Customer id',
  project_id BIGINT NULL COMMENT 'Project id',
  result_message VARCHAR(256) NULL COMMENT 'Result message',
  creator VARCHAR(64) NULL DEFAULT '' COMMENT 'Creator',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  updater VARCHAR(64) NULL DEFAULT '' COMMENT 'Updater',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (id),
  KEY idx_energy_account_event_device (device_id),
  KEY idx_energy_account_event_account (account_id),
  KEY idx_energy_account_event_auth (auth_type, account_known),
  KEY idx_energy_account_event_create_time (create_time),
  KEY idx_energy_account_event_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Energy account scan/card event';

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
) VALUES (
  190010014000, '扫码刷卡记录', 'energy:account-event:query', 2, 55, 190010000000, 'account-event', 'ep:tickets',
  'energy/account-event/index', 'EnergyAccountEvent', 0, b'1', b'1', b'1', 'system', NOW(), 'system', NOW(), b'0'
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

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, m.id, 'system', NOW(), 'system', NOW(), b'0', 1
FROM system_menu m
WHERE m.id = 190010014000
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = 1
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
  );
