package com.skillnet.serializedcoupon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rms_coupon_definition")
public class RmsCouponDefinition extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rms_coupon_id", nullable = false, unique = true, length = 64)
    private String rmsCouponId;

    @Column(name = "rms_coupon_code", length = 64)
    private String rmsCouponCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "rmsCouponDefinition")
    private List<CouponBatch> batches = new ArrayList<>();

    @OneToMany(mappedBy = "rmsCouponDefinition")
    private List<SerializedCoupon> serializedCoupons = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRmsCouponId() {
        return rmsCouponId;
    }

    public void setRmsCouponId(String rmsCouponId) {
        this.rmsCouponId = rmsCouponId;
    }

    public String getRmsCouponCode() {
        return rmsCouponCode;
    }

    public void setRmsCouponCode(String rmsCouponCode) {
        this.rmsCouponCode = rmsCouponCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<CouponBatch> getBatches() {
        return batches;
    }

    public void setBatches(List<CouponBatch> batches) {
        this.batches = batches;
    }

    public List<SerializedCoupon> getSerializedCoupons() {
        return serializedCoupons;
    }

    public void setSerializedCoupons(List<SerializedCoupon> serializedCoupons) {
        this.serializedCoupons = serializedCoupons;
    }
}
