CREATE TABLE monthly_expenses (
    id              BIGSERIAL PRIMARY KEY,
    category        VARCHAR(50) NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    due_date        DATE NOT NULL,
    payment_status  VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_monthly_expenses_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_monthly_expenses_payment_status
        CHECK (payment_status IN ('PAID', 'PENDING'))
);

CREATE INDEX idx_monthly_expenses_due_date ON monthly_expenses (due_date);
CREATE INDEX idx_monthly_expenses_category ON monthly_expenses (category);
CREATE INDEX idx_monthly_expenses_payment_status ON monthly_expenses (payment_status);
