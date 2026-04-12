CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_email VARCHAR(100),
    http_method VARCHAR(10) NOT NULL,
    endpoint_pattern VARCHAR(255) NOT NULL,
    request_uri VARCHAR(500) NOT NULL,
    target_id UUID,
    status_code INT NOT NULL,
    success BOOLEAN NOT NULL,
    client_ip VARCHAR(45),
    user_agent VARCHAR(500),
    duration_ms BIGINT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_occurred_at ON audit_logs(occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_user ON audit_logs(actor_user_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_method_path ON audit_logs(http_method, endpoint_pattern);

