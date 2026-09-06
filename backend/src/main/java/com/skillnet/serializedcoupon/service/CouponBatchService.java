package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.config.CouponGenerationProperties;
import com.skillnet.serializedcoupon.domain.Coupon;
import com.skillnet.serializedcoupon.domain.CouponBatch;
import com.skillnet.serializedcoupon.domain.CouponBatchStatus;
import com.skillnet.serializedcoupon.domain.CouponStatus;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.dto.CouponBatchResponse;
import com.skillnet.serializedcoupon.dto.CreateCouponBatchRequest;
import com.skillnet.serializedcoupon.dto.PageResponse;
import com.skillnet.serializedcoupon.exception.BusinessValidationException;
import com.skillnet.serializedcoupon.exception.CouponGenerationException;
import com.skillnet.serializedcoupon.exception.IdempotencyConflictException;
import com.skillnet.serializedcoupon.exception.ResourceNotFoundException;
import com.skillnet.serializedcoupon.exception.RmsCouponInactiveException;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
import com.skillnet.serializedcoupon.mapper.CouponMapper;
import com.skillnet.serializedcoupon.repository.CouponBatchRepository;
import com.skillnet.serializedcoupon.repository.CouponBatchSpecifications;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import com.skillnet.serializedcoupon.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CouponBatchService {

    private static final Logger log = LoggerFactory.getLogger(CouponBatchService.class);

    private final CouponBatchRepository couponBatchRepository;
    private final SerializedCouponRepository serializedCouponRepository;
    private final CouponService couponService;
    private final RmsCouponClient rmsCouponClient;
    private final CouponCodeGenerator couponCodeGenerator;
    private final CouponGenerationProperties generationProperties;
    private final CouponMapper couponMapper;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public CouponBatchService(
            CouponBatchRepository couponBatchRepository,
            SerializedCouponRepository serializedCouponRepository,
            CouponService couponService,
            RmsCouponClient rmsCouponClient,
            CouponCodeGenerator couponCodeGenerator,
            CouponGenerationProperties generationProperties,
            CouponMapper couponMapper,
            Clock clock,
            PlatformTransactionManager transactionManager
    ) {
        this.couponBatchRepository = couponBatchRepository;
        this.serializedCouponRepository = serializedCouponRepository;
        this.couponService = couponService;
        this.rmsCouponClient = rmsCouponClient;
        this.couponCodeGenerator = couponCodeGenerator;
        this.generationProperties = generationProperties;
        this.couponMapper = couponMapper;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public record BatchCreationResult(CouponBatchResponse response, boolean replayed) {
    }

    public BatchCreationResult createBatch(CreateCouponBatchRequest request, String idempotencyKey) {
        validateQuantity(request.quantity());
        String fingerprint = fingerprint(request);
        String normalizedKey = StringUtils.hasText(idempotencyKey) ? idempotencyKey.trim() : null;
        if (normalizedKey != null) {
            var existing = couponBatchRepository.findByIdempotencyKey(normalizedKey);
            if (existing.isPresent()) {
                CouponBatch prior = existing.get();
                String priorRmsCouponId = prior.getRmsCouponDefinition() != null
                        ? prior.getRmsCouponDefinition().getRmsCouponId()
                        : "";
                return replayOrConflict(prior, fingerprint, priorRmsCouponId);
            }
        }

        Coupon coupon = couponService.require(request.couponId());
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new BusinessValidationException("Coupon is not active: " + coupon.getId());
        }
        Instant now = Instant.now(clock);
        if (now.isAfter(coupon.getExpiresAt())) {
            throw new BusinessValidationException("Cannot generate a batch for an expired coupon");
        }
        String rmsCouponId = coupon.getRmsCouponDefinition().getRmsCouponId();

        rmsCouponClient.getCouponDefinition(rmsCouponId).ifPresent(rmsDetails -> {
            if (!rmsDetails.active()) {
                throw new RmsCouponInactiveException(rmsCouponId);
            }
        });

        UUID batchId;
        try {
            batchId = transactionTemplate.execute(status -> {
                CouponBatch batch = new CouponBatch();
                batch.setCoupon(coupon);
                batch.setRmsCouponDefinition(coupon.getRmsCouponDefinition());
                batch.setCouponProgramCode(coupon.getCouponProgramCode());
                batch.setRequestedQuantity(request.quantity());
                batch.setGeneratedQuantity(0);
                batch.setStartAt(coupon.getStartAt());
                batch.setExpiresAt(coupon.getExpiresAt());
                batch.setStatus(CouponBatchStatus.PROCESSING);
                batch.setExternalReference(
                        StringUtils.hasText(request.externalReference())
                                ? request.externalReference()
                                : coupon.getExternalReference()
                );
                batch.setIdempotencyKey(normalizedKey);
                batch.setRequestFingerprint(fingerprint);
                batch.setCreatedBy(CurrentUser.username());
                return couponBatchRepository.saveAndFlush(batch).getId();
            });
        } catch (DataIntegrityViolationException ex) {
            if (normalizedKey != null) {
                CouponBatch raced = couponBatchRepository.findByIdempotencyKey(normalizedKey)
                        .orElseThrow(() -> ex);
                return replayOrConflict(raced, fingerprint, rmsCouponId);
            }
            throw ex;
        }

        log.info("Starting coupon generation batchId={} couponId={} rmsCouponId={} requested={}",
                batchId, coupon.getId(), rmsCouponId, request.quantity());

        try {
            transactionTemplate.executeWithoutResult(status -> generateAndComplete(batchId, request.quantity()));
        } catch (RuntimeException ex) {
            markFailed(batchId, ex.getMessage());
            if (ex instanceof CouponGenerationException) {
                throw ex;
            }
            throw new CouponGenerationException("Coupon generation failed for batch " + batchId);
        }

        CouponBatch completed = couponBatchRepository.findWithRmsById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon batch not found: " + batchId));
        log.info("Completed coupon generation batchId={} couponId={} generated={}",
                batchId, coupon.getId(), completed.getGeneratedQuantity());
        return new BatchCreationResult(toResponse(completed), false);
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponBatchResponse> search(
            String rmsCouponId,
            String couponProgramCode,
            CouponBatchStatus status,
            Instant createdFrom,
            Instant createdTo,
            Pageable pageable
    ) {
        Specification<CouponBatch> spec = CouponBatchSpecifications.withRmsFetch()
                .and(CouponBatchSpecifications.rmsCouponIdEquals(rmsCouponId))
                .and(CouponBatchSpecifications.couponProgramCodeEquals(couponProgramCode))
                .and(CouponBatchSpecifications.statusEquals(status))
                .and(CouponBatchSpecifications.createdFrom(createdFrom))
                .and(CouponBatchSpecifications.createdTo(createdTo));
        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CouponBatchResponse> page = couponBatchRepository.findAll(spec, sorted)
                .map(batch -> couponMapper.toResponse(batch, List.of()));
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public CouponBatchResponse getBatch(UUID batchId) {
        CouponBatch batch = couponBatchRepository.findWithRmsById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon batch not found: " + batchId));
        return toResponse(batch);
    }

    public void generateAndComplete(UUID batchId, int quantity) {
        CouponBatch batch = couponBatchRepository.findWithRmsById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon batch not found: " + batchId));
        RmsCouponDefinition rmsDefinition = batch.getRmsCouponDefinition();
        int chunkSize = Math.max(1, generationProperties.getPersistenceChunkSize());
        Set<String> seenInRequest = new HashSet<>(quantity);
        List<SerializedCoupon> chunk = new ArrayList<>(chunkSize);
        Instant now = Instant.now(clock);
        SerializedCouponStatus initialStatus = now.isBefore(batch.getStartAt())
                ? SerializedCouponStatus.PENDING
                : SerializedCouponStatus.ACTIVE;

        for (int i = 0; i < quantity; i++) {
            SerializedCoupon serialized = new SerializedCoupon();
            serialized.setCouponBatch(batch);
            serialized.setRmsCouponDefinition(rmsDefinition);
            serialized.setCouponCode(nextUniqueCode(batch.getCouponProgramCode(), seenInRequest, batchId));
            serialized.setCouponProgramCode(batch.getCouponProgramCode());
            serialized.setStartAt(batch.getStartAt());
            serialized.setExpiresAt(batch.getExpiresAt());
            serialized.setStatus(initialStatus);
            serialized.setExternalReference(batch.getExternalReference());
            chunk.add(serialized);
            if (chunk.size() >= chunkSize) {
                persistChunk(chunk);
                chunk.clear();
            }
        }
        if (!chunk.isEmpty()) {
            persistChunk(chunk);
        }
        batch.setGeneratedQuantity(quantity);
        batch.setStatus(CouponBatchStatus.COMPLETED);
        couponBatchRepository.save(batch);
    }

    private BatchCreationResult replayOrConflict(CouponBatch batch, String fingerprint, String rmsCouponId) {
        if (!fingerprint.equals(batch.getRequestFingerprint())) {
            throw new IdempotencyConflictException(
                    "Idempotency-Key was already used with a different batch request"
            );
        }
        log.info("Replaying idempotent batch request batchId={} rmsCouponId={} generated={}",
                batch.getId(), rmsCouponId, batch.getGeneratedQuantity());
        return new BatchCreationResult(toResponse(batch), true);
    }

    private CouponBatchResponse toResponse(CouponBatch batch) {
        List<String> sample = serializedCouponRepository
                .findTop25ByCouponBatch_IdOrderByCreatedAtAsc(batch.getId())
                .stream()
                .map(SerializedCoupon::getCouponCode)
                .limit(generationProperties.getSampleSize())
                .toList();
        return couponMapper.toResponse(batch, sample);
    }

    private void validateQuantity(int quantity) {
        if (quantity < generationProperties.getMinBatchSize()) {
            throw new BusinessValidationException("quantity must be at least " + generationProperties.getMinBatchSize());
        }
        if (quantity > generationProperties.getMaxBatchSize()) {
            throw new BusinessValidationException(
                    "quantity exceeds configured maximum of " + generationProperties.getMaxBatchSize()
            );
        }
    }

    private void persistChunk(List<SerializedCoupon> chunk) {
        serializedCouponRepository.saveAll(chunk);
        serializedCouponRepository.flush();
    }

    String nextUniqueCode(String couponProgramCode, Set<String> seenInRequest, UUID batchId) {
        int attempts = 0;
        while (attempts <= generationProperties.getCollisionRetryLimit()) {
            attempts++;
            String code = couponCodeGenerator.generate(couponProgramCode);
            if (seenInRequest.add(code) && !serializedCouponRepository.existsByCouponCode(code)) {
                return code;
            }
            log.debug("Retrying coupon code generation after collision attempt={} batchId={}", attempts, batchId);
        }
        throw new CouponGenerationException(
                "Exhausted collision retries while generating a unique coupon code for batch " + batchId
        );
    }

    private void markFailed(UUID batchId, String message) {
        transactionTemplate.executeWithoutResult(status -> couponBatchRepository.findById(batchId).ifPresent(batch -> {
            batch.setStatus(CouponBatchStatus.FAILED);
            batch.setErrorMessage(truncate(message));
            couponBatchRepository.save(batch);
            log.error("Batch generation failed batchId={} status=FAILED", batchId);
        }));
    }

    private String truncate(String message) {
        if (message == null) {
            return "Coupon generation failed";
        }
        return message.length() <= 1024 ? message : message.substring(0, 1024);
    }

    private String fingerprint(CreateCouponBatchRequest request) {
        String canonical = String.join("|",
                request.couponId().toString(),
                String.valueOf(request.quantity()),
                nullToEmpty(request.externalReference())
        );
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
