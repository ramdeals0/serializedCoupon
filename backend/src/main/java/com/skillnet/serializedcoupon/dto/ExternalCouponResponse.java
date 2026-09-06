package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.RedeemChannel;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.domain.ValidationReason;

import java.time.Instant;

public record ExternalCouponResponse(
        String couponCode,
        boolean accepted,
        boolean markedUsed,
        ValidationReason validationReason,
        SerializedCouponStatus status,
        RedeemChannel channel,
        int timesUsed,
        int usageLimit,
        Instant redeemedAt,
        Instant checkedAt
) {
}
