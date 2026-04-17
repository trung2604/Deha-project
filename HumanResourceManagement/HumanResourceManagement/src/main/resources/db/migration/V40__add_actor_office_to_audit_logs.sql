ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS actor_office_id UUID REFERENCES offices(id) ON DELETE SET NULL;

UPDATE audit_logs al
SET actor_office_id = u.office_id
FROM users u
WHERE al.actor_user_id = u.id
  AND al.actor_office_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_office
    ON audit_logs(actor_office_id, occurred_at DESC);

