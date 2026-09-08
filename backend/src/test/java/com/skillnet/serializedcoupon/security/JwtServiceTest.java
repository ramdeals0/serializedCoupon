package com.skillnet.serializedcoupon.security;

import com.skillnet.serializedcoupon.config.AppProperties;
import com.skillnet.serializedcoupon.domain.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void roundTripsUsernameAndRole() {
        AppProperties properties = new AppProperties();
        properties.getAuth().setJwtSecret("unit-test-jwt-secret-must-be-32b");
        Clock clock = Clock.fixed(Instant.parse("2026-09-06T12:00:00Z"), ZoneOffset.UTC);
        JwtService jwtService = new JwtService(properties, clock);

        String token = jwtService.createToken("manager", "Manager", UserRole.MANAGER);
        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("manager");
        assertThat(claims.get("role", String.class)).isEqualTo("MANAGER");
        assertThat(claims.get("displayName", String.class)).isEqualTo("Manager");
        assertThat(jwtService.expiresAt()).isEqualTo(Instant.parse("2026-09-06T20:00:00Z"));
    }
}
