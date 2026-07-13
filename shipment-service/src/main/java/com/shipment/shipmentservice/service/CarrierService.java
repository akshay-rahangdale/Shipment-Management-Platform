package com.shipment.shipmentservice.service;

import com.shipment.shipmentservice.dto.response.CarrierResponse;
import com.shipment.shipmentservice.mapper.ShipmentMapper;
import com.shipment.shipmentservice.model.Carrier;
import com.shipment.shipmentservice.repository.CarrierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CarrierService {

    private final CarrierRepository carrierRepository;
    private final ShipmentMapper mapper;

    /**
     * Active carriers only — this is the list a "create shipment" form should
     * populate its carrier dropdown from, since inactive carriers shouldn't be
     * assignable to new shipments.
     */
    public List<CarrierResponse> getActiveCarriers() {
        return carrierRepository.findByActiveTrue()
            .stream()
            .map(mapper::toCarrierResponse)
            .toList();
    }

    public CarrierResponse getById(UUID id) {
        Carrier carrier = carrierRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Carrier not found: " + id));

        return mapper.toCarrierResponse(carrier);
    }
}