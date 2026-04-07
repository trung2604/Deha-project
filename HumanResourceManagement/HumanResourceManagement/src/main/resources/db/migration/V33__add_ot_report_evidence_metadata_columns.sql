ALTER TABLE ot_reports
    ADD COLUMN IF NOT EXISTS evidence_file_url VARCHAR(1000);

ALTER TABLE ot_reports
    ADD COLUMN IF NOT EXISTS evidence_file_public_id VARCHAR(255);

ALTER TABLE ot_reports
    ADD COLUMN IF NOT EXISTS evidence_file_mime_type VARCHAR(128);

ALTER TABLE ot_reports
    ADD COLUMN IF NOT EXISTS evidence_file_size_bytes BIGINT;
