-- Allow ROLE_MANAGER to be created/updated with null department_id/position_id.
-- Non-manager roles must always have both department_id and position_id.
-- For manager role, either (both null) or (both not null) is allowed.
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (
            (
                role = 'ROLE_MANAGER'
                AND (
                    (department_id IS NULL AND position_id IS NULL)
                    OR
                    (department_id IS NOT NULL AND position_id IS NOT NULL)
                )
            )
            OR
            (
                role <> 'ROLE_MANAGER'
                AND department_id IS NOT NULL
                AND position_id IS NOT NULL
            )
        );

