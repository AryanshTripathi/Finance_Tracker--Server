CREATE TABLE users (
   id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   email         VARCHAR(255) NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   display_name  VARCHAR(255) NOT NULL,
   created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
   updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(20) NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    owner_user_id   UUID NULL REFERENCES users(id) ON DELETE CASCADE,
    is_default      BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_category_name_per_owner UNIQUE (owner_user_id, name, type),
    CONSTRAINT ck_default_category_has_no_owner CHECK (NOT is_default OR owner_user_id IS NULL)
);

CREATE TABLE transactions (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount                 NUMERIC(19,4) NOT NULL CHECK (amount >= 0),
    currency               VARCHAR(3) NOT NULL,
    transaction_date       DATE NOT NULL,
    transaction_timestamp  TIMESTAMPTZ NULL,
    type                   VARCHAR(20) NOT NULL CHECK (type IN ('INCOME','EXPENSE','TRANSFER','REFUND')),
    description            VARCHAR(500) NULL,
    category_id            UUID NULL REFERENCES categories(id) ON DELETE SET NULL,
    categorization_status  VARCHAR(20) NOT NULL DEFAULT 'UNCATEGORIZED'
      CHECK (categorization_status IN ('UNCATEGORIZED','CATEGORIZED','AUTO_CATEGORIZED')),
    source                 VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    external_ref_id        VARCHAR(255) NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_txn_dedup UNIQUE (user_id, source, external_ref_id)
);

INSERT INTO categories (name, type, is_default) VALUES
    ('Salary', 'INCOME', true),
    ('Freelance', 'INCOME', true),
    ('Interest', 'INCOME', true),
    ('Other Income', 'INCOME', true),
    ('Food', 'EXPENSE', true),
    ('Groceries', 'EXPENSE', true),
    ('Transport', 'EXPENSE', true),
    ('Shopping', 'EXPENSE', true),
    ('Utilities', 'EXPENSE', true),
    ('Rent', 'EXPENSE', true),
    ('Entertainment', 'EXPENSE', true),
    ('Healthcare', 'EXPENSE', true),
    ('Other Expenses', 'EXPENSE', true);