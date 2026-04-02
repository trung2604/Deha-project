ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS ip_wifi VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_offices_ip_wifi ON offices(ip_wifi);

