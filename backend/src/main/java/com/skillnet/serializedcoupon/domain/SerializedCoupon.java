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
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "serialized_coupon")
public class SerializedCoupon extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_batch_id", nullable = false)
    private CouponBatch couponBatch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rms_coupon_definition_id", nullable = false)
    private RmsCouponDefinition rmsCouponDefinition;

    @Column(name = "coupon_code", nullable = false, unique = true, length = 14)
    private String couponCode;

    @Column(name = "coupon_program_code", nullable = false, length = 4)
    private String couponProgramCode;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SerializedCouponStatus status;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @Column(name = "times_used")
    private Integer timesUsed = 0;

    @Column(name = "external_reference", length = 128)
    private String externalReference;

    @Version
    @Column(nullable = false)
    private Long version;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CouponBatch getCouponBatch() {
        return couponBatch;
    }

    public void setCouponBatch(CouponBatch couponBatch) {
        this.couponBatch = couponBatch;
    }

    public RmsCouponDefinition getRmsCouponDefinition() {
        return rmsCouponDefinition;
    }

    public void setRmsCouponDefinition(RmsCouponDefinition rmsCouponDefinition) {
        this.rmsCouponDefinition = rmsCouponDefinition;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCouponProgramCode() {
        return couponProgramCode;
    }

    public void setCouponProgramCode(String couponProgramCode) {
        this.couponProgramCode = couponProgramCode;
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

    public SerializedCouponStatus getStatus() {
        return status;
    }

    public void setStatus(SerializedCouponStatus status) {
        this.status = status;
    }

    public Instant getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(Instant redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public Instant getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(Instant deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public int getTimesUsed() {
        return timesUsed == null ? 0 : timesUsed;
    }

    public void setTimesUsed(int timesUsed) {
        this.timesUsed = timesUsed;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
