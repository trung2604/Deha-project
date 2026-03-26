CREATE TABLE IF NOT EXISTS salary_contracts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    base_salary NUMERIC(19,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL
);

CREATE INDEX IF NOT EXISTS idx_salary_contracts_user_id ON salary_contracts(user_id);
CREATE INDEX IF NOT EXISTS idx_salary_contracts_start_date ON salary_contracts(start_date);

