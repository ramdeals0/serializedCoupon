package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;

import java.time.Instant;
import java.util.UUID;

public record SerializedCouponResponse(
        UUID id,
        String couponCode,
        UUID batchId,
        String rmsCouponId,
        String rmsCouponCode,
        String couponProgramCode,
        Instant startAt,
        Instant expiresAt,
        SerializedCouponStatus status,
        Instant redeemedAt,
        Instant deactivatedAt,
        int timesUsed,
        int usageLimit,
        String externalReference,
        Instant createdAt,
        Instant updatedAt
) {
}
