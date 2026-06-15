-- 客户老板账号权限菜单。

CREATE TABLE IF NOT EXISTS energy_customer_account (
  id BIGINT NOT NULL COMMENT '客户老板账号编号',
  customer_id BIGINT NOT NULL COMMENT '客户编号',
  system_user_id BIGINT NOT NULL COMMENT '后台登录用户编号',
  role_id BIGINT NOT NULL COMMENT '专属角色编号',
  username VARCHAR(30) NOT NULL COMMENT '登录账号',
  nickname VARCHAR(64) NOT NULL COMMENT '账号名称',
  mobile VARCHAR(32) NULL COMMENT '手机号',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0 启用，1 停用',
  menu_ids VARCHAR(1024) NULL COMMENT '开放板块菜单编号，逗号分隔',
  remark VARCHAR(512) NULL COMMENT '备注',
  creator VARCHAR(64) NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted BIT(1) NOT NULL DEFAULT b'0',
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_energy_customer_account_username (username, tenant_id, deleted),
  KEY idx_energy_customer_account_customer_id (customer_id),
  KEY idx_energy_customer_account_system_user_id (system_user_id),
  KEY idx_energy_customer_account_role_id (role_id),
  KEY idx_energy_customer_account_status (status),
  KEY idx_energy_customer_account_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动储能客户老板网页账号';

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
) VALUES
  (190010015000, '客户账号权限', 'energy:customer-account:query', 2, 56, 190010000000, 'customer-account', 'ep:avatar', 'energy/customerAccount/index', 'EnergyCustomerAccount', 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0')
ON DUPLICATE KEY UPDATE
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
  updater = VALUES(updater),
  update_time = VALUES(update_time),
  deleted = VALUES(deleted);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
) VALUES
  (190010015001, '客户账号新增', 'energy:customer-account:create', 3, 1, 190010015000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010015002, '客户账号修改', 'energy:customer-account:update', 3, 2, 190010015000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0'),
  (190010015003, '客户账号重置密码', 'energy:customer-account:reset-password', 3, 3, 190010015000, '', '', NULL, NULL, 0, b'1', b'1', b'0', 'system', NOW(), 'system', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = VALUES(type),
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  updater = VALUES(updater),
  update_time = VALUES(update_time),
  deleted = VALUES(deleted);

INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id, m.id, 'system', NOW(), 'system', NOW(), b'0'
FROM system_role r
JOIN system_menu m
WHERE r.code = 'super_admin'
  AND m.id IN (190010015000, 190010015001, 190010015002, 190010015003);
