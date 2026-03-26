ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS ot_weekday_multiplier DOUBLE PRECISION;

ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS ot_weekend_multiplier DOUBLE PRECISION;

ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS ot_holiday_multiplier DOUBLE PRECISION;

ALTER TABLE offices
    ADD COLUMN IF NOT EXISTS ot_night_bonus_multiplier DOUBLE PRECISION;

UPDATE offices
SET ot_weekday_multiplier = COALESCE(ot_weekday_multiplier, 1.5),
    ot_weekend_multiplier = COALESCE(ot_weekend_multiplier, 2.0),
    ot_holiday_multiplier = COALESCE(ot_holiday_multiplier, 3.0),
    ot_night_bonus_multiplier = COALESCE(ot_night_bonus_multiplier, 0.3);

ALTER TABLE offices
    ALTER COLUMN ot_weekday_multiplier SET NOT NULL;

ALTER TABLE offices
    ALTER COLUMN ot_weekend_multiplier SET NOT NULL;

ALTER TABLE offices
    ALTER COLUMN ot_holiday_multiplier SET NOT NULL;

ALTER TABLE offices
    ALTER COLUMN ot_night_bonus_multiplier SET NOT NULL;

