package com.shipment.shipmentservice.repository;

import com.shipment.shipmentservice.model.ShipmentEvent;
import com.shipment.shipmentservice.model.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, UUID> {

    List<ShipmentEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);

    List<ShipmentEvent> findByShipmentIdAndEventType(UUID shipmentId, EventType eventType);

    long countByShipmentIdAndEventType(UUID shipmentId, EventType eventType);

    boolean existsByShipmentIdAndEventType(UUID shipmentId, EventType eventType);

    @Query("""
        SELECT e FROM ShipmentEvent e
        WHERE e.shipment.id = :shipmentId
          AND e.occurredAt >= :since
        ORDER BY e.occurredAt ASC
        """)
    List<ShipmentEvent> findRecentEvents(UUID shipmentId, LocalDateTime since);

    @Query("""
        SELECT e FROM ShipmentEvent e
        WHERE e.shipment.id = :shipmentId
          AND e.eventType IN :types
        ORDER BY e.occurredAt ASC
        """)
    List<ShipmentEvent> findByShipmentIdAndEventTypes(UUID shipmentId, List<EventType> types);

    @Query("""
        SELECT COUNT(e) FROM ShipmentEvent e
        WHERE e.shipment.id = :shipmentId
          AND e.eventType IN :types
          AND e.occurredAt >= :since
        """)
    long countExceptionEventsSince(UUID shipmentId, List<EventType> types, LocalDateTime since);
}