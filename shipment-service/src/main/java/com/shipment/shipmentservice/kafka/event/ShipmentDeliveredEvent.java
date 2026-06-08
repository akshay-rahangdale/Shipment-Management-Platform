package com.shipment.shipmentservice.kafka.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ShipmentDeliveredEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;

    private UUID shipmentId;
    private String trackingNumber;

    private UUID recipientId;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private UUID carrierId;
    private String carrierCode;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private long transitDays;
    private boolean deliveredOnTime;

    private LocalDateTime deliveredAt;
}