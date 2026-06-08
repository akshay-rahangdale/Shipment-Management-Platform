package com.shipment.shipmentservice.service;

import com.shipment.shipmentservice.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.dto.response.ShipmentSummaryResponse;
import com.shipment.shipmentservice.kafka.producer.ShipmentEventProducer;
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

    private final ShipmentRepository     shipmentRepository;
    private final CustomerRepository     customerRepository;
    private final CarrierRepository      carrierRepository;
    private final SlaPolicyRepository    slaPolicyRepository;
    private final ShipmentEventRepository eventRepository;
    private final ShipmentMapper         mapper;
    private final TrackingNumberGenerator trackingNumberGenerator;
    private final ShipmentEventProducer   eventProducer;
    
    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────

    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        log.info("Creating shipment for sender={} recipient={} carrier={}",
            request.getSenderId(), request.getRecipientId(), request.getCarrierId());

        Customer sender    = findCustomerOrThrow(request.getSenderId());
        Customer recipient = findCustomerOrThrow(request.getRecipientId());
        Carrier  carrier   = findCarrierOrThrow(request.getCarrierId());

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

    // ─────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Cacheable(value = "shipments", key = "#trackingNumber")
    public ShipmentResponse getByTrackingNumber(String trackingNumber) {
        log.debug("Fetching shipment trackingNumber={}", trackingNumber);

        Shipment shipment = shipmentRepository
            .findByTrackingNumberWithDetails(trackingNumber)
            .orElseThrow(() -> new EntityNotFoundException(
                "Shipment not found: " + trackingNumber));

        return mapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "shipments", key = "#id")
    public ShipmentResponse getById(UUID id) {
        Shipment shipment = findShipmentOrThrow(id);
        return mapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentSummaryResponse> getShipmentsBySender(UUID senderId, Pageable pageable) {
        return shipmentRepository
            .findBySenderId(senderId, pageable)
            .map(mapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentSummaryResponse> getShipmentsByStatus(ShipmentStatus status, Pageable pageable) {
        return shipmentRepository
            .findByStatus(status, pageable)
            .map(mapper::toSummaryResponse);
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "shipments", key = "#id")
    public ShipmentResponse updateShipment(UUID id, UpdateShipmentRequest request) {
        Shipment shipment = findShipmentOrThrow(id);

        if (shipment.getStatus().isTerminal()) {
            throw new IllegalStateException(
                "Cannot update shipment in terminal status: " + shipment.getStatus());
        }

        ShipmentStatus previousStatus = shipment.getStatus();

        mapper.updateEntity(request, shipment);

        if (request.getStatus() != null && request.getStatus() != previousStatus) {
            handleStatusTransition(shipment, previousStatus, request.getStatus(), request.getUpdateReason());
        }

        Shipment updated = shipmentRepository.save(shipment);

        log.info("Shipment updated id={} status={}", id, updated.getStatus());
         eventProducer.publishShipmentUpdated(updated, previousStatus, request.getUpdateReason());

        return mapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "shipments", key = "#id")
    public ShipmentResponse markDelivered(UUID id) {
        Shipment shipment = findShipmentOrThrow(id);

        if (shipment.getStatus().isTerminal()) {
            throw new IllegalStateException(
                "Shipment already in terminal status: " + shipment.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();

        shipmentRepository.markAsDelivered(id, ShipmentStatus.DELIVERED, now);

        ShipmentEvent deliveredEvent = ShipmentEvent.builder()
            .eventType(EventType.DELIVERED)
            .description("Shipment delivered successfully")
            .occurredAt(now)
            .source("CARRIER_SYSTEM")
            .build();

        shipment.addEvent(deliveredEvent);
        shipmentRepository.save(shipment);

        log.info("Shipment marked delivered id={}", id);
        eventProducer.publishShipmentDelivered(shipment);
        return mapper.toResponse(shipment);
    }

    // ─────────────────────────────────────────────
    // AT-RISK QUERY (used by SLA Monitor)
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ShipmentSummaryResponse> getAtRiskShipments(int warningHours) {
        LocalDateTime threshold = LocalDateTime.now().plusHours(warningHours);
        List<ShipmentStatus> terminalStatuses = List.of(
            ShipmentStatus.DELIVERED,
            ShipmentStatus.CANCELLED,
            ShipmentStatus.RETURNED
        );
        return shipmentRepository
            .findAtRiskShipments(terminalStatuses, threshold)
            .stream()
            .map(mapper::toSummaryResponse)
            .toList();
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    private Customer findCustomerOrThrow(UUID id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    }

    private Carrier findCarrierOrThrow(UUID id) {
        return carrierRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Carrier not found: " + id));
    }

    private Shipment findShipmentOrThrow(UUID id) {
        return shipmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Shipment not found: " + id));
    }

    private void validateActiveSlaPolicy(UUID carrierId, String serviceTier) {
        slaPolicyRepository
            .findActivePolicy(carrierId, serviceTier, LocalDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException(
                "No active SLA policy for carrier=" + carrierId + " tier=" + serviceTier));
    }

    private void handleStatusTransition(
            Shipment shipment,
            ShipmentStatus from,
            ShipmentStatus to,
            String reason) {

        if (!from.isForwardProgress(to)) {
            throw new IllegalStateException(
                "Invalid status transition: " + from + " → " + to);
        }

        ShipmentEvent transitionEvent = ShipmentEvent.builder()
            .eventType(EventType.fromString(to.name()))
            .description(reason != null ? reason : "Status updated to " + to.getDescription())
            .occurredAt(LocalDateTime.now())
            .source("API")
            .build();

        shipment.addEvent(transitionEvent);
    }
}