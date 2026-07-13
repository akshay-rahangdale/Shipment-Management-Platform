package com.shipment.shipmentservice.controller;

import com.shipment.shipmentservice.dto.response.CarrierResponse;
import com.shipment.shipmentservice.service.CarrierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/carriers")
@RequiredArgsConstructor
public class CarrierController {

    private final CarrierService carrierService;

    @GetMapping
    public ResponseEntity<List<CarrierResponse>> getActiveCarriers() {
        return ResponseEntity.ok(carrierService.getActiveCarriers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarrierResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(carrierService.getById(id));
    }
}