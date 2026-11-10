CREATE TABLE telegram_bot_poll_state (
       id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       last_offset      BIGINT NOT NULL,
       created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
       updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);