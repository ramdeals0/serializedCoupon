package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.config.CouponValidationProperties;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.domain.ValidationReason;
import com.skillnet.serializedcoupon.dto.CouponValidationResponse;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponDetails;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class CouponValidationService {

    private final SerializedCouponRepository serializedCouponRepository;
    private final RmsCouponClient rmsCouponClient;
    private final CouponValidationProperties validationProperties;
    private final Clock clock;

    public CouponValidationService(
            SerializedCouponRepository serializedCouponRepository,
            RmsCouponClient rmsCouponClient,
            CouponValidationProperties validationProperties,
            Clock clock
    ) {
        this.serializedCouponRepository = serializedCouponRepository;
        this.rmsCouponClient = rmsCouponClient;
        this.validationProperties = validationProperties;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CouponValidationResponse validate(String couponCode) {
        Instant checkedAt = Instant.now(clock);
        return serializedCouponRepository.findByCouponCode(couponCode)
                .map(coupon -> evaluate(coupon, checkedAt))
                .orElseGet(() -> new CouponValidationResponse(
                        couponCode,
                        false,
                        ValidationReason.NOT_FOUND,
                        null,
                        null,
                        null,
                        null,
                        checkedAt
                ));
    }

    public CouponValidationResponse evaluate(SerializedCoupon coupon, Instant checkedAt) {
        ValidationReason reason = determineReason(coupon, checkedAt);
        return new CouponValidationResponse(
                coupon.getCouponCode(),
                reason == ValidationReason.VALID,
                reason,
                coupon.getStatus(),
                coupon.getStartAt(),
                coupon.getExpiresAt(),
                coupon.getRmsCouponDefinition() == null ? null : coupon.getRmsCouponDefinition().getRmsCouponId(),
                checkedAt
        );
    }

    public ValidationReason determineReason(SerializedCoupon coupon, Instant checkedAt) {
        SerializedCouponStatus status = coupon.getStatus();
        if (status == SerializedCouponStatus.REDEEMED) {
            return ValidationReason.REDEEMED;
        }
        if (status == SerializedCouponStatus.DEACTIVATED) {
            return ValidationReason.DEACTIVATED;
        }
        if (status == SerializedCouponStatus.CANCELLED) {
            return ValidationReason.CANCELLED;
        }
        if (status == SerializedCouponStatus.EXPIRED || checkedAt.isAfter(coupon.getExpiresAt())) {
            return ValidationReason.EXPIRED;
        }
        if (checkedAt.isBefore(coupon.getStartAt())) {
            return ValidationReason.NOT_STARTED;
        }
        if (status != SerializedCouponStatus.ACTIVE) {
            return ValidationReason.INVALID_STATUS;
        }
        if (!isRmsActive(coupon)) {
            return ValidationReason.RMS_COUPON_INACTIVE;
        }
        return ValidationReason.VALID;
    }

    private boolean isRmsActive(SerializedCoupon coupon) {
        RmsCouponDefinition local = coupon.getRmsCouponDefinition();
        if (local != null && !local.isActive()) {
            return false;
        }
        if (!validationProperties.isRequireLiveRmsActive()) {
            return true;
        }
        if (local == null || local.getRmsCouponId() == null) {
            return false;
        }
        return rmsCouponClient.getCouponDefinition(local.getRmsCouponId())
                .map(RmsCouponDetails::active)
                .orElse(true);
    }
}
