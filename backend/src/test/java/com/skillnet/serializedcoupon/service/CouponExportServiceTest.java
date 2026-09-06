package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.repository.CouponBatchRepository;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponExportServiceTest {

    @Mock
    private CouponBatchRepository couponBatchRepository;
    @Mock
    private SerializedCouponRepository serializedCouponRepository;

    private CouponExportService service;

    @BeforeEach
    void setUp() {
        service = new CouponExportService(couponBatchRepository, serializedCouponRepository);
    }

    @Test
    void writesOnlySerializedCouponAndExpiration() {
        UUID batchId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        SerializedCoupon coupon = new SerializedCoupon();
        coupon.setCouponCode("FF1234ABCD2345");
        coupon.setExpiresAt(Instant.parse("2026-12-31T23:59:59Z"));
        when(couponBatchRepository.existsById(batchId)).thenReturn(true);
        when(serializedCouponRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(coupon)));

        StringWriter writer = new StringWriter();
        service.writeBatchCsv(batchId, writer);

        assertThat(writer.toString()).isEqualTo(
                "couponCode,expiresAt\nFF1234ABCD2345,2026-12-31T23:59:59Z\n"
        );
    }
}
