package com.learning.user_service.config.security;

import com.learning.user_service.support.TestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtKeyProviderTest {

    @TempDir Path dir;

    private static String pem(String label, byte[] der) {
        return "-----BEGIN " + label + "-----\n" + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der)
                + "\n-----END " + label + "-----\n";
    }

    private JwtKeyProvider load(String privatePem, String publicPem) throws Exception {
        Path priv = Files.writeString(dir.resolve("private.pem"), privatePem);
        Path pub = Files.writeString(dir.resolve("public.pem"), publicPem);
        var base = TestProperties.defaults();
        var s = base.security();
        var security = new com.learning.user_service.config.AppProperties.Security(s.issuer(), s.tenantId(),
                s.accessTokenTtl(), s.refreshTokenTtl(), s.bcryptStrength(), priv.toUri().toString(),
                pub.toUri().toString(), s.keyId(), s.internalServiceToken(), s.maxFailedLogins(), s.lockoutDuration());
        return new JwtKeyProvider(new com.learning.user_service.config.AppProperties(security, base.rateLimit(),
                base.tokens(), base.idempotency(), base.events(), base.outbox(), base.dev()));
    }

    @Test
    void loadsPkcs8AndX509Pem() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair pair = g.generateKeyPair();
        JwtKeyProvider provider = load(pem("PRIVATE KEY", pair.getPrivate().getEncoded()),
                pem("PUBLIC KEY", pair.getPublic().getEncoded()));
        assertThat(provider.publicKey()).isEqualTo(pair.getPublic());
    }

    @Test
    void loadsPkcs1Pem() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair pair = g.generateKeyPair();
        // PKCS#1 is the RSAPrivateKey / RSAPublicKey structure inside the PKCS#8 / X.509 wrapper
        byte[] pkcs8 = pair.getPrivate().getEncoded();
        byte[] x509 = pair.getPublic().getEncoded();
        byte[] pkcs1Private = java.util.Arrays.copyOfRange(pkcs8, pkcs8.length - lengthOfInnerKey(pkcs8), pkcs8.length);
        byte[] pkcs1Public = java.util.Arrays.copyOfRange(x509, x509.length - lengthOfInnerKey(x509) + (x509[x509.length - lengthOfInnerKey(x509)] == 0 ? 1 : 0), x509.length);
        JwtKeyProvider provider = load(pem("RSA PRIVATE KEY", pkcs1Private), pem("RSA PUBLIC KEY", pkcs1Public));
        assertThat(provider.publicKey()).isEqualTo(pair.getPublic());
        assertThat(provider.signingKey().toRSAPrivateKey()).isNotNull();
    }

    /** Length of the last DER element (the OCTET STRING / BIT STRING payload including its header). */
    private static int lengthOfInnerKey(byte[] der) {
        // the inner key is the final element: find it by walking the outer SEQUENCE
        int i = 1;
        i += (der[i] & 0x80) != 0 ? 1 + (der[i] & 0x7f) : 1;           // outer length
        if (der[i] == 0x02) { i += 3; }                                  // PKCS#8 version INTEGER 0
        i += 2 + der[i + 1];                                             // AlgorithmIdentifier (15 bytes)
        int headerLen = 1 + ((der[i + 1] & 0x80) != 0 ? 1 + (der[i + 1] & 0x7f) : 1);
        return der.length - i - headerLen;
    }
}
