-- Most permissive rule:
-- - ROLE_MANAGER / MANAGER: allow any combination of department_id/position_id (including both NULL).
-- - Other roles: department_id and position_id must both be NOT NULL.
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (
            role IN ('ROLE_MANAGER', 'MANAGER')
            OR (
                role NOT IN ('ROLE_MANAGER', 'MANAGER')
                AND department_id IS NOT NULL
                AND position_id IS NOT NULL
            )
        );

