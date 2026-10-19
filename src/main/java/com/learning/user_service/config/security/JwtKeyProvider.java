package com.learning.user_service.config.security;

import com.learning.user_service.config.AppProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RS256 signing key (TDD §15.1). Loads PEM files when configured; otherwise generates a
 * 2048-bit pair at startup (dev only: tokens become invalid after a restart).
 */
@Component
public class JwtKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyProvider.class);

    private final RSAKey rsaKey;

    public JwtKeyProvider(AppProperties properties) {
        AppProperties.Security config = properties.security();
        this.rsaKey = buildKey(config);
    }

    public RSAKey signingKey() {
        return rsaKey;
    }

    public RSAPublicKey publicKey() {
        try {
            return rsaKey.toRSAPublicKey();
        } catch (com.nimbusds.jose.JOSEException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Public keys only, for the JWKS endpoint the gateway reads. */
    public JWKSet publicJwkSet() {
        return new JWKSet(rsaKey.toPublicJWK());
    }

    private static RSAKey buildKey(AppProperties.Security config) {
        try {
            RSAPublicKey publicKey;
            RSAPrivateKey privateKey;
            if (StringUtils.hasText(config.privateKeyLocation()) && StringUtils.hasText(config.publicKeyLocation())) {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                privateKey = (RSAPrivateKey) factory.generatePrivate(
                        new PKCS8EncodedKeySpec(readPem(config.privateKeyLocation())));
                publicKey = (RSAPublicKey) factory.generatePublic(
                        new X509EncodedKeySpec(readPem(config.publicKeyLocation())));
                log.info("Loaded JWT signing key '{}' from PEM files", config.keyId());
            } else {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair pair = generator.generateKeyPair();
                publicKey = (RSAPublicKey) pair.getPublic();
                privateKey = (RSAPrivateKey) pair.getPrivate();
                log.warn("No JWT key configured; generated a temporary RSA key pair. "
                        + "Set app.security.private-key-location/public-key-location outside dev.");
            }
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(config.keyId())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                    .build();
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("Cannot initialise JWT signing key", ex);
        }
    }

    private static byte[] readPem(String location) throws IOException {
        Resource resource = new DefaultResourceLoader().getResource(location);
        try (InputStream in = resource.getInputStream()) {
            String pem = new String(in.readAllBytes(), StandardCharsets.US_ASCII);
            String base64 = pem.replaceAll("-----BEGIN [A-Z ]+-----", "")
                    .replaceAll("-----END [A-Z ]+-----", "")
                    .replaceAll("\\s", "");
            return Base64.getDecoder().decode(base64);
        }
    }
}
