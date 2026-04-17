ALTER TABLE chat_rooms
    ADD COLUMN IF NOT EXISTS office_id UUID REFERENCES offices(id) ON DELETE CASCADE;

DROP INDEX IF EXISTS uq_chat_rooms_general;

CREATE UNIQUE INDEX IF NOT EXISTS uq_chat_rooms_general_office
    ON chat_rooms(office_id)
    WHERE type = 'GENERAL';

INSERT INTO chat_rooms (id, type, name, office_id)
SELECT
    (
        substr(md5('office-general-room-' || o.id::text), 1, 8) || '-' ||
        substr(md5('office-general-room-' || o.id::text), 9, 4) || '-' ||
        substr(md5('office-general-room-' || o.id::text), 13, 4) || '-' ||
        substr(md5('office-general-room-' || o.id::text), 17, 4) || '-' ||
        substr(md5('office-general-room-' || o.id::text), 21, 12)
    )::uuid,
    'GENERAL',
    'General - ' || o.name,
    o.id
FROM offices o
WHERE NOT EXISTS (
    SELECT 1
    FROM chat_rooms r
    WHERE r.type = 'GENERAL' AND r.office_id = o.id
);

