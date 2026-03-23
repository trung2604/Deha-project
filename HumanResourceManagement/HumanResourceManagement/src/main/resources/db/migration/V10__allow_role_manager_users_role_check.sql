-- Ensure users.role constraint includes ROLE_MANAGER.
-- Some schemas may have been created before ROLE_MANAGER existed.
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (role IN ('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE'));

