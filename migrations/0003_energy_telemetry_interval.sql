CREATE TABLE IF NOT EXISTS energy_telemetry_interval (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  telemetry_id INTEGER UNIQUE,
  device_id INTEGER,
  gateway_sn TEXT,
  meter_sn TEXT,
  meter_no TEXT,
  start_time TEXT,
  end_time TEXT,
  duration_minutes REAL,
  charge_total REAL NOT NULL DEFAULT 0,
  charge_sharp_peak REAL NOT NULL DEFAULT 0,
  charge_peak REAL NOT NULL DEFAULT 0,
  charge_flat REAL NOT NULL DEFAULT 0,
  charge_valley REAL NOT NULL DEFAULT 0,
  charge_deep_valley REAL NOT NULL DEFAULT 0,
  discharge_total REAL NOT NULL DEFAULT 0,
  discharge_sharp_peak REAL NOT NULL DEFAULT 0,
  discharge_peak REAL NOT NULL DEFAULT 0,
  discharge_flat REAL NOT NULL DEFAULT 0,
  discharge_valley REAL NOT NULL DEFAULT 0,
  discharge_deep_valley REAL NOT NULL DEFAULT 0,
  calc_method TEXT,
  consistency_status TEXT,
  create_time TEXT
);

CREATE INDEX IF NOT EXISTS idx_energy_interval_device_time ON energy_telemetry_interval(device_id, end_time);
CREATE INDEX IF NOT EXISTS idx_energy_interval_meter_time ON energy_telemetry_interval(meter_no, end_time);
