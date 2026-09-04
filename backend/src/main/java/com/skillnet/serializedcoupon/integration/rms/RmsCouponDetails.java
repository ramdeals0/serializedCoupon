package com.skillnet.serializedcoupon.integration.rms;

public record RmsCouponDetails(
        String rmsCouponId,
        String rmsCouponCode,
        String name,
        String description,
        boolean active
) {
}
