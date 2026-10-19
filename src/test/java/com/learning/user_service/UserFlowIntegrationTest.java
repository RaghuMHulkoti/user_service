package com.learning.user_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flow against H2 (Oracle mode, built from the real migrations). Refresh tokens live in the database, so
 * no Redis is needed; the rate limiter and profile cache simply fail open.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserFlowIntegrationTest {

    private static final String PASSWORD = "Str0ng-Passw0rd!";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;

    private String email;

    @BeforeEach
    void setUp() {
        email = "it-" + UUID.randomUUID() + "@example.com";
    }

    @Test
    void registerVerifyLoginAndManageProfile() throws Exception {
        String key = UUID.randomUUID().toString();
        String body = """
                {"email":"%s","password":"%s","firstName":"Raghu","lastName":"V","locale":"en-IN"}
                """.formatted(email.toUpperCase(), PASSWORD);

        JsonNode registered = json(mvc.perform(post("/api/v1/users/register").header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.email").value(email))
                .andReturn());
        String userId = registered.get("userId").asString();

        // Same key, same body: stored response, no second user.
        mvc.perform(post("/api/v1/users/register").header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "true"))
                .andExpect(jsonPath("$.userId").value(userId));
        assertThat(jdbc.queryForObject("select count(*) from users where lower(email) = ?", Integer.class, email))
                .isEqualTo(1);

        // Verify email with the token carried by the outbox event.
        String token = outboxData(userId, "EMAIL_VERIFICATION_REQUESTED").get("verificationToken").asString();
        mvc.perform(post("/api/v1/users/verify-email").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isNoContent());

        JsonNode tokens = login(PASSWORD);
        String bearer = "Bearer " + tokens.get("accessToken").asString();

        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));

        mvc.perform(patch("/api/v1/users/me").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"lastName\":\"Verma\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Verma"));
        // The cached profile was evicted, so the next read sees the change.
        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer))
                .andExpect(jsonPath("$.lastName").value("Verma"));

        // Outbox rows are published by the poller.
        waitUntil(() -> jdbc.queryForObject("select count(*) from user_outbox where aggregate_id = ? "
                + "and status <> 'SENT'", Integer.class, (Object) rawUuid(userId)) == 0);
        assertThat(jdbc.queryForList("select event_type from user_outbox where aggregate_id = ?",
                String.class, (Object) rawUuid(userId))).contains("USER_REGISTERED", "EMAIL_VERIFICATION_REQUESTED", "USER_UPDATED");
    }

    @Test
    void secondDefaultAddressTakesOverFromFirst() throws Exception {
        String bearer = registerAndLogin();
        String address = """
                {"label":"%s","line1":"1 MG Road","city":"Bengaluru","state":"KA","postalCode":"560001","country":"IN"%s}
                """;
        JsonNode home = json(mvc.perform(post("/api/v1/users/me/addresses").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(address.formatted("Home", "")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isDefault").value(true))
                .andReturn());
        mvc.perform(post("/api/v1/users/me/addresses").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(address.formatted("Work", ",\"isDefault\":true")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isDefault").value(true));

        mvc.perform(get("/api/v1/users/me/addresses").header("Authorization", bearer))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].label").value("Home"))
                .andExpect(jsonPath("$[0].isDefault").value(false))
                .andExpect(jsonPath("$[1].isDefault").value(true));

        mvc.perform(delete("/api/v1/users/me/addresses/" + home.get("id").asString()).header("Authorization", bearer))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/users/me/addresses").header("Authorization", bearer))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void refreshTokenRotatesAndLogoutRevokes() throws Exception {
        registerAndLogin();
        JsonNode first = login(PASSWORD);
        String bearer = "Bearer " + first.get("accessToken").asString();
        String refresh = first.get("refreshToken").asString();

        JsonNode second = json(mvc.perform(post("/api/v1/users/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk()).andReturn());
        // Old refresh token works only once.
        mvc.perform(post("/api/v1/users/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));

        String newRefresh = second.get("refreshToken").asString();
        mvc.perform(post("/api/v1/users/logout").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + newRefresh + "\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/users/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + newRefresh + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordResetChangesPassword() throws Exception {
        registerAndLogin();
        mvc.perform(post("/api/v1/users/password/reset-request").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isAccepted());
        String token = jdbc.queryForList("select payload from user_outbox where event_type = 'PASSWORD_RESET_REQUESTED'",
                        String.class).stream()
                .map(payload -> jsonMapper.readTree(payload).get("data"))
                .filter(data -> email.equals(data.get("email").asString()))
                .findFirst().orElseThrow().get("resetToken").asString();

        mvc.perform(post("/api/v1/users/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"N3w-Passw0rd!!\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/v1/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
        login("N3w-Passw0rd!!");
    }

    @Test
    void securityErrorsUseTheStandardEnvelope() throws Exception {
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.error.traceId").exists());

        String bearer = registerAndLogin();
        mvc.perform(get("/api/v1/admin/users").header("Authorization", bearer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(get("/api/v1/internal/users/" + UUID.randomUUID()).header("Authorization", bearer))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalLookupNeedsServiceToken() throws Exception {
        registerAndLogin();
        byte[] id = jdbc.queryForObject("select id from users where email = ?", byte[].class, email);
        String userId = uuidOf(id).toString();
        mvc.perform(get("/api/v1/internal/users/" + userId).header("X-Internal-Token", "change-me-internal-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void jwksIsPublic() throws Exception {
        mvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].d").doesNotExist());
    }

    private String registerAndLogin() throws Exception {
        mvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\",\"firstName\":\"Test\"}"
                                .formatted(email, PASSWORD)))
                .andExpect(status().isCreated());
        return "Bearer " + login(PASSWORD).get("accessToken").asString();
    }

    private JsonNode login(String password) throws Exception {
        return json(mvc.perform(post("/api/v1/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn());
    }

    /** The {@code data} object of the newest outbox event of this type for the user. */
    private JsonNode outboxData(String userId, String eventType) {
        String payload = jdbc.queryForObject("select payload from user_outbox where aggregate_id = ? "
                + "and event_type = ?", String.class, rawUuid(userId), eventType);
        return jsonMapper.readTree(payload).get("data");
    }

    /** UUID columns are RAW(16), so ids are bound as their 16 bytes. */
    private static byte[] rawUuid(String uuid) {
        UUID id = UUID.fromString(uuid);
        return ByteBuffer.allocate(16).putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits()).array();
    }

    private static UUID uuidOf(byte[] raw) {
        ByteBuffer buffer = ByteBuffer.wrap(raw);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return jsonMapper.readTree(result.getResponse().getContentAsString());
    }

    private static void waitUntil(java.util.function.BooleanSupplier condition) throws InterruptedException {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (!condition.getAsBoolean()) {
            if (Instant.now().isAfter(deadline)) {
                throw new AssertionError("Condition not met within 10s");
            }
            Thread.sleep(100);
        }
    }
}
