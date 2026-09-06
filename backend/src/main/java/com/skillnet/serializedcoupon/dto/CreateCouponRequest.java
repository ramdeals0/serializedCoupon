package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.CouponCodes;
import com.skillnet.serializedcoupon.domain.CouponSource;
import com.skillnet.serializedcoupon.validation.StartBeforeExpires;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@StartBeforeExpires
public record CreateCouponRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        @NotNull @Min(1) @Max(10000) Integer usageLimit,
        @NotBlank @Pattern(regexp = CouponCodes.PROGRAM_CODE_REGEX, message = "couponProgramCode must be exactly 4 digits")
        String couponProgramCode,
        @Size(max = 64) String posCode,
        @Size(max = 64) String atgCode,
        @NotNull CouponSource couponSource,
        @NotNull Instant startAt,
        @NotNull Instant expiresAt
) implements HasValidityWindow {
}
