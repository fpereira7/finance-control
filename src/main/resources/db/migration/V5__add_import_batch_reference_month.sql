ALTER TABLE import_batches
    ADD COLUMN reference_year INTEGER,
    ADD COLUMN reference_month INTEGER;

UPDATE import_batches
SET reference_year = CAST(substring(file_name FROM '([0-9]{4})-[0-9]{2}') AS INTEGER),
    reference_month = CAST(substring(file_name FROM '[0-9]{4}-([0-9]{2})') AS INTEGER)
WHERE file_name ~ '[0-9]{4}-[0-9]{2}';

UPDATE import_batches
SET reference_year = EXTRACT(YEAR FROM imported_at)::INTEGER,
    reference_month = EXTRACT(MONTH FROM imported_at)::INTEGER
WHERE reference_year IS NULL
   OR reference_month IS NULL;

DELETE FROM credit_card_transactions t
WHERE t.import_batch_id IN (
    SELECT b.id
    FROM import_batches b
    WHERE EXISTS (
        SELECT 1
        FROM import_batches older
        WHERE older.reference_year = b.reference_year
          AND older.reference_month = b.reference_month
          AND older.id < b.id
    )
);

DELETE FROM import_batches b
WHERE EXISTS (
    SELECT 1
    FROM import_batches older
    WHERE older.reference_year = b.reference_year
      AND older.reference_month = b.reference_month
      AND older.id < b.id
);

ALTER TABLE import_batches
    ALTER COLUMN reference_year SET NOT NULL,
    ALTER COLUMN reference_month SET NOT NULL;

ALTER TABLE import_batches
    ADD CONSTRAINT ck_import_batches_reference_month
        CHECK (reference_month BETWEEN 1 AND 12);

CREATE INDEX idx_import_batches_reference_period
    ON import_batches (reference_year, reference_month);

CREATE UNIQUE INDEX uk_import_batches_reference_period
    ON import_batches (reference_year, reference_month);
