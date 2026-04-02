CREATE TABLE IF NOT EXISTS ot_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    office_id UUID NOT NULL REFERENCES offices(id) ON DELETE CASCADE,
    log_date DATE NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(32) NOT NULL,
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP,
    decision_note VARCHAR(500),
    CONSTRAINT uq_ot_requests_user_date UNIQUE (user_id, log_date)
);

CREATE TABLE IF NOT EXISTS ot_reports (
    id UUID PRIMARY KEY,
    attendance_log_id UUID NOT NULL REFERENCES attendance_logs(id) ON DELETE CASCADE,
    ot_request_id UUID NOT NULL REFERENCES ot_requests(id) ON DELETE CASCADE,
    reported_ot_hours INT NOT NULL,
    report_note VARCHAR(500) NOT NULL,
    status VARCHAR(32) NOT NULL,
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP,
    decision_note VARCHAR(500),
    CONSTRAINT uq_ot_reports_attendance_log UNIQUE (attendance_log_id)
);

CREATE INDEX IF NOT EXISTS idx_ot_requests_office_date ON ot_requests(office_id, log_date);
CREATE INDEX IF NOT EXISTS idx_ot_reports_status ON ot_reports(status);

