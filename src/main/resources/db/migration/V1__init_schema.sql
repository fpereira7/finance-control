-- Baseline schema for finance-control
CREATE TABLE IF NOT EXISTS schema_baseline (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
