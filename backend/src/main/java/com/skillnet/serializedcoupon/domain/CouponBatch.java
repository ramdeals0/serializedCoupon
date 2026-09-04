package com.skillnet.serializedcoupon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "coupon_batch")
public class CouponBatch extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rms_coupon_definition_id", nullable = false)
    private RmsCouponDefinition rmsCouponDefinition;

    @Column(name = "coupon_program_code", nullable = false, length = 4)
    private String couponProgramCode;

    @Column(name = "requested_quantity", nullable = false)
    private int requestedQuantity;

    @Column(name = "generated_quantity", nullable = false)
    private int generatedQuantity;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CouponBatchStatus status = CouponBatchStatus.PENDING;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "idempotency_key", unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "request_fingerprint", length = 64)
    private String requestFingerprint;

    @Column(name = "external_reference", length = 128)
    private String externalReference;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @OneToMany(mappedBy = "couponBatch")
    private List<SerializedCoupon> coupons = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RmsCouponDefinition getRmsCouponDefinition() {
        return rmsCouponDefinition;
    }

    public void setRmsCouponDefinition(RmsCouponDefinition rmsCouponDefinition) {
        this.rmsCouponDefinition = rmsCouponDefinition;
    }

    public String getCouponProgramCode() {
        return couponProgramCode;
    }

    public void setCouponProgramCode(String couponProgramCode) {
        this.couponProgramCode = couponProgramCode;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public int getGeneratedQuantity() {
        return generatedQuantity;
    }

    public void setGeneratedQuantity(int generatedQuantity) {
        this.generatedQuantity = generatedQuantity;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public CouponBatchStatus getStatus() {
        return status;
    }

    public void setStatus(CouponBatchStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestFingerprint() {
        return requestFingerprint;
    }

    public void setRequestFingerprint(String requestFingerprint) {
        this.requestFingerprint = requestFingerprint;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<SerializedCoupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<SerializedCoupon> coupons) {
        this.coupons = coupons;
    }
}
