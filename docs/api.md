# API

Base path: `/api/v1`

Interactive spec: `http://localhost:8080/swagger-ui.html`

Authenticate with `POST /auth/login`, then send `Authorization: Bearer <token>`.

## Auth

- `POST /auth/login` `{ "username", "password" }` → token, role, display name
- `GET /auth/me`

Roles: `ADMIN` (full), `MANAGER` (create + batch + search), `CUSTOMER_SERVICE` (coupon status lookup).

## RMS coupons

- `GET /rms-coupons?query=&active=&page=&size=`
- `GET /rms-coupons/{rmsCouponId}`

## Coupons

Create the coupon offer first, then generate serialized codes as a separate batch step.

- `POST /coupons` `{ "title", "description?", "usageLimit", "couponProgramCode", "posCode?", "atgCode?", "couponSource", "startAt", "expiresAt" }`
- `GET /coupons?rmsCouponId=&couponProgramCode=&status=&page=&size=`
- `GET /coupons/{couponId}`

`POST` returns `201` with the coupon id used by batch generation.

## Batches

- `POST /coupon-batches` `{ "couponId", "quantity", "externalReference?" }` (header `Idempotency-Key` optional)
- `GET /coupon-batches`
- `GET /coupon-batches/{batchId}`
- `GET /coupon-batches/{batchId}/coupons`
- `GET /coupon-batches/{batchId}/export` (`text/csv`, streamed; columns `couponCode,expiresAt`)

`POST` returns `201` for a new batch and `200` for an idempotent replay. Responses include at most 25 sample codes. Validity window and program code are copied from the coupon.

## Serialized coupons

- `GET /serialized-coupons`
- `GET /serialized-coupons/{couponCode}`
- `POST /serialized-coupons/{couponCode}/validate` (internal check-only; does not mark used)
- `POST /serialized-coupons/{couponCode}/deactivate`

## External POS / e-comm

These endpoints are for POS and e-commerce systems. Authenticate with `X-Api-Key` (demo key `pos-demo-key`) or a JWT from an operations user.

Mock demo UI: `http://127.0.0.1:4200/pos-demo` (production: `{origin}/pos-demo`)

- `POST /external/coupons/validate` — check a serialized coupon without changing it
- `POST /external/coupons/redeem` — validate and mark the coupon used

Request:

```json
{
  "couponCode": "FF1234ABCD2345",
  "channel": "POS",
  "locationId": "STORE-1",
  "reference": "TXN-1001"
}
```

`channel` is `POS` or `ECOMM`. A coupon whose source is `POS` cannot be redeemed on `ECOMM`, and vice versa. `BOTH` accepts either channel.

Response (`200` for both accepted and rejected business outcomes):

```json
{
  "couponCode": "FF1234ABCD2345",
  "accepted": true,
  "markedUsed": true,
  "validationReason": "VALID",
  "status": "REDEEMED",
  "channel": "POS",
  "timesUsed": 1,
  "usageLimit": 1,
  "redeemedAt": "2026-09-06T12:00:00Z",
  "checkedAt": "2026-09-06T12:00:00Z"
}
```

`validate` always returns `markedUsed: false`. `redeem` sets `markedUsed: true` only when `accepted` is true. When `timesUsed` reaches `usageLimit`, status becomes `REDEEMED`. Missing or invalid API keys return `401`.

## Other

- `GET /dashboard`
- `GET /meta/generation-config`

Errors use RFC 7807 `ProblemDetail` (`application/problem+json` where applicable).
