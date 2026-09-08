package com.skillnet.serializedcoupon.domain;

public enum ValidationReason {
    VALID,
    NOT_FOUND,
    NOT_STARTED,
    EXPIRED,
    REDEEMED,
    DEACTIVATED,
    CANCELLED,
    RMS_COUPON_INACTIVE,
    INVALID_STATUS,
    CHANNEL_NOT_ALLOWED
}
