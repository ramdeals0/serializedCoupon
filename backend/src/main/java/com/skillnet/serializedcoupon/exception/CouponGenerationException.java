package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class CouponGenerationException extends ApiException {

    public CouponGenerationException(String message) {
        super(
                HttpStatus.CONFLICT,
                "Coupon generation failed",
                "https://api.serializedcoupon.local/problems/coupon-generation-failed",
                message
        );
    }
}
