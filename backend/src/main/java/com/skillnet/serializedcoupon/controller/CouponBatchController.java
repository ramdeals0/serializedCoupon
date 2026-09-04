package com.skillnet.serializedcoupon.controller;

import com.skillnet.serializedcoupon.domain.CouponBatchStatus;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.dto.CouponBatchResponse;
import com.skillnet.serializedcoupon.dto.CreateCouponBatchRequest;
import com.skillnet.serializedcoupon.dto.PageResponse;
import com.skillnet.serializedcoupon.dto.SerializedCouponResponse;
import com.skillnet.serializedcoupon.service.CouponBatchService;
import com.skillnet.serializedcoupon.service.CouponExportService;
import com.skillnet.serializedcoupon.service.SerializedCouponService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coupon-batches")
public class CouponBatchController {

    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final CouponBatchService couponBatchService;
    private final SerializedCouponService serializedCouponService;
    private final CouponExportService couponExportService;

    public CouponBatchController(
            CouponBatchService couponBatchService,
            SerializedCouponService serializedCouponService,
            CouponExportService couponExportService
    ) {
        this.couponBatchService = couponBatchService;
        this.serializedCouponService = serializedCouponService;
        this.couponExportService = couponExportService;
    }

    @PostMapping
    public ResponseEntity<CouponBatchResponse> create(
            @Valid @RequestBody CreateCouponBatchRequest request,
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey
    ) {
        CouponBatchService.BatchCreationResult result = couponBatchService.createBatch(request, idempotencyKey);
        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(result.response());
    }

    @GetMapping
    public PageResponse<CouponBatchResponse> search(
            @RequestParam(required = false) String rmsCouponId,
            @RequestParam(required = false) String couponProgramCode,
            @RequestParam(required = false) CouponBatchStatus status,
            @RequestParam(required = false) Instant createdFrom,
            @RequestParam(required = false) Instant createdTo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return couponBatchService.search(rmsCouponId, couponProgramCode, status, createdFrom, createdTo, pageable);
    }

    @GetMapping("/{batchId}")
    public CouponBatchResponse get(@PathVariable UUID batchId) {
        return couponBatchService.getBatch(batchId);
    }

    @GetMapping("/{batchId}/coupons")
    public PageResponse<SerializedCouponResponse> listCoupons(
            @PathVariable UUID batchId,
            @RequestParam(required = false) SerializedCouponStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Instant activeAt,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return serializedCouponService.searchByBatch(batchId, status, query, activeAt, pageable);
    }

    @GetMapping(value = "/{batchId}/export", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> export(@PathVariable UUID batchId) {
        couponBatchService.getBatch(batchId);
        String filename = "serialized-coupons-" + batchId + ".csv";
        StreamingResponseBody body = outputStream -> couponExportService.writeBatchCsv(
                batchId,
                new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
