package com.shipment.trackingservice.kafka.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentCreatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;

    private UUID shipmentId;
    private String trackingNumber;

    private UUID senderId;
    private String senderName;

    private UUID recipientId;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private UUID carrierId;
    private String carrierCode;
    private String carrierName;

    private String status;
    private String originAddress;
    private String destinationAddress;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
}