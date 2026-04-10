INSERT INTO chat_rooms (id, type, name, department_id)
SELECT
    (
        substr(md5('dept-room-' || d.id::text), 1, 8) || '-' ||
        substr(md5('dept-room-' || d.id::text), 9, 4) || '-' ||
        substr(md5('dept-room-' || d.id::text), 13, 4) || '-' ||
        substr(md5('dept-room-' || d.id::text), 17, 4) || '-' ||
        substr(md5('dept-room-' || d.id::text), 21, 12)
    )::uuid,
    'DEPARTMENT',
    d.name,
    d.id
FROM departments d
WHERE NOT EXISTS (
    SELECT 1
    FROM chat_rooms r
    WHERE r.type = 'DEPARTMENT' AND r.department_id = d.id
);

