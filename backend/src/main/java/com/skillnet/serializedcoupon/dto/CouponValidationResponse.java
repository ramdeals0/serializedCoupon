package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.domain.ValidationReason;

import java.time.Instant;

public record CouponValidationResponse(
        String couponCode,
        boolean valid,
        ValidationReason validationReason,
        SerializedCouponStatus status,
        Instant startAt,
        Instant expiresAt,
        String rmsCouponId,
        Instant checkedAt
) {
}
