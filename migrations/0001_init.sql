CREATE TABLE IF NOT EXISTS system_user (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  nickname TEXT,
  mobile TEXT,
  status INTEGER NOT NULL DEFAULT 0,
  user_type INTEGER NOT NULL DEFAULT 2,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS system_session (
  access_token TEXT PRIMARY KEY,
  refresh_token TEXT NOT NULL UNIQUE,
  user_id INTEGER NOT NULL,
  expires_time INTEGER NOT NULL,
  create_time INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS energy_customer (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  contact_name TEXT,
  contact_mobile TEXT,
  region TEXT,
  status INTEGER NOT NULL DEFAULT 0,
  remark TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_project (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  customer_id INTEGER,
  name TEXT NOT NULL,
  code TEXT,
  address TEXT,
  latitude REAL,
  longitude REAL,
  status INTEGER NOT NULL DEFAULT 0,
  remark TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_device (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  device_no TEXT,
  device_name TEXT,
  device_type INTEGER,
  gateway_sn TEXT,
  meter_sn TEXT,
  meter_no TEXT UNIQUE,
  customer_id INTEGER,
  project_id INTEGER,
  status INTEGER NOT NULL DEFAULT 0,
  run_mode INTEGER,
  latitude REAL,
  longitude REAL,
  last_soc REAL,
  last_soh REAL,
  last_power REAL,
  last_voltage REAL,
  last_current REAL,
  last_temp REAL,
  last_reading_time TEXT,
  remark TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_customer_account (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  customer_id INTEGER,
  system_user_id INTEGER NOT NULL UNIQUE,
  username TEXT NOT NULL UNIQUE,
  nickname TEXT,
  mobile TEXT,
  status INTEGER NOT NULL DEFAULT 0,
  remark TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_customer_account_menu (
  account_id INTEGER NOT NULL,
  menu_id INTEGER NOT NULL,
  PRIMARY KEY (account_id, menu_id)
);

CREATE TABLE IF NOT EXISTS energy_app_user (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT,
  openid TEXT UNIQUE,
  nickname TEXT,
  mobile TEXT,
  card_no TEXT,
  default_role TEXT NOT NULL DEFAULT 'DRIVER',
  mini_admin_enabled INTEGER NOT NULL DEFAULT 0,
  management_enabled INTEGER NOT NULL DEFAULT 0,
  status INTEGER NOT NULL DEFAULT 0,
  login_ip TEXT,
  login_date TEXT,
  remark TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_vehicle (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  vehicle_no TEXT,
  plate_no TEXT NOT NULL UNIQUE,
  qr_code TEXT,
  device_id INTEGER,
  customer_id INTEGER,
  project_id INTEGER,
  app_user_id INTEGER,
  card_no TEXT,
  status INTEGER NOT NULL DEFAULT 0,
  remark TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_account_event (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  event_scene TEXT,
  auth_type TEXT,
  scan_text TEXT,
  card_no TEXT,
  account_known INTEGER NOT NULL DEFAULT 0,
  account_id INTEGER,
  account_name TEXT,
  account_mobile TEXT,
  device_id INTEGER,
  customer_id INTEGER,
  project_id INTEGER,
  result_message TEXT,
  create_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_user_scope (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER,
  user_type INTEGER,
  customer_id INTEGER,
  project_id INTEGER,
  device_id INTEGER,
  status INTEGER NOT NULL DEFAULT 0,
  remark TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_pricing_rule (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  customer_id INTEGER,
  project_id INTEGER,
  device_id INTEGER,
  electricity_category TEXT NOT NULL DEFAULT 'general_commercial',
  pricing_mode TEXT NOT NULL DEFAULT 'two_part',
  voltage_level TEXT NOT NULL DEFAULT 'under_1kv',
  agent_purchase_price REAL NOT NULL DEFAULT 0,
  line_loss_price REAL NOT NULL DEFAULT 0,
  transmission_distribution_price REAL NOT NULL DEFAULT 0,
  system_operation_fee REAL NOT NULL DEFAULT 0,
  government_fund_surcharge REAL NOT NULL DEFAULT 0,
  sharp_peak_rate REAL NOT NULL DEFAULT 0,
  peak_rate REAL NOT NULL DEFAULT 0,
  flat_rate REAL NOT NULL DEFAULT 0,
  valley_rate REAL NOT NULL DEFAULT 0,
  deep_valley_rate REAL NOT NULL DEFAULT 0,
  tou_periods TEXT NOT NULL DEFAULT '[]',
  fee_config_json TEXT NOT NULL DEFAULT '[]',
  service_markup_percent REAL NOT NULL DEFAULT 0,
  capacity_billing_mode TEXT NOT NULL DEFAULT 'none',
  max_demand_price REAL NOT NULL DEFAULT 0,
  transformer_capacity_kva REAL NOT NULL DEFAULT 0,
  transformer_capacity_price REAL NOT NULL DEFAULT 0,
  time_rate REAL,
  energy_rate REAL,
  site_fee REAL NOT NULL DEFAULT 0,
  maintenance_fee REAL NOT NULL DEFAULT 0,
  communication_fee REAL NOT NULL DEFAULT 0,
  platform_service_fee REAL NOT NULL DEFAULT 0,
  battery_depreciation_cost REAL NOT NULL DEFAULT 0,
  other_fixed_fee REAL NOT NULL DEFAULT 0,
  guarantee_energy REAL NOT NULL DEFAULT 2500,
  diesel_generation_rate REAL NOT NULL DEFAULT 2,
  grid_estimate_base_rate REAL NOT NULL DEFAULT 1.5,
  grid_estimate_extra_rate REAL NOT NULL DEFAULT 0.18,
  effective_start TEXT,
  effective_end TEXT,
  status INTEGER NOT NULL DEFAULT 0,
  remark TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_charge_session (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  session_no TEXT UNIQUE,
  device_id INTEGER,
  customer_id INTEGER,
  pricing_rule_id INTEGER,
  operator_user_id INTEGER,
  session_type INTEGER,
  start_time TEXT,
  end_time TEXT,
  start_energy REAL,
  end_energy REAL,
  total_energy REAL,
  duration_minutes INTEGER,
  energy_fee REAL,
  time_fee REAL,
  total_fee REAL,
  status INTEGER NOT NULL DEFAULT 0,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_eiot_sync_log (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  sync_type TEXT,
  request_id TEXT,
  gateway_sn TEXT,
  meter_sn TEXT,
  payload_url TEXT,
  status INTEGER NOT NULL DEFAULT 0,
  error_msg TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_telemetry (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  device_id INTEGER,
  gateway_sn TEXT,
  meter_sn TEXT,
  meter_no TEXT,
  collect_time TEXT,
  timestamp INTEGER,
  source TEXT,
  state TEXT,
  pa REAL,
  pb REAL,
  pc REAL,
  ua REAL,
  ub REAL,
  uc REAL,
  ia REAL,
  ib REAL,
  ic REAL,
  p REAL,
  pf REAL,
  epi REAL,
  create_time TEXT,
  update_time TEXT
);

CREATE INDEX IF NOT EXISTS idx_energy_telemetry_device_time ON energy_telemetry(device_id, collect_time);
CREATE INDEX IF NOT EXISTS idx_energy_telemetry_meter_time ON energy_telemetry(meter_no, collect_time);
CREATE INDEX IF NOT EXISTS idx_energy_device_status_reading ON energy_device(status, last_reading_time);
CREATE INDEX IF NOT EXISTS idx_energy_charge_session_operator_status ON energy_charge_session(operator_user_id, status, start_time);

CREATE TABLE IF NOT EXISTS energy_alarm (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  alarm_no TEXT UNIQUE,
  device_id INTEGER,
  code TEXT,
  level INTEGER,
  title TEXT,
  content TEXT,
  status INTEGER NOT NULL DEFAULT 0,
  occur_time TEXT,
  ack_user_id INTEGER,
  ack_time TEXT,
  close_time TEXT,
  create_time TEXT,
  update_time TEXT
);

CREATE TABLE IF NOT EXISTS energy_file (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT,
  r2_key TEXT NOT NULL,
  url TEXT,
  content_type TEXT,
  size INTEGER,
  create_time TEXT
);

INSERT OR IGNORE INTO system_user(id, username, password_hash, nickname, mobile, status, user_type, create_time)
VALUES (1, 'admin', 'sha256:240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '平台管理员', '', 0, 2, datetime('now'));

INSERT OR IGNORE INTO energy_customer(id, name, contact_name, contact_mobile, region, status, remark, create_time)
VALUES (1, '示例车队客户', '客户负责人', '13800000000', '杭州', 0, 'D1 初始化示例数据，可在管理端修改或删除', datetime('now'));

INSERT OR IGNORE INTO energy_project(id, customer_id, name, code, address, latitude, longitude, status, create_time)
VALUES (1, 1, '移动储能示范项目', 'DEMO-001', '示例园区', 30.2741, 120.1551, 0, datetime('now'));

INSERT OR IGNORE INTO energy_device(id, device_no, device_name, device_type, gateway_sn, meter_sn, meter_no, customer_id, project_id, status, run_mode, last_soc, last_power, last_voltage, last_current, last_reading_time, create_time)
VALUES (1, 'DEV-001', '示例移动储能设备', 1, '12207013690004', '12207013690004', '12207013690004_12207013690004', 1, 1, 0, 1, 82.5, 12.6, 380.2, 31.4, datetime('now'), datetime('now'));

INSERT OR IGNORE INTO energy_telemetry(device_id, gateway_sn, meter_sn, meter_no, collect_time, timestamp, source, state, pa, pb, pc, ua, ub, uc, ia, ib, ic, p, pf, epi, create_time)
VALUES
(1, '12207013690004', '12207013690004', '12207013690004_12207013690004', datetime('now', '-25 minutes'), unixepoch('now', '-25 minutes'), 'REALTIME', 'ONLINE', 3.2, 3.1, 3.4, 220.1, 219.8, 220.3, 14.2, 14.0, 14.6, 9.7, 0.96, 120.1, datetime('now')),
(1, '12207013690004', '12207013690004', '12207013690004_12207013690004', datetime('now', '-20 minutes'), unixepoch('now', '-20 minutes'), 'REALTIME', 'ONLINE', 3.6, 3.2, 3.5, 220.3, 219.9, 220.1, 14.8, 14.2, 14.9, 10.3, 0.95, 120.9, datetime('now')),
(1, '12207013690004', '12207013690004', '12207013690004_12207013690004', datetime('now', '-15 minutes'), unixepoch('now', '-15 minutes'), 'REALTIME', 'ONLINE', 4.1, 3.8, 3.9, 220.4, 220.0, 220.2, 15.2, 14.9, 15.1, 11.8, 0.94, 121.8, datetime('now')),
(1, '12207013690004', '12207013690004', '12207013690004_12207013690004', datetime('now', '-10 minutes'), unixepoch('now', '-10 minutes'), 'REALTIME', 'ONLINE', 4.5, 4.1, 4.0, 220.2, 219.7, 220.0, 15.7, 15.3, 15.4, 12.6, 0.95, 122.7, datetime('now')),
(1, '12207013690004', '12207013690004', '12207013690004_12207013690004', datetime('now', '-5 minutes'), unixepoch('now', '-5 minutes'), 'REALTIME', 'ONLINE', 4.0, 3.9, 3.8, 220.0, 219.6, 219.9, 15.0, 14.8, 15.0, 11.7, 0.96, 123.4, datetime('now'));

INSERT OR IGNORE INTO energy_alarm(alarm_no, device_id, code, level, title, content, status, occur_time, create_time)
VALUES ('DEMO-ALARM-001', 1, 'PFLOW1', 1, '总功率因数低限预警', '总功率因数低限预警,设定值：0.9，当前值：0.791', 0, datetime('now', '-1 hour'), datetime('now'));

INSERT OR IGNORE INTO energy_app_user(id, username, openid, nickname, mobile, card_no, default_role, mini_admin_enabled, management_enabled, status, create_time)
VALUES (1, 'driver_demo', 'demo-openid', '示例司机', '13900000000', 'CARD-DEMO-001', 'DRIVER', 0, 0, 0, datetime('now'));

INSERT OR IGNORE INTO energy_vehicle(id, vehicle_no, plate_no, qr_code, device_id, customer_id, project_id, app_user_id, card_no, status, remark, create_time)
VALUES (1, 'VH-001', '浙A-DEMO1', 'vehicle:1', 1, 1, 1, 1, 'CARD-DEMO-001', 0, '扫码或刷卡校验示例车辆', datetime('now'));

INSERT OR IGNORE INTO energy_pricing_rule(id, customer_id, project_id, device_id, time_rate, energy_rate, effective_start, effective_end, status, remark, create_time)
VALUES (1, 1, 1, 1, 1.20, 0.85, datetime('now', '-1 day'), datetime('now', '+365 day'), 0, '示例计费规则', datetime('now'));

INSERT OR IGNORE INTO energy_charge_session(id, session_no, device_id, customer_id, pricing_rule_id, session_type, start_time, end_time, start_energy, end_energy, total_energy, duration_minutes, energy_fee, time_fee, total_fee, status, create_time)
VALUES (1, 'CS-DEMO-001', 1, 1, 1, 1, datetime('now', '-2 hour'), datetime('now', '-1 hour'), 120.1, 132.6, 12.5, 60, 10.63, 72.00, 82.63, 3, datetime('now'));

INSERT OR IGNORE INTO energy_account_event(id, event_scene, auth_type, scan_text, card_no, account_known, account_id, account_name, account_mobile, device_id, customer_id, project_id, result_message, create_time)
VALUES (1, 'PILE_AUTH', 'WECHAT_SCAN', 'vehicle:1', null, 1, 1, '示例司机', '13900000000', 1, 1, 1, '账户已识别，可按小程序操作放电', datetime('now', '-30 minutes'));

INSERT OR IGNORE INTO energy_user_scope(id, user_id, user_type, customer_id, project_id, device_id, status, remark, create_time)
VALUES (1, 1, 2, 1, 1, 1, 0, '示例用户授权范围', datetime('now'));

INSERT OR IGNORE INTO energy_eiot_sync_log(id, sync_type, request_id, gateway_sn, meter_sn, payload_url, status, error_msg, create_time)
VALUES (1, 'REALTIME', 'REQ-DEMO-001', '12207013690004', '12207013690004', 'r2://eiot/raw/demo.json', 0, '', datetime('now'));
