package com.skillnet.serializedcoupon.repository;

import com.skillnet.serializedcoupon.domain.Coupon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID>, JpaSpecificationExecutor<Coupon> {

    @EntityGraph(attributePaths = "rmsCouponDefinition")
    @Query("select c from Coupon c where c.id = :id")
    Optional<Coupon> findWithRmsById(@Param("id") UUID id);
}
