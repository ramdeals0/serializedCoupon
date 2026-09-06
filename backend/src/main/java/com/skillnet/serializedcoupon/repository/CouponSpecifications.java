package com.skillnet.serializedcoupon.repository;

import com.skillnet.serializedcoupon.domain.Coupon;
import com.skillnet.serializedcoupon.domain.CouponStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CouponSpecifications {

    private CouponSpecifications() {
    }

    public static Specification<Coupon> withRmsFetch() {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("rmsCouponDefinition");
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Coupon> rmsCouponIdEquals(String rmsCouponId) {
        if (!StringUtils.hasText(rmsCouponId)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("rmsCouponDefinition").get("rmsCouponId"), rmsCouponId.trim());
    }

    public static Specification<Coupon> couponProgramCodeEquals(String couponProgramCode) {
        if (!StringUtils.hasText(couponProgramCode)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("couponProgramCode"), couponProgramCode.trim());
    }

    public static Specification<Coupon> statusEquals(CouponStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
