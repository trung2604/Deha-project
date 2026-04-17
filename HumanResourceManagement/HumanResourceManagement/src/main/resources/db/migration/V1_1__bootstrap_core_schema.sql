-- Bootstrap core legacy tables so full Flyway chain works on an empty database.
-- This migration is intentionally minimal and idempotent.

CREATE TABLE IF NOT EXISTS offices (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS positions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department_id UUID NOT NULL REFERENCES departments(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    department_id UUID REFERENCES departments(id) ON DELETE SET NULL,
    position_id UUID REFERENCES positions(id) ON DELETE SET NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check CHECK (
        role IN (
            'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE',
            'ADMIN', 'MANAGER', 'EMPLOYEE',
            'MANAGER_OFFICE', 'MANAGER_DEPARTMENT'
        )
    )
);

CREATE INDEX IF NOT EXISTS idx_users_department_id_bootstrap ON users(department_id);
CREATE INDEX IF NOT EXISTS idx_users_position_id_bootstrap ON users(position_id);
CREATE INDEX IF NOT EXISTS idx_positions_department_id_bootstrap ON positions(department_id);

