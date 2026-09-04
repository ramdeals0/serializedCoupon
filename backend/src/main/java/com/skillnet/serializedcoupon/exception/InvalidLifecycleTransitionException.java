package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class InvalidLifecycleTransitionException extends ApiException {

    public InvalidLifecycleTransitionException(String message) {
        super(
                HttpStatus.CONFLICT,
                "Invalid lifecycle transition",
                "https://api.serializedcoupon.local/problems/invalid-lifecycle-transition",
                message
        );
    }
}
