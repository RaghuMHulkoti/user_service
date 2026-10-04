# User Service

The Identity & Access service of "Project Bazaar" (see `marketplace_tdd.md`, sections 8.2, 9.1, 11.2, 12.1–12.2, 13, 15, 16).

It handles accounts, login, profile, addresses, saved payment methods (PSP tokens only), consent, and roles.

## Run it

You need Java 25, PostgreSQL and Redis running on your machine.

```bash
createdb users_db             # once
brew services start redis     # if Redis is not running
./mvnw spring-boot:run
```

- The app runs on http://localhost:8081.
- Swagger UI: http://localhost:8081/swagger-ui.html
- Flyway creates the tables when the app starts.

The default profile is `dev`. In `dev`, the raw email-verification and password-reset tokens are printed to the log, so you can test those flows without an email service:

```
[DEV] Email verification token for user 7a14...: Gk3x...
```

### Settings (environment variables)

| Variable | Default | What it does |
|---|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/users_db` / `postgres` / `1234` | Database |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Refresh tokens, rate limits, profile cache |
| `EVENTS_PUBLISHER` | `log` | `log` prints events to the log. `kafka` sends them to Kafka. |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Used only when `EVENTS_PUBLISHER=kafka` |
| `JWT_PRIVATE_KEY_LOCATION` / `JWT_PUBLIC_KEY_LOCATION` | empty | PEM files for the RS256 key pair. If you leave them empty, the app makes a new key at each start, and old access tokens stop working. |
| `INTERNAL_SERVICE_TOKEN` | `change-me-internal-token` | Shared secret for `/api/v1/internal/**` |

To make a key pair:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -in private.pem -pubout -out public.pem
JWT_PRIVATE_KEY_LOCATION=file:private.pem JWT_PUBLIC_KEY_LOCATION=file:public.pem ./mvnw spring-boot:run
```

## API

All errors use one shape: `{ "error": { "code", "message", "details", "traceId" } }`.

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/v1/users/register` | public | Create account (accepts `Idempotency-Key`) |
| POST | `/api/v1/users/verify-email` | public | Confirm email with the token |
| POST | `/api/v1/users/verify-email/resend` | bearer | Send a new verification email |
| POST | `/api/v1/users/login` | public | Get access token (15 min) + refresh token (30 days) |
| POST | `/api/v1/users/refresh` | refresh token | New token pair. The old refresh token stops working. |
| POST | `/api/v1/users/logout` | bearer | Revoke one refresh token, or all with `"allSessions": true` |
| POST | `/api/v1/users/password/reset-request` | public | Send reset email. Always returns 202. |
| POST | `/api/v1/users/password/reset` | one-time token | Set new password. Signs out all sessions. |
| GET / PATCH / DELETE | `/api/v1/users/me` | bearer | Read, update, or delete the profile |
| POST | `/api/v1/users/me/password` | bearer | Change password |
| GET / POST | `/api/v1/users/me/addresses` | bearer | List / add (accepts `Idempotency-Key`) |
| PATCH / DELETE | `/api/v1/users/me/addresses/{id}` | bearer | Update / remove |
| GET / POST | `/api/v1/users/me/payment-methods` | bearer | List / add a PSP token (accepts `Idempotency-Key`) |
| POST | `/api/v1/users/me/payment-methods/{id}/default` | bearer | Make it the default |
| DELETE | `/api/v1/users/me/payment-methods/{id}` | bearer | Remove |
| GET / POST | `/api/v1/users/me/consents` | bearer | Current consent / record a decision |
| GET | `/api/v1/users/me/consents/history` | bearer | Every consent decision |
| GET | `/api/v1/internal/users/{id}` | `X-Internal-Token` | Lookup for other services |
| GET | `/api/v1/admin/users?status=&page=&size=` | ADMIN, OPS | List users |
| PUT | `/api/v1/admin/users/{id}/roles` | ADMIN | Replace roles |
| POST | `/api/v1/admin/users/{id}/suspend` · `/reactivate` | ADMIN, OPS | Suspend / reactivate |
| GET | `/.well-known/jwks.json` | public | Public keys, for the gateway |

New accounts get the `CUSTOMER` role. To make the first admin, add the role in SQL:

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'you@example.com' AND r.code = 'ADMIN';
```

Then log in again, so the new access token has the role.

### Quick try

```bash
curl -X POST localhost:8081/api/v1/users/register -H 'Content-Type: application/json' \
  -d '{"email":"raghu@example.com","password":"Str0ng-Passw0rd!","firstName":"Raghu"}'
# copy the token from the [DEV] log line
curl -X POST localhost:8081/api/v1/users/verify-email -H 'Content-Type: application/json' -d '{"token":"<token>"}'
curl -X POST localhost:8081/api/v1/users/login -H 'Content-Type: application/json' \
  -d '{"email":"raghu@example.com","password":"Str0ng-Passw0rd!"}'
curl localhost:8081/api/v1/users/me -H 'Authorization: Bearer <accessToken>'
```

## How it works

- **Passwords.** Hashed with BCrypt. Rule: at least 12 characters, with at least 3 of: lower case, upper case, digit, symbol.
- **Login protection.** At most 5 tries per minute per IP and 10 per hour per account (Redis counters). After 5 wrong passwords in a row, the account locks for 15 minutes.
- **Tokens.** The access token is an RS256 JWT with `sub`, `roles`, `tenantId`, `jti`, `iat` and `exp`. The refresh token looks like `<jti>.<secret>`. Redis keeps only a SHA-256 hash of the secret, under `session:refresh:{jti}`.
- **One-time tokens** (email verification, password reset). The DB stores only their SHA-256 hash.
- **Events.** Every change writes an event row to `user_outbox` in the same transaction. A poller sends pending rows every second and marks them `SENT`. If a send fails (for example, Kafka is down), the row stays `PENDING` and is retried on the next poll. Only an event Kafka will never accept (too large, bad topic) is marked `FAILED`. Topic: `domain.user.events`, keyed by user id. Event types: `USER_REGISTERED`, `USER_UPDATED`, `USER_DELETED`, `ADDRESS_UPDATED`, `EMAIL_VERIFICATION_REQUESTED`, `PASSWORD_RESET_REQUESTED`, `PASSWORD_CHANGED`.
- **Profile cache.** The cache key is `user:{id}:profile`, with a 5-minute TTL. It is cleared after each change commits. If Redis is down, the app reads from the DB.
- **Idempotency.** If you send the same `Idempotency-Key` with the same body, you get the first response back, with the header `Idempotent-Replayed: true`. The same key with a different body returns 422. Keys are kept for 24 hours.
- **Account delete.** This removes personal data (email, phone, names, addresses, payment tokens and the consent IP). It also signs the user out and sends `USER_DELETED`, so other services can delete their copies.

Code layout follows TDD §19: `domain` (pure Java: enums, events, ports, password policy), `application` (use cases), `infrastructure` (JPA, Redis, Kafka, security), `presentation` (REST controllers, error handling).

## Tests

```bash
createdb users_test
./mvnw test
```

- Unit tests have no dependencies.
- Integration tests use the local `users_test` database and Redis. They are skipped when Postgres or Redis is not running. There are no Testcontainers tests, because Docker is not required.

## Not built (outside this service, or needs infrastructure)

- OAuth2 Authorization Code + PKCE. That needs a separate authorization server. This service issues tokens from `/login`.
- mTLS between services. A shared `X-Internal-Token` stands in for it.
- CAPTCHA, breached-password check, and the DNS MX check on email.
- Vault / KMS secrets and pgcrypto column encryption.
- The email itself. The notification service sends it, using the outbox events.

The verification and reset events carry the raw one-time token, because the notification service needs it to build the link. Sent outbox rows are deleted after 7 days.
