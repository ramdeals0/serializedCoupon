package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.config.CouponValidationProperties;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.domain.ValidationReason;
import com.skillnet.serializedcoupon.dto.CouponValidationResponse;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponValidationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-15T12:00:00Z");
    private static final String CODE = "FF1234ABCD2345";

    @Mock
    private SerializedCouponRepository serializedCouponRepository;
    @Mock
    private RmsCouponClient rmsCouponClient;

    private CouponValidationProperties properties;
    private CouponValidationService service;

    @BeforeEach
    void setUp() {
        properties = new CouponValidationProperties();
        properties.setRequireLiveRmsActive(true);
        service = new CouponValidationService(
                serializedCouponRepository,
                rmsCouponClient,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void activeCouponWithinWindowIsValid() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.ACTIVE,
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                true);
        when(serializedCouponRepository.findByCouponCode(CODE)).thenReturn(Optional.of(coupon));
        when(rmsCouponClient.couponDefinitionExistsAndIsActive("RMS-COUPON-1001")).thenReturn(true);

        CouponValidationResponse response = service.validate(CODE);

        assertThat(response.valid()).isTrue();
        assertThat(response.validationReason()).isEqualTo(ValidationReason.VALID);
        assertThat(response.checkedAt()).isEqualTo(NOW);
    }

    @Test
    void futureCouponReturnsNotStarted() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.PENDING,
                Instant.parse("2026-10-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                true);
        assertThat(service.determineReason(coupon, NOW)).isEqualTo(ValidationReason.NOT_STARTED);
    }

    @Test
    void expiredCouponReturnsExpired() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-09-01T00:00:00Z"),
                true);
        assertThat(service.determineReason(coupon, NOW)).isEqualTo(ValidationReason.EXPIRED);
    }

    @Test
    void deactivatedCouponReturnsDeactivated() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.DEACTIVATED,
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                true);
        assertThat(service.determineReason(coupon, NOW)).isEqualTo(ValidationReason.DEACTIVATED);
    }

    @Test
    void redeemedCouponReturnsRedeemed() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.REDEEMED,
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                true);
        assertThat(service.determineReason(coupon, NOW)).isEqualTo(ValidationReason.REDEEMED);
    }

    @Test
    void inactiveRmsCouponReturnsRmsCouponInactive() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.ACTIVE,
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                true);
        when(rmsCouponClient.couponDefinitionExistsAndIsActive("RMS-COUPON-1001")).thenReturn(false);
        assertThat(service.determineReason(coupon, NOW)).isEqualTo(ValidationReason.RMS_COUPON_INACTIVE);
    }

    @Test
    void missingCouponReturnsNotFound() {
        when(serializedCouponRepository.findByCouponCode(CODE)).thenReturn(Optional.empty());
        CouponValidationResponse response = service.validate(CODE);
        assertThat(response.valid()).isFalse();
        assertThat(response.validationReason()).isEqualTo(ValidationReason.NOT_FOUND);
    }

    @Test
    void startAtBoundaryIsInclusive() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.ACTIVE, NOW, Instant.parse("2026-12-31T00:00:00Z"), true);
        when(rmsCouponClient.couponDefinitionExistsAndIsActive("RMS-COUPON-1001")).thenReturn(true);
        assertThat(service.determineReason(coupon, NOW)).isEqualTo(ValidationReason.VALID);
        assertThat(service.determineReason(coupon, NOW.minusNanos(1))).isEqualTo(ValidationReason.NOT_STARTED);
    }

    @Test
    void expiresAtBoundaryIsInclusive() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.ACTIVE, Instant.parse("2026-01-01T00:00:00Z"), NOW, true);
        when(rmsCouponClient.couponDefinitionExistsAndIsActive("RMS-COUPON-1001")).thenReturn(true);
        assertThat(service.determineReason(coupon, NOW)).isEqualTo(ValidationReason.VALID);
        assertThat(service.determineReason(coupon, NOW.plusNanos(1))).isEqualTo(ValidationReason.EXPIRED);
    }

    @Test
    void pendingStatusInsideWindowIsInvalidStatus() {
        SerializedCoupon coupon = coupon(SerializedCouponStatus.PENDING,
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                true);
        assertThat(service.determineReason(coupon, NOW)).isEqualTo(ValidationReason.INVALID_STATUS);
    }

    private SerializedCoupon coupon(SerializedCouponStatus status, Instant startAt, Instant expiresAt, boolean rmsActive) {
        RmsCouponDefinition rms = new RmsCouponDefinition();
        rms.setRmsCouponId("RMS-COUPON-1001");
        rms.setActive(rmsActive);
        SerializedCoupon coupon = new SerializedCoupon();
        coupon.setCouponCode(CODE);
        coupon.setStatus(status);
        coupon.setStartAt(startAt);
        coupon.setExpiresAt(expiresAt);
        coupon.setRmsCouponDefinition(rms);
        return coupon;
    }
}
