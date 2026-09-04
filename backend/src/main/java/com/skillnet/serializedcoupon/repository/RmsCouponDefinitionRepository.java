package com.skillnet.serializedcoupon.repository;

import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface RmsCouponDefinitionRepository
        extends JpaRepository<RmsCouponDefinition, UUID>, JpaSpecificationExecutor<RmsCouponDefinition> {

    Optional<RmsCouponDefinition> findByRmsCouponId(String rmsCouponId);

    Page<RmsCouponDefinition> findByActive(boolean active, Pageable pageable);
}
