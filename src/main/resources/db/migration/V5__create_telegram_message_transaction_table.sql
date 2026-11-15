CREATE TABLE telegram_message_transaction (
        id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        chat_id          BIGINT NOT NULL,
        message_id       BIGINT  NOT NULL,
        transaction_id   UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
        created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
        updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
        CONSTRAINT uq_telegram_message_transaction_chat_message UNIQUE (chat_id, message_id)
);