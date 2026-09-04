package com.skillnet.serializedcoupon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.coupon.validation")
public class CouponValidationProperties {

    /**
     * When true, validation also requires the associated RMS coupon definition to be active.
     */
    private boolean requireLiveRmsActive = true;

    public boolean isRequireLiveRmsActive() {
        return requireLiveRmsActive;
    }

    public void setRequireLiveRmsActive(boolean requireLiveRmsActive) {
        this.requireLiveRmsActive = requireLiveRmsActive;
    }
}
