package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.CouponBatchStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CouponBatchResponse(
        UUID id,
        UUID couponId,
        String rmsCouponId,
        String rmsCouponCode,
        String rmsCouponName,
        String couponProgramCode,
        int requestedQuantity,
        int generatedQuantity,
        Instant startAt,
        Instant expiresAt,
        CouponBatchStatus status,
        String createdBy,
        String externalReference,
        Instant createdAt,
        Instant updatedAt,
        List<String> sampleCouponCodes
) {
}
