package com.skillnet.serializedcoupon.dto;

import java.time.Instant;
import java.util.UUID;

public record RmsCouponDefinitionResponse(
        UUID id,
        String rmsCouponId,
        String rmsCouponCode,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
