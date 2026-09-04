package com.skillnet.serializedcoupon.integration.rms;

import java.util.Optional;

public interface RmsCouponClient {

    Optional<RmsCouponDetails> getCouponDefinition(String rmsCouponId);

    boolean couponDefinitionExistsAndIsActive(String rmsCouponId);
}
