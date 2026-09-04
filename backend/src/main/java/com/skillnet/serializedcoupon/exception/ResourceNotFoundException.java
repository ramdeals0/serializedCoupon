package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "Resource not found", "https://api.serializedcoupon.local/problems/not-found", message);
    }
}
