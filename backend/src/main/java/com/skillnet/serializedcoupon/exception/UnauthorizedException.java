package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "https://api.serializedcoupon.local/problems/unauthorized",
                message
        );
    }
}
