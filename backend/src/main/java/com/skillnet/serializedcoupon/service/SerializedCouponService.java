package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.dto.PageResponse;
import com.skillnet.serializedcoupon.dto.SerializedCouponResponse;
import com.skillnet.serializedcoupon.exception.InvalidLifecycleTransitionException;
import com.skillnet.serializedcoupon.exception.ResourceNotFoundException;
import com.skillnet.serializedcoupon.mapper.CouponMapper;
import com.skillnet.serializedcoupon.repository.CouponBatchRepository;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import com.skillnet.serializedcoupon.repository.SerializedCouponSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class SerializedCouponService {

    private static final EnumSet<SerializedCouponStatus> DEACTIVATABLE =
            EnumSet.of(SerializedCouponStatus.ACTIVE, SerializedCouponStatus.PENDING);

    private final SerializedCouponRepository serializedCouponRepository;
    private final CouponBatchRepository couponBatchRepository;
    private final CouponMapper couponMapper;
    private final Clock clock;

    public SerializedCouponService(
            SerializedCouponRepository serializedCouponRepository,
            CouponBatchRepository couponBatchRepository,
            CouponMapper couponMapper,
            Clock clock
    ) {
        this.serializedCouponRepository = serializedCouponRepository;
        this.couponBatchRepository = couponBatchRepository;
        this.couponMapper = couponMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<SerializedCouponResponse> search(
            String query,
            String rmsCouponId,
            String couponProgramCode,
            UUID batchId,
            SerializedCouponStatus status,
            Instant validAt,
            Instant startFrom,
            Instant expirationTo,
            Pageable pageable
    ) {
        Specification<SerializedCoupon> spec = SerializedCouponSpecifications.withRelationsFetch()
                .and(SerializedCouponSpecifications.couponCodeContains(query))
                .and(SerializedCouponSpecifications.rmsCouponIdEquals(rmsCouponId))
                .and(SerializedCouponSpecifications.couponProgramCodeEquals(couponProgramCode))
                .and(SerializedCouponSpecifications.batchIdEquals(batchId))
                .and(SerializedCouponSpecifications.statusEquals(status))
                .and(SerializedCouponSpecifications.validAt(validAt))
                .and(SerializedCouponSpecifications.startFrom(startFrom))
                .and(SerializedCouponSpecifications.expirationTo(expirationTo));
        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SerializedCouponResponse> page = serializedCouponRepository.findAll(spec, sorted)
                .map(couponMapper::toResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<SerializedCouponResponse> searchByBatch(
            UUID batchId,
            SerializedCouponStatus status,
            String query,
            Instant activeAt,
            Pageable pageable
    ) {
        if (!couponBatchRepository.existsById(batchId)) {
            throw new ResourceNotFoundException("Coupon batch not found: " + batchId);
        }
        return search(query, null, null, batchId, status, activeAt, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public SerializedCouponResponse getByCode(String couponCode) {
        SerializedCoupon coupon = serializedCouponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new ResourceNotFoundException("Serialized coupon not found: " + couponCode));
        return couponMapper.toResponse(coupon);
    }

    @Transactional
    public SerializedCouponResponse deactivate(String couponCode) {
        SerializedCoupon coupon = serializedCouponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new ResourceNotFoundException("Serialized coupon not found: " + couponCode));
        if (coupon.getStatus() == SerializedCouponStatus.REDEEMED) {
            throw new InvalidLifecycleTransitionException("Redeemed coupons cannot be deactivated");
        }
        if (!DEACTIVATABLE.contains(coupon.getStatus())) {
            throw new InvalidLifecycleTransitionException(
                    "Coupon in status " + coupon.getStatus() + " cannot be deactivated"
            );
        }
        coupon.setStatus(SerializedCouponStatus.DEACTIVATED);
        coupon.setDeactivatedAt(Instant.now(clock));
        return couponMapper.toResponse(serializedCouponRepository.save(coupon));
    }
}
