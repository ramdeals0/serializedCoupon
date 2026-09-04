package com.skillnet.serializedcoupon.repository;

import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SerializedCouponRepository
        extends JpaRepository<SerializedCoupon, UUID>, JpaSpecificationExecutor<SerializedCoupon> {

    @EntityGraph(attributePaths = {"couponBatch", "rmsCouponDefinition"})
    Optional<SerializedCoupon> findByCouponCode(String couponCode);

    boolean existsByCouponCode(String couponCode);

    @EntityGraph(attributePaths = {"couponBatch", "rmsCouponDefinition"})
    Page<SerializedCoupon> findByCouponBatch_Id(UUID batchId, Pageable pageable);

    List<SerializedCoupon> findTop25ByCouponBatch_IdOrderByCreatedAtAsc(UUID batchId);

    long countByStatus(SerializedCouponStatus status);

    long countByExpiresAtBeforeAndStatusIn(Instant expiresAt, Collection<SerializedCouponStatus> statuses);
}
