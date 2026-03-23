-- Force recreate users_role_check so ROLE_MANAGER can have null department_id/position_id.
-- (Some environments may have different role string values from earlier migrations.)
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (
            (
                role IN ('ROLE_MANAGER', 'MANAGER')
                AND (
                    (department_id IS NULL AND position_id IS NULL)
                    OR
                    (department_id IS NOT NULL AND position_id IS NOT NULL)
                )
            )
            OR
            (
                role NOT IN ('ROLE_MANAGER', 'MANAGER')
                AND department_id IS NOT NULL
                AND position_id IS NOT NULL
            )
        );

