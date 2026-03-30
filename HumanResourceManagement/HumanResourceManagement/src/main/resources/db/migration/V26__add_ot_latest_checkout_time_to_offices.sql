ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS ot_latest_checkout_time TIME;

UPDATE offices
SET ot_latest_checkout_time = COALESCE(ot_latest_checkout_time, TIME '22:00');

ALTER TABLE offices
    ALTER COLUMN ot_latest_checkout_time SET NOT NULL;
