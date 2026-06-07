package com.shipment.shipmentservice.service;

import com.shipment.shipmentservice.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.dto.response.ShipmentSummaryResponse;
import com.shipment.shipmentservice.mapper.ShipmentMapper;
import com.shipment.shipmentservice.model.*;
import com.shipment.shipmentservice.model.enums.EventType;
import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import com.shipment.shipmentservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {


    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final CarrierRepository carrierRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final ShipmentEventRepository eventRepository;
    private final ShipmentMapper mapper;
    private final TrackingNumberGenerator trackingNumberGenerator;


    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        log.info("Creating shipment for sender={} recipient={} carrier={}",
                request.getSenderId(), request.getRecipientId(), request.getCarrierId());

        Customer sender = findCustomerOrThrow(request.getSenderId());
        Customer recipient = findCustomerOrThrow(request.getRecipientId());
        Carrier carrier = findCarrierOrThrow(request.getCarrierId());

        validateActiveSlaPolicy(carrier.getId(), request.getServiceTier());

        Shipment shipment = mapper.toEntity(request);
        shipment.setTrackingNumber(trackingNumberGenerator.generate());
        shipment.setSender(sender);
        shipment.setRecipient(recipient);
        shipment.setCarrier(carrier);
        shipment.setStatus(ShipmentStatus.PENDING);

        ShipmentEvent createdEvent = ShipmentEvent.builder()
                .eventType(EventType.SHIPMENT_CREATED)
                .description("Shipment created")
                .occurredAt(LocalDateTime.now())
                .source("SYSTEM")
                .build();

        shipment.addEvent(createdEvent);

        Shipment saved = shipmentRepository.save(shipment);

        log.info("Shipment created trackingNumber={}", saved.getTrackingNumber());

        return mapper.toResponse(saved);
    }
}
