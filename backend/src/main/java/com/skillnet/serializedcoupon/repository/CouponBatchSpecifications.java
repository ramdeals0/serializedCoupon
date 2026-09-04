package com.skillnet.serializedcoupon.repository;

import com.skillnet.serializedcoupon.domain.CouponBatch;
import com.skillnet.serializedcoupon.domain.CouponBatchStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Locale;

public final class CouponBatchSpecifications {

    private CouponBatchSpecifications() {
    }

    public static Specification<CouponBatch> withRmsFetch() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("rmsCouponDefinition", JoinType.INNER);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    public static Specification<CouponBatch> rmsCouponIdEquals(String rmsCouponId) {
        if (isBlank(rmsCouponId)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("rmsCouponDefinition").get("rmsCouponId"), rmsCouponId);
    }

    public static Specification<CouponBatch> couponProgramCodeEquals(String couponProgramCode) {
        if (isBlank(couponProgramCode)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("couponProgramCode"), couponProgramCode);
    }

    public static Specification<CouponBatch> statusEquals(CouponBatchStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<CouponBatch> createdFrom(Instant createdFrom) {
        if (createdFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
    }

    public static Specification<CouponBatch> createdTo(Instant createdTo) {
        if (createdTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdTo);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
