package com.shipment.notificationservice.kafka.event;

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

    private String shipmentId;
    private String trackingNumber;

    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private String carrierCode;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private long transitDays;
    private boolean deliveredOnTime;

    private LocalDateTime deliveredAt;
}
