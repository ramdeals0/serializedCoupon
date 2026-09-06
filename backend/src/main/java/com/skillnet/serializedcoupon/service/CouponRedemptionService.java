package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.Coupon;
import com.skillnet.serializedcoupon.domain.CouponSource;
import com.skillnet.serializedcoupon.domain.RedeemChannel;
import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.domain.ValidationReason;
import com.skillnet.serializedcoupon.dto.ExternalCouponRequest;
import com.skillnet.serializedcoupon.dto.ExternalCouponResponse;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class CouponRedemptionService {

    private final SerializedCouponRepository serializedCouponRepository;
    private final CouponValidationService couponValidationService;
    private final Clock clock;

    public CouponRedemptionService(
            SerializedCouponRepository serializedCouponRepository,
            CouponValidationService couponValidationService,
            Clock clock
    ) {
        this.serializedCouponRepository = serializedCouponRepository;
        this.couponValidationService = couponValidationService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ExternalCouponResponse validate(ExternalCouponRequest request) {
        return process(request, false);
    }

    @Transactional
    public ExternalCouponResponse redeem(ExternalCouponRequest request) {
        return process(request, true);
    }

    private ExternalCouponResponse process(ExternalCouponRequest request, boolean markUsed) {
        Instant checkedAt = Instant.now(clock);
        String code = request.couponCode().trim();
        SerializedCoupon coupon = serializedCouponRepository.findByCouponCode(code).orElse(null);
        if (coupon == null) {
            return new ExternalCouponResponse(
                    code,
                    false,
                    false,
                    ValidationReason.NOT_FOUND,
                    null,
                    request.channel(),
                    0,
                    1,
                    null,
                    checkedAt
            );
        }
        int usageLimit = usageLimit(coupon);
        ValidationReason reason = channelReason(coupon, request.channel());
        if (reason == ValidationReason.VALID) {
            reason = couponValidationService.determineReason(coupon, checkedAt);
        }
        boolean accepted = reason == ValidationReason.VALID;
        boolean markedUsed = false;
        if (accepted && markUsed) {
            int used = coupon.getTimesUsed() + 1;
            coupon.setTimesUsed(used);
            if (used >= usageLimit) {
                coupon.setStatus(SerializedCouponStatus.REDEEMED);
                coupon.setRedeemedAt(checkedAt);
            }
            serializedCouponRepository.save(coupon);
            markedUsed = true;
        }
        return new ExternalCouponResponse(
                coupon.getCouponCode(),
                accepted,
                markedUsed,
                reason,
                coupon.getStatus(),
                request.channel(),
                coupon.getTimesUsed(),
                usageLimit,
                coupon.getRedeemedAt(),
                checkedAt
        );
    }

    private ValidationReason channelReason(SerializedCoupon coupon, RedeemChannel channel) {
        Coupon offer = coupon.getCouponBatch() == null ? null : coupon.getCouponBatch().getCoupon();
        CouponSource source = offer == null ? CouponSource.BOTH : offer.getCouponSource();
        if (source == CouponSource.BOTH || source == null) {
            return ValidationReason.VALID;
        }
        if (source == CouponSource.POS && channel != RedeemChannel.POS) {
            return ValidationReason.CHANNEL_NOT_ALLOWED;
        }
        if (source == CouponSource.ECOMM && channel != RedeemChannel.ECOMM) {
            return ValidationReason.CHANNEL_NOT_ALLOWED;
        }
        return ValidationReason.VALID;
    }

    private int usageLimit(SerializedCoupon coupon) {
        Coupon offer = coupon.getCouponBatch() == null ? null : coupon.getCouponBatch().getCoupon();
        if (offer == null || offer.getUsageLimit() < 1) {
            return 1;
        }
        return offer.getUsageLimit();
    }
}
