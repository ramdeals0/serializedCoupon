package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class IdempotencyConflictException extends ApiException {

    public IdempotencyConflictException(String message) {
        super(
                HttpStatus.CONFLICT,
                "Idempotency conflict",
                "https://api.serializedcoupon.local/problems/idempotency-conflict",
                message
        );
    }
}
