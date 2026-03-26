ALTER TABLE attendance_logs
    ADD COLUMN IF NOT EXISTS checkout_source VARCHAR(16);

ALTER TABLE attendance_logs
    ADD COLUMN IF NOT EXISTS auto_checked_out BOOLEAN;

UPDATE attendance_logs
SET checkout_source = COALESCE(checkout_source, 'MANUAL'),
    auto_checked_out = COALESCE(auto_checked_out, FALSE);

ALTER TABLE attendance_logs
    ALTER COLUMN checkout_source SET NOT NULL;

ALTER TABLE attendance_logs
    ALTER COLUMN auto_checked_out SET NOT NULL;

