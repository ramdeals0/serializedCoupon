package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class RmsCouponNotFoundException extends ApiException {

    public RmsCouponNotFoundException(String rmsCouponId) {
        super(
                HttpStatus.NOT_FOUND,
                "RMS coupon not found",
                "https://api.serializedcoupon.local/problems/rms-coupon-not-found",
                "RMS coupon definition was not found: " + rmsCouponId
        );
    }
}
