package com.skillnet.serializedcoupon.mapper;

import com.skillnet.serializedcoupon.domain.Coupon;
import com.skillnet.serializedcoupon.domain.CouponBatch;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.dto.CouponBatchResponse;
import com.skillnet.serializedcoupon.dto.CouponResponse;
import com.skillnet.serializedcoupon.dto.RmsCouponDefinitionResponse;
import com.skillnet.serializedcoupon.dto.SerializedCouponResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CouponMapper {

    public RmsCouponDefinitionResponse toResponse(RmsCouponDefinition entity) {
        return new RmsCouponDefinitionResponse(
                entity.getId(),
                entity.getRmsCouponId(),
                entity.getRmsCouponCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CouponBatchResponse toResponse(CouponBatch batch, List<String> sampleCouponCodes) {
        RmsCouponDefinition rms = batch.getRmsCouponDefinition();
        Coupon coupon = batch.getCoupon();
        return new CouponBatchResponse(
                batch.getId(),
                coupon == null ? null : coupon.getId(),
                rms.getRmsCouponId(),
                rms.getRmsCouponCode(),
                rms.getName(),
                batch.getCouponProgramCode(),
                batch.getRequestedQuantity(),
                batch.getGeneratedQuantity(),
                batch.getStartAt(),
                batch.getExpiresAt(),
                batch.getStatus(),
                batch.getCreatedBy(),
                batch.getExternalReference(),
                batch.getCreatedAt(),
                batch.getUpdatedAt(),
                sampleCouponCodes
        );
    }

    public CouponResponse toResponse(Coupon coupon) {
        RmsCouponDefinition rms = coupon.getRmsCouponDefinition();
        return new CouponResponse(
                coupon.getId(),
                coupon.getTitle(),
                coupon.getDescription(),
                coupon.getUsageLimit(),
                coupon.getPosCode(),
                coupon.getAtgCode(),
                coupon.getCouponSource(),
                rms.getRmsCouponId(),
                rms.getRmsCouponCode(),
                rms.getName(),
                coupon.getCouponProgramCode(),
                coupon.getStartAt(),
                coupon.getExpiresAt(),
                coupon.getStatus(),
                coupon.getCreatedBy(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }

    public SerializedCouponResponse toResponse(SerializedCoupon coupon) {
        return new SerializedCouponResponse(
                coupon.getId(),
                coupon.getCouponCode(),
                coupon.getCouponBatch().getId(),
                coupon.getRmsCouponDefinition().getRmsCouponId(),
                coupon.getRmsCouponDefinition().getRmsCouponCode(),
                coupon.getCouponProgramCode(),
                coupon.getStartAt(),
                coupon.getExpiresAt(),
                coupon.getStatus(),
                coupon.getRedeemedAt(),
                coupon.getDeactivatedAt(),
                coupon.getExternalReference(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }
}
