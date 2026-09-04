package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.CouponCodes;
import com.skillnet.serializedcoupon.validation.StartBeforeExpires;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@StartBeforeExpires
public record CreateCouponBatchRequest(
        @NotBlank @Size(max = 64) String rmsCouponId,
        @NotBlank @Pattern(regexp = CouponCodes.PROGRAM_CODE_REGEX, message = "couponProgramCode must be exactly 4 digits")
        String couponProgramCode,
        @NotNull @Positive Integer quantity,
        @NotNull Instant startAt,
        @NotNull Instant expiresAt,
        @Size(max = 128) String externalReference
) implements HasValidityWindow {
}
