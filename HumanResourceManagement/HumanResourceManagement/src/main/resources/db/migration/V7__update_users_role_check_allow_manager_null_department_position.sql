-- Compatibility-first constraint for existing databases with legacy data.
-- Keep DB-level role domain validation only; business rules are handled in app layer.
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (
            role IN (
                'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE',
                'ADMIN', 'MANAGER', 'EMPLOYEE'
            )
        );

