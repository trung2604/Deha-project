ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS shift_end_time TIME;

ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS standard_work_hours INT;

ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS ot_min_hours INT;

-- Backfill defaults for existing rows
UPDATE offices
SET shift_end_time = COALESCE(shift_end_time, TIME '18:00'),
    standard_work_hours = COALESCE(standard_work_hours, 9),
    ot_min_hours = COALESCE(ot_min_hours, 1);

-- If legacy column exists, migrate data to shift_end_time
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'offices' AND column_name = 'shift_start_time'
    ) THEN
        UPDATE offices
        SET shift_end_time = COALESCE(shift_end_time, shift_start_time);
    END IF;
END
$$;

