# Architecture

Serialized coupons are always tied to an RMS coupon definition. The backend owns code generation, uniqueness, lifecycle, and validation. The Angular app is an operations console. Access is role-based (`ADMIN`, `MANAGER`, `CUSTOMER_SERVICE`) using JWT Bearer tokens.

## Domain

- `RmsCouponDefinition` — local copy of an RMS coupon (`rmsCouponId` unique)
- `Coupon` — offer definition (title, optional description, usage limit, 4-digit program code, POS/ATG codes, source, validity window)
- `CouponBatch` — generation request against an existing coupon, quantity, idempotency key, status
- `SerializedCoupon` — 14-character code, optimistic lock `version`, no physical delete of historical rows

`couponProgramCode` is the canonical name across API, database, UI, tests, and docs.

## Validity

Clock is injectable (`Clock.systemUTC()` in production).

- Inclusive start and inclusive expiration
- Redeemable only when status is `ACTIVE` and the window contains `now`
- Live RMS activity is checked when `app.coupon.validation.require-live-rms-active=true`

## Integration

`RmsCouponClient` is the only RMS boundary. Local/dev uses `MockRmsCouponClient`. Production replacement is `RestRmsCouponClient` behind `app.rms.client=rest`.
