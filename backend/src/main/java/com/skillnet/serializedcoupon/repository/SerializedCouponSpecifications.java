package com.skillnet.serializedcoupon.repository;

import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class SerializedCouponSpecifications {

    private SerializedCouponSpecifications() {
    }

    public static Specification<SerializedCoupon> withRelationsFetch() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("rmsCouponDefinition", JoinType.INNER);
                root.fetch("couponBatch", JoinType.INNER).fetch("coupon", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    public static Specification<SerializedCoupon> couponCodeContains(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return null;
        }
        String like = "%" + queryText.trim().toUpperCase() + "%";
        return (root, query, cb) -> cb.like(cb.upper(root.get("couponCode")), like);
    }

    public static Specification<SerializedCoupon> rmsCouponIdEquals(String rmsCouponId) {
        if (rmsCouponId == null || rmsCouponId.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("rmsCouponDefinition").get("rmsCouponId"), rmsCouponId);
    }

    public static Specification<SerializedCoupon> couponProgramCodeEquals(String couponProgramCode) {
        if (couponProgramCode == null || couponProgramCode.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("couponProgramCode"), couponProgramCode);
    }

    public static Specification<SerializedCoupon> batchIdEquals(UUID batchId) {
        if (batchId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("couponBatch").get("id"), batchId);
    }

    public static Specification<SerializedCoupon> statusEquals(SerializedCouponStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<SerializedCoupon> validAt(Instant validAt) {
        if (validAt == null) {
            return null;
        }
        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("startAt"), validAt),
                cb.greaterThanOrEqualTo(root.get("expiresAt"), validAt)
        );
    }

    public static Specification<SerializedCoupon> startFrom(Instant startFrom) {
        if (startFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startAt"), startFrom);
    }

    public static Specification<SerializedCoupon> expirationTo(Instant expirationTo) {
        if (expirationTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("expiresAt"), expirationTo);
    }
}
