package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.CouponSource;
import com.skillnet.serializedcoupon.domain.CouponStatus;

import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String title,
        String description,
        int usageLimit,
        String posCode,
        String atgCode,
        CouponSource couponSource,
        String rmsCouponId,
        String rmsCouponCode,
        String rmsCouponName,
        String couponProgramCode,
        Instant startAt,
        Instant expiresAt,
        CouponStatus status,
        String createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
