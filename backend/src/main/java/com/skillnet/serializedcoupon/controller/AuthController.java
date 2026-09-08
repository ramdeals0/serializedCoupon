package com.skillnet.serializedcoupon.controller;

import com.skillnet.serializedcoupon.dto.AuthResponse;
import com.skillnet.serializedcoupon.dto.LoginRequest;
import com.skillnet.serializedcoupon.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }
}
