package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class BusinessValidationException extends ApiException {

    public BusinessValidationException(String message) {
        super(
                HttpStatus.BAD_REQUEST,
                "Business validation failed",
                "https://api.serializedcoupon.local/problems/business-validation",
                message
        );
    }
}
