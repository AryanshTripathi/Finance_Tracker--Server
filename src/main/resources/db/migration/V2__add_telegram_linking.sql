ALTER TABLE users ADD COLUMN telegram_chat_id BIGINT DEFAULT NULL UNIQUE;

CREATE TABLE telegram_link_request (
    code            VARCHAR(6) PRIMARY KEY,
    chat_id         BIGINT NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
