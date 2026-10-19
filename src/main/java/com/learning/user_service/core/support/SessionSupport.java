package com.learning.user_service.core.support;

import com.learning.user_service.config.AppProperties;
import com.learning.user_service.core.domain.TokenPair;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.AccessTokenFactory;
import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Token issuing and error helpers shared by login, refresh and logout. */
@Component
public class SessionSupport {

    private static final String TOKEN_TYPE = "Bearer";

    private final AccessTokenFactory accessTokenFactory;
    private final RefreshTokenStore refreshTokenStore;
    private final AppProperties.Security security;
    /** Compared against when the email is unknown, so response time does not reveal which emails exist. */
    private final String dummyHash;

    public SessionSupport(PasswordEncoder passwordEncoder, AccessTokenFactory accessTokenFactory,
                          RefreshTokenStore refreshTokenStore, AppProperties properties) {
        this.accessTokenFactory = accessTokenFactory;
        this.refreshTokenStore = refreshTokenStore;
        this.security = properties.security();
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing-" + UUID.randomUUID());
    }

    public String dummyHash() {
        return dummyHash;
    }

    public TokenPair issueTokens(UserEntity user) {
        AccessTokenFactory.IssuedAccessToken access = accessTokenFactory.issue(user.getId(), user.roleCodes());
        String jti = UUID.randomUUID().toString();
        String secret = SecureTokens.newToken();
        refreshTokenStore.save(jti, user.getId(), SecureTokens.sha256Hex(secret), security.refreshTokenTtl());
        return new TokenPair(access.value(), jti + "." + secret, TOKEN_TYPE,
                access.ttl().toSeconds(), security.refreshTokenTtl().toSeconds());
    }

    public static ApiException invalidCredentials() {
        return new ApiException(ErrorCode.INVALID_CREDENTIALS, "Email or password is incorrect");
    }

    public static ApiException invalidRefreshToken() {
        return new ApiException(ErrorCode.INVALID_TOKEN, "Refresh token is invalid, expired or revoked");
    }

    public record ParsedRefreshToken(String jti, String secret) {

        public static Optional<ParsedRefreshToken> parse(String raw) {
            if (raw == null) {
                return Optional.empty();
            }
            int dot = raw.indexOf('.');
            if (dot <= 0 || dot == raw.length() - 1) {
                return Optional.empty();
            }
            String jti = raw.substring(0, dot);
            try {
                UUID.fromString(jti);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            return Optional.of(new ParsedRefreshToken(jti, raw.substring(dot + 1)));
        }
    }
}
