package com.shipment.shipmentservice.kafka.event;

import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ShipmentUpdatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;

    private UUID shipmentId;
    private String trackingNumber;

    private ShipmentStatus previousStatus;
    private ShipmentStatus currentStatus;
    private boolean statusChanged;

    private String updatedField;
    private String updateReason;

    private UUID recipientId;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private String destinationAddress;
    private LocalDateTime estimatedDelivery;
    private boolean slaAtRisk;

    private LocalDateTime updatedAt;
}