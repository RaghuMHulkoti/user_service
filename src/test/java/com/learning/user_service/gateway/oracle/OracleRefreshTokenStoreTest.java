package com.learning.user_service.gateway.oracle;

import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.oracle.refreshtoken.OracleRefreshTokenStore;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.gateway.oracle.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Refresh tokens in the USERS schema, run against the real migration on H2 in Oracle mode. No Redis involved. */
@SpringBootTest(properties = "app.outbox.poll-interval=1h")
@ActiveProfiles("test")
class OracleRefreshTokenStoreTest {

    @Autowired RefreshTokenStore store;
    @Autowired UserRepository users;
    UUID userId;

    @BeforeEach
    void setUp() {
        var user = new UserEntity();
        user.setEmail("rt-" + UUID.randomUUID() + "@example.com");
        user.setFirstName("Test");
        user.setLocale("en-IN");
        userId = users.save(user).getId();
    }

    @Test
    void theDefaultStoreIsTheDatabaseOne() {
        assertThat(store).isInstanceOf(OracleRefreshTokenStore.class);
    }

    @Test
    void aSavedTokenCanBeFoundWithoutBeingUsedUp() {
        store.save("jti-find", userId, "hash-1", Duration.ofDays(30));

        assertThat(store.find("jti-find")).hasValueSatisfying(t -> {
            assertThat(t.userId()).isEqualTo(userId);
            assertThat(t.secretHash()).isEqualTo("hash-1");
        });
        assertThat(store.find("jti-find")).isPresent();
        assertThat(store.find("nope")).isEmpty();
    }

    @Test
    void aTokenCanBeConsumedOnlyOnce() {
        store.save("jti-once", userId, "hash-2", Duration.ofDays(30));

        assertThat(store.consume("jti-once")).isPresent();
        assertThat(store.consume("jti-once")).isEmpty();
        assertThat(store.find("jti-once")).isEmpty();
    }

    @Test
    void whenTwoRequestsRaceForTheSameTokenExactlyOneWins() throws Exception {
        store.save("jti-race", userId, "hash-3", Duration.ofDays(30));
        int callers = 6;
        var ready = new CountDownLatch(callers);
        var go = new CountDownLatch(1);
        var wins = new AtomicInteger();
        try (var pool = Executors.newFixedThreadPool(callers)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < callers; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    try { go.await(); } catch (InterruptedException e) { return; }
                    if (store.consume("jti-race").isPresent()) wins.incrementAndGet();
                }));
            }
            ready.await();
            go.countDown();
            for (var f : futures) f.get();
        }
        assertThat(wins.get()).isEqualTo(1);
    }

    @Test
    void revokeRemovesOneTokenAndRevokeAllRemovesTheUsersSessions() {
        store.save("jti-a", userId, "h", Duration.ofDays(30));
        store.save("jti-b", userId, "h", Duration.ofDays(30));
        store.save("jti-c", userId, "h", Duration.ofDays(30));

        store.revoke("jti-a");
        assertThat(store.find("jti-a")).isEmpty();
        assertThat(store.find("jti-b")).isPresent();

        store.revokeAllForUser(userId);
        assertThat(store.find("jti-b")).isEmpty();
        assertThat(store.find("jti-c")).isEmpty();
    }

    @Test
    void anExpiredTokenIsNeitherFoundNorConsumedAndIsPurged() {
        store.save("jti-old", userId, "h", Duration.ofSeconds(-5));

        assertThat(store.find("jti-old")).isEmpty();
        assertThat(store.consume("jti-old")).isEmpty();

        store.save("jti-old-2", userId, "h", Duration.ofSeconds(-5));
        assertThat(store.deleteExpired(Instant.now())).isGreaterThanOrEqualTo(1);
    }
}
