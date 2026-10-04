-- =====================================================================
-- USERS DB
-- =====================================================================
CREATE TABLE users (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email              VARCHAR(255) NOT NULL,
    phone              VARCHAR(20),
    first_name         VARCHAR(80)  NOT NULL,
    last_name          VARCHAR(80),
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING_VERIFICATION'
                       CHECK (status IN ('PENDING_VERIFICATION','ACTIVE','SUSPENDED','DELETED')),
    email_verified_at  TIMESTAMPTZ,
    phone_verified_at  TIMESTAMPTZ,
    last_login_at      TIMESTAMPTZ,
    locale             VARCHAR(10)  NOT NULL DEFAULT 'en-IN',
    version            BIGINT       NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at         TIMESTAMPTZ
);
CREATE UNIQUE INDEX uq_users_email ON users (LOWER(email)) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_users_phone ON users (phone) WHERE phone IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_users_status ON users (status);

CREATE TABLE password_credentials (
    user_id          UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    password_hash    VARCHAR(255) NOT NULL,
    algorithm        VARCHAR(20)  NOT NULL DEFAULT 'BCRYPT',
    failed_attempts  INT          NOT NULL DEFAULT 0,
    locked_until     TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id    SMALLSERIAL PRIMARY KEY,
    code  VARCHAR(40) NOT NULL UNIQUE,
    label VARCHAR(80) NOT NULL
);
INSERT INTO roles (code, label) VALUES
    ('CUSTOMER','Customer'),('SUPPLIER','Supplier'),
    ('ADMIN','Platform Admin'),('OPS','Operations');

CREATE TABLE user_roles (
    user_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id  SMALLINT NOT NULL REFERENCES roles(id),
    granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE addresses (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label        VARCHAR(40),
    line1        VARCHAR(200) NOT NULL,
    line2        VARCHAR(200),
    city         VARCHAR(100) NOT NULL,
    state        VARCHAR(100) NOT NULL,
    postal_code  VARCHAR(20)  NOT NULL,
    country      CHAR(2)      NOT NULL,
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    geo_lat      NUMERIC(9,6),
    geo_lng      NUMERIC(9,6),
    version      BIGINT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_addresses_user ON addresses (user_id);
CREATE UNIQUE INDEX uq_addresses_default_per_user ON addresses (user_id) WHERE is_default = TRUE;

CREATE TABLE payment_method_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider     VARCHAR(20) NOT NULL,                    -- STRIPE | RAZORPAY
    provider_token VARCHAR(255) NOT NULL,                 -- pm_xxx (NEVER raw PAN)
    brand        VARCHAR(20),                             -- VISA, MC...
    last4        CHAR(4),
    exp_month    SMALLINT,
    exp_year     SMALLINT,
    is_default   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pmt_user ON payment_method_tokens (user_id);

CREATE TABLE email_verifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  CHAR(64) NOT NULL,                         -- SHA-256 hex
    expires_at  TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ
);
CREATE INDEX idx_email_verif_user ON email_verifications (user_id);

CREATE TABLE consent_log (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    kind       VARCHAR(40) NOT NULL,        -- MARKETING_EMAIL, COOKIES, ...
    granted    BOOLEAN NOT NULL,
    ip         INET,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_consent_user ON consent_log (user_id, kind);

CREATE TABLE user_outbox (
    id            BIGSERIAL PRIMARY KEY,
    aggregate_id  UUID         NOT NULL,
    event_type    VARCHAR(60)  NOT NULL,
    payload       JSONB        NOT NULL,
    headers       JSONB,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING|SENT|FAILED
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at       TIMESTAMPTZ
);
CREATE INDEX idx_user_outbox_pending ON user_outbox (status, created_at) WHERE status = 'PENDING';
