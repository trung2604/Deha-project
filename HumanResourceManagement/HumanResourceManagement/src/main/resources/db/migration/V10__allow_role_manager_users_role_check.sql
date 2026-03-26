-- Compatibility-first constraint for legacy schemas.
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

