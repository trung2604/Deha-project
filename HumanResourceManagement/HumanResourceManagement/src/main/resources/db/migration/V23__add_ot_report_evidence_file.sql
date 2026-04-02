ALTER TABLE ot_reports
    ADD COLUMN IF NOT EXISTS evidence_file_name VARCHAR(255);

ALTER TABLE ot_reports
    ADD COLUMN IF NOT EXISTS evidence_file_content BYTEA;

