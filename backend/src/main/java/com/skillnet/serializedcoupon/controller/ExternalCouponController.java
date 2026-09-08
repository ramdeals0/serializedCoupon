package com.skillnet.serializedcoupon.controller;

import com.skillnet.serializedcoupon.dto.ExternalCouponRequest;
import com.skillnet.serializedcoupon.dto.ExternalCouponResponse;
import com.skillnet.serializedcoupon.service.CouponRedemptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/external/coupons")
@Tag(name = "External coupons", description = "POS and e-comm validate / redeem. Auth: X-Api-Key or JWT.")
public class ExternalCouponController {

    private final CouponRedemptionService couponRedemptionService;

    public ExternalCouponController(CouponRedemptionService couponRedemptionService) {
        this.couponRedemptionService = couponRedemptionService;
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate a serialized coupon without marking it used")
    public ExternalCouponResponse validate(@Valid @RequestBody ExternalCouponRequest request) {
        return couponRedemptionService.validate(request);
    }

    @PostMapping("/redeem")
    @Operation(summary = "Validate a serialized coupon and mark it used")
    public ExternalCouponResponse redeem(@Valid @RequestBody ExternalCouponRequest request) {
        return couponRedemptionService.redeem(request);
    }
}
