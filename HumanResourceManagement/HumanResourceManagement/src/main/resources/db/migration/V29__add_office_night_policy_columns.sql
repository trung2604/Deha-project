ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS night_start_time TIME;

ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS night_end_time TIME;

UPDATE offices
SET night_start_time = COALESCE(night_start_time, latest_checkout_time, TIME '22:00'),
    night_end_time = COALESCE(night_end_time, ((COALESCE(latest_checkout_time, TIME '22:00') + INTERVAL '8 hour')::time), TIME '06:00');

ALTER TABLE offices
    ALTER COLUMN night_start_time SET NOT NULL;

ALTER TABLE offices
    ALTER COLUMN night_end_time SET NOT NULL;

