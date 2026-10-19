-- =====================================================================
-- Tables and columns the API (§11.2) needs that the §9.1 schema leaves out.
-- =====================================================================

-- One-time password reset tokens. Same shape as email_verifications:
-- only the SHA-256 hash of the token is stored.
CREATE TABLE password_reset_tokens (
    id          RAW(16) DEFAULT SYS_GUID() NOT NULL,
    user_id     RAW(16) NOT NULL,
    token_hash  VARCHAR2(64 CHAR) NOT NULL,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT fk_pwd_reset_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uq_password_reset_token_hash ON password_reset_tokens (token_hash);
CREATE INDEX idx_password_reset_user ON password_reset_tokens (user_id);

-- Look up verification tokens by hash.
CREATE UNIQUE INDEX uq_email_verif_token_hash ON email_verifications (token_hash);

-- Idempotency-Key store (§11.1, §20.5). Rows live for 24h.
CREATE TABLE idempotency_keys (
    scope            VARCHAR2(100 CHAR) NOT NULL,
    idem_key         VARCHAR2(100 CHAR) NOT NULL,
    request_hash     VARCHAR2(64 CHAR)  NOT NULL,
    status           VARCHAR2(20 CHAR)  NOT NULL,
    response_status  NUMBER(10),
    response_body    CLOB,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_idempotency_keys PRIMARY KEY (scope, idem_key),
    CONSTRAINT ck_idempotency_status CHECK (status IN ('IN_PROGRESS','COMPLETED'))
);
CREATE INDEX idx_idempotency_created ON idempotency_keys (created_at);

-- Outbox retry bookkeeping. After max attempts a row moves to FAILED.
ALTER TABLE user_outbox ADD (
    attempts   NUMBER(10) DEFAULT 0 NOT NULL,
    last_error VARCHAR2(1000 CHAR)
);
ALTER TABLE user_outbox ADD CONSTRAINT ck_user_outbox_status CHECK (status IN ('PENDING','SENT','FAILED'));

-- Audit columns (§9 conventions) for the tables that lack them.
ALTER TABLE payment_method_tokens ADD (updated_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL);
