# Serialized Coupon Application

Angular + Spring Boot application for generating and managing serialized coupons that are linked to RMS coupon definitions.

## Architecture overview

The repository is a monorepo:

- `backend/` — Spring Boot 3.5 API (Java 17), JPA, Flyway, RFC 7807 problem details
- `frontend/` — Angular 22 standalone UI
- `docs/` — architecture and API notes
- `docker-compose.yml` — PostgreSQL 16 for production-like local runs

Layering on the backend:

`controller → service → repository / RMS client`

JPA entities are never returned from REST endpoints. API responses use immutable DTO records.

```
Browser (Angular)
    │  HTTP /api/v1
    ▼
Spring controllers
    │
    ├── CouponService (offer definition)
    ├── CouponBatchService (generation, idempotency)
    ├── CouponValidationService (Clock-based validity)
    ├── SerializedCouponService (search, deactivate)
    └── RmsCouponClient
            ├── MockRmsCouponClient (local/dev)
            └── RestRmsCouponClient (future RMS HTTP adapter)
    │
    ▼
PostgreSQL (production) or H2 (local/test)
```

## Local development prerequisites

- JDK 17 (this workspace uses Temurin 17; Java 21 is preferred when available)
- Node.js 22 and npm
- Maven Wrapper is included (`backend/mvnw` / `backend/mvnw.cmd`); a global Maven install is not required
- Optional: Docker, for PostgreSQL via Compose
- Optional: a local PostgreSQL 16 instance if you do not use Docker or H2

This development machine does **not** have Docker or PostgreSQL installed. The default `local` profile therefore uses H2 so the API can run without extra services.

## Backend setup

```bash
cd backend
./mvnw test          # Windows: mvnw.cmd test
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`.

Useful endpoints:

- OpenAPI UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
- H2 console (local profile only): `http://localhost:8080/h2-console`

## Frontend setup

If `npm install` fails with an Arborist `edgesOut` error, retry with `npm install --legacy-peer-deps`. The frontend `.npmrc` enables that flag by default.

The UI is served at `http://localhost:4200` and calls `http://localhost:8080/api/v1`.

```bash
npm test
npm run build
```

## Database setup

### H2 (default local profile)

No extra setup. Data is stored in `backend/data/serialized-coupon.mv.db`.

### PostgreSQL (recommended for production-like runs)

If Docker is available:

```bash
docker compose up -d
cd backend
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="postgres"
./mvnw.cmd spring-boot:run
```

Flyway applies `backend/src/main/resources/db/migration/V1__init.sql` on the `postgres` profile.

### Test database

Backend tests use in-memory H2 (`application-test.yml`) with Hibernate `ddl-auto: create-drop` and Flyway disabled. Differences versus PostgreSQL:

- Check constraints and PostgreSQL regex (`~`) from Flyway are **not** exercised in H2 tests
- Unique indexes, foreign keys, and application-level validation **are** exercised
- Use the `postgres` profile against a real database before production

Testcontainers was not enabled because Docker is not available in this environment.

## Environment variables

See `.env.example`. Important variables:

| Variable | Purpose | Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `local` or `postgres` | `local` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated browser origins | `http://localhost:4200,http://127.0.0.1:4200` |
| `JWT_SECRET` | HMAC secret for access tokens | local development default |
| `AUTH_ADMIN_PASSWORD` / `AUTH_MANAGER_PASSWORD` / `AUTH_CSR_PASSWORD` | Seeded account passwords | `Admin123!` / `Manager123!` / `Csr123!` |
| `POSTGRES_HOST` / `POSTGRES_PORT` / `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | PostgreSQL connection | `localhost` / `5432` / `serialized_coupon` / `coupon` / `coupon` |
| `RMS_CLIENT` | `mock` or `rest` | `mock` |
| `RMS_BASE_URL` | Required when `RMS_CLIENT=rest` | empty |
| `RMS_API_KEY` | Bearer token for RMS HTTP adapter | empty |
| `COUPON_MAX_BATCH_SIZE` | Max coupons per batch | `10000` |
| `COUPON_COLLISION_RETRY_LIMIT` | Per-code generation retries | `20` |
| `COUPON_REQUIRE_LIVE_RMS` | Validate RMS still active at redemption check | `true` |
| `EXTERNAL_API_KEY` | API key for POS/e-comm validate and redeem | `pos-demo-key` |

Do not commit real credentials.

## Local mock RMS behavior

`MockRmsCouponClient` is the default. It seeds four definitions into the local catalog at startup:

| RMS coupon ID | Active | Notes |
| --- | --- | --- |
| `RMS-COUPON-1001` | yes | Fall 2026 BOGO (`FALL26`) |
| `RMS-COUPON-1002` | yes | Save $10 (`SAVE10`) |
| `RMS-COUPON-1003` | yes | VIP 25 percent (`VIP25`) |
| `RMS-COUPON-INACTIVE` | no | Used for negative tests |

Batch creation refuses missing or inactive RMS coupons.

## API examples

Create a coupon, then generate a batch:

```bash
COUPON_ID=$(curl -s -X POST http://localhost:8080/api/v1/coupons \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Fall 2026 BOGO\",
    \"description\": \"Buy one get one serialized coupon program\",
    \"usageLimit\": 1,
    \"couponProgramCode\": \"1234\",
    \"posCode\": \"RMS-COUPON-1001\",
    \"atgCode\": \"FALL26-ATG\",
    \"couponSource\": \"BOTH\",
    \"startAt\": \"2026-09-10T00:00:00Z\",
    \"expiresAt\": \"2026-12-31T23:59:59Z\"
  }" | jq -r .id)

