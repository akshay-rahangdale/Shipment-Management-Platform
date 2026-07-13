package com.shipment.shipmentservice.controller;

import com.shipment.shipmentservice.dto.response.CustomerResponse;
import com.shipment.shipmentservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getCustomers(
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {

        return ResponseEntity.ok(customerService.getCustomers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getById(id));
    }
}