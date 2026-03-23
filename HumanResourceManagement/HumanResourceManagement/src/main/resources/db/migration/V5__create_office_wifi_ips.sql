CREATE TABLE IF NOT EXISTS office_wifi_ips (
    id UUID PRIMARY KEY,
    office_id UUID NOT NULL REFERENCES offices(id) ON DELETE CASCADE,
    ip_wifi VARCHAR(64) NOT NULL,
    CONSTRAINT uq_office_wifi_ips UNIQUE (office_id, ip_wifi)
);

CREATE INDEX IF NOT EXISTS idx_office_wifi_ips_office_id ON office_wifi_ips(office_id);

