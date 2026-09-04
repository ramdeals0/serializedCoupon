package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.config.CouponGenerationProperties;
import com.skillnet.serializedcoupon.domain.CouponBatch;
import com.skillnet.serializedcoupon.domain.CouponBatchStatus;
import com.skillnet.serializedcoupon.domain.CouponCodes;
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
import com.skillnet.serializedcoupon.exception.RmsCouponNotFoundException;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponDetails;
import com.skillnet.serializedcoupon.mapper.CouponMapper;
import com.skillnet.serializedcoupon.repository.CouponBatchRepository;
import com.skillnet.serializedcoupon.repository.CouponBatchSpecifications;
import com.skillnet.serializedcoupon.repository.RmsCouponDefinitionRepository;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
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
    private final RmsCouponDefinitionRepository rmsCouponDefinitionRepository;
    private final RmsCouponClient rmsCouponClient;
    private final CouponCodeGenerator couponCodeGenerator;
    private final CouponGenerationProperties generationProperties;
    private final CouponMapper couponMapper;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public CouponBatchService(
            CouponBatchRepository couponBatchRepository,
            SerializedCouponRepository serializedCouponRepository,
            RmsCouponDefinitionRepository rmsCouponDefinitionRepository,
            RmsCouponClient rmsCouponClient,
            CouponCodeGenerator couponCodeGenerator,
            CouponGenerationProperties generationProperties,
            CouponMapper couponMapper,
            Clock clock,
            PlatformTransactionManager transactionManager
    ) {
        this.couponBatchRepository = couponBatchRepository;
        this.serializedCouponRepository = serializedCouponRepository;
        this.rmsCouponDefinitionRepository = rmsCouponDefinitionRepository;
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
        validateRequest(request);
        String fingerprint = fingerprint(request);
        String normalizedKey = StringUtils.hasText(idempotencyKey) ? idempotencyKey.trim() : null;

        if (normalizedKey != null) {
            var existing = couponBatchRepository.findByIdempotencyKey(normalizedKey);
            if (existing.isPresent()) {
                return replayOrConflict(existing.get(), fingerprint, request.rmsCouponId());
            }
        }

        RmsCouponDetails rmsDetails = rmsCouponClient.getCouponDefinition(request.rmsCouponId())
                .orElseThrow(() -> new RmsCouponNotFoundException(request.rmsCouponId()));
        if (!rmsDetails.active()) {
            throw new RmsCouponInactiveException(request.rmsCouponId());
        }

        UUID batchId;
        try {
            batchId = transactionTemplate.execute(status -> {
                RmsCouponDefinition rmsDefinition = synchronizeRmsDefinition(rmsDetails);
                CouponBatch batch = new CouponBatch();
                batch.setRmsCouponDefinition(rmsDefinition);
                batch.setCouponProgramCode(request.couponProgramCode());
                batch.setRequestedQuantity(request.quantity());
                batch.setGeneratedQuantity(0);
                batch.setStartAt(request.startAt());
                batch.setExpiresAt(request.expiresAt());
                batch.setStatus(CouponBatchStatus.PROCESSING);
                batch.setExternalReference(request.externalReference());
                batch.setIdempotencyKey(normalizedKey);
                batch.setRequestFingerprint(fingerprint);
                return couponBatchRepository.saveAndFlush(batch).getId();
            });
        } catch (DataIntegrityViolationException ex) {
            if (normalizedKey != null) {
                CouponBatch raced = couponBatchRepository.findByIdempotencyKey(normalizedKey)
                        .orElseThrow(() -> ex);
                return replayOrConflict(raced, fingerprint, request.rmsCouponId());
            }
            throw ex;
        }

        log.info("Starting coupon generation batchId={} rmsCouponId={} requested={}",
                batchId, request.rmsCouponId(), request.quantity());

        try {
            transactionTemplate.executeWithoutResult(status -> generateAndComplete(batchId, request));
        } catch (RuntimeException ex) {
            markFailed(batchId, ex.getMessage());
            if (ex instanceof CouponGenerationException) {
                throw ex;
            }
            throw new CouponGenerationException("Coupon generation failed for batch " + batchId);
        }

        CouponBatch completed = couponBatchRepository.findWithRmsById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon batch not found: " + batchId));
        log.info("Completed coupon generation batchId={} rmsCouponId={} generated={}",
                batchId, request.rmsCouponId(), completed.getGeneratedQuantity());
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

    public void generateAndComplete(UUID batchId, CreateCouponBatchRequest request) {
        CouponBatch batch = couponBatchRepository.findWithRmsById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon batch not found: " + batchId));
        RmsCouponDefinition rmsDefinition = batch.getRmsCouponDefinition();
        int quantity = request.quantity();
        int chunkSize = Math.max(1, generationProperties.getPersistenceChunkSize());
        Set<String> seenInRequest = new HashSet<>(quantity);
        List<SerializedCoupon> chunk = new ArrayList<>(chunkSize);
        Instant now = Instant.now(clock);
        SerializedCouponStatus initialStatus = now.isBefore(request.startAt())
                ? SerializedCouponStatus.PENDING
                : SerializedCouponStatus.ACTIVE;

        for (int i = 0; i < quantity; i++) {
            SerializedCoupon coupon = new SerializedCoupon();
            coupon.setCouponBatch(batch);
            coupon.setRmsCouponDefinition(rmsDefinition);
            coupon.setCouponCode(nextUniqueCode(request.couponProgramCode(), seenInRequest, batchId));
            coupon.setCouponProgramCode(request.couponProgramCode());
            coupon.setStartAt(request.startAt());
            coupon.setExpiresAt(request.expiresAt());
            coupon.setStatus(initialStatus);
            coupon.setExternalReference(request.externalReference());
            chunk.add(coupon);
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

    private void validateRequest(CreateCouponBatchRequest request) {
        if (!CouponCodes.isValidProgramCode(request.couponProgramCode())) {
            throw new BusinessValidationException("couponProgramCode must be exactly 4 digits");
        }
        if (request.startAt() == null || request.expiresAt() == null || !request.startAt().isBefore(request.expiresAt())) {
            throw new BusinessValidationException("startAt must be strictly before expiresAt");
        }
        Instant now = Instant.now(clock);
        if (now.isAfter(request.expiresAt())) {
            throw new BusinessValidationException("expiresAt must not be in the past");
        }
        int quantity = request.quantity();
        if (quantity < generationProperties.getMinBatchSize()) {
            throw new BusinessValidationException("quantity must be at least " + generationProperties.getMinBatchSize());
        }
        if (quantity > generationProperties.getMaxBatchSize()) {
            throw new BusinessValidationException(
                    "quantity exceeds configured maximum of " + generationProperties.getMaxBatchSize()
            );
        }
    }

    private RmsCouponDefinition synchronizeRmsDefinition(RmsCouponDetails details) {
        RmsCouponDefinition entity = rmsCouponDefinitionRepository.findByRmsCouponId(details.rmsCouponId())
                .orElseGet(RmsCouponDefinition::new);
        entity.setRmsCouponId(details.rmsCouponId());
        entity.setRmsCouponCode(details.rmsCouponCode());
        entity.setName(details.name());
        entity.setDescription(details.description());
        entity.setActive(details.active());
        return rmsCouponDefinitionRepository.save(entity);
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
                nullToEmpty(request.rmsCouponId()),
                nullToEmpty(request.couponProgramCode()),
                String.valueOf(request.quantity()),
                request.startAt().toString(),
                request.expiresAt().toString(),
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
