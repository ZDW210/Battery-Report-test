ALTER TABLE energy_pricing_rule ADD COLUMN fee_config_json TEXT NOT NULL DEFAULT '[]';
ALTER TABLE energy_pricing_rule ADD COLUMN service_markup_percent REAL NOT NULL DEFAULT 0;
