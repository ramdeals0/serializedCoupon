package com.skillnet.serializedcoupon.dto;

import com.skillnet.serializedcoupon.domain.UserRole;

import java.time.Instant;

public record AuthResponse(
        String token,
        String tokenType,
        Instant expiresAt,
        String username,
        String displayName,
        UserRole role
) {
}
