ALTER TABLE attendance_logs
    ADD COLUMN IF NOT EXISTS ot_type VARCHAR(32);

