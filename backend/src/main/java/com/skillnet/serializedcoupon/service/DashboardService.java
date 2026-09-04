package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.dto.DashboardSummaryResponse;
import com.skillnet.serializedcoupon.mapper.CouponMapper;
import com.skillnet.serializedcoupon.repository.CouponBatchRepository;
import com.skillnet.serializedcoupon.repository.CouponBatchSpecifications;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class DashboardService {

    private final CouponBatchRepository couponBatchRepository;
    private final SerializedCouponRepository serializedCouponRepository;
    private final CouponMapper couponMapper;
    private final Clock clock;

    public DashboardService(
            CouponBatchRepository couponBatchRepository,
            SerializedCouponRepository serializedCouponRepository,
            CouponMapper couponMapper,
            Clock clock
    ) {
        this.couponBatchRepository = couponBatchRepository;
        this.serializedCouponRepository = serializedCouponRepository;
        this.couponMapper = couponMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summarize() {
        Instant now = Instant.now(clock);
        long totalBatches = couponBatchRepository.count();
        long totalCoupons = serializedCouponRepository.count();
        long activeCoupons = serializedCouponRepository.countByStatus(SerializedCouponStatus.ACTIVE);
        long expiredCoupons = serializedCouponRepository.countByStatus(SerializedCouponStatus.EXPIRED)
                + serializedCouponRepository.countByExpiresAtBeforeAndStatusIn(
                now,
                Set.of(SerializedCouponStatus.ACTIVE, SerializedCouponStatus.PENDING)
        );
        var recent = couponBatchRepository.findAll(
                        CouponBatchSpecifications.withRmsFetch(),
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .map(batch -> couponMapper.toResponse(batch, List.of()))
                .getContent();
        return new DashboardSummaryResponse(totalBatches, totalCoupons, activeCoupons, expiredCoupons, recent);
    }
}
