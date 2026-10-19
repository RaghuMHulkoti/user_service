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

    /**
     * Reads a PEM file and returns DER bytes in the form the JDK key specs expect: PKCS#8 for private keys,
     * X.509 for public keys. PKCS#1 files ("BEGIN RSA PRIVATE KEY" / "BEGIN RSA PUBLIC KEY"), which
     * {@code openssl genrsa} writes by default, are wrapped into those forms.
     */
    private static byte[] readPem(String location) throws IOException {
        Resource resource = new DefaultResourceLoader().getResource(location);
        try (InputStream in = resource.getInputStream()) {
            String pem = new String(in.readAllBytes(), StandardCharsets.US_ASCII);
            String base64 = pem.replaceAll("-----BEGIN [A-Z ]+-----", "")
                    .replaceAll("-----END [A-Z ]+-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(base64);
            if (pem.contains("BEGIN RSA PRIVATE KEY")) {
                return der(0x30, der(0x02, new byte[]{0}), RSA_ALGORITHM, der(0x04, der));
            }
            if (pem.contains("BEGIN RSA PUBLIC KEY")) {
                byte[] bits = new byte[der.length + 1];
                System.arraycopy(der, 0, bits, 1, der.length);
                return der(0x30, RSA_ALGORITHM, der(0x03, bits));
            }
            return der;
        }
    }

    /** DER AlgorithmIdentifier for rsaEncryption (1.2.840.113549.1.1.1) with NULL parameters. */
    private static final byte[] RSA_ALGORITHM = {0x30, 0x0d, 0x06, 0x09, 0x2a, (byte) 0x86, 0x48, (byte) 0x86,
            (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00};

    /** One DER element: tag, length, then the concatenated parts. */
    private static byte[] der(int tag, byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        out.write(tag);
        if (length < 128) {
            out.write(length);
        } else {
            int bytes = length > 0xFFFF ? 3 : length > 0xFF ? 2 : 1;
            out.write(0x80 | bytes);
            for (int i = bytes - 1; i >= 0; i--) {
                out.write(length >> (8 * i));
            }
        }
        for (byte[] part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }
}