curl -s -X POST http://localhost:8080/api/v1/coupon-batches \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d "{
    \"couponId\": \"$COUPON_ID\",
    \"quantity\": 5
  }"
```

Validate a coupon (check-only, operations JWT):

```bash
curl -s -X POST http://localhost:8080/api/v1/serialized-coupons/FF1234ABCD2345/validate \
  -H "Authorization: Bearer $TOKEN"
```

External POS / e-comm validate and redeem (API key, no JWT). Mock UI: `http://127.0.0.1:4200/pos-demo`

```bash
curl -s -X POST http://localhost:8080/api/v1/external/coupons/validate \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: pos-demo-key" \
  -d '{"couponCode":"FF1234ABCD2345","channel":"POS","locationId":"STORE-1"}'

curl -s -X POST http://localhost:8080/api/v1/external/coupons/redeem \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: pos-demo-key" \
  -d '{"couponCode":"FF1234ABCD2345","channel":"POS","locationId":"STORE-1","reference":"TXN-1001"}'
```

Export a batch as CSV (serialized coupon code and expiration date only):

```bash
curl -s -D - "http://localhost:8080/api/v1/coupon-batches/{batchId}/export" \
  -H "Authorization: Bearer $TOKEN" \
  -o coupons.csv
```

## Coupon format

- Prefix: `FF`
- Program code: exactly 4 digits (`couponProgramCode`, `0000`–`9999`)
- Random suffix: 8 characters from `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` (excludes `I`, `O`, `0`, `1`)
- Total length: 14
- Regex: `^FF[0-9]{4}[A-Z0-9]{8}$`

Examples: `FF0000A1B2C3D4`, `FF1234ZX98QW76`, `FF9999AB12CD34`

The 4-digit segment may still contain `0`–`9`. The frontend never generates codes; it only displays the format.

## Date/time rules

- Persistence and API timestamps are ISO-8601 UTC (`timestamptz` / `Instant`)
- `startAt` is inclusive
- `expiresAt` is inclusive
- A coupon is redeemable only when `status == ACTIVE`, `now >= startAt`, `now <= expiresAt`, it is not redeemed/deactivated/cancelled, and the RMS definition is active (when live RMS checks are enabled)
- Future-dated coupons are stored as `PENDING`
- Validation does **not** persist `EXPIRED`; expiration is computed from the clock
- The UI shows dates in the browser locale

## How batch generation avoids duplicates

1. `CouponCodeGenerator` uses `SecureRandom` and a reduced suffix alphabet
2. An in-memory `Set` avoids duplicates inside a single request
3. `existsByCouponCode` is checked before insert, with a configurable retry limit (default 20)
4. The database unique constraint on `serialized_coupon.coupon_code` is the final authority
5. Generation is all-or-nothing: coupon inserts run in one transaction after the batch row exists. Exhausted retries mark the batch `FAILED` and roll back generated rows from that transaction
6. Optional `Idempotency-Key` is stored on `coupon_batch`. The same key and same request fingerprint return the prior batch (`200`). A reused key with a different body returns `409`

## Replacing MockRmsCouponClient

1. Set `RMS_CLIENT=rest` and `RMS_BASE_URL` (and `RMS_API_KEY` if required)
2. Implement or adapt `RestRmsCouponClient` to the real RMS HTTP contract. The placeholder currently calls `GET {baseUrl}/coupon-definitions/{rmsCouponId}`
3. Keep credentials in environment variables only
4. Leave `RmsCouponClient` as the only integration surface used by batch creation and validation

## Authentication

JWT login is required for operations `/api/v1` calls except `POST /api/v1/auth/login`. Send `Authorization: Bearer <token>`.

POS and e-comm systems can call `POST /api/v1/external/coupons/validate` and `POST /api/v1/external/coupons/redeem` with `X-Api-Key` instead of a user JWT. The local demo key is `pos-demo-key`. The mock page is `http://127.0.0.1:4200/pos-demo`.

| Role | Access |
| --- | --- |
| `ADMIN` | Full access: dashboard, batches, create, search, validate, deactivate |
| `MANAGER` | Create coupons, generate batches, view batches, search/validate serialized coupons |
| `CUSTOMER_SERVICE` | Search and view serialized coupon status (including validate) |

Seeded local/demo accounts (override with env vars):

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `Admin123!` | Admin |
| `manager` | `Manager123!` | Manager |
| `csr` | `Csr123!` | Customer service |

Change these passwords in production through `AUTH_ADMIN_PASSWORD`, `AUTH_MANAGER_PASSWORD`, `AUTH_CSR_PASSWORD`, and set a unique `JWT_SECRET`. CORS remains configurable. Batch `createdBy` is set from the authenticated username.

## Known limitations and future work

- Password reset / self-service user management
- Async queue processing for very large batches
- Real RMS API integration
- POS/RMS redemption synchronization beyond the external validate/redeem APIs
- Request correlation IDs / audit event stream
- Testcontainers PostgreSQL once Docker is available
- Spring Boot 3.5.x is the requested 3.x line; 3.5 OSS patches ended mid-2026, so plan a Boot 4 upgrade for long-term support
