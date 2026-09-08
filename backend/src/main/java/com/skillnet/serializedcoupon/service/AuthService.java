package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.AppUser;
import com.skillnet.serializedcoupon.dto.AuthResponse;
import com.skillnet.serializedcoupon.dto.LoginRequest;
import com.skillnet.serializedcoupon.exception.UnauthorizedException;
import com.skillnet.serializedcoupon.repository.AppUserRepository;
import com.skillnet.serializedcoupon.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse currentUser(String username) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
        return toResponse(user);
    }

    private AuthResponse toResponse(AppUser user) {
        String token = jwtService.createToken(user.getUsername(), user.getDisplayName(), user.getRole());
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.expiresAt(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole()
        );
    }
}
