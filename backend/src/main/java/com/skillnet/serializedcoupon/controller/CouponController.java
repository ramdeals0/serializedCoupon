package com.skillnet.serializedcoupon.controller;

import com.skillnet.serializedcoupon.domain.CouponStatus;
import com.skillnet.serializedcoupon.dto.CouponResponse;
import com.skillnet.serializedcoupon.dto.CreateCouponRequest;
import com.skillnet.serializedcoupon.dto.PageResponse;
import com.skillnet.serializedcoupon.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@Valid @RequestBody CreateCouponRequest request) {
        return couponService.create(request);
    }

    @GetMapping
    public PageResponse<CouponResponse> search(
            @RequestParam(required = false) String rmsCouponId,
            @RequestParam(required = false) String couponProgramCode,
            @RequestParam(required = false) CouponStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return couponService.search(rmsCouponId, couponProgramCode, status, pageable);
    }

    @GetMapping("/{couponId}")
    public CouponResponse get(@PathVariable UUID couponId) {
        return couponService.get(couponId);
    }
}
