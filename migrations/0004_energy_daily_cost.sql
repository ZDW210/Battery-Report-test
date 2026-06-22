CREATE TABLE IF NOT EXISTS energy_daily_cost (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  stat_date TEXT NOT NULL,
  device_id INTEGER NOT NULL,
  customer_id INTEGER,
  project_id INTEGER,
  device_name TEXT,
  device_no TEXT,
  meter_no TEXT,
  total_charge_energy REAL NOT NULL DEFAULT 0,
  total_discharge_energy REAL NOT NULL DEFAULT 0,
  charge_sharp_peak REAL NOT NULL DEFAULT 0,
  charge_peak REAL NOT NULL DEFAULT 0,
  charge_flat REAL NOT NULL DEFAULT 0,
  charge_valley REAL NOT NULL DEFAULT 0,
  charge_deep_valley REAL NOT NULL DEFAULT 0,
  discharge_sharp_peak REAL NOT NULL DEFAULT 0,
  discharge_peak REAL NOT NULL DEFAULT 0,
  discharge_flat REAL NOT NULL DEFAULT 0,
  discharge_valley REAL NOT NULL DEFAULT 0,
  discharge_deep_valley REAL NOT NULL DEFAULT 0,
  charge_cost REAL,
  sales_revenue REAL NOT NULL DEFAULT 0,
  saved_cost REAL,
  pricing_rule_id INTEGER,
  unmatched_pricing INTEGER NOT NULL DEFAULT 0,
  update_time TEXT,
  UNIQUE(device_id, stat_date)
);

CREATE INDEX IF NOT EXISTS idx_energy_daily_cost_date ON energy_daily_cost(stat_date);
CREATE INDEX IF NOT EXISTS idx_energy_daily_cost_device_date ON energy_daily_cost(device_id, stat_date);
CREATE INDEX IF NOT EXISTS idx_energy_daily_cost_project_date ON energy_daily_cost(project_id, stat_date);
CREATE INDEX IF NOT EXISTS idx_energy_daily_cost_customer_date ON energy_daily_cost(customer_id, stat_date);
