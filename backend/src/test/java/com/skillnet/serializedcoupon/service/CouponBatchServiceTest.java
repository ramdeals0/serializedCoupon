package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.config.CouponGenerationProperties;
import com.skillnet.serializedcoupon.domain.CouponBatch;
import com.skillnet.serializedcoupon.domain.CouponBatchStatus;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.dto.CreateCouponBatchRequest;
import com.skillnet.serializedcoupon.exception.BusinessValidationException;
import com.skillnet.serializedcoupon.exception.CouponGenerationException;
import com.skillnet.serializedcoupon.exception.RmsCouponInactiveException;
import com.skillnet.serializedcoupon.exception.RmsCouponNotFoundException;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponDetails;
import com.skillnet.serializedcoupon.mapper.CouponMapper;
import com.skillnet.serializedcoupon.repository.CouponBatchRepository;
import com.skillnet.serializedcoupon.repository.RmsCouponDefinitionRepository;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponBatchServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-04T15:00:00Z");

    @Mock
    private CouponBatchRepository couponBatchRepository;
    @Mock
    private SerializedCouponRepository serializedCouponRepository;
    @Mock
    private RmsCouponDefinitionRepository rmsCouponDefinitionRepository;
    @Mock
    private RmsCouponClient rmsCouponClient;
    @Mock
    private CouponCodeGenerator couponCodeGenerator;

    private CouponGenerationProperties properties;
    private CouponBatchService service;

    @BeforeEach
    void setUp() {
        properties = new CouponGenerationProperties();
        properties.setMaxBatchSize(100);
        properties.setMinBatchSize(1);
        properties.setCollisionRetryLimit(3);
        properties.setPersistenceChunkSize(10);
        properties.setSampleSize(25);
        service = new CouponBatchService(
                couponBatchRepository,
                serializedCouponRepository,
                rmsCouponDefinitionRepository,
                rmsCouponClient,
                couponCodeGenerator,
                properties,
                new CouponMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC),
                new SynchronousTransactionManager()
        );
    }

    @Test
    void createsExpectedNumberOfCoupons() {
        CreateCouponBatchRequest request = validRequest(5);
        stubActiveRms();
        UUID batchId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(couponBatchRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            CouponBatch batch = invocation.getArgument(0);
            batch.setId(batchId);
            return batch;
        });
        when(couponBatchRepository.findWithRmsById(batchId)).thenAnswer(invocation -> Optional.of(sampleBatch(batchId)));
        when(couponCodeGenerator.generate("1234")).thenReturn(
                "FF1234ABCD2345", "FF1234ABCD2346", "FF1234ABCD2347", "FF1234ABCD2348", "FF1234ABCD2349"
        );
        when(serializedCouponRepository.existsByCouponCode(any())).thenReturn(false);
        when(serializedCouponRepository.findTop25ByCouponBatch_IdOrderByCreatedAtAsc(batchId)).thenReturn(List.of());

        CouponBatchService.BatchCreationResult result = service.createBatch(request, "idem-1");

        assertThat(result.replayed()).isFalse();
        assertThat(result.response().generatedQuantity()).isEqualTo(5);
        assertThat(result.response().status()).isEqualTo(CouponBatchStatus.COMPLETED);
        ArgumentCaptor<Iterable<SerializedCoupon>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(serializedCouponRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(5);
    }

    @Test
    void retriesWhenGeneratedCodeAlreadyExists() {
        Set<String> seen = new HashSet<>();
        when(couponCodeGenerator.generate("1234")).thenReturn("FF1234DUPLICATE", "FF1234UNIQUE12");
        when(serializedCouponRepository.existsByCouponCode("FF1234DUPLICATE")).thenReturn(true);
        when(serializedCouponRepository.existsByCouponCode("FF1234UNIQUE12")).thenReturn(false);

        String code = service.nextUniqueCode("1234", seen, UUID.randomUUID());

        assertThat(code).isEqualTo("FF1234UNIQUE12");
        verify(couponCodeGenerator, times(2)).generate("1234");
    }

    @Test
    void exhaustsCollisionRetries() {
        when(couponCodeGenerator.generate("1234")).thenReturn("FF1234DUPLICATE");
        when(serializedCouponRepository.existsByCouponCode("FF1234DUPLICATE")).thenReturn(true);

        assertThatThrownBy(() -> service.nextUniqueCode("1234", new HashSet<>(), UUID.randomUUID()))
                .isInstanceOf(CouponGenerationException.class);
    }

    @Test
    void rejectsMissingRmsCoupon() {
        when(rmsCouponClient.getCouponDefinition("MISSING")).thenReturn(Optional.empty());
        CreateCouponBatchRequest request = new CreateCouponBatchRequest(
                "MISSING", "1234", 1,
                Instant.parse("2026-09-10T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                null
        );
        assertThatThrownBy(() -> service.createBatch(request, null))
                .isInstanceOf(RmsCouponNotFoundException.class);
    }

    @Test
    void rejectsInactiveRmsCoupon() {
        when(rmsCouponClient.getCouponDefinition("RMS-COUPON-INACTIVE")).thenReturn(Optional.of(
                new RmsCouponDetails("RMS-COUPON-INACTIVE", "OLD99", "Retired", null, false)
        ));
        CreateCouponBatchRequest request = new CreateCouponBatchRequest(
                "RMS-COUPON-INACTIVE", "1234", 1,
                Instant.parse("2026-09-10T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                null
        );
        assertThatThrownBy(() -> service.createBatch(request, null))
                .isInstanceOf(RmsCouponInactiveException.class);
    }

    @Test
    void rejectsInvalidDateRange() {
        CreateCouponBatchRequest request = new CreateCouponBatchRequest(
                "RMS-COUPON-1001", "1234", 1,
                Instant.parse("2026-12-31T00:00:00Z"),
                Instant.parse("2026-09-10T00:00:00Z"),
                null
        );
        assertThatThrownBy(() -> service.createBatch(request, null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("startAt");
    }

    @Test
    void rejectsQuantityAboveMaximum() {
        CreateCouponBatchRequest request = new CreateCouponBatchRequest(
                "RMS-COUPON-1001", "1234", 101,
                Instant.parse("2026-09-10T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                null
        );
        assertThatThrownBy(() -> service.createBatch(request, null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("maximum");
    }

    @Test
    void idempotentReplayReturnsPriorBatch() {
        UUID batchId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        CouponBatch existing = sampleBatch(batchId);
        existing.setIdempotencyKey("same-key");
        existing.setRequestFingerprint(fingerprintFor(validRequest(5)));
        existing.setGeneratedQuantity(5);
        existing.setStatus(CouponBatchStatus.COMPLETED);
        when(couponBatchRepository.findByIdempotencyKey("same-key")).thenReturn(Optional.of(existing));
        when(serializedCouponRepository.findTop25ByCouponBatch_IdOrderByCreatedAtAsc(batchId)).thenReturn(List.of());

        CouponBatchService.BatchCreationResult result = service.createBatch(validRequest(5), "same-key");

        assertThat(result.replayed()).isTrue();
        assertThat(result.response().id()).isEqualTo(batchId);
        verify(couponCodeGenerator, times(0)).generate(any());
    }

    private CreateCouponBatchRequest validRequest(int quantity) {
        return new CreateCouponBatchRequest(
                "RMS-COUPON-1001",
                "1234",
                quantity,
                Instant.parse("2026-09-10T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                "FALL-2026-CAMPAIGN"
        );
    }

    private void stubActiveRms() {
        RmsCouponDetails details = new RmsCouponDetails("RMS-COUPON-1001", "FALL26", "Fall 2026 BOGO", "desc", true);
        when(rmsCouponClient.getCouponDefinition("RMS-COUPON-1001")).thenReturn(Optional.of(details));
        when(rmsCouponDefinitionRepository.findByRmsCouponId("RMS-COUPON-1001")).thenReturn(Optional.empty());
        when(rmsCouponDefinitionRepository.save(any())).thenAnswer(invocation -> {
            RmsCouponDefinition definition = invocation.getArgument(0);
            definition.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
            return definition;
        });
    }

    private CouponBatch sampleBatch(UUID batchId) {
        RmsCouponDefinition rms = new RmsCouponDefinition();
        rms.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        rms.setRmsCouponId("RMS-COUPON-1001");
        rms.setRmsCouponCode("FALL26");
        rms.setName("Fall 2026 BOGO");
        rms.setActive(true);
        CouponBatch batch = new CouponBatch();
        batch.setId(batchId);
        batch.setRmsCouponDefinition(rms);
        batch.setCouponProgramCode("1234");
        batch.setRequestedQuantity(5);
        batch.setGeneratedQuantity(5);
        batch.setStartAt(Instant.parse("2026-09-10T00:00:00Z"));
        batch.setExpiresAt(Instant.parse("2026-12-31T23:59:59Z"));
        batch.setStatus(CouponBatchStatus.COMPLETED);
        batch.setExternalReference("FALL-2026-CAMPAIGN");
        return batch;
    }

    private String fingerprintFor(CreateCouponBatchRequest request) {
        try {
            var field = CouponBatchService.class.getDeclaredMethod("fingerprint", CreateCouponBatchRequest.class);
            field.setAccessible(true);
            return (String) field.invoke(service, request);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final class SynchronousTransactionManager implements PlatformTransactionManager {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }
}
