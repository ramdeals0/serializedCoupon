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
@Table(name = "coupon")
public class Coupon extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rms_coupon_definition_id", nullable = false)
    private RmsCouponDefinition rmsCouponDefinition;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "usage_limit", nullable = false)
    private int usageLimit = 1;

    @Column(name = "pos_code", length = 64)
    private String posCode;

    @Column(name = "atg_code", length = 64)
    private String atgCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_source", nullable = false, length = 16)
    private CouponSource couponSource = CouponSource.BOTH;

    @Column(name = "coupon_program_code", nullable = false, length = 4)
    private String couponProgramCode;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CouponStatus status = CouponStatus.ACTIVE;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "external_reference", length = 128)
    private String externalReference;

    @OneToMany(mappedBy = "coupon")
    private List<CouponBatch> batches = new ArrayList<>();

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public String getPosCode() {
        return posCode;
    }

    public void setPosCode(String posCode) {
        this.posCode = posCode;
    }

    public String getAtgCode() {
        return atgCode;
    }

    public void setAtgCode(String atgCode) {
        this.atgCode = atgCode;
    }

    public CouponSource getCouponSource() {
        return couponSource;
    }

    public void setCouponSource(CouponSource couponSource) {
        this.couponSource = couponSource;
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

    public CouponStatus getStatus() {
        return status;
    }

    public void setStatus(CouponStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public List<CouponBatch> getBatches() {
        return batches;
    }

    public void setBatches(List<CouponBatch> batches) {
        this.batches = batches;
    }
}
