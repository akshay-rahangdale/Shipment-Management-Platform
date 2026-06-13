package com.shipment.notificationservice.kafka.event;

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

    private String senderName;
    private String senderEmail;

    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private String carrierName;
    private String originAddress;
    private String destinationAddress;

    private LocalDateTime estimatedDelivery;
}