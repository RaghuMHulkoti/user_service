-- Refresh tokens kept in the database, so login works without Redis. Only the SHA-256 hash of the secret is stored.
CREATE TABLE refresh_tokens (
    jti         VARCHAR2(64 CHAR) NOT NULL,
    user_id     RAW(16) NOT NULL,
    secret_hash VARCHAR2(64 CHAR) NOT NULL,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (jti),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);
