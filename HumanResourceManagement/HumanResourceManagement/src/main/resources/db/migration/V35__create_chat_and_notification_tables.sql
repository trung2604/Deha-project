CREATE TABLE IF NOT EXISTS chat_rooms (
    id UUID PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    name VARCHAR(255),
    department_id UUID REFERENCES departments(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content VARCHAR(2000) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    reference_id VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_msg_room_sent ON chat_messages(room_id, sent_at DESC);
CREATE INDEX IF NOT EXISTS idx_notif_user_read ON notifications(user_id, is_read, created_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS uq_chat_rooms_general ON chat_rooms(type) WHERE type = 'GENERAL';
CREATE UNIQUE INDEX IF NOT EXISTS uq_chat_rooms_department ON chat_rooms(department_id) WHERE type = 'DEPARTMENT';

INSERT INTO chat_rooms (id, type, name)
SELECT '00000000-0000-0000-0000-000000000001', 'GENERAL', 'General'
WHERE NOT EXISTS (
    SELECT 1
    FROM chat_rooms
    WHERE type = 'GENERAL'
);

