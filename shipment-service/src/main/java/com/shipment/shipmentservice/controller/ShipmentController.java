package com.shipment.shipmentservice.controller;

import com.shipment.shipmentservice.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.dto.response.ShipmentSummaryResponse;
import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import com.shipment.shipmentservice.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;

    
    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request) {

        ShipmentResponse response = shipmentService.createShipment(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getByTrackingNumber(
            @PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.getByTrackingNumber(trackingNumber));
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentSummaryResponse>> getShipmentsBySender(
            @RequestParam UUID senderId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(shipmentService.getShipmentsBySender(senderId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ShipmentSummaryResponse>> getByStatus(
            @PathVariable ShipmentStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(shipmentService.getShipmentsByStatus(status, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponse> updateShipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentRequest request) {

        return ResponseEntity.ok(shipmentService.updateShipment(id, request));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<ShipmentResponse> markDelivered(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.markDelivered(id));
    }

    @GetMapping("/at-risk")
    public ResponseEntity<List<ShipmentSummaryResponse>> getAtRiskShipments(
            @RequestParam(defaultValue = "3") int warningHours) {

        return ResponseEntity.ok(shipmentService.getAtRiskShipments(warningHours));
    }
}