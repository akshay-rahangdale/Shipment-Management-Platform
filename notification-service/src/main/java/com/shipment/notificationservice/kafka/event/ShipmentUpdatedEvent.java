package com.shipment.notificationservice.kafka.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentUpdatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;

    private UUID shipmentId;
    private String trackingNumber;

    private String previousStatus;
    private String currentStatus;
    private boolean statusChanged;

    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private LocalDateTime estimatedDelivery;
    private boolean slaAtRisk;
    private String updateReason;
}