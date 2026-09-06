package com.skillnet.serializedcoupon.security;

import com.skillnet.serializedcoupon.config.AppProperties;
import com.skillnet.serializedcoupon.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final AppProperties appProperties;
    private final Clock clock;
    private final SecretKey key;

    public JwtService(AppProperties appProperties, Clock clock) {
        this.appProperties = appProperties;
        this.clock = clock;
        this.key = Keys.hmacShaKeyFor(signingKeyBytes(appProperties.getAuth().getJwtSecret()));
    }

    public String createToken(String username, String displayName, UserRole role) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(appProperties.getAuth().getJwtExpiration());
        return Jwts.builder()
                .subject(username)
                .claim("displayName", displayName)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Instant expiresAt() {
        return Instant.now(clock).plus(appProperties.getAuth().getJwtExpiration());
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(Instant.now(clock)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtException("Invalid token", ex);
        }
    }

    private static byte[] signingKeyBytes(String secret) {
        byte[] raw = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 32) {
            return raw;
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(
                    (secret == null ? "serialized-coupon" : secret).getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required to derive the JWT signing key", ex);
        }
    }
}
