-- Migrate legacy manager role to new "office manager" role.
-- This keeps existing data working after we switch Role enum values.
UPDATE users
SET role = 'ROLE_MANAGER_OFFICE'
WHERE role IN ('ROLE_MANAGER', 'MANAGER');

-- Update the check constraint so it accepts the new enum values.
-- Keep legacy short forms for compatibility with older seed data.
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
                'ADMIN',
                'MANAGER',
                'EMPLOYEE'
            )
        );

