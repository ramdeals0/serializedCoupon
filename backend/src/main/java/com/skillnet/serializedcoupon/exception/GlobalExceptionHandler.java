package com.skillnet.serializedcoupon.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex, HttpServletRequest request) {
        log.warn("API error [{}] {}: {}", ex.getStatus().value(), ex.getTitle(), ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        detail.setTitle(ex.getTitle());
        detail.setType(URI.create(ex.getType()));
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("timestamp", Instant.now(clock).toString());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage()
                ))
                .toList();
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        detail.setTitle("Validation failed");
        detail.setType(URI.create("https://api.serializedcoupon.local/problems/validation"));
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("errors", errors);
        detail.setProperty("timestamp", Instant.now(clock).toString());
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(violation -> Map.of(
                        "field", violation.getPropertyPath().toString(),
                        "message", violation.getMessage()
                ))
                .toList();
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        detail.setTitle("Validation failed");
        detail.setType(URI.create("https://api.serializedcoupon.local/problems/validation"));
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("errors", errors);
        detail.setProperty("timestamp", Instant.now(clock).toString());
        return detail;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request");
        detail.setTitle("Malformed request");
        detail.setType(URI.create("https://api.serializedcoupon.local/problems/malformed-request"));
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("timestamp", Instant.now(clock).toString());
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        detail.setTitle("Internal server error");
        detail.setType(URI.create("https://api.serializedcoupon.local/problems/internal-error"));
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("timestamp", Instant.now(clock).toString());
        return detail;
    }
}
