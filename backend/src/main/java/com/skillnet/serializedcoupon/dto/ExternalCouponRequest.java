package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.RedeemChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExternalCouponRequest(
        @NotBlank @Size(max = 14) String couponCode,
        @NotNull RedeemChannel channel,
        @Size(max = 64) String locationId,
        @Size(max = 128) String reference
) {
}
