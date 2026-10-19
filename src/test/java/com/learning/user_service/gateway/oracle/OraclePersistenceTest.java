package com.learning.user_service.gateway.oracle;

import com.learning.user_service.core.support.IdempotencyService;
import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.core.job.HousekeepingJob;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.gateway.oracle.outbox.entity.UserOutboxEntity;
import com.learning.user_service.gateway.oracle.address.repository.AddressRepository;
import com.learning.user_service.gateway.oracle.consent.repository.ConsentLogRepository;
import com.learning.user_service.gateway.oracle.user.repository.RoleRepository;
import com.learning.user_service.gateway.oracle.outbox.repository.UserOutboxRepository;
import com.learning.user_service.gateway.oracle.user.repository.UserRepository;
import com.learning.user_service.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the real migrations, entity mappings and repository queries against H2 in Oracle mode. It catches schema
 * and mapping mistakes but is not a substitute for a run against Oracle itself. Needs no Redis.
 */
@SpringBootTest(properties = "app.outbox.poll-interval=1h")
@ActiveProfiles("test")
class OraclePersistenceTest {

    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired AddressRepository addresses;
    @Autowired ConsentLogRepository consents;
    @Autowired UserOutboxRepository outbox;
    @Autowired IdempotencyService idempotency;
    @Autowired HousekeepingJob housekeeping;
    @Autowired TransactionTemplate tx;

    @Test
    void seedRolesExist() {
        assertThat(roles.findByCodeIn(List.of(RoleCode.values()))).hasSize(RoleCode.values().length);
    }

    @Test
    void emailIsUniqueIgnoringCaseUntilTheOwnerIsDeleted() {
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        UserEntity first = users.saveAndFlush(user(email));

        assertThatThrownBy(() -> users.saveAndFlush(user(email.toUpperCase())))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(users.findActiveByEmail(email.toUpperCase())).get().extracting(UserEntity::getId)
                .isEqualTo(first.getId());

        first.setDeletedAt(Instant.now());
        users.saveAndFlush(first);
        assertThat(users.existsActiveByEmail(email)).isFalse();
        users.saveAndFlush(user(email));
        assertThat(users.existsActiveByEmail(email)).isTrue();
    }

    @Test
    void onlyOneDefaultAddressPerUser() {
        UserEntity owner = users.saveAndFlush(user("addr-" + UUID.randomUUID() + "@example.com"));
        addresses.saveAndFlush(address(owner.getId(), true));
        addresses.saveAndFlush(address(owner.getId(), false));

        assertThatThrownBy(() -> addresses.saveAndFlush(address(owner.getId(), true)))
                .isInstanceOf(DataIntegrityViolationException.class);

        Integer cleared = tx.execute(status -> addresses.clearDefault(owner.getId()));
        assertThat(cleared).isEqualTo(1);
        addresses.saveAndFlush(address(owner.getId(), true));
        assertThat(addresses.findByUserIdOrderByCreatedAtAsc(owner.getId())).hasSize(3);
    }

    @Test
    void latestConsentPerKindWins() {
        UserEntity owner = users.saveAndFlush(user("consent-" + UUID.randomUUID() + "@example.com"));
        consents.save(consent(owner.getId(), "MARKETING_EMAIL", true, "10.0.0.1"));
        consents.save(consent(owner.getId(), "COOKIES", true, null));
        consents.save(consent(owner.getId(), "MARKETING_EMAIL", false, "2001:db8::1"));

        List<ConsentLogEntity> latest = consents.findLatestPerKind(owner.getId());

        assertThat(latest).extracting(ConsentLogEntity::getKind).containsExactly("COOKIES", "MARKETING_EMAIL");
        assertThat(latest.get(1).isGranted()).isFalse();
        assertThat(latest.get(1).getIp()).isEqualTo("2001:db8::1");
    }

    @Test
    void outboxStoresJsonAndLocksPendingRowsInOrder() {
        UUID aggregate = UUID.randomUUID();
        for (int i = 0; i < 3; i++) {
            UserOutboxEntity row = new UserOutboxEntity();
            row.setAggregateId(aggregate);
            row.setEventType("USER_UPDATED");
            row.setPayload("{\"n\":" + i + "}");
            row.setHeaders("{\"eventType\":\"USER_UPDATED\"}");
            outbox.saveAndFlush(row);
        }

        List<UserOutboxEntity> locked = tx.execute(status -> outbox.lockPendingBatch(2));

        assertThat(locked).hasSize(2);
        assertThat(locked.get(0).getId()).isLessThan(locked.get(1).getId());
        assertThat(locked.get(0).getPayload()).startsWith("{\"n\":");

        UserOutboxEntity notJson = new UserOutboxEntity();
        notJson.setAggregateId(aggregate);
        notJson.setEventType("USER_UPDATED");
        notJson.setPayload("not json");
        assertThatThrownBy(() -> outbox.saveAndFlush(notJson)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void idempotencyKeyRunsTheActionOnceAndReplaysTheStoredResponse() {
        String key = UUID.randomUUID().toString();
        AtomicInteger runs = new AtomicInteger();
        record Body(String value) {
        }

        var first = idempotency.execute("it:scope", key, new Body("a"), Body.class,
                () -> new Body("run-" + runs.incrementAndGet()));
        var second = idempotency.execute("it:scope", key, new Body("a"), Body.class,
                () -> new Body("run-" + runs.incrementAndGet()));

        assertThat(first.replayed()).isFalse();
        assertThat(second.replayed()).isTrue();
        assertThat(second.body()).isEqualTo(new Body("run-1"));
        assertThat(runs).hasValue(1);
        assertThatThrownBy(() -> idempotency.execute("it:scope", key, new Body("different"), Body.class,
                () -> new Body("x"))).isInstanceOf(ApiException.class);
    }

    @Test
    void housekeepingRunsAgainstTheSchema() {
        housekeeping.cleanUp();
    }

    private static UserEntity user(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLocale("en-IN");
        return user;
    }

    private static AddressEntity address(UUID userId, boolean isDefault) {
        AddressEntity address = new AddressEntity();
        address.setUserId(userId);
        address.setLine1("1 MG Road");
        address.setCity("Bengaluru");
        address.setState("KA");
        address.setPostalCode("560001");
        address.setCountry("IN");
        address.setDefaultAddress(isDefault);
        return address;
    }

    private static ConsentLogEntity consent(UUID userId, String kind, boolean granted, String ip) {
        ConsentLogEntity consent = new ConsentLogEntity();
        consent.setUserId(userId);
        consent.setKind(kind);
        consent.setGranted(granted);
        consent.setIp(ip);
        return consent;
    }
}
