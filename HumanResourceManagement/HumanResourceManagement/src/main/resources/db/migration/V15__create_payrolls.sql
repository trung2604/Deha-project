CREATE TABLE IF NOT EXISTS payrolls (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    office_id UUID NOT NULL REFERENCES offices(id) ON DELETE CASCADE,
    pay_year INT NOT NULL,
    pay_month INT NOT NULL,
    base_salary_snapshot NUMERIC(19,2) NOT NULL,
    working_days_in_month INT NOT NULL,
    present_days INT NOT NULL,
    regular_pay NUMERIC(19,2) NOT NULL,
    ot_hours INT NOT NULL,
    ot_pay NUMERIC(19,2) NOT NULL,
    net_salary NUMERIC(19,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    generated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payrolls_user_period ON payrolls(user_id, pay_year, pay_month);
CREATE INDEX IF NOT EXISTS idx_payrolls_office_period ON payrolls(office_id, pay_year, pay_month);

