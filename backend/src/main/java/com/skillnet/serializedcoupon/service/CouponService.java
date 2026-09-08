package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.Coupon;
import com.skillnet.serializedcoupon.domain.CouponCodes;
import com.skillnet.serializedcoupon.domain.CouponSource;
import com.skillnet.serializedcoupon.domain.CouponStatus;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.dto.CouponResponse;
import com.skillnet.serializedcoupon.dto.CreateCouponRequest;
import com.skillnet.serializedcoupon.dto.PageResponse;
import com.skillnet.serializedcoupon.exception.BusinessValidationException;
import com.skillnet.serializedcoupon.exception.ResourceNotFoundException;
import com.skillnet.serializedcoupon.exception.RmsCouponInactiveException;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponDetails;
import com.skillnet.serializedcoupon.mapper.CouponMapper;
import com.skillnet.serializedcoupon.repository.CouponRepository;
import com.skillnet.serializedcoupon.repository.CouponSpecifications;
import com.skillnet.serializedcoupon.repository.RmsCouponDefinitionRepository;
import com.skillnet.serializedcoupon.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final RmsCouponDefinitionRepository rmsCouponDefinitionRepository;
    private final RmsCouponClient rmsCouponClient;
    private final CouponMapper couponMapper;
    private final Clock clock;

    public CouponService(
            CouponRepository couponRepository,
            RmsCouponDefinitionRepository rmsCouponDefinitionRepository,
            RmsCouponClient rmsCouponClient,
            CouponMapper couponMapper,
            Clock clock
    ) {
        this.couponRepository = couponRepository;
        this.rmsCouponDefinitionRepository = rmsCouponDefinitionRepository;
        this.rmsCouponClient = rmsCouponClient;
        this.couponMapper = couponMapper;
        this.clock = clock;
    }

    @Transactional
    public CouponResponse create(CreateCouponRequest request) {
        validate(request);
        RmsCouponDefinition rmsDefinition = synchronizeDefinition(request);
        Coupon coupon = new Coupon();
        coupon.setRmsCouponDefinition(rmsDefinition);
        coupon.setTitle(request.title().trim());
        coupon.setDescription(blankToNull(request.description()));
        coupon.setUsageLimit(request.usageLimit());
        coupon.setPosCode(blankToNull(request.posCode()));
        coupon.setAtgCode(blankToNull(request.atgCode()));
        coupon.setCouponSource(request.couponSource());
        coupon.setCouponProgramCode(request.couponProgramCode());
        coupon.setStartAt(request.startAt());
        coupon.setExpiresAt(request.expiresAt());
        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setCreatedBy(CurrentUser.username());
        return couponMapper.toResponse(couponRepository.saveAndFlush(coupon));
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> search(
            String rmsCouponId,
            String couponProgramCode,
            CouponStatus status,
            Pageable pageable
    ) {
        Specification<Coupon> spec = CouponSpecifications.withRmsFetch()
                .and(CouponSpecifications.rmsCouponIdEquals(rmsCouponId))
                .and(CouponSpecifications.couponProgramCodeEquals(couponProgramCode))
                .and(CouponSpecifications.statusEquals(status));
        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CouponResponse> page = couponRepository.findAll(spec, sorted).map(couponMapper::toResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public CouponResponse get(UUID couponId) {
        return couponMapper.toResponse(require(couponId));
    }

    @Transactional(readOnly = true)
    public Coupon require(UUID couponId) {
        return couponRepository.findWithRmsById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponId));
    }

    private void validate(CreateCouponRequest request) {
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
        boolean pos = StringUtils.hasText(request.posCode());
        boolean atg = StringUtils.hasText(request.atgCode());
        CouponSource source = request.couponSource();
        if (source == CouponSource.POS && !pos) {
            throw new BusinessValidationException("POS code is required when coupon source is POS");
        }
        if (source == CouponSource.ECOMM && !atg) {
            throw new BusinessValidationException("ATG code is required when coupon source is Ecomm");
        }
        if (source == CouponSource.BOTH && (!pos || !atg)) {
            throw new BusinessValidationException("POS code and ATG code are required when coupon source is Both");
        }
    }

    private RmsCouponDefinition synchronizeDefinition(CreateCouponRequest request) {
        String catalogId = catalogId(request);
        rmsCouponClient.getCouponDefinition(catalogId).ifPresent(details -> {
            if (!details.active()) {
                throw new RmsCouponInactiveException(catalogId);
            }
        });
        RmsCouponDefinition entity = rmsCouponDefinitionRepository.findByRmsCouponId(catalogId)
                .orElseGet(RmsCouponDefinition::new);
        entity.setRmsCouponId(catalogId);
        entity.setRmsCouponCode(StringUtils.hasText(request.posCode()) ? request.posCode().trim() : request.atgCode().trim());
        entity.setName(request.title().trim());
        entity.setDescription(blankToNull(request.description()));
        entity.setActive(true);
        return rmsCouponDefinitionRepository.save(entity);
    }

    private String catalogId(CreateCouponRequest request) {
        if (request.couponSource() == CouponSource.ECOMM) {
            return request.atgCode().trim();
        }
        return request.posCode().trim();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
