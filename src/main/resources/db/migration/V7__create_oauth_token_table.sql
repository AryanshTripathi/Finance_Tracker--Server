CREATE TABLE oauth_token (
           id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
           user_id         UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
           refresh_token   VARCHAR(512) NOT NULL,
           created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
           updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);