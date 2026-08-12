CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Default user used to own pre-existing rows during migration.
-- Password: ChangeMe123! (change or delete after first login in non-dev environments)
INSERT INTO users (email, password_hash, name, created_at, updated_at)
VALUES (
    'migration@local',
    '$2y$10$aZdRQ3JdxikypKVkFHpp.eC.Z5banD/DMUc5otjaLypflb9y0yAXC',
    'Migration User',
    NOW(),
    NOW()
);

ALTER TABLE salaries
    ADD COLUMN user_id BIGINT;

ALTER TABLE monthly_expenses
    ADD COLUMN user_id BIGINT;

ALTER TABLE import_batches
    ADD COLUMN user_id BIGINT;

UPDATE salaries
SET user_id = (SELECT id FROM users WHERE email = 'migration@local')
WHERE user_id IS NULL;

UPDATE monthly_expenses
SET user_id = (SELECT id FROM users WHERE email = 'migration@local')
WHERE user_id IS NULL;

UPDATE import_batches
SET user_id = (SELECT id FROM users WHERE email = 'migration@local')
WHERE user_id IS NULL;

ALTER TABLE salaries
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE monthly_expenses
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE import_batches
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE salaries
    ADD CONSTRAINT fk_salaries_user
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE monthly_expenses
    ADD CONSTRAINT fk_monthly_expenses_user
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE import_batches
    ADD CONSTRAINT fk_import_batches_user
        FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_salaries_user_id ON salaries (user_id);
CREATE INDEX idx_monthly_expenses_user_id ON monthly_expenses (user_id);
CREATE INDEX idx_import_batches_user_id ON import_batches (user_id);

ALTER TABLE import_batches
    DROP CONSTRAINT uk_import_batches_file_name;

ALTER TABLE import_batches
    ADD CONSTRAINT uk_import_batches_user_file_name UNIQUE (user_id, file_name);

DROP INDEX IF EXISTS uk_import_batches_reference_period;

CREATE UNIQUE INDEX uk_import_batches_user_reference_period
    ON import_batches (user_id, reference_year, reference_month);
