package com.skillnet.serializedcoupon.integration.rms;

import com.skillnet.serializedcoupon.service.RmsCouponDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.rms.client", havingValue = "mock", matchIfMissing = true)
public class MockRmsCatalogSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MockRmsCatalogSeeder.class);

    private final MockRmsCouponClient mockRmsCouponClient;
    private final RmsCouponDefinitionService rmsCouponDefinitionService;

    public MockRmsCatalogSeeder(
            MockRmsCouponClient mockRmsCouponClient,
            RmsCouponDefinitionService rmsCouponDefinitionService
    ) {
        this.mockRmsCouponClient = mockRmsCouponClient;
        this.rmsCouponDefinitionService = rmsCouponDefinitionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        mockRmsCouponClient.listAll().forEach(rmsCouponDefinitionService::synchronize);
        log.info("Synchronized {} mock RMS coupon definitions into the local catalog", mockRmsCouponClient.listAll().size());
    }
}
