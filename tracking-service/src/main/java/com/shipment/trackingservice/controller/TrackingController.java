package com.shipment.trackingservice.controller;

import com.shipment.trackingservice.dto.request.CheckpointUpdateRequest;
import com.shipment.trackingservice.dto.response.TrackingResponse;
import com.shipment.trackingservice.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<TrackingResponse> getTracking(
            @PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getByTrackingNumber(trackingNumber));
    }

    @PostMapping("/{trackingNumber}/checkpoints")
    public ResponseEntity<TrackingResponse> addCheckpoint(
            @PathVariable String trackingNumber,
            @Valid @RequestBody CheckpointUpdateRequest request) {
        return ResponseEntity.ok(trackingService.addCheckpoint(trackingNumber, request));
    }
}