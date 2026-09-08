package com.skillnet.serializedcoupon.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCouponBatchRequest(
        @NotNull UUID couponId,
        @NotNull @Positive Integer quantity,
        @Size(max = 128) String externalReference
) {
}
