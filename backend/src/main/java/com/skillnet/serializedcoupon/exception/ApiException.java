package com.skillnet.serializedcoupon.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String title;
    private final String type;

    public ApiException(HttpStatus status, String title, String type, String message) {
        super(message);
        this.status = status;
        this.title = title;
        this.type = type;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }
}
