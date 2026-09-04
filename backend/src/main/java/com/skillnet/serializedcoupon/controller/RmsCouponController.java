package com.skillnet.serializedcoupon.controller;

import com.skillnet.serializedcoupon.dto.PageResponse;
import com.skillnet.serializedcoupon.dto.RmsCouponDefinitionResponse;
import com.skillnet.serializedcoupon.service.RmsCouponDefinitionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rms-coupons")
public class RmsCouponController {

    private final RmsCouponDefinitionService rmsCouponDefinitionService;

    public RmsCouponController(RmsCouponDefinitionService rmsCouponDefinitionService) {
        this.rmsCouponDefinitionService = rmsCouponDefinitionService;
    }

    @GetMapping
    public PageResponse<RmsCouponDefinitionResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return rmsCouponDefinitionService.search(query, active, pageable);
    }

    @GetMapping("/{rmsCouponId}")
    public RmsCouponDefinitionResponse get(@PathVariable String rmsCouponId) {
        return rmsCouponDefinitionService.getByRmsCouponId(rmsCouponId);
    }
}
