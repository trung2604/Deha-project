DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'offices' AND column_name = 'ot_latest_checkout_time'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'offices' AND column_name = 'latest_checkout_time'
    ) THEN
        ALTER TABLE offices RENAME COLUMN ot_latest_checkout_time TO latest_checkout_time;
    END IF;
END
$$;

ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS latest_checkout_time TIME;

UPDATE offices
SET latest_checkout_time = COALESCE(latest_checkout_time, TIME '22:00');

ALTER TABLE offices
    ALTER COLUMN latest_checkout_time SET NOT NULL;
