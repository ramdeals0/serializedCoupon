package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.config.CouponValidationProperties;
import com.skillnet.serializedcoupon.domain.Coupon;
import com.skillnet.serializedcoupon.domain.CouponBatch;
import com.skillnet.serializedcoupon.domain.CouponSource;
import com.skillnet.serializedcoupon.domain.RedeemChannel;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.domain.ValidationReason;
import com.skillnet.serializedcoupon.dto.ExternalCouponRequest;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponDetails;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponRedemptionServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-15T12:00:00Z");
    private static final String CODE = "FF1234ABCD2345";

    @Mock
    private SerializedCouponRepository serializedCouponRepository;
    @Mock
    private RmsCouponClient rmsCouponClient;

    private CouponRedemptionService service;

    @BeforeEach
    void setUp() {
        CouponValidationProperties properties = new CouponValidationProperties();
        properties.setRequireLiveRmsActive(true);
        CouponValidationService validationService = new CouponValidationService(
                serializedCouponRepository,
                rmsCouponClient,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        service = new CouponRedemptionService(
                serializedCouponRepository,
                validationService,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void validateDoesNotMarkUsed() {
        SerializedCoupon coupon = activeCoupon(CouponSource.BOTH, 1);
        when(serializedCouponRepository.findByCouponCode(CODE)).thenReturn(Optional.of(coupon));
        stubLiveRmsActive();

        var response = service.validate(new ExternalCouponRequest(CODE, RedeemChannel.POS, "STORE-1", null));

        assertThat(response.accepted()).isTrue();
        assertThat(response.markedUsed()).isFalse();
        assertThat(response.timesUsed()).isZero();
        verify(serializedCouponRepository, never()).save(any());
    }

    @Test
    void redeemMarksCouponUsed() {
        SerializedCoupon coupon = activeCoupon(CouponSource.BOTH, 1);
        when(serializedCouponRepository.findByCouponCode(CODE)).thenReturn(Optional.of(coupon));
        stubLiveRmsActive();
        when(serializedCouponRepository.save(coupon)).thenReturn(coupon);

        var response = service.redeem(new ExternalCouponRequest(CODE, RedeemChannel.ECOMM, null, "ORDER-9"));

        assertThat(response.accepted()).isTrue();
        assertThat(response.markedUsed()).isTrue();
        assertThat(response.status()).isEqualTo(SerializedCouponStatus.REDEEMED);
        assertThat(coupon.getTimesUsed()).isEqualTo(1);
        assertThat(coupon.getRedeemedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsEcommChannelForPosOnlyCoupon() {
        SerializedCoupon coupon = activeCoupon(CouponSource.POS, 1);
        when(serializedCouponRepository.findByCouponCode(CODE)).thenReturn(Optional.of(coupon));

        var response = service.redeem(new ExternalCouponRequest(CODE, RedeemChannel.ECOMM, null, null));

        assertThat(response.accepted()).isFalse();
        assertThat(response.markedUsed()).isFalse();
        assertThat(response.validationReason()).isEqualTo(ValidationReason.CHANNEL_NOT_ALLOWED);
        verify(serializedCouponRepository, never()).save(any());
    }

    private void stubLiveRmsActive() {
        when(rmsCouponClient.getCouponDefinition("RMS-COUPON-1001")).thenReturn(Optional.of(
                new RmsCouponDetails("RMS-COUPON-1001", "FALL26", "Fall 2026 BOGO", null, true)
        ));
    }

    private SerializedCoupon activeCoupon(CouponSource source, int usageLimit) {
        RmsCouponDefinition rms = new RmsCouponDefinition();
        rms.setRmsCouponId("RMS-COUPON-1001");
        rms.setActive(true);
        Coupon offer = new Coupon();
        offer.setCouponSource(source);
        offer.setUsageLimit(usageLimit);
        CouponBatch batch = new CouponBatch();
        batch.setCoupon(offer);
        SerializedCoupon coupon = new SerializedCoupon();
        coupon.setCouponCode(CODE);
        coupon.setStatus(SerializedCouponStatus.ACTIVE);
        coupon.setStartAt(Instant.parse("2026-09-01T00:00:00Z"));
        coupon.setExpiresAt(Instant.parse("2026-12-31T23:59:59Z"));
        coupon.setRmsCouponDefinition(rms);
        coupon.setCouponBatch(batch);
        coupon.setTimesUsed(0);
        return coupon;
    }
}
