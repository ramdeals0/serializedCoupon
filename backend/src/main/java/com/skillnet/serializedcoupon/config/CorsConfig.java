package com.skillnet.serializedcoupon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public CorsConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        AppProperties.Cors cors = appProperties.getCors();
        String[] origins = CorsOrigins.resolve(
                cors.getAllowedOrigins(),
                cors.getPublicDomain(),
                cors.getStaticUrl());
        if (origins.length == 0) {
            origins = new String[] {"http://localhost:4200", "http://127.0.0.1:4200"};
        }
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition", "Location")
                .allowCredentials(true);
    }
}
