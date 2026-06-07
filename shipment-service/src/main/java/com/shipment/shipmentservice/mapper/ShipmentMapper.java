package com.shipment.shipmentservice.mapper;

import com.shipment.shipmentservice.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.dto.response.*;
import com.shipment.shipmentservice.model.*;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ShipmentMapper {

    // ─────────────────────────────────────────────
    // Customer
    // ─────────────────────────────────────────────

    CustomerResponse toCustomerResponse(Customer customer);

    // ─────────────────────────────────────────────
    // Carrier
    // ─────────────────────────────────────────────

    CarrierResponse toCarrierResponse(Carrier carrier);

    // ─────────────────────────────────────────────
    // Shipment → Response
    // ─────────────────────────────────────────────

    @Mapping(target = "statusDescription", expression = "java(shipment.getStatus().getDescription())")
    @Mapping(target = "slaAtRisk", expression = "java(shipment.isSlaAtRisk(3))")
    @Mapping(target = "transitDays", expression = "java(shipment.getTransitDays())")
    ShipmentResponse toResponse(Shipment shipment);

    @Mapping(target = "statusDescription", expression = "java(shipment.getStatus().getDescription())")
    @Mapping(target = "slaAtRisk", expression = "java(shipment.isSlaAtRisk(3))")
    @Mapping(target = "recipientName", expression = "java(shipment.getRecipient().getName())")
    ShipmentSummaryResponse toSummaryResponse(Shipment shipment);

    // ─────────────────────────────────────────────
    // CreateShipmentRequest → Shipment
    // ─────────────────────────────────────────────

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "recipient", ignore = true)
    @Mapping(target = "carrier", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "actualDelivery", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Shipment toEntity(CreateShipmentRequest request);

    // ─────────────────────────────────────────────
    // Partial update — only non-null fields applied
    // ─────────────────────────────────────────────

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "carrier", ignore = true)
    @Mapping(target = "weightKg", ignore = true)
    @Mapping(target = "declaredValue", ignore = true)
    @Mapping(target = "originAddress", ignore = true)
    @Mapping(target = "actualDelivery", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            com.shipment.shipmentservice.dto.request.UpdateShipmentRequest request,
            @MappingTarget Shipment shipment
    );
}