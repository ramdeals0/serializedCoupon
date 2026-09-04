package com.skillnet.serializedcoupon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.coupon.generation")
public class CouponGenerationProperties {

    private int maxBatchSize = 10_000;
    private int minBatchSize = 1;
    private int collisionRetryLimit = 20;
    private int persistenceChunkSize = 100;
    private int sampleSize = 25;

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getMinBatchSize() {
        return minBatchSize;
    }

    public void setMinBatchSize(int minBatchSize) {
        this.minBatchSize = minBatchSize;
    }

    public int getCollisionRetryLimit() {
        return collisionRetryLimit;
    }

    public void setCollisionRetryLimit(int collisionRetryLimit) {
        this.collisionRetryLimit = collisionRetryLimit;
    }

    public int getPersistenceChunkSize() {
        return persistenceChunkSize;
    }

    public void setPersistenceChunkSize(int persistenceChunkSize) {
        this.persistenceChunkSize = persistenceChunkSize;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }
}
