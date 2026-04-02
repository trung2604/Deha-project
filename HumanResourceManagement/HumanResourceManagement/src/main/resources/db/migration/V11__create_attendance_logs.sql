-- Attendance logs table (check-in / check-out per user per day)
CREATE TABLE IF NOT EXISTS attendance_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    office_id UUID NOT NULL REFERENCES offices(id) ON DELETE CASCADE,
    check_in_time TIMESTAMP NOT NULL,
    check_out_time TIMESTAMP NULL,
    worked_hours INTEGER NULL,
    client_ip VARCHAR(45) NULL,
    log_date DATE NOT NULL
);

-- One check-in per user per day
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendance_logs_user_log_date
    ON attendance_logs(user_id, log_date);

CREATE INDEX IF NOT EXISTS idx_attendance_logs_office_date
    ON attendance_logs(office_id, log_date);

