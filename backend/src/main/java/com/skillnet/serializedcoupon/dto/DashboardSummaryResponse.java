package com.skillnet.serializedcoupon.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long totalBatches,
        long totalSerializedCoupons,
        long activeCoupons,
        long expiredCoupons,
        List<CouponBatchResponse> recentBatches
) {
}
