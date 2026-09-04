package com.skillnet.serializedcoupon.controller;

import com.skillnet.serializedcoupon.config.CouponGenerationProperties;
import com.skillnet.serializedcoupon.domain.CouponCodes;
import com.skillnet.serializedcoupon.dto.DashboardSummaryResponse;
import com.skillnet.serializedcoupon.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CouponGenerationProperties generationProperties;

    public DashboardController(DashboardService dashboardService, CouponGenerationProperties generationProperties) {
        this.dashboardService = dashboardService;
        this.generationProperties = generationProperties;
    }

    @GetMapping("/dashboard")
    public DashboardSummaryResponse dashboard() {
        return dashboardService.summarize();
    }

    @GetMapping("/meta/generation-config")
    public GenerationConfigResponse generationConfig() {
        return new GenerationConfigResponse(
                generationProperties.getMinBatchSize(),
                generationProperties.getMaxBatchSize(),
                CouponCodes.PREFIX,
                CouponCodes.PROGRAM_CODE_LENGTH,
                CouponCodes.SUFFIX_LENGTH,
                CouponCodes.TOTAL_LENGTH,
                CouponCodes.COUPON_CODE_REGEX,
                CouponCodes.PROGRAM_CODE_REGEX
        );
    }

    public record GenerationConfigResponse(
            int minBatchSize,
            int maxBatchSize,
            String prefix,
            int programCodeLength,
            int suffixLength,
            int totalLength,
            String couponCodeRegex,
            String programCodeRegex
    ) {
    }
}
