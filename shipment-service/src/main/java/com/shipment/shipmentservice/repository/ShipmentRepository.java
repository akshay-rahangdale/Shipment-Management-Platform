package com.shipment.shipmentservice.repository;

import com.shipment.shipmentservice.model.Shipment;
import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    Page<Shipment> findBySenderId(UUID senderId, Pageable pageable);

    Page<Shipment> findByRecipientId(UUID recipientId, Pageable pageable);

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    Page<Shipment> findByCarrierId(UUID carrierId, Pageable pageable);

    @Query("""
        SELECT s FROM Shipment s
        WHERE s.status NOT IN :terminalStatuses
          AND s.estimatedDelivery <= :threshold
        """)
    List<Shipment> findAtRiskShipments(List<ShipmentStatus> terminalStatuses, LocalDateTime threshold);

    @Query("""
        SELECT s FROM Shipment s
        JOIN FETCH s.sender
        JOIN FETCH s.recipient
        JOIN FETCH s.carrier
        WHERE s.trackingNumber = :trackingNumber
        """)
    Optional<Shipment> findByTrackingNumberWithDetails(String trackingNumber);

    @Query("""
        SELECT s FROM Shipment s
        WHERE s.carrier.id = :carrierId
          AND s.status = :status
          AND s.createdAt >= :since
        """)
    List<Shipment> findByCarrierIdAndStatusSince(UUID carrierId, ShipmentStatus status, LocalDateTime since);

    @Modifying
    @Query("UPDATE Shipment s SET s.status = :status, s.updatedAt = :now WHERE s.id = :id")
    int updateStatus(UUID id, ShipmentStatus status, LocalDateTime now);

    @Modifying
    @Query("""
        UPDATE Shipment s
        SET s.status = :status,
            s.actualDelivery = :deliveredAt,
            s.updatedAt = :deliveredAt
        WHERE s.id = :id
        """)
    int markAsDelivered(UUID id, ShipmentStatus status, LocalDateTime deliveredAt);
}