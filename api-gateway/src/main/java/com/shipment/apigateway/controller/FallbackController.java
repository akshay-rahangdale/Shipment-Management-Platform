package com.shipment.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/shipment-service")
    public ResponseEntity<Map<String, Object>> shipmentServiceFallback() {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", 503,
                "error", "Shipment Service Unavailable",
                "message", "The shipment service is temporarily unavailable. Please try again shortly.",
                "timestamp", Instant.now().toString()
            ));
    }

    @GetMapping("/tracking-service")
    public ResponseEntity<Map<String, Object>> trackingServiceFallback() {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", 503,
                "error", "Tracking Service Unavailable",
                "message", "The tracking service is temporarily unavailable. Please try again shortly.",
                "timestamp", Instant.now().toString()
            ));
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", 503,
                "error", "Notification Service Unavailable",
                "message", "The notification service is temporarily unavailable.",
                "timestamp", Instant.now().toString()
            ));
    }
}