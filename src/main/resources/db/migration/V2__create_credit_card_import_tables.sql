CREATE TABLE import_batches (
    id           BIGSERIAL PRIMARY KEY,
    file_name    VARCHAR(255) NOT NULL,
    imported_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    row_count    INTEGER NOT NULL DEFAULT 0,
    skipped_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_import_batches_file_name UNIQUE (file_name)
);

CREATE TABLE credit_card_transactions (
    id                BIGSERIAL PRIMARY KEY,
    import_batch_id   BIGINT NOT NULL,
    transaction_date  DATE NOT NULL,
    title             VARCHAR(255) NOT NULL,
    amount            NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_credit_card_transactions_import_batch
        FOREIGN KEY (import_batch_id) REFERENCES import_batches (id),
    CONSTRAINT ck_credit_card_transactions_amount_positive
        CHECK (amount > 0)
);

CREATE INDEX idx_credit_card_transactions_import_batch_id
    ON credit_card_transactions (import_batch_id);

CREATE INDEX idx_credit_card_transactions_transaction_date
    ON credit_card_transactions (transaction_date);
