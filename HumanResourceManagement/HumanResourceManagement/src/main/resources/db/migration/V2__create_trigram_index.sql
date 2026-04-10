CREATE INDEX idx_users_search_all_trgm
    ON users
    USING GIN ((first_name || ' ' || last_name || ' ' || email) gin_trgm_ops);