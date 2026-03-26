-- Align DB constraint with current Role enum (RBAC v2).
-- Also migrate legacy ROLE_MANAGER/MANAGER -> ROLE_MANAGER_OFFICE.

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (
            role IN (
                'ROLE_ADMIN',
                'ROLE_MANAGER_OFFICE',
                'ROLE_MANAGER_DEPARTMENT',
                'ROLE_EMPLOYEE',
                'ROLE_MANAGER',
                'ADMIN',
                'MANAGER',
                'EMPLOYEE'
            )
        );

UPDATE users
SET role = 'ROLE_MANAGER_OFFICE'
WHERE role IN ('ROLE_MANAGER', 'MANAGER');

