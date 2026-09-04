package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class RmsCouponInactiveException extends ApiException {

    public RmsCouponInactiveException(String rmsCouponId) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "RMS coupon inactive",
                "https://api.serializedcoupon.local/problems/rms-coupon-inactive",
                "RMS coupon definition is inactive: " + rmsCouponId
        );
    }
}
