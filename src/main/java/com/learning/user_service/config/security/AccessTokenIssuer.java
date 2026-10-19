package com.learning.user_service.config.security;

import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.gateway.AccessTokenFactory;
import com.learning.user_service.config.AppProperties;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/** Issues RS256 access tokens with claims {@code sub, roles[], tenantId, jti, iat, exp} (TDD §15.1). */
@Component
public class AccessTokenIssuer implements AccessTokenFactory {

    private final JwtEncoder jwtEncoder;
    private final AppProperties.Security config;
    private final Clock clock;

    public AccessTokenIssuer(JwtEncoder jwtEncoder, AppProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.config = properties.security();
        this.clock = clock;
    }

    @Override
    public IssuedAccessToken issue(UUID userId, Collection<RoleCode> roles) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(config.accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(config.issuer())
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("roles", roles.stream().map(Enum::name).sorted().toList())
                .claim("tenantId", config.tenantId())
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(config.keyId()).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedAccessToken(token, config.accessTokenTtl());
    }
}
