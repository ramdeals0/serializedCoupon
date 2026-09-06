package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.Coupon;
import com.skillnet.serializedcoupon.domain.CouponSource;
import com.skillnet.serializedcoupon.domain.CouponStatus;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.dto.CreateCouponRequest;
import com.skillnet.serializedcoupon.exception.BusinessValidationException;
import com.skillnet.serializedcoupon.exception.RmsCouponInactiveException;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponDetails;
import com.skillnet.serializedcoupon.mapper.CouponMapper;
import com.skillnet.serializedcoupon.repository.CouponRepository;
import com.skillnet.serializedcoupon.repository.RmsCouponDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-04T15:00:00Z");

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private RmsCouponDefinitionRepository rmsCouponDefinitionRepository;
    @Mock
    private RmsCouponClient rmsCouponClient;

    private CouponService service;

    @BeforeEach
    void setUp() {
        service = new CouponService(
                couponRepository,
                rmsCouponDefinitionRepository,
                rmsCouponClient,
                new CouponMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void createsCouponFromOfferFields() {
        when(rmsCouponClient.getCouponDefinition("RMS-COUPON-1001")).thenReturn(Optional.of(
                new RmsCouponDetails("RMS-COUPON-1001", "FALL26", "Fall 2026 BOGO", "desc", true)
        ));
        when(rmsCouponDefinitionRepository.findByRmsCouponId("RMS-COUPON-1001")).thenReturn(Optional.empty());
        when(rmsCouponDefinitionRepository.save(any())).thenAnswer(invocation -> {
            RmsCouponDefinition definition = invocation.getArgument(0);
            definition.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
            return definition;
        });
        when(couponRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Coupon coupon = invocation.getArgument(0);
            coupon.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
            return coupon;
        });

        var response = service.create(validRequest());

        assertThat(response.title()).isEqualTo("Fall 2026 BOGO");
        assertThat(response.usageLimit()).isEqualTo(1);
        assertThat(response.posCode()).isEqualTo("RMS-COUPON-1001");
        assertThat(response.atgCode()).isEqualTo("FALL26-ATG");
        assertThat(response.couponSource()).isEqualTo(CouponSource.BOTH);
        assertThat(response.couponProgramCode()).isEqualTo("1234");
        assertThat(response.status()).isEqualTo(CouponStatus.ACTIVE);
    }

    @Test
    void rejectsMissingPosCodeForPosSource() {
        assertThatThrownBy(() -> service.create(new CreateCouponRequest(
                "Fall 2026 BOGO",
                null,
                1,
                "1234",
                null,
                "ATG-1",
                CouponSource.POS,
                Instant.parse("2026-09-10T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z")
        ))).isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("POS code");
    }

    @Test
    void rejectsInactiveRmsCoupon() {
        when(rmsCouponClient.getCouponDefinition("RMS-COUPON-INACTIVE")).thenReturn(Optional.of(
                new RmsCouponDetails("RMS-COUPON-INACTIVE", "OLD99", "Retired", null, false)
        ));
        assertThatThrownBy(() -> service.create(new CreateCouponRequest(
                "Retired",
                null,
                1,
                "1234",
                "RMS-COUPON-INACTIVE",
                null,
                CouponSource.POS,
                Instant.parse("2026-09-10T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z")
        ))).isInstanceOf(RmsCouponInactiveException.class);
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThatThrownBy(() -> service.create(new CreateCouponRequest(
                "Fall 2026 BOGO",
                null,
                1,
                "1234",
                "RMS-COUPON-1001",
                "FALL26-ATG",
                CouponSource.BOTH,
                Instant.parse("2026-12-31T00:00:00Z"),
                Instant.parse("2026-09-10T00:00:00Z")
        ))).isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("startAt");
    }

    private CreateCouponRequest validRequest() {
        return new CreateCouponRequest(
                "Fall 2026 BOGO",
                "Buy one get one",
                1,
                "1234",
                "RMS-COUPON-1001",
                "FALL26-ATG",
                CouponSource.BOTH,
                Instant.parse("2026-09-10T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z")
        );
    }
}
