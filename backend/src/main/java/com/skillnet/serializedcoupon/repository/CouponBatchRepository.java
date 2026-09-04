package com.skillnet.serializedcoupon.repository;

import com.skillnet.serializedcoupon.domain.CouponBatch;
import com.skillnet.serializedcoupon.domain.CouponBatchStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponBatchRepository extends JpaRepository<CouponBatch, UUID>, JpaSpecificationExecutor<CouponBatch> {

    @EntityGraph(attributePaths = "rmsCouponDefinition")
    @Query("select b from CouponBatch b where b.id = :id")
    Optional<CouponBatch> findWithRmsById(@Param("id") UUID id);

    @EntityGraph(attributePaths = "rmsCouponDefinition")
    Optional<CouponBatch> findByIdempotencyKey(String idempotencyKey);

    long countByStatus(CouponBatchStatus status);
}
