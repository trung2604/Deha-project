-- In case earlier migrations were not applied, ensure office_id columns exist.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS office_id UUID;

ALTER TABLE departments
    ADD COLUMN IF NOT EXISTS office_id UUID;

CREATE INDEX IF NOT EXISTS idx_users_office_id_ensure ON users(office_id);
CREATE INDEX IF NOT EXISTS idx_departments_office_id_ensure ON departments(office_id);

