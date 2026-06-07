package com.shipment.shipmentservice.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());

        problem.setType(URI.create("https://shipment-platform.com/errors/not-found"));
        problem.setTitle("Resource Not Found");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, ex.getMessage());

        problem.setType(URI.create("https://shipment-platform.com/errors/conflict"));
        problem.setTitle("Operation Not Allowed");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());

        problem.setType(URI.create("https://shipment-platform.com/errors/bad-request"));
        problem.setTitle("Invalid Request");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage(),
                (existing, replacement) -> existing
            ));

        log.warn("Validation failed: {}", fieldErrors);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Request validation failed");

        problem.setType(URI.create("https://shipment-platform.com/errors/validation"));
        problem.setTitle("Validation Error");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errors", fieldErrors);

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

        problem.setType(URI.create("https://shipment-platform.com/errors/internal"));
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}