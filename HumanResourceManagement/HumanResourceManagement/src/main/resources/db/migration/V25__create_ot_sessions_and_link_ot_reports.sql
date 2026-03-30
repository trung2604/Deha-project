CREATE TABLE IF NOT EXISTS ot_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    office_id UUID NOT NULL REFERENCES offices(id) ON DELETE CASCADE,
    ot_request_id UUID NOT NULL REFERENCES ot_requests(id) ON DELETE CASCADE,
    log_date DATE NOT NULL,
    check_in_time TIMESTAMP NOT NULL,
    check_out_time TIMESTAMP,
    source VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    CONSTRAINT uq_ot_sessions_user_date UNIQUE (user_id, log_date)
);

ALTER TABLE ot_reports
    ADD COLUMN IF NOT EXISTS ot_session_id UUID;

UPDATE ot_reports r
SET ot_session_id = s.id
FROM ot_sessions s
WHERE r.ot_request_id = s.ot_request_id
  AND r.ot_session_id IS NULL;

ALTER TABLE ot_reports
    ADD CONSTRAINT fk_ot_reports_ot_session
        FOREIGN KEY (ot_session_id) REFERENCES ot_sessions(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS uq_ot_reports_ot_session
    ON ot_reports(ot_session_id);
