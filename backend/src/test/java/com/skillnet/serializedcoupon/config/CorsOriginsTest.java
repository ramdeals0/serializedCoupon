package com.skillnet.serializedcoupon.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsOriginsTest {

    @Test
    void splitsCommaAndWhitespaceSeparatedOriginsAndAddsRailwayHosts() {
        String[] origins = CorsOrigins.resolve(
                List.of("https://serializedcoupon.up.railway.app https://serializedcoupon-production.up.railway.app"),
                "serializedcoupon-production.up.railway.app",
                "serializedcoupon-production.up.railway.app/");

        assertThat(origins).containsExactly(
                "https://serializedcoupon.up.railway.app",
                "https://serializedcoupon-production.up.railway.app");
    }

    @Test
    void keepsExplicitLocalOrigins() {
        String[] origins = CorsOrigins.resolve(
                List.of("http://localhost:4200", "http://127.0.0.1:4200"));

        assertThat(origins).containsExactly("http://localhost:4200", "http://127.0.0.1:4200");
    }
}
