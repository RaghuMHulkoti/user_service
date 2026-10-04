-- =====================================================================
-- Tables and columns the API (§11.2) needs that the §9.1 schema leaves out.
-- =====================================================================

-- One-time password reset tokens. Same shape as email_verifications:
-- only the SHA-256 hash of the token is stored.
CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  CHAR(64) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uq_password_reset_token_hash ON password_reset_tokens (token_hash);
CREATE INDEX idx_password_reset_user ON password_reset_tokens (user_id);

-- Look up verification tokens by hash.
CREATE UNIQUE INDEX uq_email_verif_token_hash ON email_verifications (token_hash);

-- Idempotency-Key store (§11.1, §20.5). Rows live for 24h.
CREATE TABLE idempotency_keys (
    scope            VARCHAR(100) NOT NULL,
    idem_key         VARCHAR(100) NOT NULL,
    request_hash     CHAR(64)     NOT NULL,
    status           VARCHAR(20)  NOT NULL CHECK (status IN ('IN_PROGRESS','COMPLETED')),
    response_status  INT,
    response_body    TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (scope, idem_key)
);
CREATE INDEX idx_idempotency_created ON idempotency_keys (created_at);

-- Outbox retry bookkeeping. After max attempts a row moves to FAILED.
ALTER TABLE user_outbox ADD COLUMN attempts   INT  NOT NULL DEFAULT 0;
ALTER TABLE user_outbox ADD COLUMN last_error TEXT;
ALTER TABLE user_outbox ADD CONSTRAINT ck_user_outbox_status CHECK (status IN ('PENDING','SENT','FAILED'));

-- Audit columns (§9 conventions) for the tables that lack them.
ALTER TABLE payment_method_tokens ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
