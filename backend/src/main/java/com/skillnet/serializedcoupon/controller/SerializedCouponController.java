package com.skillnet.serializedcoupon.controller;

import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.dto.CouponValidationResponse;
import com.skillnet.serializedcoupon.dto.PageResponse;
import com.skillnet.serializedcoupon.dto.SerializedCouponResponse;
import com.skillnet.serializedcoupon.service.CouponValidationService;
import com.skillnet.serializedcoupon.service.SerializedCouponService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/serialized-coupons")
public class SerializedCouponController {

    private final SerializedCouponService serializedCouponService;
    private final CouponValidationService couponValidationService;

    public SerializedCouponController(
            SerializedCouponService serializedCouponService,
            CouponValidationService couponValidationService
    ) {
        this.serializedCouponService = serializedCouponService;
        this.couponValidationService = couponValidationService;
    }

    @GetMapping
    public PageResponse<SerializedCouponResponse> search(
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String rmsCouponId,
            @RequestParam(required = false) String couponProgramCode,
            @RequestParam(required = false) UUID batchId,
            @RequestParam(required = false) SerializedCouponStatus status,
            @RequestParam(required = false) Instant validAt,
            @RequestParam(required = false) Instant startFrom,
            @RequestParam(required = false) Instant expirationTo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        String search = query != null ? query : couponCode;
        return serializedCouponService.search(
                search,
                rmsCouponId,
                couponProgramCode,
                batchId,
                status,
                validAt,
                startFrom,
                expirationTo,
                pageable
        );
    }

    @GetMapping("/{couponCode}")
    public SerializedCouponResponse get(@PathVariable String couponCode) {
        return serializedCouponService.getByCode(couponCode);
    }

    @PostMapping("/{couponCode}/validate")
    public CouponValidationResponse validate(@PathVariable String couponCode) {
        return couponValidationService.validate(couponCode);
    }

    @PostMapping("/{couponCode}/deactivate")
    public SerializedCouponResponse deactivate(@PathVariable String couponCode) {
        return serializedCouponService.deactivate(couponCode);
    }
}
