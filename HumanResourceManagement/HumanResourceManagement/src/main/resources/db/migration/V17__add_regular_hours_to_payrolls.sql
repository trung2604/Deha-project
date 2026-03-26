ALTER TABLE payrolls
    ADD COLUMN IF NOT EXISTS regular_hours INT;

UPDATE payrolls
SET regular_hours = COALESCE(regular_hours, 0);

ALTER TABLE payrolls
    ALTER COLUMN regular_hours SET NOT NULL;

