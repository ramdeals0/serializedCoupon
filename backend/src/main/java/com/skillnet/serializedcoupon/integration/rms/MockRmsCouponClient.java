package com.skillnet.serializedcoupon.integration.rms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.rms.client", havingValue = "mock", matchIfMissing = true)
public class MockRmsCouponClient implements RmsCouponClient {

    private static final Logger log = LoggerFactory.getLogger(MockRmsCouponClient.class);

    private final Map<String, RmsCouponDetails> coupons;

    public MockRmsCouponClient() {
        List<RmsCouponDetails> seed = List.of(
                new RmsCouponDetails(
                        "RMS-COUPON-1001",
                        "FALL26",
                        "Fall 2026 BOGO",
                        "Buy one get one serialized coupon program",
                        true
                ),
                new RmsCouponDetails(
                        "RMS-COUPON-1002",
                        "SAVE10",
                        "Save $10",
                        "Ten dollar off serialized coupon",
                        true
                ),
                new RmsCouponDetails(
                        "RMS-COUPON-1003",
                        "VIP25",
                        "VIP 25 percent",
                        "VIP percentage discount",
                        true
                ),
                new RmsCouponDetails(
                        "RMS-COUPON-INACTIVE",
                        "OLD99",
                        "Retired promotion",
                        "Inactive RMS coupon used for negative tests",
                        false
                )
        );
        this.coupons = seed.stream().collect(Collectors.toUnmodifiableMap(RmsCouponDetails::rmsCouponId, Function.identity()));
        log.info("Mock RMS client initialized with {} coupon definitions", this.coupons.size());
    }

    @Override
    public Optional<RmsCouponDetails> getCouponDefinition(String rmsCouponId) {
        return Optional.ofNullable(coupons.get(rmsCouponId));
    }

    @Override
    public boolean couponDefinitionExistsAndIsActive(String rmsCouponId) {
        return getCouponDefinition(rmsCouponId).map(RmsCouponDetails::active).orElse(false);
    }

    public List<RmsCouponDetails> listAll() {
        return List.copyOf(coupons.values());
    }
}
