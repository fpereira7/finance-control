CREATE TABLE salaries (
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(10) NOT NULL,
    description     VARCHAR(255),
    amount          NUMERIC(19, 2) NOT NULL,
    payment_date    DATE NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_salaries_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_salaries_type CHECK (type IN ('CLT', 'PJ')),
    CONSTRAINT ck_salaries_status CHECK (status IN ('RECEIVED', 'PENDING'))
);

CREATE INDEX idx_salaries_payment_date ON salaries (payment_date);
CREATE INDEX idx_salaries_type ON salaries (type);
CREATE INDEX idx_salaries_status ON salaries (status);
