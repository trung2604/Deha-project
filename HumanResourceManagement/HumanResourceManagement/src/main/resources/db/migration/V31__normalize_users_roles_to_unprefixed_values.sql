-- 1. Drop constraint cũ
ALTER TABLE users DROP CONSTRAINT users_role_check;

-- 2. Normalize dữ liệu trước
UPDATE users
SET role = UPPER(TRIM(role));

-- bỏ ROLE_
UPDATE users
SET role = REGEXP_REPLACE(role, '^ROLE_', '')
WHERE role LIKE 'ROLE\_%' ESCAPE '\';

-- map giá trị cũ
UPDATE users SET role = 'MANAGER_OFFICE' WHERE role = 'MANAGER';

-- (optional) debug: check còn giá trị lỗi không
-- SELECT DISTINCT role FROM users;

-- 3. Add constraint mới (SAU KHI DATA CLEAN)
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (
        role = ANY (ARRAY[
                        'ADMIN',
                    'MANAGER',
                    'MANAGER_OFFICE',
                    'MANAGER_DEPARTMENT',
                    'EMPLOYEE'
                        ])
        );