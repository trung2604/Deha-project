CREATE TABLE IF NOT EXISTS offices (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(255)
);

ALTER TABLE departments
    ADD COLUMN IF NOT EXISTS office_id UUID;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS office_id UUID;

ALTER TABLE departments
    ADD CONSTRAINT fk_departments_office
        FOREIGN KEY (office_id) REFERENCES offices(id);

ALTER TABLE users
    ADD CONSTRAINT fk_users_office
        FOREIGN KEY (office_id) REFERENCES offices(id);

CREATE INDEX IF NOT EXISTS idx_departments_office_id ON departments(office_id);
CREATE INDEX IF NOT EXISTS idx_users_office_id ON users(office_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_departments_office_name ON departments(office_id, lower(name));
