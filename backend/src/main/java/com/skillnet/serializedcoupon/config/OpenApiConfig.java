package com.skillnet.serializedcoupon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serializedCouponOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Serialized Coupon API")
                        .version("v1")
                        .description("Create and manage serialized coupons linked to RMS coupon definitions. "
                                + "All timestamps are ISO-8601 UTC."));
    }
}
