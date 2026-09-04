# API

Base path: `/api/v1`

Interactive spec: `http://localhost:8080/swagger-ui.html`

## RMS coupons

- `GET /rms-coupons?query=&active=&page=&size=`
- `GET /rms-coupons/{rmsCouponId}`

## Batches

- `POST /coupon-batches` (header `Idempotency-Key` optional)
- `GET /coupon-batches`
- `GET /coupon-batches/{batchId}`
- `GET /coupon-batches/{batchId}/coupons`
- `GET /coupon-batches/{batchId}/export` (`text/csv`, streamed)

`POST` returns `201` for a new batch and `200` for an idempotent replay. Responses include at most 25 sample codes.

## Serialized coupons

- `GET /serialized-coupons`
- `GET /serialized-coupons/{couponCode}`
- `POST /serialized-coupons/{couponCode}/validate`
- `POST /serialized-coupons/{couponCode}/deactivate`

## Other

- `GET /dashboard`
- `GET /meta/generation-config`

Errors use RFC 7807 `ProblemDetail` (`application/problem+json` where applicable).
