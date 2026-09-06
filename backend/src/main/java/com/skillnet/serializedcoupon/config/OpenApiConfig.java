package com.skillnet.serializedcoupon.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serializedCouponOpenApi() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Api-Key");
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearer)
                        .addSecuritySchemes("apiKeyAuth", apiKey))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("apiKeyAuth"))
                .info(new Info()
                        .title("Serialized Coupon API")
                        .version("v1")
                        .description("Create and manage serialized coupons. Authenticate with POST /api/v1/auth/login "
                                + "and send Authorization: Bearer <token>. External POS/e-comm systems can also call "
                                + "POST /api/v1/external/coupons/validate and POST /api/v1/external/coupons/redeem "
                                + "with header X-Api-Key. All timestamps are ISO-8601 UTC."));
    }
}
